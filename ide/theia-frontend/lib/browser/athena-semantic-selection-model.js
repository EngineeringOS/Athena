"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.resolveSemanticSelectionFromInspection = resolveSemanticSelectionFromInspection;
exports.resolveSemanticSelectionFromSourceRange = resolveSemanticSelectionFromSourceRange;
exports.matchesSemanticScmContext = matchesSemanticScmContext;
exports.selectableSemanticIdFromScmContext = selectableSemanticIdFromScmContext;
exports.graphContainsSemanticId = graphContainsSemanticId;
exports.resolveProjectionOccurrence = resolveProjectionOccurrence;
exports.resolveProjectionCrossReference = resolveProjectionCrossReference;
exports.resolveProjectionEndpointAlias = resolveProjectionEndpointAlias;
exports.resolveProjectionRelatedSubjects = resolveProjectionRelatedSubjects;
exports.retainSelectionIfPresent = retainSelectionIfPresent;
/** Resolves one canonical semantic selection from the current inspection payload, if the document publishes it. */
function resolveSemanticSelectionFromInspection(inspection, semanticId) {
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
function resolveSemanticSelectionFromSourceRange(inspection, sourceUri, selectionRange) {
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
function matchesSemanticScmContext(carrier, semanticId) {
    if (!semanticId) {
        return false;
    }
    if (carrier.subjectIdentity === semanticId) {
        return true;
    }
    return carrier.factReferences.some(reference => reference.subjectIdentity === semanticId);
}
/** Returns the first canonical semantic id that an SCM context can reveal back into the workbench. */
function selectableSemanticIdFromScmContext(carrier) {
    return carrier.subjectIdentity ?? carrier.factReferences.find(reference => reference.subjectIdentity)?.subjectIdentity;
}
/** Returns whether the current graph snapshot already exposes the canonical semantic id. */
function graphContainsSemanticId(diagram, semanticId) {
    return resolveProjectionOccurrence(diagram, semanticId).status !== 'unresolved' ||
        resolveProjectionEndpointAlias(diagram, semanticId).status !== 'unresolved';
}
/** Resolves repeated-reference status for one canonical semantic id inside current graph snapshot. */
function resolveProjectionOccurrence(diagram, semanticId) {
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
function resolveProjectionCrossReference(diagram, semanticId) {
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
function resolveProjectionEndpointAlias(diagram, semanticId) {
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
function resolveProjectionRelatedSubjects(diagram, semanticId) {
    if (!diagram) {
        return [];
    }
    const related = new Map();
    const add = (relatedSemanticId, relation) => {
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
function retainSelectionIfPresent(diagram, selection) {
    if (!selection) {
        return undefined;
    }
    return graphContainsSemanticId(diagram, selection.semanticId) ? selection : undefined;
}
function inspectionEntries(inspection) {
    return [
        ...inspection.components.map(component => ({
            semanticId: component.semanticId,
            label: component.name,
            kind: 'component',
            sourceRange: component.sourceRange
        })),
        ...inspection.ports.map(port => ({
            semanticId: port.semanticId,
            label: port.path,
            kind: 'port',
            sourceRange: port.sourceRange
        })),
        ...inspection.connections.map(connection => ({
            semanticId: connection.semanticId,
            label: `${connection.fromPath} -> ${connection.toPath}`,
            kind: 'connection',
            sourceRange: connection.sourceRange
        }))
    ];
}
function rangeContainsRange(container, candidate) {
    return comparePosition(container.start, candidate.start) <= 0 &&
        comparePosition(container.end, candidate.end) >= 0;
}
function comparePosition(left, right) {
    if (left.line !== right.line) {
        return left.line - right.line;
    }
    return left.character - right.character;
}
function rangeWeight(range) {
    const lineSpan = Math.max(range.end.line - range.start.line, 0);
    const characterSpan = lineSpan === 0
        ? Math.max(range.end.character - range.start.character, 0)
        : Math.max(range.end.character + range.start.character, 0);
    return (lineSpan * 10_000) + characterSpan;
}
//# sourceMappingURL=athena-semantic-selection-model.js.map