import assert from 'node:assert/strict';
import test from 'node:test';

const verifier = await import('./verify-athena-m41-product-proof.js');
const electronCapture = await import('./athena-electron-open-workspace-main.js');

function baseProof() {
    return {
        schemaVersion: 'M41.product-proof',
        sourceUri: verifier.resolveM41SourceUri(),
        sourceSha256: verifier.resolveM41SourceSha256(),
        viewports: []
    };
}

function validViewport(viewportName, width, height) {
    const occurrences = Array.from({ length: 8 }, (_, index) => ({
        occurrenceId: `occurrence-${index}`,
        semanticId: `component:${index}`,
        regionId: `region-${index % 3}`,
        bounds: { x: 80 + index * 100, y: 100 + (index % 3) * 160, width: 80, height: 40 }
    }));
    const anchors = Array.from({ length: 16 }, (_, index) => ({
        anchorId: `anchor-${index}`,
        occurrenceId: occurrences[index % occurrences.length].occurrenceId,
        portSemanticId: `port:${index}`,
        side: index % 2 === 0 ? 'left' : 'right',
        point: { x: 90 + index * 50, y: 120 + (index % 3) * 160 }
    }));
    const routes = Array.from({ length: 9 }, (_, index) => ({
        routeId: `route-${index}`,
        projectionConnectionId: `connection-${index}`,
        connectionId: `connection:${index}`,
        sourceAnchorId: anchors[index].anchorId,
        targetAnchorId: anchors[index + 1].anchorId,
        laneId: `lane-${index % 7}`,
        points: [{ ...anchors[index].point }, { x: anchors[index + 1].point.x, y: anchors[index].point.y }, { ...anchors[index + 1].point }]
    }));
    const lanes = Array.from({ length: 7 }, (_, index) => ({
        laneId: `lane-${index}`,
        orientation: 'horizontal',
        coordinate: 140 + index * 60,
        routeIds: routes.filter(route => route.laneId === `lane-${index}`).map(route => route.routeId)
    }));
    const regions = Array.from({ length: 3 }, (_, index) => ({
        regionId: `region-${index}`,
        bounds: { x: 40 + index * 360, y: 60, width: 320, height: 640 },
        memberOccurrenceIds: occurrences.filter(occurrence => occurrence.regionId === `region-${index}`).map(occurrence => occurrence.occurrenceId)
    }));
    const constructs = Array.from({ length: 7 }, (_, index) => ({
        constructId: `construct-${index}`,
        kind: 'device-group',
        name: `Construct ${index}`,
        bounds: { x: 80 + index * 120, y: 100, width: 100, height: 140 },
        memberOccurrenceIds: [occurrences[index % occurrences.length].occurrenceId]
    }));
    const gridSubjects = [
        ...occurrences.map(occurrence => occurrence.occurrenceId),
        ...constructs.map(construct => construct.constructId)
    ];
    const gridReferences = gridSubjects.map((subjectId, index) => ({
        gridReferenceId: `grid-${index}`,
        subjectId,
        cellReference: `${String.fromCharCode(65 + Math.floor(index / 10))}${(index % 10) + 1}`,
        rowLabel: String.fromCharCode(65 + Math.floor(index / 10)),
        columnNumber: (index % 10) + 1
    }));
    const sheet = {
        sheetId: 'schematic/sheet/S1',
        extent: { x: 0, y: 0, width: 1200, height: 800 },
        drawingArea: { x: 40, y: 60, width: 1120, height: 640 },
        occurrences,
        regions,
        constructs,
        anchors,
        routes,
        lanes,
        gridReferences,
        quality: {
            occurrenceOverlapCount: 0,
            constructContainmentFailureCount: 0,
            routeBodyIntersectionCount: 0,
            routeCrossingCount: 3,
            twistCount: 0,
            usedLaneCount: 7,
            peakRoutesPerLane: 2,
            density: 8 / 716800,
            occupancy: 25600 / 716800
        }
    };
    const renderedComponents = occurrences.map(occurrence => ({
        semanticId: occurrence.semanticId,
        bounds: { ...occurrence.bounds }
    }));
    const renderedRoutes = routes.map(route => ({
        routeId: route.routeId,
        points: route.points.map(point => ({ ...point }))
    }));
    const renderState = JSON.stringify({ renderedComponents, renderedRoutes });
    return {
        viewportName,
        sourceUri: baseProof().sourceUri,
        requestedSurface: { width, height },
        activeViewId: 'schematic',
        projectionReality: {
            productSurfaceIds: ['control-drawing'],
            backingViewIds: ['schematic'],
            activeSheetId: sheet.sheetId,
            componentIds: occurrences.map(occurrence => occurrence.occurrenceId),
            connectionIds: routes.map(route => route.projectionConnectionId),
            regionIds: regions.map(region => region.regionId),
            constructIds: constructs.map(construct => construct.constructId)
        },
        spatialReality: {
            proof: { viewId: 'schematic', activeSheetId: sheet.sheetId, sheets: [sheet] },
            activeSheet: sheet
        },
        presentationReality: {
            sheetId: sheet.sheetId,
            drawingAreaBounds: { ...sheet.drawingArea },
            sheetBounds: { ...sheet.extent },
            occurrences: occurrences.map(occurrence => ({
                occurrenceId: `paint:${occurrence.occurrenceId}`,
                semanticId: occurrence.semanticId,
                bounds: { ...occurrence.bounds },
                sourceProjectionIds: [occurrence.occurrenceId]
            })),
            connectors: routes.map(route => ({
                routeId: route.routeId,
                semanticId: route.connectionId,
                sourceProjectionIds: [route.projectionConnectionId],
                laneId: route.laneId,
                laneRouteIds: [...lanes.find(lane => lane.laneId === route.laneId).routeIds],
                routePoints: route.points.map(point => ({ ...point })),
                sourceEndpoint: { anchorId: route.sourceAnchorId, occurrenceId: anchors.find(anchor => anchor.anchorId === route.sourceAnchorId).occurrenceId, portSemanticId: anchors.find(anchor => anchor.anchorId === route.sourceAnchorId).portSemanticId, point: { ...anchors.find(anchor => anchor.anchorId === route.sourceAnchorId).point } },
                targetEndpoint: { anchorId: route.targetAnchorId, occurrenceId: anchors.find(anchor => anchor.anchorId === route.targetAnchorId).occurrenceId, portSemanticId: anchors.find(anchor => anchor.anchorId === route.targetAnchorId).portSemanticId, point: { ...anchors.find(anchor => anchor.anchorId === route.targetAnchorId).point } }
            }))
        },
        pixelReality: {
            available: true,
            classification: 'isolated-rendered-component-bodies-and-routes',
            maskApplied: true,
            geometry: {
                viewportWidth: width,
                viewportHeight: height,
                drawingAreaAttributes: { x: '40', y: '60', width: '1120', height: '640' },
                bitmapSize: { width, height },
                drawingAreaClipped: false,
                renderStateBefore: renderState,
                renderStateAfter: renderState
            },
            drawingAreaBounds: { left: 100, top: 100, right: width - 100, bottom: height - 100 },
            sampleBounds: { left: 104, top: 104, right: width - 104, bottom: height - 104 },
            horizontalBuckets: [10, 10, 10],
            verticalBuckets: [10, 10, 10],
            occupiedPixelCount: 30,
            renderedComponentSemanticIds: occurrences.map(occurrence => occurrence.semanticId),
            renderedRouteIds: routes.map(route => route.routeId),
            renderedComponents,
            renderedRoutes,
            occupiedWidthRatio: 0.8,
            occupiedHeightRatio: 0.6
        },
        screenshot: {
            viewportName,
            requestedWidth: width,
            requestedHeight: height,
            capturedViewportWidth: width,
            capturedViewportHeight: height,
            pngWidth: width,
            pngHeight: height,
            activeViewId: 'schematic',
            sourceUri: baseProof().sourceUri,
            screenshotPath: viewportName === 'desktop-1920x1080'
                ? verifier.resolveM41ScreenshotPaths().desktop1920
                : verifier.resolveM41ScreenshotPaths().narrow
        }
    };
}

