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

test('M26 sample project is an openable workspace with source/view anti-regression proof', () => {
    const sampleRoot = resolve(repoRoot, 'examples/m26/sample-project');
    const sourceA = readRepoFile('examples/m26/sample-project/src/01-workspace-semantic-source.athena');
    const sourceB = readRepoFile('examples/m26/sample-project/src/02-field-assets-not-a-sheet.athena');
    const readme = readRepoFile('examples/m26/sample-project/README.md');

    assert.equal(existsSync(resolve(sampleRoot, 'athena.yaml')), true);
    assert.equal(existsSync(resolve(sampleRoot, 'athena.lock')), true);
    assert.match(readme, /Power Distribution/);
    assert.match(readme, /Control And PLC Logic/);
    assert.match(readme, /Field Wiring And Terminal Transition/);
    assert.match(readme, /Source files are semantic workspace units, not\s+sheet-view boundaries/);

    for (const source of [sourceA, sourceB]) {
        assert.match(source, /^package com\.engineeringood\.m26\.sample/m);
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
    assert.match(sourceB, /FieldOutputModuleIOM1/);
    assert.match(sourceB, /FieldTerminalXT1/);
    assert.match(sourceB, /ConveyorMotorM1/);
    assert.match(`${sourceA}\n${sourceB}`, /FieldOutputModuleIOM1\.do1 -> FieldTerminalXT1\.in1/);
});

test('M26 usage documentation states projection proof and M25 comparison without new syntax', () => {
    const usage = readRepoFile('docs/usages/m26-proof-usage.md');

    assert.match(usage, /yarn --cwd ide start:m26/);
    assert.match(usage, /yarn --cwd ide start:smoke:m26/);
    assert.match(usage, /Power Distribution/);
    assert.match(usage, /Control And PLC Logic/);
    assert.match(usage, /Field Wiring And Terminal Transition/);
    assert.match(usage, /athena-document-projection-v0/);
    assert.match(usage, /documentProjectionId \+ sheetViewId \+ canonicalSubjectId \+ occurrenceRole \+ detailRole/);
    assert.match(usage, /M25 proved professional single-sheet presentation/);
    assert.match(usage, /M26 adds semantic document projection/);
    assert.match(usage, /\.athena source \+ compiler\/runtime semantic snapshot = engineering truth/);
    assert.match(usage, /Athena M26 sample project smoke passed/);
    assert.match(usage, /athena-m26-reference-marker-transport\.test\.mjs/);
    assert.doesNotMatch(usage, /\bfolio\b|document\s*\{|sheet\s+page|page\s*\{/i);
});

test('M26 product smoke is wired to the openable sample project', () => {
    const idePackage = JSON.parse(readRepoFile('ide/package.json'));
    const productPackage = JSON.parse(readRepoFile('ide/theia-product/package.json'));
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m26-sample-project.js');
    const opener = readRepoFile('ide/theia-product/scripts/athena-electron-open-workspace-main.js');

    assert.equal(idePackage.scripts['start:m26'], 'yarn workspace @engineeringood/athena-theia-product start:m26');
    assert.equal(idePackage.scripts['start:smoke:m26'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m26');
    assert.match(productPackage.scripts['start:m26'], /examples\/m26\/sample-project/);
    assert.equal(productPackage.scripts['start:smoke:m26'], 'node scripts/verify-athena-m26-sample-project.js');
    assert.match(smoke, /resolveM26SampleProject/);
    assert.match(smoke, /assertDocumentProjectionProof/);
    assert.match(smoke, /Power Distribution/);
    assert.match(smoke, /Control And PLC Logic/);
    assert.match(smoke, /Field Wiring And Terminal Transition/);
    assert.match(smoke, /verbose semantic route labels visible/);
    assert.match(opener, /collectDocumentProjectionProof/);
    assert.match(opener, /visibleLabelTexts/);
});

test('M26 retrospective keeps semantic document projection boundaries explicit', () => {
    const retrospective = readRepoFile(
        '_bmad-output/implementation-artifacts/m26/m26-retrospective-and-boundary-checks-2026-07-20.md'
    );

    assert.match(retrospective, /\.athena source \+ compiler\/runtime semantic snapshot/);
    assert.match(retrospective, /Document Projection IR owns/);
    assert.match(retrospective, /Presentation IR owns/);
    assert.match(retrospective, /Athena M26 sample project smoke passed/);
    assert.match(retrospective, /no `apps:desktop-viewer`/);
    assert.match(retrospective, /no `ui:compose-workbench`/);
    assert.match(retrospective, /no deprecated KMP or Compose desktop frontend module/);
    assert.match(retrospective, /future document syntax admission must update ANTLR4/);
    for (const deferred of [
        'PDF and print export',
        'terminal reports',
        'wire lists',
        'standards packs',
        'revision workflow',
        'automatic pagination'
    ]) {
        assert.match(retrospective, new RegExp(deferred));
    }
    assert.doesNotMatch(retrospective, /\bProject Folio\b|\bfolio page\b|page-authority source/i);
});
