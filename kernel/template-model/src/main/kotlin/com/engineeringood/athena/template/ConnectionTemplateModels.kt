package com.engineeringood.athena.template

import com.engineeringood.athena.connection.SemanticPortRoleId

/**
 * Stable identifier for one reusable connection template inside a semantic macro payload.
 *
 * The connection template remains a payload contract only. It does not imply canonical connection
 * identity, routing geometry, or acceptance state by itself.
 */
@JvmInline
value class ConnectionTemplateId(val value: String) {
    override fun toString(): String = value
}

/**
 * Semantic endpoint reference used by a reusable connection template.
 *
 * Endpoints are expressed in terms of component template identity and semantic port role so the
 * payload remains engineering-first and independent from graphics coordinates.
 */
data class TemplatePortReference(
    val componentTemplateId: ComponentTemplateId,
    val portRoleId: SemanticPortRoleId,
)

/**
 * Reusable semantic connection payload published by the template model.
 *
 * The template binds two semantic endpoints and may carry default metadata and optional downstream
 * hints. It does not turn line styling, sheet placement, or SVG geometry into source truth.
 */
data class ConnectionTemplate(
    val templateId: ConnectionTemplateId,
    val from: TemplatePortReference,
    val to: TemplatePortReference,
    val defaultMetadata: TemplateDefaultMetadata = TemplateDefaultMetadata(),
    val presentationHints: List<TemplatePresentationHint> = emptyList(),
    val documentationHints: List<TemplateDocumentationHint> = emptyList(),
)
