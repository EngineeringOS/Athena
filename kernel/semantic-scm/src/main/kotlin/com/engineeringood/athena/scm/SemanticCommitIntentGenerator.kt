package com.engineeringood.athena.scm

import com.engineeringood.athena.semantics.core.SemanticDiagnostic

/**
 * Deterministic semantic commit-intent generator derived from one already-computed semantic review
 * summary.
 *
 * The generator keeps commit preparation VCS-neutral, adapter-ready, and traceable back to the
 * reviewed semantic facts that justify the proposed commit grouping.
 */
class SemanticCommitIntentGenerator {
    /** Produces one deterministic semantic commit intent for [review]. */
    fun prepare(review: SemanticReviewSummary): SemanticCommitIntent {
        val authoredChanges = review.authoredChanges
            .distinct()
            .sortedWith(commitChangeComparator())
        val derivedConsequences = review.derivedConsequences
            .distinct()
            .sortedWith(commitDerivedConsequenceComparator())
        val diagnostics = review.diagnostics
            .distinct()
            .sortedWith(commitDiagnosticComparator())
        val reviewEntries = review.entries
            .distinct()
            .sortedWith(commitReviewEntryComparator())
        val entries = buildList {
            addAll(affectedPackageEntries(review.affectedPackages, reviewEntries))
            addAll(authoredEntries(authoredChanges, reviewEntries))
            addAll(derivedEntries(derivedConsequences, reviewEntries))
            addAll(validationEntries(reviewEntries))
            addAll(inputWarningEntries(reviewEntries))
        }.distinct().sortedWith(commitEntryComparator())

        return SemanticCommitIntent(
            baseline = review.baseline,
            affectedPackages = review.affectedPackages,
            authoredChanges = authoredChanges,
            derivedConsequences = derivedConsequences,
            diagnostics = diagnostics,
            entries = entries,
            summary = "Semantic commit intent for ${review.affectedPackages.size} affected package(s).",
        )
    }

    private fun affectedPackageEntries(
        affectedPackages: List<com.engineeringood.athena.repository.PackageIdentifier>,
        reviewEntries: List<SemanticReviewEntry>,
    ): List<SemanticCommitEntry> {
        return affectedPackages.map { packageId ->
            val relevantEntries = reviewEntries.filter { entry ->
                entry.affectedPackage == packageId &&
                    entry.kind != SemanticReviewEntryKind.AFFECTED_PACKAGE
            }
            SemanticCommitEntry(
                kind = SemanticCommitEntryKind.AFFECTED_PACKAGE,
                message = "Commit affected package: ${commitPackageDisplayName(packageId)}.",
                affectedPackage = packageId,
                factReferences = relevantEntries
                    .flatMap { entry ->
                        listOf(entry.commitReviewFactReference()) + entry.factReferences.map { reference ->
                            reference.toCommitFactReference()
                        }
                    }
                    .distinct()
                    .sortedWith(commitFactReferenceComparator()),
            )
        }
    }

    private fun authoredEntries(
        authoredChanges: List<SemanticChangeRecord>,
        reviewEntries: List<SemanticReviewEntry>,
    ): List<SemanticCommitEntry> {
        return authoredChanges.map { change ->
            val reviewEntry = reviewEntries.firstOrNull { entry ->
                entry.kind == change.commitReviewEntryKind() &&
                    entry.message == "Authored change: ${change.message}" &&
                    entry.affectedPackage == change.affectedPackage &&
                    entry.subjectIdentity == change.subjectIdentity
            }
            SemanticCommitEntry(
                kind = change.commitEntryKind(),
                message = change.commitMessage(),
                affectedPackage = change.affectedPackage,
                subjectIdentity = change.subjectIdentity,
                factReferences = buildList {
                    reviewEntry?.let { entry -> add(entry.commitReviewFactReference()) }
                    add(change.toCommitFactReference())
                }.distinct().sortedWith(commitFactReferenceComparator()),
            )
        }
    }

    private fun derivedEntries(
        derivedConsequences: List<SemanticDerivedConsequence>,
        reviewEntries: List<SemanticReviewEntry>,
    ): List<SemanticCommitEntry> {
        return derivedConsequences
            .filter { consequence -> consequence.type != SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE }
            .map { consequence ->
                val reviewEntry = reviewEntries.firstOrNull { entry ->
                    entry.kind == SemanticReviewEntryKind.DERIVED_CONSEQUENCE &&
                        entry.message == "Derived consequence: ${consequence.message}" &&
                        entry.affectedPackage == consequence.affectedPackage &&
                        entry.subjectIdentity == consequence.subjectIdentity
                }
                SemanticCommitEntry(
                    kind = SemanticCommitEntryKind.DERIVED_CONSEQUENCE,
                    message = "Commit derived consequence: ${consequence.message}",
                    affectedPackage = consequence.affectedPackage,
                    subjectIdentity = consequence.subjectIdentity,
                    factReferences = buildList {
                        reviewEntry?.let { entry -> add(entry.commitReviewFactReference()) }
                        add(consequence.toCommitFactReference())
                        consequence.diagnostic?.let { diagnostic ->
                            add(diagnostic.toCommitFactReference(consequence.affectedPackage))
                        }
                    }.distinct().sortedWith(commitFactReferenceComparator()),
                )
            }
    }

