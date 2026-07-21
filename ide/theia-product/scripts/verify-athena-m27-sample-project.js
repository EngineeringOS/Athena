const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 180000;

async function main() {
    const repositoryRoot = resolveM27SampleProject();
    const screenshotPath = resolveM27GraphViewScreenshotPath();
    assertInstalledLspHostPresent();
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const workingDirectory = path.resolve(__dirname, '..');
    const child = spawn(
        electronBinary,
        [entryScript, repositoryRoot],
        {
            cwd: workingDirectory,
            env: {
                ...process.env,
                ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
                ATHENA_ELECTRON_TEMP_USER_DATA: '1',
                ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'documentation',
                ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: 'src/01-workspace-semantic-source.athena',
                ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH: 'ProfessionalSheetProofAcceptance > MainPowerSupplyPS1',
                ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: screenshotPath,
                ELECTRON_ENABLE_LOGGING: '1'
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true
        }
    );

    let sawWindowCreated = false;
    let sawReady = false;
    let openedWorkspace;
    let graphWorkbenchProof;
    let graphWorkbenchScreenshot;
    let resolvedJavaHome;
    let unresolvedJavaSignal;
    const outputLines = [];

    const recordLine = line => {
        const trimmedLine = line.trim();
        if (!trimmedLine) {
            return;
        }
        outputLines.push(trimmedLine);
        if (trimmedLine === ATHENA_WINDOW_CREATED_SENTINEL) {
            sawWindowCreated = true;
        }
        if (trimmedLine === ATHENA_READY_SENTINEL) {
            sawReady = true;
        }
        if (trimmedLine.startsWith(ATHENA_WORKSPACE_OPENED_SENTINEL)) {
            openedWorkspace = trimmedLine.substring(ATHENA_WORKSPACE_OPENED_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL)) {
            graphWorkbenchProof = JSON.parse(trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL.length));
        }
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL)) {
            graphWorkbenchScreenshot = trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_JAVA_SENTINEL)) {
            resolvedJavaHome = trimmedLine.substring(ATHENA_JAVA_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_JAVA_UNRESOLVED_SENTINEL)) {
            unresolvedJavaSignal = trimmedLine.substring(ATHENA_JAVA_UNRESOLVED_SENTINEL.length);
        }
    };

    child.stdout.setEncoding('utf8');
    child.stderr.setEncoding('utf8');
    child.stdout.on('data', chunk => chunk.split(/\r?\n/).forEach(recordLine));
    child.stderr.on('data', chunk => chunk.split(/\r?\n/).forEach(recordLine));

    const timeoutHandle = setTimeout(() => {
        child.kill();
    }, STARTUP_TIMEOUT_MS);

    const exitCode = await new Promise(resolveExit => {
        child.on('exit', code => resolveExit(code ?? -1));
        child.on('error', () => resolveExit(-1));
    });
    clearTimeout(timeoutHandle);

    if (!sawWindowCreated || !sawReady || exitCode !== 0) {
        throw new Error(
            `Athena M27 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M27 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M27 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }
    if (!graphWorkbenchProof) {
        throw new Error(`Athena M27 sample project smoke did not report graph workbench DOM proof.\n${outputLines.join('\n')}`);
    }

    assertGraphWorkbenchProof(graphWorkbenchProof);
    assertRouteProof(graphWorkbenchProof.routeProof);
    assertRepresentationProof(graphWorkbenchProof.representationProof);
    assertDocumentProjectionProof(graphWorkbenchProof.documentProjectionProof);
    assertSheetSurfaceProof(graphWorkbenchProof.sheetSurfaceProof);
    assertSheetChromeVisualProof(graphWorkbenchProof.sheetChromeVisualProof);
    assertDensityProof(graphWorkbenchProof.densityProof);
    assertVisualProof(graphWorkbenchProof.visualProof);
    assertNoOrphanRouteEndpointNodes(graphWorkbenchProof.routeProof, graphWorkbenchProof.visualProof);
    assertAllSheetVisualProof(graphWorkbenchProof.allSheetVisualProof);
    assertSheetSelectorPersistenceProof(graphWorkbenchProof.sheetSelectorPersistenceProof);
    assertGraphWorkbenchScreenshot(graphWorkbenchScreenshot, screenshotPath);

    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(graphWorkbenchProof)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL}${graphWorkbenchScreenshot}`);
    console.log('Athena M27 semantic proof passed.');
    console.log(`Athena M27 sample project smoke passed. workspace=${openedWorkspace} javaHome=${resolvedJavaHome || 'n/a'}`);
}

