import type { AthenaComponentKnowledgeSessionPayload } from './athena-component-knowledge-protocol';
import type { AthenaSemanticInspectionPayload } from './athena-lsp-editor-bridge-service';
import type { AthenaActiveSemanticSelection } from './athena-semantic-selection-model';
export type AthenaInspectorPortSnapshot = {
    semanticId: string;
    label: string;
    direction: string;
    signalFamilyId: string;
    roleId: string;
    portTypeId: string;
    protocolIds: string[];
    selected: boolean;
    connectionIds: string[];
    connectedPaths: string[];
    connectedPeerSemanticIds: string[];
};
export type AthenaInspectorPhysicalTraitSnapshot = {
    displayName: string;
    widthMillimeters: number;
    heightMillimeters: number;
    depthMillimeters: number;
    mountingTypeId: string;
    installationMarkerIds: string[];
};
export type AthenaInspectorImplementationOption = {
    implementationId: string;
    displayName: string;
    vendorId: string;
    vendorPartNumber: string;
    summary?: string;
    selected: boolean;
};
export type AthenaInspectorEditDraft = {
    semanticId: string;
    name: string;
    label: string;
    description: string;
    preferredImplementationId?: string;
};
export type AthenaInspectorDraftChanges = {
    name?: string;
    label?: string;
    description?: string;
    preferredImplementationId?: string;
};
export type AthenaInspectorComponentSnapshot = {
    semanticId: string;
    name: string;
    label: string;
    description: string;
    kind: string;
    conceptId: string;
    conceptDisplayName: string;
    authoredComponentReference: string;
    implementationId?: string;
    vendorId?: string;
    vendorPartNumber?: string;
    implementationOptions: AthenaInspectorImplementationOption[];
    ports: AthenaInspectorPortSnapshot[];
    physicalTraits: AthenaInspectorPhysicalTraitSnapshot[];
};
export declare function buildAthenaInspectorComponentSnapshot(args: {
    inspection: AthenaSemanticInspectionPayload | undefined;
    knowledge: AthenaComponentKnowledgeSessionPayload | undefined;
    selection: AthenaActiveSemanticSelection | undefined;
}): AthenaInspectorComponentSnapshot | undefined;
export declare function createAthenaInspectorEditDraft(snapshot: AthenaInspectorComponentSnapshot): AthenaInspectorEditDraft;
export declare function buildAthenaInspectorDraftChanges(args: {
    snapshot: AthenaInspectorComponentSnapshot;
    draft: AthenaInspectorEditDraft;
}): AthenaInspectorDraftChanges;
//# sourceMappingURL=athena-inspector-model.d.ts.map