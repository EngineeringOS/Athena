import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const model = require('../lib/browser/diagram/connection-performance.js');

const revision = 'input:sha256:' + 'a'.repeat(64);

function connection(id, y, related = []) {
    return {
        elementId: `connection:sha256:${id}`,
        connectionId: id,
        projectionId: `projection-${id}`,
        sourceAnchorId: `source-${id}`,
        targetAnchorId: `target-${id}`,
        segments: [{ segmentId: `segment-${id}`, start: { x: 0, y }, end: { x: 20, y }, kind: 'ORTHOGONAL' }],
        markers: related.length ? [{ elementId: `marker-${id}`, kind: 'JUNCTION', point: { x: 10, y }, relatedConnectionIds: related, bridgeOwner: false, traceId: `trace-${id}` }] : [],
        annotations: [],
        zIndex: 0,
        styleId: 'default',
        traceId: `trace-${id}`,
    };
}

function evidence(overrides = {}) {
    const samples = [];
    for (let i = 0; i < 20; i += 1) {
        for (const kind of ['PAN_ZOOM', 'SELECTION', 'LOCAL_REPLAN']) {
            samples.push({
                sequence: samples.length,
                kind,
                durationMs: 1,
                selectedConnectionId: 'C1',
                traceId: 'T1',
                changedConnectionIds: kind === 'LOCAL_REPLAN' ? ['C1'] : [],
                repaintedConnectionIds: kind === 'LOCAL_REPLAN' ? ['C1'] : [],
                paintCount: kind === 'LOCAL_REPLAN' ? 1 : 10,
                acceptedInputRevision: revision,
                identityErrors: 0,
                traceErrors: 0,
                unhandledErrors: 0,
            });
        }
    }
    return {
        profileRevision: model.CONNECTION_PERFORMANCE_PROFILE_REVISION,
        fixtureDigest: 'sha256:' + 'b'.repeat(64),
        connectionCount: 1_000,
        acceptedInputRevision: revision,
        environment: {
            os: 'test', cpu: 'test', nodeVersion: '22', electronVersion: '39', konvaVersion: '10',
            viewport: { width: 1600, height: 1000 }, dpr: 1,
        },
        samples,
        identityErrors: 0,
        traceErrors: 0,
        unhandledErrors: 0,
        changedConnectionIds: ['C1'],
        repaintedConnectionIds: ['C1'],
        fullScenePaintCount: 10,
        incrementalPaintCount: 1,
        ...overrides,
    };
}

test('profile clones exactly 1,000 deterministic SceneConnection instances', () => {
    const source = connection('source', 0);
    const publication = {
        state: 'READY', schemaVersion: 1, attemptedInputRevision: revision, acceptedInputRevision: revision,
        scene: {
            schemaVersion: 2, sceneId: 'scene:sha256:' + '1'.repeat(64), inputRevision: revision, sceneDigest: 'sha256:' + '2'.repeat(64),
            page: { pageBounds: { x: 0, y: 0, width: 100, height: 100 }, drawingBounds: { x: 0, y: 0, width: 100, height: 100 } },
            plotFrame: { columns: 10, rows: 10, columnLabels: 'ALPHA', rowLabels: 'NUMERIC' }, snapGrid: { sheetId: 'sheet', step: 1, drawingOrigin: { x: 0, y: 0 }, formulaVersion: 'athena-grid-2' },
            styles: [], assets: [], occurrences: [], connections: [source], decorations: [], traces: [],
        }, assetBundle: { inputRevision: revision, entries: [] }, diagnostics: [],
    };
    const first = model.buildConnectionPerformancePublication(publication);
    const second = model.buildConnectionPerformancePublication(publication);
    assert.equal(first.scene.connections.length, 1000);
    assert.deepEqual(first.scene.connections, second.scene.connections);
    assert.equal(new Set(first.scene.connections.map(c => c.connectionId)).size, 1000);
});

