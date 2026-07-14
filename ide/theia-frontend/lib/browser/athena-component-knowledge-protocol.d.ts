export type AthenaAvailableComponentImplementationPayload = {
    implementationId: string;
    vendorId: string;
    vendorPartNumber: string;
    displayName: string;
    summary?: string;
};
export type AthenaAvailableComponentPayload = {
    conceptId: string;
    displayName: string;
    classificationKeys: string[];
    summary?: string;
    implementations: AthenaAvailableComponentImplementationPayload[];
};
export type AthenaResolvedComponentKnowledgePayload = {
    semanticSubjectId: string;
    authoredComponentReference: string;
    conceptId: string;
    conceptDisplayName: string;
    implementationId?: string;
    vendorId?: string;
    vendorPartNumber?: string;
};
export type AthenaResolvedSemanticPortPayload = {
    portSemanticId: string;
    ownerSemanticId: string;
    portTypeId: string;
    roleId: string;
    direction: string;
    signalFamilyId: string;
    protocolIds: string[];
};
export type AthenaResolvedPhysicalTraitPayload = {
    semanticSubjectId: string;
    displayName: string;
    widthMillimeters: number;
    heightMillimeters: number;
    depthMillimeters: number;
    mountingTypeId: string;
    installationMarkerIds: string[];
};
export type AthenaComponentKnowledgeDiagnosticPayload = {
    severity: string;
    ruleId: string;
    subject: string;
    message: string;
};
export type AthenaComponentKnowledgeSessionPayload = {
    projectName: string;
    systemSemanticId: string;
    semanticPath: string;
    status: string;
    contributingPluginIds: string[];
    activeConceptCount: number;
    activeImplementationCount: number;
    availableComponents: AthenaAvailableComponentPayload[];
    components: AthenaResolvedComponentKnowledgePayload[];
    semanticPorts: AthenaResolvedSemanticPortPayload[];
    physicalTraits: AthenaResolvedPhysicalTraitPayload[];
    diagnostics: AthenaComponentKnowledgeDiagnosticPayload[];
    unavailableReason?: string;
};
//# sourceMappingURL=athena-component-knowledge-protocol.d.ts.map