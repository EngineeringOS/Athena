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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
var AthenaLspEditorBridgeService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaLspEditorBridgeService = void 0;
const monaco = __importStar(require("@theia/monaco-editor-core"));
const uri_1 = __importDefault(require("@theia/core/lib/common/uri"));
const disposable_1 = require("@theia/core/lib/common/disposable");
const core_1 = require("@theia/core");
const inversify_1 = require("@theia/core/shared/inversify");
const browser_1 = require("@theia/editor/lib/browser");
const problem_manager_1 = require("@theia/markers/lib/browser/problem/problem-manager");
const output_channel_1 = require("@theia/output/lib/browser/output-channel");
const athena_language_definition_1 = require("./athena-language-definition");
const athena_source_mutation_protocol_1 = require("./athena-source-mutation-protocol");
const athena_backend_endpoint_1 = require("./athena-backend-endpoint");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
let AthenaLspEditorBridgeService = class AthenaLspEditorBridgeService {
    static { AthenaLspEditorBridgeService_1 = this; }
    static MARKER_OWNER = 'athena-lsp';
    editorManager;
    problemManager;
    outputChannelManager;
    messageService;
    repositorySessionService;
    openedDocumentVersions = new Map();
    documentSyncOperations = new Map();
    documentSymbolProviderDidChangeEmitter = new monaco.Emitter();
    activeEditorListeners = new disposable_1.DisposableCollection();
    languageProviderListeners = new disposable_1.DisposableCollection();
    semanticBoundaryMessageShown = false;
    onStart(_app) {
        this.registerAthenaLanguage();
        this.outputChannel.appendLine('Athena semantic path ready: frontend -> LSP -> runtime/compiler');
        this.editorManager.onCurrentEditorChanged(widget => {
            this.bindCurrentEditor(widget);
            void this.forwardDidOpen(widget).catch(error => this.reportBridgeFailure(error));
        });
        this.repositorySessionService.onDidChangeState(state => {
            this.notifyDocumentSymbolProviderChanged();
            if (state.lifecycle === 'ready') {
                void this.forwardDidOpen(this.editorManager.currentEditor).catch(error => this.reportBridgeFailure(error));
                return;
            }
            this.problemManager.cleanAllMarkers();
            this.openedDocumentVersions.clear();
            this.documentSyncOperations.clear();
        });
        this.bindCurrentEditor(this.editorManager.currentEditor);
        void this.forwardDidOpen(this.editorManager.currentEditor).catch(error => this.reportBridgeFailure(error));
    }
    registerAthenaLanguage() {
        const alreadyRegistered = monaco.languages.getLanguages().some(language => language.id === athena_language_definition_1.ATHENA_LANGUAGE_ID);
        if (!alreadyRegistered) {
            monaco.languages.register({
                id: athena_language_definition_1.ATHENA_LANGUAGE_ID,
                extensions: ['.athena'],
                aliases: ['Athena', 'athena']
            });
        }
        monaco.languages.setLanguageConfiguration(athena_language_definition_1.ATHENA_LANGUAGE_ID, athena_language_definition_1.athenaLanguageConfiguration);
        this.languageProviderListeners.push(monaco.editor.onDidCreateModel(model => {
            this.ensureAthenaModelLanguage(model);
        }));
        monaco.editor.getModels().forEach(model => {
            this.ensureAthenaModelLanguage(model);
        });
        this.registerAthenaLanguageProviders();
    }
    registerAthenaLanguageProviders() {
        this.languageProviderListeners.dispose();
        this.languageProviderListeners = new disposable_1.DisposableCollection();
        this.languageProviderListeners.push(monaco.languages.setMonarchTokensProvider(athena_language_definition_1.ATHENA_LANGUAGE_ID, athena_language_definition_1.athenaMonarchLanguage));
        this.languageProviderListeners.push(monaco.languages.registerCompletionItemProvider(athena_language_definition_1.ATHENA_LANGUAGE_ID, {
            triggerCharacters: ['.', ' '],
            provideCompletionItems: async (model, position) => {
                const payload = await this.sendLanguageRequest('textDocument/completion', this.toTextDocumentPositionParams(model, position), model);
                const items = Array.isArray(payload) ? payload : payload?.items ?? [];
                return {
                    suggestions: items.map(item => this.toMonacoCompletion(item, model, position))
                };
            }
        }));
        const documentSymbolProvider = {
            onDidChange: this.documentSymbolProviderDidChangeEmitter.event,
            provideDocumentSymbols: async (model) => {
                const payload = await this.sendLanguageRequest('textDocument/documentSymbol', {
                    textDocument: {
                        uri: model.uri.toString()
                    }
                }, model);
                return (payload ?? []).map(symbol => this.toMonacoDocumentSymbol(symbol));
            }
        };
        this.languageProviderListeners.push(monaco.languages.registerDocumentSymbolProvider(athena_language_definition_1.ATHENA_LANGUAGE_ID, documentSymbolProvider));
        this.languageProviderListeners.push(monaco.languages.registerDefinitionProvider(athena_language_definition_1.ATHENA_LANGUAGE_ID, {
            provideDefinition: async (model, position) => {
                const payload = await this.sendLanguageRequest('textDocument/definition', this.toTextDocumentPositionParams(model, position), model);
                return (payload ?? []).map(location => this.toMonacoLocation(location));
            }
        }));
        this.languageProviderListeners.push(monaco.languages.registerReferenceProvider(athena_language_definition_1.ATHENA_LANGUAGE_ID, {
            provideReferences: async (model, position) => {
                const payload = await this.sendLanguageRequest('textDocument/references', {
                    ...this.toTextDocumentPositionParams(model, position),
                    context: {
                        includeDeclaration: true
                    }
                }, model);
                return (payload ?? []).map(location => this.toMonacoLocation(location));
            }
        }));
    }
    async forwardDidOpen(widget) {
        if (!this.isAthenaEditor(widget)) {
            return;
        }
        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
    }
    async forwardDidChange(widget) {
        if (!this.isAthenaEditor(widget)) {
            return;
        }
        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
    }
    async ensureDocumentSynchronized(model) {
        this.ensureAthenaModelLanguage(model);
        const snapshot = await this.resolveDocumentSnapshot(model);
        if (!snapshot || !this.isAthenaDocumentUri(snapshot.uri)) {
            return;
        }
        await this.synchronizeDocumentSnapshot(snapshot);
    }
    async resolveDocumentSnapshot(model) {
        const uri = model.uri.toString();
        const widget = await this.editorManager.getByUri(new uri_1.default(uri));
        if (this.isAthenaEditor(widget)) {
            return this.toWidgetSnapshot(widget);
        }
        return {
            uri,
            version: model.getVersionId(),
            text: model.getValue(),
            languageId: model.getLanguageId()
        };
    }
    toWidgetSnapshot(widget) {
        return {
            uri: widget.editor.uri.toString(),
            version: widget.editor.document.version,
            text: widget.editor.document.getText(),
            languageId: athena_language_definition_1.ATHENA_LANGUAGE_ID
        };
    }
    async synchronizeDocumentSnapshot(snapshot) {
        await this.repositorySessionService.ensureSessionForDocument(snapshot.uri);
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            this.outputChannel.appendLine(`Skipped document synchronization for ${snapshot.uri} because the repository session is ${sessionState.lifecycle}.`);
            return;
        }
        await this.enqueueDocumentSync(snapshot.uri, async () => {
            const currentVersion = this.openedDocumentVersions.get(snapshot.uri);
            if (currentVersion === snapshot.version) {
                return;
            }
            const method = currentVersion === undefined
                ? 'textDocument/didOpen'
                : 'textDocument/didChange';
            const response = await fetch((0, athena_backend_endpoint_1.toAthenaBackendUrl)('athena/lsp/notify'), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    method,
                    params: method === 'textDocument/didOpen'
                        ? {
                            textDocument: {
                                uri: snapshot.uri,
                                languageId: snapshot.languageId,
                                version: snapshot.version,
                                text: snapshot.text
                            }
                        }
                        : {
                            textDocument: {
                                uri: snapshot.uri,
                                version: snapshot.version
                            },
                            contentChanges: [{
                                    text: snapshot.text
                                }]
                        }
                })
            });
            if (!response.ok) {
                const failure = await response.json();
                throw new Error(failure.message ?? `Athena LSP ${method} bridge failed for ${snapshot.uri}`);
            }
            this.openedDocumentVersions.set(snapshot.uri, snapshot.version);
            if (method === 'textDocument/didOpen') {
                this.outputChannel.appendLine(`frontend -> textDocument/didOpen -> Athena LSP -> runtime/compiler :: ${snapshot.uri}`);
                this.outputChannel.show({ preserveFocus: true });
                void this.repositorySessionService.refreshSessionState().catch(error => this.reportBridgeFailure(error));
                if (!this.semanticBoundaryMessageShown) {
                    this.semanticBoundaryMessageShown = true;
                    void this.messageService.info('Athena .athena files now flow through Athena LSP as the sole semantic boundary.');
                }
            }
            else {
                this.outputChannel.appendLine(`frontend -> textDocument/didChange -> Athena LSP diagnostics :: ${snapshot.uri} @ v${snapshot.version}`);
            }
            this.notifyDocumentSymbolProviderChanged();
            void this.syncPublishedDiagnosticsEventually(snapshot.uri).catch(error => this.reportBridgeFailure(error));
        });
    }
    enqueueDocumentSync(uri, task) {
        const previous = this.documentSyncOperations.get(uri) ?? Promise.resolve();
        const next = previous
            .catch(() => undefined)
            .then(task);
        const tracked = next.finally(() => {
            if (this.documentSyncOperations.get(uri) === tracked) {
                this.documentSyncOperations.delete(uri);
            }
        });
        this.documentSyncOperations.set(uri, tracked);
        return tracked;
    }
    bindCurrentEditor(widget) {
        this.activeEditorListeners.dispose();
        this.activeEditorListeners = new disposable_1.DisposableCollection();
        if (!this.isAthenaEditor(widget)) {
            return;
        }
        const model = monaco.editor.getModel(monaco.Uri.parse(widget.editor.uri.toString()));
        this.ensureAthenaModelLanguage(model);
        this.activeEditorListeners.push(widget.editor.onDocumentContentChanged(() => {
            void this.forwardDidChange(widget).catch(error => this.reportBridgeFailure(error));
        }));
    }
    isAthenaEditor(widget) {
        if (!widget) {
            return false;
        }
        return this.isAthenaDocumentUri(widget.editor.uri.toString());
    }
    isAthenaDocumentUri(uri) {
        return uri.toLowerCase().endsWith('.athena');
    }
    currentAthenaEditorModel() {
        const widget = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(widget)) {
            return undefined;
        }
        const model = monaco.editor.getModel(monaco.Uri.parse(widget.editor.uri.toString())) ?? undefined;
        this.ensureAthenaModelLanguage(model);
        return model;
    }
    ensureAthenaModelLanguage(model) {
        if (!model || !this.isAthenaDocumentUri(model.uri.toString()) || model.getLanguageId() === athena_language_definition_1.ATHENA_LANGUAGE_ID) {
            return;
        }
        monaco.editor.setModelLanguage(model, athena_language_definition_1.ATHENA_LANGUAGE_ID);
        this.notifyDocumentSymbolProviderChanged();
    }
    async syncPublishedDiagnostics(uri) {
        const response = await fetch((0, athena_backend_endpoint_1.toAthenaBackendUrl)('athena/lsp/diagnostics', {
            uri,
        }));
        if (!response.ok) {
            const failure = await response.json();
            throw new Error(failure.message ?? `Athena diagnostics fetch failed for ${uri}`);
        }
        const payload = await response.json();
        const diagnostics = payload[0]?.diagnostics ?? [];
        this.problemManager.setMarkers(new uri_1.default(uri), AthenaLspEditorBridgeService_1.MARKER_OWNER, diagnostics);
        this.outputChannel.appendLine(`Athena diagnostics synced to editor and Problems: ${diagnostics.length} item(s) for ${uri}`);
        return diagnostics.length;
    }
    async syncPublishedDiagnosticsEventually(uri) {
        for (let attempt = 0; attempt < 5; attempt += 1) {
            const diagnosticCount = await this.syncPublishedDiagnostics(uri);
            if (diagnosticCount > 0 || attempt === 4) {
                return;
            }
            await new Promise(resolve => window.setTimeout(resolve, 100));
        }
    }
    get outputChannel() {
        return this.outputChannelManager.getChannel('Athena LSP');
    }
    async requestSemanticInspection(widget) {
        if (!this.isAthenaEditor(widget)) {
            return undefined;
        }
        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
        return this.sendLanguageRequest('athena/semanticInspection', {
            textDocument: {
                uri: widget.editor.uri.toString()
            }
        });
    }
    async requestSourceMutationEvaluation(widget) {
        if (!this.isAthenaEditor(widget)) {
            return undefined;
        }
        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
        const request = (0, athena_source_mutation_protocol_1.buildAthenaSourceMutationRequest)(widget.editor.uri.toString(), this.currentAthenaEditorModel());
        return this.sendLanguageRequest(request.method, request.params, request.model);
    }
    async requestRepositoryGraphSession() {
        return this.sendLanguageRequest('athena/repositoryGraphSession', {});
    }
    async requestComponentKnowledgeSession() {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest('athena/componentKnowledgeSession', { marker: 'm15' }, model);
    }
    async requestAuthoringPreview(params) {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest('athena/authoringPreview', params, model);
    }
    async requestAuthoringDecision(params) {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest('athena/authoringDecision', params, model);
    }
    applyAuthoringSourceEdit(edit) {
        const model = monaco.editor.getModel(monaco.Uri.parse(edit.uri)) ?? this.currentAthenaEditorModel();
        if (!model || model.uri.toString() !== edit.uri) {
            throw new Error(`Athena authoring source edit target is not open: ${edit.uri}`);
        }
        model.pushEditOperations([], [{
                range: new monaco.Range(edit.range.start.line + 1, edit.range.start.character + 1, edit.range.end.line + 1, edit.range.end.character + 1),
                text: edit.newText,
                forceMoveMarkers: true,
            }], () => null);
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor) || currentEditor.editor.uri.toString() !== edit.uri || !edit.selectionRange) {
            return;
        }
        currentEditor.editor.selection = {
            ...edit.selectionRange,
            direction: 'ltr',
        };
        currentEditor.editor.revealRange(edit.selectionRange, { at: 'center' });
    }
    async requestProjectionSession() {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest('athena/projectionSession', {}, model);
    }
    async requestProjectionCommand(params) {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest('athena/projectionCommand', params, model);
    }
    async requestGraphCommandIntent(params) {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest('athena/graphCommandIntent', params, model);
    }
    async requestSemanticScmState(params) {
        return this.sendLanguageRequest('athena/semanticScmState', params);
    }
    async requestSemanticHistoryState(params) {
        return this.sendLanguageRequest('athena/semanticHistoryState', params);
    }
    async requestAiReasoning(params) {
        return this.sendLanguageRequest('athena/aiReasoning', params);
    }
    async requestAiReasoningState() {
        return this.sendLanguageRequest('athena/aiReasoningState', {});
    }
    async requestAiReasoningDecision(params) {
        return this.sendLanguageRequest('athena/aiReasoningDecision', params);
    }
    async sendLanguageRequest(method, params, model) {
        if (model) {
            await this.ensureDocumentSynchronized(model);
        }
        if (this.repositorySessionService.state.lifecycle !== 'ready') {
            return undefined;
        }
        const response = await fetch((0, athena_backend_endpoint_1.toAthenaBackendUrl)('athena/lsp/request'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                method,
                params
            })
        });
        if (!response.ok) {
            const failure = await response.json();
            throw new Error(failure.message ?? `Athena LSP request failed for ${method}`);
        }
        const payload = await response.json();
        return payload.result;
    }
    toTextDocumentPositionParams(model, position) {
        return {
            textDocument: {
                uri: model.uri.toString()
            },
            position: {
                line: position.lineNumber - 1,
                character: position.column - 1
            }
        };
    }
    toMonacoCompletion(item, model, position) {
        const label = item.label;
        const documentation = typeof item.documentation === 'string'
            ? item.documentation
            : item.documentation?.value;
        return {
            label,
            detail: item.detail,
            documentation,
            insertText: item.insertText ?? label,
            kind: this.toMonacoCompletionKind(item.kind),
            range: this.toMonacoCompletionRange(model, position)
        };
    }
    toMonacoCompletionRange(model, position) {
        const lineContent = model.getLineContent(position.lineNumber);
        let startColumn = position.column;
        while (startColumn > 1 && /[A-Za-z0-9_.]/.test(lineContent[startColumn - 2] ?? '')) {
            startColumn -= 1;
        }
        return {
            startLineNumber: position.lineNumber,
            startColumn,
            endLineNumber: position.lineNumber,
            endColumn: position.column
        };
    }
    toMonacoCompletionKind(kind) {
        switch (kind) {
            case 5:
                return monaco.languages.CompletionItemKind.Field;
            case 6:
                return monaco.languages.CompletionItemKind.Variable;
            case 7:
                return monaco.languages.CompletionItemKind.Class;
            case 10:
                return monaco.languages.CompletionItemKind.Property;
            case 13:
                return monaco.languages.CompletionItemKind.EnumMember;
            case 14:
                return monaco.languages.CompletionItemKind.Keyword;
            case 18:
                return monaco.languages.CompletionItemKind.Reference;
            default:
                return monaco.languages.CompletionItemKind.Text;
        }
    }
    toMonacoDocumentSymbol(symbol) {
        return {
            name: symbol.name,
            detail: symbol.detail ?? '',
            kind: this.toMonacoSymbolKind(symbol.kind),
            tags: symbol.tags ?? [],
            range: this.toMonacoRange(symbol.range),
            selectionRange: this.toMonacoRange(symbol.selectionRange),
            children: (symbol.children ?? []).map(child => this.toMonacoDocumentSymbol(child))
        };
    }
    toMonacoSymbolKind(kind) {
        switch (kind) {
            case 2:
                return monaco.languages.SymbolKind.Module;
            case 5:
                return monaco.languages.SymbolKind.Class;
            case 7:
                return monaco.languages.SymbolKind.Property;
            case 8:
                return monaco.languages.SymbolKind.Field;
            case 25:
                return monaco.languages.SymbolKind.Function;
            default:
                return monaco.languages.SymbolKind.Object;
        }
    }
    toMonacoLocation(location) {
        return {
            uri: monaco.Uri.parse(location.uri),
            range: this.toMonacoRange(location.range)
        };
    }
    toMonacoRange(range) {
        return {
            startLineNumber: range.start.line + 1,
            startColumn: range.start.character + 1,
            endLineNumber: range.end.line + 1,
            endColumn: range.end.character + 1
        };
    }
    reportBridgeFailure(error) {
        const message = error instanceof Error ? error.message : String(error);
        this.outputChannel.appendLine(`Athena LSP bridge failure: ${message}`);
        void this.messageService.warn(`Athena LSP bridge failure: ${message}`);
    }
    notifyDocumentSymbolProviderChanged() {
        this.documentSymbolProviderDidChangeEmitter.fire();
    }
};
exports.AthenaLspEditorBridgeService = AthenaLspEditorBridgeService;
__decorate([
    (0, inversify_1.inject)(browser_1.EditorManager),
    __metadata("design:type", browser_1.EditorManager)
], AthenaLspEditorBridgeService.prototype, "editorManager", void 0);
__decorate([
    (0, inversify_1.inject)(problem_manager_1.ProblemManager),
    __metadata("design:type", problem_manager_1.ProblemManager)
], AthenaLspEditorBridgeService.prototype, "problemManager", void 0);
__decorate([
    (0, inversify_1.inject)(output_channel_1.OutputChannelManager),
    __metadata("design:type", output_channel_1.OutputChannelManager)
], AthenaLspEditorBridgeService.prototype, "outputChannelManager", void 0);
__decorate([
    (0, inversify_1.inject)(core_1.MessageService),
    __metadata("design:type", core_1.MessageService)
], AthenaLspEditorBridgeService.prototype, "messageService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaLspEditorBridgeService.prototype, "repositorySessionService", void 0);
exports.AthenaLspEditorBridgeService = AthenaLspEditorBridgeService = AthenaLspEditorBridgeService_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaLspEditorBridgeService);
//# sourceMappingURL=athena-lsp-editor-bridge-service.js.map