function validProof() {
    const proof = baseProof();
    proof.viewports = [
        validViewport('desktop-1920x1080', 1920, 1080),
        validViewport('narrow', 720, 900)
    ];
    proof.validationSummary = verifier.buildM41ValidationSummary(proof.viewports);
    return proof;
}

function expectAuthority(proof, authority, messageFragment) {
    assert.throws(
        () => verifier.assertM41ProductProof(proof),
        error => error.failedAuthority === authority && (!messageFragment || error.message.includes(messageFragment))
    );
}

test('accepts coherent compiler-backed M41 proof', () => {
    assert.doesNotThrow(() => verifier.assertM41ProductProof(validProof()));
});

test('rejects missing Spatial payload', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality = { proof: null, activeSheet: null };
    expectAuthority(proof, 'spatial', 'Runtime Spatial proof missing');
});

test('rejects hardcoded renderer booleans without runtime proof', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality = { proof: null, activeSheet: null };
    proof.paintOnlyRenderer = { snapsEndpoints: false, reroutes: false };
    expectAuthority(proof, 'spatial', 'Runtime Spatial proof missing');
});

test('rejects route identity mismatch', () => {
    const proof = validProof();
    proof.viewports[0].projectionReality.connectionIds[8] = 'connection-missing';
    expectAuthority(proof, 'projection', 'has no Spatial Route');
});

