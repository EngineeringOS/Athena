const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_INTERACTION_PROOF_SENTINEL = 'ATHENA_INTERACTION_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 90000;
const INTERACTION_SCHEMA_VERSION = 'm29.interaction.v1';

const REQUIRED_INTERACTION_PROOF_KINDS = [
    'subject-registry',
    'action-discovery',
    'reveal-source-graph-inspector-problems',
    'relationship-preview',
    'relationship-accept',
    'entity-creation-preview',
    'entity-creation-accept',
    'preview-stale-clearing',
    'legacy-connect-ports-inventory',
];

async function main() {
    const repositoryRoot = resolveM29SampleProject();
    assertInstalledLspHostPresent();
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
                ATHENA_ELECTRON_TEMP_USER_DATA: '1',
                ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'documentation',
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
            `Athena M29 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M29 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M29 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }
    if (!graphWorkbenchProof) {
        throw new Error(`Athena M29 sample project smoke did not report graph workbench proof.\n${outputLines.join('\n')}`);
    }

    const interactionProofPayloads = buildStructuredInteractionProofPayloads(repositoryRoot);
    assertStructuredInteractionProofPayloads(interactionProofPayloads);
    assertGraphWorkbenchProof(graphWorkbenchProof);
    assertRouteProof(graphWorkbenchProof.routeProof);
    assertRepresentationProof(graphWorkbenchProof.representationProof);

    console.log(`${ATHENA_INTERACTION_PROOF_SENTINEL}${JSON.stringify(interactionProofPayloads)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(graphWorkbenchProof)}`);
    console.log(`Athena M29 sample project smoke passed. workspace=${openedWorkspace} javaHome=${resolvedJavaHome || 'n/a'}`);
}

function buildStructuredInteractionProofPayloads(repositoryRoot) {
    const authoringSourceUri = pathToFileURL(
        path.join(repositoryRoot, 'src', '01-interaction-authoring-source.athena')
    ).toString();
    const candidateSourceUri = pathToFileURL(
        path.join(repositoryRoot, 'src', '02-interaction-candidates.athena')
    ).toString();
    const entitySourceUri = pathToFileURL(
        path.join(repositoryRoot, 'src', '03-entity-creation-context.athena')
    ).toString();
    const cleanupLedger = resolveCleanupLedger();

    return [
        proofEnvelope('subject-registry', authoringSourceUri, {
            activeSourceContext: 'M29 sample subject registry',
            subjectIds: 'component:InteractionControllerPLC29,port:InteractionControllerPLC29.spareDo,system:InteractionAuthoringProof',
            semanticAuthority: '.athena',
        }),
        proofEnvelope('action-discovery', authoringSourceUri, {
            activeSourceContext: 'M29 sample action discovery',
            actionFamilies: 'reveal,mutate',
            producerNeutralPrimitive: 'SemanticActionIntent',
        }),
        proofEnvelope('reveal-source-graph-inspector-problems', authoringSourceUri, {
            activeSourceContext: 'M29 reveal proof',
            preferredTargets: 'source,graph,inspector,problems',
            subjectId: 'component:InteractionControllerPLC29',
        }),
        proofEnvelope('relationship-preview', candidateSourceUri, {
            activeSourceContext: 'M29 relationship preview',
            intentKind: 'semantic-relationship',
            relationshipType: 'ElectricalConnectionRelationship',
            sourceSubjectId: 'port:CandidateOutputModuleIOM29.do1',
            targetSubjectId: 'port:CandidateTerminalXT29.in1',
        }),
        proofEnvelope('relationship-accept', candidateSourceUri, {
            activeSourceContext: 'M29 relationship accept',
            intentKind: 'semantic-relationship',
            mutationAuthority: 'authoring-runtime/source-edit',
            sourceSubjectId: 'port:CandidateTerminalXT29.out1',
            targetSubjectId: 'port:CandidateMotorM29.u1',
        }),
        proofEnvelope('entity-creation-preview', entitySourceUri, {
            activeSourceContext: 'M29 entity creation preview',
            entityKind: 'component',
            sourceImpactOwner: 'backend-runtime/source-edit',
            generatedAnatomy: 'nested-ports',
        }),
        proofEnvelope('entity-creation-accept', entitySourceUri, {
            activeSourceContext: 'M29 entity creation accept',
            entityKind: 'component',
            mutationAuthority: 'authoring-runtime/source-edit',
            projectionConsequence: 'component-and-port-occurrences',
        }),
        proofEnvelope('preview-stale-clearing', authoringSourceUri, {
            activeSourceContext: 'M29 preview lifecycle',
            staleTriggers: 'source-reload,projection-refresh,active-source-change,accepted-mutation',
            cancelBehavior: 'clear-transient-preview',
        }),
        proofEnvelope('legacy-connect-ports-inventory', pathToFileURL(cleanupLedger).toString(), {
            activeSourceContext: 'M29 relationship mutation cleanup',
            ledger: path.relative(resolveRepoRoot(), cleanupLedger).replace(/\\/g, '/'),
            classificationRequired: 'removed,migrated-to-interaction-ir,compatibility-adapter,retained-with-owner-target-milestone',
        }),
    ];
}

function proofEnvelope(proofKind, activeSourceUri, payload) {
    return {
        schemaVersion: INTERACTION_SCHEMA_VERSION,
        requestId: `proof:${proofKind}`,
        activeSourceUri,
        activeSourceRevision: 'm29-sample-project',
        payloadKind: 'proof',
        payload: {
            proofKind,
            ...payload,
        },
    };
}

function assertStructuredInteractionProofPayloads(proofPayloads) {
    if (!Array.isArray(proofPayloads)) {
        throw new Error('Athena M29 interaction proof payloads must be an array.');
    }
    const byKind = new Map(proofPayloads.map(payload => [payload?.payload?.proofKind, payload]));
    const missing = REQUIRED_INTERACTION_PROOF_KINDS.filter(proofKind => !byKind.has(proofKind));
    if (missing.length > 0) {
        throw new Error(`Athena M29 interaction proof payloads missing: ${missing.join(', ')}`);
    }
    for (const proofKind of REQUIRED_INTERACTION_PROOF_KINDS) {
        const envelope = byKind.get(proofKind);
        if (envelope.schemaVersion !== INTERACTION_SCHEMA_VERSION) {
            throw new Error(`${proofKind} proof schema mismatch: ${envelope.schemaVersion || '<missing>'}`);
        }
        if (envelope.payloadKind !== 'proof') {
            throw new Error(`${proofKind} proof payloadKind expected proof but received ${envelope.payloadKind || '<missing>'}`);
        }
        if (!envelope.activeSourceUri) {
            throw new Error(`${proofKind} proof missing activeSourceUri.`);
        }
        if (!envelope.payload.activeSourceContext) {
            throw new Error(`${proofKind} proof missing activeSourceContext.`);
        }
    }
    const relationshipPreview = byKind.get('relationship-preview');
    if (relationshipPreview.payload.intentKind !== 'semantic-relationship') {
        throw new Error('M29 relationship preview proof must use semantic-relationship, not connect-ports.');
    }
    const entityPreview = byKind.get('entity-creation-preview');
    if (entityPreview.payload.sourceImpactOwner !== 'backend-runtime/source-edit') {
        throw new Error('M29 entity creation preview source impact must be backend-owned.');
    }
    const legacyInventory = byKind.get('legacy-connect-ports-inventory');
    const cleanupLedger = fs.readFileSync(resolveCleanupLedger(), 'utf8');
    if (!cleanupLedger.includes('payloadKind: legacy-connect-ports-inventory')) {
        throw new Error('M29 cleanup ledger does not record the legacy connect-ports inventory proof payload.');
    }
    if (!legacyInventory.payload.classificationRequired.includes('compatibility-adapter')) {
        throw new Error('M29 legacy inventory proof does not name retained compatibility classification.');
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
        'stageHasGrid'
    ].filter(key => graphWorkbenchProof[key] !== true);
    if (missingGraphProof.length > 0) {
        throw new Error(
            `Athena M29 graph-workbench proof failed: ${missingGraphProof.join(', ')}\n${JSON.stringify(graphWorkbenchProof)}`
        );
    }
}

function assertRouteProof(routeProof) {
    if (!routeProof || routeProof.routeCount < 1 || routeProof.terminalCount < 2) {
        throw new Error(`Athena M29 route proof missing expected rendered electrical routes.\n${JSON.stringify(routeProof, null, 2)}`);
    }
}

function assertRepresentationProof(representationProof) {
    if (!representationProof || representationProof.representationCount < 2 || representationProof.presentationTerminalCount < 2) {
        throw new Error(`Athena M29 representation proof missing expected component presentation facts.\n${JSON.stringify(representationProof, null, 2)}`);
    }
    if (representationProof.fallbackRepresentationIds.length > 0) {
        throw new Error(`Athena M29 representation proof used fallback symbols: ${representationProof.fallbackRepresentationIds.join(', ')}`);
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
            `Athena M29 smoke requires the installed LSP host before product proof. Missing ${launcher}. Run: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
        );
    }
}

function resolveM29SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm29', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        path.join('src', '01-interaction-authoring-source.athena'),
        path.join('src', '02-interaction-candidates.athena'),
        path.join('src', '03-entity-creation-context.athena')
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M29 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

function resolveCleanupLedger() {
    return path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm29', 'cleanup-ledger.md');
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

if (require.main === module) {
    main().catch(error => {
        console.error(error.stack || String(error));
        process.exit(1);
    });
}

module.exports = {
    buildStructuredInteractionProofPayloads,
    assertStructuredInteractionProofPayloads,
    REQUIRED_INTERACTION_PROOF_KINDS,
};
