import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import type { AthenaComponentKnowledgeSessionPayload } from './athena-component-knowledge-protocol';
import type { AthenaAuthoringPreviewPayload } from './athena-authoring-protocol';
import {
    buildAuthoringDecisionRequest,
    buildCreateComponentPreviewRequest
} from './athena-authoring-protocol';
import {
    AthenaComponentPanelGroup,
    AthenaComponentPanelItem,
    buildAthenaComponentPanelGroups
} from './athena-component-panel-model';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';

@injectable()
export class AthenaComponentPanelWidget extends ReactWidget {
    static readonly ID = 'athena.componentPanel';
    static readonly LABEL = 'Components';

    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    @inject(AthenaSemanticSelectionService)
    protected readonly semanticSelectionService: AthenaSemanticSelectionService;

    protected currentEditorListeners = new DisposableCollection();
    protected knowledge: AthenaComponentKnowledgeSessionPayload | undefined;
    protected groups: AthenaComponentPanelGroup[] = [];
    protected loading = false;
    protected errorMessage: string | undefined;
    protected previewMessage: string | undefined;
    protected activePreview: AthenaAuthoringPreviewPayload | undefined;
    protected previewingConceptId: string | undefined;
    protected applyingDecision = false;
    protected refreshHandle: number | undefined;

