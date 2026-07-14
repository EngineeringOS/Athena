import type { AthenaComponentKnowledgeSessionPayload } from './athena-component-knowledge-protocol';
import type { AthenaSemanticInspectionPayload } from './athena-lsp-editor-bridge-service';
export type AthenaCompatibleConnectionTarget = {
    semanticId: string;
    ownerSemanticId: string;
    label: string;
    direction: string;
    signalFamilyId: string;
    roleId: string;
    protocolIds: string[];
};
export type AthenaPortConnectionState = {
    connectionIds: string[];
    connectedPaths: string[];
    connectedPeerSemanticIds: string[];
};
export declare function buildAthenaCompatibleConnectionTargets(args: {
    knowledge: AthenaComponentKnowledgeSessionPayload | undefined;
    inspection: AthenaSemanticInspectionPayload | undefined;
    sourcePortSemanticId: string | undefined;
}): AthenaCompatibleConnectionTarget[];
export declare function buildAthenaPortConnectionStateMap(inspection: AthenaSemanticInspectionPayload | undefined): Map<string, AthenaPortConnectionState>;
//# sourceMappingURL=athena-guided-connection-model.d.ts.map