"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.resolveSemanticSelectionFromInspection = resolveSemanticSelectionFromInspection;
exports.matchesSemanticScmContext = matchesSemanticScmContext;
exports.selectableSemanticIdFromScmContext = selectableSemanticIdFromScmContext;
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
/** Keeps transient selection only while the refreshed projection still contains the same canonical semantic id. */
function retainSelectionIfPresent(diagram, selection) {
    if (!selection) {
        return undefined;
    }
    const existsInNodes = diagram.graph.nodes.some(node => node.id === selection.semanticId);
    const existsInEdges = diagram.graph.edges.some(edge => edge.id === selection.semanticId);
    return existsInNodes || existsInEdges ? selection : undefined;
}
//# sourceMappingURL=athena-semantic-selection-model.js.map