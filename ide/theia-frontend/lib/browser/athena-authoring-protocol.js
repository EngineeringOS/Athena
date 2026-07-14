"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildCreateComponentPreviewRequest = buildCreateComponentPreviewRequest;
exports.buildUpdateComponentPropertiesPreviewRequest = buildUpdateComponentPropertiesPreviewRequest;
exports.buildConnectPortsPreviewRequest = buildConnectPortsPreviewRequest;
exports.buildAuthoringDecisionRequest = buildAuthoringDecisionRequest;
function buildCreateComponentPreviewRequest(input) {
    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'create-component',
        originSurface: 'palette',
        originDetail: input.originDetail,
        parentIdentity: input.systemSemanticId,
        conceptId: input.conceptId,
        preferredImplementationId: input.preferredImplementationId,
        suggestedName: input.suggestedName,
    };
}
function buildUpdateComponentPropertiesPreviewRequest(input) {
    const properties = Object.fromEntries([
        toOptionalPreviewProperty('name', input.name, 'symbol'),
        toOptionalPreviewProperty('label', input.label, 'text'),
        toOptionalPreviewProperty('description', input.description, 'text'),
        toOptionalPreviewProperty('preferredImplementationId', input.preferredImplementationId, 'symbol'),
    ].filter((entry) => !!entry));
    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'update-component-properties',
        originSurface: 'inspector',
        originDetail: input.originDetail,
        componentId: input.componentId,
        properties,
    };
}
function buildConnectPortsPreviewRequest(input) {
    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'connect-ports',
        originSurface: 'graph',
        originDetail: input.originDetail,
        sourcePortId: input.sourcePortId,
        targetPortId: input.targetPortId,
    };
}
function buildAuthoringDecisionRequest(input) {
    return {
        previewId: input.previewId,
        intentId: input.intentId,
        decision: input.decision,
        note: input.note,
    };
}
function toOptionalPreviewProperty(name, value, kind) {
    const normalizedValue = value?.trim();
    if (!normalizedValue) {
        return undefined;
    }
    return [
        name,
        {
            kind,
            text: normalizedValue,
        },
    ];
}
//# sourceMappingURL=athena-authoring-protocol.js.map