import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import {
    AthenaConnectionReadModelItem,
    AthenaConnectionReadModelPublication,
    AthenaLspEditorBridgeService,
} from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';

@injectable()
export class AthenaConnectionNavigatorWidget extends ReactWidget {
    static readonly ID = 'athena.connectionNavigator';
    static readonly LABEL = 'Connections';

    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    @inject(AthenaSemanticSelectionService)
    protected readonly semanticSelectionService: AthenaSemanticSelectionService;

    protected readonly readyByAcceptedInputRevision = new Map<string, AthenaConnectionReadModelPublication>();
    protected editorListeners = new DisposableCollection();
    protected readModel: AthenaConnectionReadModelPublication | undefined;
    protected errorMessage: string | undefined;
    protected loading = false;
    protected refreshHandle: number | undefined;
    protected refreshSequence = 0;

    @postConstruct()
    protected init(): void {
        this.id = AthenaConnectionNavigatorWidget.ID;
        this.title.label = AthenaConnectionNavigatorWidget.LABEL;
        this.title.caption = AthenaConnectionNavigatorWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-circuit-board';
        this.addClass('athena-connection-navigator-widget');

        this.toDispose.push(this.editorListeners);
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(this.semanticSelectionService.onDidChangeSelection(() => this.update()));
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(widget => {
            this.bindEditor(widget);
            this.scheduleRefresh();
        }));
        this.toDispose.push(Disposable.create(() => {
            if (this.refreshHandle !== undefined) window.clearTimeout(this.refreshHandle);
        }));

        this.bindEditor(this.editorManager.currentEditor);
        this.scheduleRefresh();
    }

    protected bindEditor(widget: EditorWidget | undefined): void {
        this.editorListeners.dispose();
        this.editorListeners = new DisposableCollection();
        this.toDispose.push(this.editorListeners);
        if (this.isAthenaEditor(widget)) {
            this.editorListeners.push(widget.editor.onDocumentContentChanged(() => this.scheduleRefresh()));
        }
    }

    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }

    protected scheduleRefresh(): void {
        if (this.refreshHandle !== undefined) window.clearTimeout(this.refreshHandle);
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshConnectionReadModel();
        }, 120);
    }

    protected clearReadModel(): void {
        this.readyByAcceptedInputRevision.clear();
        this.readModel = undefined;
    }

    protected async refreshConnectionReadModel(): Promise<void> {
        const requestSequence = ++this.refreshSequence;
        const requestedRepositoryRoot = this.repositorySessionService.state.repositoryRoot;
        if (this.repositorySessionService.state.lifecycle !== 'ready' || !requestedRepositoryRoot) {
            this.clearReadModel();
            this.errorMessage = undefined;
            this.loading = false;
            this.update();
            return;
        }

        this.loading = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const response = await this.lspEditorBridgeService.requestConnectionReadModel();
            if (requestSequence !== this.refreshSequence ||
                requestedRepositoryRoot !== this.repositorySessionService.state.repositoryRoot) return;

            const acceptedRevision = response?.acceptedInputRevision;
            if (!response || response.state !== 'READY' || !acceptedRevision ||
                response.attemptedInputRevision !== acceptedRevision) {
                this.clearReadModel();
                this.errorMessage = response?.diagnostics[0]?.problem;
                return;
            }

            this.readyByAcceptedInputRevision.clear();
            this.readyByAcceptedInputRevision.set(acceptedRevision, response);
            this.readModel = this.readyByAcceptedInputRevision.get(acceptedRevision);
        } catch (error) {
            if (requestSequence !== this.refreshSequence ||
                requestedRepositoryRoot !== this.repositorySessionService.state.repositoryRoot) return;
            this.clearReadModel();
            this.errorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            if (requestSequence === this.refreshSequence &&
                requestedRepositoryRoot === this.repositorySessionService.state.repositoryRoot) {
                this.loading = false;
                this.update();
            }
        }
    }

    protected selectItem(item: AthenaConnectionReadModelItem): void {
        void this.semanticSelectionService.selectPublishedSourceTrace(item.sourceTrace, item.semanticId);
    }

    protected render(): React.ReactNode {
        const session = this.repositorySessionService.state;
        if (session.lifecycle !== 'ready') return this.renderEmpty(session.message);
        if (this.errorMessage) return this.renderEmpty(this.errorMessage, true);
        if (this.loading && !this.readModel) return this.renderEmpty('Loading connections.');
        const model = this.readModel;
        if (!model) return this.renderEmpty('No accepted connection model.');

        const connections = model.items.filter(item => item.itemKind === 'CONNECTION');
        const nets = model.items.filter(item => item.itemKind === 'NET');
        return <div className='athena-connection-navigator' data-accepted-input-revision={model.acceptedInputRevision}>
            {this.renderGroup('Connections', connections)}
            {this.renderGroup('Nets', nets)}
        </div>;
    }

    protected renderGroup(label: string, items: AthenaConnectionReadModelItem[]): React.ReactNode {
        return <section className='athena-connection-navigator__group'>
            <h3>{label}<span>{items.length}</span></h3>
            {items.length === 0
                ? <p className='athena-connection-navigator__none'>None</p>
                : <ul>{items.map(item => this.renderItem(item))}</ul>}
        </section>;
    }

    protected renderItem(item: AthenaConnectionReadModelItem): React.ReactNode {
        const selected = this.semanticSelectionService.selection?.semanticId === item.semanticId;
        const specifications = item.resolvedSpecifications.map(fact => `${fact.name} ${fact.value}`).join(' | ');
        return <li key={item.semanticId} className={selected ? 'athena-connection-navigator__item--selected' : undefined}>
            <button type='button' onClick={() => this.selectItem(item)}>
                <span className='athena-connection-navigator__item-title'>{item.displayName}</span>
                <span className='athena-connection-navigator__item-meta'>
                    {item.connectionKind} | {item.placed ? `${item.projectionCount} placed` : 'unplaced'} | {item.validation.state}
                </span>
                {item.potentialOrSignal && <span className='athena-connection-navigator__item-meta'>{item.potentialOrSignal}</span>}
                <span className='athena-connection-navigator__endpoints'>
                    {item.endpoints.map(endpoint => `${endpoint.role.toLowerCase()}: ${endpoint.authoredPath}`).join(' | ')}
                </span>
                {specifications && <span className='athena-connection-navigator__item-meta'>{specifications}</span>}
            </button>
        </li>;
    }

    protected renderEmpty(message: string, error = false): React.ReactNode {
        return <div className='athena-connection-navigator'>
            <section className={`athena-connection-navigator__empty ${error ? 'athena-connection-navigator__empty--error' : ''}`}>
                <p>{message}</p>
            </section>
        </div>;
    }
}
