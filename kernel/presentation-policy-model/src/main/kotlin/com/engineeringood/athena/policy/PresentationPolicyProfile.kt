package com.engineeringood.athena.policy

import com.engineeringood.athena.representation.RepresentationContext
import com.engineeringood.athena.representation.RepresentationId

@JvmInline
value class PresentationPolicyProfileId(val value: String) {
    init {
        require(value.isNotBlank()) { "Presentation policy profile id must not be blank." }
    }
}

@JvmInline
value class ComponentFamilyKey(val value: String) {
    init {
        require(value.isNotBlank()) { "Component family key must not be blank." }
    }
}

data class PresentationPolicyDiagnostic(
    val profileId: PresentationPolicyProfileId,
    val family: ComponentFamilyKey,
    val missingCapability: String,
    val message: String,
) {
    init {
        require(missingCapability.isNotBlank()) { "Missing capability must not be blank." }
        require(message.isNotBlank()) { "Presentation policy diagnostic message must not be blank." }
    }
}

sealed interface RepresentationSelection {
    val profileId: PresentationPolicyProfileId
    val family: ComponentFamilyKey

    data class Supported(
        override val profileId: PresentationPolicyProfileId,
        override val family: ComponentFamilyKey,
        val representationId: RepresentationId,
        val context: RepresentationContext,
    ) : RepresentationSelection

    data class Fallback(
        override val profileId: PresentationPolicyProfileId,
        override val family: ComponentFamilyKey,
        val diagnostic: PresentationPolicyDiagnostic,
    ) : RepresentationSelection
}

data class PresentationPolicyProfile(
    val profileId: PresentationPolicyProfileId,
    val claimsIecCompleteness: Boolean,
    private val supportedFamilies: Map<ComponentFamilyKey, RepresentationId>,
    val context: RepresentationContext = RepresentationContext.ELECTRICAL_SCHEMATIC,
) {
    fun selectRepresentation(family: ComponentFamilyKey): RepresentationSelection {
        val representationId = supportedFamilies[family]
        return if (representationId == null) {
            RepresentationSelection.Fallback(
                profileId = profileId,
                family = family,
                diagnostic = PresentationPolicyDiagnostic(
                    profileId = profileId,
                    family = family,
                    missingCapability = "representation-family",
                    message = "Profile `${profileId.value}` does not define representation family `${family.value}`.",
                ),
            )
        } else {
            RepresentationSelection.Supported(
                profileId = profileId,
                family = family,
                representationId = representationId,
                context = context,
            )
        }
    }
}

data class RepresentationPolicyCoverageProof(
    val profile: PresentationPolicyProfile,
    val mandatoryFamilies: List<ComponentFamilyKey>,
) {
    init {
        require(mandatoryFamilies.isNotEmpty()) { "Coverage proof requires at least one mandatory family." }
    }

    fun selections(): List<RepresentationSelection> =
        mandatoryFamilies.map(profile::selectRepresentation)

    fun fallbackSelections(): List<RepresentationSelection.Fallback> =
        selections().filterIsInstance<RepresentationSelection.Fallback>()

    fun hasZeroFallbackSymbols(): Boolean = fallbackSelections().isEmpty()
}
