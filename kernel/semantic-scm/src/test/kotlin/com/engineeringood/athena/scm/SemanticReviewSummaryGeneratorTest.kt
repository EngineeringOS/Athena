package com.engineeringood.athena.scm

import com.engineeringood.athena.ir.DerivedEngineeringInputKind
import com.engineeringood.athena.ir.EngineeringConstraintRuleKind
import com.engineeringood.athena.ir.EngineeringImpactConsequence
import com.engineeringood.athena.ir.EngineeringImpactConsequences
import com.engineeringood.athena.ir.EngineeringImpactReasonKind
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

class SemanticReviewSummaryGeneratorTest {
    @Test
    fun `generates deterministic typed review entries from semantic diff facts`() {
        val primaryPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0")
        val dependencyPackage = PackageIdentifier(name = "com.engineeringood.alpha", version = "1.0.0")
        val baseline = SemanticBaselineDescriptor(
            baselineId = "baseline-1",
            label = "Initial baseline",
        )
        val validationDiagnostic = semanticDiagnostic(
            ruleId = "validation.connection.missing",
            message = "Connection endpoint `Missing.in` does not resolve to any declared port.",
            severity = SemanticDiagnosticSeverity.ERROR,
            subjectIdentity = StableSemanticIdentity("connection:PLC1.out->Missing.in"),
            file = "src/demo.athena",
            line = 12,
        )
        val inputDiagnostic = semanticDiagnostic(
            ruleId = "semantic.current.compile.parse-failed",
            message = "Current repository input could not be compiled for comparison.",
            severity = SemanticDiagnosticSeverity.ERROR,
            file = "src/demo.athena",
            line = 1,
        )
        val knowledgeDiagnostic = semanticDiagnostic(
            ruleId = "knowledge.protection_sufficiency",
            message = "Breaker current 10A is below required 18A for `component:M1`.",
            severity = SemanticDiagnosticSeverity.ERROR,
            file = "src/demo.athena",
            line = 5,
            subjectIdentity = StableSemanticIdentity("component:M1"),
        )
        val diff = SemanticDiff(
            baseline = baseline,
            snapshot = SemanticBaselineSnapshot(
                descriptor = baseline,
                repositoryReport = repositoryGraphReport(primaryPackage),
                knowledgeDiagnostics = listOf(knowledgeDiagnostic),
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
                    provenance = provenance("src/demo.athena", 3),
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
            engineeringImpactConsequences = EngineeringImpactConsequences.canonical(
                listOf(
                    EngineeringImpactConsequence(
                        affectedSubjectIdentity = StableSemanticIdentity("component:QF1"),
                        triggerSubjectIdentities = listOf(StableSemanticIdentity("component:M1")),
                        reasonKinds = listOf(
                            EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED,
                            EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED,
                        ),
                        affectedInputKinds = listOf(DerivedEngineeringInputKind.MOTOR_POWER),
                        affectedConstraintRuleKinds = listOf(EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY),
                    ),
                ),
            ),
        )

        val generator = SemanticReviewSummaryGenerator()
        val first = generator.summarize(diff)
        val second = generator.summarize(
            diff.copy(
                authoredChanges = diff.authoredChanges.reversed(),
                derivedConsequences = diff.derivedConsequences.reversed(),
            ),
        )

        assertEquals(first, second)
        assertEquals(listOf(primaryPackage, dependencyPackage), first.affectedPackages)
        assertEquals(
            listOf(
                "knowledge.protection_sufficiency",
                "semantic.current.compile.parse-failed",
                "validation.connection.missing",
            ),
            first.diagnostics.map { diagnostic -> diagnostic.ruleId.value },
        )
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticReviewEntryKind.REPOSITORY_CONTRACT &&
                entry.message == "Authored change: Primary package contract changed."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticReviewEntryKind.PACKAGE_DEPENDENCY &&
                entry.message == "Authored change: Package dependency added: com.engineeringood.alpha."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticReviewEntryKind.ENGINEERING_CHANGE &&
                entry.message == "Authored change: Component properties changed: PLC1."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticReviewEntryKind.ENGINEERING_IMPACT &&
                entry.message.contains("`component:QF1`") &&
                entry.message.contains("`component:M1`")
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticReviewEntryKind.DERIVED_CONSEQUENCE &&
                entry.message == "Derived consequence: Canonical repository lock changed."
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticReviewEntryKind.VALIDATION_IMPACT &&
                entry.factReferences.single().identifier.contains("validation.connection.missing")
        })
        assertEquals(1, first.entries.count { entry -> entry.kind == SemanticReviewEntryKind.INPUT_WARNING })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticReviewEntryKind.VALIDATION_IMPACT &&
                entry.factReferences.single().identifier.contains("knowledge.protection_sufficiency")
        })

        val primaryPackageEntry = first.entries.first { entry ->
            entry.kind == SemanticReviewEntryKind.AFFECTED_PACKAGE && entry.affectedPackage == primaryPackage
        }
        assertTrue(primaryPackageEntry.factReferences.any { reference ->
            reference.factKind == SemanticReviewFactKind.AUTHORED_CHANGE &&
                reference.identifier.contains("REPOSITORY_CONTRACT_CHANGED")
        })
        assertTrue(primaryPackageEntry.factReferences.any { reference ->
            reference.factKind == SemanticReviewFactKind.DERIVED_CONSEQUENCE &&
                reference.identifier.contains("LOCK_UPDATED")
        })

        val inputWarning = first.entries.single { entry -> entry.kind == SemanticReviewEntryKind.INPUT_WARNING }
        assertEquals(
            listOf(
                SemanticReviewFactKind.DERIVED_CONSEQUENCE,
                SemanticReviewFactKind.DIAGNOSTIC,
            ),
            inputWarning.factReferences.map { reference -> reference.factKind },
        )
        assertEquals(
            1,
            first.engineeringImpactConsequences.consequences.size,
        )
    }
}

private fun repositoryGraphReport(primaryPackage: PackageIdentifier): RepositoryGraphReport {
    return RepositoryGraphReport(
        repository = EngineeringRepository(
            manifest = RepositoryManifest(
                primaryPackage = PrimaryPackage(id = primaryPackage),
            ),
        ),
    )
}

private fun semanticDiagnostic(
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
        provenance = provenance(file, line),
        message = message,
    )
}

private fun provenance(file: String, line: Int): SourceProvenance {
    return SourceProvenance(
        file = file,
        startLine = line,
        startColumn = 1,
        endLine = line,
        endColumn = 1,
    )
}
