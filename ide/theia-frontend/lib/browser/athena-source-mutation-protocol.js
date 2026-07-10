"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildAthenaSourceMutationRequest = buildAthenaSourceMutationRequest;
function buildAthenaSourceMutationRequest(documentUri, model) {
    return {
        method: 'athena/sourceMutationEvaluation',
        params: {
            textDocument: {
                uri: documentUri
            }
        },
        model
    };
}
//# sourceMappingURL=athena-source-mutation-protocol.js.map