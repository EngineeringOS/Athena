package com.engineeringood.athena.domain.dummyruntime

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringEntity
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.ExternalEvidenceDeclaration
import com.engineeringood.athena.language.GridDeclaration
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.ProjectionConstructDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.RegionDeclaration
import com.engineeringood.athena.language.RelationDeclaration
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SheetDeclaration
import com.engineeringood.athena.language.ViewDeclaration
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaDomainRelationshipSchema
import com.engineeringood.athena.plugin.AthenaDomainEntitySchema
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaDomainPortSchema
import com.engineeringood.athena.plugin.AthenaDomainPropertySchema
import com.engineeringood.athena.plugin.AthenaDomainPropertyValueKind
import com.engineeringood.athena.plugin.AthenaDomainSchema
import com.engineeringood.athena.plugin.AthenaDomainSchemaSubjectKind
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.plugin.AthenaRenderContribution
import com.engineeringood.athena.plugin.AthenaValidationContribution
import com.engineeringood.athena.plugin.CoreVersionRange
import com.engineeringood.athena.runtime.AthenaEngineeringGraphNodeKind
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReadyProjection
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReferenceKind
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntimePluginInspectorField
import com.engineeringood.athena.runtime.AthenaRuntimePluginInspectorGroup
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContributor
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory

