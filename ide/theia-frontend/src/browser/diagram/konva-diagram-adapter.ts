import Konva from 'konva';
import {
    AthenaDiagramScene,
    AthenaScenePublication,
    AssetBundle,
    ResolvedStyle,
    SceneAsset,
    SceneBounds,
    SceneConnection,
    SceneConnectionMarker,
    SceneOccurrence,
    ScenePort,
    EditOperationBody,
} from './generated/types';
import { validateAthenaScenePublication } from './generated/validators';
import { buildAuthoringBenchmarkPublication, buildScaleBenchmarkPublication, type ScaleBenchmarkResult } from './scale-benchmark';
import {
    buildConnectionPerformancePublication,
    CONNECTION_PERFORMANCE_PROFILE_REVISION,
    connectionBounds,
    evaluateConnectionPerformance,
    planConnectionInvalidation,
    type ConnectionPerformanceEvidence,
    type ConnectionPerformanceEvaluation,
    type ConnectionPerformanceMeasurement,
} from './connection-performance';
import { SceneSpatialIndex } from './scene-spatial-index';
import { StylePreviewFields, StylePreviewSession, StylePreviewTarget } from './style-preview-session';
import { snapSheetPoint } from './grid-snap';

const PORT_MARKER_RADIUS_PX = 1.75;
const PORT_HIT_DIAMETER_PX = 12;
const CONNECTION_HIT_WIDTH_PX = 12;
const CONNECTION_SELECTION_WIDTH_PX = 3;
const JUNCTION_RADIUS_PX = 1.75;
const CROSSING_BRIDGE_RADIUS_PX = 4;
const INTERRUPTION_RADIUS_PX = 2.25;
const CONNECTION_ANNOTATION_FONT_PX = 9;
const ROUTE_ARROW_SIZE_PX = 5;
const CONNECTION_VECTOR_CHUNK_SIZE = 200;

export type DiagramSelection = {
    kind: 'occurrence' | 'port' | 'connection';
    id: string;
    semanticId: string;
    traceId: string;
    occurrenceId?: string;
    connectionId?: string;
    additive?: boolean;
};

export type DiagramConnectionIntent = {
    kind: 'CONNECT_PORTS' | 'RECONNECT_CONNECTION_ENDPOINT';
    targetIdentities: string[];
    sourceTrace: { traceId: string; subjectId: string };
    body: Extract<EditOperationBody, { kind: 'CONNECT_PORTS' | 'RECONNECT_CONNECTION_ENDPOINT' }>;
};

export type DiagramRouteIntent = {
    targetIdentities: string[];
    sourceTrace: { traceId: string; subjectId: string };
    body: Extract<EditOperationBody, { kind: 'ADJUST_CONNECTION_ROUTE' }>;
};

export type DiagramAdapterOptions = {
    onSelection?: (selection: DiagramSelection | undefined) => void;
    onMove?: (move: { occurrenceId: string; point: { x: number; y: number }; lockAction: 'PRESERVE' | 'LOCK' | 'UNLOCK' }) => void;
    onConnectionIntent?: (intent: DiagramConnectionIntent) => Promise<boolean>;
    onRouteIntent?: (intent: DiagramRouteIntent) => Promise<boolean>;
};

type ScenePortIdentity = ScenePort & { anchorId: string; semanticPortId: string };
type PortCandidate = ScenePortIdentity & { occurrenceId: string };
type SceneConnectionIdentity = SceneConnection & { projectionId: string };
type ConnectionPaintRecord = { group: Konva.Group; visualGroup: Konva.Group; connection: SceneConnection; styleKey: string; paintCount: number; chunkIndex: number };
type IndexedSceneConnection = { bounds: SceneBounds; connection: SceneConnection };

export type ConnectionPerformanceBenchmarkResult = ConnectionPerformanceEvidence & {
    fixtureDigest: string;
    acceptedInputRevision: string;
    evaluation: ConnectionPerformanceEvaluation;
};

/** One imperative Konva owner for the canonical Presentation publication. */
export class KonvaDiagramAdapter {
    private readonly stage: Konva.Stage;
    private readonly pageLayer = new Konva.Layer({ listening: false });
    private readonly sceneLayer = new Konva.Layer();
    private readonly routeGroup = new Konva.Group();
    private readonly routeHitGroup = new Konva.Group();
    private readonly assetGroup = new Konva.Group({ listening: false });
    private readonly contentGroup = new Konva.Group();
    private readonly interactionLayer = new Konva.Layer();
    private readonly objectUrls = new Set<string>();
    private readonly loadedImages = new Map<string, HTMLImageElement>();
    private readonly onSelection?: (selection: DiagramSelection | undefined) => void;
    private readonly onMove?: DiagramAdapterOptions['onMove'];
    private readonly onConnectionIntent?: DiagramAdapterOptions['onConnectionIntent'];
    private readonly onRouteIntent?: DiagramAdapterOptions['onRouteIntent'];
    private publication: AthenaScenePublication | undefined;
    private scene: AthenaDiagramScene | undefined;
    private selectedId: string | undefined;
    private selectedConnectionId: string | undefined;
    private readonly selectedOccurrenceIds = new Set<string>();
    private loadedRevision: string | undefined;
    private commandsEnabled = false;
    private resizeObserver: ResizeObserver | undefined;
    private fitScale = 1;
    private disposed = false;
    private visiblePaintElements = 0;
    private suppressExternalSelection = false;
    private lastRenderedViewport: { x: number; y: number; width: number; height: number } | undefined;
    private occurrenceIndex: SceneSpatialIndex<SceneOccurrence> | undefined;
    private connectionIndex: SceneSpatialIndex<IndexedSceneConnection> | undefined;
    private readonly connectionPaintById = new Map<string, ConnectionPaintRecord>();
    private readonly connectionChunkByIndex = new Map<number, Konva.Group>();
    private readonly dirtyConnectionChunks = new Set<number>();
    private scaleBenchmarkOperationIndex = 0;
    private benchmarking = false;
    private viewportTransformActive = false;
    private readonly stylePreview = new StylePreviewSession();
    private readonly previewPaintByOccurrence = new Map<string, Konva.Node[]>();
    private readonly selectionPreviewPaintByOccurrence = new Map<string, Konva.Node[]>();
    private activeDraggedOccurrenceId: string | undefined;
    private pendingPortCandidate: PortCandidate | undefined;
    private routePreview: { connectionId: string; target: { kind: 'SEGMENT' | 'BEND'; ordinal: number }; delta: { x: number; y: number } } | undefined;
    private activeRouteDrag: { connection: SceneConnection; target: { kind: 'SEGMENT' | 'BEND'; ordinal: number }; origin: { x: number; y: number }; node: Konva.Node } | undefined;
    private viewportTransformEndTimer: number | undefined;

    constructor(private readonly host: HTMLDivElement, options: DiagramAdapterOptions = {}) {
        this.onSelection = options.onSelection;
        this.onMove = options.onMove;
        this.onConnectionIntent = options.onConnectionIntent;
        this.onRouteIntent = options.onRouteIntent;
        host.tabIndex = 0;
        host.setAttribute('role', 'application');
        host.addEventListener('keydown', this.handleKeyDown);
        this.stage = new Konva.Stage({ container: host, width: 1, height: 1, draggable: true });
        this.sceneLayer.add(this.routeGroup, this.routeHitGroup, this.assetGroup, this.contentGroup);
        this.stage.add(this.pageLayer, this.sceneLayer, this.interactionLayer);
        this.stage.on('click tap', event => this.handleSelection(event.target, event.evt.ctrlKey || event.evt.metaKey));
        this.stage.on('wheel', event => {
            event.evt.preventDefault();
            const pointer = this.stage.getPointerPosition();
            if (!pointer) return;
            this.beginViewportTransform();
            const oldScale = this.stage.scaleX() || 1;
            const direction = event.evt.deltaY > 0 ? -1 : 1;
            const nextScale = Math.max(this.fitScale * 0.5, Math.min(this.fitScale * 8, oldScale * (direction > 0 ? 1.08 : 0.92)));
            const logical = { x: (pointer.x - this.stage.x()) / oldScale, y: (pointer.y - this.stage.y()) / oldScale };
            this.stage.scale({ x: nextScale, y: nextScale });
            this.stage.position({ x: pointer.x - logical.x * nextScale, y: pointer.y - logical.y * nextScale });
            this.maybeRedrawVisibleContent();
            this.stage.batchDraw();
            this.scheduleViewportTransformEnd();
        });
        this.stage.on('dragstart', event => {
            if (event.target === this.stage) this.beginViewportTransform();
        });
        this.stage.on('dragmove', () => {
            this.maybeRedrawVisibleContent();
            this.stage.batchDraw();
        });
        this.stage.on('dragend', event => {
            this.maybeRedrawVisibleContent();
            if (event.target === this.stage) this.endViewportTransform();
            this.stage.batchDraw();
        });
        this.resizeObserver = new ResizeObserver(() => this.resize());
        this.resizeObserver.observe(host);
        this.resize();
    }

