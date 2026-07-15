package com.engineeringood.athena.compiler.semantic

class ProjectSemanticImportResolver {
    fun resolve(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        val packagesByKey = snapshot.packages.associateBy { it.packageKey }
        val resolvedSources = snapshot.sourceUnits.map { sourceUnit ->
            val sourcePackage = packagesByKey.getValue(sourceUnit.packageKey)
            val directDependencyKeys = sourcePackage.directDependencies
            val availablePackageKeys = (listOf(sourceUnit.packageKey) + directDependencyKeys)
                .distinct()
                .sortedBy { it.value }
            val availableSourceUnitIds = snapshot.sourceUnits
                .filter { it.packageKey in availablePackageKeys }
                .map { it.sourceUnitId }
                .sortedBy { it.value }
            sourceUnit.copy(
                resolvedImports = sourceUnit.authoredImports.map { importDeclaration ->
                    val target = importDeclaration.target.parts
                    val packageMatches = snapshot.packages
                        .map { semanticPackage ->
                            PackagePrefixMatch(
                                semanticPackage.packageKey,
                                semanticPackage.packageId.name.split('.'),
                            )
                        }
                        .filter { target.startsWith(it.parts) }
                    val longestPackagePrefixLength = packageMatches.maxOfOrNull { it.parts.size } ?: 0
                    val longestPackageMatches = packageMatches
                        .filter { it.parts.size == longestPackagePrefixLength }
                    val packageGroupKeys = longestPackageMatches.map { it.packageKey }.toSet()
                    val availablePackageGroupKeys = packageGroupKeys.filter { it in availablePackageKeys }.toSet()
                    val namespacePackageKeys = availablePackageGroupKeys.ifEmpty { packageGroupKeys }
                    val prefixCandidates = snapshot.namespaces
                        .filter { it.packageKey in namespacePackageKeys && target.startsWith(it.qualifiedName) }
                    val longestNamespacePrefixLength = prefixCandidates.maxOfOrNull { it.qualifiedName.size } ?: 0
                    val candidates = prefixCandidates
                        .filter { it.qualifiedName.size == longestNamespacePrefixLength }
                        .sortedBy { it.namespaceId.value }
                    val status = when {
                        packageGroupKeys.isNotEmpty() && availablePackageGroupKeys.isEmpty() ->
                            ProjectSemanticImportResolutionStatus.UNAVAILABLE_PACKAGE

                        candidates.size == 1 -> ProjectSemanticImportResolutionStatus.RESOLVED
                        candidates.size > 1 -> ProjectSemanticImportResolutionStatus.AMBIGUOUS_NAMESPACE
                        else -> ProjectSemanticImportResolutionStatus.UNAVAILABLE_NAMESPACE
                    }
                    val selectedNamespaceId = when (status) {
                        ProjectSemanticImportResolutionStatus.RESOLVED -> candidates.single().namespaceId
                        else -> null
                    }
                    val unresolvedPrefixLength = longestNamespacePrefixLength.takeIf { it > 0 }
                        ?: longestPackagePrefixLength
                    ProjectSemanticImportResolution(
                        sourceUnitId = sourceUnit.sourceUnitId,
                        importDeclaration = importDeclaration,
                        status = status,
                        explanation = ProjectSemanticImportExplanation(
                            sourcePackageKey = sourceUnit.packageKey,
                            directDependencyKeys = directDependencyKeys,
                            availablePackageKeys = availablePackageKeys,
                            availableSourceUnitIds = availableSourceUnitIds,
                            candidateNamespaceIds = candidates.map { it.namespaceId },
                            selectedNamespaceId = selectedNamespaceId,
                            unresolvedTargetSuffix = target.drop(unresolvedPrefixLength),
                        ),
                    )
                },
            )
        }
        return ProjectSemanticGraphSnapshot.canonical(
            snapshot.graphId,
            snapshot.rootPackageId,
            snapshot.packages,
            resolvedSources,
            snapshot.namespaces,
            snapshot.declarations,
            snapshot.bindings,
            snapshot.diagnostics,
        )
    }

    private fun List<String>.startsWith(prefix: List<String>): Boolean {
        return size >= prefix.size && subList(0, prefix.size) == prefix
    }
}

private data class PackagePrefixMatch(
    val packageKey: PackageKey,
    val parts: List<String>,
)