/** Synthetic hosted evidence plugin used to demonstrate that the SPI is not electrical-specific. */
class DummyRuntimeDomainPlugin : AthenaDomainPlugin, AthenaRuntimePluginViewContributor {
    /** Stable manifest for the synthetic evidence domain. */
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.dummy-runtime",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.RUNTIME_VIEWS,
        ),
    )

    /** Narrow synthetic capability declaration for the second hosted evidence domain. */
    override val domainCapabilities: Set<String> = setOf(DUMMY_DOMAIN_ID)

    /** Synthetic domain schema published through the stable plugin API. */
    override val domainSchema: AthenaDomainSchema = DUMMY_DOMAIN_SCHEMA

    /** Inspectable validation contribution declarations exposed by the dummy evidence plugin. */
    override val validationContributions: List<AthenaValidationContribution> = DUMMY_VALIDATION_CONTRIBUTIONS

    /** Inspectable compiler-stage contribution declarations exposed by the dummy evidence plugin. */
    override val compilerPassContributions: List<AthenaCompilerPassContribution> = DUMMY_COMPILER_PASS_CONTRIBUTIONS

    /** Inspectable renderer-facing contribution declarations exposed by the dummy evidence plugin. */
    override val renderContributions: List<AthenaRenderContribution> = DUMMY_RENDER_CONTRIBUTIONS

    /** Lowers only explicitly dummy-owned authored declarations into canonical plugin blueprints. */
    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        // Exhaustive partition over Declaration so future sealed variants fail at compile time.
        val entityDeclarations = mutableListOf<EntityDeclaration>()
        val portDeclarations = mutableListOf<PortDeclaration>()
        for (declaration in context.source.ast.declarations) {
            when (declaration) {
                is EntityDeclaration -> {
                    entityDeclarations += declaration
                    portDeclarations += declaration.nestedPorts
                    portDeclarations += declaration.nestedFunctions.flatMap { function -> function.nestedPorts }
                }
                is PortDeclaration -> portDeclarations += declaration
                is RelationDeclaration -> Unit
		                is ExternalEvidenceDeclaration -> Unit
	                is com.engineeringood.athena.language.ProjectionPolicyDeclaration -> Unit
                    is LayoutDeclaration -> Unit
                    is InstallationDeclaration -> Unit
                    is ViewDeclaration -> Unit
                    is SheetDeclaration -> Unit
                    is GridDeclaration -> Unit
                    is RegionDeclaration -> Unit
                    is ProjectionConstructDeclaration -> Unit
                }
        }

        val ownedEntityNames = entityDeclarations
            .filter { declaration -> declaration.domainMarker() == DUMMY_DOMAIN_ID }
            .map { declaration -> declaration.name }
            .toSet()

        val entities = entityDeclarations
            .filter { declaration -> declaration.name in ownedEntityNames }
            .mapNotNull(context::entityOrNull)
        val lowerableEntityNames = entities.map { entity -> entity.name }.toSet()
        val ports = portDeclarations
            .filter { declaration -> declaration.qualifiedName.parts.firstOrNull() in lowerableEntityNames }
            .mapNotNull(context::portOrNull)
        val functions = entityDeclarations
            .filter { declaration -> declaration.name in ownedEntityNames }
            .flatMap { entity ->
                entity.nestedFunctions.map { function ->
                    context.function(entity, function)
                }
            }

        return AthenaDomainLoweringContribution(
            entities = entities,
            ports = ports,
            functions = functions,
        )
    }

    /** Validates only dummy-owned canonical semantics without claiming foreign domains. */
    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        val ownedEntities = context.document.entities.filter { entity -> entity.isDummyOwned() }
        val ownedEntityIds = ownedEntities.map { entity -> entity.id }.toSet()
        val ownedPorts = context.document.ports.filter { port -> port.owner.reference.resolvedIdentity in ownedEntityIds }
        val ownedPortsById = ownedPorts.associateBy { port -> port.id }
        val ownedRelationships = context.document.relationships.filter { relationship ->
            relationship.participants.size == 2 && relationship.participants.all { participant ->
                val subject = participant.subject
                subject is com.engineeringood.athena.ir.EngineeringSubjectReference.Port &&
                    subject.port.resolvedIdentity in ownedPortsById
            }
        }

        val diagnostics = buildList {
            addAll(entityConceptDiagnostics(ownedEntities, context))
            addAll(portFlowDiagnostics(ownedPorts, context))
            addAll(portTintDiagnostics(ownedPorts, context))
            addAll(relationshipCompatibilityDiagnostics(ownedRelationships, ownedPortsById, context))
        }

        return AthenaPluginValidationResult(
            contributions = listOf(
                context.emitValidationContribution(
                    contributionId = DUMMY_VALIDATION_CONTRIBUTION_ID,
                    diagnostics = diagnostics,
                ),
            ),
        )
    }

    /** Contributes runtime-owned dummy inspection only when the active graph contains dummy-owned semantics. */
    override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
        val summary = context.dummyRuntimeSummary()
        if (summary.entityCount == 0 && summary.portCount == 0) {
            return emptyList()
        }

        return listOf(
            AthenaRuntimePluginViewContribution(
                inspectorGroups = listOf(
                    AthenaRuntimePluginInspectorGroup(
                        title = "Dummy runtime",
                        fields = listOf(
                            AthenaRuntimePluginInspectorField("Domain", DUMMY_DOMAIN_ID),
                            AthenaRuntimePluginInspectorField("Entities", summary.entityCount.toString()),
                            AthenaRuntimePluginInspectorField("Ports", summary.portCount.toString()),
                            AthenaRuntimePluginInspectorField("Tints", summary.tintCount.toString()),
                            AthenaRuntimePluginInspectorField("Compatible pairs", summary.compatiblePairCount.toString()),
                        ),
                    ),
                ),
                diagnosticsEntries = listOf(
                    "Dummy runtime plugin active: ${summary.compatiblePairCount} compatible synthetic pair(s) available.",
                ),
            ),
        )
    }

    private fun entityConceptDiagnostics(
        entities: List<EngineeringEntity>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return entities.mapNotNull { entity ->
            val concept = entity.conceptReference.authoredName.last()
            if (concept in VALID_DUMMY_TYPES) {
                null
            } else {
                context.domainDiagnostic(
                    ruleId = "entity.concept.unsupported",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = entity.id,
                    provenance = entity.provenance,
                    message = "Dummy Entity `${entity.name}` declares unsupported synthetic Concept `$concept`.",
                )
            }
        }
    }

    private fun portFlowDiagnostics(
        ports: List<EngineeringPort>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return ports.mapNotNull { port ->
            val flows = port.admittedFlowReferences.map { reference -> reference.authoredName.last() }
            when {
                flows.isEmpty() -> context.domainDiagnostic(
                    ruleId = "property.port.flow.missing",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Dummy port `${authoredPortPath(port)}` is missing required `flow`.",
                )

                flows.size > 1 -> context.domainDiagnostic(
                    ruleId = "property.port.flow.duplicate",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Dummy Port `${authoredPortPath(port)}` admits multiple Flows `${flows.joinToString()}`.",
                )

                flows.single() !in VALID_FLOWS -> {
                    context.domainDiagnostic(
                        ruleId = "property.port.flow.invalid",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Dummy Port `${authoredPortPath(port)}` admits unsupported Flow `${flows.single()}`.",
                    )
                }
                else -> null
            }
        }
    }

    private fun portTintDiagnostics(
        ports: List<EngineeringPort>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return ports.flatMap { port ->
            when (val tint = port.properties.optionalSymbolValue("tint")) {
                PropertySymbolValue.Missing,
                is PropertySymbolValue.SymbolText,
                    -> emptyList()

                is PropertySymbolValue.Invalid -> listOf(
                    context.domainDiagnostic(
                        ruleId = "property.port.tint.invalid",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Dummy port `${authoredPortPath(port)}` declares `tint` with an invalid non-symbol value `${tint.value}`.",
                    ),
                )

                is PropertySymbolValue.Duplicate -> listOf(
                    context.domainDiagnostic(
                        ruleId = "property.port.tint.duplicate",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Dummy port `${authoredPortPath(port)}` declares duplicate `tint` properties `${tint.values}`.",
                    ),
                )
            }
        }
    }

    private fun relationshipCompatibilityDiagnostics(
        relationships: List<com.engineeringood.athena.ir.EngineeringRelationship>,
        portsById: Map<StableSemanticIdentity, EngineeringPort>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return buildList {
            relationships.forEach { relationship ->
                val participants = relationship.participants.mapNotNull { participant ->
                    (participant.subject as? com.engineeringood.athena.ir.EngineeringSubjectReference.Port)
                        ?.port?.resolvedIdentity?.let(portsById::get)
                }
                if (participants.size != 2) return@forEach
                val fromPort = participants[0]
                val toPort = participants[1]

                if (fromPort.direction != EngineeringPortDirection.OUTPUT || toPort.direction != EngineeringPortDirection.INPUT) {
                    add(
                        context.domainDiagnostic(
                            ruleId = "relationship.flow.illegal",
                            category = SemanticDiagnosticCategory.RELATIONSHIP,
                            subjectIdentity = relationship.id,
                            provenance = relationship.provenance,
                            message = "Dummy relationship `${relationship.participants.joinToString(" -> ") { participant -> authoredPath(participant.subject.reference) }}` must flow from `emit` to `absorb`.",
                        ),
                    )
                }

                val fromTint = fromPort.properties.optionalSymbolValue("tint")
                val toTint = toPort.properties.optionalSymbolValue("tint")
                if (fromTint is PropertySymbolValue.SymbolText &&
                    toTint is PropertySymbolValue.SymbolText &&
                    fromTint.value != toTint.value
                ) {
                    add(
                        context.domainDiagnostic(
                            ruleId = "relationship.tint.incompatible",
                            category = SemanticDiagnosticCategory.RELATIONSHIP,
                            subjectIdentity = relationship.id,
                            provenance = relationship.provenance,
                            message = "Dummy relationship `${relationship.participants.joinToString(" -> ") { participant -> authoredPath(participant.subject.reference) }}` mixes incompatible tints `${fromTint.value}` and `${toTint.value}`.",
                        ),
                    )
                }
            }
        }
    }

    private fun EngineeringEntity.isDummyOwned(): Boolean {
        return properties.domainMarkerValue() == DUMMY_DOMAIN_ID
    }

    private fun List<EngineeringProperty>.requiredSymbolValue(name: String): PropertySymbolValue = propertySymbolValue(name)

    private fun List<EngineeringProperty>.optionalSymbolValue(name: String): PropertySymbolValue = propertySymbolValue(name)

    private fun List<EngineeringProperty>.domainMarkerValue(): String? {
        val matchingProperties = filter { property -> property.name == "domain" }
        if (matchingProperties.size != 1) {
            return null
        }
        return when (val value = matchingProperties.single().value) {
            is EngineeringValue.Symbol -> value.text
            is EngineeringValue.Text -> value.text
            else -> null
        }
    }

    private fun List<EngineeringProperty>.propertySymbolValue(name: String): PropertySymbolValue {
        val matchingProperties = filter { property -> property.name == name }
        if (matchingProperties.isEmpty()) {
            return PropertySymbolValue.Missing
        }
        if (matchingProperties.size > 1) {
            return PropertySymbolValue.Duplicate(matchingProperties.map { property -> property.value.renderedValue() })
        }

        return when (val value = matchingProperties.single().value) {
            is EngineeringValue.Symbol -> PropertySymbolValue.SymbolText(value.text)
            else -> PropertySymbolValue.Invalid(value.renderedValue())
        }
    }

    private fun authoredPortPath(port: EngineeringPort): String = authoredPath(port.owner.reference.authoredPath + port.name)

    private fun authoredPath(reference: EngineeringReference): String = authoredPath(reference.authoredPath)

    private fun authoredPath(parts: List<String>): String = parts.joinToString(".")
}

