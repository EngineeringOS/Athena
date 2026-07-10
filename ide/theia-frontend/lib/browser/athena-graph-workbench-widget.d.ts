import * as React from '@theia/core/shared/react';
import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { AthenaGraphAdapterService } from './athena-graph-adapter-service';
import { AthenaGraphViewportSize, AthenaGraphViewportTransform, buildAthenaGraphWorkbenchModel } from './athena-graph-workbench-model';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';
type AthenaGraphPanState = {
    pointerId: number;
    lastClientX: number;
    lastClientY: number;
};
/** Graph-first Athena workbench surface with a pannable and zoomable renderer viewport. */
export declare class AthenaGraphWorkbenchWidget extends ReactWidget {
    static readonly ID = "athena.graphWorkbench";
    static readonly LABEL = "Graphical View";
    protected readonly editorManager: EditorManager;
    protected readonly repositorySessionService: AthenaRepositorySessionService;
    protected readonly graphAdapterService: AthenaGraphAdapterService;
    protected readonly semanticSelectionService: AthenaSemanticSelectionService;
    protected currentEditorListeners: DisposableCollection;
    protected diagram: Awaited<ReturnType<AthenaGraphAdapterService["requestDiagram"]>>;
    protected errorMessage: string | undefined;
    protected loading: boolean;
    protected switchingView: boolean;
    protected refreshHandle: number | undefined;
    protected viewportElement: HTMLDivElement | undefined;
    protected viewportObserver: ResizeObserver | undefined;
    protected viewportSize: AthenaGraphViewportSize;
    protected viewportTransform: AthenaGraphViewportTransform;
    protected panState: AthenaGraphPanState | undefined;
    protected pendingAutoFit: boolean;
    protected overlayPanelExpanded: boolean;
    protected init(): void;
    protected bindCurrentEditor(widget: EditorWidget | undefined): void;
    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget;
    protected scheduleRefresh(): void;
    protected refreshDiagram(): Promise<void>;
    protected render(): React.ReactNode;
    protected abbreviateViewLabel(displayName: string): string;
    protected viewIconClass(viewId: string): string;
    protected viewAriaLabel(view: {
        displayName: string;
        description: string;
    }): string;
    protected toggleOverlayPanel(): void;
    protected statusIconClass(statusTone: ReturnType<typeof buildAthenaGraphWorkbenchModel>['statusTone']): string;
    protected buildStageStyle(model: ReturnType<typeof buildAthenaGraphWorkbenchModel>): React.CSSProperties;
    protected bindViewportElement: (element: HTMLDivElement | null) => void;
    protected syncViewportSize(): void;
    protected fitViewportToDiagram(): void;
    protected fitViewportToDiagramIfPossible(): void;
    protected resetZoom(): void;
    protected stepZoom(multiplier: number): void;
    protected getViewportCenterPoint(): {
        x: number;
        y: number;
    };
    protected handleViewportClick: (event: React.MouseEvent<HTMLDivElement>) => void;
    protected handleStageClick: (event: React.MouseEvent<HTMLElement>) => void;
    protected handleViewportDoubleClick: (event: React.MouseEvent<HTMLDivElement>) => void;
    protected handleViewportWheel: (event: React.WheelEvent<HTMLDivElement>) => void;
    protected handleViewportPointerDown: (event: React.PointerEvent<HTMLDivElement>) => void;
    protected handleViewportPointerMove: (event: React.PointerEvent<HTMLDivElement>) => void;
    protected handleViewportPointerEnd: (event: React.PointerEvent<HTMLDivElement>) => void;
    protected isInteractiveTarget(target: EventTarget | null): boolean;
    protected handleGraphElementKeyDown(event: React.KeyboardEvent<SVGGElement>, semanticId: string): void;
    protected reconcileTransientSelection(diagram: Awaited<ReturnType<AthenaGraphAdapterService['requestDiagram']>>): void;
    protected switchActiveView(viewId: string): Promise<void>;
}
export {};
//# sourceMappingURL=athena-graph-workbench-widget.d.ts.map