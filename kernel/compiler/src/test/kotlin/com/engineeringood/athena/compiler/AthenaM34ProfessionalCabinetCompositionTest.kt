package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.DrawingSheetAnchorReference
import com.engineeringood.athena.drawing.composition.DrawingSheetAxis
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionCompiler
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionPolicy
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionRequest
import com.engineeringood.athena.drawing.composition.DrawingSheetLabelBandIntent
import com.engineeringood.athena.drawing.composition.DrawingSheetLaneIntent
import com.engineeringood.athena.drawing.composition.DrawingSheetRailIntent
import com.engineeringood.athena.drawing.composition.DrawingSheetRouteChannelIntent
import com.engineeringood.athena.drawing.composition.DrawingSheetStructureAnchorInput
import com.engineeringood.athena.drawing.composition.DrawingSheetStructureCompiler
import com.engineeringood.athena.drawing.composition.DrawingSheetStructureLabelInput
import com.engineeringood.athena.drawing.composition.DrawingSheetStructurePolicy
import com.engineeringood.athena.drawing.composition.DrawingSheetStructureRequest
import com.engineeringood.athena.drawing.composition.DrawingSheetStructureSubjectInput
import com.engineeringood.athena.drawing.composition.DrawingSheetTerminalStripIntent
import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStager
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgAdapter
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgPalette
import com.engineeringood.athena.renderer.svg.GraphicPrimitiveSvgRenderRequest
import com.engineeringood.athena.representation.DrawingSymbolAnchorId
import com.engineeringood.athena.representation.DrawingSymbolIdentity
import com.engineeringood.athena.representation.DrawingSymbolSlotId
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertContains
import kotlin.test.assertTrue

