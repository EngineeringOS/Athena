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

            val canonicalDiagnostics = diagnostics
                .map { diagnostic ->
                    diagnostic.copy(
                        relatedLocations = diagnostic.relatedLocations
                            .map { location -> location.copy(message = location.message?.takeIf { it.isNotBlank() }) }
                            .distinct()
                            .sortedWith(relatedLocationComparator)
                            .toImmutableList(),
                    )
                }
                .sortedWith(diagnosticComparator)
                .toImmutableList()
            canonicalDiagnostics.forEach { diagnostic ->
                diagnostic.sourceSpan?.let { requireValidSpan(it, "Semantic diagnostic source span") }
                require(diagnostic.sourceUnitId == null || diagnostic.sourceUnitId in sourceUnitsById) {
                    "Semantic diagnostics must reference known source units"
                }
                require(diagnostic.relatedLocations.all { it.sourceUnitId in sourceUnitsById }) {
                    "Semantic diagnostic related locations must reference known source units"
                }
                diagnostic.relatedLocations.forEach { location ->
                    requireValidSpan(location.sourceSpan, "Semantic diagnostic related-location span")
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
    }
}
