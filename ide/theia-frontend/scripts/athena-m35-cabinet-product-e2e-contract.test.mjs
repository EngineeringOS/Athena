import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const require = createRequire(import.meta.url);
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

test('M35 Cabinet product smoke is wired as a first-class verifier', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const verifierPath = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m35-physical-installation-cabinet.js');

    assert.equal(
        idePackage.scripts['start:m35'],
        'yarn workspace @engineeringood/athena-theia-product start:m35',
    );
    assert.equal(
        idePackage.scripts['start:smoke:m35'],
        'yarn workspace @engineeringood/athena-theia-product start:smoke:m35',
    );
    assert.equal(
        productPackage.scripts['start:m35'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m35/physical-installation-cabinet --active-view cabinet',
    );
    assert.equal(
        productPackage.scripts['start:smoke:m35'],
        'node scripts/verify-athena-m35-physical-installation-cabinet.js',
    );
    assert.ok(existsSync(verifierPath), 'Missing M35 Cabinet product verifier.');
});

test('M35 verifier is falsifiable and bound to the dedicated Cabinet sample', () => {
    const verifierSource = readRepoFile('ide/theia-product/scripts/verify-athena-m35-physical-installation-cabinet.js');

    assert.match(verifierSource, /ATHENA_M35_CABINET_PRODUCT_PROOF=/);
    assert.match(verifierSource, /examples', 'm35', 'physical-installation-cabinet'/);
    assert.match(verifierSource, /01-physical-installation-cabinet\.athena/);
    assert.match(verifierSource, /m35-cabinet-product-smoke-desktop-1920x1080\.png/);
    assert.match(verifierSource, /m35-cabinet-product-smoke-desktop-1280x900\.png/);
    assert.match(verifierSource, /m35-cabinet-product-smoke-narrow\.png/);
    assert.match(verifierSource, /assertCabinetProductProof/);
    assert.match(verifierSource, /assertScreenshotEvidenceProof/);
    assert.match(verifierSource, /assertCabinetAuthorityProof/);
    assert.match(verifierSource, /assertRouteProof/);
    assert.match(verifierSource, /assertTraceProof/);
    assert.match(verifierSource, /zeroProblemMarkerCount/);
    assert.match(verifierSource, /problemMarkerCount === 0/);
    assert.match(verifierSource, /xmlRuntimeAuthorityAbsent/);
    assert.match(verifierSource, /rawMarkupAuthorityAbsent/);
    assert.match(verifierSource, /fallbackAuthorityAbsent/);
    assert.match(verifierSource, /requiredIdentitiesPresent/);
    assert.match(verifierSource, /requiredBodyIntersectionsAbsent/);
    assert.match(readRepoFile('ide/theia-product/scripts/athena-electron-open-workspace-main.js'), /governed Cabinet render proof/);
    assert.doesNotMatch(verifierSource, /m33-cabinet-products\.xml|m33-iec\.xml|m33-cabinet-bindings\.xml|m33-iec-symbols\.xml/);
});

test('M35 screenshot readiness ignores stale preload nodes that do not occlude the Cabinet workbench', () => {
    const { isBlockingScreenshotSpinner } = require(resolve(
        repoRoot,
        'ide/theia-product/scripts/athena-electron-open-workspace-main.js',
    ));

    const captureRect = { left: 100, top: 80, right: 900, bottom: 680, width: 800, height: 600 };
    const stalePreload = {
        className: 'theia-preload',
        display: 'flex',
        visibility: 'visible',
        opacity: '1',
        left: 0,
        top: 0,
        right: 1200,
        bottom: 900,
        width: 1200,
        height: 900,
        frontmostAtProbePoint: false,
    };
    const realOverlay = {
        ...stalePreload,
        frontmostAtProbePoint: true,
    };

    assert.equal(isBlockingScreenshotSpinner(stalePreload, captureRect), false);
    assert.equal(isBlockingScreenshotSpinner(realOverlay, captureRect), true);
    assert.equal(
        isBlockingScreenshotSpinner({ ...realOverlay, display: 'none' }, captureRect),
        false,
    );
});
