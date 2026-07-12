package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory

internal fun validateElectricalRuntime(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
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

private fun List<EngineeringProperty>.requiredSymbolValue(name: String): PropertySymbolValue = propertySymbolValue(name)

private fun List<EngineeringProperty>.optionalSymbolValue(name: String): PropertySymbolValue = propertySymbolValue(name)

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

private fun EngineeringPropertyValue.renderedValue(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> "\"$text\""
    }
}

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
