package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.ImportDeclaration

enum class ProjectSemanticImportResolutionStatus {
    RESOLVED,
    UNAVAILABLE_PACKAGE,
    UNAVAILABLE_NAMESPACE,
    AMBIGUOUS_NAMESPACE,
}

data class ProjectSemanticImportExplanation(
    val sourcePackageKey: PackageKey,
    val directDependencyKeys: List<PackageKey>,
    val availablePackageKeys: List<PackageKey>,
    val availableSourceUnitIds: List<SourceUnitId>,
    val candidateNamespaceIds: List<NamespaceId>,
    val selectedNamespaceId: NamespaceId?,
    val unresolvedTargetSuffix: List<String>,
)

data class ProjectSemanticImportResolution(
    val sourceUnitId: SourceUnitId,
    val importDeclaration: ImportDeclaration,
    val status: ProjectSemanticImportResolutionStatus,
    val explanation: ProjectSemanticImportExplanation,
)