test('rejects duplicate Spatial Route identity', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.routes[8].routeId = proof.viewports[0].spatialReality.activeSheet.routes[0].routeId;
    expectAuthority(proof, 'spatial', 'Route identities are duplicated');
});

test('rejects repaired Presentation occurrence bounds', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.occurrences[0].bounds.x += 1;
    expectAuthority(proof, 'presentation', 'repaired Spatial bounds');
});

test('rejects matching malformed Spatial and Presentation occurrence bounds', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.occurrences[0].bounds.x = '176';
    proof.viewports[0].presentationReality.occurrences[0].bounds.x = '176';
    expectAuthority(proof, 'spatial', 'invalid Region or bounds');
});

test('rejects repaired Presentation route points', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.connectors[0].routePoints[1].y += 1;
    expectAuthority(proof, 'presentation', 'changed ordered Route points');
});

test('rejects matching malformed Spatial and Presentation route points', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.routes[0].points[1].y = null;
    proof.viewports[0].presentationReality.connectors[0].routePoints[1].y = null;
    expectAuthority(proof, 'presentation', 'changed ordered Route points');
});

test('rejects repaired Presentation endpoint identity', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.connectors[0].sourceEndpoint.anchorId = 'anchor-15';
    expectAuthority(proof, 'presentation', 'changed source Anchor identity');
});

test('rejects non-Golden quality', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.quality.routeBodyIntersectionCount = 1;
    expectAuthority(proof, 'quality', 'differs from Golden value');
});

test('rejects missing quality snapshot with actionable authority', () => {
    const proof = validProof();
    delete proof.viewports[0].spatialReality.activeSheet.quality;
    expectAuthority(proof, 'quality', 'Spatial quality snapshot missing');
});

test('rejects empty pixel buckets after runtime proof exists', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.horizontalBuckets = [1, 0, 1];
    expectAuthority(proof, 'pixels', 'empty horizontal pixel bucket');
});

test('rejects generic non-background pixels as body or Route evidence', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.classification = 'all-non-background-pixels';
    expectAuthority(proof, 'pixels', 'isolated rendered component-body/Route classification');
});

test('rejects claimed body or Route classification without applied render mask', () => {
    const proof = validProof();
    proof.viewports[1].pixelReality.maskApplied = false;
    expectAuthority(proof, 'pixels', 'render isolation mask was not applied');
});

test('rejects missing narrow runtime authority', () => {
    const proof = validProof();
    proof.viewports[1].spatialReality = { proof: null, activeSheet: null };
    expectAuthority(proof, 'spatial', 'narrow Runtime Spatial proof missing');
});

