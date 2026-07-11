"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaSemanticSelectionService = void 0;
const core_1 = require("@theia/core");
const disposable_1 = require("@theia/core/lib/common/disposable");
const inversify_1 = require("@theia/core/shared/inversify");
const browser_1 = require("@theia/editor/lib/browser");
const athena_lsp_editor_bridge_service_1 = require("./athena-lsp-editor-bridge-service");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
const athena_semantic_selection_model_1 = require("./athena-semantic-selection-model");
/** Frontend-only semantic-selection coordinator for cross-surface synchronization in the M7 workbench. */
let AthenaSemanticSelectionService = class AthenaSemanticSelectionService {
    editorManager;
    lspEditorBridgeService;
    repositorySessionService;
    onDidChangeSelectionEmitter = new core_1.Emitter();
    currentEditorListeners = new disposable_1.DisposableCollection();
    selectionValue;
    decoratedEditor = undefined;
    decorationIds = [];
    refreshHandle;
    activeRepositoryRoot;
    suppressEditorSelectionSync = false;
    get selection() {
        return this.selectionValue;
    }
    get onDidChangeSelection() {
        return this.onDidChangeSelectionEmitter.event;
    }
    async onStart(_app) {
        this.activeRepositoryRoot = this.repositorySessionService.state.repositoryRoot;
        this.bindCurrentEditor(this.editorManager.currentEditor);
        this.repositorySessionService.onDidChangeState(state => {
            const repositoryRootChanged = state.repositoryRoot !== this.activeRepositoryRoot;
            this.activeRepositoryRoot = state.repositoryRoot;
            if (state.lifecycle !== 'ready' || repositoryRootChanged) {
                void this.clearSelection();
                return;
            }
            this.scheduleSelectionRefresh();
        });
        this.editorManager.onCurrentEditorChanged(widget => {
            this.bindCurrentEditor(widget);
            this.scheduleSelectionRefresh();
        });
    }
    async selectSemanticId(semanticId) {
        const resolved = await this.resolveSelection(semanticId);
        const nextSelection = resolved ?? { semanticId };
        this.setSelection(nextSelection);
        return nextSelection;
    }
    async clearSelection() {
        this.setSelection(undefined);
    }
    bindCurrentEditor(widget) {
        this.currentEditorListeners.dispose();
        this.currentEditorListeners = new disposable_1.DisposableCollection();
        if (!this.isAthenaEditor(widget)) {
            this.clearDecorations();
            return;
        }
        this.currentEditorListeners.push(widget.editor.onDocumentContentChanged(() => this.scheduleSelectionRefresh()));
        this.currentEditorListeners.push(widget.editor.onSelectionChanged(() => this.scheduleSelectionRefresh()));
        this.currentEditorListeners.push(disposable_1.Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
                this.refreshHandle = undefined;
            }
        }));
    }
    scheduleSelectionRefresh() {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshSelectionFromCurrentEditor();
        }, 120);
    }
    async refreshSelectionFromCurrentEditor() {
        if (this.suppressEditorSelectionSync) {
            return;
        }
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor)) {
            this.clearDecorations();
            return;
        }
        const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
        const resolvedFromSource = (0, athena_semantic_selection_model_1.resolveSemanticSelectionFromSourceRange)(inspection, currentEditor.editor.uri.toString(), currentEditor.editor.selection);
        if (resolvedFromSource) {
            this.setSelection(resolvedFromSource, false);
            return;
        }
        if (this.selectionValue?.sourceUri === currentEditor.editor.uri.toString()) {
            this.setSelection(undefined, false);
        }
        else {
            this.clearDecorations();
        }
    }
    async resolveSelection(semanticId) {
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor)) {
            return undefined;
        }
        const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
        return (0, athena_semantic_selection_model_1.resolveSemanticSelectionFromInspection)(inspection, semanticId);
    }
    setSelection(selection, applyToEditor = true) {
        if (selectionsEqual(this.selectionValue, selection)) {
            if (applyToEditor) {
                this.applySelectionToCurrentEditor(selection);
            }
            return;
        }
        this.selectionValue = selection;
        this.onDidChangeSelectionEmitter.fire(selection);
        if (applyToEditor) {
            this.applySelectionToCurrentEditor(selection);
        }
        else if (!selection) {
            this.clearDecorations();
        }
    }
    applySelectionToCurrentEditor(selection) {
        const currentEditor = this.editorManager.currentEditor;
        if (!selection || !this.isAthenaEditor(currentEditor) || selection.sourceUri !== currentEditor.editor.uri.toString() || !selection.sourceRange) {
            this.clearDecorations();
            return;
        }
        const editor = currentEditor.editor;
        this.suppressEditorSelectionSync = true;
        try {
            editor.selection = {
                ...selection.sourceRange,
                direction: 'ltr'
            };
            editor.revealRange(selection.sourceRange, { at: 'center' });
        }
        finally {
            window.setTimeout(() => {
                this.suppressEditorSelectionSync = false;
            }, 0);
        }
        this.decoratedEditor = editor;
        this.decorationIds = editor.deltaDecorations({
            oldDecorations: this.decorationIds,
            newDecorations: [{
                    range: selection.sourceRange,
                    options: {
                        className: 'athena-source-selection-decoration'
                    }
                }]
        });
    }
    clearDecorations() {
        if (this.decoratedEditor) {
            this.decoratedEditor.deltaDecorations({
                oldDecorations: this.decorationIds,
                newDecorations: []
            });
        }
        this.decoratedEditor = undefined;
        this.decorationIds = [];
    }
    isAthenaEditor(widget) {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }
};
exports.AthenaSemanticSelectionService = AthenaSemanticSelectionService;
__decorate([
    (0, inversify_1.inject)(browser_1.EditorManager),
    __metadata("design:type", browser_1.EditorManager)
], AthenaSemanticSelectionService.prototype, "editorManager", void 0);
__decorate([
    (0, inversify_1.inject)(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService),
    __metadata("design:type", athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService)
], AthenaSemanticSelectionService.prototype, "lspEditorBridgeService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaSemanticSelectionService.prototype, "repositorySessionService", void 0);
exports.AthenaSemanticSelectionService = AthenaSemanticSelectionService = __decorate([
    (0, inversify_1.injectable)()
], AthenaSemanticSelectionService);
function selectionsEqual(left, right) {
    if (left === right) {
        return true;
    }
    if (!left || !right) {
        return false;
    }
    return left.semanticId === right.semanticId &&
        left.label === right.label &&
        left.kind === right.kind &&
        left.sourceUri === right.sourceUri &&
        rangesEqual(left.sourceRange, right.sourceRange);
}
function rangesEqual(left, right) {
    if (left === right) {
        return true;
    }
    if (!left || !right) {
        return false;
    }
    return left.start.line === right.start.line &&
        left.start.character === right.start.character &&
        left.end.line === right.end.line &&
        left.end.character === right.end.character;
}
//# sourceMappingURL=athena-semantic-selection-service.js.map