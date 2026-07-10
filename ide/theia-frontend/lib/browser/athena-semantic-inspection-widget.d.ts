import * as React from '@theia/core/shared/react';
import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { AthenaLspEditorBridgeService, AthenaSemanticInspectionPayload } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaSemanticSelectionService } from './athena-semantic-selection-service';
export declare class AthenaSemanticInspectionWidget extends ReactWidget {
    static readonly ID = "athena.semanticInspection";
    static readonly LABEL = "Semantic Inspection";
    protected readonly editorManager: EditorManager;
    protected readonly repositorySessionService: AthenaRepositorySessionService;
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;
    protected readonly semanticSelectionService: AthenaSemanticSelectionService;
    protected currentEditorListeners: DisposableCollection;
    protected inspection: AthenaSemanticInspectionPayload | undefined;
    protected errorMessage: string | undefined;
    protected loading: boolean;
    protected refreshHandle: number | undefined;
    protected init(): void;
    protected bindCurrentEditor(widget: EditorWidget | undefined): void;
    protected scheduleRefresh(): void;
    protected refreshInspection(): Promise<void>;
    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget;
    protected render(): React.ReactNode;
    protected isSelected(semanticId: string): boolean;
}
//# sourceMappingURL=athena-semantic-inspection-widget.d.ts.map