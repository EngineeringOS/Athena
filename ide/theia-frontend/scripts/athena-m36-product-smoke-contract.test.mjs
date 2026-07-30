import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readJson(path) {
    return JSON.parse(readFileSync(resolve(repoRoot, path), 'utf8'));
}

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

test('M36 connectivity Cabinet smoke is wired to its dedicated sample', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const verifierPath = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m36-connectivity-cabinet.js');

    assert.equal(idePackage.scripts['start:m36'], 'yarn workspace @engineeringood/athena-theia-product start:m36');
    assert.equal(idePackage.scripts['start:smoke:m36'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m36');
    assert.equal(
        productPackage.scripts['start:m36'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m36/connectivity-cabinet --active-view cabinet',
    );
    assert.equal(productPackage.scripts['start:smoke:m36'], 'node scripts/verify-athena-m36-connectivity-cabinet.js');
    assert.ok(existsSync(verifierPath), 'Missing M36 connectivity Cabinet verifier.');
});

test('M36 verifier checks semantic connectivity, governed geometry, and screenshots', () => {
    const verifierSource = readRepoFile('ide/theia-product/scripts/verify-athena-m36-connectivity-cabinet.js');

    assert.match(verifierSource, /ATHENA_M36_CONNECTIVITY_CABINET_PRODUCT_PROOF=/);
    assert.match(verifierSource, /examples', 'm36', 'connectivity-cabinet'/);
    assert.match(verifierSource, /01-connectivity-cabinet\.athena/);
    assert.match(verifierSource, /EXPECTED_ROUTE_COUNT = 31/);
    assert.match(verifierSource, /component:PFEA112/);
    assert.match(verifierSource, /component:FeedbackJunction35/);
    assert.match(verifierSource, /assertScreenshotEvidenceProof/);
    assert.match(verifierSource, /assertCabinetAuthorityProof/);
    assert.match(verifierSource, /assertRouteProof/);
    assert.match(verifierSource, /assertTraceProof/);
    assert.match(verifierSource, /xmlRuntimeAuthorityAbsent/);
    assert.match(verifierSource, /rawMarkupAuthorityAbsent/);
    assert.match(verifierSource, /fallbackAuthorityAbsent/);
    assert.match(verifierSource, /requiredBodyIntersectionsAbsent/);
    assert.doesNotMatch(verifierSource, /examples', 'm35'/);
});
