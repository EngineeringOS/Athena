package com.engineeringood.athena.authoring

import com.engineeringood.athena.component.EngineeringConceptTemplateId
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

data class SemanticEntityCreationContext(
    val parentSubjectId: StableSemanticIdentity,
    val sourceUri: String? = null,
)

sealed interface MutableSemanticEntityIntent : AuthoringIntent {
    val revisionGuard: AuthoringRevisionGuard
    val provenance: AuthoringTransactionProvenance
}

data class CreateSemanticEntityIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val creationContext: SemanticEntityCreationContext,
    val conceptTemplateId: EngineeringConceptTemplateId,
    val conceptId: EngineeringConceptId,
    val properties: Map<AuthoringPropertyName, AuthoringValue> = emptyMap(),
    val preferredImplementationId: PartImplementationId? = null,
    val suggestedName: String? = null,
    override val revisionGuard: AuthoringRevisionGuard,
    override val provenance: AuthoringTransactionProvenance,
) : MutableSemanticEntityIntent {
    init {
        require(provenance.origin == origin) { "Entity intent provenance origin must match intent origin." }
    }
}

data class UpdateSemanticEntityPropertiesIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val subjectId: StableSemanticIdentity,
    val properties: Map<AuthoringPropertyName, AuthoringValue>,
    override val revisionGuard: AuthoringRevisionGuard,
    override val provenance: AuthoringTransactionProvenance,
) : MutableSemanticEntityIntent {
    init {
        require(properties.isNotEmpty()) { "Entity property update must include at least one property." }
        require(provenance.origin == origin) { "Entity intent provenance origin must match intent origin." }
    }
}

data class RemoveSemanticEntityIntent(
    override val intentId: AuthoringIntentId,
    override val origin: AuthoringOrigin,
    val subjectId: StableSemanticIdentity,
    override val revisionGuard: AuthoringRevisionGuard,
    override val provenance: AuthoringTransactionProvenance,
) : MutableSemanticEntityIntent {
    init {
        require(provenance.origin == origin) { "Entity intent provenance origin must match intent origin." }
    }
}

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
 * Requests validation and preview of removing one existing governed semantic relationship.
 *
 * M31 keeps this contract typed without exposing accepted removal UX. Endpoint identities remain
 * explicit so dependency-impact validation cannot accidentally treat route or projection ids as
 * relationship truth.
 */
data class RemoveSemanticRelationshipIntent(
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
