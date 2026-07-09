package com.engineeringood.athena.scm

import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryLock
import com.engineeringood.athena.repository.ResolvedPackageGraph
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticValidationResult

/**
 * Deterministic publish-oriented semantic history summarizer derived from a baseline sequence.
 *
 * The generator stays package-centered and transport-light. It reuses typed semantic diff facts
 * instead of rebuilding package evolution from vendor-native history mechanics or changelog text.
 */
class SemanticHistorySummaryGenerator {
    /** Produces one deterministic package-aware semantic history summary for [request]. */
    fun summarize(
        request: SemanticHistoryRequest,
        comparisons: List<SemanticHistoryComparison>,
    ): SemanticHistorySummary {
        val normalizedComparisons = comparisons
            .distinct()
            .sortedWith(historyComparisonComparator())

        val packageLineage = normalizedComparisons
            .map { comparison -> comparison.packageVersionMeaning(request.packageId) }
            .filter { versionMeaning -> versionMeaning.baselineVersion != null || versionMeaning.currentVersion != null }
            .distinct()
            .sortedWith(packageVersionMeaningComparator())

        val entries = normalizedComparisons
            .flatMap { comparison -> comparison.historyEntries(request.packageId) }
            .distinct()
            .sortedWith(historyEntryComparator())

        val validationMovements = entries
            .mapNotNull { entry -> entry.validationMovement }
            .distinct()
            .sortedWith(validationMovementComparator())

        return SemanticHistorySummary(
            packageId = request.packageId,
            baselineSequence = normalizedComparisons.map { comparison -> comparison.baseline.descriptor },
            packageLineage = packageLineage,
            validationMovements = validationMovements,
            entries = entries,
            releaseRelevance = entries.maxByOrNull { entry -> entry.releaseRelevance.ordinal }?.releaseRelevance
                ?: SemanticReleaseRelevance.NONE,
            contractBreakRisk = entries.maxByOrNull { entry -> entry.contractBreakRisk.ordinal }?.contractBreakRisk
                ?: SemanticContractBreakRisk.NONE,
            summary = "Publish-oriented semantic history for ${historyPackageDisplayName(request.packageId)} across ${normalizedComparisons.size} baseline(s).",
        )
    }
}

