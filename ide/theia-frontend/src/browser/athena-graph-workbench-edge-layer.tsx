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
            const tooltipLabels = edge.routeLabels.map(label => label.text).filter(Boolean).join(' | ');

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
                        data-athena-route-quality={edge.presentationConnector?.tokenOverrides.routeQuality ?? ''}
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
                    {edge.crossingMarkerPoints.map((point, index) => <circle
                        key={`${edge.id}:crossing:${index}`}
                        className={`athena-graph-workbench__edge-crossing ${edgeSelected ? 'athena-graph-workbench__edge-crossing--selected' : ''}`}
                        cx={point.x}
                        cy={point.y}
                        r={6}
                        vectorEffect='non-scaling-stroke'
                    />)}
                </g>
                {edge.routeLabels.map((label, index) => {
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
                {edge.terminals.map(terminal => {
                    const terminalSemanticId = terminal.portSemanticId ?? edge.semanticId;
                    const terminalSelected = selectedSemanticId === edge.semanticId || selectedSemanticId === terminal.portSemanticId;
                    return <circle
                        key={`${edge.id}:terminal:${terminal.role}`}
                        className={`athena-graph-workbench__edge-terminal ${terminalSelected ? 'athena-graph-workbench__edge-terminal--selected' : ''}`}
                        data-athena-graph-interactive='true'
                        data-athena-route-terminal='true'
                        data-athena-route-terminal-for={edge.id}
                        data-athena-route-terminal-role={terminal.role}
                        data-athena-route-terminal-anchor-id={terminal.anchorId ?? ''}
                        data-athena-route-terminal-port-id={terminal.portSemanticId ?? ''}
                        role='button'
                        tabIndex={0}
                        aria-label={`${terminal.role} terminal`}
                        cx={terminal.point.x}
                        cy={terminal.point.y}
                        r={4.5}
                        vectorEffect='non-scaling-stroke'
                        onClick={event => {
                            event.stopPropagation();
                            void onSelectSemanticId(terminalSemanticId);
                        }}
                        onKeyDown={event => {
                            if (event.key !== 'Enter' && event.key !== ' ') {
                                return;
                            }
                            event.preventDefault();
                            event.stopPropagation();
                            void onSelectSemanticId(terminalSemanticId);
                        }}
                    />;
                })}
            </React.Fragment>;
        })}
    </>;
}
