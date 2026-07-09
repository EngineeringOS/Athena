package com.engineeringood.athena.scm

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.PrimaryPackage
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryManifest
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SemanticBaselineResolverTest {
    @Test
    fun `returns deterministic semantic diagnostic when no adapter supports the locator`() {
        val resolver = SemanticBaselineResolver()
        val result = resolver.resolve(
            SemanticBaselineResolutionRequest(
                descriptor = SemanticBaselineDescriptor(
                    baselineId = "baseline-1",
                    label = "Missing adapter",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = "scm-git",
                    locator = "../baseline",
                ),
                currentRepositoryRoot = Path.of("C:/athena/current"),
            ),
        )

        assertFalse(result.isResolved)
        assertEquals("semantic.baseline.adapter.unsupported", result.diagnostics.single().ruleId.value)
        assertEquals(SemanticDiagnosticSeverity.ERROR, result.diagnostics.single().severity)
    }

    @Test
    fun `delegates baseline resolution to the matching adapter`() {
        val expected = SemanticBaselineSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "baseline-2",
                label = "Expected baseline",
            ),
            repositoryReport = RepositoryGraphReport(
                repository = EngineeringRepository(
                    manifest = RepositoryManifest(
                        primaryPackage = PrimaryPackage(
                            id = PackageIdentifier(name = "com.engineeringood.baseline"),
                        ),
                    ),
                ),
            ),
        )
        val adapter = object : SemanticBaselineAdapter {
            override val adapterId: String = "scm-git"

            override fun resolve(request: SemanticBaselineResolutionRequest): SemanticBaselineResolutionResult {
                return SemanticBaselineResolutionResult(
                    descriptor = request.descriptor,
                    snapshot = expected,
                )
            }
        }
        val resolver = SemanticBaselineResolver(listOf(adapter))
        val result = resolver.resolve(
            SemanticBaselineResolutionRequest(
                descriptor = expected.descriptor,
                locator = SemanticBaselineLocator(
                    adapterId = "scm-git",
                    locator = "../baseline",
                ),
                currentRepositoryRoot = Path.of("C:/athena/current"),
            ),
        )

        assertTrue(result.isResolved)
        assertSame(expected, result.snapshot)
    }
}
