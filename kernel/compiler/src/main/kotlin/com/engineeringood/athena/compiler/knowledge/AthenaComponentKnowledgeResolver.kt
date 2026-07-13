package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.component.EngineeringConceptDefinition
import com.engineeringood.athena.component.ResolvedComponentDefinition
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.part.PartImplementationDefinition
import com.engineeringood.athena.part.ResolvedPartImplementation

/**
 * Resolves one authored component reference against the active governed component-knowledge catalog.
 *
 * The resolver fails explicitly on unresolved or conflicting definitions. It never selects a winner
 * implicitly from pack order.
 */
class AthenaComponentKnowledgeResolver {
    /** Resolves [authoredComponentReference] for one canonical semantic subject. */
    fun resolve(
        semanticSubjectId: StableSemanticIdentity,
        authoredComponentReference: String,
        catalog: AthenaComponentKnowledgeCatalog,
    ): AthenaComponentKnowledgeResolutionResult {
        val directConceptMatches = catalog.concepts
            .filter { contribution -> contribution.concept.conceptId.value == authoredComponentReference }
        val directImplementationMatches = catalog.implementations
            .filter { contribution -> contribution.implementation.vendorPartNumber.value == authoredComponentReference }

        conflictDiagnosticForConcept(authoredComponentReference, directConceptMatches)?.let { diagnostic ->
            return AthenaComponentKnowledgeResolutionResult(
                resolvedComponent = null,
                resolvedImplementation = null,
                diagnostics = listOf(diagnostic),
            )
        }
        conflictDiagnosticForImplementation(authoredComponentReference, directImplementationMatches)?.let { diagnostic ->
            return AthenaComponentKnowledgeResolutionResult(
                resolvedComponent = null,
                resolvedImplementation = null,
                diagnostics = listOf(diagnostic),
            )
        }

        val resolvedImplementation = directImplementationMatches
            .map(AthenaPartImplementationContribution::implementation)
            .distinct()
            .singleOrNull()
        val resolvedConcept = when {
            resolvedImplementation != null -> {
                val conceptMatchesForImplementation = catalog.concepts.filter { contribution ->
                    contribution.concept.conceptId == resolvedImplementation.conceptId
                }
                conflictDiagnosticForConcept(resolvedImplementation.conceptId.value, conceptMatchesForImplementation)?.let { diagnostic ->
                    return AthenaComponentKnowledgeResolutionResult(
                        resolvedComponent = null,
                        resolvedImplementation = null,
                        diagnostics = listOf(diagnostic),
                    )
                }
                conceptMatchesForImplementation
                    .map(AthenaConceptDefinitionContribution::concept)
                    .distinct()
                    .singleOrNull()
            }

            else -> directConceptMatches
                .map(AthenaConceptDefinitionContribution::concept)
                .distinct()
                .singleOrNull()
        }

        if (resolvedConcept == null) {
            return AthenaComponentKnowledgeResolutionResult(
                resolvedComponent = null,
                resolvedImplementation = null,
                diagnostics = listOf(unresolvedDiagnostic(authoredComponentReference)),
            )
        }

        return AthenaComponentKnowledgeResolutionResult(
            resolvedComponent = ResolvedComponentDefinition(
                semanticSubjectId = semanticSubjectId,
                authoredComponentReference = authoredComponentReference,
                concept = resolvedConcept,
            ),
            resolvedImplementation = resolvedImplementation?.let { implementation ->
                ResolvedPartImplementation(
                    semanticSubjectId = semanticSubjectId,
                    implementation = implementation,
                )
            },
            diagnostics = emptyList(),
        )
    }

    private fun conflictDiagnosticForConcept(
        authoredComponentReference: String,
        contributions: List<AthenaConceptDefinitionContribution>,
    ): AthenaComponentKnowledgeDiagnostic? {
        val distinctConcepts = contributions.map(AthenaConceptDefinitionContribution::concept).distinct()
        if (distinctConcepts.size <= 1) {
            return null
        }
        return AthenaComponentKnowledgeDiagnostic(
            severity = AthenaComponentKnowledgeSeverity.ERROR,
            ruleId = AthenaComponentKnowledgeRuleId("component.definition.conflict.concept"),
            subject = authoredComponentReference,
            message = "Conflicting engineering concept definitions were contributed for `$authoredComponentReference` by ${artifactSummary(contributions.map { contribution -> contribution.artifactId to contribution.artifactVersion })}.",
        )
    }

    private fun conflictDiagnosticForImplementation(
        authoredComponentReference: String,
        contributions: List<AthenaPartImplementationContribution>,
    ): AthenaComponentKnowledgeDiagnostic? {
        val distinctImplementations = contributions.map(AthenaPartImplementationContribution::implementation).distinct()
        if (distinctImplementations.size <= 1) {
            return null
        }
        return AthenaComponentKnowledgeDiagnostic(
            severity = AthenaComponentKnowledgeSeverity.ERROR,
            ruleId = AthenaComponentKnowledgeRuleId("component.definition.conflict.implementation"),
            subject = authoredComponentReference,
            message = "Conflicting vendor implementation definitions were contributed for `$authoredComponentReference` by ${artifactSummary(contributions.map { contribution -> contribution.artifactId to contribution.artifactVersion })}.",
        )
    }

    private fun unresolvedDiagnostic(authoredComponentReference: String): AthenaComponentKnowledgeDiagnostic {
        return AthenaComponentKnowledgeDiagnostic(
            severity = AthenaComponentKnowledgeSeverity.ERROR,
            ruleId = AthenaComponentKnowledgeRuleId("component.definition.unresolved"),
            subject = authoredComponentReference,
            message = "No active governed engineering concept or vendor implementation matched `$authoredComponentReference`.",
        )
    }
}

private fun artifactSummary(artifacts: List<Pair<String, String>>): String {
    return artifacts
        .map { (artifactId, artifactVersion) -> "$artifactId@$artifactVersion" }
        .distinct()
        .sorted()
        .joinToString(", ")
}
