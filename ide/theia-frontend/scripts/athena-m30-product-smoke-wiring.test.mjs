import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const require = createRequire(import.meta.url);

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readJson(path) {
    return JSON.parse(readFileSync(resolve(repoRoot, path), 'utf8'));
}

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

test('M30 product smoke is wired to the openable sample project', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m30-sample-project.js');

    assert.equal(
        idePackage.scripts['start:m30'],
        'yarn workspace @engineeringood/athena-theia-product start:m30',
    );
    assert.equal(
        idePackage.scripts['start:smoke:m30'],
        'yarn workspace @engineeringood/athena-theia-product start:smoke:m30',
    );
    assert.equal(
        productPackage.scripts['start:m30'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m30/sample-project',
    );
    assert.equal(
        productPackage.scripts['start:smoke:m30'],
        'node scripts/verify-athena-m30-sample-project.js',
    );
    assert.ok(existsSync(smokeScript), 'Missing M30 product smoke script.');
});

test('M30 structured product smoke asserts representation proof before UI screenshot proof', () => {
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m30-sample-project.js');
    const openWorkspaceMain = readRepoFile('ide/theia-product/scripts/athena-electron-open-workspace-main.js');

    assert.match(smoke, /buildStructuredRepresentationProofPayloads/);
    assert.match(smoke, /assertStructuredRepresentationProofPayloads/);
    assert.match(smoke, /assertGraphWorkbenchProof/);
    assert.match(smoke, /representation-library/);
    assert.match(smoke, /binding-counts/);
    assert.match(smoke, /anchor-usage/);
    assert.match(smoke, /composition-bounds/);
    assert.match(smoke, /route-anchors/);
    assert.match(smoke, /transparent-chrome/);
    assert.match(smoke, /No Gradle verification runs in parallel/);
    assert.match(smoke, /ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE/);
    assert.match(smoke, /ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH/);
    assert.match(smoke, /src\/01-rolling-shutter-control-source\.athena/);
    assert.match(smoke, /RollingShutterControlProof > ShutterMotorM30 > up/);
    assert.match(openWorkspaceMain, /SMOKE_OUTLINE_SOURCE_RELATIVE/);
    assert.match(openWorkspaceMain, /SMOKE_OUTLINE_EXPECTED_PATH/);
    assert.match(openWorkspaceMain, /isComponentBodyBox/);
    assert.doesNotMatch(openWorkspaceMain, /routeBodyIntersectionCount:[\s\S]*port:/);
    assert.doesNotMatch(smoke, /innerText|textContent|querySelectorAll\([^)]*text/i);
});

test('M30 structured representation proof payload inventory is executable', () => {
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m30-sample-project.js');
    const {
        buildStructuredRepresentationProofPayloads,
        assertStructuredRepresentationProofPayloads,
        REQUIRED_REPRESENTATION_PROOF_KINDS,
    } = require(smokeScript);
    const sampleProject = resolve(repoRoot, 'examples/m30/sample-project');
    const graphWorkbenchProof = {
        activeViewId: 'documentation',
        root: true,
        stage: true,
        viewport: true,
        sheet: true,
        canvas: true,
        floatingBarTransparent: true,
        bottomDockTransparent: true,
        zoomDockTransparent: true,
        sheetTransparent: true,
        stageHasGrid: true,
        documentProjectionProof: {
            hasSheetViewSelector: true,
            sheetViewOptionCount: 3,
        },
        sheetSurfaceProof: {
            factDriven: true,
            sheetSize: 'A3',
            orientation: 'landscape',
            hasMargins: true,
            hasTitleBlock: true,
        },
        representationProof: {
            representationCount: 6,
            presentationTerminalCount: 8,
            presentationLabelCount: 6,
            representationIds: [
                'athena-industrial-control-v0:power-supply',
                'athena-industrial-control-v0:protection-device',
                'athena-industrial-control-v0:terminal-block',
                'athena-industrial-control-v0:load-actuator',
            ],
            fallbackRepresentationIds: [],
        },
        routeProof: {
            routeCount: 5,
            terminalCount: 10,
            routesWithTerminalAnchors: 5,
            centerFallbackRouteIds: [],
        },
        visualProof: {
            viewBoxWidth: 720,
            viewBoxHeight: 420,
            routeBodyIntersectionCount: 0,
            nonOrthogonalSegmentCount: 0,
        },
        sheetSelectorPersistenceProof: {
            skipped: false,
            selectorVisibleAfterViewSwitch: true,
            optionCountBeforeViewSwitch: 3,
            optionCountAfterViewSwitch: 3,
        },
    };

    const payloads = buildStructuredRepresentationProofPayloads(sampleProject, graphWorkbenchProof);
    assertStructuredRepresentationProofPayloads(payloads);

    assert.deepEqual(
        payloads.map(payload => payload.payload.proofKind),
        REQUIRED_REPRESENTATION_PROOF_KINDS,
    );
    assert.ok(payloads.every(payload => payload.schemaVersion === 'm30.representation.v1'));
    assert.ok(payloads.every(payload => payload.payloadKind === 'proof'));
    assert.ok(payloads.every(payload => payload.payload.activeSourceContext));

    const libraryProof = payloads.find(payload => payload.payload.proofKind === 'representation-library');
    assert.equal(
        libraryProof.payload.loadedRepresentationLibraryIds,
        'athena-industrial-control-v0',
    );
});

test('M30 screenshot guard and usage documentation are wired', () => {
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m30-sample-project.js');
    const sampleReadme = readRepoFile('examples/m30/sample-project/README.md');
    const usageDoc = readRepoFile('_bmad-output/implementation-artifacts/m30/m30-demo-usage.md');

    assert.match(smoke, /ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT/);
    assert.match(smoke, /ATHENA_GRAPH_WORKBENCH_SCREENSHOT=/);
    assert.match(smoke, /assertPngScreenshot/);
    assert.match(smoke, /m30-graph-workbench-smoke\.png/);

    assert.match(sampleReadme, /yarn --cwd ide start:m30/);
    assert.match(sampleReadme, /yarn --cwd ide start:smoke:m30/);
    assert.match(usageDoc, /examples\/m30\/sample-project/);
    assert.match(usageDoc, /m30-graph-workbench-smoke\.png/);
    assert.match(usageDoc, /qualitative visual reference/i);
    assert.match(usageDoc, /no QET\/EPLAN parity/i);
    assert.doesNotMatch(usageDoc, /pixel perfect|QET runtime|EPLAN equivalent/i);
});
