import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

function assertFile(path) {
    assert.equal(existsSync(resolve(repoRoot, path)), true, `${path} should exist`);
}

test('M22 selects a minimal source layout-hint syntax before round-trip mutation', () => {
    const syntaxPath = '_bmad-output/implementation-artifacts/m22/M22-LAYOUT-HINT-SYNTAX.md';
    assertFile(syntaxPath);

    const syntax = readRepoFile(syntaxPath);
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');

    assert.match(syntax, /^# M22 Layout Hint Syntax Decision/m);
    assert.match(syntax, /Decision: layout block/i);
    assert.match(syntax, /place/i);
    assert.match(syntax, /align/i);
    assert.match(syntax, /group/i);
    assert.match(syntax, /near/i);
    assert.match(syntax, /below/i);
    assert.match(syntax, /aligned-with/i);
    assert.match(syntax, /grouped-with/i);
    assert.match(syntax, /raw pixel coordinates are not the primary authored language/i);
    assert.match(syntax, /route and label persistence remains deferred/i);
    assert.match(usage, /M22-LAYOUT-HINT-SYNTAX\.md/);
});
