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
Object.defineProperty(exports, "__esModule", { value: true });
exports.AthenaRepositorySessionService = void 0;
const core_1 = require("@theia/core");
const inversify_1 = require("@theia/core/shared/inversify");
const workspace_service_1 = require("@theia/workspace/lib/browser/workspace-service");
const athena_backend_endpoint_1 = require("./athena-backend-endpoint");
let AthenaRepositorySessionService = class AthenaRepositorySessionService {
    workspaceService;
    messageService;
    onDidChangeStateEmitter = new core_1.Emitter();
    stateValue = {
        lifecycle: 'idle',
        lspLifecycle: 'idle',
        message: 'No Engineering Repository session is active.'
    };
    get state() {
        return this.stateValue;
    }
    get onDidChangeState() {
        return this.onDidChangeStateEmitter.event;
    }
    async onStart(_app) {
        await this.workspaceService.ready;
        if (!this.workspaceService.opened) {
            await this.refreshSessionState();
            return;
        }
        await this.activateCurrentWorkspaceSession();
    }
    async activateCurrentWorkspaceSession() {
        const roots = await this.workspaceService.roots;
        if (roots.length === 0) {
            this.setState({
                lifecycle: 'idle',
                message: 'No Engineering Repository is open in this Athena window.'
            });
            return;
        }
        if (roots.length > 1) {
            this.setState({
                lifecycle: 'unavailable',
                message: 'M4 allows only one active Engineering Repository per Athena window.'
            });
            return;
        }
        const repositoryRootPath = roots[0].resource.path.fsPath();
        this.setState({
            lifecycle: 'activating',
            repositoryRoot: repositoryRootPath,
            message: 'Activating the Athena JVM repository session.'
        });
        try {
            const response = await fetch((0, athena_backend_endpoint_1.toAthenaBackendUrl)('athena/repository-session/activate', {
                repositoryRootPath,
            }), {
                method: 'POST'
            });
            const nextState = await response.json();
            this.setState(nextState);
            if (nextState.lifecycle === 'unavailable') {
                this.messageService.warn(nextState.message);
            }
        }
        catch (error) {
            const nextState = {
                lifecycle: 'unavailable',
                repositoryRoot: repositoryRootPath,
                message: `Failed to activate the Athena JVM repository session: ${error instanceof Error ? error.message : String(error)}`
            };
            this.setState(nextState);
            this.messageService.error(nextState.message);
        }
    }
    async refreshSessionState() {
        try {
            const response = await fetch((0, athena_backend_endpoint_1.toAthenaBackendUrl)('athena/repository-session'));
            const nextState = await response.json();
            this.setState(nextState);
        }
        catch (error) {
            this.setState({
                lifecycle: 'unavailable',
                message: `Failed to query Athena repository-session state: ${error instanceof Error ? error.message : String(error)}`
            });
        }
    }
    setState(state) {
        this.stateValue = state;
        this.onDidChangeStateEmitter.fire(state);
    }
};
exports.AthenaRepositorySessionService = AthenaRepositorySessionService;
__decorate([
    (0, inversify_1.inject)(workspace_service_1.WorkspaceService),
    __metadata("design:type", workspace_service_1.WorkspaceService)
], AthenaRepositorySessionService.prototype, "workspaceService", void 0);
__decorate([
    (0, inversify_1.inject)(core_1.MessageService),
    __metadata("design:type", core_1.MessageService)
], AthenaRepositorySessionService.prototype, "messageService", void 0);
exports.AthenaRepositorySessionService = AthenaRepositorySessionService = __decorate([
    (0, inversify_1.injectable)()
], AthenaRepositorySessionService);
//# sourceMappingURL=athena-repository-session-service.js.map