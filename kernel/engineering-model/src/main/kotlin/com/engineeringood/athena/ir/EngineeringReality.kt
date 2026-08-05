package com.engineeringood.athena.ir

object EngineeringReality {
    const val name: String = "Engineering Reality"
    const val rootName: String = "EngineeringDocument"
    const val purpose: String = "Represents what the engineered system is."
    const val authority: String = "engineering compiler"

    val ownedFacts: List<String> = listOf(
        "system",
        "entity",
        "function",
        "port",
        "flow",
        "relationship",
        "constraint",
    )

    val identityRules: List<RealityIdentityRule> = listOf(
        RealityIdentityRule("system", "System identity comes from the authored system id."),
        RealityIdentityRule("entity", "Entity identity comes from its authored semantic path."),
        RealityIdentityRule("function", "Function identity comes from its owning Entity plus Function name."),
        RealityIdentityRule("port", "Port identity comes from its exact Entity or Function owner plus Port name."),
        RealityIdentityRule("relationship", "Relationship identity comes from authored definition and participant roles."),
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
