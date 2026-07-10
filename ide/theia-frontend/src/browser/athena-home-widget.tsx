import * as React from 'react';

import { CommandService } from '@theia/core/lib/common';
import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { ATHENA_WORKBENCH_EXTENSIONS, AthenaCommands } from './athena-workbench-extensions';

@injectable()
export class AthenaHomeWidget extends ReactWidget {
    static readonly ID = 'athena.home';
    static readonly LABEL = 'Athena Home';

    @inject(CommandService)
    protected readonly commandService: CommandService;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @postConstruct()
    protected init(): void {
        this.id = AthenaHomeWidget.ID;
        this.title.label = AthenaHomeWidget.LABEL;
        this.title.caption = AthenaHomeWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-home';
        this.addClass('athena-home-widget');
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.update()));
        this.update();
    }

    protected render(): React.ReactNode {
        const sessionState = this.repositorySessionService.state;
        const sessionStatusClassName = `athena-home__session-status athena-home__session-status--${sessionState.lifecycle}`;
        const sessionRows: Array<{ key: string; label: string; value: React.ReactNode }> = [
            { key: 'repository-root', label: 'Repository root', value: sessionState.repositoryRoot ?? 'Not open' },
            { key: 'manifest', label: 'Manifest', value: sessionState.manifestPath ?? 'Not validated yet' },
            { key: 'lock', label: 'Lock', value: sessionState.lockPath ?? 'Not validated yet' },
            { key: 'source-root', label: 'Governed source root', value: sessionState.sourceRootPath ?? 'Not validated yet' },
            { key: 'source', label: 'Authored source', value: sessionState.sourcePath ?? 'Not resolved' },
            { key: 'project', label: 'Project key', value: sessionState.projectName ?? 'Not activated' },
            { key: 'package', label: 'Primary package', value: sessionState.primaryPackageName ?? 'Not validated yet' },
            { key: 'semantic-path', label: 'Semantic path', value: sessionState.semanticPath ?? 'frontend -> LSP -> runtime/compiler' },
            { key: 'editor', label: 'Last Athena editor', value: sessionState.lastOpenedDocumentUri ?? 'Not opened yet' },
            { key: 'rule', label: 'Window rule', value: 'Single-session M4 rule stays enforced per window' }
        ];

        return <div className='athena-home'>
            <header className='athena-home__masthead'>
                <div className='athena-home__brand'>
                    <div className='athena-home__brand-mark' aria-hidden='true'>
                        <span className='athena-home__brand-mark-core'>A</span>
                    </div>
                    <div className='athena-home__brand-copy'>
                        <span className='athena-home__brand-name'>ATHENA</span>
                        <span className='athena-home__brand-tagline'>Engineering semantic workstation</span>
                    </div>
                </div>
                <span className={sessionStatusClassName}>{sessionState.lifecycle}</span>
            </header>

            <section className='athena-home__intro'>
                <h1>A serious engineering shell, not a generic editor demo.</h1>
                <p>
                    This product shell proves the Athena IDE boundary on top of Theia while keeping
                    semantic authority downstream in the JVM stack behind <code>ide/lsp</code>.
                </p>
            </section>

            <section className='athena-home__toolbar'>
                <button
                    className='athena-home__action-button athena-home__action-button--secondary'
                    type='button'
                    onClick={() => void this.commandService.executeCommand(AthenaCommands.CREATE_ENGINEERING_REPOSITORY.id)}
                >
                    Create Engineering Repository
                </button>
                <button
                    className='athena-home__action-button'
                    type='button'
                    onClick={() => void this.commandService.executeCommand(AthenaCommands.OPEN_ENGINEERING_REPOSITORY.id)}
                >
                    Open Engineering Repository
                </button>
            </section>

            <section className='athena-home__layout'>
                <article className='athena-home__panel'>
                    <div className='athena-home__panel-header'>
                        <h2>Current proof</h2>
                    </div>
                    <ul className='athena-home__dense-list'>
                        <li>Branded Athena desktop shell</li>
                        <li>Curated workbench capability set</li>
                        <li>Repository navigation docked as a persistent left workbench panel</li>
                        <li>Problems and Output docked as persistent bottom workbench panels</li>
                        <li>Frontend and backend ownership split</li>
                        <li>Governed repository bootstrap from the welcome flow</li>
                        <li>Athena-authored files enter semantics only through LSP</li>
                        <li>Diagnostics land in editor and Problems from the JVM stack</li>
                        <li>Completion and navigation now come from the same JVM-owned LSP path</li>
                        <li>Repository graph feedback is visible through an Athena workbench panel</li>
                        <li>Semantic review and commit-preparation feedback now stay visible through a dedicated Athena SCM panel</li>
                        <li>Text-first path with future projection safety</li>
                    </ul>
                </article>
                <article className='athena-home__panel'>
                    <div className='athena-home__panel-header'>
                        <h2>Repository session</h2>
                    </div>
                    <p className='athena-home__panel-lead'>{sessionState.message}</p>
                    <ul className='athena-home__kv-list'>
                        {sessionRows.map(row => <li key={row.key}>
                            <span className='athena-home__kv-key'>{row.label}</span>
                            <strong className='athena-home__kv-value'>{row.value}</strong>
                        </li>)}
                    </ul>
                </article>
                <article className='athena-home__panel athena-home__panel--wide'>
                    <div className='athena-home__panel-header'>
                        <h2>Workbench views</h2>
                    </div>
                    <p className='athena-home__panel-lead'>
                        Athena workbench additions attach through one product-owned extension registry, so later milestones can add panels without replacing the shell.
                    </p>
                    <div className='athena-home__toolbar athena-home__toolbar--panel'>
                        <button
                            className='athena-home__action-button athena-home__action-button--secondary'
                            type='button'
                            onClick={() => void this.commandService.executeCommand(AthenaCommands.OPEN_HOME.id)}
                        >
                            Athena Home
                        </button>
                        {ATHENA_WORKBENCH_EXTENSIONS.map(extension => <button
                            key={extension.command.id}
                            className='athena-home__action-button athena-home__action-button--secondary'
                            type='button'
                            onClick={() => void this.commandService.executeCommand(extension.command.id)}
                        >
                            {extension.quickActionLabel}
                        </button>)}
                    </div>
                </article>

                <article className='athena-home__panel athena-home__panel--wide'>
                    <div className='athena-home__panel-header'>
                        <h2>Capability boundary</h2>
                    </div>
                    <ul className='athena-home__dense-list'>
                        <li>
                            This shell now carries the M6 operability essentials on top of the M4 and M5 base: editing, completion, document symbols, go-to-definition, references, diagnostics in Problems and editor markers, repository navigation, package-graph feedback, semantic review and commit-preparation projection, output panels, terminal visibility, and product-owned workbench framing.
                        </li>
                        <li>Marketplace sprawl, semantic history publishing, and graphical projection tooling remain intentionally deferred.</li>
                    </ul>
                </article>

                <article className='athena-home__panel athena-home__panel--wide'>
                    <div className='athena-home__panel-header'>
                        <h2>Current repository rule</h2>
                    </div>
                    <ul className='athena-home__kv-list'>
                        <li>
                            <span className='athena-home__kv-key'>Contract</span>
                            <strong className='athena-home__kv-value'>
                                Athena validates <code>athena.yaml</code>, <code>athena.lock</code>, and the governed <code>src/</code> layout through the JVM stack.
                            </strong>
                        </li>
                        <li>
                            <span className='athena-home__kv-key'>Editor seed</span>
                            <strong className='athena-home__kv-value'>
                                The runtime derives one deterministic authored source as the temporary editor seed for the active session.
                            </strong>
                        </li>
                        <li>
                            <span className='athena-home__kv-key'>Repository meaning</span>
                            <strong className='athena-home__kv-value'>
                                The active package graph is inspectable through the dedicated Repository Graph workbench view rather than a single-file shortcut.
                            </strong>
                        </li>
                    </ul>
                </article>
            </section>
        </div>;
    }
}
