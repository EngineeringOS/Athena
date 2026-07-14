import type {
    AthenaAvailableComponentImplementationPayload,
    AthenaComponentKnowledgeSessionPayload,
    AthenaResolvedComponentKnowledgePayload,
    AthenaResolvedPhysicalTraitPayload,
    AthenaResolvedSemanticPortPayload
} from './athena-component-knowledge-protocol';
import type {
    AthenaSemanticInspectionComponent,
    AthenaSemanticInspectionPayload,
} from './athena-lsp-editor-bridge-service';
import type { AthenaActiveSemanticSelection } from './athena-semantic-selection-model';
import { buildAthenaPortConnectionStateMap } from './athena-guided-connection-model';

export type AthenaInspectorPortSnapshot = {
    semanticId: string;
    label: string;
    direction: string;
    signalFamilyId: string;
    roleId: string;
    portTypeId: string;
    protocolIds: string[];
    selected: boolean;
    connectionIds: string[];
    connectedPaths: string[];
    connectedPeerSemanticIds: string[];
};

export type AthenaInspectorPhysicalTraitSnapshot = {
    displayName: string;
    widthMillimeters: number;
    heightMillimeters: number;
    depthMillimeters: number;
    mountingTypeId: string;
    installationMarkerIds: string[];
};

export type AthenaInspectorImplementationOption = {
    implementationId: string;
    displayName: string;
    vendorId: string;
    vendorPartNumber: string;
    summary?: string;
    selected: boolean;
};

export type AthenaInspectorEditDraft = {
    semanticId: string;
    name: string;
    label: string;
    description: string;
    preferredImplementationId?: string;
};

export type AthenaInspectorDraftChanges = {
    name?: string;
    label?: string;
    description?: string;
    preferredImplementationId?: string;
};

export type AthenaInspectorComponentSnapshot = {
    semanticId: string;
    name: string;
    label: string;
    description: string;
    kind: string;
    conceptId: string;
    conceptDisplayName: string;
    authoredComponentReference: string;
    implementationId?: string;
    vendorId?: string;
    vendorPartNumber?: string;
    implementationOptions: AthenaInspectorImplementationOption[];
    ports: AthenaInspectorPortSnapshot[];
    physicalTraits: AthenaInspectorPhysicalTraitSnapshot[];
};

export function buildAthenaInspectorComponentSnapshot(args: {
    inspection: AthenaSemanticInspectionPayload | undefined;
    knowledge: AthenaComponentKnowledgeSessionPayload | undefined;
    selection: AthenaActiveSemanticSelection | undefined;
}): AthenaInspectorComponentSnapshot | undefined {
    const { inspection, knowledge, selection } = args;
    if (!inspection || !knowledge || knowledge.status !== 'ready' || !selection) {
        return undefined;
    }

    const componentSemanticId = resolveSelectedComponentSemanticId(selection.semanticId, knowledge);
    if (!componentSemanticId) {
        return undefined;
    }

    const knowledgeComponent = knowledge.components.find(component => component.semanticSubjectId === componentSemanticId);
    if (!knowledgeComponent) {
        return undefined;
    }

    const inspectionComponent = inspection.components.find(component => component.semanticId === componentSemanticId);
    const implementationId = resolveCurrentImplementationId(knowledgeComponent, knowledge);
    return {
        semanticId: componentSemanticId,
        name: inspectionComponent?.name ?? fallbackName(componentSemanticId),
        label: authoredPropertyValue(inspectionComponent, 'label') ?? inspectionComponent?.name ?? fallbackName(componentSemanticId),
        description: authoredPropertyValue(inspectionComponent, 'description', 'note') ?? '',
        kind: inspectionComponent?.kind ?? knowledgeComponent.conceptDisplayName,
        conceptId: knowledgeComponent.conceptId,
        conceptDisplayName: knowledgeComponent.conceptDisplayName,
        authoredComponentReference: knowledgeComponent.authoredComponentReference,
        implementationId,
        vendorId: knowledgeComponent.vendorId,
        vendorPartNumber: knowledgeComponent.vendorPartNumber,
        implementationOptions: buildImplementationOptions(knowledgeComponent, knowledge, implementationId),
        ports: buildPortSnapshots({
            inspection,
            knowledge,
            componentSemanticId,
            selectedSemanticId: selection.semanticId,
        }),
        physicalTraits: buildPhysicalTraitSnapshots(knowledge.physicalTraits, componentSemanticId),
    };
}

export function createAthenaInspectorEditDraft(
    snapshot: AthenaInspectorComponentSnapshot,
): AthenaInspectorEditDraft {
    return {
        semanticId: snapshot.semanticId,
        name: snapshot.name,
        label: snapshot.label,
        description: snapshot.description,
        preferredImplementationId: snapshot.implementationId,
    };
}

export function buildAthenaInspectorDraftChanges(args: {
    snapshot: AthenaInspectorComponentSnapshot;
    draft: AthenaInspectorEditDraft;
}): AthenaInspectorDraftChanges {
    const { snapshot, draft } = args;
    const changes: AthenaInspectorDraftChanges = {};
    const name = normalizeEditableValue(draft.name);
    if (name && name !== snapshot.name) {
        changes.name = name;
    }
    const label = normalizeEditableValue(draft.label);
    if (label && label !== snapshot.label) {
        changes.label = label;
    }
    const description = normalizeEditableValue(draft.description);
    if (description && description !== snapshot.description) {
        changes.description = description;
    }
    const preferredImplementationId = normalizeEditableValue(draft.preferredImplementationId);
    if (preferredImplementationId && preferredImplementationId !== snapshot.implementationId) {
        changes.preferredImplementationId = preferredImplementationId;
    }
    return changes;
}