private fun SemanticHistoryComparison.historyEntries(
    packageId: PackageIdentifier,
): List<SemanticHistoryEntry> {
    val versionMeaning = packageVersionMeaning(packageId)
    val relevantAuthoredChanges = relevantAuthoredChanges(packageId)
    val relevantDerivedConsequences = relevantDerivedConsequences(packageId)
    val dependencyMovements = dependencyMovements(packageId, relevantAuthoredChanges)
    val validationDerivedConsequences = relevantDerivedConsequences.filter { consequence ->
        consequence.type == SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED ||
            consequence.type == SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE ||
            consequence.type == SemanticDerivedConsequenceType.VALIDATION_RECHECK_REQUIRED
    }
    val validationMovement = validationMovement(validationDerivedConsequences)
    val churnDerivedConsequences = relevantDerivedConsequences - validationDerivedConsequences.toSet()

    val authoredEntries = relevantAuthoredChanges
        .groupBy { change -> change.category }
        .toSortedMap(compareBy { category -> category.ordinal })
        .map { (category, authoredGroup) ->
            SemanticHistoryEntry(
                kind = SemanticHistoryEntryKind.AUTHORED_EVOLUTION,
                baseline = baseline.descriptor,
                packageVersion = versionMeaning,
                changeCategory = category,
                releaseRelevance = historyReleaseRelevance(
                    authoredChanges = authoredGroup,
                    dependencyMovements = if (category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED) {
                        dependencyMovements
                    } else {
                        emptyList()
                    },
                    validationMovement = null,
                    derivedConsequences = emptyList(),
                ),
                contractBreakRisk = historyContractBreakRisk(
                    authoredChanges = authoredGroup,
                    dependencyMovements = if (category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED) {
                        dependencyMovements
                    } else {
                        emptyList()
                    },
                    validationMovement = null,
                    derivedConsequences = emptyList(),
                ),
                message = authoredHistoryMessage(
                    baselineLabel = baseline.descriptor.label,
                    category = category,
                    authoredChanges = authoredGroup,
                    dependencyMovements = if (category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED) {
                        dependencyMovements
                    } else {
                        emptyList()
                    },
                ),
                dependencyMovements = if (category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED) {
                    dependencyMovements
                } else {
                    emptyList()
                },
                authoredChanges = authoredGroup.sortedWith(historyChangeComparator()),
            )
        }

    val validationEntry = if (validationMovement != null || validationDerivedConsequences.isNotEmpty()) {
        listOf(
            SemanticHistoryEntry(
                kind = SemanticHistoryEntryKind.VALIDATION_MOVEMENT,
                baseline = baseline.descriptor,
                packageVersion = versionMeaning,
                changeCategory = SemanticChangeCategory.VALIDATION_STATE_CHANGED,
                releaseRelevance = historyReleaseRelevance(
                    authoredChanges = emptyList(),
                    dependencyMovements = emptyList(),
                    validationMovement = validationMovement,
                    derivedConsequences = validationDerivedConsequences,
                ),
                contractBreakRisk = historyContractBreakRisk(
                    authoredChanges = emptyList(),
                    dependencyMovements = emptyList(),
                    validationMovement = validationMovement,
                    derivedConsequences = validationDerivedConsequences,
                ),
                message = validationMovement?.message ?: validationDerivedConsequences
                    .joinToString(
                        prefix = "Validation movement from baseline ${baseline.descriptor.label}: ",
                        separator = " ",
                    ) { consequence -> consequence.message },
                validationMovement = validationMovement,
                derivedConsequences = validationDerivedConsequences.sortedWith(historyDerivedConsequenceComparator()),
            ),
        )
    } else {
        emptyList()
    }

    val derivedChurnEntry = if (churnDerivedConsequences.isNotEmpty()) {
        listOf(
            SemanticHistoryEntry(
                kind = SemanticHistoryEntryKind.DERIVED_CHURN,
                baseline = baseline.descriptor,
                packageVersion = versionMeaning,
                changeCategory = null,
                releaseRelevance = historyReleaseRelevance(
                    authoredChanges = emptyList(),
                    dependencyMovements = emptyList(),
                    validationMovement = null,
                    derivedConsequences = churnDerivedConsequences,
                ),
                contractBreakRisk = historyContractBreakRisk(
                    authoredChanges = emptyList(),
                    dependencyMovements = emptyList(),
                    validationMovement = null,
                    derivedConsequences = churnDerivedConsequences,
                ),
                message = "Derived churn from baseline ${baseline.descriptor.label}: " +
                    churnDerivedConsequences.joinToString(separator = " ") { consequence -> consequence.message },
                derivedConsequences = churnDerivedConsequences.sortedWith(historyDerivedConsequenceComparator()),
            ),
        )
    } else {
        emptyList()
    }

    return (authoredEntries + validationEntry + derivedChurnEntry)
        .filter { entry ->
            entry.authoredChanges.isNotEmpty() ||
                entry.derivedConsequences.isNotEmpty() ||
                entry.dependencyMovements.isNotEmpty() ||
                entry.validationMovement != null
        }
}

private fun SemanticHistoryComparison.packageVersionMeaning(
    packageId: PackageIdentifier,
): SemanticPackageVersionMeaning {
    val baselineVersion = baseline.repositoryReport.packageVersion(packageId)
    val currentVersion = diff.snapshot.repositoryReport.packageVersion(packageId)
    return SemanticPackageVersionMeaning(
        packageId = packageId.copy(version = currentVersion ?: packageId.version),
        baselineVersion = baselineVersion,
        currentVersion = currentVersion,
        changeKind = when {
            baselineVersion == null && currentVersion == null -> SemanticPackageVersionChangeKind.VERSION_UNSPECIFIED
            baselineVersion == null && currentVersion != null -> SemanticPackageVersionChangeKind.INITIAL_VERSION_DECLARED
            baselineVersion != null && currentVersion == null -> SemanticPackageVersionChangeKind.VERSION_REMOVED
            baselineVersion == currentVersion -> SemanticPackageVersionChangeKind.VERSION_UNCHANGED
            else -> SemanticPackageVersionChangeKind.VERSION_UPDATED
        },
    )
}

private fun SemanticHistoryComparison.relevantAuthoredChanges(
    packageId: PackageIdentifier,
): List<SemanticChangeRecord> {
    val currentPrimaryPackage = diff.snapshot.repositoryReport.repository.manifest.primaryPackage.id
    return diff.authoredChanges
        .filter { change ->
            when {
                samePackageLineage(change.affectedPackage, packageId) -> true
                samePackageLineage(currentPrimaryPackage, packageId) &&
                    change.category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED -> true
                else -> false
            }
        }
        .distinct()
        .sortedWith(historyChangeComparator())
}

