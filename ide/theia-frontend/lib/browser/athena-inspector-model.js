"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildAthenaInspectorComponentSnapshot = buildAthenaInspectorComponentSnapshot;
exports.createAthenaInspectorEditDraft = createAthenaInspectorEditDraft;
exports.buildAthenaInspectorDraftChanges = buildAthenaInspectorDraftChanges;
const athena_guided_connection_model_1 = require("./athena-guided-connection-model");
function buildAthenaInspectorComponentSnapshot(args) {
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
function createAthenaInspectorEditDraft(snapshot) {
    return {
        semanticId: snapshot.semanticId,
        name: snapshot.name,
        label: snapshot.label,
        description: snapshot.description,
        preferredImplementationId: snapshot.implementationId,
    };
}
function buildAthenaInspectorDraftChanges(args) {
    const { snapshot, draft } = args;
    const changes = {};
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
function resolveSelectedComponentSemanticId(selectedSemanticId, knowledge) {
    if (knowledge.components.some(component => component.semanticSubjectId === selectedSemanticId)) {
        return selectedSemanticId;
    }
    return knowledge.semanticPorts.find(port => port.portSemanticId === selectedSemanticId)?.ownerSemanticId;
}
function buildPortSnapshots(args) {
    const { inspection, knowledge, componentSemanticId, selectedSemanticId } = args;
    const connectionStates = (0, athena_guided_connection_model_1.buildAthenaPortConnectionStateMap)(inspection);
    return knowledge.semanticPorts
        .filter(port => port.ownerSemanticId === componentSemanticId)
        .map(port => toInspectorPortSnapshot(port, inspection, selectedSemanticId, connectionStates))
        .sort((left, right) => left.label.localeCompare(right.label));
}
function toInspectorPortSnapshot(port, inspection, selectedSemanticId, connectionStates) {
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
function buildPhysicalTraitSnapshots(physicalTraits, componentSemanticId) {
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
function fallbackName(semanticId) {
    return semanticId.replace(/^component:/, '') || semanticId;
}
function authoredPropertyValue(inspectionComponent, ...names) {
    if (!inspectionComponent) {
        return undefined;
    }
    return inspectionComponent.authoredProperties.find(property => names.includes(property.name))?.valueText;
}
function resolveCurrentImplementationId(knowledgeComponent, knowledge) {
    if (knowledgeComponent.implementationId) {
        return knowledgeComponent.implementationId;
    }
    return knowledge.availableComponents
        .flatMap(component => component.implementations)
        .find(implementation => implementation.vendorPartNumber === knowledgeComponent.vendorPartNumber)
        ?.implementationId;
}
function buildImplementationOptions(knowledgeComponent, knowledge, currentImplementationId) {
    const knownOptions = knowledge.availableComponents
        .find(component => component.conceptId === knowledgeComponent.conceptId)
        ?.implementations
        .map(implementation => toImplementationOption(implementation, currentImplementationId))
        ?? [];
    if (currentImplementationId &&
        !knownOptions.some(option => option.implementationId === currentImplementationId) &&
        knowledgeComponent.vendorPartNumber) {
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
function toImplementationOption(implementation, currentImplementationId) {
    return {
        implementationId: implementation.implementationId,
        displayName: implementation.displayName,
        vendorId: implementation.vendorId,
        vendorPartNumber: implementation.vendorPartNumber,
        summary: implementation.summary,
        selected: implementation.implementationId === currentImplementationId,
    };
}
function normalizeEditableValue(value) {
    const normalizedValue = value?.trim();
    return normalizedValue ? normalizedValue : undefined;
}
//# sourceMappingURL=athena-inspector-model.js.map