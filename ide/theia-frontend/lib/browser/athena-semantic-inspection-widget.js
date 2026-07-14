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
const athena_authoring_protocol_1 = require("./athena-authoring-protocol");
const athena_inspector_model_1 = require("./athena-inspector-model");
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
    componentKnowledge;
    reasoningState;
    errorMessage;
    reasoningErrorMessage;
    authoringMessage;
    authoringPreview;
    inspectorDraft;
    loading = false;
    reasoningLoading = false;
    authoringPreviewing = false;
    authoringApplyingDecision = false;
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
            this.reasoningLoading = false;
            this.errorMessage = undefined;
            this.reasoningErrorMessage = undefined;
            this.authoringMessage = undefined;
            this.authoringPreview = undefined;
            this.inspectorDraft = undefined;
            this.inspection = undefined;
            this.componentKnowledge = undefined;
            this.reasoningState = undefined;
            this.update();
            return;
        }
        if (!this.isAthenaEditor(currentEditor)) {
            this.loading = false;
            this.reasoningLoading = false;
            this.errorMessage = undefined;
            this.reasoningErrorMessage = undefined;
            this.authoringMessage = undefined;
            this.authoringPreview = undefined;
            this.inspectorDraft = undefined;
            this.inspection = undefined;
            this.componentKnowledge = undefined;
            this.reasoningState = undefined;
            this.update();
            return;
        }
        const currentUri = currentEditor.editor.uri.toString();
        this.loading = true;
        this.reasoningLoading = true;
        this.errorMessage = undefined;
        this.reasoningErrorMessage = undefined;
        this.update();
        try {
            const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
            const componentKnowledge = await this.lspEditorBridgeService.requestComponentKnowledgeSession();
            const reasoningState = await this.lspEditorBridgeService.requestAiReasoningState();
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            this.inspection = inspection;
            this.componentKnowledge = componentKnowledge;
            this.reasoningState = reasoningState;
        }
        catch (error) {
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            const message = error instanceof Error ? error.message : String(error);
            this.errorMessage = message;
            this.reasoningErrorMessage = message;
            this.authoringMessage = undefined;
            this.authoringPreview = undefined;
            this.inspectorDraft = undefined;
            this.inspection = undefined;
            this.componentKnowledge = undefined;
            this.reasoningState = undefined;
        }
        finally {
            if (this.editorManager.currentEditor?.editor.uri.toString() === currentUri) {
                this.loading = false;
                this.reasoningLoading = false;
                this.update();
            }
        }
    }
    async requestDiagnosticExplanation() {
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor)) {
            return;
        }
        this.reasoningLoading = true;
        this.reasoningErrorMessage = undefined;
        this.update();
        try {
            await this.lspEditorBridgeService.requestAiReasoning({
                requestCategory: 'diagnostic-explanation',
                subjectSemanticIds: this.currentReasoningSubjectIds()
            });
            this.reasoningState = await this.lspEditorBridgeService.requestAiReasoningState();
        }
        catch (error) {
            this.reasoningErrorMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.reasoningLoading = false;
            this.update();
        }
    }
    async applyReasoningDecision(proposalId, decision) {
        this.reasoningLoading = true;
        this.reasoningErrorMessage = undefined;
        this.update();
        try {
            await this.lspEditorBridgeService.requestAiReasoningDecision({
                proposalId,
                decision
            });
            this.reasoningState = await this.lspEditorBridgeService.requestAiReasoningState();
        }
        catch (error) {
            this.reasoningErrorMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.reasoningLoading = false;
            this.update();
        }
    }
    currentReasoningSubjectIds() {
        const selectedSemanticId = this.semanticSelectionService.selection?.semanticId;
        if (selectedSemanticId) {
            return [selectedSemanticId];
        }
        return this.inspection?.components.slice(0, 1).map(component => component.semanticId) ?? [];
    }
    diagnosticProposals() {
        const selectedSemanticId = this.semanticSelectionService.selection?.semanticId;
        return (this.reasoningState?.proposals ?? [])
            .filter(proposal => proposal.proposalCategory === 'diagnostic-explanation')
            .filter(proposal => !selectedSemanticId || proposal.subjectSemanticIds.includes(selectedSemanticId))
            .sort((left, right) => right.proposalId.localeCompare(left.proposalId));
    }
    isAthenaEditor(widget) {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }
    currentSelectedComponentSnapshot() {
        return (0, athena_inspector_model_1.buildAthenaInspectorComponentSnapshot)({
            inspection: this.inspection,
            knowledge: this.componentKnowledge,
            selection: this.semanticSelectionService.selection,
        });
    }
    ensureInspectorDraft(snapshot) {
        if (!snapshot) {
            this.inspectorDraft = undefined;
            this.authoringPreview = undefined;
            return undefined;
        }
        if (!this.inspectorDraft || this.inspectorDraft.semanticId !== snapshot.semanticId) {
            this.inspectorDraft = (0, athena_inspector_model_1.createAthenaInspectorEditDraft)(snapshot);
            this.authoringPreview = undefined;
            this.authoringMessage = undefined;
        }
        return this.inspectorDraft;
    }
    updateInspectorDraft(patch) {
        if (!this.inspectorDraft) {
            return;
        }
        this.inspectorDraft = {
            ...this.inspectorDraft,
            ...patch,
        };
        this.authoringPreview = undefined;
        this.authoringMessage = undefined;
        this.update();
    }
    resetInspectorDraft(snapshot) {
        this.inspectorDraft = (0, athena_inspector_model_1.createAthenaInspectorEditDraft)(snapshot);
        this.authoringPreview = undefined;
        this.authoringMessage = undefined;
        this.update();
    }
    async previewInspectorUpdate(snapshot) {
        const draft = this.ensureInspectorDraft(snapshot);
        if (!draft) {
            return;
        }
        const changes = (0, athena_inspector_model_1.buildAthenaInspectorDraftChanges)({
            snapshot,
            draft,
        });
        if (Object.keys(changes).length === 0) {
            this.authoringPreview = undefined;
            this.authoringMessage = 'No governed property changes are pending for the selected component.';
            this.update();
            return;
        }
        this.authoringPreviewing = true;
        this.authoringMessage = undefined;
        this.update();
        try {
            const submission = await this.lspEditorBridgeService.requestAuthoringPreview((0, athena_authoring_protocol_1.buildUpdateComponentPropertiesPreviewRequest)({
                componentId: snapshot.semanticId,
                name: changes.name,
                label: changes.label,
                description: changes.description,
                preferredImplementationId: changes.preferredImplementationId,
                originDetail: `semantic-inspection:${snapshot.semanticId}`,
            }));
            this.authoringPreview = submission?.preview;
            if (!submission?.preview) {
                this.authoringMessage = 'Athena could not create a governed inspector update preview for the selected component.';
            }
        }
        catch (error) {
            this.authoringMessage = error instanceof Error ? error.message : String(error);
            this.authoringPreview = undefined;
        }
        finally {
            this.authoringPreviewing = false;
            this.update();
        }
    }
    async acceptAuthoringPreview() {
        const preview = this.authoringPreview;
        if (!preview) {
            return;
        }
        this.authoringApplyingDecision = true;
        this.authoringMessage = undefined;
        this.update();
        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision((0, athena_authoring_protocol_1.buildAuthoringDecisionRequest)({
                previewId: preview.previewId,
                intentId: preview.intentId,
                decision: 'accepted',
                note: 'Semantic inspection update accepted.',
            }));
            if (!decision?.sourceEdit) {
                throw new Error('Athena accepted the inspector preview but did not return a governed source edit.');
            }
            this.lspEditorBridgeService.applyAuthoringSourceEdit(decision.sourceEdit);
            if (decision.sourceEdit.suggestedSemanticId) {
                window.setTimeout(() => {
                    void this.semanticSelectionService.selectSemanticId(decision.sourceEdit.suggestedSemanticId).catch(error => {
                        this.authoringMessage = error instanceof Error ? error.message : String(error);
                        this.update();
                    });
                }, 180);
            }
            this.authoringPreview = undefined;
            this.scheduleRefresh();
        }
        catch (error) {
            this.authoringMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.authoringApplyingDecision = false;
            this.update();
        }
    }
    async rejectAuthoringPreview() {
        const preview = this.authoringPreview;
        if (!preview) {
            return;
        }
        this.authoringApplyingDecision = true;
        this.authoringMessage = undefined;
        this.update();
        try {
            await this.lspEditorBridgeService.requestAuthoringDecision((0, athena_authoring_protocol_1.buildAuthoringDecisionRequest)({
                previewId: preview.previewId,
                intentId: preview.intentId,
                decision: 'rejected',
                note: 'Semantic inspection update rejected.',
            }));
            this.authoringPreview = undefined;
        }
        catch (error) {
            this.authoringMessage = error instanceof Error ? error.message : String(error);
        }
        finally {
            this.authoringApplyingDecision = false;
            this.update();
        }
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
        const selectedComponentSnapshot = this.currentSelectedComponentSnapshot();
        const inspectorDraft = this.ensureInspectorDraft(selectedComponentSnapshot);
        const draftChanges = selectedComponentSnapshot && inspectorDraft
            ? (0, athena_inspector_model_1.buildAthenaInspectorDraftChanges)({
                snapshot: selectedComponentSnapshot,
                draft: inspectorDraft,
            })
            : {};
        const hasDraftChanges = Object.keys(draftChanges).length > 0;
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
                React.createElement("h3", null, "Selected component"),
                !selectedComponentSnapshot
                    ? React.createElement("p", null, "Select one component or one of its ports to inspect governed concept, implementation, port, and physical-trait details.")
                    : React.createElement(React.Fragment, null,
                        React.createElement("div", { className: 'athena-semantic-inspection__selection' },
                            React.createElement("strong", null, selectedComponentSnapshot.name),
                            React.createElement("br", null),
                            React.createElement("code", null, selectedComponentSnapshot.semanticId)),
                        inspectorDraft
                            ? React.createElement(React.Fragment, null,
                                React.createElement("h4", { className: 'athena-semantic-inspection__subheading' }, "Editable properties"),
                                React.createElement("div", { className: 'athena-semantic-inspection__control-grid' },
                                    React.createElement("div", { className: 'athena-semantic-inspection__control' },
                                        React.createElement("label", { htmlFor: 'athena-inspector-name' }, "Name"),
                                        React.createElement("input", { id: 'athena-inspector-name', type: 'text', value: inspectorDraft.name, onChange: event => this.updateInspectorDraft({ name: event.target.value }) })),
                                    React.createElement("div", { className: 'athena-semantic-inspection__control' },
                                        React.createElement("label", { htmlFor: 'athena-inspector-label' }, "Label"),
                                        React.createElement("input", { id: 'athena-inspector-label', type: 'text', value: inspectorDraft.label, onChange: event => this.updateInspectorDraft({ label: event.target.value }) })),
                                    React.createElement("div", { className: 'athena-semantic-inspection__control athena-semantic-inspection__control--wide' },
                                        React.createElement("label", { htmlFor: 'athena-inspector-description' }, "Description"),
                                        React.createElement("textarea", { id: 'athena-inspector-description', value: inspectorDraft.description, onChange: event => this.updateInspectorDraft({ description: event.target.value }) })),
                                    React.createElement("div", { className: 'athena-semantic-inspection__control athena-semantic-inspection__control--wide' },
                                        React.createElement("label", { htmlFor: 'athena-inspector-implementation' }, "Implementation"),
                                        React.createElement("select", { id: 'athena-inspector-implementation', value: inspectorDraft.preferredImplementationId ?? '', onChange: event => this.updateInspectorDraft({
                                                preferredImplementationId: event.target.value || undefined,
                                            }), disabled: selectedComponentSnapshot.implementationOptions.length === 0 }, selectedComponentSnapshot.implementationOptions.length === 0
                                            ? React.createElement("option", { value: '' }, "No governed implementation choices")
                                            : selectedComponentSnapshot.implementationOptions.map(option => React.createElement("option", { key: option.implementationId, value: option.implementationId },
                                                option.displayName,
                                                " | ",
                                                option.vendorId,
                                                " | ",
                                                option.vendorPartNumber))),
                                        selectedComponentSnapshot.implementationOptions.length > 0
                                            ? React.createElement("div", { className: 'athena-semantic-inspection__hint' }, "Governed implementation choices come from the active component knowledge catalog.")
                                            : undefined)),
                                React.createElement("div", { className: 'athena-semantic-inspection__actions' },
                                    React.createElement("button", { className: 'athena-semantic-inspection__action', type: 'button', disabled: !hasDraftChanges || this.authoringPreviewing || this.authoringApplyingDecision, onClick: () => void this.previewInspectorUpdate(selectedComponentSnapshot) }, this.authoringPreviewing ? 'Previewing...' : 'Preview update'),
                                    React.createElement("button", { className: 'athena-semantic-inspection__action athena-semantic-inspection__action--secondary', type: 'button', disabled: this.authoringPreviewing || this.authoringApplyingDecision, onClick: () => this.resetInspectorDraft(selectedComponentSnapshot) }, "Reset")),
                                this.authoringPreview || this.authoringMessage
                                    ? React.createElement("div", { className: 'athena-semantic-inspection__preview' },
                                        React.createElement("div", { className: 'athena-semantic-inspection__preview-header' },
                                            React.createElement("strong", null, this.authoringPreview?.title ?? 'Pending update'),
                                            this.authoringPreview
                                                ? React.createElement("span", { className: `athena-semantic-inspection__status athena-semantic-inspection__status--${this.authoringPreview.status}` }, this.authoringPreview.status)
                                                : undefined),
                                        this.authoringMessage
                                            ? React.createElement("p", null, this.authoringMessage)
                                            : undefined,
                                        this.authoringPreview
                                            ? React.createElement(React.Fragment, null,
                                                React.createElement("ul", { className: 'athena-semantic-inspection__list' }, this.authoringPreview.changes.map(change => React.createElement("li", { key: `${change.kind}:${change.title}`, className: 'athena-semantic-inspection__item' },
                                                    React.createElement("div", { className: 'athena-semantic-inspection__item-title' }, change.title),
                                                    change.summary
                                                        ? React.createElement("div", { className: 'athena-semantic-inspection__item-meta' }, change.summary)
                                                        : undefined))),
                                                React.createElement("div", { className: 'athena-semantic-inspection__actions' },
                                                    React.createElement("button", { className: 'athena-semantic-inspection__action', type: 'button', disabled: this.authoringApplyingDecision, onClick: () => void this.acceptAuthoringPreview() }, this.authoringApplyingDecision ? 'Applying...' : 'Accept'),
                                                    React.createElement("button", { className: 'athena-semantic-inspection__action athena-semantic-inspection__action--secondary', type: 'button', disabled: this.authoringApplyingDecision, onClick: () => void this.rejectAuthoringPreview() }, "Reject")))
                                            : undefined)
                                    : undefined)
                            : undefined,
                        React.createElement("ul", { className: 'athena-semantic-inspection__detail-list' },
                            React.createElement("li", null,
                                React.createElement("span", null, "Kind"),
                                React.createElement("strong", null, selectedComponentSnapshot.kind)),
                            React.createElement("li", null,
                                React.createElement("span", null, "Concept"),
                                React.createElement("strong", null,
                                    selectedComponentSnapshot.conceptDisplayName,
                                    " ",
                                    React.createElement("code", null, selectedComponentSnapshot.conceptId))),
                            React.createElement("li", null,
                                React.createElement("span", null, "Reference"),
                                React.createElement("strong", null,
                                    React.createElement("code", null, selectedComponentSnapshot.authoredComponentReference))),
                            React.createElement("li", null,
                                React.createElement("span", null, "Implementation"),
                                React.createElement("strong", null, selectedComponentSnapshot.vendorPartNumber ? `${selectedComponentSnapshot.vendorId ?? 'vendor'} / ${selectedComponentSnapshot.vendorPartNumber}` : 'Concept only')),
                            React.createElement("li", null,
                                React.createElement("span", null, "Knowledge source"),
                                React.createElement("strong", null, "Active component knowledge session"))),
                        React.createElement("h4", { className: 'athena-semantic-inspection__subheading' }, "Semantic ports"),
                        selectedComponentSnapshot.ports.length === 0
                            ? React.createElement("p", null, "No governed semantic ports were resolved for this selected component.")
                            : React.createElement("ul", { className: 'athena-semantic-inspection__list' }, selectedComponentSnapshot.ports.map(port => React.createElement("li", { key: port.semanticId, className: `athena-semantic-inspection__item ${port.selected ? 'athena-semantic-inspection__item--selected' : ''}` },
                                React.createElement("button", { className: 'athena-semantic-inspection__selectable', type: 'button', onClick: () => void this.semanticSelectionService.selectSemanticId(port.semanticId) },
                                    React.createElement("span", { className: 'athena-semantic-inspection__item-title' }, port.label),
                                    React.createElement("span", { className: 'athena-semantic-inspection__item-meta' },
                                        port.direction,
                                        " | ",
                                        port.signalFamilyId,
                                        " | ",
                                        port.roleId,
                                        port.connectedPaths.length > 0 ? ` | connected to ${port.connectedPaths.join(', ')}` : ''))))),
                        React.createElement("h4", { className: 'athena-semantic-inspection__subheading' }, "Physical traits"),
                        selectedComponentSnapshot.physicalTraits.length === 0
                            ? React.createElement("p", null, "No governed physical traits were resolved for this selected component.")
                            : React.createElement("ul", { className: 'athena-semantic-inspection__dense-list' }, selectedComponentSnapshot.physicalTraits.map(trait => React.createElement("li", { key: `${selectedComponentSnapshot.semanticId}:${trait.displayName}` },
                                React.createElement("strong", null, trait.displayName),
                                " ",
                                trait.widthMillimeters,
                                "x",
                                trait.heightMillimeters,
                                "x",
                                trait.depthMillimeters,
                                " mm | ",
                                trait.mountingTypeId))))),
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
                React.createElement("h3", null, "AI diagnostic explanation"),
                React.createElement("div", { className: 'athena-ai-reasoning__actions' },
                    React.createElement("button", { className: 'athena-ai-reasoning__action', type: 'button', onClick: () => void this.requestDiagnosticExplanation(), disabled: this.reasoningLoading || inspection.diagnosticsCount === 0 }, this.reasoningLoading ? 'Explaining...' : 'Explain diagnostic')),
                this.reasoningErrorMessage
                    ? React.createElement("p", null, this.reasoningErrorMessage)
                    : undefined,
                this.diagnosticProposals().length === 0
                    ? React.createElement("p", null, "No diagnostic explanation proposal has been recorded yet for the current focus.")
                    : React.createElement("ul", { className: 'athena-ai-reasoning__proposal-list' }, this.diagnosticProposals().map(proposal => this.renderReasoningProposal(proposal)))),
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
    renderReasoningProposal(proposal) {
        return React.createElement("li", { key: proposal.proposalId, className: 'athena-ai-reasoning__proposal' },
            React.createElement("div", { className: 'athena-ai-reasoning__proposal-header' },
                React.createElement("strong", null, proposal.summary),
                React.createElement("span", { className: `athena-ai-reasoning__status athena-ai-reasoning__status--${proposal.decisionState}` }, proposal.decisionState)),
            React.createElement("div", { className: 'athena-ai-reasoning__meta' },
                React.createElement("span", null, proposal.providerStatus),
                proposal.providerId ? React.createElement("span", null, proposal.providerId) : undefined,
                React.createElement("code", null, proposal.proposalId)),
            React.createElement("p", null, proposal.response),
            React.createElement("ul", { className: 'athena-ai-reasoning__evidence-list' }, proposal.evidence.map(evidence => React.createElement("li", { key: `${proposal.proposalId}:${evidence.referenceId}` },
                React.createElement("strong", null, evidence.kind),
                " ",
                React.createElement("code", null, evidence.referenceId),
                " ",
                evidence.summary))),
            proposal.decisionState === 'unresolved'
                ? React.createElement("div", { className: 'athena-ai-reasoning__actions' },
                    React.createElement("button", { className: 'athena-ai-reasoning__action', type: 'button', onClick: () => void this.applyReasoningDecision(proposal.proposalId, 'accepted') }, "Accept"),
                    React.createElement("button", { className: 'athena-ai-reasoning__action athena-ai-reasoning__action--secondary', type: 'button', onClick: () => void this.applyReasoningDecision(proposal.proposalId, 'dismissed') }, "Dismiss"))
                : undefined);
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