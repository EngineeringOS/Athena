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

test('M39 product smoke is wired to its dedicated example', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const verifierPath = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m39-product-proof.js');

    assert.equal(idePackage.scripts['start:m39'], 'yarn workspace @engineeringood/athena-theia-product start:m39');
    assert.equal(idePackage.scripts['start:smoke:m39'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m39');
    assert.equal(
        productPackage.scripts['start:m39'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m39/reality-product-proof --active-view schematic',
    );
    assert.equal(productPackage.scripts['start:smoke:m39'], 'node scripts/verify-athena-m39-product-proof.js');
    assert.ok(existsSync(verifierPath), 'Missing M39 product verifier.');
});

test('M39 verifier checks reality chain paint-only proof and screenshots', () => {
    const verifierSource = readRepoFile('ide/theia-product/scripts/verify-athena-m39-product-proof.js');

    assert.match(verifierSource, /ATHENA_M39_PRODUCT_PROOF=/);
    assert.match(verifierSource, /examples', 'm39', 'reality-product-proof'/);
    assert.match(verifierSource, /01-reality-product-proof\.athena/);
    assert.match(verifierSource, /m39-reality-product-proof-desktop-1920x1080\.png/);
    assert.match(verifierSource, /m39-reality-product-proof-desktop-1280x900\.png/);
    assert.match(verifierSource, /m39-reality-product-proof-narrow\.png/);
    assert.match(verifierSource, /engineeringReality/);
    assert.match(verifierSource, /projectionReality/);
    assert.match(verifierSource, /spatialReality/);
    assert.match(verifierSource, /presentationReality/);
    assert.match(verifierSource, /paintOnlyRenderer/);
    assert.match(verifierSource, /screenshots/);
    assert.doesNotMatch(verifierSource, /examples', 'm36'/);
    assert.doesNotMatch(verifierSource, /examples', 'm37'/);
    assert.doesNotMatch(verifierSource, /examples', 'm38'/);
    assert.doesNotMatch(verifierSource, /ATHENA_M3[678]_/);
    assert.doesNotMatch(verifierSource, /ProfessionalControlDrawing/);
    assert.doesNotMatch(verifierSource, /Java2D/);
    assert.doesNotMatch(verifierSource, /routeIntentId/);
});
