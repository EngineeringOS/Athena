const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_M41_PRODUCT_PROOF_SENTINEL = 'ATHENA_M41_PRODUCT_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 120000;
const SOURCE_RELATIVE = 'src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena';
const REQUIRED_QUALITY_KEYS = [
    'occurrenceOverlapCount',
    'constructContainmentFailureCount',
    'routeBodyIntersectionCount',
    'routeCrossingCount',
    'twistCount',
    'usedLaneCount',
    'peakRoutesPerLane',
    'density',
    'occupancy',
];

async function main() {
    const repositoryRoot = resolveM41Example();
    const screenshotPaths = resolveM41ScreenshotPaths();
    assertInstalledLspHostPresent();

    const smokes = [
        await runElectronSmoke(repositoryRoot, screenshotPaths.desktop1920, 'desktop-1920x1080', 1920, 1080),
        await runElectronSmoke(repositoryRoot, screenshotPaths.narrow, 'narrow', 720, 900),
    ];
    const productProof = buildM41ProductProof(repositoryRoot, smokes);
    assertM41ProductProof(productProof);
    const proofPath = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm41', 'm41-product-proof.json');
    fs.mkdirSync(path.dirname(proofPath), { recursive: true });
    fs.writeFileSync(proofPath, `${JSON.stringify(productProof, null, 2)}\n`, 'utf8');

    console.log(`${ATHENA_M41_PRODUCT_PROOF_SENTINEL}${JSON.stringify(productProof)}`);
    console.log(`Athena M41 product proof passed. workspace=${smokes[0].openedWorkspace} proof=${proofPath} screenshots=${Object.values(screenshotPaths).join(',')}`);
}

