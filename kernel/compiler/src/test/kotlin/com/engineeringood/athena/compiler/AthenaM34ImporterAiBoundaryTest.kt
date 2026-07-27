package com.engineeringood.athena.compiler

import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgAdapter
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgCanvasComposer
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgCanvasRequest
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgPalette
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgRenderRequest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM34ImporterAiBoundaryTest {
    @Test
    fun `admits generated output only as canonical athena source and package local governed svg`() {
        val repositoryRoot = generatedRepository()

        val report = AthenaGeneratedRepresentationBoundaryVerifier().verify(
            AthenaGeneratedRepresentationBoundaryRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(repositoryRoot.resolve("packages/representation")),
                dependencyLockDigest = "lock:generated",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/generated"),
            ),
        )

        assertTrue(report.diagnostics.isEmpty(), report.diagnostics.toString())
        assertEquals(listOf("athena.generated.vendor_drive"), report.definitions.map { it.symbolId.value })
        val definition = report.definitions.single()
        assertTrue(definition.anchors.isEmpty())
        assertTrue(definition.labelSlots.isEmpty())
        val proof = assertNotNull(report.proof)
        assertEquals("canonical-athena-source", proof.generatedSourceBoundary)
        assertTrue(proof.qetRuntimeAuthorityAbsent)
        assertTrue(proof.foreignRuntimeSchemaAbsent)
        assertTrue(proof.xmlRuntimeAuthorityAbsent)
        assertTrue(proof.rawSvgTransportAbsent)
        assertTrue(proof.stagedSourcePaths.any { it.endsWith("athena/generated/vendor-drive.athena") })
        assertTrue(proof.stagedSourcePaths.any { it.endsWith("athena/generated/vendor-drive.svg") })
        assertTrue(proof.stagedSourcePaths.none { it.endsWith(".elmt") })
    }

    @Test
    fun `rejects qet elmt and foreign schema files in generated package roots`() {
        val repositoryRoot = generatedRepository()
        repositoryRoot.resolve("packages/representation/athena/generated/01coming_arrow.elmt")
            .writeText("<definition><uuid>qet-runtime-forbidden</uuid></definition>")

        val report = AthenaGeneratedRepresentationBoundaryVerifier().verify(
            AthenaGeneratedRepresentationBoundaryRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(repositoryRoot.resolve("packages/representation")),
                dependencyLockDigest = "lock:generated",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/foreign"),
            ),
        )

        assertTrue(report.definitions.isEmpty())
        val diagnostic = assertNotNull(report.diagnostics.singleOrNull { it.code == "generated.foreign-runtime-schema.forbidden" })
        assertEquals("packages/representation/athena/generated/01coming_arrow.elmt", diagnostic.subject)
    }

    @Test
    fun `does not infer generated connectable point contracts from geometry only svg`() {
        val repositoryRoot = generatedRepository(svg = UNGOVERNED_CONNECTABLE_POINT_SVG)

        val report = AthenaGeneratedRepresentationBoundaryVerifier().verify(
            AthenaGeneratedRepresentationBoundaryRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(repositoryRoot.resolve("packages/representation")),
                dependencyLockDigest = "lock:generated",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/uncontracted"),
            ),
        )

        assertTrue(report.diagnostics.isEmpty(), report.diagnostics.toString())
        val definition = report.definitions.single { it.symbolId.value == "athena.generated.vendor_drive" }
        assertTrue(definition.anchors.isEmpty())
        assertTrue(definition.labelSlots.isEmpty())
    }

    @Test
    fun `generated vendor style sample renders through compiled snapshot without foreign runtime metadata`() {
        val sampleRoot = Files.createTempDirectory("athena-m34-generated-sample")
        copyTree(repositoryRoot().resolve("examples/m34/sample-project"), sampleRoot)

        val report = AthenaGeneratedRepresentationBoundaryVerifier().verify(
            AthenaGeneratedRepresentationBoundaryRequest(
                repositoryRoot = sampleRoot,
                packageRoots = listOf(sampleRoot.resolve("packages/representation")),
                dependencyLockDigest = Files.readString(sampleRoot.resolve("athena.lock")),
                snapshotDirectory = sampleRoot.resolve(".athena/snapshots/generated-proof"),
            ),
        )

        assertTrue(report.diagnostics.isEmpty(), report.diagnostics.toString())
        val definition = report.definitions.single { it.symbolId.value == "athena.generated.vendor_drive" }
        val palette = GraphicPrimitiveSvgPalette(mapOf("foreground" to "#202020"), "#ffffff")
        val fragment = GraphicPrimitiveSvgAdapter().render(GraphicPrimitiveSvgRenderRequest(definition.graphicBody, palette))
        val canvas = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(definition.graphicBody, fragment, margin = 8.0),
        )

        assertTrue(fragment.isValid, fragment.diagnostics.toString())
        assertTrue(canvas.isValid, canvas.diagnostics.toString())
        assertTrue(assertNotNull(canvas.svg).contains("data-athena-render-authority=\"graphic-primitive-ir\""))
        assertTrue(canvas.svg!!.contains("GEN-DRV"))
        val proof = assertNotNull(report.proof)
        assertEquals("canonical-athena-source", proof.generatedSourceBoundary)
        assertTrue(proof.qetRuntimeAuthorityAbsent)
        assertTrue(proof.foreignRuntimeSchemaAbsent)
        assertTrue(proof.xmlRuntimeAuthorityAbsent)
        assertTrue(proof.rawSvgTransportAbsent)
    }

    private fun generatedRepository(svg: String = GENERATED_SVG): Path {
        val repositoryRoot = Files.createTempDirectory("athena-m34-generated-boundary")
        repositoryRoot.resolve("athena.yaml").writeText(
            """
            primaryPackage:
              name: com.engineeringood.m34.generated
              version: 1.0.0
              sourceRoot: src

            representationPackageRoots:
              - packages/representation
            """.trimIndent(),
        )
        repositoryRoot.resolve("athena.lock").writeText("generated-lock")
        val generatedRoot = repositoryRoot.resolve("packages/representation/athena/generated").createDirectories()
        generatedRoot.resolve("vendor-drive.athena").writeText(GENERATED_SOURCE)
        generatedRoot.resolve("vendor-drive.svg").writeText(svg)
        return repositoryRoot
    }

    companion object {
        private val GENERATED_SOURCE = """
            package athena.generated

            element generated_vendor_drive {
              identity "athena.generated.vendor_drive"
              version "1.0.0"

              resource generated_vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource generated_vendor_drive_svg
            }
        """.trimIndent()

        private val GENERATED_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 220">
              <rect id="body" x="8" y="8" width="144" height="204" data-athena-geometry-ref="body"/>
              <circle id="power-in" cx="20" cy="40" r="4" data-athena-geometry-ref="power-in"/>
              <circle id="status-out" cx="140" cy="40" r="4" data-athena-geometry-ref="status-out"/>
              <text id="tag" x="80" y="28" data-athena-geometry-ref="tag">GEN-DRV</text>
            </svg>
        """.trimIndent()

        private val UNGOVERNED_CONNECTABLE_POINT_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 220">
              <rect id="body" x="8" y="8" width="144" height="204"/>
              <circle id="looks-like-terminal" cx="20" cy="40" r="4"/>
              <text id="tag" x="80" y="28">GEN-DRV</text>
            </svg>
        """.trimIndent()
    }
}

private fun repositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !current.resolve("settings.gradle.kts").toFile().exists()) {
        current = current.parent
    }
    return current
}

private fun copyTree(sourceRoot: Path, targetRoot: Path) {
    Files.walk(sourceRoot).use { paths ->
        paths.forEach { source ->
            val target = targetRoot.resolve(sourceRoot.relativize(source).toString())
            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                target.parent?.let(Files::createDirectories)
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
