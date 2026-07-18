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

function methodBody(source, methodName) {
    const start = source.indexOf(`protected ${methodName}`);
    assert.notEqual(start, -1, `${methodName} should exist`);
    const nextMethod = source.indexOf('\n    protected ', start + methodName.length);
    return source.slice(start, nextMethod === -1 ? source.length : nextMethod);
}

test('M23 preserves active-source refresh and accepted canvas behavior while adding layout source edits', () => {
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const bridgeSource = readRepoFile('ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts');
    const css = readRepoFile('ide/theia-frontend/src/browser/style/index.css');
    const sourceEditTest = readRepoFile('ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs');

    assert.match(bridgeSource, /lastAthenaEditorWidget/);
    assert.match(widgetSource, /onDocumentContentChanged\(\(\) => this\.scheduleRefresh\(\)\)/);
    assert.match(methodBody(widgetSource, 'acceptLayoutMutationPreview'), /documentText/);
    assert.match(methodBody(widgetSource, 'acceptLayoutMutationPreview'), /this\.scheduleRefresh\(\)/);
    assert.match(methodBody(widgetSource, 'acceptLayoutMutationPreview'), /applyAuthoringSourceEdit\(sourceEdit\)/);
    assert.match(sourceEditTest, /inserts layout block inside the active system scope/);

    assert.match(css, /\.athena-graph-workbench__stage\s*\{[\s\S]*linear-gradient/);
    assert.match(css, /\.athena-graph-workbench__sheet\s*\{[\s\S]*background:\s*transparent/);
    assert.match(css, /\.athena-graph-workbench__bottom-dock\s*\{[\s\S]*background:\s*transparent/);
    assert.match(css, /\.athena-graph-workbench__zoom-dock\s*\{[\s\S]*background:\s*transparent/);
    assert.match(widgetSource, /data-athena-info-popover='true'/);
    assert.match(methodBody(widgetSource, 'handleWorkbenchClick'), /data-athena-info-button/);
    assert.match(methodBody(widgetSource, 'handleWorkbenchClick'), /data-athena-info-popover/);
    assert.match(methodBody(widgetSource, 'handleWorkbenchClick'), /this\.closeInfoPopover\(\)/);
});
