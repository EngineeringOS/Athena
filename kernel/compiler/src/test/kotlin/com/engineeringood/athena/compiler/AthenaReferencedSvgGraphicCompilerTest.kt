package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationBodyAuthority
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationDirectionPredicate
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaReferencedSvgGraphicCompilerTest {
    private val compiler = AthenaRepresentationSourceCompiler()

    @Test
    fun `compiles symbol and element graphic svg bodies through canonical primitive authority`() {
        val root = Files.createTempDirectory("athena-m34-svg-valid")
        val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
        val svg = packageRoot.resolve("vendor-drive.svg")
        svg.writeText(VALID_SVG)
        val source = packageRoot.resolve("vendor-drive.athena")
        source.writeText(VALID_SOURCE)

        val result = compiler.compile(source.toString(), VALID_SOURCE)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(listOf("vendor.drive.element", "vendor.drive.symbol"), result.definitions.map { it.symbolId.value })
        result.definitions.forEach { definition ->
            assertEquals(RepresentationBodyAuthority.GRAPHIC_PRIMITIVE, definition.bodyAuthority)
            assertEquals(GraphicBounds(0.0, 0.0, 240.0, 360.0), definition.graphicBody.bounds)
            assertEquals(listOf(source.toString(), svg.toString()), definition.graphicBody.provenanceSources)
            assertTrue(definition.graphicBody.primitives.any { it is GraphicPrimitive.Rectangle })
            assertTrue(definition.graphicBody.primitives.any { it is GraphicPrimitive.Circle })
            assertTrue(definition.graphicBody.primitives.any { it is GraphicPrimitive.Text })
            assertEquals(setOf("inspectBody", "powerIn", "statusOut"), definition.anchors.map { it.anchorId.value }.toSet())
            assertEquals(RepresentationAnchorRole.TERMINAL, definition.anchors.first { it.anchorId.value == "powerIn" }.role)
            assertEquals(RepresentationAnchorRole.HOTSPOT, definition.anchors.first { it.anchorId.value == "inspectBody" }.role)
            assertEquals(
                setOf(RepresentationDirectionPredicate.IN),
                definition.anchors.first { it.anchorId.value == "powerIn" }.acceptedDirections,
            )
            assertEquals(setOf("deviceTag"), definition.labelSlots.map { it.slotId.value }.toSet())
            assertEquals(PresentationLabelRole.DEVICE_TAG, definition.labelSlots.single().role)
            assertTrue(definition.graphicBody.forbiddenAuthorityClaims.isEmpty())
        }
        assertEquals(RepresentationDefinitionKind.SYMBOL, result.definitions.single { it.symbolId.value == "vendor.drive.symbol" }.definitionKind)
        assertEquals(RepresentationDefinitionKind.ELEMENT, result.definitions.single { it.symbolId.value == "vendor.drive.element" }.definitionKind)
    }

    @Test
    fun `composed element exports compiled svg anchor and label contracts with child transform`() {
        val root = Files.createTempDirectory("athena-m34-svg-composition")
        val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
        packageRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)
        val source = packageRoot.resolve("vendor-drive.athena")
        source.writeText(SVG_COMPOSITION_SOURCE)

        val result = compiler.compile(source.toString(), SVG_COMPOSITION_SOURCE)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val element = result.definitions.single { definition -> definition.symbolId.value == "vendor.drive.composed" }
        val anchor = element.anchors.single { candidate -> candidate.anchorId.value == "power" }
        assertEquals("power", anchor.anchorId.value)
        assertEquals(GraphicPoint(22.0, 44.0), anchor.point)
        assertEquals(RepresentationAnchorRole.TERMINAL, anchor.role)
        val label = element.labelSlots.single()
        assertEquals("tag", label.slotId.value)
        assertEquals(GraphicPoint(70.0, 36.0), label.origin)
        assertEquals(GraphicBounds(70.0, 36.0, 0.5, 0.5), label.bounds)
        assertTrue(element.intrinsicComposition?.exportedAnchors.orEmpty().all { exported -> exported.childId?.value == "glyph" })
        assertEquals("glyph", element.intrinsicComposition?.exportedLabelSlots?.single()?.childId?.value)
    }

    @Test
    fun `rejects svg root identity authority and admits no partial definitions`() {
        val root = Files.createTempDirectory("athena-m34-svg-invalid")
        val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
        val invalidSvg = VALID_SVG.replace(
            "data-athena-schema=\"representation/v1\"",
            "data-athena-schema=\"representation/v1\" data-athena-identity=\"vendor.fake\"",
        )
        val svg = packageRoot.resolve("vendor-drive.svg")
        svg.writeText(invalidSvg)
        val source = packageRoot.resolve("vendor-drive.athena")
        source.writeText(VALID_SOURCE)

        val result = compiler.compile(source.toString(), VALID_SOURCE)

        assertTrue(result.definitions.isEmpty())
        val diagnostic = assertNotNull(result.diagnostics.singleOrNull { it.code == "svg.metadata.forbidden" })
        assertEquals(svg.toString(), diagnostic.file)
        assertEquals("svg.root.data-athena-identity", diagnostic.subject)
        assertEquals(invalidSvg.spanOf("data-athena-identity"), diagnostic.span)
        assertEquals("SVG root must not declare Athena identity metadata; identity belongs to the Athena declaration.", diagnostic.message)
    }

    @Test
    fun `rejects unknown svg athena metadata and admits no partial definitions`() {
        val root = Files.createTempDirectory("athena-m34-svg-unknown")
        val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
        val invalidSvg = VALID_SVG.replace(
            "<rect id=\"body\"",
            "<rect data-athena-random=\"bad\" id=\"body\"",
        )
        val svg = packageRoot.resolve("vendor-drive.svg")
        svg.writeText(invalidSvg)
        val source = packageRoot.resolve("vendor-drive.athena")
        source.writeText(VALID_SOURCE)

        val result = compiler.compile(source.toString(), VALID_SOURCE)

        assertTrue(result.definitions.isEmpty())
        val diagnostic = assertNotNull(result.diagnostics.singleOrNull { it.code == "svg.metadata.unknown" })
        assertEquals(svg.toString(), diagnostic.file)
        assertEquals("svg.rect.data-athena-random", diagnostic.subject)
        assertEquals(invalidSvg.spanOf("data-athena-random"), diagnostic.span)
    }

    @Test
    fun `rejects traversal svg references before resource acquisition`() {
        val result = compiler.compile(
            "packages/representation/athena/vendor/vendor-drive.athena",
            TRAVERSAL_SOURCE,
        )

        assertTrue(result.definitions.isEmpty())
        val diagnostic = assertNotNull(result.diagnostics.singleOrNull { it.code == "resource.path.invalid" })
        assertEquals("packages/representation/athena/vendor/vendor-drive.athena", diagnostic.file)
        assertEquals("resource.vendor_drive_svg.path", diagnostic.subject)
        assertEquals("Resource path must stay inside the source unit directory and point to a local SVG file.", diagnostic.message)
    }

    @Test
    fun `rejects absolute svg references before resource acquisition`() {
        val result = compiler.compile(
            "packages/representation/athena/vendor/vendor-drive.athena",
            ABSOLUTE_SOURCE,
        )

        assertTrue(result.definitions.isEmpty())
        val diagnostic = assertNotNull(result.diagnostics.singleOrNull { it.code == "resource.path.invalid" })
        assertEquals("packages/representation/athena/vendor/vendor-drive.athena", diagnostic.file)
        assertEquals("resource.vendor_drive_svg.path", diagnostic.subject)
        assertEquals("Resource path must stay inside the source unit directory and point to a local SVG file.", diagnostic.message)
    }

    @Test
    fun `rejects svg resources reached through a symlinked parent directory`() {
        val root = Files.createTempDirectory("athena-m34-svg-parent-symlink")
        val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
        val outside = Files.createTempDirectory("athena-m34-svg-parent-outside")
        outside.resolve("vendor-drive.svg").writeText(VALID_SVG)
        val linkedResources = packageRoot.resolve("resources")
        try {
            Files.createSymbolicLink(linkedResources, outside)
        } catch (_: Exception) {
            return
        }
        val sourceText = VALID_SOURCE.replace("./vendor-drive.svg", "./resources/vendor-drive.svg")
        val source = packageRoot.resolve("vendor-drive.athena")
        source.writeText(sourceText)

        val result = compiler.compile(source.toString(), sourceText)

        assertTrue(result.definitions.isEmpty())
        assertEquals(
            1,
            result.diagnostics.count { diagnostic -> diagnostic.code == "resource.path.invalid" },
            result.diagnostics.toString(),
        )
    }

    @Test
    fun `rejects missing package-local svg resource`() {
        val root = Files.createTempDirectory("athena-m34-svg-missing")
        val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
        val source = packageRoot.resolve("vendor-drive.athena")
        source.writeText(VALID_SOURCE)

        val result = compiler.compile(source.toString(), VALID_SOURCE)

        assertTrue(result.definitions.isEmpty())
        val diagnostic = assertNotNull(result.diagnostics.singleOrNull { it.code == "resource.file.missing" })
        assertEquals(source.toString(), diagnostic.file)
        assertEquals("resource.vendor_drive_svg.path", diagnostic.subject)
    }

    @Test
    fun `rejects svg source byte budget before XML parsing`() {
        val root = Files.createTempDirectory("athena-m34-svg-byte-budget")
        val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
        val svg = packageRoot.resolve("vendor-drive.svg")
        svg.writeText(oversizedByteBudgetSvg())
        val source = packageRoot.resolve("vendor-drive.athena")
        source.writeText(VALID_SOURCE)

        val result = compiler.compile(source.toString(), VALID_SOURCE)

        assertTrue(result.definitions.isEmpty())
        val diagnostics = result.diagnostics.filter { it.code == "svg.budget.bytes.exceeded" }
        assertEquals(1, diagnostics.size, result.diagnostics.toString())
        diagnostics.forEach { diagnostic ->
            assertEquals(source.toString(), diagnostic.file)
            assertEquals("svg.bytes", diagnostic.subject)
        }
    }

    @Test
    fun `M34 sample referenced svg representation package compiles without xml authority`() {
        val repo = repositoryRoot()
        val sourcePath = repo.resolve("examples/m34/sample-project/packages/representation/athena/vendor/epic2-svg-elements.athena")
        val source = Files.readString(sourcePath)

        val result = compiler.compile(sourcePath.toString(), source)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(listOf("vendor.drive.element", "vendor.drive.symbol"), result.definitions.map { it.symbolId.value })
        assertTrue(result.definitions.all { it.bodyAuthority == RepresentationBodyAuthority.GRAPHIC_PRIMITIVE })
        assertTrue(result.definitions.all { it.graphicBody.primitives.isNotEmpty() })
    }

    @Test
    fun `rejects unsafe svg failure classes with stable diagnostics`() {
        invalidSvgCases().forEach { case ->
            val root = Files.createTempDirectory("athena-m34-svg-${case.name}")
            val packageRoot = root.resolve("packages/representation/athena/vendor").createDirectories()
            val svg = packageRoot.resolve("vendor-drive.svg")
            svg.writeText(case.svg)
            val source = packageRoot.resolve("vendor-drive.athena")
            source.writeText(VALID_SOURCE)

            val result = compiler.compile(source.toString(), VALID_SOURCE)

            assertTrue(result.definitions.isEmpty(), case.name)
            val diagnostic = assertNotNull(result.diagnostics.singleOrNull { it.code == case.code && it.subject == case.subject }, "$case\n${result.diagnostics}")
            assertEquals(svg.toString(), diagnostic.file, case.name)
            assertEquals(case.svg.spanOf(case.token), diagnostic.span, case.name)
        }
    }

    companion object {
        private val VALID_SOURCE = """
            package athena.vendor

            symbol vendor_drive_symbol {
              identity "vendor.drive.symbol"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }

            element vendor_drive_element {
              identity "vendor.drive.element"
              version "1.0.0"

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()

        private val SVG_COMPOSITION_SOURCE = """
            package athena.vendor

            symbol vendor_drive_symbol {
              identity "vendor.drive.symbol"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }

            element vendor_drive_composed {
              identity "vendor.drive.composed"
              version "1.0.0"
              bounds (0, 0, 160, 220)

              child glyph {
                symbol "vendor.drive.symbol"
                translate (10, 20)
                rotate 0
                scale (0.5, 0.5)
                zOrder 0
              }

              export anchor power from glyph.powerIn
              export anchor status from glyph.statusOut
              export label tag from glyph.deviceTag
            }
        """.trimIndent()

        private val TRAVERSAL_SOURCE = """
            package athena.vendor

            symbol vendor_drive_symbol {
              identity "vendor.drive.symbol"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "../vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()

        private val ABSOLUTE_SOURCE = """
            package athena.vendor

            symbol vendor_drive_symbol {
              identity "vendor.drive.symbol"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "C:/vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()

        private val VALID_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg"
                 viewBox="0 0 240 360"
                 data-athena-schema="representation/v1">
              <rect id="body" x="8" y="8" width="224" height="344"
                    data-athena-hotspot="inspectBody"
                    data-athena-point="120 180"/>
              <line id="bus" x1="24" y1="48" x2="216" y2="48"/>
              <circle id="power-in-dot" cx="24" cy="48" r="4"
                      data-athena-anchor="powerIn"
                      data-athena-point="24 48"
                      data-athena-role="terminal"
                      data-athena-direction="in"
                      data-athena-signal="AC"/>
              <circle id="status-out-dot" cx="216" cy="48" r="4"
                      data-athena-anchor="statusOut"
                      data-athena-point="216 48"
                      data-athena-role="terminal"
                      data-athena-direction="out"
                      data-athena-signal="Digital"/>
              <text id="tag" x="120" y="32"
                    data-athena-label-slot="deviceTag"
                    data-athena-point="120 32">ACS380</text>
            </svg>
        """.trimIndent()

        private fun invalidSvgCases(): List<InvalidSvgCase> = listOf(
            InvalidSvgCase(
                "missing schema",
                VALID_SVG.replace("data-athena-schema=\"representation/v1\"", ""),
                "svg.schema.invalid",
                "svg.root.data-athena-schema",
                "<svg",
            ),
            InvalidSvgCase(
                "script",
                VALID_SVG.replace("</svg>", "<script>alert(1)</script></svg>"),
                "svg.script.forbidden",
                "svg.script",
                "script",
            ),
            InvalidSvgCase(
                "foreign object",
                VALID_SVG.replace("</svg>", "<foreignObject x=\"0\" y=\"0\" width=\"1\" height=\"1\"/></svg>"),
                "svg.foreign-object.forbidden",
                "svg.foreignObject",
                "foreignObject",
            ),
            InvalidSvgCase(
                "event attribute",
                VALID_SVG.replace("<rect id=\"body\"", "<rect onclick=\"bad()\" id=\"body\""),
                "svg.event.forbidden",
                "svg.rect.onclick",
                "onclick",
            ),
            InvalidSvgCase(
                "external url",
                VALID_SVG.replace("<rect id=\"body\"", "<rect href=\"https://example.invalid/a.svg\" id=\"body\""),
                "svg.resource-url.forbidden",
                "svg.rect.href",
                "href",
            ),
            InvalidSvgCase(
                "unsafe css resource",
                VALID_SVG.replace("<line id=\"bus\"", "<line style=\"fill:url(#gradient)\" id=\"bus\""),
                "svg.resource-url.forbidden",
                "svg.line.style",
                "style",
            ),
            InvalidSvgCase(
                "node forbidden metadata",
                VALID_SVG.replace("<line id=\"bus\"", "<line data-athena-device=\"M1\" id=\"bus\""),
                "svg.metadata.forbidden",
                "svg.line.data-athena-device",
                "data-athena-device",
            ),
            InvalidSvgCase(
                "wrong node annotation",
                VALID_SVG
                    .replace("<rect id=\"body\"", "<g data-athena-anchor=\"bad\"><rect id=\"body\"")
                    .replace("data-athena-point=\"120 180\"/>", "data-athena-point=\"120 180\"/></g>"),
                "svg.annotation.node.invalid",
                "svg.g.data-athena-anchor",
                "data-athena-anchor",
            ),
            InvalidSvgCase(
                "malformed point",
                VALID_SVG.replace("data-athena-point=\"24 48\"", "data-athena-point=\"24\""),
                "svg.anchor.point.invalid",
                "svg.anchor.powerIn.point",
                "data-athena-point",
            ),
            InvalidSvgCase(
                "non-finite point",
                VALID_SVG.replace("data-athena-point=\"24 48\"", "data-athena-point=\"NaN 48\""),
                "svg.anchor.point.invalid",
                "svg.anchor.powerIn.point",
                "data-athena-point",
            ),
            InvalidSvgCase(
                "missing terminal signal contract",
                VALID_SVG.replace("data-athena-signal=\"AC\"", ""),
                "svg.anchor.signal.missing",
                "svg.anchor.powerIn.signal",
                "data-athena-anchor=\"powerIn\"",
            ),
            InvalidSvgCase(
                "invalid role",
                VALID_SVG.replace("data-athena-role=\"terminal\"", "data-athena-role=\"magic\""),
                "svg.anchor.role.invalid",
                "svg.anchor.powerIn.role",
                "data-athena-role",
            ),
            InvalidSvgCase(
                "invalid direction",
                VALID_SVG.replace("data-athena-direction=\"in\"", "data-athena-direction=\"sideways\""),
                "svg.anchor.direction.invalid",
                "svg.anchor.powerIn.direction",
                "data-athena-direction",
            ),
            InvalidSvgCase(
                "duplicate anchor",
                VALID_SVG.replace("data-athena-anchor=\"statusOut\"", "data-athena-anchor=\"powerIn\""),
                "svg.anchor.duplicate",
                "svg.anchor.powerIn",
                "data-athena-anchor=\"powerIn\"",
            ),
            InvalidSvgCase(
                "duplicate label slot",
                VALID_SVG.replace("data-athena-label-slot=\"deviceTag\"", "data-athena-label-slot=\"ratingTag\"")
                    .replace("</svg>", "<text id=\"tag2\" x=\"120\" y=\"340\" data-athena-label-slot=\"ratingTag\" data-athena-point=\"120 340\">3kW</text></svg>"),
                "svg.label-slot.duplicate",
                "svg.label-slot.ratingTag",
                "data-athena-label-slot=\"ratingTag\"",
            ),
            InvalidSvgCase(
                "hotspot malformed point",
                VALID_SVG.replace("data-athena-point=\"120 180\"", "data-athena-point=\"120\""),
                "svg.hotspot.point.invalid",
                "svg.hotspot.inspectBody.point",
                "data-athena-point",
            ),
            InvalidSvgCase(
                "duplicate svg id",
                VALID_SVG.replace("id=\"status-out-dot\"", "id=\"power-in-dot\""),
                "svg.id.duplicate",
                "svg.id.power-in-dot",
                "id=\"power-in-dot\"",
            ),
            InvalidSvgCase(
                "bad viewbox",
                VALID_SVG.replace("viewBox=\"0 0 240 360\"", "viewBox=\"0 0 0 360\""),
                "svg.viewbox.invalid",
                "svg.root.viewBox",
                "viewBox",
            ),
            InvalidSvgCase(
                "non-finite viewbox",
                VALID_SVG.replace("viewBox=\"0 0 240 360\"", "viewBox=\"0 0 NaN 360\""),
                "svg.viewbox.invalid",
                "svg.root.viewBox",
                "viewBox",
            ),
            InvalidSvgCase(
                "non-finite rectangle coordinate",
                VALID_SVG.replace("x=\"8\" y=\"8\" width=\"224\"", "x=\"NaN\" y=\"8\" width=\"224\""),
                "svg.rect.coordinate.invalid",
                "svg.rect.geometry",
                "x=\"NaN\"",
            ),
            InvalidSvgCase(
                "non-positive rectangle size",
                VALID_SVG.replace("width=\"224\" height=\"344\"", "width=\"0\" height=\"344\""),
                "svg.rect.size.invalid",
                "svg.rect.geometry",
                "width=\"0\"",
            ),
            InvalidSvgCase(
                "degenerate line",
                VALID_SVG.replace("x2=\"216\" y2=\"48\"", "x2=\"24\" y2=\"48\""),
                "svg.line.degenerate",
                "svg.line.geometry",
                "x2=\"24\"",
            ),
            InvalidSvgCase(
                "non-positive circle radius",
                VALID_SVG.replace("cx=\"24\" cy=\"48\" r=\"4\"", "cx=\"24\" cy=\"48\" r=\"0\""),
                "svg.circle.radius.invalid",
                "svg.circle.geometry",
                "r=\"0\"",
            ),
            InvalidSvgCase(
                "blank text",
                VALID_SVG.replace(">ACS380</text>", "> </text>"),
                "svg.text.value.missing",
                "svg.text.value",
                "data-athena-label-slot=\"deviceTag\"",
            ),
            InvalidSvgCase(
                "incompatible annotations",
                VALID_SVG.replace(
                    "data-athena-label-slot=\"deviceTag\"",
                    "data-athena-anchor=\"tagAnchor\" data-athena-label-slot=\"deviceTag\"",
                ),
                "svg.annotation.combination.invalid",
                "svg.text.annotation",
                "data-athena-label-slot",
            ),
            InvalidSvgCase(
                "unsupported namespace",
                VALID_SVG.replace("</svg>", "<html:div xmlns:html=\"http://www.w3.org/1999/xhtml\"/></svg>"),
                "svg.namespace.unsupported",
                "svg.div",
                "html:div",
            ),
            InvalidSvgCase(
                "unsupported transform",
                VALID_SVG.replace("<rect id=\"body\"", "<rect transform=\"translate(1 1)\" id=\"body\""),
                "svg.transform.unsupported",
                "svg.rect.transform",
                "transform",
            ),
            InvalidSvgCase(
                "unsupported path element",
                VALID_SVG.replace("</svg>", "<path id=\"complex\" d=\"M 0 0 L 1 1\"/></svg>"),
                "svg.element.unsupported",
                "svg.path",
                "path",
            ),
            InvalidSvgCase(
                "DTD",
                VALID_SVG.replace("<svg", "<!DOCTYPE svg [ <!ENTITY xxe SYSTEM \"file:///etc/passwd\"> ]>\n<svg"),
                "svg.xml.invalid",
                "svg",
                "<",
            ),
            InvalidSvgCase(
                "use",
                VALID_SVG.replace("</svg>", "<use href=\"#body\"/></svg>"),
                "svg.element.unsupported",
                "svg.use",
                "use",
            ),
            InvalidSvgCase(
                "element budget",
                oversizedElementBudgetSvg(),
                "svg.budget.elements.exceeded",
                "svg.elements",
                "<svg",
            ),
            InvalidSvgCase(
                "depth budget",
                oversizedDepthBudgetSvg(),
                "svg.budget.depth.exceeded",
                "svg.depth",
                "<svg",
            ),
            InvalidSvgCase(
                "primitive budget",
                oversizedPrimitiveBudgetSvg(),
                "svg.budget.primitives.exceeded",
                "svg.primitives",
                "<svg",
            ),
        )

        private fun oversizedByteBudgetSvg(): String = buildString {
            append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 360" data-athena-schema="representation/v1">""")
            append(" ".repeat(AthenaSvgGraphicBodySupport.MAX_SVG_BYTES.toInt() + 1))
            append("</svg>")
        }

        private fun oversizedElementBudgetSvg(): String = buildString {
            appendLine("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 360" data-athena-schema="representation/v1">""")
            repeat(AthenaSvgGraphicBodySupport.MAX_ELEMENTS + 1) { index ->
                appendLine("""<rect id="r$index" x="1" y="1" width="1" height="1"/>""")
            }
            appendLine("</svg>")
        }

        private fun oversizedDepthBudgetSvg(): String = buildString {
            append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 360" data-athena-schema="representation/v1">""")
            repeat(AthenaSvgGraphicBodySupport.MAX_DEPTH + 1) { append("<g>") }
            repeat(AthenaSvgGraphicBodySupport.MAX_DEPTH + 1) { append("</g>") }
            append("</svg>")
        }

        private fun oversizedPrimitiveBudgetSvg(): String = buildString {
            appendLine("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 360" data-athena-schema="representation/v1">""")
            repeat(AthenaSvgGraphicBodySupport.MAX_EMITTED_PRIMITIVES + 1) { index ->
                appendLine("""<rect id="p$index" x="$index" y="1" width="1" height="1"/>""")
            }
            appendLine("</svg>")
        }
    }

    private data class InvalidSvgCase(
        val name: String,
        val svg: String,
        val code: String,
        val subject: String,
        val token: String,
    )
}

private fun repositoryRoot(): java.nio.file.Path {
    var current = java.nio.file.Path.of("").toAbsolutePath()
    while (current.parent != null && !current.resolve("settings.gradle.kts").toFile().exists()) {
        current = current.parent
    }
    return current
}

private fun String.spanOf(token: String): SourceSpan {
    val start = indexOf(token)
    require(start >= 0) { "Token `$token` is missing." }

    fun position(offset: Int): SourcePosition {
        val prefix = substring(0, offset)
        return SourcePosition(
            offset = offset,
            line = prefix.count { it == '\n' } + 1,
            column = offset - prefix.lastIndexOf('\n'),
        )
    }

    return SourceSpan(position(start), position(start + token.length))
}
