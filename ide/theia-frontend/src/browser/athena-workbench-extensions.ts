import type { ApplicationShell } from '@theia/core/lib/browser';
import { CommonMenus } from '@theia/core/lib/browser/common-frontend-contribution';
import type { Command } from '@theia/core/lib/common/command';
import { PROBLEMS_WIDGET_ID } from '@theia/markers/lib/browser/problem/problem-widget';
import { FILE_NAVIGATOR_ID } from '@theia/navigator/lib/browser/navigator-widget';
import { OutputWidget } from '@theia/output/lib/browser/output-widget';
import { AthenaComponentPanelWidget } from './athena-component-panel-widget';
import { AthenaGraphWorkbenchWidget } from './athena-graph-workbench-widget';
import { AthenaRepositoryGraphWidget } from './athena-repository-graph-widget';
import { AthenaSemanticMacroCatalogWidget } from './athena-semantic-macro-catalog-widget';
import { AthenaSemanticScmWidget } from './athena-semantic-scm-widget';
import { AthenaSemanticInspectionWidget } from './athena-semantic-inspection-widget';

export const ATHENA_VIEW_MENU = [...CommonMenus.VIEW, '9_athena'];

export namespace AthenaCommands {
    export const CREATE_ENGINEERING_REPOSITORY: Command = {
        id: 'athena.createEngineeringRepository',
        category: 'Athena',
        label: 'New Engineering Repository'
    };

    export const OPEN_ENGINEERING_REPOSITORY: Command = {
        id: 'athena.openEngineeringRepository',
        category: 'Athena',
        label: 'Open Engineering Repository'
    };

    export const OPEN_HOME: Command = {
        id: 'athena.openHome',
        category: 'Athena',
        label: 'Open Athena Home'
    };

    export const REVEAL_REPOSITORY_NAVIGATOR: Command = {
        id: 'athena.revealRepositoryNavigator',
        category: 'Athena',
        label: 'Reveal Repository Navigator'
    };

    export const REVEAL_COMPONENT_PANEL: Command = {
        id: 'athena.revealComponentPanel',
        category: 'Athena',
        label: 'Reveal Components'
    };

    export const REVEAL_PROBLEMS: Command = {
        id: 'athena.revealProblems',
        category: 'Athena',
        label: 'Reveal Problems'
    };

    export const REVEAL_OUTPUT: Command = {
        id: 'athena.revealOutput',
        category: 'Athena',
        label: 'Reveal Output'
    };

    export const REVEAL_SEMANTIC_INSPECTION: Command = {
        id: 'athena.revealSemanticInspection',
        category: 'Athena',
        label: 'Reveal Semantic Inspection'
    };

    export const REVEAL_SEMANTIC_SCM: Command = {
        id: 'athena.revealSemanticScm',
        category: 'Athena',
        label: 'Reveal Semantic SCM'
    };

    export const REVEAL_REPOSITORY_GRAPH: Command = {
        id: 'athena.revealRepositoryGraph',
        category: 'Athena',
        label: 'Reveal Repository Graph'
    };

    export const REVEAL_REUSE_CATALOG: Command = {
        id: 'athena.revealReuseCatalog',
        category: 'Athena',
        label: 'Reveal Reuse Catalog'
    };

    export const REVEAL_GRAPHICAL_VIEW: Command = {
        id: 'athena.revealGraphicalView',
        category: 'Athena',
        label: 'Reveal Graphical View'
    };
}

export interface AthenaWorkbenchExtension {
    readonly command: Command;
    readonly widgetId: string;
    readonly area: ApplicationShell.Area;
    readonly menuOrder: string;
    readonly quickActionLabel: string;
    readonly startupRank?: number;
}

export const ATHENA_WORKBENCH_EXTENSIONS: readonly AthenaWorkbenchExtension[] = [
    {
        command: AthenaCommands.REVEAL_REPOSITORY_NAVIGATOR,
        widgetId: FILE_NAVIGATOR_ID,
        area: 'left',
        menuOrder: '2',
        quickActionLabel: 'Repository Navigator',
        startupRank: 100
    },
    {
        command: AthenaCommands.REVEAL_COMPONENT_PANEL,
        widgetId: AthenaComponentPanelWidget.ID,
        area: 'left',
        menuOrder: '2.5',
        quickActionLabel: 'Components',
        startupRank: 150
    },
    {
        command: AthenaCommands.REVEAL_PROBLEMS,
        widgetId: PROBLEMS_WIDGET_ID,
        area: 'bottom',
        menuOrder: '3',
        quickActionLabel: 'Problems',
        startupRank: 200
    },
    {
        command: AthenaCommands.REVEAL_OUTPUT,
        widgetId: OutputWidget.ID,
        area: 'bottom',
        menuOrder: '4',
        quickActionLabel: 'Output',
        startupRank: 250
    },
    {
        command: AthenaCommands.REVEAL_GRAPHICAL_VIEW,
        widgetId: AthenaGraphWorkbenchWidget.ID,
        area: 'main',
        menuOrder: '5',
        quickActionLabel: 'Graphical View'
    },
    {
        command: AthenaCommands.REVEAL_REUSE_CATALOG,
        widgetId: AthenaSemanticMacroCatalogWidget.ID,
        area: 'right',
        menuOrder: '6.5',
        quickActionLabel: 'Reuse Catalog',
        startupRank: 325
    },
    {
        command: AthenaCommands.REVEAL_REPOSITORY_GRAPH,
        widgetId: AthenaRepositoryGraphWidget.ID,
        area: 'right',
        menuOrder: '6',
        quickActionLabel: 'Repository Graph',
        startupRank: 300
    },
    {
        command: AthenaCommands.REVEAL_SEMANTIC_SCM,
        widgetId: AthenaSemanticScmWidget.ID,
        area: 'right',
        menuOrder: '7',
        quickActionLabel: 'Semantic SCM',
        startupRank: 350
    },
    {
        command: AthenaCommands.REVEAL_SEMANTIC_INSPECTION,
        widgetId: AthenaSemanticInspectionWidget.ID,
        area: 'right',
        menuOrder: '8',
        quickActionLabel: 'Semantic Inspection'
    }
];
