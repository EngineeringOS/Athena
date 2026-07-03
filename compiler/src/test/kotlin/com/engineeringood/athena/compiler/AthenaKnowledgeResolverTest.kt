package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactKind
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeResolver
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaKnowledgeResolverTest {
    @Test
    fun `resolves valid governed knowledge packages into deterministic active context order`() {
        val repoRoot = resolveRepoRoot()
        val source = AthenaKnowledgePackageSource(
            packageRoots = listOf(
                repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-rule"),
                repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-ontology"),
                repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-standards-mapping"),
            ),
        )
        val resolver = AthenaKnowledgeResolver()

        val first = resolver.resolve(source)
        val second = resolver.resolve(source)

        assertEquals(first, second)
        assertEquals(
            listOf(
                "com.engineeringood.athena.knowledge.mapping.automationml",
                "com.engineeringood.athena.knowledge.ontology.core",
                "com.engineeringood.athena.knowledge.rule.connection-safety",
            ),
            first.activeArtifacts.map { it.artifactId },
        )
        assertEquals(
            listOf(
                AthenaKnowledgeArtifactKind.STANDARDS_MAPPING,
                AthenaKnowledgeArtifactKind.ONTOLOGY,
                AthenaKnowledgeArtifactKind.RULE,
            ),
            first.activeArtifacts.map { it.artifactKind },
        )
        assertTrue(first.rejectedPackages.isEmpty())
    }

    @Test
    fun `rejects invalid or incompatible governed knowledge packages before they become active`() {
        val repoRoot = resolveRepoRoot()
        val source = AthenaKnowledgePackageSource(
            packageRoots = listOf(
                repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-rule"),
                repoRoot.resolve("compiler/src/test/resources/knowledge-packages/incompatible-core"),
                repoRoot.resolve("compiler/src/test/resources/knowledge-packages/malformed-manifest"),
            ),
        )

        val result = AthenaKnowledgeResolver().resolve(source)

        assertEquals(
            listOf("com.engineeringood.athena.knowledge.rule.connection-safety"),
            result.activeArtifacts.map { it.artifactId },
        )
        assertEquals(
            listOf(
                "knowledge.package.compatibility.unsupported-core",
                "knowledge.package.manifest.id.blank",
                "knowledge.package.manifest.version.blank",
                "knowledge.package.manifest.provenance.sources.missing",
                "knowledge.package.manifest.provenance.reviewed-by.blank",
                "knowledge.package.manifest.payload.missing",
            ),
            result.rejectedPackages.flatMap { rejected -> rejected.diagnostics.map { it.ruleId.value } },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.knowledge.rule.future-only",
                null,
            ),
            result.rejectedPackages.map { it.artifactId },
        )
    }

    @Test
    fun `deduplicates repeated governed knowledge package roots before activation`() {
        val repoRoot = resolveRepoRoot()
        val duplicatedRulePath = repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-rule")
        val source = AthenaKnowledgePackageSource(
            packageRoots = listOf(
                duplicatedRulePath,
                duplicatedRulePath,
                repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-ontology"),
            ),
        )

        val result = AthenaKnowledgeResolver().resolve(source)

        assertEquals(
            listOf(
                "com.engineeringood.athena.knowledge.ontology.core",
                "com.engineeringood.athena.knowledge.rule.connection-safety",
            ),
            result.activeArtifacts.map { it.artifactId },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.knowledge.ontology.core",
                "com.engineeringood.athena.knowledge.rule.connection-safety",
            ),
            result.candidates.map { it.artifactPackage.manifest.artifactId },
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
