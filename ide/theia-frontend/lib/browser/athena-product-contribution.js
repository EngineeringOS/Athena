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
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaProductContribution = void 0;
const browser_1 = require("@theia/core/lib/browser");
const common_frontend_contribution_1 = require("@theia/core/lib/browser/common-frontend-contribution");
const inversify_1 = require("@theia/core/shared/inversify");
const workspace_commands_1 = require("@theia/workspace/lib/browser/workspace-commands");
const athena_home_widget_1 = require("./athena-home-widget");
const athena_repository_creation_service_1 = require("./athena-repository-creation-service");
const athena_workbench_extensions_1 = require("./athena-workbench-extensions");
let AthenaProductContribution = class AthenaProductContribution extends browser_1.AbstractViewContribution {
    repositoryCreationService;
    constructor() {
        super({
            widgetId: athena_home_widget_1.AthenaHomeWidget.ID,
            widgetName: athena_home_widget_1.AthenaHomeWidget.LABEL,
            defaultWidgetOptions: {
                area: 'main'
            }
        });
    }
    async initializeLayout() {
        await this.openView({
            activate: true,
            reveal: true
        });
        await this.ensureProfessionalWorkbenchLayout();
    }
    registerCommands(commands) {
        super.registerCommands(commands);
        commands.registerCommand(athena_workbench_extensions_1.AthenaCommands.CREATE_ENGINEERING_REPOSITORY, {
            execute: async () => this.repositoryCreationService.createRepository()
        });
        commands.registerCommand(athena_workbench_extensions_1.AthenaCommands.OPEN_ENGINEERING_REPOSITORY, {
            execute: () => commands.executeCommand(workspace_commands_1.WorkspaceCommands.OPEN_FOLDER.id)
        });
        commands.registerCommand(athena_workbench_extensions_1.AthenaCommands.OPEN_HOME, {
            execute: () => this.openView({
                activate: true,
                reveal: true
            })
        });
        for (const extension of athena_workbench_extensions_1.ATHENA_WORKBENCH_EXTENSIONS) {
            commands.registerCommand(extension.command, {
                execute: () => this.revealWorkbenchExtension(extension)
            });
        }
    }
    registerMenus(menus) {
        super.registerMenus(menus);
        menus.registerMenuAction([...common_frontend_contribution_1.CommonMenus.FILE_NEW, '1_athena'], {
            commandId: athena_workbench_extensions_1.AthenaCommands.CREATE_ENGINEERING_REPOSITORY.id,
            label: athena_workbench_extensions_1.AthenaCommands.CREATE_ENGINEERING_REPOSITORY.label,
            order: '1'
        });
        menus.registerMenuAction([...common_frontend_contribution_1.CommonMenus.FILE_OPEN, '1_athena'], {
            commandId: athena_workbench_extensions_1.AthenaCommands.OPEN_ENGINEERING_REPOSITORY.id,
            label: athena_workbench_extensions_1.AthenaCommands.OPEN_ENGINEERING_REPOSITORY.label,
            order: '1'
        });
        menus.registerMenuAction([...common_frontend_contribution_1.CommonMenus.HELP, '9_athena'], {
            commandId: athena_workbench_extensions_1.AthenaCommands.OPEN_HOME.id,
            label: athena_workbench_extensions_1.AthenaCommands.OPEN_HOME.label,
            order: '1'
        });
        menus.registerSubmenu(athena_workbench_extensions_1.ATHENA_VIEW_MENU, 'Athena');
        menus.registerMenuAction(athena_workbench_extensions_1.ATHENA_VIEW_MENU, {
            commandId: athena_workbench_extensions_1.AthenaCommands.OPEN_HOME.id,
            label: athena_workbench_extensions_1.AthenaCommands.OPEN_HOME.label,
            order: '1'
        });
        for (const extension of athena_workbench_extensions_1.ATHENA_WORKBENCH_EXTENSIONS) {
            menus.registerMenuAction(athena_workbench_extensions_1.ATHENA_VIEW_MENU, {
                commandId: extension.command.id,
                label: extension.command.label,
                order: extension.menuOrder
            });
        }
    }
    async ensureProfessionalWorkbenchLayout() {
        const expandedAreas = new Set();
        for (const extension of athena_workbench_extensions_1.ATHENA_WORKBENCH_EXTENSIONS) {
            if (extension.startupRank === undefined) {
                continue;
            }
            await this.ensureWidget(extension.widgetId, {
                area: extension.area,
                rank: extension.startupRank
            });
            expandedAreas.add(extension.area);
        }
        for (const area of expandedAreas) {
            this.shell.expandPanel(area);
        }
    }
    async ensureWidget(widgetId, options) {
        const existing = this.shell.getWidgetById(widgetId);
        if (existing) {
            await this.shell.revealWidget(widgetId);
            return;
        }
        const widget = await this.widgetManager.getOrCreateWidget(widgetId);
        await this.shell.addWidget(widget, options);
    }
    async revealWorkbenchWidget(widgetId, area) {
        await this.ensureWidget(widgetId, { area });
        this.shell.expandPanel(area);
        await this.shell.activateWidget(widgetId);
    }
    revealWorkbenchExtension(extension) {
        return this.revealWorkbenchWidget(extension.widgetId, extension.area);
    }
};
exports.AthenaProductContribution = AthenaProductContribution;
__decorate([
    (0, inversify_1.inject)(athena_repository_creation_service_1.AthenaRepositoryCreationService),
    __metadata("design:type", athena_repository_creation_service_1.AthenaRepositoryCreationService)
], AthenaProductContribution.prototype, "repositoryCreationService", void 0);
exports.AthenaProductContribution = AthenaProductContribution = __decorate([
    (0, inversify_1.injectable)(),
    __metadata("design:paramtypes", [])
], AthenaProductContribution);
//# sourceMappingURL=athena-product-contribution.js.map