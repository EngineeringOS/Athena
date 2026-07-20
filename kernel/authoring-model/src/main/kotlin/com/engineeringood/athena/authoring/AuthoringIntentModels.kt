package com.engineeringood.athena.authoring

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.part.PartImplementationId

/**
 * Stable identifier for one guided authoring request.
 *
 * This id tracks one user-intent-level action above M8 mutation authority. It is not a canonical
 * semantic subject identity and it does not become a second truth model.
 */
@JvmInline
value class AuthoringIntentId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable name for one editable authoring property.
 *
 * Property names stay surface-agnostic so later palette, inspector, form, template, or AI flows
 * can target the same contract.
 */
@JvmInline
value class AuthoringPropertyName(val value: String) {
    override fun toString(): String = value
}

/**
 * One originating surface for a guided authoring request.
 *
 * The origin is informative product metadata only. It does not own mutation semantics.
 */
data class AuthoringOrigin(
    val surface: AuthoringSurface,
    val detail: String? = null,
)

/** Enumerates the first platform-recognized surfaces that may emit guided authoring intents. */
enum class AuthoringSurface {
    PALETTE,
    INSPECTOR,
    GRAPH,
    FORM,
    TEMPLATE,
    AI,
    API,
    DSL,
}

/** Enumerates the first workbench targets that can reveal one canonical semantic subject. */
enum class AuthoringRevealTarget {
    SOURCE,
    GRAPH,
    INSPECTOR,
    SEMANTIC_SCM,
}

/**
 * Small typed value surface for guided authoring requests.
 *
 * These values stay transport-friendly and preview-friendly without becoming the mutation runtime.
 */
sealed interface AuthoringValue {
    /** Human-entered free text such as a description or display label. */
    data class Text(val text: String) : AuthoringValue

    /** Symbolic authored value such as a tag, short code, or part-family token. */
    data class Symbol(val text: String) : AuthoringValue

    /** Boolean toggle value for future guided forms. */
    data class BooleanValue(val value: Boolean) : AuthoringValue

    /** Integer value for future guided authoring counts and narrow numeric fields. */
    data class IntegerValue(val value: Int) : AuthoringValue
}

/**
 * Shared platform-owned contract for guided authoring requests.
 *
 * Frontend widgets, graph tools, forms, templates, and AI surfaces may emit these requests, but
 * they may not treat this contract as a direct mutation path. Accepted intent still expands into
 * governed M8 mutations later.
 */
sealed interface AuthoringIntent {
    val intentId: AuthoringIntentId
    val origin: AuthoringOrigin
}

/**
 * Requests creation of one component instance from governed component knowledge.
 *
 * The canonical engineering subject does not exist yet, so the request targets one parent identity,
 * one engineering concept, and an optional preferred implementation.
 */
data class CreateComponentIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val parentIdentity: StableSemanticIdentity,
    val conceptId: EngineeringConceptId,
    val preferredImplementationId: PartImplementationId? = null,
    val suggestedName: String? = null,
) : AuthoringIntent

/**
 * Requests a governed property update for one existing canonical component.
 *
 * This contract stays above mutation execution. It records user intent only.
 */
data class UpdateComponentPropertiesIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val componentId: StableSemanticIdentity,
    val properties: Map<AuthoringPropertyName, AuthoringValue>,
) : AuthoringIntent

/** Stable relationship type name for governed semantic relationship authoring. */
@JvmInline
value class SemanticRelationshipType(val value: String) {
    override fun toString(): String = value
}

/** M28 first relationship specialization: an electrical connection between compatible port subjects. */
val ElectricalConnectionRelationship = SemanticRelationshipType("ElectricalConnectionRelationship")

/** Optional projection context that helped produce a relationship authoring request. */
data class SemanticRelationshipProjectionContext(
    val viewId: String? = null,
    val occurrenceId: String? = null,
)

/** Optional persistence target chosen or inferred for accepted semantic relationship mutation. */
data class SemanticRelationshipPersistenceTarget(
    val sourceUri: String? = null,
)

/**
 * Requests creation of one governed semantic relationship between two existing semantic subjects.
 *
 * Electrical connection is the first specialization, but this contract is intentionally not named
 * around wires so later flow, containment, control, communication, mounting, and dependency
 * relationships can reuse the same authoring boundary.
 */
data class SemanticRelationshipIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val relationshipType: SemanticRelationshipType,
    val sourceSubjectId: StableSemanticIdentity,
    val targetSubjectId: StableSemanticIdentity,
    val projectionContext: SemanticRelationshipProjectionContext = SemanticRelationshipProjectionContext(),
    val persistenceTarget: SemanticRelationshipPersistenceTarget = SemanticRelationshipPersistenceTarget(),
    val provenance: String? = null,
) : AuthoringIntent

/**
 * Requests creation of one canonical connection between two existing semantic ports.
 *
 * The contract captures user intent from graph or guided flows without allowing graph code to
 * create semantic state directly. Kept for legacy call sites; new M28 architecture should use
 * [SemanticRelationshipIntent] with [ElectricalConnectionRelationship].
 */
data class ConnectPortsIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val sourcePortId: StableSemanticIdentity,
    val targetPortId: StableSemanticIdentity,
) : AuthoringIntent {
    fun toSemanticRelationshipIntent(
        projectionContext: SemanticRelationshipProjectionContext = SemanticRelationshipProjectionContext(),
        persistenceTarget: SemanticRelationshipPersistenceTarget = SemanticRelationshipPersistenceTarget(),
        provenance: String? = null,
    ): SemanticRelationshipIntent {
        return SemanticRelationshipIntent(
            intentId = intentId,
            origin = origin,
            relationshipType = ElectricalConnectionRelationship,
            sourceSubjectId = sourcePortId,
            targetSubjectId = targetPortId,
            projectionContext = projectionContext,
            persistenceTarget = persistenceTarget,
            provenance = provenance,
        )
    }
}

/**
 * Requests reveal of one canonical semantic subject across guided authoring surfaces.
 *
 * Reveal stays canonical-identity-first so widget ids and graph ids remain secondary.
 */
data class RevealSubjectIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val subjectId: StableSemanticIdentity,
    val targets: Set<AuthoringRevealTarget>,
) : AuthoringIntent
