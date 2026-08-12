import * as React from '@theia/core/shared/react';
import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { OpenerService, open as openUri } from '@theia/core/lib/browser/opener-service';
import URI from '@theia/core/lib/common/uri';
import { AthenaConnectionReadModelPublication, AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';
import { AthenaScenePublication, EditOperationBody, EditOperationEnvelope, EditOperationResult } from './diagram/generated/types';
import { DiagramConnectionIntent, DiagramRouteIntent, DiagramSelection, KonvaDiagramAdapter } from './diagram/konva-diagram-adapter';
import { editorRulerLabels } from './diagram/editor-rulers';
import { StylePreviewFields, StylePreviewTarget } from './diagram/style-preview-session';

type AthenaPresentationOperationEvidence = {
    operation: EditOperationEnvelope;
    result: unknown;
    timestamp: string;
};

type AthenaPresentationAutomation = {
    clearEvidence(): void;
    evidence(): AthenaPresentationOperationEvidence[];
    getState(): unknown;
    getConnectionReadModel(): Promise<AthenaConnectionReadModelPublication | undefined>;
    refresh(): Promise<void>;
    executeMove(occurrenceId: string, point: { x: number; y: number }, lockAction?: 'PRESERVE' | 'LOCK' | 'UNLOCK'): Promise<AthenaPresentationOperationEvidence | undefined>;
    executeSnap(occurrenceId: string): Promise<AthenaPresentationOperationEvidence | undefined>;
    executeAlign(occurrenceIds: string[], axis: 'LEFT' | 'CENTER_X' | 'RIGHT' | 'TOP' | 'CENTER_Y' | 'BOTTOM'): Promise<AthenaPresentationOperationEvidence | undefined>;
    executeDistribute(occurrenceIds: string[], axis: 'HORIZONTAL' | 'VERTICAL'): Promise<AthenaPresentationOperationEvidence | undefined>;
    executeReconnect(connectionId: string, endpointRole: 'SOURCE' | 'SINK', replacementPortId: string): Promise<AthenaPresentationOperationEvidence | undefined>;
    executeRouteAdjust(connectionId: string, target: { kind: 'SEGMENT' | 'BEND'; ordinal: number }, point: { column: number; row: number }): Promise<AthenaPresentationOperationEvidence | undefined>;
    switchFolioPage(sheetId: string): Promise<void>;
    executeUndo(journalEntryId: string): Promise<AthenaPresentationOperationEvidence | undefined>;
    executeRedo(journalEntryId: string): Promise<AthenaPresentationOperationEvidence | undefined>;
};

type AthenaPresentationWindow = Window & {
    __athenaPresentationAutomation?: AthenaPresentationAutomation;
};

@injectable()
export class AthenaPresentationWidget extends ReactWidget {
    static readonly ID = 'athena.presentation';
    static readonly LABEL = 'Presentation';

    @inject(AthenaLspEditorBridgeService)
    protected readonly bridge: AthenaLspEditorBridgeService;
    @inject(AthenaRepositorySessionService)
    protected readonly repositorySession: AthenaRepositorySessionService;
    @inject(EditorManager)
    protected readonly editorManager: EditorManager;
    @inject(OpenerService)
    protected readonly openerService: OpenerService;
    @inject(AthenaSemanticSelectionService)
    protected readonly selectionService: AthenaSemanticSelectionService;

    protected publication: AthenaScenePublication | undefined;
    protected error: string | undefined;
    protected readonly listeners = new DisposableCollection();
    protected editorListeners = new DisposableCollection();
    protected refreshSequence = 0;
    protected canvasHost: HTMLDivElement | undefined;
    protected adapter: KonvaDiagramAdapter | undefined;
    protected selection: DiagramSelection | undefined;
    protected selectedOccurrenceIds = new Set<string>();
    protected styleTarget: 'default' | 'symbol' | 'connection' | 'label' | 'port' | 'occurrence' = 'connection';
    protected strokeColor = '#000000';
    protected strokeWidth = 1;
    protected dash = 'solid';
    protected fontSize = 1;
    protected fontWeight = 400;
    protected routeMarker: 'NONE' | 'END_ARROW' = 'NONE';
    protected portDisplay: 'HIDDEN' | 'MARKER' = 'MARKER';
    protected operationError: string | undefined;
    protected readonly operationEvidence: AthenaPresentationOperationEvidence[] = [];
    protected sheetId: string | undefined;
    protected sourceUri: string | undefined;
    protected folioPages: string[] = [];

    protected openSource = (): void => {
        if (!this.sourceUri) return;
        void openUri(this.openerService, new URI(this.sourceUri).withQuery('athenaSource=1'));
    };

    @postConstruct()
    protected init(): void {
        this.id = AthenaPresentationWidget.ID;
        this.title.label = AthenaPresentationWidget.LABEL;
        this.title.caption = AthenaPresentationWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-symbol-property';
        this.addClass('athena-presentation-widget');
        const scaleBenchmarkListener = (): void => {
            void this.runScaleBenchmark();
        };
        window.addEventListener('athena:m43-scale-benchmark', scaleBenchmarkListener);
        (window as Window & { __athenaRunScaleBenchmark?: () => Promise<void> }).__athenaRunScaleBenchmark = () => this.runScaleBenchmark();
        (window as Window & { __athenaRunM44PerformanceBenchmark?: () => Promise<void> }).__athenaRunM44PerformanceBenchmark = () => this.runM44PerformanceBenchmark();
        (window as Window & { __athenaRunM46ConnectionPerformanceBenchmark?: () => Promise<void> }).__athenaRunM46ConnectionPerformanceBenchmark = () => this.runM46ConnectionPerformanceBenchmark();
        (window as AthenaPresentationWindow).__athenaPresentationAutomation = {
            clearEvidence: () => {
                this.operationEvidence.splice(0, this.operationEvidence.length);
                this.operationError = undefined;
                this.update();
            },
            evidence: () => [...this.operationEvidence],
            getState: () => this.automationState(),
            getConnectionReadModel: () => this.bridge.requestConnectionReadModel(),
            refresh: () => this.refresh(),
            switchFolioPage: sheetId => this.switchFolioPage(sheetId),
            executeMove: async (occurrenceId, point, lockAction = 'PRESERVE') => {
                await this.moveOccurrence({ occurrenceId, point, lockAction });
                return this.latestOperationEvidence();
            },
            executeSnap: async occurrenceId => {
                const occurrence = this.occurrenceById(occurrenceId);
                await this.executePlacementOperation([occurrence.occurrenceId], {
                    kind: 'SNAP_OCCURRENCE_TO_GRID',
                    sheetId: '',
                    occurrenceId: occurrence.occurrenceId,
                    point: occurrence.placementAnchor,
                });
                return this.latestOperationEvidence();
            },
            executeAlign: async (occurrenceIds, axis) => {
                const targetIds = [...occurrenceIds].sort();
                await this.executePlacementOperation(targetIds, {
                    kind: 'ALIGN_OCCURRENCES',
                    sheetId: '',
                    occurrenceIds: targetIds,
                    axis,
                });
                return this.latestOperationEvidence();
            },
            executeDistribute: async (occurrenceIds, axis) => {
                const targetIds = [...occurrenceIds].sort();
                await this.executePlacementOperation(targetIds, {
                    kind: 'DISTRIBUTE_OCCURRENCES',
                    sheetId: '',
                    occurrenceIds: targetIds,
                    axis,
                });
                return this.latestOperationEvidence();
            },
            executeReconnect: async (connectionId, endpointRole, replacementPortId) => {
                const connection = this.connectionById(connectionId);
                await this.commitConnectionIntent({
                    kind: 'RECONNECT_CONNECTION_ENDPOINT',
                    targetIdentities: [connection.connectionId, replacementPortId],
                    sourceTrace: { traceId: connection.traceId, subjectId: connection.connectionId },
                    body: { kind: 'RECONNECT_CONNECTION_ENDPOINT', connectionId, endpointRole, replacementPortId },
                });
                return this.latestOperationEvidence();
            },
            executeRouteAdjust: async (connectionId, target, point) => {
                const connection = this.connectionById(connectionId);
                await this.commitRouteIntent({
                    targetIdentities: [connection.connectionId, connection.projectionId],
                    sourceTrace: { traceId: connection.traceId, subjectId: connection.connectionId },
                    body: {
                        kind: 'ADJUST_CONNECTION_ROUTE',
                        sheetId: this.publication?.state === 'READY' ? this.publication.scene.snapGrid.sheetId : '',
                        connectionId,
                        projectionId: connection.projectionId,
                        target,
                        point,
                    },
                });
                return this.latestOperationEvidence();
            },
            executeUndo: async journalEntryId => this.executeJournalOperation({ kind: 'UNDO', journalEntryId }),
            executeRedo: async journalEntryId => this.executeJournalOperation({ kind: 'REDO', journalEntryId }),
        };
        this.listeners.push(Disposable.create(() => {
            window.removeEventListener('athena:m43-scale-benchmark', scaleBenchmarkListener);
            delete (window as Window & { __athenaRunScaleBenchmark?: () => Promise<void> }).__athenaRunScaleBenchmark;
            delete (window as Window & { __athenaRunM44PerformanceBenchmark?: () => Promise<void> }).__athenaRunM44PerformanceBenchmark;
            delete (window as Window & { __athenaRunM46ConnectionPerformanceBenchmark?: () => Promise<void> }).__athenaRunM46ConnectionPerformanceBenchmark;
            delete (window as AthenaPresentationWindow).__athenaPresentationAutomation;
        }));
        this.listeners.push(this.repositorySession.onDidChangeState(() => void this.refresh()));
        this.listeners.push(this.selectionService.onDidChangeSelection(selection => {
            this.adapter?.selectSemanticConnection(selection?.semanticId);
        }));
        this.listeners.push(this.editorManager.onCurrentEditorChanged(widget => {
            this.bindEditor(widget);
            void this.refresh();
        }));
        this.listeners.push(Disposable.create(() => this.editorListeners.dispose()));
        this.toDispose.push(this.listeners);
        this.bindEditor(this.editorManager.currentEditor);
        void this.refresh();
    }

    configureSheet(sheetId: string | undefined, sourceUri?: string): void {
        this.sheetId = sheetId;
        this.sourceUri = sourceUri ?? this.sourceUri;
        if (sheetId) {
            this.id = `${AthenaPresentationWidget.ID}:${sheetId}`;
            const label = this.sourceUri
                ? new URI(this.sourceUri).path.toString().split(/[\\/]/).pop() || sheetId
                : sheetId.replace(/_/g, ' ');
            this.title.label = label;
            this.title.caption = label;
        }
        if (sheetId && this.repositorySession.state.lifecycle === 'ready') {
            void this.switchFolioPage(sheetId);
        }
    }

    override onActivateRequest(msg: any): void {
        super.onActivateRequest(msg);
        if (this.sheetId) void this.switchFolioPage(this.sheetId);
    }

    protected bindEditor(widget: EditorWidget | undefined): void {
        this.editorListeners.dispose();
        this.editorListeners = new DisposableCollection();
        this.listeners.push(this.editorListeners);
        if (widget?.editor.uri.toString().toLowerCase().endsWith('.athena')) {
            this.editorListeners.push(widget.editor.onDocumentContentChanged(() => void this.refresh()));
        }
    }

    protected get showFolioBar(): boolean {
        return !!this.sourceUri && new URI(this.sourceUri).path.toString().toLowerCase().endsWith('.folio.athena');
    }

    protected async refresh(): Promise<void> {
        const sequence = ++this.refreshSequence;
        if (this.repositorySession.state.lifecycle !== 'ready') {
            this.publication = undefined;
            this.error = undefined;
            this.selection = undefined;
            this.selectedOccurrenceIds.clear();
            this.adapter?.discardStylePreview();
            this.update();
            return;
        }
        try {
            this.folioPages = this.showFolioBar ? await this.bridge.requestFolioPages() : [];
            const next = await this.bridge.requestDiagramScene(this.sheetId);
            if (sequence !== this.refreshSequence) return;
            this.publication = next;
            this.error = undefined;
            this.adapter?.setPublication(next);
            this.adapter?.selectSemanticConnection(this.selectionService.selection?.semanticId);
        } catch (error) {
            if (sequence !== this.refreshSequence) return;
            this.publication = undefined;
            this.error = error instanceof Error ? error.message : String(error);
            this.selection = undefined;
        }
        this.update();
    }

    protected async switchFolioPage(sheetId: string): Promise<void> {
        const next = await this.bridge.requestDiagramScene(sheetId);
        if (!next) return;
        this.publication = next;
        this.selection = undefined;
        this.selectedOccurrenceIds.clear();
        this.adapter?.setPublication(next);
        this.update();
    }

    protected async runScaleBenchmark(): Promise<void> {
        if (!this.adapter) throw new Error('M43 scale benchmark requires an active diagram adapter.');
        try {
            const result = await this.adapter.runScaleBenchmark();
            (window as Window & { __athenaScaleBenchmarkResult?: unknown }).__athenaScaleBenchmarkResult = result;
        } catch (error) {
            (window as Window & { __athenaScaleBenchmarkError?: string }).__athenaScaleBenchmarkError = error instanceof Error ? error.message : String(error);
        }
    }

    protected async runM44PerformanceBenchmark(): Promise<void> {
        if (!this.adapter) throw new Error('M44 performance benchmark requires an active diagram adapter.');
        try {
            const result = await this.adapter.runAuthoringBenchmark();
            (window as Window & { __athenaScaleBenchmarkResult?: unknown }).__athenaScaleBenchmarkResult = result;
        } catch (error) {
            (window as Window & { __athenaScaleBenchmarkError?: string }).__athenaScaleBenchmarkError = error instanceof Error ? error.message : String(error);
        }
    }

    protected async runM46ConnectionPerformanceBenchmark(): Promise<void> {
        if (!this.adapter) throw new Error('M46 connection performance benchmark requires an active diagram adapter.');
        delete (window as Window & { __athenaM46ConnectionPerformanceResult?: unknown }).__athenaM46ConnectionPerformanceResult;
        delete (window as Window & { __athenaM46ConnectionPerformanceError?: string }).__athenaM46ConnectionPerformanceError;
        try {
            const result = await this.adapter.runConnectionPerformanceBenchmark();
            (window as Window & { __athenaM46ConnectionPerformanceResult?: unknown }).__athenaM46ConnectionPerformanceResult = result;
        } catch (error) {
            (window as Window & { __athenaM46ConnectionPerformanceError?: string }).__athenaM46ConnectionPerformanceError = error instanceof Error ? error.message : String(error);
        }
    }

    protected setCanvasHost = (host: HTMLDivElement | null): void => {
        if (host === this.canvasHost) return;
        this.adapter?.dispose();
        this.adapter = undefined;
        this.canvasHost = host ?? undefined;
        if (host) {
            this.adapter = new KonvaDiagramAdapter(host, {
                onSelection: selection => {
                    this.selection = selection;
                    if (selection?.kind === 'occurrence' && selection.occurrenceId) {
                        if (!selection.additive) this.selectedOccurrenceIds.clear();
                        if (selection.additive && this.selectedOccurrenceIds.has(selection.occurrenceId)) {
                            this.selectedOccurrenceIds.delete(selection.occurrenceId);
                        } else {
                            this.selectedOccurrenceIds.add(selection.occurrenceId);
                        }
                    } else if (selection?.kind === 'connection') {
                        if (!selection.additive) this.selectedOccurrenceIds.clear();
                    } else if (!selection) {
                        this.selectedOccurrenceIds.clear();
                    }
                    if (selection) {
                        void this.selectionService.selectSemanticId(selection.semanticId);
                    }
                    this.update();
                },
                onMove: move => void this.moveOccurrence(move),
                onConnectionIntent: intent => this.commitConnectionIntent(intent),
                onRouteIntent: intent => this.commitRouteIntent(intent),
            });
            this.adapter.setPublication(this.publication);
            this.adapter.selectSemanticConnection(this.selectionService.selection?.semanticId);
        }
    };

    protected async commitConnectionIntent(intent: DiagramConnectionIntent): Promise<boolean> {
        if (intent.kind !== 'CONNECT_PORTS' && intent.kind !== 'RECONNECT_CONNECTION_ENDPOINT') return false;
        if (!this.publication || this.publication.state !== 'READY') return false;
        const context = await this.bridge.requestPresentationEditContext(this.sheetId);
        if (!context || context.state !== 'READY' || !context.sceneId || context.engineeringWritableFiles.length === 0) {
            this.operationError = 'Engineering connection authoring context is unavailable.';
            this.update();
            return false;
        }
        const operation: EditOperationEnvelope = {
            schemaVersion: 1,
            operationId: crypto.randomUUID(),
            sceneId: this.publication.scene.sceneId,
            authorityClass: 'ENGINEERING',
            sourceRevision: context.sourceRevision,
            target: { identities: intent.targetIdentities },
            sourceTrace: intent.sourceTrace,
            requestedWritableFiles: context.engineeringWritableFiles,
            body: intent.body,
        };
        const result = await this.bridge.requestEditOperation(operation);
        this.recordOperationEvidence(operation, result);
        if (!result || result.status !== 'ACCEPTED') {
            const diagnostic = result?.status === 'REJECTED' ? result.rejection.diagnostics[0] : undefined;
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Connection edit was not accepted.';
            this.update();
            return false;
        }
        this.operationError = undefined;
        await this.refresh();
        return true;
    }

    protected async commitRouteIntent(intent: DiagramRouteIntent): Promise<boolean> {
        if (intent.body.kind !== 'ADJUST_CONNECTION_ROUTE') return false;
        if (!this.publication || this.publication.state !== 'READY') return false;
        const context = await this.bridge.requestPresentationEditContext(this.sheetId);
        if (!context || context.state !== 'READY' || !context.sceneId || context.routeWritableFiles.length === 0) {
            this.operationError = 'Connection route authoring context is unavailable.';
            this.update();
            return false;
        }
        const operation: EditOperationEnvelope = {
            schemaVersion: 1,
            operationId: crypto.randomUUID(),
            sceneId: this.publication.scene.sceneId,
            authorityClass: 'PRESENTATION',
            sourceRevision: context.sourceRevision,
            target: { identities: intent.targetIdentities },
            sourceTrace: intent.sourceTrace,
            requestedWritableFiles: context.routeWritableFiles,
            body: { ...intent.body, sheetId: context.sheetId ?? intent.body.sheetId },
        };
        const result = await this.bridge.requestEditOperation(operation);
        this.recordOperationEvidence(operation, result);
        if (!result || result.status !== 'ACCEPTED') {
            const diagnostic = result?.status === 'REJECTED' ? result.rejection.diagnostics[0] : undefined;
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Connection route edit was not accepted.';
            this.update();
            return false;
        }
        this.operationError = undefined;
        await this.refresh();
        return true;
    }

    protected override onBeforeDetach(msg: any): void {
        this.adapter?.dispose();
        this.adapter = undefined;
        this.canvasHost = undefined;
        super.onBeforeDetach(msg);
    }

    protected previewStyle = (): void => {
        if (!this.adapter || !this.publication || this.publication.state === 'UNAVAILABLE') return;
        const target = this.currentStyleTarget();
        if (!target) return;
        this.operationError = undefined;
        this.adapter.previewStyle(target, this.currentStyleFields(), this.publication.acceptedInputRevision);
        this.update();
    };

    protected discardStyle = (): void => {
        this.operationError = undefined;
        this.adapter?.discardStylePreview();
        this.update();
    };

    protected solidifyStyle = async (): Promise<void> => {
        if (!this.publication || this.publication.state !== 'READY') return;
        const target = this.currentStyleTarget();
        const sourceTrace = this.sourceTraceForTarget(target);
        if (!target || !sourceTrace) {
            this.operationError = 'Select a visible occurrence or choose a populated style role.';
            this.update();
            return;
        }
        const context = await this.bridge.requestPresentationEditContext(this.sheetId);
        if (!context || context.state !== 'READY' || !context.sheetId || !context.sceneId || context.styleWritableFiles.length === 0) {
            const diagnostic = context?.diagnostics[0];
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Style authoring context is unavailable.';
            this.update();
            return;
        }
        const fields = this.currentStyleFields();
        const operationFields = {
            ...fields,
            lineCap: fields.lineCap?.toLowerCase() as 'butt' | 'round' | 'square' | undefined,
            lineJoin: fields.lineJoin?.toLowerCase() as 'miter' | 'round' | 'bevel' | undefined,
        };
        const operation: EditOperationEnvelope = {
            schemaVersion: 1,
            operationId: crypto.randomUUID(),
            sceneId: this.publication.scene.sceneId,
            authorityClass: 'PRESENTATION',
            sourceRevision: context.sourceRevision,
            target: { identities: [target.id] },
            sourceTrace,
            requestedWritableFiles: context.styleWritableFiles,
            body: {
                kind: 'SET_STYLE',
                sheetId: context.sheetId,
                target,
                fields: operationFields,
            },
        };
        const result = await this.bridge.requestEditOperation(operation);
        this.recordOperationEvidence(operation, result);
        if (!result || result.status === 'REJECTED') {
            const diagnostic = result?.status === 'REJECTED' ? result.rejection.diagnostics[0] : undefined;
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Style edit was not accepted.';
            this.update();
            return;
        }
        this.operationError = undefined;
        await this.refresh();
    };

    protected moveOccurrence = async (move: { occurrenceId: string; point: { x: number; y: number }; lockAction: 'PRESERVE' | 'LOCK' | 'UNLOCK' }): Promise<void> => {
        if (!this.publication || this.publication.state !== 'READY') return;
        const occurrence = this.publication.scene.occurrences.find(candidate => candidate.occurrenceId === move.occurrenceId);
        const trace = occurrence && this.publication.scene.traces.find(candidate => candidate.traceId === occurrence.traceId);
        const subjectId = trace?.origins.find(origin => origin.primary)?.subjectId ?? occurrence?.subjectId;
        if (!occurrence || !subjectId) {
            this.operationError = 'Selected occurrence cannot be traced to engineering source.';
            this.update();
            return;
        }
        const context = await this.bridge.requestPresentationEditContext(this.sheetId);
        if (!context || context.state !== 'READY' || !context.sheetId || !context.sceneId || context.placementWritableFiles.length === 0) {
            const diagnostic = context?.diagnostics[0];
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Placement authoring context is unavailable.';
            this.update();
            return;
        }
        const operation: EditOperationEnvelope = {
            schemaVersion: 1,
            operationId: crypto.randomUUID(),
            sceneId: this.publication.scene.sceneId,
            authorityClass: 'PRESENTATION',
            sourceRevision: context.sourceRevision,
            target: { identities: [move.occurrenceId] },
            sourceTrace: { traceId: occurrence.traceId, subjectId },
            requestedWritableFiles: context.placementWritableFiles,
            body: {
                kind: 'MOVE_OCCURRENCE',
                sheetId: context.sheetId,
                occurrenceId: move.occurrenceId,
                point: move.point,
                lockAction: move.lockAction,
            },
        };
        const result = await this.bridge.requestEditOperation(operation);
        this.recordOperationEvidence(operation, result);
        if (!result || result.status === 'REJECTED') {
            const diagnostic = result?.status === 'REJECTED' ? result.rejection.diagnostics[0] : undefined;
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Move was not accepted.';
            await this.refresh();
            return;
        }
        this.operationError = undefined;
        await this.refresh();
    };

    protected snapSelectedOccurrence = (): void => {
        const occurrence = this.selectedOccurrence();
        if (!occurrence) return;
        void this.executePlacementOperation([occurrence.occurrenceId], {
            kind: 'SNAP_OCCURRENCE_TO_GRID',
            sheetId: '',
            occurrenceId: occurrence.occurrenceId,
            point: occurrence.placementAnchor,
        });
    };

    protected lockSelectedOccurrence = (action: 'LOCK' | 'UNLOCK'): void => {
        const occurrence = this.selectedOccurrence();
        if (!occurrence) return;
        void this.moveOccurrence({
            occurrenceId: occurrence.occurrenceId,
            point: occurrence.placementAnchor,
            lockAction: action,
        });
    };

    protected selectedOccurrence() {
        const occurrenceId = this.selection?.occurrenceId;
        const occurrence = this.publication?.state === 'READY'
            ? this.publication.scene.occurrences.find(candidate => candidate.occurrenceId === occurrenceId)
            : undefined;
        if (occurrence) return occurrence;
        this.operationError = 'Select one visible occurrence.';
        this.update();
        return undefined;
    }

    protected alignOccurrences = (axis: 'LEFT' | 'CENTER_X' | 'RIGHT' | 'TOP' | 'CENTER_Y' | 'BOTTOM'): void => {
        const occurrenceIds = [...this.selectedOccurrenceIds].sort();
        if (occurrenceIds.length < 2) {
            this.operationError = 'Select at least two occurrences.';
            this.update();
            return;
        }
        void this.executePlacementOperation(occurrenceIds, {
            kind: 'ALIGN_OCCURRENCES',
            sheetId: '',
            occurrenceIds,
            axis,
        });
    };

    protected distributeOccurrences = (axis: 'HORIZONTAL' | 'VERTICAL'): void => {
        const occurrenceIds = [...this.selectedOccurrenceIds].sort();
        if (occurrenceIds.length < 3) {
            this.operationError = 'Select at least three occurrences.';
            this.update();
            return;
        }
        void this.executePlacementOperation(occurrenceIds, {
            kind: 'DISTRIBUTE_OCCURRENCES',
            sheetId: '',
            occurrenceIds,
            axis,
        });
    };

    protected async executePlacementOperation(
        targetIds: string[],
        body: EditOperationEnvelope['body'],
    ): Promise<void> {
        if (!this.publication || this.publication.state !== 'READY') return;
        const occurrence = this.publication.scene.occurrences.find(candidate => candidate.occurrenceId === targetIds[0]);
        const trace = occurrence && this.publication.scene.traces.find(candidate => candidate.traceId === occurrence.traceId);
        const subjectId = trace?.origins.find(origin => origin.primary)?.subjectId ?? occurrence?.subjectId;
        if (!occurrence || !subjectId) {
            this.operationError = 'Selected occurrences cannot be traced to engineering source.';
            this.update();
            return;
        }
        const context = await this.bridge.requestPresentationEditContext(this.sheetId);
        if (!context || context.state !== 'READY' || !context.sheetId || !context.sceneId || context.placementWritableFiles.length === 0) {
            const diagnostic = context?.diagnostics[0];
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Placement authoring context is unavailable.';
            this.update();
            return;
        }
        const operation: EditOperationEnvelope = {
            schemaVersion: 1,
            operationId: crypto.randomUUID(),
            sceneId: this.publication.scene.sceneId,
            authorityClass: 'PRESENTATION',
            sourceRevision: context.sourceRevision,
            target: { identities: targetIds },
            sourceTrace: { traceId: occurrence.traceId, subjectId },
            requestedWritableFiles: context.placementWritableFiles,
            body: { ...body, sheetId: context.sheetId } as EditOperationEnvelope['body'],
        };
        const result = await this.bridge.requestEditOperation(operation);
        this.recordOperationEvidence(operation, result);
        if (!result || result.status === 'REJECTED') {
            const diagnostic = result?.status === 'REJECTED' ? result.rejection.diagnostics[0] : undefined;
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : 'Presentation edit was not accepted.';
            await this.refresh();
            return;
        }
        this.operationError = undefined;
        await this.refresh();
    }

    protected recordOperationEvidence(operation: EditOperationEnvelope, result: unknown): void {
        this.operationEvidence.push({ operation, result, timestamp: new Date().toISOString() });
    }

    protected latestOperationEvidence(): AthenaPresentationOperationEvidence | undefined {
        return this.operationEvidence[this.operationEvidence.length - 1];
    }

    protected executeJournalOperation = async (body: Extract<EditOperationBody, { kind: 'UNDO' | 'REDO' }>): Promise<AthenaPresentationOperationEvidence | undefined> => {
        const previous = [...this.operationEvidence].reverse().find(evidence => {
            const result = evidence.result as EditOperationResult | undefined;
            return result?.status === 'ACCEPTED' && result.acceptance.journalEntryId === body.journalEntryId;
        });
        const context = await this.bridge.requestPresentationEditContext(this.sheetId);
        if (!previous || !context || context.state !== 'READY' || !context.sceneId) return undefined;
        const accepted = previous.result as Extract<EditOperationResult, { status: 'ACCEPTED' }>;
        const operation: EditOperationEnvelope = {
            schemaVersion: 1,
            operationId: crypto.randomUUID(),
            sceneId: context.sceneId as EditOperationEnvelope['sceneId'],
            authorityClass: 'JOURNAL',
            sourceRevision: context.sourceRevision,
            target: { identities: [body.journalEntryId] },
            sourceTrace: previous.operation.sourceTrace,
            requestedWritableFiles: accepted.acceptance.acceptedPatchSet.writableFiles,
            body,
        };
        const result = await this.bridge.requestEditOperation(operation);
        this.recordOperationEvidence(operation, result);
        if (!result || result.status === 'REJECTED') {
            const diagnostic = result?.status === 'REJECTED' ? result.rejection.diagnostics[0] : undefined;
            this.operationError = diagnostic ? `${diagnostic.problem} ${diagnostic.correction}` : `${body.kind} was not accepted.`;
        } else {
            this.operationError = undefined;
        }
        await this.refresh();
        return this.latestOperationEvidence();
    };

    protected undoLatest = (): void => {
        const entry = [...this.operationEvidence].reverse().find(evidence => {
            const result = evidence.result as EditOperationResult | undefined;
            return result?.status === 'ACCEPTED' && evidence.operation.body.kind !== 'UNDO' && evidence.operation.body.kind !== 'REDO';
        });
        const result = entry?.result as Extract<EditOperationResult, { status: 'ACCEPTED' }> | undefined;
        if (result) void this.executeJournalOperation({ kind: 'UNDO', journalEntryId: result.acceptance.journalEntryId });
    };

    protected occurrenceById(occurrenceId: string) {
        const occurrence = this.publication?.state === 'READY'
            ? this.publication.scene.occurrences.find(candidate => candidate.occurrenceId === occurrenceId)
            : undefined;
        if (!occurrence) throw new Error(`Occurrence ${occurrenceId} is unavailable.`);
        return occurrence;
    }

    protected connectionById(connectionId: string) {
        if (!this.publication || this.publication.state !== 'READY') throw new Error('Accepted Connection Scene is unavailable.');
        const connection = this.publication.scene.connections.find(candidate => candidate.connectionId === connectionId);
        if (!connection) {
            throw new Error(`Connection '${connectionId}' is unavailable.`);
        }
        return connection;
    }

    protected automationState(): unknown {
        if (!this.publication || this.publication.state !== 'READY') {
            return { publicationState: this.publication?.state ?? 'UNAVAILABLE' };
        }
        const scene = this.publication.scene;
        return {
            publicationState: this.publication.state,
            sceneId: scene.sceneId,
            sceneDigest: scene.sceneDigest,
            inputRevision: scene.inputRevision,
            acceptedInputRevision: this.publication.acceptedInputRevision,
            sheetId: scene.snapGrid.sheetId,
            occurrences: scene.occurrences.map(occurrence => ({
                occurrenceId: occurrence.occurrenceId,
                subjectId: occurrence.subjectId,
                traceId: occurrence.traceId,
                placementAnchor: occurrence.placementAnchor,
                bounds: occurrence.bounds,
                labels: occurrence.labels.map(label => label.text),
            })),
            ports: scene.occurrences.flatMap(occurrence => occurrence.ports.map(port => ({
                occurrenceId: occurrence.occurrenceId,
                anchorId: port.anchorId,
                semanticPortId: port.semanticPortId,
                direction: port.direction,
                traceId: port.traceId,
                point: port.point,
            }))),
            connections: scene.connections.map(connection => ({
                connectionId: connection.connectionId,
                projectionId: connection.projectionId,
                sourceAnchorId: connection.sourceAnchorId,
                targetAnchorId: connection.targetAnchorId,
                traceId: connection.traceId,
                segments: connection.segments,
                markers: connection.markers,
                annotations: connection.annotations,
            })),
        };
    }

    protected currentStyleTarget(): StylePreviewTarget | undefined {
        if (this.styleTarget === 'occurrence') {
            return this.selection?.occurrenceId ? { kind: 'OCCURRENCE', id: this.selection.occurrenceId } : undefined;
        }
        return { kind: 'ROLE', id: this.styleTarget };
    }

    protected currentStyleFields(): StylePreviewFields {
        return {
            strokeRgba: `${this.strokeColor}ff`,
            strokeWidth: this.strokeWidth,
            dash: this.dash === 'solid' ? [] : this.dash.split(',').map(Number),
            fontSize: this.fontSize,
            fontWeight: this.fontWeight,
            routeMarker: this.routeMarker,
            portDisplay: this.portDisplay,
        };
    }

    protected sourceTraceForTarget(target: StylePreviewTarget | undefined): { traceId: string; subjectId: string } | undefined {
        if (!target || !this.publication || this.publication.state === 'UNAVAILABLE') return undefined;
        const scene = this.publication.scene;
        const traceId = target.kind === 'OCCURRENCE'
            ? this.selection?.traceId
            : target.id === 'connection' ? scene.connections[0]?.traceId
                : target.id === 'symbol' ? scene.occurrences[0]?.traceId
                    : target.id === 'label' ? scene.occurrences.flatMap(occurrence => occurrence.labels)[0]?.traceId
                        : target.id === 'port' ? scene.occurrences.flatMap(occurrence => occurrence.ports)[0]?.traceId
                            : scene.decorations[0]?.traceId;
        if (!traceId) return undefined;
        const trace = scene.traces.find(candidate => candidate.traceId === traceId);
        const subjectId = trace?.origins.find(origin => origin.primary)?.subjectId ?? trace?.origins[0]?.subjectId;
        return subjectId ? { traceId, subjectId } : undefined;
    }

    protected render(): React.ReactNode {
        const documentTitle = this.title.label || AthenaPresentationWidget.LABEL;
        if (this.error) return <section className='athena-presentation__empty'><h2>{documentTitle}</h2><p>{this.error}</p></section>;
        if (!this.publication) return <section className='athena-presentation__empty'><h2>{documentTitle}</h2><p>Waiting for a canonical scene publication.</p></section>;
        if (this.publication.state === 'UNAVAILABLE') {
            return <section className='athena-presentation__empty'><h2>{documentTitle}</h2>{this.publication.diagnostics.map(d => <p key={`${d.code}:${d.subject}`}>{d.problem} {d.correction}</p>)}</section>;
        }
        const scene = this.publication.scene;
        const rulerLabels = editorRulerLabels(scene.plotFrame.rows, scene.plotFrame.columns);
        return <section className='athena-presentation__canvas-shell' data-publication-state={this.publication.state} data-scene-id={scene.sceneId} data-scene-digest={scene.sceneDigest} data-frame-rows={scene.plotFrame.rows} data-frame-columns={scene.plotFrame.columns} data-occurrence-count={scene.occurrences.length} data-connection-count={scene.connections.length}>
            {this.showFolioBar ? <nav className='athena-presentation__folio-bar' aria-label='Folio pages'>
                {this.folioPages.map(page => <button type='button' className={page === this.sheetId ? 'is-active' : ''} key={page} onClick={() => void this.switchFolioPage(page)}>{page}</button>)}
            </nav> : undefined}
            <div className='athena-presentation__style-bar' aria-label='Presentation commands'>
                <button type='button' title='Open raw source' aria-label='Open raw source' onClick={this.openSource}><span className='codicon codicon-code' /></button>
                <select aria-label='Style target' title='Style target' value={this.styleTarget} onChange={event => { this.discardStyle(); this.styleTarget = event.currentTarget.value as typeof this.styleTarget; this.update(); }}>
                    <option value='connection'>Connection</option><option value='symbol'>Symbol</option><option value='label'>Label</option><option value='port'>Port</option><option value='default'>Sheet</option>
                    <option value='occurrence' disabled={!this.selection?.occurrenceId}>Selected</option>
                </select>
                <input aria-label='Color' title='Color' type='color' value={this.strokeColor} onChange={event => { this.strokeColor = event.currentTarget.value; this.previewStyle(); }} />
                <input aria-label='Line width' title='Line width' type='number' min={1} max={12} value={this.strokeWidth} onChange={event => { this.strokeWidth = Math.max(1, Number(event.currentTarget.value)); this.previewStyle(); }} />
                <select aria-label='Dash' title='Dash' value={this.dash} onChange={event => { this.dash = event.currentTarget.value; this.previewStyle(); }}><option value='solid'>Solid</option><option value='6,2'>Dash</option><option value='2,2'>Dot</option></select>
                <input aria-label='Font size' title='Font size' type='number' min={1} max={24} value={this.fontSize} onChange={event => { this.fontSize = Math.max(1, Number(event.currentTarget.value)); this.previewStyle(); }} />
                <select aria-label='Font weight' title='Font weight' value={this.fontWeight} onChange={event => { this.fontWeight = Number(event.currentTarget.value); this.previewStyle(); }}><option value={400}>Regular</option><option value={600}>Bold</option></select>
                <select aria-label='Route marker' title='Route marker' value={this.routeMarker} onChange={event => { this.routeMarker = event.currentTarget.value as typeof this.routeMarker; this.previewStyle(); }}><option value='NONE'>No marker</option><option value='END_ARROW'>End arrow</option></select>
                <select aria-label='Port display' title='Port display' value={this.portDisplay} onChange={event => { this.portDisplay = event.currentTarget.value as typeof this.portDisplay; this.previewStyle(); }}><option value='HIDDEN'>Ports off</option><option value='MARKER'>Connection points</option></select>
                <button type='button' title='Preview style' aria-label='Preview style' onClick={this.previewStyle}><span className='codicon codicon-eye' /></button>
                <button type='button' title='Reset preview' aria-label='Reset preview' onClick={this.discardStyle}><span className='codicon codicon-discard' /></button>
                <button type='button' title='Solidify style' aria-label='Solidify style' onClick={() => void this.solidifyStyle()}><span className='codicon codicon-save' /></button>
                <button type='button' title='Snap selected occurrence' aria-label='Snap selected occurrence' onClick={this.snapSelectedOccurrence}><span className='codicon codicon-magnet' /></button>
                <button type='button' title='Lock selected occurrence' aria-label='Lock selected occurrence' onClick={() => this.lockSelectedOccurrence('LOCK')}><span className='codicon codicon-lock' /></button>
                <button type='button' title='Unlock selected occurrence' aria-label='Unlock selected occurrence' onClick={() => this.lockSelectedOccurrence('UNLOCK')}><span className='codicon codicon-unlock' /></button>
                <button type='button' title='Align left' aria-label='Align left' onClick={() => this.alignOccurrences('LEFT')}><span className='codicon codicon-arrow-left' /></button>
                <button type='button' title='Align horizontal centers' aria-label='Align horizontal centers' onClick={() => this.alignOccurrences('CENTER_X')}><span className='codicon codicon-layout-centered' /></button>
                <button type='button' title='Align right' aria-label='Align right' onClick={() => this.alignOccurrences('RIGHT')}><span className='codicon codicon-arrow-right' /></button>
                <button type='button' title='Align top' aria-label='Align top' onClick={() => this.alignOccurrences('TOP')}><span className='codicon codicon-arrow-up' /></button>
                <button type='button' title='Align vertical centers' aria-label='Align vertical centers' onClick={() => this.alignOccurrences('CENTER_Y')}><span className='codicon codicon-layout-centered' /></button>
                <button type='button' title='Align bottom' aria-label='Align bottom' onClick={() => this.alignOccurrences('BOTTOM')}><span className='codicon codicon-arrow-down' /></button>
                <button type='button' title='Distribute horizontally' aria-label='Distribute horizontally' onClick={() => this.distributeOccurrences('HORIZONTAL')}><span className='codicon codicon-split-horizontal' /></button>
                <button type='button' title='Distribute vertically' aria-label='Distribute vertically' onClick={() => this.distributeOccurrences('VERTICAL')}><span className='codicon codicon-split-vertical' /></button>
                <button type='button' title='Undo accepted edit' aria-label='Undo accepted edit' onClick={this.undoLatest}><span className='codicon codicon-history' /></button>
            </div>
            <div className='athena-presentation__canvas-viewport'>
                <div className='athena-presentation__document-frame'>
                    <div className='athena-presentation__ruler-corner' aria-hidden='true' />
                    <div className='athena-presentation__ruler athena-presentation__ruler--columns' aria-label='Column coordinates'>
                        {rulerLabels.columns.map(label => <div className='athena-presentation__ruler-cell' key={label}>{label}</div>)}
                    </div>
                    <div className='athena-presentation__ruler athena-presentation__ruler--rows' aria-label='Row coordinates'>
                        {rulerLabels.rows.slice(0, 8).map(label => <div className='athena-presentation__ruler-cell' key={label}>{label}</div>)}
                    </div>
                    <div ref={this.setCanvasHost} className='athena-presentation__canvas-host' aria-label='Engineering document canvas' />
                </div>
            </div>
            <footer className='athena-presentation__status-bar' aria-live='polite'>
                {this.operationError || `${scene.snapGrid.sheetId.replace(/_/g, ' ')} page | ${scene.occurrences.length} symbols | ${scene.connections.length} connections`}
            </footer>
        </section>;
    }
}
