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

class SemanticHistorySummaryGeneratorTest {
    @Test
    fun `generates deterministic publish oriented history from the same baseline sequence`() {
        val currentPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "2.0.0")
        val dependencyV10 = PackageIdentifier(name = "com.engineeringood.alpha", version = "1.0.0")
        val dependencyV11 = PackageIdentifier(name = "com.engineeringood.alpha", version = "1.1.0")
        val baselineA = SemanticBaselineDescriptor(
            baselineId = "baseline-a",
            label = "Release A",
        )
        val baselineB = SemanticBaselineDescriptor(
            baselineId = "baseline-b",
            label = "Release B",
        )
        val currentSnapshot = semanticSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "current",
                label = "Working tree",
            ),
            primaryPackage = currentPackage,
            dependencies = listOf(dependencyV11),
            validation = validationResult(errorCount = 2, warningCount = 1),
        )

        val comparisonA = SemanticHistoryComparison(
            baseline = semanticSnapshot(
                descriptor = baselineA,
                primaryPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0"),
                dependencies = emptyList(),
                validation = validationResult(errorCount = 0, warningCount = 0),
            ),
            diff = SemanticDiff(
                baseline = baselineA,
                snapshot = currentSnapshot,
                authoredChanges = listOf(
                    SemanticChangeRecord(
                        category = SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED,
                        layer = SemanticChangeLayer.REPOSITORY,
                        message = "Primary package contract changed.",
                        affectedPackage = currentPackage,
                    ),
                    SemanticChangeRecord(
                        category = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                        layer = SemanticChangeLayer.PACKAGE,
                        message = "Package dependency added: com.engineeringood.alpha.",
                        affectedPackage = dependencyV11,
                        metadata = mapOf("source" to "LOCAL_PATH", "locator" to "../alpha"),
                    ),
                    SemanticChangeRecord(
                        category = SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                        layer = SemanticChangeLayer.ENGINEERING,
                        message = "Component structure changed: PLC1.",
                        affectedPackage = currentPackage,
                        subjectIdentity = StableSemanticIdentity("component:PLC1"),
                        provenance = provenance("src/demo.athena", 3),
                    ),
                ),
                derivedConsequences = listOf(
                    SemanticDerivedConsequence(
                        type = SemanticDerivedConsequenceType.LOCK_UPDATED,
                        message = "Canonical repository lock changed.",
                        affectedPackage = currentPackage,
                    ),
                    SemanticDerivedConsequence(
                        type = SemanticDerivedConsequenceType.PACKAGE_GRAPH_RECOMPUTED,
                        message = "Resolved package graph changed.",
                        affectedPackage = currentPackage,
                    ),
                    SemanticDerivedConsequence(
                        type = SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED,
                        message = "Validation outcome changed between the baseline and current repository state.",
                        affectedPackage = currentPackage,
                    ),
                ),
            ),
        )
        val comparisonB = SemanticHistoryComparison(
            baseline = semanticSnapshot(
                descriptor = baselineB,
                primaryPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.5.0"),
                dependencies = listOf(dependencyV10),
                validation = validationResult(errorCount = 2, warningCount = 1),
            ),
            diff = SemanticDiff(
                baseline = baselineB,
                snapshot = currentSnapshot,
                authoredChanges = listOf(
                    SemanticChangeRecord(
                        category = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                        layer = SemanticChangeLayer.PACKAGE,
                        message = "Package dependency removed: com.engineeringood.alpha.",
                        affectedPackage = dependencyV10,
                        metadata = mapOf("source" to "LOCAL_PATH", "locator" to "../alpha"),
                    ),
                    SemanticChangeRecord(
                        category = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                        layer = SemanticChangeLayer.PACKAGE,
                        message = "Package dependency added: com.engineeringood.alpha.",
                        affectedPackage = dependencyV11,
                        metadata = mapOf("source" to "LOCAL_PATH", "locator" to "../alpha"),
                    ),
                    SemanticChangeRecord(
                        category = SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
                        layer = SemanticChangeLayer.ENGINEERING,
                        message = "Component properties changed: PLC1.",
                        affectedPackage = currentPackage,
                        subjectIdentity = StableSemanticIdentity("component:PLC1"),
                        provenance = provenance("src/demo.athena", 7),
                    ),
                ),
                derivedConsequences = emptyList(),
            ),
        )

        val generator = SemanticHistorySummaryGenerator()
        val first = generator.summarize(
            request = SemanticHistoryRequest(packageId = currentPackage, baselineSequence = listOf(baselineA, baselineB)),
            comparisons = listOf(comparisonA, comparisonB),
        )
        val second = generator.summarize(
            request = SemanticHistoryRequest(packageId = currentPackage, baselineSequence = listOf(baselineB, baselineA)),
            comparisons = listOf(comparisonB, comparisonA),
        )

        assertEquals(first, second)
        assertEquals(SemanticReleaseRelevance.REVIEW_REQUIRED, first.releaseRelevance)
        assertEquals(SemanticContractBreakRisk.HIGH, first.contractBreakRisk)
        assertEquals(
            listOf("1.0.0", "1.5.0"),
            first.packageLineage.map { versionMeaning -> versionMeaning.baselineVersion },
        )
        assertEquals(
            listOf("2.0.0", "2.0.0"),
            first.packageLineage.map { versionMeaning -> versionMeaning.currentVersion },
        )
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticHistoryEntryKind.AUTHORED_EVOLUTION &&
                entry.changeCategory == SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticHistoryEntryKind.AUTHORED_EVOLUTION &&
                entry.dependencyMovements.any { movement ->
                    movement.kind == SemanticDependencyMovementKind.ADDED
                }
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticHistoryEntryKind.AUTHORED_EVOLUTION &&
                entry.dependencyMovements.any { movement ->
                    movement.kind == SemanticDependencyMovementKind.VERSION_CHANGED
                }
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticHistoryEntryKind.VALIDATION_MOVEMENT &&
                entry.validationMovement?.currentErrorCount == 2
        })
        assertTrue(first.entries.any { entry ->
            entry.kind == SemanticHistoryEntryKind.DERIVED_CHURN &&
                entry.derivedConsequences.any { consequence ->
                    consequence.type == SemanticDerivedConsequenceType.LOCK_UPDATED
                }
        })
        assertTrue(first.summary?.contains("2 baseline") == true)
    }

    @Test
    fun `keeps authored package evolution distinct from derived churn`() {
        val currentPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.1.0")
        val dependency = PackageIdentifier(name = "com.engineeringood.alpha", version = "1.0.0")
        val baseline = SemanticBaselineDescriptor(
            baselineId = "baseline-1",
            label = "Release baseline",
        )
        val comparison = SemanticHistoryComparison(
            baseline = semanticSnapshot(
                descriptor = baseline,
                primaryPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0"),
                dependencies = emptyList(),
                validation = validationResult(errorCount = 0, warningCount = 0),
            ),
            diff = SemanticDiff(
                baseline = baseline,
                snapshot = semanticSnapshot(
                    descriptor = SemanticBaselineDescriptor(
                        baselineId = "current",
                        label = "Working tree",
                    ),
                    primaryPackage = currentPackage,
                    dependencies = listOf(dependency),
                    validation = validationResult(errorCount = 0, warningCount = 0),
                ),
                authoredChanges = listOf(
                    SemanticChangeRecord(
                        category = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                        layer = SemanticChangeLayer.PACKAGE,
                        message = "Package dependency added: com.engineeringood.alpha.",
                        affectedPackage = dependency,
                        metadata = mapOf("source" to "LOCAL_PATH", "locator" to "../alpha"),
                    ),
                ),
                derivedConsequences = listOf(
                    SemanticDerivedConsequence(
                        type = SemanticDerivedConsequenceType.LOCK_UPDATED,
                        message = "Canonical repository lock changed.",
                        affectedPackage = currentPackage,
                    ),
                    SemanticDerivedConsequence(
                        type = SemanticDerivedConsequenceType.PACKAGE_GRAPH_RECOMPUTED,
                        message = "Resolved package graph changed.",
                        affectedPackage = currentPackage,
                    ),
                ),
            ),
        )

        val summary = SemanticHistorySummaryGenerator().summarize(
            request = SemanticHistoryRequest(packageId = currentPackage, baselineSequence = listOf(baseline)),
            comparisons = listOf(comparison),
        )

        val authoredEntry = summary.entries.single { entry -> entry.kind == SemanticHistoryEntryKind.AUTHORED_EVOLUTION }
        val churnEntry = summary.entries.single { entry -> entry.kind == SemanticHistoryEntryKind.DERIVED_CHURN }

        assertTrue(authoredEntry.authoredChanges.isNotEmpty())
        assertTrue(authoredEntry.dependencyMovements.isNotEmpty())
        assertTrue(authoredEntry.derivedConsequences.isEmpty())
        assertTrue(churnEntry.authoredChanges.isEmpty())
        assertTrue(churnEntry.derivedConsequences.map { consequence -> consequence.type }.containsAll(
            listOf(
                SemanticDerivedConsequenceType.LOCK_UPDATED,
                SemanticDerivedConsequenceType.PACKAGE_GRAPH_RECOMPUTED,
            ),
        ))
    }
}

