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
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
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
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var AthenaGraphWorkbenchWidget_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaGraphWorkbenchWidget = void 0;
const React = __importStar(require("@theia/core/shared/react"));
const react_widget_1 = require("@theia/core/lib/browser/widgets/react-widget");
const disposable_1 = require("@theia/core/lib/common/disposable");
const inversify_1 = require("@theia/core/shared/inversify");
const browser_1 = require("@theia/editor/lib/browser");
const athena_graph_adapter_service_1 = require("./athena-graph-adapter-service");
const athena_graph_workbench_model_1 = require("./athena-graph-workbench-model");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
const athena_semantic_selection_model_1 = require("./athena-semantic-selection-model");
const athena_semantic_selection_service_1 = require("./athena-semantic-selection-service");
/** Graph-first Athena workbench surface with a pannable and zoomable renderer viewport. */
let AthenaGraphWorkbenchWidget = class AthenaGraphWorkbenchWidget extends react_widget_1.ReactWidget {
    static { AthenaGraphWorkbenchWidget_1 = this; }
    static ID = 'athena.graphWorkbench';
    static LABEL = 'Graphical View';
    editorManager;
    repositorySessionService;
    graphAdapterService;
    semanticSelectionService;
    currentEditorListeners = new disposable_1.DisposableCollection();
    diagram = undefined;
    errorMessage;
    loading = false;
    switchingView = false;
    refreshHandle;
    viewportElement;
    viewportObserver;
    viewportSize = { width: 0, height: 0 };
    viewportTransform = { zoom: 1, offsetX: 0, offsetY: 0 };
    panState;
    dragState;
    pendingAutoFit = true;
    overlayPanelExpanded = false;
    lastGraphCommandIntent = undefined;
    connectPortsArmed = false;
    connectPortsSource;
    connectPortsPending = false;
    revealingSelectionSemanticId;
    init() {
        this.id = AthenaGraphWorkbenchWidget_1.ID;
        this.title.label = AthenaGraphWorkbenchWidget_1.LABEL;
        this.title.caption = AthenaGraphWorkbenchWidget_1.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-type-hierarchy-sub';
        this.addClass('athena-graph-workbench-widget');
        this.toDispose.push(this.currentEditorListeners);
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(this.semanticSelectionService.onDidChangeSelection(selection => {
            void this.handleSemanticSelectionChanged(selection);
        }));
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(widget => {
            this.bindCurrentEditor(widget);
            this.scheduleRefresh();
        }));
        this.toDispose.push(disposable_1.Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
            }
            this.viewportObserver?.disconnect();
            this.viewportObserver = undefined;
            this.viewportElement = undefined;
            this.panState = undefined;
            this.dragState = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.connectPortsPending = false;
            this.revealingSelectionSemanticId = undefined;
        }));
        this.bindCurrentEditor(this.editorManager.currentEditor);
        this.scheduleRefresh();
    }
    bindCurrentEditor(widget) {
        this.currentEditorListeners.dispose();
        this.currentEditorListeners = new disposable_1.DisposableCollection();
        this.toDispose.push(this.currentEditorListeners);
        if (!this.isAthenaEditor(widget)) {
            return;
        }
        this.currentEditorListeners.push(widget.editor.onDocumentContentChanged(() => this.scheduleRefresh()));
    }
    isAthenaEditor(widget) {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }
    scheduleRefresh() {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshDiagram();
        }, 120);
    }
    async refreshDiagram() {
        const sessionState = this.repositorySessionService.state;
        const currentRepositoryRoot = sessionState.repositoryRoot;
        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.diagram = undefined;
            this.panState = undefined;
            this.update();
            return;
        }
        this.loading = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const diagram = await this.graphAdapterService.requestDiagram();
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.diagram = diagram;
            this.lastGraphCommandIntent = undefined;
            this.dragState = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.connectPortsPending = false;
            this.reconcileTransientSelection(diagram);
            void this.handleSemanticSelectionChanged(this.semanticSelectionService.selection, diagram);
            this.pendingAutoFit = true;
            this.fitViewportToDiagramIfPossible();
        }
        catch (error) {
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.diagram = undefined;
        }
        finally {
            if (this.repositorySessionService.state.repositoryRoot === currentRepositoryRoot) {
                this.loading = false;
                this.update();
            }
        }
    }
    render() {
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            return React.createElement("div", { className: 'athena-graph-workbench' },
                React.createElement("section", { className: 'athena-graph-workbench__empty' },
                    React.createElement("h2", null, "Graphical View"),
                    React.createElement("p", null, sessionState.message)));
        }
        if (this.errorMessage) {
            return React.createElement("div", { className: 'athena-graph-workbench' },
                React.createElement("section", { className: 'athena-graph-workbench__empty athena-graph-workbench__empty--error' },
                    React.createElement("h2", null, "Graphical View"),
                    React.createElement("p", null, this.errorMessage)));
        }
        if (this.loading && !this.diagram) {
            return React.createElement("div", { className: 'athena-graph-workbench' },
                React.createElement("section", { className: 'athena-graph-workbench__empty' },
                    React.createElement("h2", null, "Graphical View"),
                    React.createElement("p", null, "Loading the runtime-owned graphical projection through the Athena adapter boundary.")));
        }
        if (!this.diagram) {
            return React.createElement("div", { className: 'athena-graph-workbench' },
                React.createElement("section", { className: 'athena-graph-workbench__empty' },
                    React.createElement("h2", null, "Graphical View"),
                    React.createElement("p", null, "No graphical projection payload is available yet for the active Athena session.")));
        }
        const model = (0, athena_graph_workbench_model_1.buildAthenaGraphWorkbenchModel)(this.diagram);
        const selectedSemantic = this.semanticSelectionService.selection;
        const selectedSemanticId = selectedSemantic?.semanticId;
        const connectPortsSupported = this.graphAdapterService.supportsConnectPortsIntent(this.diagram);
        const zoomPercent = Math.round(this.viewportTransform.zoom * 100);
        const stageStyle = this.buildStageStyle(model);
        const lastIntentSummary = this.lastGraphCommandIntent
            ? `${this.lastGraphCommandIntent.intentId} ${this.lastGraphCommandIntent.status}`
            : undefined;
        const canvasStyle = {
            width: `${model.canvas.width}px`,
            height: `${model.canvas.height}px`,
            transform: `translate(${this.viewportTransform.offsetX}px, ${this.viewportTransform.offsetY}px) scale(${this.viewportTransform.zoom})`,
            transformOrigin: '0 0',
        };
        return React.createElement("div", { className: 'athena-graph-workbench' },
            React.createElement("div", { className: 'athena-graph-workbench__workspace' },
                React.createElement("section", { className: `athena-graph-workbench__stage ${this.panState ? 'athena-graph-workbench__stage--panning' : ''}`, style: stageStyle, onClick: this.handleStageClick }, model.emptyState
                    ? React.createElement("div", { className: 'athena-graph-workbench__empty athena-graph-workbench__empty--inline' },
                        React.createElement("h4", null, model.emptyState.title),
                        React.createElement("p", null, model.emptyState.message))
                    : React.createElement(React.Fragment, null,
                        React.createElement("div", { className: 'athena-graph-workbench__overlay athena-graph-workbench__overlay--top' },
                            React.createElement("div", { className: 'athena-graph-workbench__floating-bar' },
                                React.createElement("div", { className: 'athena-graph-workbench__identity' },
                                    React.createElement("h2", null, model.headerTitle),
                                    React.createElement("div", { className: 'athena-graph-workbench__meta-strip' },
                                        React.createElement("span", { className: 'athena-graph-workbench__meta-chip' }, model.viewLabel))),
                                React.createElement("div", { className: 'athena-graph-workbench__tool-group' },
                                    React.createElement("div", { className: 'athena-graph-workbench__view-switches' }, model.supportedViews.map(view => React.createElement("button", { key: view.viewId, className: `athena-graph-workbench__tool-button athena-graph-workbench__tool-button--view ${view.isActive ? 'athena-graph-workbench__tool-button--active' : ''}`, title: this.viewAriaLabel(view), "aria-label": this.viewAriaLabel(view), type: 'button', disabled: view.isActive || this.switchingView, onClick: () => void this.switchActiveView(view.viewId) },
                                        React.createElement("span", { className: `codicon ${this.viewIconClass(view.viewId)}` })))),
                                    React.createElement("button", { className: 'athena-graph-workbench__tool-button', type: 'button', onClick: () => this.stepZoom(1 / 1.15), title: 'Zoom out', "aria-label": 'Zoom out' },
                                        React.createElement("span", { className: 'codicon codicon-zoom-out' })),
                                    React.createElement("button", { className: 'athena-graph-workbench__tool-button athena-graph-workbench__tool-button--readout', type: 'button', onClick: () => this.resetZoom(), title: 'Reset zoom', "aria-label": 'Reset zoom' },
                                        zoomPercent,
                                        "%"),
                                    React.createElement("button", { className: 'athena-graph-workbench__tool-button', type: 'button', onClick: () => this.stepZoom(1.15), title: 'Zoom in', "aria-label": 'Zoom in' },
                                        React.createElement("span", { className: 'codicon codicon-zoom-in' })),
                                    React.createElement("button", { className: 'athena-graph-workbench__tool-button', type: 'button', onClick: () => this.fitViewportToDiagram(), title: 'Fit graph to viewport', "aria-label": 'Fit graph to viewport' },
                                        React.createElement("span", { className: 'codicon codicon-screen-full' })),
                                    connectPortsSupported
                                        ? React.createElement("button", { className: `athena-graph-workbench__tool-button ${this.connectPortsArmed || this.connectPortsSource ? 'athena-graph-workbench__tool-button--active' : ''}`, type: 'button', title: this.connectPortsButtonTitle(), "aria-label": this.connectPortsButtonTitle(), disabled: this.connectPortsPending, onClick: () => this.toggleConnectPortsMode() },
                                            React.createElement("span", { className: `codicon ${this.connectPortsPending ? 'codicon-loading codicon-modifier-spin' : 'codicon-link'}` }))
                                        : undefined,
                                    React.createElement("button", { className: `athena-graph-workbench__overlay-toggle ${this.overlayPanelExpanded ? 'athena-graph-workbench__overlay-toggle--active' : ''}`, type: 'button', title: this.overlayPanelExpanded ? 'Collapse information panel' : 'Expand information panel', "aria-label": this.overlayPanelExpanded ? 'Collapse information panel' : 'Expand information panel', onClick: () => this.toggleOverlayPanel() },
                                        React.createElement("span", { className: 'codicon codicon-info' })),
                                    React.createElement("div", { className: `athena-graph-workbench__status athena-graph-workbench__status--${model.statusTone}`, title: model.statusLabel, "aria-label": model.statusLabel },
                                        React.createElement("span", { className: `athena-graph-workbench__status-icon codicon ${this.statusIconClass(model.statusTone)}` })))),
                            this.overlayPanelExpanded
                                ? React.createElement("div", { className: 'athena-graph-workbench__floating-panel' },
                                    React.createElement("section", { className: 'athena-graph-workbench__overlay-section' },
                                        React.createElement("div", { className: 'athena-graph-workbench__panel-title' }, "Selection"),
                                        !selectedSemanticId
                                            ? React.createElement("div", { className: 'athena-graph-workbench__panel-empty' }, "No selection")
                                            : React.createElement("div", { className: 'athena-graph-workbench__selection' },
                                                React.createElement("strong", null, selectedSemantic?.label ?? selectedSemanticId),
                                                React.createElement("span", { className: 'athena-graph-workbench__pill' }, selectedSemantic?.kind ?? 'semantic'),
                                                React.createElement("code", null, selectedSemanticId)),
                                        this.connectPortsArmed
                                            ? React.createElement("div", { className: 'athena-graph-workbench__panel-empty' }, this.connectPortsSource
                                                ? React.createElement(React.Fragment, null,
                                                    "Awaiting target port for ",
                                                    React.createElement("code", null, this.connectPortsSource.semanticId),
                                                    ".")
                                                : 'Connect ports is armed. Select a source port label, then a target port label.')
                                            : undefined),
                                    React.createElement("section", { className: 'athena-graph-workbench__overlay-section' },
                                        React.createElement("div", { className: 'athena-graph-workbench__panel-title' }, "Snapshot"),
                                        React.createElement("dl", { className: 'athena-graph-workbench__detail-list' },
                                            React.createElement("div", null,
                                                React.createElement("dt", null,
                                                    React.createElement("span", { className: 'codicon codicon-symbol-class' })),
                                                React.createElement("dd", null,
                                                    model.metrics.nodeCount,
                                                    " nodes")),
                                            React.createElement("div", null,
                                                React.createElement("dt", null,
                                                    React.createElement("span", { className: 'codicon codicon-git-commit' })),
                                                React.createElement("dd", null,
                                                    model.metrics.edgeCount,
                                                    " links")),
                                            React.createElement("div", null,
                                                React.createElement("dt", null,
                                                    React.createElement("span", { className: 'codicon codicon-warning' })),
                                                React.createElement("dd", null,
                                                    model.metrics.diagnosticCount,
                                                    " diagnostics")))),
                                    React.createElement("section", { className: 'athena-graph-workbench__overlay-section' },
                                        React.createElement("div", { className: 'athena-graph-workbench__panel-title' }, "Session"),
                                        React.createElement("dl", { className: 'athena-graph-workbench__detail-list' },
                                            React.createElement("div", null,
                                                React.createElement("dt", null, "View"),
                                                React.createElement("dd", null, this.diagram.activeViewId)),
                                            React.createElement("div", null,
                                                React.createElement("dt", null, "Graph"),
                                                React.createElement("dd", null,
                                                    React.createElement("code", null, this.diagram.graph.id))),
                                            React.createElement("div", null,
                                                React.createElement("dt", null, "Mappings"),
                                                React.createElement("dd", null, model.activeRenderContributions.map(contribution => contribution.displayName).join(', ') || 'None')))),
                                    this.lastGraphCommandIntent
                                        ? React.createElement("section", { className: 'athena-graph-workbench__overlay-section' },
                                            React.createElement("div", { className: 'athena-graph-workbench__panel-title' }, "Last Intent"),
                                            React.createElement("dl", { className: 'athena-graph-workbench__detail-list' },
                                                React.createElement("div", null,
                                                    React.createElement("dt", null, "Status"),
                                                    React.createElement("dd", null, this.lastGraphCommandIntent.status)),
                                                this.lastGraphCommandIntent.source
                                                    ? React.createElement("div", null,
                                                        React.createElement("dt", null, "Source"),
                                                        React.createElement("dd", null,
                                                            React.createElement("code", null, this.lastGraphCommandIntent.source.semanticId)))
                                                    : undefined,
                                                React.createElement("div", null,
                                                    React.createElement("dt", null, "Intent"),
                                                    React.createElement("dd", null,
                                                        React.createElement("code", null, this.lastGraphCommandIntent.intentId))),
                                                React.createElement("div", null,
                                                    React.createElement("dt", null, "Category"),
                                                    React.createElement("dd", null, this.lastGraphCommandIntent.mutationCategory)),
                                                React.createElement("div", null,
                                                    React.createElement("dt", null, "Target"),
                                                    React.createElement("dd", null,
                                                        React.createElement("code", null, this.lastGraphCommandIntent.target.semanticId))),
                                                React.createElement("div", null,
                                                    React.createElement("dt", null, "View"),
                                                    React.createElement("dd", null, this.lastGraphCommandIntent.viewId)),
                                                this.lastGraphCommandIntent.execution
                                                    ? React.createElement("div", null,
                                                        React.createElement("dt", null, "Command"),
                                                        React.createElement("dd", null,
                                                            React.createElement("code", null, this.lastGraphCommandIntent.execution.commandKind)))
                                                    : undefined,
                                                this.lastGraphCommandIntent.execution?.commandId
                                                    ? React.createElement("div", null,
                                                        React.createElement("dt", null, "Command Id"),
                                                        React.createElement("dd", null,
                                                            React.createElement("code", null, this.lastGraphCommandIntent.execution.commandId)))
                                                    : undefined),
                                            this.lastGraphCommandIntent.validationFeedback.length > 0
                                                ? React.createElement("ul", { className: 'athena-graph-workbench__list' }, this.lastGraphCommandIntent.validationFeedback.map(feedback => React.createElement("li", { key: `${feedback.code}:${feedback.message}`, className: 'athena-graph-workbench__item' },
                                                    React.createElement("div", { className: 'athena-graph-workbench__diagnostic-header' },
                                                        React.createElement("span", { className: `athena-graph-workbench__diagnostic-severity athena-graph-workbench__diagnostic-severity--${feedback.severity}` }, feedback.severity),
                                                        React.createElement("code", null, feedback.code)),
                                                    React.createElement("div", null, feedback.message))))
                                                : undefined,
                                            this.lastGraphCommandIntent.reason
                                                ? React.createElement("div", { className: 'athena-graph-workbench__panel-empty' }, this.lastGraphCommandIntent.reason)
                                                : undefined)
                                        : undefined,
                                    model.diagnostics.length > 0
                                        ? React.createElement("section", { className: 'athena-graph-workbench__overlay-section' },
                                            React.createElement("div", { className: 'athena-graph-workbench__panel-title' }, "Diagnostics"),
                                            React.createElement("ul", { className: 'athena-graph-workbench__list' }, model.diagnostics.slice(0, 3).map(diagnostic => React.createElement("li", { key: `${diagnostic.code}:${diagnostic.message}`, className: 'athena-graph-workbench__item' },
                                                React.createElement("div", { className: 'athena-graph-workbench__diagnostic-header' },
                                                    React.createElement("span", { className: `athena-graph-workbench__diagnostic-severity athena-graph-workbench__diagnostic-severity--${diagnostic.severity}` }, diagnostic.severity),
                                                    React.createElement("code", null, diagnostic.code)),
                                                React.createElement("div", null, diagnostic.message)))))
                                        : undefined)
                                : undefined),
                        React.createElement("div", { className: 'athena-graph-workbench__viewport', ref: this.bindViewportElement, onClick: this.handleViewportClick, onDoubleClick: this.handleViewportDoubleClick, onWheel: this.handleViewportWheel, onPointerDown: this.handleViewportPointerDown, onPointerMove: this.handleViewportPointerMove, onPointerUp: this.handleViewportPointerEnd, onPointerCancel: this.handleViewportPointerEnd },
                            React.createElement("div", { className: 'athena-graph-workbench__grid' }),
                            React.createElement("svg", { className: 'athena-graph-workbench__canvas', viewBox: model.svgViewBox, role: 'img', "aria-label": 'Athena graphical projection', style: canvasStyle },
                                model.edges.map(edge => React.createElement("g", { key: edge.id, className: 'athena-graph-workbench__element', "data-athena-graph-interactive": 'true', role: 'button', tabIndex: 0, onClick: () => void this.semanticSelectionService.selectSemanticId(edge.id), onKeyDown: event => {
                                        if (event.key !== 'Enter' && event.key !== ' ') {
                                            return;
                                        }
                                        event.preventDefault();
                                        void this.semanticSelectionService.selectSemanticId(edge.id);
                                    } },
                                    React.createElement("line", { className: `athena-graph-workbench__edge ${selectedSemanticId === edge.id ? 'athena-graph-workbench__edge--selected' : ''}`, x1: edge.sourcePoint.x, y1: edge.sourcePoint.y, x2: edge.targetPoint.x, y2: edge.targetPoint.y, vectorEffect: 'non-scaling-stroke' }))),
                                model.nodes.map(node => React.createElement("g", { key: node.id, className: 'athena-graph-workbench__element', "data-athena-graph-interactive": 'true', role: 'button', tabIndex: 0, transform: node.kind === 'component' ? this.graphNodeTransform(node.id) : undefined, onClick: event => void this.handleNodeClick(event, node.id, node.kind, node.label), onKeyDown: event => void this.handleGraphElementKeyDown(event, node.id, node.kind, node.label), onPointerDown: node.kind === 'component'
                                        ? event => this.handleComponentPointerDown(event, node.id, node.position.x, node.position.y)
                                        : undefined },
                                    React.createElement("rect", { className: `athena-graph-workbench__node athena-graph-workbench__node--${node.kind} ${selectedSemanticId === node.id ? 'athena-graph-workbench__node--selected' : ''}`, x: node.position.x, y: node.position.y, width: node.size.width, height: node.size.height, rx: node.kind === 'label' ? 10 : 18, ry: node.kind === 'label' ? 10 : 18, vectorEffect: 'non-scaling-stroke' }),
                                    React.createElement("text", { className: 'athena-graph-workbench__node-label', x: node.position.x + (node.kind === 'label' ? 12 : 20), y: node.position.y + (node.kind === 'label' ? 22 : 34) }, node.label),
                                    node.kind === 'component'
                                        ? React.createElement("text", { className: 'athena-graph-workbench__node-meta', x: node.position.x + 20, y: node.position.y + 58 }, node.id)
                                        : undefined)))),
                        React.createElement("div", { className: 'athena-graph-workbench__statusline' },
                            React.createElement("span", { title: 'Canvas size' },
                                React.createElement("span", { className: 'codicon codicon-device-desktop' }),
                                " ",
                                model.canvas.width,
                                " x ",
                                model.canvas.height),
                            React.createElement("span", { title: 'Semantic path' },
                                React.createElement("span", { className: 'codicon codicon-git-branch' }),
                                " ",
                                model.semanticPath),
                            lastIntentSummary
                                ? React.createElement("span", { title: 'Last Athena graph intent' },
                                    React.createElement("span", { className: 'codicon codicon-symbol-event' }),
                                    " ",
                                    lastIntentSummary)
                                : undefined)))));
    }
    abbreviateViewLabel(displayName) {
        const words = displayName.split(/[\s_-]+/).filter(Boolean);
        if (words.length >= 2) {
            return `${words[0][0] ?? ''}${words[1][0] ?? ''}`.toUpperCase();
        }
        return displayName.slice(0, 2).toUpperCase();
    }
    viewIconClass(viewId) {
        switch (viewId.toLowerCase()) {
            case 'cabinet':
                return 'codicon-package';
            case 'wiring':
                return 'codicon-git-commit';
            default:
                return 'codicon-symbol-misc';
        }
    }
    viewAriaLabel(view) {
        return view.description ? `${view.displayName}: ${view.description}` : view.displayName;
    }
    connectPortsButtonTitle() {
        if (this.connectPortsPending) {
            return 'Submitting connect ports request';
        }
        if (!this.connectPortsArmed) {
            return 'Connect ports';
        }
        if (!this.connectPortsSource) {
            return 'Select source port';
        }
        return `Select target port for ${this.connectPortsSource.label}`;
    }
    toggleOverlayPanel() {
        this.overlayPanelExpanded = !this.overlayPanelExpanded;
        this.update();
    }
    toggleConnectPortsMode() {
        if (this.connectPortsPending) {
            return;
        }
        if (this.connectPortsArmed || this.connectPortsSource) {
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.update();
            return;
        }
        this.connectPortsArmed = true;
        this.connectPortsSource = undefined;
        this.update();
    }
    statusIconClass(statusTone) {
        if (statusTone === 'ready') {
            return 'codicon-check';
        }
        if (statusTone === 'warning') {
            return 'codicon-warning';
        }
        return 'codicon-circle-large-outline';
    }
    buildStageStyle(model) {
        const style = {};
        const surfaceTokens = model.surfaceTokens;
        const cssVariables = {
            '--athena-graph-stage-tint': surfaceTokens.canvas.canvasTint,
            '--athena-graph-grid-major': surfaceTokens.canvas.gridMajor,
            '--athena-graph-grid-minor': surfaceTokens.canvas.gridMinor,
            '--athena-graph-node-fill': surfaceTokens.node.fill,
            '--athena-graph-node-stroke': surfaceTokens.node.stroke,
            '--athena-graph-node-label': surfaceTokens.node.label,
            '--athena-graph-node-meta': surfaceTokens.node.meta,
            '--athena-graph-edge-stroke': surfaceTokens.edge.stroke,
        };
        for (const [key, value] of Object.entries(cssVariables)) {
            if (value) {
                style[key] = value;
            }
        }
        return style;
    }
    bindViewportElement = (element) => {
        const nextElement = element ?? undefined;
        if (this.viewportElement === nextElement) {
            return;
        }
        this.viewportObserver?.disconnect();
        this.viewportElement = nextElement;
        if (!this.viewportElement) {
            this.viewportObserver = undefined;
            return;
        }
        this.viewportObserver = new ResizeObserver(() => this.syncViewportSize());
        this.viewportObserver.observe(this.viewportElement);
        this.syncViewportSize();
    };
    syncViewportSize() {
        if (!this.viewportElement) {
            return;
        }
        const nextWidth = Math.max(Math.round(this.viewportElement.clientWidth), 0);
        const nextHeight = Math.max(Math.round(this.viewportElement.clientHeight), 0);
        const changed = nextWidth !== this.viewportSize.width || nextHeight !== this.viewportSize.height;
        this.viewportSize = { width: nextWidth, height: nextHeight };
        if (this.pendingAutoFit) {
            this.fitViewportToDiagramIfPossible();
            return;
        }
        if (changed) {
            this.update();
        }
    }
    fitViewportToDiagram() {
        this.pendingAutoFit = true;
        this.fitViewportToDiagramIfPossible();
    }
    fitViewportToDiagramIfPossible() {
        if (!this.pendingAutoFit || !this.diagram || this.viewportSize.width <= 0 || this.viewportSize.height <= 0) {
            return;
        }
        const model = (0, athena_graph_workbench_model_1.buildAthenaGraphWorkbenchModel)(this.diagram);
        if (model.emptyState) {
            this.pendingAutoFit = false;
            this.viewportTransform = { zoom: 1, offsetX: 0, offsetY: 0 };
            this.update();
            return;
        }
        this.viewportTransform = (0, athena_graph_workbench_model_1.fitAthenaGraphViewport)(model.sceneBounds, this.viewportSize);
        this.pendingAutoFit = false;
        this.update();
    }
    resetZoom() {
        const center = this.getViewportCenterPoint();
        this.viewportTransform = (0, athena_graph_workbench_model_1.zoomAthenaGraphViewportAtPoint)(this.viewportTransform, center, 1);
        this.update();
    }
    graphNodeTransform(nodeId) {
        if (!this.dragState || this.dragState.semanticId !== nodeId) {
            return undefined;
        }
        const deltaX = this.dragState.currentX - this.dragState.originX;
        const deltaY = this.dragState.currentY - this.dragState.originY;
        return `translate(${deltaX}, ${deltaY})`;
    }
    stepZoom(multiplier) {
        const center = this.getViewportCenterPoint();
        this.viewportTransform = (0, athena_graph_workbench_model_1.zoomAthenaGraphViewportAtPoint)(this.viewportTransform, center, (0, athena_graph_workbench_model_1.clampAthenaGraphZoom)(this.viewportTransform.zoom * multiplier));
        this.update();
    }
    getViewportCenterPoint() {
        return {
            x: this.viewportSize.width > 0 ? this.viewportSize.width / 2 : 0,
            y: this.viewportSize.height > 0 ? this.viewportSize.height / 2 : 0,
        };
    }
    handleViewportClick = (event) => {
        if (!this.isInteractiveTarget(event.target)) {
            void this.semanticSelectionService.clearSelection();
        }
    };
    handleStageClick = (event) => {
        if (!this.overlayPanelExpanded) {
            return;
        }
        const target = event.target;
        if (target instanceof Element &&
            (target.closest('.athena-graph-workbench__floating-panel') ||
                target.closest('.athena-graph-workbench__overlay-toggle'))) {
            return;
        }
        this.overlayPanelExpanded = false;
        this.update();
    };
    handleViewportDoubleClick = (event) => {
        if (this.isInteractiveTarget(event.target)) {
            return;
        }
        this.fitViewportToDiagram();
    };
    handleViewportWheel = (event) => {
        if (!this.diagram) {
            return;
        }
        event.preventDefault();
        const rect = event.currentTarget.getBoundingClientRect();
        const screenPoint = {
            x: event.clientX - rect.left,
            y: event.clientY - rect.top,
        };
        const multiplier = event.deltaY < 0 ? 1.12 : 1 / 1.12;
        this.viewportTransform = (0, athena_graph_workbench_model_1.zoomAthenaGraphViewportAtPoint)(this.viewportTransform, screenPoint, this.viewportTransform.zoom * multiplier);
        this.update();
    };
    handleViewportPointerDown = (event) => {
        const shouldPan = event.button === 1 || !this.isInteractiveTarget(event.target);
        if (!shouldPan) {
            return;
        }
        event.preventDefault();
        this.panState = {
            pointerId: event.pointerId,
            lastClientX: event.clientX,
            lastClientY: event.clientY,
        };
        event.currentTarget.setPointerCapture(event.pointerId);
        this.update();
    };
    handleViewportPointerMove = (event) => {
        if (this.dragState && this.dragState.pointerId === event.pointerId) {
            event.preventDefault();
            const zoom = this.viewportTransform.zoom <= 0 ? 1 : this.viewportTransform.zoom;
            const deltaX = Math.round((event.clientX - this.dragState.startClientX) / zoom);
            const deltaY = Math.round((event.clientY - this.dragState.startClientY) / zoom);
            this.dragState = {
                ...this.dragState,
                currentX: this.dragState.originX + deltaX,
                currentY: this.dragState.originY + deltaY,
                moved: this.dragState.moved || deltaX !== 0 || deltaY !== 0,
            };
            this.update();
            return;
        }
        if (!this.panState || this.panState.pointerId !== event.pointerId) {
            return;
        }
        const deltaX = event.clientX - this.panState.lastClientX;
        const deltaY = event.clientY - this.panState.lastClientY;
        this.panState = {
            pointerId: event.pointerId,
            lastClientX: event.clientX,
            lastClientY: event.clientY,
        };
        this.viewportTransform = (0, athena_graph_workbench_model_1.panAthenaGraphViewport)(this.viewportTransform, deltaX, deltaY);
        this.update();
    };
    handleViewportPointerEnd = (event) => {
        if (this.dragState && this.dragState.pointerId === event.pointerId) {
            const dragState = this.dragState;
            this.dragState = undefined;
            this.update();
            if (dragState.moved) {
                void this.submitPlacementIntent(dragState);
            }
            return;
        }
        if (!this.panState || this.panState.pointerId !== event.pointerId) {
            return;
        }
        this.panState = undefined;
        if (event.currentTarget.hasPointerCapture(event.pointerId)) {
            event.currentTarget.releasePointerCapture(event.pointerId);
        }
        this.update();
    };
    isInteractiveTarget(target) {
        return target instanceof Element && !!target.closest('[data-athena-graph-interactive="true"]');
    }
    handleGraphElementKeyDown(event, semanticId, kind, label) {
        if (event.key !== 'Enter' && event.key !== ' ') {
            return;
        }
        event.preventDefault();
        return this.handleNodeSelection(semanticId, kind, label);
    }
    handleNodeClick(event, semanticId, kind, label) {
        event.stopPropagation();
        return this.handleNodeSelection(semanticId, kind, label);
    }
    async handleNodeSelection(semanticId, kind, label) {
        if (this.connectPortsArmed && this.isConnectablePortNode(semanticId, kind)) {
            return this.handleConnectablePortSelection(semanticId, label);
        }
        await this.semanticSelectionService.selectSemanticId(semanticId);
    }
    isConnectablePortNode(semanticId, kind) {
        return kind === 'label' && semanticId.startsWith('port:');
    }
    async handleConnectablePortSelection(semanticId, label) {
        await this.semanticSelectionService.selectSemanticId(semanticId);
        if (!this.connectPortsSource) {
            this.connectPortsSource = {
                semanticId,
                label,
            };
            this.update();
            return;
        }
        const source = this.connectPortsSource;
        this.connectPortsSource = undefined;
        this.connectPortsArmed = false;
        this.update();
        await this.submitConnectPortsIntent(source.semanticId, semanticId);
    }
    handleComponentPointerDown(event, semanticId, x, y) {
        if (event.button !== 0 || !this.diagram || !this.graphAdapterService.supportsAdjustLayoutPlacementIntent(this.diagram)) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        this.dragState = {
            pointerId: event.pointerId,
            semanticId,
            subjectKind: 'component',
            originX: x,
            originY: y,
            currentX: x,
            currentY: y,
            startClientX: event.clientX,
            startClientY: event.clientY,
            moved: false,
        };
        this.update();
    }
    reconcileTransientSelection(diagram) {
        if (!diagram) {
            return;
        }
        const retained = (0, athena_semantic_selection_model_1.retainSelectionIfPresent)(diagram, this.semanticSelectionService.selection);
        if (!this.semanticSelectionService.selection || retained) {
            return;
        }
    }
    async switchActiveView(viewId) {
        if (this.switchingView) {
            return;
        }
        this.switchingView = true;
        this.errorMessage = undefined;
        this.lastGraphCommandIntent = undefined;
        this.dragState = undefined;
        this.connectPortsArmed = false;
        this.connectPortsSource = undefined;
        this.update();
        try {
            const diagram = await this.graphAdapterService.switchActiveView(viewId);
            this.diagram = diagram;
            this.reconcileTransientSelection(diagram);
            this.revealingSelectionSemanticId = undefined;
            this.pendingAutoFit = true;
            this.fitViewportToDiagramIfPossible();
        }
        catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.switchingView = false;
            this.update();
        }
    }
    async submitPlacementIntent(dragState) {
        if (!this.diagram) {
            return;
        }
        try {
            const payload = await this.graphAdapterService.submitAdjustLayoutPlacementIntent({
                diagram: this.diagram,
                semanticId: dragState.semanticId,
                subjectKind: dragState.subjectKind,
                x: dragState.currentX,
                y: dragState.currentY,
            });
            this.lastGraphCommandIntent = payload;
            if (payload?.status === 'accepted') {
                const diagram = await this.graphAdapterService.requestDiagram();
                this.diagram = diagram;
                this.reconcileTransientSelection(diagram);
                this.revealingSelectionSemanticId = undefined;
                await this.semanticSelectionService.selectSemanticId(dragState.semanticId);
            }
            this.errorMessage = undefined;
        }
        catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.update();
        }
    }
    async submitConnectPortsIntent(sourceSemanticId, targetSemanticId) {
        if (!this.diagram) {
            return;
        }
        this.connectPortsPending = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const payload = await this.graphAdapterService.submitConnectPortsIntent({
                diagram: this.diagram,
                sourceSemanticId,
                targetSemanticId,
            });
            this.lastGraphCommandIntent = payload;
            if (payload?.status === 'accepted') {
                const diagram = await this.graphAdapterService.requestDiagram();
                this.diagram = diagram;
                this.reconcileTransientSelection(diagram);
                this.revealingSelectionSemanticId = undefined;
                const createdConnectionId = payload.execution?.changedSemanticIds.find(semanticId => semanticId.startsWith('connection:'));
                if (createdConnectionId) {
                    await this.semanticSelectionService.selectSemanticId(createdConnectionId);
                }
            }
        }
        catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.connectPortsPending = false;
            this.update();
        }
    }
    async handleSemanticSelectionChanged(selection, diagramOverride) {
        this.update();
        if (!selection) {
            this.revealingSelectionSemanticId = undefined;
            return;
        }
        const diagram = diagramOverride ?? this.diagram;
        if (!diagram || this.loading || this.switchingView) {
            return;
        }
        if ((0, athena_semantic_selection_model_1.graphContainsSemanticId)(diagram, selection.semanticId)) {
            this.revealingSelectionSemanticId = undefined;
            return;
        }
        if (this.revealingSelectionSemanticId === selection.semanticId) {
            return;
        }
        this.revealingSelectionSemanticId = selection.semanticId;
        try {
            const revealedDiagram = await this.graphAdapterService.revealSemanticId(selection.semanticId, diagram);
            if (!revealedDiagram || this.semanticSelectionService.selection?.semanticId !== selection.semanticId) {
                return;
            }
            const viewChanged = !this.diagram || revealedDiagram.activeViewId !== this.diagram.activeViewId;
            this.diagram = revealedDiagram;
            if (viewChanged && (0, athena_semantic_selection_model_1.graphContainsSemanticId)(revealedDiagram, selection.semanticId)) {
                this.pendingAutoFit = true;
                this.fitViewportToDiagramIfPossible();
            }
            this.errorMessage = undefined;
        }
        catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            if (this.revealingSelectionSemanticId === selection.semanticId) {
                this.revealingSelectionSemanticId = undefined;
            }
            this.update();
        }
    }
};
exports.AthenaGraphWorkbenchWidget = AthenaGraphWorkbenchWidget;
__decorate([
    (0, inversify_1.inject)(browser_1.EditorManager),
    __metadata("design:type", browser_1.EditorManager)
], AthenaGraphWorkbenchWidget.prototype, "editorManager", void 0);
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaGraphWorkbenchWidget.prototype, "repositorySessionService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_graph_adapter_service_1.AthenaGraphAdapterService),
    __metadata("design:type", athena_graph_adapter_service_1.AthenaGraphAdapterService)
], AthenaGraphWorkbenchWidget.prototype, "graphAdapterService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_semantic_selection_service_1.AthenaSemanticSelectionService),
    __metadata("design:type", athena_semantic_selection_service_1.AthenaSemanticSelectionService)
], AthenaGraphWorkbenchWidget.prototype, "semanticSelectionService", void 0);
__decorate([
    (0, inversify_1.postConstruct)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AthenaGraphWorkbenchWidget.prototype, "init", null);
exports.AthenaGraphWorkbenchWidget = AthenaGraphWorkbenchWidget = AthenaGraphWorkbenchWidget_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaGraphWorkbenchWidget);
//# sourceMappingURL=athena-graph-workbench-widget.js.map