import {
    AthenaGLSPDiagram,
    AthenaGLSPGraph,
    AthenaGLSPProjectionSource,
} from './athena-glsp-diagram-model';

/** Converts Athena-owned projection payloads into a disposable GLSP-shaped graph snapshot. */
export function translateProjectionSessionToGLSPDiagram(
    projection: AthenaGLSPProjectionSource,
): AthenaGLSPDiagram {
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
        supportedViews: normalizeArray(projection.supportedViews).map(view => ({ ...view })),
        governedCommands: normalizeArray(projection.governedCommands).map(command => ({
            ...command,
            requiredArguments: [...normalizeArray(command.requiredArguments)],
        })),
        unavailableReason: projection.unavailableReason,
        diagnostics: normalizeArray(projection.diagnostics).map(diagnostic => ({ ...diagnostic })),
        graph: toGraph(projection),
    };
}

function toGraph(projection: AthenaGLSPProjectionSource): AthenaGLSPGraph {
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
                id: component.semanticId,
                type: 'node' as const,
                kind: 'component' as const,
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
                id: label.semanticId,
                type: 'node' as const,
                kind: 'label' as const,
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
            id: connection.semanticId,
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

function normalizeArray<T>(value: readonly T[] | T[] | undefined): T[] {
    return Array.isArray(value) ? [...value] : [];
}
