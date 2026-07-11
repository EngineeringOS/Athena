"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.resolveSemanticSelectionFromInspection = resolveSemanticSelectionFromInspection;
exports.resolveSemanticSelectionFromSourceRange = resolveSemanticSelectionFromSourceRange;
exports.matchesSemanticScmContext = matchesSemanticScmContext;
exports.selectableSemanticIdFromScmContext = selectableSemanticIdFromScmContext;
exports.graphContainsSemanticId = graphContainsSemanticId;
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
    if (!diagram) {
        return false;
    }
    const existsInNodes = diagram.graph.nodes.some(node => node.id === semanticId);
    const existsInEdges = diagram.graph.edges.some(edge => edge.id === semanticId);
    return existsInNodes || existsInEdges;
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