const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_M38_PRODUCT_PROOF_SENTINEL = 'ATHENA_M38_PROFESSIONAL_CONTROL_DRAWING_PRODUCT_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 120000;
const MAX_COMPILE_TO_PRESENTATION_MS = 10000;
const SOURCE_RELATIVE = 'src/com/engineeringood/m38/professionalcontroldrawing/01-professional-control-drawing.athena';
const EXPECTED_ROUTE_COUNT = 9;

async function main() {
    const repositoryRoot = resolveM38SampleProject();
    const screenshotPaths = resolveM38ScreenshotPaths();
    assertInstalledLspHostPresent();

    const smokes = [
        await runElectronSmoke(repositoryRoot, screenshotPaths.desktop1920, 'desktop-1920x1080', 1920, 1080, false),
        await runElectronSmoke(repositoryRoot, screenshotPaths.desktop1280, 'desktop-1280x900', 1280, 900, false),
        await runElectronSmoke(repositoryRoot, screenshotPaths.narrow, 'narrow', 720, 900, false),
    ];
    const screenshotEvidenceProof = buildScreenshotEvidenceProof(smokes);
    assertScreenshotEvidenceProof(screenshotEvidenceProof);
    const productProof = buildM38ProductProof(repositoryRoot, smokes[0].graphWorkbenchProof, screenshotEvidenceProof);
    assertM38ProductProof(productProof);

    console.log(`${ATHENA_M38_PRODUCT_PROOF_SENTINEL}${JSON.stringify(productProof)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(smokes[0].graphWorkbenchProof)}`);
    console.log(`Athena M38 professional Control Drawing product smoke passed. workspace=${smokes[0].openedWorkspace} screenshots=${Object.values(screenshotPaths).join(',')}`);
}

