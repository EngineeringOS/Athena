import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const root = path.resolve(import.meta.dirname, '..');
const adapter = fs.readFileSync(path.join(root, 'src/browser/diagram/konva-diagram-adapter.ts'), 'utf8');
const fixture = JSON.parse(fs.readFileSync(path.resolve(root, '..', '..', 'contracts/presentation/v1/benchmark/scene-100k-manifest.json'), 'utf8'));
const transcript = JSON.parse(fs.readFileSync(path.resolve(root, '..', '..', 'contracts/presentation/v1/benchmark/interaction-transcript.json'), 'utf8'));
const packageJson = JSON.parse(fs.readFileSync(path.join(root, 'package.json'), 'utf8'));

test('normative scale fixture stays locked to M43 contract', () => {
    assert.deepEqual(fixture, {
        paintElementCount: 100000,
        visiblePercent: 5,
        viewportCss: { width: 1600, height: 1000 },
        cssDpr: 1,
        warmupFrames: 60,
        measuredFrames: 300,
        gates: { firstStablePaintMs: 5000, incrementalHeapMiB: 512, interactionP95Ms: 50, selectionP95Ms: 100, identityErrors: 0 },
    });
    assert.deepEqual(transcript.operations, ['pan', 'zoom', 'select', 'drag']);
    assert.equal(transcript.warmupFrames, fixture.warmupFrames);
    assert.equal(transcript.measuredFrames, fixture.measuredFrames);
});

test('scale path remains one scene, one Konva adapter, bounded paint', () => {
    assert.equal(packageJson.dependencies.konva, '10.3.0');
    assert.match(adapter, /runScaleBenchmark/);
    assert.match(adapter, /visibleSceneBounds/);
    assert.match(adapter, /maybeRedrawVisibleContent/);
    assert.match(adapter, /Path2D/);
    assert.match(adapter, /drawDenseOccurrenceBatch/);
    assert.doesNotMatch(adapter, /from ['"]pixi\.js['"]/);
    assert.doesNotMatch(adapter, /WebGPU|webgpu|GLSP/);
});
