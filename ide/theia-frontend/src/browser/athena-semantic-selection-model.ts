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

type AthenaSemanticInspectionEntry = {
    semanticId: string;
    label: string;
    kind: 'component' | 'port' | 'connection';
    sourceRange: Range;
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

/** Resolves the most specific semantic subject that contains the current source-editor selection. */
export function resolveSemanticSelectionFromSourceRange(
    inspection: AthenaSemanticInspectionPayload | undefined,
    sourceUri: string,
    selectionRange: Range
): AthenaActiveSemanticSelection | undefined {
    if (!inspection || inspection.uri !== sourceUri) {
        return undefined;
    }

    const matchingEntry = inspectionEntries(inspection)
        .filter(entry => rangeContainsRange(entry.sourceRange, selectionRange))
        .sort((left, right) => rangeWeight(left.sourceRange) - rangeWeight(right.sourceRange))[0];

    return matchingEntry
        ? {
            semanticId: matchingEntry.semanticId,
            label: matchingEntry.label,
            kind: matchingEntry.kind,
            sourceUri: inspection.uri,
            sourceRange: matchingEntry.sourceRange
        }
        : undefined;
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

/** Returns whether the current graph snapshot already exposes the canonical semantic id. */
export function graphContainsSemanticId(
    diagram: AthenaProjectionSelectionCarrier | undefined,
    semanticId: string
): boolean {
    if (!diagram) {
        return false;
    }
    const existsInNodes = diagram.graph.nodes.some(node => node.id === semanticId);
    const existsInEdges = diagram.graph.edges.some(edge => edge.id === semanticId);
    return existsInNodes || existsInEdges;
}

/** Keeps transient selection only while the refreshed projection still contains the same canonical semantic id. */
export function retainSelectionIfPresent(
    diagram: AthenaProjectionSelectionCarrier,
    selection: AthenaActiveSemanticSelection | undefined
): AthenaActiveSemanticSelection | undefined {
    if (!selection) {
        return undefined;
    }

    return graphContainsSemanticId(diagram, selection.semanticId) ? selection : undefined;
}

function inspectionEntries(inspection: AthenaSemanticInspectionPayload): AthenaSemanticInspectionEntry[] {
    return [
        ...inspection.components.map(component => ({
            semanticId: component.semanticId,
            label: component.name,
            kind: 'component' as const,
            sourceRange: component.sourceRange
        })),
        ...inspection.ports.map(port => ({
            semanticId: port.semanticId,
            label: port.path,
            kind: 'port' as const,
            sourceRange: port.sourceRange
        })),
        ...inspection.connections.map(connection => ({
            semanticId: connection.semanticId,
            label: `${connection.fromPath} -> ${connection.toPath}`,
            kind: 'connection' as const,
            sourceRange: connection.sourceRange
        }))
    ];
}

function rangeContainsRange(container: Range, candidate: Range): boolean {
    return comparePosition(container.start, candidate.start) <= 0 &&
        comparePosition(container.end, candidate.end) >= 0;
}

function comparePosition(
    left: { line: number; character: number },
    right: { line: number; character: number }
): number {
    if (left.line !== right.line) {
        return left.line - right.line;
    }
    return left.character - right.character;
}

function rangeWeight(range: Range): number {
    const lineSpan = Math.max(range.end.line - range.start.line, 0);
    const characterSpan = lineSpan === 0
        ? Math.max(range.end.character - range.start.character, 0)
        : Math.max(range.end.character + range.start.character, 0);
    return (lineSpan * 10_000) + characterSpan;
}
