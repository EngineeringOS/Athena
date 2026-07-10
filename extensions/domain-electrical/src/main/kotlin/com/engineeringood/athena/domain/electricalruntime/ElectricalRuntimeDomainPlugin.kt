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
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.ScalarValue
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
import com.engineeringood.athena.layout.ProjectionInteractivity
import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.layout.ViewEmphasis
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainConnectionSchema
import com.engineeringood.athena.plugin.AthenaDomainEntitySchema
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaDomainPortSchema
import com.engineeringood.athena.plugin.AthenaDomainPropertySchema
import com.engineeringood.athena.plugin.AthenaDomainPropertyValueKind
import com.engineeringood.athena.plugin.AthenaDomainSchema
import com.engineeringood.athena.plugin.AthenaDomainSchemaSubjectKind
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaSemanticReviewEnrichmentContributor
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.plugin.AthenaRenderContribution
import com.engineeringood.athena.plugin.AthenaRenderSurface
import com.engineeringood.athena.plugin.AthenaRenderSurfaceMapping
import com.engineeringood.athena.plugin.AthenaValidationContribution
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.plugin.CoreVersionRange
import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewEnrichmentKind
import com.engineeringood.athena.scm.SemanticReviewFactKind
import com.engineeringood.athena.scm.SemanticReviewFactReference
import com.engineeringood.athena.scm.SemanticReviewSummary
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory

