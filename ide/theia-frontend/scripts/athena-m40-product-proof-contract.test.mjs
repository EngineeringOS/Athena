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

test('M40 product smoke is wired to its dedicated example', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const verifierPath = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m40-product-proof.js');

    assert.equal(idePackage.scripts['start:m40'], 'yarn workspace @engineeringood/athena-theia-product start:m40');
    assert.equal(idePackage.scripts['start:smoke:m40'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m40');
    assert.equal(
        productPackage.scripts['start:m40'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m40/rolling-shutter-control --active-view schematic',
    );
    assert.equal(productPackage.scripts['start:smoke:m40'], 'node scripts/verify-athena-m40-product-proof.js');
    assert.ok(existsSync(verifierPath), 'Missing M40 product verifier.');
});

test('M40 verifier checks paint-only proof and screenshots under M40 artifacts', () => {
    const verifierSource = readRepoFile('ide/theia-product/scripts/verify-athena-m40-product-proof.js');

    assert.match(verifierSource, /ATHENA_M40_PRODUCT_PROOF=/);
    assert.match(verifierSource, /examples', 'm40', 'rolling-shutter-control'/);
    assert.match(verifierSource, /01-rolling-shutter-control\.athena/);
    assert.match(verifierSource, /m40-rolling-shutter-control-desktop-1920x1080\.png/);
    assert.match(verifierSource, /m40-rolling-shutter-control-narrow\.png/);
    assert.match(verifierSource, /control-drawing/);
    assert.match(verifierSource, /paintOnlyRenderer/);
    assert.match(verifierSource, /screenshots/);
    assert.doesNotMatch(verifierSource, /examples', 'm3[6-9]'/);
    assert.doesNotMatch(verifierSource, /ProfessionalControlDrawing/);
    assert.doesNotMatch(verifierSource, /Java2D/);
});
