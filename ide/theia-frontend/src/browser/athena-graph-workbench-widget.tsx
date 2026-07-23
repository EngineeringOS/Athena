import * as React from '@theia/core/shared/react';
import * as ReactDOM from '@theia/core/shared/react-dom';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { Message } from '@lumino/messaging';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import type { AthenaAuthoringPreviewPayload } from './athena-authoring-protocol';
import {
    buildAthenaGraphLayoutMutationPreview,
    buildAthenaGraphAuthoredLayoutIntent,
    captureAthenaGraphLayoutAdjustmentIntent,
    keepAthenaGraphViewportFocusedOnSelection,
    type AthenaGraphLayoutAdjustmentIntent,
    type AthenaGraphLayoutMutationPreview,
    type AthenaGraphWorkbenchModel,
    type AthenaGraphWorkbenchEdge,
    type AthenaGraphWorkbenchNode,
    type AthenaGraphWorkbenchSheetViewSelector,
    type AthenaGraphViewportSize,
    type AthenaGraphViewportTransform,
    buildAthenaGraphDocumentReferenceInspection,
    buildAthenaGraphRepresentationInspection,
    resolveAthenaGraphReferenceMarkerNavigation,
    resolveVisibleAthenaGraphSheetViewSelector,
    buildAthenaGraphRouteInspection,
    buildAthenaGraphWorkbenchModel,
    clampAthenaGraphZoom,
    fitAthenaGraphViewport,
    panAthenaGraphViewport,
    resizeAthenaGraphViewport,
    zoomAthenaGraphViewportAtPoint
} from './athena-graph-workbench-model';
import {
    buildAuthoringDecisionRequest,
    buildCreateEntityPreviewRequest,
    buildSemanticRelationshipPreviewRequest,
    collectAuthoringDecisionDiagnostics,
    isAuthoringDecisionCommitted,
    sourceEditMatchesPreviewEvidence
} from './athena-authoring-protocol';
import type { AthenaComponentKnowledgeSessionPayload } from './athena-component-knowledge-protocol';
import {
    AthenaComponentPanelItem,
    buildAthenaComponentPanelGroups
} from './athena-component-panel-model';
import { AthenaGraphAdapterService } from './athena-graph-adapter-service';
import { type AthenaGraphCommandIntentPayload } from './athena-graph-command-intent-protocol';
import { AthenaGraphWorkbenchEdgeLayer } from './athena-graph-workbench-edge-layer';
import { AthenaGraphWorkbenchPresentationNode } from './athena-graph-workbench-presentation-node';
import {
    AthenaLspEditorBridgeService,
    AthenaSemanticInspectionPayload
} from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import {
    graphContainsSemanticId,
    resolveProjectionEndpointAlias,
    resolveProjectionCrossReference,
    resolveProjectionOccurrence,
    resolveProjectionRelatedSubjects,
    retainSelectionIfPresent,
    type AthenaActiveSemanticSelection
} from './athena-semantic-selection-model';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';
import { AthenaGraphNodeDragState, AthenaGraphPanState, AthenaGraphPortConnectSource } from './athena-graph-workbench-types';

type AthenaGraphCreateEntityDraft = {
    conceptId: string;
    conceptTemplateId: string;
    suggestedName: string;
    model: string;
};

type AthenaGraphRelationshipCandidateEvidence = {
    authority: 'semantic-inspection-compatibility';
    compatibility: 'candidate' | 'rejected';
    sourceSemanticId: string;
    targetSemanticId: string;
    reason: string;
};

