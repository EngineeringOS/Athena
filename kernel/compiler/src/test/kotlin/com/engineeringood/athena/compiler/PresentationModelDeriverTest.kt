package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.PresentationCompositeOccurrenceReference
import com.engineeringood.athena.presentation.connectorsForRendering
import com.engineeringood.athena.routing.RouteFact
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PresentationModelDeriverTest {
    @Test
    fun `presentation derivation keeps routing guidance and family specific composites downstream of canonical semantics`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

        val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val cabinet = success.presentations.first { presentation -> presentation.view.id == "cabinet" }
        val schematic = success.presentations.first { presentation -> presentation.view.id == "schematic" }

        val cabinetPlc = cabinet.occurrences.first { occurrence -> occurrence.semanticId.value == "component:PLC1" }
        val schematicPlc = schematic.occurrences.first { occurrence -> occurrence.semanticId.value == "component:PLC1" }
        val cabinetReference = assertIs<PresentationCompositeOccurrenceReference>(cabinetPlc.reference)
        val schematicReference = assertIs<PresentationCompositeOccurrenceReference>(schematicPlc.reference)
        assertEquals("electrical.device.switch-panel", cabinetReference.compositeId.value)
        assertEquals("electrical.device.switch-panel", schematicReference.compositeId.value)

        val cabinetComposite = cabinet.compositePacks
            .flatMap { pack -> pack.composites }
            .first { composite -> composite.compositeId == cabinetReference.compositeId }
        val schematicComposite = schematic.compositePacks
            .flatMap { pack -> pack.composites }
            .first { composite -> composite.compositeId == schematicReference.compositeId }
        assertTrue(cabinetComposite.parts.any { part -> part.partId == "frame" })
        assertTrue(cabinetComposite.parts.any { part -> part.partId == "contact-mark" })
        assertTrue(schematicComposite.parts.none { part -> part.partId == "frame" })
        assertEquals(listOf("contact-mark"), schematicComposite.parts.map { part -> part.partId })

        val cabinetConnector = cabinet.connectors.single()
        assertEquals("connection:PLC1.out->M1.in", cabinetConnector.semanticId.value)
        assertEquals("port:PLC1.out", cabinetConnector.sourcePortSemanticId?.value)
        assertEquals("port:M1.in", cabinetConnector.targetPortSemanticId?.value)
        assertNotNull(cabinetConnector.sourceAnchorId)
        assertNotNull(cabinetConnector.targetAnchorId)
        assertTrue(cabinetConnector.routePoints.size >= 2)
        assertEquals(
            "cabinet/projection/connection/connection_PLC1_out_M1_in",
            cabinetConnector.sourceProjectionIds.single(),
        )

        assertTrue(
            cabinetPlc.anchorBindings.any { binding ->
                binding.portSemanticId?.value == "port:PLC1.out" &&
                    binding.ownerSemanticId?.value == "component:PLC1" &&
                    binding.sourceLabelId == "cabinet/projection/label/port_PLC1_out"
            },
        )
    }

    @Test
    fun `presentation derivation publishes terminal anchor route facts for rendered schematic wires`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

        val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val schematic = success.presentations.first { presentation -> presentation.view.id == "schematic" }
        val routeFacts = assertNotNull(schematic.routeFactSnapshot).routeFacts
        val route = routeFacts.single()
        val connector = schematic.connectorsForRendering().single()

        assertEquals("connection:PLC1.out->M1.in", route.connectionId.value)
        assertEquals("port:PLC1.out", route.source.portSemanticId?.value)
        assertEquals("port:M1.in", route.target.portSemanticId?.value)
        assertEquals(route.source.gridPoint.x, connector.routePoints.first().x)
        assertEquals(route.source.gridPoint.y, connector.routePoints.first().y)
        assertEquals(route.target.gridPoint.x, connector.routePoints.last().x)
        assertEquals(route.target.gridPoint.y, connector.routePoints.last().y)
        assertEquals(route.source.anchorId.value, connector.sourceAnchorId)
        assertEquals(route.target.anchorId.value, connector.targetAnchorId)
        assertEquals(route.routeId.value, connector.occurrenceId.value)
        assertTrue(routeFacts.flatMap(RouteFact::segments).all { segment ->
            segment.start.x % 20 == 0 &&
                segment.start.y % 20 == 0 &&
                segment.end.x % 20 == 0 &&
                segment.end.y % 20 == 0
        })
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
