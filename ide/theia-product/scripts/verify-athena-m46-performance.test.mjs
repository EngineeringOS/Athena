import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

test('M46 verifier measures synchronous adapter operation for every interaction budget', () => {
    const source = readFileSync(new URL('./verify-athena-m46-performance.js', import.meta.url), 'utf8');
    assert.match(source, /const operationSamples = kind => raw\.samples/);
    assert.match(source, /p95\(operationSamples\('PAN_ZOOM'\)\)/);
    assert.match(source, /p95\(operationSamples\('SELECTION'\)\)/);
    assert.match(source, /p95\(operationSamples\('LOCAL_REPLAN'\)\)/);
    assert.match(source, /frame wait[\s\S]*Electron compositor scheduling/i);
});
