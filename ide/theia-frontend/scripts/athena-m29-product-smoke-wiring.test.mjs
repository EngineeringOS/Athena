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

test('M29 product smoke is wired to the openable sample project', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m29-sample-project.js');

    assert.equal(
        idePackage.scripts['start:m29'],
        'yarn workspace @engineeringood/athena-theia-product start:m29',
    );
    assert.equal(
        idePackage.scripts['start:smoke:m29'],
        'yarn workspace @engineeringood/athena-theia-product start:smoke:m29',
    );
    assert.equal(
        productPackage.scripts['start:m29'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m29/sample-project --active-view documentation',
    );
    assert.equal(
        productPackage.scripts['start:smoke:m29'],
        'node scripts/verify-athena-m29-sample-project.js',
    );
    assert.ok(existsSync(smokeScript), 'Missing M29 product smoke script.');
});

test('M29 product smoke asserts structured Interaction proof payloads before UI proof', () => {
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m29-sample-project.js');
    const structuredProofIndex = smoke.indexOf('assertStructuredInteractionProofPayloads');
    const graphProofIndex = smoke.indexOf('assertGraphWorkbenchProof');

    assert.ok(structuredProofIndex >= 0, 'M29 smoke must assert structured Interaction proof payloads.');
    assert.ok(graphProofIndex >= 0, 'M29 smoke should keep UI graph proof as secondary coverage.');
    assert.ok(
        structuredProofIndex < graphProofIndex,
        'Structured Interaction proof must run before graph/UI proof assertions.',
    );

    [
        'subject-registry',
        'action-discovery',
        'reveal-source-graph-inspector-problems',
        'relationship-preview',
        'relationship-accept',
        'entity-creation-preview',
        'entity-creation-accept',
        'preview-stale-clearing',
        'legacy-connect-ports-inventory',
    ].forEach(proofKind => assert.match(smoke, new RegExp(proofKind)));

    assert.doesNotMatch(smoke, /innerText|textContent|querySelectorAll\([^)]*text/i);
});

test('M29 structured proof payload inventory is executable without DOM semantic authority', () => {
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m29-sample-project.js');
    const {
        buildStructuredInteractionProofPayloads,
        assertStructuredInteractionProofPayloads,
        REQUIRED_INTERACTION_PROOF_KINDS,
    } = require(smokeScript);
    const sampleProject = resolve(repoRoot, 'examples/m29/sample-project');

    const payloads = buildStructuredInteractionProofPayloads(sampleProject);
    assertStructuredInteractionProofPayloads(payloads);

    assert.deepEqual(
        payloads.map(payload => payload.payload.proofKind),
        REQUIRED_INTERACTION_PROOF_KINDS,
    );
    assert.ok(payloads.every(payload => payload.schemaVersion === 'm29.interaction.v1'));
    assert.ok(payloads.every(payload => payload.payloadKind === 'proof'));
    assert.ok(payloads.every(payload => payload.payload.activeSourceContext));

    const serialized = JSON.stringify(payloads);
    assert.doesNotMatch(serialized, /innerText|textContent|svgX|svgY|screenX|screenY/);
    assert.match(serialized, /semantic-relationship/);
    assert.match(serialized, /backend-runtime\/source-edit/);
});

test('shared product smoke opener uses Athena command hook before DOM fallback', () => {
    const opener = readRepoFile('ide/theia-product/scripts/athena-electron-open-workspace-main.js');
    const contribution = readRepoFile('ide/theia-frontend/src/browser/athena-product-contribution.ts');

    assert.match(contribution, /__athenaWorkbenchSmoke/);
    assert.match(contribution, /commands\.executeCommand\(AthenaCommands\.REVEAL_GRAPHICAL_VIEW\.id\)/);

    const hookIndex = opener.indexOf('Athena workbench smoke command hook');
    const domFallbackIndex = opener.indexOf('revealGraphicalViewThroughDom');
    assert.ok(hookIndex >= 0, 'Electron opener must wait for the Athena command hook.');
    assert.ok(domFallbackIndex >= 0, 'Electron opener should retain DOM fallback.');
    assert.ok(hookIndex < domFallbackIndex, 'Command hook must be attempted before DOM fallback.');
});
