package com.engineeringood.athena.template

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.part.PartImplementationId
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TemplateComponentContractTest {
    @Test
    fun `component template stays semantic first with optional implementation mapping`() {
        val template = ComponentTemplate(
            templateId = ComponentTemplateId("template:starter.contactor"),
            conceptId = EngineeringConceptId("electrical.contactor"),
            implementationId = PartImplementationId("siemens.3rt2015"),
            defaultMetadata = TemplateDefaultMetadata(
                displayName = "Contactor",
                summary = "Primary switching contactor for one starter assembly.",
            ),
            properties = mapOf(
                ComponentTemplatePropertyName("tag") to TemplateValue.ParameterReference(SemanticMacroParameterName("starterTag")),
                ComponentTemplatePropertyName("coilVoltage") to TemplateValue.Literal(SemanticMacroParameterValue.Symbol("24VDC")),
            ),
        )

        assertEquals("template:starter.contactor", template.templateId.value)
        assertEquals("electrical.contactor", template.conceptId.value)
        assertEquals("siemens.3rt2015", template.implementationId?.value)
        assertNotEquals(template.templateId.value, template.conceptId.value)
        assertEquals("starterTag", (template.properties.getValue(ComponentTemplatePropertyName("tag")) as TemplateValue.ParameterReference).parameterName.value)
    }

    @Test
    fun `template metadata and hints stay advisory rather than graphic truth`() {
        val template = ComponentTemplate(
            templateId = ComponentTemplateId("template:meter"),
            conceptId = EngineeringConceptId("electrical.panel-meter"),
            defaultMetadata = TemplateDefaultMetadata(
                displayName = "Panel meter",
                tags = setOf("meter", "measurement"),
            ),
            presentationHints = listOf(
                TemplatePresentationHint(
                    hintType = "preferred-symbol-family",
                    attributes = mapOf("family" to "iec-meter"),
                ),
            ),
            documentationHints = listOf(
                TemplateDocumentationHint(
                    hintType = "maintenance-note",
                    attributes = mapOf("section" to "operation"),
                ),
            ),
        )

        assertEquals("Panel meter", template.defaultMetadata.displayName)
        assertTrue(template.defaultMetadata.tags.contains("meter"))
        assertEquals("preferred-symbol-family", template.presentationHints.single().hintType)
        assertEquals("maintenance-note", template.documentationHints.single().hintType)
    }
}
