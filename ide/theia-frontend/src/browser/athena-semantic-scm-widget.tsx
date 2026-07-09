import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import {
    AthenaLspEditorBridgeService,
    AthenaSemanticCommitEntryPayload,
    AthenaSemanticHistoryBaselineParams,
    AthenaSemanticHistoryEntryPayload,
    AthenaSemanticHistoryStatePayload,
    AthenaSemanticPackagePayload,
    AthenaSemanticReviewEntryPayload,
    AthenaSemanticReviewEnrichmentPayload,
    AthenaSemanticScmStatePayload,
    AthenaSemanticValidationMovementPayload
} from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';

@injectable()
export class AthenaSemanticScmWidget extends ReactWidget {
    static readonly ID = 'athena.semanticScm';
    static readonly LABEL = 'Semantic SCM';
    protected static readonly DEFAULT_ADAPTER_ID = 'scm-git';

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    protected semanticScmState: AthenaSemanticScmStatePayload | undefined;
    protected semanticHistoryState: AthenaSemanticHistoryStatePayload | undefined;
    protected errorMessage: string | undefined;
    protected historyErrorMessage: string | undefined;
    protected loading = false;
    protected refreshHandle: number | undefined;
    protected baselineLabel = 'Baseline';
    protected baselineLocator = '../baseline';
    protected historyPackageName = '';
    protected historyBaselineSequence = 'Baseline|../baseline';

