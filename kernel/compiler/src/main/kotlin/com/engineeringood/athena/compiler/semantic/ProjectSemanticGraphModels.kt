package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.repository.PackageIdentifier

data class ProjectSemanticPackage(
    val packageId: PackageIdentifier,
    val packageKey: PackageKey,
    val sourceRoot: String,
    val directDependencies: List<PackageKey>,
)

data class ProjectSemanticSourceUnit(
    val sourceUnitId: SourceUnitId,
    val packageKey: PackageKey,
    val sourceRootRelativePath: String,
    val contentIdentity: SourceUnitContentIdentity,
)

data class ProjectSemanticNamespace(
    val namespaceId: NamespaceId,
    val packageKey: PackageKey,
    val qualifiedName: List<String>,
    val sourceUnitIds: List<SourceUnitId>,
    val declarationIds: List<DeclarationId>,
    val admittedCapabilities: List<String> = emptyList(),
)

data class ProjectSemanticDeclaration(
    val declarationId: DeclarationId,
    val namespaceId: NamespaceId,
    val sourceUnitId: SourceUnitId,
    val kind: String,
    val qualifiedAuthoredName: List<String>,
    val authoredSpan: SourceSpan,
)

data class ProjectSemanticBinding(
    val bindingId: BindingId,
    val sourceUnitId: SourceUnitId,
    val referenceSpan: SourceSpan,
    val resolvedDeclarationId: DeclarationId,
)
