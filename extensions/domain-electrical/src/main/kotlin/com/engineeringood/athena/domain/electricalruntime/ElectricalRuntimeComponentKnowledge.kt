package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.component.EngineeringConceptDefinition
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.part.PartImplementationDefinition
import com.engineeringood.athena.part.PartImplementationId
import com.engineeringood.athena.part.VendorId
import com.engineeringood.athena.part.VendorPartNumber

/**
 * First narrow electrical concept slice published by the hosted electrical extension for M14.
 *
 * These definitions stay extension-owned. They do not move electrical meaning into the kernel, and
 * they do not yet imply a governed knowledge-pack registry or loading pipeline.
 */
fun electricalEngineeringConcepts(): List<EngineeringConceptDefinition> {
    return listOf(
        EngineeringConceptDefinition(
            conceptId = EngineeringConceptId("electrical.plc.cpu"),
            displayName = "PLC CPU",
            classificationKeys = setOf("electrical", "control", "plc", "cpu"),
            summary = "Vendor-neutral programmable controller central processing unit.",
        ),
        EngineeringConceptDefinition(
            conceptId = EngineeringConceptId("electrical.contactor.power"),
            displayName = "Power contactor",
            classificationKeys = setOf("electrical", "power-control", "contactor"),
            summary = "Vendor-neutral contactor used to switch one power load.",
        ),
        EngineeringConceptDefinition(
            conceptId = EngineeringConceptId("electrical.relay.overload"),
            displayName = "Overload relay",
            classificationKeys = setOf("electrical", "protection", "relay", "overload"),
            summary = "Vendor-neutral overload relay for motor-protection review flows.",
        ),
        EngineeringConceptDefinition(
            conceptId = EngineeringConceptId("electrical.motor.ac"),
            displayName = "AC motor",
            classificationKeys = setOf("electrical", "load", "motor", "ac"),
            summary = "Vendor-neutral alternating-current motor concept.",
        ),
        EngineeringConceptDefinition(
            conceptId = EngineeringConceptId("electrical.power-supply.dc24"),
            displayName = "24V DC power supply",
            classificationKeys = setOf("electrical", "power", "supply", "24vdc"),
            summary = "Vendor-neutral 24V DC power supply concept.",
        ),
    )
}

/**
 * First Siemens-first implementation slice published by the hosted electrical extension for M14.
 *
 * Vendor part numbers remain implementation metadata. They do not replace `EngineeringConceptId`,
 * and they do not claim broad catalog parity.
 */
fun siemensElectricalPartImplementations(): List<PartImplementationDefinition> {
    return listOf(
        PartImplementationDefinition(
            implementationId = PartImplementationId("impl/electrical/plc-cpu/siemens-proof-cpu313c"),
            conceptId = EngineeringConceptId("electrical.plc.cpu"),
            vendorId = VendorId("siemens"),
            vendorPartNumber = VendorPartNumber("proof.cpu.313c"),
            displayName = "Siemens proof PLC CPU 313C",
            summary = "Narrow Siemens-first proof implementation for the PLC CPU concept.",
        ),
        PartImplementationDefinition(
            implementationId = PartImplementationId("impl/electrical/contactor/siemens-proof-3pole"),
            conceptId = EngineeringConceptId("electrical.contactor.power"),
            vendorId = VendorId("siemens"),
            vendorPartNumber = VendorPartNumber("proof.contactor.3pole"),
            displayName = "Siemens proof power contactor",
            summary = "Narrow Siemens-first proof implementation for the power contactor concept.",
        ),
        PartImplementationDefinition(
            implementationId = PartImplementationId("impl/electrical/relay/siemens-proof-overload"),
            conceptId = EngineeringConceptId("electrical.relay.overload"),
            vendorId = VendorId("siemens"),
            vendorPartNumber = VendorPartNumber("proof.relay.overload"),
            displayName = "Siemens proof overload relay",
            summary = "Narrow Siemens-first proof implementation for the overload relay concept.",
        ),
        PartImplementationDefinition(
            implementationId = PartImplementationId("impl/electrical/motor/siemens-proof-ac-motor"),
            conceptId = EngineeringConceptId("electrical.motor.ac"),
            vendorId = VendorId("siemens"),
            vendorPartNumber = VendorPartNumber("proof.motor.ac"),
            displayName = "Siemens proof AC motor",
            summary = "Narrow Siemens-first proof implementation for the AC motor concept.",
        ),
        PartImplementationDefinition(
            implementationId = PartImplementationId("impl/electrical/power-supply/siemens-proof-24vdc"),
            conceptId = EngineeringConceptId("electrical.power-supply.dc24"),
            vendorId = VendorId("siemens"),
            vendorPartNumber = VendorPartNumber("proof.power-supply.24vdc"),
            displayName = "Siemens proof 24V DC power supply",
            summary = "Narrow Siemens-first proof implementation for the 24V DC power supply concept.",
        ),
    )
}
