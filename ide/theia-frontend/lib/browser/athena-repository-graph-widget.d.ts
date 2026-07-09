import * as React from '@theia/core/shared/react';
import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import { EditorManager, EditorWidget } from '@theia/editor/lib/browser';
import { AthenaLspEditorBridgeService, AthenaRepositoryDiagnosticPayload, AthenaRepositoryGraphSessionPayload, AthenaRepositoryManifestDependencyPayload, AthenaRepositoryResolvedPackagePayload } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
export declare class AthenaRepositoryGraphWidget extends ReactWidget {
    static readonly ID = "athena.repositoryGraph";
    static readonly LABEL = "Repository Graph";
    protected readonly editorManager: EditorManager;
    protected readonly repositorySessionService: AthenaRepositorySessionService;
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;
    protected currentEditorListeners: DisposableCollection;
    protected graphSession: AthenaRepositoryGraphSessionPayload | undefined;
    protected errorMessage: string | undefined;
    protected loading: boolean;
    protected refreshHandle: number | undefined;
    protected init(): void;
    protected bindCurrentEditor(widget: EditorWidget | undefined): void;
    protected isAthenaEditor(widget: EditorWidget | undefined): widget is EditorWidget;
    protected scheduleRefresh(): void;
    protected refreshGraphSession(): Promise<void>;
    protected render(): React.ReactNode;
    protected renderDependency(dependency: AthenaRepositoryManifestDependencyPayload): React.ReactNode;
    protected renderResolvedPackage(resolvedPackage: AthenaRepositoryResolvedPackagePayload): React.ReactNode;
    protected renderDiagnostic(diagnostic: AthenaRepositoryDiagnosticPayload): React.ReactNode;
}
//# sourceMappingURL=athena-repository-graph-widget.d.ts.map