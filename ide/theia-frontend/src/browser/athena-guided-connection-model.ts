import type {
    AthenaSemanticInspectionConnection,
    AthenaSemanticInspectionPayload,
    AthenaSemanticInspectionPort
} from './athena-lsp-editor-bridge-service';

export type AthenaPortConnectionState = {
    connectionIds: string[];
    connectedPaths: string[];
    connectedPeerSemanticIds: string[];
};

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
