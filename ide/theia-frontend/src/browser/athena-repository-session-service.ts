import { Emitter, MessageService } from '@theia/core';
import { FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { inject, injectable } from '@theia/core/shared/inversify';
import { WorkspaceService } from '@theia/workspace/lib/browser/workspace-service';
import { toAthenaBackendUrl } from './athena-backend-endpoint';
import { isAthenaDocumentCoveredBySession } from './athena-repository-session-model';

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
    protected bootstrapOperation: Promise<void> | undefined;
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

    onStart(_app: FrontendApplication): void {
        if (!this.bootstrapOperation) {
            this.bootstrapOperation = this.bootstrapInitialState();
        }
    }

    protected async bootstrapInitialState(): Promise<void> {
        try {
            await this.workspaceService.ready;
            if (!this.workspaceService.opened) {
                await this.refreshSessionState();
                return;
            }
            await this.activateCurrentWorkspaceSession();
        } finally {
            this.bootstrapOperation = undefined;
        }
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
            const response = await fetch(toAthenaBackendUrl('athena/repository-session/activate', {
                repositoryRootPath,
            }), {
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
            const response = await fetch(toAthenaBackendUrl('athena/repository-session'));
            const nextState = await response.json() as AthenaRepositorySessionState;
            this.setState(nextState);
        } catch (error) {
            this.setState({
                lifecycle: 'unavailable',
                message: `Failed to query Athena repository-session state: ${error instanceof Error ? error.message : String(error)}`
            });
        }
    }

    async ensureSessionForDocument(documentUri: string): Promise<AthenaRepositorySessionState> {
        if (!documentUri.toLowerCase().endsWith('.athena')) {
            return this.stateValue;
        }
        if (isAthenaDocumentCoveredBySession(this.stateValue, documentUri)) {
            return this.stateValue;
        }
        if (this.stateValue.lifecycle === 'activating') {
            return this.stateValue;
        }

        try {
            const response = await fetch(toAthenaBackendUrl('athena/repository-session/ensure', {
                documentUri,
            }), {
                method: 'POST'
            });
            const nextState = await response.json() as AthenaRepositorySessionState;
            this.setState(nextState);
            return nextState;
        } catch (error) {
            const nextState: AthenaRepositorySessionState = {
                ...this.stateValue,
                lifecycle: 'unavailable',
                lspLifecycle: 'unavailable',
                message: `Failed to ensure the Athena repository session for ${documentUri}: ${error instanceof Error ? error.message : String(error)}`
            };
            this.setState(nextState);
            return nextState;
        }
    }

    protected setState(state: AthenaRepositorySessionState): void {
        this.stateValue = state;
        this.onDidChangeStateEmitter.fire(state);
    }
}
