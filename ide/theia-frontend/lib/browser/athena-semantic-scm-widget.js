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
var AthenaSemanticScmWidget_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaSemanticScmWidget = void 0;
const React = __importStar(require("@theia/core/shared/react"));
const react_widget_1 = require("@theia/core/lib/browser/widgets/react-widget");
const disposable_1 = require("@theia/core/lib/common/disposable");
const inversify_1 = require("@theia/core/shared/inversify");
const athena_lsp_editor_bridge_service_1 = require("./athena-lsp-editor-bridge-service");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
const athena_semantic_selection_service_1 = require("./athena-semantic-selection-service");
const athena_semantic_selection_model_1 = require("./athena-semantic-selection-model");
let AthenaSemanticScmWidget = class AthenaSemanticScmWidget extends react_widget_1.ReactWidget {
    static { AthenaSemanticScmWidget_1 = this; }
    static ID = 'athena.semanticScm';
    static LABEL = 'Semantic SCM';
    static DEFAULT_ADAPTER_ID = 'scm-git';
    repositorySessionService;
    lspEditorBridgeService;
    semanticSelectionService;
    semanticScmState;
    semanticHistoryState;
    errorMessage;
    historyErrorMessage;
    loading = false;
    refreshHandle;
    baselineLabel = 'Baseline';
    baselineLocator = '../baseline';
    historyPackageName = '';
    historyBaselineSequence = 'Baseline|../baseline';
    init() {
        this.id = AthenaSemanticScmWidget_1.ID;
        this.title.label = AthenaSemanticScmWidget_1.LABEL;
        this.title.caption = AthenaSemanticScmWidget_1.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-git-pull-request';
        this.addClass('athena-semantic-scm-widget');
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(this.semanticSelectionService.onDidChangeSelection(() => this.update()));
        this.toDispose.push(disposable_1.Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
            }
        }));
        this.scheduleRefresh();
    }
    scheduleRefresh() {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshSemanticScmState();
        }, 120);
    }
    async refreshSemanticScmState() {
        const sessionState = this.repositorySessionService.state;
        const currentRepositoryRoot = sessionState.repositoryRoot;
        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.historyErrorMessage = undefined;
            this.semanticScmState = undefined;
            this.semanticHistoryState = undefined;
            this.update();
            return;
        }
        if (!this.historyPackageName.trim() && sessionState.primaryPackageName) {
            this.historyPackageName = sessionState.primaryPackageName;
        }
        this.loading = true;
        this.errorMessage = undefined;
        this.historyErrorMessage = undefined;
        this.update();
        const historyPackageName = this.historyPackageName.trim();
        const historyBaselines = this.historyBaselines();
        const requests = await Promise.allSettled([
            this.lspEditorBridgeService.requestSemanticScmState({
                adapterId: AthenaSemanticScmWidget_1.DEFAULT_ADAPTER_ID,
                locator: this.baselineLocator,
                locatorLabel: this.baselineLabel,
                baselineId: this.baselineId(),
                baselineLabel: this.baselineLabel,
                metadata: {}
            })
        ]);
        const historyRequests = historyPackageName.length > 0
            ? await Promise.allSettled([
                this.lspEditorBridgeService.requestSemanticHistoryState({
                    packageName: historyPackageName,
                    baselines: historyBaselines
                })
            ])
            : [];
        if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
            return;
        }
        const semanticScmResult = requests[0];
        if (semanticScmResult.status === 'fulfilled') {
            this.semanticScmState = semanticScmResult.value;
            this.errorMessage = undefined;
        }
        else {
            this.semanticScmState = undefined;
            this.errorMessage = semanticScmResult.reason instanceof Error
                ? semanticScmResult.reason.message
                : String(semanticScmResult.reason);
        }
        if (historyPackageName.length === 0) {
            this.semanticHistoryState = undefined;
            this.historyErrorMessage = 'No package is available yet for semantic history inspection.';
        }
        else {
            const historyResult = historyRequests[0];
            if (historyResult.status === 'fulfilled') {
                this.semanticHistoryState = historyResult.value;
                this.historyErrorMessage = undefined;
            }
            else {
                this.semanticHistoryState = undefined;
                this.historyErrorMessage = historyResult.reason instanceof Error
                    ? historyResult.reason.message
                    : String(historyResult.reason);
            }
        }
        if (this.repositorySessionService.state.repositoryRoot === currentRepositoryRoot) {
            this.loading = false;
            this.update();
        }
    }
    baselineId() {
        const normalizedLabel = this.baselineLabel
            .trim()
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-+|-+$/g, '');
        if (normalizedLabel.length > 0) {
            return normalizedLabel;
        }
        return `baseline-${this.baselineLocator.replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-+|-+$/g, '').toLowerCase()}`;
    }
    historyBaselines() {
        const baselines = this.historyBaselineSequence
            .split(/\r?\n/)
            .map(line => line.trim())
            .filter(line => line.length > 0)
            .flatMap((line, index) => {
            const segments = line.split('|').map(segment => segment.trim()).filter(segment => segment.length > 0);
            const baselineLabel = segments.length > 1 ? segments[0] : `Baseline ${index + 1}`;
            const locator = segments.length > 1 ? segments.slice(1).join('|') : segments[0];
            if (!locator) {
                return [];
            }
            return [{
                    adapterId: AthenaSemanticScmWidget_1.DEFAULT_ADAPTER_ID,
                    locator,
                    locatorLabel: baselineLabel,
                    baselineId: this.historyBaselineId(baselineLabel, locator, index),
                    baselineLabel,
                    metadata: {}
                }];
        });
        if (baselines.length > 0) {
            return baselines;
        }
        return [{
                adapterId: AthenaSemanticScmWidget_1.DEFAULT_ADAPTER_ID,
                locator: this.baselineLocator,
                locatorLabel: this.baselineLabel,
                baselineId: this.baselineId(),
                baselineLabel: this.baselineLabel,
                metadata: {}
            }];
    }
    historyBaselineId(label, locator, index) {
        const normalized = `${label}-${locator}`
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-+|-+$/g, '');
        return normalized.length > 0 ? normalized : `history-baseline-${index + 1}`;
    }
    onBaselineLabelChanged(event) {
        this.baselineLabel = event.currentTarget.value;
        this.update();
    }
    onBaselineLocatorChanged(event) {
        this.baselineLocator = event.currentTarget.value;
        this.update();
    }
    onHistoryPackageNameChanged(event) {
        this.historyPackageName = event.currentTarget.value;
        this.update();
    }
    onHistoryBaselineSequenceChanged(event) {
        this.historyBaselineSequence = event.currentTarget.value;
        this.update();
    }
    render() {
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            return React.createElement("div", { className: 'athena-semantic-scm' },
                React.createElement("section", { className: 'athena-semantic-scm__empty' },
                    React.createElement("h2", null, "Semantic SCM"),
                    React.createElement("p", null, sessionState.message)));
        }
        if (this.loading && !this.semanticScmState && !this.semanticHistoryState && !this.errorMessage && !this.historyErrorMessage) {
            return React.createElement("div", { className: 'athena-semantic-scm' },
                this.renderControls(sessionState.primaryPackageName),
                React.createElement("section", { className: 'athena-semantic-scm__empty' },
                    React.createElement("h2", null, "Semantic SCM"),
                    React.createElement("p", null, "Loading baseline-driven semantic review, commit, and package-history state from Athena LSP.")));
        }
        const semanticScmState = this.semanticScmState;
        const semanticHistoryState = this.semanticHistoryState;
        const review = semanticScmState?.review;
        const commit = semanticScmState?.commit;
        const history = semanticHistoryState?.history;
        const statusTone = semanticScmState?.status === 'ready' &&
            (semanticHistoryState === undefined || semanticHistoryState.status === 'ready')
            ? 'ready'
            : 'warning';
        return React.createElement("div", { className: 'athena-semantic-scm' },
            this.renderControls(sessionState.primaryPackageName),
            React.createElement("header", { className: 'athena-semantic-scm__header' },
                React.createElement("div", null,
                    React.createElement("div", { className: 'athena-semantic-scm__eyebrow' }, "Athena semantic SCM"),
                    React.createElement("h2", null, semanticScmState?.baselineLabel ?? 'Semantic SCM workspace view'),
                    React.createElement("p", null,
                        React.createElement("code", null, semanticScmState?.locator ?? this.baselineLocator),
                        " | ",
                        semanticScmState?.semanticPath ?? semanticHistoryState?.semanticPath ?? 'frontend -> LSP -> runtime/compiler')),
                React.createElement("div", { className: `athena-semantic-scm__status athena-semantic-scm__status--${statusTone}` }, semanticScmState?.status ?? semanticHistoryState?.status ?? 'idle')),
            React.createElement("section", { className: 'athena-semantic-scm__summary' },
                React.createElement("ul", { className: 'athena-semantic-scm__summary-list' },
                    React.createElement("li", null,
                        React.createElement("span", null, "Review entries"),
                        React.createElement("strong", null, review?.entryCount ?? 0)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Review enrichments"),
                        React.createElement("strong", null, review?.enrichmentCount ?? 0)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Commit entries"),
                        React.createElement("strong", null, commit?.entryCount ?? 0)),
                    React.createElement("li", null,
                        React.createElement("span", null, "History entries"),
                        React.createElement("strong", null, history?.entryCount ?? 0)),
                    React.createElement("li", null,
                        React.createElement("span", null, "Baseline diagnostics"),
                        React.createElement("strong", null, semanticScmState?.diagnostics.length ?? 0)),
                    React.createElement("li", null,
                        React.createElement("span", null, "History baselines"),
                        React.createElement("strong", null, history?.baselineCount ?? semanticHistoryState?.baselines.length ?? 0)))),
            React.createElement("section", { className: 'athena-semantic-scm__section' },
                React.createElement("h3", null, "Baseline request"),
                !semanticScmState
                    ? React.createElement("p", null, this.errorMessage ?? 'No semantic SCM payload is available yet for the active Athena session.')
                    : React.createElement("ul", { className: 'athena-semantic-scm__detail-list' },
                        React.createElement("li", null,
                            React.createElement("span", null, "Adapter bridge"),
                            React.createElement("strong", null, semanticScmState.adapterId)),
                        React.createElement("li", null,
                            React.createElement("span", null, "Locator label"),
                            React.createElement("strong", null, semanticScmState.locatorLabel ?? 'Not provided')),
                        React.createElement("li", null,
                            React.createElement("span", null, "Baseline id"),
                            React.createElement("strong", null, semanticScmState.baselineId)),
                        React.createElement("li", null,
                            React.createElement("span", null, "Repository root"),
                            React.createElement("strong", null,
                                React.createElement("code", null, sessionState.repositoryRoot))))),
            React.createElement("section", { className: 'athena-semantic-scm__section' },
                React.createElement("h3", null, "Baseline diagnostics"),
                !semanticScmState
                    ? React.createElement("p", null, this.errorMessage ?? 'No semantic SCM payload is available yet for baseline diagnostics.')
                    : semanticScmState.diagnostics.length === 0
                        ? React.createElement("p", null, "No baseline diagnostics were emitted for the current semantic SCM request.")
                        : React.createElement("ul", { className: 'athena-semantic-scm__list' }, semanticScmState.diagnostics.map(diagnostic => React.createElement("li", { key: `${diagnostic.ruleId}:${diagnostic.provenance}`, className: 'athena-semantic-scm__item athena-semantic-scm__item--diagnostic' },
                            React.createElement("div", { className: 'athena-semantic-scm__diagnostic-header' },
                                React.createElement("span", { className: `athena-semantic-scm__diagnostic-severity athena-semantic-scm__diagnostic-severity--${diagnostic.severity}` }, diagnostic.severity),
                                React.createElement("code", null, diagnostic.ruleId)),
                            React.createElement("div", null, diagnostic.message),
                            React.createElement("div", null,
                                React.createElement("code", null, diagnostic.provenance)))))),
            React.createElement("section", { className: 'athena-semantic-scm__section' },
                React.createElement("h3", null, "Selected semantic"),
                this.semanticSelectionService.selection
                    ? React.createElement("div", { className: 'athena-semantic-scm__selection' },
                        React.createElement("strong", null, this.semanticSelectionService.selection.label ?? this.semanticSelectionService.selection.semanticId),
                        React.createElement("br", null),
                        React.createElement("code", null, this.semanticSelectionService.selection.semanticId))
                    : React.createElement("p", null, "No synchronized semantic selection is active yet.")),
            React.createElement("section", { className: 'athena-semantic-scm__section' },
                React.createElement("h3", null, "Semantic review"),
                !semanticScmState
                    ? React.createElement("p", null, this.errorMessage ?? 'No semantic SCM payload is available yet for review.')
                    : !review
                        ? React.createElement("p", null, "No review summary is available because the current baseline request did not resolve cleanly.")
                        : React.createElement(React.Fragment, null,
                            React.createElement("p", null, this.renderAffectedPackages(review.affectedPackages)),
                            React.createElement("ul", { className: 'athena-semantic-scm__list' }, review.entries.map(entry => this.renderReviewEntry(entry))),
                            review.enrichments.length === 0
                                ? React.createElement("p", null, "No hosted review enrichments were added for this semantic review.")
                                : React.createElement("ul", { className: 'athena-semantic-scm__list' }, review.enrichments.map(enrichment => this.renderReviewEnrichment(enrichment))))),
            React.createElement("section", { className: 'athena-semantic-scm__section' },
                React.createElement("h3", null, "Commit preparation"),
                !semanticScmState
                    ? React.createElement("p", null, this.errorMessage ?? 'No semantic SCM payload is available yet for commit preparation.')
                    : !commit
                        ? React.createElement("p", null, "No commit-preparation state is available because the current baseline request did not resolve cleanly.")
                        : React.createElement(React.Fragment, null,
                            React.createElement("p", null, commit.summary ?? this.renderAffectedPackages(commit.affectedPackages)),
                            React.createElement("ul", { className: 'athena-semantic-scm__list' }, commit.entries.map(entry => this.renderCommitEntry(entry))))),
            React.createElement("section", { className: 'athena-semantic-scm__section' },
                React.createElement("h3", null, "Package evolution"),
                this.renderHistorySection(semanticHistoryState, history, sessionState.primaryPackageName)));
    }
    renderControls(primaryPackageName) {
        return React.createElement("section", { className: 'athena-semantic-scm__controls' },
            React.createElement("div", { className: 'athena-semantic-scm__control-grid' },
                React.createElement("div", { className: 'athena-semantic-scm__control' },
                    React.createElement("label", { htmlFor: 'athena-semantic-scm-label' }, "Baseline label"),
                    React.createElement("input", { id: 'athena-semantic-scm-label', type: 'text', value: this.baselineLabel, onChange: event => this.onBaselineLabelChanged(event) })),
                React.createElement("div", { className: 'athena-semantic-scm__control' },
                    React.createElement("label", { htmlFor: 'athena-semantic-scm-locator' }, "Baseline locator"),
                    React.createElement("input", { id: 'athena-semantic-scm-locator', type: 'text', value: this.baselineLocator, onChange: event => this.onBaselineLocatorChanged(event) })),
                React.createElement("div", { className: 'athena-semantic-scm__control' },
                    React.createElement("label", { htmlFor: 'athena-semantic-scm-package' }, "History package"),
                    React.createElement("input", { id: 'athena-semantic-scm-package', type: 'text', placeholder: primaryPackageName ?? 'com.engineeringood.package', value: this.historyPackageName, onChange: event => this.onHistoryPackageNameChanged(event) })),
                React.createElement("div", { className: 'athena-semantic-scm__control athena-semantic-scm__control--wide' },
                    React.createElement("label", { htmlFor: 'athena-semantic-scm-history-baselines' }, "History baselines"),
                    React.createElement("textarea", { id: 'athena-semantic-scm-history-baselines', rows: 3, value: this.historyBaselineSequence, onChange: event => this.onHistoryBaselineSequenceChanged(event) }),
                    React.createElement("span", { className: 'athena-semantic-scm__hint' },
                        "Use one baseline per line as ",
                        React.createElement("code", null, "Label|locator"),
                        "."))),
            React.createElement("button", { className: 'athena-semantic-scm__refresh', type: 'button', onClick: () => void this.refreshSemanticScmState() }, "Refresh semantic SCM"));
    }
    renderHistorySection(semanticHistoryState, history, primaryPackageName) {
        if (this.historyErrorMessage) {
            return React.createElement("p", null, this.historyErrorMessage);
        }
        if (!semanticHistoryState) {
            return React.createElement("p", null, "No semantic history payload is available yet for the active Athena session.");
        }
        if (!history) {
            return React.createElement("p", null, "No semantic history summary is available because the current baseline sequence did not resolve cleanly.");
        }
        return React.createElement(React.Fragment, null,
            React.createElement("p", null, history.summary ?? `Package evolution for ${this.renderPackage(history.packageId)}.`),
            React.createElement("div", { className: 'athena-semantic-scm__pill-row' },
                React.createElement("span", { className: 'athena-semantic-scm__pill' }, this.renderPackage(history.packageId)),
                React.createElement("span", { className: 'athena-semantic-scm__pill' }, history.releaseRelevance),
                React.createElement("span", { className: 'athena-semantic-scm__pill' }, history.contractBreakRisk),
                React.createElement("span", { className: 'athena-semantic-scm__pill' },
                    history.baselineCount,
                    " baselines"),
                primaryPackageName && primaryPackageName !== history.packageId.name
                    ? React.createElement("span", { className: 'athena-semantic-scm__pill' }, primaryPackageName)
                    : undefined),
            React.createElement("ul", { className: 'athena-semantic-scm__dense-list' }, semanticHistoryState.baselines.map(baseline => React.createElement("li", { key: `${baseline.baselineId}:${baseline.locator}` },
                baseline.baselineLabel,
                ": ",
                React.createElement("code", null, baseline.locator)))),
            React.createElement("div", { className: 'athena-semantic-scm__subsection' },
                React.createElement("h4", null, "Package lineage"),
                history.packageLineage.length === 0
                    ? React.createElement("p", null, "No package lineage was published for the current baseline sequence.")
                    : React.createElement("ul", { className: 'athena-semantic-scm__list' }, history.packageLineage.map(lineage => React.createElement("li", { key: `${lineage.packageId.name}:${lineage.baselineVersion ?? 'none'}:${lineage.currentVersion ?? 'none'}`, className: 'athena-semantic-scm__item' },
                        React.createElement("strong", null, lineage.changeKind),
                        React.createElement("div", null, this.renderPackage(lineage.packageId)),
                        React.createElement("div", null,
                            "Baseline version: ",
                            lineage.baselineVersion ?? 'unspecified',
                            " | Current version: ",
                            lineage.currentVersion ?? 'unspecified'))))),
            React.createElement("div", { className: 'athena-semantic-scm__subsection' },
                React.createElement("h4", null, "Validation movement"),
                history.validationMovements.length === 0
                    ? React.createElement("p", null, "No validation movement was published for the current baseline sequence.")
                    : React.createElement("ul", { className: 'athena-semantic-scm__list' }, history.validationMovements.map(movement => this.renderValidationMovement(movement)))),
            React.createElement("div", { className: 'athena-semantic-scm__subsection' },
                React.createElement("h4", null, "History entries"),
                history.entries.length === 0
                    ? React.createElement("p", null, "No semantic history entries were published for the current baseline sequence.")
                    : React.createElement("ul", { className: 'athena-semantic-scm__list' }, history.entries.map(entry => this.renderHistoryEntry(entry)))));
    }
    renderAffectedPackages(packages) {
        if (packages.length === 0) {
            return 'No affected packages were published for this semantic SCM state.';
        }
        return `Affected packages: ${packages.map(pkg => this.renderPackage(pkg)).join(', ')}`;
    }
    renderPackage(pkg) {
        return pkg.version ? `${pkg.name}@${pkg.version}` : pkg.name;
    }
    renderReviewEntry(entry) {
        const semanticId = (0, athena_semantic_selection_model_1.selectableSemanticIdFromScmContext)(entry);
        return React.createElement("li", { key: `${entry.kind}:${entry.message}`, className: `athena-semantic-scm__item ${this.isSelectedContext(entry) ? 'athena-semantic-scm__item--selected' : ''}` }, semanticId
            ? React.createElement("button", { className: 'athena-semantic-scm__selectable', type: 'button', onClick: () => void this.semanticSelectionService.selectSemanticId(semanticId) },
                React.createElement("strong", null, entry.kind),
                React.createElement("div", null, entry.message),
                React.createElement("div", null,
                    React.createElement("code", null, semanticId)),
                entry.factReferences.length > 0
                    ? React.createElement("div", null, entry.factReferences.map(reference => `${reference.kind}:${reference.identifier}`).join(', '))
                    : undefined)
            : React.createElement(React.Fragment, null,
                React.createElement("strong", null, entry.kind),
                React.createElement("div", null, entry.message),
                entry.factReferences.length > 0
                    ? React.createElement("div", null, entry.factReferences.map(reference => `${reference.kind}:${reference.identifier}`).join(', '))
                    : undefined));
    }
    renderReviewEnrichment(enrichment) {
        const semanticId = (0, athena_semantic_selection_model_1.selectableSemanticIdFromScmContext)(enrichment);
        return React.createElement("li", { key: `${enrichment.pluginId}:${enrichment.kind}:${enrichment.message}`, className: `athena-semantic-scm__item athena-semantic-scm__item--enrichment ${this.isSelectedContext(enrichment) ? 'athena-semantic-scm__item--selected' : ''}` }, semanticId
            ? React.createElement("button", { className: 'athena-semantic-scm__selectable', type: 'button', onClick: () => void this.semanticSelectionService.selectSemanticId(semanticId) },
                React.createElement("strong", null, enrichment.kind),
                React.createElement("div", null, enrichment.message),
                React.createElement("div", null, enrichment.pluginId),
                React.createElement("div", null,
                    React.createElement("code", null, semanticId)))
            : React.createElement(React.Fragment, null,
                React.createElement("strong", null, enrichment.kind),
                React.createElement("div", null, enrichment.message),
                React.createElement("div", null, enrichment.pluginId)));
    }
    renderCommitEntry(entry) {
        const semanticId = (0, athena_semantic_selection_model_1.selectableSemanticIdFromScmContext)(entry);
        return React.createElement("li", { key: `${entry.kind}:${entry.message}`, className: `athena-semantic-scm__item ${this.isSelectedContext(entry) ? 'athena-semantic-scm__item--selected' : ''}` }, semanticId
            ? React.createElement("button", { className: 'athena-semantic-scm__selectable', type: 'button', onClick: () => void this.semanticSelectionService.selectSemanticId(semanticId) },
                React.createElement("strong", null, entry.kind),
                React.createElement("div", null, entry.message),
                React.createElement("div", null,
                    React.createElement("code", null, semanticId)),
                entry.factReferences.length > 0
                    ? React.createElement("div", null, entry.factReferences.map(reference => `${reference.kind}:${reference.identifier}`).join(', '))
                    : undefined)
            : React.createElement(React.Fragment, null,
                React.createElement("strong", null, entry.kind),
                React.createElement("div", null, entry.message),
                entry.factReferences.length > 0
                    ? React.createElement("div", null, entry.factReferences.map(reference => `${reference.kind}:${reference.identifier}`).join(', '))
                    : undefined));
    }
    renderValidationMovement(movement) {
        return React.createElement("li", { key: `${movement.baselineErrorCount}:${movement.currentErrorCount}:${movement.message}`, className: 'athena-semantic-scm__item' },
            React.createElement("strong", null, "validation-movement"),
            React.createElement("div", null, movement.message),
            React.createElement("div", null,
                "Errors: ",
                movement.baselineErrorCount,
                ' -> ',
                movement.currentErrorCount,
                " | Warnings: ",
                movement.baselineWarningCount,
                ' -> ',
                movement.currentWarningCount));
    }
    renderHistoryEntry(entry) {
        return React.createElement("li", { key: `${entry.baselineId}:${entry.kind}:${entry.message}`, className: 'athena-semantic-scm__item' },
            React.createElement("div", { className: 'athena-semantic-scm__item-header' },
                React.createElement("strong", null, entry.kind),
                React.createElement("span", { className: 'athena-semantic-scm__pill' }, entry.baselineLabel),
                React.createElement("span", { className: 'athena-semantic-scm__pill' }, entry.releaseRelevance),
                React.createElement("span", { className: 'athena-semantic-scm__pill' }, entry.contractBreakRisk)),
            React.createElement("div", null, entry.message),
            React.createElement("div", null, this.renderPackage(entry.packageVersion.packageId)),
            React.createElement("div", null,
                "Version meaning: ",
                entry.packageVersion.changeKind,
                " | Baseline: ",
                entry.packageVersion.baselineVersion ?? 'unspecified',
                " | Current: ",
                entry.packageVersion.currentVersion ?? 'unspecified'),
            entry.changeCategory
                ? React.createElement("div", null,
                    "Change category: ",
                    entry.changeCategory)
                : undefined,
            entry.dependencyMovements.length > 0
                ? React.createElement("div", null,
                    "Dependency movements: ",
                    entry.dependencyMovements.map(movement => `${movement.kind}:${this.renderPackage(movement.packageId)}`).join(', '))
                : undefined,
            entry.validationMovement
                ? React.createElement("div", null, entry.validationMovement.message)
                : undefined,
            React.createElement("div", null,
                "Authored changes: ",
                entry.authoredChangeCount,
                " | Derived consequences: ",
                entry.derivedConsequenceCount));
    }
    isSelectedContext(carrier) {
        return (0, athena_semantic_selection_model_1.matchesSemanticScmContext)(carrier, this.semanticSelectionService.selection?.semanticId);
    }
};
exports.AthenaSemanticScmWidget = AthenaSemanticScmWidget;
__decorate([
    (0, inversify_1.inject)(athena_repository_session_service_1.AthenaRepositorySessionService),
    __metadata("design:type", athena_repository_session_service_1.AthenaRepositorySessionService)
], AthenaSemanticScmWidget.prototype, "repositorySessionService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService),
    __metadata("design:type", athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService)
], AthenaSemanticScmWidget.prototype, "lspEditorBridgeService", void 0);
__decorate([
    (0, inversify_1.inject)(athena_semantic_selection_service_1.AthenaSemanticSelectionService),
    __metadata("design:type", athena_semantic_selection_service_1.AthenaSemanticSelectionService)
], AthenaSemanticScmWidget.prototype, "semanticSelectionService", void 0);
__decorate([
    (0, inversify_1.postConstruct)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AthenaSemanticScmWidget.prototype, "init", null);
exports.AthenaSemanticScmWidget = AthenaSemanticScmWidget = AthenaSemanticScmWidget_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaSemanticScmWidget);
//# sourceMappingURL=athena-semantic-scm-widget.js.map