    setPublication(publication: AthenaScenePublication | undefined): void {
        if (this.disposed) return;
        delete this.host.dataset.assetError;
        if (publication && !validateAthenaScenePublication(publication)) {
            this.clear();
            return;
        }
        if (publication && publication.state !== 'UNAVAILABLE' && this.publication && this.publication.state !== 'UNAVAILABLE' && publication.acceptedInputRevision !== this.publication.acceptedInputRevision) {
            this.revokeAssets();
        }
        this.stylePreview.clearForPublication(publication);
        this.publication = publication;
        this.scene = publication && publication.state !== 'UNAVAILABLE' ? publication.scene : undefined;
        this.host.dataset.expectedAssetCount = String(this.scene?.assets.length ?? 0);
        this.host.dataset.loadedAssetCount = String(
            this.scene && this.loadedRevision === this.scene.inputRevision
                ? this.scene.assets.filter(asset => this.loadedImages.has(asset.assetId)).length
                : 0,
        );
        this.occurrenceIndex = this.scene ? new SceneSpatialIndex(this.scene.occurrences) : undefined;
        this.rebuildConnectionIndex();
        this.commandsEnabled = publication?.state === 'READY';
        this.selectedId = undefined;
        this.selectedConnectionId = undefined;
        this.selectedOccurrenceIds.clear();
        this.activeDraggedOccurrenceId = undefined;
        this.pendingPortCandidate = undefined;
        this.routePreview = undefined;
        this.activeRouteDrag = undefined;
        this.draw();
    }

    previewStyle(target: StylePreviewTarget, fields: StylePreviewFields, acceptedInputRevision: string): void {
        this.stylePreview.preview(target, fields, acceptedInputRevision);
        this.draw();
    }

    discardStylePreview(): void {
        this.stylePreview.discard();
        this.draw();
    }

    selectSemanticConnection(connectionId: string | undefined): void {
        const matchingConnections = connectionId
            ? this.scene?.connections.filter(connection => connection.connectionId === connectionId).length ?? 0
            : 0;
        this.selectedConnectionId = matchingConnections > 0 ? connectionId : undefined;
        this.host.dataset.selectedConnectionProjectionCount = String(matchingConnections);
        this.renderSelectionOverlay();
    }

    resize(): void {
        if (this.disposed || this.benchmarking) return;
        const width = Math.max(1, this.host.clientWidth);
        const height = Math.max(1, this.host.clientHeight);
        this.stage.size({ width, height });
        if (this.scene) this.applyFit(this.scene);
        this.redrawVisibleContent();
        this.stage.batchDraw();
    }

    clear(): void {
        this.stylePreview.discard();
        this.publication = undefined;
        this.scene = undefined;
        this.occurrenceIndex = undefined;
        this.connectionIndex = undefined;
        this.selectedId = undefined;
        this.selectedConnectionId = undefined;
        this.selectedOccurrenceIds.clear();
        this.loadedRevision = undefined;
        this.commandsEnabled = false;
        this.activeDraggedOccurrenceId = undefined;
        this.pendingPortCandidate = undefined;
        this.routePreview = undefined;
        this.activeRouteDrag = undefined;
        this.host.dataset.expectedAssetCount = '0';
        this.host.dataset.loadedAssetCount = '0';
        this.previewPaintByOccurrence.clear();
        this.selectionPreviewPaintByOccurrence.clear();
        this.revokeAssets();
        this.pageLayer.destroyChildren();
        this.routeGroup.clearCache();
        this.routeGroup.destroyChildren();
        this.routeHitGroup.destroyChildren();
        this.connectionPaintById.clear();
        this.connectionChunkByIndex.clear();
        this.dirtyConnectionChunks.clear();
        this.assetGroup.destroyChildren();
        this.contentGroup.destroyChildren();
        this.interactionLayer.destroyChildren();
        this.previewPaintByOccurrence.clear();
        this.selectionPreviewPaintByOccurrence.clear();
        this.visiblePaintElements = 0;
        this.lastRenderedViewport = undefined;
        this.stage.batchDraw();
        this.onSelection?.(undefined);
    }

    dispose(): void {
        if (this.disposed) return;
        this.disposed = true;
        this.resizeObserver?.disconnect();
        this.host.removeEventListener('keydown', this.handleKeyDown);
        this.revokeAssets();
        this.stage.destroy();
    }

    private draw(): void {
        const scene = this.scene;
        if (!scene) {
            this.clearLayers();
            return;
        }
        this.clearLayers();
        this.applyFit(scene);
        const styles = new Map(scene.styles.map(style => [style.styleId, style]));
        const traces = new Map(scene.traces.map(trace => [trace.traceId, trace]));
        const page = scene.page.drawingBounds;
        this.pageLayer.add(new Konva.Rect({ x: page.x, y: page.y, width: page.width, height: page.height, fill: '#ffffff', listening: false }));
        this.redrawVisibleContent(styles, traces);
        void this.loadAssets(scene);
    }

    private redrawVisibleContent(
        styles = this.scene ? new Map(this.scene.styles.map(style => [style.styleId, style])) : new Map(),
        traces = this.scene ? new Map(this.scene.traces.map(trace => [trace.traceId, trace])) : new Map(),
    ): void {
        const scene = this.scene;
        if (!scene) return;
        this.assetGroup.destroyChildren();
        this.contentGroup.destroyChildren();
        this.interactionLayer.destroyChildren();
        const viewport = this.visibleSceneBounds();
        let paintCount = this.syncVisibleConnections(viewport, styles);
        if (scene.occurrences.length >= 50_000 && scene.occurrences.every(occurrence => !occurrence.assetId && occurrence.ports.length === 0 && occurrence.labels.length === 0)) {
            paintCount += this.drawDenseOccurrenceBatch(this.visibleOccurrences(scene, viewport), styles);
        } else {
            for (const occurrence of this.visibleOccurrences(scene, viewport)) {
                const occurrenceStyle = this.stylePreview.resolve(styles.get(occurrence.styleId), 'symbol', occurrence.occurrenceId);
                this.drawOccurrenceAsset(occurrence, occurrenceStyle);
                paintCount += this.drawOccurrence(occurrence, styles, traces);
            }
        }
        this.visiblePaintElements = paintCount;
        this.lastRenderedViewport = viewport;
        this.contentGroup.moveToTop();
        this.interactionLayer.moveToTop();
        this.renderSelectionOverlay();
        this.stage.batchDraw();
    }

    private syncVisibleConnections(
        viewport: SceneBounds,
        styles: Map<string, ResolvedStyle>,
    ): number {
        const scene = this.scene;
        if (!scene) return 0;
        const visible = this.connectionIndex?.query(viewport).map(entry => entry.connection)
            ?? scene.connections.filter(connection => this.boundsIntersect(connectionBounds(connection), viewport));
        const visibleIds = new Set(visible.map(connection => connection.connectionId));
        for (const [connectionId, record] of this.connectionPaintById) {
            if (visibleIds.has(connectionId)) continue;
            record.group.destroy();
            record.visualGroup.destroy();
            this.connectionPaintById.delete(connectionId);
            this.dirtyConnectionChunks.add(record.chunkIndex);
        }
        const resolvedStyles = new Map<string, { style: ResolvedStyle | undefined; key: string }>();
        for (const connection of visible) {
            let resolved = resolvedStyles.get(connection.styleId);
            if (!resolved) {
                const style = this.stylePreview.resolve(styles.get(connection.styleId), 'connection');
                resolved = { style, key: JSON.stringify(style) };
                resolvedStyles.set(connection.styleId, resolved);
            }
            const current = this.connectionPaintById.get(connection.connectionId);
            if (current?.connection === connection && current.styleKey === resolved.key) continue;
            current?.group.destroy();
            current?.visualGroup.destroy();
            if (current) this.dirtyConnectionChunks.add(current.chunkIndex);
            const sceneIndex = scene.connections.indexOf(connection);
            const chunkIndex = Math.floor(sceneIndex / CONNECTION_VECTOR_CHUNK_SIZE);
            let chunk = this.connectionChunkByIndex.get(chunkIndex);
            if (!chunk) {
                chunk = new Konva.Group({ name: `connection-vector-chunk:${chunkIndex}`, listening: false });
                this.connectionChunkByIndex.set(chunkIndex, chunk);
                this.routeGroup.add(chunk);
                chunk.moveToTop();
            }
            const record = this.paintConnection(connection, resolved.style, resolved.key, chunkIndex);
            this.connectionPaintById.set(connection.connectionId, record);
            chunk.add(record.visualGroup);
            this.routeHitGroup.add(record.group);
            record.visualGroup.moveToTop();
            record.group.moveToTop();
            this.dirtyConnectionChunks.add(chunkIndex);
        }
        for (const chunkIndex of this.dirtyConnectionChunks) {
            const chunk = this.connectionChunkByIndex.get(chunkIndex);
            if (!chunk) continue;
            if (chunk.getChildren().length === 0) {
                chunk.destroy();
                this.connectionChunkByIndex.delete(chunkIndex);
            }
        }
        this.dirtyConnectionChunks.clear();
        return visible.reduce((count, connection) => count + (this.connectionPaintById.get(connection.connectionId)?.paintCount ?? 0), 0);
    }

