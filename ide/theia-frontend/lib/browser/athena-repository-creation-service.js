"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaRepositoryCreationService = void 0;
const uri_1 = __importDefault(require("@theia/core/lib/common/uri"));
const core_1 = require("@theia/core");
const browser_1 = require("@theia/core/lib/browser");
const inversify_1 = require("@theia/core/shared/inversify");
const browser_2 = require("@theia/filesystem/lib/browser");
const workspace_service_1 = require("@theia/workspace/lib/browser/workspace-service");
const athena_backend_endpoint_1 = require("./athena-backend-endpoint");
let AthenaRepositoryCreationService = class AthenaRepositoryCreationService {
    fileDialogService;
    workspaceService;
    messageService;
    async createRepository() {
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
            this.workspaceService.open(uri_1.default.fromFilePath(bootstrapResult.repositoryRootPath), { preserveWindow: true });
        }
        catch {
            return;
        }
    }
    async promptForRepositoryName() {
        const dialog = new browser_1.SingleTextInputDialog(this.repositoryNameDialogProps());
        const result = await dialog.open();
        return result?.trim() || undefined;
    }
    repositoryNameDialogProps() {
        return {
            title: 'New Engineering Repository',
            confirmButtonLabel: 'Create Repository',
            initialValue: 'New Engineering Repository',
            placeholder: 'Repository name',
            validate: (input, _mode) => {
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
    async requestRepositoryBootstrap(parentDirectoryPath, repositoryName) {
        const response = await fetch((0, athena_backend_endpoint_1.toAthenaBackendUrl)('athena/repositories/create', {
            parentDirectoryPath,
            repositoryName,
        }), {
            method: 'POST'
        });
        const result = await response.json();
        if (!response.ok) {
            const message = 'message' in result ? result.message : 'Failed to create Engineering Repository.';
            this.messageService.error(message);
            throw new Error(message);
        }
        return result;
    }
};
exports.AthenaRepositoryCreationService = AthenaRepositoryCreationService;
__decorate([
    (0, inversify_1.inject)(browser_2.FileDialogService),
    __metadata("design:type", Object)
], AthenaRepositoryCreationService.prototype, "fileDialogService", void 0);
__decorate([
    (0, inversify_1.inject)(workspace_service_1.WorkspaceService),
    __metadata("design:type", workspace_service_1.WorkspaceService)
], AthenaRepositoryCreationService.prototype, "workspaceService", void 0);
__decorate([
    (0, inversify_1.inject)(core_1.MessageService),
    __metadata("design:type", core_1.MessageService)
], AthenaRepositoryCreationService.prototype, "messageService", void 0);
exports.AthenaRepositoryCreationService = AthenaRepositoryCreationService = __decorate([
    (0, inversify_1.injectable)()
], AthenaRepositoryCreationService);
//# sourceMappingURL=athena-repository-creation-service.js.map