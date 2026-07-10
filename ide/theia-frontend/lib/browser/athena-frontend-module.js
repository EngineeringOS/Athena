"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
require("../../src/browser/style/index.css");
const browser_1 = require("@theia/core/lib/browser");
const common_1 = require("@theia/core/lib/common");
const inversify_1 = require("@theia/core/shared/inversify");
const athena_graph_adapter_service_1 = require("./athena-graph-adapter-service");
const athena_graph_workbench_widget_1 = require("./athena-graph-workbench-widget");
const athena_home_widget_1 = require("./athena-home-widget");
const athena_lsp_editor_bridge_service_1 = require("./athena-lsp-editor-bridge-service");
const athena_product_contribution_1 = require("./athena-product-contribution");
const athena_repository_graph_widget_1 = require("./athena-repository-graph-widget");
const athena_repository_creation_service_1 = require("./athena-repository-creation-service");
const athena_repository_session_service_1 = require("./athena-repository-session-service");
const athena_semantic_selection_service_1 = require("./athena-semantic-selection-service");
const athena_semantic_scm_widget_1 = require("./athena-semantic-scm-widget");
const athena_semantic_inspection_widget_1 = require("./athena-semantic-inspection-widget");
exports.default = new inversify_1.ContainerModule(bind => {
    bind(athena_repository_creation_service_1.AthenaRepositoryCreationService).toSelf().inSingletonScope();
    bind(athena_repository_session_service_1.AthenaRepositorySessionService).toSelf().inSingletonScope();
    bind(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService).toSelf().inSingletonScope();
    bind(athena_graph_adapter_service_1.AthenaGraphAdapterService).toSelf().inSingletonScope();
    bind(athena_semantic_selection_service_1.AthenaSemanticSelectionService).toSelf().inSingletonScope();
    bind(browser_1.FrontendApplicationContribution).toService(athena_repository_session_service_1.AthenaRepositorySessionService);
    bind(browser_1.FrontendApplicationContribution).toService(athena_lsp_editor_bridge_service_1.AthenaLspEditorBridgeService);
    bind(browser_1.FrontendApplicationContribution).toService(athena_semantic_selection_service_1.AthenaSemanticSelectionService);
    bind(athena_home_widget_1.AthenaHomeWidget).toSelf();
    bind(athena_graph_workbench_widget_1.AthenaGraphWorkbenchWidget).toSelf();
    bind(athena_repository_graph_widget_1.AthenaRepositoryGraphWidget).toSelf();
    bind(athena_semantic_scm_widget_1.AthenaSemanticScmWidget).toSelf();
    bind(athena_semantic_inspection_widget_1.AthenaSemanticInspectionWidget).toSelf();
    bind(browser_1.WidgetFactory).toDynamicValue(context => ({
        id: athena_home_widget_1.AthenaHomeWidget.ID,
        createWidget: () => context.container.get(athena_home_widget_1.AthenaHomeWidget)
    })).inSingletonScope();
    bind(browser_1.WidgetFactory).toDynamicValue(context => ({
        id: athena_graph_workbench_widget_1.AthenaGraphWorkbenchWidget.ID,
        createWidget: () => context.container.get(athena_graph_workbench_widget_1.AthenaGraphWorkbenchWidget)
    })).inSingletonScope();
    bind(browser_1.WidgetFactory).toDynamicValue(context => ({
        id: athena_repository_graph_widget_1.AthenaRepositoryGraphWidget.ID,
        createWidget: () => context.container.get(athena_repository_graph_widget_1.AthenaRepositoryGraphWidget)
    })).inSingletonScope();
    bind(browser_1.WidgetFactory).toDynamicValue(context => ({
        id: athena_semantic_scm_widget_1.AthenaSemanticScmWidget.ID,
        createWidget: () => context.container.get(athena_semantic_scm_widget_1.AthenaSemanticScmWidget)
    })).inSingletonScope();
    bind(browser_1.WidgetFactory).toDynamicValue(context => ({
        id: athena_semantic_inspection_widget_1.AthenaSemanticInspectionWidget.ID,
        createWidget: () => context.container.get(athena_semantic_inspection_widget_1.AthenaSemanticInspectionWidget)
    })).inSingletonScope();
    bind(athena_product_contribution_1.AthenaProductContribution).toSelf().inSingletonScope();
    bind(common_1.CommandContribution).toService(athena_product_contribution_1.AthenaProductContribution);
    bind(common_1.MenuContribution).toService(athena_product_contribution_1.AthenaProductContribution);
    bind(browser_1.FrontendApplicationContribution).toService(athena_product_contribution_1.AthenaProductContribution);
});
//# sourceMappingURL=athena-frontend-module.js.map