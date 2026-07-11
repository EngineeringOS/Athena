"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.CONNECT_PORTS_INTENT_ID = exports.ADJUST_LAYOUT_PLACEMENT_INTENT_ID = exports.GRAPH_COMMAND_INTENT_METHOD = void 0;
exports.buildAdjustLayoutPlacementIntentRequest = buildAdjustLayoutPlacementIntentRequest;
exports.buildConnectPortsIntentRequest = buildConnectPortsIntentRequest;
exports.supportsAdjustLayoutPlacementIntent = supportsAdjustLayoutPlacementIntent;
exports.supportsConnectPortsIntent = supportsConnectPortsIntent;
exports.GRAPH_COMMAND_INTENT_METHOD = 'athena/graphCommandIntent';
exports.ADJUST_LAYOUT_PLACEMENT_INTENT_ID = 'adjust-layout-placement';
exports.CONNECT_PORTS_INTENT_ID = 'connect-ports';
function buildAdjustLayoutPlacementIntentRequest(args) {
    return {
        method: exports.GRAPH_COMMAND_INTENT_METHOD,
        params: {
            intentId: exports.ADJUST_LAYOUT_PLACEMENT_INTENT_ID,
            viewId: args.viewId,
            target: {
                semanticId: args.semanticId,
                subjectKind: args.subjectKind
            },
            requestedPlacement: {
                x: args.x,
                y: args.y
            }
        },
        model: args.model
    };
}
function buildConnectPortsIntentRequest(args) {
    return {
        method: exports.GRAPH_COMMAND_INTENT_METHOD,
        params: {
            intentId: exports.CONNECT_PORTS_INTENT_ID,
            viewId: args.viewId,
            source: {
                semanticId: args.sourceSemanticId,
                subjectKind: 'port'
            },
            target: {
                semanticId: args.targetSemanticId,
                subjectKind: 'port'
            }
        },
        model: args.model
    };
}
function supportsAdjustLayoutPlacementIntent(view) {
    return view?.ownershipContract?.interactivity === 'interactive'
        && view.ownershipContract.projectionCommandIds?.includes(exports.ADJUST_LAYOUT_PLACEMENT_INTENT_ID) === true;
}
function supportsConnectPortsIntent(view) {
    return view?.ownershipContract?.interactivity === 'interactive'
        && view.ownershipContract.semanticCommandIds?.includes(exports.CONNECT_PORTS_INTENT_ID) === true;
}
//# sourceMappingURL=athena-graph-command-intent-protocol.js.map