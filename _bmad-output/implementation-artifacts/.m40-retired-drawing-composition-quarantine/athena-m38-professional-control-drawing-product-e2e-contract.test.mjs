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

test('M38 professional control drawing smoke is wired to its dedicated sample', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const verifierPath = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m38-professional-control-drawing.js');

    assert.equal(idePackage.scripts['start:m38'], 'yarn workspace @engineeringood/athena-theia-product start:m38');
    assert.equal(idePackage.scripts['start:smoke:m38'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m38');
    assert.equal(
        productPackage.scripts['start:m38'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m38/professional-control-drawing --active-view schematic',
    );
    assert.equal(productPackage.scripts['start:smoke:m38'], 'node scripts/verify-athena-m38-professional-control-drawing.js');
    assert.ok(existsSync(verifierPath), 'Missing M38 professional control drawing verifier.');
});

test('M38 verifier checks structural route proof trace evidence and screenshots', () => {
    const verifierSource = readRepoFile('ide/theia-product/scripts/verify-athena-m38-professional-control-drawing.js');

    assert.match(verifierSource, /ATHENA_M38_PROFESSIONAL_CONTROL_DRAWING_PRODUCT_PROOF=/);
    assert.match(verifierSource, /examples', 'm38', 'professional-control-drawing'/);
    assert.match(verifierSource, /01-professional-control-drawing\.athena/);
    assert.match(verifierSource, /m38-professional-control-drawing-desktop-1920x1080\.png/);
    assert.match(verifierSource, /m38-professional-control-drawing-desktop-1280x900\.png/);
    assert.match(verifierSource, /m38-professional-control-drawing-narrow\.png/);
    assert.match(verifierSource, /EXPECTED_ROUTE_COUNT = 9/);
    assert.match(verifierSource, /m38-bindings\.athena/);
    assert.match(verifierSource, /m38-elements\.athena/);
    assert.match(verifierSource, /assertZeroDefectProof/);
    assert.match(verifierSource, /assertProfessionalTraceProof/);
    assert.match(verifierSource, /assertRouteEvidenceProof/);
    assert.match(verifierSource, /assertScreenshotEvidenceProof/);
    assert.match(verifierSource, /compileToPresentationUnderTenSeconds/);
    assert.match(verifierSource, /looseEndpointsAbsent/);
    assert.match(verifierSource, /fallbackAuthorityAbsent/);
    assert.match(verifierSource, /routeBodyIntersectionsAbsent/);
    assert.match(verifierSource, /ambiguousCrossingsAbsent/);
    assert.match(verifierSource, /labelCollisionsAbsent/);
    assert.match(verifierSource, /unclassifiedRoutesAbsent/);
    assert.match(verifierSource, /routeIntentId/);
    assert.match(verifierSource, /presentationClassId/);
    assert.match(verifierSource, /compilerSnapshotId/);
    assert.match(verifierSource, /packageResourceIds/);
    assert.match(verifierSource, /sourceSpan/);
    assert.match(verifierSource, /rawMarkupAuthorityAbsent/);
    assert.match(verifierSource, /rendererEngineeringInference/);
    assert.doesNotMatch(verifierSource, /examples', 'm36'/);
    assert.doesNotMatch(verifierSource, /examples', 'm37'/);
    assert.doesNotMatch(verifierSource, /ATHENA_M37_PROFESSIONAL_CONTROL_DRAWING_PRODUCT_PROOF/);
    assert.doesNotMatch(verifierSource, /examples', 'm35'/);
    assert.doesNotMatch(verifierSource, /ATHENA_M36_CONNECTIVITY_CABINET_PRODUCT_PROOF/);
});
