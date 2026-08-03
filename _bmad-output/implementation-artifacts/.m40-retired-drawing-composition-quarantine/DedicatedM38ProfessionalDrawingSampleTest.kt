package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import com.engineeringood.athena.packageplatform.ProjectionContextId
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class DedicatedM38ProfessionalDrawingSampleTest {
    @Test
    fun `dedicated M38 professional control drawing sample is source first and compiles offline`() {
        val repoRoot = resolveRepoRoot()
        val sampleRoot = repoRoot.resolve("examples/m38/professional-control-drawing")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m38/professionalcontroldrawing/01-professional-control-drawing.athena",
        )
        val profilePackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m38/professional/drawing-profile.athena",
        )
        val elementPackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m38/professional/m38-elements.athena",
        )
        val bindingPackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m38/professional/m38-bindings.athena",
        )
        val svgResource = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m38/professional/svg/vendor-drive.svg",
        )

        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")), "M38 sample must be IDE-openable.")
        assertTrue(Files.exists(source), "M38 source must follow package/path hierarchy.")
        assertTrue(Files.exists(profilePackage), "M38 sample must include package-local drawing profile material.")
        assertTrue(Files.exists(elementPackage), "M38 sample must include package-local element material.")
        assertTrue(Files.exists(bindingPackage), "M38 sample must include explicit package-local binding material.")
        assertTrue(Files.exists(svgResource), "M38 sample must include one package-local SVG-backed Element resource.")

        val sourceText = source.readText()
        assertEquals(
            "src/com/engineeringood/m38/professionalcontroldrawing",
            source.parent.toString().replace('\\', '/').substringAfter("professional-control-drawing/"),
        )
        assertTrue(sourceText.startsWith("package com.engineeringood.m38.professionalcontroldrawing"))
        assertTrue(sourceText.contains("model \"SVG-VENDOR-DRIVE\""))
        assertTrue(sourceText.contains("port motorU { direction out signal Power role switched terminal \"U\" }"))
        assertTrue(sourceText.contains("subject port TerminalX38.input"))
        assertTrue(sourceText.contains("earth PeBarM38.bond to [VendorDriveU38.pe, TerminalX38.pe, MotorM38.pe]"))
        assertTrue(sourceText.contains("reference \"IEC:60204-1:explicit-junction:protective_earth_junction\""))
        assertTrue(sourceText.contains("reference \"IEC:60204-1:explicit-no-connect-crossing:control_status-over-drive_power\""))
        assertFalse(sourceText.contains("intent {"), "M38 source must not keep removed connection intent blocks.")
        assertFalse(sourceText.contains("label policy"), "M38 source must not keep per-connection label policy syntax.")

        val elementText = elementPackage.readText()
        assertTrue(elementText.contains("graphic svg resource vendor_drive_svg"))
        assertTrue(elementText.contains("symbol m38_terminal_symbol"))
        assertTrue(elementText.contains("export anchor controlOut from body.controlOut"))
        assertTrue(elementText.contains("anchor pe"))

        val svgText = svgResource.readText()
        assertTrue(svgText.contains("data-athena-ref=\"anchor:supply-l\""))
        assertFalse(Regex("""data-athena-(?!ref)""").containsMatchIn(svgText), "SVG may only expose neutral data-athena-ref geometry hints.")
        listOf("portSemanticId", "signal ", "role=", "compatibility", "connection:", "route:").forEach { forbidden ->
            assertFalse(svgText.contains(forbidden, ignoreCase = true), "SVG must not carry engineering metadata `$forbidden`.")
        }

        val allSampleText = Files.walk(sampleRoot).use { stream ->
            stream
                .filter(Files::isRegularFile)
                .map { path -> path.readText() }
                .toList()
                .joinToString("\n")
        }
        listOf("<definition", ".elmt", "qelectrotech", "m36", "m37", "mock presentation", "hardcoded sample policy").forEach { forbidden ->
            assertFalse(allSampleText.contains(forbidden, ignoreCase = true), "M38 sample must not contain `$forbidden`.")
        }

        val compiler = AthenaCompiler()
        val lock = compiler.materializeRepositoryLock(sampleRoot)
        assertTrue(
            lock.isValid,
            lock.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )
        assertTrue(compiler.validateRepositoryContract(sampleRoot).isValid)
        assertTrue(compiler.validateRepositoryLock(sampleRoot).isValid)

        when (val compilation = compiler.compile(source)) {
            is CompilerCompilationSuccess -> {
                assertTrue(
                    compilation.semanticResult.diagnostics.isEmpty(),
                    compilation.semanticResult.diagnostics.joinToString("\n") { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                )
                assertTrue(compilation.document.components.size >= 6)
                assertTrue(compilation.document.connections.size >= 8)
                assertTrue(compilation.document.externalEvidence.size >= 2)
                assertTrue(compilation.document.projectionPolicies.any { policy -> policy.targetSurface == "professional-connection-drawing" })
                val material = AthenaRepresentationMaterialResolver().resolve(
                    repositoryRoot = sampleRoot,
                    document = compilation.document,
                    projectionContext = ProjectionContextId("schematic"),
                )
                assertTrue(
                    material.diagnostics.isEmpty(),
                    material.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
                )
                assertTrue(material.materials.size >= 6, "M38 source must resolve package-local representation material.")
                val presentationSummary = compilation.presentations.joinToString("\n") { presentation ->
                    "${presentation.view.id}|${presentation.view.displayName}|occ=${presentation.graphicOccurrences.size}|connectors=${presentation.connectors.size}"
                }
                assertTrue(
                    compilation.presentations.any { presentation ->
                        presentation.view.id == "schematic" &&
                            presentation.view.displayName == "Control Drawing" &&
                            presentation.connectors.isNotEmpty()
                    },
                    "M38 sample must compile into the schematic-backed Control Drawing presentation.\n$presentationSummary",
                )
            }

            is CompilerCompilationParseFailure -> fail(
                compilation.diagnostics.joinToString("\n") { diagnostic ->
                    "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
                },
            )
        }
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
