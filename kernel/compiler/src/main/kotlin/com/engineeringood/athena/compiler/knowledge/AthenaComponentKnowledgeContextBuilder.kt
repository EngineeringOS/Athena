package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.part.ResolvedPartImplementation
import com.engineeringood.athena.plugin.AthenaComponentKnowledgeContribution

/** One plugin-owned component-knowledge contribution source admitted into one compiler run. */
data class AthenaComponentKnowledgeContributionSource(
    val artifactId: String,
    val artifactVersion: String,
    val contribution: AthenaComponentKnowledgeContribution,
)

/** Deterministic compiler-owned snapshot of resolved M14 component knowledge for one document. */
data class AthenaResolvedComponentKnowledgeSnapshot(
    val contributingArtifactIds: List<String>,
    val activeConceptCount: Int,
    val activeImplementationCount: Int,
    val resolvedComponents: List<com.engineeringood.athena.component.ResolvedComponentDefinition>,
    val resolvedImplementations: List<ResolvedPartImplementation>,
    val resolvedSemanticPorts: List<com.engineeringood.athena.connection.ResolvedSemanticPortDefinition>,
    val resolvedPhysicalTraits: List<com.engineeringood.athena.physical.ResolvedPhysicalTraitDefinition>,
    val diagnostics: List<AthenaComponentKnowledgeDiagnostic>,
)

/**
 * Builds compiler-owned resolved component knowledge from plugin-published M14 contributions.
 *
 * The builder stays read-only. It enriches the compilation knowledge boundary without creating a
 * second mutation authority.
 */
class AthenaComponentKnowledgeContextBuilder(
    private val resolver: AthenaComponentKnowledgeResolver = AthenaComponentKnowledgeResolver(),
) {
    /** Resolves M14 component knowledge for [document] from deterministic plugin-owned [contributions]. */
    fun build(
        document: EngineeringDocument,
        contributions: List<AthenaComponentKnowledgeContributionSource>,
    ): AthenaResolvedComponentKnowledgeSnapshot {
        val catalog = AthenaComponentKnowledgeCatalog.canonical(
            concepts = contributions.flatMap { contribution ->
                contribution.contribution.engineeringConcepts.map { concept ->
                    AthenaConceptDefinitionContribution(
                        artifactId = contribution.artifactId,
                        artifactVersion = contribution.artifactVersion,
                        concept = concept,
                    )
                }
            },
            implementations = contributions.flatMap { contribution ->
                contribution.contribution.partImplementations.map { implementation ->
                    AthenaPartImplementationContribution(
                        artifactId = contribution.artifactId,
                        artifactVersion = contribution.artifactVersion,
                        implementation = implementation,
                    )
                }
            },
        )
        val resolutionResults = document.components
            .sortedBy { component -> component.id.value }
            .map { component ->
                component to resolver.resolve(
                    semanticSubjectId = component.id,
                    authoredComponentReference = component.authoredComponentReference(),
                    catalog = catalog,
                )
            }
        val activeSemanticIds = buildSet {
            add(document.system.id.value)
            document.components.forEach { component -> add(component.id.value) }
            document.ports.forEach { port -> add(port.id.value) }
            document.connections.forEach { connection -> add(connection.id.value) }
        }

        return AthenaResolvedComponentKnowledgeSnapshot(
            contributingArtifactIds = contributions.map { contribution -> contribution.artifactId }.distinct().sorted(),
            activeConceptCount = catalog.concepts.map { contribution -> contribution.concept }.distinct().size,
            activeImplementationCount = catalog.implementations.map { contribution -> contribution.implementation }.distinct().size,
            resolvedComponents = resolutionResults.mapNotNull { (_, result) -> result.resolvedComponent },
            resolvedImplementations = resolutionResults.mapNotNull { (_, result) -> result.resolvedImplementation },
            resolvedSemanticPorts = contributions
                .flatMap { contribution -> contribution.contribution.semanticPorts }
                .filter { resolvedPort ->
                    resolvedPort.portSemanticId.value in activeSemanticIds &&
                        resolvedPort.ownerSemanticId.value in activeSemanticIds
                }
                .distinct()
                .sortedWith(compareBy(
                    { resolvedPort -> resolvedPort.ownerSemanticId.value },
                    { resolvedPort -> resolvedPort.portSemanticId.value },
                    { resolvedPort -> resolvedPort.definition.roleId.value },
                )),
            resolvedPhysicalTraits = contributions
                .flatMap { contribution -> contribution.contribution.physicalTraits }
                .filter { physicalTrait -> physicalTrait.semanticSubjectId.value in activeSemanticIds }
                .distinct()
                .sortedBy { physicalTrait -> physicalTrait.semanticSubjectId.value },
            diagnostics = resolutionResults
                .flatMap { (_, result) -> result.diagnostics }
                .sortedWith(compareBy(
                    AthenaComponentKnowledgeDiagnostic::subject,
                    { diagnostic -> diagnostic.ruleId.value },
                    AthenaComponentKnowledgeDiagnostic::message,
                )),
        )
    }
}

private fun EngineeringComponent.authoredComponentReference(): String {
    return COMPONENT_REFERENCE_PROPERTY_NAMES.firstNotNullOfOrNull { propertyName ->
        properties.firstOrNull { property -> property.name == propertyName }?.value?.asAuthoredText()
    }?.takeIf { reference -> reference.isNotBlank() } ?: kind
}

private fun <T, R : Any> Iterable<T>.firstNotNullOfOrNull(transform: (T) -> R?): R? {
    for (element in this) {
        transform(element)?.let { return it }
    }
    return null
}

private fun EngineeringPropertyValue.asAuthoredText(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> text
    }
}

private val COMPONENT_REFERENCE_PROPERTY_NAMES = listOf(
    "componentRef",
    "component-ref",
    "vendorPartNumber",
    "vendorPart",
    "partNumber",
    "model",
    "part",
    "type",
)
