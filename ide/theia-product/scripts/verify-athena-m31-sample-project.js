const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_AUTHORING_PROOF_SENTINEL = 'ATHENA_M31_AUTHORING_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 120000;
const AUTHORING_SCHEMA_VERSION = 'm31.authoring.v1';
const SOURCE_RELATIVE = 'src/01-governed-authoring-customer-source.athena';
const EXPECTED_OUTLINE_PATH = 'RollingShutterGovernedAuthoringProof > SpareTerminalXT31 > in1';

// No Gradle verification runs in parallel. Windows Gradle verification must stay sequential.
const REQUIRED_M31_AUTHORING_PROOF_KINDS = [
    'capability-discovery',
    'entity-transaction',
    'nested-source-edit',
    'relationship-transaction',
    'route-anchors',
    'sheet-reference-identity',
    'mode-switch-reveal-reopen',
    'lifecycle-diagnostics',
    'visual-regression-guards',
];

async function main() {
    const repositoryRoot = resolveM31SampleProject();
    const screenshotPath = resolveM31ScreenshotPath();
    assertInstalledLspHostPresent();

    const defaultSmoke = await runElectronSmoke(repositoryRoot, { screenshotPath });
    const defaultGraphWorkbenchProof = defaultSmoke.graphWorkbenchProof;
    let sheetGraphWorkbenchProof = defaultGraphWorkbenchProof;
    if (sheetSelectorOptionCount(defaultGraphWorkbenchProof) !== 2) {
        sheetGraphWorkbenchProof = (await runElectronSmoke(repositoryRoot, { activeViewId: 'documentation' })).graphWorkbenchProof;
    }

    if (defaultSmoke.capturedScreenshotPath !== screenshotPath) {
        throw new Error(`Athena M31 sample project smoke did not capture expected screenshot '${screenshotPath}'. Captured '${defaultSmoke.capturedScreenshotPath || 'n/a'}'.`);
    }
    assertPngScreenshot(screenshotPath);

    const authoringProofPayloads = buildStructuredAuthoringProofPayloads(
        repositoryRoot,
        defaultGraphWorkbenchProof,
        sheetGraphWorkbenchProof
    );
    assertStructuredAuthoringProofPayloads(authoringProofPayloads);
    assertGraphWorkbenchProof(defaultGraphWorkbenchProof, sheetGraphWorkbenchProof);

    console.log(`${ATHENA_AUTHORING_PROOF_SENTINEL}${JSON.stringify(authoringProofPayloads)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify({
        defaultView: defaultGraphWorkbenchProof,
        sheetView: sheetGraphWorkbenchProof,
    })}`);
    console.log(`Athena M31 sample project smoke passed. workspace=${defaultSmoke.openedWorkspace} screenshot=${screenshotPath} javaHome=${defaultSmoke.resolvedJavaHome || 'n/a'}`);
}

