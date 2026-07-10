import { AthenaGLSPDiagram, translateProjectionSessionToGLSPDiagram } from '@engineeringood/athena-graph-glsp';
import { inject, injectable } from '@theia/core/shared/inversify';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';

/** Thin Theia-side host that keeps graph adapter consumption downstream of the Athena LSP bridge. */
@injectable()
export class AthenaGraphAdapterService {
    protected static readonly SWITCH_ACTIVE_VIEW_COMMAND_ID = 'switch-active-view';

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    async requestDiagram(): Promise<AthenaGLSPDiagram | undefined> {
        const projectionSession = await this.lspEditorBridgeService.requestProjectionSession();
        if (!projectionSession) {
            return undefined;
        }
        return translateProjectionSessionToGLSPDiagram(projectionSession);
    }

    async switchActiveView(viewId: string): Promise<AthenaGLSPDiagram | undefined> {
        const commandPayload = await this.lspEditorBridgeService.requestProjectionCommand({
            commandId: AthenaGraphAdapterService.SWITCH_ACTIVE_VIEW_COMMAND_ID,
            viewId
        });
        if (!commandPayload) {
            return undefined;
        }
        if (commandPayload.status !== 'applied') {
            throw new Error(commandPayload.reason ?? `Athena rejected governed view switch \`${viewId}\`.`);
        }
        if (!commandPayload.session) {
            return undefined;
        }
        return translateProjectionSessionToGLSPDiagram(commandPayload.session);
    }
}
