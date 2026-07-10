import * as monaco from '@theia/monaco-editor-core';
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import { FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { MessageService } from '@theia/core';
import { CompletionItem, DocumentSymbol, Location, Range } from '@theia/core/shared/vscode-languageserver-protocol';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { ProblemManager } from '@theia/markers/lib/browser/problem/problem-manager';
import { OutputChannelManager } from '@theia/output/lib/browser/output-channel';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
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
    sourceRange: Range;
};
export type AthenaSemanticInspectionPort = {
    semanticId: string;
    path: string;
    properties: string;
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
};
export type AthenaProjectionGovernedCommandPayload = {
    commandId: string;
    displayName: string;
    description: string;
    requiredArguments: string[];
};
export type AthenaProjectionComponentPayload = {
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};
export type AthenaProjectionConnectionPayload = {
    semanticId: string;
    x1: number;
    y1: number;
    x2: number;
    y2: number;
};
export type AthenaProjectionLabelPayload = {
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};
export type AthenaProjectionReadyPayload = {
    viewId: string;
    systemName: string;
    canvasWidth: number;
    canvasHeight: number;
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
export declare class AthenaLspEditorBridgeService implements FrontendApplicationContribution {
    protected static readonly MARKER_OWNER = "athena-lsp";
    protected readonly editorManager: EditorManager;
    protected readonly problemManager: ProblemManager;
    protected readonly outputChannelManager: OutputChannelManager;
    protected readonly messageService: MessageService;
    protected readonly repositorySessionService: AthenaRepositorySessionService;
    protected readonly openedDocumentVersions: Map<string, number>;
    protected readonly documentSyncOperations: Map<string, Promise<void>>;
    protected activeEditorListeners: DisposableCollection;
    protected languageProviderListeners: DisposableCollection;
    protected semanticBoundaryMessageShown: boolean;
    onStart(_app: FrontendApplication): Promise<void>;
    protected registerAthenaLanguage(): void;
    protected registerAthenaLanguageProviders(): void;
    protected forwardDidOpen(widget: EditorWidget | undefined): Promise<void>;
    protected forwardDidChange(widget: EditorWidget): Promise<void>;
    protected ensureDocumentSynchronized(model: monaco.editor.ITextModel): Promise<void>;
    protected resolveDocumentSnapshot(model: monaco.editor.ITextModel): Promise<AthenaDocumentSnapshot | undefined>;
    protected toWidgetSnapshot(widget: EditorWidget): AthenaDocumentSnapshot;
    protected synchronizeDocumentSnapshot(snapshot: AthenaDocumentSnapshot): Promise<void>;
    protected enqueueDocumentSync(uri: string, task: () => Promise<void>): Promise<void>;
    protected bindCurrentEditor(widget: EditorWidget | undefined): void;
    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget;
    protected isAthenaDocumentUri(uri: string): boolean;
    protected currentAthenaEditorModel(): monaco.editor.ITextModel | undefined;
    protected syncPublishedDiagnostics(uri: string): Promise<void>;
    protected get outputChannel(): import("@theia/output/lib/browser/output-channel").OutputChannel;
    requestSemanticInspection(widget: EditorWidget | undefined): Promise<AthenaSemanticInspectionPayload | undefined>;
    requestRepositoryGraphSession(): Promise<AthenaRepositoryGraphSessionPayload | undefined>;
    requestProjectionSession(): Promise<AthenaProjectionSessionPayload | undefined>;
    requestProjectionCommand(params: AthenaProjectionCommandParams): Promise<AthenaProjectionCommandPayload | undefined>;
    requestSemanticScmState(params: AthenaSemanticScmStateParams): Promise<AthenaSemanticScmStatePayload | undefined>;
    requestSemanticHistoryState(params: AthenaSemanticHistoryStateParams): Promise<AthenaSemanticHistoryStatePayload | undefined>;
    protected sendLanguageRequest<T>(method: string, params: unknown, model?: monaco.editor.ITextModel): Promise<T | undefined>;
    protected toTextDocumentPositionParams(model: monaco.editor.ITextModel, position: monaco.Position): AthenaTextDocumentPositionParams;
    protected toMonacoCompletion(item: CompletionItem, model: monaco.editor.ITextModel, position: monaco.Position): monaco.languages.CompletionItem;
    protected toMonacoCompletionRange(model: monaco.editor.ITextModel, position: monaco.Position): monaco.IRange;
    protected toMonacoCompletionKind(kind?: number): monaco.languages.CompletionItemKind;
    protected toMonacoDocumentSymbol(symbol: DocumentSymbol): monaco.languages.DocumentSymbol;
    protected toMonacoSymbolKind(kind: number): monaco.languages.SymbolKind;
    protected toMonacoLocation(location: Location): monaco.languages.Location;
    protected toMonacoRange(range: {
        start: {
            line: number;
            character: number;
        };
        end: {
            line: number;
            character: number;
        };
    }): monaco.IRange;
    protected reportBridgeFailure(error: unknown): void;
}
export {};
//# sourceMappingURL=athena-lsp-editor-bridge-service.d.ts.map