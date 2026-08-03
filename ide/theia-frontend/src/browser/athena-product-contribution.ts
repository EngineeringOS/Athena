import { AbstractViewContribution, ApplicationShell, FrontendApplication, FrontendApplicationContribution, OpenerService, open } from '@theia/core/lib/browser';
import { CommonMenus } from '@theia/core/lib/browser/common-frontend-contribution';
import { CommandContribution, CommandRegistry } from '@theia/core/lib/common/command';
import { MenuContribution, MenuModelRegistry } from '@theia/core/lib/common/menu';
import URI from '@theia/core/lib/common/uri';
import { injectable, inject } from '@theia/core/shared/inversify';
import { EditorManager } from '@theia/editor/lib/browser';
import { ProblemManager } from '@theia/markers/lib/browser/problem/problem-manager';
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
        __athenaWorkbenchAutomation?: {
            revealGraphicalView: () => Promise<void>;
            switchProjectionView: (viewId: string) => Promise<boolean>;
            refreshProjectionView: () => Promise<boolean>;
            revealOutlineForSource: (sourceUri: string) => Promise<AthenaOutlineSnapshot>;
            openSourceEditor: (sourceUri: string) => Promise<AthenaSourceEditorSnapshot>;
            revealSourceLine: (lineNumber: number) => Promise<void>;
            collectGraphWorkbenchSnapshot: () => Promise<AthenaGraphWorkbenchSnapshot>;
        };
    }
}

export interface AthenaOutlineSnapshot {
    readonly widgetId: string;
    readonly hasOutlineWidget: boolean;
    readonly nodeNames: string[];
    readonly paths: string[];
}

export interface AthenaSourceEditorSnapshot {
    readonly widgetId: string;
    readonly resourceUri: string;
    readonly currentEditorWidgetId: string;
    readonly problemMarkerCount: number;
    readonly zeroProblemMarkerCount: boolean;
    readonly problemMarkers: string[];
}

export interface AthenaGraphWorkbenchSnapshot {
    readonly available: boolean;
    readonly hasDiagram: boolean;
    readonly activeViewId: string;
    readonly presentationGraphicOccurrenceCount: number;
    readonly presentationConnectorCount: number;
    readonly presentationOccurrenceCount: number;
    readonly drawingCompositionPrimitiveCount: number;
    readonly firstGraphicOccurrenceId: string;
    readonly firstGraphicOccurrenceBounds?: unknown;
    readonly canvasWidth: number;
    readonly canvasHeight: number;
}

