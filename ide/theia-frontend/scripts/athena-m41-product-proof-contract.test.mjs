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

test('M41 product smoke is wired to its dedicated example', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const verifierPath = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m41-product-proof.js');

    assert.equal(idePackage.scripts['start:m41'], 'yarn workspace @engineeringood/athena-theia-product start:m41');
    assert.equal(idePackage.scripts['start:smoke:m41'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m41');
    assert.equal(
        productPackage.scripts['start:m41'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m41/rolling-shutter --active-view schematic',
    );
    assert.equal(productPackage.scripts['start:smoke:m41'], 'node scripts/verify-athena-m41-product-proof.js');
    assert.ok(existsSync(verifierPath), 'Missing M41 product verifier.');
});

test('M41 verifier checks paint-only proof and screenshots under M41 artifacts', () => {
    const verifierSource = readRepoFile('ide/theia-product/scripts/verify-athena-m41-product-proof.js');

    assert.match(verifierSource, /ATHENA_M41_PRODUCT_PROOF=/);
    assert.match(verifierSource, /examples', 'm41', 'rolling-shutter'/);
    assert.match(verifierSource, /01-rolling-shutter-spatial\.athena/);
    assert.match(verifierSource, /m41-rolling-shutter-desktop-1920x1080\.png/);
    assert.match(verifierSource, /m41-rolling-shutter-narrow\.png/);
    assert.match(verifierSource, /control-drawing/);
    assert.match(verifierSource, /paintOnlyRenderer/);
    assert.doesNotMatch(verifierSource, /examples', 'm40'/);
    assert.doesNotMatch(verifierSource, /ProfessionalControlDrawing/);
});
