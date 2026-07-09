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
var AthenaHomeWidget_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaHomeWidget = void 0;
const React = __importStar(require("react"));
const common_1 = require("@theia/core/lib/common");
const react_widget_1 = require("@theia/core/lib/browser/widgets/react-widget");
const inversify_1 = require("@theia/core/shared/inversify");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
const athena_workbench_extensions_1 = require("./athena-workbench-extensions");
let AthenaHomeWidget = class AthenaHomeWidget extends react_widget_1.ReactWidget {
    static { AthenaHomeWidget_1 = this; }
    static ID = 'athena.home';
    static LABEL = 'Athena Home';
    commandService;
    repositorySessionService;
    init() {
        this.id = AthenaHomeWidget_1.ID;
        this.title.label = AthenaHomeWidget_1.LABEL;
        this.title.caption = AthenaHomeWidget_1.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-home';
        this.addClass('athena-home-widget');
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.update()));
        this.update();
    }
    render() {
        const sessionState = this.repositorySessionService.state;
        const sessionStatusClassName = `athena-home__session-status athena-home__session-status--${sessionState.lifecycle}`;
        return React.createElement("div", { className: 'athena-home' },
            React.createElement("header", { className: 'athena-home__masthead' },
                React.createElement("div", { className: 'athena-home__brand' },
                    React.createElement("div", { className: 'athena-home__brand-mark', "aria-hidden": 'true' },
                        React.createElement("span", { className: 'athena-home__brand-mark-core' }, "A")),
                    React.createElement("div", { className: 'athena-home__brand-copy' },
                        React.createElement("span", { className: 'athena-home__brand-name' }, "ATHENA"),
                        React.createElement("span", { className: 'athena-home__brand-tagline' }, "Engineering semantic workstation")))),
            React.createElement("section", { className: 'athena-home__hero' },
                React.createElement("div", { className: 'athena-home__badge' }, "Athena M6"),
                React.createElement("h1", null, "A serious engineering shell, not a generic editor demo."),
                React.createElement("p", null,
                    "This product shell proves the Athena IDE boundary on top of Theia while keeping semantic authority downstream in the JVM stack behind ",
                    React.createElement("code", null, "ide/lsp"),
                    "."),
                React.createElement("div", { className: 'athena-home__actions' },
                    React.createElement("button", { className: 'athena-home__action-button athena-home__action-button--secondary', type: 'button', onClick: () => void this.commandService.executeCommand(athena_workbench_extensions_1.AthenaCommands.CREATE_ENGINEERING_REPOSITORY.id) }, "Create Engineering Repository"),
                    React.createElement("button", { className: 'athena-home__action-button', type: 'button', onClick: () => void this.commandService.executeCommand(athena_workbench_extensions_1.AthenaCommands.OPEN_ENGINEERING_REPOSITORY.id) }, "Open Engineering Repository"),
                    React.createElement("span", { className: sessionStatusClassName }, sessionState.lifecycle))),
            React.createElement("section", { className: 'athena-home__grid' },
                React.createElement("article", { className: 'athena-home__card' },
                    React.createElement("h2", null, "Current proof"),
                    React.createElement("ul", null,
                        React.createElement("li", null, "Branded Athena desktop shell"),
                        React.createElement("li", null, "Curated workbench capability set"),
                        React.createElement("li", null, "Repository navigation docked as a persistent left workbench panel"),
                        React.createElement("li", null, "Problems and Output docked as persistent bottom workbench panels"),
                        React.createElement("li", null, "Frontend and backend ownership split"),
                        React.createElement("li", null, "Governed repository bootstrap from the welcome flow"),
                        React.createElement("li", null, "Athena-authored files enter semantics only through LSP"),
                        React.createElement("li", null, "Diagnostics land in editor and Problems from the JVM stack"),
                        React.createElement("li", null, "Completion and navigation now come from the same JVM-owned LSP path"),
                        React.createElement("li", null, "Repository graph feedback is visible through an Athena workbench panel"),
                        React.createElement("li", null, "Semantic review and commit-preparation feedback now stay visible through a dedicated Athena SCM panel"),
                        React.createElement("li", null, "Text-first path with future projection safety"))),
                React.createElement("article", { className: 'athena-home__card' },
                    React.createElement("h2", null, "Repository session"),
                    React.createElement("p", null, sessionState.message),
                    React.createElement("ul", null,
                        React.createElement("li", null,
                            "Repository root: ",
                            sessionState.repositoryRoot ?? 'Not open'),
                        React.createElement("li", null,
                            "Manifest: ",
                            sessionState.manifestPath ?? 'Not validated yet'),
                        React.createElement("li", null,
                            "Lock: ",
                            sessionState.lockPath ?? 'Not validated yet'),
                        React.createElement("li", null,
                            "Governed source root: ",
                            sessionState.sourceRootPath ?? 'Not validated yet'),
                        React.createElement("li", null,
                            "Authored source: ",
                            sessionState.sourcePath ?? 'Not resolved'),
                        React.createElement("li", null,
                            "Project key: ",
                            sessionState.projectName ?? 'Not activated'),
                        React.createElement("li", null,
                            "Primary package: ",
                            sessionState.primaryPackageName ?? 'Not validated yet'),
                        React.createElement("li", null,
                            "Semantic path: ",
                            sessionState.semanticPath ?? 'frontend -> LSP -> runtime/compiler'),
                        React.createElement("li", null,
                            "Last Athena editor: ",
                            sessionState.lastOpenedDocumentUri ?? 'Not opened yet'),
                        React.createElement("li", null, "Single-session M4 rule stays enforced per window")))),
            React.createElement("section", { className: 'athena-home__card athena-home__card--wide' },
                React.createElement("h2", null, "Workbench views"),
                React.createElement("p", null, "Athena workbench additions attach through one product-owned extension registry, so later milestones can add panels without replacing the shell."),
                React.createElement("div", { className: 'athena-home__actions' },
                    React.createElement("button", { className: 'athena-home__action-button athena-home__action-button--secondary', type: 'button', onClick: () => void this.commandService.executeCommand(athena_workbench_extensions_1.AthenaCommands.OPEN_HOME.id) }, "Athena Home"),
                    athena_workbench_extensions_1.ATHENA_WORKBENCH_EXTENSIONS.map(extension => React.createElement("button", { key: extension.command.id, className: 'athena-home__action-button athena-home__action-button--secondary', type: 'button', onClick: () => void this.commandService.executeCommand(extension.command.id) }, extension.quickActionLabel)))),
            React.createElement("section", { className: 'athena-home__card athena-home__card--wide' },
                React.createElement("h2", null, "Capability boundary"),
                React.createElement("p", null, "This shell now carries the M6 operability essentials on top of the M4 and M5 base: editing, completion, document symbols, go-to-definition, references, diagnostics in Problems and editor markers, repository navigation, package-graph feedback, semantic review and commit-preparation projection, output panels, terminal visibility, and product-owned workbench framing."),
                React.createElement("p", null, "Marketplace sprawl, semantic history publishing, and graphical projection tooling remain intentionally deferred.")),
            React.createElement("section", { className: 'athena-home__card athena-home__card--wide' },
                React.createElement("h2", null, "Current repository rule"),
                React.createElement("p", null,
                    "Repository opening now starts from the governed root contract: Athena validates",
                    React.createElement("code", null, "athena.yaml"),
                    ", ",
                    React.createElement("code", null, "athena.lock"),
                    ", and the governed",
                    React.createElement("code", null, "src/"),
                    " layout through the JVM stack, then derives one deterministic authored source as the temporary editor seed for the current runtime path."),
                React.createElement("p", null,
                    "Repository meaning no longer comes from \"exactly one file under ",
                    React.createElement("code", null, "src/"),
                    "\". The active package graph is now inspectable through the dedicated Repository Graph workbench view.")));
    }
};
exports.AthenaHomeWidget = AthenaHomeWidget;
__decorate([
    (0, inversify_1.inject)(common_1.CommandService),
    __metadata("design:type", Object)
], AthenaHomeWidget.prototype, "commandService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaHomeWidget.prototype, "repositorySessionService", void 0);
__decorate([
    (0, inversify_1.postConstruct)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AthenaHomeWidget.prototype, "init", null);
exports.AthenaHomeWidget = AthenaHomeWidget = AthenaHomeWidget_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaHomeWidget);
//# sourceMappingURL=athena-home-widget.js.map