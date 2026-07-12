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

export type AthenaProjectionOccurrenceResolution = {
    semanticId: string;
    status: 'resolved' | 'ambiguous' | 'unresolved';
    occurrenceIds: string[];
};

export type AthenaProjectionCrossReferenceResolution = {
    semanticId: string;
    kind: string;
    sheetIds: string[];
    occurrenceIds: string[];
};

type AthenaSemanticScmContextCarrier = {
    subjectIdentity?: string;
    factReferences: AthenaSemanticFactReferencePayload[];
};

type AthenaProjectionSelectionCarrier = {
    crossReferences?: Array<{
        semanticId: string;
        kind: string;
        sheetIds: string[];
        occurrenceIds: string[];
    }>;
    graph: {
        nodes: Array<{ id: string; semanticId?: string }>;
        edges: Array<{ id: string; semanticId?: string }>;
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
    return resolveProjectionOccurrence(diagram, semanticId).status !== 'unresolved';
}

/** Resolves repeated-reference status for one canonical semantic id inside current graph snapshot. */
export function resolveProjectionOccurrence(
    diagram: AthenaProjectionSelectionCarrier | undefined,
    semanticId: string
): AthenaProjectionOccurrenceResolution {
    if (!diagram) {
        return {
            semanticId,
            status: 'unresolved',
            occurrenceIds: []
        };
    }

    const occurrenceIds = [
        ...diagram.graph.nodes
            .filter(node => (node.semanticId ?? node.id) === semanticId)
            .map(node => node.id),
        ...diagram.graph.edges
            .filter(edge => (edge.semanticId ?? edge.id) === semanticId)
            .map(edge => edge.id)
    ];
    const status = occurrenceIds.length === 0
        ? 'unresolved'
        : occurrenceIds.length === 1
            ? 'resolved'
            : 'ambiguous';
    return {
        semanticId,
        status,
        occurrenceIds
    };
}

/** Returns published repeated-reference metadata for one canonical subject, if available. */
export function resolveProjectionCrossReference(
    diagram: AthenaProjectionSelectionCarrier | undefined,
    semanticId: string
): AthenaProjectionCrossReferenceResolution | undefined {
    const crossReference = diagram?.crossReferences?.find(reference => reference.semanticId === semanticId);
    if (!crossReference) {
        return undefined;
    }
    return {
        semanticId,
        kind: crossReference.kind,
        sheetIds: [...crossReference.sheetIds],
        occurrenceIds: [...crossReference.occurrenceIds]
    };
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
