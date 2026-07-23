import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const graphWorkbenchSource = await readFile(
    new URL('../src/browser/athena-graph-workbench-widget.tsx', import.meta.url),
    'utf8',
);
const densityContractSource = await readFile(
    new URL('./athena-ide-density-contract.test.mjs', import.meta.url),
    'utf8',
);
const m32SmokeSource = await readFile(
    new URL('../../theia-product/scripts/verify-athena-m32-sample-project.js', import.meta.url),
    'utf8',
);
const electronSmokeSource = await readFile(
    new URL('../../theia-product/scripts/athena-electron-open-workspace-main.js', import.meta.url),
    'utf8',
);

test('Graph View customer toolbar text uses product taxonomy instead of architecture labels', () => {
    const visibleToolbarText = [
        'Cabinet Main information',
        'Close Cabinet Main information',
        'Document projection sheet view',
        'Create governed entity',
        'Create governed entity transaction',
        'Preview governed entity creation',
        'Accept governed entity creation',
        'Reject governed entity creation',
    ];

    for (const forbiddenText of visibleToolbarText) {
        assert.doesNotMatch(graphWorkbenchSource, new RegExp(forbiddenText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
        assert.doesNotMatch(densityContractSource, new RegExp(forbiddenText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
        assert.doesNotMatch(m32SmokeSource, new RegExp(forbiddenText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
        assert.doesNotMatch(electronSmokeSource, new RegExp(forbiddenText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
    }

    assert.match(graphWorkbenchSource, /title='Projection information'/);
    assert.match(graphWorkbenchSource, /aria-label='Projection information'/);
    assert.match(graphWorkbenchSource, /title='Sheet'/);
    assert.match(graphWorkbenchSource, /aria-label='Sheet'/);
    assert.match(graphWorkbenchSource, /title='Create device'/);
    assert.match(graphWorkbenchSource, /aria-label='Create device'/);
});

test('Graph View product toolbar exposes Cabinet as the single primary customer projection', () => {
    assert.match(graphWorkbenchSource, /resolveVisibleProjectionViews/);
    assert.match(graphWorkbenchSource, /view\.viewId === 'cabinet'/);
    assert.doesNotMatch(graphWorkbenchSource, /const documentationView = model\.supportedViews\.find/);
    assert.match(graphWorkbenchSource, /data-athena-visible-projection-view-count/);
    assert.match(graphWorkbenchSource, /data-athena-compatibility-projection-view-count/);
    assert.match(electronSmokeSource, /collectProjectionViewProof/);
    assert.match(m32SmokeSource, /assertProjectionViewFocusProof/);
    assert.match(m32SmokeSource, /only the Cabinet demo view/);
});
