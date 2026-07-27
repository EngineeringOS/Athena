import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const graphWorkbenchSource = await readFile(
    new URL('../src/browser/athena-graph-workbench-widget.tsx', import.meta.url),
    'utf8',
);
const electronLauncherSource = await readFile(
    new URL('../../theia-product/scripts/athena-electron-open-workspace-main.js', import.meta.url),
    'utf8',
);
const productContributionSource = await readFile(
    new URL('../src/browser/athena-product-contribution.ts', import.meta.url),
    'utf8',
);
const m32ProductSmokeSource = await readFile(
    new URL('../../theia-product/scripts/verify-athena-m32-sample-project.js', import.meta.url),
    'utf8',
);

function methodBody(source, methodName) {
    const match = source.match(new RegExp(`protected\\s+(?:async\\s+)?${methodName}\\b`));
    const start = match?.index ?? -1;
    assert.notEqual(start, -1, `${methodName} should exist`);
    const nextMethod = source.indexOf('\n    protected ', start + methodName.length);
    return source.slice(start, nextMethod === -1 ? source.length : nextMethod);
}

test('Document navigation remains hidden compatibility gated to Documentation', () => {
    const stageChrome = methodBody(graphWorkbenchSource, 'renderStageChrome');
    const contextualNavigation = methodBody(graphWorkbenchSource, 'renderContextualDocumentNavigation');
    const visibleSheetSelector = methodBody(graphWorkbenchSource, 'resolveVisibleSheetViewSelector');

    assert.doesNotMatch(stageChrome, /renderSheetViewSelector/);
    assert.doesNotMatch(stageChrome, /renderReferenceMarkerControls/);
    assert.match(contextualNavigation, /renderSheetViewSelector/);
    assert.match(contextualNavigation, /renderReferenceMarkerControls/);
    assert.match(contextualNavigation, /athena-graph-workbench__document-navigation/);
    assert.match(visibleSheetSelector, /resolveVisibleAthenaGraphSheetViewSelector/);
    assert.match(visibleSheetSelector, /activeViewId/);
    assert.match(visibleSheetSelector, /documentation/);
    assert.doesNotMatch(graphWorkbenchSource, /lastDocumentSheetViewSelector/);
});

test('Create Device preview and acceptance do not require Graph View to yield active editor focus', () => {
    const controls = methodBody(graphWorkbenchSource, 'renderCreateEntityControls');
    const preview = methodBody(graphWorkbenchSource, 'previewCreateEntityTransaction');
    const accept = methodBody(graphWorkbenchSource, 'acceptCreateEntityPreview');

    assert.doesNotMatch(controls, /isAthenaEditor\(this\.editorManager\.currentEditor\)/);
    assert.doesNotMatch(controls, /Open an Athena source editor/);
    assert.doesNotMatch(preview, /isAthenaEditor\(this\.editorManager\.currentEditor\)/);
    assert.doesNotMatch(preview, /Open an Athena source editor/);
    assert.doesNotMatch(accept, /if \(!this\.currentEditorMatchesCreateEntityPreview\(preview\)\)/);
    assert.match(accept, /if \(this\.currentEditorMatchesCreateEntityPreview\(preview\) &&/);
    assert.match(accept, /sourceEditMatchesActiveDocument/);
});

test('Create Device fails closed instead of exposing a placeholder without governed capability', () => {
    const action = methodBody(graphWorkbenchSource, 'renderCreateEntityActionButton');
    const controls = methodBody(graphWorkbenchSource, 'renderCreateEntityControls');

    assert.match(action, /componentKnowledge\?\.systemSemanticId/);
    assert.match(action, /availableItems\.length === 0/);
    assert.doesNotMatch(graphWorkbenchSource, /placeholder until governed concept evidence is available/i);
    assert.doesNotMatch(controls, /createEntityCapabilityReady/);
});

test('M32 Electron proof creates from Graph View in a temporary workspace and verifies reopen persistence', () => {
    assert.match(m32ProductSmokeSource, /runGraphFirstAuthoringProof/);
    assert.match(m32ProductSmokeSource, /mkdtempSync/);
    assert.match(m32ProductSmokeSource, /cpSync/);
    assert.match(m32ProductSmokeSource, /graphFirstAuthoringProof/);
    assert.match(m32ProductSmokeSource, /reopenPersistenceProof/);
    assert.match(electronLauncherSource, /ATHENA_ELECTRON_SMOKE_CREATE_ENTITY_TAG/);
    assert.match(electronLauncherSource, /ATHENA_ELECTRON_SMOKE_EXPECT_SEMANTIC_ID/);
    assert.match(electronLauncherSource, /graphFirstAuthoringProof/);
    assert.match(electronLauncherSource, /cabinetActiveAfterCreate/);
    assert.match(m32ProductSmokeSource, /graphFirstAuthoringProof\.cabinetActiveAfterCreate/);
});

test('M32 product proof refreshes Cabinet without entering hidden compatibility views', () => {
    const smokeSwitch = methodBody(productContributionSource, 'switchProjectionViewForSmoke');

    assert.doesNotMatch(electronLauncherSource, /collectDocumentationCompatibilityProof/);
    assert.doesNotMatch(electronLauncherSource, /switchGraphWorkbenchProjectionView\('documentation'\)/);
    assert.match(electronLauncherSource, /__athenaWorkbenchSmoke\?\.switchProjectionView/);
    assert.match(electronLauncherSource, /__athenaWorkbenchSmoke\?\.refreshProjectionView/);
    assert.match(productContributionSource, /switchProjectionViewForSmoke/);
    assert.match(productContributionSource, /refreshProjectionForSmoke/);
    assert.match(smokeSwitch, /ATHENA_WORKBENCH_EXTENSIONS\.find/);
    assert.match(smokeSwitch, /revealGraphWorkbench\(graphExtension\)/);
    assert.match(m32ProductSmokeSource, /cabinetRefreshAccepted/);
    assert.match(m32ProductSmokeSource, /cabinetActiveAfterRefresh/);
    assert.doesNotMatch(m32ProductSmokeSource, /assertDocumentProjectionProof/);
});
