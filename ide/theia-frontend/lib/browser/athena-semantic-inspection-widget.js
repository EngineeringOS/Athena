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
var AthenaSemanticInspectionWidget_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaSemanticInspectionWidget = void 0;
const React = __importStar(require("@theia/core/shared/react"));
const react_widget_1 = require("@theia/core/lib/browser/widgets/react-widget");
const disposable_1 = require("@theia/core/lib/common/disposable");
const inversify_1 = require("@theia/core/shared/inversify");
const browser_1 = require("@theia/editor/lib/browser");
const athena_lsp_editor_bridge_service_1 = require("./athena-lsp-editor-bridge-service");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
const athena_semantic_selection_service_1 = require("./athena-semantic-selection-service");
let AthenaSemanticInspectionWidget = class AthenaSemanticInspectionWidget extends react_widget_1.ReactWidget {
    static { AthenaSemanticInspectionWidget_1 = this; }
    static ID = 'athena.semanticInspection';
    static LABEL = 'Semantic Inspection';
    editorManager;
    repositorySessionService;
    lspEditorBridgeService;
    semanticSelectionService;
    currentEditorListeners = new disposable_1.DisposableCollection();
    inspection;
    errorMessage;
    loading = false;
    refreshHandle;
    init() {
        this.id = AthenaSemanticInspectionWidget_1.ID;
        this.title.label = AthenaSemanticInspectionWidget_1.LABEL;
        this.title.caption = AthenaSemanticInspectionWidget_1.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-symbol-structure';
        this.addClass('athena-semantic-inspection-widget');
        this.toDispose.push(this.currentEditorListeners);
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(this.semanticSelectionService.onDidChangeSelection(() => this.update()));
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(widget => {
            this.bindCurrentEditor(widget);
            this.scheduleRefresh();
        }));
        this.toDispose.push(disposable_1.Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
            }
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
    scheduleRefresh() {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshInspection();
        }, 120);
    }
    async refreshInspection() {
        const sessionState = this.repositorySessionService.state;
        const currentEditor = this.editorManager.currentEditor;
        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.inspection = undefined;
            this.update();
            return;
        }
        if (!this.isAthenaEditor(currentEditor)) {
            this.loading = false;
            this.errorMessage = undefined;
            this.inspection = undefined;
            this.update();
            return;
        }
        const currentUri = currentEditor.editor.uri.toString();
        this.loading = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            this.inspection = inspection;
        }
        catch (error) {
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.inspection = undefined;
        }
        finally {
            if (this.editorManager.currentEditor?.editor.uri.toString() === currentUri) {
                this.loading = false;
                this.update();
            }
        }
    }
    isAthenaEditor(widget) {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }
    render() {
        const sessionState = this.repositorySessionService.state;
        const currentEditor = this.editorManager.currentEditor;
        if (sessionState.lifecycle !== 'ready') {
            return React.createElement("div", { className: 'athena-semantic-inspection' },
                React.createElement("section", { className: 'athena-semantic-inspection__empty' },
                    React.createElement("h2", null, "Semantic Inspection"),
                    React.createElement("p", null, sessionState.message)));
        }
        if (!this.isAthenaEditor(currentEditor)) {
            return React.createElement("div", { className: 'athena-semantic-inspection' },
                React.createElement("section", { className: 'athena-semantic-inspection__empty' },
                    React.createElement("h2", null, "Semantic Inspection"),
                    React.createElement("p", null,
                        "Open one ",
                        React.createElement("code", null, ".athena"),
                        " file to inspect Athena-owned semantic state beside the editor.")));
        }
        if (this.errorMessage) {
            return React.createElement("div", { className: 'athena-semantic-inspection' },
                React.createElement("section", { className: 'athena-semantic-inspection__empty athena-semantic-inspection__empty--error' },
                    React.createElement("h2", null, "Semantic Inspection"),
                    React.createElement("p", null, this.errorMessage)));
        }
        if (this.loading && !this.inspection) {
            return React.createElement("div", { className: 'athena-semantic-inspection' },
                React.createElement("section", { className: 'athena-semantic-inspection__empty' },
                    React.createElement("h2", null, "Semantic Inspection"),
                    React.createElement("p", null, "Loading the latest Athena semantic snapshot for the current editor.")));
        }
        const inspection = this.inspection;
        if (!inspection) {
            return React.createElement("div", { className: 'athena-semantic-inspection' },
                React.createElement("section", { className: 'athena-semantic-inspection__empty' },
                    React.createElement("h2", null, "Semantic Inspection"),
                    React.createElement("p", null, "No semantic inspection snapshot is available yet for the current Athena document.")));
        }
        return React.createElement("div", { className: 'athena-semantic-inspection' },
            React.createElement("header", { className: 'athena-semantic-inspection__header' },
                React.createElement("div", null,
                    React.createElement("div", { className: 'athena-semantic-inspection__eyebrow' }, "Athena LSP inspection"),
                    React.createElement("h2", null, inspection.systemName ?? 'Unresolved system'),
                    React.createElement("p", null,
                        sessionState.projectName ?? 'Unknown project',
                        " | ",
                        React.createElement("code", null, inspection.uri))),
                React.createElement("div", { className: `athena-semantic-inspection__status athena-semantic-inspection__status--${inspection.status}` }, inspection.status)),
            React.createElement("section", { className: 'athena-semantic-inspection__summary' },
                React.createElement("ul", { className: 'athena-semantic-inspection__summary-list' },
                    React.createElement("li", null,
                        React.createElement("span", null, "Components"),
                        React.createElement("strong", null, inspection.componentCount)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Ports"),
                        React.createElement("strong", null, inspection.portCount)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Connections"),
                        React.createElement("strong", null, inspection.connectionCount)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Diagnostics"),
                        React.createElement("strong", null, inspection.diagnosticsCount)))),
            React.createElement("section", { className: 'athena-semantic-inspection__section' },
                React.createElement("h3", null, "Selected semantic"),
                this.semanticSelectionService.selection
                    ? React.createElement("div", { className: 'athena-semantic-inspection__selection' },
                        React.createElement("strong", null, this.semanticSelectionService.selection.label ?? this.semanticSelectionService.selection.semanticId),
                        React.createElement("br", null),
                        React.createElement("code", null, this.semanticSelectionService.selection.semanticId))
                    : React.createElement("p", null, "No synchronized semantic selection is active yet.")),
            React.createElement("section", { className: 'athena-semantic-inspection__section' },
                React.createElement("h3", null, "Document state"),
                React.createElement("ul", { className: 'athena-semantic-inspection__detail-list' },
                    React.createElement("li", null,
                        React.createElement("span", null, "Version"),
                        React.createElement("strong", null, inspection.version)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Semantic path"),
                        React.createElement("strong", null, sessionState.semanticPath ?? 'frontend -> LSP -> runtime/compiler')),
                    React.createElement("li", null,
                        React.createElement("span", null, "Current editor"),
                        React.createElement("strong", null,
                            React.createElement("code", null, currentEditor.editor.uri.toString()))))),
            React.createElement("section", { className: 'athena-semantic-inspection__section' },
                React.createElement("h3", null, "Diagnostics"),
                inspection.diagnosticSummaries.length === 0
                    ? React.createElement("p", null, "No diagnostics are currently attached to this tracked document state.")
                    : React.createElement("ul", { className: 'athena-semantic-inspection__dense-list' }, inspection.diagnosticSummaries.map(summary => React.createElement("li", { key: summary }, summary)))),
            React.createElement("section", { className: 'athena-semantic-inspection__section' },
                React.createElement("h3", null, "Components"),
                inspection.components.length === 0
                    ? React.createElement("p", null, "No canonical components were derived from the current document state.")
                    : React.createElement("ul", { className: 'athena-semantic-inspection__list' }, inspection.components.map(component => React.createElement("li", { key: component.semanticId, className: `athena-semantic-inspection__item ${this.isSelected(component.semanticId) ? 'athena-semantic-inspection__item--selected' : ''}` },
                        React.createElement("button", { className: 'athena-semantic-inspection__selectable', type: 'button', onClick: () => void this.semanticSelectionService.selectSemanticId(component.semanticId) },
                            React.createElement("span", { className: 'athena-semantic-inspection__item-title' },
                                component.name,
                                " ",
                                React.createElement("span", null,
                                    "(",
                                    component.kind,
                                    ")")),
                            React.createElement("span", { className: 'athena-semantic-inspection__item-meta' }, component.properties)))))),
            React.createElement("section", { className: 'athena-semantic-inspection__section' },
                React.createElement("h3", null, "Ports"),
                inspection.ports.length === 0
                    ? React.createElement("p", null, "No canonical ports were derived from the current document state.")
                    : React.createElement("ul", { className: 'athena-semantic-inspection__list' }, inspection.ports.map(port => React.createElement("li", { key: port.semanticId, className: `athena-semantic-inspection__item ${this.isSelected(port.semanticId) ? 'athena-semantic-inspection__item--selected' : ''}` },
                        React.createElement("button", { className: 'athena-semantic-inspection__selectable', type: 'button', onClick: () => void this.semanticSelectionService.selectSemanticId(port.semanticId) },
                            React.createElement("span", { className: 'athena-semantic-inspection__item-title' }, port.path),
                            React.createElement("span", { className: 'athena-semantic-inspection__item-meta' }, port.properties)))))),
            React.createElement("section", { className: 'athena-semantic-inspection__section' },
                React.createElement("h3", null, "Connections"),
                inspection.connections.length === 0
                    ? React.createElement("p", null, "No canonical connections are present in the current document state.")
                    : React.createElement("ul", { className: 'athena-semantic-inspection__list' }, inspection.connections.map(connection => React.createElement("li", { key: connection.semanticId, className: `athena-semantic-inspection__item ${this.isSelected(connection.semanticId) ? 'athena-semantic-inspection__item--selected' : ''}` },
                        React.createElement("button", { className: 'athena-semantic-inspection__selectable', type: 'button', onClick: () => void this.semanticSelectionService.selectSemanticId(connection.semanticId) },
                            React.createElement("span", { className: 'athena-semantic-inspection__item-title' },
                                connection.fromPath,
                                " ",
                                React.createElement("span", null, "->"),
                                " ",
                                connection.toPath)))))));
    }
    isSelected(semanticId) {
        return this.semanticSelectionService.selection?.semanticId === semanticId;
    }
};
exports.AthenaSemanticInspectionWidget = AthenaSemanticInspectionWidget;
__decorate([
    (0, inversify_1.inject)(browser_1.EditorManager),
    __metadata("design:type", browser_1.EditorManager)
], AthenaSemanticInspectionWidget.prototype, "editorManager", void 0);
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaSemanticInspectionWidget.prototype, "repositorySessionService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService),
    __metadata("design:type", athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService)
], AthenaSemanticInspectionWidget.prototype, "lspEditorBridgeService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_semantic_selection_service_1.AthenaSemanticSelectionService),
    __metadata("design:type", athena_semantic_selection_service_1.AthenaSemanticSelectionService)
], AthenaSemanticInspectionWidget.prototype, "semanticSelectionService", void 0);
__decorate([
    (0, inversify_1.postConstruct)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AthenaSemanticInspectionWidget.prototype, "init", null);
exports.AthenaSemanticInspectionWidget = AthenaSemanticInspectionWidget = AthenaSemanticInspectionWidget_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaSemanticInspectionWidget);
//# sourceMappingURL=athena-semantic-inspection-widget.js.map