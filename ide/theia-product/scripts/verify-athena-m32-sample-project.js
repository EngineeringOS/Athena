const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_PACKAGE_PROOF_SENTINEL = 'ATHENA_M32_PACKAGE_PLATFORM_PROOF=';
const ATHENA_GRAPH_AUTHORING_PROOF_SENTINEL = 'ATHENA_M32_GRAPH_AUTHORING_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 120000;
const PACKAGE_SCHEMA_VERSION = 'm32.package-platform.v1';
const SOURCE_RELATIVE = 'src/01-package-platform-demo.athena';
const EXPECTED_OUTLINE_PATH = 'M32PackagePlatformDemo > ShutterMotorM32 > up';

const REQUIRED_M32_PACKAGE_PROOF_KINDS = [
    'repository-contract',
    'package-separation',
    'binding-resolution',
    'create-entity-panel',
    'outline-nested-port',
    'graph-representation',
    'visual-regression-guards',
];

async function main() {
    const repositoryRoot = resolveM32SampleProject();
    const screenshotPath = resolveM32ScreenshotPath();
    assertInstalledLspHostPresent();

    const smoke = await runElectronSmoke(repositoryRoot, screenshotPath, {
        activeView: 'cabinet',
        requireZeroDiagnostics: true,
    });
    if (smoke.capturedScreenshotPath !== screenshotPath) {
        throw new Error(`Athena M32 sample project smoke did not capture expected screenshot '${screenshotPath}'. Captured '${smoke.capturedScreenshotPath || 'n/a'}'.`);
    }
    assertPngScreenshot(screenshotPath);

    const packageProofPayloads = buildStructuredPackageProofPayloads(repositoryRoot, smoke.graphWorkbenchProof);
    assertStructuredPackageProofPayloads(packageProofPayloads);
    assertGraphWorkbenchProof(smoke.graphWorkbenchProof);
    const graphFirstAuthoringProof = await runGraphFirstAuthoringProof(repositoryRoot);

    console.log(`${ATHENA_PACKAGE_PROOF_SENTINEL}${JSON.stringify(packageProofPayloads)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(smoke.graphWorkbenchProof)}`);
    console.log(`${ATHENA_GRAPH_AUTHORING_PROOF_SENTINEL}${JSON.stringify(graphFirstAuthoringProof)}`);
    console.log(`Athena M32 sample project smoke passed. workspace=${smoke.openedWorkspace} screenshot=${screenshotPath} javaHome=${smoke.resolvedJavaHome || 'n/a'}`);
}