test('rejects Projection and Spatial active Sheet disagreement', () => {
    const proof = validProof();
    proof.viewports[0].projectionReality.activeSheetId = 'schematic/sheet/S2';
    expectAuthority(proof, 'projection', 'Spatial active Sheet differs');
});

test('rejects duplicate Presentation Occurrence substitution', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.occurrences[7] = {
        ...proof.viewports[0].presentationReality.occurrences[0],
        bounds: { ...proof.viewports[0].presentationReality.occurrences[0].bounds }
    };
    expectAuthority(proof, 'presentation', 'Occurrence identities are duplicated');
});

test('rejects missing Presentation source identity', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.occurrences[0].sourceProjectionIds = [];
    expectAuthority(proof, 'presentation', 'changed Occurrence source identity');
});

test('rejects duplicate Presentation connector substitution', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.connectors[8] = structuredClone(
        proof.viewports[0].presentationReality.connectors[0]
    );
    expectAuthority(proof, 'presentation', 'Route identities differ');
});

test('rejects duplicate Spatial Projection Connection identity', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.routes[8].projectionConnectionId =
        proof.viewports[0].spatialReality.activeSheet.routes[0].projectionConnectionId;
    expectAuthority(proof, 'projection', 'Spatial Projection Connection identities are duplicated');
});

test('rejects duplicate Spatial Anchor identity', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.anchors[15].anchorId =
        proof.viewports[0].spatialReality.activeSheet.anchors[0].anchorId;
    expectAuthority(proof, 'spatial', 'Anchor identities are duplicated');
});

test('rejects multiple Lane owners for one Route', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.lanes[1].routeIds.push(
        proof.viewports[0].spatialReality.activeSheet.routes[0].routeId
    );
    expectAuthority(proof, 'spatial', 'does not have exactly one owning Lane');
});

test('counts only used Lanes against Golden coverage', () => {
    const proof = validProof();
    for (const viewport of proof.viewports) {
        viewport.spatialReality.activeSheet.lanes.push({
            laneId: 'unused-lane', orientation: 'horizontal', coordinate: 777, routeIds: []
        });
    }
    assert.doesNotThrow(() => verifier.assertM41ProductProof(proof));
});

test('rejects missing required Golden quality value', () => {
    const proof = validProof();
    delete proof.viewports[0].spatialReality.activeSheet.quality.twistCount;
    expectAuthority(proof, 'quality', 'Invalid quality twistCount');
});

test('rejects non-finite pixel metrics', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.occupiedWidthRatio = Infinity;
    expectAuthority(proof, 'pixels', 'invalid occupied width ratio');
});

test('rejects missing governed Drawing Area pixel bounds', () => {
    const proof = validProof();
    delete proof.viewports[0].pixelReality.drawingAreaBounds;
    expectAuthority(proof, 'pixels', 'Drawing Area bounds missing');
});

test('rejects resized screenshot hiding actual viewport dimensions', () => {
    const proof = validProof();
    proof.viewports[0].screenshot.capturedViewportWidth = 1500;
    proof.viewports[0].screenshot.capturedViewportHeight = 952;
    expectAuthority(proof, 'screenshot', 'actual Electron viewport');
});

test('maps CSS Drawing Area bounds using actual captured viewport dimensions', () => {
    assert.deepEqual(
        electronCapture.scaleScreenRectToBitmap(
            { left: 475, top: 118, right: 1025, bottom: 395 },
            { x: 0, y: 0, width: 1500, height: 952 },
            { width: 3000, height: 1904 }
        ),
        { left: 950, top: 236, right: 2050, bottom: 790 }
    );
});

