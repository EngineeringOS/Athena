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

test('M31 product smoke is wired to the governed authoring sample project', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m31-sample-project.js');

    assert.equal(
        idePackage.scripts['start:m31'],
        'yarn workspace @engineeringood/athena-theia-product start:m31',
    );
    assert.equal(
        idePackage.scripts['start:smoke:m31'],
        'yarn workspace @engineeringood/athena-theia-product start:smoke:m31',
    );
    assert.equal(
        productPackage.scripts['start:m31'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m31/sample-project',
    );
    assert.equal(
        productPackage.scripts['start:smoke:m31'],
        'node scripts/verify-athena-m31-sample-project.js',
    );
    assert.ok(existsSync(smokeScript), 'Missing M31 product smoke script.');
});

test('M31 structured product smoke names the governed workflow proof inventory', () => {
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m31-sample-project.js');

    assert.match(smoke, /buildStructuredAuthoringProofPayloads/);
    assert.match(smoke, /assertStructuredAuthoringProofPayloads/);
    assert.match(smoke, /REQUIRED_M31_AUTHORING_PROOF_KINDS/);
    assert.match(smoke, /assertGraphWorkbenchProof/);
    assert.match(smoke, /assertPngScreenshot/);
    assert.match(smoke, /m31\.authoring\.v1/);
    assert.match(smoke, /ATHENA_M31_AUTHORING_PROOF=/);

    [
        'capability-discovery',
        'entity-transaction',
        'nested-source-edit',
        'relationship-transaction',
        'route-anchors',
        'sheet-reference-identity',
        'mode-switch-reveal-reopen',
        'lifecycle-diagnostics',
        'visual-regression-guards',
    ].forEach(proofKind => assert.match(smoke, new RegExp(proofKind)));

    assert.match(smoke, /'examples',\s*'m31',\s*'sample-project'/);
    assert.match(smoke, /src\/01-governed-authoring-customer-source\.athena/);
    assert.match(smoke, /m31-graph-workbench-smoke\.png/);
    assert.match(smoke, /ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE/);
    assert.match(smoke, /ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH/);
    assert.doesNotMatch(smoke, /innerText|textContent|querySelectorAll\([^)]*text/i);
});

test('M31 structured proof payload inventory is executable without DOM semantic authority', () => {
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m31-sample-project.js');
    const {
        buildStructuredAuthoringProofPayloads,
        assertStructuredAuthoringProofPayloads,
        REQUIRED_M31_AUTHORING_PROOF_KINDS,
    } = require(smokeScript);
    const sampleProject = resolve(repoRoot, 'examples/m31/sample-project');
    const graphWorkbenchProof = {
        activeViewId: 'cabinet',
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
            sheetViewOptionCount: 2,
        },
        sheetSelectorPersistenceProof: {
            skipped: false,
            selectorVisibleAfterViewSwitch: true,
            optionCountBeforeViewSwitch: 2,
            optionCountAfterViewSwitch: 2,
        },
        representationProof: {
            representationCount: 8,
            presentationTerminalCount: 12,
            presentationLabelCount: 8,
            fallbackRepresentationIds: [],
        },
        routeProof: {
            routeCount: 6,
            terminalCount: 12,
            routesWithTerminalAnchors: 6,
            centerFallbackRouteIds: [],
        },
        visualProof: {
            svgViewBox: '0 0 900 520',
            viewBoxWidth: 900,
            viewBoxHeight: 520,
            routeBodyIntersectionCount: 0,
            nonOrthogonalSegmentCount: 0,
            duplicateOffsheetOccurrenceIds: [],
            repeatedTerminalLabelIds: [],
        },
        outlineProof: {
            skipped: false,
            hasOutlineWidget: true,
            widgetId: 'outline-view',
            paths: [
                'RollingShutterGovernedAuthoringProof > OperatorHMI1 > status',
                'RollingShutterGovernedAuthoringProof > SpareTerminalXT31 > in1',
            ],
        },
    };

    const payloads = buildStructuredAuthoringProofPayloads(sampleProject, graphWorkbenchProof);
    assertStructuredAuthoringProofPayloads(payloads);

    assert.deepEqual(
        payloads.map(payload => payload.payload.proofKind),
        REQUIRED_M31_AUTHORING_PROOF_KINDS,
    );
    assert.ok(payloads.every(payload => payload.schemaVersion === 'm31.authoring.v1'));
    assert.ok(payloads.every(payload => payload.payloadKind === 'proof'));
    assert.ok(payloads.every(payload => payload.payload.activeSourceContext));

    const serialized = JSON.stringify(payloads);
    assert.match(serialized, /SemanticAuthoringTransaction/);
    assert.match(serialized, /Revision Guard/);
    assert.match(serialized, /ServiceMotorM31/);
    assert.match(serialized, /ControlRelayK31\.spareOut/);
    assert.match(serialized, /SpareTerminalXT31\.in1/);
    assert.match(serialized, /control-and-plc-logic/);
    assert.match(serialized, /field-wiring-and-terminal-transition/);
    assert.doesNotMatch(serialized, /examples\/m29|examples\/m30|innerText|textContent|svgX|svgY|screenX|screenY/);
});

