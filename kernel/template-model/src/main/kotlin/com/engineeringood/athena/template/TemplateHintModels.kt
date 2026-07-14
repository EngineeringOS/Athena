package com.engineeringood.athena.template

/**
 * Template-scoped default metadata carried by reusable component and connection payloads.
 *
 * This metadata improves naming, review, and downstream presentation, but it does not become
 * canonical geometry, package identity, or runtime execution truth.
 */
data class TemplateDefaultMetadata(
    val displayName: String? = null,
    val summary: String? = null,
    val tags: Set<String> = emptySet(),
)

/**
 * Optional advisory presentation hint attached to one reusable template payload.
 *
 * Hints may be consumed by downstream preview or renderer layers, but they remain replaceable and
 * may not define the engineering source of truth.
 */
data class TemplatePresentationHint(
    val hintType: String,
    val attributes: Map<String, String> = emptyMap(),
)

/**
 * Optional advisory documentation hint attached to one reusable template payload.
 *
 * These hints improve generated documentation or review narratives without affecting semantic
 * expansion meaning.
 */
data class TemplateDocumentationHint(
    val hintType: String,
    val attributes: Map<String, String> = emptyMap(),
)
