import { AbstractViewContribution, ApplicationShell, FrontendApplicationContribution, OpenerService, open } from '@theia/core/lib/browser';
import { CommonMenus } from '@theia/core/lib/browser/common-frontend-contribution';
import { CommandContribution, CommandRegistry } from '@theia/core/lib/common/command';
import { MenuContribution, MenuModelRegistry } from '@theia/core/lib/common/menu';
import URI from '@theia/core/lib/common/uri';
import { injectable, inject } from '@theia/core/shared/inversify';
import { EditorManager } from '@theia/editor/lib/browser';
import { WorkspaceCommands } from '@theia/workspace/lib/browser/workspace-commands';
import { AthenaGraphWorkbenchWidget } from './athena-graph-workbench-widget';
import { AthenaHomeWidget } from './athena-home-widget';
import { AthenaRepositoryCreationService } from './athena-repository-creation-service';
import {
    ATHENA_VIEW_MENU,
    ATHENA_WORKBENCH_EXTENSIONS,
    AthenaCommands,
    AthenaWorkbenchExtension
} from './athena-workbench-extensions';

declare global {
    interface Window {
        __athenaWorkbenchSmoke?: {
            revealGraphicalView: () => Promise<void>;
            revealOutlineForSource: (sourceUri: string) => Promise<AthenaOutlineSmokeProof>;
            openSourceEditorForSmoke: (sourceUri: string) => Promise<AthenaSourceEditorSmokeProof>;
            revealSourceLineForSmoke: (lineNumber: number) => Promise<void>;
        };
    }
}

export interface AthenaOutlineSmokeProof {
    readonly widgetId: string;
    readonly hasOutlineWidget: boolean;
    readonly nodeNames: string[];
    readonly paths: string[];
}

export interface AthenaSourceEditorSmokeProof {
    readonly widgetId: string;
    readonly resourceUri: string;
    readonly currentEditorWidgetId: string;
}

type AthenaOpenableEditorWidget = {
    id?: string;
};