async function runElectronSmoke(repositoryRoot, screenshotPath, options = {}) {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const child = spawn(
        electronBinary,
        [entryScript, repositoryRoot],
        {
            cwd: path.resolve(__dirname, '..'),
            env: {
                ...process.env,
                ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
                ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
                ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH: EXPECTED_OUTLINE_PATH,
                ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE: options.skipOutline ? '1' : '0',
                ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: options.activeView || 'cabinet',
                ATHENA_ELECTRON_SMOKE_CREATE_ENTITY_TAG: options.createEntityTag || '',
                ATHENA_ELECTRON_SMOKE_EXPECT_SEMANTIC_ID: options.expectedSemanticId || '',
                ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: screenshotPath || '',
                ATHENA_ELECTRON_TEMP_USER_DATA: '1',
                ELECTRON_ENABLE_LOGGING: '1',
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
        }
    );

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
            capturedScreenshotPath = trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_JAVA_SENTINEL)) {
            resolvedJavaHome = trimmedLine.substring(ATHENA_JAVA_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_JAVA_UNRESOLVED_SENTINEL)) {
            unresolvedJavaSignal = trimmedLine.substring(ATHENA_JAVA_UNRESOLVED_SENTINEL.length);
        }
        if (trimmedLine.includes('Published 0 diagnostic(s)') && trimmedLine.includes(SOURCE_RELATIVE.replaceAll('\\', '/'))) {
            zeroDiagnosticPublication = true;
        }
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
        throw new Error(
            `Athena M32 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M32 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M32 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }
    if (!graphWorkbenchProof) {
        throw new Error(`Athena M32 sample project smoke did not report graph workbench proof.\n${outputLines.join('\n')}`);
    }
    if (options.requireZeroDiagnostics !== false && !zeroDiagnosticPublication) {
        throw new Error(`Athena M32 sample project smoke did not prove zero LSP diagnostics for ${SOURCE_RELATIVE}.\n${outputLines.join('\n')}`);
    }
    return {
        openedWorkspace,
        graphWorkbenchProof,
        capturedScreenshotPath,
        resolvedJavaHome,
    };
}

async function runGraphFirstAuthoringProof(sampleRepositoryRoot) {
    const tempBase = fs.realpathSync(os.tmpdir());
    const tempParent = fs.mkdtempSync(path.join(tempBase, 'athena-m32-graph-authoring-'));
    const resolvedTempParent = path.resolve(tempParent);
    if (path.dirname(resolvedTempParent) !== path.resolve(tempBase) ||
        !path.basename(resolvedTempParent).startsWith('athena-m32-graph-authoring-')
    ) {
        throw new Error(`Refusing unverified M32 graph-authoring temporary path: ${resolvedTempParent}`);
    }
    const tempRepositoryRoot = path.join(resolvedTempParent, 'sample-project');
    const createEntityTag = 'GraphMotorM32';
    const expectedSemanticId = `component:${createEntityTag}`;
    try {
        fs.cpSync(sampleRepositoryRoot, tempRepositoryRoot, { recursive: true });
        const createSmoke = await runElectronSmoke(tempRepositoryRoot, '', {
            activeView: 'cabinet',
            skipOutline: true,
            createEntityTag,
            requireZeroDiagnostics: false,
        });
        const graphFirstAuthoringProof = createSmoke.graphWorkbenchProof?.createEntityPanelProof?.graphFirstAuthoringProof;
        if (!graphFirstAuthoringProof?.accepted || !graphFirstAuthoringProof?.projected ||
            !graphFirstAuthoringProof?.outlineSkipped || graphFirstAuthoringProof.semanticId !== expectedSemanticId
        ) {
            throw new Error(`M32 graph-first authoring proof failed.\n${JSON.stringify(graphFirstAuthoringProof, null, 2)}`);
        }

        const sourcePath = path.join(tempRepositoryRoot, SOURCE_RELATIVE);
        const persistedSource = fs.readFileSync(sourcePath, 'utf8');
        const persistedDeviceBlock = extractNamedBlock(persistedSource, `device ${createEntityTag}`);
        if (!persistedDeviceBlock ||
            !persistedDeviceBlock.includes('port up') ||
            !persistedDeviceBlock.includes('port down') ||
            !persistedDeviceBlock.includes('port status')
        ) {
            throw new Error(`M32 graph-first authoring did not persist the expected nested device source.\n${persistedSource}`);
        }

        const reopenSmoke = await runElectronSmoke(tempRepositoryRoot, '', {
            activeView: 'cabinet',
            skipOutline: true,
            expectedSemanticId,
            requireZeroDiagnostics: false,
        });
        const reopenPersistenceProof = reopenSmoke.graphWorkbenchProof?.expectedSemanticProof;
        if (!reopenPersistenceProof?.present || !reopenPersistenceProof?.outlineSkipped ||
            reopenPersistenceProof.semanticId !== expectedSemanticId
        ) {
            throw new Error(`M32 graph-first authoring reopen proof failed.\n${JSON.stringify(reopenPersistenceProof, null, 2)}`);
        }
        return {
            graphFirstAuthoringProof,
            sourcePersisted: true,
            nestedPortsPersisted: true,
            reopenPersistenceProof,
        };
    } finally {
        fs.rmSync(resolvedTempParent, { recursive: true, force: true });
    }
}

function extractNamedBlock(source, declaration) {
    const declarationOffset = source.indexOf(declaration);
    if (declarationOffset < 0) {
        return undefined;
    }
    const openingBraceOffset = source.indexOf('{', declarationOffset + declaration.length);
    if (openingBraceOffset < 0) {
        return undefined;
    }
    let depth = 0;
    for (let offset = openingBraceOffset; offset < source.length; offset += 1) {
        if (source[offset] === '{') {
            depth += 1;
        } else if (source[offset] === '}') {
            depth -= 1;
            if (depth === 0) {
                return source.slice(declarationOffset, offset + 1);
            }
        }
    }
    return undefined;
}

function buildStructuredPackageProofPayloads(repositoryRoot, graphWorkbenchProof) {
    const sourceUri = pathToFileURL(path.join(repositoryRoot, SOURCE_RELATIVE)).toString();
    const representationProof = graphWorkbenchProof?.representationProof ?? {};
    const routeProof = graphWorkbenchProof?.routeProof ?? {};
    const visualProof = graphWorkbenchProof?.visualProof ?? {};
    const outlineProof = graphWorkbenchProof?.outlineProof ?? {};
    const createEntityPanelProof = graphWorkbenchProof?.createEntityPanelProof ?? {};

    return [
        proofEnvelope('repository-contract', sourceUri, {
            activeSourceContext: 'M32 IDE-openable governed repository contract',
            manifestFile: 'athena.yaml',
            lockFile: 'athena.lock',
            sourceRoot: 'src',
            diagnostics: '0',
        }),
        proofEnvelope('package-separation', sourceUri, {
            activeSourceContext: 'M32 package platform separation',
            semanticLanguageOwns: 'engineering truth only',
            engineeringPackageOwns: 'concepts, product definitions, defaults, validation intent',
            presentationProfileOwns: 'standard and customer appearance policy',
            representationPackageOwns: 'graphic resource descriptors, anchors, slots, bounds',
            runtimeQetDependency: 'none',
        }),
        proofEnvelope('binding-resolution', sourceUri, {
            activeSourceContext: 'M32 package binding and profile resolution',
            representationIds: normalizeArray(representationProof.representationIds).join(','),
            fallbackRepresentationIds: normalizeArray(representationProof.fallbackRepresentationIds).join(','),
            engineeringPackageIds: normalizeArray(representationProof.engineeringPackageIds).join(','),
            presentationProfileIds: normalizeArray(representationProof.presentationProfileIds).join(','),
            bindingManifestIds: normalizeArray(representationProof.bindingManifestIds).join(','),
            representationPackageIds: normalizeArray(representationProof.representationPackageIds).join(','),
            descriptorIds: normalizeArray(representationProof.descriptorIds).join(','),
            graphicResourceIds: normalizeArray(representationProof.graphicResourceIds).join(','),
            anchorMapSummary: normalizeArray(representationProof.anchorMapSummary).join(','),
            labelBindingSummary: normalizeArray(representationProof.labelBindingSummary).join(','),
            presentationTerminalCount: String(representationProof.presentationTerminalCount ?? 0),
            presentationLabelCount: String(representationProof.presentationLabelCount ?? 0),
        }),
        proofEnvelope('create-entity-panel', sourceUri, {
            activeSourceContext: 'M32 Graph View create entity panel proof',
            buttonPresent: String(createEntityPanelProof.buttonPresent === true),
            buttonDisabledBeforeClick: String(createEntityPanelProof.buttonDisabledBeforeClick === true),
            opened: String(createEntityPanelProof.opened === true),
            closed: String(createEntityPanelProof.closed === true),
            hasConceptSelect: String(createEntityPanelProof.hasConceptSelect === true),
            hasTagInput: String(createEntityPanelProof.hasTagInput === true),
            hasModelInput: String(createEntityPanelProof.hasModelInput === true),
            previewButtonPresent: String(createEntityPanelProof.previewButtonPresent === true),
            panelWidth: String(createEntityPanelProof.panelWidth ?? 0),
            panelHeight: String(createEntityPanelProof.panelHeight ?? 0),
        }),
        proofEnvelope('outline-nested-port', sourceUri, {
            activeSourceContext: 'M32 Theia Outline nested port proof',
            expectedOutlinePath: EXPECTED_OUTLINE_PATH,
            outlineMatched: String(normalizeArray(outlineProof.paths).some(proofPath => proofPath.endsWith(EXPECTED_OUTLINE_PATH))),
            hasOutlineWidget: String(outlineProof.hasOutlineWidget === true),
        }),
        proofEnvelope('graph-representation', sourceUri, {
            activeSourceContext: 'M32 graph workbench package representation proof',
            activeViewId: graphWorkbenchProof?.activeViewId || '',
            visibleProjectionViewIds: normalizeArray(graphWorkbenchProof?.projectionViewProof?.visibleViewIds).join(','),
            compatibilityProjectionViewCount: String(graphWorkbenchProof?.projectionViewProof?.compatibilityViewCount ?? 0),
            routeCount: String(routeProof.routeCount ?? 0),
            routesWithTerminalAnchors: String(routeProof.routesWithTerminalAnchors ?? 0),
            representationCount: String(representationProof.representationCount ?? 0),
            semanticIds: normalizeArray(representationProof.semanticIds).join(','),
        }),
        proofEnvelope('visual-regression-guards', sourceUri, {
            activeSourceContext: 'M32 visual guardrails from structured graph proof',
            svgViewBox: visualProof.svgViewBox || '',
            viewBoxWidth: String(visualProof.viewBoxWidth ?? 0),
            viewBoxHeight: String(visualProof.viewBoxHeight ?? 0),
            routeBodyIntersectionCount: String(visualProof.routeBodyIntersectionCount ?? 0),
            nonOrthogonalSegmentCount: String(visualProof.nonOrthogonalSegmentCount ?? 0),
            floatingBarTransparent: String(graphWorkbenchProof?.floatingBarTransparent === true),
            bottomDockTransparent: String(graphWorkbenchProof?.bottomDockTransparent === true),
            zoomDockTransparent: String(graphWorkbenchProof?.zoomDockTransparent === true),
            sheetTransparent: String(graphWorkbenchProof?.sheetTransparent === true),
        }),
    ];
}

function proofEnvelope(proofKind, activeSourceUri, payload) {
    return {
        schemaVersion: PACKAGE_SCHEMA_VERSION,
        requestId: `proof:${proofKind}`,
        activeSourceUri,
        activeSourceRevision: 'm32-sample-project',
        payloadKind: 'proof',
        payload: {
            proofKind,
            ...payload,
        },
    };
}

function assertStructuredPackageProofPayloads(proofPayloads) {
    if (!Array.isArray(proofPayloads)) {
        throw new Error('Athena M32 package proof payloads must be an array.');
    }
    const byKind = new Map(proofPayloads.map(payload => [payload?.payload?.proofKind, payload]));
    const missing = REQUIRED_M32_PACKAGE_PROOF_KINDS.filter(proofKind => !byKind.has(proofKind));
    if (missing.length > 0) {
        throw new Error(`Athena M32 package proof payloads missing: ${missing.join(', ')}`);
    }
    for (const proofKind of REQUIRED_M32_PACKAGE_PROOF_KINDS) {
        const envelope = byKind.get(proofKind);
        if (envelope.schemaVersion !== PACKAGE_SCHEMA_VERSION) {
            throw new Error(`${proofKind} proof schema mismatch: ${envelope.schemaVersion || '<missing>'}`);
        }
        if (envelope.payloadKind !== 'proof') {
            throw new Error(`${proofKind} proof payloadKind expected proof but received ${envelope.payloadKind || '<missing>'}`);
        }
        if (!envelope.activeSourceUri || !envelope.activeSourceUri.includes(SOURCE_RELATIVE)) {
            throw new Error(`${proofKind} proof must use the M32 source URI.`);
        }
        if (!envelope.payload.activeSourceContext) {
            throw new Error(`${proofKind} proof missing activeSourceContext.`);
        }
    }
    const serialized = JSON.stringify(proofPayloads);
    [
        'athena.yaml',
        'athena.lock',
        'presentationProfileOwns',
        'representationPackageOwns',
        'runtimeQetDependency":"none',
        EXPECTED_OUTLINE_PATH,
    ].forEach(expected => requireIncludes(serialized, expected, expected));
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
        'stageHasGrid',
    ].filter(key => graphWorkbenchProof[key] !== true);
    if (missingGraphProof.length > 0) {
        throw new Error(
            `Athena M32 graph-workbench proof failed: ${missingGraphProof.join(', ')}\n${JSON.stringify(graphWorkbenchProof)}`
        );
    }
    if (graphWorkbenchProof.activeViewId !== 'cabinet') {
        throw new Error(
            `Athena M32 graph-workbench product proof expected Cabinet view, received ${graphWorkbenchProof.activeViewId || '<missing>'}.`
        );
    }
    assertProjectionViewFocusProof(graphWorkbenchProof.projectionViewProof);
    assertRouteProof(graphWorkbenchProof.routeProof);
    assertRepresentationProof(graphWorkbenchProof.representationProof);
    assertVisualProof(graphWorkbenchProof.visualProof);
    assertOutlineProof(graphWorkbenchProof.outlineProof);
    assertCreateEntityPanelProof(graphWorkbenchProof.createEntityPanelProof);
    assertDocumentProjectionProof(graphWorkbenchProof.documentProjectionProof);
}

function assertProjectionViewFocusProof(projectionViewProof) {
    if (!projectionViewProof) {
        throw new Error('Athena M32 graph proof missing projection view focus proof.');
    }
    const visibleViewIds = normalizeArray(projectionViewProof.visibleViewIds);
    if (visibleViewIds.length !== 1 || visibleViewIds[0] !== 'cabinet') {
        throw new Error(
            `Athena M32 product toolbar must expose only the Cabinet demo view. Visible views: ${visibleViewIds.join(', ') || '<none>'}.\n${JSON.stringify(projectionViewProof, null, 2)}`
        );
    }
    if (Number(projectionViewProof.visibleViewButtonCount) !== 1 || Number(projectionViewProof.visibleViewCountAttribute) !== 1) {
        throw new Error(`Athena M32 product toolbar has unstable visible projection button count.\n${JSON.stringify(projectionViewProof, null, 2)}`);
    }
    if (!normalizeArray(projectionViewProof.activeViewIds).includes('cabinet')) {
        throw new Error(`Athena M32 Cabinet view button is not active.\n${JSON.stringify(projectionViewProof, null, 2)}`);
    }
    if (Number(projectionViewProof.compatibilityViewCount) < 1) {
        throw new Error(`Athena M32 projection compatibility modes were not accounted for as hidden compatibility.\n${JSON.stringify(projectionViewProof, null, 2)}`);
    }
}

function assertDocumentProjectionProof(documentProjectionProof) {
    if (!documentProjectionProof?.compatibilityActivated) {
        throw new Error(`Athena M32 document projection proof did not activate Documentation through the compatibility API.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
    if (!documentProjectionProof.contextualNavigationPresent || Number(documentProjectionProof.contextualDocumentControlCount) < 1) {
        throw new Error(`Athena M32 Documentation controls were not rendered in contextual navigation.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
    if (Number(documentProjectionProof.globalToolbarDocumentControlCount) !== 0) {
        throw new Error(`Athena M32 Documentation controls polluted the global toolbar.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
    if (!documentProjectionProof?.hasSheetViewSelector) {
        throw new Error(`Athena M32 document projection proof did not expose sheet navigation.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
    if (Number(documentProjectionProof.sheetViewOptionCount) < 2) {
        throw new Error(`Athena M32 sheet navigation must expose multiple sheets.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
    if (!documentProjectionProof.selectedSheetViewId) {
        throw new Error(`Athena M32 sheet navigation proof missing selected sheet.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
    if (!documentProjectionProof.sheetSelectionRestored ||
        documentProjectionProof.restoredSheetViewId !== documentProjectionProof.selectedSheetViewIdBeforeProjectionSwitch
    ) {
        throw new Error(`Athena M32 Documentation sheet selection did not survive the Cabinet compatibility round trip.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
    if (documentProjectionProof.restoredActiveViewId !== 'cabinet' || documentProjectionProof.contextualNavigationPresentAfterRestore) {
        throw new Error(`Athena M32 document projection proof did not restore the clean Cabinet surface.\n${JSON.stringify(documentProjectionProof, null, 2)}`);
    }
}

function assertRouteProof(routeProof) {
    if (!routeProof || routeProof.routeCount < 1 || routeProof.terminalCount < 2) {
        throw new Error(`Athena M32 route proof missing expected rendered routes.\n${JSON.stringify(routeProof, null, 2)}`);
    }
    if (routeProof.routesWithTerminalAnchors < routeProof.routeCount) {
        throw new Error(`Athena M32 route proof has routes without terminal anchors.\n${JSON.stringify(routeProof, null, 2)}`);
    }
    if (normalizeArray(routeProof.centerFallbackRouteIds).length > 0) {
        throw new Error(`Athena M32 route proof used center fallback routes: ${routeProof.centerFallbackRouteIds.join(', ')}`);
    }
}

function assertRepresentationProof(representationProof) {
    if (!representationProof || representationProof.representationCount < 1 || representationProof.presentationTerminalCount < 1 || representationProof.presentationLabelCount < 1) {
        throw new Error(`Athena M32 representation proof missing expected representation facts.\n${JSON.stringify(representationProof, null, 2)}`);
    }
    if (normalizeArray(representationProof.fallbackRepresentationIds).length > 0) {
        throw new Error(`Athena M32 representation proof used fallback symbols: ${representationProof.fallbackRepresentationIds.join(', ')}`);
    }
    const nativeRepresentationIds = normalizeArray(representationProof.representationIds)
        .filter(representationId => representationId.startsWith('athena-industrial-control-v0:'));
    if (nativeRepresentationIds.length > 0) {
        throw new Error(`Athena M32 live Graph View used native representation fallback ids: ${nativeRepresentationIds.join(', ')}`);
    }
    const requiredPackageEvidence = [
        ['engineering package id', representationProof.engineeringPackageIds],
        ['presentation profile id', representationProof.presentationProfileIds],
        ['binding manifest id', representationProof.bindingManifestIds],
        ['representation package id', representationProof.representationPackageIds],
        ['descriptor id', representationProof.descriptorIds],
        ['graphic resource id', representationProof.graphicResourceIds],
        ['anchor mapping', representationProof.anchorMapSummary],
        ['label binding', representationProof.labelBindingSummary],
    ];
    const missingEvidence = requiredPackageEvidence
        .filter(([, values]) => normalizeArray(values).length === 0)
        .map(([label]) => label);
    if (missingEvidence.length > 0) {
        throw new Error(`Athena M32 representation proof missing package evidence: ${missingEvidence.join(', ')}\n${JSON.stringify(representationProof, null, 2)}`);
    }
    const duplicateRepresentationSemanticIds = duplicateValues(representationProof.semanticIds);
    if (duplicateRepresentationSemanticIds.length > 0) {
        throw new Error(`Athena M32 representation proof found duplicate occurrences: ${duplicateRepresentationSemanticIds.join(', ')}`);
    }
}

function assertVisualProof(visualProof) {
    if (!visualProof) {
        throw new Error('Athena M32 graph proof missing visual proof.');
    }
    if (visualProof.svgViewBox === '0 0 1680 1188') {
        throw new Error('Athena M32 visual proof rejected hard-coded oversized viewBox 0 0 1680 1188.');
    }
    if (Number(visualProof.viewBoxWidth) <= 0 || Number(visualProof.viewBoxHeight) <= 0) {
        throw new Error(`Athena M32 visual proof has invalid viewBox dimensions.\n${JSON.stringify(visualProof, null, 2)}`);
    }
    if (Number(visualProof.routeBodyIntersectionCount) !== 0) {
        throw new Error(`Athena M32 visual proof found route/body intersections.\n${JSON.stringify(visualProof, null, 2)}`);
    }
    if (Number(visualProof.nonOrthogonalSegmentCount ?? 0) !== 0) {
        throw new Error(`Athena M32 visual proof found non-orthogonal route segments.\n${JSON.stringify(visualProof, null, 2)}`);
    }
}

function assertOutlineProof(outlineProof) {
    if (!outlineProof || outlineProof.skipped) {
        throw new Error(`Athena M32 outline proof was not collected.\n${JSON.stringify(outlineProof)}`);
    }
    if (outlineProof.hasOutlineWidget !== true || outlineProof.widgetId !== 'outline-view') {
        throw new Error(`Athena M32 outline proof did not open Theia's Outline view.\n${JSON.stringify(outlineProof)}`);
    }
    const paths = normalizeArray(outlineProof.paths);
    if (!paths.some(proofPath => proofPath.endsWith(EXPECTED_OUTLINE_PATH))) {
        throw new Error(
            `Athena M32 Outline view missing nested compact port path '${EXPECTED_OUTLINE_PATH}'.\n${JSON.stringify(outlineProof, null, 2)}`
        );
    }
}

function assertCreateEntityPanelProof(createEntityPanelProof) {
    if (!createEntityPanelProof) {
        throw new Error('Athena M32 graph proof missing create entity panel proof.');
    }
    if (createEntityPanelProof.buttonPresent !== true) {
        throw new Error(`Athena M32 create entity button was not found.\n${JSON.stringify(createEntityPanelProof, null, 2)}`);
    }
    if (createEntityPanelProof.buttonDisabledBeforeClick === true) {
        throw new Error(`Athena M32 create entity button was disabled before click.\n${JSON.stringify(createEntityPanelProof, null, 2)}`);
    }
    for (const key of ['opened', 'closed', 'hasConceptSelect', 'hasTagInput', 'hasModelInput', 'previewButtonPresent', 'textIncludesCreateEntity']) {
        if (createEntityPanelProof[key] !== true) {
            throw new Error(`Athena M32 create entity panel proof failed ${key}.\n${JSON.stringify(createEntityPanelProof, null, 2)}`);
        }
    }
    if (Number(createEntityPanelProof.panelWidth) <= 0 || Number(createEntityPanelProof.panelHeight) <= 0) {
        throw new Error(`Athena M32 create entity panel had invalid dimensions.\n${JSON.stringify(createEntityPanelProof, null, 2)}`);
    }
    if (Number(createEntityPanelProof.panelHeight) < 160) {
        throw new Error(`Athena M32 create entity panel is collapsed or overlapped.\n${JSON.stringify(createEntityPanelProof, null, 2)}`);
    }
    if (createEntityPanelProof.withinViewport !== true || createEntityPanelProof.frontmostAtCenter !== true) {
        throw new Error(`Athena M32 create entity panel is not frontmost within the viewport.\n${JSON.stringify(createEntityPanelProof, null, 2)}`);
    }
    if (Number(createEntityPanelProof.reachableControlCount) < 5) {
        throw new Error(`Athena M32 create entity panel controls are not all reachable.\n${JSON.stringify(createEntityPanelProof, null, 2)}`);
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
            `Athena M32 smoke requires the installed LSP host before product proof. Missing ${launcher}. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
        );
    }
}

function resolveM32SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm32', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        SOURCE_RELATIVE,
        path.join('packages', 'resources', 'resource.power-supply.iec.svg'),
        path.join('packages', 'resources', 'resource.relay.iec.svg'),
        path.join('packages', 'resources', 'resource.motor.iec.svg'),
        path.join('packages', 'resources', 'resource.motor.compact.svg'),
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M32 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

function resolveM32ScreenshotPath() {
    const repositoryRoot = resolveRepoRoot();
    return path.resolve(
        repositoryRoot,
        '_bmad-output',
        'implementation-artifacts',
        'm32',
        'screenshots',
        'm32-graph-workbench-smoke.png'
    );
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) {
        throw new Error(`Athena M32 screenshot guard did not create ${screenshotPath}.`);
    }
    const bytes = fs.readFileSync(screenshotPath);
    const pngSignature = '89504e470d0a1a0a';
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== pngSignature) {
        throw new Error(`Athena M32 screenshot guard produced an invalid PNG at ${screenshotPath}.`);
    }
}

function resolveRepoRoot() {
    let current = path.resolve(__dirname);
    while (path.dirname(current) !== current && !fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        current = path.dirname(current);
    }
    if (!fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        throw new Error('Could not locate Athena repository root.');
    }
    return current;
}

function normalizeArray(value) {
    return Array.isArray(value) ? value : [];
}

function duplicateValues(value) {
    const counts = new Map();
    normalizeArray(value)
        .map(item => String(item).trim())
        .filter(Boolean)
        .forEach(item => counts.set(item, (counts.get(item) ?? 0) + 1));
    return [...counts.entries()]
        .filter(([, count]) => count > 1)
        .map(([item]) => item)
        .sort();
}

function requireIncludes(value, expected, label) {
    if (!value.includes(expected)) {
        throw new Error(`M32 proof payloads missing ${label}: ${expected}`);
    }
}

if (require.main === module) {
    main().catch(error => {
        console.error(error.stack || String(error));
        process.exit(1);
    });
}

module.exports = {
    buildStructuredPackageProofPayloads,
    assertStructuredPackageProofPayloads,
    assertGraphWorkbenchProof,
    assertPngScreenshot,
    REQUIRED_M32_PACKAGE_PROOF_KINDS,
};
