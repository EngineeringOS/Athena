import { AbstractViewContribution, ApplicationShell, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { CommonMenus } from '@theia/core/lib/browser/common-frontend-contribution';
import { CommandContribution, CommandRegistry } from '@theia/core/lib/common/command';
import { MenuContribution, MenuModelRegistry } from '@theia/core/lib/common/menu';
import { injectable, inject } from '@theia/core/shared/inversify';
import { WorkspaceCommands } from '@theia/workspace/lib/browser/workspace-commands';
import { AthenaHomeWidget } from './athena-home-widget';
import { AthenaRepositoryCreationService } from './athena-repository-creation-service';
import {
    ATHENA_VIEW_MENU,
    ATHENA_WORKBENCH_EXTENSIONS,
    AthenaCommands,
    AthenaWorkbenchExtension
} from './athena-workbench-extensions';

@injectable()
export class AthenaProductContribution extends AbstractViewContribution<AthenaHomeWidget>
implements FrontendApplicationContribution, CommandContribution, MenuContribution {
    @inject(AthenaRepositoryCreationService)
    protected readonly repositoryCreationService: AthenaRepositoryCreationService;

    constructor() {
        super({
            widgetId: AthenaHomeWidget.ID,
            widgetName: AthenaHomeWidget.LABEL,
            defaultWidgetOptions: {
                area: 'main'
            }
        });
    }

    async initializeLayout(): Promise<void> {
        await this.openView({
            activate: true,
            reveal: true
        });
        await this.ensureProfessionalWorkbenchLayout();
    }

    registerCommands(commands: CommandRegistry): void {
        super.registerCommands(commands);
        commands.registerCommand(AthenaCommands.CREATE_ENGINEERING_REPOSITORY, {
            execute: async () => this.repositoryCreationService.createRepository()
        });
        commands.registerCommand(AthenaCommands.OPEN_ENGINEERING_REPOSITORY, {
            execute: () => commands.executeCommand(WorkspaceCommands.OPEN_FOLDER.id)
        });
        commands.registerCommand(AthenaCommands.OPEN_HOME, {
            execute: () => this.openView({
                activate: true,
                reveal: true
            })
        });
        for (const extension of ATHENA_WORKBENCH_EXTENSIONS) {
            commands.registerCommand(extension.command, {
                execute: () => this.revealWorkbenchExtension(extension)
            });
        }
    }

    registerMenus(menus: MenuModelRegistry): void {
        super.registerMenus(menus);
        menus.registerMenuAction([...CommonMenus.FILE_NEW, '1_athena'], {
            commandId: AthenaCommands.CREATE_ENGINEERING_REPOSITORY.id,
            label: AthenaCommands.CREATE_ENGINEERING_REPOSITORY.label,
            order: '1'
        });
        menus.registerMenuAction([...CommonMenus.FILE_OPEN, '1_athena'], {
            commandId: AthenaCommands.OPEN_ENGINEERING_REPOSITORY.id,
            label: AthenaCommands.OPEN_ENGINEERING_REPOSITORY.label,
            order: '1'
        });
        menus.registerMenuAction([...CommonMenus.HELP, '9_athena'], {
            commandId: AthenaCommands.OPEN_HOME.id,
            label: AthenaCommands.OPEN_HOME.label,
            order: '1'
        });
        menus.registerSubmenu(ATHENA_VIEW_MENU, 'Athena');
        menus.registerMenuAction(ATHENA_VIEW_MENU, {
            commandId: AthenaCommands.OPEN_HOME.id,
            label: AthenaCommands.OPEN_HOME.label,
            order: '1'
        });
        for (const extension of ATHENA_WORKBENCH_EXTENSIONS) {
            menus.registerMenuAction(ATHENA_VIEW_MENU, {
                commandId: extension.command.id,
                label: extension.command.label,
                order: extension.menuOrder
            });
        }
    }

    protected async ensureProfessionalWorkbenchLayout(): Promise<void> {
        const expandedAreas = new Set<ApplicationShell.Area>();
        for (const extension of ATHENA_WORKBENCH_EXTENSIONS) {
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

    protected async ensureWidget(
        widgetId: string,
        options: ApplicationShell.WidgetOptions
    ): Promise<void> {
        const existing = this.shell.getWidgetById(widgetId);
        if (existing) {
            await this.shell.revealWidget(widgetId);
            return;
        }

        const widget = await this.widgetManager.getOrCreateWidget(widgetId);
        await this.shell.addWidget(widget, options);
    }

    protected async revealWorkbenchWidget(
        widgetId: string,
        area: ApplicationShell.Area
    ): Promise<void> {
        await this.ensureWidget(widgetId, { area });
        this.shell.expandPanel(area);
        await this.shell.activateWidget(widgetId);
    }

    protected revealWorkbenchExtension(extension: AthenaWorkbenchExtension): Promise<void> {
        return this.revealWorkbenchWidget(extension.widgetId, extension.area);
    }
}
