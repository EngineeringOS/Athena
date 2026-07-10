import { Emitter } from '@theia/core';
import { FrontendApplication, FrontendApplicationContribution } from '@theia/core/lib/browser';
import { Disposable, DisposableCollection } from '@theia/core/lib/common/disposable';
import { inject, injectable } from '@theia/core/shared/inversify';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import {
    AthenaActiveSemanticSelection,
    resolveSemanticSelectionFromInspection
} from './athena-semantic-selection-model';

/** Frontend-only semantic-selection coordinator for cross-surface synchronization in the M7 workbench. */
@injectable()
export class AthenaSemanticSelectionService implements FrontendApplicationContribution {
    @inject(EditorManager)
    protected readonly editorManager: EditorManager;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    protected readonly onDidChangeSelectionEmitter = new Emitter<AthenaActiveSemanticSelection | undefined>();
    protected currentEditorListeners = new DisposableCollection();
    protected selectionValue: AthenaActiveSemanticSelection | undefined;
    protected decoratedEditor = undefined as EditorWidget['editor'] | undefined;
    protected decorationIds: string[] = [];
    protected refreshHandle: number | undefined;
    protected activeRepositoryRoot: string | undefined;

    get selection(): AthenaActiveSemanticSelection | undefined {
        return this.selectionValue;
    }

    get onDidChangeSelection() {
        return this.onDidChangeSelectionEmitter.event;
    }

    async onStart(_app: FrontendApplication): Promise<void> {
        this.activeRepositoryRoot = this.repositorySessionService.state.repositoryRoot;
        this.bindCurrentEditor(this.editorManager.currentEditor);
        this.repositorySessionService.onDidChangeState(state => {
            const repositoryRootChanged = state.repositoryRoot !== this.activeRepositoryRoot;
            this.activeRepositoryRoot = state.repositoryRoot;
            if (state.lifecycle !== 'ready' || repositoryRootChanged) {
                void this.clearSelection();
                return;
            }
            this.scheduleSelectionRefresh();
        });
        this.editorManager.onCurrentEditorChanged(widget => {
            this.bindCurrentEditor(widget);
            this.scheduleSelectionRefresh();
        });
    }

    async selectSemanticId(semanticId: string): Promise<AthenaActiveSemanticSelection> {
        const resolved = await this.resolveSelection(semanticId);
        const nextSelection = resolved ?? { semanticId };
        this.setSelection(nextSelection);
        return nextSelection;
    }

    async clearSelection(): Promise<void> {
        this.setSelection(undefined);
    }

    protected bindCurrentEditor(widget: EditorWidget | undefined): void {
        this.currentEditorListeners.dispose();
        this.currentEditorListeners = new DisposableCollection();

        if (!this.isAthenaEditor(widget)) {
            this.clearDecorations();
            return;
        }

        this.currentEditorListeners.push(widget.editor.onDocumentContentChanged(() => this.scheduleSelectionRefresh()));
        this.currentEditorListeners.push(Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
                this.refreshHandle = undefined;
            }
        }));
    }

    protected scheduleSelectionRefresh(): void {
        if (!this.selectionValue) {
            this.clearDecorations();
            return;
        }
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshSelectionFromCurrentEditor();
        }, 120);
    }

    protected async refreshSelectionFromCurrentEditor(): Promise<void> {
        const currentSelection = this.selectionValue;
        if (!currentSelection) {
            this.clearDecorations();
            return;
        }

        const resolved = await this.resolveSelection(currentSelection.semanticId);
        if (resolved) {
            this.selectionValue = resolved;
            this.onDidChangeSelectionEmitter.fire(resolved);
        }

        this.applySelectionToCurrentEditor(resolved ?? currentSelection);
    }

    protected async resolveSelection(semanticId: string): Promise<AthenaActiveSemanticSelection | undefined> {
        const currentEditor = this.editorManager.currentEditor;
        if (!this.isAthenaEditor(currentEditor)) {
            return undefined;
        }

        const inspection = await this.lspEditorBridgeService.requestSemanticInspection(currentEditor);
        return resolveSemanticSelectionFromInspection(inspection, semanticId);
    }

    protected setSelection(selection: AthenaActiveSemanticSelection | undefined): void {
        this.selectionValue = selection;
        this.onDidChangeSelectionEmitter.fire(selection);
        this.applySelectionToCurrentEditor(selection);
    }

    protected applySelectionToCurrentEditor(selection: AthenaActiveSemanticSelection | undefined): void {
        const currentEditor = this.editorManager.currentEditor;
        if (!selection || !this.isAthenaEditor(currentEditor) || selection.sourceUri !== currentEditor.editor.uri.toString() || !selection.sourceRange) {
            this.clearDecorations();
            return;
        }

        const editor = currentEditor.editor;
        editor.selection = {
            ...selection.sourceRange,
            direction: 'ltr'
        };
        editor.revealRange(selection.sourceRange, { at: 'center' });
        this.decoratedEditor = editor;
        this.decorationIds = editor.deltaDecorations({
            oldDecorations: this.decorationIds,
            newDecorations: [{
                range: selection.sourceRange,
                options: {
                    className: 'athena-source-selection-decoration'
                }
            }]
        });
    }

    protected clearDecorations(): void {
        if (this.decoratedEditor) {
            this.decoratedEditor.deltaDecorations({
                oldDecorations: this.decorationIds,
                newDecorations: []
            });
        }
        this.decoratedEditor = undefined;
        this.decorationIds = [];
    }

    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget {
        return !!widget && widget.editor.uri.toString().toLowerCase().endsWith('.athena');
    }
}
