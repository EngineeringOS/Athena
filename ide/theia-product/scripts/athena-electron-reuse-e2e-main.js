const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED';
const ATHENA_REUSE_E2E_RESULT_SENTINEL = 'ATHENA_REUSE_CATALOG_E2E_RESULT=';
const ATHENA_REUSE_E2E_FAILURE_SENTINEL = 'ATHENA_REUSE_CATALOG_E2E_FAILURE=';
const SHOULD_EXIT_ON_SUCCESS = process.env.ATHENA_ELECTRON_SMOKE_EXIT_ON_REUSE_E2E === '1';
const TARGET_MACRO_ID = process.env.ATHENA_E2E_REUSE_MACRO_ID || 'macro:dol-starter';
const TARGET_REPOSITORY_ROOT = process.argv[2];

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
            void runReuseCatalogE2E(window).catch(error => {
                console.error(`${ATHENA_REUSE_E2E_FAILURE_SENTINEL}${error.stack || String(error)}`);
                setTimeout(() => app.exit(1), 50);
            });
        });
    });

    require('../lib/backend/electron-main.js');
}

async function runReuseCatalogE2E(window) {
    const summary = await window.webContents.executeJavaScript(`
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

            const query = selector => document.querySelector(selector);
            const queryAll = selector => Array.from(document.querySelectorAll(selector));
            const normalizeText = value => (value || '').replace(/\\s+/g, ' ').trim();
            const waitForEnabled = async (selector, description, timeoutMs = 60000) => waitFor(() => {
                const element = query(selector);
                return element && !element.disabled ? element : undefined;
            }, description, timeoutMs);
            const findMacroButton = macroId => queryAll('[data-testid="athena-reuse-macro-item"]')
                .find(node => normalizeText(node.textContent).includes(macroId));
            let bridgeService;
            let repositorySessionService;
            let reuseCatalogWidget;
            const setFieldValue = async (selector, value) => {
                const element = await waitFor(() => query(selector), selector);
                element.focus();
                const prototype = element.tagName === 'SELECT'
                    ? HTMLSelectElement.prototype
                    : HTMLInputElement.prototype;
                const valueSetter = Object.getOwnPropertyDescriptor(prototype, 'value')?.set;
                if (valueSetter) {
                    valueSetter.call(element, value);
                } else {
                    element.value = value;
                }
                if (element.tagName === 'SELECT') {
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                } else {
                    element.dispatchEvent(new Event('input', { bubbles: true }));
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                }
                return element;
            };

            await waitFor(() => window.theia?.container, 'theia container');
            if (typeof require === 'function') {
                const URI = require('@theia/core/lib/common/uri').default;
                const { WidgetManager } = require('@theia/core/lib/browser');
                const { WorkspaceService } = require('@theia/workspace/lib/browser/workspace-service');
                const { AthenaLspEditorBridgeService } = require('@engineeringood/athena-theia-frontend/lib/browser/athena-lsp-editor-bridge-service');
                const { AthenaSemanticMacroCatalogWidget } = require('@engineeringood/athena-theia-frontend/lib/browser/athena-semantic-macro-catalog-widget');
                const { AthenaRepositorySessionService } = require('@engineeringood/athena-theia-frontend/lib/browser/athena-repository-session-service');
                const workspaceService = window.theia.container.get(WorkspaceService);
                const widgetManager = window.theia.container.get(WidgetManager);
                bridgeService = window.theia.container.get(AthenaLspEditorBridgeService);
                repositorySessionService = window.theia.container.get(AthenaRepositorySessionService);
                if (${JSON.stringify(!!TARGET_REPOSITORY_ROOT)}) {
                    await workspaceService.open(URI.fromFilePath(${JSON.stringify(TARGET_REPOSITORY_ROOT)}), { preserveWindow: true });
                }
                if (repositorySessionService?.activateCurrentWorkspaceSession) {
                    await repositorySessionService.activateCurrentWorkspaceSession();
                }
                await waitFor(
                    () => repositorySessionService.state?.lifecycle === 'ready' ? repositorySessionService.state.lifecycle : undefined,
                    'Athena repository session ready'
                );
                reuseCatalogWidget = await widgetManager.getOrCreateWidget(AthenaSemanticMacroCatalogWidget.ID);
                if (reuseCatalogWidget?.refreshCatalog) {
                    await reuseCatalogWidget.refreshCatalog();
                }
            }
            await waitFor(() => query('[data-testid="athena-reuse-catalog"]'), 'reuse catalog root');
            let macroButton;
            try {
                macroButton = await waitFor(async () => {
                    const button = findMacroButton(${JSON.stringify(TARGET_MACRO_ID)});
                    if (button) {
                        return button;
                    }
                    const directCatalog = bridgeService?.requestSemanticMacroCatalog
                        ? await bridgeService.requestSemanticMacroCatalog()
                        : undefined;
                    if (directCatalog?.entries?.some(entry => entry.macroId === ${JSON.stringify(TARGET_MACRO_ID)})) {
                        if (reuseCatalogWidget?.refreshCatalog) {
                            await reuseCatalogWidget.refreshCatalog();
                        }
                    }
                    return findMacroButton(${JSON.stringify(TARGET_MACRO_ID)});
                }, 'target macro button');
            } catch (error) {
                const directCatalog = bridgeService?.requestSemanticMacroCatalog
                    ? await bridgeService.requestSemanticMacroCatalog()
                    : undefined;
                throw new Error(JSON.stringify({
                    reason: error instanceof Error ? error.message : String(error),
                    sessionState: repositorySessionService?.state,
                    directCatalog,
                    widgetCatalog: reuseCatalogWidget?.catalog,
                    widgetGroups: reuseCatalogWidget?.groups?.map(group => ({
                        categoryId: group.categoryId,
                        itemCount: group.items.length,
                        macroIds: group.items.map(item => item.macroId)
                    })),
                    renderedMacroTexts: queryAll('[data-testid="athena-reuse-macro-item"]').map(node => normalizeText(node.textContent))
                }));
            }
            macroButton.click();

            await setFieldValue('[data-testid="athena-reuse-instance-id"]', 'instance:M1');
            await setFieldValue('[data-testid="athena-reuse-parameter-motorPower"]', '7.5kW');
            await setFieldValue('[data-testid="athena-reuse-parameter-controlVoltage"]', '24VDC');
            await setFieldValue('[data-testid="athena-reuse-parameter-vendorFamily"]', 'Siemens');
            await setFieldValue('[data-testid="athena-reuse-parameter-tagPrefix"]', 'M1');

            const validateButton = await waitForEnabled('[data-testid="athena-reuse-validate"]', 'enabled validate button');
            validateButton.click();
            await waitFor(() => {
                const message = normalizeText(query('[data-testid="athena-reuse-flow-message"]')?.textContent);
                return message.includes('Parameter validation passed') ? message : undefined;
            }, 'successful validation flow message');

            const previewButton = await waitForEnabled('[data-testid="athena-reuse-preview"]', 'enabled preview button');
            previewButton.click();

            try {
                await waitFor(
                    () => normalizeText(query('[data-testid="athena-reuse-preview-status"]')?.textContent) === 'ready',
                    'ready preview status'
                );
            } catch (error) {
                throw new Error(JSON.stringify({
                    reason: error instanceof Error ? error.message : String(error),
                    flowMessage: normalizeText(query('[data-testid="athena-reuse-flow-message"]')?.textContent),
                    previewStatus: normalizeText(query('[data-testid="athena-reuse-preview-status"]')?.textContent),
                    previewPanelText: normalizeText(query('[data-testid="athena-reuse-preview-panel"]')?.textContent),
                    parameterInputs: reuseCatalogWidget?.parameterInputValues,
                    validationStatus: reuseCatalogWidget?.validation?.status,
                    validationDiagnostics: reuseCatalogWidget?.validation?.diagnostics,
                    validationNormalizedValues: reuseCatalogWidget?.validation?.normalizedValues,
                    previewPayload: reuseCatalogWidget?.preview,
                }));
            }

            const componentTexts = await waitFor(() => {
                const texts = queryAll('[data-testid="athena-reuse-preview-component"]').map(node => normalizeText(node.textContent));
                return texts.length > 0 ? texts : undefined;
            }, 'preview components');

            if (!componentTexts.some(text => text.includes('Main contactor'))) {
                throw new Error('Preview components did not include Main contactor.');
            }

            const approveButton = await waitForEnabled('[data-testid="athena-reuse-accept"]', 'enabled approve preview button');
            approveButton.click();

            const approvalMessage = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-flow-message"]')?.textContent);
                return value ? value : undefined;
            }, 'approval response');
            const approvalBundleId = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-acceptance-bundle-id"]')?.textContent);
                return value ? value : undefined;
            }, 'approval bundle id');
            const approvalOperationCount = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-acceptance-operation-count"]')?.textContent);
                if (!value) {
                    return undefined;
                }
                const parsed = Number(value);
                return Number.isFinite(parsed) ? parsed : undefined;
            }, 'approval operation count');
            const approvalCommandId = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-acceptance-command-id"]')?.textContent);
                return value ? value : undefined;
            }, 'approval command id');
            const approvalChangedCount = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-acceptance-changed-count"]')?.textContent);
                if (!value) {
                    return undefined;
                }
                const parsed = Number(value);
                return Number.isFinite(parsed) ? parsed : undefined;
            }, 'approval changed count');
            const originStatus = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-origin-status"]')?.textContent);
                return value === 'ready' ? value : undefined;
            }, 'ready origin status');
            const originExpansionId = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-origin-expansion-id"]')?.textContent);
                return value ? value : undefined;
            }, 'origin expansion id');
            const originMatchedRole = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-origin-matched-role"]')?.textContent);
                return value ? value : undefined;
            }, 'origin matched role');
            const originMembershipCount = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-origin-membership-count"]')?.textContent);
                if (!value) {
                    return undefined;
                }
                const parsed = Number(value);
                return Number.isFinite(parsed) ? parsed : undefined;
            }, 'origin membership count');

            const cancelButton = await waitFor(() => query('[data-testid="athena-reuse-cancel"]'), 'cancel preview button');
            cancelButton.click();

            const cancellationMessage = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-flow-message"]')?.textContent);
                return value.includes('Canonical state remains unchanged') ? value : undefined;
            }, 'cancellation confirmation');
            const persistentOriginStatus = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-origin-status"]')?.textContent);
                return value === 'ready' ? value : undefined;
            }, 'persistent origin status after cancel');
            const persistentOriginExpansionId = await waitFor(() => {
                const value = normalizeText(query('[data-testid="athena-reuse-origin-expansion-id"]')?.textContent);
                return value ? value : undefined;
            }, 'persistent origin expansion id after cancel');

            return {
                catalogStatus: normalizeText(query('[data-testid="athena-reuse-catalog-status"]')?.textContent),
                previewStatus: 'ready',
                componentCount: componentTexts.length,
                approvalMessage,
                approvalBundleId,
                approvalOperationCount,
                approvalCommandId,
                approvalChangedCount,
                originStatus,
                originExpansionId,
                originMatchedRole,
                originMembershipCount,
                cancellationMessage,
                persistentOriginStatus,
                persistentOriginExpansionId,
                componentTexts,
                directCatalogStatus: bridgeService?.requestSemanticMacroCatalog
                    ? (await bridgeService.requestSemanticMacroCatalog())?.status
                    : undefined,
            };
        })();
    `, true);

    console.log(`${ATHENA_REUSE_E2E_RESULT_SENTINEL}${JSON.stringify(summary)}`);
    if (SHOULD_EXIT_ON_SUCCESS) {
        setTimeout(() => app.exit(0), 250);
    }
}

function configureJvmRuntime() {
    const resolver = new AthenaJvmRuntimeResolver();
    return resolver.configureProcessEnvironment(process.env, process.platform);
}

main();