    private paintConnection(connection: SceneConnection, style: ResolvedStyle | undefined, styleKey: string, chunkIndex: number): ConnectionPaintRecord {
        const group = new Konva.Group({ name: `connection-hit:${connection.connectionId}`, id: connection.connectionId });
        const visualGroup = new Konva.Group({ name: `connection-paint:${connection.connectionId}`, listening: false });
        group.setAttr('athenaKind', 'connection-group');
        group.setAttr('connectionId', connection.connectionId);
        group.setAttr('traceId', connection.traceId);
        let paintCount = 0;
        for (const segment of connection.segments) {
            const ordinal = connection.segments.indexOf(segment);
            const points = [segment.start.x, segment.start.y, segment.end.x, segment.end.y];
            const hitTarget = new Konva.Line({
                points,
                stroke: 'rgba(0,0,0,0)',
                strokeWidth: 1,
                hitStrokeWidth: CONNECTION_HIT_WIDTH_PX,
                lineCap: 'butt',
                lineJoin: 'miter',
                listening: true,
                draggable: this.commandsEnabled,
                name: `connection:${connection.connectionId}`,
                id: connection.connectionId,
            });
            this.configureConnectionIdentity(hitTarget, connection);
            this.configureRouteDrag(hitTarget, connection, { kind: 'SEGMENT', ordinal }, segment.start);
            group.add(hitTarget);

            if (segment.kind !== 'SHARED' || this.ownsSharedSegment(connection, segment.segmentId)) {
                const lineConfig = {
                    points,
                    stroke: styleColor(style, 'strokeRgba'),
                    strokeWidth: style?.strokeWidth ?? 1,
                    strokeScaleEnabled: false,
                    dash: style?.dash ?? [],
                    lineCap: (style?.lineCap ?? 'BUTT').toLowerCase() as 'butt' | 'round' | 'square',
                    lineJoin: (style?.lineJoin ?? 'MITER').toLowerCase() as 'miter' | 'round' | 'bevel',
                    opacity: (style?.opacity ?? 255) / 255,
                    listening: false,
                    perfectDrawEnabled: false,
                };
                const arrowSize = this.screenPixelsToScene(ROUTE_ARROW_SIZE_PX);
                visualGroup.add(style?.routeMarker === 'END_ARROW' && ordinal === connection.segments.length - 1
                    ? new Konva.Arrow({ ...lineConfig, pointerLength: arrowSize, pointerWidth: arrowSize })
                    : new Konva.Line(lineConfig));
                paintCount += 1;
            }
        }
        for (let ordinal = 0; ordinal < connection.segments.length - 1; ordinal += 1) {
            const left = connection.segments[ordinal];
            const right = connection.segments[ordinal + 1];
            const bend = left.end;
            if (bend.x !== right.start.x || bend.y !== right.start.y) continue;
            const bendTarget = new Konva.Circle({
                x: bend.x,
                y: bend.y,
                radius: this.screenPixelsToScene(PORT_HIT_DIAMETER_PX / 2),
                fill: 'rgba(0,0,0,0)',
                listening: true,
                draggable: this.commandsEnabled,
                name: `connection-bend:${connection.connectionId}:${ordinal}`,
                id: connection.connectionId,
            });
            this.configureConnectionIdentity(bendTarget, connection);
            this.configureRouteDrag(bendTarget, connection, { kind: 'BEND', ordinal }, bend);
            group.add(bendTarget);
        }
        for (const marker of connection.markers) {
            if (!this.ownsElement(connection, marker.elementId)) continue;
            const markerNode = this.paintConnectionMarker(marker, style);
            if (markerNode) {
                visualGroup.add(markerNode);
                paintCount += 1;
            }
        }
        for (const annotation of connection.annotations) {
            if (!this.ownsElement(connection, annotation.elementId)) continue;
            visualGroup.add(new Konva.Text({
                x: annotation.bounds.x,
                y: annotation.bounds.y,
                width: annotation.bounds.width,
                height: annotation.bounds.height,
                text: annotation.value,
                fontSize: this.screenPixelsToScene(CONNECTION_ANNOTATION_FONT_PX),
                fontStyle: 'normal',
                fill: styleColor(style, 'strokeRgba'),
                opacity: (style?.opacity ?? 255) / 255,
                align: 'center',
                verticalAlign: 'middle',
                listening: false,
            }));
            paintCount += 1;
        }
        return { group, visualGroup, connection, styleKey, paintCount, chunkIndex };
    }

    private ownsSharedSegment(connection: SceneConnection, segmentId: string): boolean {
        return this.scene?.connections.find(candidate => candidate.segments.some(segment => segment.kind === 'SHARED' && segment.segmentId === segmentId))?.connectionId === connection.connectionId;
    }

    private ownsElement(connection: SceneConnection, elementId: string): boolean {
        return this.scene?.connections.find(candidate =>
            candidate.markers.some(marker => marker.elementId === elementId)
            || candidate.annotations.some(annotation => annotation.elementId === elementId)
        )?.connectionId === connection.connectionId;
    }

    private rebuildConnectionIndex(): void {
        this.connectionIndex = this.scene
            ? new SceneSpatialIndex(this.scene.connections.map(connection => ({ bounds: connectionBounds(connection), connection })), 64)
            : undefined;
    }

    applyIncrementalConnectionChanges(nextConnections: readonly SceneConnection[], changedConnectionIds: readonly string[]): {
        changedConnectionIds: string[];
        repaintedConnectionIds: string[];
        incrementalPaintCount: number;
        fullScenePaintCount: number;
        retainedConnectionIds: string[];
    } {
        const scene = this.scene;
        if (!scene) throw new Error('Incremental connection paint requires an active Scene.');
        const changed = new Set(changedConnectionIds);
        if (changed.size === 0 || changed.size >= nextConnections.length) throw new Error('Incremental connection paint requires a bounded non-empty changed set.');
        if ([...changed].some(id => !nextConnections.some(connection => connection.connectionId === id))) {
            throw new Error('Incremental connection paint changed set contains an unknown connection.');
        }
        const retainedGroups = new Map([...this.connectionPaintById].filter(([id]) => !changed.has(id)).map(([id, record]) => [id, record.group]));
        for (const connectionId of changed) {
            const record = this.connectionPaintById.get(connectionId);
            record?.group.destroy();
            record?.visualGroup.destroy();
            if (record) this.dirtyConnectionChunks.add(record.chunkIndex);
            this.connectionPaintById.delete(connectionId);
        }
        this.scene = { ...scene, connections: [...nextConnections] };
        if (this.publication?.state !== 'UNAVAILABLE') this.publication = { ...this.publication, scene: this.scene };
        this.rebuildConnectionIndex();
        const styles = new Map(this.scene.styles.map(style => [style.styleId, style]));
        this.syncVisibleConnections(this.visibleSceneBounds(), styles);
        const repaintedConnectionIds = [...changed].filter(id => this.connectionPaintById.has(id));
        const retainedConnectionIds = [...retainedGroups].filter(([id, group]) => this.connectionPaintById.get(id)?.group === group).map(([id]) => id);
        const incrementalPaintCount = repaintedConnectionIds.reduce((count, id) => count + (this.connectionPaintById.get(id)?.paintCount ?? 0), 0);
        const fullScenePaintCount = [...this.connectionPaintById.values()].reduce((count, record) => count + record.paintCount, 0);
        this.visiblePaintElements = fullScenePaintCount;
        this.renderSelectionOverlay();
        this.stage.batchDraw();
        return { changedConnectionIds: [...changed], repaintedConnectionIds, incrementalPaintCount, fullScenePaintCount, retainedConnectionIds };
    }

    private configureConnectionIdentity(node: Konva.Node, connection: SceneConnection): void {
        node.setAttr('athenaKind', 'connection');
        node.setAttr('semanticId', connection.connectionId);
        node.setAttr('traceId', connection.traceId);
        node.setAttr('connectionId', connection.connectionId);
    }

    private configureRouteDrag(
        node: Konva.Node,
        connection: SceneConnection,
        target: { kind: 'SEGMENT' | 'BEND'; ordinal: number },
        origin: { x: number; y: number },
    ): void {
        node.setAttr('routeTarget', target);
        node.setAttr('routeOrigin', origin);
        node.setAttr('routeNodeOrigin', { x: node.x(), y: node.y() });
        node.on('dragstart', () => {
            this.activeRouteDrag = { connection, target, origin, node };
            this.routePreview = { connectionId: connection.connectionId, target, delta: { x: 0, y: 0 } };
            this.renderSelectionOverlay();
            this.stage.batchDraw();
        });
        node.on('dragmove', () => {
            if (!this.activeRouteDrag) return;
            const nodeOrigin = node.getAttr('routeNodeOrigin') as { x: number; y: number };
            this.routePreview = { connectionId: connection.connectionId, target, delta: { x: node.x() - nodeOrigin.x, y: node.y() - nodeOrigin.y } };
            this.renderSelectionOverlay();
            this.stage.batchDraw();
        });
        node.on('dragend', () => {
            const point = this.pointFromRouteDrag(connection, target, node);
            node.position(node.getAttr('routeNodeOrigin') as { x: number; y: number });
            this.routePreview = undefined;
            this.activeRouteDrag = undefined;
            if (this.commandsEnabled) {
                void this.commitRouteIntent(connection, target, point);
            }
            this.renderSelectionOverlay();
            this.stage.batchDraw();
        });
    }

