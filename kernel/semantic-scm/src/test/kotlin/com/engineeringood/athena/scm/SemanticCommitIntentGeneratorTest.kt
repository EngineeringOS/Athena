package com.engineeringood.athena.scm

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.PrimaryPackage
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryManifest
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import com.engineeringood.athena.semantics.core.SemanticValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticCommitIntentGeneratorTest {
    @Test
    fun `prepares deterministic typed commit intent from semantic review facts`() {
        val diff = commitIntentDiff()
        val review = SemanticReviewSummaryGenerator().summarize(diff)

        val generator = SemanticCommitIntentGenerator()
        val first = generator.prepare(review)
        val second = generator.prepare(
            review.copy(
                entries = review.entries.reversed(),
                authoredChanges = review.authoredChanges.reversed(),
                derivedConsequences = review.derivedConsequences.reversed(),
            ),
        )

        assertEquals(first, second)
        assertEquals(
            "Semantic commit intent for 2 affected package(s).",
            first.summary,
        )
        assertEquals(review.baseline, first.baseline)
        assertEquals(review.affectedPackages, first.affectedPackages)
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticCommitEntryKind.REPOSITORY_CONTRACT &&
                entry.message == "Commit repository contract change: Primary package contract changed."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticCommitEntryKind.PACKAGE_DEPENDENCY &&
                entry.message == "Commit package dependency change: Package dependency added: com.engineeringood.alpha."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticCommitEntryKind.ENGINEERING_CHANGE &&
                entry.message == "Commit engineering change: Component properties changed: PLC1."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticCommitEntryKind.DERIVED_CONSEQUENCE &&
                entry.message == "Commit derived consequence: Canonical repository lock changed."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticCommitEntryKind.VALIDATION_CONSEQUENCE &&
                entry.factReferences.any { reference ->
                    reference.factKind == SemanticCommitFactKind.DIAGNOSTIC &&
                        reference.identifier.contains("validation.connection.missing")
                }
        })

        val primaryPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0")
        val primaryPackageEntry = first.entries.first { entry ->
            entry.kind == SemanticCommitEntryKind.AFFECTED_PACKAGE && entry.affectedPackage == primaryPackage
        }
        assertTrue(primaryPackageEntry.factReferences.any { reference ->
            reference.factKind == SemanticCommitFactKind.REVIEW_ENTRY &&
                reference.identifier.contains("REPOSITORY_CONTRACT")
        })
        assertTrue(primaryPackageEntry.factReferences.any { reference ->
            reference.factKind == SemanticCommitFactKind.REVIEW_ENTRY &&
                reference.identifier.contains("DERIVED_CONSEQUENCE")
        })

        val inputWarning = first.entries.single { entry -> entry.kind == SemanticCommitEntryKind.INPUT_WARNING }
        assertEquals(
            listOf(
                SemanticCommitFactKind.REVIEW_ENTRY,
                SemanticCommitFactKind.DERIVED_CONSEQUENCE,
                SemanticCommitFactKind.DIAGNOSTIC,
            ),
            inputWarning.factReferences.map { reference -> reference.factKind },
        )
    }
}

private fun commitIntentDiff(): SemanticDiff {
    val primaryPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0")
    val dependencyPackage = PackageIdentifier(name = "com.engineeringood.alpha", version = "1.0.0")
    val baseline = SemanticBaselineDescriptor(
        baselineId = "baseline-commit-1",
        label = "Initial baseline",
    )
    val validationDiagnostic = commitSemanticDiagnostic(
        ruleId = "validation.connection.missing",
        message = "Connection endpoint `Missing.in` does not resolve to any declared port.",
        severity = SemanticDiagnosticSeverity.ERROR,
        subjectIdentity = StableSemanticIdentity("connection:PLC1.out->Missing.in"),
        file = "src/demo.athena",
        line = 12,
    )
    val inputDiagnostic = commitSemanticDiagnostic(
        ruleId = "semantic.current.compile.parse-failed",
        message = "Current repository input could not be compiled for comparison.",
        severity = SemanticDiagnosticSeverity.ERROR,
        file = "src/demo.athena",
        line = 1,
    )
    return SemanticDiff(
        baseline = baseline,
        snapshot = SemanticBaselineSnapshot(
            descriptor = baseline,
            repositoryReport = commitRepositoryGraphReport(primaryPackage),
            validationResult = SemanticValidationResult(
                diagnostics = listOf(validationDiagnostic),
                continuationDecision = SemanticContinuationDecision.STOP_DOWNSTREAM,
            ),
            diagnostics = listOf(inputDiagnostic),
        ),
        authoredChanges = listOf(
            SemanticChangeRecord(
                category = SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
                layer = SemanticChangeLayer.ENGINEERING,
                message = "Component properties changed: PLC1.",
                affectedPackage = primaryPackage,
                subjectIdentity = StableSemanticIdentity("component:PLC1"),
                provenance = commitProvenance("src/demo.athena", 3),
            ),
            SemanticChangeRecord(
                category = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                layer = SemanticChangeLayer.PACKAGE,
                message = "Package dependency added: com.engineeringood.alpha.",
                affectedPackage = dependencyPackage,
            ),
            SemanticChangeRecord(
                category = SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED,
                layer = SemanticChangeLayer.REPOSITORY,
                message = "Primary package contract changed.",
                affectedPackage = primaryPackage,
            ),
        ),
        derivedConsequences = listOf(
            SemanticDerivedConsequence(
                type = SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE,
                message = "Current comparison input is incomplete: ${inputDiagnostic.message}",
                affectedPackage = primaryPackage,
                diagnostic = inputDiagnostic,
                metadata = mapOf("inputSide" to "current"),
            ),
            SemanticDerivedConsequence(
                type = SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED,
                message = "Validation outcome changed between the baseline and current repository state.",
                affectedPackage = primaryPackage,
            ),
            SemanticDerivedConsequence(
                type = SemanticDerivedConsequenceType.LOCK_UPDATED,
                message = "Canonical repository lock changed.",
                affectedPackage = primaryPackage,
            ),
        ),
    )
}

private fun commitRepositoryGraphReport(primaryPackage: PackageIdentifier): RepositoryGraphReport {
    return RepositoryGraphReport(
        repository = EngineeringRepository(
            manifest = RepositoryManifest(
                primaryPackage = PrimaryPackage(id = primaryPackage),
            ),
        ),
    )
}

private fun commitSemanticDiagnostic(
    ruleId: String,
    message: String,
    severity: SemanticDiagnosticSeverity,
    file: String,
    line: Int,
    subjectIdentity: StableSemanticIdentity? = null,
): SemanticDiagnostic {
    return SemanticDiagnostic(
        severity = severity,
        ruleId = SemanticRuleId(ruleId),
        category = SemanticDiagnosticCategory.DOMAIN,
        subjectIdentity = subjectIdentity,
        provenance = commitProvenance(file, line),
        message = message,
    )
}

private fun commitProvenance(file: String, line: Int): SourceProvenance {
    return SourceProvenance(
        file = file,
        startLine = line,
        startColumn = 1,
        endLine = line,
        endColumn = 1,
    )
}
