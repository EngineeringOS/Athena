package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactKind
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaKnowledgePackageLoaderTest {
    @Test
    fun `loads valid ontology mapping and rule packages deterministically`() {
        val loader = AthenaKnowledgePackageLoader()
        val repoRoot = resolveRepoRoot()
        val ontologyPath = repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-ontology")
        val mappingPath = repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-standards-mapping")
        val rulePath = repoRoot.resolve("compiler/src/test/resources/knowledge-packages/valid-rule")

        val ontologyResult = loader.load(ontologyPath)
        val mappingResult = loader.load(mappingPath)
        val ruleResult = loader.load(rulePath)
        val ontologyResultRepeat = loader.load(ontologyPath)

        assertTrue(ontologyResult.isValid)
        assertTrue(mappingResult.isValid)
        assertTrue(ruleResult.isValid)
        assertEquals(ontologyResult, ontologyResultRepeat)

        assertEquals(
            listOf(
                AthenaKnowledgeArtifactKind.ONTOLOGY,
                AthenaKnowledgeArtifactKind.STANDARDS_MAPPING,
                AthenaKnowledgeArtifactKind.RULE,
            ),
            listOf(
                assertNotNull(ontologyResult.loadedPackage).manifest.artifactKind,
                assertNotNull(mappingResult.loadedPackage).manifest.artifactKind,
                assertNotNull(ruleResult.loadedPackage).manifest.artifactKind,
            ),
        )
        assertEquals(
            listOf(
                "ontology",
                "mapping",
                "rule",
            ),
            assertNotNull(ontologyResult.loadedPackage).payloadEntries.map { it.entryId } +
                assertNotNull(mappingResult.loadedPackage).payloadEntries.map { it.entryId } +
                assertNotNull(ruleResult.loadedPackage).payloadEntries.map { it.entryId },
        )
        assertEquals(
            listOf(
                "payload/base-entities.txt",
                "payload/automationml-map.txt",
                "payload/connection-safety-rule.txt",
            ),
            listOf(
                assertNotNull(ontologyResult.loadedPackage).payloadEntries.single().relativePath,
                assertNotNull(mappingResult.loadedPackage).payloadEntries.single().relativePath,
                assertNotNull(ruleResult.loadedPackage).payloadEntries.single().relativePath,
            ),
        )
    }

    @Test
    fun `rejects malformed manifests with stable diagnostics and inspectable subjects`() {
        val malformedPath = resolveRepoRoot().resolve("compiler/src/test/resources/knowledge-packages/malformed-manifest")

        val result = AthenaKnowledgePackageLoader().load(malformedPath)

        assertTrue(!result.isValid)
        assertEquals(
            listOf(
                "knowledge.package.manifest.id.blank",
                "knowledge.package.manifest.version.blank",
                "knowledge.package.manifest.provenance.sources.missing",
                "knowledge.package.manifest.provenance.reviewed-by.blank",
                "knowledge.package.manifest.payload.missing",
            ),
            result.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf(
                "artifact.id",
                "artifact.version",
                "provenance.sources",
                "provenance.reviewedBy",
                "payload",
            ),
            result.diagnostics.map { it.subject },
        )
    }

    @Test
    fun `rejects missing payload files deterministically`() {
        val missingPayloadPath = resolveRepoRoot().resolve("compiler/src/test/resources/knowledge-packages/missing-payload")

        val first = AthenaKnowledgePackageLoader().load(missingPayloadPath)
        val second = AthenaKnowledgePackageLoader().load(missingPayloadPath)

        assertEquals(first, second)
        assertTrue(!first.isValid)
        assertEquals(
            listOf("knowledge.package.payload.path.missing"),
            first.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf("payload.mapping.path"),
            first.diagnostics.map { it.subject },
        )
    }

    @Test
    fun `rejects payload paths that escape the package root`() {
        val packageRoot = Files.createTempDirectory("athena-knowledge-outside-root-")
        val externalPayload = packageRoot.parent.resolve("athena-knowledge-external-${packageRoot.fileName}.txt")
        Files.writeString(externalPayload, "external")
        Files.writeString(
            packageRoot.resolve("athena-knowledge.properties"),
            """
                artifact.id=com.engineeringood.athena.knowledge.escape
                artifact.kind=RULE
                package.format.version=1
                artifact.version=0.1.0
                provenance.sources=manifesto/docs/rfc/RFC-0008-knowledge.md
                provenance.reviewedBy=Athena Rule Maintainers
                compatibility.core.minimum=0.0.1-SNAPSHOT
                payload.rule.kind=rule
                payload.rule.path=../${externalPayload.fileName}
            """.trimIndent(),
        )

        try {
            val result = AthenaKnowledgePackageLoader().load(packageRoot)

            assertTrue(!result.isValid)
            assertEquals(
                listOf("knowledge.package.payload.path.outside-root"),
                result.diagnostics.map { it.ruleId.value },
            )
            assertEquals(
                listOf("payload.rule.path"),
                result.diagnostics.map { it.subject },
            )
        } finally {
            Files.deleteIfExists(packageRoot.resolve("athena-knowledge.properties"))
            Files.deleteIfExists(packageRoot)
            Files.deleteIfExists(externalPayload)
        }
    }

    @Test
    fun `rejects unsupported artifact kinds and invalid compatibility ranges before operational use`() {
        val repoRoot = resolveRepoRoot()
        val invalidKindPath = repoRoot.resolve("compiler/src/test/resources/knowledge-packages/invalid-kind")
        val invalidCompatibilityPath = repoRoot.resolve("compiler/src/test/resources/knowledge-packages/invalid-compatibility")

        val invalidKind = AthenaKnowledgePackageLoader().load(invalidKindPath)
        val invalidCompatibility = AthenaKnowledgePackageLoader().load(invalidCompatibilityPath)

        assertTrue(!invalidKind.isValid)
        assertTrue(!invalidCompatibility.isValid)
        assertEquals(
            listOf("knowledge.package.manifest.kind.unsupported"),
            invalidKind.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf("artifact.kind"),
            invalidKind.diagnostics.map { it.subject },
        )
        assertEquals(
            listOf("knowledge.package.manifest.compatibility.minimum.invalid"),
            invalidCompatibility.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf("compatibility.core.minimum"),
            invalidCompatibility.diagnostics.map { it.subject },
        )
    }

    @Test
    fun `does not treat authored dsl files or plugin-like layouts as governed knowledge packages`() {
        val repoRoot = resolveRepoRoot()
        val dslPath = repoRoot.resolve("examples/m0/demo-cabinet.athena")
        val pluginLikePath = repoRoot.resolve("compiler/src/test/resources/knowledge-packages/plugin-like-layout")

        val dslResult = AthenaKnowledgePackageLoader().load(dslPath)
        val pluginLikeResult = AthenaKnowledgePackageLoader().load(pluginLikePath)

        assertTrue(!dslResult.isValid)
        assertTrue(!pluginLikeResult.isValid)
        assertEquals(
            listOf("knowledge.package.root.not-directory"),
            dslResult.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf("knowledge.package.manifest.missing"),
            pluginLikeResult.diagnostics.map { it.ruleId.value },
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
