package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.runtime.AthenaCommandRuntimeService
import com.engineeringood.athena.runtime.AthenaConnectPortsCommand
import com.engineeringood.athena.runtime.AthenaEngineeringGraphNodeKind
import com.engineeringood.athena.runtime.AthenaEngineeringGraphProjection
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReadyProjection
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReferenceKind
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandContributor
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandFactory
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandReady
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandRejected
import com.engineeringood.athena.runtime.AthenaRuntimePluginInspectorField
import com.engineeringood.athena.runtime.AthenaRuntimePluginInspectorGroup
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContributor
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.layout.ViewEmphasis
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.plugin.CoreVersionRange
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory

/** Real Electrical/Runtime domain plugin that contributes the first M0 lowering and validation semantics. */
class ElectricalRuntimeDomainPlugin : AthenaDomainPlugin, AthenaViewDefinitionContributor, AthenaRuntimePluginCommandContributor, AthenaRuntimePluginViewContributor {
    /** Core-owned manifest declaring the sample plugin's identity, type, compatibility, and extension point. */
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.electrical-runtime",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.VIEW_DEFINITIONS,
            AthenaExtensionPoint.RUNTIME_COMMANDS,
            AthenaExtensionPoint.RUNTIME_VIEWS,
        ),
    )

    /** Minimal capability declaration showing the plugin remains a domain extension rather than core authority. */
    override val domainCapabilities: Set<String> = setOf("electrical-runtime")

    /** Lowers the current M0 Electrical/Runtime declarations into compiler-owned semantic blueprints. */
    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        val components = context.source.ast.declarations.filterIsInstance<DeviceDeclaration>().map { declaration ->
            context.component(
                name = declaration.name,
                kind = "device",
                properties = context.lowerProperties(declaration.fields),
                provenance = context.provenance(declaration.span),
            )
        }
        val ports = context.source.ast.declarations.filterIsInstance<PortDeclaration>().map { declaration ->
            context.port(
                ownerPath = declaration.qualifiedName.parts.dropLast(1),
                ownerProvenance = context.provenance(declaration.qualifiedName.span),
                name = declaration.qualifiedName.parts.last(),
                properties = context.lowerProperties(declaration.fields),
                provenance = context.provenance(declaration.span),
            )
        }
        val connections = context.source.ast.declarations.filterIsInstance<ConnectionDeclaration>().map { declaration ->
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

    /** Validates Electrical/Runtime-specific properties and connection compatibility over canonical Engineering IR. */
    override fun validate(context: AthenaPluginValidationContext): List<SemanticDiagnostic> {
        val document = context.document
        val portsById = document.ports.associateBy { it.id }
        return buildList {
            addAll(componentTypeDiagnostics(document, context))
            addAll(portDirectionDiagnostics(document, context))
            addAll(portSignalDiagnostics(document, context))
            addAll(connectionCompatibilityDiagnostics(document.connections, portsById, context))
        }
    }

    /** Contributes the first runtime-hosted electrical command proof without bypassing the command runtime. */
    override fun commandContributions(): List<AthenaRuntimePluginCommandContribution> {
        return listOf(
            AthenaRuntimePluginCommandContribution(
                contributionId = "electrical-runtime.connect-first-compatible",
                displayName = "Connect first compatible electrical ports",
                description = "Finds the first unconnected out->in electrical pair with the same signal and routes it through the command runtime.",
                factory = AthenaRuntimePluginCommandFactory { context ->
                    val pair = context.firstCompatibleElectricalPair()
                        ?: return@AthenaRuntimePluginCommandFactory AthenaRuntimePluginCommandRejected(
                            "No compatible electrical port pair is available for plugin command execution.",
                        )
                    AthenaRuntimePluginCommandReady(
                        command = AthenaConnectPortsCommand(
                            sourcePortSemanticId = pair.sourceSemanticId,
                            targetPortSemanticId = pair.targetSemanticId,
                        ),
                    )
                },
            ),
        )
    }

    /** Contributes the first supported multi-view proof pair for M2 without turning views into semantic truth. */
    override fun viewDefinitions(): List<ViewDefinition> {
        return listOf(
            ViewDefinition(
                id = "cabinet",
                displayName = "Cabinet",
                layoutIntent = LayoutIntent.STRUCTURAL,
                groupingRules = listOf("group-by-owner", "group-by-component"),
                viewEmphasis = listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT),
                description = "Highlights structural placement and ownership relationships for electrical devices.",
            ),
            ViewDefinition(
                id = "wiring",
                displayName = "Wiring",
                layoutIntent = LayoutIntent.CONNECTIVITY,
                groupingRules = listOf("group-by-signal", "group-by-connection-path"),
                viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
                description = "Highlights compatible signal flow and connection relationships between ports.",
            ),
        )
    }

    /** Contributes the first runtime-hosted electrical view proof through existing shell seams. */
    override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
        val summary = context.electricalRuntimeSummary()
        return listOf(
            AthenaRuntimePluginViewContribution(
                inspectorGroups = listOf(
                    AthenaRuntimePluginInspectorGroup(
                        title = "Electrical runtime",
                        fields = listOf(
                            AthenaRuntimePluginInspectorField("Domain", "electrical-runtime"),
                            AthenaRuntimePluginInspectorField("Signals", summary.signalCount.toString()),
                            AthenaRuntimePluginInspectorField("Compatible pairs", summary.compatiblePairCount.toString()),
                            AthenaRuntimePluginInspectorField("Connected pairs", summary.connectedPairCount.toString()),
                        ),
                    ),
                ),
                diagnosticsEntries = listOf(
                    "Electrical runtime plugin active: ${summary.compatiblePairCount} compatible pair(s) available.",
                ),
            ),
        )
    }

    private fun componentTypeDiagnostics(
        document: EngineeringDocument,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return document.components.mapNotNull { component ->
            when (val type = component.properties.requiredSymbolValue("type")) {
                PropertySymbolValue.Missing -> context.domainDiagnostic(
                    ruleId = "property.component.type.missing",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = component.id,
                    provenance = component.provenance,
                    message = "Component `${component.name}` is missing required `type`.",
                )

                is PropertySymbolValue.Invalid -> context.domainDiagnostic(
                    ruleId = "property.component.type.invalid",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = component.id,
                    provenance = component.provenance,
                    message = "Component `${component.name}` declares `type` with an invalid non-symbol value `${type.value}`.",
                )

                is PropertySymbolValue.Duplicate -> context.domainDiagnostic(
                    ruleId = "property.component.type.duplicate",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = component.id,
                    provenance = component.provenance,
                    message = "Component `${component.name}` declares duplicate `type` properties `${type.values}`.",
                )

                is PropertySymbolValue.SymbolText -> if (type.value !in VALID_DEVICE_TYPES) {
                    context.domainDiagnostic(
                        ruleId = "property.component.type.invalid",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = component.id,
                        provenance = component.provenance,
                        message = "Component `${component.name}` declares unsupported device type `${type.value}`.",
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun portDirectionDiagnostics(
        document: EngineeringDocument,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return document.ports.mapNotNull { port ->
            when (val direction = port.properties.requiredSymbolValue("direction")) {
                PropertySymbolValue.Missing -> context.domainDiagnostic(
                    ruleId = "property.port.direction.missing",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Port `${authoredPortPath(port)}` is missing required `direction`.",
                )

                is PropertySymbolValue.Invalid -> context.domainDiagnostic(
                    ruleId = "property.port.direction.invalid",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Port `${authoredPortPath(port)}` declares `direction` with an invalid non-symbol value `${direction.value}`.",
                )

                is PropertySymbolValue.Duplicate -> context.domainDiagnostic(
                    ruleId = "property.port.direction.duplicate",
                    category = SemanticDiagnosticCategory.PROPERTY,
                    subjectIdentity = port.id,
                    provenance = port.provenance,
                    message = "Port `${authoredPortPath(port)}` declares duplicate `direction` properties `${direction.values}`.",
                )

                is PropertySymbolValue.SymbolText -> if (direction.value !in VALID_DIRECTIONS) {
                    context.domainDiagnostic(
                        ruleId = "property.port.direction.invalid",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Port `${authoredPortPath(port)}` declares unsupported direction `${direction.value}`.",
                    )
                } else {
                    null
                }
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
                val fromPort = connection.from.resolvedIdentity?.let(portsById::get)
                val toPort = connection.to.resolvedIdentity?.let(portsById::get)
                if (fromPort == null || toPort == null) {
                    return@forEach
                }

                val fromDirection = fromPort.direction()
                val toDirection = toPort.direction()
                if (fromDirection != null && toDirection != null && (fromDirection != PortDirection.OUT || toDirection != PortDirection.IN)) {
                    add(
                        context.domainDiagnostic(
                            ruleId = "connection.direction.illegal",
                            category = SemanticDiagnosticCategory.CONNECTION,
                            subjectIdentity = connection.id,
                            provenance = connection.provenance,
                            message = "Connection `${authoredPath(connection.from)} -> ${authoredPath(connection.to)}` must flow from `out` to `in`.",
                        ),
                    )
                }

                val fromSignal = fromPort.properties.optionalSymbolValue("signal")
                val toSignal = toPort.properties.optionalSymbolValue("signal")
                if (fromSignal is PropertySymbolValue.SymbolText &&
                    toSignal is PropertySymbolValue.SymbolText &&
                    fromSignal.value != toSignal.value
                ) {
                    add(
                        context.domainDiagnostic(
                            ruleId = "connection.signal.incompatible",
                            category = SemanticDiagnosticCategory.CONNECTION,
                            subjectIdentity = connection.id,
                            provenance = connection.provenance,
                            message = "Connection `${authoredPath(connection.from)} -> ${authoredPath(connection.to)}` mixes incompatible signals `${fromSignal.value}` and `${toSignal.value}`.",
                        ),
                    )
                }
            }
        }
    }

    private fun portSignalDiagnostics(
        document: EngineeringDocument,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return document.ports.flatMap { port ->
            when (val signal = port.properties.optionalSymbolValue("signal")) {
                PropertySymbolValue.Missing,
                is PropertySymbolValue.SymbolText -> emptyList()

                is PropertySymbolValue.Invalid -> listOf(
                    context.domainDiagnostic(
                        ruleId = "property.port.signal.invalid",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Port `${authoredPortPath(port)}` declares `signal` with an invalid non-symbol value `${signal.value}`.",
                    ),
                )

                is PropertySymbolValue.Duplicate -> listOf(
                    context.domainDiagnostic(
                        ruleId = "property.port.signal.duplicate",
                        category = SemanticDiagnosticCategory.PROPERTY,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Port `${authoredPortPath(port)}` declares duplicate `signal` properties `${signal.values}`.",
                    ),
                )
            }
        }
    }

    private fun EngineeringPort.direction(): PortDirection? {
        return when (val direction = properties.optionalSymbolValue("direction")) {
            PropertySymbolValue.Missing -> null
            is PropertySymbolValue.SymbolText -> VALID_DIRECTIONS[direction.value]
            is PropertySymbolValue.Invalid,
            is PropertySymbolValue.Duplicate,
                -> null
        }
    }

    private fun List<EngineeringProperty>.requiredSymbolValue(name: String): PropertySymbolValue {
        return propertySymbolValue(name)
    }

    private fun List<EngineeringProperty>.optionalSymbolValue(name: String): PropertySymbolValue {
        return propertySymbolValue(name)
    }

    private fun List<EngineeringProperty>.propertySymbolValue(name: String): PropertySymbolValue {
        val matchingProperties = filter { it.name == name }
        if (matchingProperties.isEmpty()) {
            return PropertySymbolValue.Missing
        }
        if (matchingProperties.size > 1) {
            return PropertySymbolValue.Duplicate(matchingProperties.map { it.value.renderedValue() })
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

private val VALID_DEVICE_TYPES = setOf("PLC", "Motor")

private val VALID_DIRECTIONS = mapOf(
    "in" to PortDirection.IN,
    "out" to PortDirection.OUT,
)

private enum class PortDirection {
    IN,
    OUT,
}

private sealed interface PropertySymbolValue {
    data object Missing : PropertySymbolValue

    data class SymbolText(val value: String) : PropertySymbolValue

    data class Invalid(val value: String) : PropertySymbolValue

    data class Duplicate(val values: List<String>) : PropertySymbolValue
}

private fun EngineeringPropertyValue.renderedValue(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> "\"$text\""
    }
}

/**
 * Runtime-owned summary derived from the active electrical graph projection.
 */
private data class ElectricalRuntimeSummary(
    val signalCount: Int,
    val compatiblePairCount: Int,
    val connectedPairCount: Int,
)

/**
 * Resolves the first deterministic compatible electrical pair available for plugin command execution.
 */
private fun AthenaExecutionContext.firstCompatibleElectricalPair(): ElectricalCompatiblePair? {
    return electricalCompatiblePairs().firstOrNull()
}

/**
 * Derives the electrical runtime summary used by the first plugin view contribution.
 */
private fun AthenaExecutionContext.electricalRuntimeSummary(): ElectricalRuntimeSummary {
    val graphProjection = projectEngineeringGraphProjection() as? AthenaEngineeringGraphReadyProjection
        ?: return ElectricalRuntimeSummary(
            signalCount = 0,
            compatiblePairCount = 0,
            connectedPairCount = 0,
        )
    val portCandidates = graphProjection.electricalPortCandidates()
    val connectedPairs = graphProjection.connectedPairs()
    return ElectricalRuntimeSummary(
        signalCount = portCandidates.mapNotNull { candidate -> candidate.signal }.distinct().size,
        compatiblePairCount = electricalCompatiblePairs(graphProjection).size,
        connectedPairCount = connectedPairs.size,
    )
}

/**
 * Returns all compatible electrical pairs in deterministic label order.
 */
private fun AthenaExecutionContext.electricalCompatiblePairs(): List<ElectricalCompatiblePair> {
    val graphProjection = projectEngineeringGraphProjection() as? AthenaEngineeringGraphReadyProjection ?: return emptyList()
    return electricalCompatiblePairs(graphProjection)
}

/**
 * Returns all compatible electrical pairs using the supplied ready graph projection.
 */
private fun electricalCompatiblePairs(
    graphProjection: AthenaEngineeringGraphReadyProjection,
): List<ElectricalCompatiblePair> {
    val candidates = graphProjection.electricalPortCandidates()
    val connectedPairs = graphProjection.connectedPairs()
    val outputs = candidates.filter { candidate -> candidate.direction == "out" && candidate.signal != null }
    val inputs = candidates.filter { candidate -> candidate.direction == "in" && candidate.signal != null }
    return outputs.flatMap { output ->
        inputs.mapNotNull { input ->
            if (
                output.signal == input.signal &&
                output.semanticId != input.semanticId &&
                connectedPairs.none { pair ->
                    pair.first == output.semanticId && pair.second == input.semanticId
                }
            ) {
                ElectricalCompatiblePair(
                    sourceSemanticId = output.semanticId,
                    targetSemanticId = input.semanticId,
                    sortKey = "${output.label}->${input.label}",
                )
            } else {
                null
            }
        }
    }.sortedBy { pair -> pair.sortKey }
}

/**
 * Collects electrical port candidates from the runtime-owned graph projection.
 */
private fun AthenaEngineeringGraphReadyProjection.electricalPortCandidates(): List<ElectricalPortCandidate> {
    val graph = graph
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.PORT).map { portNode ->
        val ownerSemanticId = portNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.OWNER }
            ?.resolvedSemanticId
        val ownerName = ownerSemanticId?.let { semanticId -> graph.node(semanticId)?.displayName }
            ?: portNode.references.firstOrNull()?.authoredPath?.joinToString(".")
            ?: "Unknown"
        ElectricalPortCandidate(
            semanticId = portNode.semanticId,
            label = "$ownerName.${portNode.displayName}",
            direction = portNode.properties.firstOrNull { property -> property.name == "direction" }?.value,
            signal = portNode.properties.firstOrNull { property -> property.name == "signal" }?.value,
        )
    }
}

/**
 * Collects already connected source-target port pairs from the runtime-owned graph projection.
 */
private fun AthenaEngineeringGraphReadyProjection.connectedPairs(): Set<Pair<String, String>> {
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.CONNECTION).mapNotNull { connectionNode ->
        val sourceSemanticId = connectionNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.CONNECTION_SOURCE }
            ?.resolvedSemanticId
        val targetSemanticId = connectionNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.CONNECTION_TARGET }
            ?.resolvedSemanticId
        if (sourceSemanticId == null || targetSemanticId == null) {
            null
        } else {
            sourceSemanticId to targetSemanticId
        }
    }.toSet()
}

/**
 * Runtime-visible electrical port candidate used to derive plugin command and view contributions.
 */
private data class ElectricalPortCandidate(
    val semanticId: String,
    val label: String,
    val direction: String?,
    val signal: String?,
)

/**
 * Deterministic compatible electrical pair used by the hosted plugin command contribution.
 */
private data class ElectricalCompatiblePair(
    val sourceSemanticId: String,
    val targetSemanticId: String,
    val sortKey: String,
)
