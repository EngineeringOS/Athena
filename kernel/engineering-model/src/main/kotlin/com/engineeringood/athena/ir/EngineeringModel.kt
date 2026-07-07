package com.engineeringood.athena.ir

/**
 * Stable semantic identifier for one canonical engineering object.
 *
 * The identifier is derived from authored meaning rather than parser offsets or runtime object identity.
 */
@JvmInline
value class StableSemanticIdentity(val value: String) {
    override fun toString(): String = value
}

/** Provenance captured for one authored span that contributed to a canonical engineering intermediate representation object. */
data class SourceProvenance(
    val file: String,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
)

/** Root canonical engineering intermediate representation document emitted by the lowering boundary. */
data class EngineeringDocument(
    val system: EngineeringSystem,
    val components: List<EngineeringComponent>,
    val ports: List<EngineeringPort>,
    val connections: List<EngineeringConnection>,
)

/** Canonical semantic representation of the authored system root. */
data class EngineeringSystem(
    val id: StableSemanticIdentity,
    val name: String,
    val provenance: SourceProvenance,
)

/** Canonical semantic representation of an engineering component such as a device. */
data class EngineeringComponent(
    val id: StableSemanticIdentity,
    val name: String,
    val kind: String,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

/** Canonical semantic representation of a port owned by another engineering object. */
data class EngineeringPort(
    val id: StableSemanticIdentity,
    val ownerReference: EngineeringReference,
    val name: String,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

/** Canonical semantic relationship between two authored engineering references. */
data class EngineeringConnection(
    val id: StableSemanticIdentity,
    val from: EngineeringReference,
    val to: EngineeringReference,
    val provenance: SourceProvenance,
)

/** Authored semantic reference preserved for later validation, whether or not it resolved during lowering. */
data class EngineeringReference(
    val authoredPath: List<String>,
    val resolvedIdentity: StableSemanticIdentity?,
    val provenance: SourceProvenance,
)

/** Typed authored property carried into the canonical engineering model. */
data class EngineeringProperty(
    val name: String,
    val value: EngineeringPropertyValue,
)

/** Small typed value surface for the first M0 engineering property set. */
sealed interface EngineeringPropertyValue {
    /** Symbolic authored value such as `PLC`, `Digital`, or `out`. */
    data class Symbol(val text: String) : EngineeringPropertyValue

    /** Text-authored value such as a quoted model string. */
    data class Text(val text: String) : EngineeringPropertyValue
}