    @postConstruct()
    protected init(): void {
        this.id = AthenaSemanticScmWidget.ID;
        this.title.label = AthenaSemanticScmWidget.LABEL;
        this.title.caption = AthenaSemanticScmWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-git-pull-request';
        this.addClass('athena-semantic-scm-widget');

        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
            }
        }));

        this.scheduleRefresh();
    }

    protected scheduleRefresh(): void {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshSemanticScmState();
        }, 120);
    }

    protected async refreshSemanticScmState(): Promise<void> {
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
        const requests: Array<PromiseSettledResult<AthenaSemanticScmStatePayload | undefined>> = await Promise.allSettled([
            this.lspEditorBridgeService.requestSemanticScmState({
                adapterId: AthenaSemanticScmWidget.DEFAULT_ADAPTER_ID,
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
        } else {
            this.semanticScmState = undefined;
            this.errorMessage = semanticScmResult.reason instanceof Error
                ? semanticScmResult.reason.message
                : String(semanticScmResult.reason);
        }

        if (historyPackageName.length === 0) {
            this.semanticHistoryState = undefined;
            this.historyErrorMessage = 'No package is available yet for semantic history inspection.';
        } else {
            const historyResult = historyRequests[0];
            if (historyResult.status === 'fulfilled') {
                this.semanticHistoryState = historyResult.value;
                this.historyErrorMessage = undefined;
            } else {
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

    protected baselineId(): string {
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

    protected historyBaselines(): AthenaSemanticHistoryBaselineParams[] {
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
                    adapterId: AthenaSemanticScmWidget.DEFAULT_ADAPTER_ID,
                    locator,
                    locatorLabel: baselineLabel,
                    baselineId: this.historyBaselineId(baselineLabel, locator, index),
                    baselineLabel,
                    metadata: {}
                } satisfies AthenaSemanticHistoryBaselineParams];
            });

        if (baselines.length > 0) {
            return baselines;
        }

        return [{
            adapterId: AthenaSemanticScmWidget.DEFAULT_ADAPTER_ID,
            locator: this.baselineLocator,
            locatorLabel: this.baselineLabel,
            baselineId: this.baselineId(),
            baselineLabel: this.baselineLabel,
            metadata: {}
        }];
    }

    protected historyBaselineId(
        label: string,
        locator: string,
        index: number
    ): string {
        const normalized = `${label}-${locator}`
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-+|-+$/g, '');
        return normalized.length > 0 ? normalized : `history-baseline-${index + 1}`;
    }

    protected onBaselineLabelChanged(event: React.ChangeEvent<HTMLInputElement>): void {
        this.baselineLabel = event.currentTarget.value;
        this.update();
    }

    protected onBaselineLocatorChanged(event: React.ChangeEvent<HTMLInputElement>): void {
        this.baselineLocator = event.currentTarget.value;
        this.update();
    }

    protected onHistoryPackageNameChanged(event: React.ChangeEvent<HTMLInputElement>): void {
        this.historyPackageName = event.currentTarget.value;
        this.update();
    }

    protected onHistoryBaselineSequenceChanged(event: React.ChangeEvent<HTMLTextAreaElement>): void {
        this.historyBaselineSequence = event.currentTarget.value;
        this.update();
    }

    protected render(): React.ReactNode {
        const sessionState = this.repositorySessionService.state;

        if (sessionState.lifecycle !== 'ready') {
            return <div className='athena-semantic-scm'>
                <section className='athena-semantic-scm__empty'>
                    <h2>Semantic SCM</h2>
                    <p>{sessionState.message}</p>
                </section>
            </div>;
        }

        if (this.loading && !this.semanticScmState && !this.semanticHistoryState && !this.errorMessage && !this.historyErrorMessage) {
            return <div className='athena-semantic-scm'>
                {this.renderControls(sessionState.primaryPackageName)}
                <section className='athena-semantic-scm__empty'>
                    <h2>Semantic SCM</h2>
                    <p>Loading baseline-driven semantic review, commit, and package-history state from Athena LSP.</p>
                </section>
            </div>;
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

        return <div className='athena-semantic-scm'>
            {this.renderControls(sessionState.primaryPackageName)}

            <header className='athena-semantic-scm__header'>
                <div>
                    <div className='athena-semantic-scm__eyebrow'>Athena semantic SCM</div>
                    <h2>{semanticScmState?.baselineLabel ?? 'Semantic SCM workspace view'}</h2>
                    <p>
                        <code>{semanticScmState?.locator ?? this.baselineLocator}</code> | {semanticScmState?.semanticPath ?? semanticHistoryState?.semanticPath ?? 'frontend -> LSP -> runtime/compiler'}
                    </p>
                </div>
                <div className={`athena-semantic-scm__status athena-semantic-scm__status--${statusTone}`}>
                    {semanticScmState?.status ?? semanticHistoryState?.status ?? 'idle'}
                </div>
            </header>

            <section className='athena-semantic-scm__metrics'>
                <article className='athena-semantic-scm__metric'>
                    <span className='athena-semantic-scm__metric-value'>{review?.entryCount ?? 0}</span>
                    <span className='athena-semantic-scm__metric-label'>Review entries</span>
                </article>
                <article className='athena-semantic-scm__metric'>
                    <span className='athena-semantic-scm__metric-value'>{review?.enrichmentCount ?? 0}</span>
                    <span className='athena-semantic-scm__metric-label'>Review enrichments</span>
                </article>
                <article className='athena-semantic-scm__metric'>
                    <span className='athena-semantic-scm__metric-value'>{commit?.entryCount ?? 0}</span>
                    <span className='athena-semantic-scm__metric-label'>Commit entries</span>
                </article>
                <article className='athena-semantic-scm__metric'>
                    <span className='athena-semantic-scm__metric-value'>{history?.entryCount ?? 0}</span>
                    <span className='athena-semantic-scm__metric-label'>History entries</span>
                </article>
                <article className='athena-semantic-scm__metric'>
                    <span className='athena-semantic-scm__metric-value'>{semanticScmState?.diagnostics.length ?? 0}</span>
                    <span className='athena-semantic-scm__metric-label'>Baseline diagnostics</span>
                </article>
                <article className='athena-semantic-scm__metric'>
                    <span className='athena-semantic-scm__metric-value'>{history?.baselineCount ?? semanticHistoryState?.baselines.length ?? 0}</span>
                    <span className='athena-semantic-scm__metric-label'>History baselines</span>
                </article>
            </section>

            <section className='athena-semantic-scm__section'>
                <h3>Baseline request</h3>
                {!semanticScmState
                    ? <p>{this.errorMessage ?? 'No semantic SCM payload is available yet for the active Athena session.'}</p>
                    : <ul>
                        <li>Adapter bridge: {semanticScmState.adapterId}</li>
                        <li>Locator label: {semanticScmState.locatorLabel ?? 'Not provided'}</li>
                        <li>Baseline id: {semanticScmState.baselineId}</li>
                        <li>Repository root: <code>{sessionState.repositoryRoot}</code></li>
                    </ul>}
            </section>

            <section className='athena-semantic-scm__section'>
                <h3>Baseline diagnostics</h3>
                {!semanticScmState
                    ? <p>{this.errorMessage ?? 'No semantic SCM payload is available yet for baseline diagnostics.'}</p>
                    : semanticScmState.diagnostics.length === 0
                        ? <p>No baseline diagnostics were emitted for the current semantic SCM request.</p>
                        : <ul className='athena-semantic-scm__list'>
                            {semanticScmState.diagnostics.map(diagnostic => <li
                                key={`${diagnostic.ruleId}:${diagnostic.provenance}`}
                                className='athena-semantic-scm__item athena-semantic-scm__item--diagnostic'
                            >
                                <div className='athena-semantic-scm__diagnostic-header'>
                                    <span className={`athena-semantic-scm__diagnostic-severity athena-semantic-scm__diagnostic-severity--${diagnostic.severity}`}>
                                        {diagnostic.severity}
                                    </span>
                                    <code>{diagnostic.ruleId}</code>
                                </div>
                                <div>{diagnostic.message}</div>
                                <div><code>{diagnostic.provenance}</code></div>
                            </li>)}
                        </ul>}
            </section>

            <section className='athena-semantic-scm__section'>
                <h3>Semantic review</h3>
                {!semanticScmState
                    ? <p>{this.errorMessage ?? 'No semantic SCM payload is available yet for review.'}</p>
                    : !review
                        ? <p>No review summary is available because the current baseline request did not resolve cleanly.</p>
                        : <>
                            <p>{this.renderAffectedPackages(review.affectedPackages)}</p>
                            <ul className='athena-semantic-scm__list'>
                                {review.entries.map(entry => this.renderReviewEntry(entry))}
                            </ul>
                            {review.enrichments.length === 0
                                ? <p>No hosted review enrichments were added for this semantic review.</p>
                                : <ul className='athena-semantic-scm__list'>
                                    {review.enrichments.map(enrichment => this.renderReviewEnrichment(enrichment))}
                                </ul>}
                        </>}
            </section>

            <section className='athena-semantic-scm__section'>
                <h3>Commit preparation</h3>
                {!semanticScmState
                    ? <p>{this.errorMessage ?? 'No semantic SCM payload is available yet for commit preparation.'}</p>
                    : !commit
                        ? <p>No commit-preparation state is available because the current baseline request did not resolve cleanly.</p>
                        : <>
                            <p>{commit.summary ?? this.renderAffectedPackages(commit.affectedPackages)}</p>
                            <ul className='athena-semantic-scm__list'>
                                {commit.entries.map(entry => this.renderCommitEntry(entry))}
                            </ul>
                        </>}
            </section>

            <section className='athena-semantic-scm__section'>
                <h3>Package evolution</h3>
                {this.renderHistorySection(semanticHistoryState, history, sessionState.primaryPackageName)}
            </section>
        </div>;
    }

    protected renderControls(primaryPackageName: string | undefined): React.ReactNode {
        return <section className='athena-semantic-scm__controls'>
            <div className='athena-semantic-scm__control'>
                <label htmlFor='athena-semantic-scm-label'>Baseline label</label>
                <input
                    id='athena-semantic-scm-label'
                    type='text'
                    value={this.baselineLabel}
                    onChange={event => this.onBaselineLabelChanged(event)}
                />
            </div>
            <div className='athena-semantic-scm__control'>
                <label htmlFor='athena-semantic-scm-locator'>Baseline locator</label>
                <input
                    id='athena-semantic-scm-locator'
                    type='text'
                    value={this.baselineLocator}
                    onChange={event => this.onBaselineLocatorChanged(event)}
                />
            </div>
            <div className='athena-semantic-scm__control'>
                <label htmlFor='athena-semantic-scm-package'>History package</label>
                <input
                    id='athena-semantic-scm-package'
                    type='text'
                    placeholder={primaryPackageName ?? 'com.engineeringood.package'}
                    value={this.historyPackageName}
                    onChange={event => this.onHistoryPackageNameChanged(event)}
                />
            </div>
            <div className='athena-semantic-scm__control athena-semantic-scm__control--wide'>
                <label htmlFor='athena-semantic-scm-history-baselines'>History baselines</label>
                <textarea
                    id='athena-semantic-scm-history-baselines'
                    rows={3}
                    value={this.historyBaselineSequence}
                    onChange={event => this.onHistoryBaselineSequenceChanged(event)}
                />
                <span className='athena-semantic-scm__hint'>Use one baseline per line as <code>Label|locator</code>.</span>
            </div>
            <button
                className='athena-semantic-scm__refresh'
                type='button'
                onClick={() => void this.refreshSemanticScmState()}
            >
                Refresh semantic SCM
            </button>
        </section>;
    }

    protected renderHistorySection(
        semanticHistoryState: AthenaSemanticHistoryStatePayload | undefined,
        history: AthenaSemanticHistoryStatePayload['history'],
        primaryPackageName: string | undefined
    ): React.ReactNode {
        if (this.historyErrorMessage) {
            return <p>{this.historyErrorMessage}</p>;
        }
        if (!semanticHistoryState) {
            return <p>No semantic history payload is available yet for the active Athena session.</p>;
        }
        if (!history) {
            return <p>No semantic history summary is available because the current baseline sequence did not resolve cleanly.</p>;
        }

        return <>
            <p>{history.summary ?? `Package evolution for ${this.renderPackage(history.packageId)}.`}</p>
            <div className='athena-semantic-scm__pill-row'>
                <span className='athena-semantic-scm__pill'>{this.renderPackage(history.packageId)}</span>
                <span className='athena-semantic-scm__pill'>{history.releaseRelevance}</span>
                <span className='athena-semantic-scm__pill'>{history.contractBreakRisk}</span>
                <span className='athena-semantic-scm__pill'>{history.baselineCount} baselines</span>
                {primaryPackageName && primaryPackageName !== history.packageId.name
                    ? <span className='athena-semantic-scm__pill'>{primaryPackageName}</span>
                    : undefined}
            </div>
            <ul>
                {semanticHistoryState.baselines.map(baseline => <li key={`${baseline.baselineId}:${baseline.locator}`}>
                    {baseline.baselineLabel}: <code>{baseline.locator}</code>
                </li>)}
            </ul>
            <div className='athena-semantic-scm__subsection'>
                <h4>Package lineage</h4>
                {history.packageLineage.length === 0
                    ? <p>No package lineage was published for the current baseline sequence.</p>
                    : <ul className='athena-semantic-scm__list'>
                        {history.packageLineage.map(lineage => <li
                            key={`${lineage.packageId.name}:${lineage.baselineVersion ?? 'none'}:${lineage.currentVersion ?? 'none'}`}
                            className='athena-semantic-scm__item'
                        >
                            <strong>{lineage.changeKind}</strong>
                            <div>{this.renderPackage(lineage.packageId)}</div>
                            <div>Baseline version: {lineage.baselineVersion ?? 'unspecified'} | Current version: {lineage.currentVersion ?? 'unspecified'}</div>
                        </li>)}
                    </ul>}
            </div>
            <div className='athena-semantic-scm__subsection'>
                <h4>Validation movement</h4>
                {history.validationMovements.length === 0
                    ? <p>No validation movement was published for the current baseline sequence.</p>
                    : <ul className='athena-semantic-scm__list'>
                        {history.validationMovements.map(movement => this.renderValidationMovement(movement))}
                    </ul>}
            </div>
            <div className='athena-semantic-scm__subsection'>
                <h4>History entries</h4>
                {history.entries.length === 0
                    ? <p>No semantic history entries were published for the current baseline sequence.</p>
                    : <ul className='athena-semantic-scm__list'>
                        {history.entries.map(entry => this.renderHistoryEntry(entry))}
                    </ul>}
            </div>
        </>;
    }

    protected renderAffectedPackages(
        packages: AthenaSemanticPackagePayload[]
    ): string {
        if (packages.length === 0) {
            return 'No affected packages were published for this semantic SCM state.';
        }
        return `Affected packages: ${packages.map(pkg => this.renderPackage(pkg)).join(', ')}`;
    }

    protected renderPackage(pkg: AthenaSemanticPackagePayload): string {
        return pkg.version ? `${pkg.name}@${pkg.version}` : pkg.name;
    }

    protected renderReviewEntry(entry: AthenaSemanticReviewEntryPayload): React.ReactNode {
        return <li key={`${entry.kind}:${entry.message}`} className='athena-semantic-scm__item'>
            <strong>{entry.kind}</strong>
            <div>{entry.message}</div>
            {entry.factReferences.length > 0
                ? <div>{entry.factReferences.map(reference => `${reference.kind}:${reference.identifier}`).join(', ')}</div>
                : undefined}
        </li>;
    }

    protected renderReviewEnrichment(enrichment: AthenaSemanticReviewEnrichmentPayload): React.ReactNode {
        return <li key={`${enrichment.pluginId}:${enrichment.kind}:${enrichment.message}`} className='athena-semantic-scm__item athena-semantic-scm__item--enrichment'>
            <strong>{enrichment.kind}</strong>
            <div>{enrichment.message}</div>
            <div>{enrichment.pluginId}</div>
        </li>;
    }

    protected renderCommitEntry(entry: AthenaSemanticCommitEntryPayload): React.ReactNode {
        return <li key={`${entry.kind}:${entry.message}`} className='athena-semantic-scm__item'>
            <strong>{entry.kind}</strong>
            <div>{entry.message}</div>
            {entry.factReferences.length > 0
                ? <div>{entry.factReferences.map(reference => `${reference.kind}:${reference.identifier}`).join(', ')}</div>
                : undefined}
        </li>;
    }

    protected renderValidationMovement(
        movement: AthenaSemanticValidationMovementPayload
    ): React.ReactNode {
        return <li
            key={`${movement.baselineErrorCount}:${movement.currentErrorCount}:${movement.message}`}
            className='athena-semantic-scm__item'
        >
            <strong>validation-movement</strong>
            <div>{movement.message}</div>
            <div>
                Errors: {movement.baselineErrorCount}{' -> '}{movement.currentErrorCount} | Warnings: {movement.baselineWarningCount}{' -> '}{movement.currentWarningCount}
            </div>
        </li>;
    }

    protected renderHistoryEntry(entry: AthenaSemanticHistoryEntryPayload): React.ReactNode {
        return <li
            key={`${entry.baselineId}:${entry.kind}:${entry.message}`}
            className='athena-semantic-scm__item'
        >
            <div className='athena-semantic-scm__item-header'>
                <strong>{entry.kind}</strong>
                <span className='athena-semantic-scm__pill'>{entry.baselineLabel}</span>
                <span className='athena-semantic-scm__pill'>{entry.releaseRelevance}</span>
                <span className='athena-semantic-scm__pill'>{entry.contractBreakRisk}</span>
            </div>
            <div>{entry.message}</div>
            <div>{this.renderPackage(entry.packageVersion.packageId)}</div>
            <div>
                Version meaning: {entry.packageVersion.changeKind} | Baseline: {entry.packageVersion.baselineVersion ?? 'unspecified'} | Current: {entry.packageVersion.currentVersion ?? 'unspecified'}
            </div>
            {entry.changeCategory
                ? <div>Change category: {entry.changeCategory}</div>
                : undefined}
            {entry.dependencyMovements.length > 0
                ? <div>
                    Dependency movements: {entry.dependencyMovements.map(movement =>
                        `${movement.kind}:${this.renderPackage(movement.packageId)}`
                    ).join(', ')}
                </div>
                : undefined}
            {entry.validationMovement
                ? <div>{entry.validationMovement.message}</div>
                : undefined}
            <div>Authored changes: {entry.authoredChangeCount} | Derived consequences: {entry.derivedConsequenceCount}</div>
        </li>;
    }
}