/** Graph-first Athena workbench surface with a pannable and zoomable renderer viewport. */
@injectable()
export class AthenaGraphWorkbenchWidget extends ReactWidget {
    static readonly ID = 'athena.graphWorkbench';
    static readonly LABEL = 'Graphical View';
    protected static readonly BOTTOM_DOCK_AUTO_FIT_RESERVE = 52;

    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaGraphAdapterService)
    protected readonly graphAdapterService: AthenaGraphAdapterService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    @inject(AthenaSemanticSelectionService)
    protected readonly semanticSelectionService: AthenaSemanticSelectionService;

    protected currentEditorListeners = new DisposableCollection();
    protected diagram = undefined as Awaited<ReturnType<AthenaGraphAdapterService['requestDiagram']>>;
    protected componentKnowledge = undefined as AthenaComponentKnowledgeSessionPayload | undefined;
    protected semanticInspection = undefined as AthenaSemanticInspectionPayload | undefined;
    protected errorMessage: string | undefined;
    protected loading = false;
    protected switchingView = false;
    protected refreshHandle: number | undefined;
    protected viewportElement: HTMLDivElement | undefined;
    protected viewportObserver: ResizeObserver | undefined;
    protected viewportSize: AthenaGraphViewportSize = { width: 0, height: 0 };
    protected viewportTransform: AthenaGraphViewportTransform = { zoom: 1, offsetX: 0, offsetY: 0 };
    protected viewportMode: 'auto-fit' | 'manual' = 'auto-fit';
    protected panState: AthenaGraphPanState | undefined;
    protected dragState: AthenaGraphNodeDragState | undefined;
    protected pendingAutoFit = true;
    protected lastDiagramViewportKey: string | undefined;
    protected lastGraphCommandIntent = undefined as AthenaGraphCommandIntentPayload | undefined;
    protected lastLayoutAdjustmentIntent = undefined as AthenaGraphLayoutAdjustmentIntent | undefined;
    protected layoutMutationPreview = undefined as AthenaGraphLayoutMutationPreview | undefined;
    protected connectPortsArmed = false;
    protected connectPortsSource: AthenaGraphPortConnectSource | undefined;
    protected connectPortsPending = false;
    protected connectPreview = undefined as AthenaAuthoringPreviewPayload | undefined;
    protected connectPreviewMessage: string | undefined;
    protected connectApplyingDecision = false;
    protected connectPreviewRequestToken = 0;
    protected createEntityControlsOpen = false;
    protected createEntityDraft = undefined as AthenaGraphCreateEntityDraft | undefined;
    protected createEntityPreview = undefined as AthenaAuthoringPreviewPayload | undefined;
    protected createEntityPreviewMessage: string | undefined;
    protected createEntityPreviewing = false;
    protected createEntityApplyingDecision = false;
    protected createEntityPreviewRequestToken = 0;
    protected revealingSelectionSemanticId: string | undefined;
    protected infoPopoverOpen = false;
    protected lastDocumentSheetViewSelector: AthenaGraphWorkbenchSheetViewSelector | undefined;

    @postConstruct()
    protected init(): void {
        this.id = AthenaGraphWorkbenchWidget.ID;
        this.title.label = AthenaGraphWorkbenchWidget.LABEL;
        this.title.caption = AthenaGraphWorkbenchWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-type-hierarchy-sub';
        this.node.tabIndex = -1;
        this.addClass('athena-graph-workbench-widget');

        this.toDispose.push(this.currentEditorListeners);
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(this.semanticSelectionService.onDidChangeSelection(selection => {
            void this.handleSemanticSelectionChanged(selection);
        }));
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(widget => {
            this.clearCreateEntityPreview('Active Athena editor changed after create-entity preview. Request a fresh preview before accepting.');
            void this.cancelStaleConnectPreview('Active Athena editor changed after semantic relationship preview. Request a fresh preview before accepting.');
            this.bindCurrentEditor(widget);
            this.scheduleRefresh();
        }));
        this.toDispose.push(Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
            }
            this.viewportObserver?.disconnect();
            this.viewportObserver = undefined;
            this.viewportElement = undefined;
            this.panState = undefined;
            this.dragState = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.connectPortsPending = false;
            this.connectPreview = undefined;
            this.connectPreviewMessage = undefined;
            this.connectApplyingDecision = false;
            this.connectPreviewRequestToken++;
            this.createEntityControlsOpen = false;
            this.createEntityDraft = undefined;
            this.createEntityPreview = undefined;
            this.createEntityPreviewMessage = undefined;
            this.createEntityPreviewing = false;
            this.createEntityApplyingDecision = false;
            this.revealingSelectionSemanticId = undefined;
            this.infoPopoverOpen = false;
        }));

        this.bindCurrentEditor(this.editorManager.currentEditor);
        this.scheduleRefresh();
    }

    protected override onActivateRequest(msg: Message): void {
        super.onActivateRequest(msg);
        this.node.focus();
    }

    protected bindCurrentEditor(widget: EditorWidget | undefined): void {
        this.currentEditorListeners.dispose();
        this.currentEditorListeners = new DisposableCollection();
        this.toDispose.push(this.currentEditorListeners);

        if (!this.isAthenaEditor(widget)) {
            return;
        }

        this.currentEditorListeners.push(widget.editor.onDocumentContentChanged(() => {
            this.clearCreateEntityPreview('Athena source changed after create-entity preview. Request a fresh preview before accepting.');
            void this.cancelStaleConnectPreview('Athena source changed after semantic relationship preview. Request a fresh preview before accepting.');
            this.scheduleRefresh();
        }));
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
            void this.refreshDiagram();
        }, 120);
    }

    protected async refreshDiagram(): Promise<void> {
        const sessionState = this.repositorySessionService.state;
        const currentRepositoryRoot = sessionState.repositoryRoot;

        if (sessionState.lifecycle !== 'ready') {
            this.loading = false;
            this.errorMessage = undefined;
            this.diagram = undefined;
            this.panState = undefined;
            this.lastDocumentSheetViewSelector = undefined;
            this.update();
            return;
        }

        this.loading = true;
        this.errorMessage = undefined;
        this.update();

        try {
            const currentEditor = this.isAthenaEditor(this.editorManager.currentEditor) ? this.editorManager.currentEditor : undefined;
            const [diagram, componentKnowledge, semanticInspection] = await Promise.all([
                this.graphAdapterService.requestDiagram(),
                this.lspEditorBridgeService.requestComponentKnowledgeSession(),
                currentEditor ? this.lspEditorBridgeService.requestSemanticInspection(currentEditor) : Promise.resolve(undefined),
            ]);
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            const nextDiagramViewportKey = this.diagramViewportKey(diagram);
            const shouldAutoFit = this.lastDiagramViewportKey !== nextDiagramViewportKey;
            this.diagram = diagram;
            this.componentKnowledge = componentKnowledge;
            this.semanticInspection = semanticInspection;
            this.lastGraphCommandIntent = undefined;
            this.dragState = undefined;
            if (!this.connectApplyingDecision) {
                await this.cancelStaleConnectPreview('Diagram refreshed after semantic relationship preview. Request a fresh preview before accepting.');
            }
            this.createEntityPreviewing = false;
            if (!this.createEntityApplyingDecision) {
                this.clearCreateEntityPreview('Diagram refreshed after create-entity preview. Request a fresh preview before accepting.');
            }
            this.infoPopoverOpen = false;
            this.reconcileTransientSelection(diagram);
            void this.handleSemanticSelectionChanged(this.semanticSelectionService.selection, diagram);
            this.lastDiagramViewportKey = nextDiagramViewportKey;
            if (shouldAutoFit) {
                this.pendingAutoFit = true;
                this.viewportMode = 'auto-fit';
                this.fitViewportToDiagramIfPossible();
            } else {
                this.pendingAutoFit = false;
                this.viewportMode = 'manual';
                this.keepSelectionVisible(diagram);
            }
        } catch (error) {
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.diagram = undefined;
            this.componentKnowledge = undefined;
            this.semanticInspection = undefined;
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
            return <div className='athena-graph-workbench'>
                <section className='athena-graph-workbench__empty'>
                    <h2>Graphical View</h2>
                    <p>{sessionState.message}</p>
                </section>
            </div>;
        }

        if (this.errorMessage) {
            return <div className='athena-graph-workbench'>
                <section className='athena-graph-workbench__empty athena-graph-workbench__empty--error'>
                    <h2>Graphical View</h2>
                    <p>{this.errorMessage}</p>
                </section>
            </div>;
        }

        if (this.loading && !this.diagram) {
            return <div className='athena-graph-workbench'>
                <section className='athena-graph-workbench__empty'>
                    <h2>Graphical View</h2>
                    <p>Loading the runtime-owned graphical projection through the Athena adapter boundary.</p>
                </section>
            </div>;
        }

        if (!this.diagram) {
            return <div className='athena-graph-workbench'>
                <section className='athena-graph-workbench__empty'>
                    <h2>Graphical View</h2>
                    <p>No graphical projection payload is available yet for the active Athena session.</p>
                </section>
            </div>;
        }

        const model = buildAthenaGraphWorkbenchModel(this.diagram);
        const selectedSemantic = this.semanticSelectionService.selection;
        const selectedSemanticId = selectedSemantic?.semanticId;
        const selectionResolution = selectedSemanticId
            ? resolveProjectionOccurrence(this.diagram, selectedSemanticId)
            : undefined;
        const endpointAliasResolution = selectedSemanticId
            ? resolveProjectionEndpointAlias(this.diagram, selectedSemanticId)
            : undefined;
        const crossReference = selectedSemanticId
            ? resolveProjectionCrossReference(this.diagram, selectedSemanticId)
            : undefined;
        const relatedSubjects = selectedSemanticId
            ? resolveProjectionRelatedSubjects(this.diagram, selectedSemanticId)
            : [];
        const canRevealDocumentationReferences = !!crossReference &&
            this.diagram.activeViewId !== 'documentation' &&
            this.diagram.supportedViews.some(view => view.viewId === 'documentation');
        const connectPortsSupported = this.graphAdapterService.supportsCreateSemanticRelationshipIntent(this.diagram);
        const renderViewportTransform = this.resolveRenderViewportTransform(model);
        const zoomPercent = Math.round(renderViewportTransform.zoom * 100);
        const stageStyle = this.buildStageStyle(model);
        this.rememberDocumentSheetViewSelector(model);
        const projectionInfoRows = this.buildProjectionInfoRows(
            model,
            selectedSemantic,
            selectedSemanticId,
            selectionResolution,
            endpointAliasResolution,
            crossReference,
            relatedSubjects,
        );
        const sheetSurfaceStyle: React.CSSProperties = {
            width: `${model.canvas.width}px`,
            height: `${model.canvas.height}px`,
            transform: `translate(${renderViewportTransform.offsetX + (model.sceneBounds.minX * renderViewportTransform.zoom)}px, ${renderViewportTransform.offsetY + (model.sceneBounds.minY * renderViewportTransform.zoom)}px) scale(${renderViewportTransform.zoom})`,
            transformOrigin: '0 0',
        };
        const createEntityControls = this.createEntityControlsOpen && typeof document !== 'undefined'
            ? ReactDOM.createPortal(this.renderCreateEntityControls(), document.body)
            : undefined;

        const stageClassName = [
            'athena-graph-workbench__stage',
            this.panState ? 'athena-graph-workbench__stage--panning' : '',
            model.isElectricalFamily ? 'athena-graph-workbench__stage--electrical' : '',
        ].filter(Boolean).join(' ');

        return <div className='athena-graph-workbench' onClick={this.handleWorkbenchClick}>
            <div className='athena-graph-workbench__workspace'>
                <section
                    className={stageClassName}
                    style={stageStyle}
                >
                    {model.emptyState
                        ? (<div className='athena-graph-workbench__empty athena-graph-workbench__empty--inline'>
                            <h4>{model.emptyState.title}</h4>
                            <p>{model.emptyState.message}</p>
                        </div>)
                        : <>
                            {this.renderStageChrome(model, connectPortsSupported, projectionInfoRows)}
                            <div
                                className='athena-graph-workbench__viewport'
                                ref={this.bindViewportElement}
                                onClick={this.handleViewportClick}
                                onDoubleClick={this.handleViewportDoubleClick}
                                onWheel={this.handleViewportWheel}
                                onPointerDown={this.handleViewportPointerDown}
                                onPointerMove={this.handleViewportPointerMove}
                                onPointerUp={this.handleViewportPointerEnd}
                                onPointerCancel={this.handleViewportPointerEnd}
                            >
                                <div className='athena-graph-workbench__sheet' style={sheetSurfaceStyle}>
                                    {this.renderSheetChrome(model)}
                                    <svg
                                        className='athena-graph-workbench__canvas'
                                        viewBox={model.svgViewBox}
                                        role='img'
                                        aria-label='Athena graphical projection'
                                    >
                                        <AthenaGraphWorkbenchEdgeLayer
                                            edges={model.edges}
                                            selectedSemanticId={selectedSemanticId}
                                            onSelectSemanticId={semanticId => this.semanticSelectionService.selectSemanticId(semanticId)}
                                        />
                                        {model.nodes.map(node => this.renderGraphNode(node, selectedSemanticId))}
                                    </svg>
                                </div>
                            </div>
                            {this.renderLayoutMutationPreview()}
                            {this.renderBottomDock(model, zoomPercent)}
                        </>
                    }
                </section>
                {createEntityControls}
            </div>
        </div>;
    }

    protected resolveRenderViewportTransform(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
    ): AthenaGraphViewportTransform {
        if (this.viewportMode !== 'auto-fit' || this.viewportSize.width <= 0 || this.viewportSize.height <= 0 || model.emptyState) {
            return this.viewportTransform;
        }
        return fitAthenaGraphViewport(model.sceneBounds, this.resolveAutoFitViewportSize());
    }

    protected resolveAutoFitViewportSize(): AthenaGraphViewportSize {
        return {
            width: this.viewportSize.width,
            height: Math.max(1, this.viewportSize.height - AthenaGraphWorkbenchWidget.BOTTOM_DOCK_AUTO_FIT_RESERVE),
        };
    }

    protected buildProjectionInfoRows(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
        selectedSemantic: AthenaActiveSemanticSelection | undefined,
        selectedSemanticId: string | undefined,
        selectionResolution: ReturnType<typeof resolveProjectionOccurrence> | undefined,
        endpointAliasResolution: ReturnType<typeof resolveProjectionEndpointAlias> | undefined,
        crossReference: ReturnType<typeof resolveProjectionCrossReference>,
        relatedSubjects: ReturnType<typeof resolveProjectionRelatedSubjects>,
    ): Array<{ key: string; label: string; value: React.ReactNode; code?: boolean }> {
        const selectionLabel = selectedSemantic?.label ?? selectedSemanticId ?? 'No selection';
        const sheetTitle = model.sheetChrome.titleBlock?.displayName ?? 'No active sheet';
        const sheetId = model.sheetChrome.titleBlock?.sheetId ?? '-';
        const occurrenceSummary = selectionResolution
            ? `${selectionResolution.status}${selectionResolution.occurrenceIds.length > 0 ? `: ${selectionResolution.occurrenceIds.join(', ')}` : ''}`
            : '-';
        const endpointSummary = endpointAliasResolution
            ? `${endpointAliasResolution.status}${endpointAliasResolution.endpointIds.length > 0 ? `: ${endpointAliasResolution.endpointIds.join(', ')}` : ''}`
            : '-';
        const crossReferenceSummary = crossReference
            ? `${crossReference.kind}: ${crossReference.sheetIds.join(', ')}`
            : '-';
        const relatedSummary = relatedSubjects.length > 0
            ? relatedSubjects.map(item => `${item.relation}:${item.relatedSemanticId}`).join(', ')
            : '-';
        const sourceSummary = selectedSemantic?.sourceUri ?? '-';
        const routeInspection = selectedSemanticId
            ? buildAthenaGraphRouteInspection(model, selectedSemanticId)
            : undefined;
        const representationInspection = selectedSemanticId
            ? buildAthenaGraphRepresentationInspection(model, selectedSemanticId)
            : undefined;
        const documentReferenceInspection = selectedSemanticId
            ? buildAthenaGraphDocumentReferenceInspection(model, selectedSemanticId)
            : undefined;
        const routeRows = routeInspection?.status === 'ready'
            ? [
                { key: 'route-quality', label: 'Route quality', value: routeInspection.routeQuality },
                {
                    key: 'route-ports',
                    label: 'Route ports',
                    value: `${routeInspection.sourcePortSemanticId ?? '-'} -> ${routeInspection.targetPortSemanticId ?? '-'}`,
                    code: true,
                },
                { key: 'route-policy', label: 'Route policy', value: routeInspection.policySummary, code: true },
            ]
            : [];
        const representationRows = representationInspection?.status === 'ready'
            ? [
                { key: 'representation-id', label: 'Representation', value: representationInspection.representationId, code: true },
                { key: 'symbol-family', label: 'Symbol family', value: representationInspection.symbolFamilyId, code: true },
                {
                    key: 'terminal-ids',
                    label: 'Terminals',
                    value: representationInspection.terminals.map(terminal => `${terminal.terminalId}:${terminal.number}`).join(', ') || '-',
                    code: true,
                },
                {
                    key: 'label-ids',
                    label: 'Labels',
                    value: representationInspection.labels.map(label => `${label.labelId}:${label.role}`).join(', ') || '-',
                    code: true,
                },
            ]
            : [];
        const documentReferenceRows = documentReferenceInspection?.status === 'ready'
            ? [
                {
                    key: 'document-reference-relation',
                    label: 'Reference',
                    value: documentReferenceInspection.references.map(reference => `${reference.relationType}:${reference.compactNotation}`).join(', '),
                    code: true,
                },
                {
                    key: 'document-reference-target',
                    label: 'Reference target',
                    value: documentReferenceInspection.references.map(reference => `${reference.targetOccurrenceId}@${reference.targetLocation}`).join(', '),
                    code: true,
                },
            ]
            : [];

        return [
            { key: 'project', label: 'Project', value: model.headerTitle, code: true },
            { key: 'view', label: 'View', value: model.viewLabel },
            { key: 'sheet', label: 'Sheet', value: sheetTitle },
            { key: 'sheet-id', label: 'Sheet ID', value: sheetId, code: true },
            { key: 'selection', label: 'Selection', value: selectionLabel },
            { key: 'semantic-id', label: 'Semantic ID', value: selectedSemanticId ?? '-', code: true },
            { key: 'occurrence', label: 'Occurrence', value: occurrenceSummary, code: true },
            { key: 'endpoint', label: 'Endpoint', value: endpointSummary, code: true },
            { key: 'cross-refs', label: 'Cross refs', value: crossReferenceSummary, code: true },
            { key: 'related', label: 'Related', value: relatedSummary, code: true },
            ...documentReferenceRows,
            ...representationRows,
            ...routeRows,
            { key: 'source', label: 'Source', value: sourceSummary, code: true },
        ];
    }

    protected renderStageChrome(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
        connectPortsSupported: boolean,
        projectionInfoRows: Array<{ key: string; label: string; value: React.ReactNode; code?: boolean }>,
    ): React.ReactNode {
        const visibleProjectionViews = this.resolveVisibleProjectionViews(model);
        const compatibilityProjectionViewCount = Math.max(0, model.supportedViews.length - visibleProjectionViews.length);
        return <div className='athena-graph-workbench__overlay athena-graph-workbench__overlay--top'>
            <div className='athena-graph-workbench__floating-bar'>
                <div className='athena-graph-workbench__identity'>
                    <h2>{model.headerTitle}</h2>
                    <div className='athena-graph-workbench__meta-strip'>
                        <span className='athena-graph-workbench__meta-chip'>{model.viewLabel}</span>
                    </div>
                </div>
                <div className='athena-graph-workbench__tool-group'>
                    {this.renderCreateEntityActionButton()}
                    <div
                        className='athena-graph-workbench__view-switches'
                        data-athena-visible-projection-view-count={visibleProjectionViews.length}
                        data-athena-compatibility-projection-view-count={compatibilityProjectionViewCount}
                    >
                        {visibleProjectionViews.map(view => <button
                            key={view.viewId}
                            className={`athena-graph-workbench__tool-button athena-graph-workbench__tool-button--view ${view.isActive ? 'athena-graph-workbench__tool-button--active' : ''}`}
                            title={this.viewAriaLabel(view)}
                            aria-label={this.viewAriaLabel(view)}
                            data-athena-projection-view-id={view.viewId}
                            type='button'
                            disabled={view.isActive || this.switchingView}
                            onClick={() => void this.switchActiveView(view.viewId)}
                        >
                            <span className={`codicon ${this.viewIconClass(view)}`} />
                        </button>)}
                    </div>
                    {connectPortsSupported
                        ? <button
                            className={`athena-graph-workbench__tool-button ${this.connectPortsArmed || this.connectPortsSource || this.connectPreview ? 'athena-graph-workbench__tool-button--active' : ''}`}
                            type='button'
                            title={this.connectPortsButtonTitle()}
                            aria-label={this.connectPortsButtonTitle()}
                            disabled={this.connectPortsPending || this.connectApplyingDecision}
                            onClick={() => this.toggleConnectPortsMode()}
                        >
                            <span className={`codicon ${this.connectPortsPending ? 'codicon-loading codicon-modifier-spin' : 'codicon-link'}`} />
                        </button>
                        : undefined}
                    <button
                        className={`athena-graph-workbench__tool-button ${this.infoPopoverOpen ? 'athena-graph-workbench__tool-button--active' : ''}`}
                        data-athena-info-button='true'
                        type='button'
                        title='Projection information'
                        aria-label='Projection information'
                        aria-expanded={this.infoPopoverOpen}
                        onClick={() => this.toggleInfoPopover()}
                    >
                        <span className='codicon codicon-info' />
                    </button>
                    <div
                        className={`athena-graph-workbench__status athena-graph-workbench__status--${model.statusTone}`}
                        title={model.statusLabel}
                        aria-label={model.statusLabel}
                    >
                        <span className={`athena-graph-workbench__status-icon codicon ${this.statusIconClass(model.statusTone)}`} />
                    </div>
                </div>
            </div>
            {this.renderContextualDocumentNavigation(model)}
            {this.connectPreview || this.connectPreviewMessage ? this.renderSemanticRelationshipPreview() : undefined}
            {this.infoPopoverOpen ? this.renderProjectionInfoPopover(projectionInfoRows) : undefined}
        </div>;
    }

    protected renderContextualDocumentNavigation(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
    ): React.ReactNode {
        const hasSheetSelector = !!this.resolveVisibleSheetViewSelector(model);
        const hasReferenceMarkers = model.referenceMarkers.length > 0;
        if (!hasSheetSelector && !hasReferenceMarkers) {
            return undefined;
        }
        return <nav
            className='athena-graph-workbench__document-navigation'
            aria-label='Document navigation'
        >
            {hasSheetSelector ? this.renderSheetViewSelector(model) : undefined}
            {hasReferenceMarkers ? this.renderReferenceMarkerControls(model) : undefined}
        </nav>;
    }

    protected renderCreateEntityActionButton(): React.ReactNode {
        const availableItems = this.resolveCreateEntityItems();
        const disabled = this.createEntityPreviewing ||
            this.createEntityApplyingDecision;
        return <button
            className={`athena-graph-workbench__tool-button ${this.createEntityControlsOpen || this.createEntityPreview ? 'athena-graph-workbench__tool-button--active' : ''}`}
            data-athena-create-entity-button='true'
            type='button'
            title='Create device'
            aria-label='Create device'
            aria-expanded={this.createEntityControlsOpen}
            disabled={disabled}
            onClick={() => this.toggleCreateEntityControls()}
        >
            <span className={`codicon ${this.createEntityPreviewing ? 'codicon-loading codicon-modifier-spin' : 'codicon-add'}`} />
        </button>;
    }

    protected resolveVisibleProjectionViews(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
    ): AthenaGraphWorkbenchModel['supportedViews'] {
        const cabinetView = model.supportedViews.find(view => view.viewId === 'cabinet');
        if (cabinetView) {
            return [cabinetView];
        }
        const activeView = model.supportedViews.find(view => view.isActive);
        return activeView ? [activeView] : model.supportedViews.slice(0, 1);
    }

    protected renderCreateEntityControls(): React.ReactNode {
        const items = this.resolveCreateEntityItems();
        const draft = this.resolveCreateEntityDraft(items);
        const selectedItem = this.resolveSelectedCreateEntityItem(items, draft);
        const preview = this.createEntityPreview;
        const evidence = preview?.entityCreationEvidence;
        const createEntityCapabilityReady = !!this.componentKnowledge?.systemSemanticId && items.length > 0;
        const canPreview = !!selectedItem?.conceptTemplateId && !!this.componentKnowledge?.systemSemanticId &&
            !this.createEntityPreviewing &&
            !this.createEntityApplyingDecision;
        const canAccept = !!preview?.acceptanceEligible && !!evidence?.sourceEdit && !this.createEntityApplyingDecision;

        return <section
            className='athena-graph-workbench__create-entity-panel'
            data-athena-create-entity-panel='true'
            aria-label='Create device'
        >
            <div className='athena-graph-workbench__create-entity-header'>
                <div>
                    <span className='athena-graph-workbench__info-popover-eyebrow'>Authoring</span>
                    <h3>Create Device</h3>
                </div>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Close create entity controls'
                    aria-label='Close create entity controls'
                    onClick={() => void this.closeCreateEntityControls()}
                >
                    <span className='codicon codicon-close' />
                </button>
            </div>
            {!createEntityCapabilityReady ? <p className='athena-graph-workbench__create-entity-message'>
                Create Device is a placeholder until governed concept evidence is available.
            </p> : undefined}
            <div className='athena-graph-workbench__create-entity-grid'>
                <label>
                    <span>Concept</span>
                    <select
                        value={draft.conceptId}
                        onChange={event => this.updateCreateEntityConcept(event.currentTarget.value)}
                        disabled={this.createEntityPreviewing || this.createEntityApplyingDecision}
                    >
                        {items.map(item => <option key={item.conceptId} value={item.conceptId}>
                            {item.displayName}
                        </option>)}
                    </select>
                </label>
                <label>
                    <span>Tag</span>
                    <input
                        value={draft.suggestedName}
                        onChange={event => this.updateCreateEntityDraft({ suggestedName: event.currentTarget.value })}
                        placeholder='ShutterMotorM31'
                        disabled={this.createEntityPreviewing || this.createEntityApplyingDecision}
                    />
                </label>
                <label>
                    <span>Model</span>
                    <input
                        value={draft.model}
                        onChange={event => this.updateCreateEntityDraft({ model: event.currentTarget.value })}
                        placeholder='SPARE-XT'
                        disabled={this.createEntityPreviewing || this.createEntityApplyingDecision}
                    />
                </label>
            </div>
            <div className='athena-graph-workbench__create-entity-actions'>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Preview device creation'
                    aria-label='Preview device creation'
                    disabled={!canPreview}
                    onClick={() => void this.previewCreateEntityTransaction()}
                >
                    <span className={`codicon ${this.createEntityPreviewing ? 'codicon-loading codicon-modifier-spin' : 'codicon-eye'}`} />
                </button>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Accept device creation'
                    aria-label='Accept device creation'
                    disabled={!canAccept}
                    onClick={() => void this.acceptCreateEntityPreview()}
                >
                    <span className='codicon codicon-check' />
                </button>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Reject device creation'
                    aria-label='Reject device creation'
                    disabled={!preview || this.createEntityApplyingDecision}
                    onClick={() => void this.rejectCreateEntityPreview()}
                >
                    <span className='codicon codicon-close' />
                </button>
            </div>
            {this.createEntityPreviewMessage ? <p className='athena-graph-workbench__create-entity-message'>{this.createEntityPreviewMessage}</p> : undefined}
            {preview ? this.renderCreateEntityPreview(preview) : undefined}
        </section>;
    }

    protected renderCreateEntityPreview(preview: AthenaAuthoringPreviewPayload): React.ReactNode {
        const evidence = preview.entityCreationEvidence;
        const projectionOccurrenceIds = Array.isArray(evidence?.projectionOccurrenceIds) ? evidence.projectionOccurrenceIds : [];
        const affectedSemanticIds = Array.isArray(evidence?.affectedSemanticIds) ? evidence.affectedSemanticIds : [];
        const nestedPorts = Array.isArray(evidence?.nestedPorts) ? evidence.nestedPorts : [];
        const diagnostics = Array.isArray(preview.diagnostics) ? preview.diagnostics : [];
        const sourceEditPreviewText = preview.sourceImpact?.newText ?? evidence?.sourceEdit?.admittedText;
        return <div className='athena-graph-workbench__create-entity-preview'>
            <div className='athena-graph-workbench__create-entity-preview-header'>
                <strong>{preview.title}</strong>
                <span className={`athena-graph-workbench__status athena-graph-workbench__status--${preview.status}`}>
                    {preview.status}
                </span>
            </div>
            <table className='athena-graph-workbench__info-table'>
                <tbody>
                    <tr><th>Transaction</th><td><code>{preview.previewId}</code></td></tr>
                    <tr><th>Intent</th><td><code>{preview.intentId}</code></td></tr>
                    <tr><th>Lifecycle</th><td>{preview.status}</td></tr>
                    <tr><th>Canonical tag</th><td><code>{evidence?.canonicalTag ?? '-'}</code></td></tr>
                    <tr><th>Type</th><td><code>{evidence?.semanticType ?? '-'}</code></td></tr>
                    <tr><th>Model</th><td>{evidence?.model ?? '-'}</td></tr>
                    <tr><th>Representation</th><td><code>{evidence?.representationId ?? '-'}</code></td></tr>
                    <tr><th>Composition</th><td><code>{evidence?.compositionTargetId ?? '-'}</code></td></tr>
                    <tr><th>Occurrences</th><td><code>{projectionOccurrenceIds.join(', ') || '-'}</code></td></tr>
                    <tr><th>Affected</th><td><code>{affectedSemanticIds.join(', ') || '-'}</code></td></tr>
                    <tr><th>Ports</th><td><code>{nestedPorts.map(port => `${port.name}:${port.direction}:${port.signalOrMedium}`).join(', ') || '-'}</code></td></tr>
                </tbody>
            </table>
            {diagnostics.length > 0
                ? <ul className='athena-graph-workbench__create-entity-diagnostics'>
                    {diagnostics.map(diagnostic => <li key={`${diagnostic.code}:${diagnostic.authority}:${diagnostic.lifecycleStage}`}>
                        <code>{diagnostic.code}</code>
                        <span>{diagnostic.authority}/{diagnostic.lifecycleStage}</span>
                        <span>{diagnostic.message}</span>
                        {diagnostic.recoveryAction ? <span>Recovery: {diagnostic.recoveryAction}</span> : undefined}
                    </li>)}
                </ul>
                : undefined}
            {sourceEditPreviewText
                ? <pre className='athena-graph-workbench__create-entity-source'>{sourceEditPreviewText}</pre>
                : undefined}
            {preview.sourceImpact?.newText && evidence?.sourceEdit?.admittedText && preview.sourceImpact.newText !== evidence.sourceEdit.admittedText
                ? <pre className='athena-graph-workbench__create-entity-source athena-graph-workbench__create-entity-source--admitted'>{evidence.sourceEdit.admittedText}</pre>
                : undefined}
        </div>;
    }

    protected renderSheetViewSelector(model: ReturnType<typeof buildAthenaGraphWorkbenchModel>): React.ReactNode {
        const selector = this.resolveVisibleSheetViewSelector(model);
        if (!selector) {
            return undefined;
        }

        return <label
            className='athena-graph-workbench__sheet-view-selector'
            title='Sheet'
        >
            <span className='codicon codicon-layout' aria-hidden='true' />
            <select
                value={selector.activeSheetViewId ?? ''}
                aria-label='Sheet'
                disabled={this.switchingView}
                onChange={event => void this.switchActiveSheetView(event.currentTarget.value)}
            >
                {selector.entries.map(entry => <option
                    key={entry.sheetViewId}
                    value={entry.sheetViewId}
                >
                    {entry.label}{entry.role ? ` (${entry.role})` : ''}
                </option>)}
            </select>
        </label>;
    }

    protected rememberDocumentSheetViewSelector(model: ReturnType<typeof buildAthenaGraphWorkbenchModel>): void {
        const selector = resolveVisibleAthenaGraphSheetViewSelector(model, undefined);
        if (selector) {
            this.lastDocumentSheetViewSelector = selector;
        }
    }

    protected resolveVisibleSheetViewSelector(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
    ): AthenaGraphWorkbenchSheetViewSelector | undefined {
        const activeViewId = model.supportedViews.find(view => view.isActive)?.viewId;
        if (activeViewId !== 'documentation') {
            return undefined;
        }
        return resolveVisibleAthenaGraphSheetViewSelector(model, this.lastDocumentSheetViewSelector);
    }

    protected renderReferenceMarkerControls(model: ReturnType<typeof buildAthenaGraphWorkbenchModel>): React.ReactNode {
        return <div className='athena-graph-workbench__reference-marker-controls'>
            {model.referenceMarkers.slice(0, 4).map(marker => <button
                key={marker.markerId}
                className='athena-graph-workbench__reference-marker-button'
                type='button'
                title={`${marker.relationType}: ${marker.targetIdentity}`}
                aria-label={`Follow ${marker.relationType} ${marker.compactNotation}`}
                data-athena-reference-marker='true'
                data-athena-reference-marker-id={marker.markerId}
                onClick={() => void this.handleReferenceMarkerClick(marker.markerId)}
            >
                <span className='codicon codicon-arrow-swap' aria-hidden='true' />
                <span>{marker.compactNotation}</span>
            </button>)}
        </div>;
    }

    protected renderProjectionInfoPopover(
        projectionInfoRows: Array<{ key: string; label: string; value: React.ReactNode; code?: boolean }>,
    ): React.ReactNode {
        return <section
            className='athena-graph-workbench__info-popover'
            data-athena-info-popover='true'
            role='dialog'
            aria-label='Projection information'
        >
            <div className='athena-graph-workbench__info-popover-header'>
                <div>
                    <span className='athena-graph-workbench__info-popover-eyebrow'>Sheet</span>
                    <h3>Projection Information</h3>
                </div>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Close projection information'
                    aria-label='Close projection information'
                    onClick={() => this.closeInfoPopover()}
                >
                    <span className='codicon codicon-close' />
                </button>
            </div>
            <table className='athena-graph-workbench__info-table'>
                <tbody>
                    {projectionInfoRows.map(row => <tr key={row.key}>
                        <th>{row.label}</th>
                        <td>{row.code ? <code>{row.value}</code> : row.value}</td>
                    </tr>)}
                </tbody>
            </table>
        </section>;
    }

    protected renderBottomDock(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
        zoomPercent: number,
    ): React.ReactNode {
        return <section className='athena-graph-workbench__bottom-dock' aria-label='Canvas controls'>
            <div className='athena-graph-workbench__zoom-dock'>
                <div
                    className='athena-graph-workbench__statusline-readout'
                    title={`Canvas size ${model.canvas.width} x ${model.canvas.height}`}
                    aria-label={`Canvas size ${model.canvas.width} x ${model.canvas.height}`}
                >
                    Canvas {model.canvas.width} x {model.canvas.height}
                </div>
                <button className='athena-graph-workbench__tool-button' type='button' onClick={() => this.stepZoom(1 / 1.15)} title='Zoom out' aria-label='Zoom out'>
                    <span className='codicon codicon-zoom-out' />
                </button>
                <button className='athena-graph-workbench__tool-button athena-graph-workbench__tool-button--readout' type='button' onClick={() => this.resetZoom()} title='Reset zoom' aria-label='Reset zoom'>
                    {zoomPercent}%
                </button>
                <button className='athena-graph-workbench__tool-button' type='button' onClick={() => this.stepZoom(1.15)} title='Zoom in' aria-label='Zoom in'>
                    <span className='codicon codicon-zoom-in' />
                </button>
                <button className='athena-graph-workbench__tool-button' type='button' onClick={() => this.fitViewportToDiagram()} title='Fit graph to viewport' aria-label='Fit graph to viewport'>
                    <span className='codicon codicon-screen-full' />
                </button>
            </div>
        </section>;
    }

    protected renderLayoutMutationPreview(): React.ReactNode {
        const preview = this.layoutMutationPreview;
        if (!preview) {
            return undefined;
        }
        return <section className='athena-graph-workbench__layout-preview' aria-label='Layout mutation preview'>
            <div className='athena-graph-workbench__layout-preview-header'>
                <span>{preview.title}</span>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    onClick={() => void this.acceptLayoutMutationPreview()}
                    title='Apply layout preview'
                    aria-label='Apply layout preview'
                >
                    <span className='codicon codicon-check' aria-hidden='true' />
                </button>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    onClick={() => this.rejectLayoutMutationPreview()}
                    title='Reject layout preview'
                    aria-label='Reject layout preview'
                >
                    <span className='codicon codicon-close' aria-hidden='true' />
                </button>
            </div>
            <pre className='athena-graph-workbench__layout-preview-code'>{preview.layoutBlockSnippet}</pre>
        </section>;
    }

    protected async acceptLayoutMutationPreview(): Promise<void> {
        const preview = this.layoutMutationPreview;
        if (!preview) {
            return;
        }
        await this.lspEditorBridgeService.applyAuthoringSourceEdit(preview.sourceEdit);
        this.layoutMutationPreview = undefined;
        this.scheduleRefresh();
        void this.semanticSelectionService.selectSemanticId(preview.subjectSemanticId);
        this.update();
    }

    protected rejectLayoutMutationPreview(): void {
        this.layoutMutationPreview = undefined;
        this.update();
    }

    protected renderSheetChrome(model: ReturnType<typeof buildAthenaGraphWorkbenchModel>): React.ReactNode {
        const frame = model.sheetChrome.frame;
        const metadata = model.sheetChrome.metadata;
        const titleBlock = model.sheetChrome.titleBlock;
        const fields = titleBlock?.fields ?? [];
        return <>
            <div
                className='athena-graph-workbench__sheet-frame'
                aria-hidden='true'
                data-athena-sheet-surface={frame.source ? 'true' : undefined}
                data-athena-sheet-surface-id={frame.surfaceId}
                data-athena-sheet-surface-source={frame.source}
                data-athena-sheet-size={metadata?.sheetSize}
                data-athena-sheet-orientation={metadata?.orientation}
                data-athena-sheet-policy-id={metadata?.projectionPolicyId}
                data-athena-sheet-zone-columns={frame.zoneColumns?.join(',')}
                data-athena-sheet-zone-rows={frame.zoneRows?.join(',')}
                data-athena-sheet-margin-top={frame.margins?.top}
                data-athena-sheet-margin-right={frame.margins?.right}
                data-athena-sheet-margin-bottom={frame.margins?.bottom}
                data-athena-sheet-margin-left={frame.margins?.left}
                data-athena-sheet-title-block={fields.length > 0 ? 'true' : undefined}
                data-athena-sheet-title-field-roles={fields.map(field => field.role).join(',')}
            />
        </>;
    }

    protected renderGraphNode(
        node: AthenaGraphWorkbenchNode,
        selectedSemanticId: string | undefined,
    ): React.ReactNode {
        const selected = selectedSemanticId === node.semanticId
            || node.electricalAnchors.some(anchor => anchor.portSemanticId === selectedSemanticId);
        const relationshipCandidateEvidence = this.relationshipCandidateEvidence(node);
        const isRelationshipCandidate = this.isRelationshipCandidateNode(node);
        const relationshipCandidateReason = relationshipCandidateEvidence?.reason;
        const labelClassName = [
            'athena-graph-workbench__node-label',
            `athena-graph-workbench__node-label--${node.renderVariant}`,
            selected ? 'athena-graph-workbench__node-label--selected' : '',
            isRelationshipCandidate ? 'athena-graph-workbench__node-label--connect-target' : '',
        ].filter(Boolean).join(' ');
        const nodeClassName = [
            'athena-graph-workbench__node',
            `athena-graph-workbench__node--${node.kind}`,
            `athena-graph-workbench__node--${node.renderVariant}`,
            selected ? 'athena-graph-workbench__node--selected' : '',
            isRelationshipCandidate ? 'athena-graph-workbench__node--connect-target' : '',
        ].filter(Boolean).join(' ');

        return <g
            key={node.id}
            className='athena-graph-workbench__element'
            data-athena-graph-interactive='true'
            data-athena-representation-fact={node.presentationRepresentation ? 'true' : undefined}
            data-athena-representation-id={node.presentationRepresentation?.representationId}
            data-athena-engineering-package-id={node.presentationRepresentation?.packageEvidence?.engineeringPackageId}
            data-athena-presentation-profile-id={node.presentationRepresentation?.packageEvidence?.presentationProfileId}
            data-athena-binding-manifest-id={node.presentationRepresentation?.packageEvidence?.bindingManifestId}
            data-athena-representation-package-id={node.presentationRepresentation?.packageEvidence?.representationPackageId}
            data-athena-representation-descriptor-id={node.presentationRepresentation?.packageEvidence?.descriptorId}
            data-athena-graphic-resource-id={node.presentationRepresentation?.packageEvidence?.graphicResourceId}
            data-athena-representation-anchor-map={node.presentationRepresentation?.packageEvidence?.anchorMapSummary?.join(';')}
            data-athena-representation-label-binding={node.presentationRepresentation?.packageEvidence?.labelBindingSummary?.join(';')}
            data-athena-semantic-id={node.semanticId}
            data-athena-relationship-candidate-reason={relationshipCandidateReason}
            data-athena-render-fallback={node.presentationRepresentation ? 'false' : undefined}
            role='button'
            tabIndex={0}
            transform={node.kind === 'component' ? this.graphNodeTransform(node.semanticId) : undefined}
            onClick={event => void this.handleNodeClick(event, node.semanticId, node.kind, node.label)}
            onKeyDown={event => void this.handleGraphElementKeyDown(event, node.semanticId, node.kind, node.label)}
            onPointerDown={node.kind === 'component'
                ? event => this.handleComponentPointerDown(event, node.semanticId, node.position.x, node.position.y)
                : undefined}
        >
            <rect
                className='athena-graph-workbench__node-hitbox'
                x={node.position.x}
                y={node.position.y}
                width={node.size.width}
                height={node.size.height}
                rx={0}
                ry={0}
                vectorEffect='non-scaling-stroke'
            />
            {this.renderGraphNodeBody(node, nodeClassName, labelClassName, selected)}
        </g>;
    }

    protected renderGraphNodeBody(
        node: AthenaGraphWorkbenchNode,
        nodeClassName: string,
        labelClassName: string,
        selected: boolean,
    ): React.ReactNode {
        if (node.presentationParts.length > 0 && node.presentationOccurrence) {
            return <>
                <AthenaGraphWorkbenchPresentationNode
                    node={node}
                    nodeClassName={nodeClassName}
                    labelClassName={labelClassName}
                    selected={selected}
                />
                {node.presentationTerminals.map(terminal => this.renderPresentationTerminal(terminal, selected))}
                {node.presentationLabels.map(label => this.renderPresentationLabel(label, labelClassName))}
                {node.renderVariant === 'electrical-device'
                    ? node.electricalAnchors.map(anchor => this.renderElectricalNodeAnchor(anchor, selected))
                    : undefined}
            </>;
        }

        if (node.renderVariant === 'electrical-terminal-label') {
            return <>
                {node.labelLeader
                    ? <line
                        className={`athena-graph-workbench__label-leader ${selected ? 'athena-graph-workbench__label-leader--selected' : ''}`}
                        x1={node.labelLeader.start.x}
                        y1={node.labelLeader.start.y}
                        x2={node.labelLeader.end.x}
                        y2={node.labelLeader.end.y}
                        vectorEffect='non-scaling-stroke'
                    />
                    : undefined}
                <text
                    className={labelClassName}
                    x={node.position.x}
                    y={node.position.y + Math.min(node.size.height - 4, 14)}
                >
                    {node.label}
                </text>
            </>;
        }

        const labelX = node.renderVariant === 'electrical-device'
            ? node.position.x + 4
            : node.position.x + (node.kind === 'label' ? 12 : 20);
        const labelY = node.renderVariant === 'electrical-device'
            ? Math.max(node.position.y - 8, 16)
            : node.position.y + (node.kind === 'label' ? 22 : 34);

        return <>
            <rect
                className={nodeClassName}
                x={node.position.x}
                y={node.position.y}
                width={node.size.width}
                height={node.size.height}
                rx={node.renderVariant === 'electrical-device' ? 0 : node.kind === 'label' ? 3 : 4}
                ry={node.renderVariant === 'electrical-device' ? 0 : node.kind === 'label' ? 3 : 4}
                vectorEffect='non-scaling-stroke'
            />
            {node.renderVariant === 'electrical-device'
                ? node.electricalAnchors.map(anchor => this.renderElectricalNodeAnchor(anchor, selected))
                : undefined}
            <text
                className={labelClassName}
                x={labelX}
                y={labelY}
            >
                {node.label}
            </text>
            {node.kind === 'component' && node.renderVariant !== 'electrical-device'
                ? <text
                    className='athena-graph-workbench__node-meta'
                    x={node.position.x + 20}
                    y={node.position.y + 58}
                >
                    {node.semanticId}
                </text>
                : undefined}
        </>;
    }

    protected renderPresentationTerminal(
        terminal: AthenaGraphWorkbenchNode['presentationTerminals'][number],
        selected: boolean,
    ): React.ReactNode {
        const markerClassName = `athena-graph-workbench__presentation-terminal ${selected ? 'athena-graph-workbench__presentation-terminal--selected' : ''}`;
        const numberOffset = terminal.side.toLowerCase() === 'left' ? -34 : 10;
        const marker = terminal.marker.toLowerCase();
        return <g
            key={terminal.terminalId}
            data-athena-presentation-terminal='true'
            data-athena-presentation-terminal-id={terminal.terminalId}
            data-athena-presentation-terminal-number={terminal.number}
            data-athena-presentation-terminal-anchor-id={terminal.anchorId}
            onClick={event => {
                event.stopPropagation();
                void this.semanticSelectionService.selectSemanticId(terminal.terminalId);
            }}
        >
            {marker === 'square'
                ? <rect
                    className={markerClassName}
                    x={terminal.point.x - 4}
                    y={terminal.point.y - 4}
                    width={8}
                    height={8}
                    vectorEffect='non-scaling-stroke'
                />
                : marker === 'line'
                    ? <line
                        className={markerClassName}
                        x1={terminal.point.x - 5}
                        y1={terminal.point.y}
                        x2={terminal.point.x + 5}
                        y2={terminal.point.y}
                        vectorEffect='non-scaling-stroke'
                    />
                    : <circle
                        className={markerClassName}
                        cx={terminal.point.x}
                        cy={terminal.point.y}
                        r={5}
                        vectorEffect='non-scaling-stroke'
                    />}
            <text
                className='athena-graph-workbench__presentation-terminal-number'
                x={terminal.point.x + numberOffset}
                y={terminal.point.y - 8}
            >
                {terminal.number}
            </text>
        </g>;
    }

    protected renderPresentationLabel(
        label: AthenaGraphWorkbenchNode['presentationLabels'][number],
        labelClassName: string,
    ): React.ReactNode {
        return <text
            key={label.labelId}
            className={`${labelClassName} athena-graph-workbench__presentation-label`}
            data-athena-presentation-label='true'
            data-athena-presentation-label-id={label.labelId}
            data-athena-presentation-label-role={label.role}
            x={label.point.x}
            y={label.point.y}
            onClick={event => {
                event.stopPropagation();
                void this.semanticSelectionService.selectSemanticId(label.labelId);
            }}
        >
            {label.value}
        </text>;
    }

    protected renderElectricalNodeAnchor(
        anchor: AthenaGraphWorkbenchNode['electricalAnchors'][number],
        selected: boolean,
    ): React.ReactNode {
        const delta = this.electricalAnchorInset(anchor.side);
        return <line
            key={anchor.anchorId}
            className={`athena-graph-workbench__node-anchor ${selected ? 'athena-graph-workbench__node-anchor--selected' : ''}`}
            x1={anchor.point.x}
            y1={anchor.point.y}
            x2={anchor.point.x + delta.x}
            y2={anchor.point.y + delta.y}
            vectorEffect='non-scaling-stroke'
        />;
    }

    protected electricalAnchorInset(side: string): { x: number; y: number } {
        switch (side.toLowerCase()) {
            case 'left':
                return { x: 8, y: 0 };
            case 'right':
                return { x: -8, y: 0 };
            case 'top':
                return { x: 0, y: 8 };
            case 'bottom':
                return { x: 0, y: -8 };
            default:
                return { x: 0, y: 0 };
        }
    }

    protected abbreviateViewLabel(displayName: string): string {
        const words = displayName.split(/[\s_-]+/).filter(Boolean);
        if (words.length >= 2) {
            return `${words[0][0] ?? ''}${words[1][0] ?? ''}`.toUpperCase();
        }
        return displayName.slice(0, 2).toUpperCase();
    }

    protected viewIconClass(view: { viewId: string; familyId?: string }): string {
        const normalizedFamilyId = view.familyId?.toLowerCase();
        if (normalizedFamilyId?.endsWith('/cabinet')) {
            return 'codicon-package';
        }
        if (normalizedFamilyId?.endsWith('/wiring')) {
            return 'codicon-git-commit';
        }
        return 'codicon-symbol-misc';
    }

    protected viewAriaLabel(view: { displayName: string; description: string }): string {
        return view.description ? `${view.displayName}: ${view.description}` : view.displayName;
    }

    protected connectPortsButtonTitle(): string {
        if (this.connectApplyingDecision) {
            return 'Applying relationship preview decision';
        }
        if (this.connectPortsPending) {
            return 'Submitting relationship request';
        }
        if (this.connectPreview) {
            return 'Review relationship preview';
        }
        if (!this.connectPortsArmed) {
            return 'Create relationship';
        }
        if (!this.connectPortsSource) {
            return 'Select source terminal';
        }
        return `Select target terminal for ${this.connectPortsSource.label}`;
    }

    protected renderSemanticRelationshipPreview(): React.ReactNode {
        const preview = this.connectPreview;
        const evidence = preview?.relationshipEvidence;
        const diagnostics = Array.isArray(preview?.diagnostics) ? preview.diagnostics : [];
        const affectedSemanticIds = Array.isArray(evidence?.affectedSemanticIds) ? evidence.affectedSemanticIds : [];
        const routePreview = evidence?.routePreview;
        const sourceEditPreviewText = preview?.sourceImpact?.newText ?? evidence?.sourceEdit?.admittedText;
        const canAccept = !!preview?.acceptanceEligible && !!evidence?.sourceEdit && !this.connectApplyingDecision;
        return <section
            className='athena-graph-workbench__create-entity-panel athena-graph-workbench__relationship-preview-panel'
            aria-label='Semantic relationship transaction'
        >
            <div className='athena-graph-workbench__create-entity-header'>
                <div>
                    <span className='athena-graph-workbench__info-popover-eyebrow'>Semantic authoring</span>
                    <h3>Relationship Preview</h3>
                </div>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Cancel semantic relationship preview'
                    aria-label='Cancel semantic relationship preview'
                    disabled={this.connectApplyingDecision}
                    onClick={() => void this.cancelConnectPreview()}
                >
                    <span className='codicon codicon-close' />
                </button>
            </div>
            <div className='athena-graph-workbench__create-entity-actions'>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Accept semantic relationship'
                    aria-label='Accept semantic relationship'
                    disabled={!canAccept}
                    onClick={() => void this.acceptConnectPreview()}
                >
                    <span className='codicon codicon-check' />
                </button>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Reject semantic relationship'
                    aria-label='Reject semantic relationship'
                    disabled={!preview || this.connectApplyingDecision}
                    onClick={() => void this.rejectConnectPreview()}
                >
                    <span className='codicon codicon-discard' />
                </button>
            </div>
            {this.connectPreviewMessage ? <p className='athena-graph-workbench__create-entity-message'>{this.connectPreviewMessage}</p> : undefined}
            {preview ? <div className='athena-graph-workbench__create-entity-preview'>
                <div className='athena-graph-workbench__create-entity-preview-header'>
                    <strong>{preview.title}</strong>
                    <span className={`athena-graph-workbench__status athena-graph-workbench__status--${preview.status}`}>
                        {preview.status}
                    </span>
                </div>
                <table className='athena-graph-workbench__info-table'>
                    <tbody>
                        <tr><th>Transaction</th><td><code>{preview.previewId}</code></td></tr>
                        <tr><th>Intent</th><td><code>{preview.intentId}</code></td></tr>
                        <tr><th>Lifecycle</th><td>{preview.status}</td></tr>
                        <tr><th>Source</th><td><code>{evidence?.sourceSubjectId ?? '-'}</code></td></tr>
                        <tr><th>Target</th><td><code>{evidence?.targetSubjectId ?? '-'}</code></td></tr>
                        <tr><th>Type</th><td><code>{evidence?.relationshipType ?? '-'}</code></td></tr>
                        <tr><th>Compatibility</th><td>{evidence?.compatibility ?? 'not-evaluated'}</td></tr>
                        <tr><th>Route</th><td><code>{routePreview ? `${routePreview.routeId}:${routePreview.quality}:${routePreview.pointCount}` : '-'}</code></td></tr>
                        <tr><th>Revision Guard</th><td><code>{evidence?.sourceEdit?.revisionGuard.contentSha256 ?? preview.revisionGuard?.contentSha256 ?? '-'}</code></td></tr>
                        <tr><th>Affected</th><td><code>{affectedSemanticIds.join(', ') || '-'}</code></td></tr>
                    </tbody>
                </table>
                {diagnostics.length > 0
                    ? <ul className='athena-graph-workbench__create-entity-diagnostics'>
                        {diagnostics.map(diagnostic => <li key={`${diagnostic.code}:${diagnostic.authority}:${diagnostic.lifecycleStage}`}>
                            <code>{diagnostic.code}</code>
                            <span>{diagnostic.authority}/{diagnostic.lifecycleStage}</span>
                            <span>{diagnostic.message}</span>
                            {diagnostic.recoveryAction ? <span>Recovery: {diagnostic.recoveryAction}</span> : undefined}
                        </li>)}
                    </ul>
                    : undefined}
                {sourceEditPreviewText
                    ? <pre className='athena-graph-workbench__create-entity-source'>{sourceEditPreviewText}</pre>
                    : undefined}
                {preview.sourceImpact?.newText && evidence?.sourceEdit?.admittedText && preview.sourceImpact.newText !== evidence.sourceEdit.admittedText
                    ? <pre className='athena-graph-workbench__create-entity-source athena-graph-workbench__create-entity-source--admitted'>{evidence.sourceEdit.admittedText}</pre>
                    : undefined}
            </div> : undefined}
        </section>;
    }

    protected toggleInfoPopover(): void {
        this.infoPopoverOpen = !this.infoPopoverOpen;
        this.update();
    }

    protected closeInfoPopover(): void {
        if (!this.infoPopoverOpen) {
            return;
        }
        this.infoPopoverOpen = false;
        this.update();
    }

    protected toggleCreateEntityControls(): void {
        if (this.createEntityPreviewing || this.createEntityApplyingDecision) {
            return;
        }
        if (this.createEntityControlsOpen) {
            void this.closeCreateEntityControls();
            return;
        }
        this.createEntityControlsOpen = !this.createEntityControlsOpen;
        if (this.createEntityControlsOpen) {
            this.createEntityDraft = this.resolveCreateEntityDraft(this.resolveCreateEntityItems());
            this.infoPopoverOpen = false;
        }
        this.update();
    }

    protected async closeCreateEntityControls(): Promise<void> {
        if (!this.createEntityControlsOpen) {
            return;
        }
        if (this.createEntityPreview) {
            await this.cancelCreateEntityPreview();
        }
        this.createEntityControlsOpen = false;
        this.update();
    }

    protected clearCreateEntityPreview(message?: string): void {
        if (!this.createEntityPreview && !this.createEntityPreviewMessage) {
            return;
        }
        this.createEntityPreview = undefined;
        this.createEntityPreviewing = false;
        this.createEntityApplyingDecision = false;
        this.createEntityPreviewMessage = message;
        this.createEntityPreviewRequestToken++;
    }

    protected resolveCreateEntityItems(): AthenaComponentPanelItem[] {
        return buildAthenaComponentPanelGroups(this.componentKnowledge?.availableComponents ?? [])
            .flatMap(group => group.items)
            .filter(item => !!item.conceptTemplateId);
    }

    protected resolveCreateEntityDraft(items: AthenaComponentPanelItem[]): AthenaGraphCreateEntityDraft {
        const existing = this.createEntityDraft;
        const selectedItem = existing
            ? items.find(item => item.conceptId === existing.conceptId)
            : undefined;
        const item = selectedItem ?? items[0];
        return {
            conceptId: item?.conceptId ?? '',
            conceptTemplateId: item?.conceptTemplateId ?? '',
            suggestedName: existing?.suggestedName ?? this.defaultCreateEntityTag(item),
            model: existing?.model ?? item?.preferredImplementation?.vendorPartNumber ?? '',
        };
    }

    protected resolveSelectedCreateEntityItem(
        items: AthenaComponentPanelItem[],
        draft: AthenaGraphCreateEntityDraft,
    ): AthenaComponentPanelItem | undefined {
        return items.find(item => item.conceptId === draft.conceptId && item.conceptTemplateId === draft.conceptTemplateId)
            ?? items.find(item => item.conceptId === draft.conceptId)
            ?? items[0];
    }

    protected updateCreateEntityConcept(conceptId: string): void {
        const items = this.resolveCreateEntityItems();
        const item = items.find(candidate => candidate.conceptId === conceptId);
        this.createEntityDraft = {
            conceptId: item?.conceptId ?? '',
            conceptTemplateId: item?.conceptTemplateId ?? '',
            suggestedName: this.defaultCreateEntityTag(item),
            model: item?.preferredImplementation?.vendorPartNumber ?? '',
        };
        this.createEntityPreview = undefined;
        this.createEntityPreviewMessage = undefined;
        this.update();
    }

    protected updateCreateEntityDraft(update: Partial<Pick<AthenaGraphCreateEntityDraft, 'suggestedName' | 'model'>>): void {
        this.createEntityDraft = {
            ...this.resolveCreateEntityDraft(this.resolveCreateEntityItems()),
            ...update,
        };
        this.createEntityPreview = undefined;
        this.createEntityPreviewMessage = undefined;
        this.update();
    }

    protected async previewCreateEntityTransaction(): Promise<void> {
        const requestToken = ++this.createEntityPreviewRequestToken;
        const knowledge = this.componentKnowledge;
        const items = this.resolveCreateEntityItems();
        const draft = this.resolveCreateEntityDraft(items);
        const selectedItem = this.resolveSelectedCreateEntityItem(items, draft);
        if (!knowledge?.systemSemanticId || !selectedItem?.conceptTemplateId) {
            this.createEntityPreviewMessage = 'Athena cannot preview entity creation until governed concept capability evidence is available.';
            this.createEntityPreview = undefined;
            this.update();
            return;
        }
        this.createEntityDraft = draft;
        this.createEntityPreviewing = true;
        this.createEntityPreviewMessage = undefined;
        this.createEntityPreview = undefined;
        this.update();
        try {
            const submission = await this.lspEditorBridgeService.requestAuthoringPreview(
                buildCreateEntityPreviewRequest({
                    systemSemanticId: knowledge.systemSemanticId,
                    conceptTemplateId: selectedItem.conceptTemplateId,
                    conceptId: selectedItem.conceptId,
                    actor: 'user:theia',
                    preferredImplementationId: selectedItem.preferredImplementation?.implementationId,
                    suggestedName: draft.suggestedName,
                    model: draft.model,
                    originSurface: 'graph',
                    originDetail: `graph:create-entity:${selectedItem.conceptId}`,
                }),
            );
            if (requestToken !== this.createEntityPreviewRequestToken) {
                return;
            }
            this.createEntityPreview = submission?.preview;
            if (!submission?.preview) {
                this.createEntityPreviewMessage = 'Athena could not create a governed entity preview for the selected concept.';
            }
        } catch (error) {
            if (requestToken !== this.createEntityPreviewRequestToken) {
                return;
            }
            this.createEntityPreview = undefined;
            this.createEntityPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            if (requestToken === this.createEntityPreviewRequestToken) {
                this.createEntityPreviewing = false;
                this.update();
            }
        }
    }

    protected async acceptCreateEntityPreview(): Promise<void> {
        const preview = this.createEntityPreview;
        if (!preview || !preview.acceptanceEligible || !preview.entityCreationEvidence?.sourceEdit) {
            this.createEntityPreviewMessage = 'Athena create-entity preview is not eligible for acceptance.';
            this.update();
            return;
        }
        if (this.currentEditorMatchesCreateEntityPreview(preview) &&
            !await this.lspEditorBridgeService.sourceEditMatchesActiveDocument(preview.entityCreationEvidence.sourceEdit.revisionGuard)
        ) {
            this.clearCreateEntityPreview('Active Athena editor revision no longer matches the create-entity preview. Request a fresh preview before accepting.');
            this.update();
            return;
        }
        this.createEntityApplyingDecision = true;
        this.createEntityPreviewMessage = undefined;
        this.update();
        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'accepted',
                    note: 'Graph create-entity preview accepted.',
                }),
            );
            if (!isAuthoringDecisionCommitted(decision)) {
                this.createEntityPreviewMessage = collectAuthoringDecisionDiagnostics(decision) ||
                    'Athena did not commit the create-entity preview.';
                return;
            }
            if (!decision?.sourceEdit) {
                throw new Error('Athena accepted the create-entity preview but did not return a governed source edit.');
            }
            if (!sourceEditMatchesPreviewEvidence(decision.sourceEdit, preview.entityCreationEvidence.sourceEdit)) {
                throw new Error('Athena returned a source edit that does not match the governed create-entity preview evidence.');
            }
            const committedDiagnostics = collectAuthoringDecisionDiagnostics(decision);
            await this.lspEditorBridgeService.applyAuthoringSourceEdit(decision.sourceEdit);
            const suggestedSemanticId = decision.sourceEdit.suggestedSemanticId
                ?? this.resolveCreatedEntitySemanticId(preview);
            this.createEntityPreview = undefined;
            this.createEntityControlsOpen = !!committedDiagnostics;
            this.createEntityPreviewMessage = committedDiagnostics || undefined;
            this.scheduleRefresh();
            if (suggestedSemanticId) {
                window.setTimeout(() => {
                    void this.semanticSelectionService.selectSemanticId(suggestedSemanticId).catch(error => {
                        this.createEntityPreviewMessage = error instanceof Error ? error.message : String(error);
                        this.update();
                    });
                }, 180);
            }
        } catch (error) {
            this.createEntityPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.createEntityApplyingDecision = false;
            this.update();
        }
    }

    protected async rejectCreateEntityPreview(): Promise<void> {
        const preview = this.createEntityPreview;
        if (!preview) {
            this.createEntityPreviewMessage = undefined;
            this.update();
            return;
        }
        this.createEntityApplyingDecision = true;
        this.createEntityPreviewMessage = undefined;
        this.update();
        try {
            await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'rejected',
                    note: 'Graph create-entity preview rejected.',
                }),
            ).then(decision => {
                if (decision?.status === 'updated' || decision?.status === 'unavailable') {
                    this.createEntityPreview = undefined;
                    return;
                }
                this.createEntityPreviewMessage = collectAuthoringDecisionDiagnostics(decision) ||
                    'Athena did not reject the create-entity preview.';
            });
        } catch (error) {
            this.createEntityPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.createEntityApplyingDecision = false;
            this.update();
        }
    }

    protected async cancelCreateEntityPreview(): Promise<void> {
        const preview = this.createEntityPreview;
        if (!preview) {
            this.createEntityPreviewMessage = undefined;
            return;
        }
        this.createEntityApplyingDecision = true;
        this.createEntityPreviewMessage = undefined;
        this.update();
        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'cancelled',
                    note: 'Graph create-entity preview cancelled.',
                }),
            );
            if (decision?.status === 'updated' || decision?.status === 'unavailable') {
                this.createEntityPreview = undefined;
                return;
            }
            this.createEntityPreviewMessage = collectAuthoringDecisionDiagnostics(decision) ||
                'Athena did not cancel the create-entity preview.';
        } catch (error) {
            this.createEntityPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.createEntityApplyingDecision = false;
            this.update();
        }
    }

    protected currentEditorMatchesCreateEntityPreview(preview: AthenaAuthoringPreviewPayload): boolean {
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor)) {
            return false;
        }
        const sourceEdit = preview.entityCreationEvidence?.sourceEdit;
        return !!sourceEdit &&
            currentEditor.editor.uri.toString() === sourceEdit.uri &&
            currentEditor.editor.uri.toString() === sourceEdit.revisionGuard.sourceUri;
    }

    protected resolveCreatedEntitySemanticId(preview: AthenaAuthoringPreviewPayload): string | undefined {
        const affectedSemanticIds = preview.entityCreationEvidence?.affectedSemanticIds ?? [];
        return affectedSemanticIds.find(semanticId => !semanticId.startsWith('port:')) ??
            preview.entityCreationEvidence?.affectedSemanticIds[0];
    }

    protected defaultCreateEntityTag(item: AthenaComponentPanelItem | undefined): string {
        const conceptTail = item?.conceptId.split('.').filter(Boolean).at(-1) ?? 'Device';
        return `${conceptTail.replace(/[^A-Za-z0-9_]/g, '') || 'Device'}M31`;
    }

    protected handleWorkbenchClick = (event: React.MouseEvent<HTMLDivElement>): void => {
        if (!this.infoPopoverOpen) {
            return;
        }
        const target = event.target instanceof HTMLElement ? event.target : undefined;
        if (target?.closest('[data-athena-info-button="true"], [data-athena-info-popover="true"]')) {
            return;
        }
        this.closeInfoPopover();
    };

    protected toggleConnectPortsMode(): void {
        if (this.connectPortsPending || this.connectApplyingDecision) {
            return;
        }
        if (this.connectPreview) {
            void this.cancelConnectPreview();
            return;
        }
        if (this.connectPortsArmed || this.connectPortsSource) {
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.connectPreviewMessage = undefined;
            this.update();
            return;
        }
        this.connectPortsArmed = true;
        this.connectPortsSource = undefined;
        this.connectPreviewMessage = undefined;
        this.update();
    }

    protected clearConnectPreview(message?: string): void {
        if (!this.connectPreview && !this.connectPreviewMessage && !this.connectPortsArmed && !this.connectPortsSource) {
            return;
        }
        this.connectPortsArmed = false;
        this.connectPortsSource = undefined;
        this.connectPortsPending = false;
        this.connectPreview = undefined;
        this.connectPreviewMessage = message;
        this.connectApplyingDecision = false;
        this.connectPreviewRequestToken++;
    }

    protected async cancelStaleConnectPreview(message: string): Promise<void> {
        const preview = this.connectPreview;
        this.clearConnectPreview(message);
        this.update();
        if (!preview) {
            return;
        }
        try {
            await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'cancelled',
                    note: message,
                }),
            );
        } catch (error) {
            this.connectPreviewMessage = `${message} ${error instanceof Error ? error.message : String(error)}`;
            this.update();
        }
    }

    protected async previewSemanticRelationship(sourceSemanticId: string, targetSemanticId: string): Promise<void> {
        const requestToken = ++this.connectPreviewRequestToken;
        this.connectPortsPending = true;
        this.connectPreview = undefined;
        this.connectPreviewMessage = undefined;
        this.update();
        try {
            const submission = await this.lspEditorBridgeService.requestAuthoringPreview(
                buildSemanticRelationshipPreviewRequest({
                    sourceSubjectId: sourceSemanticId,
                    targetSubjectId: targetSemanticId,
                    originDetail: `graph:${this.diagram?.activeViewId ?? 'unknown-view'}`,
                }),
            );
            if (requestToken !== this.connectPreviewRequestToken) {
                return;
            }
            this.connectPreview = submission?.preview;
            if (!submission?.preview) {
                this.connectPreviewMessage = 'Athena could not create a semantic relationship preview for the selected terminals.';
            }
        } catch (error) {
            if (requestToken !== this.connectPreviewRequestToken) {
                return;
            }
            this.connectPreview = undefined;
            this.connectPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            if (requestToken === this.connectPreviewRequestToken) {
                this.connectPortsPending = false;
                this.update();
            }
        }
    }

    protected async acceptConnectPreview(): Promise<void> {
        const preview = this.connectPreview;
        if (!preview || !preview.acceptanceEligible || !preview.relationshipEvidence?.sourceEdit) {
            this.connectPreviewMessage = 'Athena semantic relationship preview is not eligible for acceptance.';
            this.update();
            return;
        }
        if (!this.currentEditorMatchesConnectPreview(preview)) {
            this.clearConnectPreview('Active Athena editor does not match the semantic relationship preview source. Request a fresh preview before accepting.');
            this.update();
            return;
        }
        this.connectApplyingDecision = true;
        this.connectPreviewMessage = undefined;
        this.update();
        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'accepted',
                    note: 'Graph semantic relationship preview accepted.',
                }),
            );
            if (!isAuthoringDecisionCommitted(decision)) {
                this.connectPreviewMessage = collectAuthoringDecisionDiagnostics(decision) ||
                    'Athena did not commit the semantic relationship preview.';
                return;
            }
            if (!this.isCurrentConnectPreview(preview)) {
                this.connectPreviewMessage = 'Athena semantic relationship preview became stale before acceptance completed.';
                return;
            }
            if (!decision?.sourceEdit) {
                throw new Error('Athena accepted the semantic relationship preview but did not return a governed source edit.');
            }
            if (!sourceEditMatchesPreviewEvidence(decision.sourceEdit, preview.relationshipEvidence.sourceEdit)) {
                throw new Error('Athena returned a source edit that does not match the governed semantic relationship preview evidence.');
            }
            const committedDiagnostics = collectAuthoringDecisionDiagnostics(decision);
            await this.lspEditorBridgeService.applyAuthoringSourceEdit(decision.sourceEdit);
            const suggestedSemanticId = decision.sourceEdit.suggestedSemanticId
                ?? this.resolveCreatedRelationshipSemanticId(preview);
            this.connectPreview = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.connectPreviewMessage = committedDiagnostics || undefined;
            this.scheduleRefresh();
            if (suggestedSemanticId) {
                window.setTimeout(() => {
                    void this.semanticSelectionService.selectSemanticId(suggestedSemanticId).catch(error => {
                        this.connectPreviewMessage = error instanceof Error ? error.message : String(error);
                        this.update();
                    });
                }, 180);
            }
        } catch (error) {
            this.connectPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.connectApplyingDecision = false;
            this.update();
        }
    }

    protected async rejectConnectPreview(): Promise<void> {
        const preview = this.connectPreview;
        if (!preview) {
            this.connectPreviewMessage = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.update();
            return;
        }
        this.connectApplyingDecision = true;
        this.connectPreviewMessage = undefined;
        this.update();
        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'rejected',
                    note: 'Graph semantic relationship preview rejected.',
                }),
            );
            if (!this.isSameConnectPreviewSession(preview)) {
                return;
            }
            if (decision?.status === 'updated' || decision?.status === 'unavailable') {
                this.connectPreview = undefined;
                this.connectPortsArmed = false;
                this.connectPortsSource = undefined;
                return;
            }
            this.connectPreviewMessage = collectAuthoringDecisionDiagnostics(decision) ||
                'Athena did not reject the semantic relationship preview.';
        } catch (error) {
            this.connectPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.connectApplyingDecision = false;
            this.update();
        }
    }

    protected async cancelConnectPreview(): Promise<void> {
        const preview = this.connectPreview;
        if (!preview) {
            this.clearConnectPreview();
            this.update();
            return;
        }
        this.connectApplyingDecision = true;
        this.connectPreviewMessage = undefined;
        this.update();
        try {
            const decision = await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'cancelled',
                    note: 'Graph semantic relationship preview cancelled.',
                }),
            );
            if (!this.isSameConnectPreviewSession(preview)) {
                return;
            }
            if (decision?.status === 'updated' || decision?.status === 'unavailable') {
                this.connectPreview = undefined;
                this.connectPortsArmed = false;
                this.connectPortsSource = undefined;
                return;
            }
            this.connectPreviewMessage = collectAuthoringDecisionDiagnostics(decision) ||
                'Athena did not cancel the semantic relationship preview.';
        } catch (error) {
            this.connectPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.connectApplyingDecision = false;
            this.update();
        }
    }

    protected currentEditorMatchesConnectPreview(preview: AthenaAuthoringPreviewPayload): boolean {
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor)) {
            return false;
        }
        const sourceEdit = preview.relationshipEvidence?.sourceEdit;
        return !!sourceEdit &&
            currentEditor.editor.uri.toString() === sourceEdit.uri &&
            currentEditor.editor.uri.toString() === sourceEdit.revisionGuard.sourceUri;
    }

    protected isCurrentConnectPreview(preview: AthenaAuthoringPreviewPayload): boolean {
        return this.isSameConnectPreviewSession(preview) &&
            this.currentEditorMatchesConnectPreview(preview);
    }

    protected isSameConnectPreviewSession(preview: AthenaAuthoringPreviewPayload): boolean {
        const currentPreview = this.connectPreview;
        return currentPreview?.previewId === preview.previewId &&
            currentPreview.intentId === preview.intentId;
    }

    protected resolveCreatedRelationshipSemanticId(preview: AthenaAuthoringPreviewPayload): string | undefined {
        return preview.relationshipEvidence?.affectedSemanticIds.find(semanticId =>
            semanticId.startsWith('connection:') || semanticId.startsWith('relationship:'),
        );
    }

    protected statusIconClass(statusTone: ReturnType<typeof buildAthenaGraphWorkbenchModel>['statusTone']): string {
        if (statusTone === 'ready') {
            return 'codicon-check';
        }
        if (statusTone === 'warning') {
            return 'codicon-warning';
        }
        return 'codicon-circle-large-outline';
    }

    protected buildStageStyle(model: ReturnType<typeof buildAthenaGraphWorkbenchModel>): React.CSSProperties {
        const style = {} as React.CSSProperties & Record<string, string>;
        const surfaceTokens = model.surfaceTokens;
        style['--athena-graph-sheet-grid-major-step'] = `${model.sheetChrome.grid.majorStep}px`;
        style['--athena-graph-sheet-grid-minor-step'] = `${model.sheetChrome.grid.minorStep}px`;
        const cssVariables: Record<string, string | undefined> = {
            '--athena-graph-stage-tint': surfaceTokens.canvas.canvasTint,
            '--athena-graph-grid-major': surfaceTokens.canvas.gridMajor,
            '--athena-graph-grid-minor': surfaceTokens.canvas.gridMinor,
            '--athena-graph-node-fill': surfaceTokens.node.fill,
            '--athena-graph-node-stroke': surfaceTokens.node.stroke,
            '--athena-graph-node-label': surfaceTokens.node.label,
            '--athena-graph-node-meta': surfaceTokens.node.meta,
            '--athena-graph-edge-stroke': surfaceTokens.edge.stroke,
        };

        for (const [key, value] of Object.entries(cssVariables)) {
            if (value && this.isThemeRelativeSurfaceToken(value)) {
                style[key] = value;
            }
        }

        return style;
    }

    protected isThemeRelativeSurfaceToken(value: string): boolean {
        const normalized = value.trim();
        return normalized === 'transparent'
            || normalized === 'currentColor'
            || normalized === 'inherit'
            || normalized.startsWith('var(');
    }

    protected bindViewportElement = (element: HTMLDivElement | null): void => {
        const nextElement = element ?? undefined;
        if (this.viewportElement === nextElement) {
            return;
        }

        this.viewportObserver?.disconnect();
        this.viewportElement = nextElement;

        if (!this.viewportElement) {
            this.viewportObserver = undefined;
            return;
        }

        this.viewportObserver = new ResizeObserver(() => this.syncViewportSize());
        this.viewportObserver.observe(this.viewportElement);
        this.syncViewportSize();
    };

    protected syncViewportSize(): void {
        if (!this.viewportElement) {
            return;
        }

        const nextSize = this.readViewportElementSize();
        const nextWidth = nextSize.width;
        const nextHeight = nextSize.height;
        const changed = nextWidth !== this.viewportSize.width || nextHeight !== this.viewportSize.height;
        const previousViewportSize = this.viewportSize;
        this.viewportSize = { width: nextWidth, height: nextHeight };

        if (this.pendingAutoFit) {
            this.fitViewportToDiagramIfPossible();
            return;
        }

        if (changed) {
            if (this.viewportMode === 'auto-fit' && this.diagram) {
                const model = buildAthenaGraphWorkbenchModel(this.diagram);
                if (!model.emptyState) {
                    this.viewportTransform = fitAthenaGraphViewport(model.sceneBounds, this.resolveAutoFitViewportSize());
                }
            } else if (previousViewportSize.width > 0 && previousViewportSize.height > 0) {
                this.viewportTransform = resizeAthenaGraphViewport(
                    this.viewportTransform,
                    previousViewportSize,
                    this.viewportSize,
                );
            }
            this.update();
        }
    }

    protected readViewportElementSize(): AthenaGraphViewportSize {
        return {
            width: Math.max(Math.round(this.viewportElement?.clientWidth ?? 0), 0),
            height: Math.max(Math.round(this.viewportElement?.clientHeight ?? 0), 0),
        };
    }

    protected refreshViewportSizeFromElement(): void {
        if (!this.viewportElement) {
            return;
        }
        this.viewportSize = this.readViewportElementSize();
    }

    protected fitViewportToDiagram(): void {
        this.pendingAutoFit = true;
        this.viewportMode = 'auto-fit';
        this.fitViewportToDiagramIfPossible();
    }

    protected fitViewportToDiagramIfPossible(): void {
        this.refreshViewportSizeFromElement();
        if (!this.pendingAutoFit || !this.diagram || this.viewportSize.width <= 0 || this.viewportSize.height <= 0) {
            return;
        }

        const model = buildAthenaGraphWorkbenchModel(this.diagram);
        if (model.emptyState) {
            this.pendingAutoFit = false;
            this.viewportTransform = { zoom: 1, offsetX: 0, offsetY: 0 };
            this.update();
            return;
        }

        this.viewportTransform = fitAthenaGraphViewport(model.sceneBounds, this.resolveAutoFitViewportSize());
        this.pendingAutoFit = false;
        this.viewportMode = 'auto-fit';
        this.update();
    }

    protected resetZoom(): void {
        const center = this.getViewportCenterPoint();
        this.viewportTransform = zoomAthenaGraphViewportAtPoint(this.viewportTransform, center, 1);
        this.viewportMode = 'manual';
        this.update();
    }

    protected graphNodeTransform(nodeId: string): string | undefined {
        if (!this.dragState || this.dragState.semanticId !== nodeId) {
            return undefined;
        }
        const deltaX = this.dragState.currentX - this.dragState.originX;
        const deltaY = this.dragState.currentY - this.dragState.originY;
        return `translate(${deltaX}, ${deltaY})`;
    }

    protected stepZoom(multiplier: number): void {
        const center = this.getViewportCenterPoint();
        this.viewportTransform = zoomAthenaGraphViewportAtPoint(
            this.viewportTransform,
            center,
            clampAthenaGraphZoom(this.viewportTransform.zoom * multiplier)
        );
        this.viewportMode = 'manual';
        this.update();
    }

    protected getViewportCenterPoint(): { x: number; y: number } {
        return {
            x: this.viewportSize.width > 0 ? this.viewportSize.width / 2 : 0,
            y: this.viewportSize.height > 0 ? this.viewportSize.height / 2 : 0,
        };
    }

    protected diagramViewportKey(
        diagram: Awaited<ReturnType<AthenaGraphAdapterService['requestDiagram']>>
    ): string {
        return [
            diagram.kind,
            diagram.graph?.id ?? 'unknown-graph',
            diagram.activeViewId ?? 'unknown-view',
            diagram.activeSheetId ?? 'unknown-sheet'
        ].join('|');
    }

    protected keepSelectionVisible(
        diagram: Awaited<ReturnType<AthenaGraphAdapterService['requestDiagram']>> | undefined = this.diagram,
    ): void {
        const currentDiagram = diagram ?? this.diagram;
        const selection = this.semanticSelectionService.selection;
        if (!currentDiagram || !selection) {
            return;
        }

        const model = buildAthenaGraphWorkbenchModel(currentDiagram);
        const nextTransform = keepAthenaGraphViewportFocusedOnSelection(
            this.viewportTransform,
            this.viewportSize,
            model.nodes,
            model.edges,
            selection.semanticId,
        );
        if (
            nextTransform.zoom === this.viewportTransform.zoom &&
            nextTransform.offsetX === this.viewportTransform.offsetX &&
            nextTransform.offsetY === this.viewportTransform.offsetY
        ) {
            return;
        }

        this.viewportTransform = nextTransform;
        this.update();
    }

    protected handleViewportClick = (event: React.MouseEvent<HTMLDivElement>): void => {
        if (!this.isInteractiveTarget(event.target)) {
            void this.semanticSelectionService.clearSelection();
        }
    };

    protected handleViewportDoubleClick = (event: React.MouseEvent<HTMLDivElement>): void => {
        if (this.isInteractiveTarget(event.target)) {
            return;
        }
        this.fitViewportToDiagram();
    };

    protected handleViewportWheel = (event: React.WheelEvent<HTMLDivElement>): void => {
        if (!this.diagram) {
            return;
        }

        event.preventDefault();
        const rect = event.currentTarget.getBoundingClientRect();
        const screenPoint = {
            x: event.clientX - rect.left,
            y: event.clientY - rect.top,
        };
        const multiplier = event.deltaY < 0 ? 1.12 : 1 / 1.12;
        this.viewportTransform = zoomAthenaGraphViewportAtPoint(
            this.viewportTransform,
            screenPoint,
            this.viewportTransform.zoom * multiplier
        );
        this.viewportMode = 'manual';
        this.update();
    };

    protected handleViewportPointerDown = (event: React.PointerEvent<HTMLDivElement>): void => {
        const shouldPan = event.button === 1 || !this.isInteractiveTarget(event.target);
        if (!shouldPan) {
            return;
        }

        event.preventDefault();
        this.panState = {
            pointerId: event.pointerId,
            lastClientX: event.clientX,
            lastClientY: event.clientY,
        };
        this.viewportMode = 'manual';
        event.currentTarget.setPointerCapture(event.pointerId);
        this.update();
    };

    protected handleViewportPointerMove = (event: React.PointerEvent<HTMLDivElement>): void => {
        if (this.dragState && this.dragState.pointerId === event.pointerId) {
            event.preventDefault();
            const zoom = this.viewportTransform.zoom <= 0 ? 1 : this.viewportTransform.zoom;
            const deltaX = Math.round((event.clientX - this.dragState.startClientX) / zoom);
            const deltaY = Math.round((event.clientY - this.dragState.startClientY) / zoom);
            this.dragState = {
                ...this.dragState,
                currentX: this.dragState.originX + deltaX,
                currentY: this.dragState.originY + deltaY,
                moved: this.dragState.moved || deltaX !== 0 || deltaY !== 0,
            };
            this.update();
            return;
        }

        if (!this.panState || this.panState.pointerId !== event.pointerId) {
            return;
        }

        const deltaX = event.clientX - this.panState.lastClientX;
        const deltaY = event.clientY - this.panState.lastClientY;
        this.panState = {
            pointerId: event.pointerId,
            lastClientX: event.clientX,
            lastClientY: event.clientY,
        };
        this.viewportTransform = panAthenaGraphViewport(this.viewportTransform, deltaX, deltaY);
        this.update();
    };

    protected handleViewportPointerEnd = (event: React.PointerEvent<HTMLDivElement>): void => {
        if (this.dragState && this.dragState.pointerId === event.pointerId) {
            const dragState = this.dragState;
            this.dragState = undefined;
            this.update();
            if (dragState.moved) {
                void this.submitPlacementIntent(dragState);
            }
            return;
        }

        if (!this.panState || this.panState.pointerId !== event.pointerId) {
            return;
        }

        this.panState = undefined;
        if (event.currentTarget.hasPointerCapture(event.pointerId)) {
            event.currentTarget.releasePointerCapture(event.pointerId);
        }
        this.update();
    };

    protected isInteractiveTarget(target: EventTarget | null): boolean {
        return target instanceof Element && !!target.closest('[data-athena-graph-interactive="true"]');
    }

    protected handleGraphElementKeyDown(
        event: React.KeyboardEvent<SVGGElement>,
        semanticId: string,
        kind: 'component' | 'label',
        label: string,
    ): Promise<void> | void {
        if (event.key !== 'Enter' && event.key !== ' ') {
            return;
        }
        event.preventDefault();
        return this.handleNodeSelection(semanticId, kind, label);
    }

    protected handleNodeClick(
        event: React.MouseEvent<SVGGElement>,
        semanticId: string,
        kind: 'component' | 'label',
        label: string,
    ): Promise<void> | void {
        event.stopPropagation();
        return this.handleNodeSelection(semanticId, kind, label);
    }

    protected async handleNodeSelection(
        semanticId: string,
        kind: 'component' | 'label',
        label: string,
    ): Promise<void> {
        if (this.connectPortsArmed && this.isConnectablePortNode(semanticId, kind)) {
            return this.handleConnectablePortSelection(semanticId, label);
        }
        await this.semanticSelectionService.selectSemanticId(semanticId);
    }

    protected isConnectablePortNode(semanticId: string, kind: 'component' | 'label'): boolean {
        return kind === 'label' && !!this.semanticInspectionPort(semanticId);
    }

    protected isRelationshipCandidateNode(node: AthenaGraphWorkbenchNode): boolean {
        return this.relationshipCandidateEvidence(node)?.compatibility === 'candidate';
    }

    protected relationshipCandidateEvidence(
        node: AthenaGraphWorkbenchNode,
    ): AthenaGraphRelationshipCandidateEvidence | undefined {
        if (!this.connectPortsSource || !this.diagram || !this.graphAdapterService.supportsCreateSemanticRelationshipIntent(this.diagram)) {
            return undefined;
        }
        if (!this.isConnectablePortNode(node.semanticId, node.kind) || node.semanticId === this.connectPortsSource.semanticId) {
            return undefined;
        }
        return this.relationshipCandidateEvidenceFor(this.connectPortsSource.semanticId, node.semanticId);
    }

    protected relationshipCandidateEvidenceFor(
        sourceSemanticId: string,
        targetSemanticId: string,
    ): AthenaGraphRelationshipCandidateEvidence | undefined {
        const sourcePort = this.semanticInspectionPort(sourceSemanticId);
        const targetPort = this.semanticInspectionPort(targetSemanticId);
        if (!sourcePort || !targetPort) {
            return undefined;
        }

        const sourceSignal = this.semanticInspectionPortProperty(sourceSemanticId, 'signal');
        const targetSignal = this.semanticInspectionPortProperty(targetSemanticId, 'signal');
        if (!sourceSignal || !targetSignal) {
            return {
                authority: 'semantic-inspection-compatibility',
                compatibility: 'rejected',
                sourceSemanticId,
                targetSemanticId,
                reason: 'Relationship compatibility evidence is missing signal-family facts for this projection endpoint.',
            };
        }
        if (sourceSignal !== targetSignal) {
            return {
                authority: 'semantic-inspection-compatibility',
                compatibility: 'rejected',
                sourceSemanticId,
                targetSemanticId,
                reason: 'Relationship compatibility evidence rejects this endpoint because signal families differ.',
            };
        }

        const sourceDirection = this.semanticInspectionPortProperty(sourceSemanticId, 'direction');
        const targetDirection = this.semanticInspectionPortProperty(targetSemanticId, 'direction');
        if (!sourceDirection || !targetDirection || !this.semanticDirectionsCompatible(sourceDirection, targetDirection)) {
            return {
                authority: 'semantic-inspection-compatibility',
                compatibility: 'rejected',
                sourceSemanticId,
                targetSemanticId,
                reason: 'Relationship compatibility evidence rejects this endpoint because semantic directions are incompatible.',
            };
        }

        return {
            authority: 'semantic-inspection-compatibility',
            compatibility: 'candidate',
            sourceSemanticId,
            targetSemanticId,
            reason: 'Compatible semantic relationship compatibility evidence is available for this projection endpoint.',
        };
    }

    protected semanticInspectionPort(semanticId: string): AthenaSemanticInspectionPayload['ports'][number] | undefined {
        return this.semanticInspection?.ports.find(port => port.semanticId === semanticId);
    }

    protected semanticInspectionPortProperty(semanticId: string, propertyName: string): string | undefined {
        return this.semanticInspectionPort(semanticId)
            ?.authoredProperties
            .find(property => property.name === propertyName)
            ?.valueText;
    }

    protected semanticDirectionsCompatible(sourceDirection: string, targetDirection: string): boolean {
        const source = sourceDirection.toLowerCase();
        const target = targetDirection.toLowerCase();
        if (source === 'output' || source === 'out') {
            return ['input', 'in', 'passive', 'bidirectional'].includes(target);
        }
        if (source === 'input' || source === 'in') {
            return ['output', 'out', 'passive', 'bidirectional'].includes(target);
        }
        if (source === 'bidirectional') {
            return ['output', 'out', 'input', 'in', 'bidirectional'].includes(target);
        }
        return source === 'passive';
    }

    protected async handleConnectablePortSelection(
        semanticId: string,
        label: string,
    ): Promise<void> {
        await this.semanticSelectionService.selectSemanticId(semanticId);
        this.connectPreviewMessage = undefined;
        if (!this.connectPortsSource) {
            this.connectPortsSource = {
                semanticId,
                label,
            };
            this.update();
            return;
        }

        const source = this.connectPortsSource;
        this.connectPortsSource = undefined;
        this.connectPortsArmed = false;
        this.update();
        await this.previewSemanticRelationship(source.semanticId, semanticId);
    }

    protected handleComponentPointerDown(
        event: React.PointerEvent<SVGGElement>,
        semanticId: string,
        x: number,
        y: number
    ): void {
        if (event.button !== 0 || !this.diagram || !this.graphAdapterService.supportsAdjustLayoutPlacementIntent(this.diagram)) {
            return;
        }

        event.preventDefault();
        event.stopPropagation();
        this.dragState = {
            pointerId: event.pointerId,
            semanticId,
            subjectKind: 'component',
            originX: x,
            originY: y,
            currentX: x,
            currentY: y,
            startClientX: event.clientX,
            startClientY: event.clientY,
            moved: false,
        };
        this.update();
    }

    protected reconcileTransientSelection(
        diagram: Awaited<ReturnType<AthenaGraphAdapterService['requestDiagram']>>
    ): void {
        if (!diagram) {
            return;
        }

        const retained = retainSelectionIfPresent(diagram, this.semanticSelectionService.selection);
        if (!this.semanticSelectionService.selection || retained) {
            return;
        }
    }

    protected async switchActiveView(viewId: string): Promise<boolean> {
        if (this.switchingView) {
            return false;
        }

        this.switchingView = true;
        this.errorMessage = undefined;
        this.lastGraphCommandIntent = undefined;
        this.dragState = undefined;
        this.connectPortsArmed = false;
        this.connectPortsSource = undefined;
        this.update();

        try {
            const diagram = await this.graphAdapterService.switchActiveView(viewId);
            if (!diagram) {
                this.errorMessage = `Athena did not return a governed diagram for view \`${viewId}\`.`;
                return false;
            }
            this.lastDiagramViewportKey = this.diagramViewportKey(diagram);
            this.diagram = diagram;
            this.reconcileTransientSelection(diagram);
            this.revealingSelectionSemanticId = undefined;
            this.pendingAutoFit = true;
            this.viewportMode = 'auto-fit';
            this.fitViewportToDiagramIfPossible();
            return true;
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
            return false;
        } finally {
            this.switchingView = false;
            this.update();
        }
    }

    protected async switchActiveSheetView(sheetViewId: string): Promise<boolean> {
        if (!sheetViewId || sheetViewId === this.diagram?.activeSheetId) {
            return true;
        }
        return this.switchActiveView(sheetViewId);
    }

    protected async handleReferenceMarkerClick(markerId: string): Promise<void> {
        if (!this.diagram) {
            return;
        }

        const navigation = resolveAthenaGraphReferenceMarkerNavigation(
            buildAthenaGraphWorkbenchModel(this.diagram),
            markerId,
        );
        if (navigation.status !== 'ready') {
            this.errorMessage = navigation.reason;
            this.update();
            return;
        }

        if (navigation.requiresSheetSwitch) {
            const switched = await this.switchActiveSheetView(navigation.targetSheetViewId);
            if (!switched) {
                return;
            }
        }
        await this.semanticSelectionService.selectSemanticId(navigation.targetCanonicalId);
    }

    protected async submitPlacementIntent(dragState: AthenaGraphNodeDragState): Promise<void> {
        if (!this.diagram) {
            return;
        }
        const model = buildAthenaGraphWorkbenchModel(this.diagram);
        const node = model.nodes.find(candidate => candidate.semanticId === dragState.semanticId);
        let capturedIntent: AthenaGraphLayoutAdjustmentIntent | undefined;
        if (node) {
            const capture = captureAthenaGraphLayoutAdjustmentIntent({
                model,
                node,
                kind: 'place',
                relation: 'near',
            });
            capturedIntent = capture.accepted ? capture.intent : undefined;
            this.lastLayoutAdjustmentIntent = capturedIntent;
            this.layoutMutationPreview = undefined;
        }

        try {
            const payload = await this.graphAdapterService.submitAdjustLayoutPlacementIntent({
                diagram: this.diagram,
                semanticId: dragState.semanticId,
                subjectKind: dragState.subjectKind,
                x: dragState.currentX,
                y: dragState.currentY,
                authoredLayoutIntent: capturedIntent
                    ? buildAthenaGraphAuthoredLayoutIntent(capturedIntent)
                    : undefined,
            });
            this.lastGraphCommandIntent = payload;
            if (payload?.status === 'accepted') {
                this.layoutMutationPreview = capturedIntent && payload.sourceEdit
                    ? buildAthenaGraphLayoutMutationPreview(capturedIntent, payload.sourceEdit)
                    : undefined;
                const diagram = await this.graphAdapterService.requestDiagram();
                this.lastDiagramViewportKey = this.diagramViewportKey(diagram);
                this.diagram = diagram;
                this.reconcileTransientSelection(diagram);
                this.revealingSelectionSemanticId = undefined;
                this.keepSelectionVisible(diagram);
                await this.semanticSelectionService.selectSemanticId(dragState.semanticId);
            }
            this.errorMessage = undefined;
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.update();
        }
    }

    protected async handleSemanticSelectionChanged(
        selection: AthenaActiveSemanticSelection | undefined,
        diagramOverride?: Awaited<ReturnType<AthenaGraphAdapterService['requestDiagram']>>,
    ): Promise<void> {
        this.update();
        if (!selection) {
            this.revealingSelectionSemanticId = undefined;
            return;
        }

        const diagram = diagramOverride ?? this.diagram;
        if (!diagram || this.loading || this.switchingView) {
            return;
        }
        if (graphContainsSemanticId(diagram, selection.semanticId)) {
            this.revealingSelectionSemanticId = undefined;
            this.keepSelectionVisible(diagram);
            return;
        }
        if (this.revealingSelectionSemanticId === selection.semanticId) {
            return;
        }

        this.revealingSelectionSemanticId = selection.semanticId;
        try {
            const revealedDiagram = await this.graphAdapterService.revealSemanticId(selection.semanticId, diagram);
            if (!revealedDiagram || this.semanticSelectionService.selection?.semanticId !== selection.semanticId) {
                return;
            }
            const viewChanged = !this.diagram || revealedDiagram.activeViewId !== this.diagram.activeViewId;
            this.lastDiagramViewportKey = this.diagramViewportKey(revealedDiagram);
            this.diagram = revealedDiagram;
            if (viewChanged && graphContainsSemanticId(revealedDiagram, selection.semanticId)) {
                this.pendingAutoFit = true;
                this.viewportMode = 'auto-fit';
                this.fitViewportToDiagramIfPossible();
            } else {
                this.keepSelectionVisible(revealedDiagram);
            }
            this.errorMessage = undefined;
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            if (this.revealingSelectionSemanticId === selection.semanticId) {
                this.revealingSelectionSemanticId = undefined;
            }
            this.update();
        }
    }
}