private fun SemanticHistoryComparison.relevantDerivedConsequences(
    packageId: PackageIdentifier,
): List<SemanticDerivedConsequence> {
    return diff.derivedConsequences
        .filter { consequence -> samePackageLineage(consequence.affectedPackage, packageId) }
        .distinct()
        .sortedWith(historyDerivedConsequenceComparator())
}

private fun dependencyMovements(
    packageId: PackageIdentifier,
    authoredChanges: List<SemanticChangeRecord>,
): List<SemanticDependencyMovement> {
    if (authoredChanges.none { change -> change.category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED }) {
        return emptyList()
    }

    val dependencyChanges = authoredChanges
        .filter { change -> change.category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED }
        .groupBy { change -> change.affectedPackage?.name.orEmpty() }

    return dependencyChanges.flatMap { (_, changesForName) ->
        val addedChanges = changesForName.filter { change -> change.message.contains("added:", ignoreCase = true) }
        val removedChanges = changesForName.filter { change -> change.message.contains("removed:", ignoreCase = true) }

        when {
            addedChanges.isNotEmpty() && removedChanges.isNotEmpty() -> listOf(
                SemanticDependencyMovement(
                    packageId = addedChanges.last().affectedPackage ?: removedChanges.last().affectedPackage ?: packageId,
                    kind = if (
                        removedChanges.last().metadata["source"] == addedChanges.last().metadata["source"] &&
                        removedChanges.last().metadata["locator"] == addedChanges.last().metadata["locator"]
                    ) {
                        SemanticDependencyMovementKind.VERSION_CHANGED
                    } else {
                        SemanticDependencyMovementKind.RETARGETED
                    },
                    baselineVersion = removedChanges.last().affectedPackage?.version,
                    currentVersion = addedChanges.last().affectedPackage?.version,
                    message = if (
                        removedChanges.last().metadata["source"] == addedChanges.last().metadata["source"] &&
                        removedChanges.last().metadata["locator"] == addedChanges.last().metadata["locator"]
                    ) {
                        "Dependency version changed for ${addedChanges.last().affectedPackage?.name.orEmpty()}."
                    } else {
                        "Dependency target changed for ${addedChanges.last().affectedPackage?.name.orEmpty()}."
                    },
                    metadata = buildMap {
                        putAll(removedChanges.last().metadata)
                        putAll(addedChanges.last().metadata)
                    },
                ),
            )

            addedChanges.isNotEmpty() -> addedChanges.map { change ->
                SemanticDependencyMovement(
                    packageId = change.affectedPackage ?: packageId,
                    kind = SemanticDependencyMovementKind.ADDED,
                    currentVersion = change.affectedPackage?.version,
                    message = "Dependency added for ${change.affectedPackage?.name.orEmpty()}.",
                    metadata = change.metadata,
                )
            }

            else -> removedChanges.map { change ->
                SemanticDependencyMovement(
                    packageId = change.affectedPackage ?: packageId,
                    kind = SemanticDependencyMovementKind.REMOVED,
                    baselineVersion = change.affectedPackage?.version,
                    currentVersion = null,
                    message = "Dependency removed for ${change.affectedPackage?.name.orEmpty()}.",
                    metadata = change.metadata,
                )
            }
        }
    }.distinct().sortedWith(dependencyMovementComparator())
}

private fun SemanticHistoryComparison.validationMovement(
    validationDerivedConsequences: List<SemanticDerivedConsequence>,
): SemanticValidationMovement? {
    val baselineValidation = baseline.validationResult ?: return null
    val currentValidation = diff.snapshot.validationResult ?: return null
    if (baselineValidation == currentValidation && validationDerivedConsequences.none { consequence ->
            consequence.type == SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED
        }
    ) {
        return null
    }

    return SemanticValidationMovement(
        baselineErrorCount = baselineValidation.errorCount(),
        baselineWarningCount = baselineValidation.warningCount(),
        currentErrorCount = currentValidation.errorCount(),
        currentWarningCount = currentValidation.warningCount(),
        baselineContinuationDecision = baselineValidation.continuationDecision,
        currentContinuationDecision = currentValidation.continuationDecision,
        message = "Validation movement from baseline ${baseline.descriptor.label}: " +
            "errors ${baselineValidation.errorCount()} -> ${currentValidation.errorCount()}, " +
            "warnings ${baselineValidation.warningCount()} -> ${currentValidation.warningCount()}.",
    )
}