function resolveSelectedComponentSemanticId(
    selectedSemanticId: string,
    knowledge: AthenaComponentKnowledgeSessionPayload
): string | undefined {
    if (knowledge.components.some(component => component.semanticSubjectId === selectedSemanticId)) {
        return selectedSemanticId;
    }
    return knowledge.semanticPorts.find(port => port.portSemanticId === selectedSemanticId)?.ownerSemanticId;
}

function buildPortSnapshots(args: {
    inspection: AthenaSemanticInspectionPayload;
    knowledge: AthenaComponentKnowledgeSessionPayload;
    componentSemanticId: string;
    selectedSemanticId: string;
}): AthenaInspectorPortSnapshot[] {
    const { inspection, knowledge, componentSemanticId, selectedSemanticId } = args;
    const connectionStates = buildAthenaPortConnectionStateMap(inspection);
    return knowledge.semanticPorts
        .filter(port => port.ownerSemanticId === componentSemanticId)
        .map(port => toInspectorPortSnapshot(port, inspection, selectedSemanticId, connectionStates))
        .sort((left, right) => left.label.localeCompare(right.label));
}

function toInspectorPortSnapshot(
    port: AthenaResolvedSemanticPortPayload,
    inspection: AthenaSemanticInspectionPayload,
    selectedSemanticId: string,
    connectionStates: Map<string, {
        connectionIds: string[];
        connectedPaths: string[];
        connectedPeerSemanticIds: string[];
    }>,
): AthenaInspectorPortSnapshot {
    const inspectionPort = inspection.ports.find(candidate => candidate.semanticId === port.portSemanticId);
    const connectionState = connectionStates.get(port.portSemanticId);
    return {
        semanticId: port.portSemanticId,
        label: inspectionPort?.path ?? port.roleId,
        direction: port.direction,
        signalFamilyId: port.signalFamilyId,
        roleId: port.roleId,
        portTypeId: port.portTypeId,
        protocolIds: [...port.protocolIds],
        selected: selectedSemanticId === port.portSemanticId,
        connectionIds: [...(connectionState?.connectionIds ?? [])],
        connectedPaths: [...(connectionState?.connectedPaths ?? [])],
        connectedPeerSemanticIds: [...(connectionState?.connectedPeerSemanticIds ?? [])],
    };
}

function buildPhysicalTraitSnapshots(
    physicalTraits: AthenaResolvedPhysicalTraitPayload[],
    componentSemanticId: string,
): AthenaInspectorPhysicalTraitSnapshot[] {
    return physicalTraits
        .filter(trait => trait.semanticSubjectId === componentSemanticId)
        .map(trait => ({
            displayName: trait.displayName,
            widthMillimeters: trait.widthMillimeters,
            heightMillimeters: trait.heightMillimeters,
            depthMillimeters: trait.depthMillimeters,
            mountingTypeId: trait.mountingTypeId,
            installationMarkerIds: [...trait.installationMarkerIds],
        }))
        .sort((left, right) => left.displayName.localeCompare(right.displayName));
}

function fallbackName(semanticId: string): string {
    return semanticId.replace(/^component:/, '') || semanticId;
}

function authoredPropertyValue(
    inspectionComponent: AthenaSemanticInspectionComponent | undefined,
    ...names: string[]
): string | undefined {
    if (!inspectionComponent) {
        return undefined;
    }
    return inspectionComponent.authoredProperties.find(property => names.includes(property.name))?.valueText;
}

function resolveCurrentImplementationId(
    knowledgeComponent: AthenaResolvedComponentKnowledgePayload,
    knowledge: AthenaComponentKnowledgeSessionPayload,
): string | undefined {
    if (knowledgeComponent.implementationId) {
        return knowledgeComponent.implementationId;
    }
    return knowledge.availableComponents
        .flatMap(component => component.implementations)
        .find(implementation => implementation.vendorPartNumber === knowledgeComponent.vendorPartNumber)
        ?.implementationId;
}

function buildImplementationOptions(
    knowledgeComponent: AthenaResolvedComponentKnowledgePayload,
    knowledge: AthenaComponentKnowledgeSessionPayload,
    currentImplementationId: string | undefined,
): AthenaInspectorImplementationOption[] {
    const knownOptions = knowledge.availableComponents
        .find(component => component.conceptId === knowledgeComponent.conceptId)
        ?.implementations
        .map(implementation => toImplementationOption(implementation, currentImplementationId))
        ?? [];
    if (
        currentImplementationId &&
        !knownOptions.some(option => option.implementationId === currentImplementationId) &&
        knowledgeComponent.vendorPartNumber
    ) {
        knownOptions.push({
            implementationId: currentImplementationId,
            displayName: knowledgeComponent.vendorPartNumber,
            vendorId: knowledgeComponent.vendorId ?? 'vendor',
            vendorPartNumber: knowledgeComponent.vendorPartNumber,
            summary: 'Resolved implementation is not present in the active available component catalog.',
            selected: true,
        });
    }
    return knownOptions;
}

function toImplementationOption(
    implementation: AthenaAvailableComponentImplementationPayload,
    currentImplementationId: string | undefined,
): AthenaInspectorImplementationOption {
    return {
        implementationId: implementation.implementationId,
        displayName: implementation.displayName,
        vendorId: implementation.vendorId,
        vendorPartNumber: implementation.vendorPartNumber,
        summary: implementation.summary,
        selected: implementation.implementationId === currentImplementationId,
    };
}

function normalizeEditableValue(value: string | undefined): string | undefined {
    const normalizedValue = value?.trim();
    return normalizedValue ? normalizedValue : undefined;
}
