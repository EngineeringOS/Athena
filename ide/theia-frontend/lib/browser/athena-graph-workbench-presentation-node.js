"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaGraphWorkbenchPresentationNode = AthenaGraphWorkbenchPresentationNode;
const React = __importStar(require("@theia/core/shared/react"));
function AthenaGraphWorkbenchPresentationNode(props) {
    const { node, nodeClassName, labelClassName, selected } = props;
    const occurrence = node.presentationOccurrence;
    if (!occurrence || node.presentationParts.length === 0) {
        return undefined;
    }
    return React.createElement(React.Fragment, null,
        node.presentationParts.map(part => React.createElement(React.Fragment, { key: `${node.id}:${part.partId}` },
            part.commands.map((command, index) => renderPresentationCommand(node, command, {
                key: `${node.id}:${part.partId}:${index}`,
                nodeClassName,
                selected,
                tokenDefaults: part.tokenDefaults,
                tokenOverrides: part.tokenOverrides,
            })),
            part.textSlots.map(slot => slot.text
                ? React.createElement("text", { key: `${node.id}:${part.partId}:text:${slot.slotId}`, className: labelClassName, x: slot.x, y: slot.y }, slot.text)
                : undefined))),
        occurrence.textSlots.map(slot => slot.text
            ? React.createElement("text", { key: `${node.id}:occurrence:text:${slot.slotId}`, className: labelClassName, x: slot.x, y: slot.y }, slot.text)
            : undefined));
}
function renderPresentationCommand(node, command, args) {
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
            return React.createElement("rect", { key: args.key, className: className, x: command.bounds.x, y: command.bounds.y, width: command.bounds.width, height: command.bounds.height, rx: command.radius ?? (node.kind === 'label' ? 3 : 0), ry: command.radius ?? (node.kind === 'label' ? 3 : 0), style: { stroke, strokeWidth, fill }, vectorEffect: 'non-scaling-stroke' });
        case 'stroke_line':
            if (!command.start || !command.end) {
                return undefined;
            }
            return React.createElement("line", { key: args.key, className: className, x1: command.start.x, y1: command.start.y, x2: command.end.x, y2: command.end.y, style: { stroke, strokeWidth }, vectorEffect: 'non-scaling-stroke' });
        case 'circle':
            if (!command.center || !command.radius) {
                return undefined;
            }
            return React.createElement("circle", { key: args.key, className: className, cx: command.center.x, cy: command.center.y, r: command.radius, style: { stroke, strokeWidth, fill }, vectorEffect: 'non-scaling-stroke' });
        case 'svg_path':
            if (!command.pathData) {
                return undefined;
            }
            return React.createElement("path", { key: args.key, className: className, d: command.pathData, style: { stroke, strokeWidth, fill }, vectorEffect: 'non-scaling-stroke' });
        default:
            return undefined;
    }
}
function resolveToken(defaults, overrides, tokenKey, fallback) {
    if (!tokenKey) {
        return fallback;
    }
    return overrides[tokenKey] ?? defaults[tokenKey] ?? fallback;
}
//# sourceMappingURL=athena-graph-workbench-presentation-node.js.map