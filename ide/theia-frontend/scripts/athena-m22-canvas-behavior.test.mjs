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

test('M22 preserves accepted graph workbench canvas behavior while layout preview is active', () => {
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const css = readRepoFile('ide/theia-frontend/src/browser/style/index.css');
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m22-sample-project.js');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');

    assert.match(cssRule(css, '.athena-graph-workbench__stage'), /background:\s*[\s\S]*linear-gradient/);
    assert.match(cssRule(css, '.athena-graph-workbench__sheet'), /background:\s*transparent/);
    assert.match(cssRule(css, '.athena-graph-workbench__node--electrical-device'), /fill:\s*transparent/);
    assert.match(cssRule(css, '.athena-graph-workbench__bottom-dock'), /border:\s*0/);
    assert.match(cssRule(css, '.athena-graph-workbench__bottom-dock'), /background:\s*transparent/);
    assert.match(cssRule(css, '.athena-graph-workbench__floating-bar'), /border:\s*0/);
    assert.match(cssRule(css, '.athena-graph-workbench__floating-bar'), /background:\s*transparent/);
    assert.match(cssRule(css, '.athena-graph-workbench__zoom-dock'), /border:\s*0/);
    assert.match(cssRule(css, '.athena-graph-workbench__zoom-dock'), /background:\s*transparent/);

    assert.match(widgetSource, /data-athena-info-button='true'/);
    assert.match(widgetSource, /data-athena-info-popover='true'/);
    assert.match(methodBody(widgetSource, 'renderCabinetMainPopover'), /Cabinet Main/);
    assert.doesNotMatch(methodBody(widgetSource, 'renderBottomDock'), /Cabinet Main/);
    assert.doesNotMatch(methodBody(widgetSource, 'renderSheetChrome'), /Cabinet Main/);
    assert.match(methodBody(widgetSource, 'renderLayoutMutationPreview'), /layoutBlockSnippet/);
    assert.doesNotMatch(methodBody(widgetSource, 'renderLayoutMutationPreview'), /Cabinet Main/);

    [
        'stageHasGrid',
        'sheetTransparent',
        'floatingBarTransparent',
        'bottomDockTransparent',
        'zoomDockTransparent',
        'infoPopoverOpened',
        'infoPopoverClosedOnWhitespace'
    ].forEach(marker => {
        assert.match(smoke, new RegExp(marker));
    });

    assert.match(usage, /stage grid remains the coordinate surface/i);
    assert.match(usage, /Cabinet Main.*top information popover/i);
    assert.match(usage, /floating bottom controls/i);
    assert.match(usage, /transparent/i);
});
