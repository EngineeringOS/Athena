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
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_SMOKE_STEP_SENTINEL = 'ATHENA_SMOKE_STEP=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED';
const SHOULD_EXIT_ON_WORKSPACE_OPEN = process.env.ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN === '1';
const REQUESTED_ACTIVE_VIEW = resolveRequestedActiveView();
const GRAPH_VIEW_SCREENSHOT_PATH = process.env.ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT || '';
const CAPTURE_CREATE_ENTITY_PANEL = process.env.ATHENA_ELECTRON_SMOKE_CAPTURE_CREATE_ENTITY_PANEL === '1';
const SKIP_SMOKE_OUTLINE = process.env.ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE === '1';
const SMOKE_CREATE_ENTITY_TAG = process.env.ATHENA_ELECTRON_SMOKE_CREATE_ENTITY_TAG || '';
const SMOKE_EXPECT_SEMANTIC_ID = process.env.ATHENA_ELECTRON_SMOKE_EXPECT_SEMANTIC_ID || '';
const COLLECT_EDITOR_SYNTAX_COLOR_PROOF = process.env.ATHENA_ELECTRON_SMOKE_EDITOR_SYNTAX_COLORS === '1';
const SMOKE_OUTLINE_SOURCE_RELATIVE = process.env.ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE
    || 'src/01-interaction-authoring-source.athena';
