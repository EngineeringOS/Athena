import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => {
    try {
        return !!readFileSync(resolve(candidate, 'ide/theia-product/package.json'), 'utf8');
    } catch {
        return false;
    }
}) ?? process.cwd();

test('Athena Theia product reveals an already-open editor instead of spawning a duplicate pane', () => {
    const productPackage = JSON.parse(readFileSync(resolve(repoRoot, 'ide/theia-product/package.json'), 'utf8'));
    const preferences = productPackage?.theia?.frontend?.config?.preferences ?? {};
    const usage = readFileSync(resolve(repoRoot, 'docs/usages/m21-proof-usage.md'), 'utf8');

    assert.equal(preferences['editor.enablePreview'], false);
    assert.equal(preferences['workbench.editor.revealIfOpen'], true);
    assert.match(usage, /use outline navigation in the same editor tab for `\.athena` files/);
    assert.match(usage, /inspect source and Problems against the same canonical subject/);
});
