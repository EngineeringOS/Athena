package com.engineeringood.athena.scm

import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity

/**
 * Deterministic semantic review-summary generator derived from one already-computed semantic diff.
 *
 * The generator keeps review output VCS-neutral and traceable back to the authored changes,
 * derived consequences, and diagnostics that justified each review statement.
 */
class SemanticReviewSummaryGenerator {
    /** Produces one deterministic semantic review summary for [diff]. */
    fun summarize(diff: SemanticDiff): SemanticReviewSummary {
        val authoredChanges = diff.authoredChanges
            .distinct()
            .sortedWith(reviewChangeComparator())
        val derivedConsequences = diff.derivedConsequences
            .distinct()
            .sortedWith(reviewDerivedConsequenceComparator())
        val diagnostics = collectDiagnostics(diff)
        val entries = buildList {
            addAll(
                affectedPackageEntries(
                    affectedPackages = diff.affectedPackages,
                    authoredChanges = authoredChanges,
                    derivedConsequences = derivedConsequences,
                ),
            )
            addAll(authoredEntries(authoredChanges))
            addAll(derivedEntries(derivedConsequences))
            addAll(validationImpactEntries(diff.snapshot.validationResult?.diagnostics.orEmpty()))
            addAll(
                inputWarningEntries(
                    diagnostics = diff.snapshot.diagnostics,
                    derivedConsequences = derivedConsequences,
                ),
            )
        }.distinct().sortedWith(reviewEntryComparator())

        return SemanticReviewSummary(
            baseline = diff.baseline,
            affectedPackages = diff.affectedPackages,
            authoredChanges = authoredChanges,
            derivedConsequences = derivedConsequences,
            diagnostics = diagnostics,
            entries = entries,
        )
    }

    private fun collectDiagnostics(diff: SemanticDiff): List<SemanticDiagnostic> {
        return (
            diff.snapshot.validationResult?.diagnostics.orEmpty() +
                diff.snapshot.diagnostics +
                diff.derivedConsequences.mapNotNull { consequence -> consequence.diagnostic }
            ).distinct()
            .sortedWith(semanticDiagnosticComparator())
    }

    private fun affectedPackageEntries(
        affectedPackages: List<PackageIdentifier>,
        authoredChanges: List<SemanticChangeRecord>,
        derivedConsequences: List<SemanticDerivedConsequence>,
    ): List<SemanticReviewEntry> {
        return affectedPackages.map { packageId ->
            SemanticReviewEntry(
                kind = SemanticReviewEntryKind.AFFECTED_PACKAGE,
                message = "Affected package: ${packageDisplayName(packageId)}.",
                affectedPackage = packageId,
                factReferences = (
                    authoredChanges
                        .filter { change -> change.affectedPackage == packageId }
                        .map { change -> change.reviewFactReference() } +
                        derivedConsequences
                            .filter { consequence -> consequence.affectedPackage == packageId }
                            .map { consequence -> consequence.reviewFactReference() }
                    )
                    .distinct()
                    .sortedWith(reviewFactReferenceComparator()),
            )
        }
    }

    private fun authoredEntries(
        authoredChanges: List<SemanticChangeRecord>,
    ): List<SemanticReviewEntry> {
        return authoredChanges.map { change ->
            SemanticReviewEntry(
                kind = change.reviewEntryKind(),
                message = "Authored change: ${change.message}",
                affectedPackage = change.affectedPackage,
                subjectIdentity = change.subjectIdentity,
                factReferences = listOf(
                    change.reviewFactReference(),
                ),
            )
        }
    }

