package com.engineeringood.athena.domain.dummyruntime

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.ConnectionGroupDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaDomainConnectionSchema
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

/** Synthetic hosted proof plugin used to demonstrate that the SPI is not electrical-specific. */
class DummyRuntimeDomainPlugin : AthenaDomainPlugin, AthenaRuntimePluginViewContributor {
    /** Stable manifest for the synthetic proof domain. */
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

    /** Narrow synthetic capability declaration for the second hosted proof domain. */
    override val domainCapabilities: Set<String> = setOf(DUMMY_DOMAIN_ID)

    /** Synthetic domain schema published through the stable plugin API. */
    override val domainSchema: AthenaDomainSchema = DUMMY_DOMAIN_SCHEMA

    /** Inspectable validation contribution declarations exposed by the dummy proof plugin. */
    override val validationContributions: List<AthenaValidationContribution> = DUMMY_VALIDATION_CONTRIBUTIONS

    /** Inspectable compiler-stage contribution declarations exposed by the dummy proof plugin. */
    override val compilerPassContributions: List<AthenaCompilerPassContribution> = DUMMY_COMPILER_PASS_CONTRIBUTIONS

    /** Inspectable renderer-facing contribution declarations exposed by the dummy proof plugin. */
    override val renderContributions: List<AthenaRenderContribution> = DUMMY_RENDER_CONTRIBUTIONS

    /** Lowers only explicitly dummy-owned authored declarations into canonical plugin blueprints. */
    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        // Exhaustive partition over Declaration so future sealed variants fail at compile time.
        val deviceDeclarations = mutableListOf<DeviceDeclaration>()
        val portDeclarations = mutableListOf<PortDeclaration>()
        val connectionDeclarations = mutableListOf<ConnectionDeclaration>()
        for (declaration in context.source.ast.declarations) {
            when (declaration) {
                is DeviceDeclaration -> deviceDeclarations += declaration
                is PortDeclaration -> portDeclarations += declaration
                is ConnectionDeclaration -> connectionDeclarations += declaration
                is ConnectionGroupDeclaration -> connectionDeclarations += declaration.connections
                is LayoutDeclaration -> Unit
            }
        }

        val ownedDeviceNames = deviceDeclarations
            .filter { declaration -> declaration.domainMarker() == DUMMY_DOMAIN_ID }
            .map { declaration -> declaration.name }
            .toSet()

        val components = deviceDeclarations
            .filter { declaration -> declaration.name in ownedDeviceNames }
            .map { declaration ->
                context.component(
                    name = declaration.name,
                    kind = "device",
                    properties = context.lowerProperties(declaration.fields),
                    provenance = context.provenance(declaration.span),
                )
            }
        val ports = portDeclarations
            .filter { declaration -> declaration.qualifiedName.parts.firstOrNull() in ownedDeviceNames }
            .map { declaration ->
                context.port(
                    ownerPath = declaration.qualifiedName.parts.dropLast(1),
                    ownerProvenance = context.provenance(declaration.qualifiedName.span),
                    name = declaration.qualifiedName.parts.last(),
                    properties = context.lowerProperties(declaration.fields),
                    provenance = context.provenance(declaration.span),
                )
            }
        val connections = connectionDeclarations
            .filter { declaration ->
                declaration.from.parts.firstOrNull() in ownedDeviceNames &&
                    declaration.to.parts.firstOrNull() in ownedDeviceNames
            }
            .map { declaration ->
                context.connection(
                    fromPath = declaration.from.parts,
                    fromProvenance = context.provenance(declaration.from.span),
                    toPath = declaration.to.parts,
                    toProvenance = context.provenance(declaration.to.span),
                    provenance = context.provenance(declaration.span),
                )
            }