private fun authoredHistoryMessage(
    baselineLabel: String,
    category: SemanticChangeCategory,
    authoredChanges: List<SemanticChangeRecord>,
    dependencyMovements: List<SemanticDependencyMovement>,
): String {
    return when (category) {
        SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED ->
            "Package evolution from baseline $baselineLabel: repository contract changed."
        SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED ->
            "Package evolution from baseline $baselineLabel: dependency movement detected for " +
                dependencyMovements.joinToString(separator = ", ") { movement -> historyPackageDisplayName(movement.packageId) } +
                "."
        SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
        SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
        SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED,
        SemanticChangeCategory.EXTENSION_SEMANTICS_CHANGED,
        SemanticChangeCategory.VALIDATION_STATE_CHANGED,
        -> "Package evolution from baseline $baselineLabel: ${authoredChanges.first().message}"
    }
}

private fun historyReleaseRelevance(
    authoredChanges: List<SemanticChangeRecord>,
    dependencyMovements: List<SemanticDependencyMovement>,
    validationMovement: SemanticValidationMovement?,
    derivedConsequences: List<SemanticDerivedConsequence>,
): SemanticReleaseRelevance {
    if (derivedConsequences.any { consequence -> consequence.type == SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE }) {
        return SemanticReleaseRelevance.REVIEW_REQUIRED
    }
    if (validationMovement != null && validationMovement.currentErrorCount > validationMovement.baselineErrorCount) {
        return SemanticReleaseRelevance.REVIEW_REQUIRED
    }
    if (authoredChanges.any { change -> change.category == SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED }) {
        return SemanticReleaseRelevance.MAJOR_CANDIDATE
    }
    if (
        dependencyMovements.isNotEmpty() ||
        authoredChanges.any { change ->
            change.category == SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED ||
                change.category == SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED
        }
    ) {
        return SemanticReleaseRelevance.MINOR_CANDIDATE
    }
    if (
        authoredChanges.isNotEmpty() ||
        validationMovement != null ||
        derivedConsequences.isNotEmpty()
    ) {
        return SemanticReleaseRelevance.PATCH_CANDIDATE
    }
    return SemanticReleaseRelevance.NONE
}

private fun historyContractBreakRisk(
    authoredChanges: List<SemanticChangeRecord>,
    dependencyMovements: List<SemanticDependencyMovement>,
    validationMovement: SemanticValidationMovement?,
    derivedConsequences: List<SemanticDerivedConsequence>,
): SemanticContractBreakRisk {
    if (derivedConsequences.any { consequence -> consequence.type == SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE }) {
        return SemanticContractBreakRisk.UNDETERMINED
    }
    if (
        authoredChanges.any { change -> change.category == SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED } ||
        dependencyMovements.any { movement ->
            movement.kind == SemanticDependencyMovementKind.REMOVED ||
                movement.kind == SemanticDependencyMovementKind.RETARGETED
        } ||
        (validationMovement != null && validationMovement.currentErrorCount > validationMovement.baselineErrorCount)
    ) {
        return SemanticContractBreakRisk.HIGH
    }
    if (
        dependencyMovements.any() ||
        authoredChanges.any { change ->
            change.category == SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED ||
                change.category == SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED ||
                change.category == SemanticChangeCategory.VALIDATION_STATE_CHANGED
        } ||
        (validationMovement != null &&
            validationMovement.currentContinuationDecision != validationMovement.baselineContinuationDecision)
    ) {
        return SemanticContractBreakRisk.MEDIUM
    }
    if (authoredChanges.isNotEmpty() || derivedConsequences.isNotEmpty()) {
        return SemanticContractBreakRisk.LOW
    }
    return SemanticContractBreakRisk.NONE
}

private fun RepositoryGraphReport.packageVersion(packageId: PackageIdentifier): String? {
    repository.manifest.primaryPackage.id.takeIf { primary -> samePackageLineage(primary, packageId) }?.let { primary ->
        return primary.version
    }
    repository.manifest.dependencies.firstOrNull { dependency ->
        samePackageLineage(dependency.packageId, packageId)
    }?.let { dependency ->
        return dependency.packageId.version
    }
    graph.packageVersion(packageId)?.let { version -> return version }
    repository.lock.packageVersion(packageId)?.let { version -> return version }
    return null
}