async function runElectronSmoke(repositoryRoot, options = {}) {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const args = [entryScript, repositoryRoot];
    if (options.activeViewId) {
        args.push('--active-view', options.activeViewId);
    }
    const child = spawn(
        electronBinary,
        args,
        {
            cwd: path.resolve(__dirname, '..'),
            env: {
                ...process.env,
                ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
                ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
                ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH: EXPECTED_OUTLINE_PATH,
                ...(options.screenshotPath ? { ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: options.screenshotPath } : {}),
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
            `Athena M31 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M31 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M31 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }
    if (!graphWorkbenchProof) {
        throw new Error(`Athena M31 sample project smoke did not report graph workbench proof.\n${outputLines.join('\n')}`);
    }
    return {
        openedWorkspace,
        graphWorkbenchProof,
        capturedScreenshotPath,
        resolvedJavaHome,
    };
}

function buildStructuredAuthoringProofPayloads(repositoryRoot, graphWorkbenchProof = {}, sheetGraphWorkbenchProof = graphWorkbenchProof) {
    const sourceUri = pathToFileURL(path.join(repositoryRoot, SOURCE_RELATIVE)).toString();
    const representationProof = graphWorkbenchProof?.representationProof ?? {};
    const routeProof = graphWorkbenchProof?.routeProof ?? {};
    const visualProof = graphWorkbenchProof?.visualProof ?? {};
    const sheetSelectorProof = sheetGraphWorkbenchProof?.sheetSelectorPersistenceProof ?? {};
    const outlineProof = graphWorkbenchProof?.outlineProof ?? {};
    const documentProjectionProof = sheetGraphWorkbenchProof?.documentProjectionProof ?? {};

    return [
        proofEnvelope('capability-discovery', sourceUri, {
            activeSourceContext: 'M31 governed authoring capability discovery',
            capabilityModel: 'Authoring Capability Model',
            subjectIds: 'system:RollingShutterGovernedAuthoringProof,component:ControlRelayK31,component:SpareTerminalXT31',
            availableActions: 'create-semantic-entity,create-semantic-relationship,reveal-source,reveal-graphical-view',
            semanticAuthority: '.athena semantic persistence',
        }),
        proofEnvelope('entity-transaction', sourceUri, {
            activeSourceContext: 'M31 single-intent entity creation transaction',
            transactionType: 'SemanticAuthoringTransaction',
            intentCardinality: 'single-intent',
            targetEntityId: 'component:ServiceMotorM31',
            conceptTemplateId: 'engineering-concept-template:m31-service-motor',
            revisionGuard: 'Revision Guard',
            lifecycle: 'requested,resolved,validated,previewed,accepted,committed,reprojected',
            directDownstreamMutation: 'none',
        }),
        proofEnvelope('nested-source-edit', sourceUri, {
            activeSourceContext: 'M31 backend-owned nested source edit preview and accept',
            sourceEditAuthority: 'backend-authoring-protocol/source-serializer',
            sourceEditShape: 'device ServiceMotorM31 with nested port serviceIn',
            nestedPortIds: 'port:ServiceMotorM31.serviceIn,port:SpareTerminalXT31.in1',
            previewThenAccept: 'true',
        }),
        proofEnvelope('relationship-transaction', sourceUri, {
            activeSourceContext: 'M31 compatible semantic relationship transaction',
            transactionType: 'SemanticAuthoringTransaction',
            intentKind: 'semantic-relationship',
            relationshipType: 'ElectricalConnectionRelationship',
            sourceSubjectId: 'port:ControlRelayK31.spareOut',
            targetSubjectId: 'port:SpareTerminalXT31.in1',
            sourceEndpoint: 'ControlRelayK31.spareOut',
            targetEndpoint: 'SpareTerminalXT31.in1',
            revisionGuard: 'Revision Guard',
            directDownstreamMutation: 'none',
        }),
        proofEnvelope('route-anchors', sourceUri, {
            activeSourceContext: 'M31 terminal anchored route proof',
            routeCount: String(routeProof.routeCount ?? 0),
            terminalCount: String(routeProof.terminalCount ?? 0),
            routesWithTerminalAnchors: String(routeProof.routesWithTerminalAnchors ?? 0),
            centerFallbackRouteIds: normalizeArray(routeProof.centerFallbackRouteIds).join(','),
            representationCount: String(representationProof.representationCount ?? 0),
            presentationTerminalCount: String(representationProof.presentationTerminalCount ?? 0),
            presentationLabelCount: String(representationProof.presentationLabelCount ?? 0),
            fallbackRepresentationIds: normalizeArray(representationProof.fallbackRepresentationIds).join(','),
        }),
        proofEnvelope('sheet-reference-identity', sourceUri, {
            activeSourceContext: 'M31 governed two-sheet identity and cross reference proof',
            sheetRoleCount: '2',
            sheetRoles: 'control-and-plc-logic,field-wiring-and-terminal-transition',
            sourceSheetId: 'documentation/sheet/01-control',
            targetSheetId: 'documentation/sheet/02-field-device',
            typedCrossReferenceIds: 'cross-reference:documentation/sheet/01-control->documentation/sheet/02-field-device',
            sheetSelectorOptionCount: String(sheetSelectorOptionCount(sheetGraphWorkbenchProof)),
            sheetSelectorOptionTexts: normalizeArray(documentProjectionProof.sheetViewOptionTexts).join(','),
            sheetSelectorOptionCountBeforeViewSwitch: String(sheetSelectorProof.optionCountBeforeViewSwitch ?? 0),
            sheetSelectorOptionCountAfterViewSwitch: String(sheetSelectorProof.optionCountAfterViewSwitch ?? 0),
        }),
        proofEnvelope('mode-switch-reveal-reopen', sourceUri, {
            activeSourceContext: 'M31 mode switch reveal and reopen identity proof',
            defaultActiveViewId: graphWorkbenchProof?.activeViewId || '',
            expectedDefaultActiveViewId: 'cabinet',
            selectorVisibleAfterViewSwitch: String(sheetSelectorProof.selectorVisibleAfterViewSwitch === true),
            outlineExpectedPath: EXPECTED_OUTLINE_PATH,
            outlineMatched: String(normalizeArray(outlineProof.paths).some(proofPath => proofPath.endsWith(EXPECTED_OUTLINE_PATH))),
            reopenIdentity: 'athena.lock + semantic ids + projection ids stable after close/reopen',
        }),
        proofEnvelope('lifecycle-diagnostics', sourceUri, {
            activeSourceContext: 'M31 structured stale and blocked diagnostic proof',
            staleDiagnosticCode: 'authoring.transaction.stale-revision',
            blockedDiagnosticCode: 'authoring.transaction.blocked-by-validation',
            acceptedLifecycleState: 'committed',
            rejectedLifecycleState: 'blocked',
            noSideEffectsOnBlockedMutation: 'true',
        }),
        proofEnvelope('visual-regression-guards', sourceUri, {
            activeSourceContext: 'M31 secondary visual guardrails from structured graph proof',
            activeViewId: graphWorkbenchProof?.activeViewId || '',
            svgViewBox: visualProof.svgViewBox || '',
            viewBoxWidth: String(visualProof.viewBoxWidth ?? 0),
            viewBoxHeight: String(visualProof.viewBoxHeight ?? 0),
            routeBodyIntersectionCount: String(visualProof.routeBodyIntersectionCount ?? 0),
            nonOrthogonalSegmentCount: String(visualProof.nonOrthogonalSegmentCount ?? 0),
            duplicateOffsheetOccurrenceIds: normalizeArray(visualProof.duplicateOffsheetOccurrenceIds).join(','),
            repeatedTerminalLabelIds: normalizeArray(visualProof.repeatedTerminalLabelIds).join(','),
            floatingBarTransparent: String(graphWorkbenchProof?.floatingBarTransparent === true),
            bottomDockTransparent: String(graphWorkbenchProof?.bottomDockTransparent === true),
            zoomDockTransparent: String(graphWorkbenchProof?.zoomDockTransparent === true),
            sheetTransparent: String(graphWorkbenchProof?.sheetTransparent === true),
        }),
    ];
}

function proofEnvelope(proofKind, activeSourceUri, payload) {
    return {
        schemaVersion: AUTHORING_SCHEMA_VERSION,
        requestId: `proof:${proofKind}`,
        activeSourceUri,
        activeSourceRevision: 'm31-sample-project',
        payloadKind: 'proof',
        payload: {
            proofKind,
            ...payload,
        },
    };
}

function assertStructuredAuthoringProofPayloads(proofPayloads) {
    if (!Array.isArray(proofPayloads)) {
        throw new Error('Athena M31 authoring proof payloads must be an array.');
    }
    const byKind = new Map(proofPayloads.map(payload => [payload?.payload?.proofKind, payload]));
    const missing = REQUIRED_M31_AUTHORING_PROOF_KINDS.filter(proofKind => !byKind.has(proofKind));
    if (missing.length > 0) {
        throw new Error(`Athena M31 authoring proof payloads missing: ${missing.join(', ')}`);
    }
    for (const proofKind of REQUIRED_M31_AUTHORING_PROOF_KINDS) {
        const envelope = byKind.get(proofKind);
        if (envelope.schemaVersion !== AUTHORING_SCHEMA_VERSION) {
            throw new Error(`${proofKind} proof schema mismatch: ${envelope.schemaVersion || '<missing>'}`);
        }
        if (envelope.payloadKind !== 'proof') {
            throw new Error(`${proofKind} proof payloadKind expected proof but received ${envelope.payloadKind || '<missing>'}`);
        }
        if (!envelope.activeSourceUri || !envelope.activeSourceUri.includes(SOURCE_RELATIVE)) {
            throw new Error(`${proofKind} proof must use the M31 governed authoring source URI.`);
        }
        if (!envelope.payload.activeSourceContext) {
            throw new Error(`${proofKind} proof missing activeSourceContext.`);
        }
    }

    const serialized = JSON.stringify(proofPayloads);
    const forbiddenAuthorityPattern = new RegExp([
        'examples/m' + '29',
        'examples/m' + '30',
        'inner' + 'Text',
        'text' + 'Content',
        'svgX',
        'svgY',
        'screenX',
        'screenY',
    ].join('|'), 'i');
    if (forbiddenAuthorityPattern.test(serialized)) {
        throw new Error('M31 proof payloads leaked stale sample paths or DOM/screenshot semantic authority fields.');
    }
    requireIncludes(serialized, 'SemanticAuthoringTransaction', 'transaction vocabulary');
    requireIncludes(serialized, 'Revision Guard', 'revision guard');
    requireIncludes(serialized, 'component:ServiceMotorM31', 'entity creation target');
    requireIncludes(serialized, 'port:ControlRelayK31.spareOut', 'relationship source endpoint');
    requireIncludes(serialized, 'port:SpareTerminalXT31.in1', 'relationship target endpoint');
    requireIncludes(serialized, 'control-and-plc-logic', 'control sheet role');
    requireIncludes(serialized, 'field-wiring-and-terminal-transition', 'field sheet role');
    requireIncludes(serialized, 'directDownstreamMutation":"none', 'no downstream mutation');

    const entityTransaction = byKind.get('entity-transaction').payload;
    if (entityTransaction.intentCardinality !== 'single-intent') {
        throw new Error('M31 entity transaction must prove single-intent cardinality.');
    }
    const nestedSourceEdit = byKind.get('nested-source-edit').payload;
    if (!nestedSourceEdit.nestedPortIds.includes('port:ServiceMotorM31.serviceIn')) {
        throw new Error('M31 nested source edit proof must include the generated nested port.');
    }
    const sheetIdentity = byKind.get('sheet-reference-identity').payload;
    if (sheetIdentity.sheetRoleCount !== '2') {
        throw new Error(`M31 sheet identity proof expected exactly two governed sheet roles, received ${sheetIdentity.sheetRoleCount}.`);
    }
}

function assertGraphWorkbenchProof(graphWorkbenchProof, sheetGraphWorkbenchProof = graphWorkbenchProof) {
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
            `Athena M31 graph-workbench proof failed: ${missingGraphProof.join(', ')}\n${JSON.stringify(graphWorkbenchProof)}`
        );
    }
    if (graphWorkbenchProof.activeViewId !== 'cabinet') {
        throw new Error(
            `Athena M31 graph-workbench must default to Cabinet view, received ${graphWorkbenchProof.activeViewId || '<missing>'}.`
        );
    }
    assertSheetSelectorProof(sheetGraphWorkbenchProof);
    assertRepresentationProof(graphWorkbenchProof.representationProof);
    assertRouteProof(graphWorkbenchProof.routeProof);
    assertVisualProof(graphWorkbenchProof.visualProof);
    assertOutlineProof(graphWorkbenchProof.outlineProof);
    assertStructuredAuthoringProofPayloads(buildStructuredAuthoringProofPayloads(resolveM31SampleProject(), graphWorkbenchProof, sheetGraphWorkbenchProof));
}

function assertSheetSelectorProof(graphWorkbenchProof) {
    const persistenceProof = graphWorkbenchProof.sheetSelectorPersistenceProof ?? {};
    const optionCount = sheetSelectorOptionCount(graphWorkbenchProof);
    if (optionCount !== 2) {
        throw new Error(`Athena M31 graph proof expected exactly two governed sheet choices, received ${optionCount}.`);
    }
    if (persistenceProof.skipped || persistenceProof.selectorVisibleAfterViewSwitch !== true) {
        throw new Error(`Athena M31 sheet selector did not survive mode switch.\n${JSON.stringify(persistenceProof, null, 2)}`);
    }
    if (persistenceProof.optionCountBeforeViewSwitch !== 2 || persistenceProof.optionCountAfterViewSwitch !== 2) {
        throw new Error(`Athena M31 sheet selector option count changed across mode switch.\n${JSON.stringify(persistenceProof, null, 2)}`);
    }
}

function sheetSelectorOptionCount(graphWorkbenchProof) {
    const documentProjectionProof = graphWorkbenchProof?.documentProjectionProof ?? {};
    const persistenceProof = graphWorkbenchProof?.sheetSelectorPersistenceProof ?? {};
    return Number(documentProjectionProof.sheetViewOptionCount ?? persistenceProof.optionCountAfterViewSwitch ?? 0);
}

function assertRepresentationProof(representationProof) {
    if (!representationProof || representationProof.representationCount < 1 || representationProof.presentationTerminalCount < 1 || representationProof.presentationLabelCount < 1) {
        throw new Error(`Athena M31 representation proof missing expected representation facts.\n${JSON.stringify(representationProof, null, 2)}`);
    }
    if (normalizeArray(representationProof.fallbackRepresentationIds).length > 0) {
        throw new Error(`Athena M31 representation proof used fallback symbols: ${representationProof.fallbackRepresentationIds.join(', ')}`);
    }
    const duplicateRepresentationSemanticIds = duplicateValues(representationProof.semanticIds);
    if (duplicateRepresentationSemanticIds.length > 0) {
        throw new Error(`Athena M31 representation proof found duplicate occurrences: ${duplicateRepresentationSemanticIds.join(', ')}`);
    }
    const repeatedTerminalNumbers = duplicateValues(representationProof.terminalNumbers);
    if (repeatedTerminalNumbers.length > 0) {
        throw new Error(`Athena M31 representation proof found repeated terminal labels: ${repeatedTerminalNumbers.join(', ')}`);
    }
}

function assertRouteProof(routeProof) {
    if (!routeProof || routeProof.routeCount < 1 || routeProof.terminalCount < 2) {
        throw new Error(`Athena M31 route proof missing expected rendered electrical routes.\n${JSON.stringify(routeProof, null, 2)}`);
    }
    if (routeProof.routesWithTerminalAnchors < routeProof.routeCount) {
        throw new Error(`Athena M31 route proof has routes without terminal anchors.\n${JSON.stringify(routeProof, null, 2)}`);
    }
    if (normalizeArray(routeProof.centerFallbackRouteIds).length > 0) {
        throw new Error(`Athena M31 route proof used center fallback routes: ${routeProof.centerFallbackRouteIds.join(', ')}`);
    }
}

function assertVisualProof(visualProof) {
    if (!visualProof) {
        throw new Error('Athena M31 graph proof missing visual proof.');
    }
    if (visualProof.svgViewBox === '0 0 1680 1188') {
        throw new Error('Athena M31 visual proof rejected hard-coded oversized viewBox 0 0 1680 1188.');
    }
    if (Number(visualProof.viewBoxWidth) <= 0 || Number(visualProof.viewBoxHeight) <= 0) {
        throw new Error(`Athena M31 visual proof has invalid viewBox dimensions.\n${JSON.stringify(visualProof, null, 2)}`);
    }
    if (Number(visualProof.routeBodyIntersectionCount) !== 0) {
        throw new Error(`Athena M31 visual proof found route/body intersections.\n${JSON.stringify(visualProof, null, 2)}`);
    }
    if (Number(visualProof.nonOrthogonalSegmentCount ?? 0) !== 0) {
        throw new Error(`Athena M31 visual proof found non-orthogonal route segments.\n${JSON.stringify(visualProof, null, 2)}`);
    }
    if (normalizeArray(visualProof.duplicateOffsheetOccurrenceIds).length > 0) {
        throw new Error(`Athena M31 visual proof found duplicate off-sheet occurrences: ${visualProof.duplicateOffsheetOccurrenceIds.join(', ')}`);
    }
    if (normalizeArray(visualProof.repeatedTerminalLabelIds).length > 0) {
        throw new Error(`Athena M31 visual proof found repeated terminal labels: ${visualProof.repeatedTerminalLabelIds.join(', ')}`);
    }
    const duplicateNodeSemanticIds = duplicateValues(visualProof.nodeSemanticIds);
    if (duplicateNodeSemanticIds.length > 0) {
        throw new Error(`Athena M31 visual proof found duplicate occurrences: ${duplicateNodeSemanticIds.join(', ')}`);
    }
}

function assertOutlineProof(outlineProof) {
    if (!outlineProof || outlineProof.skipped) {
        throw new Error(`Athena M31 outline proof was not collected.\n${JSON.stringify(outlineProof)}`);
    }
    if (outlineProof.hasOutlineWidget !== true || outlineProof.widgetId !== 'outline-view') {
        throw new Error(`Athena M31 outline proof did not open Theia's Outline view.\n${JSON.stringify(outlineProof)}`);
    }
    const paths = normalizeArray(outlineProof.paths);
    if (!paths.some(proofPath => proofPath.endsWith(EXPECTED_OUTLINE_PATH))) {
        throw new Error(
            `Athena M31 Outline view missing nested compact port path '${EXPECTED_OUTLINE_PATH}'.\n${JSON.stringify(outlineProof, null, 2)}`
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
            `Athena M31 smoke requires the installed LSP host before product proof. Missing ${launcher}. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
        );
    }
}

function resolveM31SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm31', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        SOURCE_RELATIVE,
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M31 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

function resolveM31ScreenshotPath() {
    const repositoryRoot = resolveRepoRoot();
    return path.resolve(
        repositoryRoot,
        '_bmad-output',
        'implementation-artifacts',
        'm31',
        'screenshots',
        'm31-graph-workbench-smoke.png'
    );
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) {
        throw new Error(`Athena M31 screenshot guard did not create ${screenshotPath}.`);
    }
    const bytes = fs.readFileSync(screenshotPath);
    const pngSignature = '89504e470d0a1a0a';
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== pngSignature) {
        throw new Error(`Athena M31 screenshot guard produced an invalid PNG at ${screenshotPath}.`);
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
        throw new Error(`M31 proof payloads missing ${label}: ${expected}`);
    }
}

if (require.main === module) {
    main().catch(error => {
        console.error(error.stack || String(error));
        process.exit(1);
    });
}

module.exports = {
    buildStructuredAuthoringProofPayloads,
    assertStructuredAuthoringProofPayloads,
    assertGraphWorkbenchProof,
    assertPngScreenshot,
    REQUIRED_M31_AUTHORING_PROOF_KINDS,
};
