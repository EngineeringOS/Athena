package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.WorkspaceFolder
import java.nio.file.Path

internal fun workspaceInitializeParams(repositoryRoot: Path): InitializeParams = InitializeParams().apply {
    workspaceFolders = listOf(
        WorkspaceFolder(
            repositoryRoot.toUri().toString(),
            repositoryRoot.fileName?.toString() ?: "Athena",
        ),
    )
}