class AthenaM34ProfessionalCabinetCompositionTest {
    @Test
    fun `m34 sample composes a professional governed cabinet drawing`() {
        val sampleRoot = Files.createTempDirectory("athena-m34-professional-cabinet")
        copyProfessionalCabinetTree(professionalCabinetRepositoryRoot().resolve("examples/m34/sample-project"), sampleRoot)
        val projectSource = Files.readString(sampleRoot.resolve("src/com/engineeringood/m34/sample/01-native-cabinet-proof.athena"))

        REQUIRED_SAMPLE_DEVICES.forEach { device ->
            assertTrue(projectSource.contains("device $device "), "M34 Cabinet sample is missing device $device.")
        }
        assertEquals(13, Regex("""\bdevice\s+[A-Za-z0-9_]+\s*\{""").findAll(projectSource).count())
        assertTrue(projectSource.contains("connect cabinet_control_chain {"))
        assertTrue(projectSource.contains("layout cabinet {"))
        assertFalse(projectSource.contains("layout schematic-sheet"))
        assertFalse(projectSource.contains("layout documentation"))

        val compilation = AthenaCompiler().compile(sampleRoot.resolve("src/com/engineeringood/m34/sample/01-native-cabinet-proof.athena"))
        assertTrue(compilation is CompilerCompilationSuccess, compilation.toString())
        assertTrue(compilation.diagnosticMessages().isEmpty(), compilation.diagnosticMessages().joinToString("\n"))

        val staged = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/m34-professional-cabinet"),
        )
        assertTrue(staged.diagnostics.isEmpty(), staged.diagnostics.toString())
        val packageCompilation = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))
        assertTrue(packageCompilation.diagnostics.isEmpty(), packageCompilation.diagnostics.toString())
        assertTrue(packageCompilation.proof.xmlRuntimeAuthorityAbsent)
        assertTrue(packageCompilation.proof.rawSvgTransportAbsent)

        val bindings = packageCompilation.bindingRules.associateBy { rule -> rule.ruleId.value }
        REQUIRED_BINDINGS.forEach { (ruleId, elementId) ->
            val binding = assertNotNull(bindings[ruleId], "Missing Cabinet binding $ruleId.")
            assertEquals("CabinetIEC", binding.profileId.value)
            assertEquals("cabinet", binding.projectionContext.value)
            assertEquals(elementId, binding.target.descriptorId.value)
        }

        val definitions = packageCompilation.definitions
            .filter { definition -> definition.definitionKind == RepresentationDefinitionKind.ELEMENT }
            .associateBy { definition -> definition.symbolId.value }
        val plan = PROFESSIONAL_CABINET_PLAN.map { occurrence ->
            val definition = assertNotNull(definitions[occurrence.elementId], "Missing compiled element ${occurrence.elementId}.")
            occurrence.toSubject(definition)
        }
        val elementIdsInPlan = PROFESSIONAL_CABINET_PLAN.map { it.elementId }.toSet()
        assertTrue(elementIdsInPlan.containsAll(REQUIRED_COMPOSITION_ELEMENTS), elementIdsInPlan.toString())

        val contentBounds = union(plan.map { it.bounds })
        val sheetId = ProjectionSheetId("cabinet/sheet/m34-professional")
        val sheetPlan = requireNotNull(DrawingSheetCompositionCompiler().compile(
            DrawingSheetCompositionRequest(
                sheetId = sheetId,
                publication = ProjectionSheetPublication.defaultFor(sheetId, "M34 Professional Cabinet", 0, emptyList()),
                contentBounds = contentBounds,
                policy = DrawingSheetCompositionPolicy(
                    policyId = "m34-professional-cabinet-sheet",
                    contentToFrame = 24.0,
                    frameToSheet = 10.0,
                    titleBlockHeight = 34.0,
                    maximumSheetWidth = 900.0,
                    maximumSheetHeight = 720.0,
                    columnLabels = listOf("A", "B", "C", "D", "E"),
                    rowLabels = listOf("1", "2", "3", "4"),
                ),
            ),
        ).plan)

        val structure = DrawingSheetStructureCompiler().compile(
            DrawingSheetStructureRequest(
                sheetPlan = sheetPlan,
                subjects = plan,
                rails = listOf(
                    DrawingSheetRailIntent("rail:upper", DrawingSheetAxis.HORIZONTAL, GraphicPoint(62.0, 132.0), GraphicPoint(584.0, 132.0), listOf("device:MainPowerSupplyPS34", "device:MainBreakerQF34", "device:WallSwitchS34", "device:ControlRelayK34")),
                    DrawingSheetRailIntent("rail:lower", DrawingSheetAxis.HORIZONTAL, GraphicPoint(62.0, 300.0), GraphicPoint(584.0, 300.0), listOf("device:FieldTerminalXT34", "device:ShutterMotorM34", "device:PilotLampHL34", "device:SpareTerminalXT34")),
                ),
                lanes = listOf(
                    DrawingSheetLaneIntent("lane:upper-devices", DrawingSheetAxis.HORIZONTAL, GraphicBounds(58.0, 92.0, 540.0, 148.0), listOf("device:MainPowerSupplyPS34", "device:MainBreakerQF34", "device:WallSwitchS34", "device:ControlRelayK34")),
                    DrawingSheetLaneIntent("lane:lower-devices", DrawingSheetAxis.HORIZONTAL, GraphicBounds(58.0, 260.0, 540.0, 132.0), listOf("device:FieldTerminalXT34", "device:ShutterMotorM34", "device:PilotLampHL34", "device:SpareTerminalXT34")),
                    DrawingSheetLaneIntent("lane:enclosure", DrawingSheetAxis.VERTICAL, GraphicBounds(32.0, 32.0, 600.0, 420.0), listOf("device:CabinetEnclosure34")),
                    DrawingSheetLaneIntent("lane:rails", DrawingSheetAxis.VERTICAL, GraphicBounds(72.0, 120.0, 520.0, 120.0), listOf("device:UpperRail34", "device:LowerRail34")),
                    DrawingSheetLaneIntent("lane:route-channel", DrawingSheetAxis.HORIZONTAL, GraphicBounds(72.0, 424.0, 500.0, 32.0), listOf("device:RouteChannel34")),
                    DrawingSheetLaneIntent("lane:title", DrawingSheetAxis.HORIZONTAL, GraphicBounds(78.0, 58.0, 40.0, 40.0), listOf("device:CabinetTitleLabel34")),
                ),
                terminalStrips = listOf(DrawingSheetTerminalStripIntent("strip:FieldTerminalXT34", listOf("device:FieldTerminalXT34", "device:SpareTerminalXT34"))),
                labelBands = listOf(
                    DrawingSheetLabelBandIntent("band:title", GraphicBounds(72.0, 52.0, 260.0, 40.0), listOf("label:CabinetTitleLabel34")),
                    DrawingSheetLabelBandIntent("band:devices", GraphicBounds(360.0, 308.0, 160.0, 64.0), listOf("label:PilotLampHL34")),
                    DrawingSheetLabelBandIntent("band:terminals", GraphicBounds(66.0, 386.0, 520.0, 24.0), emptyList()),
                ),
                routeChannels = listOf(
                    DrawingSheetRouteChannelIntent(
                        "channel:control-wiring",
                        DrawingSheetAxis.HORIZONTAL,
                        GraphicBounds(70.0, 424.0, 500.0, 32.0),
                        listOf(
                            DrawingSheetAnchorReference("device:MainPowerSupplyPS34", DrawingSymbolAnchorId("output")),
                            DrawingSheetAnchorReference("device:MainBreakerQF34", DrawingSymbolAnchorId("line")),
                            DrawingSheetAnchorReference("device:MainBreakerQF34", DrawingSymbolAnchorId("load")),
                            DrawingSheetAnchorReference("device:ControlRelayK34", DrawingSymbolAnchorId("supply")),
                            DrawingSheetAnchorReference("device:FieldTerminalXT34", DrawingSymbolAnchorId("field")),
                            DrawingSheetAnchorReference("device:ShutterMotorM34", DrawingSymbolAnchorId("up")),
                        ),
                    ),
                ),
                policy = DrawingSheetStructurePolicy("m34.professional-cabinet-v1", terminalStripPadding = 10.0, maximumSubjectGap = 120.0),
            ),
        )
        assertTrue(structure.isValid, structure.diagnostics.toString())
        val proof = assertNotNull(structure.proof)
        assertEquals(listOf("rail:lower", "rail:upper"), proof.railIds)
        assertEquals(listOf("strip:FieldTerminalXT34"), proof.terminalStripIds)
        assertEquals(listOf("channel:control-wiring"), proof.routeChannelIds)
        assertTrue(sheetPlan.drawingAreaBounds.containsBounds(contentBounds))
        assertTrue(VIEWPORTS.all { viewport -> fits(contentBounds, viewport) })

        val fragments = PROFESSIONAL_CABINET_PLAN.map { occurrence ->
            val document = assertNotNull(definitions[occurrence.elementId]).graphicBody
            val fragment = GraphicPrimitiveSvgAdapter().render(GraphicPrimitiveSvgRenderRequest(document, INDUSTRIAL_PALETTE))
            assertTrue(fragment.isValid, fragment.diagnostics.toString())
            assertFalse(assertNotNull(fragment.proof).normalChromeVisible)
            assertFalse(assertNotNull(fragment.fragment).contains("stroke-dasharray"), "Normal Cabinet chrome must not include active dotted borders.")
            document
        }
        assertTrue(fragments.all { document -> document.bounds != null })
    }

    @Test
    fun `m34 sample derives live control drawing presentation from athena package-backed materials`() {
        val sampleRoot = Files.createTempDirectory("athena-m34-live-presentation")
        copyProfessionalCabinetTree(professionalCabinetRepositoryRoot().resolve("examples/m34/professional-control-drawing"), sampleRoot)

        val compilation = AthenaCompiler().compile(sampleRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena"))
        assertTrue(compilation is CompilerCompilationSuccess, compilation.toString())
        assertTrue(compilation.diagnosticMessages().isEmpty(), compilation.diagnosticMessages().joinToString("\n"))

        val presentation = compilation.presentations.single { document -> document.view.id == "schematic" }
        assertEquals("Control Drawing", presentation.view.displayName)
        assertTrue(presentation.graphicOccurrences.isNotEmpty(), "M34 Control Drawing must publish live package-backed graphic occurrences.")
        assertTrue(
            presentation.graphicOccurrences.all { occurrence ->
                occurrence.packageId.isNotBlank() &&
                    occurrence.definitionId.isNotBlank() &&
                    occurrence.sourceProvenance.all { source ->
                        !source.endsWith(".xml") &&
                            !source.endsWith(".elmt") &&
                            !source.contains("qelectrotech", ignoreCase = true)
                    }
            },
            "M34 Control Drawing graphic occurrences must carry package/material identity without raw legacy authority.",
        )
        assertFalse(
            presentation.representationFacts.any { fact ->
                fact.anatomy.representationId.value.startsWith("athena-industrial-control-v0:")
            },
            "M34 Control Drawing must not publish legacy fallback representation facts.",
        )
        assertEquals(22, presentation.graphicOccurrences.size)
        assertEquals(34, presentation.routeFactSnapshot?.routeFacts?.size)
        assertNotNull(presentation.drawingComposition, "M34 Control Drawing sample should derive a drawing composition.")
        assertTrue(presentation.drawingComposition!!.structureSubjects.isNotEmpty())
        val structureKinds = presentation.drawingComposition!!.structureFacts.map { it.kind }.toSet()
        assertContains(structureKinds, "rail")
        assertContains(structureKinds, "lane")
        assertContains(structureKinds, "terminal-strip")
        assertContains(structureKinds, "label-band")
        assertContains(structureKinds, "route-channel")
        assertTrue(presentation.canvasWidth > 0)
        assertTrue(presentation.canvasHeight > 0)
    }
}