test('invalidation stays bounded to topology and collision closure', () => {
    const scene = [connection('C1', 0, ['C2']), connection('C2', 4), connection('C3', 40)];
    assert.deepEqual(model.planConnectionInvalidation(scene, 'C1'), ['C1', 'C2']);
});

test('evaluation fails closed for missing samples, errors, and full repaint', () => {
    const result = model.evaluateConnectionPerformance(evidence({ samples: [], fullScenePaintCount: 1, incrementalPaintCount: 1 }));
    assert.equal(result.pass, false);
    assert.ok(result.failures.length > 0);
    const hardcoded = model.evaluateConnectionPerformance({ ...evidence(), pass: true });
    assert.equal(hardcoded.pass, false);
    const invalid = model.evaluateConnectionPerformance(evidence({ identityErrors: 1 }));
    assert.equal(invalid.gates.identity, false);
});

test('percentile rejects empty and non-finite samples', () => {
    assert.throws(() => model.percentileNearestRank([], 0.95));
    assert.throws(() => model.percentileNearestRank([1, Number.NaN], 0.95));
    assert.equal(model.percentileNearestRank([1, 2, 3, 4], 0.95), 4);
});

test('pan and zoom samples include the requested animation frame', () => {
    const source = readFileSync(new URL('../src/browser/diagram/konva-diagram-adapter.ts', import.meta.url), 'utf8');
    const measuredLoop = source.slice(
        source.indexOf('for (let index = 0; index < 40; index += 1) {', source.indexOf('runConnectionPerformanceBenchmark')),
        source.indexOf("kind: 'SELECTION'", source.indexOf('runConnectionPerformanceBenchmark')),
    );
    assert.match(
        measuredLoop,
        /const started = performance\.now\(\);[\s\S]*this\.stage\.batchDraw\(\);[\s\S]*await nextFrame\(\);[\s\S]*const durationMs = roundMetric\(performance\.now\(\) - started\);/,
    );
});

test('viewport transforms suspend invisible connection hit geometry until selection is possible again', () => {
    const source = readFileSync(new URL('../src/browser/diagram/konva-diagram-adapter.ts', import.meta.url), 'utf8');
    const benchmark = source.slice(
        source.indexOf('async runConnectionPerformanceBenchmark'),
        source.indexOf('/** Run normative M43 scale measurements'),
    );
    assert.match(source, /this\.beginViewportTransform\(\);[\s\S]*this\.maybeRedrawVisibleContent\(\);/);
    assert.match(source, /this\.endViewportTransform\(\);/);
    assert.match(benchmark, /this\.beginViewportTransform\(\);[\s\S]*kind: 'PAN_ZOOM'[\s\S]*this\.endViewportTransform\(\);[\s\S]*kind: 'SELECTION'/);
    assert.match(source, /this\.routeHitGroup\.visible\(false\);/);
    assert.match(source, /this\.routeHitGroup\.visible\(true\);/);
});

test('viewport motion defers visible-content rebuilding until the transform settles', () => {
    const source = readFileSync(new URL('../src/browser/diagram/konva-diagram-adapter.ts', import.meta.url), 'utf8');
    const viewportRedraw = source.slice(source.indexOf('private maybeRedrawVisibleContent'), source.indexOf('private beginViewportTransform'));
    const transformEnd = source.slice(source.indexOf('private endViewportTransform'), source.indexOf('/** Run checked 1,000-connection'));
    assert.match(viewportRedraw, /if \(this\.viewportTransformActive\) return;/);
    assert.match(transformEnd, /this\.viewportTransformActive = false;[\s\S]*this\.maybeRedrawVisibleContent\(\);/);
});

test('product performance evidence separates canvas operation cost from animation-frame wait', () => {
    const source = readFileSync(new URL('../src/browser/diagram/konva-diagram-adapter.ts', import.meta.url), 'utf8');
    assert.match(source, /operationDurationMs/);
    assert.match(source, /frameWaitMs/);
});
