import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import {
    AthenaLspEditorBridgeService,
    AthenaRepositoryDiagnosticPayload,
    AthenaRepositoryGraphSessionPayload,
    AthenaRepositoryManifestDependencyPayload,
    AthenaRepositoryResolvedPackagePayload
} from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';

@injectable()
export class AthenaRepositoryGraphWidget extends ReactWidget {
    static readonly ID = 'athena.repositoryGraph';
    static readonly LABEL = 'Repository Graph';

    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    protected currentEditorListeners = new DisposableCollection();
    protected graphSession: AthenaRepositoryGraphSessionPayload | undefined;
    protected errorMessage: string | undefined;
    protected loading = false;
    protected refreshHandle: number | undefined;

    @postConstruct()
    protected init(): void {
        this.id = AthenaRepositoryGraphWidget.ID;
        this.title.label = AthenaRepositoryGraphWidget.LABEL;
        this.title.caption = AthenaRepositoryGraphWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-list-tree';
        this.addClass('athena-repository-graph-widget');

        this.toDispose.push(this.currentEditorListeners);
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(widget => {
            this.bindCurrentEditor(widget);
            this.scheduleRefresh();
        }));
        this.toDispose.push(Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
            }
        }));

        this.bindCurrentEditor(this.editorManager.currentEditor);
        this.scheduleRefresh();
    }

    protected bindCurrentEditor(widget: EditorWidget | undefined): void {
        this.currentEditorListeners.dispose();
        this.currentEditorListeners = new DisposableCollection();
        this.toDispose.push(this.currentEditorListeners);

        if (!this.isAthenaEditor(widget)) {
            return;
        }

        this.currentEditorListeners.push(widget.editor.onDocumentContentChanged(() => this.scheduleRefresh()));
    }

    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }

    protected scheduleRefresh(): void {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshGraphSession();
        }, 120);
    }

    protected async refreshGraphSession(): Promise<void> {
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
        } catch (error) {
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.graphSession = undefined;
        } finally {
            if (this.repositorySessionService.state.repositoryRoot === currentRepositoryRoot) {
                this.loading = false;
                this.update();
            }
        }
    }

    protected render(): React.ReactNode {
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            return <div className='athena-repository-graph'>
                <section className='athena-repository-graph__empty'>
                    <h2>Repository Graph</h2>
                    <p>{sessionState.message}</p>
                </section>
            </div>;
        }

        if (this.errorMessage) {
            return <div className='athena-repository-graph'>
                <section className='athena-repository-graph__empty athena-repository-graph__empty--error'>
                    <h2>Repository Graph</h2>
                    <p>{this.errorMessage}</p>
                </section>
            </div>;
        }

        if (this.loading && !this.graphSession) {
            return <div className='athena-repository-graph'>
                <section className='athena-repository-graph__empty'>
                    <h2>Repository Graph</h2>
                    <p>Loading the canonical package graph from Athena LSP.</p>
                </section>
            </div>;
        }

        const graphSession = this.graphSession;
        if (!graphSession) {
            return <div className='athena-repository-graph'>
                <section className='athena-repository-graph__empty'>
                    <h2>Repository Graph</h2>
                    <p>No repository graph payload is available yet for the active Athena session.</p>
                </section>
            </div>;
        }

        const statusTone = graphSession.isValid && graphSession.diagnostics.length === 0 ? 'clean' : 'issue';
        const lockStateTone = graphSession.isValid ? 'clean' : 'issue';

        return <div className='athena-repository-graph'>
            <header className='athena-repository-graph__header'>
                <div>
                    <div className='athena-repository-graph__eyebrow'>Athena package graph</div>
                    <h2>{graphSession.primaryPackageName}</h2>
                    <p><code>{graphSession.repositoryRoot}</code></p>
                </div>
                <div className={`athena-repository-graph__status athena-repository-graph__status--${statusTone}`}>
                    {graphSession.isValid ? 'ready' : 'issues'}
                </div>
            </header>

            <section className='athena-repository-graph__summary'>
                <ul className='athena-repository-graph__summary-list'>
                    <li><span>Resolved packages</span><strong>{graphSession.resolvedPackages.length}</strong></li>
                    <li><span>Manifest dependencies</span><strong>{graphSession.manifestDependencies.length}</strong></li>
                    <li><span>Package diagnostics</span><strong>{graphSession.diagnostics.length}</strong></li>
                    <li><span>Lock state</span><strong>{graphSession.lockState}</strong></li>
                </ul>
            </section>

            <section className='athena-repository-graph__section'>
                <h3>Repository contract</h3>
                <ul className='athena-repository-graph__detail-list'>
                    <li><span>Manifest</span><strong><code>{graphSession.manifestPath}</code></strong></li>
                    <li><span>Lock</span><strong><code>{graphSession.lockPath}</code></strong></li>
                    <li><span>Governed source root</span><strong><code>{graphSession.sourceRootPath}</code></strong></li>
                    <li><span>Authored source</span><strong><code>{graphSession.sourcePath}</code></strong></li>
                    <li><span>Project key</span><strong>{graphSession.projectName}</strong></li>
                    <li><span>Semantic path</span><strong>{graphSession.semanticPath}</strong></li>
                    <li><span>Last Athena editor</span><strong>{graphSession.lastOpenedDocumentUri ?? 'Not opened yet'}</strong></li>
                    <li><span>Lock status</span><strong><span className={`athena-repository-graph__pill athena-repository-graph__pill--${lockStateTone}`}>{graphSession.lockState}</span></strong></li>
                </ul>
            </section>

            <section className='athena-repository-graph__section'>
                <h3>Manifest dependency intent</h3>
                {graphSession.manifestDependencies.length === 0
                    ? <p>The primary package currently declares no external package dependencies.</p>
                    : <ul className='athena-repository-graph__list'>
                        {graphSession.manifestDependencies.map(dependency => this.renderDependency(dependency))}
                    </ul>}
            </section>

            <section className='athena-repository-graph__section'>
                <h3>Resolved package graph</h3>
                {graphSession.resolvedPackages.length === 0
                    ? <p>No resolved packages are available for the active repository session.</p>
                    : <ul className='athena-repository-graph__list'>
                        {graphSession.resolvedPackages.map(resolvedPackage => this.renderResolvedPackage(resolvedPackage))}
                    </ul>}
            </section>

            <section className='athena-repository-graph__section'>
                <h3>Package diagnostics</h3>
                {graphSession.diagnostics.length === 0
                    ? <p>No package diagnostics are currently attached to this repository graph session.</p>
                    : <ul className='athena-repository-graph__list'>
                        {graphSession.diagnostics.map(diagnostic => this.renderDiagnostic(diagnostic))}
                    </ul>}
            </section>
        </div>;
    }

    protected renderDependency(dependency: AthenaRepositoryManifestDependencyPayload): React.ReactNode {
        const dependencyTarget = dependency.locator ?? dependency.version ?? 'unspecified';
        return <li key={`${dependency.name}:${dependency.source}:${dependencyTarget}`} className='athena-repository-graph__item'>
            <span className='athena-repository-graph__item-title'>{dependency.name}</span>
            <span className='athena-repository-graph__item-meta'>{dependency.source} | {dependencyTarget}</span>
        </li>;
    }

    protected renderResolvedPackage(resolvedPackage: AthenaRepositoryResolvedPackagePayload): React.ReactNode {
        const packageLabel = resolvedPackage.version
            ? `${resolvedPackage.name}@${resolvedPackage.version}`
            : resolvedPackage.name;
        const dependencies = resolvedPackage.directDependencies.length === 0
            ? 'No direct dependencies'
            : resolvedPackage.directDependencies.join(', ');
        return <li key={`${resolvedPackage.name}:${resolvedPackage.sourceRoot}`} className='athena-repository-graph__item'>
            <span className='athena-repository-graph__item-title'>{packageLabel}</span>
            <span className='athena-repository-graph__item-meta'><code>{resolvedPackage.sourceRoot}</code></span>
            <span className='athena-repository-graph__item-meta'>{dependencies}</span>
        </li>;
    }

    protected renderDiagnostic(diagnostic: AthenaRepositoryDiagnosticPayload): React.ReactNode {
        const severity = diagnostic.severity.toLowerCase();
        return <li key={`${diagnostic.code}:${diagnostic.message}`} className='athena-repository-graph__item athena-repository-graph__item--diagnostic'>
            <div className='athena-repository-graph__diagnostic-header'>
                <span className={`athena-repository-graph__diagnostic-severity athena-repository-graph__diagnostic-severity--${severity}`}>
                    {diagnostic.severity}
                </span>
                <code>{diagnostic.code}</code>
            </div>
            <div>{diagnostic.message}</div>
        </li>;
    }
}
