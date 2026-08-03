package com.engineeringood.athena.ir

data class RealityDeclaration(
    val name: String,
    val rootName: String,
    val purpose: String,
    val authority: String,
    val ownedFacts: List<String>,
    val identityRules: List<RealityIdentityRule>,
    val requiredFacts: List<String>,
) {
    init {
        require(name.isNotBlank()) { "Reality name must not be blank." }
        require(rootName.isNotBlank()) { "Reality root name must not be blank." }
        require(purpose.isNotBlank()) { "Reality purpose must not be blank." }
        require(authority.isNotBlank()) { "Reality authority must not be blank." }
        require(ownedFacts.isNotEmpty()) { "Reality owned facts must not be empty." }
        require(identityRules.isNotEmpty()) { "Reality identity rules must not be empty." }
        require(requiredFacts.isNotEmpty()) { "Reality required facts must not be empty." }
        require(ownedFacts.all(String::isNotBlank)) { "Reality owned fact names must not be blank." }
        require(requiredFacts.all(String::isNotBlank)) { "Reality required fact names must not be blank." }
    }
}

data class RealityIdentityRule(
    val fact: String,
    val rule: String,
) {
    init {
        require(fact.isNotBlank()) { "Reality identity rule fact must not be blank." }
        require(rule.isNotBlank()) { "Reality identity rule must not be blank." }
    }
}

data class RealityValidationIssue(
    val reality: String,
    val message: String,
) {
    init {
        require(reality.isNotBlank()) { "Reality validation issue reality must not be blank." }
        require(message.isNotBlank()) { "Reality validation issue message must not be blank." }
    }
}

data class RealityValidationResult(
    val issues: List<RealityValidationIssue>,
) {
    val isValid: Boolean
        get() = issues.isEmpty()

    companion object {
        val Valid: RealityValidationResult = RealityValidationResult(emptyList())
    }
}

