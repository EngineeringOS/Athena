package com.engineeringood.athena.packageplatform

import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertFails

class PartContractsTest {
    @Test
    fun `part rejects duplicate technical fields and accessories`() {
        val accessory = PackageItemIdentity(PackageIdentifier("com.athena.parts", "1.0.0"), "aux", "1.0.0")
        assertFails {
            PartFacts(
                "Vendor",
                "A-1",
                listOf(
                    PartTechnicalField("voltage", PackageItemValue.NumberValue("24")),
                    PartTechnicalField("voltage", PackageItemValue.NumberValue("48")),
                ),
                accessories = listOf(PartAccessory(accessory, "aux"), PartAccessory(accessory, "aux")),
            )
        }
    }
}