private const val DUMMY_DOMAIN_ID = "dummy-runtime"

private val VALID_DUMMY_TYPES = setOf("Glyph", "Pulse", "Totem")

private val VALID_FLOWS = mapOf(
    "emit" to DummyFlow.EMIT,
    "absorb" to DummyFlow.ABSORB,
)

private val DUMMY_DOMAIN_SCHEMA = AthenaDomainSchema(
    domainId = DUMMY_DOMAIN_ID,
    displayName = "Dummy Runtime",
    description = "Synthetic hosted evidence-domain schema used to show the Athena SPI is not electrical-specific.",
    capabilities = setOf(DUMMY_DOMAIN_ID),
    entities = listOf(
        AthenaDomainEntitySchema(
            typeId = "Glyph",
            displayName = "Glyph",
            subjectKind = AthenaDomainSchemaSubjectKind.ENTITY,
            description = "Synthetic evidence component with no engineering meaning.",
            propertyNames = setOf("domain", "type"),
            portTypeIds = setOf("dummy-port"),
        ),
        AthenaDomainEntitySchema(
            typeId = "Pulse",
            displayName = "Pulse",
            subjectKind = AthenaDomainSchemaSubjectKind.ENTITY,
            description = "Synthetic evidence component with no engineering meaning.",
            propertyNames = setOf("domain", "type"),
            portTypeIds = setOf("dummy-port"),
        ),
        AthenaDomainEntitySchema(
            typeId = "Totem",
            displayName = "Totem",
            subjectKind = AthenaDomainSchemaSubjectKind.ENTITY,
            description = "Synthetic evidence component with no engineering meaning.",
            propertyNames = setOf("domain", "type"),
            portTypeIds = setOf("dummy-port"),
        ),
    ),
    properties = listOf(
        AthenaDomainPropertySchema(
            name = "domain",
            displayName = "Domain marker",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.ENTITY),
            required = true,
            allowedSymbolValues = setOf(DUMMY_DOMAIN_ID),
            description = "Explicit synthetic ownership marker used to keep dummy semantics scoped away from other evidence domains.",
        ),
        AthenaDomainPropertySchema(
            name = "type",
            displayName = "Synthetic type",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.ENTITY),
            required = true,
            allowedSymbolValues = VALID_DUMMY_TYPES,
            description = "Synthetic component type selector interpreted only by the dummy evidence plugin.",
        ),
        AthenaDomainPropertySchema(
            name = "flow",
            displayName = "Port flow",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = true,
            allowedSymbolValues = setOf("emit", "absorb"),
            description = "Synthetic flow metadata used by the dummy evidence plugin.",
        ),
        AthenaDomainPropertySchema(
            name = "tint",
            displayName = "Port tint",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = false,
            description = "Optional synthetic tint metadata used to prove plugin-owned compatibility rules.",
        ),
    ),
    ports = listOf(
        AthenaDomainPortSchema(
            typeId = "dummy-port",
            displayName = "Dummy port",
            description = "Synthetic hosted port for the second evidence domain.",
            propertyNames = setOf("flow", "tint"),
            allowedDirections = setOf("emit", "absorb"),
        ),
    ),
    relationships = listOf(
        AthenaDomainRelationshipSchema(
            typeId = "DummyLink",
            displayName = "Dummy link",
            description = "Synthetic hosted relationship with no engineering meaning.",
            sourcePortTypeIds = setOf("dummy-port"),
            targetPortTypeIds = setOf("dummy-port"),
        ),
    ),
)

