package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhysicalTraitModelContractTest {
    @Test
    fun `physical trait definition stays minimal and reusable`() {
        val definition = PhysicalTraitDefinition(
            displayName = "DIN rail mounted PLC CPU",
            size = PhysicalSize(
                widthMillimeters = 80,
                heightMillimeters = 125,
                depthMillimeters = 130,
            ),
            mountingTypeId = PhysicalMountingTypeId("din-rail"),
            installationMarkerIds = setOf(
                PhysicalInstallationMarkerId("cabinet-interior"),
                PhysicalInstallationMarkerId("front-clearance-required"),
            ),
            summary = "Minimal physical trait contract only.",
        )

        assertEquals(80, definition.size.widthMillimeters)
        assertEquals(125, definition.size.heightMillimeters)
        assertEquals(130, definition.size.depthMillimeters)
        assertEquals("din-rail", definition.mountingTypeId.value)
        assertEquals(
            setOf("cabinet-interior", "front-clearance-required"),
            definition.installationMarkerIds.map { marker -> marker.value }.toSet(),
        )
    }

    @Test
    fun `resolved physical traits stay anchored to canonical semantic identity`() {
        val resolved = ResolvedPhysicalTraitDefinition(
            semanticSubjectId = StableSemanticIdentity("component:PLC1"),
            definition = PhysicalTraitDefinition(
                displayName = "DIN rail mounted PLC CPU",
                size = PhysicalSize(80, 125, 130),
                mountingTypeId = PhysicalMountingTypeId("din-rail"),
                installationMarkerIds = setOf(PhysicalInstallationMarkerId("cabinet-interior")),
            ),
        )

        assertEquals("component:PLC1", resolved.semanticSubjectId.value)
        assertTrue(resolved.definition.size.depthMillimeters > 0)
    }
}