private fun ResolvedPackageGraph?.packageVersion(packageId: PackageIdentifier): String? {
    return this?.packages
        ?.firstOrNull { resolved -> samePackageLineage(resolved.packageId, packageId) }
        ?.packageId
        ?.version
}

private fun RepositoryLock?.packageVersion(packageId: PackageIdentifier): String? {
    this ?: return null
    if (samePackageLineage(primaryPackage, packageId)) {
        return primaryPackage.version
    }
    return packages.firstOrNull { resolved -> samePackageLineage(resolved.packageId, packageId) }?.packageId?.version
}

private fun samePackageLineage(
    left: PackageIdentifier?,
    right: PackageIdentifier?,
): Boolean {
    return left != null && right != null && left.name == right.name
}

private fun historyPackageDisplayName(packageId: PackageIdentifier): String {
    return packageId.version?.let { version -> "${packageId.name}@${version}" } ?: packageId.name
}

private fun SemanticValidationResult.errorCount(): Int {
    return diagnostics.count { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.ERROR }
}

private fun SemanticValidationResult.warningCount(): Int {
    return diagnostics.count { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.WARNING }
}

private fun historyComparisonComparator(): Comparator<SemanticHistoryComparison> {
    return compareBy<SemanticHistoryComparison>(
        { comparison -> comparison.baseline.descriptor.label },
        { comparison -> comparison.baseline.descriptor.baselineId },
        { comparison -> comparison.diff.snapshot.descriptor.label },
        { comparison -> comparison.diff.snapshot.descriptor.baselineId },
    )
}

private fun packageVersionMeaningComparator(): Comparator<SemanticPackageVersionMeaning> {
    return compareBy<SemanticPackageVersionMeaning>(
        { versionMeaning -> versionMeaning.packageId.name },
        { versionMeaning -> versionMeaning.baselineVersion.orEmpty() },
        { versionMeaning -> versionMeaning.currentVersion.orEmpty() },
        { versionMeaning -> versionMeaning.changeKind.ordinal },
    )
}

private fun historyEntryComparator(): Comparator<SemanticHistoryEntry> {
    return compareBy<SemanticHistoryEntry>(
        { entry -> entry.baseline.label },
        { entry -> entry.baseline.baselineId },
        { entry -> entry.kind.ordinal },
        { entry -> entry.changeCategory?.ordinal ?: Int.MAX_VALUE },
        { entry -> entry.releaseRelevance.ordinal },
        { entry -> entry.contractBreakRisk.ordinal },
        { entry -> entry.message },
    )
}

private fun historyChangeComparator(): Comparator<SemanticChangeRecord> {
    return compareBy<SemanticChangeRecord>(
        { change -> change.layer.ordinal },
        { change -> change.category.ordinal },
        { change -> change.affectedPackage?.name ?: "" },
        { change -> change.affectedPackage?.version.orEmpty() },
        { change -> change.subjectIdentity?.value ?: "" },
        { change -> change.message },
    )
}

private fun historyDerivedConsequenceComparator(): Comparator<SemanticDerivedConsequence> {
    return compareBy<SemanticDerivedConsequence>(
        { consequence -> consequence.type.ordinal },
        { consequence -> consequence.affectedPackage?.name ?: "" },
        { consequence -> consequence.affectedPackage?.version.orEmpty() },
        { consequence -> consequence.subjectIdentity?.value ?: "" },
        { consequence -> consequence.message },
    )
}

private fun dependencyMovementComparator(): Comparator<SemanticDependencyMovement> {
    return compareBy<SemanticDependencyMovement>(
        { movement -> movement.packageId.name },
        { movement -> movement.baselineVersion.orEmpty() },
        { movement -> movement.currentVersion.orEmpty() },
        { movement -> movement.kind.ordinal },
        { movement -> movement.message },
    )
}

private fun validationMovementComparator(): Comparator<SemanticValidationMovement> {
    return compareBy<SemanticValidationMovement>(
        { movement -> movement.baselineErrorCount },
        { movement -> movement.currentErrorCount },
        { movement -> movement.baselineWarningCount },
        { movement -> movement.currentWarningCount },
        { movement -> movement.message },
    )
}
