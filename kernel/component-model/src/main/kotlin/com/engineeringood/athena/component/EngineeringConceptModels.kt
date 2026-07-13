package com.engineeringood.athena.component

/**
 * Stable identifier for one vendor-neutral engineering concept.
 *
 * An engineering concept names the semantic idea Athena resolves to, such as a PLC CPU or motor
 * starter. It is not a vendor product id and it does not replace canonical authored identity in
 * `Engineering IR`.
 */
@JvmInline
value class EngineeringConceptId(val value: String) {
    override fun toString(): String = value
}

/**
 * Vendor-neutral semantic definition published by the component knowledge layer.
 *
 * This contract stays above canonical `Engineering IR` and below later vendor implementation,
 * semantic-port, physical-trait, and downstream knowledge or presentation consumers.
 */
data class EngineeringConceptDefinition(
    val conceptId: EngineeringConceptId,
    val displayName: String,
    val classificationKeys: Set<String> = emptySet(),
    val summary: String? = null,
)
