import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
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
    assert.equal(
        productPackage.scripts['start:smoke:m41'],
        'yarn --cwd .. build && node scripts/verify-athena-m41-product-proof.js'
    );
    assert.ok(existsSync(verifierPath), 'Missing M41 product verifier.');
});

test('M41 verifier rejects proof without runtime Spatial authority', () => {
    const require = createRequire(import.meta.url);
    const verifier = require(resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m41-product-proof.js'));
    const sourceUri = verifier.resolveM41SourceUri();
    const proof = {
        schemaVersion: 'M41.product-proof',
        sourceUri,
        sourceSha256: verifier.resolveM41SourceSha256(),
        viewports: ['desktop-1920x1080', 'narrow'].map(viewportName => ({
            viewportName,
            sourceUri,
            requestedSurface: viewportName === 'desktop-1920x1080'
                ? { width: 1920, height: 1080 }
                : { width: 720, height: 900 },
            activeViewId: 'schematic',
            projectionReality: {
                productSurfaceIds: ['control-drawing'],
                backingViewIds: ['schematic'],
                activeSheetId: 'schematic/sheet/S1',
                componentIds: [],
                connectionIds: [],
            },
            spatialReality: { proof: null, activeSheet: null },
            presentationReality: { occurrences: [], connectors: [] },
            pixelReality: {},
            screenshot: {},
        })),
    };

    assert.throws(() => verifier.assertM41ProductProof(proof), error => error.failedAuthority === 'spatial');
});
