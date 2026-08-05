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
    val entities: List<EngineeringEntity>,
    val functions: List<EngineeringFunction> = emptyList(),
    val ports: List<EngineeringPort>,
    val relationships: List<EngineeringRelationship> = emptyList(),
    val flows: List<EngineeringFlow> = emptyList(),
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
    val value: EngineeringValue,
    val provenance: SourceProvenance,
)
