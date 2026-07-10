import { AthenaGLSPDiagram } from '@engineeringood/athena-graph-glsp';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
/** Thin Theia-side host that keeps graph adapter consumption downstream of the Athena LSP bridge. */
export declare class AthenaGraphAdapterService {
    protected static readonly SWITCH_ACTIVE_VIEW_COMMAND_ID = "switch-active-view";
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;
    requestDiagram(): Promise<AthenaGLSPDiagram | undefined>;
    switchActiveView(viewId: string): Promise<AthenaGLSPDiagram | undefined>;
}
//# sourceMappingURL=athena-graph-adapter-service.d.ts.map