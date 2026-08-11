import assert from 'node:assert/strict';
import test from 'node:test';

const { SceneSpatialIndex } = await import('../lib/browser/diagram/scene-spatial-index.js');

const item = (id, x, y, width = 4, height = 4) => ({
    id,
    bounds: { x, y, width, height },
});

test('spatial index returns intersecting items once across bucket boundaries', () => {
    const index = new SceneSpatialIndex([
        item('left', 0, 0),
        item('boundary', 124, 0, 12, 12),
        item('right', 136, 0),
        item('far', 10_000, 10_000),
    ], 128);

    assert.deepEqual(
        index.query({ x: 120, y: -2, width: 24, height: 20 }).map(entry => entry.id),
        ['boundary', 'right'],
    );
});

test('spatial index preserves source order for deterministic paint and hit identity', () => {
    const entries = Array.from({ length: 1000 }, (_, id) => item(`item-${id}`, id % 100, Math.floor(id / 100)));
    const index = new SceneSpatialIndex(entries, 16);

    assert.deepEqual(
        index.query({ x: 0, y: 0, width: 100, height: 10 }).map(entry => entry.id),
        entries.slice(0, 1000).filter(entry => entry.bounds.y < 10).map(entry => entry.id),
    );
});
