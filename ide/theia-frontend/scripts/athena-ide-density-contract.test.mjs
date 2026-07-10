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

test('Athena graph overlay uses dense detail sections instead of floating cards', () => {
    const source = readBrowserSource('athena-graph-workbench-widget.tsx');

    assert.match(source, /athena-graph-workbench__overlay-section/);
    assert.match(source, /athena-graph-workbench__detail-list/);
    assert.match(source, /athena-graph-workbench__floating-bar/);
    assert.match(source, /athena-graph-workbench__floating-panel/);
    assert.match(source, /athena-graph-workbench__status-icon/);
    assert.match(source, /title=\{model\.statusLabel\}/);
    assert.match(source, /viewIconClass\(view\.viewId\)/);
    assert.match(source, /viewAriaLabel\(view\)/);
    assert.match(source, /handleStageClick/);
    assert.match(source, /this\.overlayPanelExpanded = false;/);
    assert.match(source, /width:\s*`\$\{model\.canvas\.width\}px`/);
    assert.match(source, /height:\s*`\$\{model\.canvas\.height\}px`/);
    assert.doesNotMatch(source, />\{model\.statusLabel\}<\/div>/);
    assert.doesNotMatch(source, />\{this\.abbreviateViewLabel\(view\.displayName\)\}<\/button>/);
    assert.doesNotMatch(source, /athena-graph-workbench__hud-chip/);
});

test('Athena shared styles define an IDE-density surface language', () => {
    const styles = readFileSync(stylesPath, 'utf8');

    assert.match(styles, /--athena-workbench-radius:\s*4px/);
    assert.match(styles, /--athena-workbench-row-min-height:\s*28px/);
    assert.match(styles, /\.athena-graph-workbench__floating-bar\s*\{/);
    assert.match(styles, /\.athena-semantic-inspection__section,\s*\n\.athena-repository-graph__section,\s*\n\.athena-semantic-scm__section\s*\{/);
    assert.match(styles, /border:\s*0;/);
    assert.match(styles, /\.athena-graph-workbench\s*\{[\s\S]*height:\s*100%/);
    assert.match(styles, /\.athena-graph-workbench__overlay--top\s*\{[\s\S]*pointer-events:\s*auto/);
    assert.match(styles, /\.athena-graph-workbench__floating-panel\s*\{[\s\S]*pointer-events:\s*auto/);
    assert.match(styles, /\.athena-graph-workbench__status\s*\{[\s\S]*background:\s*rgba\(255,\s*255,\s*255,\s*0\.08\)/);
    assert.doesNotMatch(styles, /backdrop-filter\s*:/);
});
