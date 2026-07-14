package com.engineeringood.athena.template

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.part.PartImplementationId
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValue

/**
 * Stable identifier for one reusable component template inside a semantic macro payload.
 *
 * Template identity stays separate from both macro identity and eventual canonical semantic subject
 * identity so later expansion can remain deterministic.
 */
@JvmInline
value class ComponentTemplateId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable name for one authored component-template property.
 *
 * Property names stay semantic and transport-friendly. They are not UI field ids or renderer keys.
 */
@JvmInline
value class ComponentTemplatePropertyName(val value: String) {
    override fun toString(): String = value
}

/**
 * Template-scoped property value used by reusable component payloads.
 *
 * A value may be literal or may defer to one semantic-macro parameter. This keeps payloads reusable
 * without pushing form or runtime logic into the template model.
 */
sealed interface TemplateValue {
    /** Literal value carried directly inside the reusable template payload. */
    data class Literal(val value: SemanticMacroParameterValue) : TemplateValue

    /** Reference to one macro parameter that will be resolved later by runtime-owned flows. */
    data class ParameterReference(val parameterName: SemanticMacroParameterName) : TemplateValue
}

/**
 * Reusable semantic component payload published by the template model.
 *
 * The template identifies which engineering concept should be instantiated and may suggest one
 * vendor implementation mapping, default metadata, and advisory hints. It does not define graphic
 * truth, package resolution, or runtime expansion behavior.
 */
data class ComponentTemplate(
    val templateId: ComponentTemplateId,
    val conceptId: EngineeringConceptId,
    val implementationId: PartImplementationId? = null,
    val defaultMetadata: TemplateDefaultMetadata = TemplateDefaultMetadata(),
    val properties: Map<ComponentTemplatePropertyName, TemplateValue> = emptyMap(),
    val presentationHints: List<TemplatePresentationHint> = emptyList(),
    val documentationHints: List<TemplateDocumentationHint> = emptyList(),
)