async function runElectronSmoke(repositoryRoot, screenshotPath, viewportName, windowWidth, windowHeight) {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const child = spawn(electronBinary, [entryScript, repositoryRoot, '--active-view', 'schematic'], {
        cwd: path.resolve(__dirname, '..'),
        env: {
            ...process.env,
            ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
            ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE: '1',
            ATHENA_ELECTRON_SMOKE_OPEN_SOURCE_FOR_DIAGNOSTICS: '1',
            ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
            ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'schematic',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_ID: 'control-drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_LABEL: 'Control Drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_BACKING_VIEW_ID: 'schematic',
            ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: screenshotPath,
            ATHENA_ELECTRON_PIXEL_PROOF_MODE: 'body-routes',
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
    const outputLines = [];
    let stdoutBuffer = '';
    let stderrBuffer = '';

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
    };

    child.stdout.setEncoding('utf8');
    child.stderr.setEncoding('utf8');
    const consumeLines = (buffer, chunk) => {
        const lines = `${buffer}${chunk}`.split(/\r?\n/);
        for (let index = 0; index < lines.length - 1; index += 1) recordLine(lines[index]);
        return lines.at(-1) || '';
    };
    child.stdout.on('data', chunk => { stdoutBuffer = consumeLines(stdoutBuffer, chunk); });
    child.stderr.on('data', chunk => { stderrBuffer = consumeLines(stderrBuffer, chunk); });

    const timeoutHandle = setTimeout(() => child.kill(), STARTUP_TIMEOUT_MS);
    const exitCode = await new Promise(resolveExit => {
        child.once('close', code => resolveExit(code ?? -1));
        child.once('error', () => resolveExit(-1));
    });
    clearTimeout(timeoutHandle);
    if (stdoutBuffer) recordLine(stdoutBuffer);
    if (stderrBuffer) recordLine(stderrBuffer);

    if (!sawWindowCreated || !sawReady || exitCode !== 0) {
        fail('electron-runtime', `M41 smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}`, outputLines);
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        fail('java-runtime', `M41 smoke did not resolve Java runtime: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        fail('repository-contract', `Opened ${openedWorkspace || '<missing>'} instead of ${repositoryRoot}.`, outputLines);
    }
    if (!graphWorkbenchProof) {
        fail('graph-workbench-proof', 'M41 smoke did not report graph workbench proof.', outputLines);
    }
    return { viewportName, windowWidth, windowHeight, screenshotPath, openedWorkspace, graphWorkbenchProof, capturedScreenshotPath };
}

function buildM41ProductProof(repositoryRoot, smokes) {
    const viewports = smokes.map(buildViewportProof);
    return {
        schemaVersion: 'M41.product-proof',
        sourceUri: viewports[0]?.sourceUri || '',
        sourceSha256: resolveM41SourceSha256(),
        viewports,
        validationSummary: buildM41ValidationSummary(viewports),
    };
}

function buildViewportProof(smoke) {
    const product = smoke.graphWorkbenchProof || {};
    const widget = product.widgetDiagramProof || {};
    const spatialProof = widget.spatialProof || null;
    const projectionSheetId = widget.activeSheetId || '';
    const activeSheet = normalizeArray(spatialProof?.sheets)
        .find(sheet => sheet.sheetId === projectionSheetId) || null;
    return {
        viewportName: smoke.viewportName,
        sourceUri: product.sourceDiagnosticActivationProof?.resourceUri || '',
        requestedSurface: { width: smoke.windowWidth, height: smoke.windowHeight },
        activeViewId: product.activeViewId || '',
        projectionReality: {
            productSurfaceIds: normalizeArray(product.projectionViewProof?.visibleProductSurfaceIds),
            backingViewIds: normalizeArray(product.projectionViewProof?.backingProjectionViewIds),
            activeSheetId: projectionSheetId,
            componentIds: normalizeArray(widget.projectionComponentIds),
            connectionIds: normalizeArray(widget.projectionConnectionIds),
            regionIds: normalizeArray(widget.projectionRegionIds),
            constructIds: normalizeArray(widget.projectionConstructIds),
        },
        spatialReality: {
            proof: spatialProof,
            activeSheet: activeSheet,
        },
        presentationReality: {
            drawingLayerVisible: Number(product.drawingLayerProof?.visibleItemCount || 0) > 0,
            sheetId: widget.presentationSheetId || '',
            occurrences: normalizeArray(widget.presentationOccurrences),
            connectors: normalizeArray(widget.presentationConnectors),
            drawingAreaBounds: widget.drawingAreaBounds || null,
            sheetBounds: widget.sheetBounds || null,
            routeProof: product.routeProof || null,
        },
        pixelReality: product.pixelProof || null,
        screenshot: buildScreenshotProof(smoke),
    };
}

function assertM41ProductProof(proof) {
    requireValue(proof.schemaVersion === 'M41.product-proof', 'schema', 'Unexpected M41 proof schema.', proof);
    const expectedSourceUri = resolveM41SourceUri();
    const golden = loadM41GoldenBaseline();
    requireValue(sameFileUri(proof.sourceUri, expectedSourceUri), 'source', 'Active M41 source URI differs from requested example source URI.', proof);
    requireValue(golden.sourcePath === `examples/m41/rolling-shutter/${SOURCE_RELATIVE}` && proof.sourceSha256 === golden.sourceSha256 && proof.sourceSha256 === resolveM41SourceSha256(), 'source', 'Active M41 source bytes differ from the Golden fixture source.', proof);
    requireValue(Array.isArray(proof.viewports) && proof.viewports.length === 2, 'viewport', 'Expected desktop and narrow runtime proofs.', proof);
    const viewportNames = proof.viewports.map(viewport => viewport.viewportName);
    requireValue(new Set(viewportNames).size === 2 && viewportNames.includes('desktop-1920x1080') && viewportNames.includes('narrow'), 'viewport', 'Desktop and narrow runtime proof identities are incomplete.', proof);
    for (const viewport of proof.viewports) {
        const requiredSurface = viewport.viewportName === 'desktop-1920x1080'
            ? { width: 1920, height: 1080 }
            : { width: 720, height: 900 };
        requireValue(viewport.requestedSurface?.width === requiredSurface.width && viewport.requestedSurface?.height === requiredSurface.height, 'viewport', `${viewport.viewportName} does not use its required surface dimensions.`, viewport);
        requireValue(sameFileUri(viewport.sourceUri, expectedSourceUri) && sameFileUri(viewport.sourceUri, proof.sourceUri), 'source', `${viewport.viewportName} runtime source URI differs from active example source URI.`, viewport);
        requireValue(viewport.activeViewId === 'schematic', 'projection', `${viewport.viewportName} expected schematic view, got ${viewport.activeViewId || '<missing>'}.`, viewport);
        requireValue(viewport.projectionReality?.productSurfaceIds?.includes('control-drawing'), 'projection', `${viewport.viewportName} Control Drawing product surface missing.`, viewport);
        requireValue(viewport.projectionReality?.backingViewIds?.includes('schematic'), 'projection', `${viewport.viewportName} schematic Projection backing view missing.`, viewport);
        assertRuntimeAuthority(viewport);
        assertScreenshotProof(viewport);
        assertPixelProof(viewport);
    }
    requireValue(sameRuntimeAuthority(proof.viewports[0], proof.viewports[1]), 'runtime', 'Desktop and narrow runtime authority differs.', proof);
    requireValue(JSON.stringify(proof.validationSummary) === JSON.stringify(buildM41ValidationSummary(proof.viewports)), 'summary', 'Persisted M41 validation summary is missing or differs from validated runtime facts.', proof);
}

function assertRuntimeAuthority(viewport) {
    const spatial = viewport.spatialReality?.proof;
    const name = viewport.viewportName;
    requireValue(spatial && Array.isArray(spatial.sheets), 'spatial', `${name} Runtime Spatial proof missing.`, viewport);
    requireValue(spatial.sheets.length === 1, 'spatial', `${name} requires exactly one Spatial Sheet.`, viewport);
    const sheet = spatial.sheets.find(candidate => candidate?.sheetId === spatial.activeSheetId);
    requireValue(sheet && JSON.stringify(viewport.spatialReality?.activeSheet) === JSON.stringify(sheet), 'spatial', `${name} detached active Spatial Sheet differs from compiler-backed Spatial sheets.`, viewport);
    requireValue(spatial.viewId === viewport.activeViewId, 'spatial', `${name} Spatial proof view identity differs from active Projection view.`, viewport);
    requireValue(spatial.activeSheetId === viewport.projectionReality.activeSheetId, 'projection', `${name} Spatial active Sheet differs from Projection active Sheet.`, viewport);
    requireValue(sheet && sheet.sheetId === viewport.projectionReality.activeSheetId, 'projection', `${name} active Projection Sheet does not equal Spatial Sheet identity.`, viewport);
    requireValue(viewport.presentationReality?.sheetId === sheet.sheetId, 'presentation', `${name} Presentation Sheet identity differs from active Projection/Spatial Sheet.`, viewport);
    const golden = loadM41GoldenBaseline();
    requireValue(sheet.sheetId === golden.sheetId, 'spatial', `${name} expected Golden Sheet ${golden.sheetId}, got ${sheet.sheetId}.`, viewport);
    requireValue(sameRect(sheet.extent, golden.extent), 'spatial', `${name} Spatial Sheet extent differs from M41 Golden geometry.`, viewport);
    requireValue(sameRect(sheet.drawingArea, golden.drawingArea), 'spatial', `${name} Spatial Drawing Area differs from M41 Golden geometry.`, viewport);
    requireValue(sameRect(viewport.presentationReality?.drawingAreaBounds, sheet.drawingArea), 'presentation', `${name} Presentation Drawing Area bounds differ from Spatial.`, viewport);
    requireValue(sameRect(viewport.presentationReality?.sheetBounds, sheet.extent), 'presentation', `${name} Presentation Sheet bounds differ from Spatial.`, viewport);
    for (const [collection, expected] of Object.entries(golden.counts)) {
        const actual = collection === 'lanes'
            ? normalizeArray(sheet.lanes).filter(lane => normalizeArray(lane.routeIds).length > 0).length
            : normalizeArray(sheet[collection]).length;
        requireValue(actual === expected, 'spatial', `${name} Spatial ${collection} count differs from M41 Golden coverage.`, viewport);
    }
    const spatialOccurrences = normalizeArray(sheet.occurrences);
    const spatialRegions = normalizeArray(sheet.regions);
    const spatialConstructs = normalizeArray(sheet.constructs);
    const spatialGridReferences = normalizeArray(sheet.gridReferences);
    const spatialRoutes = normalizeArray(sheet.routes);
    const spatialAnchors = normalizeArray(sheet.anchors);
    const spatialLanes = normalizeArray(sheet.lanes);
    const presentationOccurrences = normalizeArray(viewport.presentationReality?.occurrences);
    const presentationConnectors = normalizeArray(viewport.presentationReality?.connectors);

    assertSpatialGroupingAndGrid(name, sheet, spatialOccurrences, spatialRegions, spatialConstructs, spatialGridReferences, viewport);

    const projectionComponentIds = new Set(viewport.projectionReality.componentIds);
    const spatialOccurrenceIds = new Set(spatialOccurrences.map(occurrence => occurrence.occurrenceId));
    requireValue(projectionComponentIds.size === viewport.projectionReality.componentIds.length, 'projection', `${name} Projection Component identities are duplicated.`, viewport);
    requireValue(projectionComponentIds.size === spatialOccurrenceIds.size && [...spatialOccurrenceIds].every(id => projectionComponentIds.has(id)), 'projection', `${name} Projection Component identities differ from Spatial Occurrences.`, viewport);
    requireExactIds(viewport.projectionReality.regionIds, spatialRegions.map(region => region.regionId), 'projection', `${name} Projection Region identities differ from Spatial.`, viewport);
    requireExactIds(viewport.projectionReality.constructIds, spatialConstructs.map(construct => construct.constructId), 'projection', `${name} Projection Construct identities differ from Spatial.`, viewport);
    const projectionConnectionIds = new Set(viewport.projectionReality.connectionIds);
    requireValue(projectionConnectionIds.size === viewport.projectionReality.connectionIds.length, 'projection', `${name} Projection Connection identities are duplicated.`, viewport);
    const spatialRouteIds = new Set(spatialRoutes.map(route => route.routeId));
    requireValue(spatialRouteIds.size === spatialRoutes.length, 'spatial', `${name} Spatial Route identities are duplicated.`, viewport);
    assertSpatialAnchors(name, spatialAnchors, spatialOccurrences, viewport);
    const spatialConnectionIds = new Set(spatialRoutes.map(route => route.projectionConnectionId));
    requireValue(spatialConnectionIds.size === spatialRoutes.length, 'projection', `${name} Spatial Projection Connection identities are duplicated.`, viewport);
    requireValue(spatialConnectionIds.size === projectionConnectionIds.size, 'projection', `${name} Projection Connection count differs from Spatial Route count.`, viewport);
    for (const connectionId of projectionConnectionIds) {
        requireValue(spatialConnectionIds.has(connectionId), 'projection', `${name} Projection Connection ${connectionId} has no Spatial Route.`, viewport);
    }

    const presentationOccurrenceIds = new Set(presentationOccurrences.map(occurrence => occurrence.occurrenceId));
    requireValue(presentationOccurrenceIds.size === presentationOccurrences.length, 'presentation', `${name} Presentation Occurrence identities are duplicated.`, viewport);
    const presentationSemanticIds = new Set(presentationOccurrences.map(occurrence => occurrence.semanticId));
    const spatialSemanticIds = new Set(spatialOccurrences.map(occurrence => occurrence.semanticId));
    requireValue(presentationSemanticIds.size === presentationOccurrences.length && presentationSemanticIds.size === spatialSemanticIds.size && [...spatialSemanticIds].every(id => presentationSemanticIds.has(id)), 'presentation', `${name} Presentation Occurrence identities differ from Spatial.`, viewport);
    const occurrenceBySemantic = new Map(spatialOccurrences.map(occurrence => [occurrence.semanticId, occurrence]));
    for (const occurrence of presentationOccurrences) {
        const spatialOccurrence = occurrenceBySemantic.get(occurrence.semanticId);
        requireValue(normalizeArray(occurrence.sourceProjectionIds).includes(spatialOccurrence.occurrenceId), 'presentation', `${name} Presentation Occurrence ${occurrence.semanticId} changed Occurrence source identity.`, viewport);
        requireValue(sameRect(occurrence.bounds, spatialOccurrence.bounds), 'presentation', `${name} Presentation Occurrence ${occurrence.semanticId} repaired Spatial bounds.`, viewport);
    }

    const routeById = new Map(spatialRoutes.map(route => [route.routeId, route]));
    const presentationRouteIds = new Set(presentationConnectors.map(connector => connector.routeId));
    requireValue(presentationRouteIds.size === presentationConnectors.length && presentationRouteIds.size === spatialRouteIds.size && [...spatialRouteIds].every(id => presentationRouteIds.has(id)), 'presentation', `${name} Presentation Route identities differ from Spatial.`, viewport);
    const anchorById = new Map(spatialAnchors.map(anchor => [anchor.anchorId, anchor]));
    const routeSheetOwners = new Map();
    for (const candidateSheet of spatial.sheets) {
        for (const route of normalizeArray(candidateSheet.routes)) {
            routeSheetOwners.set(route.routeId, (routeSheetOwners.get(route.routeId) || 0) + 1);
        }
    }
    for (const connector of presentationConnectors) {
        const route = routeById.get(connector.routeId);
        requireValue(route, 'presentation', `${name} Presentation connector ${connector.routeId} has no Spatial Route.`, viewport);
        requireValue(normalizeArray(route.points).length >= 2, 'spatial', `${name} Route ${route.routeId} requires at least two points.`, viewport);
        requireValue(samePoints(connector.routePoints, route.points), 'presentation', `${name} Presentation connector ${connector.routeId} changed ordered Route points.`, viewport);
        requireValue(connector.sourceEndpoint && connector.sourceEndpoint.anchorId === route.sourceAnchorId, 'presentation', `${name} Presentation connector ${connector.routeId} changed source Anchor identity.`, viewport);
        requireValue(connector.targetEndpoint && connector.targetEndpoint.anchorId === route.targetAnchorId, 'presentation', `${name} Presentation connector ${connector.routeId} changed target Anchor identity.`, viewport);
        for (const endpoint of [connector.sourceEndpoint, connector.targetEndpoint]) {
            const anchor = anchorById.get(endpoint?.anchorId);
            requireValue(anchor, 'spatial', `${name} Presentation endpoint ${endpoint?.anchorId || '<missing>'} has no Spatial Anchor.`, viewport);
            requireValue(endpoint?.point?.x === anchor.point.x && endpoint?.point?.y === anchor.point.y, 'presentation', `${name} Presentation endpoint ${endpoint?.anchorId || '<missing>'} changed Anchor point.`, viewport);
            requireValue(endpoint?.occurrenceId === anchor.occurrenceId && endpoint?.portSemanticId === anchor.portSemanticId, 'presentation', `${name} Presentation endpoint ${endpoint?.anchorId || '<missing>'} changed endpoint semantic identity.`, viewport);
        }
        const sourceAnchor = anchorById.get(route.sourceAnchorId);
        const targetAnchor = anchorById.get(route.targetAnchorId);
        requireValue(sourceAnchor && samePoint(route.points[0], sourceAnchor.point), 'spatial', `${name} Route ${route.routeId} does not start at its source Anchor point.`, viewport);
        requireValue(targetAnchor && samePoint(route.points.at(-1), targetAnchor.point), 'spatial', `${name} Route ${route.routeId} does not end at its target Anchor point.`, viewport);
        requireValue(routeSheetOwners.get(route.routeId) === 1, 'spatial', `${name} Route ${route.routeId} does not have exactly one owning Sheet.`, viewport);
        const owningLanes = spatialLanes.filter(lane => normalizeArray(lane.routeIds).includes(route.routeId));
        requireValue(route.laneId && owningLanes.length === 1 && owningLanes[0].laneId === route.laneId, 'spatial', `${name} Route ${route.routeId} does not have exactly one owning Lane.`, viewport);
        requireValue(connector.semanticId === route.connectionId, 'presentation', `${name} Presentation connector ${connector.routeId} changed semantic identity.`, viewport);
        requireValue(normalizeArray(connector.sourceProjectionIds).includes(route.projectionConnectionId), 'presentation', `${name} Presentation connector ${connector.routeId} changed Projection source identity.`, viewport);
        requireValue(connector.laneId === route.laneId && sameIdSet(connector.laneRouteIds, owningLanes[0].routeIds), 'presentation', `${name} Presentation connector ${connector.routeId} changed Lane identity.`, viewport);
    }
    assertSpatialLanes(name, spatialRoutes, spatialLanes, viewport);

    const quality = sheet.quality;
    requireValue(quality && typeof quality === 'object', 'quality', `${name} Spatial quality snapshot missing.`, viewport);
    for (const key of REQUIRED_QUALITY_KEYS) {
        const expected = golden.quality[key];
        requireValue(typeof expected === 'number' && Number.isFinite(expected), 'quality', `Golden quality baseline missing ${key}.`, golden.quality);
        requireValue(typeof quality[key] === 'number' && Number.isFinite(quality[key]), 'quality', `${name} Invalid quality ${key}.`, viewport);
        requireValue(quality[key] === expected, 'quality', `${name} M41 quality ${key} differs from Golden value.`, viewport);
    }
    const occupiedSpan = spatialOccupiedSpan(sheet);
    requireValue(occupiedSpan.widthRatio >= 0.55 && occupiedSpan.heightRatio >= 0.45, 'spatial', `${name} compiled Spatial occupied span is below the M41 0.55 width / 0.45 height gate.`, occupiedSpan);
}

function assertSpatialGroupingAndGrid(name, sheet, occurrences, regions, constructs, gridReferences, viewport) {
    requireUniqueNonBlankIds(occurrences, 'occurrenceId', 'spatial', `${name} Spatial Occurrence identities are invalid.`, viewport);
    requireUniqueNonBlankIds(occurrences, 'semanticId', 'spatial', `${name} Spatial Occurrence semantic identities are invalid.`, viewport);
    requireUniqueNonBlankIds(regions, 'regionId', 'spatial', `${name} Spatial Region identities are invalid or duplicated.`, viewport);
    requireUniqueNonBlankIds(constructs, 'constructId', 'spatial', `${name} Spatial Construct identities are invalid or duplicated.`, viewport);
    requireUniqueNonBlankIds(gridReferences, 'gridReferenceId', 'spatial', `${name} Spatial Grid Reference identities are invalid or duplicated.`, viewport);
    const occurrenceIds = new Set(occurrences.map(occurrence => occurrence.occurrenceId));
    const regionIds = new Set(regions.map(region => region.regionId));
    for (const occurrence of occurrences) {
        requireValue(isNonBlankString(occurrence.regionId) && regionIds.has(occurrence.regionId) && validRuntimeRect(occurrence.bounds), 'spatial', `${name} Spatial Occurrence ${occurrence.occurrenceId} has invalid Region or bounds.`, viewport);
    }
    for (const region of regions) {
        const members = normalizeArray(region.memberOccurrenceIds);
        const expectedMembers = occurrences.filter(occurrence => occurrence.regionId === region.regionId).map(occurrence => occurrence.occurrenceId);
        requireValue(validRuntimeRect(region.bounds) && uniqueNonBlankStrings(members) && members.every(id => occurrenceIds.has(id)) && sameIdSet(members, expectedMembers), 'spatial', `${name} Spatial Region membership or bounds are invalid.`, viewport);
    }
    for (const construct of constructs) {
        const members = normalizeArray(construct.memberOccurrenceIds);
        requireValue(isNonBlankString(construct.kind) && isNonBlankString(construct.name) && validRuntimeRect(construct.bounds) && members.length > 0 && uniqueNonBlankStrings(members) && members.every(id => occurrenceIds.has(id)), 'spatial', `${name} Spatial Construct facts are invalid.`, viewport);
    }
    const expectedSubjects = [...occurrenceIds, ...constructs.map(construct => construct.constructId)];
    const actualSubjects = gridReferences.map(reference => reference.subjectId);
    requireValue(uniqueNonBlankStrings(actualSubjects) && sameIdSet(actualSubjects, expectedSubjects), 'spatial', `${name} Grid Reference subject coverage is incomplete or duplicated.`, viewport);
    for (const reference of gridReferences) {
        requireValue(isNonBlankString(reference.cellReference) && /^[A-Za-z]+$/.test(reference.rowLabel) && Number.isSafeInteger(reference.columnNumber) && reference.columnNumber > 0 && reference.cellReference === `${reference.rowLabel}${reference.columnNumber}`, 'spatial', `${name} Grid Reference cell identity is invalid.`, viewport);
    }
}

function assertSpatialLanes(name, routes, lanes, viewport) {
    requireUniqueNonBlankIds(lanes, 'laneId', 'spatial', `${name} Spatial Lane identities are invalid or duplicated.`, viewport);
    const routeIds = routes.map(route => route.routeId);
    const laneRouteIds = [];
    for (const lane of lanes) {
        const members = normalizeArray(lane.routeIds);
        requireValue((lane.orientation === 'horizontal' || lane.orientation === 'vertical') && Number.isSafeInteger(lane.coordinate) && uniqueNonBlankStrings(members) && members.every(routeId => routeIds.includes(routeId)), 'spatial', `${name} Spatial Lane facts are invalid.`, viewport);
        laneRouteIds.push(...members);
    }
    requireValue(sameIdSet(laneRouteIds, routeIds), 'spatial', `${name} Spatial Lanes do not form an exact Route partition.`, viewport);
}

function assertSpatialAnchors(name, anchors, occurrences, viewport) {
    requireUniqueNonBlankIds(anchors, 'anchorId', 'spatial', `${name} Spatial Anchor identities are duplicated or invalid.`, viewport);
    const occurrenceIds = new Set(occurrences.map(occurrence => occurrence.occurrenceId));
    for (const anchor of anchors) {
        requireValue(occurrenceIds.has(anchor.occurrenceId) && isNonBlankString(anchor.portSemanticId) && ['top', 'right', 'bottom', 'left'].includes(anchor.side) && validRuntimePoint(anchor.point), 'spatial', `${name} Spatial Anchor facts are invalid.`, viewport);
    }
}

function spatialOccupiedSpan(sheet) {
    const xs = [];
    const ys = [];
    for (const occurrence of normalizeArray(sheet.occurrences)) {
        if (!validRuntimeRect(occurrence.bounds)) continue;
        xs.push(occurrence.bounds.x, occurrence.bounds.x + occurrence.bounds.width);
        ys.push(occurrence.bounds.y, occurrence.bounds.y + occurrence.bounds.height);
    }
    for (const route of normalizeArray(sheet.routes)) {
        for (const point of normalizeArray(route.points)) {
            if (!validRuntimePoint(point)) continue;
            xs.push(point.x);
            ys.push(point.y);
        }
    }
    const width = xs.length > 0 ? Math.max(...xs) - Math.min(...xs) : 0;
    const height = ys.length > 0 ? Math.max(...ys) - Math.min(...ys) : 0;
    return {
        width,
        height,
        widthRatio: width / sheet.drawingArea.width,
        heightRatio: height / sheet.drawingArea.height,
    };
}

function sameRect(left, right) {
    return validRuntimeRect(left) && validRuntimeRect(right)
        && left.x === right.x && left.y === right.y
        && left.width === right.width && left.height === right.height;
}

function samePoints(left, right) {
    return Array.isArray(left) && Array.isArray(right) && left.length === right.length
        && left.every((point, index) => validRuntimePoint(point) && validRuntimePoint(right[index])
            && point.x === right[index].x && point.y === right[index].y);
}

function samePoint(left, right) {
    return validRuntimePoint(left) && validRuntimePoint(right) && left.x === right.x && left.y === right.y;
}

function requireUniqueNonBlankIds(items, key, authority, message, proof) {
    const ids = items.map(item => item?.[key]);
    requireValue(uniqueNonBlankStrings(ids), authority, message, proof);
}

function uniqueNonBlankStrings(values) {
    return values.every(isNonBlankString) && new Set(values).size === values.length;
}

function isNonBlankString(value) {
    return typeof value === 'string' && value.trim().length > 0;
}

function sameIdSet(left, right) {
    const leftIds = normalizeArray(left);
    const rightIds = normalizeArray(right);
    return uniqueNonBlankStrings(leftIds) && uniqueNonBlankStrings(rightIds)
        && leftIds.length === rightIds.length
        && leftIds.every(id => rightIds.includes(id));
}

function requireExactIds(left, right, authority, message, proof) {
    requireValue(sameIdSet(left, right), authority, message, proof);
}

function sameRuntimeAuthority(left, right) {
    const authority = viewport => ({
        sourceUri: canonicalFileUri(viewport.sourceUri),
        activeViewId: viewport.activeViewId,
        projectionReality: viewport.projectionReality,
        spatialReality: viewport.spatialReality?.proof,
        presentationReality: {
            sheetId: viewport.presentationReality?.sheetId,
            drawingAreaBounds: viewport.presentationReality?.drawingAreaBounds,
            sheetBounds: viewport.presentationReality?.sheetBounds,
            occurrences: viewport.presentationReality?.occurrences,
            connectors: viewport.presentationReality?.connectors,
        },
    });
    return JSON.stringify(authority(left)) === JSON.stringify(authority(right));
}

function validRuntimeRect(rect) {
    return rect && Number.isSafeInteger(rect.x) && Number.isSafeInteger(rect.y)
        && Number.isSafeInteger(rect.width) && rect.width > 0
        && Number.isSafeInteger(rect.height) && rect.height > 0;
}

function validRuntimePoint(point) {
    return point && Number.isSafeInteger(point.x) && Number.isSafeInteger(point.y);
}

function loadM41GoldenQuality() {
    const baselinePath = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm41', 'm41-spatial-quality-baseline.properties');
    const values = {};
    const ratios = {};
    if (!fs.existsSync(baselinePath)) return values;
    for (const line of fs.readFileSync(baselinePath, 'utf8').split(/\r?\n/)) {
        const match = line.match(/^sheet\.0\.metric\.([^.]+)\.(value|numerator|denominator)=(.+)$/);
        if (!match) continue;
        const key = match[1].replace(/-([a-z])/g, (_m, letter) => letter.toUpperCase());
        if (match[2] === 'value') {
            values[key] = Number(match[3]);
        } else {
            ratios[key] = { ...ratios[key], [match[2]]: Number(match[3]) };
        }
    }
    for (const [key, ratio] of Object.entries(ratios)) {
        if (Number.isFinite(ratio.numerator) && Number.isFinite(ratio.denominator) && ratio.denominator !== 0) {
            values[key] = ratio.numerator / ratio.denominator;
        }
    }
    return values;
}

function loadM41GoldenBaseline() {
    const baselinePath = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm41', 'm41-spatial-quality-baseline.properties');
    const properties = {};
    if (fs.existsSync(baselinePath)) {
        for (const line of fs.readFileSync(baselinePath, 'utf8').split(/\r?\n/)) {
            const match = line.match(/^([^=]+)=(.*)$/);
            if (match) {
                const rawValue = match[2].replace(/\\([:=\\])/g, '$1');
                properties[match[1]] = Number.isNaN(Number(rawValue)) ? rawValue : Number(rawValue);
            }
        }
    }
    const rect = prefix => ({
        x: properties[`sheet.0.${prefix}.x`],
        y: properties[`sheet.0.${prefix}.y`],
        width: properties[`sheet.0.${prefix}.width`],
        height: properties[`sheet.0.${prefix}.height`],
    });
    return {
        sourcePath: properties['fixture.source.path'],
        sourceSha256: properties['fixture.source.sha256'],
        sheetId: properties['sheet.0.id'],
        extent: rect('extent'),
        drawingArea: rect('drawing-area'),
        counts: {
            occurrences: properties['sheet.0.count.occurrences'],
            regions: properties['sheet.0.count.regions'],
            constructs: properties['sheet.0.count.constructs'],
            anchors: properties['sheet.0.count.anchors'],
            routes: properties['sheet.0.count.routes'],
            lanes: properties['sheet.0.count.used-lanes'],
            gridReferences: properties['sheet.0.count.grid-references'],
        },
        quality: loadM41GoldenQuality(),
    };
}

function buildScreenshotProof(smoke) {
    if (smoke.capturedScreenshotPath !== smoke.screenshotPath) {
        fail('screenshot', `Expected screenshot ${smoke.screenshotPath}, got ${smoke.capturedScreenshotPath || '<missing>'}.`);
    }
    const png = assertPngScreenshot(smoke.screenshotPath);
    const geometry = smoke.graphWorkbenchProof?.pixelProof?.geometry || {};
    return {
        viewportName: smoke.viewportName,
        requestedWidth: smoke.windowWidth,
        requestedHeight: smoke.windowHeight,
        capturedViewportWidth: geometry.viewportWidth,
        capturedViewportHeight: geometry.viewportHeight,
        screenshotPath: smoke.screenshotPath,
        pngWidth: png.width,
        pngHeight: png.height,
        pngBytes: png.bytes,
        activeViewId: smoke.graphWorkbenchProof?.activeViewId || '',
        sourceUri: smoke.graphWorkbenchProof?.sourceDiagnosticActivationProof?.resourceUri || '',
    };
}

function assertScreenshotProof(viewport) {
    const screenshot = viewport.screenshot;
    const requested = viewport.requestedSurface;
    requireValue(screenshot?.viewportName === viewport.viewportName, 'screenshot', `${viewport.viewportName} screenshot identity missing.`, screenshot);
    requireValue(screenshot.activeViewId === viewport.activeViewId, 'screenshot', `${viewport.viewportName} screenshot active view differs from runtime proof.`, screenshot);
    requireValue(sameFileUri(screenshot.sourceUri, viewport.sourceUri), 'source', `${viewport.viewportName} screenshot source URI differs from runtime proof.`, screenshot);
    requireValue(screenshot.requestedWidth === requested.width && screenshot.requestedHeight === requested.height, 'screenshot', `${viewport.viewportName} requested screenshot surface differs from proof contract.`, screenshot);
    requireValue(screenshot.capturedViewportWidth === requested.width && screenshot.capturedViewportHeight === requested.height, 'screenshot', `${viewport.viewportName} actual Electron viewport dimensions differ from requested surface.`, screenshot);
    requireValue(screenshot.pngWidth === requested.width && screenshot.pngHeight === requested.height, 'screenshot', `${viewport.viewportName} screenshot dimensions do not match requested Electron surface.`, screenshot);
    const expectedPath = viewport.viewportName === 'desktop-1920x1080'
        ? resolveM41ScreenshotPaths().desktop1920
        : resolveM41ScreenshotPaths().narrow;
    requireValue(path.resolve(screenshot.screenshotPath || '') === path.resolve(expectedPath), 'screenshot', `${viewport.viewportName} screenshot path differs from required M41 artifact path.`, screenshot);
}

function assertPixelProof(viewport) {
    const pixelProof = viewport.pixelReality;
    const viewportName = viewport.viewportName;
    const requested = viewport.requestedSurface;
    requireValue(pixelProof?.available === true, 'pixels', `${viewportName} screenshot has no native pixel proof.`, pixelProof);
    requireValue(pixelProof.classification === 'isolated-rendered-component-bodies-and-routes', 'pixels', `${viewportName} pixel proof lacks isolated rendered component-body/Route classification.`, pixelProof);
    requireValue(pixelProof.maskApplied === true, 'pixels', `${viewportName} component-body/Route render isolation mask was not applied.`, pixelProof);
    const geometry = pixelProof.geometry;
    requireValue(geometry?.viewportWidth === requested.width && geometry?.viewportHeight === requested.height, 'pixels', `${viewportName} pixel geometry does not match requested Electron viewport.`, pixelProof);
    requireValue(geometry?.bitmapSize?.width === requested.width && geometry?.bitmapSize?.height === requested.height, 'pixels', `${viewportName} native bitmap dimensions differ from requested Electron surface.`, pixelProof);
    const runtimeDrawingArea = viewport.spatialReality?.activeSheet?.drawingArea;
    const attributes = geometry?.drawingAreaAttributes;
    requireValue(runtimeDrawingArea && attributes && Number(attributes.x) === runtimeDrawingArea.x && Number(attributes.y) === runtimeDrawingArea.y && Number(attributes.width) === runtimeDrawingArea.width && Number(attributes.height) === runtimeDrawingArea.height, 'pixels', `${viewportName} DOM Drawing Area differs from runtime Spatial authority.`, pixelProof);
    requireValue(validPixelRect(pixelProof.drawingAreaBounds, geometry.bitmapSize), 'pixels', `${viewportName} Drawing Area bounds missing or outside native bitmap.`, pixelProof);
    requireValue(validPixelRect(pixelProof.sampleBounds, geometry.bitmapSize) && rectContains(pixelProof.drawingAreaBounds, pixelProof.sampleBounds), 'pixels', `${viewportName} sample bounds missing or outside governed Drawing Area.`, pixelProof);
    const horizontalBuckets = normalizeArray(pixelProof.horizontalBuckets);
    const verticalBuckets = normalizeArray(pixelProof.verticalBuckets);
    requireValue(horizontalBuckets.length === 3 && horizontalBuckets.every(count => Number.isSafeInteger(count) && count > 0), 'pixels', `${viewportName} Drawing Area has empty horizontal pixel bucket or invalid count.`, pixelProof);
    requireValue(verticalBuckets.length === 3 && verticalBuckets.every(count => Number.isSafeInteger(count) && count > 0), 'pixels', `${viewportName} Drawing Area has empty vertical pixel bucket or invalid count.`, pixelProof);
    requireValue(Number.isSafeInteger(pixelProof.occupiedPixelCount) && pixelProof.occupiedPixelCount > 0, 'pixels', `${viewportName} occupied pixel count is invalid.`, pixelProof);
    const horizontalTotal = horizontalBuckets.reduce((sum, count) => sum + count, 0);
    const verticalTotal = verticalBuckets.reduce((sum, count) => sum + count, 0);
    requireValue(horizontalTotal === pixelProof.occupiedPixelCount && verticalTotal === pixelProof.occupiedPixelCount, 'pixels', `${viewportName} pixel counts are inconsistent.`, pixelProof);
    const sampleArea = (pixelProof.sampleBounds.right - pixelProof.sampleBounds.left) * (pixelProof.sampleBounds.bottom - pixelProof.sampleBounds.top);
    requireValue(pixelProof.occupiedPixelCount <= sampleArea, 'pixels', `${viewportName} occupied pixel count exceeds sample area.`, pixelProof);
    const expectedComponents = normalizeArray(viewport.spatialReality?.activeSheet?.occurrences).map(occurrence => occurrence.semanticId);
    const expectedRoutes = normalizeArray(viewport.spatialReality?.activeSheet?.routes).map(route => route.routeId);
    requireExactIds(pixelProof.renderedComponentSemanticIds, expectedComponents, 'pixels', `${viewportName} rendered Component identities differ from Spatial.`, pixelProof);
    requireExactIds(pixelProof.renderedRouteIds, expectedRoutes, 'pixels', `${viewportName} rendered Route identities differ from Spatial.`, pixelProof);
    const renderedComponents = normalizeArray(pixelProof.renderedComponents);
    const renderedRoutes = normalizeArray(pixelProof.renderedRoutes);
    requireUniqueNonBlankIds(renderedComponents, 'semanticId', 'pixels', `${viewportName} duplicate rendered Component identity or missing rendered geometry.`, pixelProof);
    requireUniqueNonBlankIds(renderedRoutes, 'routeId', 'pixels', `${viewportName} duplicate rendered Route identity or missing rendered geometry.`, pixelProof);
    requireExactIds(renderedComponents.map(component => component.semanticId), expectedComponents, 'pixels', `${viewportName} rendered geometry coverage differs from Spatial Components.`, pixelProof);
    requireExactIds(renderedRoutes.map(route => route.routeId), expectedRoutes, 'pixels', `${viewportName} rendered geometry coverage differs from Spatial Routes.`, pixelProof);
    const presentationOccurrenceBySemantic = new Map(normalizeArray(viewport.presentationReality?.occurrences).map(occurrence => [occurrence.semanticId, occurrence]));
    for (const component of renderedComponents) {
        requireValue(sameRect(component.bounds, presentationOccurrenceBySemantic.get(component.semanticId)?.bounds), 'pixels', `${viewportName} rendered geometry differs from Presentation Component ${component.semanticId}.`, pixelProof);
    }
    const presentationRouteById = new Map(normalizeArray(viewport.presentationReality?.connectors).map(connector => [connector.routeId, connector]));
    for (const route of renderedRoutes) {
        requireValue(samePoints(route.points, presentationRouteById.get(route.routeId)?.routePoints), 'pixels', `${viewportName} rendered geometry differs from Presentation Route ${route.routeId}.`, pixelProof);
    }
    requireValue(geometry.drawingAreaClipped === false, 'pixels', `${viewportName} governed Drawing Area is clipped outside the Electron viewport.`, pixelProof);
    requireValue(isNonBlankString(geometry.renderStateBefore) && geometry.renderStateBefore === geometry.renderStateAfter, 'pixels', `${viewportName} rendered geometry changed between retained screenshot and isolated pixel capture.`, pixelProof);
    requireValue(finiteRatio(pixelProof.occupiedWidthRatio), 'pixels', `${viewportName} invalid occupied width ratio.`, pixelProof);
    requireValue(finiteRatio(pixelProof.occupiedHeightRatio), 'pixels', `${viewportName} invalid occupied height ratio.`, pixelProof);
    requireValue(pixelProof.occupiedWidthRatio >= 0.55, 'pixels', `${viewportName} occupied Drawing Area width below M41 Golden gate.`, pixelProof);
    requireValue(pixelProof.occupiedHeightRatio >= 0.45, 'pixels', `${viewportName} occupied Drawing Area height below M41 Golden gate.`, pixelProof);
}

function buildM41ValidationSummary(viewports) {
    return {
        runtimeCounts: viewports.map(viewport => {
            const sheet = viewport.spatialReality?.proof?.sheets?.find(candidate => candidate.sheetId === viewport.projectionReality?.activeSheetId);
            return {
                viewportName: viewport.viewportName,
                occurrences: normalizeArray(sheet?.occurrences).length,
                regions: normalizeArray(sheet?.regions).length,
                constructs: normalizeArray(sheet?.constructs).length,
                anchors: normalizeArray(sheet?.anchors).length,
                routes: normalizeArray(sheet?.routes).length,
                usedLanes: normalizeArray(sheet?.lanes).filter(lane => normalizeArray(lane.routeIds).length > 0).length,
                gridReferences: normalizeArray(sheet?.gridReferences).length,
            };
        }),
        coordinatePreservation: viewports.map(viewport => {
            const sheet = viewport.spatialReality?.proof?.sheets?.find(candidate => candidate.sheetId === viewport.projectionReality?.activeSheetId);
            const occurrenceBySemantic = new Map(normalizeArray(sheet?.occurrences).map(occurrence => [occurrence.semanticId, occurrence]));
            const routeById = new Map(normalizeArray(sheet?.routes).map(route => [route.routeId, route]));
            const occurrenceBoundsExact = normalizeArray(viewport.presentationReality?.occurrences).length === occurrenceBySemantic.size
                && normalizeArray(viewport.presentationReality?.occurrences).every(occurrence => sameRect(occurrence.bounds, occurrenceBySemantic.get(occurrence.semanticId)?.bounds));
            const routePointsExact = normalizeArray(viewport.presentationReality?.connectors).length === routeById.size
                && normalizeArray(viewport.presentationReality?.connectors).every(connector => samePoints(connector.routePoints, routeById.get(connector.routeId)?.points));
            const renderedGeometryExact = normalizeArray(viewport.pixelReality?.renderedComponents).length === occurrenceBySemantic.size
                && normalizeArray(viewport.pixelReality?.renderedComponents).every(component => sameRect(component.bounds, occurrenceBySemantic.get(component.semanticId)?.bounds))
                && normalizeArray(viewport.pixelReality?.renderedRoutes).length === routeById.size
                && normalizeArray(viewport.pixelReality?.renderedRoutes).every(route => samePoints(route.points, routeById.get(route.routeId)?.points));
            return { viewportName: viewport.viewportName, occurrenceBoundsExact, routePointsExact, renderedGeometryExact };
        }),
        spatialSpan: viewports.map(viewport => ({
            viewportName: viewport.viewportName,
            ...spatialOccupiedSpan(viewport.spatialReality?.proof?.sheets?.find(candidate => candidate.sheetId === viewport.projectionReality?.activeSheetId) || { drawingArea: { width: 1, height: 1 } }),
        })),
        pixelBuckets: viewports.map(viewport => ({
            viewportName: viewport.viewportName,
            horizontal: normalizeArray(viewport.pixelReality?.horizontalBuckets),
            vertical: normalizeArray(viewport.pixelReality?.verticalBuckets),
            passed: normalizeArray(viewport.pixelReality?.horizontalBuckets).every(value => value > 0)
                && normalizeArray(viewport.pixelReality?.verticalBuckets).every(value => value > 0),
        })),
    };
}

function validPixelRect(rect, bitmapSize) {
    return rect && [rect.left, rect.top, rect.right, rect.bottom].every(Number.isSafeInteger)
        && rect.left >= 0 && rect.top >= 0 && rect.right > rect.left && rect.bottom > rect.top
        && rect.right <= bitmapSize.width && rect.bottom <= bitmapSize.height;
}

function rectContains(outer, inner) {
    return inner.left >= outer.left && inner.top >= outer.top
        && inner.right <= outer.right && inner.bottom <= outer.bottom;
}

function finiteRatio(value) {
    return typeof value === 'number' && Number.isFinite(value) && value >= 0 && value <= 1;
}

function resolveM41Example() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm41', 'rolling-shutter');
    const requiredFiles = ['athena.yaml', SOURCE_RELATIVE];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        fail('repository-contract', `M41 product proof example missing: ${missing.join(', ')}`);
    }
    return repositoryRoot;
}

function resolveM41SourceUri() {
    return pathToFileUri(path.join(resolveM41Example(), SOURCE_RELATIVE));
}

function resolveM41SourceSha256() {
    const bytes = fs.readFileSync(path.join(resolveM41Example(), SOURCE_RELATIVE));
    return `sha256:${crypto.createHash('sha256').update(bytes).digest('hex')}`;
}

function resolveM41ScreenshotPaths() {
    const screenshotRoot = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm41', 'screenshots');
    fs.mkdirSync(screenshotRoot, { recursive: true });
    return {
        desktop1920: path.join(screenshotRoot, 'm41-rolling-shutter-desktop-1920x1080.png'),
        narrow: path.join(screenshotRoot, 'm41-rolling-shutter-narrow.png'),
    };
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) fail('screenshot', `Screenshot missing: ${screenshotPath}`);
    const bytes = fs.readFileSync(screenshotPath);
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') {
        fail('screenshot', `Invalid PNG screenshot: ${screenshotPath}`);
    }
    return { bytes: bytes.length, width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}

function assertInstalledLspHostPresent() {
    const launcher = path.resolve(__dirname, '..', '..', 'lsp', 'build', 'install', 'athena-lsp-host', 'bin', process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host');
    if (!fs.existsSync(launcher)) {
        fail('lsp-installation', `M41 smoke requires installed LSP host. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`);
    }
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

function canonicalFileUri(value) {
    if (typeof value !== 'string') return '';
    let normalized;
    try {
        normalized = decodeURIComponent(value).replace(/\\/g, '/');
    } catch (_error) {
        return '';
    }
    return process.platform === 'win32'
        ? normalized.toLowerCase()
        : normalized;
}

function sameFileUri(left, right) {
    return canonicalFileUri(left) === canonicalFileUri(right);
}

function normalizeArray(value) {
    return Array.isArray(value) ? value : [];
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
    buildM41ProductProof,
    assertM41ProductProof,
    assertRuntimeAuthority,
    assertPixelProof,
    resolveM41SourceUri,
    resolveM41SourceSha256,
    resolveM41ScreenshotPaths,
    buildM41ValidationSummary,
    canonicalFileUri,
};

