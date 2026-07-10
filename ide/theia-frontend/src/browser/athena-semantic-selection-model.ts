import { Range } from '@theia/core/shared/vscode-languageserver-protocol';
import {
    AthenaSemanticFactReferencePayload,
    AthenaSemanticInspectionPayload
} from './athena-lsp-editor-bridge-service';

/** Transient cross-surface semantic selection used only for frontend synchronization. */
export type AthenaActiveSemanticSelection = {
    semanticId: string;
    label?: string;
    kind?: 'component' | 'port' | 'connection';
    sourceUri?: string;
    sourceRange?: Range;
};

type AthenaSemanticScmContextCarrier = {
    subjectIdentity?: string;
    factReferences: AthenaSemanticFactReferencePayload[];
};

type AthenaProjectionSelectionCarrier = {
    graph: {
        nodes: Array<{ id: string }>;
        edges: Array<{ id: string }>;
    };
};

/** Resolves one canonical semantic selection from the current inspection payload, if the document publishes it. */
export function resolveSemanticSelectionFromInspection(
    inspection: AthenaSemanticInspectionPayload | undefined,
    semanticId: string
): AthenaActiveSemanticSelection | undefined {
    if (!inspection) {
        return undefined;
    }

    const component = inspection.components.find(entry => entry.semanticId === semanticId);
    if (component) {
        return {
            semanticId,
            label: component.name,
            kind: 'component',
            sourceUri: inspection.uri,
            sourceRange: component.sourceRange
        };
    }

    const port = inspection.ports.find(entry => entry.semanticId === semanticId);
    if (port) {
        return {
            semanticId,
            label: port.path,
            kind: 'port',
            sourceUri: inspection.uri,
            sourceRange: port.sourceRange
        };
    }

    const connection = inspection.connections.find(entry => entry.semanticId === semanticId);
    if (connection) {
        return {
            semanticId,
            label: `${connection.fromPath} -> ${connection.toPath}`,
            kind: 'connection',
            sourceUri: inspection.uri,
            sourceRange: connection.sourceRange
        };
    }

    return undefined;
}

/** Reuses M6 semantic SCM subject-identity vocabulary to determine whether one SCM context matches the active selection. */
export function matchesSemanticScmContext(
    carrier: AthenaSemanticScmContextCarrier,
    semanticId: string | undefined
): boolean {
    if (!semanticId) {
        return false;
    }
    if (carrier.subjectIdentity === semanticId) {
        return true;
    }
    return carrier.factReferences.some(reference => reference.subjectIdentity === semanticId);
}

/** Returns the first canonical semantic id that an SCM context can reveal back into the workbench. */
export function selectableSemanticIdFromScmContext(
    carrier: AthenaSemanticScmContextCarrier
): string | undefined {
    return carrier.subjectIdentity ?? carrier.factReferences.find(reference => reference.subjectIdentity)?.subjectIdentity;
}

/** Keeps transient selection only while the refreshed projection still contains the same canonical semantic id. */
export function retainSelectionIfPresent(
    diagram: AthenaProjectionSelectionCarrier,
    selection: AthenaActiveSemanticSelection | undefined
): AthenaActiveSemanticSelection | undefined {
    if (!selection) {
        return undefined;
    }

    const existsInNodes = diagram.graph.nodes.some(node => node.id === selection.semanticId);
    const existsInEdges = diagram.graph.edges.some(edge => edge.id === selection.semanticId);
    return existsInNodes || existsInEdges ? selection : undefined;
}
