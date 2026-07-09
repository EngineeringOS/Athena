import { AbstractViewContribution, ApplicationShell, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { CommandContribution, CommandRegistry } from '@theia/core/lib/common/command';
import { MenuContribution, MenuModelRegistry } from '@theia/core/lib/common/menu';
import { AthenaHomeWidget } from './athena-home-widget';
import { AthenaRepositoryCreationService } from './athena-repository-creation-service';
import { AthenaWorkbenchExtension } from './athena-workbench-extensions';
export declare class AthenaProductContribution extends AbstractViewContribution<AthenaHomeWidget> implements FrontendApplicationContribution, CommandContribution, MenuContribution {
    protected readonly repositoryCreationService: AthenaRepositoryCreationService;
    constructor();
    initializeLayout(): Promise<void>;
    registerCommands(commands: CommandRegistry): void;
    registerMenus(menus: MenuModelRegistry): void;
    protected ensureProfessionalWorkbenchLayout(): Promise<void>;
    protected ensureWidget(widgetId: string, options: ApplicationShell.WidgetOptions): Promise<void>;
    protected revealWorkbenchWidget(widgetId: string, area: ApplicationShell.Area): Promise<void>;
    protected revealWorkbenchExtension(extension: AthenaWorkbenchExtension): Promise<void>;
}
//# sourceMappingURL=athena-product-contribution.d.ts.map