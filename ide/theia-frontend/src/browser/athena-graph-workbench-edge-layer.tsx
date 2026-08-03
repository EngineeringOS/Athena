import * as React from '@theia/core/shared/react';
import { AthenaGraphWorkbenchEdge } from './athena-graph-workbench-model';

type AthenaGraphWorkbenchEdgeLayerProps = {
    edges: AthenaGraphWorkbenchEdge[];
    selectedSemanticId: string | undefined;
    onSelectSemanticId: (semanticId: string) => void | Promise<unknown>;
};

/** Pure SVG edge layer for conductor-style Athena graph rendering. */
export function AthenaGraphWorkbenchEdgeLayer(
    props: AthenaGraphWorkbenchEdgeLayerProps,
): React.ReactNode {
    const { edges, selectedSemanticId, onSelectSemanticId } = props;
    return <>
        {edges.map(edge => {
            const sourceSelected = !!edge.sourcePortSemanticId && selectedSemanticId === edge.sourcePortSemanticId;
            const targetSelected = !!edge.targetPortSemanticId && selectedSemanticId === edge.targetPortSemanticId;
            const edgeSelected = selectedSemanticId === edge.semanticId || sourceSelected || targetSelected;
            const tooltipLabels = edge.connectionLabels.map(label => label.text).filter(Boolean).join(' | ');
            const lineStyle = edge.line?.style.toLowerCase();
            const strokeDasharray = lineStyle === 'dashed'
                ? '8 5'
                : lineStyle === 'dotted'
                    ? '2 5'
                    : undefined;
            const strokeColor = edge.line?.colorKey ? `var(--athena-${edge.line.colorKey.replace(/\./g, '-')})` : undefined;
            const sourceSpan = formatSourceSpan(
                edge.presentationConnector?.sourceSpan ?? edge.presentationConnector?.trace?.sourceSpan
            );

            return <React.Fragment key={edge.id}>
                <g
                    className='athena-graph-workbench__element'
                    data-athena-graph-interactive='true'
                    role='button'
                    tabIndex={0}
                    onClick={() => void onSelectSemanticId(edge.semanticId)}
                    onKeyDown={event => {
                        if (event.key !== 'Enter' && event.key !== ' ') {
                            return;
                        }
                        event.preventDefault();
                        void onSelectSemanticId(edge.semanticId);
                    }}
                >
                    <title>{tooltipLabels || edge.semanticId}</title>
                    <path
                        className={`athena-graph-workbench__edge-casing ${edgeSelected ? 'athena-graph-workbench__edge-casing--selected' : ''}`}
                        d={edge.path}
                        fill='none'
                        vectorEffect='non-scaling-stroke'
                    />
                    <path
                        className={`athena-graph-workbench__edge athena-graph-workbench__edge--${edge.conductorStyle} ${edgeSelected ? 'athena-graph-workbench__edge--selected' : ''}`}
                        d={edge.path}
                        data-athena-route-fact='true'
                        data-athena-route-id={edge.id}
                        data-athena-route-semantic-id={edge.semanticId}
                        data-athena-route-points={edge.routePoints.map(point => `${point.x},${point.y}`).join(';')}
                        data-athena-route-point-count={edge.routePoints.length}
                        data-athena-route-source-anchor-id={edge.terminals[0]?.anchorId ?? ''}
	                        data-athena-route-target-anchor-id={edge.terminals[1]?.anchorId ?? ''}
	                        data-athena-route-source-port-id={edge.sourcePortSemanticId ?? ''}
		                    data-athena-route-target-port-id={edge.targetPortSemanticId ?? ''}
		                    data-athena-presentation-route-id={edge.presentationConnector?.routeId ?? ''}
	                        data-athena-route-bundle-id={edge.presentationConnector?.bundleId ?? ''}
	                        data-athena-route-lane-id={edge.presentationConnector?.laneId ?? ''}
	                        data-athena-route-lane-route-ids={edge.presentationConnector?.laneRouteIds.join('|') ?? ''}
	                        data-athena-route-selected-channel-ids={edge.presentationConnector?.selectedChannelIds.join('|') ?? ''}
	                        data-athena-route-label-ids={edge.presentationConnector?.labels.map(label => label.labelId).join('|') ?? ''}
	                        data-athena-route-line-kind={edge.presentationConnector?.line.lineKind ?? ''}
	                        data-athena-route-presentation-class-id={edge.presentationConnector?.line.classId ?? ''}
	                        data-athena-route-compiler-snapshot-id={edge.presentationConnector?.line.compilerSnapshotId ?? ''}
                            data-athena-route-source-span={sourceSpan}
	                        data-athena-route-quality={edge.presentationConnector?.quality ?? ''}
                        strokeDasharray={strokeDasharray}
                        style={{
                            ...(edge.line?.weight ? { strokeWidth: edge.line.weight } : {}),
                            ...(strokeColor ? { stroke: strokeColor } : {}),
                        }}
                        fill='none'
                        vectorEffect='non-scaling-stroke'
                    />
                    {edge.bendMarkerPoints.map((point, index) => <circle
                        key={`${edge.id}:bend:${index}`}
                        className={`athena-graph-workbench__edge-marker ${edgeSelected ? 'athena-graph-workbench__edge-marker--selected' : ''}`}
                        cx={point.x}
                        cy={point.y}
                        r={4}
                        vectorEffect='non-scaling-stroke'
                    />)}
                    {edge.crossingMarkerPoints.map(marker => <circle
                        key={`${edge.id}:marker:${marker.markerId}`}
                        className={`athena-graph-workbench__edge-crossing ${edgeSelected ? 'athena-graph-workbench__edge-crossing--selected' : ''}`}
                        data-athena-connection-marker-id={marker.markerId}
                        data-athena-connection-marker-kind={marker.kind}
                        data-athena-connection-marker-appearance={marker.appearanceClassId}
                        cx={marker.point.x}
                        cy={marker.point.y}
                        r={marker.kind === 'junction' ? 3.5 : 6}
                        vectorEffect='non-scaling-stroke'
                    />)}
                </g>
                {edge.connectionLabels.map((label, index) => {
                    const deferred = label.canvasDisplay === 'selection' && !edgeSelected;
                    return <text
                        key={`${edge.id}:route-label:${index}`}
                        className={`athena-graph-workbench__edge-label ${deferred ? 'athena-graph-workbench__edge-label--deferred' : ''}`}
                        data-athena-route-label='true'
                        data-athena-route-label-for={edge.id}
                        data-athena-route-label-display={label.canvasDisplay}
                        x={label.point.x}
                        y={label.point.y}
                        textAnchor='middle'
                        dominantBaseline='central'
                    >
                        {label.text}
                    </text>;
                })}
            </React.Fragment>;
        })}
    </>;
}

function formatSourceSpan(
    sourceSpan: NonNullable<AthenaGraphWorkbenchEdge['presentationConnector']>['sourceSpan'],
): string {
    if (!sourceSpan?.file) {
        return '';
    }
    return `${sourceSpan.file}:${sourceSpan.startLine}:${sourceSpan.startColumn}-${sourceSpan.endLine}:${sourceSpan.endColumn}`;
}
