import { MessageService } from '@theia/core';
import { SingleTextInputDialogProps } from '@theia/core/lib/browser';
import { FileDialogService } from '@theia/filesystem/lib/browser';
import { WorkspaceService } from '@theia/workspace/lib/browser/workspace-service';
type AthenaRepositoryBootstrapResult = {
    repositoryRootPath: string;
    sourcePath: string;
    repositoryName: string;
    projectName: string;
};
export declare class AthenaRepositoryCreationService {
    protected readonly fileDialogService: FileDialogService;
    protected readonly workspaceService: WorkspaceService;
    protected readonly messageService: MessageService;
    createRepository(): Promise<void>;
    protected promptForRepositoryName(): Promise<string | undefined>;
    protected repositoryNameDialogProps(): SingleTextInputDialogProps;
    protected requestRepositoryBootstrap(parentDirectoryPath: string, repositoryName: string): Promise<AthenaRepositoryBootstrapResult>;
}
export {};
//# sourceMappingURL=athena-repository-creation-service.d.ts.map