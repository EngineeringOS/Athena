import type { ApplicationShell } from '@theia/core/lib/browser';
import type { Command } from '@theia/core/lib/common/command';
export declare const ATHENA_VIEW_MENU: string[];
export declare namespace AthenaCommands {
    const CREATE_ENGINEERING_REPOSITORY: Command;
    const OPEN_ENGINEERING_REPOSITORY: Command;
    const OPEN_HOME: Command;
    const REVEAL_REPOSITORY_NAVIGATOR: Command;
    const REVEAL_PROBLEMS: Command;
    const REVEAL_OUTPUT: Command;
    const REVEAL_SEMANTIC_INSPECTION: Command;
    const REVEAL_SEMANTIC_SCM: Command;
    const REVEAL_REPOSITORY_GRAPH: Command;
    const REVEAL_GRAPHICAL_VIEW: Command;
}
export interface AthenaWorkbenchExtension {
    readonly command: Command;
    readonly widgetId: string;
    readonly area: ApplicationShell.Area;
    readonly menuOrder: string;
    readonly quickActionLabel: string;
    readonly startupRank?: number;
}
export declare const ATHENA_WORKBENCH_EXTENSIONS: readonly AthenaWorkbenchExtension[];
//# sourceMappingURL=athena-workbench-extensions.d.ts.map