test('pixel isolation rejects targets hidden before proof capture', () => {
    assert.equal(electronCapture.isNormallyRenderedTargetSnapshot({ display: 'none', visibility: 'visible', opacity: '1', width: 20, height: 20 }), false);
    assert.equal(electronCapture.isNormallyRenderedTargetSnapshot({ display: 'block', visibility: 'hidden', opacity: '1', width: 20, height: 20 }), false);
    assert.equal(electronCapture.isNormallyRenderedTargetSnapshot({ display: 'block', visibility: 'visible', opacity: '0', width: 20, height: 20 }), false);
    assert.equal(electronCapture.isNormallyRenderedTargetSnapshot({ display: 'block', visibility: 'visible', opacity: '1', width: 0, height: 20 }), true);
    assert.equal(electronCapture.isNormallyRenderedTargetSnapshot({ display: 'block', visibility: 'visible', opacity: '1', width: 0, height: 0 }), false);
    assert.equal(electronCapture.isNormallyRenderedTargetSnapshot({ display: 'block', visibility: 'visible', opacity: '1', width: 20, height: 20 }), true);
});

test('rejects source identity synthesized independently from viewport runtime proof', () => {
    const proof = validProof();
    proof.viewports[0].sourceUri = proof.sourceUri.replace('01-rolling-shutter-spatial.athena', 'wrong.athena');
    expectAuthority(proof, 'source', 'source URI');
});

test('normalizes encoded Windows drive URI identity', () => {
    assert.equal(
        verifier.canonicalFileUri('file:///d%3A/Aaron/Workspace/source.athena'),
        verifier.canonicalFileUri('file:///D:/Aaron/Workspace/source.athena')
    );
});

test('rejects more than one Spatial Sheet', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.proof.sheets.push({ sheetId: 'schematic/sheet/S2' });
    expectAuthority(proof, 'spatial', 'exactly one Spatial Sheet');
});

test('rejects duplicate Spatial Region identity', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.regions[2].regionId = 'region-0';
    expectAuthority(proof, 'spatial', 'Region identities');
});

test('rejects Region membership that disagrees with Occurrence region identity', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.regions[0].memberOccurrenceIds.pop();
    expectAuthority(proof, 'spatial', 'Region membership');
});

test('rejects incomplete Grid Reference subject coverage', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.gridReferences[0].subjectId = 'unknown-subject';
    expectAuthority(proof, 'spatial', 'Grid Reference subject coverage');
});

test('rejects malformed Grid Reference coordinates', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.gridReferences[0].cellReference = 'Z99';
    expectAuthority(proof, 'spatial', 'Grid Reference cell');
});

test('rejects Projection Region identity drift', () => {
    const proof = validProof();
    proof.viewports[0].projectionReality.regionIds[0] = 'region-wrong';
    expectAuthority(proof, 'projection', 'Region identities differ');
});

test('rejects Projection Construct identity drift', () => {
    const proof = validProof();
    proof.viewports[0].projectionReality.constructIds[0] = 'construct-wrong';
    expectAuthority(proof, 'projection', 'Construct identities differ');
});

test('rejects wrong Projection backing view', () => {
    const proof = validProof();
    proof.viewports[0].projectionReality.backingViewIds = ['cabinet'];
    expectAuthority(proof, 'projection', 'backing view');
});

test('rejects Presentation Sheet identity drift', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.sheetId = 'schematic/sheet/S2';
    expectAuthority(proof, 'presentation', 'Sheet identity');
});

test('rejects repaired Presentation Drawing Area bounds', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.drawingAreaBounds.x += 1;
    expectAuthority(proof, 'presentation', 'Drawing Area bounds');
});

test('rejects Spatial Route detached from source Anchor', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.routes[0].points[0].x += 1;
    proof.viewports[0].presentationReality.connectors[0].routePoints[0].x += 1;
    expectAuthority(proof, 'spatial', 'source Anchor point');
});

test('rejects Route without drawable point sequence', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.routes[0].points = [];
    proof.viewports[0].presentationReality.connectors[0].routePoints = [];
    expectAuthority(proof, 'spatial', 'at least two points');
});

test('rejects Presentation connector semantic identity drift', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.connectors[0].semanticId = 'connection:wrong';
    expectAuthority(proof, 'presentation', 'semantic identity');
});

