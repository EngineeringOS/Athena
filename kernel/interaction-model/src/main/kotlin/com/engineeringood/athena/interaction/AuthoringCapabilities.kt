package com.engineeringood.athena.interaction

enum class AuthoringIntentKind {
    CREATE_ENTITY,
    UPDATE_ENTITY,
    REMOVE_ENTITY,
    CREATE_RELATIONSHIP,
    REMOVE_RELATIONSHIP,
}

enum class AuthoringCapabilityRequirementKind {
    DOMAIN,
    CONCEPT_TEMPLATE,
    PROJECTION,
    REPRESENTATION,
}

data class AuthoringCapabilityRequirement(
    val kind: AuthoringCapabilityRequirementKind,
    val identifier: String,
    val satisfied: Boolean,
    val reason: String? = null,
) {
    init {
        require(identifier.isNotBlank()) { "Authoring capability requirement identifier must not be blank." }
        require(satisfied || !reason.isNullOrBlank()) {
            "Unsatisfied authoring capability requirements must explain why they are unavailable."
        }
    }
}

data class AuthoringCapability(
    val intentKind: AuthoringIntentKind,
    val allowedOrigins: Set<InteractionOriginSurface>,
    val requirements: List<AuthoringCapabilityRequirement>,
) {
    init {
        require(allowedOrigins.isNotEmpty()) { "Authoring capability actor policy must allow at least one origin." }
        require(requirements.map(AuthoringCapabilityRequirement::kind).distinct().size == requirements.size) {
            "Authoring capability requirements must not repeat a requirement kind."
        }
    }
}

data class AuthoringCapabilityEvidence(
    val capabilityId: String,
    val intentKind: AuthoringIntentKind,
    val subject: InteractionSubjectKey,
    val actorOrigin: InteractionOriginSurface,
    val satisfiedRequirements: List<AuthoringCapabilityRequirement>,
    val relatedSubjects: Set<InteractionSubjectKey> = emptySet(),
)

data class AuthoringCapabilityDiscoveryResult(
    val evidence: List<AuthoringCapabilityEvidence>,
    val diagnostics: List<InteractionDiagnostic>,
)