    private fun validationEntries(
        reviewEntries: List<SemanticReviewEntry>,
    ): List<SemanticCommitEntry> {
        return reviewEntries
            .filter { entry -> entry.kind == SemanticReviewEntryKind.VALIDATION_IMPACT }
            .map { entry ->
                SemanticCommitEntry(
                    kind = SemanticCommitEntryKind.VALIDATION_CONSEQUENCE,
                    message = entry.message.replace("validation impact:", "validation consequence:"),
                    affectedPackage = entry.affectedPackage,
                    subjectIdentity = entry.subjectIdentity,
                    factReferences = listOf(entry.commitReviewFactReference()) +
                        entry.factReferences.map { reference -> reference.toCommitFactReference() },
                )
            }
    }

    private fun inputWarningEntries(
        reviewEntries: List<SemanticReviewEntry>,
    ): List<SemanticCommitEntry> {
        return reviewEntries
            .filter { entry -> entry.kind == SemanticReviewEntryKind.INPUT_WARNING }
            .map { entry ->
                SemanticCommitEntry(
                    kind = SemanticCommitEntryKind.INPUT_WARNING,
                    message = entry.message,
                    affectedPackage = entry.affectedPackage,
                    subjectIdentity = entry.subjectIdentity,
                    factReferences = listOf(entry.commitReviewFactReference()) +
                        entry.factReferences.map { reference -> reference.toCommitFactReference() },
                )
            }
    }
}

private fun SemanticChangeRecord.commitEntryKind(): SemanticCommitEntryKind {
    return when (category) {
        SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED -> SemanticCommitEntryKind.REPOSITORY_CONTRACT
        SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED -> SemanticCommitEntryKind.PACKAGE_DEPENDENCY
        SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
        SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
        SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED,
        SemanticChangeCategory.EXTENSION_SEMANTICS_CHANGED,
        SemanticChangeCategory.VALIDATION_STATE_CHANGED,
        -> SemanticCommitEntryKind.ENGINEERING_CHANGE
    }
}

private fun SemanticChangeRecord.commitReviewEntryKind(): SemanticReviewEntryKind {
    return when (commitEntryKind()) {
        SemanticCommitEntryKind.REPOSITORY_CONTRACT -> SemanticReviewEntryKind.REPOSITORY_CONTRACT
        SemanticCommitEntryKind.PACKAGE_DEPENDENCY -> SemanticReviewEntryKind.PACKAGE_DEPENDENCY
        SemanticCommitEntryKind.ENGINEERING_CHANGE -> SemanticReviewEntryKind.ENGINEERING_CHANGE
        else -> error("Unsupported review kind mapping for $category")
    }
}

private fun SemanticChangeRecord.commitMessage(): String {
    return when (commitEntryKind()) {
        SemanticCommitEntryKind.REPOSITORY_CONTRACT -> "Commit repository contract change: $message"
        SemanticCommitEntryKind.PACKAGE_DEPENDENCY -> "Commit package dependency change: $message"
        SemanticCommitEntryKind.ENGINEERING_CHANGE -> "Commit engineering change: $message"
        else -> error("Unsupported commit entry kind for authored change $category")
    }
}

