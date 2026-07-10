"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var AthenaGraphAdapterService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaGraphAdapterService = void 0;
const athena_graph_glsp_1 = require("@engineeringood/athena-graph-glsp");
const inversify_1 = require("@theia/core/shared/inversify");
const athena_lsp_editor_bridge_service_1 = require("./athena-lsp-editor-bridge-service");
/** Thin Theia-side host that keeps graph adapter consumption downstream of the Athena LSP bridge. */
let AthenaGraphAdapterService = class AthenaGraphAdapterService {
    static { AthenaGraphAdapterService_1 = this; }
    static SWITCH_ACTIVE_VIEW_COMMAND_ID = 'switch-active-view';
    lspEditorBridgeService;
    async requestDiagram() {
        const projectionSession = await this.lspEditorBridgeService.requestProjectionSession();
        if (!projectionSession) {
            return undefined;
        }
        return (0, athena_graph_glsp_1.translateProjectionSessionToGLSPDiagram)(projectionSession);
    }
    async switchActiveView(viewId) {
        const commandPayload = await this.lspEditorBridgeService.requestProjectionCommand({
            commandId: AthenaGraphAdapterService_1.SWITCH_ACTIVE_VIEW_COMMAND_ID,
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
        return (0, athena_graph_glsp_1.translateProjectionSessionToGLSPDiagram)(commandPayload.session);
    }
};
exports.AthenaGraphAdapterService = AthenaGraphAdapterService;
__decorate([
    (0, inversify_1.inject)(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService),
    __metadata("design:type", athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService)
], AthenaGraphAdapterService.prototype, "lspEditorBridgeService", void 0);
exports.AthenaGraphAdapterService = AthenaGraphAdapterService = AthenaGraphAdapterService_1 = __decorate([
    (0, inversify_1.injectable)()
], AthenaGraphAdapterService);
//# sourceMappingURL=athena-graph-adapter-service.js.map