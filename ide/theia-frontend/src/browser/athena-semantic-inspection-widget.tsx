import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import {
    AthenaLspEditorBridgeService,
    AthenaSemanticInspectionPayload,
} from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';

@injectable()
export class AthenaSemanticInspectionWidget extends ReactWidget {
    static readonly ID = 'athena.semanticInspection';
    static readonly LABEL = 'Engineering Inspection';

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
        if (this.isAthenaEditor(widget)) {
            this.currentEditorListeners.push(widget.editor.onDocumentContentChanged(() => this.scheduleRefresh()));
        }
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
        const currentEditor = this.editorManager.currentEditor;
        if (this.repositorySessionService.state.lifecycle !== 'ready' || !this.isAthenaEditor(currentEditor)) {
            this.inspection = undefined;
            this.errorMessage = undefined;
            this.loading = false;
            this.update();
            return;
        }

        const currentUri = currentEditor.editor.uri.toString();
        this.loading = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
            if (this.editorManager.currentEditor?.editor.uri.toString() === currentUri) {
                this.inspection = inspection;
            }
        } catch (error) {
            if (this.editorManager.currentEditor?.editor.uri.toString() === currentUri) {
                this.inspection = undefined;
                this.errorMessage = error instanceof Error ? error.message : String(error);
            }
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

    protected isSelected(semanticId: string): boolean {
        return this.semanticSelectionService.selection?.semanticId === semanticId;
    }

    protected render(): React.ReactNode {
        const sessionState = this.repositorySessionService.state;
        const currentEditor = this.editorManager.currentEditor;
        if (sessionState.lifecycle !== 'ready') {
            return this.renderEmpty(sessionState.message);
        }
        if (!this.isAthenaEditor(currentEditor)) {
            return this.renderEmpty('Open an Athena source file.');
        }
        if (this.errorMessage) {
            return this.renderEmpty(this.errorMessage, true);
        }
        if (this.loading && !this.inspection) {
            return this.renderEmpty('Loading engineering facts.');
        }
        const inspection = this.inspection;
        if (!inspection) {
            return this.renderEmpty('No engineering inspection available.');
        }

        return <div className='athena-semantic-inspection'>
            <header className='athena-semantic-inspection__header'>
                <div>
                    <div className='athena-semantic-inspection__eyebrow'>Compiled Engineering Reality</div>
                    <h2>{inspection.systemName ?? 'Unresolved system'}</h2>
                    <p><code>{inspection.uri}</code></p>
                </div>
                <div className={`athena-semantic-inspection__status athena-semantic-inspection__status--${inspection.status}`}>
                    {inspection.status}
                </div>
            </header>

            <section className='athena-semantic-inspection__summary'>
                <ul className='athena-semantic-inspection__summary-list'>
                    <li><span>Entities</span><strong>{inspection.entityCount}</strong></li>
                    <li><span>Ports</span><strong>{inspection.portCount}</strong></li>
                    <li><span>Relationships</span><strong>{inspection.relationshipCount}</strong></li>
                    <li><span>Diagnostics</span><strong>{inspection.diagnosticsCount}</strong></li>
                </ul>
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Entities</h3>
                {inspection.entities.length === 0
                    ? <p>No Entities compiled.</p>
                    : <ul className='athena-semantic-inspection__list'>
                        {inspection.entities.map(entity => <li
                            key={entity.semanticId}
                            className={`athena-semantic-inspection__item ${this.isSelected(entity.semanticId) ? 'athena-semantic-inspection__item--selected' : ''}`}
                        >
                            <button
                                className='athena-semantic-inspection__selectable'
                                type='button'
                                onClick={() => void this.semanticSelectionService.selectSemanticId(entity.semanticId)}
                            >
                                <span className='athena-semantic-inspection__item-title'>{entity.name}</span>
                                <span className='athena-semantic-inspection__item-meta'>{entity.concept} | {entity.semanticId}</span>
                            </button>
                        </li>)}
                    </ul>}
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Ports</h3>
                {inspection.ports.length === 0
                    ? <p>No Ports compiled.</p>
                    : <ul className='athena-semantic-inspection__list'>
                        {inspection.ports.map(port => <li key={port.semanticId} className='athena-semantic-inspection__item'>
                            <button
                                className='athena-semantic-inspection__selectable'
                                type='button'
                                onClick={() => void this.semanticSelectionService.selectSemanticId(port.semanticId)}
                            >
                                <span className='athena-semantic-inspection__item-title'>{port.path}</span>
                                <span className='athena-semantic-inspection__item-meta'>{port.semanticId}</span>
                            </button>
                        </li>)}
                    </ul>}
            </section>

            <section className='athena-semantic-inspection__section'>
                <h3>Diagnostics</h3>
                {inspection.diagnosticSummaries.length === 0
                    ? <p>No diagnostics.</p>
                    : <ul className='athena-semantic-inspection__dense-list'>
                        {inspection.diagnosticSummaries.map(summary => <li key={summary}>{summary}</li>)}
                    </ul>}
            </section>
        </div>;
    }

    protected renderEmpty(message: string, error = false): React.ReactNode {
        return <div className='athena-semantic-inspection'>
            <section className={`athena-semantic-inspection__empty ${error ? 'athena-semantic-inspection__empty--error' : ''}`}>
                <h2>{AthenaSemanticInspectionWidget.LABEL}</h2>
                <p>{message}</p>
            </section>
        </div>;
    }
}
