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

export type AthenaProjectionEndpointAliasResolution = {
    semanticId: string;
    status: 'resolved' | 'ambiguous' | 'unresolved';
    endpointIds: string[];
    anchorIds: string[];
    connectionIds: string[];
};

export type AthenaProjectionRelatedSubjectResolution = {
    semanticId: string;
    relatedSemanticId: string;
    relation: 'owner' | 'owned-port' | 'connection' | 'source-port' | 'target-port';
};

export type AthenaRenderedSelectionSubject = {
    id?: string;
    type?: 'node' | 'edge';
    kind?: 'component' | 'label';
    semanticId?: string;
    endpointId?: string;
    anchorId?: string;
    portSemanticId?: string;
    role?: 'source' | 'target';
};

export type AthenaRenderedSelectionTarget = {
    semanticId: string;
    occurrenceId?: string;
    endpointId?: string;
    anchorId?: string;
    subjectKind: 'component' | 'port' | 'connection';
    source: 'node' | 'edge' | 'terminal';
};

type AthenaSemanticScmContextCarrier = {
    subjectIdentity?: string;
    factReferences: AthenaSemanticFactReferencePayload[];
};

type AthenaProjectionSelectionCarrier = {
    activeSheetId?: string;
    crossReferences?: Array<{
        semanticId: string;
        kind: string;
        sheetIds: string[];
        occurrenceIds: string[];
    }>;
    sheets?: Array<{
        sheetId: string;
        subjectSemanticIds?: string[];
    }>;
    electricalAnchors?: Array<{
        anchorId: string;
        portSemanticId: string;
        ownerSemanticId: string;
        nodeId: string;
        labelId?: string;
    }>;
    electricalConnectionEndpoints?: Array<{
        endpointId: string;
        projectionConnectionId: string;
        connectionSemanticId: string;
        endpointRole: string;
        portSemanticId: string;
        anchorId: string;
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

/** Converts a rendered projection subject into the canonical id consumed by cross-surface selection. */
export function resolveRenderedSelectionTarget(
    subject: AthenaRenderedSelectionSubject,
): AthenaRenderedSelectionTarget | undefined {
    const semanticId = subject.portSemanticId ?? subject.semanticId;
    const subjectKind = semanticId ? subjectKindFromSemanticId(semanticId) : undefined;
    if (!semanticId || !subjectKind) {
        return undefined;
    }

    return {
        semanticId,
        subjectKind,
        source: subject.portSemanticId ? 'terminal' : subject.type === 'edge' ? 'edge' : 'node',
        ...(subject.id ? { occurrenceId: subject.id } : {}),
        ...(subject.endpointId ? { endpointId: subject.endpointId } : {}),
        ...(subject.anchorId ? { anchorId: subject.anchorId } : {}),
    };
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
    return resolveProjectionOccurrence(diagram, semanticId).status !== 'unresolved' ||
        projectionSheetsContainSemanticId(diagram, semanticId) ||
        resolveProjectionEndpointAlias(diagram, semanticId).status !== 'unresolved';
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

/** Resolves governed endpoint and anchor aliases for one canonical port selection. */
export function resolveProjectionEndpointAlias(
    diagram: AthenaProjectionSelectionCarrier | undefined,
    semanticId: string
): AthenaProjectionEndpointAliasResolution {
    if (!diagram) {
        return {
            semanticId,
            status: 'unresolved',
            endpointIds: [],
            anchorIds: [],
            connectionIds: []
        };
    }

    const endpointMatches = (diagram.electricalConnectionEndpoints ?? [])
        .filter(endpoint => endpoint.portSemanticId === semanticId);
    const anchorIds = Array.from(new Set([
        ...(diagram.electricalAnchors ?? [])
            .filter(anchor => anchor.portSemanticId === semanticId)
            .map(anchor => anchor.anchorId),
        ...endpointMatches.map(endpoint => endpoint.anchorId),
    ]));
    const endpointIds = endpointMatches.map(endpoint => endpoint.endpointId);
    const connectionIds = Array.from(new Set(endpointMatches.map(endpoint => endpoint.connectionSemanticId)));
    const totalAliases = endpointIds.length + anchorIds.length;
    const status = totalAliases === 0
        ? 'unresolved'
        : totalAliases === 1
            ? 'resolved'
            : 'ambiguous';
    return {
        semanticId,
        status,
        endpointIds,
        anchorIds,
        connectionIds
    };
}

/** Resolves governed related semantic subjects without inventing a renderer-owned navigation graph. */
export function resolveProjectionRelatedSubjects(
    diagram: AthenaProjectionSelectionCarrier | undefined,
    semanticId: string,
): AthenaProjectionRelatedSubjectResolution[] {
    if (!diagram) {
        return [];
    }

    const related = new Map<string, AthenaProjectionRelatedSubjectResolution>();
    const add = (
        relatedSemanticId: string | undefined,
        relation: AthenaProjectionRelatedSubjectResolution['relation'],
    ): void => {
        if (!relatedSemanticId || relatedSemanticId === semanticId) {
            return;
        }
        related.set(`${relation}:${relatedSemanticId}`, {
            semanticId,
            relatedSemanticId,
            relation,
        });
    };

    const anchors = diagram.electricalAnchors ?? [];
    const endpoints = diagram.electricalConnectionEndpoints ?? [];

    if (semanticId.startsWith('component:')) {
        for (const anchor of anchors) {
            if (anchor.ownerSemanticId === semanticId) {
                add(anchor.portSemanticId, 'owned-port');
            }
        }
    }

    if (semanticId.startsWith('port:')) {
        for (const anchor of anchors) {
            if (anchor.portSemanticId === semanticId) {
                add(anchor.ownerSemanticId, 'owner');
            }
        }
        for (const endpoint of endpoints) {
            if (endpoint.portSemanticId === semanticId) {
                add(endpoint.connectionSemanticId, 'connection');
            }
        }
    }

    if (semanticId.startsWith('connection:')) {
        for (const endpoint of endpoints) {
            if (endpoint.connectionSemanticId === semanticId) {
                add(endpoint.portSemanticId, endpoint.endpointRole === 'target' ? 'target-port' : 'source-port');
            }
        }
    }

    return [...related.values()];
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

function projectionSheetsContainSemanticId(
    diagram: AthenaProjectionSelectionCarrier | undefined,
    semanticId: string
): boolean {
    if (!diagram?.sheets?.length) {
        return false;
    }
    const sheets = diagram.activeSheetId
        ? diagram.sheets.filter(sheet => sheet.sheetId === diagram.activeSheetId)
        : diagram.sheets;
    return sheets.some(sheet => sheet.subjectSemanticIds?.includes(semanticId));
}

function subjectKindFromSemanticId(
    semanticId: string,
): AthenaRenderedSelectionTarget['subjectKind'] | undefined {
    if (semanticId.startsWith('component:')) {
        return 'component';
    }
    if (semanticId.startsWith('port:')) {
        return 'port';
    }
    if (semanticId.startsWith('connection:')) {
        return 'connection';
    }
    return undefined;
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
