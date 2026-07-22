package com.engineeringood.athena.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EngineeringConceptTemplateContractTest {
    @Test
    fun `concept template describes semantic anatomy without visual vocabulary`() {
        val template = EngineeringConceptTemplate(
            templateId = EngineeringConceptTemplateId("generic.actuator.default"),
            conceptId = EngineeringConceptId("generic.actuator"),
            semanticType = EngineeringSemanticType("Actuator"),
            defaultModel = "ACTUATOR",
            propertySchema = listOf(
                EngineeringConceptPropertyTemplate(
                    name = "model",
                    valueKind = EngineeringConceptPropertyValueKind.TEXT,
                    required = true,
                    defaultValue = "ACTUATOR",
                ),
            ),
            nestedPorts = listOf(
                EngineeringConceptPortTemplate(
                    name = "command",
                    direction = EngineeringConceptPortDirection.IN,
                    signalOrMedium = EngineeringSignalOrMedium("Digital"),
                    terminalNumber = "A1",
                ),
            ),
            relationshipCapabilities = listOf(
                EngineeringConceptRelationshipCapability(
                    relationshipType = "ControlRelationship",
                    portNames = setOf("command"),
                ),
            ),
            provenance = EngineeringConceptTemplateProvenance(
                domainId = "generic",
                source = "platform-test",
            ),
        )

        assertEquals("Actuator", template.semanticType.value)
        assertEquals("command", template.nestedPorts.single().name)
        assertEquals("Digital", template.nestedPorts.single().signalOrMedium.value)
        assertEquals("ControlRelationship", template.relationshipCapabilities.single().relationshipType)

        val forbidden = setOf(
            "primitive", "svg", "path", "style", "anchor", "geometry", "bounds",
            "x", "y", "width", "height", "transform", "viewbox",
        )
        val publicContractFields = EngineeringConceptTemplate::class.java.declaredFields
            .map { field -> field.name.lowercase() }
            .toSet()
        assertTrue(publicContractFields.intersect(forbidden).isEmpty(), "Visual fields leaked into concept template: $publicContractFields")
    }
}
