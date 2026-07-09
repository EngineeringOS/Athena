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
var AthenaRepositoryGraphWidget_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaRepositoryGraphWidget = void 0;
const React = __importStar(require("@theia/core/shared/react"));
const react_widget_1 = require("@theia/core/lib/browser/widgets/react-widget");
const disposable_1 = require("@theia/core/lib/common/disposable");
const inversify_1 = require("@theia/core/shared/inversify");
const browser_1 = require("@theia/editor/lib/browser");
const athena_lsp_editor_bridge_service_1 = require("./athena-lsp-editor-bridge-service");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
let AthenaRepositoryGraphWidget = class AthenaRepositoryGraphWidget extends react_widget_1.ReactWidget {
    static { AthenaRepositoryGraphWidget_1 = this; }
    static ID = 'athena.repositoryGraph';
    static LABEL = 'Repository Graph';
    editorManager;
    repositorySessionService;
    lspEditorBridgeService;
    currentEditorListeners = new disposable_1.DisposableCollection();
    graphSession;
    errorMessage;
    loading = false;
    refreshHandle;
    init() {
        this.id = AthenaRepositoryGraphWidget_1.ID;
        this.title.label = AthenaRepositoryGraphWidget_1.LABEL;
        this.title.caption = AthenaRepositoryGraphWidget_1.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-list-tree';
        this.addClass('athena-repository-graph-widget');
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
    isAthenaEditor(widget) {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }
    scheduleRefresh() {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshGraphSession();
        }, 120);
    }
    async refreshGraphSession() {
        const sessionState = this.repositorySessionService.state;
        const currentRepositoryRoot = sessionState.repositoryRoot;
        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.graphSession = undefined;
            this.update();
            return;
        }
        this.loading = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const graphSession = await this.lspEditorBridgeService.requestRepositoryGraphSession();
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.graphSession = graphSession;
        }
        catch (error) {
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.graphSession = undefined;
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
            return React.createElement("div", { className: 'athena-repository-graph' },
                React.createElement("section", { className: 'athena-repository-graph__empty' },
                    React.createElement("h2", null, "Repository Graph"),
                    React.createElement("p", null, sessionState.message)));
        }
        if (this.errorMessage) {
            return React.createElement("div", { className: 'athena-repository-graph' },
                React.createElement("section", { className: 'athena-repository-graph__empty athena-repository-graph__empty--error' },
                    React.createElement("h2", null, "Repository Graph"),
                    React.createElement("p", null, this.errorMessage)));
        }
        if (this.loading && !this.graphSession) {
            return React.createElement("div", { className: 'athena-repository-graph' },
                React.createElement("section", { className: 'athena-repository-graph__empty' },
                    React.createElement("h2", null, "Repository Graph"),
                    React.createElement("p", null, "Loading the canonical package graph from Athena LSP.")));
        }
        const graphSession = this.graphSession;
        if (!graphSession) {
            return React.createElement("div", { className: 'athena-repository-graph' },
                React.createElement("section", { className: 'athena-repository-graph__empty' },
                    React.createElement("h2", null, "Repository Graph"),
                    React.createElement("p", null, "No repository graph payload is available yet for the active Athena session.")));
        }
        const statusTone = graphSession.isValid && graphSession.diagnostics.length === 0 ? 'clean' : 'issue';
        const lockStateTone = graphSession.isValid ? 'clean' : 'issue';
        return React.createElement("div", { className: 'athena-repository-graph' },
            React.createElement("header", { className: 'athena-repository-graph__header' },
                React.createElement("div", null,
                    React.createElement("div", { className: 'athena-repository-graph__eyebrow' }, "Athena package graph"),
                    React.createElement("h2", null, graphSession.primaryPackageName),
                    React.createElement("p", null,
                        React.createElement("code", null, graphSession.repositoryRoot))),
                React.createElement("div", { className: `athena-repository-graph__status athena-repository-graph__status--${statusTone}` }, graphSession.isValid ? 'ready' : 'issues')),
            React.createElement("section", { className: 'athena-repository-graph__metrics' },
                React.createElement("article", { className: 'athena-repository-graph__metric' },
                    React.createElement("span", { className: 'athena-repository-graph__metric-value' }, graphSession.resolvedPackages.length),
                    React.createElement("span", { className: 'athena-repository-graph__metric-label' }, "Resolved packages")),
                React.createElement("article", { className: 'athena-repository-graph__metric' },
                    React.createElement("span", { className: 'athena-repository-graph__metric-value' }, graphSession.manifestDependencies.length),
                    React.createElement("span", { className: 'athena-repository-graph__metric-label' }, "Manifest dependencies")),
                React.createElement("article", { className: 'athena-repository-graph__metric' },
                    React.createElement("span", { className: 'athena-repository-graph__metric-value' }, graphSession.diagnostics.length),
                    React.createElement("span", { className: 'athena-repository-graph__metric-label' }, "Package diagnostics")),
                React.createElement("article", { className: 'athena-repository-graph__metric' },
                    React.createElement("span", { className: 'athena-repository-graph__metric-value' }, graphSession.lockState),
                    React.createElement("span", { className: 'athena-repository-graph__metric-label' }, "Lock state"))),
            React.createElement("section", { className: 'athena-repository-graph__section' },
                React.createElement("h3", null, "Repository contract"),
                React.createElement("ul", null,
                    React.createElement("li", null,
                        "Manifest: ",
                        React.createElement("code", null, graphSession.manifestPath)),
                    React.createElement("li", null,
                        "Lock: ",
                        React.createElement("code", null, graphSession.lockPath)),
                    React.createElement("li", null,
                        "Governed source root: ",
                        React.createElement("code", null, graphSession.sourceRootPath)),
                    React.createElement("li", null,
                        "Authored source: ",
                        React.createElement("code", null, graphSession.sourcePath)),
                    React.createElement("li", null,
                        "Project key: ",
                        graphSession.projectName),
                    React.createElement("li", null,
                        "Semantic path: ",
                        graphSession.semanticPath),
                    React.createElement("li", null,
                        "Last Athena editor: ",
                        graphSession.lastOpenedDocumentUri ?? 'Not opened yet'),
                    React.createElement("li", null,
                        "Lock status:",
                        React.createElement("span", { className: `athena-repository-graph__pill athena-repository-graph__pill--${lockStateTone}` }, graphSession.lockState)))),
            React.createElement("section", { className: 'athena-repository-graph__section' },
                React.createElement("h3", null, "Manifest dependency intent"),
                graphSession.manifestDependencies.length === 0
                    ? React.createElement("p", null, "The primary package currently declares no external package dependencies.")
                    : React.createElement("ul", { className: 'athena-repository-graph__list' }, graphSession.manifestDependencies.map(dependency => this.renderDependency(dependency)))),
            React.createElement("section", { className: 'athena-repository-graph__section' },
                React.createElement("h3", null, "Resolved package graph"),
                graphSession.resolvedPackages.length === 0
                    ? React.createElement("p", null, "No resolved packages are available for the active repository session.")
                    : React.createElement("ul", { className: 'athena-repository-graph__list' }, graphSession.resolvedPackages.map(resolvedPackage => this.renderResolvedPackage(resolvedPackage)))),
            React.createElement("section", { className: 'athena-repository-graph__section' },
                React.createElement("h3", null, "Package diagnostics"),
                graphSession.diagnostics.length === 0
                    ? React.createElement("p", null, "No package diagnostics are currently attached to this repository graph session.")
                    : React.createElement("ul", { className: 'athena-repository-graph__list' }, graphSession.diagnostics.map(diagnostic => this.renderDiagnostic(diagnostic)))));
    }
    renderDependency(dependency) {
        const dependencyTarget = dependency.locator ?? dependency.version ?? 'unspecified';
        return React.createElement("li", { key: `${dependency.name}:${dependency.source}:${dependencyTarget}`, className: 'athena-repository-graph__item' },
            React.createElement("strong", null, dependency.name),
            React.createElement("div", null,
                dependency.source,
                " | ",
                dependencyTarget));
    }
    renderResolvedPackage(resolvedPackage) {
        const packageLabel = resolvedPackage.version
            ? `${resolvedPackage.name}@${resolvedPackage.version}`
            : resolvedPackage.name;
        const dependencies = resolvedPackage.directDependencies.length === 0
            ? 'No direct dependencies'
            : resolvedPackage.directDependencies.join(', ');
        return React.createElement("li", { key: `${resolvedPackage.name}:${resolvedPackage.sourceRoot}`, className: 'athena-repository-graph__item' },
            React.createElement("strong", null, packageLabel),
            React.createElement("div", null,
                React.createElement("code", null, resolvedPackage.sourceRoot)),
            React.createElement("div", null, dependencies));
    }
    renderDiagnostic(diagnostic) {
        const severity = diagnostic.severity.toLowerCase();
        return React.createElement("li", { key: `${diagnostic.code}:${diagnostic.message}`, className: 'athena-repository-graph__item athena-repository-graph__item--diagnostic' },
            React.createElement("div", { className: 'athena-repository-graph__diagnostic-header' },
                React.createElement("span", { className: `athena-repository-graph__diagnostic-severity athena-repository-graph__diagnostic-severity--${severity}` }, diagnostic.severity),
                React.createElement("code", null, diagnostic.code)),
            React.createElement("div", null, diagnostic.message));
    }
};
exports.AthenaRepositoryGraphWidget = AthenaRepositoryGraphWidget;
__decorate([
    (0, inversify_1.inject)(browser_1.EditorManager),
    __metadata("design:type", browser_1.EditorManager)
], AthenaRepositoryGraphWidget.prototype, "editorManager", void 0);
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaRepositoryGraphWidget.prototype, "repositorySessionService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService),
    __metadata("design:type", athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService)
], AthenaRepositoryGraphWidget.prototype, "lspEditorBridgeService", void 0);
__decorate([
    (0, inversify_1.postConstruct)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AthenaRepositoryGraphWidget.prototype, "init", null);
exports.AthenaRepositoryGraphWidget = AthenaRepositoryGraphWidget = AthenaRepositoryGraphWidget_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaRepositoryGraphWidget);
//# sourceMappingURL=athena-repository-graph-widget.js.map