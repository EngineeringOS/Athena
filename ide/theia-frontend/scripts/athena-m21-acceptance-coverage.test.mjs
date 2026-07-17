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

function assertContains(path, pattern, message) {
    assert.match(readRepoFile(path), pattern, message ?? `${path} should contain ${pattern}`);
}

test('M21 acceptance coverage names every required proof layer', () => {
    const usage = readRepoFile('docs/usages/m21-proof-usage.md');

    [
        /:kernel:layout-engine:test/,
        /:kernel:routing-model:test/,
        /athena-m21-boundary\.test\.mjs/,
        /athena-theia-editor-navigation\.test\.mjs/,
        /athena-m21-sample-project\.test\.mjs/,
        /athena-m21-graph-workbench-visual-proof\.test\.mjs/,
        /start:smoke:m21/,
        /encoding-audit\.ps1/,
    ].forEach(pattern => assert.match(usage, pattern));
});

test('M21 proof scripts cover sample identity, same-tab navigation, and graph workbench DOM evidence', () => {
    assertContains(
        'ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs',
        /M21 routing sample preserves source endpoint identity vocabulary/,
    );
    assertContains(
        'ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs',
        /workbench\.editor\.revealIfOpen/,
    );

    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m21-sample-project.js');
    [
        /ATHENA_GRAPH_WORKBENCH_PROOF=/,
        /stageHasGrid/,
        /sheetTransparent/,
        /floatingBarTransparent/,
        /bottomDockTransparent/,
        /zoomDockTransparent/,
        /infoPopoverOpened/,
        /infoPopoverClosedOnWhitespace/,
    ].forEach(pattern => assert.match(smoke, pattern));
});

test('M21 kernel tests cover deterministic layout route and label facts', () => {
    assertContains(
        'kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt',
        /region facts remain deterministic across repeated runs/,
    );
    assertContains(
        'kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicRoutingModelTest.kt',
        /route facts remain deterministic across repeated runs/,
    );
    assertContains(
        'kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicLabelModelTest.kt',
        /label facts remain deterministic across repeated runs/,
    );
});
