package com.engineeringood.athena.policy

import com.engineeringood.athena.representation.RepresentationId

object AthenaIndustrialControlV0Profile {
    private val profileId = PresentationPolicyProfileId("athena-industrial-control-v0")

    fun profile(): PresentationPolicyProfile = PresentationPolicyProfile(
        profileId = profileId,
        claimsIecCompleteness = false,
        supportedFamilies = listOf(
            "plc-controller",
            "hmi-operator",
            "terminal-block",
            "power-supply",
            "protection-device",
            "load-actuator",
        ).associate { family ->
            ComponentFamilyKey(family) to RepresentationId("${profileId.value}:$family")
        },
    )
}

data class PresentationPolicyModelModuleMarker(val moduleName: String = "kernel:presentation-policy-model")
