package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.document.CrossReferenceRelationType
import com.engineeringood.athena.document.DocumentLocation
import com.engineeringood.athena.document.DocumentOccurrenceId
import com.engineeringood.athena.document.LogicalZoneId
import com.engineeringood.athena.document.SheetViewId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationReferenceMarkerFact
import com.engineeringood.athena.presentation.PresentationReferenceMarkerId
import com.engineeringood.athena.presentation.PresentationReferenceMarkerKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaPresentationReferenceMarkerPayloadTest {
    @Test
    fun `presentation payload transports compact reference markers with canonical payload`() {
        val marker = PresentationReferenceMarkerFact(
            markerId = PresentationReferenceMarkerId("reference-marker:route-1"),
            markerKind = PresentationReferenceMarkerKind.CONTINUATION,
            relationType = CrossReferenceRelationType.ROUTE_CONTINUATION,
            selectedSheetViewId = SheetViewId("sheet-view:control-and-plc-logic"),
            sourceOccurrenceId = DocumentOccurrenceId("occurrence:route:control"),
            targetOccurrenceId = DocumentOccurrenceId("occurrence:route:field"),
            sourceIdentity = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1"),
            targetIdentity = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1"),
            sourceDocumentLocation = DocumentLocation(
                sheetViewId = SheetViewId("sheet-view:control-and-plc-logic"),
                zoneId = LogicalZoneId("A1"),
                displayNotation = "A1",
            ),
            targetDocumentLocation = DocumentLocation(
                sheetViewId = SheetViewId("sheet-view:field-wiring-and-terminal-transition"),
                zoneId = LogicalZoneId("B2"),
                displayNotation = "B2",
            ),
            compactNotation = "sheet-view:field-wiring-and-terminal-transition B2",
            sourceProjectionIds = listOf("cross-reference:route-1"),
        )
        val document = presentationDocument(referenceMarkers = listOf(marker))

        val payload = document.toPayload()
        val markerPayload = payload.referenceMarkers.single()

        assertEquals("reference-marker:route-1", markerPayload.markerId)
        assertEquals("continuation", markerPayload.markerKind)
        assertEquals("route_continuation", markerPayload.relationType)
        assertEquals("sheet-view:control-and-plc-logic", markerPayload.selectedSheetViewId)
        assertEquals("occurrence:route:control", markerPayload.sourceOccurrenceId)
        assertEquals("occurrence:route:field", markerPayload.targetOccurrenceId)
        assertEquals("connection:PLC1.Q0.0->XT1.1", markerPayload.sourceIdentity)
        assertEquals("connection:PLC1.Q0.0->XT1.1", markerPayload.targetIdentity)
        assertEquals("sheet-view:field-wiring-and-terminal-transition B2", markerPayload.compactNotation)
        assertEquals(listOf("cross-reference:route-1"), markerPayload.sourceProjectionIds)
    }

    @Test
    fun `presentation payload remains marker empty for existing single sheet documents`() {
        val payload = presentationDocument().toPayload()

        assertTrue(payload.referenceMarkers.isEmpty())
    }

    private fun presentationDocument(
        referenceMarkers: List<PresentationReferenceMarkerFact> = emptyList(),
    ): PresentationDocument =
        PresentationDocument(
            view = ViewDefinition(
                id = "schematic",
                displayName = "Schematic",
                layoutIntent = LayoutIntent.CONNECTIVITY,
            ),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            referenceMarkers = referenceMarkers,
        )
}
