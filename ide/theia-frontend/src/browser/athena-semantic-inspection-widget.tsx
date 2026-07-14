import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import {
    buildAuthoringDecisionRequest,
    buildUpdateComponentPropertiesPreviewRequest,
    AthenaAuthoringPreviewPayload,
} from './athena-authoring-protocol';
import type { AthenaComponentKnowledgeSessionPayload } from './athena-component-knowledge-protocol';
import {
    AthenaInspectorComponentSnapshot,
    AthenaInspectorEditDraft,
    buildAthenaInspectorComponentSnapshot,
    buildAthenaInspectorDraftChanges,
    createAthenaInspectorEditDraft,
} from './athena-inspector-model';
import {
    AthenaAiReasoningProposalPayload,
    AthenaAiReasoningStatePayload,
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
    protected componentKnowledge: AthenaComponentKnowledgeSessionPayload | undefined;
    protected reasoningState: AthenaAiReasoningStatePayload | undefined;
    protected errorMessage: string | undefined;
    protected reasoningErrorMessage: string | undefined;
    protected authoringMessage: string | undefined;
    protected authoringPreview: AthenaAuthoringPreviewPayload | undefined;
    protected inspectorDraft: AthenaInspectorEditDraft | undefined;
    protected loading = false;
    protected reasoningLoading = false;
    protected authoringPreviewing = false;
    protected authoringApplyingDecision = false;
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
            this.reasoningLoading = false;
            this.errorMessage = undefined;
            this.reasoningErrorMessage = undefined;
            this.authoringMessage = undefined;
            this.authoringPreview = undefined;
            this.inspectorDraft = undefined;
            this.inspection = undefined;
            this.componentKnowledge = undefined;
            this.reasoningState = undefined;
            this.update();
            return;
        }

        if (!this.isAthenaEditor(currentEditor)) {
            this.loading = false;
            this.reasoningLoading = false;
            this.errorMessage = undefined;
            this.reasoningErrorMessage = undefined;
            this.authoringMessage = undefined;
            this.authoringPreview = undefined;
            this.inspectorDraft = undefined;
            this.inspection = undefined;
            this.componentKnowledge = undefined;
            this.reasoningState = undefined;
            this.update();
            return;
        }

        const currentUri = currentEditor.editor.uri.toString();
        this.loading = true;
        this.reasoningLoading = true;
        this.errorMessage = undefined;
        this.reasoningErrorMessage = undefined;
        this.update();

        try {
            const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
            const componentKnowledge = await this.lspEditorBridgeService.requestComponentKnowledgeSession();
            const reasoningState = await this.lspEditorBridgeService.requestAiReasoningState();
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            this.inspection = inspection;
            this.componentKnowledge = componentKnowledge;
            this.reasoningState = reasoningState;
        } catch (error) {
            if (this.editorManager.currentEditor?.editor.uri.toString() !== currentUri) {
                return;
            }
            const message = error instanceof Error ? error.message : String(error);
            this.errorMessage = message;
            this.reasoningErrorMessage = message;
            this.authoringMessage = undefined;
            this.authoringPreview = undefined;
            this.inspectorDraft = undefined;
            this.inspection = undefined;
            this.componentKnowledge = undefined;
            this.reasoningState = undefined;
        } finally {
            if (this.editorManager.currentEditor?.editor.uri.toString() === currentUri) {
                this.loading = false;
                this.reasoningLoading = false;
                this.update();
            }
        }
    }

    protected async requestDiagnosticExplanation(): Promise<void> {
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor)) {
            return;
        }
        this.reasoningLoading = true;
        this.reasoningErrorMessage = undefined;
        this.update();
        try {
            await this.lspEditorBridgeService.requestAiReasoning({
                requestCategory: 'diagnostic-explanation',
                subjectSemanticIds: this.currentReasoningSubjectIds()
            });
            this.reasoningState = await this.lspEditorBridgeService.requestAiReasoningState();
        } catch (error) {
            this.reasoningErrorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.reasoningLoading = false;
            this.update();
        }
    }

    protected async applyReasoningDecision(
        proposalId: string,
        decision: 'accepted' | 'dismissed'
    ): Promise<void> {
        this.reasoningLoading = true;
        this.reasoningErrorMessage = undefined;
        this.update();
        try {
            await this.lspEditorBridgeService.requestAiReasoningDecision({
                proposalId,
                decision
            });
            this.reasoningState = await this.lspEditorBridgeService.requestAiReasoningState();
        } catch (error) {
            this.reasoningErrorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.reasoningLoading = false;
            this.update();
        }
    }

    protected currentReasoningSubjectIds(): string[] {
        const selectedSemanticId = this.semanticSelectionService.selection?.semanticId;
        if (selectedSemanticId) {
            return [selectedSemanticId];
        }
        return this.inspection?.components.slice(0, 1).map(component => component.semanticId) ?? [];
    }

    protected diagnosticProposals(): AthenaAiReasoningProposalPayload[] {
        const selectedSemanticId = this.semanticSelectionService.selection?.semanticId;
        return (this.reasoningState?.proposals ?? [])
            .filter(proposal => proposal.proposalCategory === 'diagnostic-explanation')
            .filter(proposal => !selectedSemanticId || proposal.subjectSemanticIds.includes(selectedSemanticId))
            .sort((left, right) => right.proposalId.localeCompare(left.proposalId));
    }

    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }

    protected currentSelectedComponentSnapshot(): AthenaInspectorComponentSnapshot | undefined {
        return buildAthenaInspectorComponentSnapshot({
            inspection: this.inspection,
            knowledge: this.componentKnowledge,
            selection: this.semanticSelectionService.selection,
        });
    }

    protected ensureInspectorDraft(
        snapshot: AthenaInspectorComponentSnapshot | undefined,
    ): AthenaInspectorEditDraft | undefined {
        if (!snapshot) {
            this.inspectorDraft = undefined;
            this.authoringPreview = undefined;
            return undefined;
        }
        if (!this.inspectorDraft || this.inspectorDraft.semanticId !== snapshot.semanticId) {
            this.inspectorDraft = createAthenaInspectorEditDraft(snapshot);
            this.authoringPreview = undefined;
            this.authoringMessage = undefined;
        }
        return this.inspectorDraft;
    }

    protected updateInspectorDraft(
        patch: Partial<Omit<AthenaInspectorEditDraft, 'semanticId'>>,
    ): void {
        if (!this.inspectorDraft) {
            return;
        }
        this.inspectorDraft = {
            ...this.inspectorDraft,
            ...patch,
        };
        this.authoringPreview = undefined;
        this.authoringMessage = undefined;
        this.update();
    }

    protected resetInspectorDraft(snapshot: AthenaInspectorComponentSnapshot): void {
        this.inspectorDraft = createAthenaInspectorEditDraft(snapshot);
        this.authoringPreview = undefined;
        this.authoringMessage = undefined;
        this.update();
    }

    protected async previewInspectorUpdate(
        snapshot: AthenaInspectorComponentSnapshot,
    ): Promise<void> {
        const draft = this.ensureInspectorDraft(snapshot);
        if (!draft) {
            return;
        }
        const changes = buildAthenaInspectorDraftChanges({
            snapshot,
            draft,
        });
        if (Object.keys(changes).length === 0) {
            this.authoringPreview = undefined;
            this.authoringMessage = 'No governed property changes are pending for the selected component.';
            this.update();
            return;
        }

        this.authoringPreviewing = true;
        this.authoringMessage = undefined;
        this.update();
        try {
            const submission = await this.lspEditorBridgeService.requestAuthoringPreview(
                buildUpdateComponentPropertiesPreviewRequest({
                    componentId: snapshot.semanticId,
                    name: changes.name,
                    label: changes.label,
                    description: changes.description,
                    preferredImplementationId: changes.preferredImplementationId,
                    originDetail: `semantic-inspection:${snapshot.semanticId}`,
                }),
            );
            this.authoringPreview = submission?.preview;
            if (!submission?.preview) {
                this.authoringMessage = 'Athena could not create a governed inspector update preview for the selected component.';
            }
        } catch (error) {
            this.authoringMessage = error instanceof Error ? error.message : String(error);
            this.authoringPreview = undefined;
        } finally {
            this.authoringPreviewing = false;
            this.update();
        }
    }

    protected async acceptAuthoringPreview(): Promise<void> {
        const preview = this.authoringPreview;
        if (!preview) {
            return;
        }
        this.authoringApplyingDecision = true;
        this.authoringMessage = undefined;
        this.update();

        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'accepted',
                    note: 'Semantic inspection update accepted.',
                }),
            );
            if (!decision?.sourceEdit) {
                throw new Error('Athena accepted the inspector preview but did not return a governed source edit.');
            }
            this.lspEditorBridgeService.applyAuthoringSourceEdit(decision.sourceEdit);
            if (decision.sourceEdit.suggestedSemanticId) {
                window.setTimeout(() => {
                    void this.semanticSelectionService.selectSemanticId(decision.sourceEdit!.suggestedSemanticId!).catch(error => {
                        this.authoringMessage = error instanceof Error ? error.message : String(error);
                        this.update();
                    });
                }, 180);
            }
            this.authoringPreview = undefined;
            this.scheduleRefresh();
        } catch (error) {
            this.authoringMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.authoringApplyingDecision = false;
            this.update();
        }
    }

    protected async rejectAuthoringPreview(): Promise<void> {
        const preview = this.authoringPreview;
        if (!preview) {
            return;
        }
        this.authoringApplyingDecision = true;
        this.authoringMessage = undefined;
        this.update();

        try {
            await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'rejected',
                    note: 'Semantic inspection update rejected.',
                }),
            );
            this.authoringPreview = undefined;
        } catch (error) {
            this.authoringMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.authoringApplyingDecision = false;
            this.update();
        }
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
        const selectedComponentSnapshot = this.currentSelectedComponentSnapshot();
        const inspectorDraft = this.ensureInspectorDraft(selectedComponentSnapshot);
        const draftChanges = selectedComponentSnapshot && inspectorDraft
            ? buildAthenaInspectorDraftChanges({
                snapshot: selectedComponentSnapshot,
                draft: inspectorDraft,
            })
            : {};
        const hasDraftChanges = Object.keys(draftChanges).length > 0;

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
                <h3>Selected component</h3>
                {!selectedComponentSnapshot
                    ? <p>Select one component or one of its ports to inspect governed concept, implementation, port, and physical-trait details.</p>
                    : <>
                        <div className='athena-semantic-inspection__selection'>
                            <strong>{selectedComponentSnapshot.name}</strong><br />
                            <code>{selectedComponentSnapshot.semanticId}</code>
                        </div>
                        {inspectorDraft
                            ? <>
                                <h4 className='athena-semantic-inspection__subheading'>Editable properties</h4>
                                <div className='athena-semantic-inspection__control-grid'>
                                    <div className='athena-semantic-inspection__control'>
                                        <label htmlFor='athena-inspector-name'>Name</label>
                                        <input
                                            id='athena-inspector-name'
                                            type='text'
                                            value={inspectorDraft.name}
                                            onChange={event => this.updateInspectorDraft({ name: event.target.value })}
                                        />
                                    </div>
                                    <div className='athena-semantic-inspection__control'>
                                        <label htmlFor='athena-inspector-label'>Label</label>
                                        <input
                                            id='athena-inspector-label'
                                            type='text'
                                            value={inspectorDraft.label}
                                            onChange={event => this.updateInspectorDraft({ label: event.target.value })}
                                        />
                                    </div>
                                    <div className='athena-semantic-inspection__control athena-semantic-inspection__control--wide'>
                                        <label htmlFor='athena-inspector-description'>Description</label>
                                        <textarea
                                            id='athena-inspector-description'
                                            value={inspectorDraft.description}
                                            onChange={event => this.updateInspectorDraft({ description: event.target.value })}
                                        />
                                    </div>
                                    <div className='athena-semantic-inspection__control athena-semantic-inspection__control--wide'>
                                        <label htmlFor='athena-inspector-implementation'>Implementation</label>
                                        <select
                                            id='athena-inspector-implementation'
                                            value={inspectorDraft.preferredImplementationId ?? ''}
                                            onChange={event => this.updateInspectorDraft({
                                                preferredImplementationId: event.target.value || undefined,
                                            })}
                                            disabled={selectedComponentSnapshot.implementationOptions.length === 0}
                                        >
                                            {selectedComponentSnapshot.implementationOptions.length === 0
                                                ? <option value=''>No governed implementation choices</option>
                                                : selectedComponentSnapshot.implementationOptions.map(option => <option
                                                    key={option.implementationId}
                                                    value={option.implementationId}
                                                >
                                                    {option.displayName} | {option.vendorId} | {option.vendorPartNumber}
                                                </option>)}
                                        </select>
                                        {selectedComponentSnapshot.implementationOptions.length > 0
                                            ? <div className='athena-semantic-inspection__hint'>
                                                Governed implementation choices come from the active component knowledge catalog.
                                            </div>
                                            : undefined}
                                    </div>
                                </div>
                                <div className='athena-semantic-inspection__actions'>
                                    <button
                                        className='athena-semantic-inspection__action'
                                        type='button'
                                        disabled={!hasDraftChanges || this.authoringPreviewing || this.authoringApplyingDecision}
                                        onClick={() => void this.previewInspectorUpdate(selectedComponentSnapshot)}
                                    >
                                        {this.authoringPreviewing ? 'Previewing...' : 'Preview update'}
                                    </button>
                                    <button
                                        className='athena-semantic-inspection__action athena-semantic-inspection__action--secondary'
                                        type='button'
                                        disabled={this.authoringPreviewing || this.authoringApplyingDecision}
                                        onClick={() => this.resetInspectorDraft(selectedComponentSnapshot)}
                                    >
                                        Reset
                                    </button>
                                </div>
                                {this.authoringPreview || this.authoringMessage
                                    ? <div className='athena-semantic-inspection__preview'>
                                        <div className='athena-semantic-inspection__preview-header'>
                                            <strong>{this.authoringPreview?.title ?? 'Pending update'}</strong>
                                            {this.authoringPreview
                                                ? <span className={`athena-semantic-inspection__status athena-semantic-inspection__status--${this.authoringPreview.status}`}>
                                                    {this.authoringPreview.status}
                                                </span>
                                                : undefined}
                                        </div>
                                        {this.authoringMessage
                                            ? <p>{this.authoringMessage}</p>
                                            : undefined}
                                        {this.authoringPreview
                                            ? <>
                                                <ul className='athena-semantic-inspection__list'>
                                                    {this.authoringPreview.changes.map(change => <li
                                                        key={`${change.kind}:${change.title}`}
                                                        className='athena-semantic-inspection__item'
                                                    >
                                                        <div className='athena-semantic-inspection__item-title'>{change.title}</div>
                                                        {change.summary
                                                            ? <div className='athena-semantic-inspection__item-meta'>{change.summary}</div>
                                                            : undefined}
                                                    </li>)}
                                                </ul>
                                                <div className='athena-semantic-inspection__actions'>
                                                    <button
                                                        className='athena-semantic-inspection__action'
                                                        type='button'
                                                        disabled={this.authoringApplyingDecision}
                                                        onClick={() => void this.acceptAuthoringPreview()}
                                                    >
                                                        {this.authoringApplyingDecision ? 'Applying...' : 'Accept'}
                                                    </button>
                                                    <button
                                                        className='athena-semantic-inspection__action athena-semantic-inspection__action--secondary'
                                                        type='button'
                                                        disabled={this.authoringApplyingDecision}
                                                        onClick={() => void this.rejectAuthoringPreview()}
                                                    >
                                                        Reject
                                                    </button>
                                                </div>
                                            </>
                                            : undefined}
                                    </div>
                                    : undefined}
                            </>
                            : undefined}
                        <ul className='athena-semantic-inspection__detail-list'>
                            <li><span>Kind</span><strong>{selectedComponentSnapshot.kind}</strong></li>
                            <li><span>Concept</span><strong>{selectedComponentSnapshot.conceptDisplayName} <code>{selectedComponentSnapshot.conceptId}</code></strong></li>
                            <li><span>Reference</span><strong><code>{selectedComponentSnapshot.authoredComponentReference}</code></strong></li>
                            <li><span>Implementation</span><strong>{selectedComponentSnapshot.vendorPartNumber ? `${selectedComponentSnapshot.vendorId ?? 'vendor'} / ${selectedComponentSnapshot.vendorPartNumber}` : 'Concept only'}</strong></li>
                            <li><span>Knowledge source</span><strong>Active component knowledge session</strong></li>
                        </ul>
                        <h4 className='athena-semantic-inspection__subheading'>Semantic ports</h4>
                        {selectedComponentSnapshot.ports.length === 0
                            ? <p>No governed semantic ports were resolved for this selected component.</p>
                            : <ul className='athena-semantic-inspection__list'>
                                {selectedComponentSnapshot.ports.map(port => <li
                                    key={port.semanticId}
                                    className={`athena-semantic-inspection__item ${port.selected ? 'athena-semantic-inspection__item--selected' : ''}`}
                                >
                                    <button
                                        className='athena-semantic-inspection__selectable'
                                        type='button'
                                        onClick={() => void this.semanticSelectionService.selectSemanticId(port.semanticId)}
                                    >
                                        <span className='athena-semantic-inspection__item-title'>{port.label}</span>
                                        <span className='athena-semantic-inspection__item-meta'>
                                            {port.direction} | {port.signalFamilyId} | {port.roleId}
                                            {port.connectedPaths.length > 0 ? ` | connected to ${port.connectedPaths.join(', ')}` : ''}
                                        </span>
                                    </button>
                                </li>)}
                            </ul>}
                        <h4 className='athena-semantic-inspection__subheading'>Physical traits</h4>
                        {selectedComponentSnapshot.physicalTraits.length === 0
                            ? <p>No governed physical traits were resolved for this selected component.</p>
                            : <ul className='athena-semantic-inspection__dense-list'>
                                {selectedComponentSnapshot.physicalTraits.map(trait => <li key={`${selectedComponentSnapshot.semanticId}:${trait.displayName}`}>
                                    <strong>{trait.displayName}</strong> {trait.widthMillimeters}x{trait.heightMillimeters}x{trait.depthMillimeters} mm | {trait.mountingTypeId}
                                </li>)}
                            </ul>}
                    </>}
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
                <h3>AI diagnostic explanation</h3>
                <div className='athena-ai-reasoning__actions'>
                    <button
                        className='athena-ai-reasoning__action'
                        type='button'
                        onClick={() => void this.requestDiagnosticExplanation()}
                        disabled={this.reasoningLoading || inspection.diagnosticsCount === 0}
                    >
                        {this.reasoningLoading ? 'Explaining...' : 'Explain diagnostic'}
                    </button>
                </div>
                {this.reasoningErrorMessage
                    ? <p>{this.reasoningErrorMessage}</p>
                    : undefined}
                {this.diagnosticProposals().length === 0
                    ? <p>No diagnostic explanation proposal has been recorded yet for the current focus.</p>
                    : <ul className='athena-ai-reasoning__proposal-list'>
                        {this.diagnosticProposals().map(proposal => this.renderReasoningProposal(proposal))}
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

    protected renderReasoningProposal(proposal: AthenaAiReasoningProposalPayload): React.ReactNode {
        return <li
            key={proposal.proposalId}
            className='athena-ai-reasoning__proposal'
        >
            <div className='athena-ai-reasoning__proposal-header'>
                <strong>{proposal.summary}</strong>
                <span className={`athena-ai-reasoning__status athena-ai-reasoning__status--${proposal.decisionState}`}>
                    {proposal.decisionState}
                </span>
            </div>
            <div className='athena-ai-reasoning__meta'>
                <span>{proposal.providerStatus}</span>
                {proposal.providerId ? <span>{proposal.providerId}</span> : undefined}
                <code>{proposal.proposalId}</code>
            </div>
            <p>{proposal.response}</p>
            <ul className='athena-ai-reasoning__evidence-list'>
                {proposal.evidence.map(evidence => <li key={`${proposal.proposalId}:${evidence.referenceId}`}>
                    <strong>{evidence.kind}</strong> <code>{evidence.referenceId}</code> {evidence.summary}
                </li>)}
            </ul>
            {proposal.decisionState === 'unresolved'
                ? <div className='athena-ai-reasoning__actions'>
                    <button
                        className='athena-ai-reasoning__action'
                        type='button'
                        onClick={() => void this.applyReasoningDecision(proposal.proposalId, 'accepted')}
                    >
                        Accept
                    </button>
                    <button
                        className='athena-ai-reasoning__action athena-ai-reasoning__action--secondary'
                        type='button'
                        onClick={() => void this.applyReasoningDecision(proposal.proposalId, 'dismissed')}
                    >
                        Dismiss
                    </button>
                </div>
                : undefined}
        </li>;
    }
}
