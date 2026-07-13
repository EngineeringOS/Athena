package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaActiveKnowledgePackSet
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactKind
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackRegistry
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackRegistryEntry
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaKnowledgeResolutionModelContractTest {
    @Test
    fun `registry canonicalizes entries by package and artifact identity`() {
        val rootPackage = PackageIdentifier(name = "athena.root", version = "0.0.1")
        val registry = AthenaKnowledgePackRegistry.canonical(
            repositoryRootPackage = rootPackage,
            entries = listOf(
                entry("pkg-b", "0.0.1", "knowledge.b", "0.0.2", "D:/packs/pkg-b"),
                entry("pkg-a", "0.0.2", "knowledge.c", "0.0.1", "D:/packs/pkg-a-c"),
                entry("pkg-a", "0.0.1", "knowledge.a", "0.0.3", "D:/packs/pkg-a-a"),
            ),
        )

        assertEquals(
            listOf(
                "pkg-a:0.0.1:knowledge.a:0.0.3",
                "pkg-a:0.0.2:knowledge.c:0.0.1",
                "pkg-b:0.0.1:knowledge.b:0.0.2",
            ),
            registry.entries.map { entry ->
                "${entry.packageId.name}:${entry.packageId.version}:${entry.artifactId}:${entry.artifactVersion}"
            },
        )
    }

    @Test
    fun `active pack set drives package roots from explicit package governed entries`() {
        val rootPackage = PackageIdentifier(name = "athena.root", version = "0.0.1")
        val activePackSet = AthenaActiveKnowledgePackSet.canonical(
            repositoryRootPackage = rootPackage,
            entries = listOf(
                entry("pkg-z", "0.0.1", "knowledge.z", "0.0.1", "D:/packs/pkg-z"),
                entry("pkg-a", "0.0.1", "knowledge.a", "0.0.1", "D:/packs/pkg-a"),
            ),
        )
        val source = AthenaKnowledgePackageSource.fromActivePackSet(activePackSet)

        assertEquals(
            listOf("pkg-a", "pkg-z"),
            source.activePackSet!!.entries.map { entry -> entry.packageId.name },
        )
        assertEquals(
            listOf(
                Path.of("D:/packs/pkg-a"),
                Path.of("D:/packs/pkg-z"),
            ),
            source.packageRoots,
        )
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
