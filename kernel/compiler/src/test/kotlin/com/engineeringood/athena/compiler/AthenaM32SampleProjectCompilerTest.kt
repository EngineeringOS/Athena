package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionNodeId
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM32SampleProjectCompilerTest {
    @Test
    fun `m32 package platform sample is an ide openable athena repository with compilable source`() {
        val compiler = AthenaCompiler()
        val sampleRoot = resolveRepoRoot().resolve("examples/m32/sample-project")
        val source = sampleRoot.resolve("src/01-package-platform-demo.athena")

        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")), "M32 sample must include athena.yaml for LSP repository initialization.")
        assertTrue(Files.exists(sampleRoot.resolve("athena.lock")), "M32 sample must include athena.lock for governed repository validation.")
        assertTrue(Files.exists(source), "Missing M32 sample source: $source")

        val contractValidation = compiler.validateRepositoryContract(sampleRoot)
        assertTrue(
            contractValidation.isValid,
            contractValidation.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.code}: ${diagnostic.message}"
            },
        )

        val compilation = compiler.compile(source)
        when (compilation) {
            is CompilerCompilationSuccess -> assertTrue(
                compilation.semanticResult.diagnostics.isEmpty(),
                compilation.semanticResult.diagnostics.joinToString(separator = "\n") { diagnostic ->
                    "${diagnostic.ruleId.value}: ${diagnostic.message}"
                },
            )

            is CompilerCompilationParseFailure -> fail(
                compilation.diagnostics.joinToString(
                    separator = "\n",
                    prefix = "M32 sample syntax diagnostics:\n",
                ) { diagnostic ->
                    "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
                },
            )
        }
    }

    @Test
    fun `m32 documentation presentation uses package backed representation evidence as live authority`() {
        val compiler = AthenaCompiler()
        val sampleRoot = resolveRepoRoot().resolve("examples/m32/sample-project")
        val source = sampleRoot.resolve("src/01-package-platform-demo.athena")

        val compilation = compiler.compile(source)
        val success = compilation as? CompilerCompilationSuccess ?: fail("M32 sample must compile successfully.")
        val documentationPresentation = assertNotNull(
            success.presentations.firstOrNull { presentation -> presentation.view.id == "documentation" },
            "M32 sample must expose the documentation presentation used by the live Graph View.",
        )
        val representationFacts = documentationPresentation.representationFacts
        assertTrue(representationFacts.isNotEmpty(), "M32 documentation presentation must expose live representation facts.")
        assertFalse(
            representationFacts.any { fact -> fact.anatomy.representationId.value.startsWith("athena-industrial-control-v0:") },
            "M32 documentation presentation must not accept native athena-industrial-control-v0 facts as package success.",
        )

        val packageEvidenceBySubject = representationFacts.associateBy { fact -> fact.subjectId.value }
        val powerSupply = assertNotNull(packageEvidenceBySubject["component:MainPowerSupplyPS32"]?.packageEvidence)
        assertEquals("com.athena.sample.engineering.power.supply-24v", powerSupply.engineeringPackageId)
        assertEquals("m32-iec", powerSupply.presentationProfileId)
        assertEquals("binding.m32.power-supply-24v", powerSupply.bindingManifestId)
        assertEquals("com.athena.sample.representation.power.supply.iec", powerSupply.representationPackageId)
        assertEquals("descriptor.power-supply.iec", powerSupply.descriptorId)
        assertEquals("resource.power-supply.iec", powerSupply.graphicResourceId)
        assertEquals(listOf("port:MainPowerSupplyPS32.lplus=lplus"), powerSupply.anchorMapSummary)
        assertEquals(listOf("device-tag=PS32"), powerSupply.labelBindingSummary)
        assertFalse(powerSupply.rendererFallbackAccepted)
    }

    @Test
    fun `m32 cabinet presentation uses package backed representation evidence as live authority`() {
        val compiler = AthenaCompiler()
        val sampleRoot = resolveRepoRoot().resolve("examples/m32/sample-project")
        val source = sampleRoot.resolve("src/01-package-platform-demo.athena")

        val compilation = compiler.compile(source)
        val success = compilation as? CompilerCompilationSuccess ?: fail("M32 sample must compile successfully.")
        val cabinetPresentation = assertNotNull(
            success.presentations.firstOrNull { presentation -> presentation.view.id == "cabinet" },
            "M32 sample must expose the Cabinet presentation used by the primary Graph View.",
        )
        val representationFacts = cabinetPresentation.representationFacts
        assertTrue(representationFacts.isNotEmpty(), "M32 Cabinet presentation must expose live representation facts.")
        assertFalse(
            representationFacts.any { fact -> fact.anatomy.representationId.value.startsWith("athena-industrial-control-v0:") },
            "M32 Cabinet presentation must not accept native athena-industrial-control-v0 facts as package success.",
        )
        assertTrue(
            representationFacts.all { fact -> fact.packageEvidence != null },
            "Every M32 Cabinet representation must retain package binding evidence.",
        )
    }

    @Test
    fun `m32 package representation preserves every projection occurrence id for one semantic subject`() {
        val compiler = AthenaCompiler()
        val sampleRoot = resolveRepoRoot().resolve("examples/m32/sample-project")
        val source = sampleRoot.resolve("src/01-package-platform-demo.athena")
        val success = compiler.compile(source) as? CompilerCompilationSuccess
            ?: fail("M32 sample must compile successfully.")
        val cabinetProjection = success.projections.first { projection -> projection.view.id == "cabinet" }
        val originalNode = cabinetProjection.nodes.first { node ->
            node.semanticId.value == "component:MainPowerSupplyPS32"
        }
        val repeatedProjectionId = ProjectionNodeId("${originalNode.projectionId.value}:repeat")
        val repeatedProjection = cabinetProjection.copy(
            nodes = cabinetProjection.nodes + originalNode.copy(projectionId = repeatedProjectionId),
        )

        val fact = assertNotNull(
            M32PackageBackedPresentationFactDeriver()
                .derive(repeatedProjection)
                .firstOrNull { candidate -> candidate.subjectId.value == originalNode.semanticId.value },
        )

        assertEquals(
            listOf(originalNode.projectionId.value, repeatedProjectionId.value).sorted(),
            fact.sourceProjectionIds,
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts")) && Files.exists(current.resolve("examples"))) {
                return current
            }
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}
