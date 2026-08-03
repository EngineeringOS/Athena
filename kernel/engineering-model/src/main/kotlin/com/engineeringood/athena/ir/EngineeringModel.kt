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
    val functions: List<EngineeringFunction> = emptyList(),
    val connectionNetworks: List<EngineeringConnectionNetwork> = emptyList(),
    val externalEvidence: List<EngineeringExternalEvidenceMapping> = emptyList(),
    val projectionPolicies: List<EngineeringProjectionPolicy> = emptyList(),
    val projectionViews: List<EngineeringProjectionView> = emptyList(),
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

/** Device-owned functional partition referencing canonical project ports. */
data class EngineeringFunction(
    val id: StableSemanticIdentity,
    val ownerReference: EngineeringReference,
    val name: String,
    val role: EngineeringFunctionRole,
    val portReferences: List<EngineeringReference>,
    val provenance: SourceProvenance,
)

/** Extensible authored function role; domain plugins interpret known values. */
@JvmInline
value class EngineeringFunctionRole(val value: String) {
    init {
        require(value.isNotBlank()) { "Engineering function role must not be blank" }
    }
}

/** Canonical semantic relationship between two authored engineering references. */
data class EngineeringConnection(
    val id: StableSemanticIdentity,
    val from: EngineeringReference,
    val to: EngineeringReference,
    val provenance: SourceProvenance,
    val properties: List<EngineeringProperty> = emptyList(),
)

/** Canonical semantic network derived from authored grouped connections. */
data class EngineeringConnectionNetwork(
    val id: StableSemanticIdentity,
    val name: String,
    val members: List<EngineeringConnectionNetworkMember>,
    val junctions: List<EngineeringNetworkJunction>,
    val compatibilityEvidence: List<EngineeringNetworkCompatibilityEvidence>,
    val provenance: SourceProvenance,
    val properties: List<EngineeringProperty> = emptyList(),
)

/** One connection participating in a semantic network. */
data class EngineeringConnectionNetworkMember(
    val connectionReference: EngineeringReference,
    val fromPortReference: EngineeringReference,
    val toPortReference: EngineeringReference,
)

/** One semantic junction compiled from shared network membership. */
data class EngineeringNetworkJunction(
    val id: StableSemanticIdentity,
    val sharedPortReference: EngineeringReference,
    val memberConnectionReferences: List<EngineeringReference>,
    val provenance: SourceProvenance,
)

/** Typed evidence explaining why one semantic network is compatible. */
data class EngineeringNetworkCompatibilityEvidence(
    val kind: String,
    val value: String,
    val provenance: SourceProvenance,
)

/** External citation or classification evidence attached to an Athena-owned engineering subject. */
data class EngineeringExternalEvidenceMapping(
    val name: String,
    val namespace: String,
    val reference: String,
    val subject: EngineeringExternalEvidenceSubject,
    val externalProvenance: String,
    val provenance: SourceProvenance,
)

data class EngineeringExternalEvidenceSubject(
    val kind: EngineeringExternalEvidenceSubjectKind,
    val authoredPath: List<String>,
)

enum class EngineeringExternalEvidenceSubjectKind {
    CONTRACT,
    INTERFACE,
    PORT,
    RELATION_CONTRACT,
    ROUTE_POLICY,
}

/** Projection selection authored in Athena source. It selects compiler behavior and owns no engineering truth. */
data class EngineeringProjectionPolicy(
    val name: String,
    val targetSurface: String?,
    val layoutStrategy: String?,
    val drawingProfile: String?,
    val routeQualityPolicy: String?,
    val proofObligations: List<String>,
    val forbiddenEngineeringTruth: List<EngineeringProjectionForbiddenTruth>,
    val provenance: SourceProvenance,
)

data class EngineeringProjectionForbiddenTruth(
    val kind: String,
    val provenance: SourceProvenance,
)

/** Authored projection view: the view-specific engineering document root (M40). */
data class EngineeringProjectionView(
    val name: String,
    val sheets: List<EngineeringProjectionSheet>,
    val regions: List<EngineeringProjectionRegion>,
    val constructs: List<EngineeringProjectionConstruct> = emptyList(),
    val grid: EngineeringProjectionGrid?,
    val readingOrder: List<String> = emptyList(),
    val provenance: SourceProvenance,
)

/** Authored projection sheet with view-local identity and declared order. */
data class EngineeringProjectionSheet(
    val name: String,
    val order: Int,
    val provenance: SourceProvenance,
)

/** Authored sheet grid reference system (rows/columns/cell references); carries no coordinates. */
data class EngineeringProjectionGrid(
    val name: String,
    val rows: Int,
    val columns: Int,
    val provenance: SourceProvenance,
)

/** Authored functional region: a logical document section grouping occurrences by identity. */
data class EngineeringProjectionRegion(
    val name: String,
    val sheetName: String,
    val occurrences: List<String>,
    val provenance: SourceProvenance,
)

/** Authored projection construct carrier: domain-neutral, kind supplied by a domain package. */
data class EngineeringProjectionConstruct(
    val name: String,
    val kind: String,
    val sheetName: String,
    val occurrences: List<String>,
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
