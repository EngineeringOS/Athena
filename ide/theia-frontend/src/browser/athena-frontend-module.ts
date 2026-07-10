import '../../src/browser/style/index.css';

import { FrontendApplicationContribution, WidgetFactory } from '@theia/core/lib/browser';
import { CommandContribution, MenuContribution } from '@theia/core/lib/common';
import { ContainerModule } from '@theia/core/shared/inversify';
import { AthenaGraphAdapterService } from './athena-graph-adapter-service';
import { AthenaGraphWorkbenchWidget } from './athena-graph-workbench-widget';
import { AthenaHomeWidget } from './athena-home-widget';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaProductContribution } from './athena-product-contribution';
import { AthenaRepositoryGraphWidget } from './athena-repository-graph-widget';
import { AthenaRepositoryCreationService } from './athena-repository-creation-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';
import { AthenaSemanticScmWidget } from './athena-semantic-scm-widget';
import { AthenaSemanticInspectionWidget } from './athena-semantic-inspection-widget';

export default new ContainerModule(bind => {
    bind(AthenaRepositoryCreationService).toSelf().inSingletonScope();
    bind(AthenaRepositorySessionService).toSelf().inSingletonScope();
    bind(AthenaLspEditorBridgeService).toSelf().inSingletonScope();
    bind(AthenaGraphAdapterService).toSelf().inSingletonScope();
    bind(AthenaSemanticSelectionService).toSelf().inSingletonScope();
    bind(FrontendApplicationContribution).toService(AthenaRepositorySessionService);
    bind(FrontendApplicationContribution).toService(AthenaLspEditorBridgeService);
    bind(FrontendApplicationContribution).toService(AthenaSemanticSelectionService);

    bind(AthenaHomeWidget).toSelf();
    bind(AthenaGraphWorkbenchWidget).toSelf();
    bind(AthenaRepositoryGraphWidget).toSelf();
    bind(AthenaSemanticScmWidget).toSelf();
    bind(AthenaSemanticInspectionWidget).toSelf();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: AthenaHomeWidget.ID,
        createWidget: () => context.container.get<AthenaHomeWidget>(AthenaHomeWidget)
    })).inSingletonScope();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: AthenaGraphWorkbenchWidget.ID,
        createWidget: () => context.container.get<AthenaGraphWorkbenchWidget>(AthenaGraphWorkbenchWidget)
    })).inSingletonScope();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: AthenaRepositoryGraphWidget.ID,
        createWidget: () => context.container.get<AthenaRepositoryGraphWidget>(AthenaRepositoryGraphWidget)
    })).inSingletonScope();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: AthenaSemanticScmWidget.ID,
        createWidget: () => context.container.get<AthenaSemanticScmWidget>(AthenaSemanticScmWidget)
    })).inSingletonScope();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: AthenaSemanticInspectionWidget.ID,
        createWidget: () => context.container.get<AthenaSemanticInspectionWidget>(AthenaSemanticInspectionWidget)
    })).inSingletonScope();

    bind(AthenaProductContribution).toSelf().inSingletonScope();
    bind(CommandContribution).toService(AthenaProductContribution);
    bind(MenuContribution).toService(AthenaProductContribution);
    bind(FrontendApplicationContribution).toService(AthenaProductContribution);
});