test('M31 graph proof rejects duplicate occurrences and repeated terminal labels', () => {
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m31-sample-project.js');
    const { assertGraphWorkbenchProof } = require(smokeScript);
    const baseGraphWorkbenchProof = {
        activeViewId: 'cabinet',
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
        representationProof: {
            representationCount: 2,
            presentationTerminalCount: 2,
            presentationLabelCount: 2,
            representationIds: ['athena-industrial-control-v0:terminal-block'],
            semanticIds: ['component:FieldTerminalXT31'],
            terminalNumbers: ['1', '2'],
            fallbackRepresentationIds: [],
        },
        routeProof: {
            routeCount: 1,
            terminalCount: 2,
            routesWithTerminalAnchors: 1,
            centerFallbackRouteIds: [],
        },
        visualProof: {
            svgViewBox: '0 0 720 420',
            viewBoxWidth: 720,
            viewBoxHeight: 420,
            routeBodyIntersectionCount: 0,
            nonOrthogonalSegmentCount: 0,
            nodeSemanticIds: ['component:FieldTerminalXT31'],
        },
        outlineProof: {
            skipped: false,
            hasOutlineWidget: true,
            widgetId: 'outline-view',
            paths: ['RollingShutterGovernedAuthoringProof > SpareTerminalXT31 > in1'],
        },
    };
    const sheetGraphWorkbenchProof = {
        ...baseGraphWorkbenchProof,
        activeViewId: 'documentation',
        documentProjectionProof: {
            hasSheetViewSelector: true,
            sheetViewOptionCount: 2,
            sheetViewOptionTexts: ['1 - Control (control_logic)', '2 - Field Device'],
        },
        sheetSelectorPersistenceProof: {
            skipped: false,
            selectorVisibleAfterViewSwitch: true,
            optionCountBeforeViewSwitch: 2,
            optionCountAfterViewSwitch: 2,
        },
    };

    assert.throws(
        () => assertGraphWorkbenchProof({
            ...baseGraphWorkbenchProof,
            representationProof: {
                ...baseGraphWorkbenchProof.representationProof,
                semanticIds: ['component:FieldTerminalXT31', 'component:FieldTerminalXT31'],
            },
        }, sheetGraphWorkbenchProof),
        /duplicate/i,
    );
    assert.throws(
        () => assertGraphWorkbenchProof({
            ...baseGraphWorkbenchProof,
            representationProof: {
                ...baseGraphWorkbenchProof.representationProof,
                terminalNumbers: ['1', '1'],
            },
        }, sheetGraphWorkbenchProof),
        /repeated terminal/i,
    );
});

test('M31 screenshot guard and usage documentation are wired as secondary evidence', () => {
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m31-sample-project.js');
    const sampleReadme = readRepoFile('examples/m31/sample-project/README.md');

    assert.match(smoke, /ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT/);
    assert.match(smoke, /ATHENA_GRAPH_WORKBENCH_SCREENSHOT=/);
    assert.match(smoke, /assertPngScreenshot/);
    assert.match(smoke, /activeViewId !== 'cabinet'/);
    assert.match(smoke, /0 0 1680 1188/);

    assert.match(sampleReadme, /yarn --cwd ide start:m31/);
    assert.match(sampleReadme, /yarn --cwd ide start:smoke:m31/);
    assert.match(sampleReadme, /m31-graph-workbench-smoke\.png/);
    const forbiddenReadmePattern = new RegExp([
        'QET ' + 'runtime',
        '\\.' + 'elmt',
        'source-level ' + 'geometry',
        'view' + 'Box',
    ].join('|'), 'i');
    assert.doesNotMatch(sampleReadme, forbiddenReadmePattern);
});
