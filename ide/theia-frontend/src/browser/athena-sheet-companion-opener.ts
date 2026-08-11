import URI from '@theia/core/lib/common/uri';
import { OpenHandler } from '@theia/core/lib/browser';
import { CommandService } from '@theia/core/lib/common';
import { inject, injectable } from '@theia/core/shared/inversify';
import { AthenaCommands } from './athena-workbench-extensions';

/** Opens a colocated `.sheet.athena` companion in Athena's engineering-document designer. */
@injectable()
export class AthenaSheetCompanionOpener implements OpenHandler {
    readonly id = 'athena.sheet-companion-opener';
    readonly label = 'Athena Sheet Designer';
    readonly iconClass = 'codicon codicon-layout';

    @inject(CommandService)
    protected readonly commands: CommandService;

    canHandle(uri: URI): number {
        return uri.path.toString().toLowerCase().endsWith('.sheet.athena') ? 200000 : 0;
    }

    async open(_uri: URI): Promise<object | undefined> {
        return this.commands.executeCommand(AthenaCommands.REVEAL_PRESENTATION.id);
    }
}