test('rejects Presentation connector Projection source drift', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.connectors[0].sourceProjectionIds = [];
    expectAuthority(proof, 'presentation', 'Projection source identity');
});

test('rejects Presentation endpoint semantic drift', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.connectors[0].sourceEndpoint.portSemanticId = 'port:wrong';
    expectAuthority(proof, 'presentation', 'endpoint semantic identity');
});

test('rejects Presentation connector Lane drift', () => {
    const proof = validProof();
    proof.viewports[0].presentationReality.connectors[0].laneId = 'lane-wrong';
    expectAuthority(proof, 'presentation', 'Lane identity');
});

test('rejects near-Golden quality values', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.quality.density += 1e-13;
    expectAuthority(proof, 'quality', 'differs from Golden value');
});

test('rejects inconsistent pixel bucket sums', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.occupiedPixelCount = 29;
    expectAuthority(proof, 'pixels', 'pixel counts are inconsistent');
});

test('rejects occupied pixel count beyond sample area', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.sampleBounds = { left: 104, top: 104, right: 110, bottom: 110 };
    proof.viewports[0].pixelReality.horizontalBuckets = [14, 13, 13];
    proof.viewports[0].pixelReality.verticalBuckets = [14, 13, 13];
    proof.viewports[0].pixelReality.occupiedPixelCount = 40;
    expectAuthority(proof, 'pixels', 'exceeds sample area');
});

test('rejects viewport name with wrong requested dimensions', () => {
    const proof = validProof();
    const viewport = proof.viewports[0];
    viewport.requestedSurface.width = 1919;
    viewport.pixelReality.geometry.viewportWidth = 1919;
    viewport.pixelReality.geometry.bitmapSize.width = 1919;
    viewport.screenshot.requestedWidth = 1919;
    viewport.screenshot.capturedViewportWidth = 1919;
    viewport.screenshot.pngWidth = 1919;
    expectAuthority(proof, 'viewport', 'required surface');
});

test('rejects rendered component identity drift', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.renderedComponentSemanticIds[0] = 'component:wrong';
    expectAuthority(proof, 'pixels', 'rendered Component identities');
});

test('rejects rendered Route identity drift', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.renderedRouteIds[0] = 'route-wrong';
    expectAuthority(proof, 'pixels', 'rendered Route identities');
});

test('rejects viewport-dependent runtime authority', () => {
    const proof = validProof();
    proof.viewports[1].spatialReality.activeSheet.occurrences[0].bounds.x += 1;
    proof.viewports[1].presentationReality.occurrences[0].bounds.x += 1;
    proof.viewports[1].pixelReality.renderedComponents[0].bounds.x += 1;
    const renderState = JSON.stringify({
        renderedComponents: proof.viewports[1].pixelReality.renderedComponents,
        renderedRoutes: proof.viewports[1].pixelReality.renderedRoutes
    });
    proof.viewports[1].pixelReality.geometry.renderStateBefore = renderState;
    proof.viewports[1].pixelReality.geometry.renderStateAfter = renderState;
    expectAuthority(proof, 'runtime', 'runtime authority differs');
});

test('rejects detached active Sheet authority', () => {
    const proof = validProof();
    for (const viewport of proof.viewports) {
        viewport.spatialReality.proof.sheets = [{
            ...viewport.spatialReality.proof.sheets[0],
            extent: { x: 0, y: 0, width: 999, height: 999 }
        }];
    }
    expectAuthority(proof, 'spatial', 'detached');
});

