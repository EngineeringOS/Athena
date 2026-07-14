package com.engineeringood.athena.template

import com.engineeringood.athena.connection.SemanticPortRoleId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplateConnectionContractTest {
    @Test
    fun `connection template references semantic endpoints instead of graphics coordinates`() {
        val connection = ConnectionTemplate(
            templateId = ConnectionTemplateId("template:starter.power-link"),
            from = TemplatePortReference(
                componentTemplateId = ComponentTemplateId("template:starter.contactor"),
                portRoleId = SemanticPortRoleId("L1"),
            ),
            to = TemplatePortReference(
                componentTemplateId = ComponentTemplateId("template:starter.overload"),
                portRoleId = SemanticPortRoleId("L1"),
            ),
            defaultMetadata = TemplateDefaultMetadata(
                displayName = "Power feed",
            ),
        )

        assertEquals("template:starter.power-link", connection.templateId.value)
        assertEquals("template:starter.contactor", connection.from.componentTemplateId.value)
        assertEquals("L1", connection.from.portRoleId.value)
        assertEquals("template:starter.overload", connection.to.componentTemplateId.value)
    }

    @Test
    fun `connection template may carry optional downstream hints without owning layout truth`() {
        val connection = ConnectionTemplate(
            templateId = ConnectionTemplateId("template:meter.signal-link"),
            from = TemplatePortReference(
                componentTemplateId = ComponentTemplateId("template:meter"),
                portRoleId = SemanticPortRoleId("SIG+"),
            ),
            to = TemplatePortReference(
                componentTemplateId = ComponentTemplateId("template:controller"),
                portRoleId = SemanticPortRoleId("AI0+"),
            ),
            presentationHints = listOf(
                TemplatePresentationHint(
                    hintType = "line-style",
                    attributes = mapOf("style" to "dashed"),
                ),
            ),
        )

        assertEquals("line-style", connection.presentationHints.single().hintType)
        assertTrue(connection.documentationHints.isEmpty())
    }
}
