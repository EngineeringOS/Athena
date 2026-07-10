import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import {
    AthenaLspEditorBridgeService,
    AthenaSemanticInspectionPayload
} from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';

@injectable()
export class AthenaSemanticInspectionWidget extends ReactWidget {
    static readonly ID = 'athena.semanticInspection';
    static readonly LABEL = 'Semantic Inspection';

    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    @inject(AthenaSemanticSelectionService)
    protected readonly semanticSelectionService: AthenaSemanticSelectionService;

    protected currentEditorListeners = new DisposableCollection();
    protected inspection: AthenaSemanticInspectionPayload | undefined;
    protected errorMessage: string | undefined;
    protected loading = false;
    protected refreshHandle: number | undefined;

    @postConstruct()
    protected init(): void {
        this.id = AthenaSemanticInspectionWidget.ID;
        this.title.label = AthenaSemanticInspectionWidget.LABEL;
        this.title.caption = AthenaSemanticInspectionWidget.LABEL;
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

    protected scheduleRefresh(): void {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshInspection();
        }, 120);
    }

    protected async refreshInspection(): Promise<void> {
        const sessionState = this.repositorySessionService.state;
        const currentEditor = this.editorManager.currentEditor;

        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.inspection = undefined;
            this.update();
            return;
        }

        if (!this.isAthenaEditor(currentEditor)) {
            this.loading = false;
            this.errorMessage = undefined;
            this.inspection = undefined;
            this.update();
            return;
        }

        const currentUri = currentEditor.editor.uri.toString();
        this.loading = true;
        this.errorMessage = undefined;
        this.update();

        try {
            const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            this.inspection = inspection;
        } catch (error) {
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.inspection = undefined;
        } finally {
            if (this.editorManager.currentEditor?.editor.uri.toString() === currentUri) {
                this.loading = false;
                this.update();
            }
        }
    }

    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }

    protected render(): React.ReactNode {
        const sessionState = this.repositorySessionService.state;
        const currentEditor = this.editorManager.currentEditor;

        if (sessionState.lifecycle !== 'ready') {
            return <div className='athena-semantic-inspection'>
                <section className='athena-semantic-inspection__empty'>
                    <h2>Semantic Inspection</h2>
                    <p>{sessionState.message}</p>
                </section>
            </div>;
        }

        if (!this.isAthenaEditor(currentEditor)) {
            return <div className='athena-semantic-inspection'>
                <section className='athena-semantic-inspection__empty'>
                    <h2>Semantic Inspection</h2>
                    <p>Open one <code>.athena</code> file to inspect Athena-owned semantic state beside the editor.</p>
                </section>
            </div>;
        }

        if (this.errorMessage) {
            return <div className='athena-semantic-inspection'>
                <section className='athena-semantic-inspection__empty athena-semantic-inspection__empty--error'>
                    <h2>Semantic Inspection</h2>
                    <p>{this.errorMessage}</p>
                </section>
            </div>;
        }

        if (this.loading && !this.inspection) {
            return <div className='athena-semantic-inspection'>
                <section className='athena-semantic-inspection__empty'>
                    <h2>Semantic Inspection</h2>
                    <p>Loading the latest Athena semantic snapshot for the current editor.</p>
                </section>
            </div>;
        }

        const inspection = this.inspection;
        if (!inspection) {
            return <div className='athena-semantic-inspection'>
                <section className='athena-semantic-inspection__empty'>
                    <h2>Semantic Inspection</h2>
                    <p>No semantic inspection snapshot is available yet for the current Athena document.</p>
                </section>
            </div>;
        }

        return <div className='athena-semantic-inspection'>
            <header className='athena-semantic-inspection__header'>
                <div>
                    <div className='athena-semantic-inspection__eyebrow'>Athena LSP inspection</div>
                    <h2>{inspection.systemName ?? 'Unresolved system'}</h2>
                    <p>{sessionState.projectName ?? 'Unknown project'} | <code>{inspection.uri}</code></p>
                </div>
                <div className={`athena-semantic-inspection__status athena-semantic-inspection__status--${inspection.status}`}>
                    {inspection.status}
                </div>
            </header>

            <section className='athena-semantic-inspection__summary'>
                <ul className='athena-semantic-inspection__summary-list'>
                    <li><span>Components</span><strong>{inspection.componentCount}</strong></li>
                    <li><span>Ports</span><strong>{inspection.portCount}</strong></li>
                    <li><span>Connections</span><strong>{inspection.connectionCount}</strong></li>
                    <li><span>Diagnostics</span><strong>{inspection.diagnosticsCount}</strong></li>
                </ul>
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Selected semantic</h3>
                {this.semanticSelectionService.selection
                    ? <div className='athena-semantic-inspection__selection'>
                        <strong>{this.semanticSelectionService.selection.label ?? this.semanticSelectionService.selection.semanticId}</strong><br />
                        <code>{this.semanticSelectionService.selection.semanticId}</code>
                    </div>
                    : <p>No synchronized semantic selection is active yet.</p>}
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Document state</h3>
                <ul className='athena-semantic-inspection__detail-list'>
                    <li><span>Version</span><strong>{inspection.version}</strong></li>
                    <li><span>Semantic path</span><strong>{sessionState.semanticPath ?? 'frontend -> LSP -> runtime/compiler'}</strong></li>
                    <li><span>Current editor</span><strong><code>{currentEditor.editor.uri.toString()}</code></strong></li>
                </ul>
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Diagnostics</h3>
                {inspection.diagnosticSummaries.length === 0
                    ? <p>No diagnostics are currently attached to this tracked document state.</p>
                    : <ul className='athena-semantic-inspection__dense-list'>
                        {inspection.diagnosticSummaries.map(summary => <li key={summary}>{summary}</li>)}
                    </ul>}
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Components</h3>
                {inspection.components.length === 0
                    ? <p>No canonical components were derived from the current document state.</p>
                    : <ul className='athena-semantic-inspection__list'>
                        {inspection.components.map(component => <li
                            key={component.semanticId}
                            className={`athena-semantic-inspection__item ${this.isSelected(component.semanticId) ? 'athena-semantic-inspection__item--selected' : ''}`}
                        >
                            <button
                                className='athena-semantic-inspection__selectable'
                                type='button'
                                onClick={() => void this.semanticSelectionService.selectSemanticId(component.semanticId)}
                            >
                                <span className='athena-semantic-inspection__item-title'>{component.name} <span>({component.kind})</span></span>
                                <span className='athena-semantic-inspection__item-meta'>{component.properties}</span>
                            </button>
                        </li>)}
                    </ul>}
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Ports</h3>
                {inspection.ports.length === 0
                    ? <p>No canonical ports were derived from the current document state.</p>
                    : <ul className='athena-semantic-inspection__list'>
                        {inspection.ports.map(port => <li
                            key={port.semanticId}
                            className={`athena-semantic-inspection__item ${this.isSelected(port.semanticId) ? 'athena-semantic-inspection__item--selected' : ''}`}
                        >
                            <button
                                className='athena-semantic-inspection__selectable'
                                type='button'
                                onClick={() => void this.semanticSelectionService.selectSemanticId(port.semanticId)}
                            >
                                <span className='athena-semantic-inspection__item-title'>{port.path}</span>
                                <span className='athena-semantic-inspection__item-meta'>{port.properties}</span>
                            </button>
                        </li>)}
                    </ul>}
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Connections</h3>
                {inspection.connections.length === 0
                    ? <p>No canonical connections are present in the current document state.</p>
                    : <ul className='athena-semantic-inspection__list'>
                        {inspection.connections.map(connection => <li
                            key={connection.semanticId}
                            className={`athena-semantic-inspection__item ${this.isSelected(connection.semanticId) ? 'athena-semantic-inspection__item--selected' : ''}`}
                        >
                            <button
                                className='athena-semantic-inspection__selectable'
                                type='button'
                                onClick={() => void this.semanticSelectionService.selectSemanticId(connection.semanticId)}
                            >
                                <span className='athena-semantic-inspection__item-title'>{connection.fromPath} <span>-&gt;</span> {connection.toPath}</span>
                            </button>
                        </li>)}
                    </ul>}
            </section>
        </div>;
    }

    protected isSelected(semanticId: string): boolean {
        return this.semanticSelectionService.selection?.semanticId === semanticId;
    }
}