private data class CabinetOccurrence(
    val subjectId: String,
    val elementId: String,
    val x: Double,
    val y: Double,
) {
    fun toSubject(definition: RepresentationDefinition): DrawingSheetStructureSubjectInput {
        val bounds = requireNotNull(definition.graphicBody.bounds)
        return DrawingSheetStructureSubjectInput(
            subjectId = subjectId,
            representationIdentity = DrawingSymbolIdentity(elementId),
            bounds = GraphicBounds(x, y, bounds.width, bounds.height),
            anchors = definition.anchors.map { anchor ->
                DrawingSheetStructureAnchorInput(
                    DrawingSymbolAnchorId(anchor.anchorId.value),
                    GraphicPoint(x + anchor.point.x, y + anchor.point.y),
                )
            },
            requiredAnchorIds = definition.anchors.map { DrawingSymbolAnchorId(it.anchorId.value) }.toSet(),
            labels = definition.labelSlots.map { slot ->
                DrawingSheetStructureLabelInput(
                    "label:${subjectId.removePrefix("device:")}",
                    DrawingSymbolSlotId(slot.slotId.value),
                    GraphicBounds(x + 8.0, y + 8.0, max(24.0, bounds.width - 16.0), 18.0),
                )
            },
            requiredLabelSlotIds = definition.labelSlots.map { DrawingSymbolSlotId(it.slotId.value) }.toSet(),
        )
    }
}

