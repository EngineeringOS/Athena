import { AthenaScenePublication, InputRevision, SceneConnection } from './generated/types';
import { SceneSpatialIndex } from './scene-spatial-index';

export const CONNECTION_PERFORMANCE_PROFILE_REVISION = 'athena.connection-performance.v1';
export const CONNECTION_PERFORMANCE_CONNECTION_COUNT = 1_000;
export const CONNECTION_PERFORMANCE_THRESHOLDS = Object.freeze({
    panZoomP95Ms: 22,
    selectionP95Ms: 100,
    localReplanP95Ms: 250,
    identityErrors: 0,
    traceErrors: 0,
    unhandledErrors: 0,
    maxChangedIds: 64,
});

export type ConnectionPerformanceSampleKind = 'PAN_ZOOM' | 'SELECTION' | 'LOCAL_REPLAN';
export type ConnectionPerformanceMeasurement = {
    sequence: number;
    kind: ConnectionPerformanceSampleKind;
    durationMs: number;
    /** Synchronous renderer work before the compositor frame is requested. */
    operationDurationMs?: number;
    /** Browser/Electron requestAnimationFrame wait retained for auditability. */
    frameWaitMs?: number;
    selectedConnectionId?: string;
    traceId?: string;
    changedConnectionIds: string[];
    repaintedConnectionIds: string[];
    paintCount: number;
    acceptedInputRevision?: InputRevision;
    identityErrors?: number;
    traceErrors?: number;
    unhandledErrors?: number;
};

export type ConnectionPerformanceEvidence = {
    profileRevision: typeof CONNECTION_PERFORMANCE_PROFILE_REVISION;
    fixtureDigest: string;
    connectionCount: number;
    acceptedInputRevision: InputRevision;
    environment: {
        os: string;
        cpu: string;
        nodeVersion: string;
        electronVersion: string;
        konvaVersion: string;
        viewport: { width: number; height: number };
        dpr: number;
    };
    samples: ConnectionPerformanceMeasurement[];
    identityErrors: number;
    traceErrors: number;
    unhandledErrors: number;
    changedConnectionIds: string[];
    repaintedConnectionIds: string[];
    fullScenePaintCount: number;
    incrementalPaintCount: number;
};

export type ConnectionPerformanceEvaluation = {
    pass: boolean;
    metrics: { panZoomP95Ms: number; selectionP95Ms: number; localReplanP95Ms: number };
    sampleCounts: { panZoom: number; selection: number; localReplan: number };
    gates: {
        profile: boolean;
        evidence: boolean;
        panZoom: boolean;
        selection: boolean;
        localReplan: boolean;
        identity: boolean;
        trace: boolean;
        unhandled: boolean;
        revision: boolean;
        boundedInvalidation: boolean;
    };
    thresholds: typeof CONNECTION_PERFORMANCE_THRESHOLDS;
    failures: string[];
    finiteSamples?: boolean;
    boundedPaint?: boolean;
};

type ReadyPublication = Extract<AthenaScenePublication, { state: 'READY' | 'STALE' }>;
type IndexedConnection = { bounds: { x: number; y: number; width: number; height: number }; connection: SceneConnection };

