import '../../src/browser/style/index.css';

import { FrontendApplicationContribution, WidgetFactory } from '@theia/core/lib/browser';
import { CommandContribution, MenuContribution } from '@theia/core/lib/common';
import { ContainerModule } from '@theia/core/shared/inversify';
import { AthenaHomeWidget } from './athena-home-widget';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaProductContribution } from './athena-product-contribution';
import { AthenaRepositoryGraphWidget } from './athena-repository-graph-widget';
import { AthenaRepositoryCreationService } from './athena-repository-creation-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';
import { AthenaSemanticInspectionWidget } from './athena-semantic-inspection-widget';
import { AthenaTreeSitterHighlightingService } from './athena-tree-sitter-highlighting-service';

export default new ContainerModule(bind => {
    bind(AthenaRepositoryCreationService).toSelf().inSingletonScope();
    bind(AthenaRepositorySessionService).toSelf().inSingletonScope();
    bind(AthenaTreeSitterHighlightingService).toSelf().inSingletonScope();
    bind(AthenaLspEditorBridgeService).toSelf().inSingletonScope();
    bind(AthenaSemanticSelectionService).toSelf().inSingletonScope();
    bind(FrontendApplicationContribution).toService(AthenaRepositorySessionService);
    bind(FrontendApplicationContribution).toService(AthenaLspEditorBridgeService);
    bind(FrontendApplicationContribution).toService(AthenaSemanticSelectionService);

    bind(AthenaHomeWidget).toSelf();
    bind(AthenaRepositoryGraphWidget).toSelf();
    bind(AthenaSemanticInspectionWidget).toSelf();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: AthenaHomeWidget.ID,
        createWidget: () => context.container.get<AthenaHomeWidget>(AthenaHomeWidget)
    })).inSingletonScope();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: AthenaRepositoryGraphWidget.ID,
        createWidget: () => context.container.get<AthenaRepositoryGraphWidget>(AthenaRepositoryGraphWidget)
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