private val REQUIRED_SAMPLE_DEVICES = setOf(
    "MainPowerSupplyPS34",
    "MainBreakerQF34",
    "WallSwitchS34",
    "ControlRelayK34",
    "FieldTerminalXT34",
    "ShutterMotorM34",
    "PilotLampHL34",
    "SpareTerminalXT34",
    "CabinetEnclosure34",
    "UpperRail34",
    "LowerRail34",
    "RouteChannel34",
    "CabinetTitleLabel34",
)

private val REQUIRED_BINDINGS = mapOf(
    "M34PowerSupplyCabinet" to "cabinet.power_supply.element",
    "M34ProtectiveDeviceCabinet" to "cabinet.protective_device.element",
    "M34SwitchControlCabinet" to "cabinet.switch_control.element",
    "M34RelayContactorCabinet" to "cabinet.relay_contactor.element",
    "M34TerminalBlockCabinet" to "cabinet.terminal_block.element",
    "M34ActuatorLoadCabinet" to "cabinet.actuator_load.element",
    "M34PilotLampCabinet" to "cabinet.label.element",
    "M34CabinetTitleLabel" to "cabinet.label.element",
    "M34CabinetEnclosure" to "cabinet.enclosure.element",
    "M34CabinetDinRail" to "cabinet.din_rail.element",
    "M34CabinetRouteChannel" to "cabinet.route_channel.element",
)

