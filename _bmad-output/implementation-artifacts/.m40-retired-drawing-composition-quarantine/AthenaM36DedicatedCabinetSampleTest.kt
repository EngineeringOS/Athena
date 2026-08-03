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

class AthenaM36DedicatedCabinetSampleTest {
    @Test
    fun `m36 dedicated cabinet sample is governed package backed and compiles offline`() {
        val repoRoot = resolveRepoRoot()
        val sampleRoot = repoRoot.resolve("examples/m36/connectivity-cabinet")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena",
        )
        val standardPackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m36/standard/iec/standard-elements.athena",
        )
        val vendorPackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m36/vendor/abb/pfea112/vendor-elements.athena",
        )
        val vendorSvg = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m36/vendor/abb/pfea112/pfea112.svg",
        )

        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")), "M36 Cabinet sample must be IDE-openable.")
        assertTrue(Files.exists(sampleRoot.resolve("athena.lock")), "M36 Cabinet sample must carry RepositoryLockV2 evidence.")
        assertTrue(Files.exists(source), "M36 Cabinet source must follow package/path hierarchy.")
        assertTrue(Files.exists(standardPackage), "M36 Cabinet sample must include a standard package.")
        assertTrue(Files.exists(vendorPackage), "M36 Cabinet sample must include a vendor package.")
        assertTrue(Files.exists(vendorSvg), "M36 Cabinet sample must include the vendor SVG geometry resource.")

        val compiler = AthenaCompiler()
        val contractValidation = compiler.validateRepositoryContract(sampleRoot)
        assertTrue(
            contractValidation.isValid,
            contractValidation.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )
        val lockValidation = compiler.validateRepositoryLock(sampleRoot)
        assertTrue(
            lockValidation.isValid,
            buildString {
                appendLine(lockValidation.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" })
                append(lockValidation.renderedExpectedLock.orEmpty())
            },
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
                assertTrue(rendering.model.connections.isNotEmpty(), "M36 Cabinet render must contain connection output.")
                assertTrue(rendering.svg.contains("""class="connection""""))
                assertTrue(rendering.svg.contains("""class="component""""))
                assertTrue(rendering.svg.contains("""class="label""""))
                val cabinetCompilation = AthenaCabinetProjectionCompiler().compile(
                    repositoryRoot = sampleRoot,
                    sourcePath = source,
                    success = compilation,
                )
                assertTrue(
                    cabinetCompilation.diagnostics.isEmpty(),
                    cabinetCompilation.diagnostics.joinToString("\n") { diagnostic ->
                        "${diagnostic.code}: ${diagnostic.subject}: ${diagnostic.message}"
                    },
                )
                val cabinet = requireNotNull(cabinetCompilation.presentation)
                val supply = cabinet.graphicOccurrences.single { occurrence ->
                    occurrence.semanticSubjectId == "component:Supply35"
                }
                assertEquals(100, supply.bounds.width, "Physical footprint must own occurrence width.")
                assertEquals(80, supply.bounds.height, "Physical footprint must own occurrence height.")
                assertEquals(compilation.connectionIr?.connections?.size, cabinet.connectors.size)
                cabinet.connectors.forEach { connector ->
                    assertTrue(connector.routeId.isNotBlank())
                    assertFalse(connector.routeId.contains("intent", ignoreCase = true))
                    assertTrue(connector.bundleId.isNotBlank())
                    assertTrue(connector.selectedChannelIds.isNotEmpty())
                    assertTrue(connector.line.compilerSnapshotId.isNotBlank())
                    assertTrue(connector.sourceProjectionIds.isNotEmpty())
                }
                val supplyRoute = cabinet.connectors.single { connector ->
                    connector.semanticId.value.endsWith(":supply_main_to_power_bus")
                }
                assertEquals(listOf("TopPowerChannel"), supplyRoute.selectedChannelIds)
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
                .map { path -> path.readText() }
                .toList()
                .joinToString("\n")
        }

        assertTrue(sourceText.contains("installation cabinet MainCabinet"))
        assertTrue(sourceText.contains("connect power_feed {"))
        assertTrue(sourceText.contains("connect control_distribution {"))
        assertTrue(sourceText.contains("connect feedback_return {"))
        assertTrue(sourceText.contains("route supply_main_to_power_bus through [TopPowerChannel]"))
        assertTrue(sourceText.contains("route vendor_signal_to_control_bus through [LeftControlChannel]"))
        assertTrue(sourceText.contains("route xt35_feedback through [RightFieldChannel]"))
        assertTrue(sourceText.contains("route cabinet_pe through [RightFieldChannel]"))

        assertTrue(standardText.contains("identity \"iec.control-junction.element\""))
        assertTrue(vendorText.contains("identity \"vendor.abb.pfea112.element\""))
        assertTrue(vendorText.contains("graphic svg resource pfea112_svg"))
        assertFalse(vendorText.contains("manufacturer:"), "Visual package must not become product identity authority.")

        assertTrue(allSampleText.contains("data-athena-ref=\"anchor:body\""))
        assertFalse(allSampleText.contains("<definition"), "M36 sample must not carry QET XML authority.")
        assertFalse(allSampleText.contains("m35.productSurface"), "M36 sample must not reuse M35 surface markers.")
        assertFalse(allSampleText.contains("fallback card"), "M36 sample must not contain fallback authoring text.")
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
