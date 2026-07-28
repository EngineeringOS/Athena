package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM35DedicatedCabinetSampleTest {
    @Test
    fun `m35 dedicated cabinet sample is governed package backed and compiles offline`() {
        val repoRoot = resolveRepoRoot()
        val sampleRoot = repoRoot.resolve("examples/m35/physical-installation-cabinet")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m35/physicalinstallationcabinet/01-physical-installation-cabinet.athena",
        )
        val standardPackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m35/standard/iec/standard-elements.athena",
        )
        val vendorPackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m35/vendor/abb/pfea112/vendor-elements.athena",
        )
        val vendorSvg = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m35/vendor/abb/pfea112/pfea112.svg",
        )

        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")), "M35 Cabinet sample must be IDE-openable.")
        assertTrue(Files.exists(sampleRoot.resolve("athena.lock")), "M35 Cabinet sample must carry RepositoryLockV2 evidence.")
        assertTrue(Files.exists(source), "M35 Cabinet source must follow package/path hierarchy.")
        assertTrue(Files.exists(standardPackage), "M35 Cabinet sample must include a standard package.")
        assertTrue(Files.exists(vendorPackage), "M35 Cabinet sample must include a vendor package.")
        assertTrue(Files.exists(vendorSvg), "M35 Cabinet sample must include the vendor SVG geometry resource.")

        val compiler = AthenaCompiler()
        val contractValidation = compiler.validateRepositoryContract(sampleRoot)
        assertTrue(
            contractValidation.isValid,
            contractValidation.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )

        val compilation = compiler.compile(source)
        when (compilation) {
            is CompilerCompilationSuccess -> {
                assertTrue(
                    compilation.semanticResult.diagnostics.isEmpty(),
                    compilation.semanticResult.diagnostics.joinToString("\n") { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                )
                val rendering = assertIs<CompilerRenderingSuccess>(compilation.rendering)
                assertEquals("cabinet", rendering.viewId)
                assertEquals("svg", rendering.rendererTarget)
                assertTrue(rendering.model.connections.isNotEmpty(), "M35 Cabinet render must contain connection output.")
                assertTrue(rendering.svg.contains("""class="connection""""))
                assertTrue(rendering.svg.contains("""class="component""""))
                assertTrue(rendering.svg.contains("""class="label""""))
            }

            is CompilerCompilationParseFailure -> fail(
                compilation.diagnostics.joinToString("\n") { diagnostic ->
                    "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
                },
            )
        }

        val sourceText = source.readText()
        val standardText = standardPackage.readText()
        val vendorText = vendorPackage.readText()
        val allSampleText = Files.walk(sampleRoot).use { stream ->
            stream
                .filter(Files::isRegularFile)
                .filter { path -> path.fileName.toString() != "pfea112.svg" }
                .map { path -> path.readText() }
                .toList()
                .joinToString("\n")
        }

        assertTrue(sourceText.contains("installation cabinet MainCabinet"))
        assertTrue(sourceText.contains("surface Backplate in ENC35"))
        assertTrue(sourceText.contains("rail DIN35A on Backplate"))
        assertTrue(sourceText.contains("rail DIN35B on Backplate"))
        assertTrue(sourceText.contains("duct WireDuctLeft in ENC35"))
        assertTrue(sourceText.contains("channel LeftControlChannel in WireDuctLeft"))
        assertTrue(sourceText.contains("terminal-group XT35 in ENC35"))
        assertTrue(sourceText.contains("mount PFEA112Mount device PFEA112 on DIN35A"))
        assertTrue(sourceText.contains("route supply_to_breaker through [LeftControlChannel]"))
        assertTrue(sourceText.contains("connect power"))
        assertTrue(sourceText.contains("supply_to_breaker Supply35.lplus -> BreakerQF35.line"))

        assertTrue(standardText.contains("identity \"iec.protective-earth.element\""))
        assertTrue(vendorText.contains("identity \"vendor.abb.pfea112.element\""))
        assertTrue(vendorText.contains("graphic svg resource pfea112_svg"))
        assertFalse(vendorText.contains("manufacturer:"), "Visual package must not become product identity authority.")

        assertTrue(allSampleText.contains("m35.productSurface cabinet-only"))
        assertFalse(allSampleText.contains("<definition"), "M35 sample must not carry QET XML authority.")
        assertFalse(allSampleText.contains("Documentation"), "M35 sample must not advertise Documentation surface.")
        assertFalse(allSampleText.contains("Schematic"), "M35 sample must not advertise Schematic surface.")
        assertEquals(
            emptyList(),
            listOf("fallback card", "placeholder authoring", "raw-markup authority").filter(allSampleText::contains),
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
