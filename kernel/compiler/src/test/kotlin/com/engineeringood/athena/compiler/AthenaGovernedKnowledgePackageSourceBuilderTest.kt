package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaGovernedKnowledgePackageSourceBuilder
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactKind
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackRegistry
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackRegistryEntry
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.ResolvedPackage
import com.engineeringood.athena.repository.ResolvedPackageGraph
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AthenaGovernedKnowledgePackageSourceBuilderTest {
    @Test
    fun `build selects only packs admitted by the resolved package graph`() {
        val rootPackage = PackageIdentifier("athena.root", "0.0.1")
        val graph = ResolvedPackageGraph(
            rootPackage = rootPackage,
            packages = listOf(
                ResolvedPackage(packageId = rootPackage, sourceRoot = "src"),
                ResolvedPackage(packageId = PackageIdentifier("pkg-a", "0.0.1"), sourceRoot = "packages/pkg-a/src"),
                ResolvedPackage(packageId = PackageIdentifier("pkg-b", "0.0.1"), sourceRoot = "packages/pkg-b/src"),
            ),
        )
        val registry = AthenaKnowledgePackRegistry.canonical(
            repositoryRootPackage = rootPackage,
            entries = listOf(
                entry("pkg-z", "0.0.1", "knowledge.z", "0.0.1", "D:/packs/pkg-z"),
                entry("pkg-a", "0.0.1", "knowledge.a", "0.0.1", "D:/packs/pkg-a"),
                entry("pkg-b", "0.0.1", "knowledge.b", "0.0.1", "D:/packs/pkg-b"),
            ),
        )

        val source = AthenaGovernedKnowledgePackageSourceBuilder().build(graph, registry)

        assertEquals(
            listOf("pkg-a", "pkg-b"),
            source.activePackSet!!.entries.map { entry -> entry.packageId.name },
        )
        assertEquals(
            listOf(Path.of("D:/packs/pkg-a"), Path.of("D:/packs/pkg-b")),
            source.packageRoots,
        )
    }

    @Test
    fun `build returns empty source when registry root does not match graph root`() {
        val graph = ResolvedPackageGraph(
            rootPackage = PackageIdentifier("athena.root", "0.0.1"),
            packages = listOf(
                ResolvedPackage(packageId = PackageIdentifier("athena.root", "0.0.1"), sourceRoot = "src"),
            ),
        )
        val registry = AthenaKnowledgePackRegistry.canonical(
            repositoryRootPackage = PackageIdentifier("other.root", "0.0.1"),
            entries = listOf(
                entry("athena.root", "0.0.1", "knowledge.a", "0.0.1", "D:/packs/root"),
            ),
        )

        val source = AthenaGovernedKnowledgePackageSourceBuilder().build(graph, registry)

        assertEquals(emptyList(), source.packageRoots)
        assertNull(source.activePackSet)
    }

    private fun entry(
        packageName: String,
        packageVersion: String,
        artifactId: String,
        artifactVersion: String,
        packageRoot: String,
    ): AthenaKnowledgePackRegistryEntry {
        return AthenaKnowledgePackRegistryEntry(
            packageId = PackageIdentifier(name = packageName, version = packageVersion),
            packageRoot = Path.of(packageRoot),
            artifactId = artifactId,
            artifactKind = AthenaKnowledgeArtifactKind.KNOWLEDGE_PACK,
            artifactVersion = artifactVersion,
        )
    }
}