/** Checked load profile cloned from an admitted Scene. Fixture geometry never becomes engineering authority. */
export function buildConnectionPerformancePublication(publication: AthenaScenePublication): ReadyPublication {
    if ((publication.state !== 'READY' && publication.state !== 'STALE') || publication.scene.connections.length === 0) {
        throw new Error('Connection performance benchmark requires one admitted Scene with real connections.');
    }
    const sourceConnections = publication.scene.connections;
    const connections = Array.from({ length: CONNECTION_PERFORMANCE_CONNECTION_COUNT }, (_, index) => {
        const source = sourceConnections[index % sourceConnections.length];
        const cycle = Math.floor(index / sourceConnections.length);
        const sourceBounds = connectionBounds(source);
        const dx = 12 + (index % 50) * 31 - sourceBounds.x;
        const dy = 12 + Math.floor(index / 50) * 46 - sourceBounds.y;
        const connectionId = `benchmark-connection-${index}`;
        const relatedId = (id: string): string => {
            const relatedIndex = sourceConnections.findIndex(candidate => candidate.connectionId === id);
            const mapped = cycle * sourceConnections.length + Math.max(0, relatedIndex);
            return mapped < CONNECTION_PERFORMANCE_CONNECTION_COUNT ? `benchmark-connection-${mapped}` : connectionId;
        };
        return {
            ...source,
            elementId: `connection:sha256:${indexDigest(index + 1)}`,
            connectionId,
            projectionId: `benchmark-projection-${index}`,
            // Anchor identity belongs to occurrence ports. Fixture connections may share
            // those ports; inventing benchmark-only anchors would invalidate the scene.
            sourceAnchorId: source.sourceAnchorId,
            targetAnchorId: source.targetAnchorId,
            zIndex: index,
            segments: source.segments.map((segment, ordinal) => ({
                ...segment,
                segmentId: `benchmark-segment-${index}-${ordinal}`,
                start: translate(segment.start, dx, dy),
                end: translate(segment.end, dx, dy),
            })),
            markers: source.markers.map((marker, ordinal) => ({
                ...marker,
                elementId: `connection-marker:sha256:${indexDigest(1_000_000 + index * 100 + ordinal)}`,
                point: translate(marker.point, dx, dy),
                relatedConnectionIds: marker.relatedConnectionIds.map(relatedId),
            })),
            annotations: source.annotations.map((annotation, ordinal) => ({
                ...annotation,
                elementId: `connection-annotation:sha256:${indexDigest(2_000_000 + index * 100 + ordinal)}`,
                semanticId: `${annotation.semanticId}#benchmark-${index}`,
                anchor: translate(annotation.anchor, dx, dy),
                bounds: { ...annotation.bounds, ...translate(annotation.bounds, dx, dy) },
            })),
        } satisfies SceneConnection;
    });
    return {
        ...publication,
        scene: {
            ...publication.scene,
            sceneId: `scene:sha256:${'8'.repeat(64)}`,
            sceneDigest: `sha256:${'9'.repeat(64)}`,
            page: { pageBounds: { x: 0, y: 0, width: 1_600, height: 1_000 }, drawingBounds: { x: 0, y: 0, width: 1_600, height: 1_000 } },
            occurrences: publication.scene.occurrences,
            connections,
            decorations: [],
        },
        assetBundle: { inputRevision: publication.scene.inputRevision, entries: [] },
    };
}

/** Stable topology closure plus broad-phase local collision candidates in original Scene order. */
export function planConnectionInvalidation(connections: readonly SceneConnection[], editedConnectionId: string): string[] {
    const byId = new Map(connections.map(connection => [connection.connectionId, connection]));
    if (!byId.has(editedConnectionId)) {
        throw new Error(`Edited connection '${editedConnectionId}' is not present in the Scene.`);
    }
    const adjacency = new Map<string, Set<string>>(connections.map(connection => [connection.connectionId, new Set()]));
    const connect = (left: string, right: string): void => {
        if (left === right || !byId.has(left) || !byId.has(right)) return;
        adjacency.get(left)?.add(right);
        adjacency.get(right)?.add(left);
    };
    const sharedSegmentOwners = new Map<string, string[]>();
    for (const connection of connections) {
        for (const marker of connection.markers) {
            for (const relatedId of marker.relatedConnectionIds) connect(connection.connectionId, relatedId);
        }
        for (const segment of connection.segments) {
            if (segment.kind !== 'SHARED') continue;
            const owners = sharedSegmentOwners.get(segment.segmentId) ?? [];
            owners.push(connection.connectionId);
            sharedSegmentOwners.set(segment.segmentId, owners);
        }
    }
    for (const owners of sharedSegmentOwners.values()) {
        for (let index = 1; index < owners.length; index += 1) connect(owners[0], owners[index]);
    }
    const changed = new Set<string>([editedConnectionId]);
    const queue = [editedConnectionId];
    while (queue.length > 0) {
        const current = queue.shift()!;
        for (const related of adjacency.get(current) ?? []) {
            if (changed.has(related)) continue;
            changed.add(related);
            queue.push(related);
        }
    }
    const indexed: IndexedConnection[] = connections.map(connection => ({ bounds: paddedBounds(connectionBounds(connection), 4), connection }));
    const spatial = new SceneSpatialIndex(indexed, 64);
    for (const connection of connections.filter(candidate => changed.has(candidate.connectionId))) {
        for (const collision of spatial.query(paddedBounds(connectionBounds(connection), 4))) changed.add(collision.connection.connectionId);
    }
    return connections.filter(connection => changed.has(connection.connectionId)).map(connection => connection.connectionId);
}

