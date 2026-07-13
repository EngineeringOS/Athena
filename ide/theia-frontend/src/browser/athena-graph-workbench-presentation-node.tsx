import * as React from '@theia/core/shared/react';
import { AthenaGraphWorkbenchNode } from './athena-graph-workbench-model';

type AthenaGraphWorkbenchPresentationNodeProps = {
    node: AthenaGraphWorkbenchNode;
    nodeClassName: string;
    labelClassName: string;
    selected: boolean;
};

export function AthenaGraphWorkbenchPresentationNode(
    props: AthenaGraphWorkbenchPresentationNodeProps,
): React.ReactNode {
    const { node, nodeClassName, labelClassName, selected } = props;
    const occurrence = node.presentationOccurrence;
    if (!occurrence || node.presentationParts.length === 0) {
        return undefined;
    }

    return <>
        {node.presentationParts.map(part => <React.Fragment key={`${node.id}:${part.partId}`}>
            {part.commands.map((command, index) => renderPresentationCommand(
                node,
                command,
                {
                    key: `${node.id}:${part.partId}:${index}`,
                    nodeClassName,
                    selected,
                    tokenDefaults: part.tokenDefaults,
                    tokenOverrides: part.tokenOverrides,
                },
            ))}
            {part.textSlots.map(slot => slot.text
                ? <text
                    key={`${node.id}:${part.partId}:text:${slot.slotId}`}
                    className={labelClassName}
                    x={slot.x}
                    y={slot.y}
                >
                    {slot.text}
                </text>
                : undefined)}
        </React.Fragment>)}
        {occurrence.textSlots.map(slot => slot.text
            ? <text
                key={`${node.id}:occurrence:text:${slot.slotId}`}
                className={labelClassName}
                x={slot.x}
                y={slot.y}
            >
                {slot.text}
            </text>
            : undefined)}
    </>;
}

function renderPresentationCommand(
    node: AthenaGraphWorkbenchNode,
    command: AthenaGraphWorkbenchNode['presentationParts'][number]['commands'][number],
    args: {
        key: string;
        nodeClassName: string;
        selected: boolean;
        tokenDefaults: Record<string, string>;
        tokenOverrides: Record<string, string>;
    },
): React.ReactNode {
    const stroke = resolveToken(args.tokenDefaults, args.tokenOverrides, command.strokeTokenKey, 'var(--athena-graph-node-stroke)');
    const strokeWidth = resolveToken(args.tokenDefaults, args.tokenOverrides, command.strokeWidthTokenKey, '1.6');
    const fill = command.fillTokenKey
        ? resolveToken(args.tokenDefaults, args.tokenOverrides, command.fillTokenKey, 'none')
        : 'none';
    const className = `${args.nodeClassName} ${args.selected ? 'athena-graph-workbench__presentation-shape--selected' : ''}`;

    switch (command.kind) {
        case 'stroke_rectangle':
            if (!command.bounds) {
                return undefined;
            }
            return <rect
                key={args.key}
                className={className}
                x={command.bounds.x}
                y={command.bounds.y}
                width={command.bounds.width}
                height={command.bounds.height}
                rx={command.radius ?? (node.kind === 'label' ? 3 : 0)}
                ry={command.radius ?? (node.kind === 'label' ? 3 : 0)}
                style={{ stroke, strokeWidth, fill }}
                vectorEffect='non-scaling-stroke'
            />;

        case 'stroke_line':
            if (!command.start || !command.end) {
                return undefined;
            }
            return <line
                key={args.key}
                className={className}
                x1={command.start.x}
                y1={command.start.y}
                x2={command.end.x}
                y2={command.end.y}
                style={{ stroke, strokeWidth }}
                vectorEffect='non-scaling-stroke'
            />;

        case 'circle':
            if (!command.center || !command.radius) {
                return undefined;
            }
            return <circle
                key={args.key}
                className={className}
                cx={command.center.x}
                cy={command.center.y}
                r={command.radius}
                style={{ stroke, strokeWidth, fill }}
                vectorEffect='non-scaling-stroke'
            />;

        case 'svg_path':
            if (!command.pathData) {
                return undefined;
            }
            return <path
                key={args.key}
                className={className}
                d={command.pathData}
                style={{ stroke, strokeWidth, fill }}
                vectorEffect='non-scaling-stroke'
            />;

        default:
            return undefined;
    }
}

function resolveToken(
    defaults: Record<string, string>,
    overrides: Record<string, string>,
    tokenKey: string | undefined,
    fallback: string,
): string {
    if (!tokenKey) {
        return fallback;
    }
    return overrides[tokenKey] ?? defaults[tokenKey] ?? fallback;
}
