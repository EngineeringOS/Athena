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

function cssRule(source, selector) {
    const escaped = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const match = source.match(new RegExp(`${escaped}\\s*\\{[\\s\\S]*?\\n\\}`));
    assert.ok(match, `${selector} rule should exist`);
    return match[0];
}

function methodBody(source, methodName) {
    const start = source.indexOf(`protected ${methodName}`);
    assert.notEqual(start, -1, `${methodName} should exist`);
    const nextMethod = source.indexOf('\n    protected ', start + methodName.length);
    return source.slice(start, nextMethod === -1 ? source.length : nextMethod);
}

test('M24 preserves accepted graph workbench behavior while adding route rendering', () => {
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const modelSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-model.ts');
    const bridgeSource = readRepoFile('ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts');
    const styles = readRepoFile('ide/theia-frontend/src/browser/style/index.css');
    const sprint = readRepoFile('_bmad-output/implementation-artifacts/m24/sprint-status.yaml');

    assert.match(cssRule(styles, '.athena-graph-workbench__stage'), /linear-gradient/);
    assert.match(cssRule(styles, '.athena-graph-workbench__sheet'), /background:\s*transparent/);
    assert.match(cssRule(styles, '.athena-graph-workbench__floating-bar'), /background:\s*transparent/);
    assert.match(cssRule(styles, '.athena-graph-workbench__bottom-dock'), /background:\s*transparent/);
    assert.match(cssRule(styles, '.athena-graph-workbench__zoom-dock'), /background:\s*transparent/);

    assert.match(widgetSource, /data-athena-info-button='true'/);
    assert.match(widgetSource, /data-athena-info-popover='true'/);
    assert.match(widgetSource, /athena-graph-workbench__sheet-view-selector/);
    assert.match(widgetSource, /switchActiveSheetView/);
    assert.match(widgetSource, /data-athena-reference-marker='true'/);
    assert.match(widgetSource, /handleReferenceMarkerClick/);
    assert.match(widgetSource, /resolveAthenaGraphReferenceMarkerNavigation/);
    assert.match(widgetSource, /buildAthenaGraphDocumentReferenceInspection/);
    assert.match(widgetSource, /document-reference-relation/);
    assert.match(methodBody(widgetSource, 'handleWorkbenchClick'), /this\.closeInfoPopover\(\)/);
    assert.doesNotMatch(methodBody(widgetSource, 'renderBottomDock'), /Cabinet Main/);
    assert.doesNotMatch(methodBody(widgetSource, 'renderSheetChrome'), /Cabinet Main/);
    assert.doesNotMatch(widgetSource, /DocumentExplorer|document explorer/i);

    assert.match(bridgeSource, /lastAthenaEditorWidget/);
    assert.match(widgetSource, /onDocumentContentChanged\(\(\) => this\.scheduleRefresh\(\)\)/);
    assert.match(modelSource, /No governed route fact is available/);
    assert.doesNotMatch(modelSource, /localStorage|sessionStorage|indexedDB|canvasCoordinates/);
    assert.doesNotMatch(sprint, /desktop-viewer|apps\/desktop-viewer|Compose desktop/i);
});