function assertVisualProof(visualProof) {
    if (!visualProof) {
        throw new Error('Athena M27 smoke did not receive visualProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (visualProof.viewportWidth <= 0 || visualProof.viewportHeight <= 0) {
        failures.push(`viewport has invalid dimensions: ${visualProof.viewportWidth}x${visualProof.viewportHeight}`);
    }
    if (visualProof.svgViewBox === '0 0 1680 1188') {
        failures.push('svg viewBox still uses full A3 publication bounds instead of active content bounds');
    }
    if (visualProof.viewBoxWidth <= 0 || visualProof.viewBoxHeight <= 0) {
        failures.push(`invalid svg viewBox dimensions: ${visualProof.svgViewBox || '<empty>'}`);
    }
    if (visualProof.viewBoxHeight >= 1188) {
        failures.push(`svg viewBox still uses publication-height bounds instead of active content bounds: ${visualProof.svgViewBox}`);
    }
    if (visualProof.sheetWidth > visualProof.viewportWidth || visualProof.sheetHeight > visualProof.viewportHeight) {
        failures.push(
            `active SVG surface does not fit the main viewport: sheet=${visualProof.sheetWidth}x${visualProof.sheetHeight} ` +
            `viewport=${visualProof.viewportWidth}x${visualProof.viewportHeight}`
        );
    }
    if (visualProof.sheetCenterDeltaX > 24 || visualProof.sheetCenterDeltaY > 24) {
        failures.push(
            `active SVG surface is not centered in the main viewport: ` +
            `delta=${visualProof.sheetCenterDeltaX},${visualProof.sheetCenterDeltaY}`
        );
    }
    if (visualProof.routeCount < 1) {
        failures.push('no rendered route paths detected');
    }
    if (visualProof.routesWithSerializedPoints !== visualProof.routeCount) {
        failures.push(`routes missing serialized route points: ${visualProof.routesWithSerializedPoints}/${visualProof.routeCount}`);
    }
    if (visualProof.nonOrthogonalSegmentCount !== 0) {
        failures.push(`non-orthogonal rendered route segments: ${visualProof.nonOrthogonalSegmentCount}`);
    }
    if (visualProof.routeBodyIntersectionCount !== 0) {
        failures.push(`routes crossing component bodies: ${JSON.stringify(visualProof.routeBodyIntersections)}`);
    }
    if (failures.length > 0) {
        throw new Error(`Athena M27 visual graph proof failed: ${failures.join('; ')}\n${JSON.stringify(visualProof, null, 2)}`);
    }
}

function assertAllSheetVisualProof(allSheetVisualProof) {
    if (!Array.isArray(allSheetVisualProof) || allSheetVisualProof.length < 3) {
        throw new Error(`Athena M27 smoke did not inspect every document sheet projection.\n${JSON.stringify(allSheetVisualProof, null, 2)}`);
    }
    const failures = [];
    for (const sheetProof of allSheetVisualProof) {
        if (!sheetProof.selected) {
            failures.push(`${sheetProof.sheetViewId}: not selected during inspection`);
        }
        try {
            assertRouteProof(sheetProof.routeProof);
            assertRepresentationProof(sheetProof.representationProof);
            assertVisualProof(sheetProof.visualProof);
            assertNoOrphanRouteEndpointNodes(sheetProof.routeProof, sheetProof.visualProof);
        } catch (error) {
            failures.push(`${sheetProof.sheetViewId}: ${error instanceof Error ? error.message : String(error)}`);
        }
    }
    if (failures.length > 0) {
        throw new Error(`Athena M27 all-sheet visual proof failed: ${failures.join('\n')}`);
    }
}

function assertSheetSelectorPersistenceProof(sheetSelectorPersistenceProof) {
    if (!sheetSelectorPersistenceProof) {
        throw new Error('Athena M27 smoke did not receive sheetSelectorPersistenceProof in graph-workbench proof payload.');
    }
    if (sheetSelectorPersistenceProof.skipped) {
        throw new Error(`Athena M27 sheet selector persistence proof skipped: ${sheetSelectorPersistenceProof.reason || '<no reason>'}`);
    }
    const failures = [];
    if (!sheetSelectorPersistenceProof.selectorVisibleAfterViewSwitch) {
        failures.push(`selector disappeared after switching to ${sheetSelectorPersistenceProof.alternateViewId || '<unknown view>'}`);
    }
    if (sheetSelectorPersistenceProof.optionCountAfterViewSwitch !== sheetSelectorPersistenceProof.optionCountBeforeViewSwitch) {
        failures.push(
            `selector option count changed after view switch: before=${sheetSelectorPersistenceProof.optionCountBeforeViewSwitch} ` +
            `after=${sheetSelectorPersistenceProof.optionCountAfterViewSwitch}`
        );
    }
    if (!sheetSelectorPersistenceProof.restoredSheetViewId) {
        failures.push('sheet selector did not restore a document sheet after returning from alternate view');
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M27 sheet selector persistence proof failed: ${failures.join('; ')}\n${JSON.stringify(sheetSelectorPersistenceProof, null, 2)}`
        );
    }
}

function assertNoOrphanRouteEndpointNodes(routeProof, visualProof) {
    const renderedComponentIds = new Set((visualProof?.nodeSemanticIds ?? [])
        .filter(semanticId => typeof semanticId === 'string' && semanticId.startsWith('component:')));
    const failures = [];
    for (const routeState of routeProof?.routeStates ?? []) {
        const endpointOwners = routeEndpointOwnerComponentIds(routeState.semanticId);
        for (const ownerId of endpointOwners) {
            if (!renderedComponentIds.has(ownerId)) {
                failures.push(`${routeState.semanticId} endpoint owner ${ownerId} has no same-sheet rendered node`);
            }
        }
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M27 route endpoint proof failed: ${failures.join('; ')}\n` +
            JSON.stringify({ routeProof, visualProof }, null, 2)
        );
    }
}

