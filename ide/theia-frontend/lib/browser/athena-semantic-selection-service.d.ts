import { Emitter } from '@theia/core';
import { FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import { AthenaActiveSemanticSelection } from './athena-semantic-selection-model';
/** Frontend-only semantic-selection coordinator for cross-surface synchronization in the M7 workbench. */
export declare class AthenaSemanticSelectionService implements FrontendApplicationContribution {
    protected readonly editorManager: EditorManager;
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;
    protected readonly repositorySessionService: AthenaRepositorySessionService;
    protected readonly onDidChangeSelectionEmitter: Emitter<AthenaActiveSemanticSelection>;
    protected currentEditorListeners: DisposableCollection;
    protected selectionValue: AthenaActiveSemanticSelection | undefined;
    protected decoratedEditor: EditorWidget["editor"] | undefined;
    protected decorationIds: string[];
    protected refreshHandle: number | undefined;
    protected activeRepositoryRoot: string | undefined;
    protected suppressEditorSelectionSync: boolean;
    get selection(): AthenaActiveSemanticSelection | undefined;
    get onDidChangeSelection(): import("@theia/core").Event<AthenaActiveSemanticSelection>;
    onStart(_app: FrontendApplication): Promise<void>;
    selectSemanticId(semanticId: string): Promise<AthenaActiveSemanticSelection>;
    clearSelection(): Promise<void>;
    protected bindCurrentEditor(widget: EditorWidget | undefined): void;
    protected scheduleSelectionRefresh(): void;
    protected refreshSelectionFromCurrentEditor(): Promise<void>;
    protected resolveSelection(semanticId: string): Promise<AthenaActiveSemanticSelection | undefined>;
    protected setSelection(selection: AthenaActiveSemanticSelection | undefined, applyToEditor?: boolean): void;
    protected applySelectionToCurrentEditor(selection: AthenaActiveSemanticSelection | undefined): void;
    protected clearDecorations(): void;
    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget;
}
//# sourceMappingURL=athena-semantic-selection-service.d.ts.map