    private paintConnectionMarker(marker: SceneConnectionMarker, style: ResolvedStyle | undefined): Konva.Shape | undefined {
        const stroke = styleColor(style, 'strokeRgba');
        const opacity = (style?.opacity ?? 255) / 255;
        if (marker.kind === 'JUNCTION') {
            return new Konva.Circle({
                x: marker.point.x,
                y: marker.point.y,
                radius: this.screenPixelsToScene(JUNCTION_RADIUS_PX),
                fill: stroke,
                opacity,
                listening: false,
            });
        }
        if (marker.kind === 'CROSSING') {
            if (!marker.bridgeOwner) return undefined;
            const radius = this.screenPixelsToScene(CROSSING_BRIDGE_RADIUS_PX);
            return new Konva.Line({
                points: [
                    marker.point.x - radius, marker.point.y,
                    marker.point.x - radius / 2, marker.point.y - radius * 0.86,
                    marker.point.x, marker.point.y - radius,
                    marker.point.x + radius / 2, marker.point.y - radius * 0.86,
                    marker.point.x + radius, marker.point.y,
                ],
                stroke,
                strokeWidth: style?.strokeWidth ?? 1,
                strokeScaleEnabled: false,
                lineCap: 'butt',
                lineJoin: 'round',
                tension: 0.35,
                opacity,
                listening: false,
            });
        }
        if (marker.kind === 'INTERRUPTION_START' || marker.kind === 'INTERRUPTION_END') {
            return new Konva.Circle({
                x: marker.point.x,
                y: marker.point.y,
                radius: this.screenPixelsToScene(INTERRUPTION_RADIUS_PX),
                fill: '#ffffff',
                stroke,
                strokeWidth: style?.strokeWidth ?? 1,
                strokeScaleEnabled: false,
                opacity,
                listening: false,
            });
        }
        return undefined;
    }

    private drawOccurrence(occurrence: SceneOccurrence, styles: Map<string, ResolvedStyle>, traces: Map<string, AthenaDiagramScene['traces'][number]>): number {
        const style = this.stylePreview.resolve(styles.get(occurrence.styleId), 'symbol', occurrence.occurrenceId);
        const occurrenceHitTarget = new Konva.Rect({ x: occurrence.bounds.x, y: occurrence.bounds.y, width: occurrence.bounds.width, height: occurrence.bounds.height, fill: 'rgba(0,0,0,0)', strokeWidth: 0, name: `occurrence:${occurrence.occurrenceId}`, id: occurrence.elementId, listening: true, draggable: this.commandsEnabled });
        occurrenceHitTarget.setAttr('athenaKind', 'occurrence');
        occurrenceHitTarget.setAttr('semanticId', occurrence.semanticId);
        occurrenceHitTarget.setAttr('traceId', occurrence.traceId);
        occurrenceHitTarget.setAttr('occurrenceId', occurrence.occurrenceId);
        occurrenceHitTarget.setAttr('originalPosition', { x: occurrence.bounds.x, y: occurrence.bounds.y });
        this.configureOccurrenceDrag(occurrenceHitTarget, occurrence);
        this.contentGroup.add(occurrenceHitTarget);
        let paintedPorts = 0;
        for (const port of occurrence.ports) {
            const portStyle = this.stylePreview.resolve(styles.get(port.styleId), 'port');
            if (this.drawPort(port, portStyle, occurrence, traces)) paintedPorts += 1;
        }
        for (const label of occurrence.labels) {
            const labelStyle = this.stylePreview.resolve(styles.get(label.styleId), 'label');
            const labelNode = new Konva.Text({ x: label.bounds.x, y: label.bounds.y, text: label.text, width: label.bounds.width, height: label.bounds.height, fontSize: labelStyle?.fontSize ?? 12, fontStyle: labelStyle?.fontWeight && labelStyle.fontWeight >= 600 ? 'bold' : 'normal', fill: styleColor(labelStyle, 'strokeRgba'), opacity: (labelStyle?.opacity ?? 255) / 255, align: labelStyle?.textAlign === 'MIDDLE' ? 'center' : labelStyle?.textAlign === 'END' ? 'right' : 'left', verticalAlign: 'middle', rotation: label.rotationDegrees, listening: false });
            this.contentGroup.add(labelNode);
            this.registerOccurrencePreviewPaint(occurrence.occurrenceId, labelNode);
        }
        return 1 + paintedPorts + occurrence.labels.length;
    }

    private drawOccurrenceAsset(occurrence: SceneOccurrence, style: ResolvedStyle | undefined): void {
        const assetId = occurrence.assetId;
        if (!assetId) return;
        const image = this.loadedImages.get(assetId);
        if (!image) return;
        const intrinsicWidth = image.naturalWidth || image.width;
        const intrinsicHeight = image.naturalHeight || image.height;
        const fit = fitAssetIntoBounds(occurrence.bounds, intrinsicWidth, intrinsicHeight);
        const imageNode = new Konva.Image({
            image,
            x: fit.x,
            y: fit.y,
            width: fit.width,
            height: fit.height,
            listening: false,
            perfectDrawEnabled: false,
            opacity: (style?.opacity ?? 255) / 255,
        });
        this.assetGroup.add(imageNode);
        this.registerOccurrencePreviewPaint(occurrence.occurrenceId, imageNode);
    }

    private drawDenseOccurrenceBatch(visible: SceneOccurrence[], styles: Map<string, ResolvedStyle>): number {
        const style = styles.get(visible[0]?.styleId);
        const stroke = styleColor(style, 'strokeRgba');
        const fill = styleColor(style, 'fillRgba');
        const paintPath = new Path2D();
        for (const occurrence of visible) {
            paintPath.rect(occurrence.bounds.x, occurrence.bounds.y, occurrence.bounds.width, occurrence.bounds.height);
        }
        const batch = new Konva.Shape({
            name: 'dense-occurrence-batch',
            listening: false,
            perfectDrawEnabled: false,
            sceneFunc: (context, shape) => {
                const canvas = (context as unknown as { _context: CanvasRenderingContext2D })._context;
                canvas.fillStyle = fill;
                canvas.strokeStyle = stroke;
                canvas.lineWidth = style?.strokeWidth ?? 1;
                canvas.fill(paintPath);
                canvas.stroke(paintPath);
            },
        });
        this.contentGroup.add(batch);
        const first = visible[0];
        if (first) {
            const hitNode = new Konva.Rect({
                x: first.bounds.x,
                y: first.bounds.y,
                width: first.bounds.width,
                height: first.bounds.height,
                fill: 'rgba(0,0,0,0)',
                listening: true,
                draggable: this.commandsEnabled,
                perfectDrawEnabled: false,
                name: `occurrence:${first.occurrenceId}`,
                id: first.elementId,
            });
            hitNode.setAttr('athenaKind', 'occurrence');
            hitNode.setAttr('semanticId', first.semanticId);
            hitNode.setAttr('traceId', first.traceId);
            hitNode.setAttr('occurrenceId', first.occurrenceId);
            hitNode.setAttr('originalPosition', { x: first.bounds.x, y: first.bounds.y });
            this.configureOccurrenceDrag(hitNode, first);
            this.contentGroup.add(hitNode);
        }
        return visible.length;
    }

    private visibleOccurrences(scene: AthenaDiagramScene, viewport: { x: number; y: number; width: number; height: number }): SceneOccurrence[] {
        return this.occurrenceIndex?.query(viewport) ?? scene.occurrences.filter(occurrence => this.boundsIntersect(occurrence.bounds, viewport));
    }

    private drawPort(port: ScenePort, style: ResolvedStyle | undefined, occurrence: SceneOccurrence, traces: Map<string, AthenaDiagramScene['traces'][number]>): boolean {
        if (style?.portDisplay === 'HIDDEN') return false;
        const identity = port as ScenePortIdentity;
        const node = new Konva.Circle({ x: port.point.x, y: port.point.y, radius: this.screenPixelsToScene(PORT_MARKER_RADIUS_PX), fill: styleColor(style, 'fillRgba'), stroke: styleColor(style, 'strokeRgba'), strokeWidth: Math.max(1, style?.strokeWidth ?? 1), strokeScaleEnabled: false, name: `port:${identity.semanticPortId}`, id: identity.anchorId, hitStrokeWidth: PORT_HIT_DIAMETER_PX, listening: true });
        node.setAttr('athenaKind', 'port');
        node.setAttr('semanticId', identity.semanticPortId);
        node.setAttr('semanticPortId', identity.semanticPortId);
        node.setAttr('anchorId', identity.anchorId);
        node.setAttr('traceId', port.traceId);
        node.setAttr('occurrenceId', occurrence.occurrenceId);
        node.setAttr('direction', port.direction);
        this.contentGroup.add(node);
        this.registerOccurrencePreviewPaint(occurrence.occurrenceId, node);
        return true;
    }

    private configureOccurrenceDrag(node: Konva.Rect, occurrence: SceneOccurrence): void {
        node.on('dragstart', () => {
            this.activeDraggedOccurrenceId = occurrence.occurrenceId;
            this.host.focus();
        });
        node.on('dragmove', () => {
            this.previewOccurrence(occurrence.occurrenceId, node);
            this.stage.batchDraw();
        });
        node.on('dragend', () => {
            const point = this.pointFromOccurrenceDrag(occurrence, node);
            this.restoreOccurrencePreview(occurrence.occurrenceId);
            const original = node.getAttr('originalPosition') as { x: number; y: number };
            node.position(original);
            this.activeDraggedOccurrenceId = undefined;
            if (this.commandsEnabled) this.onMove?.({ occurrenceId: occurrence.occurrenceId, point, lockAction: 'PRESERVE' });
            this.stage.batchDraw();
        });
    }