const SMOKE_OUTLINE_EXPECTED_PATH = process.env.ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH
    || 'InteractionAuthoringProof > OperatorHMI1 > status';

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
        if (SHOULD_EXIT_ON_WORKSPACE_OPEN) {
            window.setSize(1920, 1080, false);
            window.center();
        }
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
            const smokeStep = step => {
                console.log(${JSON.stringify(ATHENA_SMOKE_STEP_SENTINEL)} + step);
            };
            const visibleElement = element => {
                if (!element) {
                    return false;
                }
                const rect = element.getBoundingClientRect();
                return rect.width > 0 && rect.height > 0;
            };
            const commandText = element => {
                return [
                    element.textContent || '',
                    element.getAttribute?.('aria-label') || '',
                    element.getAttribute?.('title') || ''
                ].join(' ').replace(/\\s+/g, ' ').trim();
            };
            const isTransparent = element => {
                const color = window.getComputedStyle(element).backgroundColor;
                return color === 'transparent' || color === 'rgba(0, 0, 0, 0)';
            };

            await waitFor(() => window.theia?.container, 'theia container');
            smokeStep('theia-container');
            const target = ${JSON.stringify(targetWorkspace)};
            const normalizedTarget = normalizePath(target);
            await waitFor(() => {
                const normalizedHash = normalizePath(window.location.hash);
                return normalizedHash === normalizedTarget ? target : undefined;
            }, 'target workspace URL fragment');
            smokeStep('workspace-fragment');

            const electronRequire = await waitFor(
                () => typeof require === 'function' ? require : undefined,
                'Electron renderer require for Graphical View command',
                10000
            ).catch(() => undefined);
            const athenaWorkbenchSmoke = await waitFor(
                () => window.__athenaWorkbenchSmoke?.revealGraphicalView,
                'Athena workbench smoke command hook',
                60000
            ).catch(() => undefined);
            const outlineProof = ${JSON.stringify(SKIP_SMOKE_OUTLINE)}
                ? { skipped: true, reason: 'ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE=1' }
                : await collectOutlineProof(target);
            smokeStep('outline-proof');
            smokeStep('graph-reveal-start');
            if (athenaWorkbenchSmoke) {
                await Promise.race([
                    athenaWorkbenchSmoke(),
                    new Promise((_, reject) => setTimeout(
                        () => reject(new Error('Timed out waiting for Athena Graphical View smoke command hook')),
                        10000
                    ))
                ]).catch(() => revealGraphicalViewThroughDom());
            } else if (electronRequire) {
                const { CommandRegistry } = electronRequire('@theia/core/lib/common/command');
                const commandRegistry = window.theia.container.get(CommandRegistry);
                await Promise.race([
                    commandRegistry.executeCommand('athena.revealGraphicalView'),
                    new Promise((_, reject) => setTimeout(
                        () => reject(new Error('Timed out waiting for Graphical View command registry execution')),
                        10000
                    ))
                ]).catch(() => revealGraphicalViewThroughDom());
            } else {
                await revealGraphicalViewThroughDom();
            }
            smokeStep('graph-reveal-end');

            const workbench = await requireElement('.athena-graph-workbench', 'graph workbench root');
            smokeStep('graph-workbench-root');
            const stage = await requireElement('.athena-graph-workbench__stage', 'graph workbench stage');
            const smokeActiveView = ${JSON.stringify(REQUESTED_ACTIVE_VIEW)};
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
            smokeStep('graph-workbench-viewport');
            const sheet = await requireElement('.athena-graph-workbench__sheet', 'graph workbench sheet');
            smokeStep('graph-workbench-sheet');
            const canvas = await requireElement('.athena-graph-workbench__canvas', 'graph workbench canvas');
            smokeStep('graph-workbench-canvas');
            const floatingBar = await requireElement('.athena-graph-workbench__floating-bar', 'graph workbench floating bar');
            const bottomDock = await requireElement('.athena-graph-workbench__bottom-dock', 'graph workbench bottom dock');
            const zoomDock = await requireElement('.athena-graph-workbench__zoom-dock', 'graph workbench zoom dock');
            const sheetFrame = await requireElement('.athena-graph-workbench__sheet-frame', 'graph workbench sheet frame');
            smokeStep('graph-workbench-chrome');
            const infoButton = await requireElement('[data-athena-info-button="true"]', 'graph workbench info button');
            const createEntityButton = await requireElement('[data-athena-create-entity-button="true"]', 'graph workbench create entity button');
            const referenceMarkerButtons = Array.from(document.querySelectorAll('[data-athena-reference-marker="true"]'));
            const projectionViewProof = collectProjectionViewProof();
            smokeStep('graph-workbench-controls');

            infoButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
            const infoPopover = await requireElement('[data-athena-info-popover="true"]', 'graph workbench info popover');
            const popoverText = infoPopover.textContent || '';
            stage.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
            await waitFor(() => !document.querySelector('[data-athena-info-popover="true"]'), 'graph workbench info popover close');
            smokeStep('graph-workbench-info-popover');
            const captureCreateEntityPanel = ${JSON.stringify(CAPTURE_CREATE_ENTITY_PANEL)};
            const createEntityPanelProof = await collectCreateEntityPanelProof(createEntityButton, captureCreateEntityPanel);
            smokeStep('graph-workbench-create-panel');
            if (captureCreateEntityPanel) {
                return {
                    workspace: target,
                    graphWorkbench: {
                        root: !!workbench,
                        stage: !!stage,
                        viewport: !!viewport,
                        sheet: !!sheet,
                        canvas: !!canvas,
                        activeViewId: collectActiveProjectionViewId(),
                        createEntityPanelProof
                    }
                };
            }
            const expectedSemanticProof = await collectExpectedSemanticProof();
            const documentProjectionProof = await collectWithSmokeStep(
                'documentation-compatibility-proof',
                collectDocumentationCompatibilityProof
            );

            return {
                workspace: target,
                graphWorkbench: {
                    root: !!workbench,
                    stage: !!stage,
                    viewport: !!viewport,
                    sheet: !!sheet,
                    canvas: !!canvas,
                    activeViewId: collectActiveProjectionViewId(),
                    floatingBarTransparent: isTransparent(floatingBar),
                    bottomDockTransparent: isTransparent(bottomDock),
                    zoomDockTransparent: isTransparent(zoomDock),
                    sheetTransparent: isTransparent(sheet),
                    sheetFrame: !!sheetFrame,
                    sheetChromeVisualProof: collectSheetChromeVisualProof(sheet, sheetFrame),
                    stageHasGrid: window.getComputedStyle(stage).backgroundImage.includes('linear-gradient'),
                    infoPopoverOpened: popoverText.includes('Projection Information'),
                    infoPopoverClosedOnWhitespace: !document.querySelector('[data-athena-info-popover="true"]'),
                    projectionViewProof,
                    createEntityPanelProof,
                    expectedSemanticProof,
                    documentProjectionProof,
                    sheetSurfaceProof: collectSheetSurfaceProof(sheetFrame),
                    densityProof: collectDensityProof(referenceMarkerButtons),
                    routeProof: collectRouteProof(),
                    representationProof: collectRepresentationProof(),
                    visualProof: collectVisualProof(),
                    editorSyntaxColorProof: await collectWithSmokeStep('editor-syntax-color-proof', () => collectEditorSyntaxColorProof(target)),
                    outlineProof
                }
            };

            async function collectWithSmokeStep(step, collector) {
                smokeStep(step + '-start');
                const result = await collector();
                smokeStep(step + '-end');
                return result;
            }

            function collectProjectionViewProof() {
                const viewSwitches = document.querySelector('.athena-graph-workbench__view-switches');
                const visibleButtons = Array.from(document.querySelectorAll('[data-athena-projection-view-id]'));
                return {
                    visibleViewIds: visibleButtons
                        .map(button => button.getAttribute('data-athena-projection-view-id') || '')
                        .filter(Boolean),
                    visibleViewButtonCount: visibleButtons.length,
                    activeViewIds: visibleButtons
                        .filter(button => button.disabled)
                        .map(button => button.getAttribute('data-athena-projection-view-id') || '')
                        .filter(Boolean),
                    compatibilityViewCount: Number(viewSwitches?.getAttribute('data-athena-compatibility-projection-view-count') || 0),
                    visibleViewCountAttribute: Number(viewSwitches?.getAttribute('data-athena-visible-projection-view-count') || 0)
                };
            }

            async function collectCreateEntityPanelProof(createEntityButton, keepOpen = false) {
                const buttonWasDisabled = !!createEntityButton.disabled;
                if (!buttonWasDisabled) {
                    createEntityButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                }
                const panel = await requireElement('[data-athena-create-entity-panel="true"]', 'graph workbench create entity panel');
                const panelRect = roundedClientRect(panel);
                const panelText = (panel.textContent || '').replace(/\\s+/g, ' ').trim();
                const conceptSelect = panel.querySelector('select');
                const tagInput = panel.querySelector('input[placeholder="ShutterMotorM31"]');
                const modelInput = panel.querySelector('input[placeholder="SPARE-XT"]');
                const previewButton = panel.querySelector('button[aria-label="Preview device creation"]');
                const closeButton = panel.querySelector('button[aria-label="Close create entity controls"]');
                const panelCenterX = panelRect.left + panelRect.width / 2;
                const panelCenterY = panelRect.top + panelRect.height / 2;
                const frontmostElement = document.elementFromPoint(panelCenterX, panelCenterY);
                const preloadOverlay = document.querySelector('.theia-preload');
                const preloadRect = preloadOverlay ? roundedClientRect(preloadOverlay) : undefined;
                const preloadStyle = preloadOverlay ? window.getComputedStyle(preloadOverlay) : undefined;
                const reachableControls = [conceptSelect, tagInput, modelInput, previewButton, closeButton]
                    .filter(Boolean)
                    .map(control => roundedClientRect(control));
                const proof = {
                    buttonPresent: !!createEntityButton,
                    buttonDisabledBeforeClick: buttonWasDisabled,
                    opened: visibleElement(panel),
                    panelWidth: panelRect.width,
                    panelHeight: panelRect.height,
                    panelLeft: panelRect.left,
                    panelTop: panelRect.top,
                    panelRight: panelRect.right,
                    panelBottom: panelRect.bottom,
                    viewportWidth: window.innerWidth,
                    viewportHeight: window.innerHeight,
                    withinViewport: panelRect.left >= 0 &&
                        panelRect.top >= 0 &&
                        panelRect.right <= window.innerWidth &&
                        panelRect.bottom <= window.innerHeight,
                    frontmostAtCenter: !!frontmostElement && (frontmostElement === panel || panel.contains(frontmostElement)),
                    centerHitTagName: frontmostElement?.tagName || '',
                    centerHitClassName: String(frontmostElement?.className || ''),
                    preloadOverlayPresent: !!preloadOverlay,
                    preloadOverlayDisplay: preloadStyle?.display || '',
                    preloadOverlayVisibility: preloadStyle?.visibility || '',
                    preloadOverlayOpacity: preloadStyle?.opacity || '',
                    preloadOverlayPointerEvents: preloadStyle?.pointerEvents || '',
                    preloadOverlayZIndex: preloadStyle?.zIndex || '',
                    preloadOverlayLeft: preloadRect?.left ?? 0,
                    preloadOverlayTop: preloadRect?.top ?? 0,
                    preloadOverlayRight: preloadRect?.right ?? 0,
                    preloadOverlayBottom: preloadRect?.bottom ?? 0,
                    preloadOverlayContainsCenter: !!preloadRect &&
                        panelCenterX >= preloadRect.left &&
                        panelCenterX <= preloadRect.right &&
                        panelCenterY >= preloadRect.top &&
                        panelCenterY <= preloadRect.bottom,
                    reachableControlCount: reachableControls.filter(rect => rect.width > 0 && rect.height > 0).length,
                    hasConceptSelect: !!conceptSelect,
                    hasTagInput: !!tagInput,
                    hasModelInput: !!modelInput,
                    previewButtonPresent: !!previewButton,
                    previewButtonDisabled: !!previewButton?.disabled,
                    textIncludesCreateEntity: panelText.includes('Create Device'),
                    textIncludesSourceEditorGuidance: panelText.includes('Open an Athena source editor before previewing a source-backed create action.'),
                };
                const createEntityTag = ${JSON.stringify(SMOKE_CREATE_ENTITY_TAG)};
                if (createEntityTag) {
                    const graphFirstAuthoringProof = await createEntityFromGraphPanel(createEntityTag, {
                        conceptSelect,
                        tagInput,
                        modelInput,
                        previewButton,
                    });
                    return {
                        ...proof,
                        closed: !document.querySelector('[data-athena-create-entity-panel="true"]'),
                        graphFirstAuthoringProof,
                    };
                }
                if (keepOpen) {
                    return {
                        ...proof,
                        closed: false
                    };
                }
                closeButton?.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                await waitFor(
                    () => !document.querySelector('[data-athena-create-entity-panel="true"]'),
                    'graph workbench create entity panel close'
                );
                return {
                    ...proof,
                    closed: !document.querySelector('[data-athena-create-entity-panel="true"]')
                };
            }

            async function createEntityFromGraphPanel(createEntityTag, controls) {
                const { conceptSelect, tagInput, modelInput, previewButton } = controls;
                if (!conceptSelect || !tagInput || !modelInput || !previewButton) {
                    throw new Error('Graph-first create proof requires all Create Device controls.');
                }
                const motorOption = Array.from(conceptSelect.options).find(option =>
                    /motor/i.test(option.value) || /motor/i.test(option.textContent || '')
                );
                if (motorOption) {
                    conceptSelect.value = motorOption.value;
                    conceptSelect.dispatchEvent(new Event('change', { bubbles: true }));
                    await new Promise(resolve => requestAnimationFrame(resolve));
                }
                const currentTagInput = await waitFor(
                    () => document.querySelector('[data-athena-create-entity-panel="true"] input[placeholder="ShutterMotorM31"]'),
                    'graph-first create tag input'
                );
                const valueSetter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set;
                valueSetter?.call(currentTagInput, createEntityTag);
                currentTagInput.dispatchEvent(new Event('input', { bubbles: true }));
                currentTagInput.dispatchEvent(new Event('change', { bubbles: true }));

                const activePreviewButton = await waitFor(() => {
                    const button = document.querySelector('button[aria-label="Preview device creation"]');
                    return button && !button.disabled ? button : undefined;
                }, 'enabled graph-first create preview button');
                activePreviewButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));

                const acceptButton = await waitFor(() => {
                    const button = document.querySelector('button[aria-label="Accept device creation"]');
                    return button && !button.disabled ? button : undefined;
                }, 'acceptance-eligible graph-first create preview', 60000);
                const previewPanelText = (document.querySelector('[data-athena-create-entity-panel="true"]')?.textContent || '')
                    .replace(/\s+/g, ' ')
                    .trim();
                smokeStep('graph-create-preview-eligible');
                acceptButton.click();
                smokeStep('graph-create-accept-requested');

                const semanticId = 'component:' + createEntityTag;
                await waitFor(() => {
                    const occurrence = document.querySelector('[data-athena-semantic-id="' + semanticId + '"]');
                    return occurrence && !document.querySelector('[data-athena-create-entity-panel="true"]')
                        ? occurrence
                        : undefined;
                }, 'created semantic occurrence ' + semanticId, 60000).catch(error => {
                    const panel = document.querySelector('[data-athena-create-entity-panel="true"]');
                    const renderedSemanticIds = Array.from(document.querySelectorAll('[data-athena-semantic-id]'))
                        .map(element => element.getAttribute('data-athena-semantic-id') || '')
                        .filter(Boolean);
                    const emptyText = (document.querySelector('.athena-graph-workbench__empty')?.textContent || '')
                        .replace(/\s+/g, ' ')
                        .trim();
                    const loadingClasses = Array.from(document.querySelectorAll('.codicon-loading, .codicon-modifier-spin, .theia-loading, .theia-preload'))
                        .map(element => String(element.className || ''));
                    throw new Error(error.message + ': ' + JSON.stringify({
                        activeViewId: collectActiveProjectionViewId(),
                        panelPresent: !!panel,
                        panelText: (panel?.textContent || '').replace(/\s+/g, ' ').trim(),
                        renderedSemanticIds,
                        emptyText,
                        loadingClasses
                    }));
                });
                smokeStep('graph-create-projected');
                return {
                    requested: true,
                    tag: createEntityTag,
                    semanticId,
                    previewEligible: true,
                    previewTextIncludesTag: previewPanelText.includes(createEntityTag),
                    accepted: true,
                    projected: true,
                    outlineSkipped: ${JSON.stringify(SKIP_SMOKE_OUTLINE)},
                };
            }

            async function collectExpectedSemanticProof() {
                const expectedSemanticId = ${JSON.stringify(SMOKE_EXPECT_SEMANTIC_ID)};
                if (!expectedSemanticId) {
                    return { requested: false };
                }
                const occurrence = await waitFor(
                    () => document.querySelector('[data-athena-semantic-id="' + expectedSemanticId + '"]'),
                    'reopened semantic occurrence ' + expectedSemanticId,
                    60000
                );
                return {
                    requested: true,
                    semanticId: expectedSemanticId,
                    present: !!occurrence,
                    outlineSkipped: ${JSON.stringify(SKIP_SMOKE_OUTLINE)},
                };
            }

            async function collectOutlineProof(target) {
                const outlineSourceRelative = ${JSON.stringify(SMOKE_OUTLINE_SOURCE_RELATIVE)};
                const sourcePath = target.replace(/\\\\/g, '/') + '/' + outlineSourceRelative.replace(/^\\/+/, '');
                const sourceUri = 'file:///' + sourcePath.replace(/^\\/?([A-Za-z]:)/, '$1');
                const revealOutlineForSource = await waitFor(
                    () => window.__athenaWorkbenchSmoke?.revealOutlineForSource,
                    'Athena outline smoke command hook'
                );
                smokeStep('outline-hook');
                const proof = await Promise.race([
                    revealOutlineForSource(sourceUri),
                    new Promise((_, reject) => setTimeout(
                        () => reject(new Error('Timed out waiting for Athena Outline proof hook result for ' + sourceUri)),
                        60000
                    ))
                ]);
                smokeStep('outline-hook-result');
                const expectedOutlinePath = ${JSON.stringify(SMOKE_OUTLINE_EXPECTED_PATH)};
                await waitFor(() => {
                    const paths = Array.isArray(proof.paths) ? proof.paths : [];
                    return paths.some(path => path.endsWith(expectedOutlinePath))
                        ? true
                        : undefined;
                }, 'structured Outline proof nested path ' + expectedOutlinePath + ' in paths ' + JSON.stringify(proof.paths || []).slice(0, 500));
                return {
                    sourceUri,
                    expectedOutlinePath,
                    ...proof,
                };
            }

            function collectActiveProjectionViewId() {
                const activeButton = document.querySelector('.athena-graph-workbench__tool-button--view.athena-graph-workbench__tool-button--active');
                return activeButton?.getAttribute('data-athena-projection-view-id') || '';
            }

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

            async function collectDocumentationCompatibilityProof() {
                await switchGraphWorkbenchProjectionView('documentation');
                let proof;
                try {
                    const initialNavigation = await requireElement(
                        '.athena-graph-workbench__document-navigation',
                        'contextual Documentation navigation'
                    );
                    const initialSelector = initialNavigation.querySelector('.athena-graph-workbench__sheet-view-selector select');
                    const sheetViewIds = initialSelector
                        ? Array.from(initialSelector.options).map(option => option.value).filter(Boolean)
                        : [];
                    const selectedSheetViewId = sheetViewIds[1] || initialSelector?.value || '';
                    if (selectedSheetViewId) {
                        await switchSheetView(selectedSheetViewId);
                    }
                    const selectedSheetViewIdBeforeProjectionSwitch = document.querySelector(
                        '.athena-graph-workbench__document-navigation .athena-graph-workbench__sheet-view-selector select'
                    )?.value || '';

                    await switchGraphWorkbenchProjectionView('cabinet');
                    await switchGraphWorkbenchProjectionView('documentation');

                    const contextualNavigation = await requireElement(
                        '.athena-graph-workbench__document-navigation',
                        'restored contextual Documentation navigation'
                    );
                    const selector = contextualNavigation.querySelector('.athena-graph-workbench__sheet-view-selector select');
                    const restoredSheetViewId = selector?.value || '';
                    const markers = Array.from(contextualNavigation.querySelectorAll('[data-athena-reference-marker="true"]'));
                    const toolGroup = document.querySelector('.athena-graph-workbench__tool-group');
                    const documentControlSelector = '.athena-graph-workbench__sheet-view-selector, [data-athena-reference-marker="true"]';
                    proof = {
                        ...collectDocumentProjectionProof(selector, markers),
                        compatibilityActivated: true,
                        contextualNavigationPresent: visibleElement(contextualNavigation),
                        contextualDocumentControlCount: contextualNavigation.querySelectorAll(documentControlSelector).length,
                        globalToolbarDocumentControlCount: toolGroup?.querySelectorAll(documentControlSelector).length ?? 0,
                        selectedSheetViewIdBeforeProjectionSwitch,
                        restoredSheetViewId,
                        sheetSelectionRestored: !!selectedSheetViewIdBeforeProjectionSwitch &&
                            restoredSheetViewId === selectedSheetViewIdBeforeProjectionSwitch
                    };
                } finally {
                    await switchGraphWorkbenchProjectionView('cabinet');
                }
                await waitFor(() => {
                    const cabinetButton = document.querySelector('[data-athena-projection-view-id="cabinet"]');
                    return cabinetButton?.disabled ? cabinetButton : undefined;
                }, 'restored Cabinet projection after Documentation compatibility proof');
                return {
                    ...proof,
                    restoredActiveViewId: collectActiveProjectionViewId(),
                    contextualNavigationPresentAfterRestore: !!document.querySelector('.athena-graph-workbench__document-navigation')
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
                        routePoints: parseRoutePoints(route.getAttribute('data-athena-route-points') || ''),
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

            function collectSheetSurfaceProof(sheetFrame) {
                return {
                    factDriven: sheetFrame.getAttribute('data-athena-sheet-surface') === 'true',
                    surfaceId: sheetFrame.getAttribute('data-athena-sheet-surface-id') || '',
                    source: sheetFrame.getAttribute('data-athena-sheet-surface-source') || '',
                    sheetSize: sheetFrame.getAttribute('data-athena-sheet-size') || '',
                    orientation: sheetFrame.getAttribute('data-athena-sheet-orientation') || '',
                    policyId: sheetFrame.getAttribute('data-athena-sheet-policy-id') || '',
                    zoneColumnCount: (sheetFrame.getAttribute('data-athena-sheet-zone-columns') || '')
                        .split(',')
                        .filter(Boolean)
                        .length,
                    zoneRowCount: (sheetFrame.getAttribute('data-athena-sheet-zone-rows') || '')
                        .split(',')
                        .filter(Boolean)
                        .length,
                    hasMargins: ['top', 'right', 'bottom', 'left'].every(side =>
                        !!sheetFrame.getAttribute('data-athena-sheet-margin-' + side)
                    ),
                    hasTitleBlock: sheetFrame.getAttribute('data-athena-sheet-title-block') === 'true',
                    titleFieldRoles: (sheetFrame.getAttribute('data-athena-sheet-title-field-roles') || '')
                        .split(',')
                        .filter(Boolean)
                };
            }

            function collectSheetChromeVisualProof(sheet, sheetFrame) {
                const sheetStyles = window.getComputedStyle(sheet);
                const sheetFrameStyles = window.getComputedStyle(sheetFrame);
                return {
                    sheetBackgroundColor: sheetStyles.backgroundColor,
                    sheetBorderTopWidth: sheetStyles.borderTopWidth,
                    sheetBorderRightWidth: sheetStyles.borderRightWidth,
                    sheetBorderBottomWidth: sheetStyles.borderBottomWidth,
                    sheetBorderLeftWidth: sheetStyles.borderLeftWidth,
                    sheetBoxShadow: sheetStyles.boxShadow,
                    sheetFrameBackgroundColor: sheetFrameStyles.backgroundColor,
                    sheetFrameBorderTopWidth: sheetFrameStyles.borderTopWidth,
                    sheetFrameBorderRightWidth: sheetFrameStyles.borderRightWidth,
                    sheetFrameBorderBottomWidth: sheetFrameStyles.borderBottomWidth,
                    sheetFrameBorderLeftWidth: sheetFrameStyles.borderLeftWidth,
                    sheetFrameBoxShadow: sheetFrameStyles.boxShadow
                };
            }

            function collectDensityProof(referenceMarkerButtons) {
                const rootStyles = window.getComputedStyle(document.documentElement);
                const visibleRouteLabels = Array.from(document.querySelectorAll('[data-athena-route-label="true"]'))
                    .filter(label => label.getAttribute('data-athena-route-label-display') !== 'selection');
                const deferredRouteLabels = Array.from(document.querySelectorAll('[data-athena-route-label="true"]'))
                    .filter(label => label.getAttribute('data-athena-route-label-display') === 'selection');
                const semanticRouteLabels = visibleRouteLabels
                    .map(label => (label.textContent || '').replace(/\\s+/g, ' ').trim())
                    .filter(label => label.includes(' -> ') && label.includes('.'));
                const textNodes = [
                    ...Array.from(document.querySelectorAll('.athena-graph-workbench__presentation-terminal-number')),
                    ...Array.from(document.querySelectorAll('.athena-graph-workbench__presentation-label')),
                    ...visibleRouteLabels,
                    ...referenceMarkerButtons
                ];
                const invalidTextBoxes = textNodes.filter(node => {
                    const rect = node.getBoundingClientRect();
                    return rect.width <= 0 || rect.height <= 0 || !Number.isFinite(rect.width) || !Number.isFinite(rect.height);
                });
                return {
                    electricalLineWidth: rootStyles.getPropertyValue('--athena-graph-electrical-line-width').trim(),
                    terminalTextSize: rootStyles.getPropertyValue('--athena-graph-terminal-text-size').trim(),
                    deviceTextSize: rootStyles.getPropertyValue('--athena-graph-device-text-size').trim(),
                    routeLabelSize: rootStyles.getPropertyValue('--athena-graph-route-label-size').trim(),
                    referenceMarkerSize: rootStyles.getPropertyValue('--athena-graph-reference-marker-size').trim(),
                    visibleRouteLabelCount: visibleRouteLabels.length,
                    deferredRouteLabelCount: deferredRouteLabels.length,
                    visibleVerboseRouteLabelCount: semanticRouteLabels.length,
                    referenceMarkerCount: referenceMarkerButtons.length,
                    textBoxCount: textNodes.length,
                    invalidTextBoxCount: invalidTextBoxes.length
                };
            }

            function collectRepresentationProof() {
                const representations = Array.from(document.querySelectorAll('[data-athena-representation-fact="true"]'));
                const terminals = Array.from(document.querySelectorAll('[data-athena-presentation-terminal="true"]'));
                const labels = Array.from(document.querySelectorAll('[data-athena-presentation-label="true"]'));
                const representationStates = representations.map(representation => ({
                    representationId: representation.getAttribute('data-athena-representation-id') || '',
                    fallback: representation.getAttribute('data-athena-render-fallback') === 'true',
                    semanticId: representation.getAttribute('data-athena-semantic-id') || '',
                    engineeringPackageId: representation.getAttribute('data-athena-engineering-package-id') || '',
                    presentationProfileId: representation.getAttribute('data-athena-presentation-profile-id') || '',
                    bindingManifestId: representation.getAttribute('data-athena-binding-manifest-id') || '',
                    representationPackageId: representation.getAttribute('data-athena-representation-package-id') || '',
                    descriptorId: representation.getAttribute('data-athena-representation-descriptor-id') || '',
                    graphicResourceId: representation.getAttribute('data-athena-graphic-resource-id') || '',
                    anchorMapSummary: (representation.getAttribute('data-athena-representation-anchor-map') || '')
                        .split(';')
                        .filter(Boolean),
                    labelBindingSummary: (representation.getAttribute('data-athena-representation-label-binding') || '')
                        .split(';')
                        .filter(Boolean)
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
                    engineeringPackageIds: representationStates.map(representation => representation.engineeringPackageId).filter(Boolean),
                    presentationProfileIds: representationStates.map(representation => representation.presentationProfileId).filter(Boolean),
                    bindingManifestIds: representationStates.map(representation => representation.bindingManifestId).filter(Boolean),
                    representationPackageIds: representationStates.map(representation => representation.representationPackageId).filter(Boolean),
                    descriptorIds: representationStates.map(representation => representation.descriptorId).filter(Boolean),
                    graphicResourceIds: representationStates.map(representation => representation.graphicResourceId).filter(Boolean),
                    anchorMapSummary: representationStates.flatMap(representation => representation.anchorMapSummary),
                    labelBindingSummary: representationStates.flatMap(representation => representation.labelBindingSummary),
                    fallbackRepresentationIds: representationStates
                        .filter(representation => representation.fallback)
                        .map(representation => representation.representationId || representation.semanticId || '<unknown>'),
                    representationStates
                };
            }

            async function revealGraphicalViewThroughDom() {
                if (document.querySelector('.athena-graph-workbench')) {
                    return;
                }
                const graphicalViewButton = await waitFor(() => {
                    const candidates = Array.from(document.querySelectorAll('button, [role="button"], .p-TabBar-tab, .lm-TabBar-tab'));
                    return candidates.find(candidate =>
                        visibleElement(candidate) && commandText(candidate).includes('Graphical View')
                    );
                }, 'Graphical View quick action or tab').catch(error => {
                    const nearbyCommands = Array.from(document.querySelectorAll('button, [role="button"], .p-TabBar-tab, .lm-TabBar-tab'))
                        .map(commandText)
                        .filter(Boolean)
                        .slice(0, 80);
                    const bodyText = (document.body?.textContent || '').replace(/\\s+/g, ' ').trim().slice(0, 2000);
                    throw new Error(error.message + '; visible commands=' + JSON.stringify(nearbyCommands) + '; body=' + JSON.stringify(bodyText));
                });
                graphicalViewButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                await waitFor(() => {
                    if (document.querySelector('.athena-graph-workbench')) {
                        return true;
                    }
                    return undefined;
                }, 'Graphical View workbench after DOM activation', 30000).catch(error => {
                    const nearbyCommands = Array.from(document.querySelectorAll('button, [role="button"], .p-TabBar-tab'))
                        .map(commandText)
                        .filter(Boolean)
                        .slice(0, 40);
                    throw new Error(error.message + '; visible commands=' + JSON.stringify(nearbyCommands));
                });
            }

            async function collectAllSheetVisualProofs(sheetViewSelector) {
                if (!sheetViewSelector) {
                    return [];
                }
                const originalSheetViewId = sheetViewSelector.value;
                const sheetViewIds = Array.from(sheetViewSelector.options)
                    .map(option => option.value)
                    .filter(Boolean);
                const results = [];
                for (const sheetViewId of sheetViewIds) {
                    await switchSheetView(sheetViewId);
                    const currentSelector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
                    results.push({
                        sheetViewId,
                        selected: currentSelector?.value === sheetViewId,
                        routeProof: collectRouteProof(),
                        representationProof: collectRepresentationProof(),
                        visualProof: collectVisualProof()
                    });
                }
                await switchSheetView(originalSheetViewId);
                return results;
            }

            async function switchSheetView(sheetViewId, force = false) {
                const selector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
                if (!selector || (selector.value === sheetViewId && !force)) {
                    return;
                }
                await switchGraphWorkbenchSheetView(sheetViewId);
                await waitFor(() => {
                    const nextSelector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
                    return nextSelector?.value === sheetViewId ? nextSelector : undefined;
                }, 'sheet projection view ' + sheetViewId + ' through widget API', 5000).catch(async () => {
                    dispatchNativeSheetSelectorChange(sheetViewId);
                    await waitFor(() => {
                        const nextSelector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
                        return nextSelector?.value === sheetViewId ? nextSelector : undefined;
                    }, 'sheet projection view ' + sheetViewId, 30000);
                });
                await waitFor(() => {
                    const nextSelector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
                    const routeCount = document.querySelectorAll('[data-athena-route-fact="true"]').length;
                    const nodeBoxCount = document.querySelectorAll('.athena-graph-workbench__node-hitbox').length;
                    return nextSelector?.value === sheetViewId && routeCount > 0 && nodeBoxCount > 0
                        ? true
                        : undefined;
                }, 'rendered sheet content for ' + sheetViewId, 30000);
                await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
            }

            async function switchGraphWorkbenchSheetView(sheetViewId) {
                if (typeof require !== 'function') {
                    const selector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
                    selector.value = sheetViewId;
                    selector.dispatchEvent(new Event('change', { bubbles: true }));
                    return;
                }
                const { WidgetManager } = require('@theia/core/lib/browser/widget-manager');
                const { AthenaGraphWorkbenchWidget } = require('@engineeringood/athena-theia-frontend/lib/browser/athena-graph-workbench-widget');
                const widgetManager = window.theia.container.get(WidgetManager);
                const graphWidget = await widgetManager.getOrCreateWidget(AthenaGraphWorkbenchWidget.ID);
                await Promise.race([
                    graphWidget.switchActiveSheetView(sheetViewId),
                    new Promise((_, reject) => setTimeout(
                        () => reject(new Error('Timed out waiting for graph widget sheet switch ' + sheetViewId)),
                        5000
                    ))
                ]);
            }

            async function switchGraphWorkbenchProjectionView(viewId) {
                const switchProjectionView = await waitFor(
                    () => window.__athenaWorkbenchSmoke?.switchProjectionView,
                    'Athena projection compatibility smoke hook'
                );
                const switched = await Promise.race([
                    switchProjectionView(viewId),
                    new Promise((_, reject) => setTimeout(
                        () => reject(new Error('Timed out waiting for graph widget projection switch ' + viewId)),
                        10000
                    ))
                ]);
                if (!switched) {
                    throw new Error('Graph widget rejected projection compatibility switch ' + viewId);
                }
                await waitFor(() => {
                    const routeCount = document.querySelectorAll('[data-athena-route-fact="true"]').length;
                    const nodeBoxCount = document.querySelectorAll('.athena-graph-workbench__node-hitbox').length;
                    return routeCount > 0 && nodeBoxCount > 0 ? true : undefined;
                }, 'rendered projection compatibility view ' + viewId, 30000);
                await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
            }

            function dispatchNativeSheetSelectorChange(sheetViewId) {
                const selector = document.querySelector('.athena-graph-workbench__sheet-view-selector select');
                if (!selector) {
                    return;
                }
                const valueSetter = Object.getOwnPropertyDescriptor(HTMLSelectElement.prototype, 'value')?.set;
                if (valueSetter) {
                    valueSetter.call(selector, sheetViewId);
                } else {
                    selector.value = sheetViewId;
                }
                selector.dispatchEvent(new Event('input', { bubbles: true }));
                selector.dispatchEvent(new Event('change', { bubbles: true }));
            }

            async function collectEditorSyntaxColorProof(target) {
                if (!${JSON.stringify(COLLECT_EDITOR_SYNTAX_COLOR_PROOF)}) {
                    return {
                        skipped: true,
                        reason: 'ATHENA_ELECTRON_SMOKE_EDITOR_SYNTAX_COLORS not enabled'
                    };
                }
                const sourceRelative = ${JSON.stringify(SMOKE_OUTLINE_SOURCE_RELATIVE)};
                const sourcePath = target.replace(/\\\\/g, '/') + '/' + sourceRelative.replace(/^\\/+/, '');
                const sourceUri = 'file:///' + sourcePath.replace(/^\\/?([A-Za-z]:)/, '$1');
                const revealOutlineForSource = await waitFor(
                    () => window.__athenaWorkbenchSmoke?.revealOutlineForSource,
                    'Athena editor syntax color source reveal hook'
                );
                await Promise.race([
                    revealOutlineForSource(sourceUri),
                    new Promise((_, reject) => setTimeout(
                        () => reject(new Error('Timed out waiting for Athena source editor syntax color proof for ' + sourceUri)),
                        60000
                    ))
                ]);
                const sourceEditorProof = await waitFor(
                    () => window.__athenaWorkbenchSmoke?.openSourceEditorForSmoke,
                    'Athena source editor smoke command hook'
                ).then(openSourceEditorForSmoke => openSourceEditorForSmoke(sourceUri));
                if (!sourceEditorProof.currentEditorWidgetId) {
                    throw new Error('Athena source editor smoke hook did not activate an editor: ' + JSON.stringify(sourceEditorProof));
                }
                await waitFor(() => {
                    const visibleLines = Array.from(document.querySelectorAll('.monaco-editor .view-line'))
                        .filter(visibleElement);
                    return visibleLines.length > 0 ? visibleLines : undefined;
                }, 'visible Monaco source editor lines for syntax color proof');
                const observed = {};
                const revealSourceLineForSmoke = await waitFor(
                    () => window.__athenaWorkbenchSmoke?.revealSourceLineForSmoke,
                    'Athena source editor line reveal smoke command hook'
                );
                await revealEditorLine(revealSourceLineForSmoke, 2);
                collectVisibleTokenColors(observed);
                await revealEditorLine(revealSourceLineForSmoke, 8);
                collectVisibleTokenColors(observed);
                await revealEditorLine(revealSourceLineForSmoke, 144);
                collectVisibleTokenColors(observed);
                await revealEditorLine(revealSourceLineForSmoke, 161);
                collectVisibleTokenColors(observed);
                await revealEditorLine(revealSourceLineForSmoke, 163);
                collectVisibleTokenColors(observed);
                await revealEditorLine(revealSourceLineForSmoke, 171);
                collectVisibleTokenColors(observed);
                await revealEditorLine(revealSourceLineForSmoke, 173);
                collectVisibleTokenColors(observed);

                const selected = {};
                [
                    'system',
                    'device',
                    'direction',
                    'out',
                    'connect',
                    '->',
                    'layout',
                    'place',
                    'near',
                    'align',
                    'aligned-with',
                    'axis',
                    'vertical',
                    'group',
                    'grouped-with'
                ].forEach(token => {
                    selected[token] = observed[token] || '';
                });
                return {
                    skipped: false,
                    sourceUri,
                    sourceEditorProof,
                    selected,
                    distinctCategoryColorCount: new Set([
                        selected.system,
                        selected.direction,
                        selected.connect,
                        selected.layout,
                        selected['aligned-with']
                    ].filter(Boolean)).size
                };
            }

            async function revealEditorLine(revealSourceLineForSmoke, lineNumber) {
                await revealSourceLineForSmoke(lineNumber);
                await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
            }

            function collectVisibleTokenColors(target) {
                Array.from(document.querySelectorAll('.monaco-editor .view-line span')).forEach(span => {
                    const text = (span.textContent || '').trim();
                    if (!text || target[text]) {
                        return;
                    }
                    target[text] = window.getComputedStyle(span).color;
                });
            }

            function collectVisualProof() {
                const canvas = document.querySelector('.athena-graph-workbench__canvas');
                const viewportRect = roundedClientRect(document.querySelector('.athena-graph-workbench__viewport'));
                const sheetRect = roundedClientRect(document.querySelector('.athena-graph-workbench__sheet'));
                const canvasRect = roundedClientRect(canvas);
                const bottomDockRect = roundedClientRect(document.querySelector('.athena-graph-workbench__bottom-dock'));
                const usableViewportRect = {
                    ...viewportRect,
                    bottom: bottomDockRect.top > viewportRect.top ? Math.min(viewportRect.bottom, bottomDockRect.top) : viewportRect.bottom
                };
                usableViewportRect.height = Math.max(0, usableViewportRect.bottom - usableViewportRect.top);
                const svgViewBox = canvas?.getAttribute('viewBox') || '';
                const viewBoxParts = (svgViewBox.match(/-?\\d+(?:\\.\\d+)?/g) || []).map(Number);
                const nodeBoxes = collectNodeBoxes();
                const routes = Array.from(document.querySelectorAll('[data-athena-route-fact="true"]'));
                    const routeStates = routes.map(route => {
                    const routeId = route.getAttribute('data-athena-route-id') || '';
                    const semanticId = route.getAttribute('data-athena-route-semantic-id') || '';
                    const routePoints = parseRoutePoints(route.getAttribute('data-athena-route-points') || '');
                    const ownerIds = routeEndpointOwnerIds(semanticId);
	                const crossedNodeBoxes = nodeBoxes
	                        .filter(isComponentBodyBox)
	                        .filter(nodeBox => !ownerIds.has(stripSemanticPrefix(nodeBox.semanticId)))
	                        .filter(nodeBox => routeCrossesNodeBox(routePoints, nodeBox));
                    const nonOrthogonalSegments = countNonOrthogonalSegments(routePoints);
                    return {
                        routeId,
                        semanticId,
                        pointCount: routePoints.length,
                        nonOrthogonalSegments,
                        crossedNodeSemanticIds: crossedNodeBoxes.map(nodeBox => nodeBox.semanticId)
                    };
                });
                return {
                    viewportWidth: viewportRect.width,
                    viewportHeight: viewportRect.height,
                    usableViewportWidth: usableViewportRect.width,
                    usableViewportHeight: usableViewportRect.height,
                    sheetWidth: sheetRect.width,
                    sheetHeight: sheetRect.height,
                    sheetCenterDeltaX: Math.round(Math.abs(((sheetRect.left + sheetRect.right) / 2) - ((usableViewportRect.left + usableViewportRect.right) / 2))),
                    sheetCenterDeltaY: Math.round(Math.abs(((sheetRect.top + sheetRect.bottom) / 2) - ((usableViewportRect.top + usableViewportRect.bottom) / 2))),
                    canvasWidth: canvasRect.width,
                    canvasHeight: canvasRect.height,
                    svgViewBox,
                    viewBoxMinX: Number.isFinite(viewBoxParts[0]) ? viewBoxParts[0] : 0,
                    viewBoxMinY: Number.isFinite(viewBoxParts[1]) ? viewBoxParts[1] : 0,
                    viewBoxWidth: Number.isFinite(viewBoxParts[2]) ? viewBoxParts[2] : 0,
                    viewBoxHeight: Number.isFinite(viewBoxParts[3]) ? viewBoxParts[3] : 0,
                    nodeBoxCount: nodeBoxes.length,
                    nodeSemanticIds: nodeBoxes.map(nodeBox => nodeBox.semanticId).filter(Boolean),
                    routeCount: routes.length,
                    routesWithSerializedPoints: routeStates.filter(route => route.pointCount >= 2).length,
                    nonOrthogonalSegmentCount: routeStates.reduce((sum, route) => sum + route.nonOrthogonalSegments, 0),
                    routeBodyIntersectionCount: routeStates.filter(route => route.crossedNodeSemanticIds.length > 0).length,
                    routeBodyIntersections: routeStates
                        .filter(route => route.crossedNodeSemanticIds.length > 0)
                        .map(route => ({
                            routeId: route.routeId || route.semanticId,
                            crossedNodeSemanticIds: route.crossedNodeSemanticIds
                        }))
                };
            }

            function roundedClientRect(element) {
                const rect = element?.getBoundingClientRect();
                return {
                    width: Math.round(rect?.width ?? 0),
                    height: Math.round(rect?.height ?? 0),
                    left: Math.round(rect?.left ?? 0),
                    top: Math.round(rect?.top ?? 0),
                    right: Math.round(rect?.right ?? 0),
                    bottom: Math.round(rect?.bottom ?? 0)
                };
            }

	            function collectNodeBoxes() {
	                return Array.from(document.querySelectorAll('.athena-graph-workbench__node-hitbox'))
	                    .map(hitbox => {
	                        const group = hitbox.closest('[data-athena-semantic-id]');
	                        return {
	                            semanticId: group?.getAttribute('data-athena-semantic-id') || '',
	                            representationId: group?.getAttribute('data-athena-representation-id') || '',
	                            x: Number(hitbox.getAttribute('x') || '0'),
	                            y: Number(hitbox.getAttribute('y') || '0'),
	                            width: Number(hitbox.getAttribute('width') || '0'),
                            height: Number(hitbox.getAttribute('height') || '0')
                        };
	                    })
	                    .filter(box => box.semanticId && box.width > 0 && box.height > 0);
	            }

	            function isComponentBodyBox(nodeBox) {
	                return nodeBox.semanticId.startsWith('component:') && !!nodeBox.representationId;
	            }

            function parseRoutePoints(value) {
                return value
                    .split(';')
                    .map(part => part.trim())
                    .filter(Boolean)
                    .map(part => {
                        const [x, y] = part.split(',').map(Number);
                        return { x, y };
                    })
                    .filter(point => Number.isFinite(point.x) && Number.isFinite(point.y));
            }

            function routeEndpointOwnerIds(semanticId) {
                const owners = new Set();
                const normalized = semanticId.replace(/^connection:/, '');
                normalized.split('->').forEach(endpoint => {
                    const owner = endpoint.trim().split('.')[0];
                    if (owner) {
                        owners.add(owner);
                    }
                });
                return owners;
            }

            function stripSemanticPrefix(semanticId) {
                return semanticId.replace(/^component:/, '');
            }

            function countNonOrthogonalSegments(routePoints) {
                let count = 0;
                for (let index = 0; index < routePoints.length - 1; index += 1) {
                    const start = routePoints[index];
                    const end = routePoints[index + 1];
                    if (start.x !== end.x && start.y !== end.y) {
                        count += 1;
                    }
                }
                return count;
            }

            function routeCrossesNodeBox(routePoints, nodeBox) {
                for (let index = 0; index < routePoints.length - 1; index += 1) {
                    if (segmentCrossesNodeBox(routePoints[index], routePoints[index + 1], nodeBox)) {
                        return true;
                    }
                }
                return false;
            }

            function segmentCrossesNodeBox(start, end, nodeBox) {
                const padding = 2;
                const left = nodeBox.x + padding;
                const right = nodeBox.x + nodeBox.width - padding;
                const top = nodeBox.y + padding;
                const bottom = nodeBox.y + nodeBox.height - padding;
                if (right <= left || bottom <= top) {
                    return false;
                }
                if (start.y === end.y) {
                    const y = start.y;
                    if (y <= top || y >= bottom) {
                        return false;
                    }
                    const minX = Math.min(start.x, end.x);
                    const maxX = Math.max(start.x, end.x);
                    return Math.max(minX, left) < Math.min(maxX, right);
                }
                if (start.x === end.x) {
                    const x = start.x;
                    if (x <= left || x >= right) {
                        return false;
                    }
                    const minY = Math.min(start.y, end.y);
                    const maxY = Math.max(start.y, end.y);
                    return Math.max(minY, top) < Math.min(maxY, bottom);
                }
                return true;
            }
        })();
    `, true);

    await captureGraphWorkbenchScreenshot(window);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(proof.graphWorkbench)}`);
    console.log(`${ATHENA_WORKSPACE_OPENED_SENTINEL}${proof.workspace}`);
    if (SHOULD_EXIT_ON_WORKSPACE_OPEN) {
        setTimeout(() => app.exit(0), 250);
    }
}

