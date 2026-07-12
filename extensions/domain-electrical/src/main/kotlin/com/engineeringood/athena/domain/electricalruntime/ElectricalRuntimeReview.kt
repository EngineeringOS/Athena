package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewEnrichmentKind
import com.engineeringood.athena.scm.SemanticReviewFactKind
import com.engineeringood.athena.scm.SemanticReviewFactReference
import com.engineeringood.athena.scm.SemanticReviewSummary

internal fun enrichElectricalRuntimeReview(
    pluginId: String,
    review: SemanticReviewSummary,
): List<SemanticReviewEnrichment> {
    val matchingDiagnostics = review.diagnostics.filter { diagnostic ->
        diagnostic.ruleId.value.contains("connection.direction", ignoreCase = true) ||
            diagnostic.ruleId.value.contains("connection.signal", ignoreCase = true) ||
            diagnostic.message.contains("`direction`", ignoreCase = true) ||
            diagnostic.message.contains("`signal`", ignoreCase = true) ||
            diagnostic.message.contains("device type", ignoreCase = true)
    }
    val matchingEntries = review.entries.filter { entry ->
        entry.message.contains("Connection", ignoreCase = true) ||
            entry.message.contains("signal", ignoreCase = true) ||
            entry.message.contains("direction", ignoreCase = true)
    }
    if (matchingDiagnostics.isEmpty() && matchingEntries.isEmpty()) {
        return emptyList()
    }

    val factReferences = (
        matchingEntries.flatMap { entry -> entry.factReferences } +
            matchingDiagnostics.map { diagnostic ->
                SemanticReviewFactReference(
                    factKind = SemanticReviewFactKind.DIAGNOSTIC,
                    identifier = diagnostic.ruleId.value,
                    subjectIdentity = diagnostic.subjectIdentity,
                )
            }
        ).distinct()
        .sortedWith(
            compareBy<SemanticReviewFactReference>(
                { reference -> reference.factKind.name },
                { reference -> reference.identifier },
                { reference -> reference.subjectIdentity?.value.orEmpty() },
            ),
        )

    return listOf(
        SemanticReviewEnrichment(
            pluginId = pluginId,
            kind = SemanticReviewEnrichmentKind.DOMAIN_LABEL,
            message = "Electrical runtime semantics are implicated in this review.",
            factReferences = factReferences,
        ),
        SemanticReviewEnrichment(
            pluginId = pluginId,
            kind = SemanticReviewEnrichmentKind.REVIEW_HINT,
            message = "Check direction, signal, and device-type consistency before finalizing the change.",
            factReferences = factReferences,
        ),
        SemanticReviewEnrichment(
            pluginId = pluginId,
            kind = SemanticReviewEnrichmentKind.DOMAIN_SUMMARY,
            message = "Electrical review found ${matchingDiagnostics.size} electrical diagnostic(s) and ${matchingEntries.size} electrical review entry candidate(s).",
            factReferences = factReferences,
        ),
    )
}
