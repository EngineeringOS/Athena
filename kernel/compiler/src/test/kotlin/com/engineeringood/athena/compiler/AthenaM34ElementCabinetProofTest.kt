package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionCompiler
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionPolicy
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionRequest
import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStager
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgAdapter
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgCanvasComposer
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgCanvasRequest
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgPalette
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgRenderRequest
import com.engineeringood.athena.representation.RepresentationBindingCompiler
import com.engineeringood.athena.representation.RepresentationBindingRequest
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationFallbackBehavior
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationPolicy
import com.engineeringood.athena.representation.RepresentationPolicyId
import com.engineeringood.athena.representation.RepresentationPolicyPriority
import com.engineeringood.athena.representation.RepresentationProjectionKind
import com.engineeringood.athena.representation.RepresentationProjectionOccurrenceId
import com.engineeringood.athena.representation.RepresentationSemanticRole
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSubjectKind
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.SymbolFamilyId
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.GraphicPrimitiveTransportPayload
import com.engineeringood.athena.representation.RepresentationLabelSlotId
import com.engineeringood.athena.representation.toM34CabinetRenderPathProof
import com.engineeringood.athena.representation.toTransportPayload
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM34ElementCabinetProofTest {
    @Test
    fun `compiled element binds composes and renders through typed cabinet path`() {
        val compiled = AthenaRepresentationSourceCompiler().compile(
            "m34-cabinet-library.athena",
            AthenaElementSourceCompilerTest.VALID_MIXED_SOURCE,
        )
        assertTrue(compiled.diagnostics.isEmpty(), compiled.diagnostics.toString())
        val sourceDefinition = compiled.definitions.single { it.definitionKind == RepresentationDefinitionKind.ELEMENT }
        val proofDefinition = sourceDefinition.copy(kind = RepresentationSymbolKind.SWITCH_CONTACT)
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("m34-evidence-only"),
            projectionKind = RepresentationProjectionKind.CABINET,
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("cabinet-control-module"),
            occurrenceRole = RepresentationOccurrenceRole.SWITCH_CONTACT,
            symbolFamilyId = SymbolFamilyId("m34.switch-module"),
            symbolId = proofDefinition.symbolId,
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val binding = RepresentationBindingCompiler().bind(
            RepresentationBindingRequest(
                canonicalSemanticId = RepresentationSubjectId("device:ControlModuleK34"),
                projectionOccurrenceId = RepresentationProjectionOccurrenceId("cabinet:main/occurrence:ControlModuleK34"),
                subjectKind = RepresentationSubjectKind.COMPONENT,
                semanticRole = RepresentationSemanticRole("cabinet-control-module"),
                projectionKind = RepresentationProjectionKind.CABINET,
                policy = policy,
                definition = proofDefinition,
                labelValues = emptyMap(),
                terminalPorts = emptyMap(),
                priority = RepresentationPolicyPriority(100),
            ),
        )
        assertTrue(binding.diagnostics.isEmpty(), binding.diagnostics.toString())

        val document = proofDefinition.graphicBody
        val sheetId = ProjectionSheetId("cabinet/sheet/01")
        val composition = DrawingSheetCompositionCompiler().compile(
            DrawingSheetCompositionRequest(
                sheetId = sheetId,
                publication = ProjectionSheetPublication.defaultFor(sheetId, "M34 Cabinet", 0, emptyList()),
                contentBounds = document.bounds,
                policy = DrawingSheetCompositionPolicy(
                    policyId = "m34-cabinet-evidence",
                    contentToFrame = 12.0,
                    frameToSheet = 6.0,
                    titleBlockHeight = 24.0,
                    maximumSheetWidth = 1000.0,
                    maximumSheetHeight = 1000.0,
                    columnLabels = listOf("A", "B"),
                    rowLabels = listOf("1", "2"),
                ),
            ),
        )
        assertTrue(composition.isValid, composition.diagnostics.toString())

        val palette = GraphicPrimitiveSvgPalette(mapOf("drawing.foreground" to "#202020"), "#ffffff")
        val fragment = GraphicPrimitiveSvgAdapter().render(GraphicPrimitiveSvgRenderRequest(document, palette))
        val canvas = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(document, fragment, margin = 8.0),
        )
        val transport = document.toTransportPayload()
        val renderPathProof = document.toM34CabinetRenderPathProof(
            documentViewBoxAuthority = assertNotNull(canvas.evidence).boundsAuthority,
            adapterAuthority = assertNotNull(fragment.evidence).documentId,
            xmlRuntimeAuthorityAbsent = true,
            rawMarkupAuthorityAbsent = true,
            fallbackAuthorityAbsent = true,
            hardCodedDocumentBoundsAbsent = true,
            presentationPrimitiveActiveProducerAbsent = true,
            compatibilityLedger = mapOf("PresentationPrimitive" to "compatibility-only; no active M34 Cabinet producer"),
        )

        assertTrue(fragment.isValid, fragment.diagnostics.toString())
        assertTrue(canvas.isValid, canvas.diagnostics.toString())
        assertFalse(assertNotNull(fragment.evidence).normalChromeVisible)
        assertTrue(assertNotNull(canvas.svg).contains("data-athena-render-authority=\"graphic-primitive-ir\""))
        assertTrue(canvas.svg!!.contains("data-athena-primitive-id=\"primary.line\""))
        assertEquals("graphic-primitive-ir", canvas.evidence?.boundsAuthority)
        assertEquals(document.bounds, composition.evidence?.contentBounds)
        assertEquals("m34-cabinet-library.athena", sourceDefinition.lifecycle.provenance.source)
        assertEquals(listOf("primary", "secondary"), sourceDefinition.intrinsicComposition?.children?.map { it.childId.value })
        assertEquals(listOf(0, 1), sourceDefinition.intrinsicComposition?.children?.map { it.zOrder })
        assertEquals(4, sourceDefinition.anchors.size)
        assertEquals(document.documentId?.value, transport.documentId)
        assertEquals(document.primitives.size, transport.primitives.size)
        assertTrue(transport.primitives.all { it.kind in setOf("group", "transform") })
        assertTrue(transport.primitives.nestedKinds().all { it in TYPED_GRAPHIC_PRIMITIVE_KINDS })
        assertTrue(renderPathProof.accepted, renderPathProof.toString())
        assertEquals("graphic-primitive-document", renderPathProof.visualTransportKind)
        assertEquals("graphic-primitive-ir", renderPathProof.rendererInputAuthority)
        assertEquals("typed-graphic-primitives", renderPathProof.payloadAuthority)
        assertEquals(0, renderPathProof.rawMarkupSinkCount)
        assertEquals(0, renderPathProof.fallbackComponentCount)
        assertEquals(0, renderPathProof.hardCodedDocumentBoundsCount)
        assertTrue(renderPathProof.presentationPrimitiveActiveProducerAbsent)
        assertEquals(
            "device:ControlModuleK34@cabinet:main/occurrence:ControlModuleK34",
            binding.occurrence.occurrenceId.value,
        )
        assertTrue(compiled.definitions.none { it.bodyAuthority.name.contains("LEGACY") })
    }

    @Test
    fun `referenced svg element binds composes and renders through typed cabinet path`() {
        val sampleRoot = Files.createTempDirectory("athena-m34-cabinet-evidence-repo")
        copyTree(repositoryRoot().resolve("examples/m34/sample-project"), sampleRoot)
        val staged = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/m34-cabinet-evidence"),
        )
        assertTrue(staged.diagnostics.isEmpty(), staged.diagnostics.toString())
        val compiled = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))
        assertTrue(compiled.diagnostics.isEmpty(), compiled.diagnostics.toString())
        val sourceDefinition = compiled.definitions.single { it.symbolId.value == "vendor.drive.element" }
        val proofDefinition = sourceDefinition.copy(kind = RepresentationSymbolKind.SWITCH_CONTACT)
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("m34-svg-evidence-only"),
            projectionKind = RepresentationProjectionKind.CABINET,
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("cabinet-vendor-drive"),
            occurrenceRole = RepresentationOccurrenceRole.SWITCH_CONTACT,
            symbolFamilyId = SymbolFamilyId("m34.vendor-drive"),
            symbolId = proofDefinition.symbolId,
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val binding = RepresentationBindingCompiler().bind(
            RepresentationBindingRequest(
                canonicalSemanticId = RepresentationSubjectId("device:VendorDriveG34"),
                projectionOccurrenceId = RepresentationProjectionOccurrenceId("cabinet:main/occurrence:VendorDriveG34"),
                subjectKind = RepresentationSubjectKind.COMPONENT,
                semanticRole = RepresentationSemanticRole("cabinet-vendor-drive"),
                projectionKind = RepresentationProjectionKind.CABINET,
                policy = policy,
                definition = proofDefinition,
                labelValues = mapOf(RepresentationLabelSlotId("deviceTag") to LabelValue("G34")),
                terminalPorts = emptyMap(),
                priority = RepresentationPolicyPriority(100),
            ),
        )
        assertTrue(binding.diagnostics.isEmpty(), binding.diagnostics.toString())

        val document = proofDefinition.graphicBody
        val sheetId = ProjectionSheetId("cabinet/sheet/svg-evidence")
        val composition = DrawingSheetCompositionCompiler().compile(
            DrawingSheetCompositionRequest(
                sheetId = sheetId,
                publication = ProjectionSheetPublication.defaultFor(sheetId, "M34 SVG Cabinet", 0, emptyList()),
                contentBounds = document.bounds,
                policy = DrawingSheetCompositionPolicy(
                    policyId = "m34-svg-cabinet-evidence",
                    contentToFrame = 12.0,
                    frameToSheet = 6.0,
                    titleBlockHeight = 24.0,
                    maximumSheetWidth = 1000.0,
                    maximumSheetHeight = 1000.0,
                    columnLabels = listOf("A", "B"),
                    rowLabels = listOf("1", "2"),
                ),
            ),
        )
        assertTrue(composition.isValid, composition.diagnostics.toString())

        val palette = GraphicPrimitiveSvgPalette(
            mapOf(
                "drawing.foreground" to "#202020",
                "foreground" to "#202020",
                "symbol" to "#202020",
                "device-label" to "#202020",
            ),
            "#ffffff",
        )
        val fragment = GraphicPrimitiveSvgAdapter().render(GraphicPrimitiveSvgRenderRequest(document, palette))
        val canvas = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(document, fragment, margin = 8.0),
        )
        assertTrue(fragment.isValid, fragment.diagnostics.toString())
        assertTrue(canvas.isValid, canvas.diagnostics.toString())
        assertFalse(assertNotNull(fragment.evidence).normalChromeVisible)
        assertTrue(assertNotNull(canvas.svg).contains("data-athena-render-authority=\"graphic-primitive-ir\""))
        assertTrue(canvas.svg!!.contains("data-athena-primitive-kind=\"rectangle\""))
        assertTrue(canvas.svg!!.contains("data-athena-primitive-kind=\"line\""))
        val transport = document.toTransportPayload()
        val renderPathProof = document.toM34CabinetRenderPathProof(
            documentViewBoxAuthority = assertNotNull(canvas.evidence).boundsAuthority,
            adapterAuthority = assertNotNull(fragment.evidence).documentId,
            xmlRuntimeAuthorityAbsent = compiled.evidence.xmlRuntimeAuthorityAbsent,
            rawMarkupAuthorityAbsent = compiled.evidence.rawSvgTransportAbsent,
            fallbackAuthorityAbsent = true,
            hardCodedDocumentBoundsAbsent = true,
            presentationPrimitiveActiveProducerAbsent = true,
            compatibilityLedger = mapOf("PresentationPrimitive" to "compatibility-only; no active M34 Cabinet producer"),
        )
        assertEquals("graphic-primitive-ir", canvas.evidence?.boundsAuthority)
        assertEquals(document.bounds, composition.evidence?.contentBounds)
        assertTrue(sourceDefinition.lifecycle.provenance.source.contains(".athena"))
        assertTrue(sourceDefinition.graphicBody.provenanceSources.all { it.contains(".athena") })
        assertEquals(staged.snapshot?.snapshotId, compiled.evidence.snapshotId)
        assertTrue(compiled.evidence.dependencyLockDigest.startsWith("sha256:"))
        assertTrue(compiled.evidence.rendererFileAccessAuthorityAbsent)
        assertTrue(compiled.evidence.xmlRuntimeAuthorityAbsent)
        assertTrue(compiled.evidence.rawSvgTransportAbsent)
        assertEquals(document.documentId?.value, transport.documentId)
        assertTrue(transport.primitives.nestedKinds().containsAll(listOf("rectangle", "line")))
        assertTrue(renderPathProof.accepted, renderPathProof.toString())
        assertEquals("graphic-primitive-document", renderPathProof.visualTransportKind)
        assertEquals("graphic-primitive-ir", renderPathProof.rendererInputAuthority)
        assertEquals("typed-graphic-primitives", renderPathProof.payloadAuthority)
        assertEquals(0, renderPathProof.rawMarkupSinkCount)
        assertEquals(0, renderPathProof.fallbackComponentCount)
        assertEquals(0, renderPathProof.hardCodedDocumentBoundsCount)
        assertTrue(renderPathProof.presentationPrimitiveActiveProducerAbsent)
        assertTrue(sourceDefinition.anchors.isEmpty())
        assertEquals(setOf("deviceTag"), sourceDefinition.labelSlots.map { it.slotId.value }.toSet())
        assertTrue(compiled.definitions.none { it.bodyAuthority.name.contains("LEGACY") })
    }
}

private fun repositoryRoot(): java.nio.file.Path {
    var current = java.nio.file.Path.of("").toAbsolutePath()
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

private fun List<GraphicPrimitiveTransportPayload>.nestedKinds(): List<String> =
    flatMap { primitive -> listOf(primitive.kind) + primitive.children.nestedKinds() }

private val TYPED_GRAPHIC_PRIMITIVE_KINDS = setOf(
    "line",
    "polyline",
    "arc",
    "circle",
    "rectangle",
    "text",
    "marker",
    "connection-dot",
    "reference-arrow",
    "group",
    "transform",
)
