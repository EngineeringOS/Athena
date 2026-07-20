const path = require('node:path');
const fs = require('node:fs');
const os = require('node:os');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED';
const SHOULD_EXIT_ON_WORKSPACE_OPEN = process.env.ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN === '1';
const SMOKE_ACTIVE_VIEW = process.env.ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW || '';

const targetWorkspace = process.argv[2] ? path.resolve(process.cwd(), process.argv[2]) : undefined;
if (process.env.ATHENA_ELECTRON_TEMP_USER_DATA === '1') {
    app.setPath('userData', fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m21-smoke-')));
}

function main() {
    const runtimeResolution = configureJvmRuntime();
    if (runtimeResolution.status === 'ready') {
        console.log(`${ATHENA_JAVA_SENTINEL}=${runtimeResolution.javaHome}`);
    } else if (process.platform === 'win32') {
        console.warn(`${ATHENA_JAVA_UNRESOLVED_SENTINEL}=${runtimeResolution.message}`);
    }

    app.on('browser-window-created', (_event, window) => {
        console.log(ATHENA_WINDOW_CREATED_SENTINEL);
        window.webContents.once('did-finish-load', () => {
            console.log(ATHENA_READY_SENTINEL);
            void openWorkspace(window).catch(error => {
                console.error(`${ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL}${error.stack || String(error)}`);
                if (SHOULD_EXIT_ON_WORKSPACE_OPEN) {
                    setTimeout(() => app.exit(1), 50);
                }
            });
        });
    });

    require('../lib/backend/electron-main.js');
}

async function openWorkspace(window) {
    if (!targetWorkspace) {
        throw new Error('No target workspace path was provided.');
    }
    const proof = await window.webContents.executeJavaScript(`
        (async () => {
            const waitFor = async (predicate, description, timeoutMs = 60000, intervalMs = 100) => {
                const startedAt = Date.now();
                while (Date.now() - startedAt < timeoutMs) {
                    const result = await predicate();
                    if (result) {
                        return result;
                    }
                    await new Promise(resolve => setTimeout(resolve, intervalMs));
                }
                throw new Error('Timed out waiting for ' + description);
            };
            const normalizePath = value => {
                const raw = String(value || '');
                let decoded = raw;
                try {
                    decoded = decodeURIComponent(raw);
                } catch (_error) {
                    decoded = decodeURI(raw);
                }
                const normalized = decoded
                    .replace(/^#/, '')
                    .replace(/^\\//, '')
                    .replace(/\\\\/g, '/')
                    .replace(/^\\/([A-Za-z]:\\/)/, '$1')
                    .replace(/\\/+$/, '')
                    .toLowerCase();
                return normalized;
            };
            const requireElement = async (selector, description) => {
                return waitFor(() => document.querySelector(selector), description);
            };
            const isTransparent = element => {
                const color = window.getComputedStyle(element).backgroundColor;
                return color === 'transparent' || color === 'rgba(0, 0, 0, 0)';
            };

            await waitFor(() => window.theia?.container, 'theia container');
            const target = ${JSON.stringify(targetWorkspace)};
            const normalizedTarget = normalizePath(target);
            await waitFor(() => {
                const normalizedHash = normalizePath(window.location.hash);
                return normalizedHash === normalizedTarget ? target : undefined;
            }, 'target workspace URL fragment');

            if (typeof require === 'function') {
                const { CommandRegistry } = require('@theia/core/lib/common/command');
                const commandRegistry = window.theia.container.get(CommandRegistry);
                await commandRegistry.executeCommand('athena.revealGraphicalView');
            } else {
                const graphicalViewButton = await waitFor(() => {
                    return Array.from(document.querySelectorAll('button'))
                        .find(button => (button.textContent || '').trim() === 'Graphical View');
                }, 'Graphical View quick action');
                graphicalViewButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
            }

            const workbench = await requireElement('.athena-graph-workbench', 'graph workbench root');
            const stage = await requireElement('.athena-graph-workbench__stage', 'graph workbench stage');
            const smokeActiveView = ${JSON.stringify(SMOKE_ACTIVE_VIEW)};
            if (smokeActiveView) {
                const viewButton = await requireElement(
                    '[data-athena-projection-view-id="' + smokeActiveView + '"]',
                    'projection view button ' + smokeActiveView
                );
                if (!viewButton.disabled) {
                    viewButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                }
                await waitFor(() => {
                    const activeButton = document.querySelector('[data-athena-projection-view-id="' + smokeActiveView + '"]');
                    return activeButton?.disabled ? activeButton : undefined;
                }, 'active projection view ' + smokeActiveView);
            }
            const viewport = await waitFor(() => {
                const element = document.querySelector('.athena-graph-workbench__viewport');
                if (element) {
                    return element;
                }
                const empty = document.querySelector('.athena-graph-workbench__empty');
                if (empty) {
                    const text = (empty.textContent || '').replace(/\\s+/g, ' ').trim();
                    if (text.includes('Projection unavailable') || text.includes('Projection is empty')) {
                        throw new Error('Graph workbench rendered empty state before viewport: ' + text);
                    }
                }
                return undefined;
            }, 'graph workbench viewport');
            const sheet = await requireElement('.athena-graph-workbench__sheet', 'graph workbench sheet');
            const canvas = await requireElement('.athena-graph-workbench__canvas', 'graph workbench canvas');
            const floatingBar = await requireElement('.athena-graph-workbench__floating-bar', 'graph workbench floating bar');
            const bottomDock = await requireElement('.athena-graph-workbench__bottom-dock', 'graph workbench bottom dock');
            const zoomDock = await requireElement('.athena-graph-workbench__zoom-dock', 'graph workbench zoom dock');
            const sheetFrame = await requireElement('.athena-graph-workbench__sheet-frame', 'graph workbench sheet frame');
            const infoButton = await requireElement('[data-athena-info-button="true"]', 'graph workbench info button');
            const sheetViewSelector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
            const referenceMarkerButtons = Array.from(document.querySelectorAll('[data-athena-reference-marker="true"]'));

            infoButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
            const infoPopover = await requireElement('[data-athena-info-popover="true"]', 'graph workbench info popover');
            const popoverText = infoPopover.textContent || '';
            stage.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
            await waitFor(() => !document.querySelector('[data-athena-info-popover="true"]'), 'graph workbench info popover close');

            return {
                workspace: target,
                graphWorkbench: {
                    root: !!workbench,
                    stage: !!stage,
                    viewport: !!viewport,
                    sheet: !!sheet,
                    canvas: !!canvas,
                    floatingBarTransparent: isTransparent(floatingBar),
                    bottomDockTransparent: isTransparent(bottomDock),
                    zoomDockTransparent: isTransparent(zoomDock),
                    sheetTransparent: isTransparent(sheet),
                    sheetFrame: !!sheetFrame,
                    stageHasGrid: window.getComputedStyle(stage).backgroundImage.includes('linear-gradient'),
                    infoPopoverOpened: popoverText.includes('Cabinet Main'),
                    infoPopoverClosedOnWhitespace: !document.querySelector('[data-athena-info-popover="true"]'),
                    documentProjectionProof: collectDocumentProjectionProof(sheetViewSelector, referenceMarkerButtons),
                    routeProof: collectRouteProof(),
                    representationProof: collectRepresentationProof()
                }
            };

            function collectDocumentProjectionProof(sheetViewSelector, referenceMarkerButtons) {
                const options = sheetViewSelector
                    ? Array.from(sheetViewSelector.options).map(option => ({
                        value: option.value,
                        text: option.textContent || '',
                        selected: option.selected
                    }))
                    : [];
                return {
                    hasSheetViewSelector: !!sheetViewSelector,
                    sheetViewOptionCount: options.length,
                    sheetViewOptionTexts: options.map(option => option.text),
                    selectedSheetViewId: options.find(option => option.selected)?.value || '',
                    compactReferenceMarkerCount: referenceMarkerButtons.length,
                    compactReferenceMarkerNotations: referenceMarkerButtons
                        .map(button => (button.textContent || '').replace(/\\s+/g, ' ').trim())
                        .filter(Boolean),
                    markerTargetIds: referenceMarkerButtons
                        .map(button => button.getAttribute('data-athena-reference-marker-id') || '')
                        .filter(Boolean)
                };
            }

            function collectRouteProof() {
                const routes = Array.from(document.querySelectorAll('[data-athena-route-fact="true"]'));
                const terminals = Array.from(document.querySelectorAll('[data-athena-route-terminal="true"]'));
                const labels = Array.from(document.querySelectorAll('[data-athena-route-label="true"]'));
                const routeStates = routes.map(route => {
                    const pointCount = Number(route.getAttribute('data-athena-route-point-count') || '0');
                    const sourceAnchorId = route.getAttribute('data-athena-route-source-anchor-id') || '';
                    const targetAnchorId = route.getAttribute('data-athena-route-target-anchor-id') || '';
                    return {
                        routeId: route.getAttribute('data-athena-route-id') || '',
                        semanticId: route.getAttribute('data-athena-route-semantic-id') || '',
                        pointCount,
                        sourceAnchorId,
                        targetAnchorId,
                        quality: route.getAttribute('data-athena-route-quality') || '',
                        hasTerminalAnchors: !!sourceAnchorId && !!targetAnchorId,
                        hasOrthogonalBends: pointCount >= 4
                    };
                });
                return {
                    routeCount: routes.length,
                    terminalCount: terminals.length,
                    labelCount: labels.length,
                    visibleLabelTexts: labels
                        .filter(label => label.getAttribute('data-athena-route-label-display') !== 'selection')
                        .map(label => (label.textContent || '').replace(/\\s+/g, ' ').trim())
                        .filter(Boolean),
                    routesWithTerminalAnchors: routeStates.filter(route => route.hasTerminalAnchors).length,
                    routesWithOrthogonalBends: routeStates.filter(route => route.hasOrthogonalBends).length,
                    centerFallbackRouteIds: routeStates
                        .filter(route => !route.hasTerminalAnchors || route.pointCount <= 2)
                        .map(route => route.routeId || route.semanticId || '<unknown>'),
                    routeStates
                };
            }

            function collectRepresentationProof() {
                const representations = Array.from(document.querySelectorAll('[data-athena-representation-fact="true"]'));
                const terminals = Array.from(document.querySelectorAll('[data-athena-presentation-terminal="true"]'));
                const labels = Array.from(document.querySelectorAll('[data-athena-presentation-label="true"]'));
                const representationStates = representations.map(representation => ({
                    representationId: representation.getAttribute('data-athena-representation-id') || '',
                    fallback: representation.getAttribute('data-athena-render-fallback') === 'true',
                    semanticId: representation.getAttribute('data-athena-semantic-id') || ''
                }));
                return {
                    representationCount: representations.length,
                    presentationTerminalCount: terminals.length,
                    presentationLabelCount: labels.length,
                    terminalNumbers: terminals
                        .map(terminal => terminal.getAttribute('data-athena-presentation-terminal-number') || '')
                        .filter(Boolean),
                    labelRoles: labels
                        .map(label => label.getAttribute('data-athena-presentation-label-role') || '')
                        .filter(Boolean),
                    representationIds: representationStates.map(representation => representation.representationId).filter(Boolean),
                    semanticIds: representationStates.map(representation => representation.semanticId).filter(Boolean),
                    fallbackRepresentationIds: representationStates
                        .filter(representation => representation.fallback)
                        .map(representation => representation.representationId || representation.semanticId || '<unknown>'),
                    representationStates
                };
            }
        })();
    `, true);

    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(proof.graphWorkbench)}`);
    console.log(`${ATHENA_WORKSPACE_OPENED_SENTINEL}${proof.workspace}`);
    if (SHOULD_EXIT_ON_WORKSPACE_OPEN) {
        setTimeout(() => app.exit(0), 250);
    }
}

function configureJvmRuntime() {
    const resolver = new AthenaJvmRuntimeResolver();
    return resolver.configureProcessEnvironment(process.env, process.platform);
}

main();