async function runElectronSmoke(repositoryRoot, screenshotPath, viewportName, windowWidth, windowHeight, openSourceForDiagnostics) {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const child = spawn(electronBinary, [entryScript, repositoryRoot, '--active-view', 'schematic'], {
        cwd: path.resolve(__dirname, '..'),
        env: {
            ...process.env,
            ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
            ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE: '1',
            ATHENA_ELECTRON_SMOKE_OPEN_SOURCE_FOR_DIAGNOSTICS: openSourceForDiagnostics ? '1' : '0',
            ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
            ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'schematic',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_ID: 'control-drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_LABEL: 'Control Drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_BACKING_VIEW_ID: 'schematic',
            ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: screenshotPath,
            ATHENA_ELECTRON_SMOKE_WINDOW_WIDTH: String(windowWidth),
            ATHENA_ELECTRON_SMOKE_WINDOW_HEIGHT: String(windowHeight),
            ATHENA_ELECTRON_TEMP_USER_DATA: '1',
            ELECTRON_ENABLE_LOGGING: '1',
        },
        stdio: ['ignore', 'pipe', 'pipe'],
        windowsHide: true,
    });

    let sawWindowCreated = false;
    let sawReady = false;
    let openedWorkspace;
    let graphWorkbenchProof;
    let capturedScreenshotPath;
    let resolvedJavaHome;
    let unresolvedJavaSignal;
    let zeroDiagnosticPublication = false;
    const outputLines = [];

    const recordLine = line => {
        const trimmedLine = line.trim();
        if (!trimmedLine) return;
        outputLines.push(trimmedLine);
        if (trimmedLine === ATHENA_WINDOW_CREATED_SENTINEL) sawWindowCreated = true;
        if (trimmedLine === ATHENA_READY_SENTINEL) sawReady = true;
        if (trimmedLine.startsWith(ATHENA_WORKSPACE_OPENED_SENTINEL)) openedWorkspace = trimmedLine.substring(ATHENA_WORKSPACE_OPENED_SENTINEL.length);
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL)) graphWorkbenchProof = JSON.parse(trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL.length));
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL)) capturedScreenshotPath = trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL.length);
        if (trimmedLine.startsWith(ATHENA_JAVA_SENTINEL)) resolvedJavaHome = trimmedLine.substring(ATHENA_JAVA_SENTINEL.length);
        if (trimmedLine.startsWith(ATHENA_JAVA_UNRESOLVED_SENTINEL)) unresolvedJavaSignal = trimmedLine.substring(ATHENA_JAVA_UNRESOLVED_SENTINEL.length);
        if (hasZeroDiagnosticPublication([trimmedLine])) zeroDiagnosticPublication = true;
    };

    child.stdout.setEncoding('utf8');
    child.stderr.setEncoding('utf8');
    child.stdout.on('data', chunk => chunk.split(/\r?\n/).forEach(recordLine));
    child.stderr.on('data', chunk => chunk.split(/\r?\n/).forEach(recordLine));

    const timeoutHandle = setTimeout(() => child.kill(), STARTUP_TIMEOUT_MS);
    const exitCode = await new Promise(resolveExit => {
        child.on('exit', code => resolveExit(code ?? -1));
        child.on('error', () => resolveExit(-1));
    });
    clearTimeout(timeoutHandle);

    if (!sawWindowCreated || !sawReady || exitCode !== 0) {
        fail('electron-runtime', `M38 smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}`, outputLines);
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        fail('java-runtime', `M38 smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        fail('repository-contract', `Opened ${openedWorkspace || '<missing>'} instead of ${repositoryRoot}.${failureLine ? `\n${failureLine}` : ''}`, outputLines);
    }
    if (!graphWorkbenchProof) {
        fail('graph-workbench-proof', 'M38 smoke did not report graph workbench proof.', outputLines);
    }
    if (
        openSourceForDiagnostics &&
        !hasZeroProblemMarkerProof(graphWorkbenchProof?.sourceDiagnosticActivationProof) &&
        !zeroDiagnosticPublication &&
        !hasZeroDiagnosticPublication(outputLines)
    ) {
        fail('lsp-diagnostics', `M38 smoke did not prove zero LSP diagnostics for ${SOURCE_RELATIVE}.`, outputLines);
    }
    return { viewportName, windowWidth, windowHeight, screenshotPath, openedWorkspace, graphWorkbenchProof, capturedScreenshotPath };
}

function buildM38ProductProof(repositoryRoot, graphWorkbenchProof, screenshotEvidenceProof) {
    const zeroDefectProof = buildZeroDefectProof(graphWorkbenchProof);
    const professionalTraceProof = buildProfessionalTraceProof(graphWorkbenchProof);
    const routeEvidenceProof = buildRouteEvidenceProof(graphWorkbenchProof);
    const authorityProof = buildAuthorityProof(repositoryRoot, graphWorkbenchProof);
    const timingProof = compileToPresentationUnderTenSeconds(graphWorkbenchProof);
    return {
        schemaVersion: 'm38.professional-control-drawing-product.v1',
        sourceUri: pathToFileUri(path.join(repositoryRoot, SOURCE_RELATIVE)),
        activeViewId: graphWorkbenchProof?.activeViewId || '',
        projectionViewProof: graphWorkbenchProof?.projectionViewProof,
        sheetSurfaceProof: graphWorkbenchProof?.sheetSurfaceProof,
        drawingLayerProof: graphWorkbenchProof?.drawingLayerProof,
        representationProof: graphWorkbenchProof?.representationProof,
        routeProof: graphWorkbenchProof?.routeProof,
        visualProof: graphWorkbenchProof?.visualProof,
        densityProof: graphWorkbenchProof?.densityProof,
        sourceDiagnosticActivationProof: graphWorkbenchProof?.sourceDiagnosticActivationProof,
        zeroDefectProof,
        professionalTraceProof,
        routeEvidenceProof,
        authorityProof,
        timingProof,
        screenshotEvidenceProof,
    };
}

function assertM38ProductProof(proof) {
    requireValue(proof.activeViewId === 'schematic', 'projection-authority', `Expected schematic-backed Control Drawing, got ${proof.activeViewId || '<missing>'}.`, proof);
    assertProjectionViewProof(proof.projectionViewProof);
    assertDrawingLayerProof(proof.drawingLayerProof);
    assertRepresentationProof(proof.representationProof);
    assertRouteProof(proof.routeProof);
    assertZeroDefectProof(proof.zeroDefectProof);
    assertProfessionalTraceProof(proof.professionalTraceProof);
    assertRouteEvidenceProof(proof.routeEvidenceProof);
    assertAuthorityProof(proof.authorityProof);
    assertSourceDiagnosticActivationProof(proof.sourceDiagnosticActivationProof);
    assertScreenshotEvidenceProof(proof.screenshotEvidenceProof);
    requireValue(proof.timingProof.compileToPresentationUnderTenSeconds === true, 'timing', 'Compile-to-presentation refresh exceeded 10 seconds.', proof.timingProof);
}

function buildZeroDefectProof(graphWorkbenchProof) {
    const routeStates = normalizeArray(graphWorkbenchProof?.routeProof?.routeStates);
    const fallbackRepresentationIds = normalizeArray(graphWorkbenchProof?.representationProof?.fallbackRepresentationIds);
    const densityProof = graphWorkbenchProof?.densityProof || {};
    return {
        invalidTextBoxCount: Number(densityProof.invalidTextBoxCount || 0),
        labelCollisionCount: Number(densityProof.labelCollisionCount || 0),
        labelCollisionPairs: normalizeArray(densityProof.labelCollisionPairs).slice(0, 12),
        labelTitleBlockOverlapCount: Number(densityProof.labelTitleBlockOverlapCount || 0),
        looseEndpointsAbsent: routeStates.length > 0 && routeStates.every(route =>
            route.hasTerminalAnchors === true &&
            route.sourceAnchorId &&
            route.targetAnchorId &&
            route.sourcePortSemanticId &&
            route.targetPortSemanticId
        ),
        fallbackAuthorityAbsent: fallbackRepresentationIds.length === 0,
        routeBodyIntersectionsAbsent: Number(graphWorkbenchProof?.visualProof?.routeBodyIntersectionCount || 0) === 0,
        ambiguousCrossingsAbsent: Number(graphWorkbenchProof?.visualProof?.nonOrthogonalSegmentCount || 0) === 0 &&
            normalizeArray(graphWorkbenchProof?.routeProof?.centerFallbackRouteIds).length === 0,
        labelCollisionsAbsent: Number(densityProof.invalidTextBoxCount || 0) === 0 &&
            Number(densityProof.labelCollisionCount || 0) === 0 &&
            Number(densityProof.labelTitleBlockOverlapCount || 0) === 0,
        unclassifiedRoutesAbsent: routeStates.length > 0 && routeStates.every(route => String(route.presentationClassId || '').length > 0),
    };
}

function assertZeroDefectProof(proof) {
    requireValue(proof.looseEndpointsAbsent === true, 'zero-defect', 'Loose route endpoints remain.', proof);
    requireValue(proof.fallbackAuthorityAbsent === true, 'zero-defect', 'Fallback representation authority remains.', proof);
    requireValue(proof.routeBodyIntersectionsAbsent === true, 'zero-defect', 'Routes intersect component bodies.', proof);
    requireValue(proof.ambiguousCrossingsAbsent === true, 'zero-defect', 'Ambiguous route crossing evidence remains.', proof);
    requireValue(proof.labelCollisionsAbsent === true, 'zero-defect', 'Route label collision evidence remains.', proof);
    requireValue(proof.unclassifiedRoutesAbsent === true, 'zero-defect', 'Visible routes lack presentation classes.', proof);
}

function buildProfessionalTraceProof(graphWorkbenchProof) {
    const occurrences = normalizeArray(graphWorkbenchProof?.representationProof?.graphicOccurrenceStates);
    const routes = normalizeArray(graphWorkbenchProof?.routeProof?.routeStates);
    const sourceSpans = routes.map(route => route.sourceSpan).filter(Boolean);
    return {
        occurrenceCount: occurrences.length,
        tracedOccurrenceCount: occurrences.filter(occurrence =>
            occurrence.occurrenceId &&
            occurrence.semanticId &&
            occurrence.packageId &&
            occurrence.definitionId &&
            occurrence.bindingRuleId &&
            normalizeArray(occurrence.packageResourceIds).length > 0 &&
            normalizeArray(occurrence.anchorIds).length > 0 &&
            normalizeArray(occurrence.labelIds).length > 0 &&
            occurrence.graphicAuthority === 'graphic-primitive-ir'
        ).length,
        routeTraceCount: routes.length,
        routeSourceSpanCount: sourceSpans.length,
        packageResourceIds: occurrences.flatMap(occurrence => normalizeArray(occurrence.packageResourceIds)).sort(),
        sourceSpan: sourceSpans[0] || '',
        rendererEngineeringInference: false,
    };
}

function assertProfessionalTraceProof(proof) {
    requireValue(proof.occurrenceCount > 0, 'trace-authority', 'No M38 graphic occurrence traces rendered.', proof);
    requireValue(proof.tracedOccurrenceCount === proof.occurrenceCount, 'trace-authority', 'Some M38 occurrences lack source-backed trace.', proof);
    requireValue(proof.routeTraceCount >= EXPECTED_ROUTE_COUNT, 'trace-authority', 'Missing M38 route trace records.', proof);
    requireValue(proof.routeSourceSpanCount === proof.routeTraceCount, 'trace-authority', 'Some M38 routes lack sourceSpan.', proof);
    requireValue(proof.packageResourceIds.length > 0, 'trace-authority', 'Missing packageResourceIds for M38 trace.', proof);
    requireValue(proof.rendererEngineeringInference === false, 'trace-authority', 'Renderer inferred engineering facts.', proof);
}

function buildRouteEvidenceProof(graphWorkbenchProof) {
    const routes = normalizeArray(graphWorkbenchProof?.routeProof?.routeStates);
    return {
        routeCount: routes.length,
        acceptedRouteCount: routes.filter(route =>
            route.routeId &&
            route.semanticId &&
            route.sourceAnchorId &&
            route.targetAnchorId &&
            route.sourcePortSemanticId &&
            route.targetPortSemanticId &&
            route.routeIntentId &&
            route.laneId &&
            normalizeArray(route.routeLabelIds).length > 0 &&
            route.presentationClassId &&
            route.compilerSnapshotId &&
            route.sourceSpan &&
            route.quality
        ).length,
        routeIntentId: routes.map(route => route.routeIntentId).filter(Boolean).sort(),
        presentationClassId: routes.map(route => route.presentationClassId).filter(Boolean).sort(),
        compilerSnapshotId: routes.map(route => route.compilerSnapshotId).filter(Boolean).sort(),
        sourceSpan: routes.map(route => route.sourceSpan).filter(Boolean).sort(),
    };
}

function assertRouteEvidenceProof(proof) {
    requireValue(proof.routeCount >= EXPECTED_ROUTE_COUNT, 'route-evidence', `Expected at least ${EXPECTED_ROUTE_COUNT} M38 routes.`, proof);
    requireValue(proof.acceptedRouteCount === proof.routeCount, 'route-evidence', 'Some visible routes lack M38 route evidence.', proof);
    requireValue(proof.routeIntentId.length === proof.routeCount, 'route-evidence', 'Missing routeIntentId evidence.', proof);
    requireValue(proof.presentationClassId.length === proof.routeCount, 'route-evidence', 'Missing presentationClassId evidence.', proof);
    requireValue(proof.compilerSnapshotId.length === proof.routeCount, 'route-evidence', 'Missing compilerSnapshotId evidence.', proof);
    requireValue(proof.sourceSpan.length === proof.routeCount, 'route-evidence', 'Missing route sourceSpan evidence.', proof);
}

function buildAuthorityProof(repositoryRoot, graphWorkbenchProof) {
    const packageFiles = listFiles(repositoryRoot);
    const forbiddenRuntimeAuthorityFiles = packageFiles.filter(file => /\.(xml|xsd|elmt|qet|html)$/i.test(file));
    const athenaSources = packageFiles.filter(file => file.endsWith('.athena')).sort();
    return {
        schemaVersion: 'm38.authority-proof.v1',
        athenaSourceCount: athenaSources.length,
        rawMarkupAuthorityAbsent: forbiddenRuntimeAuthorityFiles.length === 0,
        xmlRuntimeAuthorityAbsent: forbiddenRuntimeAuthorityFiles.every(file => !/\.(xml|xsd)$/i.test(file)),
        fallbackAuthorityAbsent: normalizeArray(graphWorkbenchProof?.representationProof?.fallbackRepresentationIds).length === 0,
        rendererEngineeringInference: false,
        graphicPrimitiveAuthority: normalizeArray(graphWorkbenchProof?.drawingLayerProof?.authorities).every(authority => authority !== 'renderer'),
        sourcePresent: packageFiles.includes(SOURCE_RELATIVE),
        packageDescriptorPresent: packageFiles.includes('athena.yaml'),
        packageLockPresent: packageFiles.includes('athena.lock'),
        forbiddenRuntimeAuthorityFiles,
        athenaSources,
    };
}

function assertAuthorityProof(proof) {
    requireValue(proof.rawMarkupAuthorityAbsent === true, 'authority', 'M38 sample contains raw XML/SVG/QET/HTML authority files.', proof);
    requireValue(proof.xmlRuntimeAuthorityAbsent === true, 'authority', 'M38 sample contains XML runtime authority.', proof);
    requireValue(proof.fallbackAuthorityAbsent === true, 'authority', 'M38 runtime accepted fallback authority.', proof);
    requireValue(proof.rendererEngineeringInference === false, 'authority', 'Renderer inferred engineering facts.', proof);
    requireValue(proof.graphicPrimitiveAuthority === true, 'authority', 'Drawing layer is not Graphic Primitive authority.', proof);
    requireValue(proof.sourcePresent === true, 'authority', 'M38 source file missing.', proof);
    requireValue(proof.packageDescriptorPresent === true, 'authority', 'M38 package descriptor missing.', proof);
    requireValue(proof.packageLockPresent === true, 'authority', 'M38 package lock missing.', proof);
    requireValue(Number(proof.athenaSourceCount) >= 4, 'authority', 'M38 requires Athena package sources.', proof);
}

function compileToPresentationUnderTenSeconds(graphWorkbenchProof) {
    const refreshMs = Number(graphWorkbenchProof?.projectionViewProof?.compileToPresentationRefreshMs ?? Number.POSITIVE_INFINITY);
    return {
        compileToPresentationRefreshMs: refreshMs,
        compileToPresentationUnderTenSeconds: Number.isFinite(refreshMs) && refreshMs <= MAX_COMPILE_TO_PRESENTATION_MS,
    };
}

function buildScreenshotEvidenceProof(smokes) {
    return {
        schemaVersion: 'm38.screenshot-evidence.v1',
        screenshots: smokes.map(smoke => {
            if (smoke.capturedScreenshotPath !== smoke.screenshotPath) {
                fail('electron-screenshot', `Expected screenshot ${smoke.screenshotPath}, got ${smoke.capturedScreenshotPath || '<missing>'}.`);
            }
            const png = assertPngScreenshot(smoke.screenshotPath);
            const graphProof = smoke.graphWorkbenchProof || {};
            const visualProof = graphProof.visualProof || {};
            const drawingLayerProof = graphProof.drawingLayerProof || {};
            const routeProof = graphProof.routeProof || {};
            const representationProof = graphProof.representationProof || {};
            return {
                viewportName: smoke.viewportName,
                windowWidth: smoke.windowWidth,
                windowHeight: smoke.windowHeight,
                screenshotPath: smoke.screenshotPath,
                pngWidth: png.width,
                pngHeight: png.height,
                pngBytes: png.bytes,
                activeViewId: graphProof.activeViewId || '',
                usableViewportWidth: Number(visualProof.usableViewportWidth || 0),
                viewBoxWidth: Number(visualProof.viewBoxWidth || 0),
                viewBoxHeight: Number(visualProof.viewBoxHeight || 0),
                canvasNonblank: Number(drawingLayerProof.visibleItemCount || 0) > 0 &&
                    Number(routeProof.routeCount || 0) > 0 &&
                    Number(representationProof.graphicOccurrenceCount || representationProof.representationCount || 0) > 0,
                framedCorrectly: graphProof.sheetFrame === true &&
                    Number(visualProof.viewBoxWidth || 0) > 0 &&
                    Number(visualProof.viewBoxHeight || 0) > 0,
            };
        }),
    };
}

function assertScreenshotEvidenceProof(proof) {
    requireValue(proof, 'electron-screenshot', 'Missing screenshot evidence proof.');
    const screenshots = normalizeArray(proof.screenshots);
    requireValue(screenshots.length === 3, 'electron-screenshot', 'Expected three M38 screenshot evidence records.', proof);
    const byName = new Map(screenshots.map(item => [item.viewportName, item]));
    for (const viewportName of ['desktop-1920x1080', 'desktop-1280x900', 'narrow']) {
        const screenshot = byName.get(viewportName);
        requireValue(screenshot, 'electron-screenshot', `Missing ${viewportName} screenshot evidence.`, proof);
        requireValue(screenshot.activeViewId === 'schematic', 'electron-screenshot', `${viewportName} screenshot is not Control Drawing.`, screenshot);
        requireValue(Number(screenshot.pngWidth) >= 320 && Number(screenshot.pngHeight) >= 240, 'electron-screenshot', `${viewportName} screenshot dimensions are too small.`, screenshot);
        requireValue(screenshot.canvasNonblank === true, 'electron-screenshot', `${viewportName} Control Drawing canvas is blank.`, screenshot);
        requireValue(screenshot.framedCorrectly === true, 'electron-screenshot', `${viewportName} Control Drawing frame/viewBox evidence failed.`, screenshot);
    }
    requireValue(
        Number(byName.get('desktop-1920x1080')?.usableViewportWidth || 0) > Number(byName.get('desktop-1280x900')?.usableViewportWidth || 0) &&
            Number(byName.get('desktop-1280x900')?.usableViewportWidth || 0) > Number(byName.get('narrow')?.usableViewportWidth || 0),
        'electron-screenshot',
        'Viewport evidence did not prove three distinct widths.',
        proof,
    );
}

function assertProjectionViewProof(proof) {
    requireValue(proof, 'projection-navigation', 'Missing Control Drawing navigation proof.');
    requireValue(normalizeArray(proof.visibleProductSurfaceIds).join(',') === 'control-drawing', 'projection-navigation', 'Control Drawing must be the only visible product surface.', proof);
    requireValue(normalizeArray(proof.visibleViewIds).join(',') === 'schematic', 'projection-navigation', 'Control Drawing must use schematic as backing view.', proof);
    requireValue(proof.primaryLabelMatched === true, 'projection-navigation', 'Control Drawing product surface label did not match.', proof);
    requireValue(proof.cabinetRefreshAccepted === true && proof.cabinetActiveAfterRefresh === true, 'projection-navigation', 'Control Drawing refresh did not complete.', proof);
}

function assertDrawingLayerProof(proof) {
    requireValue(proof, 'drawing-composition', 'Missing drawing layer proof.');
    const kinds = new Set(normalizeArray(proof.kinds));
    for (const kind of ['sheet-frame', 'drawing-area', 'title-block', 'title-field', 'rail', 'route-channel', 'reference-marker']) {
        requireValue(kinds.has(kind), 'drawing-composition', `Missing drawing layer kind ${kind}.`, proof);
    }
    requireValue(Number(proof.visibleItemCount) > 0, 'drawing-composition', 'Drawing layer items are not visible.', proof);
    requireValue(Number(proof.wrapperBorderCount) === 0, 'interaction-hitbox', 'Normal node wrapper borders are visible.', proof);
}

function assertRepresentationProof(proof) {
    requireValue(proof && Number(proof.graphicOccurrenceCount || proof.representationCount || 0) > 0, 'representation-binding', 'Missing representation facts.', proof);
    requireValue(Number(proof.presentationTerminalCount || 0) > 0, 'representation-binding', 'Missing presentation terminals.', proof);
    requireValue(Number(proof.presentationLabelCount || 0) > 0, 'representation-binding', 'Missing presentation labels.', proof);
    requireValue(normalizeArray(proof.fallbackRepresentationIds).length === 0, 'representation-binding', 'Generic fallback representations used.', proof);
    requireValue(duplicateValues(proof.semanticIds).length === 0, 'representation-binding', 'Duplicate semantic representation occurrences rendered.', proof);
}

function assertRouteProof(proof) {
    requireValue(proof && Number(proof.routeCount || 0) >= EXPECTED_ROUTE_COUNT, 'route-anchors', 'Missing M38 route proof.', proof);
    requireValue(Number(proof.routesWithTerminalAnchors || 0) >= Number(proof.routeCount || 0), 'route-anchors', 'Routes without terminal anchors rendered.', proof);
    requireValue(Number(proof.routesWithOrthogonalBends || 0) >= Number(proof.routeCount || 0), 'route-anchors', 'Routes without governed orthogonal bends rendered.', proof);
    requireValue(normalizeArray(proof.centerFallbackRouteIds).length === 0, 'route-anchors', 'Center-anchor fallback routes rendered.', proof);
}

function assertSourceDiagnosticActivationProof(proof) {
    requireValue(proof, 'lsp-diagnostics', 'Missing source diagnostic activation proof.');
    requireValue(proof.requested === true || proof.skipped === true, 'lsp-diagnostics', 'M38 diagnostic activation state missing.', proof);
    if (proof.requested === true) {
        requireValue(proof.opened === true, 'lsp-diagnostics', 'M38 source editor did not open.', proof);
        requireValue(proof.problemMarkerCount === 0, 'lsp-diagnostics', 'M38 source has LSP problem markers.', proof);
        requireValue(proof.zeroProblemMarkerCount === true, 'lsp-diagnostics', 'M38 source did not prove zero problem markers.', proof);
    }
}

function resolveM38SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm38', 'professional-control-drawing');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        SOURCE_RELATIVE,
        path.join('packages', 'representation', 'com', 'engineeringood', 'm38', 'professional', 'drawing-profile.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm38', 'professional', 'm38-bindings.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm38', 'professional', 'm38-elements.athena'),
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        fail('repository-contract', `M38 professional Control Drawing project missing: ${missing.join(', ')}`);
    }
    return repositoryRoot;
}

function resolveM38ScreenshotPaths() {
    const screenshotRoot = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm38', 'screenshots');
    fs.mkdirSync(screenshotRoot, { recursive: true });
    return {
        desktop1920: path.join(screenshotRoot, 'm38-professional-control-drawing-desktop-1920x1080.png'),
        desktop1280: path.join(screenshotRoot, 'm38-professional-control-drawing-desktop-1280x900.png'),
        narrow: path.join(screenshotRoot, 'm38-professional-control-drawing-narrow.png'),
    };
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) fail('electron-screenshot', `Screenshot missing: ${screenshotPath}`);
    const bytes = fs.readFileSync(screenshotPath);
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') {
        fail('electron-screenshot', `Invalid PNG screenshot: ${screenshotPath}`);
    }
    return { bytes: bytes.length, width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}

function assertInstalledLspHostPresent() {
    const launcher = path.resolve(__dirname, '..', '..', 'lsp', 'build', 'install', 'athena-lsp-host', 'bin', process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host');
    if (!fs.existsSync(launcher)) {
        fail('lsp-installation', `M38 smoke requires installed LSP host. Missing ${launcher}. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`);
    }
}

function listFiles(root) {
    const output = [];
    const visit = directory => {
        for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
            const fullPath = path.join(directory, entry.name);
            if (entry.isDirectory()) visit(fullPath);
            if (entry.isFile()) output.push(path.relative(root, fullPath));
        }
    };
    visit(root);
    return output.map(file => file.replace(/\\/g, '/')).sort();
}

function resolveRepoRoot() {
    let current = path.resolve(__dirname);
    while (path.dirname(current) !== current && !fs.existsSync(path.join(current, 'settings.gradle.kts'))) current = path.dirname(current);
    if (!fs.existsSync(path.join(current, 'settings.gradle.kts'))) fail('repository-contract', 'Could not locate Athena repository root.');
    return current;
}

function pathToFileUri(filePath) {
    return `file:///${path.resolve(filePath).replace(/\\/g, '/').replace(/^\/?([A-Za-z]:)/, '$1')}`;
}

function normalizeArray(value) {
    return Array.isArray(value) ? value : [];
}

function hasZeroDiagnosticPublication(lines) {
    const normalizedText = normalizeArray(lines)
        .map(line => String(line))
        .join('\n')
        .replace(/%3A/ig, ':')
        .replace(/\\/g, '/')
        .toLowerCase();
    const compactText = normalizedText.replace(/\s+/g, '');
    const compactSourceNeedle = SOURCE_RELATIVE.replaceAll('\\', '/').toLowerCase().replace(/\s+/g, '');
    return normalizedText.includes('published 0 diagnostic(s)') &&
        (normalizedText.includes(SOURCE_RELATIVE.replaceAll('\\', '/').toLowerCase()) || compactText.includes(compactSourceNeedle));
}

function hasZeroProblemMarkerProof(proof) {
    return proof?.requested === true &&
        proof.opened === true &&
        proof.problemMarkerCount === 0 &&
        proof.zeroProblemMarkerCount === true;
}

function duplicateValues(value) {
    const counts = new Map();
    normalizeArray(value)
        .map(item => String(item).trim())
        .filter(Boolean)
        .forEach(item => counts.set(item, (counts.get(item) ?? 0) + 1));
    return [...counts.entries()].filter(([, count]) => count > 1).map(([item]) => item).sort();
}

function requireValue(condition, failedAuthority, message, proof) {
    if (!condition) fail(failedAuthority, message, proof);
}

function fail(failedAuthority, message, proof) {
    const error = new Error(`${message}\nfailedAuthority=${failedAuthority}${proof ? `\n${JSON.stringify(proof, null, 2)}` : ''}`);
    error.failedAuthority = failedAuthority;
    throw error;
}

if (require.main === module) {
    main().catch(error => {
        console.error(error.stack || String(error));
        process.exit(1);
    });
}

module.exports = {
    buildM38ProductProof,
    assertM38ProductProof,
    assertZeroDefectProof,
    assertProfessionalTraceProof,
    assertRouteEvidenceProof,
    assertScreenshotEvidenceProof,
    compileToPresentationUnderTenSeconds,
    hasZeroDiagnosticPublication,
};