async function captureGraphWorkbenchScreenshot(window) {
    if (!GRAPH_VIEW_SCREENSHOT_PATH) {
        return;
    }
    const captureRect = await window.webContents.executeJavaScript(`
        (async () => {
            const waitFor = async (predicate, description, timeoutMs = 30000, intervalMs = 100) => {
                const startedAt = Date.now();
                while (Date.now() - startedAt < timeoutMs) {
                    const result = predicate();
                    if (result) {
                        return result;
                    }
                    await new Promise(resolve => setTimeout(resolve, intervalMs));
                }
                throw new Error('Timed out waiting for screenshot-ready graph workbench: ' + description);
            };
            let lastReadinessState;
            await waitFor(() => {
                const workbench = document.querySelector('.athena-graph-workbench');
                const viewport = document.querySelector('.athena-graph-workbench__viewport');
                const sheet = document.querySelector('.athena-graph-workbench__sheet');
                const sheetFrame = document.querySelector('.athena-graph-workbench__sheet-frame');
                const routeCount = document.querySelectorAll('[data-athena-route-fact="true"]').length;
                const emptyText = (document.querySelector('.athena-graph-workbench__empty')?.textContent || '').replace(/\\s+/g, ' ').trim();
                const activeSpinner = document.querySelector(
                    '.codicon-loading, .codicon-modifier-spin, .theia-loading, .theia-preload'
                );
                const workbenchRect = workbench?.getBoundingClientRect();
                const viewportRect = viewport?.getBoundingClientRect();
                const sheetRect = sheet?.getBoundingClientRect();
                const spinnerStyle = activeSpinner ? window.getComputedStyle(activeSpinner) : undefined;
                lastReadinessState = {
                    workbench: !!workbench,
                    viewportPresent: !!viewport,
                    sheet: !!sheet,
                    sheetFrame: !!sheetFrame,
                    routeCount,
                    emptyText,
                    activeSpinnerClass: activeSpinner?.className || '',
                    activeSpinnerDisplay: spinnerStyle?.display || '',
                    activeSpinnerVisibility: spinnerStyle?.visibility || '',
                    activeSpinnerOpacity: spinnerStyle?.opacity || '',
                    workbenchWidth: workbenchRect?.width || 0,
                    workbenchHeight: workbenchRect?.height || 0,
                    viewportWidth: viewportRect?.width || 0,
                    viewportHeight: viewportRect?.height || 0,
                    sheetWidth: sheetRect?.width || 0,
                    sheetHeight: sheetRect?.height || 0
                };
                return workbench
                    && viewport
                    && sheet
                    && sheetFrame
                    && routeCount > 0
                    && !emptyText
                    && !activeSpinner
                    && workbenchRect?.width > 0
                    && workbenchRect?.height > 0
                    && viewportRect?.width > 0
                    && viewportRect?.height > 0
                    && sheetRect?.width > 0
                    && sheetRect?.height > 0;
            }, 'visible routed sheet').catch(error => {
                throw new Error(error.message + ': ' + JSON.stringify(lastReadinessState));
            });
            await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
            const rect = document.querySelector('.athena-graph-workbench').getBoundingClientRect();
            return {
                x: Math.max(0, Math.round(rect.left)),
                y: Math.max(0, Math.round(rect.top)),
                width: Math.max(1, Math.round(rect.width)),
                height: Math.max(1, Math.round(rect.height))
            };
        })();
    `, true);
    fs.mkdirSync(path.dirname(GRAPH_VIEW_SCREENSHOT_PATH), { recursive: true });
    const image = await window.webContents.capturePage(captureRect);
    fs.writeFileSync(GRAPH_VIEW_SCREENSHOT_PATH, image.toPNG());
    console.log(`${ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL}${GRAPH_VIEW_SCREENSHOT_PATH}`);
}

function configureJvmRuntime() {
    const resolver = new AthenaJvmRuntimeResolver();
    return resolver.configureProcessEnvironment(process.env, process.platform);
}

main();

function resolveRequestedActiveView() {
    const activeViewArgIndex = process.argv.indexOf('--active-view');
    const activeViewArg = activeViewArgIndex >= 0 ? process.argv[activeViewArgIndex + 1] : undefined;
    return activeViewArg || process.env.ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW || '';
}
