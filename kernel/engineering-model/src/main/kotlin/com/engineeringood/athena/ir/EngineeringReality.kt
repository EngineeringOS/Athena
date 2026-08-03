package com.engineeringood.athena.ir

object EngineeringReality {
    const val name: String = "Engineering Reality"
    const val rootName: String = "EngineeringDocument"
    const val purpose: String = "Represents what the engineered system is."
    const val authority: String = "engineering compiler"

    val ownedFacts: List<String> = listOf(
        "system",
        "device",
        "port",
        "signal",
        "connection",
        "network",
        "constraint",
    )

    val identityRules: List<RealityIdentityRule> = listOf(
        RealityIdentityRule("system", "System identity comes from the authored system id."),
        RealityIdentityRule("device", "Device identity comes from the authored component path."),
        RealityIdentityRule("port", "Port identity comes from the owning device plus port name."),
        RealityIdentityRule("connection", "Connection identity comes from source and target port identities."),
    )

    val requiredFacts: List<String> = listOf(
        "system identity",
        "engineering source identity",
    )

    val declaration: RealityDeclaration = RealityDeclaration(
        name = name,
        rootName = rootName,
        purpose = purpose,
        authority = authority,
        ownedFacts = ownedFacts,
        identityRules = identityRules,
        requiredFacts = requiredFacts,
    )

    fun validate(document: EngineeringDocument): RealityValidationResult {
        val issues = buildList {
            if (document.system.id.value.isBlank() || document.system.name.isBlank()) {
                add(RealityValidationIssue(name, "missing system identity"))
            }
            if (document.system.provenance.file.isBlank()) {
                add(RealityValidationIssue(name, "missing engineering source identity"))
            }
        }
        return RealityValidationResult(issues)
    }
}
