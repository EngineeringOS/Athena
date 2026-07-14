import type { AthenaComponentKnowledgeSessionPayload, AthenaResolvedSemanticPortPayload } from './athena-component-knowledge-protocol';
import type {
    AthenaSemanticInspectionConnection,
    AthenaSemanticInspectionProperty,
    AthenaSemanticInspectionPayload,
    AthenaSemanticInspectionPort
} from './athena-lsp-editor-bridge-service';

export type AthenaCompatibleConnectionTarget = {
    semanticId: string;
    ownerSemanticId: string;
    label: string;
    direction: string;
    signalFamilyId: string;
    roleId: string;
    protocolIds: string[];
};

export type AthenaPortConnectionState = {
    connectionIds: string[];
    connectedPaths: string[];
    connectedPeerSemanticIds: string[];
};

export function buildAthenaCompatibleConnectionTargets(args: {
    knowledge: AthenaComponentKnowledgeSessionPayload | undefined;
    inspection: AthenaSemanticInspectionPayload | undefined;
    sourcePortSemanticId: string | undefined;
}): AthenaCompatibleConnectionTarget[] {
    const { knowledge, inspection, sourcePortSemanticId } = args;
    if (!inspection || !sourcePortSemanticId) {
        return [];
    }

    const sourcePort = resolvePortMeaning({
        knowledge,
        inspection,
        semanticId: sourcePortSemanticId,
    });
    if (!sourcePort) {
        return [];
    }

    return inspection.ports
        .map(port => resolvePortMeaning({
            knowledge,
            inspection,
            semanticId: port.semanticId,
        }))
        .filter((candidate): candidate is ResolvedPortMeaning => !!candidate)
        .filter(candidate => isAthenaCompatibleConnectionTarget(sourcePort, candidate))
        .map(candidate => ({
            semanticId: candidate.semanticId,
            ownerSemanticId: candidate.ownerSemanticId,
            label: candidate.label,
            direction: candidate.direction,
            signalFamilyId: candidate.signalFamilyId,
            roleId: candidate.roleId,
            protocolIds: [...candidate.protocolIds],
        }))
        .sort((left, right) => left.label.localeCompare(right.label));
}

export function buildAthenaPortConnectionStateMap(
    inspection: AthenaSemanticInspectionPayload | undefined,
): Map<string, AthenaPortConnectionState> {
    const states = new Map<string, AthenaPortConnectionState>();
    if (!inspection) {
        return states;
    }

    const portsByPath = new Map(inspection.ports.map(port => [port.path, port]));
    for (const connection of inspection.connections) {
        const sourcePort = portsByPath.get(connection.fromPath);
        const targetPort = portsByPath.get(connection.toPath);
        if (sourcePort && targetPort) {
            appendConnectionState(states, sourcePort, targetPort, connection);
            appendConnectionState(states, targetPort, sourcePort, connection);
        }
    }
    return states;
}

function appendConnectionState(
    states: Map<string, AthenaPortConnectionState>,
    sourcePort: AthenaSemanticInspectionPort,
    targetPort: AthenaSemanticInspectionPort,
    connection: AthenaSemanticInspectionConnection,
): void {
    const current = states.get(sourcePort.semanticId) ?? {
        connectionIds: [],
        connectedPaths: [],
        connectedPeerSemanticIds: [],
    };
    current.connectionIds.push(connection.semanticId);
    current.connectedPaths.push(targetPort.path);
    current.connectedPeerSemanticIds.push(targetPort.semanticId);
    states.set(sourcePort.semanticId, current);
}

function isAthenaCompatibleConnectionTarget(
    sourcePort: ResolvedPortMeaning,
    targetPort: ResolvedPortMeaning,
): boolean {
    if (sourcePort.semanticId === targetPort.semanticId) {
        return false;
    }
    if (sourcePort.signalFamilyId !== targetPort.signalFamilyId) {
        return false;
    }
    if (!directionsAreCompatible(sourcePort.direction, targetPort.direction)) {
        return false;
    }

    const sourceProtocols = new Set(sourcePort.protocolIds);
    const targetProtocols = new Set(targetPort.protocolIds);
    if (sourceProtocols.size === 0 && targetProtocols.size === 0) {
        return true;
    }
    for (const protocolId of sourceProtocols) {
        if (targetProtocols.has(protocolId)) {
            return true;
        }
    }
    return false;
}

type ResolvedPortMeaning = {
    semanticId: string;
    ownerSemanticId: string;
    label: string;
    direction: string;
    signalFamilyId: string;
    roleId: string;
    protocolIds: string[];
};

function resolvePortMeaning(args: {
    knowledge: AthenaComponentKnowledgeSessionPayload | undefined;
    inspection: AthenaSemanticInspectionPayload;
    semanticId: string;
}): ResolvedPortMeaning | undefined {
    const { knowledge, inspection, semanticId } = args;
    const inspectionPort = inspection.ports.find(port => port.semanticId === semanticId);
    const knowledgePort = knowledge?.semanticPorts.find(port => port.portSemanticId === semanticId);
    if (knowledgePort) {
        return {
            semanticId: knowledgePort.portSemanticId,
            ownerSemanticId: knowledgePort.ownerSemanticId,
            label: inspectionPort?.path ?? knowledgePort.roleId,
            direction: knowledgePort.direction,
            signalFamilyId: knowledgePort.signalFamilyId,
            roleId: knowledgePort.roleId,
            protocolIds: [...knowledgePort.protocolIds],
        };
    }
    if (!inspectionPort) {
        return undefined;
    }
    const ownerPath = inspectionPort.path.includes('.') ? inspectionPort.path.slice(0, inspectionPort.path.lastIndexOf('.')) : inspectionPort.path;
    const roleId = inspectionPort.path.includes('.') ? inspectionPort.path.slice(inspectionPort.path.lastIndexOf('.') + 1) : inspectionPort.path;
    return {
        semanticId: inspectionPort.semanticId,
        ownerSemanticId: `component:${ownerPath}`,
        label: inspectionPort.path,
        direction: authoredPortProperty(inspectionPort.authoredProperties, 'direction') ?? 'passive',
        signalFamilyId: authoredPortProperty(inspectionPort.authoredProperties, 'signal') ?? inspectionPort.properties,
        roleId,
        protocolIds: authoredPortProtocols(inspectionPort.authoredProperties),
    };
}

function authoredPortProperty(
    properties: AthenaSemanticInspectionProperty[] | undefined,
    name: string,
): string | undefined {
    return properties?.find(property => property.name === name)?.valueText;
}

function authoredPortProtocols(
    properties: AthenaSemanticInspectionProperty[] | undefined,
): string[] {
    const protocolValue = authoredPortProperty(properties, 'protocol') ?? authoredPortProperty(properties, 'protocols');
    return protocolValue
        ? protocolValue.split(/[,\s]+/).map(token => token.trim()).filter(Boolean)
        : [];
}

function directionsAreCompatible(sourceDirection: string, targetDirection: string): boolean {
    const source = sourceDirection.toLowerCase();
    const target = targetDirection.toLowerCase();
    switch (source) {
        case 'output':
        case 'out':
            return target === 'input' || target === 'in' || target === 'passive' || target === 'bidirectional';
        case 'input':
        case 'in':
            return target === 'output' || target === 'out' || target === 'passive' || target === 'bidirectional';
        case 'bidirectional':
            return target === 'output' || target === 'out' || target === 'input' || target === 'in' || target === 'bidirectional';
        case 'passive':
            return true;
        default:
            return false;
    }
}
