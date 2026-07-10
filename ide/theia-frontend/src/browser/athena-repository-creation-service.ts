import URI from '@theia/core/lib/common/uri';
import { MessageService } from '@theia/core';
import { DialogMode, SingleTextInputDialog, SingleTextInputDialogProps } from '@theia/core/lib/browser';
import { injectable, inject } from '@theia/core/shared/inversify';
import { FileDialogService } from '@theia/filesystem/lib/browser';
import { WorkspaceService } from '@theia/workspace/lib/browser/workspace-service';
import { toAthenaBackendUrl } from './athena-backend-endpoint';

type AthenaRepositoryBootstrapResult = {
    repositoryRootPath: string;
    sourcePath: string;
    repositoryName: string;
    projectName: string;
};

@injectable()
export class AthenaRepositoryCreationService {
    @inject(FileDialogService)
    protected readonly fileDialogService: FileDialogService;

    @inject(WorkspaceService)
    protected readonly workspaceService: WorkspaceService;

    @inject(MessageService)
    protected readonly messageService: MessageService;

    async createRepository(): Promise<void> {
        await this.workspaceService.ready;
        const [currentRoot] = await this.workspaceService.roots;
        const parentDirectoryUri = await this.fileDialogService.showOpenDialog({
            title: 'Choose Parent Folder For Engineering Repository',
            openLabel: 'Select Parent Folder',
            canSelectFolders: true,
            canSelectFiles: false,
            canSelectMany: false
        }, currentRoot);
        if (!parentDirectoryUri) {
            return;
        }

        const repositoryName = await this.promptForRepositoryName();
        if (!repositoryName) {
            return;
        }

        try {
            const bootstrapResult = await this.requestRepositoryBootstrap(parentDirectoryUri.path.fsPath(), repositoryName);
            this.messageService.info(`Created Engineering Repository ${bootstrapResult.repositoryName}`);
            this.workspaceService.open(
                URI.fromFilePath(bootstrapResult.repositoryRootPath),
                { preserveWindow: true }
            );
        } catch {
            return;
        }
    }

    protected async promptForRepositoryName(): Promise<string | undefined> {
        const dialog = new SingleTextInputDialog(this.repositoryNameDialogProps());
        const result = await dialog.open();
        return result?.trim() || undefined;
    }

    protected repositoryNameDialogProps(): SingleTextInputDialogProps {
        return {
            title: 'New Engineering Repository',
            confirmButtonLabel: 'Create Repository',
            initialValue: 'New Engineering Repository',
            placeholder: 'Repository name',
            validate: (input: string, _mode: DialogMode) => {
                const trimmedInput = input.trim();
                if (!trimmedInput) {
                    return 'Repository name is required.';
                }
                if (!/^[A-Za-z0-9][A-Za-z0-9 _-]*$/.test(trimmedInput)) {
                    return 'Use letters, numbers, spaces, hyphens, or underscores only.';
                }
                return true;
            }
        };
    }

    protected async requestRepositoryBootstrap(
        parentDirectoryPath: string,
        repositoryName: string,
    ): Promise<AthenaRepositoryBootstrapResult> {
        const response = await fetch(
            toAthenaBackendUrl('athena/repositories/create', {
                parentDirectoryPath,
                repositoryName,
            }),
            {
                method: 'POST'
            }
        );
        const result = await response.json() as AthenaRepositoryBootstrapResult | { message: string };
        if (!response.ok) {
            const message = 'message' in result ? result.message : 'Failed to create Engineering Repository.';
            this.messageService.error(message);
            throw new Error(message);
        }
        return result as AthenaRepositoryBootstrapResult;
    }
}