    private registerOccurrencePreviewPaint(occurrenceId: string, node: Konva.Node): void {
        node.setAttr('previewOrigin', { x: node.x(), y: node.y() });
        const nodes = this.previewPaintByOccurrence.get(occurrenceId) ?? [];
        nodes.push(node);
        this.previewPaintByOccurrence.set(occurrenceId, nodes);
    }

    private previewOccurrence(occurrenceId: string, dragNode: Konva.Node): void {
        const original = dragNode.getAttr('originalPosition') as { x: number; y: number };
        const deltaX = dragNode.x() - original.x;
        const deltaY = dragNode.y() - original.y;
        for (const node of this.previewNodesForOccurrence(occurrenceId)) {
            const paintOrigin = node.getAttr('previewOrigin') as { x: number; y: number };
            node.position({ x: paintOrigin.x + deltaX, y: paintOrigin.y + deltaY });
        }
    }

    private restoreOccurrencePreview(occurrenceId: string | undefined = this.activeDraggedOccurrenceId): void {
        if (!occurrenceId) return;
        for (const node of this.previewNodesForOccurrence(occurrenceId)) {
            node.position(node.getAttr('previewOrigin') as { x: number; y: number });
        }
        this.activeDraggedOccurrenceId = undefined;
        this.stage.batchDraw();
    }

    private previewNodesForOccurrence(occurrenceId: string): Konva.Node[] {
        return [
            ...(this.previewPaintByOccurrence.get(occurrenceId) ?? []),
            ...(this.selectionPreviewPaintByOccurrence.get(occurrenceId) ?? []),
        ];
    }

    private screenPixelsToScene(pixels: number): number {
        return pixels / Math.max(0.001, this.stage.scaleX() || 1);
    }

    private async loadAssets(scene: AthenaDiagramScene): Promise<void> {
        const bundle = this.publication && this.publication.state !== 'UNAVAILABLE' ? this.publication.assetBundle : undefined;
        if (!bundle || this.loadedRevision === scene.inputRevision) return;
        const revision = scene.inputRevision;
        const entries = new Map(bundle.entries.map(entry => [entry.entryId, entry]));
        try {
            await Promise.all(scene.assets.map(async asset => {
            const entry = entries.get(asset.bundleEntryId);
            if (!entry || asset.mediaKind === 'WOFF2' || entry.digest !== asset.digest) {
                throw new Error(`Asset bundle entry does not match admitted asset ${asset.assetId}.`);
            }
            const bytes = decodeBase64(entry.bytesBase64);
            if (await sha256Digest(bytes) !== asset.digest) {
                throw new Error(`Asset bytes do not match admitted digest ${asset.assetId}.`);
            }
            const url = URL.createObjectURL(new Blob([bytes], { type: asset.mediaKind === 'PNG' ? 'image/png' : 'image/svg+xml' }));
            this.objectUrls.add(url);
            const image = await loadImage(url);
            if (this.disposed || this.scene?.inputRevision !== revision) return;
            this.loadedImages.set(asset.assetId, image);
        }));
        } catch (error) {
            if (!this.disposed && this.scene?.inputRevision === revision) {
                this.host.dataset.assetError = error instanceof Error ? error.message : String(error);
                console.error('Athena representation asset paint failed:', error);
                this.clear();
            }
            return;
        }
        if (this.disposed || this.scene?.inputRevision !== revision) return;
        this.loadedRevision = revision;
        this.host.dataset.loadedAssetCount = String(scene.assets.filter(asset => this.loadedImages.has(asset.assetId)).length);
        this.redrawVisibleContent();
    }

    private applyFit(scene: AthenaDiagramScene): void {
        const bounds = scene.page.drawingBounds;
        const width = Math.max(1, this.stage.width());
        const height = Math.max(1, this.stage.height());
        this.fitScale = Math.min(width / bounds.width, height / bounds.height);
        this.stage.scale({ x: this.fitScale, y: this.fitScale });
        this.stage.position({ x: (width - bounds.width * this.fitScale) / 2 - bounds.x * this.fitScale, y: -bounds.y * this.fitScale });
    }

    private handleSelection(target: Konva.Node, additive: boolean): void {
        const kind = target.getAttr('athenaKind');
        if (kind !== 'port' && kind !== 'occurrence' && kind !== 'connection') return;
        if (kind === 'port') {
            void this.commitPortIntent(target);
        }
        this.selectedId = target.id();
        const occurrenceId = target.getAttr('occurrenceId') as string | undefined;
        const connectionId = target.getAttr('connectionId') as string | undefined;
        if (kind === 'connection') {
            this.selectedConnectionId = connectionId;
            if (!additive) this.selectedOccurrenceIds.clear();
        } else if (kind !== 'port' && !additive) {
            this.selectedConnectionId = undefined;
        }
        if (occurrenceId) {
            if (!additive) this.selectedOccurrenceIds.clear();
            if (additive && this.selectedOccurrenceIds.has(occurrenceId)) {
                this.selectedOccurrenceIds.delete(occurrenceId);
            } else {
                this.selectedOccurrenceIds.add(occurrenceId);
            }
        }
        this.renderSelectionOverlay();
        if (!this.suppressExternalSelection) {
            this.onSelection?.({ kind, id: target.id(), semanticId: target.getAttr('semanticId'), traceId: target.getAttr('traceId'), occurrenceId, connectionId, additive });
        }
        this.host.dataset.selectedKind = kind;
        this.host.dataset.selectedId = target.id();
        this.host.dataset.selectedTraceId = target.getAttr('traceId') || '';
    }

    private async commitPortIntent(target: Konva.Node): Promise<void> {
        const port = this.scene?.occurrences
            .flatMap(occurrence => occurrence.ports.map(candidate => ({ ...candidate, occurrenceId: occurrence.occurrenceId } as PortCandidate)))
            .find(candidate => candidate.anchorId === target.getAttr('anchorId'));
        if (!port) return;
        if (this.selectedConnectionId) {
            const connection = this.scene?.connections.find(candidate => candidate.connectionId === this.selectedConnectionId);
            if (!connection) return;
            const endpointRole = port.direction === 'IN' ? 'SINK' : 'SOURCE';
            let accepted = false;
            try {
                accepted = await this.onConnectionIntent?.({
                    kind: 'RECONNECT_CONNECTION_ENDPOINT',
                    targetIdentities: [connection.connectionId, port.semanticPortId],
                    sourceTrace: { traceId: connection.traceId, subjectId: connection.connectionId },
                    body: {
                        kind: 'RECONNECT_CONNECTION_ENDPOINT',
                        connectionId: connection.connectionId,
                        endpointRole,
                        replacementPortId: port.semanticPortId,
                    },
                }) ?? false;
            } catch {
                accepted = false;
            }
            this.clearConnectionPreview();
            if (!accepted) this.renderSelectionOverlay();
            return;
        }
        if (!this.pendingPortCandidate) {
            this.pendingPortCandidate = port;
            this.renderSelectionOverlay();
            return;
        }
        const first = this.pendingPortCandidate;
        if (first.semanticPortId === port.semanticPortId) {
            this.clearConnectionPreview();
            this.renderSelectionOverlay();
            return;
        }
        const firstRole = this.portRole(first);
        const secondRole = this.portRole(port);
        const endpoints: [{ role: 'SOURCE'; portId: string }, { role: 'SINK'; portId: string }] = firstRole === 'SOURCE' && secondRole === 'SINK'
            ? [{ role: 'SOURCE', portId: first.semanticPortId }, { role: 'SINK', portId: port.semanticPortId }]
            : [{ role: 'SOURCE', portId: port.semanticPortId }, { role: 'SINK', portId: first.semanticPortId }];
        let accepted = false;
        try {
            accepted = await this.onConnectionIntent?.({
                kind: 'CONNECT_PORTS',
                targetIdentities: endpoints.map(endpoint => endpoint.portId),
                sourceTrace: { traceId: first.traceId, subjectId: first.semanticPortId },
                body: { kind: 'CONNECT_PORTS', connectionKind: 'CONDUCTOR', endpoints, requirements: [] },
            }) ?? false;
        } catch {
            accepted = false;
        }
        this.clearConnectionPreview();
        if (!accepted) this.renderSelectionOverlay();
    }

    private portRole(port: PortCandidate): 'SOURCE' | 'SINK' {
        return port.direction === 'IN' ? 'SINK' : 'SOURCE';
    }

    private clearConnectionPreview(): void {
        this.pendingPortCandidate = undefined;
    }

    private clearRoutePreview(): void {
        this.routePreview = undefined;
        this.activeRouteDrag = undefined;
    }

    private async commitRouteIntent(
        connection: SceneConnection,
        target: { kind: 'SEGMENT' | 'BEND'; ordinal: number },
        point: { column: number; row: number },
    ): Promise<void> {
        const identity = connection as SceneConnectionIdentity;
        let accepted = false;
        try {
            accepted = await this.onRouteIntent?.({
                targetIdentities: [identity.connectionId, identity.projectionId],
                sourceTrace: { traceId: connection.traceId, subjectId: connection.connectionId },
                body: {
                    kind: 'ADJUST_CONNECTION_ROUTE',
                    sheetId: this.scene?.snapGrid.sheetId ?? '',
                    connectionId: connection.connectionId,
                    projectionId: identity.projectionId,
                    target,
                    point,
                },
            }) ?? false;
        } catch {
            accepted = false;
        }
        if (!accepted) this.clearRoutePreview();
    }

