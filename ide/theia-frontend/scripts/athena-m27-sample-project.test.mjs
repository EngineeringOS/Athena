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

test('M27 sample project is an openable semantic proof workspace', () => {
    const sampleRoot = resolve(repoRoot, 'examples/m27/sample-project');
    const sourceA = readRepoFile('examples/m27/sample-project/src/01-workspace-semantic-source.athena');
    const sourceB = readRepoFile('examples/m27/sample-project/src/02-field-assets-not-a-sheet.athena');
    const readme = readRepoFile('examples/m27/sample-project/README.md');

    assert.equal(existsSync(resolve(sampleRoot, 'athena.yaml')), true);
    assert.equal(existsSync(resolve(sampleRoot, 'athena.lock')), true);
    assert.match(readme, /yarn --cwd ide start:m27/);
    assert.match(readme, /Source files are semantic workspace units, not\s+sheet-view boundaries/);

    for (const source of [sourceA, sourceB]) {
        assert.match(source, /^package com\.engineeringood\.m27\.sample/m);
        assert.match(source, /\bsystem\b/);
        assert.match(source, /\bdevice\b/);
        assert.match(source, /\bport\b/);
        assert.match(source, /\bconnect\b/);
        assert.doesNotMatch(source, /\bfolio\b|document\s*\{|sheet\s+page|page\s*\{/i);
    }

    assert.match(sourceA, /MainPowerSupplyPS1/);
    assert.match(sourceA, /MainBreakerQF1/);
    assert.match(sourceA, /ControllerPLC1/);
    assert.match(sourceA, /OperatorHMI1/);
    assert.match(sourceA, /FieldOutputModuleIOM1/);
    assert.match(sourceA, /FieldTerminalXT1/);
    assert.match(sourceA, /ConveyorMotorM1/);
    assert.match(sourceB, /SpareFieldGatewayGW1/);
    assert.match(sourceB, /SpareTerminalXT99/);

    const combined = `${sourceA}\n${sourceB}`;
    assert.match(combined, /MainPowerSupplyPS1\.lplus -> MainBreakerQF1\.line/);
    assert.match(combined, /ControllerPLC1\.hmiStatus -> OperatorHMI1\.status/);
    assert.match(combined, /FieldOutputModuleIOM1\.do1 -> FieldTerminalXT1\.in1/);
    assert.match(combined, /FieldTerminalXT1\.out1 -> ConveyorMotorM1\.u1/);
    assert.match(sourceB, /SpareFieldGatewayGW1\.do1 -> SpareTerminalXT99\.in1/);
    assert.doesNotMatch(sourceB, /FieldOutputModuleIOM1|FieldTerminalXT1|ConveyorMotorM1/);
    assert.doesNotMatch(sourceB, /device\s+ControllerPLC1\b/);
});

test('M27 product smoke is wired to the openable sample project', () => {
    const idePackage = JSON.parse(readRepoFile('ide/package.json'));
    const productPackage = JSON.parse(readRepoFile('ide/theia-product/package.json'));
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m27-sample-project.js');
    const opener = readRepoFile('ide/theia-product/scripts/athena-electron-open-workspace-main.js');

    assert.equal(idePackage.scripts['start:m27'], 'yarn workspace @engineeringood/athena-theia-product start:m27');
    assert.equal(idePackage.scripts['start:smoke:m27'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m27');
    assert.match(productPackage.scripts['start:m27'], /examples\/m27\/sample-project/);
    assert.match(productPackage.scripts['start:m27'], /--active-view documentation/);
    assert.equal(productPackage.scripts['start:smoke:m27'], 'node scripts/verify-athena-m27-sample-project.js');
    assert.match(smoke, /resolveM27SampleProject/);
    assert.match(smoke, /assertGraphWorkbenchProof/);
    assert.match(smoke, /assertRouteProof/);
    assert.match(smoke, /assertRepresentationProof/);
    assert.match(smoke, /assertDocumentProjectionProof/);
    assert.match(smoke, /assertSheetSurfaceProof/);
    assert.match(smoke, /assertDensityProof/);
    assert.match(smoke, /assertVisualProof/);
    assert.match(smoke, /assertAllSheetVisualProof/);
    assert.match(smoke, /assertGraphWorkbenchScreenshot/);
    assert.match(smoke, /examples', 'm27', 'sample-project/);
    assert.match(opener, /collectRouteProof/);
    assert.match(opener, /collectRepresentationProof/);
    assert.match(opener, /collectDocumentProjectionProof/);
    assert.match(opener, /collectSheetSurfaceProof/);
    assert.match(opener, /collectDensityProof/);
    assert.match(opener, /collectAllSheetVisualProofs/);
    assert.match(opener, /collectVisualProof/);
    assert.match(opener, /captureGraphWorkbenchScreenshot/);
    assert.match(opener, /resolveRequestedActiveView/);
    assert.match(opener, /data-athena-route-points/);
});
