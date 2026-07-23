import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const graphWorkbenchSource = await readFile(
    new URL('../src/browser/athena-graph-workbench-widget.tsx', import.meta.url),
    'utf8',
);
const graphWorkbenchStyles = await readFile(
    new URL('../src/browser/style/index.css', import.meta.url),
    'utf8',
);
const electronSmokeSource = await readFile(
    new URL('../../theia-product/scripts/athena-electron-open-workspace-main.js', import.meta.url),
    'utf8',
);
const m32SmokeSource = await readFile(
    new URL('../../theia-product/scripts/verify-athena-m32-sample-project.js', import.meta.url),
    'utf8',
);

function methodBody(source, methodName) {
    const methodPattern = new RegExp(`protected\\s+(?:async\\s+)?${methodName}\\b`);
    const match = source.match(methodPattern);
    const start = match?.index ?? -1;
    assert.notEqual(start, -1, `${methodName} should exist`);
    const nextMethod = source.indexOf('\n    protected ', start + methodName.length);
    return source.slice(start, nextMethod === -1 ? source.length : nextMethod);
}

test('Graph View add button opens a governed create panel without requiring current editor focus', () => {
    const actionButtonBody = methodBody(graphWorkbenchSource, 'renderCreateEntityActionButton');
    const controlsBody = methodBody(graphWorkbenchSource, 'renderCreateEntityControls');
    const renderBody = methodBody(graphWorkbenchSource, 'render');
    const stageChromeBody = methodBody(graphWorkbenchSource, 'renderStageChrome');

    assert.match(actionButtonBody, /data-athena-create-entity-button='true'/);
    assert.match(controlsBody, /data-athena-create-entity-panel='true'/);
    assert.match(renderBody, /ReactDOM\.createPortal\(this\.renderCreateEntityControls\(\), document\.body\)/);
    assert.doesNotMatch(stageChromeBody, /this\.createEntityControlsOpen \? this\.renderCreateEntityControls\(\) : undefined/);
    assert.doesNotMatch(actionButtonBody, /!this\.isAthenaEditor\(this\.editorManager\.currentEditor\)/);
    assert.doesNotMatch(actionButtonBody, /availableItems\.length === 0/);
    assert.doesNotMatch(actionButtonBody, /!this\.componentKnowledge\?\.systemSemanticId/);
    assert.doesNotMatch(controlsBody, /!this\.isAthenaEditor\(this\.editorManager\.currentEditor\)/);
    assert.doesNotMatch(controlsBody, /Open an Athena source editor/);
});

test('M32 product smoke proves the Graph View add panel opens as a visible overlay', () => {
    assert.match(electronSmokeSource, /data-athena-create-entity-button/);
    assert.match(electronSmokeSource, /data-athena-create-entity-panel/);
    assert.match(electronSmokeSource, /createEntityPanelProof/);
    assert.match(m32SmokeSource, /assertCreateEntityPanelProof/);
    assert.match(m32SmokeSource, /createEntityPanelProof/);
});

test('Graph View create panel uses viewport-bounded frontmost geometry', () => {
    assert.match(graphWorkbenchStyles, /\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*position:\s*fixed/);
    assert.match(graphWorkbenchStyles, /\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*z-index:\s*1200/);
    assert.match(graphWorkbenchStyles, /\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*left:\s*50%/);
    assert.match(graphWorkbenchStyles, /\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*right:\s*auto/);
    assert.match(graphWorkbenchStyles, /\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*transform:\s*translateX\(-50%\)/);
    assert.match(graphWorkbenchStyles, /\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*min-height:\s*220px/);
    assert.match(graphWorkbenchStyles, /\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*max-height:\s*min\(560px,\s*calc\(100vh - 96px\)\)/);
    assert.match(graphWorkbenchStyles, /@media\s*\(max-width:\s*720px\)\s*\{[\s\S]*\.athena-graph-workbench__create-entity-panel\s*\{[\s\S]*left:\s*8px/);
    assert.match(electronSmokeSource, /frontmostAtCenter/);
    assert.match(electronSmokeSource, /withinViewport/);
    assert.match(m32SmokeSource, /panelHeight\) < 160/);
    assert.match(m32SmokeSource, /reachableControlCount/);
});
