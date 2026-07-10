"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ATHENA_WORKBENCH_EXTENSIONS = exports.AthenaCommands = exports.ATHENA_VIEW_MENU = void 0;
const common_frontend_contribution_1 = require("@theia/core/lib/browser/common-frontend-contribution");
const problem_widget_1 = require("@theia/markers/lib/browser/problem/problem-widget");
const navigator_widget_1 = require("@theia/navigator/lib/browser/navigator-widget");
const output_widget_1 = require("@theia/output/lib/browser/output-widget");
const athena_graph_workbench_widget_1 = require("./athena-graph-workbench-widget");
const athena_repository_graph_widget_1 = require("./athena-repository-graph-widget");
const athena_semantic_scm_widget_1 = require("./athena-semantic-scm-widget");
const athena_semantic_inspection_widget_1 = require("./athena-semantic-inspection-widget");
exports.ATHENA_VIEW_MENU = [...common_frontend_contribution_1.CommonMenus.VIEW, '9_athena'];
var AthenaCommands;
(function (AthenaCommands) {
    AthenaCommands.CREATE_ENGINEERING_REPOSITORY = {
        id: 'athena.createEngineeringRepository',
        category: 'Athena',
        label: 'New Engineering Repository'
    };
    AthenaCommands.OPEN_ENGINEERING_REPOSITORY = {
        id: 'athena.openEngineeringRepository',
        category: 'Athena',
        label: 'Open Engineering Repository'
    };
    AthenaCommands.OPEN_HOME = {
        id: 'athena.openHome',
        category: 'Athena',
        label: 'Open Athena Home'
    };
    AthenaCommands.REVEAL_REPOSITORY_NAVIGATOR = {
        id: 'athena.revealRepositoryNavigator',
        category: 'Athena',
        label: 'Reveal Repository Navigator'
    };
    AthenaCommands.REVEAL_PROBLEMS = {
        id: 'athena.revealProblems',
        category: 'Athena',
        label: 'Reveal Problems'
    };
    AthenaCommands.REVEAL_OUTPUT = {
        id: 'athena.revealOutput',
        category: 'Athena',
        label: 'Reveal Output'
    };
    AthenaCommands.REVEAL_SEMANTIC_INSPECTION = {
        id: 'athena.revealSemanticInspection',
        category: 'Athena',
        label: 'Reveal Semantic Inspection'
    };
    AthenaCommands.REVEAL_SEMANTIC_SCM = {
        id: 'athena.revealSemanticScm',
        category: 'Athena',
        label: 'Reveal Semantic SCM'
    };
    AthenaCommands.REVEAL_REPOSITORY_GRAPH = {
        id: 'athena.revealRepositoryGraph',
        category: 'Athena',
        label: 'Reveal Repository Graph'
    };
    AthenaCommands.REVEAL_GRAPHICAL_VIEW = {
        id: 'athena.revealGraphicalView',
        category: 'Athena',
        label: 'Reveal Graphical View'
    };
})(AthenaCommands || (exports.AthenaCommands = AthenaCommands = {}));
exports.ATHENA_WORKBENCH_EXTENSIONS = [
    {
        command: AthenaCommands.REVEAL_REPOSITORY_NAVIGATOR,
        widgetId: navigator_widget_1.FILE_NAVIGATOR_ID,
        area: 'left',
        menuOrder: '2',
        quickActionLabel: 'Repository Navigator',
        startupRank: 100
    },
    {
        command: AthenaCommands.REVEAL_PROBLEMS,
        widgetId: problem_widget_1.PROBLEMS_WIDGET_ID,
        area: 'bottom',
        menuOrder: '3',
        quickActionLabel: 'Problems',
        startupRank: 200
    },
    {
        command: AthenaCommands.REVEAL_OUTPUT,
        widgetId: output_widget_1.OutputWidget.ID,
        area: 'bottom',
        menuOrder: '4',
        quickActionLabel: 'Output',
        startupRank: 250
    },
    {
        command: AthenaCommands.REVEAL_GRAPHICAL_VIEW,
        widgetId: athena_graph_workbench_widget_1.AthenaGraphWorkbenchWidget.ID,
        area: 'main',
        menuOrder: '5',
        quickActionLabel: 'Graphical View'
    },
    {
        command: AthenaCommands.REVEAL_REPOSITORY_GRAPH,
        widgetId: athena_repository_graph_widget_1.AthenaRepositoryGraphWidget.ID,
        area: 'right',
        menuOrder: '6',
        quickActionLabel: 'Repository Graph',
        startupRank: 300
    },
    {
        command: AthenaCommands.REVEAL_SEMANTIC_SCM,
        widgetId: athena_semantic_scm_widget_1.AthenaSemanticScmWidget.ID,
        area: 'right',
        menuOrder: '7',
        quickActionLabel: 'Semantic SCM',
        startupRank: 350
    },
    {
        command: AthenaCommands.REVEAL_SEMANTIC_INSPECTION,
        widgetId: athena_semantic_inspection_widget_1.AthenaSemanticInspectionWidget.ID,
        area: 'right',
        menuOrder: '8',
        quickActionLabel: 'Semantic Inspection'
    }
];
//# sourceMappingURL=athena-workbench-extensions.js.map