    @postConstruct()
    protected init(): void {
        this.id = AthenaComponentPanelWidget.ID;
        this.title.label = AthenaComponentPanelWidget.LABEL;
        this.title.caption = AthenaComponentPanelWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-package';
        this.addClass('athena-component-panel-widget');

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

    protected scheduleRefresh(): void {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshCatalog();
        }, 120);
    }

    protected async refreshCatalog(): Promise<void> {
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.knowledge = undefined;
            this.groups = [];
            this.activePreview = undefined;
            this.previewMessage = undefined;
            this.update();
            return;
        }

        this.loading = true;
        this.errorMessage = undefined;
        this.update();

        try {
            const knowledge = await this.lspEditorBridgeService.requestComponentKnowledgeSession();
            this.knowledge = knowledge;
            this.groups = buildAthenaComponentPanelGroups(knowledge?.availableComponents ?? []);
            if (knowledge?.status === 'unavailable') {
                this.errorMessage = knowledge.unavailableReason ?? 'Athena component knowledge is unavailable.';
            }
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.knowledge = undefined;
            this.groups = [];
        } finally {
            this.loading = false;
            this.update();
        }
    }

    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }

    protected canPreviewInsert(): boolean {
        return !!this.knowledge?.systemSemanticId && this.isAthenaEditor(this.editorManager.currentEditor);
    }

    protected async previewComponentInsertion(item: AthenaComponentPanelItem): Promise<void> {
        const knowledge = this.knowledge;
        if (!knowledge?.systemSemanticId) {
            this.previewMessage = 'Athena cannot preview insertion until the active system semantic identity is available.';
            this.update();
            return;
        }

        this.previewingConceptId = item.conceptId;
        this.previewMessage = undefined;
        this.update();

        try {
            const submission = await this.lspEditorBridgeService.requestAuthoringPreview(
                buildCreateComponentPreviewRequest({
                    systemSemanticId: knowledge.systemSemanticId,
                    conceptId: item.conceptId,
                    preferredImplementationId: item.preferredImplementation?.implementationId,
                    originDetail: `component-panel:${item.conceptId}`,
                }),
            );
            this.activePreview = submission?.preview;
            if (!submission?.preview) {
                this.previewMessage = 'Athena could not create a guided insertion preview for the selected component.';
            }
        } catch (error) {
            this.previewMessage = error instanceof Error ? error.message : String(error);
            this.activePreview = undefined;
        } finally {
            this.previewingConceptId = undefined;
            this.update();
        }
    }

    protected async acceptActivePreview(): Promise<void> {
        const preview = this.activePreview;
        if (!preview) {
            return;
        }
        this.applyingDecision = true;
        this.previewMessage = undefined;
        this.update();

        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'accepted',
                    note: 'Component panel placement accepted.',
                }),
            );
            if (!decision?.sourceEdit) {
                throw new Error('Athena accepted the preview but did not return a governed source edit.');
            }
            this.lspEditorBridgeService.applyAuthoringSourceEdit(decision.sourceEdit);
            if (decision.sourceEdit.suggestedSemanticId) {
                window.setTimeout(() => {
                    void this.semanticSelectionService.selectSemanticId(decision.sourceEdit!.suggestedSemanticId!).catch(error => {
                        this.previewMessage = error instanceof Error ? error.message : String(error);
                        this.update();
                    });
                }, 180);
            }
            this.activePreview = undefined;
            this.scheduleRefresh();
        } catch (error) {
            this.previewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.applyingDecision = false;
            this.update();
        }
    }

    protected async rejectActivePreview(): Promise<void> {
        const preview = this.activePreview;
        if (!preview) {
            return;
        }
        this.applyingDecision = true;
        this.previewMessage = undefined;
        this.update();

        try {
            await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'rejected',
                    note: 'Component panel placement rejected.',
                }),
            );
            this.activePreview = undefined;
        } catch (error) {
            this.previewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.applyingDecision = false;
            this.update();
        }
    }

    protected render(): React.ReactNode {
        const sessionState = this.repositorySessionService.state;

        if (sessionState.lifecycle !== 'ready') {
            return <div className='athena-component-panel'>
                <section className='athena-component-panel__empty'>
                    <h2>Components</h2>
                    <p>{sessionState.message}</p>
                </section>
            </div>;
        }

        if (this.errorMessage) {
            return <div className='athena-component-panel'>
                <section className='athena-component-panel__empty athena-component-panel__empty--error'>
                    <h2>Components</h2>
                    <p>{this.errorMessage}</p>
                </section>
            </div>;
        }

        if (this.loading && !this.knowledge) {
            return <div className='athena-component-panel'>
                <section className='athena-component-panel__empty'>
                    <h2>Components</h2>
                    <p>Loading governed component knowledge from the active Athena runtime session.</p>
                </section>
            </div>;
        }

        const knowledge = this.knowledge;
        if (!knowledge) {
            return <div className='athena-component-panel'>
                <section className='athena-component-panel__empty'>
                    <h2>Components</h2>
                    <p>No governed component catalog is available yet for the active repository.</p>
                </section>
            </div>;
        }

        return <div className='athena-component-panel'>
            <header className='athena-component-panel__header'>
                <div>
                    <div className='athena-component-panel__eyebrow'>Guided authoring foundation</div>
                    <h2>Available components</h2>
                    <p>{knowledge.projectName} | <code>{knowledge.semanticPath}</code></p>
                </div>
                <div className={`athena-component-panel__status athena-component-panel__status--${knowledge.status}`}>
                    {knowledge.status}
                </div>
            </header>

            <section className='athena-component-panel__summary'>
                <ul className='athena-component-panel__summary-list'>
                    <li><span>Concepts</span><strong>{knowledge.activeConceptCount}</strong></li>
                    <li><span>Implementations</span><strong>{knowledge.activeImplementationCount}</strong></li>
                    <li><span>Contributors</span><strong>{knowledge.contributingPluginIds.length}</strong></li>
                </ul>
            </section>

            <section className='athena-component-panel__section'>
                <h3>Scope</h3>
                <ul className='athena-component-panel__detail-list'>
                    <li><span>Proof slice</span><strong>Electrical / Siemens-first</strong></li>
                    <li><span>Source</span><strong>Active component knowledge packs</strong></li>
                    <li><span>Insertion</span><strong>{this.canPreviewInsert() ? 'Preview-first ready' : 'Open one .athena editor to insert'}</strong></li>
                </ul>
            </section>

            {this.activePreview || this.previewMessage
                ? <section className='athena-component-panel__section'>
                    <h3>Pending insertion</h3>
                    {this.previewMessage
                        ? <p>{this.previewMessage}</p>
                        : undefined}
                    {this.activePreview
                        ? <div className='athena-component-panel__preview'>
                            <div className='athena-component-panel__preview-header'>
                                <strong>{this.activePreview.title}</strong>
                                <span className={`athena-component-panel__status athena-component-panel__status--${this.activePreview.status}`}>
                                    {this.activePreview.status}
                                </span>
                            </div>
                            <ul className='athena-component-panel__list'>
                                {this.activePreview.changes.map(change => <li
                                    key={`${change.kind}:${change.title}`}
                                    className='athena-component-panel__item'
                                >
                                    <div className='athena-component-panel__item-header'>
                                        <span className='athena-component-panel__item-title'>{change.title}</span>
                                        <span className='athena-component-panel__pill'>{change.kind}</span>
                                    </div>
                                    {change.summary
                                        ? <p className='athena-component-panel__item-summary'>{change.summary}</p>
                                        : undefined}
                                </li>)}
                            </ul>
                            <div className='athena-component-panel__actions'>
                                <button
                                    className='athena-component-panel__action'
                                    type='button'
                                    disabled={this.applyingDecision}
                                    onClick={() => void this.acceptActivePreview()}
                                >
                                    {this.applyingDecision ? 'Applying...' : 'Accept'}
                                </button>
                                <button
                                    className='athena-component-panel__action athena-component-panel__action--secondary'
                                    type='button'
                                    disabled={this.applyingDecision}
                                    onClick={() => void this.rejectActivePreview()}
                                >
                                    Reject
                                </button>
                            </div>
                        </div>
                        : undefined}
                </section>
                : undefined}

            {this.groups.length === 0
                ? <section className='athena-component-panel__section'>
                    <h3>Available components</h3>
                    <p>No active component concepts are currently available from the hosted knowledge set.</p>
                </section>
                : this.groups.map(group => <section
                    key={group.categoryId}
                    className='athena-component-panel__section'
                >
                    <h3>{group.label}</h3>
                    <ul className='athena-component-panel__list'>
                        {group.items.map(item => this.renderItem(item))}
                    </ul>
                </section>)}
        </div>;
    }

    protected renderItem(item: AthenaComponentPanelItem): React.ReactNode {
        const preferred = item.preferredImplementation;
        const previewing = this.previewingConceptId === item.conceptId;
        return <li
            key={item.conceptId}
            className='athena-component-panel__item'
        >
            <div className='athena-component-panel__item-header'>
                <span className='athena-component-panel__item-title'>{item.displayName}</span>
                {preferred
                    ? <span className='athena-component-panel__pill'>{preferred.vendorId}</span>
                    : undefined}
            </div>
            <div className='athena-component-panel__item-meta'>
                <code>{item.conceptId}</code>
                {preferred
                    ? <span>{preferred.displayName} | <code>{preferred.vendorPartNumber}</code></span>
                    : <span>No implementation published yet.</span>}
            </div>
            {item.summary
                ? <p className='athena-component-panel__item-summary'>{item.summary}</p>
                : undefined}
            <div className='athena-component-panel__actions'>
                <button
                    className='athena-component-panel__action'
                    type='button'
                    disabled={!this.canPreviewInsert() || previewing || this.applyingDecision}
                    onClick={() => void this.previewComponentInsertion(item)}
                >
                    {previewing ? 'Previewing...' : 'Preview insert'}
                </button>
            </div>
        </li>;
    }
}
