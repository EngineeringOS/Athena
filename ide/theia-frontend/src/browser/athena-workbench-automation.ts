import URI from '@theia/core/lib/common/uri';
import { CommandService } from '@theia/core/lib/common';
import { ApplicationShell, FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { OpenerService, open } from '@theia/core/lib/browser/opener-service';
import { WorkspaceService } from '@theia/workspace/lib/browser/workspace-service';
import { FileNavigatorCommands } from '@theia/navigator/lib/browser/navigator-contribution';
import { inject, injectable } from '@theia/core/shared/inversify';
import { AthenaCommands } from './athena-workbench-extensions';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';

export type AthenaWorkbenchAutomation = {
    openWorkspaceAndSource(workspaceRoot: string, sourceFile?: string): Promise<void>;
    revealEngineeringDocument(): Promise<void>;
    getState(): Promise<{
        workspaceOpened: boolean;
        workspaceRoots: string[];
        sourceFile?: string;
        repositoryLifecycle: string;
        repositoryRoot?: string;
        lspRepositoryRoot?: string;
    }>;
};

type AthenaWindow = Window & {
    __athenaWorkbenchAutomation?: AthenaWorkbenchAutomation;
};

/**
 * Exposes one small, product-owned browser automation seam.
 * It keeps Electron proof flows on the same DI services as user actions.
 */
@injectable()
export class AthenaWorkbenchAutomationContribution implements FrontendApplicationContribution {
    @inject(CommandService)
    protected readonly commandService: CommandService;

    @inject(ApplicationShell)
    protected readonly shell: ApplicationShell;

    @inject(OpenerService)
    protected readonly openerService: OpenerService;

    @inject(WorkspaceService)
    protected readonly workspaceService: WorkspaceService;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    protected openedWorkspaceRoot: string | undefined;
    protected openedSourceFile: string | undefined;
    protected opening: Promise<void> | undefined;

    onStart(_app: FrontendApplication): void {
        const automation: AthenaWorkbenchAutomation = {
            openWorkspaceAndSource: async (workspaceRoot, sourceFile) => {
                if (this.openedWorkspaceRoot === workspaceRoot && (!sourceFile || this.openedSourceFile === sourceFile)) return;
                if (this.opening) return this.opening;
                this.opening = this.openWorkspaceAndSource(workspaceRoot, sourceFile).finally(() => { this.opening = undefined; });
                return this.opening;
            },
            revealEngineeringDocument: async () => {
                await this.commandService.executeCommand(AthenaCommands.REVEAL_PRESENTATION.id);
            },
            getState: async () => {
                const roots = await this.workspaceService.roots;
                const graphSession = await this.lspEditorBridgeService.requestRepositoryGraphSession();
                return {
                    workspaceOpened: this.workspaceService.opened,
                    workspaceRoots: roots.map(root => root.resource.path.fsPath()),
                    sourceFile: this.openedSourceFile,
                    repositoryLifecycle: this.repositorySessionService.state.lifecycle,
                    repositoryRoot: this.repositorySessionService.state.repositoryRoot,
                    lspRepositoryRoot: graphSession?.repositoryRoot
                };
            }
        };
        (window as AthenaWindow).__athenaWorkbenchAutomation = automation;
    }

    protected async openWorkspaceAndSource(workspaceRoot: string, sourceFile?: string): Promise<void> {
        await this.workspaceService.ready;
        const rootUri = URI.fromFilePath(workspaceRoot);
        const roots = await this.workspaceService.roots;
        const alreadyOpen = roots.some(root => root.resource.toString() === rootUri.toString());
        if (!alreadyOpen) await this.workspaceService.addRoot(rootUri);
        this.openedWorkspaceRoot = workspaceRoot;
        await this.commandService.executeCommand(FileNavigatorCommands.FOCUS.id);
        await this.shell.activateWidget('files');
        await this.commandService.executeCommand(FileNavigatorCommands.REFRESH_NAVIGATOR.id);
        if (sourceFile && this.openedSourceFile !== sourceFile) {
            await open(this.openerService, URI.fromFilePath(sourceFile));
            this.openedSourceFile = sourceFile;
        }
        await this.commandService.executeCommand(AthenaCommands.REVEAL_PRESENTATION.id);
        await this.shell.activateWidget('files');
    }
}
