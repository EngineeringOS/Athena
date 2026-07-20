import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const browserDir = path.resolve(import.meta.dirname, '../src/browser');
const stylesPath = path.join(browserDir, 'style/index.css');

function readBrowserSource(fileName) {
    return readFileSync(path.join(browserDir, fileName), 'utf8');
}

test('Athena home workbench uses dense panel markup instead of dashboard cards', () => {
    const source = readBrowserSource('athena-home-widget.tsx');

    assert.match(source, /athena-home__toolbar/);
    assert.match(source, /athena-home__panel/);
    assert.match(source, /athena-home__kv-list/);
    assert.doesNotMatch(source, /athena-home__card/);
    assert.doesNotMatch(source, /athena-home__hero/);
    assert.doesNotMatch(source, /athena-home__badge/);
});

test('Athena inspection, repository graph, and SCM views expose dense summary rows', () => {
    const inspectionSource = readBrowserSource('athena-semantic-inspection-widget.tsx');
    const repositoryGraphSource = readBrowserSource('athena-repository-graph-widget.tsx');
    const semanticScmSource = readBrowserSource('athena-semantic-scm-widget.tsx');

    assert.match(inspectionSource, /athena-semantic-inspection__summary-list/);
    assert.match(repositoryGraphSource, /athena-repository-graph__summary-list/);
    assert.match(semanticScmSource, /athena-semantic-scm__summary-list/);
    assert.match(semanticScmSource, /athena-semantic-scm__control-grid/);
});