private fun semanticSnapshot(
    descriptor: SemanticBaselineDescriptor,
    primaryPackage: PackageIdentifier,
    dependencies: List<PackageIdentifier>,
    validation: SemanticValidationResult,
): SemanticBaselineSnapshot {
    return SemanticBaselineSnapshot(
        descriptor = descriptor,
        repositoryReport = RepositoryGraphReport(
            repository = EngineeringRepository(
                manifest = RepositoryManifest(
                    primaryPackage = PrimaryPackage(id = primaryPackage),
                    dependencies = dependencies.map { dependency ->
                        com.engineeringood.athena.repository.PackageDependency(
                            packageId = dependency,
                            locator = "../${dependency.name.substringAfterLast('.')}",
                        )
                    },
                ),
            ),
        ),
        validationResult = validation,
    )
}

private fun validationResult(
    errorCount: Int,
    warningCount: Int,
): SemanticValidationResult {
    val diagnostics = buildList {
        repeat(errorCount) { index ->
            add(
                semanticDiagnostic(
                    ruleId = "validation.error.$index",
                    message = "Validation error $index.",
                    severity = SemanticDiagnosticSeverity.ERROR,
                    file = "src/demo.athena",
                    line = index + 1,
                ),
            )
        }
        repeat(warningCount) { index ->
            add(
                semanticDiagnostic(
                    ruleId = "validation.warning.$index",
                    message = "Validation warning $index.",
                    severity = SemanticDiagnosticSeverity.WARNING,
                    file = "src/demo.athena",
                    line = index + 20,
                ),
            )
        }
    }
    return SemanticValidationResult(
        diagnostics = diagnostics,
        continuationDecision = if (errorCount > 0) {
            SemanticContinuationDecision.STOP_DOWNSTREAM
        } else {
            SemanticContinuationDecision.CONTINUE
        },
    )
}

private fun semanticDiagnostic(
    ruleId: String,
    message: String,
    severity: SemanticDiagnosticSeverity,
    file: String,
    line: Int,
): SemanticDiagnostic {
    return SemanticDiagnostic(
        severity = severity,
        ruleId = SemanticRuleId(ruleId),
        category = SemanticDiagnosticCategory.DOMAIN,
        subjectIdentity = StableSemanticIdentity("diagnostic:$ruleId"),
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
