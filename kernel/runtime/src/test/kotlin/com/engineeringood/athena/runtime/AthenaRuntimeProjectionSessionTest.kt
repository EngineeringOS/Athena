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
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(session.activeProjection)
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
        assertEquals("documentation/sheet/01-overview", ready.activeSheetId)
        assertEquals(
            listOf("documentation/sheet/01-overview", "documentation/sheet/02-reference"),
            ready.sheets.map { sheet -> sheet.sheetId },
        )
        assertEquals("documentation/sheet/02-reference", ready.sheets.first().nextSheetId)
        assertEquals("documentation/sheet/01-overview", ready.sheets.last().previousSheetId)
        assertTrue(ready.sheets.first().subjectSemanticIds.contains("component:PLC1"))
        assertTrue(ready.sheets.last().subjectSemanticIds.contains("component:PLC1"))
        val expectedOverviewPublication = AthenaRuntimeProjectionSheetPublication(
            pageSize = AthenaRuntimeProjectionSheetPageSize(
                format = "A3",
                orientation = "landscape",
            ),
            frame = AthenaRuntimeProjectionSheetFrame(
                frameId = "engineering-sheet-frame",
                style = "schematic",
            ),
            coordinateZones = listOf(
                AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "header", label = "Header", order = 0),
                AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "body", label = "Body", order = 1),
                AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "title-block", label = "Title Block", order = 2),
            ),
            titleBlock = AthenaRuntimeProjectionSheetTitleBlock(
                sheetTitle = "Overview",
                sheetFamily = "documentation",
                sheetNumber = "01-overview",
            ),
            revisionMetadata = AthenaRuntimeProjectionSheetRevisionMetadata(
                revisionCode = "A",
                revisionNote = "Initial governed sheet publication",
            ),
            viewComposition = AthenaRuntimeProjectionSheetViewComposition(
                primaryViewId = "documentation",
                primarySheetOrder = 0,
                subjectSemanticIds = listOf(
                    "component:M1",
                    "component:PLC1",
                    "connection:PLC1.out->M1.in",
                    "port:M1.in",
                    "port:PLC1.out",
                ),
            ),
        )
        val expectedOverviewComposition = AthenaRuntimeProjectionSheetComposition(
            sheetId = "documentation/sheet/01-overview",
            displayName = "Overview",
            order = 0,
            publication = expectedOverviewPublication,
            subjectSemanticIds = listOf(
                "component:M1",
                "component:PLC1",
                "connection:PLC1.out->M1.in",
                "port:M1.in",
                "port:PLC1.out",
            ),
            representationFamilyId = "schematic-sheet",
        )
        val expectedReferencePublication = AthenaRuntimeProjectionSheetPublication(
            pageSize = AthenaRuntimeProjectionSheetPageSize(
                format = "A3",
                orientation = "landscape",
            ),
            frame = AthenaRuntimeProjectionSheetFrame(
                frameId = "engineering-sheet-frame",
                style = "schematic",
            ),
            coordinateZones = listOf(
                AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "header", label = "Header", order = 0),
                AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "body", label = "Body", order = 1),
                AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "title-block", label = "Title Block", order = 2),
            ),
            titleBlock = AthenaRuntimeProjectionSheetTitleBlock(
                sheetTitle = "Reference",
                sheetFamily = "documentation",
                sheetNumber = "02-reference",
            ),
            revisionMetadata = AthenaRuntimeProjectionSheetRevisionMetadata(
                revisionCode = "A",
                revisionNote = "Initial governed sheet publication",
            ),
            viewComposition = AthenaRuntimeProjectionSheetViewComposition(
                primaryViewId = "documentation",
                primarySheetOrder = 1,
                subjectSemanticIds = listOf(
                    "component:M1",
                    "component:PLC1",
                ),
            ),
        )
        val expectedReferenceComposition = AthenaRuntimeProjectionSheetComposition(
            sheetId = "documentation/sheet/02-reference",
            displayName = "Reference",
            order = 1,
            publication = expectedReferencePublication,
            subjectSemanticIds = listOf(
                "component:M1",
                "component:PLC1",
            ),
            representationFamilyId = "schematic-sheet",
        )
        assertEquals("schematic-sheet", ready.sheets.first().composition.representationFamilyId)
        assertEquals(expectedOverviewPublication, ready.sheets.first().publication)
        assertEquals(expectedOverviewComposition, ready.sheets.first().composition)
        assertEquals("schematic-sheet", ready.sheets.last().composition.representationFamilyId)
        assertEquals(expectedReferencePublication, ready.sheets.last().publication)
        assertEquals(expectedReferenceComposition, ready.sheets.last().composition)
        val sheetLayout = assertNotNull(ready.sheetLayout)
        assertEquals("documentation/sheet/01-overview", sheetLayout.sheetId)
        assertEquals("Overview", sheetLayout.displayName)
        assertEquals(0, sheetLayout.order)
        assertEquals("schematic-sheet", sheetLayout.representationFamilyId)
        assertEquals(ready.sheets.first().subjectSemanticIds, sheetLayout.subjectSemanticIds)
        assertEquals(ready.scene.canvasWidth, sheetLayout.frame.canvasWidth)
        assertEquals(ready.scene.canvasHeight, sheetLayout.frame.canvasHeight)
        assertEquals(ready.scene.components.size, sheetLayout.placements.size)
        assertEquals(ready.scene.labels.size, sheetLayout.labelLayouts.size)
        assertEquals(ready.electricalRoutingCorridors.size, sheetLayout.routingGuidance.size)
        assertEquals(2, ready.scene.components.count { component -> component.semanticId == "component:PLC1" })
        assertEquals(
            2,
            ready.scene.components
                .filter { component -> component.semanticId == "component:PLC1" }
                .map { component -> component.projectionId }
                .distinct()
                .size,
        )
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
