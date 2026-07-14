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
var AthenaComponentPanelWidget_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaComponentPanelWidget = void 0;
const React = __importStar(require("@theia/core/shared/react"));
const react_widget_1 = require("@theia/core/lib/browser/widgets/react-widget");
const disposable_1 = require("@theia/core/lib/common/disposable");
const inversify_1 = require("@theia/core/shared/inversify");
const browser_1 = require("@theia/editor/lib/browser");
const athena_authoring_protocol_1 = require("./athena-authoring-protocol");
const athena_component_panel_model_1 = require("./athena-component-panel-model");
const athena_lsp_editor_bridge_service_1 = require("./athena-lsp-editor-bridge-service");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
const athena_semantic_selection_service_1 = require("./athena-semantic-selection-service");
let AthenaComponentPanelWidget = class AthenaComponentPanelWidget extends react_widget_1.ReactWidget {
    static { AthenaComponentPanelWidget_1 = this; }
    static ID = 'athena.componentPanel';
    static LABEL = 'Components';
    editorManager;
    repositorySessionService;
    lspEditorBridgeService;
    semanticSelectionService;
    currentEditorListeners = new disposable_1.DisposableCollection();
    knowledge;
    groups = [];
    loading = false;
    errorMessage;
    previewMessage;
    activePreview;
    previewingConceptId;
    applyingDecision = false;
    refreshHandle;
    init() {
        this.id = AthenaComponentPanelWidget_1.ID;
        this.title.label = AthenaComponentPanelWidget_1.LABEL;
        this.title.caption = AthenaComponentPanelWidget_1.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-package';
        this.addClass('athena-component-panel-widget');
        this.toDispose.push(this.currentEditorListeners);
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
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
            void this.refreshCatalog();
        }, 120);
    }
    async refreshCatalog() {
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.knowledge = undefined;
            this.groups = [];
            this.activePreview = undefined;
            this.previewMessage = undefined;
            this.update();
            return;
        }
        this.loading = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const knowledge = await this.lspEditorBridgeService.requestComponentKnowledgeSession();
            this.knowledge = knowledge;
            this.groups = (0, athena_component_panel_model_1.buildAthenaComponentPanelGroups)(knowledge?.availableComponents ?? []);
            if (knowledge?.status === 'unavailable') {
                this.errorMessage = knowledge.unavailableReason ?? 'Athena component knowledge is unavailable.';
            }
        }
        catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.knowledge = undefined;
            this.groups = [];
        }
        finally {
            this.loading = false;
            this.update();
        }
    }
    isAthenaEditor(widget) {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }
    canPreviewInsert() {
        return !!this.knowledge?.systemSemanticId && this.isAthenaEditor(this.editorManager.currentEditor);
    }
    async previewComponentInsertion(item) {
        const knowledge = this.knowledge;
        if (!knowledge?.systemSemanticId) {
            this.previewMessage = 'Athena cannot preview insertion until the active system semantic identity is available.';
            this.update();
            return;
        }
        this.previewingConceptId = item.conceptId;
        this.previewMessage = undefined;
        this.update();
        try {
            const submission = await this.lspEditorBridgeService.requestAuthoringPreview((0, athena_authoring_protocol_1.buildCreateComponentPreviewRequest)({
                systemSemanticId: knowledge.systemSemanticId,
                conceptId: item.conceptId,
                preferredImplementationId: item.preferredImplementation?.implementationId,
                originDetail: `component-panel:${item.conceptId}`,
            }));
            this.activePreview = submission?.preview;
            if (!submission?.preview) {
                this.previewMessage = 'Athena could not create a guided insertion preview for the selected component.';
            }
        }
        catch (error) {
            this.previewMessage = error instanceof Error ? error.message : String(error);
            this.activePreview = undefined;
        }
        finally {
            this.previewingConceptId = undefined;
            this.update();
        }
    }
    async acceptActivePreview() {
        const preview = this.activePreview;
        if (!preview) {
            return;
        }
        this.applyingDecision = true;
        this.previewMessage = undefined;
        this.update();
        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision((0, athena_authoring_protocol_1.buildAuthoringDecisionRequest)({
                previewId: preview.previewId,
                intentId: preview.intentId,
                decision: 'accepted',
                note: 'Component panel placement accepted.',
            }));
            if (!decision?.sourceEdit) {
                throw new Error('Athena accepted the preview but did not return a governed source edit.');
            }
            this.lspEditorBridgeService.applyAuthoringSourceEdit(decision.sourceEdit);
            if (decision.sourceEdit.suggestedSemanticId) {
                window.setTimeout(() => {
                    void this.semanticSelectionService.selectSemanticId(decision.sourceEdit.suggestedSemanticId).catch(error => {
                        this.previewMessage = error instanceof Error ? error.message : String(error);
                        this.update();
                    });
                }, 180);
            }
            this.activePreview = undefined;
            this.scheduleRefresh();
        }
        catch (error) {
            this.previewMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.applyingDecision = false;
            this.update();
        }
    }
    async rejectActivePreview() {
        const preview = this.activePreview;
        if (!preview) {
            return;
        }
        this.applyingDecision = true;
        this.previewMessage = undefined;
        this.update();
        try {
            await this.lspEditorBridgeService.requestAuthoringDecision((0, athena_authoring_protocol_1.buildAuthoringDecisionRequest)({
                previewId: preview.previewId,
                intentId: preview.intentId,
                decision: 'rejected',
                note: 'Component panel placement rejected.',
            }));
            this.activePreview = undefined;
        }
        catch (error) {
            this.previewMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.applyingDecision = false;
            this.update();
        }
    }
    render() {
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            return React.createElement("div", { className: 'athena-component-panel' },
                React.createElement("section", { className: 'athena-component-panel__empty' },
                    React.createElement("h2", null, "Components"),
                    React.createElement("p", null, sessionState.message)));
        }
        if (this.errorMessage) {
            return React.createElement("div", { className: 'athena-component-panel' },
                React.createElement("section", { className: 'athena-component-panel__empty athena-component-panel__empty--error' },
                    React.createElement("h2", null, "Components"),
                    React.createElement("p", null, this.errorMessage)));
        }
        if (this.loading && !this.knowledge) {
            return React.createElement("div", { className: 'athena-component-panel' },
                React.createElement("section", { className: 'athena-component-panel__empty' },
                    React.createElement("h2", null, "Components"),
                    React.createElement("p", null, "Loading governed component knowledge from the active Athena runtime session.")));
        }
        const knowledge = this.knowledge;
        if (!knowledge) {
            return React.createElement("div", { className: 'athena-component-panel' },
                React.createElement("section", { className: 'athena-component-panel__empty' },
                    React.createElement("h2", null, "Components"),
                    React.createElement("p", null, "No governed component catalog is available yet for the active repository.")));
        }
        return React.createElement("div", { className: 'athena-component-panel' },
            React.createElement("header", { className: 'athena-component-panel__header' },
                React.createElement("div", null,
                    React.createElement("div", { className: 'athena-component-panel__eyebrow' }, "Guided authoring foundation"),
                    React.createElement("h2", null, "Available components"),
                    React.createElement("p", null,
                        knowledge.projectName,
                        " | ",
                        React.createElement("code", null, knowledge.semanticPath))),
                React.createElement("div", { className: `athena-component-panel__status athena-component-panel__status--${knowledge.status}` }, knowledge.status)),
            React.createElement("section", { className: 'athena-component-panel__summary' },
                React.createElement("ul", { className: 'athena-component-panel__summary-list' },
                    React.createElement("li", null,
                        React.createElement("span", null, "Concepts"),
                        React.createElement("strong", null, knowledge.activeConceptCount)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Implementations"),
                        React.createElement("strong", null, knowledge.activeImplementationCount)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Contributors"),
                        React.createElement("strong", null, knowledge.contributingPluginIds.length)))),
            React.createElement("section", { className: 'athena-component-panel__section' },
                React.createElement("h3", null, "Scope"),
                React.createElement("ul", { className: 'athena-component-panel__detail-list' },
                    React.createElement("li", null,
                        React.createElement("span", null, "Proof slice"),
                        React.createElement("strong", null, "Electrical / Siemens-first")),
                    React.createElement("li", null,
                        React.createElement("span", null, "Source"),
                        React.createElement("strong", null, "Active component knowledge packs")),
                    React.createElement("li", null,
                        React.createElement("span", null, "Insertion"),
                        React.createElement("strong", null, this.canPreviewInsert() ? 'Preview-first ready' : 'Open one .athena editor to insert')))),
            this.activePreview || this.previewMessage
                ? React.createElement("section", { className: 'athena-component-panel__section' },
                    React.createElement("h3", null, "Pending insertion"),
                    this.previewMessage
                        ? React.createElement("p", null, this.previewMessage)
                        : undefined,
                    this.activePreview
                        ? React.createElement("div", { className: 'athena-component-panel__preview' },
                            React.createElement("div", { className: 'athena-component-panel__preview-header' },
                                React.createElement("strong", null, this.activePreview.title),
                                React.createElement("span", { className: `athena-component-panel__status athena-component-panel__status--${this.activePreview.status}` }, this.activePreview.status)),
                            React.createElement("ul", { className: 'athena-component-panel__list' }, this.activePreview.changes.map(change => React.createElement("li", { key: `${change.kind}:${change.title}`, className: 'athena-component-panel__item' },
                                React.createElement("div", { className: 'athena-component-panel__item-header' },
                                    React.createElement("span", { className: 'athena-component-panel__item-title' }, change.title),
                                    React.createElement("span", { className: 'athena-component-panel__pill' }, change.kind)),
                                change.summary
                                    ? React.createElement("p", { className: 'athena-component-panel__item-summary' }, change.summary)
                                    : undefined))),
                            React.createElement("div", { className: 'athena-component-panel__actions' },
                                React.createElement("button", { className: 'athena-component-panel__action', type: 'button', disabled: this.applyingDecision, onClick: () => void this.acceptActivePreview() }, this.applyingDecision ? 'Applying...' : 'Accept'),
                                React.createElement("button", { className: 'athena-component-panel__action athena-component-panel__action--secondary', type: 'button', disabled: this.applyingDecision, onClick: () => void this.rejectActivePreview() }, "Reject")))
                        : undefined)
                : undefined,
            this.groups.length === 0
                ? React.createElement("section", { className: 'athena-component-panel__section' },
                    React.createElement("h3", null, "Available components"),
                    React.createElement("p", null, "No active component concepts are currently available from the hosted knowledge set."))
                : this.groups.map(group => React.createElement("section", { key: group.categoryId, className: 'athena-component-panel__section' },
                    React.createElement("h3", null, group.label),
                    React.createElement("ul", { className: 'athena-component-panel__list' }, group.items.map(item => this.renderItem(item))))));
    }
    renderItem(item) {
        const preferred = item.preferredImplementation;
        const previewing = this.previewingConceptId === item.conceptId;
        return React.createElement("li", { key: item.conceptId, className: 'athena-component-panel__item' },
            React.createElement("div", { className: 'athena-component-panel__item-header' },
                React.createElement("span", { className: 'athena-component-panel__item-title' }, item.displayName),
                preferred
                    ? React.createElement("span", { className: 'athena-component-panel__pill' }, preferred.vendorId)
                    : undefined),
            React.createElement("div", { className: 'athena-component-panel__item-meta' },
                React.createElement("code", null, item.conceptId),
                preferred
                    ? React.createElement("span", null,
                        preferred.displayName,
                        " | ",
                        React.createElement("code", null, preferred.vendorPartNumber))
                    : React.createElement("span", null, "No implementation published yet.")),
            item.summary
                ? React.createElement("p", { className: 'athena-component-panel__item-summary' }, item.summary)
                : undefined,
            React.createElement("div", { className: 'athena-component-panel__actions' },
                React.createElement("button", { className: 'athena-component-panel__action', type: 'button', disabled: !this.canPreviewInsert() || previewing || this.applyingDecision, onClick: () => void this.previewComponentInsertion(item) }, previewing ? 'Previewing...' : 'Preview insert')));
    }
};
exports.AthenaComponentPanelWidget = AthenaComponentPanelWidget;
__decorate([
    (0, inversify_1.inject)(browser_1.EditorManager),
    __metadata("design:type", browser_1.EditorManager)
], AthenaComponentPanelWidget.prototype, "editorManager", void 0);
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaComponentPanelWidget.prototype, "repositorySessionService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService),
    __metadata("design:type", athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService)
], AthenaComponentPanelWidget.prototype, "lspEditorBridgeService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_semantic_selection_service_1.AthenaSemanticSelectionService),
    __metadata("design:type", athena_semantic_selection_service_1.AthenaSemanticSelectionService)
], AthenaComponentPanelWidget.prototype, "semanticSelectionService", void 0);
__decorate([
    (0, inversify_1.postConstruct)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AthenaComponentPanelWidget.prototype, "init", null);
exports.AthenaComponentPanelWidget = AthenaComponentPanelWidget = AthenaComponentPanelWidget_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaComponentPanelWidget);
//# sourceMappingURL=athena-component-panel-widget.js.map