package com.engineeringood.athena.ir

/** Exact subject level admitted by one package-defined Relationship role. */
sealed interface EngineeringSubjectReference {
    val reference: EngineeringReference

    data class Entity(val entity: EngineeringEntityReference) : EngineeringSubjectReference {
        override val reference: EngineeringReference
            get() = entity.reference
    }

    data class Function(val function: EngineeringFunctionReference) : EngineeringSubjectReference {
        override val reference: EngineeringReference
            get() = function.reference
    }

    data class Port(val port: EngineeringReference) : EngineeringSubjectReference {
        override val reference: EngineeringReference
            get() = port
    }

    /** Invalid authored subject retained only so validation can report exact provenance. */
    data class Unresolved(val authored: EngineeringReference) : EngineeringSubjectReference {
        override val reference: EngineeringReference
            get() = authored
    }
}

/** One named role binding in a project Engineering Relationship. */
data class EngineeringParticipant(
    val role: String,
    val subject: EngineeringSubjectReference,
    val provenance: SourceProvenance,
) {
    init {
        require(role.isNotBlank()) { "Engineering Relationship participant role must not be blank" }
        require(subject.reference.authoredPath.isNotEmpty()) {
            "Engineering Relationship participant requires an authored subject path"
        }
    }
}

/** Project-level semantic relationship. Definition meaning is supplied by a package. */
data class EngineeringRelationship(
    val id: StableSemanticIdentity,
    val definitionReference: EngineeringDefinitionReference,
    val participants: List<EngineeringParticipant>,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
) {
    init {
        require(participants.size >= 2) { "Engineering Relationship requires at least two participants" }
        require(participants.map { it.role }.distinct().size == participants.size) {
            "Engineering Relationship participant roles must be unique"
        }
    }
}

/** Independent project Flow fact attached to a Relationship and its named roles. */
data class EngineeringFlow(
    val id: StableSemanticIdentity,
    val relationship: EngineeringReference,
    val definitionReference: EngineeringDefinitionReference,
    val sourceRole: String,
    val sinkRole: String,
    val medium: EngineeringDefinitionReference?,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
) {
    init {
        require(sourceRole.isNotBlank()) { "Engineering Flow source role must not be blank" }
        require(sinkRole.isNotBlank()) { "Engineering Flow sink role must not be blank" }
        require(relationship.authoredPath.isNotEmpty()) {
            "Engineering Flow requires an authored Relationship reference"
        }
    }
}