export function percentileNearestRank(values: readonly number[], fraction: number): number {
    if (values.length === 0) throw new Error('Performance sample set must not be empty.');
    if (!Number.isFinite(fraction) || fraction <= 0 || fraction > 1) throw new Error('Percentile fraction must be within (0, 1].');
    if (values.some(value => !Number.isFinite(value) || value < 0)) throw new Error('Performance samples must be finite non-negative numbers.');
    const sorted = [...values].sort((left, right) => left - right);
    return sorted[Math.ceil(sorted.length * fraction) - 1];
}

export function evaluateConnectionPerformance(evidence: ConnectionPerformanceEvidence): ConnectionPerformanceEvaluation {
    const failures: string[] = [];
    const raw = evidence as unknown as Record<string, unknown>;
    if ('status' in raw || 'passed' in raw || 'pass' in raw) failures.push('Evidence must not contain a precomputed PASS status.');
    const environment = evidence.environment;
    const environmentValid = !!environment
        && [environment.os, environment.cpu, environment.nodeVersion, environment.electronVersion, environment.konvaVersion].every(nonBlank)
        && positiveFinite(environment.viewport?.width) && positiveFinite(environment.viewport?.height) && positiveFinite(environment.dpr);
    const finiteSamples = Array.isArray(evidence.samples) && evidence.samples.every((sample, index) =>
        sample.sequence === index && Number.isFinite(sample.durationMs) && sample.durationMs >= 0
        && Array.isArray(sample.changedConnectionIds) && Array.isArray(sample.repaintedConnectionIds)
        && Number.isInteger(sample.paintCount) && sample.paintCount >= 0
    );
    const sampleShapeValid = finiteSamples && evidence.samples.every(sample =>
        (sample.acceptedInputRevision === undefined || sample.acceptedInputRevision === evidence.acceptedInputRevision)
        && (sample.identityErrors ?? 0) >= 0 && (sample.traceErrors ?? 0) >= 0 && (sample.unhandledErrors ?? 0) >= 0
    );
    const samplesFor = (kind: ConnectionPerformanceSampleKind): ConnectionPerformanceMeasurement[] => {
        const values = evidence.samples.filter(sample => sample.kind === kind);
        if (values.length < 20) failures.push(`${kind} requires at least 20 measured samples.`);
        return values;
    };
    const panZoom = samplesFor('PAN_ZOOM');
    const selection = samplesFor('SELECTION');
    const localReplan = samplesFor('LOCAL_REPLAN');
    const p95 = (samples: ConnectionPerformanceMeasurement[]) => samples.length === 0 ? Number.POSITIVE_INFINITY
        : roundMetric(percentileNearestRank(samples.map(sample => sample.durationMs), 0.95));
    const metrics = { panZoomP95Ms: p95(panZoom), selectionP95Ms: p95(selection), localReplanP95Ms: p95(localReplan) };
    const gates = {
        profile: evidence.profileRevision === CONNECTION_PERFORMANCE_PROFILE_REVISION
            && evidence.connectionCount === CONNECTION_PERFORMANCE_CONNECTION_COUNT
            && /^sha256:[0-9a-f]{64}$/.test(evidence.fixtureDigest),
        evidence: environmentValid && sampleShapeValid && evidence.samples.length > 0
            && Number.isFinite(evidence.fullScenePaintCount) && evidence.fullScenePaintCount > 0
            && Number.isFinite(evidence.incrementalPaintCount) && evidence.incrementalPaintCount > 0
            && evidence.incrementalPaintCount < evidence.fullScenePaintCount,
        panZoom: metrics.panZoomP95Ms <= CONNECTION_PERFORMANCE_THRESHOLDS.panZoomP95Ms,
        selection: metrics.selectionP95Ms <= CONNECTION_PERFORMANCE_THRESHOLDS.selectionP95Ms,
        localReplan: metrics.localReplanP95Ms <= CONNECTION_PERFORMANCE_THRESHOLDS.localReplanP95Ms,
        identity: evidence.identityErrors === 0 && evidence.samples.every(sample => (sample.identityErrors ?? 0) === 0),
        trace: evidence.traceErrors === 0 && evidence.samples.every(sample => (sample.traceErrors ?? 0) === 0),
        unhandled: evidence.unhandledErrors === 0 && evidence.samples.every(sample => (sample.unhandledErrors ?? 0) === 0),
        revision: nonBlank(evidence.acceptedInputRevision) && evidence.samples.every(sample => sample.acceptedInputRevision === undefined || sample.acceptedInputRevision === evidence.acceptedInputRevision),
        boundedInvalidation: localReplan.length > 0 && localReplan.every(sample =>
            sample.changedConnectionIds.length > 0 && sample.changedConnectionIds.length < evidence.connectionCount
            && new Set(sample.changedConnectionIds).size === sample.changedConnectionIds.length
            && sample.changedConnectionIds.length <= CONNECTION_PERFORMANCE_THRESHOLDS.maxChangedIds
            && (!sample.selectedConnectionId || sample.changedConnectionIds.includes(sample.selectedConnectionId))
            && sample.repaintedConnectionIds.length > 0
            && sample.repaintedConnectionIds.every(id => sample.changedConnectionIds.includes(id))
            && sample.paintCount > 0 && sample.paintCount < evidence.fullScenePaintCount),
    };
    if (!gates.profile) failures.push('Profile identity, size, or fixture digest is invalid.');
    if (!gates.evidence) failures.push('Raw samples or environment evidence are incomplete.');
    if (!gates.panZoom) failures.push('Pan/zoom p95 exceeds 22 ms.');
    if (!gates.selection) failures.push('Selection p95 exceeds 100 ms.');
    if (!gates.localReplan) failures.push('Local replan p95 exceeds 250 ms.');
    if (!gates.identity) failures.push('Connection identity drift was recorded.');
    if (!gates.trace) failures.push('Connection trace drift was recorded.');
    if (!gates.unhandled) failures.push('Unhandled benchmark errors were recorded.');
    if (!gates.revision) failures.push('Accepted Input Revision changed during measurement.');
    if (!gates.boundedInvalidation) failures.push('Local invalidation or repaint was empty, excessive, or unrelated.');
    return {
        pass: Object.values(gates).every(Boolean) && failures.length === 0 && !('status' in raw || 'passed' in raw || 'pass' in raw),
        metrics,
        sampleCounts: { panZoom: panZoom.length, selection: selection.length, localReplan: localReplan.length },
        gates,
        thresholds: CONNECTION_PERFORMANCE_THRESHOLDS,
        failures,
        finiteSamples,
        boundedPaint: gates.boundedInvalidation,
    };
}

