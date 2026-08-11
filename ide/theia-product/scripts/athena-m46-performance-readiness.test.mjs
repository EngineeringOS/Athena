import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

test('M46 performance waits for accepted workspace state before benchmarking', () => {
    const source = readFileSync(new URL('./athena-m46-performance-main.js', import.meta.url), 'utf8');
    assert.match(source, /const state = await acceptedState\(window\);/);
    assert.match(source, /state\.workbench\.workspaceOpened/);
    assert.match(source, /state\.workbench\.repositoryLifecycle\?\.toUpperCase\(\) === 'READY'/);
    assert.match(source, /normalize\(state\.workbench\.lspRepositoryRoot\) === expected/);
    assert.match(source, /state\.connectionReadModel\.state === 'READY'/);
    assert.match(source, /state\.presentation\.publicationState === 'READY'/);
    assert.match(source, /visibleRootCandidates/);
});