test('rejects collapsed compiled Spatial occupied span', () => {
    const proof = validProof();
    for (const viewport of proof.viewports) {
        const sheet = viewport.spatialReality.activeSheet;
        sheet.occurrences.forEach((occurrence, index) => {
            occurrence.bounds = { x: 100 + index * 18, y: 100 + (index % 2) * 18, width: 12, height: 12 };
            viewport.presentationReality.occurrences[index].bounds = { ...occurrence.bounds };
        });
        sheet.anchors.forEach((anchor, index) => {
            anchor.point = { x: 102 + index * 8, y: 104 + (index % 3) * 8 };
        });
        sheet.routes.forEach((route, index) => {
            const source = sheet.anchors.find(anchor => anchor.anchorId === route.sourceAnchorId);
            const target = sheet.anchors.find(anchor => anchor.anchorId === route.targetAnchorId);
            route.points = [{ ...source.point }, { x: target.point.x, y: source.point.y }, { ...target.point }];
            const connector = viewport.presentationReality.connectors[index];
            connector.routePoints = route.points.map(point => ({ ...point }));
            connector.sourceEndpoint.point = { ...source.point };
            connector.targetEndpoint.point = { ...target.point };
        });
    }
    expectAuthority(proof, 'spatial', 'occupied span');
});

test('rejects duplicate or malformed Spatial Lane facts', () => {
    const duplicate = validProof();
    duplicate.viewports[0].spatialReality.activeSheet.lanes.push({
        ...duplicate.viewports[0].spatialReality.activeSheet.lanes[0],
        routeIds: []
    });
    expectAuthority(duplicate, 'spatial', 'Lane');

    const malformed = validProof();
    malformed.viewports[0].spatialReality.activeSheet.lanes[0].orientation = '';
    malformed.viewports[0].spatialReality.activeSheet.lanes[0].coordinate = null;
    expectAuthority(malformed, 'spatial', 'Lane');
});

test('rejects empty Construct membership', () => {
    const proof = validProof();
    proof.viewports[0].spatialReality.activeSheet.constructs[0].memberOccurrenceIds = [];
    expectAuthority(proof, 'spatial', 'Construct');
});

test('rejects incomplete Anchor facts with actionable authority', () => {
    const proof = validProof();
    const anchor = proof.viewports[0].spatialReality.activeSheet.anchors[15];
    anchor.occurrenceId = '';
    anchor.portSemanticId = '';
    anchor.side = '';
    anchor.point = null;
    expectAuthority(proof, 'spatial', 'Anchor');
});

test('rejects active source bytes that differ from Golden baseline', () => {
    const proof = validProof();
    proof.sourceSha256 = 'sha256:wrong';
    expectAuthority(proof, 'source', 'Golden fixture');
});

test('rejects screenshot path outside M41 artifact directory', () => {
    const proof = validProof();
    proof.viewports[0].screenshot.screenshotPath = 'outside-m41.png';
    expectAuthority(proof, 'screenshot', 'artifact path');
});

test('rejects rendered Component and Route geometry drift', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.renderedComponents = [{
        semanticId: 'component:0',
        bounds: { x: 999, y: 999, width: 1, height: 1 }
    }];
    proof.viewports[0].pixelReality.renderedRoutes = [{
        routeId: 'route-0',
        points: [{ x: 999, y: 999 }, { x: 1000, y: 999 }]
    }];
    expectAuthority(proof, 'pixels', 'rendered geometry');
});

test('rejects duplicate rendered DOM identities hidden by normalized id lists', () => {
    const proof = validProof();
    const occurrence = proof.viewports[0].spatialReality.activeSheet.occurrences[0];
    proof.viewports[0].pixelReality.renderedComponents = [
        { semanticId: occurrence.semanticId, bounds: { ...occurrence.bounds } },
        { semanticId: occurrence.semanticId, bounds: { ...occurrence.bounds } }
    ];
    expectAuthority(proof, 'pixels', 'duplicate rendered');
});

test('rejects clipped Drawing Area pixel capture', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.geometry.drawingAreaClipped = true;
    expectAuthority(proof, 'pixels', 'clipped');
});

test('rejects render changes between screenshot and isolated capture', () => {
    const proof = validProof();
    proof.viewports[0].pixelReality.geometry.renderStateBefore = 'before';
    proof.viewports[0].pixelReality.geometry.renderStateAfter = 'after';
    expectAuthority(proof, 'pixels', 'changed between');
});

test('rejects missing persisted validation summaries', () => {
    const proof = validProof();
    delete proof.validationSummary;
    expectAuthority(proof, 'summary', 'validation summary');
});