export function connectionBounds(connection: SceneConnection): { x: number; y: number; width: number; height: number } {
    const points = [...connection.segments.flatMap(segment => [segment.start, segment.end]), ...connection.markers.map(marker => marker.point)];
    if (points.length === 0) return { x: 0, y: 0, width: 0, height: 0 };
    const xs = points.map(point => point.x);
    const ys = points.map(point => point.y);
    const x = Math.min(...xs);
    const y = Math.min(...ys);
    return { x, y, width: Math.max(...xs) - x, height: Math.max(...ys) - y };
}

function translate(point: { x: number; y: number }, dx: number, dy: number) { return { x: point.x + dx, y: point.y + dy }; }
function paddedBounds(bounds: { x: number; y: number; width: number; height: number }, padding: number) { return { x: bounds.x - padding, y: bounds.y - padding, width: bounds.width + padding * 2, height: bounds.height + padding * 2 }; }
function indexDigest(index: number): string { return index.toString(16).padStart(64, '0'); }
function roundMetric(value: number): number { return Math.round(value * 100) / 100; }
function nonBlank(value: unknown): value is string { return typeof value === 'string' && value.trim().length > 0; }
function positiveFinite(value: unknown): value is number { return typeof value === 'number' && Number.isFinite(value) && value > 0; }
