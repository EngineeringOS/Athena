package com.engineeringood.athena.authoring

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.SemanticActionIntent
import com.engineeringood.athena.part.PartImplementationId

private const val RELATIONSHIP_TYPE = "relationshipType"
private const val PROJECTION_VIEW_ID = "projectionViewId"
private const val PROJECTION_OCCURRENCE_ID = "projectionOccurrenceId"
private const val PERSISTENCE_SOURCE_URI = "persistenceSourceUri"
private const val COMPONENT_CONCEPT_ID = "componentConceptId"
private const val PREFERRED_IMPLEMENTATION_ID = "preferredImplementationId"
private const val SUGGESTED_NAME = "suggestedName"

fun SemanticActionIntent.toSemanticRelationshipIntent(): SemanticRelationshipIntent {
    val relationshipType = parameters[RELATIONSHIP_TYPE]?.let(::SemanticRelationshipType)
        ?: ElectricalConnectionRelationship
    val targetSubject = requireNotNull(targetSubjects.singleOrNull()) {
        "Semantic relationship mutation requires exactly one target subject."
    }

    return SemanticRelationshipIntent(
        intentId = AuthoringIntentId(actionIntentId),
        origin = AuthoringOrigin(requestedBy.originSurface.toAuthoringSurface()),
        relationshipType = relationshipType,
        sourceSubjectId = subject.canonicalSubjectId,
        targetSubjectId = targetSubject.canonicalSubjectId,
        projectionContext = SemanticRelationshipProjectionContext(
            viewId = parameters[PROJECTION_VIEW_ID],
            occurrenceId = parameters[PROJECTION_OCCURRENCE_ID],
        ),
        persistenceTarget = SemanticRelationshipPersistenceTarget(
            sourceUri = parameters[PERSISTENCE_SOURCE_URI],
        ),
        provenance = requestedBy.reason,
    )
}

fun SemanticActionIntent.toCreateComponentIntent(): CreateComponentIntent {
    val conceptId = requireNotNull(parameters[COMPONENT_CONCEPT_ID]) {
        "Component creation requires parameter '$COMPONENT_CONCEPT_ID'."
    }

    return CreateComponentIntent(
        intentId = AuthoringIntentId(actionIntentId),
        origin = AuthoringOrigin(requestedBy.originSurface.toAuthoringSurface()),
        parentIdentity = subject.canonicalSubjectId,
        conceptId = EngineeringConceptId(conceptId),
        preferredImplementationId = parameters[PREFERRED_IMPLEMENTATION_ID]?.let(::PartImplementationId),
        suggestedName = parameters[SUGGESTED_NAME],
    )
}

private fun InteractionOriginSurface.toAuthoringSurface(): AuthoringSurface {
    return when (this) {
        InteractionOriginSurface.GRAPH -> AuthoringSurface.GRAPH
        InteractionOriginSurface.SOURCE -> AuthoringSurface.DSL
        InteractionOriginSurface.INSPECTOR -> AuthoringSurface.INSPECTOR
        InteractionOriginSurface.PROBLEMS -> AuthoringSurface.FORM
        InteractionOriginSurface.PALETTE,
        InteractionOriginSurface.COMMAND_PALETTE,
        -> AuthoringSurface.PALETTE
        InteractionOriginSurface.AI -> AuthoringSurface.AI
        InteractionOriginSurface.API -> AuthoringSurface.API
        InteractionOriginSurface.RUNTIME -> AuthoringSurface.TEMPLATE
    }
}
