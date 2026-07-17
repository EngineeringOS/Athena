import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { Message } from '@lumino/messaging';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import type { AthenaAuthoringPreviewPayload } from './athena-authoring-protocol';
import {
    buildAthenaGraphLayoutMutationPreview,
    buildAthenaGraphLayoutSourceEdit,
    captureAthenaGraphLayoutAdjustmentIntent,
    keepAthenaGraphViewportFocusedOnSelection,
    type AthenaGraphLayoutAdjustmentIntent,
    type AthenaGraphLayoutMutationPreview,
    type AthenaGraphWorkbenchEdge,
    type AthenaGraphWorkbenchNode,
    type AthenaGraphViewportSize,
    type AthenaGraphViewportTransform,
    buildAthenaGraphWorkbenchModel,
    clampAthenaGraphZoom,
    fitAthenaGraphViewport,
    panAthenaGraphViewport,
    resizeAthenaGraphViewport,
    zoomAthenaGraphViewportAtPoint
} from './athena-graph-workbench-model';
import {
    buildAuthoringDecisionRequest,
    buildConnectPortsPreviewRequest
} from './athena-authoring-protocol';
import type { AthenaComponentKnowledgeSessionPayload } from './athena-component-knowledge-protocol';
import { AthenaGraphAdapterService } from './athena-graph-adapter-service';
import { type AthenaGraphCommandIntentPayload } from './athena-graph-command-intent-protocol';
import {
    AthenaCompatibleConnectionTarget,
    buildAthenaCompatibleConnectionTargets
} from './athena-guided-connection-model';
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
    protected revealingSelectionSemanticId: string | undefined;
    protected infoPopoverOpen = false;

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
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.connectPortsPending = false;
            this.connectPreview = undefined;
            this.connectPreviewMessage = undefined;
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
        const connectPortsSupported = this.graphAdapterService.supportsConnectPortsIntent(this.diagram);
        const compatibleConnectTargets = buildAthenaCompatibleConnectionTargets({
            knowledge: this.componentKnowledge,
            inspection: this.semanticInspection,
            sourcePortSemanticId: this.connectPortsSource?.semanticId,
        });
        const compatibleConnectTargetIds = new Set(compatibleConnectTargets.map(target => target.semanticId));
        const zoomPercent = Math.round(this.viewportTransform.zoom * 100);
        const stageStyle = this.buildStageStyle(model);
        const cabinetMainRows = this.buildCabinetMainInfoRows(
            model,
            selectedSemantic,
            selectedSemanticId,
            selectionResolution,
            endpointAliasResolution,
            crossReference,
            relatedSubjects,
        );
        const sheetSurfaceStyle: React.CSSProperties = {
            width: `${model.sheetChrome.frame.width}px`,
            height: `${model.sheetChrome.frame.height}px`,
            transform: `translate(${this.viewportTransform.offsetX}px, ${this.viewportTransform.offsetY}px) scale(${this.viewportTransform.zoom})`,
            transformOrigin: '0 0',
        };

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
                            {this.renderStageChrome(model, connectPortsSupported, cabinetMainRows)}
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
                                    {this.renderSheetChrome()}
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
                                        {model.nodes.map(node => this.renderGraphNode(node, selectedSemanticId, compatibleConnectTargetIds))}
                                    </svg>
                                </div>
                            </div>
                            {this.renderLayoutMutationPreview()}
                            {this.renderBottomDock(model, zoomPercent)}
                        </>
                    }
                </section>
            </div>
        </div>;
    }

    protected buildCabinetMainInfoRows(
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
            { key: 'source', label: 'Source', value: sourceSummary, code: true },
        ];
    }

    protected renderStageChrome(
        model: ReturnType<typeof buildAthenaGraphWorkbenchModel>,
        connectPortsSupported: boolean,
        cabinetMainRows: Array<{ key: string; label: string; value: React.ReactNode; code?: boolean }>,
    ): React.ReactNode {
        return <div className='athena-graph-workbench__overlay athena-graph-workbench__overlay--top'>
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
                        title='Cabinet Main information'
                        aria-label='Cabinet Main information'
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
            {this.infoPopoverOpen ? this.renderCabinetMainPopover(cabinetMainRows) : undefined}
        </div>;
    }

    protected renderCabinetMainPopover(
        cabinetMainRows: Array<{ key: string; label: string; value: React.ReactNode; code?: boolean }>,
    ): React.ReactNode {
        return <section
            className='athena-graph-workbench__info-popover'
            data-athena-info-popover='true'
            role='dialog'
            aria-label='Cabinet Main information'
        >
            <div className='athena-graph-workbench__info-popover-header'>
                <div>
                    <span className='athena-graph-workbench__info-popover-eyebrow'>Cabinet Main</span>
                    <h3>Projection Information</h3>
                </div>
                <button
                    className='athena-graph-workbench__tool-button'
                    type='button'
                    title='Close Cabinet Main information'
                    aria-label='Close Cabinet Main information'
                    onClick={() => this.closeInfoPopover()}
                >
                    <span className='codicon codicon-close' />
                </button>
            </div>
            <table className='athena-graph-workbench__info-table'>
                <tbody>
                    {cabinetMainRows.map(row => <tr key={row.key}>
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
                    onClick={() => this.acceptLayoutMutationPreview()}
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

    protected acceptLayoutMutationPreview(): void {
        const preview = this.layoutMutationPreview;
        const currentEditor = this.isAthenaEditor(this.editorManager.currentEditor)
            ? this.editorManager.currentEditor
            : undefined;
        if (!preview || !currentEditor) {
            return;
        }
        const documentText = currentEditor.editor.document.getText();
        const lines = documentText.split(/\r\n|\r|\n/);
        const insertionLine = Math.max(0, lines.length - 1);
        const insertionCharacter = lines[lines.length - 1]?.length ?? 0;
        const sourceEdit = buildAthenaGraphLayoutSourceEdit({
            preview,
            insertionLine,
            insertionCharacter,
        });
        this.lspEditorBridgeService.applyAuthoringSourceEdit(sourceEdit);
        this.layoutMutationPreview = undefined;
        this.scheduleRefresh();
        void this.semanticSelectionService.selectSemanticId(preview.subjectSemanticId);
        this.update();
    }

    protected rejectLayoutMutationPreview(): void {
        this.layoutMutationPreview = undefined;
        this.update();
    }

    protected renderSheetChrome(): React.ReactNode {
        return <>
            <div className='athena-graph-workbench__sheet-frame' aria-hidden='true' />
        </>;
    }

    protected renderGraphNode(
        node: AthenaGraphWorkbenchNode,
        selectedSemanticId: string | undefined,
        compatibleConnectTargetIds: Set<string>,
    ): React.ReactNode {
        const selected = selectedSemanticId === node.semanticId
            || node.electricalAnchors.some(anchor => anchor.portSemanticId === selectedSemanticId);
        const isConnectablePort = this.isConnectablePortNode(node.semanticId, node.kind);
        const isCompatibleConnectTarget = isConnectablePort && compatibleConnectTargetIds.has(node.semanticId);
        const isBlockedConnectTarget = !!this.connectPortsSource && isConnectablePort && !isCompatibleConnectTarget && node.semanticId !== this.connectPortsSource.semanticId;
        const labelClassName = [
            'athena-graph-workbench__node-label',
            `athena-graph-workbench__node-label--${node.renderVariant}`,
            selected ? 'athena-graph-workbench__node-label--selected' : '',
            isCompatibleConnectTarget ? 'athena-graph-workbench__node-label--connect-target' : '',
            isBlockedConnectTarget ? 'athena-graph-workbench__node-label--connect-blocked' : '',
        ].filter(Boolean).join(' ');
        const nodeClassName = [
            'athena-graph-workbench__node',
            `athena-graph-workbench__node--${node.kind}`,
            `athena-graph-workbench__node--${node.renderVariant}`,
            selected ? 'athena-graph-workbench__node--selected' : '',
            isCompatibleConnectTarget ? 'athena-graph-workbench__node--connect-target' : '',
            isBlockedConnectTarget ? 'athena-graph-workbench__node--connect-blocked' : '',
        ].filter(Boolean).join(' ');

        return <g
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
            return 'Applying connect preview decision';
        }
        if (this.connectPortsPending) {
            return 'Submitting connect ports request';
        }
        if (this.connectPreview) {
            return 'Review guided connect preview';
        }
        if (!this.connectPortsArmed) {
            return 'Connect ports';
        }
        if (!this.connectPortsSource) {
            return 'Select source port';
        }
        return `Select target port for ${this.connectPortsSource.label}`;
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

    protected currentCompatibleConnectTargets(): AthenaCompatibleConnectionTarget[] {
        return buildAthenaCompatibleConnectionTargets({
            knowledge: this.componentKnowledge,
            inspection: this.semanticInspection,
            sourcePortSemanticId: this.connectPortsSource?.semanticId,
        });
    }

    protected isCompatibleConnectTarget(semanticId: string): boolean {
        return this.currentCompatibleConnectTargets().some(target => target.semanticId === semanticId);
    }

    protected toggleConnectPortsMode(): void {
        if (this.connectPortsPending || this.connectApplyingDecision) {
            return;
        }
        if (this.connectPreview) {
            void this.rejectConnectPreview();
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

    protected async previewConnectPortsIntent(sourceSemanticId: string, targetSemanticId: string): Promise<void> {
        this.connectPortsPending = true;
        this.connectPreview = undefined;
        this.connectPreviewMessage = undefined;
        this.update();
        try {
            const submission = await this.lspEditorBridgeService.requestAuthoringPreview(
                buildConnectPortsPreviewRequest({
                    sourcePortId: sourceSemanticId,
                    targetPortId: targetSemanticId,
                    originDetail: `graph:${this.diagram?.activeViewId ?? 'unknown-view'}`,
                }),
            );
            this.connectPreview = submission?.preview;
            if (!submission?.preview) {
                this.connectPreviewMessage = 'Athena could not create a guided connection preview for the selected ports.';
            }
        } catch (error) {
            this.connectPreview = undefined;
            this.connectPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.connectPortsPending = false;
            this.update();
        }
    }

    protected async acceptConnectPreview(): Promise<void> {
        const preview = this.connectPreview;
        if (!preview) {
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
                    note: 'Graph connect preview accepted.',
                }),
            );
            if (!decision?.sourceEdit) {
                throw new Error('Athena accepted the connect preview but did not return a governed source edit.');
            }
            this.lspEditorBridgeService.applyAuthoringSourceEdit(decision.sourceEdit);
            this.connectPreview = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
            this.scheduleRefresh();
            if (decision.sourceEdit.suggestedSemanticId) {
                window.setTimeout(() => {
                    void this.semanticSelectionService.selectSemanticId(decision.sourceEdit!.suggestedSemanticId!).catch(error => {
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
            await this.lspEditorBridgeService.requestAuthoringDecision(
                buildAuthoringDecisionRequest({
                    previewId: preview.previewId,
                    intentId: preview.intentId,
                    decision: 'rejected',
                    note: 'Graph connect preview rejected.',
                }),
            );
            this.connectPreview = undefined;
            this.connectPortsArmed = false;
            this.connectPortsSource = undefined;
        } catch (error) {
            this.connectPreviewMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.connectApplyingDecision = false;
            this.update();
        }
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
        return kind === 'label' && semanticId.startsWith('port:');
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
        if (!this.isCompatibleConnectTarget(semanticId)) {
            this.connectPreviewMessage = `Port \`${label}\` is not a compatible target for \`${source.label}\`.`;
            this.update();
            return;
        }
        this.connectPortsSource = undefined;
        this.connectPortsArmed = false;
        this.update();
        await this.previewConnectPortsIntent(source.semanticId, semanticId);
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
            this.lastDiagramViewportKey = this.diagramViewportKey(diagram);
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
        const model = buildAthenaGraphWorkbenchModel(this.diagram);
        const node = model.nodes.find(candidate => candidate.semanticId === dragState.semanticId);
        if (node) {
            const capture = captureAthenaGraphLayoutAdjustmentIntent({
                model,
                node,
                kind: 'place',
                relation: 'near',
            });
            this.lastLayoutAdjustmentIntent = capture.accepted ? capture.intent : undefined;
            this.layoutMutationPreview = capture.accepted
                ? buildAthenaGraphLayoutMutationPreview(capture.intent)
                : undefined;
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
                this.lastDiagramViewportKey = this.diagramViewportKey(diagram);
                this.diagram = diagram;
                this.reconcileTransientSelection(diagram);
                this.revealingSelectionSemanticId = undefined;
                this.keepSelectionVisible(diagram);
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
