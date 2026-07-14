import { Emitter, MessageService } from '@theia/core';
import { FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { WorkspaceService } from '@theia/workspace/lib/browser/workspace-service';
export type AthenaRepositorySessionState = {
    lifecycle: 'idle' | 'activating' | 'ready' | 'unavailable';
    repositoryRoot?: string;
    manifestPath?: string;
    lockPath?: string;
    sourceRootPath?: string;
    sourcePath?: string;
    projectName?: string;
    primaryPackageName?: string;
    semanticPath?: string;
    lastOpenedDocumentUri?: string;
    lspLifecycle?: 'idle' | 'starting' | 'ready' | 'unavailable';
    message: string;
};
export declare class AthenaRepositorySessionService implements FrontendApplicationContribution {
    protected readonly workspaceService: WorkspaceService;
    protected readonly messageService: MessageService;
    protected readonly onDidChangeStateEmitter: Emitter<AthenaRepositorySessionState>;
    protected bootstrapOperation: Promise<void> | undefined;
    protected stateValue: AthenaRepositorySessionState;
    get state(): AthenaRepositorySessionState;
    get onDidChangeState(): import("@theia/core").Event<AthenaRepositorySessionState>;
    onStart(_app: FrontendApplication): void;
    protected bootstrapInitialState(): Promise<void>;
    activateCurrentWorkspaceSession(): Promise<void>;
    refreshSessionState(): Promise<void>;
    ensureSessionForDocument(documentUri: string): Promise<AthenaRepositorySessionState>;
    protected setState(state: AthenaRepositorySessionState): void;
}
//# sourceMappingURL=athena-repository-session-service.d.ts.map