@injectable()
export class AthenaProductContribution extends AbstractViewContribution<AthenaHomeWidget>
implements FrontendApplicationContribution, CommandContribution, MenuContribution {
    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositoryCreationService)
    protected readonly repositoryCreationService: AthenaRepositoryCreationService;

    @inject(OpenerService)
    protected readonly openerService: OpenerService;

    constructor() {
        super({
            widgetId: AthenaHomeWidget.ID,
            widgetName: AthenaHomeWidget.LABEL,
            defaultWidgetOptions: {
                area: 'main'
            }
        });
    }

    async initializeLayout(): Promise<void> {
        await this.openView({
            activate: true,
            reveal: true
        });
        await this.ensureProfessionalWorkbenchLayout();
    }

    registerCommands(commands: CommandRegistry): void {
        super.registerCommands(commands);
        commands.registerCommand(AthenaCommands.CREATE_ENGINEERING_REPOSITORY, {
            execute: async () => this.repositoryCreationService.createRepository()
        });
        commands.registerCommand(AthenaCommands.OPEN_ENGINEERING_REPOSITORY, {
            execute: () => commands.executeCommand(WorkspaceCommands.OPEN_FOLDER.id)
        });
        commands.registerCommand(AthenaCommands.OPEN_HOME, {
            execute: () => this.openView({
                activate: true,
                reveal: true
            })
        });
        for (const extension of ATHENA_WORKBENCH_EXTENSIONS) {
            commands.registerCommand(extension.command, {
                execute: () => this.revealWorkbenchExtension(extension)
            });
        }
        if (typeof window !== 'undefined') {
            window.__athenaWorkbenchSmoke = {
                revealGraphicalView: () => commands.executeCommand(AthenaCommands.REVEAL_GRAPHICAL_VIEW.id),
                revealOutlineForSource: sourceUri => this.revealOutlineForSource(commands, sourceUri),
                openSourceEditorForSmoke: sourceUri => this.openSourceEditorForSmoke(sourceUri),
                revealSourceLineForSmoke: lineNumber => this.revealSourceLineForSmoke(lineNumber)
            };
        }
    }

    registerMenus(menus: MenuModelRegistry): void {
        super.registerMenus(menus);
        menus.registerMenuAction([...CommonMenus.FILE_NEW, '1_athena'], {
            commandId: AthenaCommands.CREATE_ENGINEERING_REPOSITORY.id,
            label: AthenaCommands.CREATE_ENGINEERING_REPOSITORY.label,
            order: '1'
        });
        menus.registerMenuAction([...CommonMenus.FILE_OPEN, '1_athena'], {
            commandId: AthenaCommands.OPEN_ENGINEERING_REPOSITORY.id,
            label: AthenaCommands.OPEN_ENGINEERING_REPOSITORY.label,
            order: '1'
        });
        menus.registerMenuAction([...CommonMenus.HELP, '9_athena'], {
            commandId: AthenaCommands.OPEN_HOME.id,
            label: AthenaCommands.OPEN_HOME.label,
            order: '1'
        });
        menus.registerSubmenu(ATHENA_VIEW_MENU, 'Athena');
        menus.registerMenuAction(ATHENA_VIEW_MENU, {
            commandId: AthenaCommands.OPEN_HOME.id,
            label: AthenaCommands.OPEN_HOME.label,
            order: '1'
        });
        for (const extension of ATHENA_WORKBENCH_EXTENSIONS) {
            menus.registerMenuAction(ATHENA_VIEW_MENU, {
                commandId: extension.command.id,
                label: extension.command.label,
                order: extension.menuOrder
            });
        }
    }

    protected async ensureProfessionalWorkbenchLayout(): Promise<void> {
        const expandedAreas = new Set<ApplicationShell.Area>();
        for (const extension of ATHENA_WORKBENCH_EXTENSIONS) {
            if (extension.startupRank === undefined) {
                continue;
            }
            await this.ensureWidget(extension.widgetId, {
                area: extension.area,
                rank: extension.startupRank
            });
            expandedAreas.add(extension.area);
        }

        for (const area of expandedAreas) {
            this.shell.expandPanel(area);
        }
    }

    protected async ensureWidget(
        widgetId: string,
        options: ApplicationShell.WidgetOptions
    ): Promise<void> {
        const existing = this.shell.getWidgetById(widgetId);
        if (existing) {
            await this.shell.revealWidget(widgetId);
            return;
        }

        const widget = await this.widgetManager.getOrCreateWidget(widgetId);
        await this.shell.addWidget(widget, options);
    }

    protected async revealWorkbenchWidget(
        widgetId: string,
        area: ApplicationShell.Area
    ): Promise<void> {
        await this.ensureWidget(widgetId, { area });
        this.shell.expandPanel(area);
        await this.shell.activateWidget(widgetId);
    }

    protected revealWorkbenchExtension(extension: AthenaWorkbenchExtension): Promise<void> {
        if (extension.widgetId === AthenaGraphWorkbenchWidget.ID) {
            return this.revealGraphWorkbench(extension);
        }
        return this.revealWorkbenchWidget(extension.widgetId, extension.area);
    }

    protected async revealGraphWorkbench(extension: AthenaWorkbenchExtension): Promise<void> {
        const existing = this.shell.getWidgetById(extension.widgetId);
        if (!existing) {
            const widget = await this.widgetManager.getOrCreateWidget(extension.widgetId);
            const currentEditor = this.editorManager.currentEditor;
            if (currentEditor) {
                await this.shell.addWidget(widget, {
                    area: 'main',
                    mode: 'open-to-right',
                    ref: currentEditor
                });
            } else {
                await this.shell.addWidget(widget, { area: extension.area });
            }
        } else {
            await this.shell.revealWidget(extension.widgetId);
        }

        await this.shell.activateWidget(extension.widgetId);
    }

    protected async revealOutlineForSource(
        commands: CommandRegistry,
        sourceUri: string
    ): Promise<AthenaOutlineSmokeProof> {
        await open(this.openerService, new URI(sourceUri));
        await this.revealWorkbenchWidget('outline-view', 'right');
        await commands.executeCommand('outlineView.expand.all').catch(() => undefined);
        const outlineWidget = this.shell.getWidgetById('outline-view') as unknown as {
            id?: string;
            model?: { root?: AthenaOutlineNode };
        } | undefined;
        const root = await this.waitForOutlineRoot(outlineWidget);
        const tree = this.collectOutlineTree(root);
        return {
            widgetId: outlineWidget?.id ?? '',
            hasOutlineWidget: outlineWidget?.id === 'outline-view',
            nodeNames: this.collectOutlineNodeNames(root),
            paths: this.collectOutlinePaths(tree)
        };
    }

    protected async openSourceEditorForSmoke(sourceUri: string): Promise<AthenaSourceEditorSmokeProof> {
        const widget = await open(this.openerService, new URI(sourceUri)) as AthenaOpenableEditorWidget | undefined;
        if (widget?.id) {
            await this.shell.activateWidget(widget.id).catch(() => undefined);
        }
        const editorWidget = this.editorManager.currentEditor;
        if (editorWidget?.id) {
            await this.shell.activateWidget(editorWidget.id).catch(() => undefined);
        }
        return {
            widgetId: widget?.id ?? '',
            resourceUri: editorWidget?.getResourceUri()?.toString() ?? '',
            currentEditorWidgetId: editorWidget?.id ?? ''
        };
    }

    protected async revealSourceLineForSmoke(lineNumber: number): Promise<void> {
        const editorWidget = this.editorManager.currentEditor;
        if (!editorWidget) {
            return;
        }
        editorWidget.editor.cursor = { line: Math.max(0, lineNumber - 1), character: 0 };
        editorWidget.editor.revealPosition(
            { line: Math.max(0, lineNumber - 1), character: 0 },
            { vertical: 'center', horizontal: true }
        );
        editorWidget.editor.focus();
        await new Promise(resolve => window.requestAnimationFrame(() => window.requestAnimationFrame(resolve)));
    }

    protected async waitForOutlineRoot(
        outlineWidget: { model?: { root?: AthenaOutlineNode } } | undefined,
        timeoutMs = 10000,
        intervalMs = 100,
    ): Promise<AthenaOutlineNode | undefined> {
        const startedAt = Date.now();
        let lastRoot = outlineWidget?.model?.root;
        while (Date.now() - startedAt < timeoutMs) {
            const currentRoot = outlineWidget?.model?.root;
            if (currentRoot) {
                const paths = this.collectOutlinePaths(this.collectOutlineTree(currentRoot));
                if (paths.length > 0) {
                    return currentRoot;
                }
                lastRoot = currentRoot;
            }
            await new Promise(resolve => window.setTimeout(resolve, intervalMs));
        }
        return lastRoot;
    }

    protected collectOutlineTree(node: AthenaOutlineNode | undefined): AthenaOutlineTree | undefined {
        if (!node) {
            return undefined;
        }
        return {
            name: node.name ?? '',
            id: node.id ?? '',
            children: Array.from(node.children ?? [])
                .map(child => this.collectOutlineTree(child))
                .filter((child): child is AthenaOutlineTree => !!child)
        };
    }

    protected collectOutlineNodeNames(node: AthenaOutlineNode | undefined): string[] {
        if (!node) {
            return [];
        }
        return [
            node.name ?? '',
            ...Array.from(node.children ?? []).flatMap(child => this.collectOutlineNodeNames(child))
        ].filter(Boolean);
    }

    protected collectOutlinePaths(tree: AthenaOutlineTree | undefined, prefix: string[] = []): string[] {
        if (!tree) {
            return [];
        }
        const current = tree.name && tree.name !== 'Outline' ? [...prefix, tree.name] : prefix;
        const childPaths = tree.children.flatMap(child => this.collectOutlinePaths(child, current));
        return current.length > 0 ? [current.join(' > '), ...childPaths] : childPaths;
    }
}

interface AthenaOutlineNode {
    readonly id?: string;
    readonly name?: string;
    readonly children?: readonly AthenaOutlineNode[];
}

interface AthenaOutlineTree {
    readonly id: string;
    readonly name: string;
    readonly children: AthenaOutlineTree[];
}
