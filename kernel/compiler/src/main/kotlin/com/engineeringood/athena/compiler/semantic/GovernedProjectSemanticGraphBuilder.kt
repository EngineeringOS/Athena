package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity

class GovernedProjectSemanticGraphBuilder(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun build(
        publication: AthenaRepositoryReportPublicationResult,
        sources: List<ProjectSemanticSourceInput>,
    ): ProjectSemanticGraphBuildResult {
        val diagnostics = publication.diagnostics.mapTo(mutableListOf(), ::repositoryDiagnostic)
        val resolvedGraph = publication.graph
        if (!publication.isValid || resolvedGraph == null) {
            diagnostics += diagnostic(
                code = "semantic.repository.publication.invalid",
                severity = ProjectSemanticDiagnosticSeverity.ERROR,
                message = "Project semantic graph construction requires a current valid governed repository publication.",
            )
            return rejected(diagnostics)
        }

        val identityCheckedPackages = runCatching {
            resolvedGraph.packages.map { resolvedPackage ->
                ProjectSemanticPackage(
                    packageId = resolvedPackage.packageId,
                    packageKey = CanonicalSemanticIdentityBuilder.packageKey(resolvedPackage.packageId),
                    sourceRoot = resolvedPackage.sourceRoot,
                    directDependencies = resolvedPackage.directDependencies.map(CanonicalSemanticIdentityBuilder::packageKey),
                )
            }
        }.getOrElse { failure ->
            diagnostics += diagnostic(
                code = "semantic.repository.graph.invalid",
                severity = ProjectSemanticDiagnosticSeverity.ERROR,
                message = "Resolved package graph contains an invalid package or dependency identity: ${failure.message}",
            )
            return rejected(diagnostics)
        }
        val semanticPackages = runCatching {
            identityCheckedPackages.map { semanticPackage ->
                semanticPackage.copy(
                    sourceRoot = CanonicalSemanticIdentityBuilder.normalizePackageSourceRoot(semanticPackage.sourceRoot),
                )
            }
        }.getOrElse { failure ->
            diagnostics += diagnostic(
                code = "semantic.package.source-root.nonportable",
                severity = ProjectSemanticDiagnosticSeverity.ERROR,
                message = "Resolved package graph contains a nonportable package source root: ${failure.message}",
            )
            return rejected(diagnostics)
        }
        val rootPackageId = runCatching { CanonicalSemanticIdentityBuilder.packageKey(resolvedGraph.rootPackage) }
            .getOrElse { failure ->
                diagnostics += diagnostic(
                    code = "semantic.repository.graph.invalid",
                    severity = ProjectSemanticDiagnosticSeverity.ERROR,
                    message = "Resolved package graph has an invalid root package identity: ${failure.message}",
                )
                return rejected(diagnostics)
            }
        val admittedPackagesByKey = semanticPackages.associateBy { it.packageKey }

        val candidates = sources.mapNotNull { source ->
            canonicalCandidate(source, admittedPackagesByKey, diagnostics)
        }
        val admittedCandidates = candidates
            .groupBy { it.sourceUnitId }
            .toSortedMap(compareBy { it.value })
            .map { (_, duplicates) ->
                val ordered = duplicates.sortedBy { it.contentIdentity.contentHash }
                if (ordered.size > 1) {
                    diagnostics += diagnostic(
                        code = "semantic.source.duplicate",
                        severity = ProjectSemanticDiagnosticSeverity.ERROR,
                        message = "Duplicate governed source unit `${ordered.first().sourceRootRelativePath}` was supplied for package `${ordered.first().packageKey.value}`.",
                        sourceUnitId = ordered.first().sourceUnitId,
                    )
                }
                ordered.first()
            }

        val namespaceSources = linkedMapOf<NamespaceKey, MutableList<SourceUnitId>>()
        val semanticSources = admittedCandidates.map { candidate ->
            var declarations = emptyList<com.engineeringood.athena.language.Declaration>()
            val imports = when (val parseResult = parser.parse(candidate.sourceRootRelativePath, candidate.sourceContent)) {
                is ParseFailure -> {
                    diagnostics += parseResult.diagnostics.map { syntaxDiagnostic ->
                        diagnostic(
                            code = "semantic.source.syntax.invalid",
                            severity = ProjectSemanticDiagnosticSeverity.ERROR,
                            message = syntaxDiagnostic.message,
                            sourceUnitId = candidate.sourceUnitId,
                            sourceSpan = syntaxDiagnostic.span,
                        )
                    }
                    emptyList()
                }

                is ParseSuccess -> {
                    val packageDeclaration = parseResult.ast.packageDeclaration
                    declarations = parseResult.ast.declarations
                    when {
                        packageDeclaration == null -> diagnostics += diagnostic(
                            code = "semantic.source.package.missing",
                            severity = ProjectSemanticDiagnosticSeverity.ERROR,
                            message = "Governed source `${candidate.sourceRootRelativePath}` must declare package `${candidate.packageId.name}`.",
                            sourceUnitId = candidate.sourceUnitId,
                            sourceSpan = parseResult.ast.span,
                        )

                        packageDeclaration.name.parts.joinToString(".") != candidate.packageId.name -> diagnostics += diagnostic(
                            code = "semantic.source.package.mismatch",
                            severity = ProjectSemanticDiagnosticSeverity.ERROR,
                            message = "Governed source `${candidate.sourceRootRelativePath}` declares package `${packageDeclaration.name.parts.joinToString(".")}` instead of `${candidate.packageId.name}`.",
                            sourceUnitId = candidate.sourceUnitId,
                            sourceSpan = packageDeclaration.span,
                        )

                        else -> namespaceSources.getOrPut(
                            NamespaceKey(candidate.packageKey, packageDeclaration.name.parts),
                        ) { mutableListOf() } += candidate.sourceUnitId
                    }
                    parseResult.ast.imports
                }
            }
            ProjectSemanticSourceUnit(
                sourceUnitId = candidate.sourceUnitId,
                packageKey = candidate.packageKey,
                sourceRootRelativePath = candidate.sourceRootRelativePath,
                contentIdentity = candidate.contentIdentity,
                authoredImports = imports,
                authoredDeclarations = declarations,
            )
        }
        val namespaces = namespaceSources.map { (key, sourceUnitIds) ->
            ProjectSemanticNamespace(
                namespaceId = CanonicalSemanticIdentityBuilder.namespaceId(key.packageKey, key.qualifiedName),
                packageKey = key.packageKey,
                qualifiedName = key.qualifiedName,
                sourceUnitIds = sourceUnitIds,
                declarationIds = emptyList(),
            )
        }
        val graphId = runCatching {
            CanonicalSemanticIdentityBuilder.graphId(
                rootPackageId,
                semanticPackages.map { semanticPackage ->
                    GraphPackageIdentity(
                        semanticPackage.packageKey,
                        semanticPackage.sourceRoot,
                        semanticPackage.directDependencies,
                    )
                },
                semanticSources.map { it.contentIdentity },
            )
        }.getOrElse { failure ->
            diagnostics += diagnostic(
                code = "semantic.repository.graph.invalid",
                severity = ProjectSemanticDiagnosticSeverity.ERROR,
                message = "Resolved package graph cannot produce a canonical semantic identity: ${failure.message}",
            )
            return rejected(diagnostics)
        }

        val snapshot = runCatching {
            ProjectSemanticGraphSnapshot.canonical(
                graphId,
                rootPackageId,
                semanticPackages,
                semanticSources,
                namespaces,
                declarations = emptyList(),
                bindings = emptyList(),
                diagnostics = diagnostics,
            )
        }.getOrElse { failure ->
            diagnostics += diagnostic(
                code = "semantic.repository.graph.invalid",
                severity = ProjectSemanticDiagnosticSeverity.ERROR,
                message = "Governed repository state cannot form a valid project semantic graph: ${failure.message}",
            )
            return rejected(diagnostics)
        }
        return ProjectSemanticGraphBuildResult(snapshot, snapshot.diagnostics)
    }

    private fun canonicalCandidate(
        source: ProjectSemanticSourceInput,
        admittedPackagesByKey: Map<PackageKey, ProjectSemanticPackage>,
        diagnostics: MutableList<ProjectSemanticDiagnostic>,
    ): CanonicalSourceCandidate? {
        val packageKey = runCatching { CanonicalSemanticIdentityBuilder.packageKey(source.packageId) }
            .getOrElse { failure ->
                diagnostics += diagnostic(
                    code = "semantic.source.package.invalid",
                    severity = ProjectSemanticDiagnosticSeverity.ERROR,
                    message = "Source input has an invalid governed package identity: ${failure.message}",
                )
                return null
            }
        val normalizedPath = runCatching {
            CanonicalSemanticIdentityBuilder.normalizeSourceRootRelativePath(source.sourceRootRelativePath)
        }.getOrElse { failure ->
            diagnostics += diagnostic(
                code = "semantic.source.path.invalid",
                severity = ProjectSemanticDiagnosticSeverity.ERROR,
                message = "Source input path `${source.sourceRootRelativePath}` is not package-relative: ${failure.message}",
            )
            return null
        }
        val admittedPackage = admittedPackagesByKey[packageKey]
        if (admittedPackage == null || admittedPackage.packageId != source.packageId) {
            diagnostics += diagnostic(
                code = "semantic.source.package.not-admitted",
                severity = ProjectSemanticDiagnosticSeverity.ERROR,
                message = "Source input `$normalizedPath` belongs to package `${packageKey.value}` outside the governed resolved graph.",
            )
            return null
        }
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, normalizedPath)
        return CanonicalSourceCandidate(
            source.packageId,
            packageKey,
            normalizedPath,
            source.sourceContent,
            sourceUnitId,
            CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, source.sourceContent),
        )
    }

    private fun repositoryDiagnostic(
        repositoryDiagnostic: com.engineeringood.athena.repository.RepositoryDiagnostic,
    ): ProjectSemanticDiagnostic {
        val severity = when (repositoryDiagnostic.severity) {
            RepositoryDiagnosticSeverity.ERROR -> ProjectSemanticDiagnosticSeverity.ERROR
            RepositoryDiagnosticSeverity.WARNING -> ProjectSemanticDiagnosticSeverity.WARNING
            RepositoryDiagnosticSeverity.INFO -> ProjectSemanticDiagnosticSeverity.INFO
        }
        return diagnostic(repositoryDiagnostic.code, severity, repositoryDiagnostic.message)
    }

    private fun diagnostic(
        code: String,
        severity: ProjectSemanticDiagnosticSeverity,
        message: String,
        sourceUnitId: SourceUnitId? = null,
        sourceSpan: com.engineeringood.athena.language.SourceSpan? = null,
    ): ProjectSemanticDiagnostic {
        return ProjectSemanticDiagnostic(ProjectSemanticDiagnosticCode(code), severity, message, sourceUnitId, sourceSpan)
    }

    private fun rejected(diagnostics: List<ProjectSemanticDiagnostic>): ProjectSemanticGraphBuildResult {
        return ProjectSemanticGraphBuildResult(
            snapshot = null,
            diagnostics = ProjectSemanticGraphSnapshot.canonicalizeDiagnostics(diagnostics),
        )
    }
}

private data class CanonicalSourceCandidate(
    val packageId: com.engineeringood.athena.repository.PackageIdentifier,
    val packageKey: PackageKey,
    val sourceRootRelativePath: String,
    val sourceContent: String,
    val sourceUnitId: SourceUnitId,
    val contentIdentity: SourceUnitContentIdentity,
)

private data class NamespaceKey(
    val packageKey: PackageKey,
    val qualifiedName: List<String>,
)