        return AthenaDomainLoweringContribution(
            components = components,
            ports = ports,
            connections = connections,
        )
    }

    /** Validates only dummy-owned canonical semantics without claiming foreign domains. */
    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        val ownedComponents = context.document.components.filter { component -> component.isDummyOwned() }
        val ownedComponentIds = ownedComponents.map { component -> component.id }.toSet()
        val ownedPorts = context.document.ports.filter { port -> port.ownerReference.resolvedIdentity in ownedComponentIds }
        val ownedPortsById = ownedPorts.associateBy { port -> port.id }
        val ownedConnections = context.document.connections.filter { connection ->
            connection.from.resolvedIdentity in ownedPortsById &&
                connection.to.resolvedIdentity in ownedPortsById
        }

        val diagnostics = buildList {
            addAll(componentTypeDiagnostics(ownedComponents, context))
            addAll(portFlowDiagnostics(ownedPorts, context))
            addAll(portTintDiagnostics(ownedPorts, context))
            addAll(connectionCompatibilityDiagnostics(ownedConnections, ownedPortsById, context))
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
        if (summary.componentCount == 0 && summary.portCount == 0) {
            return emptyList()
        }

        return listOf(
            AthenaRuntimePluginViewContribution(
                inspectorGroups = listOf(
                    AthenaRuntimePluginInspectorGroup(
                        title = "Dummy runtime",
                        fields = listOf(
                            AthenaRuntimePluginInspectorField("Domain", DUMMY_DOMAIN_ID),
                            AthenaRuntimePluginInspectorField("Components", summary.componentCount.toString()),
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

    private fun componentTypeDiagnostics(
        components: List<com.engineeringood.athena.ir.EngineeringComponent>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return components.mapNotNull { component ->
            when (val type = component.properties.requiredSymbolValue("type")) {
                PropertySymbolValue.Missing -> context.domainDiagnostic(
                    ruleId = "property.component.type.missing",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = component.id,
                    provenance = component.provenance,
                    message = "Dummy component `${component.name}` is missing required `type`.",
                )

                is PropertySymbolValue.Invalid -> context.domainDiagnostic(
                    ruleId = "property.component.type.invalid",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = component.id,
                    provenance = component.provenance,
                    message = "Dummy component `${component.name}` declares `type` with an invalid non-symbol value `${type.value}`.",
                )

                is PropertySymbolValue.Duplicate -> context.domainDiagnostic(
                    ruleId = "property.component.type.duplicate",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = component.id,
                    provenance = component.provenance,
                    message = "Dummy component `${component.name}` declares duplicate `type` properties `${type.values}`.",
                )

                is PropertySymbolValue.SymbolText -> if (type.value !in VALID_DUMMY_TYPES) {
                    context.domainDiagnostic(
                        ruleId = "property.component.type.invalid",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = component.id,
                        provenance = component.provenance,
                        message = "Dummy component `${component.name}` declares unsupported synthetic type `${type.value}`.",
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun portFlowDiagnostics(
        ports: List<EngineeringPort>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return ports.mapNotNull { port ->
            when (val flow = port.properties.requiredSymbolValue("flow")) {
                PropertySymbolValue.Missing -> context.domainDiagnostic(
                    ruleId = "property.port.flow.missing",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Dummy port `${authoredPortPath(port)}` is missing required `flow`.",
                )

                is PropertySymbolValue.Invalid -> context.domainDiagnostic(
                    ruleId = "property.port.flow.invalid",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Dummy port `${authoredPortPath(port)}` declares `flow` with an invalid non-symbol value `${flow.value}`.",
                )

                is PropertySymbolValue.Duplicate -> context.domainDiagnostic(
                    ruleId = "property.port.flow.duplicate",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Dummy port `${authoredPortPath(port)}` declares duplicate `flow` properties `${flow.values}`.",
                )

                is PropertySymbolValue.SymbolText -> if (flow.value !in VALID_FLOWS) {
                    context.domainDiagnostic(
                        ruleId = "property.port.flow.invalid",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Dummy port `${authoredPortPath(port)}` declares unsupported flow `${flow.value}`.",
                    )
                } else {
                    null
                }
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

    private fun connectionCompatibilityDiagnostics(
        connections: List<EngineeringConnection>,
        portsById: Map<StableSemanticIdentity, EngineeringPort>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return buildList {
            connections.forEach { connection ->
                val fromPort = connection.from.resolvedIdentity?.let(portsById::get) ?: return@forEach
                val toPort = connection.to.resolvedIdentity?.let(portsById::get) ?: return@forEach

                val fromFlow = fromPort.flow()
                val toFlow = toPort.flow()
                if (fromFlow != null && toFlow != null && (fromFlow != DummyFlow.EMIT || toFlow != DummyFlow.ABSORB)) {
                    add(
                        context.domainDiagnostic(
                            ruleId = "connection.flow.illegal",
                            category = SemanticDiagnosticCategory.CONNECTION,
                            subjectIdentity = connection.id,
                            provenance = connection.provenance,
                            message = "Dummy connection `${authoredPath(connection.from)} -> ${authoredPath(connection.to)}` must flow from `emit` to `absorb`.",
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
                            ruleId = "connection.tint.incompatible",
                            category = SemanticDiagnosticCategory.CONNECTION,
                            subjectIdentity = connection.id,
                            provenance = connection.provenance,
                            message = "Dummy connection `${authoredPath(connection.from)} -> ${authoredPath(connection.to)}` mixes incompatible tints `${fromTint.value}` and `${toTint.value}`.",
                        ),
                    )
                }
            }
        }
    }

    private fun EngineeringPort.flow(): DummyFlow? {
        return when (val flow = properties.optionalSymbolValue("flow")) {
            PropertySymbolValue.Missing -> null
            is PropertySymbolValue.SymbolText -> VALID_FLOWS[flow.value]
            is PropertySymbolValue.Invalid,
            is PropertySymbolValue.Duplicate,
                -> null
        }
    }

    private fun com.engineeringood.athena.ir.EngineeringComponent.isDummyOwned(): Boolean {
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
            is EngineeringPropertyValue.Symbol -> value.text
            is EngineeringPropertyValue.Text -> value.text
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
            is EngineeringPropertyValue.Symbol -> PropertySymbolValue.SymbolText(value.text)
            is EngineeringPropertyValue.Text -> PropertySymbolValue.Invalid(value.text)
        }
    }

    private fun authoredPortPath(port: EngineeringPort): String = authoredPath(port.ownerReference.authoredPath + port.name)

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
    description = "Synthetic hosted proof-domain schema used to show the Athena SPI is not electrical-specific.",
    capabilities = setOf(DUMMY_DOMAIN_ID),
    entities = listOf(
        AthenaDomainEntitySchema(
            typeId = "Glyph",
            displayName = "Glyph",
            subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
            description = "Synthetic proof component with no engineering meaning.",
            propertyNames = setOf("domain", "type"),
            portTypeIds = setOf("dummy-port"),
        ),
        AthenaDomainEntitySchema(
            typeId = "Pulse",
            displayName = "Pulse",
            subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
            description = "Synthetic proof component with no engineering meaning.",
            propertyNames = setOf("domain", "type"),
            portTypeIds = setOf("dummy-port"),
        ),
        AthenaDomainEntitySchema(
            typeId = "Totem",
            displayName = "Totem",
            subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
            description = "Synthetic proof component with no engineering meaning.",
            propertyNames = setOf("domain", "type"),
            portTypeIds = setOf("dummy-port"),
        ),
    ),
    properties = listOf(
        AthenaDomainPropertySchema(
            name = "domain",
            displayName = "Domain marker",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.COMPONENT),
            required = true,
            allowedSymbolValues = setOf(DUMMY_DOMAIN_ID),
            description = "Explicit synthetic ownership marker used to keep dummy semantics scoped away from other proof domains.",
        ),
        AthenaDomainPropertySchema(
            name = "type",
            displayName = "Synthetic type",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.COMPONENT),
            required = true,
            allowedSymbolValues = VALID_DUMMY_TYPES,
            description = "Synthetic component type selector interpreted only by the dummy proof plugin.",
        ),
        AthenaDomainPropertySchema(
            name = "flow",
            displayName = "Port flow",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = true,
            allowedSymbolValues = setOf("emit", "absorb"),
            description = "Synthetic flow metadata used by the dummy proof plugin.",
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
            description = "Synthetic hosted port for the second proof domain.",
            propertyNames = setOf("flow", "tint"),
            allowedDirections = setOf("emit", "absorb"),
        ),
    ),
    connections = listOf(
        AthenaDomainConnectionSchema(
            typeId = "DummyLink",
            displayName = "Dummy link",
            description = "Synthetic hosted connection with no engineering meaning.",
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
        is ScalarValue.Identifier -> value.text
        is ScalarValue.StringLiteral -> value.text
    }
}

private fun DeviceDeclaration.domainMarker(): String? {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText()
}

private fun EngineeringPropertyValue.renderedValue(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> "\"$text\""
    }
}

/** Runtime-owned summary derived from the active dummy graph projection. */
private data class DummyRuntimeSummary(
    val componentCount: Int,
    val portCount: Int,
    val tintCount: Int,
    val compatiblePairCount: Int,
)

/** Derives the dummy runtime summary used by the synthetic plugin view contribution. */
private fun AthenaExecutionContext.dummyRuntimeSummary(): DummyRuntimeSummary {
    val graphProjection = projectEngineeringGraphProjection() as? AthenaEngineeringGraphReadyProjection
        ?: return DummyRuntimeSummary(
            componentCount = 0,
            portCount = 0,
            tintCount = 0,
            compatiblePairCount = 0,
        )
    val componentIds = graphProjection.dummyComponentSemanticIds()
    val portCandidates = graphProjection.dummyPortCandidates(componentIds)
    return DummyRuntimeSummary(
        componentCount = componentIds.size,
        portCount = portCandidates.size,
        tintCount = portCandidates.mapNotNull { candidate -> candidate.tint }.distinct().size,
        compatiblePairCount = compatibleDummyPairs(portCandidates).size,
    )
}

/** Collects dummy-owned component ids from the runtime-owned graph projection. */
private fun AthenaEngineeringGraphReadyProjection.dummyComponentSemanticIds(): Set<String> {
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.COMPONENT)
        .filter { node ->
            node.properties.firstOrNull { property -> property.name == "domain" }?.value == DUMMY_DOMAIN_ID
        }
        .map { node -> node.semanticId }
        .toSet()
}

/** Collects dummy-owned port candidates from the runtime-owned graph projection. */
private fun AthenaEngineeringGraphReadyProjection.dummyPortCandidates(
    ownedComponentIds: Set<String>,
): List<DummyPortCandidate> {
    val graph = graph
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.PORT).mapNotNull { portNode ->
        val ownerSemanticId = portNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.OWNER }
            ?.resolvedSemanticId
            ?: return@mapNotNull null
        if (ownerSemanticId !in ownedComponentIds) {
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
