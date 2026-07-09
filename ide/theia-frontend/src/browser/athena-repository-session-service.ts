import { Emitter, MessageService } from '@theia/core';
import { FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { inject, injectable } from '@theia/core/shared/inversify';
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

@injectable()
export class AthenaRepositorySessionService implements FrontendApplicationContribution {
    @inject(WorkspaceService)
    protected readonly workspaceService: WorkspaceService;

    @inject(MessageService)
    protected readonly messageService: MessageService;

    protected readonly onDidChangeStateEmitter = new Emitter<AthenaRepositorySessionState>();
    protected stateValue: AthenaRepositorySessionState = {
        lifecycle: 'idle',
        lspLifecycle: 'idle',
        message: 'No Engineering Repository session is active.'
    };

    get state(): AthenaRepositorySessionState {
        return this.stateValue;
    }

    get onDidChangeState() {
        return this.onDidChangeStateEmitter.event;
    }

    async onStart(_app: FrontendApplication): Promise<void> {
        await this.workspaceService.ready;
        if (!this.workspaceService.opened) {
            await this.refreshSessionState();
            return;
        }
        await this.activateCurrentWorkspaceSession();
    }

    async activateCurrentWorkspaceSession(): Promise<void> {
        const roots = await this.workspaceService.roots;
        if (roots.length === 0) {
            this.setState({
                lifecycle: 'idle',
                message: 'No Engineering Repository is open in this Athena window.'
            });
            return;
        }
        if (roots.length > 1) {
            this.setState({
                lifecycle: 'unavailable',
                message: 'M4 allows only one active Engineering Repository per Athena window.'
            });
            return;
        }

        const repositoryRootPath = roots[0].resource.path.fsPath();
        this.setState({
            lifecycle: 'activating',
            repositoryRoot: repositoryRootPath,
            message: 'Activating the Athena JVM repository session.'
        });

        try {
            const response = await fetch(`/athena/repository-session/activate?repositoryRootPath=${encodeURIComponent(repositoryRootPath)}`, {
                method: 'POST'
            });
            const nextState = await response.json() as AthenaRepositorySessionState;
            this.setState(nextState);
            if (nextState.lifecycle === 'unavailable') {
                this.messageService.warn(nextState.message);
            }
        } catch (error) {
            const nextState: AthenaRepositorySessionState = {
                lifecycle: 'unavailable',
                repositoryRoot: repositoryRootPath,
                message: `Failed to activate the Athena JVM repository session: ${error instanceof Error ? error.message : String(error)}`
            };
            this.setState(nextState);
            this.messageService.error(nextState.message);
        }
    }

    async refreshSessionState(): Promise<void> {
        try {
            const response = await fetch('/athena/repository-session');
            const nextState = await response.json() as AthenaRepositorySessionState;
            this.setState(nextState);
        } catch (error) {
            this.setState({
                lifecycle: 'unavailable',
                message: `Failed to query Athena repository-session state: ${error instanceof Error ? error.message : String(error)}`
            });
        }
    }

    protected setState(state: AthenaRepositorySessionState): void {
        this.stateValue = state;
        this.onDidChangeStateEmitter.fire(state);
    }
}