    private fun derivedEntries(
        derivedConsequences: List<SemanticDerivedConsequence>,
    ): List<SemanticReviewEntry> {
        return derivedConsequences.map { consequence ->
            SemanticReviewEntry(
                kind = if (consequence.type == SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE) {
                    SemanticReviewEntryKind.INPUT_WARNING
                } else {
                    SemanticReviewEntryKind.DERIVED_CONSEQUENCE
                },
                message = if (consequence.type == SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE) {
                    "Comparison input warning: ${consequence.message}"
                } else {
                    "Derived consequence: ${consequence.message}"
                },
                affectedPackage = consequence.affectedPackage,
                subjectIdentity = consequence.subjectIdentity,
                factReferences = buildList {
                    add(
                        SemanticReviewFactReference(
                            factKind = consequence.reviewFactReference().factKind,
                            identifier = consequence.reviewFactReference().identifier,
                            affectedPackage = consequence.reviewFactReference().affectedPackage,
                            subjectIdentity = consequence.reviewFactReference().subjectIdentity,
                        ),
                    )
                    consequence.diagnostic?.let { diagnostic ->
                        add(diagnostic.reviewFactReference(consequence.affectedPackage))
                    }
                },
            )
        }
    }

    private fun validationImpactEntries(
        diagnostics: List<SemanticDiagnostic>,
    ): List<SemanticReviewEntry> {
        return diagnostics.map { diagnostic ->
            SemanticReviewEntry(
                kind = SemanticReviewEntryKind.VALIDATION_IMPACT,
                message = "${diagnostic.severity.label()} validation impact: ${diagnostic.message}",
                subjectIdentity = diagnostic.subjectIdentity,
                factReferences = listOf(
                    diagnostic.reviewFactReference(),
                ),
            )
        }
    }

    private fun inputWarningEntries(
        diagnostics: List<SemanticDiagnostic>,
        derivedConsequences: List<SemanticDerivedConsequence>,
    ): List<SemanticReviewEntry> {
        val coveredDiagnostics = derivedConsequences
            .filter { consequence -> consequence.type == SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE }
            .mapNotNull { consequence -> consequence.diagnostic }
            .toSet()
        return diagnostics.map { diagnostic ->
            diagnostic
        }.filterNot { diagnostic -> diagnostic in coveredDiagnostics }.map { diagnostic ->
            SemanticReviewEntry(
                kind = SemanticReviewEntryKind.INPUT_WARNING,
                message = "Comparison input warning: ${diagnostic.message}",
                subjectIdentity = diagnostic.subjectIdentity,
                factReferences = listOf(
                    diagnostic.reviewFactReference(),
                ),
            )
        }
    }
}

private fun SemanticChangeRecord.reviewEntryKind(): SemanticReviewEntryKind {
    return when (category) {
        SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED -> SemanticReviewEntryKind.REPOSITORY_CONTRACT
        SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED -> SemanticReviewEntryKind.PACKAGE_DEPENDENCY
        SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
        SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
        SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED,
        SemanticChangeCategory.EXTENSION_SEMANTICS_CHANGED,
        SemanticChangeCategory.VALIDATION_STATE_CHANGED,
        -> SemanticReviewEntryKind.ENGINEERING_CHANGE
    }
}

