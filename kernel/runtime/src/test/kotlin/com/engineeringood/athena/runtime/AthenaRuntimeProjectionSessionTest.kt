package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.layout.ProjectionInteractivity
import com.engineeringood.athena.presentation.PresentationSvgPath
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaRuntimeProjectionSessionTest {
    @Test
    fun `runtime hosts supported projection views with deterministic default active view`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val session = context.projectProjectionSession()
        val cabinetView = session.supportedViews.first { view -> view.viewId == "cabinet" }
        val wiringView = session.supportedViews.first { view -> view.viewId == "wiring" }

        assertEquals("demo-cabinet", session.projectName)
        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            session.supportedViews.map { view -> view.viewId },
        )
        assertEquals("cabinet", session.activeViewId)
        assertEquals("electrical/cabinet", cabinetView.familyId)
        assertEquals("electrical/wiring", wiringView.familyId)
        assertEquals(ProjectionInteractivity.INTERACTIVE, cabinetView.ownershipContract.interactivity)
        assertEquals(
            listOf("adjust-layout-placement", "adjust-layout-grouping"),
            cabinetView.ownershipContract.projectionCommandIds,
        )
        assertEquals(
            listOf("navigate-view", "inspect-selection", "preview-related-elements"),
            cabinetView.ownershipContract.transientInteractionKinds,
        )
        assertEquals(
            listOf("layout-placement", "layout-group-membership"),
            cabinetView.ownershipContract.persistedProjectionMetadataKeys,
        )
        assertEquals(ProjectionInteractivity.INSPECT_ONLY, wiringView.ownershipContract.interactivity)
        assertTrue(wiringView.ownershipContract.projectionCommandIds.isEmpty())
        assertEquals(
            listOf("navigate-view", "inspect-selection", "preview-related-elements"),
            wiringView.ownershipContract.transientInteractionKinds,
        )
        val cabinetSession = assertIs<AthenaRuntimeProjectionSwitchSuccess>(
            context.switchActiveProjectionView("cabinet"),
        ).session
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(cabinetSession.activeProjection)
        assertEquals("cabinet", ready.viewId)
        assertEquals("electrical/cabinet", ready.familyId)
        assertEquals("DemoCabinet", ready.scene.systemName)
        assertEquals(480, ready.scene.canvasWidth)
        assertEquals(172, ready.scene.canvasHeight)
        assertEquals("cabinet/projection/node/component_PLC1", ready.scene.components.first { component -> component.semanticId == "component:PLC1" }.projectionId)
        assertEquals("electrical-notation/cabinet/default-v1", ready.notationPack?.packId)
        assertTrue(ready.notationPack?.subjects?.any { subject -> subject.semanticId == "component:PLC1" && subject.symbolKey == "device.cabinet.default" } == true)
        assertEquals("cabinet/sheet/01-main", ready.activeSheetId)
        assertEquals(listOf("cabinet/sheet/01-main"), ready.sheets.map { sheet -> sheet.sheetId })
        assertEquals(
            listOf("component:M1", "component:PLC1", "connection:PLC1.out->M1.in", "port:M1.in", "port:PLC1.out"),
            ready.sheets.single().subjectSemanticIds,
        )
        assertEquals(listOf("electrical-runtime.render.cabinet"), ready.activeRenderContributions.map { contribution -> contribution.contributionId })
        assertEquals(2, ready.electricalAnchors.size)
        assertEquals(2, ready.electricalConnectionEndpoints.size)
        assertEquals(1, ready.electricalRoutingCorridors.size)
        val presentation = assertNotNull(ready.presentation)
        assertEquals(1, presentation.primitivePacks.count { pack -> "electrical/cabinet" in pack.familyIds })
        assertIs<PresentationSvgPath>(
            presentation.primitivePacks
                .flatMap { pack -> pack.primitives }
                .first { primitive -> primitive.primitiveId.value == "electrical.mark.contact-open" }
                .commands
                .first()
        )
        assertTrue(ready.electricalAnchors.any { anchor -> anchor.portSemanticId == "port:PLC1.out" && anchor.ownerSemanticId == "component:PLC1" })
        assertTrue(ready.electricalConnectionEndpoints.any { endpoint -> endpoint.endpointRole == "source" && endpoint.portSemanticId == "port:PLC1.out" })
        assertEquals(
            ready.scene.connections.single().projectionId,
            ready.electricalRoutingCorridors.single().projectionConnectionId,
        )
        assertEquals("orthogonal", ready.electricalRoutingCorridors.single().routingStyle)
        assertEquals(2, ready.scene.components.size)
        assertEquals(1, ready.scene.connections.size)
        assertEquals(2, ready.scene.labels.size)
    }

    @Test
    fun `graph workbench defaults to cabinet while documentation remains an explicit projection`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val session = context.projectProjectionSession()
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(session.activeProjection)

        assertEquals("cabinet", session.activeViewId)
        assertEquals("cabinet", ready.viewId)
        assertEquals("electrical/cabinet", ready.familyId)
        assertEquals("cabinet/sheet/01-main", ready.activeSheetId)
        assertEquals(1, ready.sheets.size)
    }

    @Test
    fun `switching active view stays runtime owned and preserves canonical semantic state`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        val baselineDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document

        val switchResult = context.switchActiveProjectionView("wiring")

        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        assertEquals("demo-cabinet", success.projectName)
        assertEquals("wiring", success.requestedViewId)
        assertEquals("wiring", success.session.activeViewId)
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)
        assertEquals("wiring", ready.viewId)
        assertEquals(490, ready.scene.canvasWidth)
        assertEquals(244, ready.scene.canvasHeight)
        assertEquals(listOf("electrical-runtime.render.wiring"), ready.activeRenderContributions.map { contribution -> contribution.contributionId })
        assertEquals(2, ready.scene.components.size)
        assertEquals(1, ready.scene.connections.size)
        assertEquals(2, ready.scene.labels.size)

        val afterDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document
        assertEquals(baselineDocument, afterDocument)

        val legacyProjection = assertIs<AthenaRuntimeViewerReadyProjection>(context.projectViewerProjection())
        assertEquals(490, legacyProjection.scene.canvasWidth)
        assertEquals(244, legacyProjection.scene.canvasHeight)
    }

    @Test
    fun `documentation view exposes governed sheet navigation without redefining canonical semantics`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val switchResult = context.switchActiveProjectionView("documentation")

        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)
        assertEquals("documentation", ready.viewId)
        assertEquals("electrical/documentation", ready.familyId)
        assertEquals("documentation/sheet/01-control", ready.activeSheetId)
        assertEquals(
            listOf(
                "documentation/sheet/01-control",
                "documentation/sheet/02-field-device",
            ),
            ready.sheets.map { sheet -> sheet.sheetId },
        )
        assertEquals("documentation/sheet/02-field-device", ready.sheets.first().nextSheetId)
        assertEquals("documentation/sheet/01-control", ready.sheets.last().previousSheetId)
        assertTrue(ready.sheets.first().subjectSemanticIds.contains("component:PLC1"))
        assertTrue(ready.sheets.last().subjectSemanticIds.contains("component:M1"))
        assertEquals("schematic-sheet", ready.sheets.first().composition.representationFamilyId)
        assertEquals("Control", ready.sheets.first().publication.titleBlock.sheetTitle)
        assertEquals("01-control", ready.sheets.first().publication.titleBlock.sheetNumber)
        assertEquals("documentation", ready.sheets.first().publication.viewComposition.primaryViewId)
        assertEquals(0, ready.sheets.first().publication.viewComposition.primarySheetOrder)
        assertEquals(ready.sheets.first().subjectSemanticIds, ready.sheets.first().composition.subjectSemanticIds)
        assertEquals("schematic-sheet", ready.sheets.last().composition.representationFamilyId)
        assertEquals("Field Device", ready.sheets.last().publication.titleBlock.sheetTitle)
        assertEquals("02-field-device", ready.sheets.last().publication.titleBlock.sheetNumber)
        assertEquals("documentation", ready.sheets.last().publication.viewComposition.primaryViewId)
        assertEquals(1, ready.sheets.last().publication.viewComposition.primarySheetOrder)
        assertEquals(ready.sheets.last().subjectSemanticIds, ready.sheets.last().composition.subjectSemanticIds)
        val sheetLayout = assertNotNull(ready.sheetLayout)
        assertEquals("documentation/sheet/01-control", sheetLayout.sheetId)
        assertEquals("Control", sheetLayout.displayName)
        assertEquals(0, sheetLayout.order)
        assertEquals("schematic-sheet", sheetLayout.representationFamilyId)
        assertEquals(ready.sheets.first().subjectSemanticIds, sheetLayout.subjectSemanticIds)
        assertEquals(ready.scene.canvasWidth, sheetLayout.frame.canvasWidth)
        assertEquals(ready.scene.canvasHeight, sheetLayout.frame.canvasHeight)
        assertEquals(ready.scene.components.size, sheetLayout.placements.size)
        assertEquals(ready.scene.labels.size, sheetLayout.labelLayouts.size)
        assertEquals(ready.electricalRoutingCorridors.size, sheetLayout.routingGuidance.size)
        assertEquals(1, ready.scene.components.count { component -> component.semanticId == "component:PLC1" })
        assertTrue(
            ready.scene.components.none { component -> component.projectionId.endsWith("_reference") },
            "Sheet layout must not depend on duplicate off-sheet reference components.",
        )
        assertEquals(
            1,
            ready.scene.components
                .filter { component -> component.semanticId == "component:PLC1" }
                .map { component -> component.projectionId }
                .distinct()
                .size,
        )
    }

    @Test
    fun `m31 customer documentation projection publishes two stable policy backed sheets after reopen`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val firstContext = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val firstReady = assertIs<AthenaRuntimeProjectionReadySnapshot>(
            assertIs<AthenaRuntimeProjectionSwitchSuccess>(
                firstContext.switchActiveProjectionView("documentation"),
            ).session.activeProjection,
        )
        val reopenedContext = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        val reopenedReady = assertIs<AthenaRuntimeProjectionReadySnapshot>(
            assertIs<AthenaRuntimeProjectionSwitchSuccess>(
                reopenedContext.switchActiveProjectionView("documentation"),
            ).session.activeProjection,
        )

        assertEquals(
            listOf("documentation/sheet/01-control", "documentation/sheet/02-field-device"),
            firstReady.sheets.map { sheet -> sheet.sheetId },
        )
        assertEquals(firstReady.sheets.map { sheet -> sheet.sheetId }, reopenedReady.sheets.map { sheet -> sheet.sheetId })
        assertEquals(firstReady.sheets.map { sheet -> sheet.order }, reopenedReady.sheets.map { sheet -> sheet.order })
        assertEquals(firstReady.sheets.map { sheet -> sheet.publication }, reopenedReady.sheets.map { sheet -> sheet.publication })
        assertEquals(
            firstReady.scene.components.map { component -> component.semanticId to component.projectionId },
            reopenedReady.scene.components.map { component -> component.semanticId to component.projectionId },
            "Reopening must preserve semantic component ids and projection occurrence ids.",
        )
        assertEquals(
            firstReady.scene.connections.map { connection -> connection.semanticId to connection.projectionId },
            reopenedReady.scene.connections.map { connection -> connection.semanticId to connection.projectionId },
            "Reopening must preserve semantic relationship ids and projection connection ids.",
        )
        assertEquals(
            firstReady.electricalRoutingCorridors.map { corridor ->
                corridor.connectionSemanticId to corridor.corridorId
            },
            reopenedReady.electricalRoutingCorridors.map { corridor ->
                corridor.connectionSemanticId to corridor.corridorId
            },
            "Reopening must preserve governed route ids.",
        )
        assertEquals(
            listOf("control-and-plc-logic", "field-wiring-and-terminal-transition"),
            firstReady.sheets.map { sheet -> sheet.policyEvidence?.sheetViewRole },
        )
        assertEquals(
            listOf("athena-m31-customer-projection-v0"),
            firstReady.sheets.mapNotNull { sheet -> sheet.policyEvidence?.policyId }.distinct(),
        )
        assertEquals(
            firstReady.presentation?.occurrences?.map { occurrence -> occurrence.occurrenceId.value },
            reopenedReady.presentation?.occurrences?.map { occurrence -> occurrence.occurrenceId.value },
        )
        val firstReference = firstReady.crossReferences.first { reference -> reference.links.isNotEmpty() }
        val firstLink = firstReference.links.first()
        assertTrue(firstReference.crossReferenceId.isNotBlank(), "Runtime cross-reference id must be stable and inspectable.")
        assertEquals(
            "documentation/sheet/01-control",
            firstLink.sourceSheetId,
            "Runtime cross-reference link must preserve source sheet identity.",
        )
        assertEquals(
            "documentation/sheet/02-field-device",
            firstLink.targetSheetId,
            "Runtime cross-reference link must preserve target sheet identity.",
        )
        assertTrue(firstLink.sourceOccurrenceId.isNotBlank(), "Runtime cross-reference link must preserve source occurrence identity.")
        assertTrue(firstLink.targetOccurrenceId.isNotBlank(), "Runtime cross-reference link must preserve target occurrence identity.")
        assertEquals(
            "01-control -> 02-field-device",
            firstLink.compactNotation,
            "Runtime compact notation must be transported from governed projection facts.",
        )
        assertEquals(
            firstReady.crossReferences,
            reopenedReady.crossReferences,
            "Reopening an unchanged project must preserve cross-reference identity and typed links.",
        )
    }

    @Test
    fun `documentation sheet switch changes active sheet without treating source files as pages`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        val baselineDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document

        val switchResult = context.switchActiveProjectionView("documentation/sheet/02-field-device")

        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        assertEquals("documentation/sheet/02-field-device", success.requestedViewId)
        assertEquals("documentation", success.session.activeViewId)
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)
        assertEquals("documentation", ready.viewId)
        assertEquals("documentation/sheet/02-field-device", ready.activeSheetId)
        val sheetLayout = assertNotNull(ready.sheetLayout)
        assertEquals("documentation/sheet/02-field-device", sheetLayout.sheetId)
        assertEquals("Field Device", sheetLayout.displayName)
        assertEquals(1, sheetLayout.order)
        assertEquals(
            ready.sheets[1].subjectSemanticIds,
            sheetLayout.subjectSemanticIds,
        )
        assertEquals(baselineDocument, assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document)
    }

    @Test
    fun `documentation sheet selection survives cabinet round trip`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        assertIs<AthenaRuntimeProjectionSwitchSuccess>(
            context.switchActiveProjectionView("documentation/sheet/02-field-device"),
        )
        val cabinet = assertIs<AthenaRuntimeProjectionSwitchSuccess>(
            context.switchActiveProjectionView("cabinet"),
        )
        assertEquals("cabinet", cabinet.session.activeViewId)

        val documentation = assertIs<AthenaRuntimeProjectionSwitchSuccess>(
            context.switchActiveProjectionView("documentation"),
        )
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(documentation.session.activeProjection)

        assertEquals("documentation/sheet/02-field-device", ready.activeSheetId)
        assertEquals("Field Device", ready.sheetLayout?.displayName)
    }

    @Test
    fun `m27 field wiring sheet renders field subjects from the active semantic source`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m27/sample-project/src/01-workspace-semantic-source.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "m27-sample-project",
            sourcePath = sourcePath,
        )

        val switchResult = context.switchActiveProjectionView("documentation/sheet/02-field-device")

        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)
        assertEquals("documentation/sheet/02-field-device", ready.activeSheetId)
        assertTrue(
            ready.scene.components.any { component -> component.semanticId == "component:FieldTerminalXT1" },
            "M27 field sheet must render the terminal transition subject from the active projected system.",
        )
        assertTrue(
            ready.scene.components.any { component -> component.semanticId == "component:ConveyorMotorM1" },
            "M27 field sheet must render the field load subject from the active projected system.",
        )
        assertTrue(
            ready.scene.connections.any { connection ->
                connection.semanticId == "connection:FieldOutputModuleIOM1.do1->FieldTerminalXT1.in1"
            },
            "M27 field sheet must include a routed field output to terminal connection.",
        )
        assertTrue(
            ready.electricalAnchors.any { anchor -> anchor.ownerSemanticId == "component:FieldTerminalXT1" },
            "M27 field sheet must keep terminal anchors available after active-sheet scoping.",
        )
    }

    @Test
    fun `m27 document sheets do not render orphan terminal labels`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m27/sample-project/src/01-workspace-semantic-source.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "m27-sample-project",
            sourcePath = sourcePath,
        )

        val documentSheetIds = assertIs<AthenaRuntimeProjectionReadySnapshot>(
            assertIs<AthenaRuntimeProjectionSwitchSuccess>(
                context.switchActiveProjectionView("documentation"),
            ).session.activeProjection,
        ).sheets.map { sheet -> sheet.sheetId }

        documentSheetIds.forEach { sheetId ->
            val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(
                assertIs<AthenaRuntimeProjectionSwitchSuccess>(
                    context.switchActiveProjectionView(sheetId),
                ).session.activeProjection,
            )
            val renderedComponentIds = ready.scene.components.map { component -> component.semanticId }.toSet()
            val orphanTerminalLabels = ready.scene.labels
                .map { label -> label.semanticId }
                .filter { semanticId -> semanticId.startsWith("port:") }
                .filterNot { semanticId ->
                    val ownerName = semanticId.removePrefix("port:").substringBefore('.')
                    "component:$ownerName" in renderedComponentIds
                }

            assertTrue(
                orphanTerminalLabels.isEmpty(),
                "Sheet `$sheetId` renders terminal labels without their owning component: $orphanTerminalLabels",
            )
        }
    }

    @Test
    fun `projection session cache stays stable until runtime invalidates it`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val firstSession = context.projectProjectionSession()
        val secondSession = context.projectProjectionSession()

        assertSame(firstSession, secondSession)

        val switchResult = assertIs<AthenaRuntimeProjectionSwitchSuccess>(context.switchActiveProjectionView("wiring"))
        assertNotSame(firstSession, switchResult.session)
        assertSame(switchResult.session, context.projectProjectionSession())
        assertEquals("wiring", switchResult.session.activeViewId)
    }

    @Test
    fun `projection preview can follow in memory compilation without mutating runtime owned cache`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        val baselineSession = context.projectProjectionSession()
        val editedCompilation = context.compiler().compile(
            sourcePath,
            """
                system DemoCabinet {
                  device PLC1 {
                    type Switch
                    model "S7-1200"
                  }

                  device M1 {
                    type Motor
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }

                  port M1.in {
                    direction in
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        val previewSession = context.previewProjectionSession(editedCompilation)

        assertEquals(0, assertIs<AthenaRuntimeProjectionReadySnapshot>(previewSession.activeProjection).scene.connections.size)
        assertSame(baselineSession, context.projectProjectionSession())
        assertEquals(1, assertIs<AthenaRuntimeProjectionReadySnapshot>(baselineSession.activeProjection).scene.connections.size)
    }

    @Test
    fun `accepted cabinet placement mutation refreshes projection state without changing canonical semantics`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        assertIs<AthenaRuntimeProjectionSwitchSuccess>(context.switchActiveProjectionView("cabinet"))
        val baselineDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document
        val baselineSession = context.projectProjectionSession()
        val baselineReady = assertIs<AthenaRuntimeProjectionReadySnapshot>(baselineSession.activeProjection)
        val plc = baselineReady.scene.components.first { component -> component.semanticId == "component:PLC1" }
        val plcPortLabel = baselineReady.scene.labels.first { label -> label.semanticId == "port:PLC1.out" }
        val baselineConnection = baselineReady.scene.connections.single()

        val result = context.graphCommandIntentRuntime().submit(
            context = context,
            intent = AthenaAdjustLayoutPlacementIntent(
                viewId = "cabinet",
                target = AthenaGraphCommandTarget(
                    semanticId = "component:PLC1",
                    subjectKind = AthenaGraphCommandSubjectKind.COMPONENT,
                ),
                requestedPlacement = AthenaGraphPlacement(
                    x = plc.x + 48,
                    y = plc.y + 24,
                ),
            ),
        )

        assertIs<AthenaGraphCommandIntentAccepted>(result)
        val refreshedReady = assertIs<AthenaRuntimeProjectionReadySnapshot>(context.projectProjectionSession().activeProjection)
        val movedPlc = refreshedReady.scene.components.first { component -> component.semanticId == "component:PLC1" }
        val movedPortLabel = refreshedReady.scene.labels.first { label -> label.semanticId == "port:PLC1.out" }
        val movedConnection = refreshedReady.scene.connections.single()

        assertEquals(plc.x + 48, movedPlc.x)
        assertEquals(plc.y + 24, movedPlc.y)
        assertEquals(plcPortLabel.x + 48, movedPortLabel.x)
        assertEquals(plcPortLabel.y + 24, movedPortLabel.y)
        assertEquals(baselineConnection.x1 + 48, movedConnection.x1)
        assertEquals(baselineConnection.y1 + 24, movedConnection.y1)
        assertEquals(baselineConnection.x2, movedConnection.x2)
        assertEquals(baselineConnection.y2, movedConnection.y2)
        assertEquals(baselineDocument, assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document)
    }

    @Test
    fun `unsupported active view ids are rejected explicitly`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val switchResult = context.switchActiveProjectionView("missing")

        val rejected = assertIs<AthenaRuntimeProjectionSwitchRejected>(switchResult)
        assertEquals("demo-cabinet", rejected.projectName)
        assertEquals("missing", rejected.requestedViewId)
        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            rejected.supportedViewIds,
        )
        assertContains(rejected.reason, "missing")
        assertEquals("cabinet", context.projectProjectionSession().activeViewId)
    }

    @Test
    fun `projection session supported views stay aligned with typed hosted plugin view definitions`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val sessionViewIds = context.projectProjectionSession().supportedViews.map { view -> view.viewId }
        val hostedViewIds = runtime.serviceRegistry.pluginRuntimeServices().viewDefinitionContributions()
            .flatMap { contribution -> contribution.viewDefinitions }
            .map { definition -> definition.id }

        assertTrue(hostedViewIds.isNotEmpty())
        assertEquals(hostedViewIds, sessionViewIds)
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