private fun SemanticReviewEntry.commitReviewFactReference(): SemanticCommitFactReference {
    return SemanticCommitFactReference(
        factKind = SemanticCommitFactKind.REVIEW_ENTRY,
        identifier = listOfNotNull(
            kind.name,
            affectedPackage?.let(::commitPackageDisplayName),
            subjectIdentity?.value,
            message,
        ).joinToString("|"),
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun SemanticReviewFactReference.toCommitFactReference(): SemanticCommitFactReference {
    return SemanticCommitFactReference(
        factKind = when (factKind) {
            SemanticReviewFactKind.AUTHORED_CHANGE -> SemanticCommitFactKind.AUTHORED_CHANGE
            SemanticReviewFactKind.DERIVED_CONSEQUENCE -> SemanticCommitFactKind.DERIVED_CONSEQUENCE
            SemanticReviewFactKind.DIAGNOSTIC -> SemanticCommitFactKind.DIAGNOSTIC
        },
        identifier = identifier,
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun SemanticChangeRecord.toCommitFactReference(): SemanticCommitFactReference {
    return SemanticCommitFactReference(
        factKind = SemanticCommitFactKind.AUTHORED_CHANGE,
        identifier = listOfNotNull(
            "${layer.name}:${category.name}",
            affectedPackage?.let(::commitPackageDisplayName),
            subjectIdentity?.value,
            provenance?.file,
            provenance?.startLine?.toString(),
            message,
            metadata.entries
                .sortedBy { entry -> entry.key }
                .takeIf { entries -> entries.isNotEmpty() }
                ?.joinToString(";") { entry -> "${entry.key}=${entry.value}" },
        ).joinToString("|"),
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun SemanticDerivedConsequence.toCommitFactReference(): SemanticCommitFactReference {
    return SemanticCommitFactReference(
        factKind = SemanticCommitFactKind.DERIVED_CONSEQUENCE,
        identifier = listOfNotNull(
            type.name,
            affectedPackage?.let(::commitPackageDisplayName),
            subjectIdentity?.value,
            diagnostic?.provenance?.file,
            diagnostic?.provenance?.startLine?.toString(),
            message,
            metadata.entries
                .sortedBy { entry -> entry.key }
                .takeIf { entries -> entries.isNotEmpty() }
                ?.joinToString(";") { entry -> "${entry.key}=${entry.value}" },
        ).joinToString("|"),
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun SemanticDiagnostic.toCommitFactReference(
    affectedPackage: com.engineeringood.athena.repository.PackageIdentifier? = null,
): SemanticCommitFactReference {
    return SemanticCommitFactReference(
        factKind = SemanticCommitFactKind.DIAGNOSTIC,
        identifier = listOfNotNull(
            ruleId.value,
            affectedPackage?.let(::commitPackageDisplayName),
            subjectIdentity?.value,
            provenance.file,
            provenance.startLine.toString(),
            message,
        ).joinToString("|"),
        affectedPackage = affectedPackage,
        subjectIdentity = subjectIdentity,
    )
}

private fun commitPackageDisplayName(
    packageId: com.engineeringood.athena.repository.PackageIdentifier,
): String {
    return packageId.version?.let { version -> "${packageId.name}@${version}" } ?: packageId.name
}

private fun commitEntryComparator(): Comparator<SemanticCommitEntry> {
    return compareBy<SemanticCommitEntry>(
        { entry -> entry.kind.ordinal },
        { entry -> entry.affectedPackage?.name ?: "" },
        { entry -> entry.affectedPackage?.version.orEmpty() },
        { entry -> entry.subjectIdentity?.value ?: "" },
        { entry -> entry.factReferences.joinToString("|") { reference -> "${reference.factKind.name}:${reference.identifier}" } },
        { entry -> entry.message },
    )
}

private fun commitFactReferenceComparator(): Comparator<SemanticCommitFactReference> {
    return compareBy<SemanticCommitFactReference>(
        { reference -> reference.factKind.ordinal },
        { reference -> reference.affectedPackage?.name ?: "" },
        { reference -> reference.affectedPackage?.version.orEmpty() },
        { reference -> reference.subjectIdentity?.value ?: "" },
        { reference -> reference.identifier },
    )
}

private fun commitChangeComparator(): Comparator<SemanticChangeRecord> {
    return compareBy<SemanticChangeRecord>(
        { change -> change.layer.ordinal },
        { change -> change.category.ordinal },
        { change -> change.affectedPackage?.name ?: "" },
        { change -> change.subjectIdentity?.value ?: "" },
        { change -> change.message },
    )
}

private fun commitDerivedConsequenceComparator(): Comparator<SemanticDerivedConsequence> {
    return compareBy<SemanticDerivedConsequence>(
        { consequence -> consequence.type.ordinal },
        { consequence -> consequence.affectedPackage?.name ?: "" },
        { consequence -> consequence.subjectIdentity?.value ?: "" },
        { consequence -> consequence.diagnostic?.ruleId?.value ?: "" },
        { consequence -> consequence.diagnostic?.provenance?.file ?: "" },
        { consequence -> consequence.message },
    )
}

private fun commitDiagnosticComparator(): Comparator<SemanticDiagnostic> {
    return compareBy<SemanticDiagnostic>(
        { diagnostic -> diagnostic.ruleId.value },
        { diagnostic -> diagnostic.provenance.file },
        { diagnostic -> diagnostic.provenance.startLine },
        { diagnostic -> diagnostic.provenance.startColumn },
        { diagnostic -> diagnostic.message },
    )
}

private fun commitReviewEntryComparator(): Comparator<SemanticReviewEntry> {
    return compareBy<SemanticReviewEntry>(
        { entry -> entry.kind.ordinal },
        { entry -> entry.affectedPackage?.name ?: "" },
        { entry -> entry.affectedPackage?.version.orEmpty() },
        { entry -> entry.subjectIdentity?.value ?: "" },
        { entry -> entry.message },
    )
}
