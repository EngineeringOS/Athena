package com.engineeringood.athena.apps.composeviewer

import com.engineeringood.athena.composeruntime.AthenaComposeShellDescriptor
import com.engineeringood.athena.composeruntime.AthenaComposeShellState
import com.engineeringood.athena.composeruntime.AthenaComposeInspectorField
import com.engineeringood.athena.composeruntime.AthenaComposeInspectorGroup
import com.engineeringood.athena.composeruntime.AthenaComposeSourceDocument
import com.engineeringood.athena.composeruntime.AthenaComposeWorkspaceTreeItem
import com.engineeringood.athena.composeruntime.AthenaSemanticViewerScene

/**
 * Runtime-owned snapshot consumed by the desktop Compose viewer shell.
 */
data class AthenaComposeViewerProjectSnapshot(
    val projectName: String,
    val descriptor: AthenaComposeShellDescriptor,
    val scene: AthenaSemanticViewerScene?,
    val sourcePath: String,
    val sourceText: String,
)

/**
 * Derives a deterministic workbench shell state from the runtime-owned viewer snapshot.
 */
fun AthenaComposeViewerProjectSnapshot.toShellState(): AthenaComposeShellState {
    val relativeSourcePath = sourcePath.replace('\\', '/')
    val sourceDocument = AthenaComposeSourceDocument.fromText(
        path = relativeSourcePath,
        text = sourceText,
    )
    return AthenaComposeShellState(
        descriptor = descriptor,
        workspaceName = projectName,
        projectName = scene?.systemName ?: projectName,
        workspaceTreeItems = buildList {
            add(AthenaComposeWorkspaceTreeItem(label = "Workspace", depth = 0))
            add(
                AthenaComposeWorkspaceTreeItem(
                    label = projectName,
                    depth = 1,
                    meta = "project",
                    isSelected = true,
                ),
            )
            add(
                AthenaComposeWorkspaceTreeItem(
                    label = relativeSourcePath.substringAfterLast('/'),
                    depth = 1,
                    meta = "source",
                ),
            )
            add(
                AthenaComposeWorkspaceTreeItem(
                    label = scene?.systemName ?: "No active system",
                    depth = 1,
                    meta = "render",
                ),
            )
            scene?.components?.forEach { component ->
                add(
                    AthenaComposeWorkspaceTreeItem(
                        label = component.label,
                        depth = 2,
                        meta = component.semanticId.substringBefore(':'),
                    ),
                )
            }
        },
        sourceDocument = sourceDocument,
        inspectorGroups = listOf(
            AthenaComposeInspectorGroup(
                title = "Project",
                fields = listOf(
                    AthenaComposeInspectorField("System", scene?.systemName ?: "Unavailable"),
                    AthenaComposeInspectorField("Project key", projectName),
                    AthenaComposeInspectorField("Source", relativeSourcePath),
                ),
            ),
            AthenaComposeInspectorGroup(
                title = "Runtime view",
                fields = listOf(
                    AthenaComposeInspectorField("Components", scene?.componentCount?.toString() ?: "0"),
                    AthenaComposeInspectorField("Connections", scene?.connectionCount?.toString() ?: "0"),
                    AthenaComposeInspectorField("Selection", "No semantic selection yet."),
                ),
            ),
        ),
        diagnosticsEntries = listOf(
            "No active diagnostics.",
            "Viewer shell remains non-sovereign; canonical semantics stay runtime-owned.",
        ),
        consoleEntries = listOf(
            "Runtime project activated: $projectName",
            "Semantic viewer projection resolved for ${scene?.systemName ?: "unavailable scene"}.",
            "Workbench shell ready with source and render surfaces.",
        ),
        scene = scene,
    )
}
