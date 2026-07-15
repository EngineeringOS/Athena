package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.SourceSpan
import java.util.Locale

class ProjectSemanticGraphSnapshot private constructor(
    val graphId: GraphId,
    val rootPackageId: PackageKey,
    val packages: List<ProjectSemanticPackage>,
    val sourceUnits: List<ProjectSemanticSourceUnit>,
    val namespaces: List<ProjectSemanticNamespace>,
    val declarations: List<ProjectSemanticDeclaration>,
    val bindings: List<ProjectSemanticBinding>,
    val diagnostics: List<ProjectSemanticDiagnostic>,
) {
    companion object {
        fun canonical(
            graphId: GraphId,
            rootPackageId: PackageKey,
            packages: List<ProjectSemanticPackage>,
            sourceUnits: List<ProjectSemanticSourceUnit>,
            namespaces: List<ProjectSemanticNamespace>,
            declarations: List<ProjectSemanticDeclaration>,
            bindings: List<ProjectSemanticBinding>,
            diagnostics: List<ProjectSemanticDiagnostic>,
        ): ProjectSemanticGraphSnapshot {
            requireUnique(packages.map { it.packageKey }, "package keys")
            requireUnique(sourceUnits.map { it.sourceUnitId }, "source unit ids")
            requireUnique(namespaces.map { it.namespaceId }, "namespace ids")
            requireUnique(declarations.map { it.declarationId }, "declaration ids")
            requireUnique(bindings.map { it.bindingId }, "binding ids")

            val canonicalPackages = packages
                .map { semanticPackage ->
                    semanticPackage.copy(
                        sourceRoot = CanonicalSemanticIdentityBuilder.normalizePackageSourceRoot(semanticPackage.sourceRoot),
                        directDependencies = immutableSortedDistinct(semanticPackage.directDependencies) { it.value },
                    )
                }
                .sortedBy { it.packageKey.value }
                .toImmutableList()
            val packageKeys = canonicalPackages.mapTo(mutableSetOf()) { it.packageKey }
            require(rootPackageId in packageKeys) { "Semantic graph packages must contain the root package id" }
            canonicalPackages.forEach { semanticPackage ->
                require(CanonicalSemanticIdentityBuilder.packageKey(semanticPackage.packageId) == semanticPackage.packageKey) {
                    "Semantic package key must match its governed package identifier"
                }
                require(semanticPackage.directDependencies.all(packageKeys::contains)) {
                    "Semantic package dependencies must target resolved package keys"
                }
            }

            val canonicalSourceUnits = sourceUnits
                .map { sourceUnit ->
                    sourceUnit.copy(
                        sourceRootRelativePath = CanonicalSemanticIdentityBuilder.normalizeSourceRootRelativePath(
                            sourceUnit.sourceRootRelativePath,
                        ),
                        authoredImports = sourceUnit.authoredImports
                            .distinct()
                            .sortedWith(importComparator)
                            .toImmutableList(),
                        resolvedImports = sourceUnit.resolvedImports
                            .map { resolution ->
                                resolution.copy(
                                    explanation = resolution.explanation.copy(
                                        directDependencyKeys = immutableSortedDistinct(
                                            resolution.explanation.directDependencyKeys,
                                        ) { it.value },
                                        availablePackageKeys = immutableSortedDistinct(
                                            resolution.explanation.availablePackageKeys,
                                        ) { it.value },
                                        availableSourceUnitIds = immutableSortedDistinct(
                                            resolution.explanation.availableSourceUnitIds,
                                        ) { it.value },
                                        candidateNamespaceIds = immutableSortedDistinct(
                                            resolution.explanation.candidateNamespaceIds,
                                        ) { it.value },
                                        unresolvedTargetSuffix = resolution.explanation.unresolvedTargetSuffix.toImmutableList(),
                                    ),
                                )
                            }
                            .distinct()
                            .sortedWith(importResolutionComparator)
                            .toImmutableList(),
                    )
                }
                .sortedBy { it.sourceUnitId.value }
                .toImmutableList()
            val sourceUnitsById = canonicalSourceUnits.associateBy { it.sourceUnitId }
            canonicalSourceUnits.forEach { sourceUnit ->
                require(sourceUnit.packageKey in packageKeys) { "Semantic source units must belong to resolved packages" }
                require(sourceUnit.contentIdentity.sourceUnitId == sourceUnit.sourceUnitId) {
                    "Semantic source content identity must belong to its source unit"
                }
                require(
                    CanonicalSemanticIdentityBuilder.sourceUnitId(
                        sourceUnit.packageKey,
                        sourceUnit.sourceRootRelativePath,
                    ) == sourceUnit.sourceUnitId,
                ) { "Semantic source unit id must match its package and normalized path" }
                sourceUnit.authoredImports.forEach { importDeclaration ->
                    requireQualifiedName(importDeclaration.target.parts, "import")
                    requireValidSpan(importDeclaration.target.span, "Semantic import target span")
                    requireValidSpan(importDeclaration.span, "Semantic import declaration span")
                    require(
                        importDeclaration.target.span.start.offset >= importDeclaration.span.start.offset &&
                            importDeclaration.target.span.end.offset <= importDeclaration.span.end.offset,
                    ) { "Semantic import target span must be contained by its declaration span" }
                }
            }
            require(
                CanonicalSemanticIdentityBuilder.graphId(
                    rootPackageId,
                    canonicalPackages.map { semanticPackage ->
                        GraphPackageIdentity(
                            semanticPackage.packageKey,
                            semanticPackage.sourceRoot,
                            semanticPackage.directDependencies,
                        )
                    },
                    canonicalSourceUnits.map { it.contentIdentity },
                ) == graphId,
            ) { "Semantic snapshot graph id must match its canonical package and source content identities" }

            val canonicalNamespaces = namespaces
                .map { namespace ->
                    requireQualifiedName(namespace.qualifiedName, "namespace")
                    require(namespace.admittedCapabilities.all { it.isNotBlank() }) {
                        "Semantic namespace capabilities must not be blank"
                    }
                    namespace.copy(
                        qualifiedName = namespace.qualifiedName.toImmutableList(),
                        sourceUnitIds = immutableSortedDistinct(namespace.sourceUnitIds) { it.value },
                        declarationIds = immutableSortedDistinct(namespace.declarationIds) { it.value },
                        admittedCapabilities = immutableSortedDistinct(namespace.admittedCapabilities) { it },
                    )
                }
                .sortedBy { it.namespaceId.value }
                .toImmutableList()
            val namespacesById = canonicalNamespaces.associateBy { it.namespaceId }
            canonicalNamespaces.forEach { namespace ->
                require(namespace.packageKey in packageKeys) { "Semantic namespaces must belong to resolved packages" }
                require(CanonicalSemanticIdentityBuilder.namespaceId(namespace.packageKey, namespace.qualifiedName) == namespace.namespaceId) {
                    "Semantic namespace id must match its package and qualified name"
                }
                require(namespace.sourceUnitIds.all(sourceUnitsById::containsKey)) {
                    "Semantic namespaces must reference known source units"
                }
                require(namespace.sourceUnitIds.all { sourceUnitsById.getValue(it).packageKey == namespace.packageKey }) {
                    "Semantic namespace source units must belong to its package"
                }
            }

            canonicalSourceUnits.forEach { sourceUnit ->
                val semanticPackage = canonicalPackages.single { it.packageKey == sourceUnit.packageKey }
                val expectedAvailablePackageKeys = (listOf(sourceUnit.packageKey) + semanticPackage.directDependencies)
                    .distinct()
                    .sortedBy { it.value }
                val expectedAvailableSourceUnitIds = canonicalSourceUnits
                    .filter { it.packageKey in expectedAvailablePackageKeys }
                    .map { it.sourceUnitId }
                    .sortedBy { it.value }
                require(
                    sourceUnit.resolvedImports.isEmpty() ||
                        sourceUnit.resolvedImports.size == sourceUnit.authoredImports.size,
                ) {
                    "Semantic source units must publish either zero import resolutions or one resolution per authored import"
                }
                require(sourceUnit.resolvedImports.map { it.importDeclaration }.distinct().size == sourceUnit.resolvedImports.size) {
                    "Semantic source units may publish only one resolution per authored import"
                }
                sourceUnit.resolvedImports.forEach { resolution ->
                    val explanation = resolution.explanation
                    require(resolution.sourceUnitId == sourceUnit.sourceUnitId) {
                        "Semantic import resolutions must belong to their source unit"
                    }
                    require(resolution.importDeclaration in sourceUnit.authoredImports) {
                        "Semantic import resolutions must reference an authored import"
                    }
                    require(explanation.sourcePackageKey == sourceUnit.packageKey) {
                        "Semantic import explanations must identify their source package"
                    }
                    require(explanation.directDependencyKeys == semanticPackage.directDependencies) {
                        "Semantic import explanations must use canonical direct dependencies"
                    }
                    require(explanation.availablePackageKeys == expectedAvailablePackageKeys) {
                        "Semantic import explanations must use source and direct-dependency package availability"
                    }
                    require(explanation.availableSourceUnitIds == expectedAvailableSourceUnitIds) {
                        "Semantic import explanations must list every available source unit"
                    }
                    val candidates = explanation.candidateNamespaceIds.map { namespaceId ->
                        namespacesById[namespaceId]
                            ?: throw IllegalArgumentException("Semantic import explanations must reference known namespaces")
                    }
                    val target = resolution.importDeclaration.target.parts
                    val packageMatches = canonicalPackages
                        .map { semanticPackage ->
                            semanticPackage.packageKey to semanticPackage.packageId.name.split('.')
                        }
                        .filter { (_, packageParts) -> target.startsWith(packageParts) }
                    val longestPackagePrefixLength = packageMatches.maxOfOrNull { (_, packageParts) -> packageParts.size } ?: 0
                    val longestPackageKeys = packageMatches
                        .filter { (_, packageParts) -> packageParts.size == longestPackagePrefixLength }
                        .map { (packageKey, _) -> packageKey }
                        .toSet()
                    val longestPackageHasAvailableKey = longestPackageKeys.any { it in expectedAvailablePackageKeys }
                    require(candidates.all { resolution.importDeclaration.target.parts.startsWith(it.qualifiedName) }) {
                        "Semantic import candidates must prefix the authored target"
                    }
                    require(candidates.map { it.qualifiedName.size }.distinct().size <= 1) {
                        "Semantic import candidates must use the same longest prefix length"
                    }
                    require(longestPackageKeys.isEmpty() || candidates.all { it.packageKey in longestPackageKeys }) {
                        "Semantic import candidates must belong to the longest matching package prefix group"
                    }
                    val namespacePrefixLength = candidates.firstOrNull()?.qualifiedName?.size ?: 0
                    val expectedSuffixPrefixLength = namespacePrefixLength.takeIf { it > 0 }
                        ?: longestPackagePrefixLength
                    val expectedSuffix = resolution.importDeclaration.target.parts.drop(expectedSuffixPrefixLength)
                    require(explanation.unresolvedTargetSuffix == expectedSuffix) {
                        "Semantic import explanation suffix must follow the selected namespace prefix"
                    }
                    when (resolution.status) {
                        ProjectSemanticImportResolutionStatus.RESOLVED -> {
                            require(candidates.all { it.packageKey in expectedAvailablePackageKeys }) {
                                "Resolved import candidates must belong to available packages"
                            }
                            require(candidates.size == 1 && explanation.selectedNamespaceId == candidates.single().namespaceId) {
                                "Resolved imports require one selected namespace"
                            }
                        }

                        ProjectSemanticImportResolutionStatus.AMBIGUOUS_NAMESPACE -> {
                            require(candidates.all { it.packageKey in expectedAvailablePackageKeys }) {
                                "Ambiguous import candidates must belong to available packages"
                            }
                            require(candidates.size > 1 && explanation.selectedNamespaceId == null) {
                                "Ambiguous imports require multiple namespace candidates and no selection"
                            }
                        }

                        ProjectSemanticImportResolutionStatus.UNAVAILABLE_PACKAGE -> {
                            require(longestPackageKeys.isNotEmpty() && !longestPackageHasAvailableKey) {
                                "Unavailable-package imports require a known unavailable longest package prefix"
                            }
                            require(candidates.all { it.packageKey !in expectedAvailablePackageKeys }) {
                                "Unavailable-package candidates must not belong to available packages"
                            }
                            require(explanation.selectedNamespaceId == null) {
                                "Unavailable-package imports must not select a namespace"
                            }
                        }

                        ProjectSemanticImportResolutionStatus.UNAVAILABLE_NAMESPACE -> {
                            require(longestPackageKeys.isEmpty() || longestPackageHasAvailableKey) {
                                "Unavailable-namespace imports must not hide an unavailable package"
                            }
                            require(candidates.isEmpty() && explanation.selectedNamespaceId == null) {
                                "Unavailable-namespace imports must not select namespace candidates"
                            }
                        }
                    }
                }
            }

            val canonicalDeclarations = declarations
                .map { declaration ->
                    val normalizedKind = declaration.kind.trim().lowercase(Locale.ROOT)
                    require(normalizedKind.isNotEmpty()) { "Semantic declaration kind must not be blank" }
                    requireQualifiedName(declaration.qualifiedAuthoredName, "declaration")
                    declaration.copy(
                        kind = normalizedKind,
                        qualifiedAuthoredName = declaration.qualifiedAuthoredName.toImmutableList(),
                    )
                }
                .sortedBy { it.declarationId.value }
                .toImmutableList()
            val declarationsById = canonicalDeclarations.associateBy { it.declarationId }
            canonicalDeclarations.forEach { declaration ->
                requireValidSpan(declaration.authoredSpan, "Semantic declaration authored span")
                val namespace = namespacesById[declaration.namespaceId]
                    ?: throw IllegalArgumentException("Semantic declarations must reference known namespaces")
                val sourceUnit = sourceUnitsById[declaration.sourceUnitId]
                    ?: throw IllegalArgumentException("Semantic declarations must reference known source units")
                require(sourceUnit.packageKey == namespace.packageKey) {
                    "Semantic declarations must use source units from their namespace package"
                }
                require(declaration.sourceUnitId in namespace.sourceUnitIds) {
                    "Semantic declarations must use source units indexed by their namespace"
                }
                require(
                    CanonicalSemanticIdentityBuilder.declarationId(
                        declaration.sourceUnitId,
                        declaration.kind,
                        declaration.qualifiedAuthoredName,
                    ) == declaration.declarationId,
                ) { "Semantic declaration id must match its authored declaration components" }
                require(declaration.declarationId in namespace.declarationIds) {
                    "Semantic declarations must be indexed by their namespace"
                }
            }
            canonicalNamespaces.forEach { namespace ->
                require(namespace.declarationIds.all(declarationsById::containsKey)) {
                    "Semantic namespaces must reference known declarations"
                }
                require(namespace.declarationIds.all { declarationsById.getValue(it).namespaceId == namespace.namespaceId }) {
                    "Semantic namespace declarations must belong to that namespace"
                }
            }

            val canonicalBindings = bindings
                .sortedBy { it.bindingId.value }
                .toImmutableList()
            canonicalBindings.forEach { binding ->
                requireValidSpan(binding.referenceSpan, "Semantic binding reference span")
                require(binding.sourceUnitId in sourceUnitsById) { "Semantic bindings must reference known source units" }
                require(binding.resolvedDeclarationId in declarationsById) {
                    "Semantic bindings must resolve to known declarations"
                }
                require(
                    CanonicalSemanticIdentityBuilder.bindingId(
                        binding.sourceUnitId,
                        binding.referenceSpan,
                        binding.resolvedDeclarationId,
                    ) == binding.bindingId,
                ) { "Semantic binding id must match its reference and resolution components" }
            }

            val canonicalDiagnostics = canonicalizeDiagnostics(diagnostics)
            canonicalDiagnostics.forEach { diagnostic ->
                require(diagnostic.sourceUnitId == null || diagnostic.sourceUnitId in sourceUnitsById) {
                    "Semantic diagnostics must reference known source units"
                }
                require(diagnostic.relatedLocations.all { it.sourceUnitId in sourceUnitsById }) {
                    "Semantic diagnostic related locations must reference known source units"
                }
            }

            return ProjectSemanticGraphSnapshot(
                graphId,
                rootPackageId,
                canonicalPackages,
                canonicalSourceUnits,
                canonicalNamespaces,
                canonicalDeclarations,
                canonicalBindings,
                canonicalDiagnostics,
            )
        }

        internal fun canonicalizeDiagnostics(
            diagnostics: List<ProjectSemanticDiagnostic>,
        ): List<ProjectSemanticDiagnostic> {
            val canonical = diagnostics
                .map { diagnostic ->
                    diagnostic.copy(
                        relatedLocations = diagnostic.relatedLocations
                            .map { location -> location.copy(message = location.message?.takeIf { it.isNotBlank() }) }
                            .distinct()
                            .sortedWith(relatedLocationComparator)
                            .toImmutableList(),
                    )
                }
                .distinct()
                .sortedWith(diagnosticComparator)
                .toImmutableList()
            canonical.forEach { diagnostic ->
                diagnostic.sourceSpan?.let { requireValidSpan(it, "Semantic diagnostic source span") }
                diagnostic.relatedLocations.forEach { location ->
                    requireValidSpan(location.sourceSpan, "Semantic diagnostic related-location span")
                }
            }
            return canonical
        }

        private val relatedLocationComparator = compareBy<ProjectSemanticRelatedLocation>(
            { it.sourceUnitId.value },
            { it.sourceSpan.start.offset },
            { it.sourceSpan.start.line },
            { it.sourceSpan.start.column },
            { it.sourceSpan.end.offset },
            { it.sourceSpan.end.line },
            { it.sourceSpan.end.column },
            { it.message.orEmpty() },
        )

        private val importComparator = compareBy<com.engineeringood.athena.language.ImportDeclaration>(
            { it.target.parts.joinToString(".") },
            { it.target.span.start.offset },
            { it.target.span.start.line },
            { it.target.span.start.column },
            { it.target.span.end.offset },
            { it.target.span.end.line },
            { it.target.span.end.column },
            { it.span.start.offset },
            { it.span.start.line },
            { it.span.start.column },
            { it.span.end.offset },
            { it.span.end.line },
            { it.span.end.column },
        )

        private val importResolutionComparator = compareBy<ProjectSemanticImportResolution>(
            { it.sourceUnitId.value },
            { it.importDeclaration.target.parts.joinToString(".") },
            { it.importDeclaration.target.span.start.offset },
            { it.importDeclaration.target.span.start.line },
            { it.importDeclaration.target.span.start.column },
            { it.importDeclaration.target.span.end.offset },
            { it.importDeclaration.target.span.end.line },
            { it.importDeclaration.target.span.end.column },
            { it.importDeclaration.span.start.offset },
            { it.importDeclaration.span.start.line },
            { it.importDeclaration.span.start.column },
            { it.importDeclaration.span.end.offset },
            { it.importDeclaration.span.end.line },
            { it.importDeclaration.span.end.column },
            { it.status.ordinal },
        )

        private val diagnosticComparator = Comparator<ProjectSemanticDiagnostic> { left, right ->
            compareValuesBy(
                left,
                right,
                { it.code.value },
                { it.severity.ordinal },
                { it.sourceUnitId?.value.orEmpty() },
                { it.sourceSpan?.start?.offset ?: -1 },
                { it.sourceSpan?.start?.line ?: -1 },
                { it.sourceSpan?.start?.column ?: -1 },
                { it.sourceSpan?.end?.offset ?: -1 },
                { it.sourceSpan?.end?.line ?: -1 },
                { it.sourceSpan?.end?.column ?: -1 },
                { it.message },
            ).takeIf { it != 0 }
                ?: compareRelatedLocations(left.relatedLocations, right.relatedLocations)
        }

        private fun compareRelatedLocations(
            left: List<ProjectSemanticRelatedLocation>,
            right: List<ProjectSemanticRelatedLocation>,
        ): Int {
            repeat(minOf(left.size, right.size)) { index ->
                val result = relatedLocationComparator.compare(left[index], right[index])
                if (result != 0) {
                    return result
                }
            }
            return left.size.compareTo(right.size)
        }

        private fun requireQualifiedName(parts: List<String>, subject: String) {
            require(parts.isNotEmpty() && parts.all { it.isNotBlank() && '.' !in it }) {
                "Semantic $subject qualified names must contain nonblank undotted parts"
            }
        }

        private fun requireValidSpan(span: SourceSpan, subject: String) {
            require(span.start.offset >= 0 && span.start.line >= 1 && span.start.column >= 1) {
                "$subject start position must be nonnegative and one-based"
            }
            require(span.end.offset >= span.start.offset && span.end.line >= 1 && span.end.column >= 1) {
                "$subject end position must follow its start"
            }
            require(
                span.end.line > span.start.line ||
                    (span.end.line == span.start.line && span.end.column >= span.start.column),
            ) { "$subject line and column must be ordered" }
            if (span.end.offset == span.start.offset) {
                require(span.end.line == span.start.line && span.end.column == span.start.column) {
                    "$subject zero-length offsets must use the same source position"
                }
            }
        }

        private fun <T> requireUnique(values: List<T>, subject: String) {
            require(values.distinct().size == values.size) { "Semantic graph $subject must be unique" }
        }

        private fun <T> immutableSortedDistinct(values: List<T>, selector: (T) -> String): List<T> {
            return values.distinct().sortedBy(selector).toImmutableList()
        }

        private fun <T> List<T>.toImmutableList(): List<T> = java.util.List.copyOf(this)

        private fun List<String>.startsWith(prefix: List<String>): Boolean {
            return size >= prefix.size && subList(0, prefix.size) == prefix
        }
    }
}