private val DUMMY_VALIDATION_CONTRIBUTIONS = listOf(
    AthenaValidationContribution(
        contributionId = DUMMY_VALIDATION_CONTRIBUTION_ID,
        displayName = "Dummy property and compatibility validation",
        description = "Validates synthetic component types, port flow metadata, and tint compatibility.",
    ),
)

private val DUMMY_COMPILER_PASS_CONTRIBUTIONS = listOf(
    AthenaCompilerPassContribution(
        contributionId = "dummy-runtime.lower.synthetic-declarations",
        stage = AthenaCompilerContributionStage.LOWER,
        displayName = "Dummy lowering",
        description = "Interprets explicitly dummy-owned authored declarations during the compiler-owned lowering stage.",
    ),
    AthenaCompilerPassContribution(
        contributionId = "dummy-runtime.validate.synthetic-rules",
        stage = AthenaCompilerContributionStage.VALIDATE,
        displayName = "Dummy validation",
        description = "Applies synthetic property and compatibility validation during the compiler-owned validate stage.",
    ),
)

private val DUMMY_RENDER_CONTRIBUTIONS = listOf(
    AthenaRenderContribution(
        contributionId = "dummy-runtime.render.synthetic-panel",
        displayName = "Dummy render intent",
        description = "Publishes synthetic renderer-facing intent without widening the default global view-definition set.",
        viewIds = setOf("dummy-panel"),
        rendererTargets = setOf("svg"),
    ),
)

