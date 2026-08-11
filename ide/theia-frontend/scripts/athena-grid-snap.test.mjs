import test from 'node:test';
import assert from 'node:assert/strict';
import { snapSheetPoint } from '../lib/browser/diagram/grid-snap.js';

test('canonical Sheet points snap on an independent step', () => {
    assert.deepEqual(snapSheetPoint({ x: 1, y: 1 }, 1), { x: 1, y: 1 });
    assert.deepEqual(snapSheetPoint({ x: 9, y: 13 }, 4), { x: 8, y: 12 });
    assert.deepEqual(snapSheetPoint({ x: 10, y: 14 }, 4), { x: 12, y: 16 });
});

test('invalid Sheet points and snap steps fail closed', () => {
    assert.throws(() => snapSheetPoint({ x: 0, y: 1 }, 1));
    assert.throws(() => snapSheetPoint({ x: 1, y: 1 }, 0));
    assert.throws(() => snapSheetPoint({ x: 1.5, y: 1 }, 1));
});