    private renderSelectionOverlay(): void {
        this.interactionLayer.destroyChildren();
        this.selectionPreviewPaintByOccurrence.clear();
        const scene = this.scene;
        if (!scene) return;
        if (this.pendingPortCandidate) {
            this.interactionLayer.add(new Konva.Circle({
                x: this.pendingPortCandidate.point.x,
                y: this.pendingPortCandidate.point.y,
                radius: this.screenPixelsToScene(PORT_MARKER_RADIUS_PX + 3),
                stroke: '#1967d2',
                strokeWidth: 1,
                strokeScaleEnabled: false,
                listening: false,
            }));
        }
        const selectedConnections = scene.connections.filter(connection => connection.connectionId === this.selectedConnectionId);
        for (const selectedConnection of selectedConnections) {
            for (const segment of selectedConnection.segments) {
                this.interactionLayer.add(new Konva.Line({
                    points: [segment.start.x, segment.start.y, segment.end.x, segment.end.y],
                    stroke: '#1967d2',
                    strokeWidth: CONNECTION_SELECTION_WIDTH_PX,
                    strokeScaleEnabled: false,
                    opacity: 0.45,
                    lineCap: 'butt',
                    lineJoin: 'miter',
                    listening: false,
                }));
            }
        }
        for (const occurrence of scene.occurrences) {
            if (!this.selectedOccurrenceIds.has(occurrence.occurrenceId)) continue;
            const outline = new Konva.Rect({
                x: occurrence.bounds.x,
                y: occurrence.bounds.y,
                width: occurrence.bounds.width,
                height: occurrence.bounds.height,
                stroke: '#1967d2',
                strokeWidth: 1,
                strokeScaleEnabled: false,
                dash: [3, 2],
                listening: false,
            });
            this.interactionLayer.add(outline);
            outline.setAttr('previewOrigin', { x: outline.x(), y: outline.y() });
            this.selectionPreviewPaintByOccurrence.set(occurrence.occurrenceId, [outline]);
        }
        if (this.routePreview) {
            const connection = scene.connections.find(candidate => candidate.connectionId === this.routePreview?.connectionId);
            if (connection) {
                const { target, delta } = this.routePreview;
                const segments = connection.segments.map((segment, ordinal) => {
                    const shifted = target.kind === 'SEGMENT' && ordinal === target.ordinal;
                    const bendEnd = target.kind === 'BEND' && ordinal === target.ordinal;
                    const bendStart = target.kind === 'BEND' && ordinal === target.ordinal + 1;
                    return shifted
                        ? { start: { x: segment.start.x + delta.x, y: segment.start.y + delta.y }, end: { x: segment.end.x + delta.x, y: segment.end.y + delta.y } }
                        : {
                            start: bendStart ? { x: segment.start.x + delta.x, y: segment.start.y + delta.y } : segment.start,
                            end: bendEnd ? { x: segment.end.x + delta.x, y: segment.end.y + delta.y } : segment.end,
                        };
                });
                for (const segment of segments) {
                    this.interactionLayer.add(new Konva.Line({
                        points: [segment.start.x, segment.start.y, segment.end.x, segment.end.y],
                        stroke: '#1967d2',
                        strokeWidth: CONNECTION_SELECTION_WIDTH_PX,
                        strokeScaleEnabled: false,
                        opacity: 0.45,
                        listening: false,
                    }));
                }
            }
        }
        this.interactionLayer.batchDraw();
    }

    private readonly handleKeyDown = (event: KeyboardEvent): void => {
        if (event.key === 'Escape') {
            this.restoreOccurrencePreview();
            this.clearConnectionPreview();
            this.clearRoutePreview();
            this.renderSelectionOverlay();
            return;
        }
        if (event.key.toLowerCase() !== 'l' || !this.commandsEnabled || !this.scene || !this.selectedId) return;
        const occurrence = this.scene.occurrences.find(item => item.elementId === this.selectedId || item.occurrenceId === this.selectedId);
        if (!occurrence) return;
        const point = this.pointFromScenePoint(occurrence.placementAnchor);
        this.onMove?.({ occurrenceId: occurrence.occurrenceId, point, lockAction: 'LOCK' });
    };

    private pointFromNode(node: Konva.Node): { x: number; y: number } {
        return this.pointFromScenePoint({ x: Math.round(node.x()), y: Math.round(node.y()) });
    }

    private pointFromOccurrenceDrag(occurrence: SceneOccurrence, node: Konva.Node): { x: number; y: number } {
        const original = node.getAttr('originalPosition') as { x: number; y: number };
        return this.pointFromScenePoint({
            x: occurrence.placementAnchor.x + Math.round(node.x() - original.x),
            y: occurrence.placementAnchor.y + Math.round(node.y() - original.y),
        });
    }

    private pointFromRouteDrag(
        connection: SceneConnection,
        target: { kind: 'SEGMENT' | 'BEND'; ordinal: number },
        node: Konva.Node,
    ): { column: number; row: number } {
        const segment = connection.segments[Math.min(target.ordinal, connection.segments.length - 1)];
        const base = target.kind === 'BEND'
            ? segment.end
            : { x: (segment.start.x + segment.end.x) / 2, y: (segment.start.y + segment.end.y) / 2 };
        const origin = node.getAttr('routeNodeOrigin') as { x: number; y: number };
        const point = this.pointFromScenePoint({ x: base.x + node.x() - origin.x, y: base.y + node.y() - origin.y });
        return { column: point.x, row: point.y };
    }

    private pointFromScenePoint(point: { x: number; y: number }): { x: number; y: number } {
        if (!this.scene) throw new Error('No READY scene available for move.');
        const grid = this.scene.snapGrid;
        return snapSheetPoint({
            x: Math.max(1, Math.round(point.x - grid.drawingOrigin.x + 1)),
            y: Math.max(1, Math.round(point.y - grid.drawingOrigin.y + 1)),
        }, grid.step);
    }

    private clearLayers(): void {
        this.pageLayer.destroyChildren();
        this.routeGroup.clearCache();
        this.routeGroup.destroyChildren();
        this.routeHitGroup.destroyChildren();
        this.connectionPaintById.clear();
        this.connectionChunkByIndex.clear();
        this.dirtyConnectionChunks.clear();
        this.assetGroup.destroyChildren();
        this.contentGroup.destroyChildren();
        this.interactionLayer.destroyChildren();
        this.visiblePaintElements = 0;
        this.lastRenderedViewport = undefined;
    }

    private maybeRedrawVisibleContent(): void {
        const scene = this.scene;
        if (!scene) return;
        if (this.viewportTransformActive) return;
        const next = this.visibleSceneBounds();
        // Full page remains visible: transform only, no culling rebuild needed.
        const page = scene.page.drawingBounds;
        if (this.boundsContains(next, page)) return;
        const previous = this.lastRenderedViewport;
        if (!previous || Math.abs(previous.x - next.x) > 96 || Math.abs(previous.y - next.y) > 96 || Math.abs(previous.width - next.width) > 96 || Math.abs(previous.height - next.height) > 96) {
            this.redrawVisibleContent();
        }
    }

    private beginViewportTransform(): void {
        if (this.viewportTransformEndTimer !== undefined) {
            window.clearTimeout(this.viewportTransformEndTimer);
            this.viewportTransformEndTimer = undefined;
        }
        this.viewportTransformActive = true;
        this.routeHitGroup.listening(false);
        this.routeHitGroup.visible(false);
    }

    private scheduleViewportTransformEnd(): void {
        if (this.viewportTransformEndTimer !== undefined) window.clearTimeout(this.viewportTransformEndTimer);
        this.viewportTransformEndTimer = window.setTimeout(() => {
            this.viewportTransformEndTimer = undefined;
            this.endViewportTransform();
        }, 96);
    }

    private endViewportTransform(): void {
        if (this.viewportTransformEndTimer !== undefined) {
            window.clearTimeout(this.viewportTransformEndTimer);
            this.viewportTransformEndTimer = undefined;
        }
        this.viewportTransformActive = false;
        this.routeHitGroup.listening(true);
        this.routeHitGroup.visible(true);
        this.maybeRedrawVisibleContent();
    }

