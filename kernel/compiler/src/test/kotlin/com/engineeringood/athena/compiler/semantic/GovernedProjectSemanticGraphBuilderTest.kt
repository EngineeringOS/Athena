package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportLockState
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.PackageDependency
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.PrimaryPackage
import com.engineeringood.athena.repository.RepositoryLock
import com.engineeringood.athena.repository.RepositoryManifest
import com.engineeringood.athena.repository.RepositoryResolutionInput
import com.engineeringood.athena.repository.ResolvedPackage
import com.engineeringood.athena.repository.ResolvedPackageGraph
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GovernedProjectSemanticGraphBuilderTest {
    @Test
    fun `builds deterministic cross-package snapshot only from governed publication and source inputs`() {
        val fixture = governedFixture()
        val builder = GovernedProjectSemanticGraphBuilder()

        val first = builder.build(fixture.publication, fixture.sources)
        val reordered = builder.build(fixture.publication, fixture.sources.reversed())
        val compilerResult = AthenaCompiler().buildProjectSemanticGraph(fixture.publication, fixture.sources.reversed())

        val snapshot = assertNotNull(first.snapshot)
        val reorderedSnapshot = assertNotNull(reordered.snapshot)
        assertEquals(emptyList(), first.diagnostics)
        assertEquals(snapshot.graphId, reorderedSnapshot.graphId)
        assertEquals(snapshot.graphId, assertNotNull(compilerResult.snapshot).graphId)
        assertEquals(snapshot.sourceUnits, reorderedSnapshot.sourceUnits)
        assertEquals(snapshot.namespaces, reorderedSnapshot.namespaces)
        assertEquals(snapshot.diagnostics, reorderedSnapshot.diagnostics)
        assertEquals(listOf("com.controls", "com.root"), snapshot.packages.map { it.packageId.name })
        assertEquals(snapshot.namespaces.map { it.namespaceId.value }.sorted(), snapshot.namespaces.map { it.namespaceId.value })
        assertEquals(setOf("com.controls", "com.root"), snapshot.namespaces.map { it.qualifiedName.joinToString(".") }.toSet())
        assertEquals(2, snapshot.sourceUnits.size)
        assertEquals(
            listOf("com.controls", "java.lang.String"),
            snapshot.sourceUnits
                .single { it.packageKey == fixture.rootKey }
                .authoredImports
                .map { it.target.parts.joinToString(".") },
        )
    }

    @Test
    fun `reports invalid publication unknown and duplicate sources without fallback admission`() {
        val fixture = governedFixture()
        val builder = GovernedProjectSemanticGraphBuilder()
        val invalidPublication = fixture.publication.copy(
            lockPresent = false,
            actualLock = null,
            lockState = AthenaRepositoryReportLockState.MISSING,
        )

        val invalid = builder.build(invalidPublication, fixture.sources)

        assertEquals(null, invalid.snapshot)
        assertTrue(invalid.diagnostics.any { it.code.value == "semantic.repository.publication.invalid" })

        val unknownPackage = PackageIdentifier("com.unknown", "1")
        val unknownSource = ProjectSemanticSourceInput(
            unknownPackage,
            "unknown.athena",
            "package com.unknown\nsystem Unknown {}",
        )
        val changedDuplicate = fixture.sources.first().copy(
            sourceContent = "package com.root\nsystem ChangedRoot {}",
        )
        val admitted = builder.build(
            fixture.publication,
            fixture.sources + changedDuplicate + unknownSource,
        )
        val reversed = builder.build(fixture.publication, (fixture.sources + changedDuplicate + unknownSource).reversed())
        val admittedSnapshot = assertNotNull(admitted.snapshot)
        val reversedSnapshot = assertNotNull(reversed.snapshot)

        assertEquals(2, admittedSnapshot.sourceUnits.size)
        assertEquals(admittedSnapshot.graphId, reversedSnapshot.graphId)
        assertEquals(admittedSnapshot.sourceUnits, reversedSnapshot.sourceUnits)
        assertTrue(admitted.diagnostics.any { it.code.value == "semantic.source.duplicate" })
        assertTrue(admitted.diagnostics.any { it.code.value == "semantic.source.package.not-admitted" })
    }

    @Test
    fun `reports package declaration and syntax failures while preserving valid unresolved imports`() {
        val fixture = governedFixture()
        val builder = GovernedProjectSemanticGraphBuilder()
        val rootId = fixture.rootId
        val inputs = listOf(
            ProjectSemanticSourceInput(rootId, "missing-package.athena", "system MissingPackage {}"),
            ProjectSemanticSourceInput(rootId, "wrong-package.athena", "package com.wrong\nsystem WrongPackage {}"),
            ProjectSemanticSourceInput(rootId, "raw-path.athena", "package com.root\nimport ./other\nsystem RawPath {}"),
            ProjectSemanticSourceInput(rootId, "frontend.athena", "package com.root\nimport @controls/PLC\nsystem Frontend {}"),
            ProjectSemanticSourceInput(rootId, "valid-import.athena", "package com.root\nimport java.lang.String\nsystem ValidImport {}"),
        )

        val result = builder.build(fixture.publication, inputs)

        val snapshot = assertNotNull(result.snapshot)
        assertEquals(5, snapshot.sourceUnits.size)
        assertTrue(result.diagnostics.any { it.code.value == "semantic.source.package.missing" })
        assertTrue(result.diagnostics.any { it.code.value == "semantic.source.package.mismatch" })
        assertEquals(2, result.diagnostics.count { it.code.value == "semantic.source.syntax.invalid" })
        assertEquals(1, snapshot.namespaces.size)
        assertEquals(
            listOf("valid-import.athena"),
            snapshot.namespaces.single().sourceUnitIds.map { sourceUnitId ->
                snapshot.sourceUnits.single { it.sourceUnitId == sourceUnitId }.sourceRootRelativePath
            },
        )
        assertEquals(
            setOf("raw-path.athena", "frontend.athena"),
            result.diagnostics
                .filter { it.code.value == "semantic.source.syntax.invalid" }
                .map { diagnostic -> snapshot.sourceUnits.single { it.sourceUnitId == diagnostic.sourceUnitId }.sourceRootRelativePath }
                .toSet(),
        )
        assertEquals(
            listOf("java.lang.String"),
            snapshot.sourceUnits
                .single { it.sourceRootRelativePath == "valid-import.athena" }
                .authoredImports
                .map { it.target.parts.joinToString(".") },
        )
    }

    @Test
    fun `rejects nonportable resolved package source roots before graph identity construction`() {
        val fixture = governedFixture()
        val packages = fixture.publication.graph!!.packages.map { resolvedPackage ->
            if (resolvedPackage.packageId == fixture.rootId) resolvedPackage.copy(sourceRoot = "C:/external/src") else resolvedPackage
        }
        val publication = fixture.publication.copy(
            graph = fixture.publication.graph.copy(packages = packages),
            expectedLock = fixture.publication.expectedLock!!.copy(packages = packages),
            actualLock = fixture.publication.actualLock!!.copy(packages = packages),
        )

        val result = GovernedProjectSemanticGraphBuilder().build(publication, emptyList())

        assertEquals(null, result.snapshot)
        assertTrue(result.diagnostics.any { it.code.value == "semantic.package.source-root.nonportable" })
    }

    @Test
    fun `requires exact package identifiers and reports malformed graph identities separately`() {
        val fixture = governedFixture()
        val governedId = PackageIdentifier("com.root", "a|b")
        val governedPackage = ResolvedPackage(governedId, "src")
        val lock = RepositoryLock(primaryPackage = governedId, packages = listOf(governedPackage))
        val collisionPublication = fixture.publication.copy(
            repository = EngineeringRepository(RepositoryManifest(PrimaryPackage(governedId)), lock),
            resolutionInput = RepositoryResolutionInput(governedId, "src"),
            graph = ResolvedPackageGraph(governedId, listOf(governedPackage)),
            expectedLock = lock,
            actualLock = lock,
        )
        val collidingSourceId = PackageIdentifier("com.root|a", "b")

        val collision = GovernedProjectSemanticGraphBuilder().build(
            collisionPublication,
            listOf(ProjectSemanticSourceInput(collidingSourceId, "collision.athena", "system Collision {}")),
        )

        assertEquals(0, assertNotNull(collision.snapshot).sourceUnits.size)
        assertTrue(collision.diagnostics.any { it.code.value == "semantic.source.package.not-admitted" })

        val invalidId = PackageIdentifier("", "1")
        val invalidPackage = ResolvedPackage(invalidId, "src")
        val invalidLock = RepositoryLock(primaryPackage = invalidId, packages = listOf(invalidPackage))
        val invalidPublication = fixture.publication.copy(
            repository = EngineeringRepository(RepositoryManifest(PrimaryPackage(invalidId)), invalidLock),
            resolutionInput = RepositoryResolutionInput(invalidId, "src"),
            graph = ResolvedPackageGraph(invalidId, listOf(invalidPackage)),
            expectedLock = invalidLock,
            actualLock = invalidLock,
        )
        val malformed = GovernedProjectSemanticGraphBuilder().build(invalidPublication, emptyList())

        assertEquals(null, malformed.snapshot)
        assertTrue(malformed.diagnostics.any { it.code.value == "semantic.repository.graph.invalid" })
        assertTrue(malformed.diagnostics.none { it.code.value == "semantic.package.source-root.nonportable" })
    }

    @Test
    fun `rejected graph diagnostics are deterministic across source input order`() {
        val fixture = governedFixture()
        val missingId = PackageIdentifier("com.missing", "1")
        val packages = fixture.publication.graph!!.packages.map { resolvedPackage ->
            if (resolvedPackage.packageId == fixture.rootId) resolvedPackage.copy(directDependencies = listOf(missingId)) else resolvedPackage
        }
        val publication = fixture.publication.copy(
            graph = fixture.publication.graph.copy(packages = packages),
            expectedLock = fixture.publication.expectedLock!!.copy(packages = packages),
            actualLock = fixture.publication.actualLock!!.copy(packages = packages),
        )
        val invalidSources = listOf(
            ProjectSemanticSourceInput(fixture.rootId, "a.athena", "package com.root\nimport ./a\nsystem A {}"),
            ProjectSemanticSourceInput(fixture.rootId, "b.athena", "package com.root\nimport ./b\nsystem B {}"),
        )

        val first = GovernedProjectSemanticGraphBuilder().build(publication, invalidSources)
        val second = GovernedProjectSemanticGraphBuilder().build(publication, invalidSources.reversed())

        assertEquals(null, first.snapshot)
        assertEquals(first.diagnostics, second.diagnostics)
    }

    private fun governedFixture(): GovernedFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val dependencyId = PackageIdentifier("com.controls", "2")
        val rootPackage = ResolvedPackage(rootId, "src", listOf(dependencyId))
        val dependencyPackage = ResolvedPackage(dependencyId, "packages/controls/src")
        val graph = ResolvedPackageGraph(rootId, listOf(rootPackage, dependencyPackage))
        val lock = RepositoryLock(primaryPackage = rootId, packages = graph.packages)
        val repository = EngineeringRepository(
            RepositoryManifest(
                PrimaryPackage(rootId),
                listOf(PackageDependency(dependencyId, locator = "packages/controls")),
            ),
            lock,
        )
        val publication = AthenaRepositoryReportPublicationResult(
            repositoryRoot = Path.of("repo"),
            manifestPath = Path.of("repo/athena.yaml"),
            lockPath = Path.of("repo/athena.lock"),
            manifestPresent = true,
            lockPresent = true,
            repository = repository,
            resolutionInput = RepositoryResolutionInput(rootId, "src"),
            graph = graph,
            expectedLock = lock,
            actualLock = lock,
            lockState = AthenaRepositoryReportLockState.CURRENT,
        )
        val sources = listOf(
            ProjectSemanticSourceInput(
                rootId,
                "main.athena",
                "package com.root\nimport java.lang.String\nimport com.controls\nsystem Root {}",
            ),
            ProjectSemanticSourceInput(
                dependencyId,
                "controls.athena",
                "package com.controls\nsystem Controls {}",
            ),
        )
        return GovernedFixture(
            rootId,
            CanonicalSemanticIdentityBuilder.packageKey(rootId),
            publication,
            sources,
        )
    }
}

private data class GovernedFixture(
    val rootId: PackageIdentifier,
    val rootKey: PackageKey,
    val publication: AthenaRepositoryReportPublicationResult,
    val sources: List<ProjectSemanticSourceInput>,
)