private const val DUMMY_VALIDATION_CONTRIBUTION_ID = "dummy-runtime.validation.synthetic-rules"

private enum class DummyFlow {
    EMIT,
    ABSORB,
}

private sealed interface PropertySymbolValue {
    data object Missing : PropertySymbolValue

    data class SymbolText(val value: String) : PropertySymbolValue

    data class Invalid(val value: String) : PropertySymbolValue

    data class Duplicate(val values: List<String>) : PropertySymbolValue
}

private fun PropertyAssignment.scalarIdentifierText(): String? {
    return when (val value = value) {
        is ScalarValue.Symbol -> value.text
        is ScalarValue.Text -> value.text
        is ScalarValue.Boolean,
        is ScalarValue.Integer,
        is ScalarValue.Quantity,
        is ScalarValue.Reference -> null
    }
}

private fun EntityDeclaration.domainMarker(): String? {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText()
}

private fun EngineeringValue.renderedValue(): String {
    return when (this) {
        is EngineeringValue.Quantity -> "$value ${unit.authoredName.joinToString(".")}"
        is EngineeringValue.Integer -> value.toString()
        is EngineeringValue.Boolean -> value.toString()
        is EngineeringValue.Text -> "\"$text\""
        is EngineeringValue.Symbol -> text
        is EngineeringValue.Reference -> "@${reference.authoredPath.joinToString(".")}"
    }
}

