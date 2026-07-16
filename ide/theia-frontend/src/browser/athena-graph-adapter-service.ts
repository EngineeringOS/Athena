import { AthenaGLSPDiagram, translateProjectionSessionToGLSPDiagram } from '@engineeringood/athena-graph-glsp';
import { inject, injectable } from '@theia/core/shared/inversify';
import {
    buildConnectPortsIntentRequest,
    buildAdjustLayoutPlacementIntentRequest,
    supportsConnectPortsIntent,
    supportsAdjustLayoutPlacementIntent,
    type AthenaGraphCommandIntentPayload,
    type AthenaGraphCommandSubjectKind
} from './athena-graph-command-intent-protocol';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { graphContainsSemanticId, nextRevealViewId } from './athena-semantic-selection-model';

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

    async revealSemanticId(
        semanticId: string,
        diagram?: AthenaGLSPDiagram,
    ): Promise<AthenaGLSPDiagram | undefined> {
        const initialDiagram = diagram ?? await this.requestDiagram();
        if (!initialDiagram || graphContainsSemanticId(initialDiagram, semanticId)) {
            return initialDiagram;
        }

        let activeDiagram = initialDiagram;
        const attemptedViewIds = initialDiagram.activeViewId ? [initialDiagram.activeViewId] : [];
        let viewId = nextRevealViewId(activeDiagram, attemptedViewIds);
        while (viewId) {
            attemptedViewIds.push(viewId);
            activeDiagram = await this.switchActiveView(viewId) ?? activeDiagram;
            if (graphContainsSemanticId(activeDiagram, semanticId)) {
                return activeDiagram;
            }
            viewId = nextRevealViewId(activeDiagram, attemptedViewIds);
        }

        if (activeDiagram.activeViewId !== initialDiagram.activeViewId) {
            await this.switchActiveView(initialDiagram.activeViewId);
        }
        return undefined;
    }

    supportsAdjustLayoutPlacementIntent(diagram: AthenaGLSPDiagram | undefined): boolean {
        if (!diagram) {
            return false;
        }
        return supportsAdjustLayoutPlacementIntent(
            diagram.supportedViews.find(view => view.viewId === diagram.activeViewId)
        );
    }

    supportsConnectPortsIntent(diagram: AthenaGLSPDiagram | undefined): boolean {
        if (!diagram) {
            return false;
        }
        return supportsConnectPortsIntent(
            diagram.supportedViews.find(view => view.viewId === diagram.activeViewId)
        );
    }

    async submitAdjustLayoutPlacementIntent(args: {
        diagram: AthenaGLSPDiagram;
        semanticId: string;
        subjectKind: AthenaGraphCommandSubjectKind;
        x: number;
        y: number;
    }): Promise<AthenaGraphCommandIntentPayload | undefined> {
        const request = buildAdjustLayoutPlacementIntentRequest({
            viewId: args.diagram.activeViewId,
            semanticId: args.semanticId,
            subjectKind: args.subjectKind,
            x: args.x,
            y: args.y,
        });
        return this.lspEditorBridgeService.requestGraphCommandIntent(request.params);
    }

    async submitConnectPortsIntent(args: {
        diagram: AthenaGLSPDiagram;
        sourceSemanticId: string;
        targetSemanticId: string;
    }): Promise<AthenaGraphCommandIntentPayload | undefined> {
        const request = buildConnectPortsIntentRequest({
            viewId: args.diagram.activeViewId,
            sourceSemanticId: args.sourceSemanticId,
            targetSemanticId: args.targetSemanticId,
        });
        return this.lspEditorBridgeService.requestGraphCommandIntent(request.params);
    }
}
