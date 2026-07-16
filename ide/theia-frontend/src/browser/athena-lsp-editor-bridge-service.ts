import * as monaco from '@theia/monaco-editor-core';

import URI from '@theia/core/lib/common/uri';
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import { FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { MessageService } from '@theia/core';
import { inject, injectable } from '@theia/core/shared/inversify';
import {
    CompletionItem,
    CompletionList,
    Diagnostic,
    DocumentSymbol,
    Location,
    Range
} from '@theia/core/shared/vscode-languageserver-protocol';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { ProblemManager } from '@theia/markers/lib/browser/problem/problem-manager';
import { OutputChannelManager } from '@theia/output/lib/browser/output-channel';
import type { AthenaComponentKnowledgeSessionPayload } from './athena-component-knowledge-protocol';
import type {
    AthenaAuthoringDecisionParams,
    AthenaAuthoringPreviewDecisionPayload,
    AthenaAuthoringPreviewParams,
    AthenaAuthoringPreviewSubmissionPayload,
    AthenaAuthoringSourceEditPayload
} from './athena-authoring-protocol';
import {
    ATHENA_LANGUAGE_ID,
    athenaLanguageConfiguration,
    athenaMonarchLanguage
} from './athena-language-definition';
import { AthenaTreeSitterHighlightingService } from './athena-tree-sitter-highlighting-service';
import {
    buildAthenaSourceMutationRequest,
    type AthenaSourceMutationPayload
} from './athena-source-mutation-protocol';
import {
    buildAthenaSemanticMacroAcceptanceRequest,
    buildAthenaSemanticMacroCatalogRequest,
    buildAthenaSemanticMacroOriginInspectionRequest,
    buildAthenaSemanticMacroPreviewRequest,
    buildAthenaSemanticMacroValidationRequest,
    type AthenaSemanticMacroAcceptanceParams,
    type AthenaSemanticMacroAcceptancePayload,
    type AthenaSemanticMacroCatalogPayload,
    type AthenaSemanticMacroOriginInspectionParams,
    type AthenaSemanticMacroOriginInspectionPayload,
    type AthenaSemanticMacroPreviewParams,
    type AthenaSemanticMacroPreviewPayload,
    type AthenaSemanticMacroValidationParams,
    type AthenaSemanticMacroValidationPayload
} from './athena-semantic-macro-protocol';
import type {
    AthenaGraphCommandIntentParams,
    AthenaGraphCommandIntentPayload
} from './athena-graph-command-intent-protocol';
import { toAthenaBackendUrl } from './athena-backend-endpoint';
import { AthenaRepositorySessionService } from './athena-repository-session-service';

type AthenaLspTransportEnvelope = {
    method: string;
    params?: unknown;
};

type AthenaTextDocumentPositionParams = {
    textDocument: {
        uri: string;
    };
    position: {
        line: number;
        character: number;
    };
};

type AthenaDocumentSnapshot = {
    uri: string;
    version: number;
    text: string;
    languageId: string;
};

export type AthenaSemanticInspectionComponent = {
    semanticId: string;
    name: string;
    kind: string;
    properties: string;
    authoredProperties: AthenaSemanticInspectionProperty[];
    sourceRange: Range;
};

export type AthenaSemanticInspectionProperty = {
    name: string;
    valueKind: string;
    valueText: string;
};

export type AthenaSemanticInspectionPort = {
    semanticId: string;
    path: string;
    properties: string;
    authoredProperties: AthenaSemanticInspectionProperty[];
    sourceRange: Range;
};

export type AthenaSemanticInspectionConnection = {
    semanticId: string;
    fromPath: string;
    toPath: string;
    sourceRange: Range;
};

export type AthenaSemanticInspectionPayload = {
    uri: string;
    version: number;
    status: string;
    systemName?: string;
    diagnosticsCount: number;
    diagnosticSummaries: string[];
    componentCount: number;
    portCount: number;
    connectionCount: number;
    components: AthenaSemanticInspectionComponent[];
    ports: AthenaSemanticInspectionPort[];
    connections: AthenaSemanticInspectionConnection[];
};

export type AthenaRepositoryManifestDependencyPayload = {
    name: string;
    version?: string;
    source: string;
    locator?: string;
};

export type AthenaRepositoryResolvedPackagePayload = {
    name: string;
    version?: string;
    sourceRoot: string;
    directDependencies: string[];
};

export type AthenaRepositoryDiagnosticPayload = {
    code: string;
    severity: string;
    message: string;
};

export type AthenaSemanticPackagePayload = {
    name: string;
    version?: string;
};

export type AthenaSemanticFactReferencePayload = {
    kind: string;
    identifier: string;
    affectedPackage?: AthenaSemanticPackagePayload;
    subjectIdentity?: string;
};

export type AthenaSemanticScmDiagnosticPayload = {
    severity: string;
    ruleId: string;
    message: string;
    provenance: string;
};

export type AthenaSemanticReviewEntryPayload = {
    kind: string;
    message: string;
    affectedPackage?: AthenaSemanticPackagePayload;
    subjectIdentity?: string;
    factReferences: AthenaSemanticFactReferencePayload[];
};

export type AthenaSemanticReviewEnrichmentPayload = {
    pluginId: string;
    kind: string;
    message: string;
    affectedPackage?: AthenaSemanticPackagePayload;
    subjectIdentity?: string;
    factReferences: AthenaSemanticFactReferencePayload[];
};

export type AthenaSemanticReviewPayload = {
    baselineId: string;
    baselineLabel: string;
    affectedPackages: AthenaSemanticPackagePayload[];
    entryCount: number;
    enrichmentCount: number;
    entries: AthenaSemanticReviewEntryPayload[];
    enrichments: AthenaSemanticReviewEnrichmentPayload[];
};

export type AthenaSemanticCommitEntryPayload = {
    kind: string;
    message: string;
    affectedPackage?: AthenaSemanticPackagePayload;
    subjectIdentity?: string;
    factReferences: AthenaSemanticFactReferencePayload[];
};

export type AthenaSemanticCommitPayload = {
    baselineId: string;
    baselineLabel: string;
    affectedPackages: AthenaSemanticPackagePayload[];
    summary?: string;
    entryCount: number;
    entries: AthenaSemanticCommitEntryPayload[];
};

export type AthenaSemanticScmStatePayload = {
    status: string;
    adapterId: string;
    locator: string;
    locatorLabel?: string;
    baselineId: string;
    baselineLabel: string;
    semanticPath: string;
    diagnostics: AthenaSemanticScmDiagnosticPayload[];
    review?: AthenaSemanticReviewPayload;
    commit?: AthenaSemanticCommitPayload;
};

export type AthenaSemanticScmStateParams = {
    adapterId: string;
    locator: string;
    locatorLabel?: string;
    baselineId?: string;
    baselineLabel?: string;
    metadata?: Record<string, string>;
};

export type AthenaSemanticHistoryBaselineParams = {
    adapterId: string;
    locator: string;
    locatorLabel?: string;
    baselineId?: string;
    baselineLabel?: string;
    metadata?: Record<string, string>;
};

export type AthenaSemanticHistoryStateParams = {
    packageName: string;
    packageVersion?: string;
    baselines: AthenaSemanticHistoryBaselineParams[];
};

export type AthenaSemanticHistoryBaselinePayload = {
    adapterId: string;
    locator: string;
    locatorLabel?: string;
    baselineId: string;
    baselineLabel: string;
};

export type AthenaSemanticPackageVersionPayload = {
    packageId: AthenaSemanticPackagePayload;
    baselineVersion?: string;
    currentVersion?: string;
    changeKind: string;
};

export type AthenaSemanticDependencyMovementPayload = {
    packageId: AthenaSemanticPackagePayload;
    kind: string;
    baselineVersion?: string;
    currentVersion?: string;
    message: string;
};

export type AthenaSemanticValidationMovementPayload = {
    baselineErrorCount: number;
    baselineWarningCount: number;
    currentErrorCount: number;
    currentWarningCount: number;
    baselineContinuationDecision?: string;
    currentContinuationDecision?: string;
    message: string;
};

export type AthenaSemanticHistoryEntryPayload = {
    kind: string;
    baselineId: string;
    baselineLabel: string;
    packageVersion: AthenaSemanticPackageVersionPayload;
    changeCategory?: string;
    releaseRelevance: string;
    contractBreakRisk: string;
    message: string;
    dependencyMovements: AthenaSemanticDependencyMovementPayload[];
    validationMovement?: AthenaSemanticValidationMovementPayload;
    authoredChangeCount: number;
    derivedConsequenceCount: number;
};

export type AthenaSemanticHistoryPayload = {
    packageId: AthenaSemanticPackagePayload;
    baselineCount: number;
    packageLineage: AthenaSemanticPackageVersionPayload[];
    validationMovements: AthenaSemanticValidationMovementPayload[];
    entryCount: number;
    releaseRelevance: string;
    contractBreakRisk: string;
    summary?: string;
    entries: AthenaSemanticHistoryEntryPayload[];
};

export type AthenaSemanticHistoryStatePayload = {
    status: string;
    semanticPath: string;
    packageId: AthenaSemanticPackagePayload;
    baselines: AthenaSemanticHistoryBaselinePayload[];
    diagnostics: AthenaSemanticScmDiagnosticPayload[];
    history?: AthenaSemanticHistoryPayload;
};

export type AthenaAiReasoningRequestParams = {
    requestCategory: 'diagnostic-explanation' | 'impact-summary' | 'next-check';
    subjectSemanticIds?: string[];
    baseline?: AthenaSemanticScmStateParams;
};

export type AthenaAiReasoningEvidencePayload = {
    kind: string;
    referenceId: string;
    summary: string;
};

export type AthenaAiReasoningProposalPayload = {
    proposalId: string;
    proposalCategory: string;
    providerStatus: string;
    decisionState: string;
    summary: string;
    response: string;
    providerId?: string;
    subjectSemanticIds: string[];
    evidence: AthenaAiReasoningEvidencePayload[];
};

export type AthenaAiReasoningSessionPayload = {
    sessionId: string;
    requestCategory: string;
    providerStatus: string;
    providerId?: string;
    subjectSemanticIds: string[];
    proposalId: string;
    semanticPath: string;
};

export type AthenaAiReasoningSubmissionPayload = {
    session: AthenaAiReasoningSessionPayload;
    proposal: AthenaAiReasoningProposalPayload;
};

export type AthenaAiReasoningStatePayload = {
    sessions: AthenaAiReasoningSessionPayload[];
    proposals: AthenaAiReasoningProposalPayload[];
};

export type AthenaAiReasoningDecisionParams = {
    proposalId: string;
    decision: 'accepted' | 'dismissed';
};

export type AthenaRepositoryGraphSessionPayload = {
    repositoryRoot: string;
    manifestPath: string;
    lockPath: string;
    sourceRootPath: string;
    sourcePath: string;
    projectName: string;
    primaryPackageName: string;
    semanticPath: string;
    lastOpenedDocumentUri?: string;
    lockState: string;
    isValid: boolean;
    manifestDependencies: AthenaRepositoryManifestDependencyPayload[];
    resolvedPackages: AthenaRepositoryResolvedPackagePayload[];
    diagnostics: AthenaRepositoryDiagnosticPayload[];
};

export type AthenaProjectionViewPayload = {
    viewId: string;
    displayName: string;
    description: string;
    familyId?: string;
    ownershipContract: AthenaProjectionOwnershipContractPayload;
};

export type AthenaProjectionOwnershipContractPayload = {
    interactivity: string;
    displayScopes: string[];
    semanticCommandIds: string[];
    projectionCommandIds: string[];
    transientInteractionKinds: string[];
    persistedProjectionMetadataKeys: string[];
};

export type AthenaProjectionGovernedCommandPayload = {
    commandId: string;
    displayName: string;
    description: string;
    requiredArguments: string[];
};

export type AthenaProjectionComponentPayload = {
    projectionId: string;
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaProjectionConnectionPayload = {
    projectionId: string;
    semanticId: string;
    x1: number;
    y1: number;
    x2: number;
    y2: number;
};

export type AthenaProjectionLabelPayload = {
    projectionId: string;
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaProjectionSheetPayload = {
    sheetId: string;
    displayName: string;
    order: number;
    previousSheetId?: string;
    nextSheetId?: string;
    subjectSemanticIds: string[];
    publication: AthenaProjectionSheetPublicationPayload;
};

export type AthenaProjectionSheetPublicationPayload = {
    pageSize: AthenaProjectionSheetPageSizePayload;
    frame: AthenaProjectionSheetFramePayload;
    coordinateZones: AthenaProjectionSheetCoordinateZonePayload[];
    titleBlock: AthenaProjectionSheetTitleBlockPayload;
    revisionMetadata: AthenaProjectionSheetRevisionMetadataPayload;
    viewComposition: AthenaProjectionSheetViewCompositionPayload;
};

export type AthenaProjectionSheetPageSizePayload = {
    format: string;
    orientation: string;
};

export type AthenaProjectionSheetFramePayload = {
    frameId: string;
    style: string;
};

export type AthenaProjectionSheetCoordinateZonePayload = {
    zoneId: string;
    label: string;
    order: number;
};

export type AthenaProjectionSheetTitleBlockPayload = {
    sheetTitle: string;
    sheetFamily: string;
    sheetNumber: string;
};

export type AthenaProjectionSheetRevisionMetadataPayload = {
    revisionCode: string;
    revisionNote: string;
};

export type AthenaProjectionSheetViewCompositionPayload = {
    primaryViewId: string;
    primarySheetOrder: number;
    subjectSemanticIds: string[];
};

export type AthenaProjectionNotationSubjectPayload = {
    semanticId: string;
    symbolKey: string;
    labelPolicy: string;
    markerKeys: string[];
};

export type AthenaProjectionNotationPackPayload = {
    packId: string;
    displayName: string;
    subjects: AthenaProjectionNotationSubjectPayload[];
};

export type AthenaProjectionCrossReferencePayload = {
    semanticId: string;
    kind: string;
    sheetIds: string[];
    occurrenceIds: string[];
};

export type AthenaProjectionPointPayload = {
    x: number;
    y: number;
};

export type AthenaProjectionSheetLayoutPayload = {
    sheetId: string;
    displayName: string;
    order: number;
    subjectSemanticIds: string[];
    representationFamilyId: string;
    frame: AthenaProjectionSheetLayoutFramePayload;
    placements: AthenaProjectionSheetLayoutPlacementPayload[];
    routingGuidance: AthenaProjectionSheetLayoutRoutingGuidancePayload[];
    labelLayouts: AthenaProjectionSheetLayoutLabelLayoutPayload[];
};

export type AthenaProjectionSheetLayoutFramePayload = {
    canvasWidth: number;
    canvasHeight: number;
    gridMajorStep: number;
    gridMinorStep: number;
};

export type AthenaProjectionSheetLayoutPlacementPayload = {
    projectionId: string;
    semanticId: string;
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaProjectionSheetLayoutRoutingGuidancePayload = {
    projectionConnectionId: string;
    connectionSemanticId: string;
    sourcePoint: AthenaProjectionPointPayload;
    targetPoint: AthenaProjectionPointPayload;
    routingStyle: string;
    bendPoints: AthenaProjectionPointPayload[];
};

export type AthenaProjectionSheetLayoutLabelLayoutPayload = {
    projectionId: string;
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaProjectionReadyPayload = {
    viewId: string;
    familyId?: string;
    systemName: string;
    canvasWidth: number;
    canvasHeight: number;
    activeSheetId?: string;
    sheets: AthenaProjectionSheetPayload[];
    sheetLayout?: AthenaProjectionSheetLayoutPayload;
    notationPack?: AthenaProjectionNotationPackPayload;
    crossReferences: AthenaProjectionCrossReferencePayload[];
    activeRenderContributions: AthenaProjectionRenderContributionPayload[];
    components: AthenaProjectionComponentPayload[];
    connections: AthenaProjectionConnectionPayload[];
    labels: AthenaProjectionLabelPayload[];
};

export type AthenaProjectionRenderContributionPayload = {
    pluginId: string;
    contributionId: string;
    displayName: string;
    description: string;
    rendererTarget: string;
    surfaceMappings: AthenaProjectionSurfaceMappingPayload[];
};

export type AthenaProjectionSurfaceMappingPayload = {
    surface: string;
    tokens: Record<string, string>;
};

export type AthenaProjectionDiagnosticPayload = {
    severity: string;
    code: string;
    message: string;
    provenance?: string;
};

export type AthenaProjectionCommandParams = {
    commandId: string;
    viewId?: string;
};

export type AthenaProjectionSessionPayload = {
    projectName: string;
    semanticPath: string;
    activeViewId: string;
    supportedViews: AthenaProjectionViewPayload[];
    governedCommands: AthenaProjectionGovernedCommandPayload[];
    status: string;
    readyProjection?: AthenaProjectionReadyPayload;
    unavailableReason?: string;
    diagnostics: AthenaProjectionDiagnosticPayload[];
};

export type AthenaProjectionCommandPayload = {
    commandId: string;
    status: string;
    reason?: string;
    session?: AthenaProjectionSessionPayload;
};

export type {
    AthenaMutationValidationFeedbackPayload,
    AthenaProjectionRefreshConsequencePayload,
    AthenaSemanticDiffEntryPayload,
    AthenaSemanticDiffInspectionPayload,
    AthenaSemanticHistoryConsequencePayload,
    AthenaSourceMutationParams,
    AthenaSourceMutationTextDocument
} from './athena-source-mutation-protocol';

export type {
    AthenaGraphCommandIntentParams,
    AthenaGraphCommandIntentPayload
} from './athena-graph-command-intent-protocol';

@injectable()
export class AthenaLspEditorBridgeService implements FrontendApplicationContribution {
    protected static readonly MARKER_OWNER = 'athena-lsp';

    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(ProblemManager)
    protected readonly problemManager: ProblemManager;

    @inject(OutputChannelManager)
    protected readonly outputChannelManager: OutputChannelManager;

    @inject(MessageService)
    protected readonly messageService: MessageService;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaTreeSitterHighlightingService)
    protected readonly treeSitterHighlightingService: AthenaTreeSitterHighlightingService;

    protected readonly openedDocumentVersions = new Map<string, number>();
    protected readonly documentSyncOperations = new Map<string, Promise<void>>();
    protected readonly documentSymbolProviderDidChangeEmitter = new monaco.Emitter<void>();
    protected activeEditorListeners = new DisposableCollection();
    protected languageProviderListeners = new DisposableCollection();
    protected semanticBoundaryMessageShown = false;

    onStart(_app: FrontendApplication): void {
        this.registerAthenaLanguage();
        this.outputChannel.appendLine('Athena semantic path ready: frontend -> LSP -> runtime/compiler');

        this.editorManager.onCurrentEditorChanged(widget => {
            this.bindCurrentEditor(widget);
            void this.forwardDidOpen(widget).catch(error => this.reportBridgeFailure(error));
        });
        this.repositorySessionService.onDidChangeState(state => {
            this.notifyDocumentSymbolProviderChanged();
            if (state.lifecycle === 'ready') {
                void this.forwardDidOpen(this.editorManager.currentEditor).catch(error => this.reportBridgeFailure(error));
                return;
            }
            this.problemManager.cleanAllMarkers();
            this.openedDocumentVersions.clear();
            this.documentSyncOperations.clear();
        });

        this.bindCurrentEditor(this.editorManager.currentEditor);
        void this.forwardDidOpen(this.editorManager.currentEditor).catch(error => this.reportBridgeFailure(error));
    }

    protected registerAthenaLanguage(): void {
        const alreadyRegistered = monaco.languages.getLanguages().some(language => language.id === ATHENA_LANGUAGE_ID);
        if (!alreadyRegistered) {
            monaco.languages.register({
                id: ATHENA_LANGUAGE_ID,
                extensions: ['.athena'],
                aliases: ['Athena', 'athena']
            });
        }
        monaco.languages.setLanguageConfiguration(ATHENA_LANGUAGE_ID, athenaLanguageConfiguration);
        this.languageProviderListeners.push(monaco.editor.onDidCreateModel(model => {
            this.ensureAthenaModelLanguage(model);
        }));
        monaco.editor.getModels().forEach(model => {
            this.ensureAthenaModelLanguage(model);
        });
        this.registerAthenaLanguageProviders();
    }

    protected registerAthenaLanguageProviders(): void {
        this.languageProviderListeners.dispose();
        this.languageProviderListeners = new DisposableCollection();

        this.languageProviderListeners.push(
            monaco.languages.setMonarchTokensProvider(ATHENA_LANGUAGE_ID, athenaMonarchLanguage)
        );

        // Tree-sitter-backed syntax highlighting (Story 3.2, AD-107): layers on top of the
        // Monarch tokenizer above; it is a syntax-classification aid only and must never emit
        // diagnostics/markers (AD-108 stays owned by the sendLanguageRequest(...) path below).
        this.languageProviderListeners.push(monaco.languages.registerDocumentSemanticTokensProvider(ATHENA_LANGUAGE_ID, {
            getLegend: () => this.treeSitterHighlightingService.getLegend(),
            provideDocumentSemanticTokens: async model => {
                const tokens = await this.treeSitterHighlightingService.provideDocumentSemanticTokens(model);
                return tokens ?? null;
            },
            releaseDocumentSemanticTokens: () => undefined
        }));

        this.languageProviderListeners.push(monaco.languages.registerCompletionItemProvider(ATHENA_LANGUAGE_ID, {
            triggerCharacters: ['.', ' '],
            provideCompletionItems: async (model, position) => {
                const payload = await this.sendLanguageRequest<CompletionItem[] | CompletionList>(
                    'textDocument/completion',
                    this.toTextDocumentPositionParams(model, position),
                    model
                );
                const items = Array.isArray(payload) ? payload : payload?.items ?? [];
                return {
                    suggestions: items.map(item => this.toMonacoCompletion(item, model, position))
                };
            }
        }));

        const documentSymbolProvider = {
            onDidChange: this.documentSymbolProviderDidChangeEmitter.event,
            provideDocumentSymbols: async model => {
                const payload = await this.sendLanguageRequest<DocumentSymbol[]>(
                    'textDocument/documentSymbol',
                    {
                        textDocument: {
                            uri: model.uri.toString()
                        }
                    },
                    model
                );
                return (payload ?? []).map(symbol => this.toMonacoDocumentSymbol(symbol));
            }
        };
        this.languageProviderListeners.push(monaco.languages.registerDocumentSymbolProvider(ATHENA_LANGUAGE_ID, documentSymbolProvider));

        this.languageProviderListeners.push(monaco.languages.registerDefinitionProvider(ATHENA_LANGUAGE_ID, {
            provideDefinition: async (model, position) => {
                const payload = await this.sendLanguageRequest<Location[]>(
                    'textDocument/definition',
                    this.toTextDocumentPositionParams(model, position),
                    model
                );
                return (payload ?? []).map(location => this.toMonacoLocation(location));
            }
        }));

        this.languageProviderListeners.push(monaco.languages.registerReferenceProvider(ATHENA_LANGUAGE_ID, {
            provideReferences: async (model, position) => {
                const payload = await this.sendLanguageRequest<Location[]>(
                    'textDocument/references',
                    {
                        ...this.toTextDocumentPositionParams(model, position),
                        context: {
                            includeDeclaration: true
                        }
                    },
                    model
                );
                return (payload ?? []).map(location => this.toMonacoLocation(location));
            }
        }));
    }

    protected async forwardDidOpen(widget: EditorWidget | undefined): Promise<void> {
        if (!this.isAthenaEditor(widget)) {
            return;
        }
        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
    }

    protected async forwardDidChange(widget: EditorWidget): Promise<void> {
        if (!this.isAthenaEditor(widget)) {
            return;
        }
        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
    }

    protected async ensureDocumentSynchronized(model: monaco.editor.ITextModel): Promise<void> {
        this.ensureAthenaModelLanguage(model);
        const snapshot = await this.resolveDocumentSnapshot(model);
        if (!snapshot || !this.isAthenaDocumentUri(snapshot.uri)) {
            return;
        }
        await this.synchronizeDocumentSnapshot(snapshot);
    }

    protected async resolveDocumentSnapshot(model: monaco.editor.ITextModel): Promise<AthenaDocumentSnapshot | undefined> {
        const uri = model.uri.toString();
        const widget = await this.editorManager.getByUri(new URI(uri));
        if (this.isAthenaEditor(widget)) {
            return this.toWidgetSnapshot(widget);
        }
        return {
            uri,
            version: model.getVersionId(),
            text: model.getValue(),
            languageId: model.getLanguageId()
        };
    }

    protected toWidgetSnapshot(widget: EditorWidget): AthenaDocumentSnapshot {
        return {
            uri: widget.editor.uri.toString(),
            version: widget.editor.document.version,
            text: widget.editor.document.getText(),
            languageId: ATHENA_LANGUAGE_ID
        };
    }

    protected async synchronizeDocumentSnapshot(snapshot: AthenaDocumentSnapshot): Promise<void> {
        await this.repositorySessionService.ensureSessionForDocument(snapshot.uri);
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            this.outputChannel.appendLine(
                `Skipped document synchronization for ${snapshot.uri} because the repository session is ${sessionState.lifecycle}.`
            );
            return;
        }

        await this.enqueueDocumentSync(snapshot.uri, async () => {
            const currentVersion = this.openedDocumentVersions.get(snapshot.uri);
            if (currentVersion === snapshot.version) {
                return;
            }

            const method = currentVersion === undefined
                ? 'textDocument/didOpen'
                : 'textDocument/didChange';
            const response = await fetch(toAthenaBackendUrl('athena/lsp/notify'), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    method,
                    params: method === 'textDocument/didOpen'
                        ? {
                            textDocument: {
                                uri: snapshot.uri,
                                languageId: snapshot.languageId,
                                version: snapshot.version,
                                text: snapshot.text
                            }
                        }
                        : {
                            textDocument: {
                                uri: snapshot.uri,
                                version: snapshot.version
                            },
                            contentChanges: [{
                                text: snapshot.text
                            }]
                        }
                } satisfies AthenaLspTransportEnvelope)
            });

            if (!response.ok) {
                const failure = await response.json() as { message?: string };
                throw new Error(failure.message ?? `Athena LSP ${method} bridge failed for ${snapshot.uri}`);
            }

            this.openedDocumentVersions.set(snapshot.uri, snapshot.version);
            if (method === 'textDocument/didOpen') {
                this.outputChannel.appendLine(`frontend -> textDocument/didOpen -> Athena LSP -> runtime/compiler :: ${snapshot.uri}`);
                this.outputChannel.show({ preserveFocus: true });
                void this.repositorySessionService.refreshSessionState().catch(error => this.reportBridgeFailure(error));
                if (!this.semanticBoundaryMessageShown) {
                    this.semanticBoundaryMessageShown = true;
                    void this.messageService.info('Athena .athena files now flow through Athena LSP as the sole semantic boundary.');
                }
            } else {
                this.outputChannel.appendLine(`frontend -> textDocument/didChange -> Athena LSP diagnostics :: ${snapshot.uri} @ v${snapshot.version}`);
            }
            this.notifyDocumentSymbolProviderChanged();
            void this.syncPublishedDiagnosticsEventually(snapshot.uri).catch(error => this.reportBridgeFailure(error));
        });
    }

    protected enqueueDocumentSync(uri: string, task: () => Promise<void>): Promise<void> {
        const previous = this.documentSyncOperations.get(uri) ?? Promise.resolve();
        const next = previous
            .catch(() => undefined)
            .then(task);
        const tracked = next.finally(() => {
            if (this.documentSyncOperations.get(uri) === tracked) {
                this.documentSyncOperations.delete(uri);
            }
        });
        this.documentSyncOperations.set(uri, tracked);
        return tracked;
    }

    protected bindCurrentEditor(widget: EditorWidget | undefined): void {
        this.activeEditorListeners.dispose();
        this.activeEditorListeners = new DisposableCollection();
        if (!this.isAthenaEditor(widget)) {
            return;
        }

        const model = monaco.editor.getModel(monaco.Uri.parse(widget.editor.uri.toString()));
        this.ensureAthenaModelLanguage(model);

        this.activeEditorListeners.push(widget.editor.onDocumentContentChanged(() => {
            void this.forwardDidChange(widget).catch(error => this.reportBridgeFailure(error));
        }));
    }

    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget {
        if (!widget) {
            return false;
        }
        return this.isAthenaDocumentUri(widget.editor.uri.toString());
    }

    protected isAthenaDocumentUri(uri: string): boolean {
        return uri.toLowerCase().endsWith('.athena');
    }

    protected currentAthenaEditorModel(): monaco.editor.ITextModel | undefined {
        const widget = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(widget)) {
            return undefined;
        }
        const model = monaco.editor.getModel(monaco.Uri.parse(widget.editor.uri.toString())) ?? undefined;
        this.ensureAthenaModelLanguage(model);
        return model;
    }

    protected ensureAthenaModelLanguage(model: monaco.editor.ITextModel | undefined): void {
        if (!model || !this.isAthenaDocumentUri(model.uri.toString()) || model.getLanguageId() === ATHENA_LANGUAGE_ID) {
            return;
        }
        monaco.editor.setModelLanguage(model, ATHENA_LANGUAGE_ID);
        this.notifyDocumentSymbolProviderChanged();
    }

    protected async syncPublishedDiagnostics(uri: string): Promise<number> {
        const response = await fetch(toAthenaBackendUrl('athena/lsp/diagnostics', {
            uri,
        }));
        if (!response.ok) {
            const failure = await response.json() as { message?: string };
            throw new Error(failure.message ?? `Athena diagnostics fetch failed for ${uri}`);
        }

        const payload = await response.json() as Array<{ uri: string; diagnostics: Diagnostic[] }>;
        const diagnostics = payload[0]?.diagnostics ?? [];
        this.problemManager.setMarkers(new URI(uri), AthenaLspEditorBridgeService.MARKER_OWNER, diagnostics);
        this.outputChannel.appendLine(`Athena diagnostics synced to editor and Problems: ${diagnostics.length} item(s) for ${uri}`);
        return diagnostics.length;
    }

    protected async syncPublishedDiagnosticsEventually(uri: string): Promise<void> {
        for (let attempt = 0; attempt < 5; attempt += 1) {
            const diagnosticCount = await this.syncPublishedDiagnostics(uri);
            if (diagnosticCount > 0 || attempt === 4) {
                return;
            }
            await new Promise(resolve => window.setTimeout(resolve, 100));
        }
    }

    protected get outputChannel() {
        return this.outputChannelManager.getChannel('Athena LSP');
    }

    async requestSemanticInspection(widget: EditorWidget | undefined): Promise<AthenaSemanticInspectionPayload | undefined> {
        if (!this.isAthenaEditor(widget)) {
            return undefined;
        }

        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
        return this.sendLanguageRequest<AthenaSemanticInspectionPayload>('athena/semanticInspection', {
            textDocument: {
                uri: widget.editor.uri.toString()
            }
        });
    }

    async requestSourceMutationEvaluation(widget: EditorWidget | undefined): Promise<AthenaSourceMutationPayload | undefined> {
        if (!this.isAthenaEditor(widget)) {
            return undefined;
        }

        await this.synchronizeDocumentSnapshot(this.toWidgetSnapshot(widget));
        const request = buildAthenaSourceMutationRequest(
            widget.editor.uri.toString(),
            this.currentAthenaEditorModel(),
        );
        return this.sendLanguageRequest<AthenaSourceMutationPayload>(
            request.method,
            request.params,
            request.model,
        );
    }

    async requestRepositoryGraphSession(): Promise<AthenaRepositoryGraphSessionPayload | undefined> {
        return this.sendLanguageRequest<AthenaRepositoryGraphSessionPayload>(
            'athena/repositoryGraphSession',
            {}
        );
    }

    async requestComponentKnowledgeSession(): Promise<AthenaComponentKnowledgeSessionPayload | undefined> {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest<AthenaComponentKnowledgeSessionPayload>(
            'athena/componentKnowledgeSession',
            { marker: 'm15' },
            model,
        );
    }

    async requestSemanticMacroCatalog(): Promise<AthenaSemanticMacroCatalogPayload | undefined> {
        const request = buildAthenaSemanticMacroCatalogRequest();
        return this.sendLanguageRequest<AthenaSemanticMacroCatalogPayload>(
            request.method,
            request.params,
        );
    }

    async requestSemanticMacroValidation(
        params: AthenaSemanticMacroValidationParams
    ): Promise<AthenaSemanticMacroValidationPayload | undefined> {
        const request = buildAthenaSemanticMacroValidationRequest(params);
        return this.sendLanguageRequest<AthenaSemanticMacroValidationPayload>(
            request.method,
            request.params,
        );
    }

    async requestSemanticMacroPreview(
        params: AthenaSemanticMacroPreviewParams
    ): Promise<AthenaSemanticMacroPreviewPayload | undefined> {
        const request = buildAthenaSemanticMacroPreviewRequest(params);
        return this.sendLanguageRequest<AthenaSemanticMacroPreviewPayload>(
            request.method,
            request.params,
        );
    }

    async requestSemanticMacroAcceptance(
        params: AthenaSemanticMacroAcceptanceParams
    ): Promise<AthenaSemanticMacroAcceptancePayload | undefined> {
        const request = buildAthenaSemanticMacroAcceptanceRequest(params);
        return this.sendLanguageRequest<AthenaSemanticMacroAcceptancePayload>(
            request.method,
            request.params,
        );
    }

    async requestSemanticMacroOriginInspection(
        params: AthenaSemanticMacroOriginInspectionParams
    ): Promise<AthenaSemanticMacroOriginInspectionPayload | undefined> {
        const request = buildAthenaSemanticMacroOriginInspectionRequest(params);
        return this.sendLanguageRequest<AthenaSemanticMacroOriginInspectionPayload>(
            request.method,
            request.params,
        );
    }

    async requestAuthoringPreview(
        params: AthenaAuthoringPreviewParams
    ): Promise<AthenaAuthoringPreviewSubmissionPayload | undefined> {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest<AthenaAuthoringPreviewSubmissionPayload>(
            'athena/authoringPreview',
            params,
            model,
        );
    }

    async requestAuthoringDecision(
        params: AthenaAuthoringDecisionParams
    ): Promise<AthenaAuthoringPreviewDecisionPayload | undefined> {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest<AthenaAuthoringPreviewDecisionPayload>(
            'athena/authoringDecision',
            params,
            model,
        );
    }

    applyAuthoringSourceEdit(edit: AthenaAuthoringSourceEditPayload): void {
        const model = monaco.editor.getModel(monaco.Uri.parse(edit.uri)) ?? this.currentAthenaEditorModel();
        if (!model || model.uri.toString() !== edit.uri) {
            throw new Error(`Athena authoring source edit target is not open: ${edit.uri}`);
        }
        model.pushEditOperations(
            [],
            [{
                range: new monaco.Range(
                    edit.range.start.line + 1,
                    edit.range.start.character + 1,
                    edit.range.end.line + 1,
                    edit.range.end.character + 1,
                ),
                text: edit.newText,
                forceMoveMarkers: true,
            }],
            () => null,
        );
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor) || currentEditor.editor.uri.toString() !== edit.uri || !edit.selectionRange) {
            return;
        }
        currentEditor.editor.selection = {
            ...edit.selectionRange,
            direction: 'ltr',
        };
        currentEditor.editor.revealRange(edit.selectionRange, { at: 'center' });
    }

    async requestProjectionSession(): Promise<AthenaProjectionSessionPayload | undefined> {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest<AthenaProjectionSessionPayload>(
            'athena/projectionSession',
            {},
            model,
        );
    }

    async requestProjectionCommand(
        params: AthenaProjectionCommandParams
    ): Promise<AthenaProjectionCommandPayload | undefined> {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest<AthenaProjectionCommandPayload>(
            'athena/projectionCommand',
            params,
            model,
        );
    }

    async requestGraphCommandIntent(
        params: AthenaGraphCommandIntentParams
    ): Promise<AthenaGraphCommandIntentPayload | undefined> {
        const model = this.currentAthenaEditorModel();
        return this.sendLanguageRequest<AthenaGraphCommandIntentPayload>(
            'athena/graphCommandIntent',
            params,
            model,
        );
    }

    async requestSemanticScmState(
        params: AthenaSemanticScmStateParams
    ): Promise<AthenaSemanticScmStatePayload | undefined> {
        return this.sendLanguageRequest<AthenaSemanticScmStatePayload>(
            'athena/semanticScmState',
            params
        );
    }

    async requestSemanticHistoryState(
        params: AthenaSemanticHistoryStateParams
    ): Promise<AthenaSemanticHistoryStatePayload | undefined> {
        return this.sendLanguageRequest<AthenaSemanticHistoryStatePayload>(
            'athena/semanticHistoryState',
            params
        );
    }

    async requestAiReasoning(
        params: AthenaAiReasoningRequestParams
    ): Promise<AthenaAiReasoningSubmissionPayload | undefined> {
        return this.sendLanguageRequest<AthenaAiReasoningSubmissionPayload>(
            'athena/aiReasoning',
            params
        );
    }

    async requestAiReasoningState(): Promise<AthenaAiReasoningStatePayload | undefined> {
        return this.sendLanguageRequest<AthenaAiReasoningStatePayload>(
            'athena/aiReasoningState',
            {}
        );
    }

    async requestAiReasoningDecision(
        params: AthenaAiReasoningDecisionParams
    ): Promise<AthenaAiReasoningProposalPayload | undefined> {
        return this.sendLanguageRequest<AthenaAiReasoningProposalPayload>(
            'athena/aiReasoningDecision',
            params
        );
    }

    protected async sendLanguageRequest<T>(
        method: string,
        params: unknown,
        model?: monaco.editor.ITextModel
    ): Promise<T | undefined> {
        if (model) {
            await this.ensureDocumentSynchronized(model);
        }
        if (this.repositorySessionService.state.lifecycle !== 'ready') {
            return undefined;
        }

        const response = await fetch(toAthenaBackendUrl('athena/lsp/request'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                method,
                params
            } satisfies AthenaLspTransportEnvelope)
        });

        if (!response.ok) {
            const failure = await response.json() as { message?: string };
            throw new Error(failure.message ?? `Athena LSP request failed for ${method}`);
        }

        const payload = await response.json() as { result?: T };
        return payload.result;
    }

    protected toTextDocumentPositionParams(
        model: monaco.editor.ITextModel,
        position: monaco.Position
    ): AthenaTextDocumentPositionParams {
        return {
            textDocument: {
                uri: model.uri.toString()
            },
            position: {
                line: position.lineNumber - 1,
                character: position.column - 1
            }
        };
    }

    protected toMonacoCompletion(
        item: CompletionItem,
        model: monaco.editor.ITextModel,
        position: monaco.Position
    ): monaco.languages.CompletionItem {
        const label = item.label;
        const documentation = typeof item.documentation === 'string'
            ? item.documentation
            : item.documentation?.value;
        return {
            label,
            detail: item.detail,
            documentation,
            insertText: item.insertText ?? label,
            kind: this.toMonacoCompletionKind(item.kind),
            range: this.toMonacoCompletionRange(model, position)
        };
    }

    protected toMonacoCompletionRange(
        model: monaco.editor.ITextModel,
        position: monaco.Position
    ): monaco.IRange {
        const lineContent = model.getLineContent(position.lineNumber);
        let startColumn = position.column;
        while (startColumn > 1 && /[A-Za-z0-9_.]/.test(lineContent[startColumn - 2] ?? '')) {
            startColumn -= 1;
        }
        return {
            startLineNumber: position.lineNumber,
            startColumn,
            endLineNumber: position.lineNumber,
            endColumn: position.column
        };
    }

    protected toMonacoCompletionKind(kind?: number): monaco.languages.CompletionItemKind {
        switch (kind) {
            case 5:
                return monaco.languages.CompletionItemKind.Field;
            case 6:
                return monaco.languages.CompletionItemKind.Variable;
            case 7:
                return monaco.languages.CompletionItemKind.Class;
            case 10:
                return monaco.languages.CompletionItemKind.Property;
            case 13:
                return monaco.languages.CompletionItemKind.EnumMember;
            case 14:
                return monaco.languages.CompletionItemKind.Keyword;
            case 18:
                return monaco.languages.CompletionItemKind.Reference;
            default:
                return monaco.languages.CompletionItemKind.Text;
        }
    }

    protected toMonacoDocumentSymbol(symbol: DocumentSymbol): monaco.languages.DocumentSymbol {
        return {
            name: symbol.name,
            detail: symbol.detail ?? '',
            kind: this.toMonacoSymbolKind(symbol.kind),
            tags: symbol.tags ?? [],
            range: this.toMonacoRange(symbol.range),
            selectionRange: this.toMonacoRange(symbol.selectionRange),
            children: (symbol.children ?? []).map(child => this.toMonacoDocumentSymbol(child))
        };
    }

    protected toMonacoSymbolKind(kind: number): monaco.languages.SymbolKind {
        switch (kind) {
            case 2:
                return monaco.languages.SymbolKind.Module;
            case 5:
                return monaco.languages.SymbolKind.Class;
            case 7:
                return monaco.languages.SymbolKind.Property;
            case 8:
                return monaco.languages.SymbolKind.Field;
            case 25:
                return monaco.languages.SymbolKind.Function;
            default:
                return monaco.languages.SymbolKind.Object;
        }
    }

    protected toMonacoLocation(location: Location): monaco.languages.Location {
        return {
            uri: monaco.Uri.parse(location.uri),
            range: this.toMonacoRange(location.range)
        };
    }

    protected toMonacoRange(range: { start: { line: number; character: number }; end: { line: number; character: number } }): monaco.IRange {
        return {
            startLineNumber: range.start.line + 1,
            startColumn: range.start.character + 1,
            endLineNumber: range.end.line + 1,
            endColumn: range.end.character + 1
        };
    }

    protected reportBridgeFailure(error: unknown): void {
        const message = error instanceof Error ? error.message : String(error);
        this.outputChannel.appendLine(`Athena LSP bridge failure: ${message}`);
        void this.messageService.warn(`Athena LSP bridge failure: ${message}`);
    }

    protected notifyDocumentSymbolProviderChanged(): void {
        this.documentSymbolProviderDidChangeEmitter.fire();
    }
}