@injectable()
export class AthenaProductContribution extends AbstractViewContribution<AthenaHomeWidget>
implements FrontendApplicationContribution, CommandContribution, MenuContribution {
    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaRepositoryCreationService)
    protected readonly repositoryCreationService: AthenaRepositoryCreationService;

    @inject(OpenerService)
    protected readonly openerService: OpenerService;

    @inject(ProblemManager)
    protected readonly problemManager: ProblemManager;

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

    onStart(_app: FrontendApplication): void {
        this.clearGeneratedPreloadOverlay();
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
            window.__athenaWorkbenchAutomation = {
                revealGraphicalView: () => commands.executeCommand(AthenaCommands.REVEAL_GRAPHICAL_VIEW.id),
                switchProjectionView: viewId => this.switchProjectionView(viewId),
                refreshProjectionView: () => this.refreshProjection(),
                revealOutlineForSource: sourceUri => this.revealOutlineForSource(commands, sourceUri),
                openSourceEditor: sourceUri => this.openSourceEditor(sourceUri),
                revealSourceLine: lineNumber => this.revealSourceLine(lineNumber),
                collectGraphWorkbenchSnapshot: () => this.collectGraphWorkbenchSnapshot()
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

    protected clearGeneratedPreloadOverlay(): void {
        if (typeof document === 'undefined') {
            return;
        }
        window.requestAnimationFrame(() => {
            document.querySelectorAll<HTMLElement>('.theia-preload').forEach(preload => {
                preload.style.display = 'none';
                preload.style.pointerEvents = 'none';
            });
        });
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
            await this.shell.addWidget(widget, { area: extension.area });
        } else {
            await this.shell.revealWidget(extension.widgetId);
        }

        await this.shell.activateWidget(extension.widgetId);
    }

    protected async switchProjectionView(viewId: string): Promise<boolean> {
        const graphExtension = ATHENA_WORKBENCH_EXTENSIONS.find(extension =>
            extension.widgetId === AthenaGraphWorkbenchWidget.ID
        );
        if (!graphExtension) {
            return false;
        }
        await this.revealGraphWorkbench(graphExtension);
        const widget = this.shell.getWidgetById(AthenaGraphWorkbenchWidget.ID)
            ?? await this.widgetManager.getOrCreateWidget(AthenaGraphWorkbenchWidget.ID);
        const graphWidget = widget as unknown as {
            switchActiveView: (requestedViewId: string) => Promise<boolean>;
        };
        return graphWidget.switchActiveView(viewId);
    }

    protected async refreshProjection(): Promise<boolean> {
        const graphExtension = ATHENA_WORKBENCH_EXTENSIONS.find(extension =>
            extension.widgetId === AthenaGraphWorkbenchWidget.ID
        );
        if (!graphExtension) {
            return false;
        }
        await this.revealGraphWorkbench(graphExtension);
        const widget = this.shell.getWidgetById(AthenaGraphWorkbenchWidget.ID)
            ?? await this.widgetManager.getOrCreateWidget(AthenaGraphWorkbenchWidget.ID);
        const graphWidget = widget as AthenaGraphWorkbenchWidget;
        await graphWidget.refreshProjection();
        return true;
    }

    protected async collectGraphWorkbenchSnapshot(): Promise<AthenaGraphWorkbenchSnapshot> {
        const widget = this.shell.getWidgetById(AthenaGraphWorkbenchWidget.ID)
            ?? await this.widgetManager.getOrCreateWidget(AthenaGraphWorkbenchWidget.ID);
        const diagram = (widget as unknown as { diagram?: {
            activeViewId?: string;
            presentation?: {
                canvasWidth?: number;
                canvasHeight?: number;
                graphicOccurrences?: Array<{ occurrenceId?: string; bounds?: unknown }>;
                connectors?: unknown[];
                occurrences?: unknown[];
                drawingComposition?: { primitives?: unknown[] };
            };
        } }).diagram;
        const presentation = diagram?.presentation;
        return {
            available: !!widget,
            hasDiagram: !!diagram,
            activeViewId: diagram?.activeViewId ?? '',
            presentationGraphicOccurrenceCount: Array.isArray(presentation?.graphicOccurrences)
                ? presentation.graphicOccurrences.length
                : -1,
            presentationConnectorCount: Array.isArray(presentation?.connectors)
                ? presentation.connectors.length
                : -1,
            presentationOccurrenceCount: Array.isArray(presentation?.occurrences)
                ? presentation.occurrences.length
                : -1,
            drawingCompositionPrimitiveCount: Array.isArray(presentation?.drawingComposition?.primitives)
                ? presentation.drawingComposition.primitives.length
                : -1,
            firstGraphicOccurrenceId: presentation?.graphicOccurrences?.[0]?.occurrenceId ?? '',
            firstGraphicOccurrenceBounds: presentation?.graphicOccurrences?.[0]?.bounds,
            canvasWidth: presentation?.canvasWidth ?? 0,
            canvasHeight: presentation?.canvasHeight ?? 0
        };
    }

    protected async revealOutlineForSource(
        commands: CommandRegistry,
        sourceUri: string
    ): Promise<AthenaOutlineSnapshot> {
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

    protected async openSourceEditor(sourceUri: string): Promise<AthenaSourceEditorSnapshot> {
        const uri = new URI(sourceUri);
        await open(this.openerService, uri);
        await new Promise(resolve => window.setTimeout(resolve, 500));
        const editorWidget = this.editorManager.currentEditor;
        if (editorWidget?.id) {
            await this.shell.activateWidget(editorWidget.id).catch(() => undefined);
        }
        const problemMarkerCount = this.problemManager.findMarkers({ uri }).length;
        const problemMarkers = this.problemManager.findMarkers({ uri })
            .map(marker => {
                const data = marker.data as { message?: string; code?: string | number; source?: string };
                return [data.source, data.code, data.message]
                    .filter(value => value !== undefined && value !== '')
                    .join(' ');
            })
            .filter(Boolean);
        return {
            widgetId: editorWidget?.id ?? '',
            resourceUri: editorWidget?.getResourceUri()?.toString() ?? '',
            currentEditorWidgetId: editorWidget?.id ?? '',
            problemMarkerCount,
            zeroProblemMarkerCount: problemMarkerCount === 0,
            problemMarkers
        };
    }

    protected async revealSourceLine(lineNumber: number): Promise<void> {
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