    /** Run checked 1,000-connection interaction and bounded repaint measurements. */
    async runConnectionPerformanceBenchmark(): Promise<ConnectionPerformanceBenchmarkResult> {
        if (!this.publication || this.publication.state === 'UNAVAILABLE') {
            throw new Error('Connection performance benchmark requires an active admitted publication.');
        }
        const benchmarkPublication = buildConnectionPerformancePublication(this.publication);
        const previousPublication = this.publication;
        const previousStageSize = { width: this.stage.width(), height: this.stage.height() };
        const previousSelectionSuppression = this.suppressExternalSelection;
        const originalPosition = { x: this.stage.x(), y: this.stage.y() };
        const originalScale = { x: this.stage.scaleX(), y: this.stage.scaleY() };
        this.suppressExternalSelection = true;
        this.benchmarking = true;
        this.stage.size({ width: 1_600, height: 1_000 });
        let identityErrors = 0;
        let traceErrors = 0;
        let unhandledErrors = 0;
        const samples: ConnectionPerformanceMeasurement[] = [];
        let latestPatch: ReturnType<KonvaDiagramAdapter['applyIncrementalConnectionChanges']> | undefined;
        try {
            console.info('ATHENA_CONNECTION_PERFORMANCE_PROGRESS publication-start');
            this.setPublication(benchmarkPublication);
            console.info('ATHENA_CONNECTION_PERFORMANCE_PROGRESS publication-complete');
            await nextFrame();
            await nextFrame();
            const fixtureDigest = await sha256Digest(new TextEncoder().encode(JSON.stringify(benchmarkPublication.scene.connections)).buffer);
            const fullScenePaintCount = [...this.connectionPaintById.values()].reduce((count, record) => count + record.paintCount, 0);
            this.beginViewportTransform();
            for (let index = 0; index < 20; index += 1) {
                this.stage.position({ x: (index % 5) - 2, y: 0 });
                this.maybeRedrawVisibleContent();
                await nextFrame();
            }
            console.info('ATHENA_CONNECTION_PERFORMANCE_PROGRESS warmup-complete');
            for (let index = 0; index < 40; index += 1) {
                await nextFrame();
                const started = performance.now();
                const scale = this.fitScale * (1 + ((index % 8) - 4) * 0.002);
                this.stage.scale({ x: scale, y: scale });
                this.stage.position({ x: ((index % 10) - 5) * 2, y: 0 });
                this.maybeRedrawVisibleContent();
                this.stage.batchDraw();
                const operationDurationMs = roundMetric(performance.now() - started);
                await nextFrame();
                const durationMs = roundMetric(performance.now() - started);
                samples.push({ sequence: samples.length, kind: 'PAN_ZOOM', durationMs, operationDurationMs, frameWaitMs: roundMetric(durationMs - operationDurationMs), selectedConnectionId: benchmarkPublication.scene.connections[0].connectionId, traceId: benchmarkPublication.scene.connections[0].traceId, changedConnectionIds: [], repaintedConnectionIds: [], paintCount: this.visiblePaintElements, acceptedInputRevision: benchmarkPublication.acceptedInputRevision, identityErrors: 0, traceErrors: 0, unhandledErrors: 0 });
            }
            console.info('ATHENA_CONNECTION_PERFORMANCE_PROGRESS pan-zoom-complete');
            this.endViewportTransform();
            for (let index = 0; index < 40; index += 1) {
                const connection = benchmarkPublication.scene.connections[index];
                const record = this.connectionPaintById.get(connection.connectionId);
                const node = record?.group.getChildren().find(candidate => (candidate as Konva.Node).getAttr('athenaKind') === 'connection') as Konva.Node | undefined;
                const started = performance.now();
                if (!node) {
                    identityErrors += 1;
                } else {
                    this.handleSelection(node, false);
                    if (this.host.dataset.selectedId !== connection.connectionId) identityErrors += 1;
                    if (this.host.dataset.selectedTraceId !== connection.traceId) traceErrors += 1;
                }
                this.stage.batchDraw();
                const operationDurationMs = roundMetric(performance.now() - started);
                await nextFrame();
                const durationMs = roundMetric(performance.now() - started);
                samples.push({
                    sequence: samples.length, kind: 'SELECTION',
                    durationMs, operationDurationMs, frameWaitMs: roundMetric(durationMs - operationDurationMs),
                    selectedConnectionId: this.host.dataset.selectedId || '',
                    traceId: this.host.dataset.selectedTraceId,
                    changedConnectionIds: [], repaintedConnectionIds: [], paintCount: this.visiblePaintElements,
                    acceptedInputRevision: benchmarkPublication.acceptedInputRevision,
                    identityErrors: node ? 0 : 1, traceErrors: node && this.host.dataset.selectedTraceId === connection.traceId ? 0 : 1, unhandledErrors: 0,
                });
            }
            console.info('ATHENA_CONNECTION_PERFORMANCE_PROGRESS selection-complete');
            const baseConnections = benchmarkPublication.scene.connections;
            const editedId = baseConnections[0].connectionId;
            for (let index = 0; index < 40; index += 1) {
                const current = this.scene?.connections ?? baseConnections;
                const source = current.find(connection => connection.connectionId === editedId) ?? baseConnections[0];
                const delta = index % 2 === 0 ? 1 : -1;
                const edited: SceneConnection = {
                    ...source,
                    segments: source.segments.map((segment, ordinal) => ordinal === 0
                        ? { ...segment, end: { x: segment.end.x, y: segment.end.y + delta } }
                        : segment),
                };
                const nextConnections = current.map(connection => connection.connectionId === editedId ? edited : connection);
                const started = performance.now();
                try {
                    const changedConnectionIds = planConnectionInvalidation(nextConnections, editedId);
                    latestPatch = this.applyIncrementalConnectionChanges(nextConnections, changedConnectionIds);
                    const expectedRetained = this.connectionPaintById.size - latestPatch.repaintedConnectionIds.length;
                    if (latestPatch.retainedConnectionIds.length !== expectedRetained) identityErrors += 1;
                    const operationDurationMs = roundMetric(performance.now() - started);
                    await nextFrame();
                    const durationMs = roundMetric(performance.now() - started);
                    samples.push({
                        sequence: samples.length, kind: 'LOCAL_REPLAN',
                        durationMs, operationDurationMs, frameWaitMs: roundMetric(durationMs - operationDurationMs),
                        selectedConnectionId: editedId, traceId: edited.traceId,
                        changedConnectionIds: latestPatch.changedConnectionIds,
                        repaintedConnectionIds: latestPatch.repaintedConnectionIds,
                        paintCount: latestPatch.incrementalPaintCount,
                        acceptedInputRevision: benchmarkPublication.acceptedInputRevision,
                        identityErrors: 0, traceErrors: 0, unhandledErrors: 0,
                    });
                } catch {
                    unhandledErrors += 1;
                }
                if ((index + 1) % 10 === 0) console.info(`ATHENA_CONNECTION_PERFORMANCE_PROGRESS local-replan-${index + 1}`);
            }
            if (!latestPatch) throw new Error('Connection performance benchmark produced no local-replan measurement.');
            const evidence: ConnectionPerformanceEvidence = {
                profileRevision: CONNECTION_PERFORMANCE_PROFILE_REVISION,
                fixtureDigest,
                connectionCount: benchmarkPublication.scene.connections.length,
                acceptedInputRevision: benchmarkPublication.acceptedInputRevision,
                environment: {
                    os: typeof navigator === 'undefined' ? 'unknown' : navigator.platform || 'unknown',
                    cpu: typeof navigator === 'undefined' ? 'unknown' : String(navigator.hardwareConcurrency || 'unknown'),
                    nodeVersion: runtimeVersion('node'),
                    electronVersion: runtimeVersion('electron'),
                    konvaVersion: (Konva as unknown as { version?: string }).version ?? '10.3.0',
                    viewport: { width: this.stage.width(), height: this.stage.height() },
                    dpr: typeof window === 'undefined' ? 1 : window.devicePixelRatio || 1,
                },
                samples,
                identityErrors,
                traceErrors,
                unhandledErrors,
                changedConnectionIds: latestPatch.changedConnectionIds,
                repaintedConnectionIds: latestPatch.repaintedConnectionIds,
                fullScenePaintCount,
                incrementalPaintCount: latestPatch.incrementalPaintCount,
            };
            const evaluation = evaluateConnectionPerformance(evidence);
            return {
                ...evidence,
                evaluation,
            };
        } finally {
            this.endViewportTransform();
            this.suppressExternalSelection = previousSelectionSuppression;
            this.benchmarking = false;
            this.stage.size(previousStageSize);
            this.stage.scale(originalScale);
            this.stage.position(originalPosition);
            this.setPublication(previousPublication);
            this.resize();
        }
    }

    /** Run normative M43 scale measurements through this adapter instance. */
    async runScaleBenchmark(): Promise<ScaleBenchmarkResult> {
        const publication = buildScaleBenchmarkPublication();
        const previousPublication = this.publication;
        const previousStageSize = { width: this.stage.width(), height: this.stage.height() };
        const previousSelectionSuppression = this.suppressExternalSelection;
        this.suppressExternalSelection = true;
        this.benchmarking = true;
        this.scaleBenchmarkOperationIndex = 0;
        this.stage.size({ width: 1600, height: 1000 });
        try {
            return await this.measureScaleBenchmark(publication);
        } finally {
            this.suppressExternalSelection = previousSelectionSuppression;
            this.benchmarking = false;
            this.stage.size(previousStageSize);
            this.setPublication(previousPublication);
            this.resize();
        }
    }

    /** Run the normative M44 300-real-occurrence authoring profile. */
    async runAuthoringBenchmark(): Promise<ScaleBenchmarkResult> {
        if (!this.publication) throw new Error('M44 performance benchmark requires an active publication.');
        const publication = buildAuthoringBenchmarkPublication(this.publication);
        const previousPublication = this.publication;
        const previousStageSize = { width: this.stage.width(), height: this.stage.height() };
        const previousSelectionSuppression = this.suppressExternalSelection;
        this.suppressExternalSelection = true;
        this.benchmarking = true;
        this.scaleBenchmarkOperationIndex = 0;
        this.stage.size({ width: 1920, height: 1080 });
        try {
            return await this.measureScaleBenchmark(publication, 60_000, true);
        } finally {
            this.suppressExternalSelection = previousSelectionSuppression;
            this.benchmarking = false;
            this.stage.size(previousStageSize);
            this.setPublication(previousPublication);
            this.resize();
        }
    }

