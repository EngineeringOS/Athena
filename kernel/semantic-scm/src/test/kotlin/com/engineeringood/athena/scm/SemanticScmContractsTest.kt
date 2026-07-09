package com.engineeringood.athena.scm

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.PrimaryPackage
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryManifest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticScmContractsTest {
    @Test
    fun `semantic scm contracts keep change intent and derived consequences inspectable`() {
        val packageId = PackageIdentifier(name = "athena.root", version = "1.0.0")
        val baseline = SemanticBaselineDescriptor(
            baselineId = "baseline-1",
            label = "Initial semantic baseline",
        )
        val report = RepositoryGraphReport(
            repository = EngineeringRepository(
                manifest = RepositoryManifest(
                    primaryPackage = PrimaryPackage(id = packageId),
                ),
            ),
        )
        val snapshot = SemanticBaselineSnapshot(
            descriptor = baseline,
            repositoryReport = report,
        )
        val authoredChange = SemanticChangeRecord(
            category = SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED,
            layer = SemanticChangeLayer.REPOSITORY,
            message = "Repository manifest changed",
            affectedPackage = packageId,
        )
        val derivedConsequence = SemanticDerivedConsequence(
            type = SemanticDerivedConsequenceType.LOCK_UPDATED,
            message = "Lock state must be refreshed",
            affectedPackage = packageId,
        )
        val diff = SemanticDiff(
            baseline = baseline,
            snapshot = snapshot,
            authoredChanges = listOf(authoredChange),
            derivedConsequences = listOf(derivedConsequence),
        )
        val review = SemanticReviewSummary(
            baseline = baseline,
            affectedPackages = listOf(packageId),
            authoredChanges = listOf(authoredChange),
            derivedConsequences = listOf(derivedConsequence),
        )
        val commitIntent = SemanticCommitIntent(
            baseline = baseline,
            affectedPackages = listOf(packageId),
            authoredChanges = listOf(authoredChange),
            derivedConsequences = listOf(derivedConsequence),
        )
        val versionMeaning = SemanticPackageVersionMeaning(
            packageId = packageId,
            baselineVersion = "0.9.0",
            currentVersion = "1.0.0",
            changeKind = SemanticPackageVersionChangeKind.VERSION_UPDATED,
        )
        val history = SemanticHistorySummary(
            packageId = packageId,
            baselineSequence = listOf(baseline),
            packageLineage = listOf(versionMeaning),
            entries = listOf(
                SemanticHistoryEntry(
                    baseline = baseline,
                    packageVersion = versionMeaning,
                    changeCategory = SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED,
                    releaseRelevance = SemanticReleaseRelevance.MINOR_CANDIDATE,
                    message = "Repository contract evolved",
                ),
            ),
            releaseRelevance = SemanticReleaseRelevance.MINOR_CANDIDATE,
        )

        assertEquals("baseline-1", diff.baseline.baselineId)
        assertEquals(SemanticChangeLayer.REPOSITORY, review.authoredChanges.single().layer)
        assertEquals(SemanticDerivedConsequenceType.LOCK_UPDATED, commitIntent.derivedConsequences.single().type)
        assertEquals(packageId, history.packageId)
        assertEquals(SemanticReleaseRelevance.MINOR_CANDIDATE, history.releaseRelevance)
        assertTrue(history.entries.isNotEmpty())
    }

    @Test
    fun `semantic history contracts stay package aware and transport light`() {
        val rootPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "2.0.0")
        val dependencyPackage = PackageIdentifier(name = "com.engineeringood.alpha", version = "1.1.0")
        val baseline = SemanticBaselineDescriptor(
            baselineId = "baseline-2",
            label = "Release review baseline",
        )

        val request = SemanticHistoryRequest(
            packageId = rootPackage,
            baselineSequence = listOf(baseline),
        )
        val versionMeaning = SemanticPackageVersionMeaning(
            packageId = rootPackage,
            baselineVersion = "1.5.0",
            currentVersion = "2.0.0",
            changeKind = SemanticPackageVersionChangeKind.VERSION_UPDATED,
        )
        val dependencyMovement = SemanticDependencyMovement(
            packageId = dependencyPackage,
            kind = SemanticDependencyMovementKind.VERSION_CHANGED,
            baselineVersion = "1.0.0",
            currentVersion = "1.1.0",
            message = "Dependency version updated for `com.engineeringood.alpha`.",
        )
        val history = SemanticHistorySummary(
            packageId = rootPackage,
            baselineSequence = request.baselineSequence,
            packageLineage = listOf(versionMeaning),
            entries = listOf(
                SemanticHistoryEntry(
                    baseline = baseline,
                    packageVersion = versionMeaning,
                    changeCategory = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                    releaseRelevance = SemanticReleaseRelevance.MAJOR_CANDIDATE,
                    message = "Package release relevance increased after dependency and contract evolution.",
                    dependencyMovements = listOf(dependencyMovement),
                ),
            ),
            releaseRelevance = SemanticReleaseRelevance.MAJOR_CANDIDATE,
        )

        assertEquals(rootPackage, request.packageId)
        assertEquals("1.5.0", history.packageLineage.single().baselineVersion)
        assertEquals("2.0.0", history.packageLineage.single().currentVersion)
        assertEquals(
            SemanticPackageVersionChangeKind.VERSION_UPDATED,
            history.packageLineage.single().changeKind,
        )
        assertEquals(
            SemanticDependencyMovementKind.VERSION_CHANGED,
            history.entries.single().dependencyMovements.single().kind,
        )
        assertEquals(
            SemanticReleaseRelevance.MAJOR_CANDIDATE,
            history.entries.single().releaseRelevance,
        )
        assertTrue(history.entries.single().message.contains("release relevance"))
    }
}
