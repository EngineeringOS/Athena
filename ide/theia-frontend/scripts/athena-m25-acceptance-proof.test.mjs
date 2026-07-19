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

test('M25 acceptance proof compares M24 generic routes with governed presentation anatomy', () => {
    const acceptance = readRepoFile('docs/usages/m25-representation-acceptance-proof.md');
    const usage = readRepoFile('docs/usages/m25-proof-usage.md');

    assert.match(acceptance, /M24.*generic-box.*route/i);
    assert.match(acceptance, /M25.*presentation anatomy/i);
    assert.match(acceptance, /terminal markers/i);
    assert.match(acceptance, /terminal numbers/i);
    assert.match(acceptance, /label anchors/i);
    assert.match(acceptance, /zero-fallback/i);
    assert.match(acceptance, /QElectroTech-inspired/i);
    assert.match(acceptance, /documentation-only/i);
    assert.match(acceptance, /no QElectroTech import/i);
    assert.match(acceptance, /no IEC completeness/i);
    assert.match(acceptance, /no EPLAN parity/i);
    assert.match(usage, /m25-representation-acceptance-proof\.md/);
    assert.match(usage, /examples\/m25\/sample-project/);

    [
        /QElectroTech import is supported/i,
        /IEC completeness is supported/i,
        /EPLAN parity is supported/i,
        /M25 implements full IEC/i,
        /M25 imports QElectroTech/i,
    ].forEach(overclaim => {
        assert.doesNotMatch(acceptance, overclaim);
        assert.doesNotMatch(usage, overclaim);
    });
});
