package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.component.EngineeringConceptPortDirection
import com.engineeringood.athena.component.EngineeringConceptPortTemplate
import com.engineeringood.athena.component.EngineeringConceptPropertyTemplate
import com.engineeringood.athena.component.EngineeringConceptPropertyValueKind
import com.engineeringood.athena.component.EngineeringConceptRelationshipCapability
import com.engineeringood.athena.component.EngineeringConceptTemplate
import com.engineeringood.athena.component.EngineeringConceptTemplateId
import com.engineeringood.athena.component.EngineeringConceptTemplateProvenance
import com.engineeringood.athena.component.EngineeringSemanticType
import com.engineeringood.athena.component.EngineeringSignalOrMedium

/** Electrical-domain semantic creation templates; representation selection remains downstream. */
fun electricalEngineeringConceptTemplates(): List<EngineeringConceptTemplate> = listOf(
    EngineeringConceptTemplate(
        templateId = EngineeringConceptTemplateId("electrical.plc.cpu.default"),
        conceptId = EngineeringConceptId("electrical.plc.cpu"),
        semanticType = EngineeringSemanticType("Switch"),
        propertySchema = standardDeviceProperties(),
        nestedPorts = listOf(
            electricalPort("lplus", EngineeringConceptPortDirection.IN, "Power24"),
            electricalPort("m", EngineeringConceptPortDirection.IN, "Common24"),
            electricalPort("pe", EngineeringConceptPortDirection.IN, "ProtectiveEarth"),
            electricalPort("mpi", EngineeringConceptPortDirection.OUT, "MPIBus"),
        ),
        relationshipCapabilities = listOf(
            EngineeringConceptRelationshipCapability(
                relationshipType = "ElectricalConnectionRelationship",
                portNames = setOf("lplus", "m", "pe", "mpi"),
            ),
        ),
        provenance = electricalTemplateProvenance(),
    ),
    EngineeringConceptTemplate(
        templateId = EngineeringConceptTemplateId("electrical.power-supply.dc24.default"),
        conceptId = EngineeringConceptId("electrical.power-supply.dc24"),
        semanticType = EngineeringSemanticType("Switch"),
        propertySchema = standardDeviceProperties(),
        nestedPorts = listOf(
            electricalPort("out", EngineeringConceptPortDirection.OUT, "Power24"),
            electricalPort("m", EngineeringConceptPortDirection.OUT, "Common24"),
            electricalPort("pe", EngineeringConceptPortDirection.OUT, "ProtectiveEarth"),
        ),
        relationshipCapabilities = listOf(
            EngineeringConceptRelationshipCapability(
                relationshipType = "ElectricalConnectionRelationship",
                portNames = setOf("out", "m", "pe"),
            ),
        ),
        provenance = electricalTemplateProvenance(),
    ),
    EngineeringConceptTemplate(
        templateId = EngineeringConceptTemplateId("electrical.motor.ac.default"),
        conceptId = EngineeringConceptId("electrical.motor.ac"),
        semanticType = EngineeringSemanticType("Motor"),
        defaultModel = "MOTOR-AC",
        propertySchema = listOf(
            EngineeringConceptPropertyTemplate(
                name = "model",
                valueKind = EngineeringConceptPropertyValueKind.TEXT,
                required = true,
                defaultValue = "MOTOR-AC",
            ),
        ),
        nestedPorts = listOf(
            EngineeringConceptPortTemplate(
                name = "up",
                direction = EngineeringConceptPortDirection.IN,
                signalOrMedium = EngineeringSignalOrMedium("Digital"),
            ),
            EngineeringConceptPortTemplate(
                name = "down",
                direction = EngineeringConceptPortDirection.IN,
                signalOrMedium = EngineeringSignalOrMedium("Digital"),
            ),
            EngineeringConceptPortTemplate(
                name = "status",
                direction = EngineeringConceptPortDirection.OUT,
                signalOrMedium = EngineeringSignalOrMedium("Digital"),
            ),
        ),
        relationshipCapabilities = listOf(
            EngineeringConceptRelationshipCapability(
                relationshipType = "ElectricalConnectionRelationship",
                portNames = setOf("up", "down", "status"),
            ),
        ),
        provenance = electricalTemplateProvenance(),
    ),
)

private fun standardDeviceProperties(): List<EngineeringConceptPropertyTemplate> = listOf(
    EngineeringConceptPropertyTemplate(
        name = "model",
        valueKind = EngineeringConceptPropertyValueKind.TEXT,
    ),
    EngineeringConceptPropertyTemplate(
        name = "vendorPartNumber",
        valueKind = EngineeringConceptPropertyValueKind.TEXT,
    ),
    EngineeringConceptPropertyTemplate(
        name = "label",
        valueKind = EngineeringConceptPropertyValueKind.TEXT,
    ),
)

private fun electricalPort(
    name: String,
    direction: EngineeringConceptPortDirection,
    signal: String,
): EngineeringConceptPortTemplate = EngineeringConceptPortTemplate(
    name = name,
    direction = direction,
    signalOrMedium = EngineeringSignalOrMedium(signal),
)

private fun electricalTemplateProvenance(): EngineeringConceptTemplateProvenance =
    EngineeringConceptTemplateProvenance(
        domainId = "electrical",
        source = "extensions/domain-electrical",
    )
