package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.repository.PackageIdentifier

data class ProjectSemanticSourceInput(
    val packageId: PackageIdentifier,
    val sourceRootRelativePath: String,
    val sourceContent: String,
)

data class ProjectSemanticGraphBuildResult(
    val snapshot: ProjectSemanticGraphSnapshot?,
    val diagnostics: List<ProjectSemanticDiagnostic>,
)
