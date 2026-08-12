import URI from '@theia/core/lib/common/uri';
import { ApplicationShell, OpenHandler, WidgetManager } from '@theia/core/lib/browser';
import { CommandService } from '@theia/core/lib/common';
import { inject, injectable } from '@theia/core/shared/inversify';
import { AthenaCommands } from './athena-workbench-extensions';
import { AthenaPresentationWidget } from './athena-presentation-widget';

/** Opens a Folio or Page Companion in Athena's engineering-document designer. */
@injectable()
export class AthenaSheetCompanionOpener implements OpenHandler {
    readonly id = 'athena.sheet-companion-opener';
    readonly label = 'Athena Sheet Designer';
    readonly iconClass = 'codicon codicon-layout';

    @inject(CommandService)
    protected readonly commands: CommandService;
    @inject(WidgetManager)
    protected readonly widgetManager: WidgetManager;
    @inject(ApplicationShell)
    protected readonly shell: ApplicationShell;

    canHandle(uri: URI): number {
        if (uri.query === 'athenaSource=1') return 0;
        const path = uri.path.toString().toLowerCase();
        return path.endsWith('.folio.athena') || path.endsWith('.sheet.athena') ? 200000 : 0;
    }

    async open(uri: URI): Promise<object | undefined> {
        const match = uri.path.toString().match(/\.([^.\\/]+)\.sheet\.athena$/i);
        const sheetId = match?.[1];
        if (!sheetId) return this.commands.executeCommand(AthenaCommands.REVEAL_PRESENTATION.id);
        const widget = await this.widgetManager.getOrCreateWidget<AthenaPresentationWidget>(AthenaPresentationWidget.ID, {
            sheetId,
            sourceUri: uri.withQuery('view=canvas').toString(),
        });
        if (!widget.isAttached) await this.shell.addWidget(widget, { area: 'main' });
        await this.shell.activateWidget(widget.id);
        return widget;
    }
}
