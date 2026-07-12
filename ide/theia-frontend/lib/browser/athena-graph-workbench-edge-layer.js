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
exports.AthenaGraphWorkbenchEdgeLayer = AthenaGraphWorkbenchEdgeLayer;
const React = __importStar(require("@theia/core/shared/react"));
/** Pure SVG edge layer for conductor-style Athena graph rendering. */
function AthenaGraphWorkbenchEdgeLayer(props) {
    const { edges, selectedSemanticId, onSelectSemanticId } = props;
    return React.createElement(React.Fragment, null, edges.map(edge => {
        const sourceSelected = !!edge.sourcePortSemanticId && selectedSemanticId === edge.sourcePortSemanticId;
        const targetSelected = !!edge.targetPortSemanticId && selectedSemanticId === edge.targetPortSemanticId;
        const edgeSelected = selectedSemanticId === edge.semanticId || sourceSelected || targetSelected;
        return React.createElement(React.Fragment, { key: edge.id },
            React.createElement("g", { className: 'athena-graph-workbench__element', "data-athena-graph-interactive": 'true', role: 'button', tabIndex: 0, onClick: () => void onSelectSemanticId(edge.semanticId), onKeyDown: event => {
                    if (event.key !== 'Enter' && event.key !== ' ') {
                        return;
                    }
                    event.preventDefault();
                    void onSelectSemanticId(edge.semanticId);
                } },
                React.createElement("path", { className: `athena-graph-workbench__edge-casing ${edgeSelected ? 'athena-graph-workbench__edge-casing--selected' : ''}`, d: edge.path, fill: 'none', vectorEffect: 'non-scaling-stroke' }),
                React.createElement("path", { className: `athena-graph-workbench__edge athena-graph-workbench__edge--${edge.conductorStyle} ${edgeSelected ? 'athena-graph-workbench__edge--selected' : ''}`, d: edge.path, fill: 'none', vectorEffect: 'non-scaling-stroke' }),
                edge.bendMarkerPoints.map((point, index) => React.createElement("circle", { key: `${edge.id}:bend:${index}`, className: `athena-graph-workbench__edge-marker ${edgeSelected ? 'athena-graph-workbench__edge-marker--selected' : ''}`, cx: point.x, cy: point.y, r: 4, vectorEffect: 'non-scaling-stroke' }))),
            edge.terminals.map(terminal => {
                const terminalSemanticId = terminal.portSemanticId ?? edge.semanticId;
                const terminalSelected = selectedSemanticId === edge.semanticId || selectedSemanticId === terminal.portSemanticId;
                return React.createElement("circle", { key: `${edge.id}:terminal:${terminal.role}`, className: `athena-graph-workbench__edge-terminal ${terminalSelected ? 'athena-graph-workbench__edge-terminal--selected' : ''}`, "data-athena-graph-interactive": 'true', role: 'button', tabIndex: 0, "aria-label": `${terminal.role} terminal`, cx: terminal.point.x, cy: terminal.point.y, r: 4.5, vectorEffect: 'non-scaling-stroke', onClick: event => {
                        event.stopPropagation();
                        void onSelectSemanticId(terminalSemanticId);
                    }, onKeyDown: event => {
                        if (event.key !== 'Enter' && event.key !== ' ') {
                            return;
                        }
                        event.preventDefault();
                        event.stopPropagation();
                        void onSelectSemanticId(terminalSemanticId);
                    } });
            }));
    }));
}
//# sourceMappingURL=athena-graph-workbench-edge-layer.js.map