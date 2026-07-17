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

function assertCssRule(styles, selector, pattern) {
    const escapedSelector = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const matches = [...styles.matchAll(new RegExp(`${escapedSelector}\\s*\\{[^}]*\\}`, 'g'))];
    assert.ok(matches.length > 0, `${selector} CSS rule should exist`);
    assert.ok(
        matches.some(match => pattern.test(match[0])),
        `${selector} CSS rule should match ${pattern}`
    );
}

test('M21 graph workbench visual proof keeps the accepted canvas contract visible', () => {
    const widget = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const styles = readRepoFile('ide/theia-frontend/src/browser/style/index.css');
    const opener = readRepoFile('ide/theia-product/scripts/athena-electron-open-workspace-main.js');
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m21-sample-project.js');
    const usage = readRepoFile('docs/usages/m21-proof-usage.md');

    assert.match(widget, /renderStageChrome\(/);
    assert.match(widget, /renderCabinetMainPopover\(/);
    assert.match(widget, /renderBottomDock\(/);
    assert.match(widget, /handleWorkbenchClick/);
    assert.match(widget, /data-athena-info-button='true'/);
    assert.match(widget, /data-athena-info-popover='true'/);
    assert.match(widget, /athena-graph-workbench__floating-bar/);
    assert.match(widget, /athena-graph-workbench__bottom-dock/);
    assert.match(widget, /athena-graph-workbench__zoom-dock/);
    assert.match(widget, /athena-graph-workbench__info-popover/);
    assert.match(widget, /athena-graph-workbench__sheet-frame/);
    assert.match(widget, /athena-graph-workbench__canvas/);
    assert.match(widget, /athena-graph-workbench__stage/);
    assertCssRule(styles, '.athena-graph-workbench__stage', /linear-gradient\(/);
    assertCssRule(styles, '.athena-graph-workbench__stage', /background-size:/);
    assertCssRule(styles, '.athena-graph-workbench__sheet', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__overlay--top', /pointer-events:\s*auto/);
    assertCssRule(styles, '.athena-graph-workbench__floating-bar', /border:\s*0;/);
    assertCssRule(styles, '.athena-graph-workbench__floating-bar', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__bottom-dock', /border:\s*0;/);
    assertCssRule(styles, '.athena-graph-workbench__bottom-dock', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__zoom-dock', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__info-popover', /\{/);
    assertCssRule(styles, '.athena-graph-workbench__tool-button', /background:/);
    assertCssRule(styles, '.athena-graph-workbench__node', /fill:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__status', /background:\s*transparent/);
    assert.doesNotMatch(widget, /athena-graph-workbench__sheet-title-block/);
    assert.doesNotMatch(widget, /athena-graph-workbench__sheet-cross-reference-marker/);
    assert.doesNotMatch(widget, /athena-graph-workbench__bottom-dock-heading/);
    assert.doesNotMatch(widget, /athena-graph-workbench__overlay--bottom-right/);
    assert.doesNotMatch(widget, /athena-graph-workbench__hud-chip/);
    assert.doesNotMatch(widget, /athena-graph-workbench__floating-panel/);
    assert.doesNotMatch(styles, /athena-graph-workbench__sheet-grid/);
    assert.doesNotMatch(styles, /athena-graph-workbench__grid/);
    assert.doesNotMatch(styles, /backdrop-filter\s*:/);
    assert.match(opener, /ATHENA_GRAPH_WORKBENCH_PROOF=/);
    assert.match(opener, /Graphical View quick action/);
    assert.match(opener, /requireElement\('\.athena-graph-workbench__stage'/);
    assert.match(opener, /infoPopoverClosedOnWhitespace/);
    assert.match(smoke, /missingGraphProof/);
    assert.match(smoke, /graph-workbench DOM proof passed/);
    assert.match(usage, /node --test ide\/theia-frontend\/scripts\/athena-m21-graph-workbench-visual-proof\.test\.mjs/);
});
