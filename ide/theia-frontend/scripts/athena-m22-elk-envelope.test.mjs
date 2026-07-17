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

test('M22 records the ELK spike envelope before adapter implementation', () => {
    const envelopePath = '_bmad-output/implementation-artifacts/m22/M22-ELK-SPIKE-ENVELOPE.md';
    assertFile(envelopePath);

    const envelope = readRepoFile(envelopePath);
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');

    assert.match(envelope, /^# M22 ELK Spike Envelope/m);
    assert.match(envelope, /Decision: isolated experimental adapter/i);
    assert.match(envelope, /no direct renderer dependency/i);
    assert.match(envelope, /no frontend runtime dependency/i);
    assert.match(envelope, /local-only/i);
    assert.match(envelope, /no remote service tier/i);
    assert.match(envelope, /removable without changing layout facts/i);
    assert.match(envelope, /removable without changing renderer contracts/i);
    assert.match(envelope, /does not select ELK as final/i);
    assert.match(usage, /M22-ELK-SPIKE-ENVELOPE\.md/);
});
