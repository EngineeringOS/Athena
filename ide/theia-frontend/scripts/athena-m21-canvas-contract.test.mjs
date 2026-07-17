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

test('M21 keeps the accepted M20 canvas contract explicit in source and docs', () => {
    const widget = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const styles = readRepoFile('ide/theia-frontend/src/browser/style/index.css');
    const usage = readRepoFile('docs/usages/m21-proof-usage.md');

    assert.match(widget, /renderCabinetMainPopover\(/);
    assert.match(widget, /renderBottomDock\(/);
    assert.match(widget, /handleWorkbenchClick/);
    assert.match(widget, /data-athena-info-button='true'/);
    assert.match(widget, /data-athena-info-popover='true'/);
    assert.match(widget, /athena-graph-workbench__info-popover/);
    assert.match(widget, /athena-graph-workbench__bottom-dock/);
    assert.match(widget, /athena-graph-workbench__zoom-dock/);
    assert.match(widget, /athena-graph-workbench__sheet-frame/);
    assert.match(widget, /athena-graph-workbench__stage/);
    assertCssRule(styles, '.athena-graph-workbench__stage', /linear-gradient\(/);
    assertCssRule(styles, '.athena-graph-workbench__sheet', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__overlay--top', /pointer-events:\s*auto/);
    assertCssRule(styles, '.athena-graph-workbench__floating-bar', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__bottom-dock', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__zoom-dock', /background:\s*transparent/);
    assertCssRule(styles, '.athena-graph-workbench__tool-button', /border-radius:\s*0;/);
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
    assert.match(usage, /whitespace click closes the info popover/);
    assert.match(usage, /bottom controls remain icon-only and transparent/);
});
