package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.RepresentationResourceDeclaration
import com.engineeringood.athena.language.RepresentationResourceKind
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SymbolStringField
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.Path

class AthenaReferencedSvgGraphicCompilerTest {
    @Test
    fun `compiles complex svg geometry with use expansion and geometry refs`() {
        val project = createTempProject()
        val svgPath = project.resolve("asset.svg")
        svgPath.writeText(COMPLEX_SVG)

        val result = AthenaSvgGraphicBodyCompiler.compile(
            athenaFile = project.resolve("source.athena").toString(),
            definitionId = "vendor.drive.element",
            resource = svgResource(),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val document = requireNotNull(result.document)
        assertEquals(GraphicBounds(0.0, 0.0, 120.0, 60.0), document.bounds)
        assertEquals(
            listOf(
                CanonicalLine(
                    bounds = GraphicBounds(19.9995, 10.0, 0.001, 8.0),
                    start = GraphicPoint(20.0, 10.0),
                    end = GraphicPoint(20.0, 18.0),
                ),
                CanonicalLine(
                    bounds = GraphicBounds(17.0, 17.9995, 6.0, 0.001),
                    start = GraphicPoint(17.0, 18.0),
                    end = GraphicPoint(23.0, 18.0),
                ),
                CanonicalLine(
                    bounds = GraphicBounds(52.0, 9.9995, 8.0, 0.001),
                    start = GraphicPoint(60.0, 10.0),
                    end = GraphicPoint(52.0, 10.0),
                ),
                CanonicalLine(
                    bounds = GraphicBounds(51.9995, 7.0, 0.001, 6.0),
                    start = GraphicPoint(52.0, 7.0),
                    end = GraphicPoint(52.0, 13.0),
                ),
            ),
            canonicalAtomicShapes(document.primitives),
        )
    }

    @Test
    fun `rejects geometry ref mismatches and duplicate ids`() {
        val project = createTempProject()
        project.resolve("asset.svg").writeText(INVALID_GEOMETRY_REF_SVG)

        val result = AthenaSvgGraphicBodyCompiler.compile(
            athenaFile = project.resolve("source.athena").toString(),
            definitionId = "vendor.drive.element",
            resource = svgResource(),
        )

        assertTrue(result.document == null)
        assertTrue(
            result.diagnostics.any { it.code == "svg.geometry-ref.mismatch" },
            result.diagnostics.joinToString("\n"),
        )
        assertTrue(
            result.diagnostics.any { it.code == "svg.id.duplicate" },
            result.diagnostics.joinToString("\n"),
        )
        assertTrue(
            result.diagnostics.any { it.code == "svg.use.reference.missing" },
            result.diagnostics.joinToString("\n"),
        )
    }

    @Test
    fun `rejects forbidden svg metadata unsupported namespaces and unsafe urls`() {
        val compiler = AthenaRepresentationSourceCompiler()

        val diagnostics = compiler.lintSvg(
            file = "asset.svg",
            source = INVALID_METADATA_SVG,
        )

        assertTrue(diagnostics.any { it.code == "svg.metadata.forbidden" }, diagnostics.joinToString("\n"))
        assertTrue(diagnostics.any { it.code == "svg.namespace.unsupported" }, diagnostics.joinToString("\n"))
        assertTrue(diagnostics.any { it.code == "svg.resource-url.forbidden" }, diagnostics.joinToString("\n"))
    }

    @Test
    fun `native and svg backed bodies emit the same canonical atomic primitives`() {
        val project = createTempProject()
        project.resolve("asset.svg").writeText(COMPLEX_SVG)

        val svgResult = AthenaRepresentationSourceCompiler().compile(
            listOf(
                AthenaRepresentationSourceInput(
                    file = project.resolve("svg.athena").toString(),
                    source = SVG_BACKED_SOURCE,
                ),
                AthenaRepresentationSourceInput(
                    file = project.resolve("native.athena").toString(),
                    source = NATIVE_SOURCE,
                ),
            ),
        )

        assertTrue(svgResult.diagnostics.isEmpty(), svgResult.diagnostics.joinToString("\n"))
        val native = svgResult.definitions.single { it.symbolId.value == "vendor.drive.native.element" }
        val svg = svgResult.definitions.single { it.symbolId.value == "vendor.drive.svg.element" }

        assertEquals(
            canonicalAtomicShapes(native.graphicBody.primitives),
            canonicalAtomicShapes(svg.graphicBody.primitives),
        )
        assertEquals(native.graphicBody.bounds, svg.graphicBody.bounds)
        assertEquals(native.bodyAuthority, svg.bodyAuthority)
        assertEquals(native.definitionKind, svg.definitionKind)
    }

    @Test
    fun `svg backed symbol keeps athena authored anchors for element export`() {
        val project = createTempProject()
        project.resolve("asset.svg").writeText(COMPLEX_SVG)

        val result = AthenaRepresentationSourceCompiler().compile(
            project.resolve("svg-symbol.athena").toString(),
            SVG_BACKED_SYMBOL_WITH_ANCHOR,
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val symbol = result.definitions.single { it.symbolId.value == "vendor.drive.svg.symbol" }
        val element = result.definitions.single { it.symbolId.value == "vendor.drive.svg.element" }
        assertEquals(listOf("powerIn"), symbol.anchors.map { it.anchorId.value })
        assertEquals(listOf("powerIn"), element.anchors.map { it.anchorId.value })
    }

    private fun createTempProject(): Path {
        val root = Files.createTempDirectory("athena-svg-geometry-test")
        root.resolve("source.athena").writeText("package athena.vendor")
        root.createDirectories()
        return root
    }

    private fun svgResource(): RepresentationResourceDeclaration =
        RepresentationResourceDeclaration(
            id = "vendor_svg",
            kind = RepresentationResourceKind.SVG,
            path = SymbolStringField("./asset.svg", span()),
            span = span(),
        )

    private fun flattenAtomicPrimitives(primitives: List<GraphicPrimitive>): List<GraphicPrimitive> = primitives.flatMap { primitive ->
        when (primitive) {
            is GraphicPrimitive.Group -> flattenAtomicPrimitives(primitive.children)
            is GraphicPrimitive.Transformed -> flattenAtomicPrimitives(listOf(primitive.child))
            else -> listOf(primitive)
        }
    }

    private fun canonicalAtomicShapes(primitives: List<GraphicPrimitive>): List<CanonicalAtomicShape> =
        flattenAtomicPrimitives(primitives).map { primitive ->
            when (primitive) {
                is GraphicPrimitive.Line -> CanonicalLine(
                    bounds = primitive.bounds,
                    start = primitive.start,
                    end = primitive.end,
                )
                else -> error("Unexpected primitive kind in SVG geometry test: ${primitive.kind}")
            }
        }

    private fun span(): SourceSpan = SourceSpan(
        start = SourcePosition(0, 1, 1),
        end = SourcePosition(1, 1, 2),
    )

    private companion object {
        val COMPLEX_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" width="120" height="60">
              <defs>
                <g id="terminal" data-athena-geometry-ref="terminal">
                  <line x1="0" y1="0" x2="0" y2="8"/>
                  <line x1="-3" y1="8" x2="3" y2="8"/>
                </g>
              </defs>
              <use href="#terminal" x="20" y="10"/>
              <use href="#terminal" transform="translate(60 10) rotate(90)"/>
            </svg>
        """.trimIndent()

        val INVALID_GEOMETRY_REF_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" width="80" height="40">
              <defs>
                <g id="terminal" data-athena-geometry-ref="wrong">
                  <line x1="0" y1="0" x2="0" y2="8"/>
                </g>
                <g id="terminal">
                  <line x1="0" y1="0" x2="0" y2="8"/>
                </g>
              </defs>
              <use href="#missing"/>
            </svg>
        """.trimIndent()

        val INVALID_METADATA_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" width="80" height="40" data-athena-schema="representation/v1">
              <g id="terminal" data-athena-geometry-ref="terminal">
                <line x1="0" y1="0" x2="0" y2="8" onclick="alert(1)"/>
              </g>
              <bad:node xmlns:bad="urn:bad"/>
              <use href="http://example.com/external.svg#terminal"/>
            </svg>
        """.trimIndent()

        val NATIVE_SOURCE = """
            package athena.vendor

            symbol vendor_drive_native_symbol {
              identity "vendor.drive.native.symbol"
              version "1.0.0"

              graphic {
                bounds (0, 0, 120, 60)
                line terminal1 from (20, 10) to (20, 18) style symbol
                line terminal2 from (17, 18) to (23, 18) style symbol
                line terminal3 from (60, 10) to (52, 10) style symbol
                line terminal4 from (52, 7) to (52, 13) style symbol
              }
            }

            element vendor_drive_native_element {
              identity "vendor.drive.native.element"
              version "1.0.0"
              bounds (0, 0, 120, 60)

              child body {
                symbol "vendor.drive.native.symbol"
                translate (0, 0)
                rotate 0
                scale (1, 1)
                zOrder 0
              }
            }
        """.trimIndent()

        val SVG_BACKED_SOURCE = """
            package athena.vendor

            element vendor_drive_svg_element {
              identity "vendor.drive.svg.element"
              version "1.0.0"

              resource vendor_svg {
                kind svg
                path "./asset.svg"
              }

              graphic svg resource vendor_svg
            }
        """.trimIndent()

        val SVG_BACKED_SYMBOL_WITH_ANCHOR = """
            package athena.vendor

            symbol vendor_drive_svg_symbol {
              identity "vendor.drive.svg.symbol"
              version "1.0.0"

              resource vendor_svg {
                kind svg
                path "./asset.svg"
              }

              graphic svg resource vendor_svg

              anchor powerIn {
                primitiveRef vendor_svg
                point (20, 10)
                role terminal
                accepts direction in
                accepts signal Power
              }
            }

            element vendor_drive_svg_element {
              identity "vendor.drive.svg.element"
              version "1.0.0"
              bounds (0, 0, 120, 60)

              child body {
                symbol "vendor.drive.svg.symbol"
                translate (0, 0)
                rotate 0
                scale (1, 1)
                zOrder 0
              }

              export anchor powerIn from body.powerIn
            }
        """.trimIndent()
    }

    private sealed interface CanonicalAtomicShape

    private data class CanonicalLine(
        val bounds: GraphicBounds,
        val start: GraphicPoint,
        val end: GraphicPoint,
    ) : CanonicalAtomicShape
}