/** Runtime-owned summary derived from the active dummy graph projection. */
private data class DummyRuntimeSummary(
    val entityCount: Int,
    val portCount: Int,
    val tintCount: Int,
    val compatiblePairCount: Int,
)

/** Derives the dummy runtime summary used by the synthetic plugin view contribution. */
private fun AthenaExecutionContext.dummyRuntimeSummary(): DummyRuntimeSummary {
    val graphProjection = projectEngineeringGraphProjection() as? AthenaEngineeringGraphReadyProjection
        ?: return DummyRuntimeSummary(
            entityCount = 0,
            portCount = 0,
            tintCount = 0,
            compatiblePairCount = 0,
        )
    val entityIds = graphProjection.dummyEntitySemanticIds()
    val portCandidates = graphProjection.dummyPortCandidates(entityIds)
    return DummyRuntimeSummary(
        entityCount = entityIds.size,
        portCount = portCandidates.size,
        tintCount = portCandidates.mapNotNull { candidate -> candidate.tint }.distinct().size,
        compatiblePairCount = compatibleDummyPairs(portCandidates).size,
    )
}

/** Collects dummy-owned component ids from the runtime-owned graph projection. */
private fun AthenaEngineeringGraphReadyProjection.dummyEntitySemanticIds(): Set<String> {
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.ENTITY)
        .filter { node ->
            node.properties.firstOrNull { property -> property.name == "domain" }?.value == DUMMY_DOMAIN_ID
        }
        .map { node -> node.semanticId }
        .toSet()
}

/** Collects dummy-owned port candidates from the runtime-owned graph projection. */
private fun AthenaEngineeringGraphReadyProjection.dummyPortCandidates(
    ownedEntityIds: Set<String>,
): List<DummyPortCandidate> {
    val graph = graph
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.PORT).mapNotNull { portNode ->
        val ownerSemanticId = portNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.OWNER }
            ?.resolvedSemanticId
            ?: return@mapNotNull null
        if (ownerSemanticId !in ownedEntityIds) {
            return@mapNotNull null
        }

        val ownerName = graph.node(ownerSemanticId)?.displayName ?: "Unknown"
        DummyPortCandidate(
            semanticId = portNode.semanticId,
            label = "$ownerName.${portNode.displayName}",
            flow = portNode.properties.firstOrNull { property -> property.name == "flow" }?.value,
            tint = portNode.properties.firstOrNull { property -> property.name == "tint" }?.value,
        )
    }
}

/** Returns all compatible dummy port pairs in deterministic label order. */
private fun compatibleDummyPairs(portCandidates: List<DummyPortCandidate>): List<DummyCompatiblePair> {
    val emitters = portCandidates.filter { candidate -> candidate.flow == "emit" && candidate.tint != null }
    val absorbers = portCandidates.filter { candidate -> candidate.flow == "absorb" && candidate.tint != null }
    return emitters.flatMap { emitter ->
        absorbers.mapNotNull { absorber ->
            if (emitter.tint == absorber.tint && emitter.semanticId != absorber.semanticId) {
                DummyCompatiblePair(
                    sourceSemanticId = emitter.semanticId,
                    targetSemanticId = absorber.semanticId,
                    sortKey = "${emitter.label}->${absorber.label}",
                )
            } else {
                null
            }
        }
    }.sortedBy { pair -> pair.sortKey }
}

/** Runtime-visible dummy port candidate used to derive synthetic runtime-view contributions. */
private data class DummyPortCandidate(
    val semanticId: String,
    val label: String,
    val flow: String?,
    val tint: String?,
)

/** Deterministic compatible dummy pair used only for synthetic runtime-view inspection. */
private data class DummyCompatiblePair(
    val sourceSemanticId: String,
    val targetSemanticId: String,
    val sortKey: String,
)