test('Athena graph workbench keeps controls in the bottom dock and Cabinet Main info in a top popover', () => {
    const source = readBrowserSource('athena-graph-workbench-widget.tsx');
    const presentationSource = readBrowserSource('athena-graph-workbench-presentation-node.tsx');

    assert.match(source, /renderBottomDock\(/);
    assert.match(source, /renderCabinetMainPopover\(/);
    assert.match(source, /buildCabinetMainInfoRows\(/);
    assert.match(source, /athena-graph-workbench__bottom-dock/);
    assert.match(source, /athena-graph-workbench__info-popover/);
    assert.match(source, /athena-graph-workbench__info-table/);
    assert.match(source, /athena-graph-workbench__floating-bar/);
    assert.match(source, /athena-graph-workbench__status-icon/);
    assert.match(source, /codicon-info/);
    assert.match(source, /aria-label='Canvas controls'/);
    assert.match(source, /aria-label='Cabinet Main information'/);
    assert.match(source, /handleWorkbenchClick/);
    assert.match(source, /data-athena-info-button='true'/);
    assert.match(source, /data-athena-info-popover='true'/);
    assert.match(source, /title=\{model\.statusLabel\}/);
    assert.match(source, /viewIconClass\(view\)/);
    assert.match(source, /viewAriaLabel\(view\)/);
    assert.match(source, /handleViewportClick/);
    assert.match(source, /athena-graph-workbench__sheet/);
    assert.match(source, /athena-graph-workbench__sheet-frame/);
    assert.doesNotMatch(source, /athena-graph-workbench__sheet-grid/);
    assert.match(source, /width:\s*`\$\{model\.sheetChrome\.frame\.width\}px`/);
    assert.match(source, /height:\s*`\$\{model\.sheetChrome\.frame\.height\}px`/);
    assert.doesNotMatch(source, />\{model\.statusLabel\}<\/div>/);
    assert.doesNotMatch(source, />\{this\.abbreviateViewLabel\(view\.displayName\)\}<\/button>/);
    assert.doesNotMatch(source, /athena-graph-workbench__overlay--bottom-right/);
    assert.doesNotMatch(source, /athena-graph-workbench__hud-chip/);
    assert.doesNotMatch(source, /athena-graph-workbench__bottom-dock-heading/);
    assert.doesNotMatch(source, /athena-graph-workbench__sheet-title-block/);
    assert.doesNotMatch(source, /athena-graph-workbench__sheet-cross-reference-marker/);
    assert.doesNotMatch(source, /titleBlock\.displayName/);
    assert.doesNotMatch(source, /aria-label='Cabinet Main'/);
    assert.match(presentationSource, /const fill = 'transparent'/);
    assert.doesNotMatch(presentationSource, /resolveToken\(args\.tokenDefaults,\s*args\.tokenOverrides,\s*command\.fillTokenKey/);
});

test('Athena shared styles define an IDE-density surface language', () => {
    const styles = readFileSync(stylesPath, 'utf8');

    assert.match(styles, /--athena-workbench-radius:\s*4px/);
    assert.match(styles, /--athena-workbench-row-min-height:\s*28px/);
    assert.match(styles, /\.athena-graph-workbench__floating-bar\s*\{/);
    assert.match(styles, /\.athena-graph-workbench__info-popover\s*\{/);
    assert.match(styles, /\.athena-graph-workbench__sheet-frame\s*\{/);
    assert.match(styles, /\.athena-semantic-inspection__section,\s*\n\.athena-repository-graph__section,\s*\n\.athena-semantic-scm__section\s*\{/);
    assert.match(styles, /border:\s*0;/);
    assert.match(styles, /\.athena-graph-workbench\s*\{[\s\S]*height:\s*100%/);
    assert.match(styles, /\.athena-graph-workbench__stage\s*\{[\s\S]*linear-gradient\(var\(--athena-graph-grid-major\)/);
    assert.match(styles, /\.athena-graph-workbench__stage\s*\{[\s\S]*background-size:[\s\S]*var\(--athena-graph-sheet-grid-major-step\)/);
    assert.match(styles, /\.athena-graph-workbench__sheet\s*\{[\s\S]*background:\s*transparent/);
    assert.match(styles, /\.athena-graph-workbench__overlay--top\s*\{[\s\S]*pointer-events:\s*auto/);
    assert.match(styles, /\.athena-graph-workbench__bottom-dock\s*\{[\s\S]*position:\s*absolute/);
    assert.match(styles, /\.athena-graph-workbench__bottom-dock\s*\{[\s\S]*border:\s*0;/);
    assert.match(styles, /\.athena-graph-workbench__bottom-dock\s*\{[\s\S]*background:\s*transparent/);
    assert.match(styles, /\.athena-graph-workbench__floating-bar\s*\{[\s\S]*border:\s*0;/);
    assert.match(styles, /\.athena-graph-workbench__floating-bar\s*\{[\s\S]*background:\s*transparent/);
    assert.match(styles, /\.athena-graph-workbench__tool-button\s*\{[\s\S]*background:\s*color-mix/);
    assert.match(styles, /\.athena-graph-workbench__zoom-dock\s*\{[\s\S]*border:\s*0;/);
    assert.match(styles, /\.athena-graph-workbench__zoom-dock\s*\{[\s\S]*background:\s*transparent/);
    assert.match(styles, /\.athena-graph-workbench__node\s*\{[\s\S]*fill:\s*transparent/);
    assert.match(styles, /\.athena-graph-workbench__node--electrical-device\s*\{[\s\S]*fill:\s*transparent/);
    assert.match(styles, /\.athena-graph-workbench__status\s*\{[\s\S]*background:\s*transparent/);
    assert.doesNotMatch(styles, /athena-graph-workbench__bottom-dock-heading/);
    assert.doesNotMatch(styles, /athena-graph-workbench__floating-panel/);
    assert.doesNotMatch(styles, /athena-graph-workbench__overlay-toggle/);
    assert.doesNotMatch(styles, /athena-graph-workbench__sheet-title-block/);
    assert.doesNotMatch(styles, /athena-graph-workbench__sheet-cross-reference-marker/);
    assert.doesNotMatch(styles, /athena-graph-workbench__sheet-grid/);
    assert.doesNotMatch(styles, /athena-graph-workbench__grid/);
    assert.doesNotMatch(styles, /backdrop-filter\s*:/);
});