function routeEndpointOwnerComponentIds(connectionSemanticId) {
    if (typeof connectionSemanticId !== 'string' || !connectionSemanticId.startsWith('connection:')) {
        return [];
    }
    return [...new Set(
        connectionSemanticId
            .substring('connection:'.length)
            .split('->')
            .map(endpoint => endpoint.split('.')[0])
            .filter(Boolean)
            .map(ownerName => `component:${ownerName}`)
    )];
}

function assertGraphWorkbenchScreenshot(graphWorkbenchScreenshot, expectedPath) {
    if (graphWorkbenchScreenshot !== expectedPath) {
        throw new Error(`Athena M27 graph screenshot path mismatch. expected=${expectedPath} actual=${graphWorkbenchScreenshot || '<missing>'}`);
    }
    const stats = fs.existsSync(expectedPath) ? fs.statSync(expectedPath) : undefined;
    if (!stats || stats.size < 10000) {
        throw new Error(`Athena M27 graph screenshot was not captured or is too small: ${expectedPath}`);
    }
}

function assertGraphWorkbenchProof(graphWorkbenchProof) {
    const missingGraphProof = [
        'root',
        'stage',
        'viewport',
        'sheet',
        'canvas',
        'floatingBarTransparent',
        'bottomDockTransparent',
        'zoomDockTransparent',
        'sheetTransparent',
        'sheetFrame',
        'stageHasGrid',
        'infoPopoverOpened',
        'infoPopoverClosedOnWhitespace'
    ].filter(key => graphWorkbenchProof[key] !== true);
    if (missingGraphProof.length > 0) {
        throw new Error(
            `Athena M27 graph-workbench DOM proof failed: ${missingGraphProof.join(', ')}\n${JSON.stringify(graphWorkbenchProof)}`
        );
    }
}

