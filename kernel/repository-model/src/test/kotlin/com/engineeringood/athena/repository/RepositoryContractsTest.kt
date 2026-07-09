package com.engineeringood.athena.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepositoryContractsTest {
    @Test
    fun `manifest and lock keep authored versus derived contract roles explicit`() {
        val primaryPackage = PrimaryPackage(
            id = PackageIdentifier(name = "athena.demo", version = null),
            sourceRoot = "src",
        )

        val manifest = RepositoryManifest(primaryPackage = primaryPackage)
        val lock = RepositoryLock(primaryPackage = primaryPackage.id)

        assertEquals(RepositoryArtifactRole.AUTHORED_INTENT, manifest.artifactRole)
        assertEquals(RepositoryArtifactRole.DERIVED_STATE, lock.artifactRole)
        assertEquals(1, lock.version)
    }

    @Test
    fun `engineering repository remains vcs neutral by default`() {
        val primaryPackage = PrimaryPackage(id = PackageIdentifier(name = "athena.demo"))
        val repository = EngineeringRepository(
            manifest = RepositoryManifest(primaryPackage = primaryPackage),
            lock = null,
        )

        assertEquals("athena.demo", repository.manifest.primaryPackage.id.name)
        assertNull(repository.lock)
    }

    @Test
    fun `resolved package graph report keeps graph and diagnostics inspectable`() {
        val rootPackage = PackageIdentifier(name = "athena.root", version = "1.0.0")
        val dependencyPackage = PackageIdentifier(name = "athena.dep", version = "1.2.3")
        val graph = ResolvedPackageGraph(
            rootPackage = rootPackage,
            packages = listOf(
                ResolvedPackage(
                    packageId = rootPackage,
                    sourceRoot = "src",
                    directDependencies = listOf(dependencyPackage),
                ),
                ResolvedPackage(
                    packageId = dependencyPackage,
                    sourceRoot = "vendor/dep",
                ),
            ),
        )
        val report = RepositoryGraphReport(
            repository = EngineeringRepository(
                manifest = RepositoryManifest(
                    primaryPackage = PrimaryPackage(id = rootPackage, sourceRoot = "src"),
                    dependencies = listOf(PackageDependency(packageId = dependencyPackage)),
                ),
            ),
            graph = graph,
            diagnostics = listOf(RepositoryDiagnostic(code = "repository.sample", message = "sample")),
        )

        assertEquals(rootPackage, report.graph?.rootPackage)
        assertEquals(listOf(dependencyPackage), report.graph?.packages?.first()?.directDependencies)
        assertEquals("repository.sample", report.diagnostics.single().code)
    }

    @Test
    fun `repository resolution input keeps normalized dependency intent separate from resolved graph state`() {
        val input = RepositoryResolutionInput(
            rootPackage = PackageIdentifier(name = "athena.root", version = "1.0.0"),
            rootSourcePath = "src",
            dependencies = listOf(
                RepositoryResolutionDependency(
                    packageId = PackageIdentifier(name = "athena.dep", version = "1.2.3"),
                    source = PackageDependencySource.LOCAL_PATH,
                    locator = "vendor/dep",
                ),
            ),
        )

        assertEquals("athena.root", input.rootPackage.name)
        assertEquals("src", input.rootSourcePath)
        assertEquals("athena.dep", input.dependencies.single().packageId.name)
        assertEquals("vendor/dep", input.dependencies.single().locator)
    }
}