private fun SemanticChangeRecord.reviewFactReference(): SemanticReviewFactReference {
    return SemanticReviewFactReference(
        factKind = SemanticReviewFactKind.AUTHORED_CHANGE,
        identifier = traceIdentifier(
            head = "${layer.name}:${category.name}",
            affectedPackage = affectedPackage,
            subjectIdentity = subjectIdentity,
            file = provenance?.file,
            line = provenance?.startLine,
            message = message,
            metadata = metadata,
        ),
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun SemanticDerivedConsequence.reviewFactReference(): SemanticReviewFactReference {
    return SemanticReviewFactReference(
        factKind = SemanticReviewFactKind.DERIVED_CONSEQUENCE,
        identifier = traceIdentifier(
            head = type.name,
            affectedPackage = affectedPackage,
            subjectIdentity = subjectIdentity,
            file = diagnostic?.provenance?.file,
            line = diagnostic?.provenance?.startLine,
            message = message,
            metadata = metadata,
        ),
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun SemanticDiagnostic.reviewFactReference(
    affectedPackage: PackageIdentifier? = null,
): SemanticReviewFactReference {
    return SemanticReviewFactReference(
        factKind = SemanticReviewFactKind.DIAGNOSTIC,
        identifier = traceIdentifier(
            head = ruleId.value,
            affectedPackage = affectedPackage,
            subjectIdentity = subjectIdentity,
            file = provenance.file,
            line = provenance.startLine,
            message = message,
        ),
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun packageDisplayName(packageId: PackageIdentifier): String {
    return packageId.version?.let { version -> "${packageId.name}@${version}" } ?: packageId.name
}

private fun traceIdentifier(
    head: String,
    affectedPackage: PackageIdentifier? = null,
    subjectIdentity: com.engineeringood.athena.ir.StableSemanticIdentity? = null,
    file: String? = null,
    line: Int? = null,
    message: String,
    metadata: Map<String, String> = emptyMap(),
): String {
    return listOfNotNull(
        head,
        affectedPackage?.let(::packageDisplayName),
        subjectIdentity?.value,
        file,
        line?.toString(),
        message,
        metadata.entries
            .sortedBy { entry -> entry.key }
            .takeIf { entries -> entries.isNotEmpty() }
            ?.joinToString(";") { entry -> "${entry.key}=${entry.value}" },
    ).joinToString("|")
}

private fun reviewEntryComparator(): Comparator<SemanticReviewEntry> {
    return compareBy<SemanticReviewEntry>(
        { entry -> entry.kind.ordinal },
        { entry -> entry.affectedPackage?.name ?: "" },
        { entry -> entry.affectedPackage?.version.orEmpty() },
        { entry -> entry.subjectIdentity?.value ?: "" },
        { entry -> entry.factReferences.joinToString("|") { reference -> "${reference.factKind.name}:${reference.identifier}" } },
        { entry -> entry.message },
    )
}

private fun reviewFactReferenceComparator(): Comparator<SemanticReviewFactReference> {
    return compareBy<SemanticReviewFactReference>(
        { reference -> reference.factKind.ordinal },
        { reference -> reference.affectedPackage?.name ?: "" },
        { reference -> reference.affectedPackage?.version.orEmpty() },
        { reference -> reference.subjectIdentity?.value ?: "" },
        { reference -> reference.identifier },
    )
}

private fun reviewChangeComparator(): Comparator<SemanticChangeRecord> {
    return compareBy<SemanticChangeRecord>(
        { change -> change.layer.ordinal },
        { change -> change.category.ordinal },
        { change -> change.affectedPackage?.name ?: "" },
        { change -> change.subjectIdentity?.value ?: "" },
        { change -> change.message },
    )
}

private fun reviewDerivedConsequenceComparator(): Comparator<SemanticDerivedConsequence> {
    return compareBy<SemanticDerivedConsequence>(
        { consequence -> consequence.type.ordinal },
        { consequence -> consequence.affectedPackage?.name ?: "" },
        { consequence -> consequence.subjectIdentity?.value ?: "" },
        { consequence -> consequence.diagnostic?.ruleId?.value ?: "" },
        { consequence -> consequence.diagnostic?.provenance?.file ?: "" },
        { consequence -> consequence.message },
    )
}

private fun semanticDiagnosticComparator(): Comparator<SemanticDiagnostic> {
    return compareBy<SemanticDiagnostic>(
        { diagnostic -> diagnostic.ruleId.value },
        { diagnostic -> diagnostic.provenance.file },
        { diagnostic -> diagnostic.provenance.startLine },
        { diagnostic -> diagnostic.provenance.startColumn },
        { diagnostic -> diagnostic.message },
    )
}

private fun SemanticDiagnosticSeverity.label(): String {
    return name.lowercase().replaceFirstChar { character -> character.uppercase() }
}