    private async measureScaleBenchmark(publication: AthenaScenePublication, heapWindowMs = 0, zoomIntoScene = false): Promise<ScaleBenchmarkResult> {
        const scene = publication.state === 'UNAVAILABLE' ? undefined : publication.scene;
        const acceptedInputRevision = publication.state === 'UNAVAILABLE' ? undefined : publication.acceptedInputRevision;
        const beforeHeap = readHeapBytes();
        const paintStart = performance.now();
        this.setPublication(publication);
        if (zoomIntoScene) {
            const scale = this.fitScale * 2;
            this.stage.scale({ x: scale, y: scale });
            this.stage.position({ x: 48, y: 30 });
            this.redrawVisibleContent();
        }
        await nextFrame();
        await nextFrame();
        const firstStablePaintMs = performance.now() - paintStart;
        const initialVisiblePaintElements = this.visiblePaintElements;
        for (let index = 0; index < 60; index += 1) {
            this.performScaleOperation(index % 4, false);
            await nextFrame();
        }
        const samples: ScaleBenchmarkResult['samples'] = [];
        const interactionDurations: number[] = [];
        const panZoomDurations: number[] = [];
        const dragPreviewDurations: number[] = [];
        const selectionDurations: number[] = [];
        let identityErrors = 0;
        let errorCount = 0;
        for (let index = 0; index < 300; index += 1) {
            const operation = index % 4;
            const started = performance.now();
            try {
                this.performScaleOperation(operation, true);
                const durationMs = performance.now() - started;
                await nextFrame();
                interactionDurations.push(durationMs);
                if (operation === 0 || operation === 1) panZoomDurations.push(durationMs);
                if (operation === 3) dragPreviewDurations.push(durationMs);
                if (operation === 2) selectionDurations.push(durationMs);
                const selectedTraceId = this.host.dataset.selectedTraceId || '';
                const selectedId = this.host.dataset.selectedId || '';
                if (operation === 2 && (!selectedId || !selectedTraceId)) identityErrors += 1;
                samples.push({
                    frame: index,
                    operation: ['pan', 'zoom', 'select', 'drag'][operation],
                    durationMs: roundMetric(durationMs),
                    visiblePaintElements: this.visiblePaintElements,
                    selectedId,
                    traceId: selectedTraceId,
                });
            } catch {
                errorCount += 1;
            }
        }
        const heapWindowStarted = performance.now();
        while (performance.now() - heapWindowStarted < heapWindowMs) {
            this.performScaleOperation(0, false);
            await nextFrame();
        }
        const afterHeap = readHeapBytes();
        const visiblePaintElements = initialVisiblePaintElements;
        return {
            scenePaintElementCount: scene?.occurrences.length ?? 0,
            visiblePaintElements,
            visiblePercent: scene ? (visiblePaintElements / scene.occurrences.length) * 100 : 0,
            firstStablePaintMs: roundMetric(firstStablePaintMs),
            incrementalHeapMiB: beforeHeap !== undefined && afterHeap !== undefined ? roundMetric(Math.max(0, afterHeap - beforeHeap) / (1024 * 1024)) : null,
            interactionP95Ms: roundMetric(percentile(interactionDurations, 0.95)),
            panZoomP95Ms: roundMetric(percentile(panZoomDurations, 0.95)),
            dragPreviewP95Ms: roundMetric(percentile(dragPreviewDurations, 0.95)),
            selectionP95Ms: roundMetric(percentile(selectionDurations, 0.95)),
            identityErrors,
            traceErrors: identityErrors,
            unhandledErrors: errorCount,
            stableSceneRevision: scene?.inputRevision === acceptedInputRevision,
            samples,
        };
    }

    private performScaleOperation(operation: number, measured: boolean): void {
        const scene = this.scene;
        if (!scene) throw new Error('Scale benchmark requires a scene.');
        const step = this.scaleBenchmarkOperationIndex++;
        if (operation === 0) {
            this.stage.position({ x: 48 + ((step % 20) - 10) * 2, y: 30 });
            this.maybeRedrawVisibleContent();
        } else if (operation === 1) {
            const nextScale = this.fitScale * (1 + ((step % 20) - 10) * 0.002);
            this.stage.scale({ x: nextScale, y: nextScale });
            this.stage.position({ x: 48, y: 30 });
            this.maybeRedrawVisibleContent();
        } else {
            const node = this.contentGroup.getChildren().find((item: Konva.Node) => item.getAttr('athenaKind') === 'occurrence');
            if (!node) throw new Error('Scale benchmark has no visible occurrence.');
            if (operation === 2) {
                this.handleSelection(node, false);
            } else {
                const x = node.x();
                const y = node.y();
                node.position({ x: x + 1, y });
                this.sceneLayer.batchDraw();
                node.position({ x, y });
            }
        }
        if (measured && operation > 1) this.stage.batchDraw();
    }

    private visibleSceneBounds(): { x: number; y: number; width: number; height: number } {
        const scale = this.stage.scaleX() || 1;
        return {
            x: (0 - this.stage.x()) / scale - 64,
            y: (0 - this.stage.y()) / scale - 64,
            width: this.stage.width() / scale + 128,
            height: this.stage.height() / scale + 128,
        };
    }

    private boundsIntersect(a: { x: number; y: number; width: number; height: number }, b: { x: number; y: number; width: number; height: number }): boolean {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    private segmentIntersectsBounds(
        start: { x: number; y: number },
        end: { x: number; y: number },
        bounds: { x: number; y: number; width: number; height: number },
    ): boolean {
        const left = bounds.x;
        const right = bounds.x + bounds.width;
        const top = bounds.y;
        const bottom = bounds.y + bounds.height;
        if (start.x === end.x) {
            return start.x >= left && start.x <= right && Math.max(start.y, end.y) >= top && Math.min(start.y, end.y) <= bottom;
        }
        if (start.y === end.y) {
            return start.y >= top && start.y <= bottom && Math.max(start.x, end.x) >= left && Math.min(start.x, end.x) <= right;
        }
        const segmentBounds = {
            x: Math.min(start.x, end.x),
            y: Math.min(start.y, end.y),
            width: Math.abs(end.x - start.x),
            height: Math.abs(end.y - start.y),
        };
        return this.boundsIntersect(segmentBounds, bounds);
    }

    private boundsContains(outer: { x: number; y: number; width: number; height: number }, inner: { x: number; y: number; width: number; height: number }): boolean {
        return outer.x <= inner.x && outer.y <= inner.y && outer.x + outer.width >= inner.x + inner.width && outer.y + outer.height >= inner.y + inner.height;
    }

    private pointInBounds(point: { x: number; y: number }, bounds: { x: number; y: number; width: number; height: number }): boolean {
        return point.x >= bounds.x && point.x <= bounds.x + bounds.width && point.y >= bounds.y && point.y <= bounds.y + bounds.height;
    }

    private revokeAssets(): void {
        for (const url of this.objectUrls) URL.revokeObjectURL(url);
        this.objectUrls.clear();
        this.loadedImages.clear();
        this.loadedRevision = undefined;
        this.host.dataset.loadedAssetCount = '0';
    }
}

function nextFrame(): Promise<void> {
    return new Promise(resolve => requestAnimationFrame(() => resolve()));
}

function percentile(values: number[], fraction: number): number {
    if (values.length === 0) return Number.POSITIVE_INFINITY;
    const sorted = [...values].sort((left, right) => left - right);
    return sorted[Math.min(sorted.length - 1, Math.ceil(sorted.length * fraction) - 1)];
}

function roundMetric(value: number): number {
    return Math.round(value * 100) / 100;
}

function readHeapBytes(): number | undefined {
    const memory = (performance as Performance & { memory?: { usedJSHeapSize: number } }).memory;
    return memory?.usedJSHeapSize;
}

function runtimeVersion(name: 'node' | 'electron'): string {
    const runtime = (globalThis as typeof globalThis & { process?: { versions?: Record<string, string> } }).process;
    return runtime?.versions?.[name] ?? 'unknown';
}

function styleColor(style: ResolvedStyle | undefined, key: 'strokeRgba' | 'fillRgba'): string {
    const value = style?.[key];
    if (!value) return key === 'fillRgba' ? '#ffffff' : '#20252b';
    return value.length === 9 && value.startsWith('#') ? value.slice(0, 7) : value;
}

function decodeBase64(value: string): ArrayBuffer {
    const binary = atob(value);
    const bytes = Uint8Array.from(binary, character => character.charCodeAt(0));
    return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) as ArrayBuffer;
}

async function sha256Digest(bytes: ArrayBuffer): Promise<string> {
    const digest = await globalThis.crypto.subtle.digest('SHA-256', bytes);
    return `sha256:${Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')}`;
}

function loadImage(url: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
        const image = new Image();
        image.onload = () => resolve(image);
        image.onerror = () => reject(new Error(`Could not decode asset image ${url}.`));
        image.src = url;
    });
}

function fitAssetIntoBounds(bounds: SceneBounds, intrinsicWidth: number, intrinsicHeight: number): SceneBounds {
    if (!(intrinsicWidth > 0) || !(intrinsicHeight > 0)) return bounds;
    const scale = Math.min(bounds.width / intrinsicWidth, bounds.height / intrinsicHeight);
    const width = intrinsicWidth * scale;
    const height = intrinsicHeight * scale;
    return {
        x: bounds.x + (bounds.width - width) / 2,
        y: bounds.y + (bounds.height - height) / 2,
        width,
        height,
    };
}
