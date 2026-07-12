"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.translateProjectionSessionToGLSPDiagram = translateProjectionSessionToGLSPDiagram;
/** Converts Athena-owned projection payloads into a disposable GLSP-shaped graph snapshot. */
function translateProjectionSessionToGLSPDiagram(projection) {
    const readyProjection = projection.readyProjection;
    return {
        kind: 'athena-glsp-diagram',
        projectName: projection.projectName,
        semanticPath: projection.semanticPath,
        activeViewId: projection.activeViewId,
        status: projection.status,
        activeRenderContributions: normalizeArray(readyProjection?.activeRenderContributions).map(contribution => ({
            ...contribution,
            surfaceMappings: normalizeArray(contribution.surfaceMappings).map(mapping => ({
                surface: mapping.surface,
                tokens: { ...mapping.tokens },
            })),
        })) ?? [],
        supportedViews: normalizeArray(projection.supportedViews).map(view => ({
            ...view,
            ownershipContract: normalizeOwnershipContract(view.ownershipContract),
        })),
        governedCommands: normalizeArray(projection.governedCommands).map(command => ({
            ...command,
            requiredArguments: [...normalizeArray(command.requiredArguments)],
        })),
        activeSheetId: readyProjection?.activeSheetId,
        sheets: normalizeArray(readyProjection?.sheets).map(sheet => ({
            ...sheet,
            subjectSemanticIds: [...normalizeArray(sheet.subjectSemanticIds)],
        })),
        notationPack: readyProjection?.notationPack
            ? {
                ...readyProjection.notationPack,
                subjects: normalizeArray(readyProjection.notationPack.subjects).map(subject => ({
                    ...subject,
                    markerKeys: [...normalizeArray(subject.markerKeys)],
                })),
            }
            : undefined,
        crossReferences: normalizeArray(readyProjection?.crossReferences).map(crossReference => ({
            ...crossReference,
            sheetIds: [...normalizeArray(crossReference.sheetIds)],
            occurrenceIds: [...normalizeArray(crossReference.occurrenceIds)],
        })),
        unavailableReason: projection.unavailableReason,
        diagnostics: normalizeArray(projection.diagnostics).map(diagnostic => ({ ...diagnostic })),
        graph: toGraph(projection),
    };
}
function toGraph(projection) {
    const readyProjection = projection.readyProjection;
    if (projection.status !== 'ready' || !readyProjection) {
        return {
            id: `${projection.projectName}:${projection.activeViewId}`,
            type: 'graph',
            canvas: {
                width: 0,
                height: 0,
            },
            nodes: [],
            edges: [],
        };
    }
    return {
        id: `${projection.projectName}:${readyProjection.viewId}`,
        type: 'graph',
        canvas: {
            width: readyProjection.canvasWidth,
            height: readyProjection.canvasHeight,
        },
        nodes: [
            ...normalizeArray(readyProjection.components).map(component => ({
                id: component.projectionId,
                semanticId: component.semanticId,
                type: 'node',
                kind: 'component',
                label: component.label,
                position: {
                    x: component.x,
                    y: component.y,
                },
                size: {
                    width: component.width,
                    height: component.height,
                },
            })),
            ...normalizeArray(readyProjection.labels).map(label => ({
                id: label.projectionId,
                semanticId: label.semanticId,
                type: 'node',
                kind: 'label',
                label: label.label,
                position: {
                    x: label.x,
                    y: label.y,
                },
                size: {
                    width: label.width,
                    height: label.height,
                },
            })),
        ],
        edges: normalizeArray(readyProjection.connections).map(connection => ({
            id: connection.projectionId,
            semanticId: connection.semanticId,
            type: 'edge',
            sourcePoint: {
                x: connection.x1,
                y: connection.y1,
            },
            targetPoint: {
                x: connection.x2,
                y: connection.y2,
            },
        })),
    };
}
function normalizeArray(value) {
    return Array.isArray(value) ? [...value] : [];
}
function normalizeOwnershipContract(ownershipContract) {
    return {
        interactivity: ownershipContract?.interactivity ?? 'inspect_only',
        displayScopes: [...normalizeArray(ownershipContract?.displayScopes)],
        semanticCommandIds: [...normalizeArray(ownershipContract?.semanticCommandIds)],
        projectionCommandIds: [...normalizeArray(ownershipContract?.projectionCommandIds)],
        transientInteractionKinds: [...normalizeArray(ownershipContract?.transientInteractionKinds)],
        persistedProjectionMetadataKeys: [...normalizeArray(ownershipContract?.persistedProjectionMetadataKeys)],
    };
}
//# sourceMappingURL=athena-glsp-projection-adapter.js.map