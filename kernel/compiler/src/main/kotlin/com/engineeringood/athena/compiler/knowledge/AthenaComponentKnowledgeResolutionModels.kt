package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.component.EngineeringConceptDefinition
import com.engineeringood.athena.component.ResolvedComponentDefinition
import com.engineeringood.athena.part.PartImplementationDefinition
import com.engineeringood.athena.part.ResolvedPartImplementation

/** Severity assigned to compiler-owned component-knowledge diagnostics. */
enum class AthenaComponentKnowledgeSeverity {
    ERROR,
}

/** Stable identifier for one compiler-owned component-knowledge rule. */
@JvmInline
value class AthenaComponentKnowledgeRuleId(val value: String) {
    override fun toString(): String = value
}

/** Inspectable compiler-owned diagnostic emitted while resolving component definitions. */
data class AthenaComponentKnowledgeDiagnostic(
    val severity: AthenaComponentKnowledgeSeverity,
    val ruleId: AthenaComponentKnowledgeRuleId,
    val subject: String,
    val message: String,
)

/** One engineering-concept definition contributed by one active governed knowledge artifact. */
data class AthenaConceptDefinitionContribution(
    val artifactId: String,
    val artifactVersion: String,
    val concept: EngineeringConceptDefinition,
)

/** One vendor-implementation definition contributed by one active governed knowledge artifact. */
data class AthenaPartImplementationContribution(
    val artifactId: String,
    val artifactVersion: String,
    val implementation: PartImplementationDefinition,
)

/**
 * Deterministic active component-knowledge definitions admitted by the current governed pack set.
 *
 * The catalog is canonicalized in stable artifact and identity order so resolution does not depend
 * on load order.
 */
data class AthenaComponentKnowledgeCatalog(
    val concepts: List<AthenaConceptDefinitionContribution>,
    val implementations: List<AthenaPartImplementationContribution>,
) {
    companion object {
        /** Builds a canonical catalog from active concept and implementation contributions. */
        fun canonical(
            concepts: List<AthenaConceptDefinitionContribution>,
            implementations: List<AthenaPartImplementationContribution>,
        ): AthenaComponentKnowledgeCatalog {
            return AthenaComponentKnowledgeCatalog(
                concepts = concepts.sortedWith(compareBy(
                    AthenaConceptDefinitionContribution::artifactId,
                    AthenaConceptDefinitionContribution::artifactVersion,
                    { contribution -> contribution.concept.conceptId.value },
                    { contribution -> contribution.concept.displayName },
                )),
                implementations = implementations.sortedWith(compareBy(
                    AthenaPartImplementationContribution::artifactId,
                    AthenaPartImplementationContribution::artifactVersion,
                    { contribution -> contribution.implementation.vendorId.value },
                    { contribution -> contribution.implementation.vendorPartNumber.value },
                    { contribution -> contribution.implementation.implementationId.value },
                )),
            )
        }
    }
}

/** Result of resolving one authored component reference through the active governed knowledge catalog. */
data class AthenaComponentKnowledgeResolutionResult(
    val resolvedComponent: ResolvedComponentDefinition?,
    val resolvedImplementation: ResolvedPartImplementation?,
    val diagnostics: List<AthenaComponentKnowledgeDiagnostic>,
) {
    /** True when the reference resolved without compiler-owned errors. */
    val isResolved: Boolean
        get() = resolvedComponent != null && diagnostics.none { it.severity == AthenaComponentKnowledgeSeverity.ERROR }
}
