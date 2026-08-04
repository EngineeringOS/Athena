package com.engineeringood.athena.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AthenaRuntimeProjectionAuthoritySelectionTest {
    @Test
    fun `Spatial authority requires exact active Sheet membership`() {
        val candidates = listOf(
            AuthorityCandidate("spatial-s1", listOf("schematic/sheet/S1")),
            AuthorityCandidate("spatial-s2", listOf("schematic/sheet/S2")),
        )

        assertEquals(
            "spatial-s2",
            selectSpatialAuthority(candidates, "schematic/sheet/S2", AuthorityCandidate::sheetIds)?.id,
        )
        assertNull(selectSpatialAuthority(candidates.take(1), "schematic/sheet/S2", AuthorityCandidate::sheetIds))
        assertNull(selectSpatialAuthority(candidates + candidates[1], "schematic/sheet/S2", AuthorityCandidate::sheetIds))
    }

    @Test
    fun `Presentation authority never substitutes another active Sheet`() {
        val candidates = listOf(
            PresentationCandidate("presentation-s1", "schematic/sheet/S1"),
            PresentationCandidate("presentation-s2", "schematic/sheet/S2"),
        )

        assertEquals(
            "presentation-s2",
            selectPresentationAuthority(
                candidates,
                "schematic/sheet/S2",
                PresentationCandidate::sheetId,
            )?.id,
        )
        assertNull(
            selectPresentationAuthority(
                candidates.take(1),
                "schematic/sheet/S2",
                PresentationCandidate::sheetId,
            ),
        )
    }

    @Test
    fun `single sheetless Presentation remains available to non-Spatial views`() {
        val candidate = PresentationCandidate("documentation", null)

        assertEquals(
            candidate,
            selectPresentationAuthority(listOf(candidate), "documentation/sheet/S1", PresentationCandidate::sheetId),
        )
        assertNull(
            selectPresentationAuthority(
                listOf(candidate),
                "schematic/sheet/S1",
                PresentationCandidate::sheetId,
                requireSheetAuthority = true,
            ),
        )
    }

    @Test
    fun `Projection identity export selects exactly the active Sheet`() {
        val candidates = listOf(
            ProjectionSheetCandidate("schematic/sheet/S1", listOf("region-s1")),
            ProjectionSheetCandidate("schematic/sheet/S2", listOf("region-s2")),
        )

        assertEquals(
            listOf("region-s2"),
            selectActiveProjectionSheet(candidates, "schematic/sheet/S2", ProjectionSheetCandidate::sheetId)?.regionIds,
        )
        assertNull(selectActiveProjectionSheet(candidates + candidates[1], "schematic/sheet/S2", ProjectionSheetCandidate::sheetId))
    }
}

private data class AuthorityCandidate(val id: String, val sheetIds: List<String>)

private data class PresentationCandidate(val id: String, val sheetId: String?)

private data class ProjectionSheetCandidate(val sheetId: String, val regionIds: List<String>)
