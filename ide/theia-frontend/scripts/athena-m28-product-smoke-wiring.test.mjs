import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const {
    buildAuthoringDecisionRequest,
    buildSemanticRelationshipPreviewRequest,
} = await import('../lib/browser/athena-authoring-protocol.js');
const {
    resolveRenderedSelectionTarget,
} = await import('../lib/browser/athena-semantic-selection-model.js');

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readJson(path) {
    return JSON.parse(readFileSync(resolve(repoRoot, path), 'utf8'));
}

test('M28 product smoke is wired to the openable sample project', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m28-sample-project.js');

    assert.equal(
        idePackage.scripts['start:m28'],
        'yarn workspace @engineeringood/athena-theia-product start:m28',
    );
    assert.equal(
        idePackage.scripts['start:smoke:m28'],
        'yarn workspace @engineeringood/athena-theia-product start:smoke:m28',
    );
    assert.equal(
        productPackage.scripts['start:m28'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m28/sample-project --active-view documentation',
    );
    assert.equal(
        productPackage.scripts['start:smoke:m28'],
        'node scripts/verify-athena-m28-sample-project.js',
    );
    assert.ok(existsSync(smokeScript), 'Missing M28 product smoke script.');
});

test('M28 product authoring path uses projection facts and generic semantic relationship payloads', () => {
    const source = resolveRenderedSelectionTarget({
        endpointId: 'documentation/projection/connection/c1/endpoint/source',
        anchorId: 'documentation/projection/label/port_ControllerPLC1_spareDo/anchor',
        portSemanticId: 'port:ControllerPLC1.spareDo',
        role: 'source',
    });
    const target = resolveRenderedSelectionTarget({
        id: 'visible-label-SpareTerminalXT99-in1',
        endpointId: 'documentation/projection/connection/candidate/endpoint/target',
        anchorId: 'documentation/projection/label/port_SpareTerminalXT99_in1/anchor',
        portSemanticId: 'port:SpareTerminalXT99.in1',
        role: 'target',
    });

    assert.equal(source?.semanticId, 'port:ControllerPLC1.spareDo');
    assert.equal(target?.semanticId, 'port:SpareTerminalXT99.in1');

    assert.deepEqual(
        buildSemanticRelationshipPreviewRequest({
            sourceSubjectId: source.semanticId,
            targetSubjectId: target.semanticId,
            projectionViewId: 'documentation',
            persistenceSourceUri: 'file:///workspace/src/01-relationship-authoring-source.athena',
            provenance: 'projection-fact-terminal',
            originDetail: 'graph:documentation',
            intentId: 'intent-m28-product-valid',
        }),
        {
            intentId: 'intent-m28-product-valid',
            intentKind: 'semantic-relationship',
            originSurface: 'graph',
            originDetail: 'graph:documentation',
            relationshipType: 'ElectricalConnectionRelationship',
            sourceSubjectId: 'port:ControllerPLC1.spareDo',
            targetSubjectId: 'port:SpareTerminalXT99.in1',
            projectionViewId: 'documentation',
            projectionOccurrenceId: undefined,
            persistenceSourceUri: 'file:///workspace/src/01-relationship-authoring-source.athena',
            provenance: 'projection-fact-terminal',
        },
    );

    assert.deepEqual(
        buildAuthoringDecisionRequest({
            previewId: 'authoring-preview-0001',
            intentId: 'intent-m28-product-valid',
            decision: 'accepted',
        }),
        {
            previewId: 'authoring-preview-0001',
            intentId: 'intent-m28-product-valid',
            decision: 'accepted',
            note: undefined,
        },
    );
});
