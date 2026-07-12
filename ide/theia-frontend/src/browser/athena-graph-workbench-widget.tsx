import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { AthenaGraphAdapterService } from './athena-graph-adapter-service';
import { type AthenaGraphCommandIntentPayload } from './athena-graph-command-intent-protocol';
import { AthenaGraphWorkbenchEdgeLayer } from './athena-graph-workbench-edge-layer';
import {
    AthenaGraphViewportSize,
    AthenaGraphViewportTransform,
    buildAthenaGraphWorkbenchModel,
    clampAthenaGraphZoom,
    fitAthenaGraphViewport,
    panAthenaGraphViewport,
    resizeAthenaGraphViewport,
    zoomAthenaGraphViewportAtPoint
} from './athena-graph-workbench-model';
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

/** Graph-first Athena workbench surface with a pannable and zoomable renderer viewport. */
@injectable()
export class AthenaGraphWorkbenchWidget extends ReactWidget {
    static readonly ID = 'athena.graphWorkbench';
    static readonly LABEL = 'Graphical View';

    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaGraphAdapterService)
    protected readonly graphAdapterService: AthenaGraphAdapterService;

    @inject(AthenaSemanticSelectionService)
    protected readonly semanticSelectionService: AthenaSemanticSelectionService;

    protected currentEditorListeners = new DisposableCollection();
    protected diagram = undefined as Awaited<ReturnType<AthenaGraphAdapterService['requestDiagram']>>;
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
    protected overlayPanelExpanded = false;
    protected lastGraphCommandIntent = undefined as AthenaGraphCommandIntentPayload | undefined;
    protected connectPortsArmed = false;
    protected connectPortsSource: AthenaGraphPortConnectSource | undefined;
    protected connectPortsPending = false;
    protected revealingSelectionSemanticId: string | undefined;

    @postConstruct()
    protected init(): void {
        this.id = AthenaGraphWorkbenchWidget.ID;
        this.title.label = AthenaGraphWorkbenchWidget.LABEL;
        this.title.caption = AthenaGraphWorkbenchWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-type-hierarchy-sub';
        this.addClass('athena-graph-workbench-widget');

        this.toDispose.push(this.currentEditorListeners);
        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(this.semanticSelectionService.onDidChangeSelection(selection => {
            void this.handleSemanticSelectionChanged(selection);
        }));
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(widget => {
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
            this.revealingSelectionSemanticId = undefined;
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
            this.update();
            return;
        }

        this.loading = true;
        this.errorMessage = undefined;
        this.update();

        try {
            const diagram = await this.graphAdapterService.requestDiagram();
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.diagram = diagram;
            this.lastGraphCommandIntent = undefined;
            this.dragState = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.connectPortsPending = false;
            this.reconcileTransientSelection(diagram);
            void this.handleSemanticSelectionChanged(this.semanticSelectionService.selection, diagram);
            this.pendingAutoFit = true;
            this.viewportMode = 'auto-fit';
            this.fitViewportToDiagramIfPossible();
        } catch (error) {
            if (this.repositorySessionService.state.repositoryRoot !== currentRepositoryRoot) {
                return;
            }
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.diagram = undefined;
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
        const connectPortsSupported = this.graphAdapterService.supportsConnectPortsIntent(this.diagram);
        const zoomPercent = Math.round(this.viewportTransform.zoom * 100);
        const stageStyle = this.buildStageStyle(model);
        const lastIntentSummary = this.lastGraphCommandIntent
            ? `${this.lastGraphCommandIntent.intentId} ${this.lastGraphCommandIntent.status}`
            : undefined;
        const canvasStyle: React.CSSProperties = {
            width: `${model.canvas.width}px`,
            height: `${model.canvas.height}px`,
            transform: `translate(${this.viewportTransform.offsetX}px, ${this.viewportTransform.offsetY}px) scale(${this.viewportTransform.zoom})`,
            transformOrigin: '0 0',
        };

        return <div className='athena-graph-workbench'>
            <div className='athena-graph-workbench__workspace'>
                <section
                    className={`athena-graph-workbench__stage ${this.panState ? 'athena-graph-workbench__stage--panning' : ''}`}
                    style={stageStyle}
                    onClick={this.handleStageClick}
                >
                    {model.emptyState
                        ? <div className='athena-graph-workbench__empty athena-graph-workbench__empty--inline'>
                            <h4>{model.emptyState.title}</h4>
                            <p>{model.emptyState.message}</p>
                        </div>
                        : <>
                            <div className='athena-graph-workbench__overlay athena-graph-workbench__overlay--top'>
                                <div className='athena-graph-workbench__floating-bar'>
                                    <div className='athena-graph-workbench__identity'>
                                        <h2>{model.headerTitle}</h2>
                                        <div className='athena-graph-workbench__meta-strip'>
                                            <span className='athena-graph-workbench__meta-chip'>{model.viewLabel}</span>
                                        </div>
                                    </div>
                                    <div className='athena-graph-workbench__tool-group'>
                                        <div className='athena-graph-workbench__view-switches'>
                                            {model.supportedViews.map(view => <button
                                                key={view.viewId}
                                                className={`athena-graph-workbench__tool-button athena-graph-workbench__tool-button--view ${view.isActive ? 'athena-graph-workbench__tool-button--active' : ''}`}
                                                title={this.viewAriaLabel(view)}
                                                aria-label={this.viewAriaLabel(view)}
                                                type='button'
                                                disabled={view.isActive || this.switchingView}
                                                onClick={() => void this.switchActiveView(view.viewId)}
                                            >
                                                <span className={`codicon ${this.viewIconClass(view.viewId)}`} />
                                            </button>)}
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
                                        {connectPortsSupported
                                            ? <button
                                                className={`athena-graph-workbench__tool-button ${this.connectPortsArmed || this.connectPortsSource ? 'athena-graph-workbench__tool-button--active' : ''}`}
                                                type='button'
                                                title={this.connectPortsButtonTitle()}
                                                aria-label={this.connectPortsButtonTitle()}
                                                disabled={this.connectPortsPending}
                                                onClick={() => this.toggleConnectPortsMode()}
                                            >
                                                <span className={`codicon ${this.connectPortsPending ? 'codicon-loading codicon-modifier-spin' : 'codicon-link'}`} />
                                            </button>
                                            : undefined}
                                        <button
                                            className={`athena-graph-workbench__overlay-toggle ${this.overlayPanelExpanded ? 'athena-graph-workbench__overlay-toggle--active' : ''}`}
                                            type='button'
                                            title={this.overlayPanelExpanded ? 'Collapse information panel' : 'Expand information panel'}
                                            aria-label={this.overlayPanelExpanded ? 'Collapse information panel' : 'Expand information panel'}
                                            onClick={() => this.toggleOverlayPanel()}
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
                                {this.overlayPanelExpanded
                                    ? <div className='athena-graph-workbench__floating-panel'>
                                        <section className='athena-graph-workbench__overlay-section'>
                                            <div className='athena-graph-workbench__panel-title'>Selection</div>
                                            {!selectedSemanticId
                                                ? <div className='athena-graph-workbench__panel-empty'>No selection</div>
                                                : <div className='athena-graph-workbench__selection'>
                                                    <strong>{selectedSemantic?.label ?? selectedSemanticId}</strong>
                                                    <span className='athena-graph-workbench__pill'>{selectedSemantic?.kind ?? 'semantic'}</span>
                                                    <code>{selectedSemanticId}</code>
                                                    {selectionResolution
                                                        ? <div className='athena-graph-workbench__panel-empty'>
                                                            {selectionResolution.status} - {selectionResolution.occurrenceIds.length} occurrence(s)
                                                        </div>
                                                        : undefined}
                                                    {endpointAliasResolution && endpointAliasResolution.status !== 'unresolved'
                                                        ? <div className='athena-graph-workbench__panel-empty'>
                                                            terminal aliases - {endpointAliasResolution.endpointIds.length} endpoint(s), {endpointAliasResolution.anchorIds.length} anchor(s)
                                                        </div>
                                                        : undefined}
                                                    {crossReference
                                                        ? <div className='athena-graph-workbench__panel-empty'>
                                                            {crossReference.kind} - {crossReference.sheetIds.length} sheet(s)
                                                        </div>
                                                        : undefined}
                                                </div>}
                                            {this.connectPortsArmed
                                                ? <div className='athena-graph-workbench__panel-empty'>
                                                    {this.connectPortsSource
                                                        ? <>Awaiting target port for <code>{this.connectPortsSource.semanticId}</code>.</>
                                                        : 'Connect ports is armed. Select a source port label, then a target port label.'}
                                                </div>
                                                : undefined}
                                        </section>

                                        <section className='athena-graph-workbench__overlay-section'>
                                            <div className='athena-graph-workbench__panel-title'>Related</div>
                                            {!selectedSemanticId
                                                ? <div className='athena-graph-workbench__panel-empty'>No canonical subject is selected.</div>
                                                : <>
                                                    {canRevealDocumentationReferences
                                                        ? <button
                                                            className='athena-graph-workbench__inline-action'
                                                            type='button'
                                                            onClick={() => void this.switchActiveView('documentation')}
                                                        >
                                                            Show repeated references
                                                        </button>
                                                        : undefined}
                                                    {relatedSubjects.length === 0
                                                        ? <div className='athena-graph-workbench__panel-empty'>No governed related subjects are published for the current selection.</div>
                                                        : <ul className='athena-graph-workbench__list'>
                                                            {relatedSubjects.map(subject => <li
                                                                key={`${subject.relation}:${subject.relatedSemanticId}`}
                                                                className='athena-graph-workbench__item'
                                                            >
                                                                <button
                                                                    className='athena-graph-workbench__inline-action'
                                                                    type='button'
                                                                    onClick={() => void this.semanticSelectionService.selectSemanticId(subject.relatedSemanticId)}
                                                                >
                                                                    {this.relatedSubjectLabel(subject.relation)}
                                                                </button>
                                                                <div className='athena-graph-workbench__related-meta'>
                                                                    <code>{subject.relatedSemanticId}</code>
                                                                </div>
                                                            </li>)}
                                                        </ul>}
                                                </>}
                                        </section>

                                        <section className='athena-graph-workbench__overlay-section'>
                                            <div className='athena-graph-workbench__panel-title'>Snapshot</div>
                                            <dl className='athena-graph-workbench__detail-list'>
                                                <div>
                                                    <dt><span className='codicon codicon-symbol-class' /></dt>
                                                    <dd>{model.metrics.nodeCount} nodes</dd>
                                                </div>
                                                <div>
                                                    <dt><span className='codicon codicon-git-commit' /></dt>
                                                    <dd>{model.metrics.edgeCount} links</dd>
                                                </div>
                                                <div>
                                                    <dt><span className='codicon codicon-warning' /></dt>
                                                    <dd>{model.metrics.diagnosticCount} diagnostics</dd>
                                                </div>
                                            </dl>
                                        </section>

                                        <section className='athena-graph-workbench__overlay-section'>
                                            <div className='athena-graph-workbench__panel-title'>Session</div>
                                            <dl className='athena-graph-workbench__detail-list'>
                                                <div>
                                                    <dt>View</dt>
                                                    <dd>{this.diagram.activeViewId}</dd>
                                                </div>
                                                {model.viewFamilyId
                                                    ? <div>
                                                        <dt>Family</dt>
                                                        <dd><code>{model.viewFamilyId}</code></dd>
                                                    </div>
                                                    : undefined}
                                                {model.activeSheetId
                                                    ? <div>
                                                        <dt>Sheet</dt>
                                                        <dd><code>{model.activeSheetId}</code></dd>
                                                    </div>
                                                    : undefined}
                                                {model.notationPackId
                                                    ? <div>
                                                        <dt>Notation</dt>
                                                        <dd><code>{model.notationPackId}</code></dd>
                                                    </div>
                                                    : undefined}
                                                <div>
                                                    <dt>Sheets</dt>
                                                    <dd>{model.sheetCount}</dd>
                                                </div>
                                                <div>
                                                    <dt>Cross refs</dt>
                                                    <dd>{model.crossReferenceCount}</dd>
                                                </div>
                                                <div>
                                                    <dt>Graph</dt>
                                                    <dd><code>{this.diagram.graph.id}</code></dd>
                                                </div>
                                                <div>
                                                    <dt>Mappings</dt>
                                                    <dd>{model.activeRenderContributions.map(contribution => contribution.displayName).join(', ') || 'None'}</dd>
                                                </div>
                                            </dl>
                                        </section>

                                        {this.lastGraphCommandIntent
                                            ? <section className='athena-graph-workbench__overlay-section'>
                                                <div className='athena-graph-workbench__panel-title'>Last Intent</div>
                                                <dl className='athena-graph-workbench__detail-list'>
                                                    <div>
                                                        <dt>Status</dt>
                                                        <dd>{this.lastGraphCommandIntent.status}</dd>
                                                    </div>
                                                    {this.lastGraphCommandIntent.source
                                                        ? <div>
                                                            <dt>Source</dt>
                                                            <dd><code>{this.lastGraphCommandIntent.source.semanticId}</code></dd>
                                                        </div>
                                                        : undefined}
                                                    <div>
                                                        <dt>Intent</dt>
                                                        <dd><code>{this.lastGraphCommandIntent.intentId}</code></dd>
                                                    </div>
                                                    <div>
                                                        <dt>Category</dt>
                                                        <dd>{this.lastGraphCommandIntent.mutationCategory}</dd>
                                                    </div>
                                                    <div>
                                                        <dt>Target</dt>
                                                        <dd><code>{this.lastGraphCommandIntent.target.semanticId}</code></dd>
                                                    </div>
                                                    <div>
                                                        <dt>View</dt>
                                                        <dd>{this.lastGraphCommandIntent.viewId}</dd>
                                                    </div>
                                                    {this.lastGraphCommandIntent.execution
                                                        ? <div>
                                                            <dt>Command</dt>
                                                            <dd><code>{this.lastGraphCommandIntent.execution.commandKind}</code></dd>
                                                        </div>
                                                        : undefined}
                                                    {this.lastGraphCommandIntent.execution?.commandId
                                                        ? <div>
                                                            <dt>Command Id</dt>
                                                            <dd><code>{this.lastGraphCommandIntent.execution.commandId}</code></dd>
                                                        </div>
                                                        : undefined}
                                                </dl>
                                                {this.lastGraphCommandIntent.validationFeedback.length > 0
                                                    ? <ul className='athena-graph-workbench__list'>
                                                        {this.lastGraphCommandIntent.validationFeedback.map(feedback => <li
                                                            key={`${feedback.code}:${feedback.message}`}
                                                            className='athena-graph-workbench__item'
                                                        >
                                                            <div className='athena-graph-workbench__diagnostic-header'>
                                                                <span className={`athena-graph-workbench__diagnostic-severity athena-graph-workbench__diagnostic-severity--${feedback.severity}`}>
                                                                    {feedback.severity}
                                                                </span>
                                                                <code>{feedback.code}</code>
                                                            </div>
                                                            <div>{feedback.message}</div>
                                                        </li>)}
                                                    </ul>
                                                    : undefined}
                                                {this.lastGraphCommandIntent.reason
                                                    ? <div className='athena-graph-workbench__panel-empty'>{this.lastGraphCommandIntent.reason}</div>
                                                    : undefined}
                                            </section>
                                            : undefined}

                                        {model.diagnostics.length > 0
                                            ? <section className='athena-graph-workbench__overlay-section'>
                                                <div className='athena-graph-workbench__panel-title'>Diagnostics</div>
                                                <ul className='athena-graph-workbench__list'>
                                                    {model.diagnostics.slice(0, 3).map(diagnostic => <li
                                                        key={`${diagnostic.code}:${diagnostic.message}`}
                                                        className='athena-graph-workbench__item'
                                                    >
                                                        <div className='athena-graph-workbench__diagnostic-header'>
                                                            <span className={`athena-graph-workbench__diagnostic-severity athena-graph-workbench__diagnostic-severity--${diagnostic.severity}`}>
                                                                {diagnostic.severity}
                                                            </span>
                                                            <code>{diagnostic.code}</code>
                                                        </div>
                                                        <div>{diagnostic.message}</div>
                                                    </li>)}
                                                </ul>
                                            </section>
                                            : undefined}
                            </div>
                                    : undefined}
                            </div>

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
                                <div className='athena-graph-workbench__grid' />
                                <svg
                                    className='athena-graph-workbench__canvas'
                                    viewBox={model.svgViewBox}
                                    role='img'
                                    aria-label='Athena graphical projection'
                                    style={canvasStyle}
                                >
                                    <AthenaGraphWorkbenchEdgeLayer
                                        edges={model.edges}
                                        selectedSemanticId={selectedSemanticId}
                                        onSelectSemanticId={semanticId => this.semanticSelectionService.selectSemanticId(semanticId)}
                                    />
                                    {model.nodes.map(node => <g
                                        key={node.id}
                                        className='athena-graph-workbench__element'
                                        data-athena-graph-interactive='true'
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
                                            className={`athena-graph-workbench__node athena-graph-workbench__node--${node.kind} ${selectedSemanticId === node.semanticId ? 'athena-graph-workbench__node--selected' : ''}`}
                                            x={node.position.x}
                                            y={node.position.y}
                                            width={node.size.width}
                                            height={node.size.height}
                                            rx={node.kind === 'label' ? 3 : 4}
                                            ry={node.kind === 'label' ? 3 : 4}
                                            vectorEffect='non-scaling-stroke'
                                        />
                                        <text
                                            className='athena-graph-workbench__node-label'
                                            x={node.position.x + (node.kind === 'label' ? 12 : 20)}
                                            y={node.position.y + (node.kind === 'label' ? 22 : 34)}
                                        >
                                            {node.label}
                                        </text>
                                        {node.kind === 'component'
                                            ? <text
                                                className='athena-graph-workbench__node-meta'
                                                x={node.position.x + 20}
                                                y={node.position.y + 58}
                                            >
                                                {node.semanticId}
                                            </text>
                                            : undefined}
                                    </g>)}
                                </svg>
                            </div>

                            <div className='athena-graph-workbench__statusline'>
                                <span title={`Canvas size ${model.canvas.width} x ${model.canvas.height}`} aria-label={`Canvas size ${model.canvas.width} x ${model.canvas.height}`}>
                                    <span className='codicon codicon-device-desktop' />
                                </span>
                                <span title={model.semanticPath} aria-label={`Semantic path ${model.semanticPath}`}>
                                    <span className='codicon codicon-git-branch' />
                                </span>
                                {lastIntentSummary
                                    ? <span title={lastIntentSummary} aria-label={`Last Athena graph intent ${lastIntentSummary}`}>
                                        <span className='codicon codicon-symbol-event' />
                                    </span>
                                    : undefined}
                            </div>
                        </>}
                </section>
            </div>
        </div>;
    }

    protected abbreviateViewLabel(displayName: string): string {
        const words = displayName.split(/[\s_-]+/).filter(Boolean);
        if (words.length >= 2) {
            return `${words[0][0] ?? ''}${words[1][0] ?? ''}`.toUpperCase();
        }
        return displayName.slice(0, 2).toUpperCase();
    }

    protected viewIconClass(viewId: string): string {
        switch (viewId.toLowerCase()) {
            case 'cabinet':
                return 'codicon-package';
            case 'wiring':
                return 'codicon-git-commit';
            default:
                return 'codicon-symbol-misc';
        }
    }

    protected viewAriaLabel(view: { displayName: string; description: string }): string {
        return view.description ? `${view.displayName}: ${view.description}` : view.displayName;
    }

    protected connectPortsButtonTitle(): string {
        if (this.connectPortsPending) {
            return 'Submitting connect ports request';
        }
        if (!this.connectPortsArmed) {
            return 'Connect ports';
        }
        if (!this.connectPortsSource) {
            return 'Select source port';
        }
        return `Select target port for ${this.connectPortsSource.label}`;
    }

    protected relatedSubjectLabel(
        relation: 'owner' | 'owned-port' | 'connection' | 'source-port' | 'target-port',
    ): string {
        switch (relation) {
            case 'owner':
                return 'Reveal owner';
            case 'owned-port':
                return 'Reveal owned port';
            case 'connection':
                return 'Reveal connection';
            case 'source-port':
                return 'Reveal source port';
            case 'target-port':
                return 'Reveal target port';
            default:
                return 'Reveal related subject';
        }
    }

    protected toggleOverlayPanel(): void {
        this.overlayPanelExpanded = !this.overlayPanelExpanded;
        this.update();
    }

    protected toggleConnectPortsMode(): void {
        if (this.connectPortsPending) {
            return;
        }
        if (this.connectPortsArmed || this.connectPortsSource) {
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.update();
            return;
        }
        this.connectPortsArmed = true;
        this.connectPortsSource = undefined;
        this.update();
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
            if (value) {
                style[key] = value;
            }
        }

        return style;
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

        const nextWidth = Math.max(Math.round(this.viewportElement.clientWidth), 0);
        const nextHeight = Math.max(Math.round(this.viewportElement.clientHeight), 0);
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
                    this.viewportTransform = fitAthenaGraphViewport(model.sceneBounds, this.viewportSize);
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

    protected fitViewportToDiagram(): void {
        this.pendingAutoFit = true;
        this.viewportMode = 'auto-fit';
        this.fitViewportToDiagramIfPossible();
    }

    protected fitViewportToDiagramIfPossible(): void {
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

        this.viewportTransform = fitAthenaGraphViewport(model.sceneBounds, this.viewportSize);
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

    protected handleViewportClick = (event: React.MouseEvent<HTMLDivElement>): void => {
        if (!this.isInteractiveTarget(event.target)) {
            void this.semanticSelectionService.clearSelection();
        }
    };

    protected handleStageClick = (event: React.MouseEvent<HTMLElement>): void => {
        if (!this.overlayPanelExpanded) {
            return;
        }
        const target = event.target;
        if (
            target instanceof Element &&
            (target.closest('.athena-graph-workbench__floating-panel') ||
                target.closest('.athena-graph-workbench__overlay-toggle'))
        ) {
            return;
        }
        this.overlayPanelExpanded = false;
        this.update();
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
        return kind === 'label' && semanticId.startsWith('port:');
    }

    protected async handleConnectablePortSelection(
        semanticId: string,
        label: string,
    ): Promise<void> {
        await this.semanticSelectionService.selectSemanticId(semanticId);
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
        await this.submitConnectPortsIntent(source.semanticId, semanticId);
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

    protected async switchActiveView(viewId: string): Promise<void> {
        if (this.switchingView) {
            return;
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
            this.diagram = diagram;
            this.reconcileTransientSelection(diagram);
            this.revealingSelectionSemanticId = undefined;
            this.pendingAutoFit = true;
            this.viewportMode = 'auto-fit';
            this.fitViewportToDiagramIfPossible();
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.switchingView = false;
            this.update();
        }
    }

    protected async submitPlacementIntent(dragState: AthenaGraphNodeDragState): Promise<void> {
        if (!this.diagram) {
            return;
        }

        try {
            const payload = await this.graphAdapterService.submitAdjustLayoutPlacementIntent({
                diagram: this.diagram,
                semanticId: dragState.semanticId,
                subjectKind: dragState.subjectKind,
                x: dragState.currentX,
                y: dragState.currentY,
            });
            this.lastGraphCommandIntent = payload;
            if (payload?.status === 'accepted') {
                const diagram = await this.graphAdapterService.requestDiagram();
                this.diagram = diagram;
                this.reconcileTransientSelection(diagram);
                this.revealingSelectionSemanticId = undefined;
                await this.semanticSelectionService.selectSemanticId(dragState.semanticId);
            }
            this.errorMessage = undefined;
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.update();
        }
    }

    protected async submitConnectPortsIntent(sourceSemanticId: string, targetSemanticId: string): Promise<void> {
        if (!this.diagram) {
            return;
        }

        this.connectPortsPending = true;
        this.errorMessage = undefined;
        this.update();
        try {
            const payload = await this.graphAdapterService.submitConnectPortsIntent({
                diagram: this.diagram,
                sourceSemanticId,
                targetSemanticId,
            });
            this.lastGraphCommandIntent = payload;
            if (payload?.status === 'accepted') {
                const diagram = await this.graphAdapterService.requestDiagram();
                this.diagram = diagram;
                this.reconcileTransientSelection(diagram);
                this.revealingSelectionSemanticId = undefined;
                const createdConnectionId = payload.execution?.changedSemanticIds.find(semanticId => semanticId.startsWith('connection:'));
                if (createdConnectionId) {
                    await this.semanticSelectionService.selectSemanticId(createdConnectionId);
                }
            }
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.connectPortsPending = false;
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
            this.diagram = revealedDiagram;
            if (viewChanged && graphContainsSemanticId(revealedDiagram, selection.semanticId)) {
                this.pendingAutoFit = true;
                this.viewportMode = 'auto-fit';
                this.fitViewportToDiagramIfPossible();
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