/** Reference Electrical/Runtime proof plugin that publishes the stable M3 hosted domain surface. */
class ElectricalRuntimeDomainPlugin : AthenaDomainPlugin, AthenaViewDefinitionContributor, AthenaRuntimePluginCommandContributor, AthenaRuntimePluginViewContributor, AthenaSemanticReviewEnrichmentContributor {
    /** Core-owned manifest declaring the sample plugin's identity, type, compatibility, and extension point. */
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.electrical-runtime",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.VIEW_DEFINITIONS,
            AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
            AthenaExtensionPoint.RUNTIME_COMMANDS,
            AthenaExtensionPoint.RUNTIME_VIEWS,
        ),
    )

    /** Minimal capability declaration showing the plugin remains a domain extension rather than core authority. */
    override val domainCapabilities: Set<String> = setOf("electrical-runtime")

    /** Generic electrical proof schema published through the stable plugin API. */
    override val domainSchema: AthenaDomainSchema = ELECTRICAL_DOMAIN_SCHEMA

    /** Inspectable validation contribution declarations exposed by the electrical proof plugin. */
    override val validationContributions: List<AthenaValidationContribution> = ELECTRICAL_VALIDATION_CONTRIBUTIONS

    /** Inspectable compiler-stage contribution declarations exposed by the electrical proof plugin. */
    override val compilerPassContributions: List<AthenaCompilerPassContribution> = ELECTRICAL_COMPILER_PASS_CONTRIBUTIONS

    /** Inspectable renderer-facing contribution declarations exposed by the electrical proof plugin. */
    override val renderContributions: List<AthenaRenderContribution> = ELECTRICAL_RENDER_CONTRIBUTIONS

    /** Lowers authored Electrical/Runtime proof declarations into compiler-owned semantic blueprints. */
    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        val deviceDeclarations = context.source.ast.declarations.filterIsInstance<DeviceDeclaration>()
        val explicitForeignDeviceNames = deviceDeclarations
            .filter { declaration -> declaration.isExplicitForeignDomain() }
            .map { declaration -> declaration.name }
            .toSet()
        val ownedDeviceNames = deviceDeclarations
            .filter { declaration -> declaration.isElectricalOwned() }
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
        val ports = context.source.ast.declarations
            .filterIsInstance<PortDeclaration>()
            .filter { declaration -> declaration.qualifiedName.parts.firstOrNull() !in explicitForeignDeviceNames }
            .map { declaration ->
            context.port(
                ownerPath = declaration.qualifiedName.parts.dropLast(1),
                ownerProvenance = context.provenance(declaration.qualifiedName.span),
                name = declaration.qualifiedName.parts.last(),
                properties = context.lowerProperties(declaration.fields),
                provenance = context.provenance(declaration.span),
            )
        }
        val connections = context.source.ast.declarations
            .filterIsInstance<ConnectionDeclaration>()
            .filter { declaration ->
                declaration.from.parts.firstOrNull() !in explicitForeignDeviceNames &&
                    declaration.to.parts.firstOrNull() !in explicitForeignDeviceNames
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

    /** Validates Electrical/Runtime proof properties and connection compatibility over canonical Engineering IR. */
    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        val ownedComponents = context.document.components.filter { component -> component.isElectricalOwned() }
        val ownedComponentIds = ownedComponents.map { component -> component.id }.toSet()
        val ownedPorts = context.document.ports.filter { port -> port.ownerReference.resolvedIdentity in ownedComponentIds }
        val portsById = ownedPorts.associateBy { it.id }
        val ownedConnections = context.document.connections.filter { connection ->
            connection.from.resolvedIdentity in portsById &&
                connection.to.resolvedIdentity in portsById
        }
        val diagnostics = buildList {
            addAll(componentTypeDiagnostics(ownedComponents, context))
            addAll(portDirectionDiagnostics(ownedPorts, context))
            addAll(portSignalDiagnostics(ownedPorts, context))
            addAll(connectionCompatibilityDiagnostics(ownedConnections, portsById, context))
        }
        return AthenaPluginValidationResult(
            contributions = listOf(
                context.emitValidationContribution(
                    contributionId = ELECTRICAL_VALIDATION_CONTRIBUTION_ID,
                    diagnostics = diagnostics,
                ),
            ),
        )
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
                ownershipContract = ProjectionOwnershipContract(
                    interactivity = ProjectionInteractivity.INTERACTIVE,
                    displayScopes = listOf(
                        "devices",
                        "ports",
                        "ownership-relationships",
                        "connectivity-relationships",
                        "grouped-placement",
                    ),
                    projectionCommandIds = listOf(
                        "adjust-layout-placement",
                        "adjust-layout-grouping",
                    ),
                    transientInteractionKinds = listOf(
                        "navigate-view",
                        "inspect-selection",
                        "preview-related-elements",
                    ),
                    persistedProjectionMetadataKeys = listOf(
                        "layout-placement",
                        "layout-group-membership",
                    ),
                ),
            ),
            ViewDefinition(
                id = "wiring",
                displayName = "Wiring",
                layoutIntent = LayoutIntent.CONNECTIVITY,
                groupingRules = listOf("group-by-signal", "group-by-connection-path"),
                viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
                description = "Highlights compatible signal flow and connection relationships between ports.",
                ownershipContract = ProjectionOwnershipContract(
                    interactivity = ProjectionInteractivity.INSPECT_ONLY,
                    displayScopes = listOf(
                        "devices",
                        "ports",
                        "signal-groups",
                        "connectivity-relationships",
                    ),
                    transientInteractionKinds = listOf(
                        "navigate-view",
                        "inspect-selection",
                        "preview-related-elements",
                    ),
                ),
            ),
        )
    }

    /** Contributes the first runtime-hosted electrical view proof through existing shell seams. */
    override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
        val summary = context.electricalRuntimeSummary()
        if (summary.ownedPortCount == 0 && summary.ownedComponentCount == 0) {
            return emptyList()
        }
        return listOf(
            AthenaRuntimePluginViewContribution(
                inspectorGroups = listOf(
                    AthenaRuntimePluginInspectorGroup(
                        title = "Electrical runtime",
                        fields = listOf(
                            AthenaRuntimePluginInspectorField("Domain", "electrical-runtime"),
                            AthenaRuntimePluginInspectorField("Components", summary.ownedComponentCount.toString()),
                            AthenaRuntimePluginInspectorField("Ports", summary.ownedPortCount.toString()),
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

    /** Adds electrical review interpretation without mutating or replacing the core semantic review facts. */
    override fun enrichReview(review: SemanticReviewSummary): List<SemanticReviewEnrichment> {
        val matchingDiagnostics = review.diagnostics.filter { diagnostic ->
            diagnostic.ruleId.value.contains("connection.direction", ignoreCase = true) ||
                diagnostic.ruleId.value.contains("connection.signal", ignoreCase = true) ||
                diagnostic.message.contains("`direction`", ignoreCase = true) ||
                diagnostic.message.contains("`signal`", ignoreCase = true) ||
                diagnostic.message.contains("device type", ignoreCase = true)
        }
        val matchingEntries = review.entries.filter { entry ->
            entry.message.contains("Connection", ignoreCase = true) ||
                entry.message.contains("signal", ignoreCase = true) ||
                entry.message.contains("direction", ignoreCase = true)
        }
        if (matchingDiagnostics.isEmpty() && matchingEntries.isEmpty()) {
            return emptyList()
        }

        val factReferences = (
            matchingEntries.flatMap { entry -> entry.factReferences } +
                matchingDiagnostics.map { diagnostic ->
                    SemanticReviewFactReference(
                        factKind = SemanticReviewFactKind.DIAGNOSTIC,
                        identifier = diagnostic.ruleId.value,
                        subjectIdentity = diagnostic.subjectIdentity,
                    )
                }
            ).distinct()
            .sortedWith(
                compareBy<SemanticReviewFactReference>(
                    { reference -> reference.factKind.name },
                    { reference -> reference.identifier },
                    { reference -> reference.subjectIdentity?.value.orEmpty() },
                ),
            )

        return listOf(
            SemanticReviewEnrichment(
                pluginId = manifest.pluginId,
                kind = SemanticReviewEnrichmentKind.DOMAIN_LABEL,
                message = "Electrical runtime semantics are implicated in this review.",
                factReferences = factReferences,
            ),
            SemanticReviewEnrichment(
                pluginId = manifest.pluginId,
                kind = SemanticReviewEnrichmentKind.REVIEW_HINT,
                message = "Check direction, signal, and device-type consistency before finalizing the change.",
                factReferences = factReferences,
            ),
            SemanticReviewEnrichment(
                pluginId = manifest.pluginId,
                kind = SemanticReviewEnrichmentKind.DOMAIN_SUMMARY,
                message = "Electrical review found ${matchingDiagnostics.size} electrical diagnostic(s) and ${matchingEntries.size} electrical review entry candidate(s).",
                factReferences = factReferences,
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
        ports: List<EngineeringPort>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return ports.mapNotNull { port ->
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
        ports: List<EngineeringPort>,
        context: AthenaPluginValidationContext,
    ): List<SemanticDiagnostic> {
        return ports.flatMap { port ->
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

    private fun com.engineeringood.athena.ir.EngineeringComponent.isElectricalOwned(): Boolean {
        val domain = properties.domainMarkerValue()
        return domain == null || domain == ELECTRICAL_DOMAIN_ID
    }

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
}

private val VALID_DEVICE_TYPES = setOf("Lamp", "Motor", "Switch")

private const val ELECTRICAL_DOMAIN_ID = "electrical-runtime"

private val VALID_DIRECTIONS = mapOf(
    "in" to PortDirection.IN,
    "out" to PortDirection.OUT,
)

private val ELECTRICAL_DOMAIN_SCHEMA = AthenaDomainSchema(
    domainId = ELECTRICAL_DOMAIN_ID,
    displayName = "Electrical Runtime",
    description = "Reference real proof-domain schema for hosted electrical semantics on the Athena JVM-first path.",
    capabilities = setOf(ELECTRICAL_DOMAIN_ID),
    entities = listOf(
        AthenaDomainEntitySchema(
            typeId = "Lamp",
            displayName = "Lamp",
            subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
            description = "Electrical indicator or load component declared through the generic device surface.",
            propertyNames = setOf("type"),
            portTypeIds = setOf("electrical-port"),
        ),
        AthenaDomainEntitySchema(
            typeId = "Motor",
            displayName = "Motor",
            subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
            description = "Electrical rotating load component declared through the generic device surface.",
            propertyNames = setOf("type"),
            portTypeIds = setOf("electrical-port"),
        ),
        AthenaDomainEntitySchema(
            typeId = "Switch",
            displayName = "Switch",
            subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
            description = "Electrical switching or control component declared through the generic device surface.",
            propertyNames = setOf("type"),
            portTypeIds = setOf("electrical-port"),
        ),
    ),
    properties = listOf(
        AthenaDomainPropertySchema(
            name = "type",
            displayName = "Component type",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.COMPONENT),
            required = true,
            allowedSymbolValues = VALID_DEVICE_TYPES,
            description = "Proof-domain type selector interpreted by the electrical proof plugin over generic components.",
        ),
        AthenaDomainPropertySchema(
            name = "direction",
            displayName = "Port direction",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = true,
            allowedSymbolValues = setOf("in", "out"),
            description = "Direction metadata interpreted by the electrical proof plugin over generic ports.",
        ),
        AthenaDomainPropertySchema(
            name = "signal",
            displayName = "Port signal",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = false,
            description = "Optional signal metadata used to validate port compatibility and wiring intent.",
        ),
    ),
    ports = listOf(
        AthenaDomainPortSchema(
            typeId = "electrical-port",
            displayName = "Electrical port",
            description = "Generic hosted electrical port for the first proof domain.",
            propertyNames = setOf("direction", "signal"),
            allowedDirections = setOf("in", "out"),
        ),
    ),
    connections = listOf(
        AthenaDomainConnectionSchema(
            typeId = "Wire",
            displayName = "Wire",
            description = "Hosted wire connection interpreted over the generic connection surface.",
            sourcePortTypeIds = setOf("electrical-port"),
            targetPortTypeIds = setOf("electrical-port"),
        ),
    ),
)

private val ELECTRICAL_VALIDATION_CONTRIBUTIONS = listOf(
    AthenaValidationContribution(
        contributionId = ELECTRICAL_VALIDATION_CONTRIBUTION_ID,
        displayName = "Electrical property and compatibility validation",
        description = "Validates supported component types, port direction and signal metadata, and connection compatibility.",
    ),
)

private val ELECTRICAL_COMPILER_PASS_CONTRIBUTIONS = listOf(
    AthenaCompilerPassContribution(
        contributionId = "electrical-runtime.lower.devices-ports-connections",
        stage = AthenaCompilerContributionStage.LOWER,
        displayName = "Electrical lowering",
        description = "Interprets generic devices, ports, and connections as hosted electrical proof semantics during lowering.",
    ),
    AthenaCompilerPassContribution(
        contributionId = "electrical-runtime.validate.compatibility-and-property-rules",
        stage = AthenaCompilerContributionStage.VALIDATE,
        displayName = "Electrical validation",
        description = "Applies hosted electrical compatibility and property validation during the compiler-owned validate stage.",
    ),
)

private val ELECTRICAL_RENDER_CONTRIBUTIONS = listOf(
    AthenaRenderContribution(
        contributionId = "electrical-runtime.render.cabinet",
        displayName = "Electrical cabinet rendering intent",
        description = "Publishes cabinet-view visual intent for hosted electrical structure without taking renderer ownership.",
        viewIds = setOf("cabinet"),
        rendererTargets = setOf("svg", "graph-workbench"),
        surfaceMappings = listOf(
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.CANVAS,
                tokens = mapOf(
                    "canvasTint" to "rgba(22, 18, 12, 0.92)",
                    "gridMajor" to "rgba(209, 151, 67, 0.16)",
                    "gridMinor" to "rgba(209, 151, 67, 0.06)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.NODE,
                tokens = mapOf(
                    "fill" to "rgba(52, 38, 21, 0.88)",
                    "stroke" to "rgba(224, 176, 92, 0.94)",
                    "label" to "#fff3d9",
                    "meta" to "rgba(235, 206, 150, 0.84)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.EDGE,
                tokens = mapOf(
                    "stroke" to "rgba(240, 191, 98, 0.92)",
                ),
            ),
        ),
    ),
    AthenaRenderContribution(
        contributionId = "electrical-runtime.render.wiring",
        displayName = "Electrical wiring rendering intent",
        description = "Publishes wiring-view visual intent for hosted electrical connectivity without taking renderer ownership.",
        viewIds = setOf("wiring"),
        rendererTargets = setOf("svg", "graph-workbench"),
        surfaceMappings = listOf(
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.CANVAS,
                tokens = mapOf(
                    "canvasTint" to "rgba(9, 24, 33, 0.94)",
                    "gridMajor" to "rgba(95, 207, 240, 0.16)",
                    "gridMinor" to "rgba(95, 207, 240, 0.06)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.NODE,
                tokens = mapOf(
                    "fill" to "rgba(15, 42, 56, 0.9)",
                    "stroke" to "rgba(101, 216, 247, 0.96)",
                    "label" to "#e8fbff",
                    "meta" to "rgba(158, 225, 240, 0.82)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.EDGE,
                tokens = mapOf(
                    "stroke" to "rgba(96, 223, 255, 0.94)",
                ),
            ),
        ),
    ),
)

private const val ELECTRICAL_VALIDATION_CONTRIBUTION_ID = "electrical-runtime.validation.component-and-port-rules"

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

private fun PropertyAssignment.scalarIdentifierText(): String? {
    return when (val value = value) {
        is ScalarValue.Identifier -> value.text
        is ScalarValue.StringLiteral -> value.text
    }
}

private fun DeviceDeclaration.isElectricalOwned(): Boolean {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText().let { domain ->
        domain == null || domain == ELECTRICAL_DOMAIN_ID
    }
}

private fun DeviceDeclaration.isExplicitForeignDomain(): Boolean {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText().let { domain ->
        domain != null && domain != ELECTRICAL_DOMAIN_ID
    }
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
    val ownedComponentCount: Int,
    val ownedPortCount: Int,
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
            ownedComponentCount = 0,
            ownedPortCount = 0,
            signalCount = 0,
            compatiblePairCount = 0,
            connectedPairCount = 0,
        )
    val electricalComponentIds = graphProjection.electricalComponentSemanticIds()
    val portCandidates = graphProjection.electricalPortCandidates(electricalComponentIds)
    val connectedPairs = graphProjection.connectedPairs()
    return ElectricalRuntimeSummary(
        ownedComponentCount = electricalComponentIds.size,
        ownedPortCount = portCandidates.size,
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
    val electricalComponentIds = graphProjection.electricalComponentSemanticIds()
    val candidates = graphProjection.electricalPortCandidates(electricalComponentIds)
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
 * Collects electrical-owned component ids from the runtime-owned graph projection.
 */
private fun AthenaEngineeringGraphReadyProjection.electricalComponentSemanticIds(): Set<String> {
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.COMPONENT)
        .filter { node ->
            val domainValue = node.properties.firstOrNull { property -> property.name == "domain" }?.value
            domainValue == null || domainValue == ELECTRICAL_DOMAIN_ID
        }
        .map { node -> node.semanticId }
        .toSet()
}

/**
 * Collects electrical port candidates from the runtime-owned graph projection.
 */
private fun AthenaEngineeringGraphReadyProjection.electricalPortCandidates(
    electricalComponentIds: Set<String>,
): List<ElectricalPortCandidate> {
    val graph = graph
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.PORT).mapNotNull { portNode ->
        val ownerSemanticId = portNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.OWNER }
            ?.resolvedSemanticId
            ?: return@mapNotNull null
        if (ownerSemanticId !in electricalComponentIds) {
            return@mapNotNull null
        }
        val ownerName = graph.node(ownerSemanticId)?.displayName
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
