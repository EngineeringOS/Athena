import { AthenaDiagramScene, AthenaScenePublication } from './generated/types';

export type ScaleFrameMetric = {
    frame: number;
    operation: string;
    durationMs: number;
    visiblePaintElements: number;
    selectedId: string;
    traceId: string;
};

export type ScaleBenchmarkResult = {
    scenePaintElementCount: number;
    visiblePaintElements: number;
    visiblePercent: number;
    firstStablePaintMs: number;
    incrementalHeapMiB: number | null;
    interactionP95Ms: number;
    panZoomP95Ms: number;
    dragPreviewP95Ms: number;
    selectionP95Ms: number;
    identityErrors: number;
    traceErrors: number;
    unhandledErrors: number;
    stableSceneRevision: boolean;
    samples: ScaleFrameMetric[];
};

const ELEMENT_COUNT = 100_000;
const VISIBLE_COUNT = 5_000;
const VIEWPORT_WIDTH = 1_600;
const VIEWPORT_HEIGHT = 1_000;
const DIGEST = '0123456789abcdef'.repeat(4);
const STYLE_ID = `style:sha256:${'1'.repeat(64)}` as `style:sha256:${string}`;
const TRACE_ID = `trace:sha256:${'2'.repeat(64)}` as `trace:sha256:${string}`;
const INPUT_REVISION = `input:sha256:${'3'.repeat(64)}` as `input:sha256:${string}`;

/** Deterministic synthetic publication. Geometry is fixture data, never engineering authority. */
export function buildScaleBenchmarkPublication(): AthenaScenePublication {
    const occurrences: AthenaDiagramScene['occurrences'] = Array.from({ length: ELEMENT_COUNT }, (_, index) => {
        const visible = index < VISIBLE_COUNT;
        const column = index % 100;
        const row = Math.floor(index / 100);
        const x = visible ? column * 16 : 100_000 + (index % 1_000) * 16;
        const y = visible ? row * 20 : (Math.floor(index / 1_000) % 100) * 20;
        const digest = indexDigest(index);
        return {
            elementId: `occurrence:sha256:${digest}`,
            occurrenceId: `benchmark-occurrence-${index}`,
            subjectId: `benchmark-subject-${index}`,
            semanticId: `benchmark-subject-${index}`,
            bounds: { x, y, width: 4, height: 4 },
            placementAnchor: { x, y },
            zIndex: index,
            styleId: STYLE_ID,
            traceId: TRACE_ID,
            ports: [],
            labels: [],
        };
    });
    const scene: AthenaDiagramScene = {
        schemaVersion: 2,
        sceneId: `scene:sha256:${'4'.repeat(64)}` as `scene:sha256:${string}`,
            inputRevision: INPUT_REVISION,
        sceneDigest: `sha256:${'5'.repeat(64)}` as `sha256:${string}`,
        page: {
            pageBounds: { x: 0, y: 0, width: VIEWPORT_WIDTH, height: VIEWPORT_HEIGHT },
            drawingBounds: { x: 0, y: 0, width: VIEWPORT_WIDTH, height: VIEWPORT_HEIGHT },
        },
        plotFrame: {
            columns: 400,
            rows: 250,
            columnLabels: 'ALPHA',
            rowLabels: 'NUMERIC',
        },
        snapGrid: {
            sheetId: 'scale-benchmark',
            step: 1,
            drawingOrigin: { x: 0, y: 0 },
            formulaVersion: 'athena-grid-2',
        },
        styles: [{
            styleId: STYLE_ID,
            strokeRgba: '#20252bff',
            fillRgba: '#d9e7f5ff',
            strokeWidth: 1,
            dash: [],
            lineCap: 'BUTT',
            lineJoin: 'MITER',
            fillRule: 'NONZERO',
            opacity: 255,
            fontSize: 12,
            fontWeight: 400,
            textAlign: 'START',
            textBaseline: 'ALPHABETIC',
            routeMarker: 'NONE',
            portDisplay: 'MARKER',
        }],
        assets: [],
        occurrences,
        connections: [],
        decorations: [],
        traces: [{
            traceId: TRACE_ID,
            origins: [{
                relativePath: 'benchmark/scene-100k-manifest.json',
                sourceDigest: DIGEST,
                role: 'SPATIAL_DERIVATION',
                startLine: 0,
                startCharacter: 0,
                endLine: 0,
                endCharacter: 1,
                subjectId: 'benchmark',
                primary: true,
            }],
        }],
    };
    return {
        schemaVersion: 1,
        state: 'READY',
        attemptedInputRevision: INPUT_REVISION,
        acceptedInputRevision: INPUT_REVISION,
        scene,
        assetBundle: { inputRevision: INPUT_REVISION, entries: [] },
        diagnostics: [],
    };
}

/** M44 authoring profile: 300 occurrences cloned from the active admitted engineering scene. */
export function buildAuthoringBenchmarkPublication(publication: AthenaScenePublication): AthenaScenePublication {
    if (publication.state === 'UNAVAILABLE' || publication.scene.occurrences.length === 0) {
        throw new Error('M44 performance benchmark requires an active scene with real occurrences.');
    }
    const scene = publication.scene;
    const occurrences: AthenaDiagramScene['occurrences'] = Array.from({ length: 300 }, (_, index) => {
        const source = scene.occurrences[index % scene.occurrences.length];
        const digest = indexDigest(index + 1);
        const column = index % 20;
        const row = Math.floor(index / 20);
        const x = 16 + column * 70;
        const y = 16 + row * 58;
        return {
            ...source,
            elementId: `occurrence:sha256:${digest}`,
            occurrenceId: `m44-benchmark-${index}`,
            subjectId: `${source.subjectId}#benchmark-${index}`,
            semanticId: `${source.semanticId}#benchmark-${index}`,
            bounds: { ...source.bounds, x, y },
            placementAnchor: { x, y },
            zIndex: index,
            ports: source.ports.map((port, portIndex) => ({
                ...port,
                elementId: `port:sha256:${indexDigest(10_000 + index * 100 + portIndex)}`,
                anchorId: `${port.anchorId}#benchmark-${index}`,
                semanticPortId: `${port.semanticPortId}#benchmark-${index}`,
                point: {
                    x: x + (port.point.x - source.bounds.x),
                    y: y + (port.point.y - source.bounds.y),
                },
            })),
            labels: source.labels.map((label, labelIndex) => ({
                ...label,
                elementId: `label:sha256:${indexDigest(100_000 + index * 100 + labelIndex)}`,
                anchor: {
                    x: x + (label.anchor.x - source.bounds.x),
                    y: y + (label.anchor.y - source.bounds.y),
                },
                bounds: {
                    ...label.bounds,
                    x: x + (label.bounds.x - source.bounds.x),
                    y: y + (label.bounds.y - source.bounds.y),
                },
            })),
        };
    });
    const benchmarkScene: AthenaDiagramScene = {
        ...scene,
        sceneId: `scene:sha256:${'6'.repeat(64)}`,
        sceneDigest: `sha256:${'7'.repeat(64)}`,
        page: {
            pageBounds: { x: 0, y: 0, width: 1_440, height: 900 },
            drawingBounds: { x: 0, y: 0, width: 1_440, height: 900 },
        },
        occurrences,
        connections: [],
        decorations: [],
    };
    return { ...publication, scene: benchmarkScene };
}

function indexDigest(index: number): string {
    return index.toString(16).padStart(64, '0');
}