function assertRouteProof(routeProof) {
    if (!routeProof) {
        throw new Error('Athena M27 smoke did not receive routeProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (routeProof.routeCount < 1) {
        failures.push('routeCount < 1');
    }
    if (routeProof.terminalCount < 2) {
        failures.push('terminalCount < 2');
    }
    if (routeProof.routesWithOrthogonalBends < 1) {
        failures.push('no route reports orthogonal bend points');
    }
    const verboseVisibleLabels = (routeProof.visibleLabelTexts ?? [])
        .filter(label => label.includes(' -> ') && label.includes('.'));
    if (verboseVisibleLabels.length > 0) {
        failures.push(`verbose semantic route labels visible: ${verboseVisibleLabels.join(', ')}`);
    }
    if (failures.length > 0) {
        throw new Error(`Athena M27 route proof failed: ${failures.join('; ')}\n${JSON.stringify(routeProof, null, 2)}`);
    }
}

function assertRepresentationProof(representationProof) {
    if (!representationProof) {
        throw new Error('Athena M27 smoke did not receive representationProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (representationProof.representationCount < 2) {
        failures.push('representationCount < 2');
    }
    if (representationProof.presentationTerminalCount < 2) {
        failures.push('presentationTerminalCount < 2');
    }
    if (representationProof.fallbackRepresentationIds.length > 0) {
        failures.push(`fallback representation ids: ${representationProof.fallbackRepresentationIds.join(', ')}`);
    }
    const duplicateSemanticIds = (representationProof.semanticIds ?? [])
        .filter((semanticId, index, all) => all.indexOf(semanticId) !== index);
    if (duplicateSemanticIds.length > 0) {
        failures.push(`duplicate active-sheet representation semantic ids: ${[...new Set(duplicateSemanticIds)].join(', ')}`);
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M27 representation proof failed: ${failures.join('; ')}\n${JSON.stringify(representationProof, null, 2)}`
        );
    }
}

function assertDocumentProjectionProof(documentProjectionProof) {
    if (!documentProjectionProof) {
        throw new Error('Athena M27 smoke did not receive documentProjectionProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (!documentProjectionProof.hasSheetViewSelector) {
        failures.push('sheet-view selector missing');
    }
    if (documentProjectionProof.sheetViewOptionCount < 1) {
        failures.push('sheetViewOptionCount < 1');
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M27 document projection proof failed: ${failures.join('; ')}\n${JSON.stringify(documentProjectionProof, null, 2)}`
        );
    }
}

function assertSheetSurfaceProof(sheetSurfaceProof) {
    if (!sheetSurfaceProof) {
        throw new Error('Athena M27 smoke did not receive sheetSurfaceProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (!sheetSurfaceProof.factDriven) {
        failures.push('sheet surface is not fact-driven');
    }
    if (!sheetSurfaceProof.surfaceId) {
        failures.push('surfaceId missing');
    }
    if (sheetSurfaceProof.sheetSize !== 'A3') {
        failures.push(`sheetSize expected A3 but received ${sheetSurfaceProof.sheetSize || '<empty>'}`);
    }
    if (sheetSurfaceProof.orientation !== 'landscape') {
        failures.push(`orientation expected landscape but received ${sheetSurfaceProof.orientation || '<empty>'}`);
    }
    if (!sheetSurfaceProof.hasMargins) {
        failures.push('sheet margins missing');
    }
    if (sheetSurfaceProof.zoneColumnCount < 4 || sheetSurfaceProof.zoneRowCount < 3) {
        failures.push(`insufficient sheet zones: columns=${sheetSurfaceProof.zoneColumnCount} rows=${sheetSurfaceProof.zoneRowCount}`);
    }
    if (!sheetSurfaceProof.hasTitleBlock) {
        failures.push('title block missing');
    }
    if (!(sheetSurfaceProof.titleFieldRoles ?? []).includes('sheet')) {
        failures.push('sheet title field missing');
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M27 sheet surface proof failed: ${failures.join('; ')}\n${JSON.stringify(sheetSurfaceProof, null, 2)}`
        );
    }
}

function assertSheetChromeVisualProof(sheetChromeVisualProof) {
    if (!sheetChromeVisualProof) {
        throw new Error('Athena M27 smoke did not receive sheetChromeVisualProof in graph-workbench proof payload.');
    }
    const failures = [];
    const transparentValues = new Set(['transparent', 'rgba(0, 0, 0, 0)']);
    if (!transparentValues.has(sheetChromeVisualProof.sheetBackgroundColor)) {
        failures.push(`sheet background is not transparent: ${sheetChromeVisualProof.sheetBackgroundColor || '<empty>'}`);
    }
    if (!transparentValues.has(sheetChromeVisualProof.sheetFrameBackgroundColor)) {
        failures.push(`sheet frame background is not transparent: ${sheetChromeVisualProof.sheetFrameBackgroundColor || '<empty>'}`);
    }
    for (const key of [
        'sheetBorderTopWidth',
        'sheetBorderRightWidth',
        'sheetBorderBottomWidth',
        'sheetBorderLeftWidth',
        'sheetFrameBorderTopWidth',
        'sheetFrameBorderRightWidth',
        'sheetFrameBorderBottomWidth',
        'sheetFrameBorderLeftWidth'
    ]) {
        if (sheetChromeVisualProof[key] !== '0px') {
            failures.push(`${key} expected 0px but received ${sheetChromeVisualProof[key] || '<empty>'}`);
        }
    }
    for (const key of ['sheetBoxShadow', 'sheetFrameBoxShadow']) {
        if (sheetChromeVisualProof[key] !== 'none') {
            failures.push(`${key} expected none but received ${sheetChromeVisualProof[key] || '<empty>'}`);
        }
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M27 sheet chrome visual proof failed: ${failures.join('; ')}\n${JSON.stringify(sheetChromeVisualProof, null, 2)}`
        );
    }
}

function assertDensityProof(densityProof) {
    if (!densityProof) {
        throw new Error('Athena M27 smoke did not receive densityProof in graph-workbench proof payload.');
    }
    const failures = [];
    const expectedTokens = {
        electricalLineWidth: '1.6px',
        terminalTextSize: '10px',
        deviceTextSize: '11px',
        routeLabelSize: '10px',
        referenceMarkerSize: '10px'
    };
    for (const [key, expected] of Object.entries(expectedTokens)) {
        if (densityProof[key] !== expected) {
            failures.push(`${key} expected ${expected} but received ${densityProof[key] || '<empty>'}`);
        }
    }
    if (densityProof.deferredRouteLabelCount < 1) {
        failures.push('no deferred route labels available for selection detail');
    }
    if (densityProof.visibleVerboseRouteLabelCount !== 0) {
        failures.push(`visible verbose route labels: ${densityProof.visibleVerboseRouteLabelCount}`);
    }
    if (densityProof.textBoxCount < 1) {
        failures.push('no bounded sheet text nodes detected');
    }
    if (densityProof.invalidTextBoxCount !== 0) {
        failures.push(`invalid text bounding boxes: ${densityProof.invalidTextBoxCount}`);
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M27 density proof failed: ${failures.join('; ')}\n${JSON.stringify(densityProof, null, 2)}`
        );
    }
}

function assertInstalledLspHostPresent() {
    const launcher = path.resolve(
        __dirname,
        '..',
        '..',
        'lsp',
        'build',
        'install',
        'athena-lsp-host',
        'bin',
        process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host'
    );
    if (!fs.existsSync(launcher)) {
        throw new Error(
            `Athena M27 smoke requires the installed LSP host before product proof. Missing ${launcher}. Run: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
        );
    }
}

function resolveM27SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm27', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        path.join('src', '01-workspace-semantic-source.athena'),
        path.join('src', '02-field-assets-not-a-sheet.athena')
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M27 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

function resolveM27GraphViewScreenshotPath() {
    return path.resolve(
        __dirname,
        '..',
        '..',
        '..',
        '_bmad-output',
        'implementation-artifacts',
        'm27',
        'proofs',
        'm27-graph-workbench-smoke.png'
    );
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