private val REQUIRED_COMPOSITION_ELEMENTS = setOf(
    "cabinet.enclosure.element",
    "cabinet.din_rail.element",
    "cabinet.protective_device.element",
    "cabinet.switch_control.element",
    "cabinet.relay_contactor.element",
    "cabinet.terminal_block.element",
    "cabinet.power_supply.element",
    "cabinet.actuator_load.element",
    "cabinet.label.element",
    "cabinet.route_channel.element",
)

private val PROFESSIONAL_CABINET_PLAN = listOf(
    CabinetOccurrence("device:CabinetEnclosure34", "cabinet.enclosure.element", 32.0, 32.0),
    CabinetOccurrence("device:UpperRail34", "cabinet.din_rail.element", 72.0, 120.0),
    CabinetOccurrence("device:LowerRail34", "cabinet.din_rail.element", 72.0, 180.0),
    CabinetOccurrence("device:MainPowerSupplyPS34", "cabinet.power_supply.element", 82.0, 144.0),
    CabinetOccurrence("device:MainBreakerQF34", "cabinet.protective_device.element", 222.0, 132.0),
    CabinetOccurrence("device:WallSwitchS34", "cabinet.switch_control.element", 326.0, 148.0),
    CabinetOccurrence("device:ControlRelayK34", "cabinet.relay_contactor.element", 454.0, 136.0),
    CabinetOccurrence("device:FieldTerminalXT34", "cabinet.terminal_block.element", 104.0, 312.0),
    CabinetOccurrence("device:ShutterMotorM34", "cabinet.actuator_load.element", 228.0, 312.0),
    CabinetOccurrence("device:PilotLampHL34", "cabinet.label.element", 382.0, 324.0),
    CabinetOccurrence("device:SpareTerminalXT34", "cabinet.terminal_block.element", 504.0, 312.0),
    CabinetOccurrence("device:RouteChannel34", "cabinet.route_channel.element", 72.0, 424.0),
    CabinetOccurrence("device:CabinetTitleLabel34", "cabinet.label.element", 78.0, 58.0),
)

private val VIEWPORTS = listOf(
    GraphicBounds(0.0, 0.0, 900.0, 720.0),
    GraphicBounds(0.0, 0.0, 640.0, 520.0),
)

private val INDUSTRIAL_PALETTE = GraphicPrimitiveSvgPalette(
    mapOf(
        "drawing.foreground" to "#202020",
        "foreground" to "#202020",
    ),
    "#ffffff",
)

private fun fits(bounds: GraphicBounds, viewport: GraphicBounds): Boolean {
    val scale = min(viewport.width / bounds.width, viewport.height / bounds.height)
    return bounds.width * scale <= viewport.width && bounds.height * scale <= viewport.height
}

private fun GraphicBounds.containsBounds(other: GraphicBounds): Boolean =
    other.x >= x && other.y >= y && other.x + other.width <= x + width && other.y + other.height <= y + height

private fun union(bounds: List<GraphicBounds>): GraphicBounds {
    val left = bounds.minOf { it.x }
    val top = bounds.minOf { it.y }
    val right = bounds.maxOf { it.x + it.width }
    val bottom = bounds.maxOf { it.y + it.height }
    return GraphicBounds(left, top, right - left, bottom - top)
}

private fun professionalCabinetRepositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath().normalize()
    while (current.parent != null) {
        if (Files.exists(current.resolve("settings.gradle.kts")) && Files.exists(current.resolve("examples"))) return current
        current = current.parent
    }
    error("Could not locate Athena repository root.")
}

private fun copyProfessionalCabinetTree(sourceRoot: Path, targetRoot: Path) {
    Files.walk(sourceRoot).use { paths ->
        paths.forEach { source ->
            val target = targetRoot.resolve(sourceRoot.relativize(source))
            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                target.parent?.let(Files::createDirectories)
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
