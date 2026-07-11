import { AthenaGLSPDiagram } from '@engineeringood/athena-graph-glsp';
import { type AthenaGraphCommandIntentPayload, type AthenaGraphCommandSubjectKind } from './athena-graph-command-intent-protocol';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
/** Thin Theia-side host that keeps graph adapter consumption downstream of the Athena LSP bridge. */
export declare class AthenaGraphAdapterService {
    protected static readonly SWITCH_ACTIVE_VIEW_COMMAND_ID = "switch-active-view";
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;
    requestDiagram(): Promise<AthenaGLSPDiagram | undefined>;
    switchActiveView(viewId: string): Promise<AthenaGLSPDiagram | undefined>;
    revealSemanticId(semanticId: string, diagram?: AthenaGLSPDiagram): Promise<AthenaGLSPDiagram | undefined>;
    supportsAdjustLayoutPlacementIntent(diagram: AthenaGLSPDiagram | undefined): boolean;
    supportsConnectPortsIntent(diagram: AthenaGLSPDiagram | undefined): boolean;
    submitAdjustLayoutPlacementIntent(args: {
        diagram: AthenaGLSPDiagram;
        semanticId: string;
        subjectKind: AthenaGraphCommandSubjectKind;
        x: number;
        y: number;
    }): Promise<AthenaGraphCommandIntentPayload | undefined>;
    submitConnectPortsIntent(args: {
        diagram: AthenaGLSPDiagram;
        sourceSemanticId: string;
        targetSemanticId: string;
    }): Promise<AthenaGraphCommandIntentPayload | undefined>;
}
//# sourceMappingURL=athena-graph-adapter-service.d.ts.map