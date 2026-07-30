package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringConnectionNetwork
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Typed, generic connectivity view derived from canonical Engineering IR. */
data class ConnectableEntityContract(
    val id: StableSemanticIdentity,
    val name: String,
    val kind: String,
    val interfaces: List<ConnectableInterfaceContract>,
    val ports: List<ConnectablePortContract>,
    val provenance: SourceProvenance,
)

data class ConnectableInterfaceContract(
    val id: ConnectableInterfaceId,
    val provenance: SourceProvenance,
)

@JvmInline
value class ConnectableInterfaceId(val value: String)

data class ConnectablePortContract(
    val id: StableSemanticIdentity,
    val name: String,
    val ownerId: StableSemanticIdentity,
    val interfaceIds: List<ConnectableInterfaceId>,
    val compatibility: ConnectablePortCompatibility,
    val provenance: SourceProvenance,
)

data class ConnectablePortCompatibility(
    val direction: ConnectablePortDirection,
    val multiplicity: ConnectablePortMultiplicity,
    val signalKind: String?,
    val role: String?,
    val parameters: Map<String, String>,
    val owner: ConnectableConstraintOwner = ConnectableConstraintOwner.SEMANTIC,
    val strength: ConnectableConstraintStrength = ConnectableConstraintStrength.REQUIRED,
)

enum class ConnectablePortDirection {
    INPUT,
    OUTPUT,
    BIDIRECTIONAL,
    PASSIVE,
}

enum class ConnectablePortMultiplicity {
    SINGLE,
    MULTIPLE,
}

enum class ConnectableConstraintOwner {
    SEMANTIC,
    REPRESENTATION,
    PHYSICAL,
    LAYOUT_PREFERENCE,
}

enum class ConnectableConstraintStrength {
    REQUIRED,
    PREFERRED,
    OPTIONAL,
}

data class ConnectableConnectionEndpointContract(
    val portId: StableSemanticIdentity,
    val provenance: SourceProvenance,
)

data class ConnectableConnectionContract(
    val id: StableSemanticIdentity,
    val from: ConnectableConnectionEndpointContract,
    val to: ConnectableConnectionEndpointContract,
    val provenance: SourceProvenance,
)

data class ConnectableNetworkContract(
    val id: StableSemanticIdentity,
    val name: String,
    val members: List<ConnectableNetworkMemberContract>,
    val junctions: List<ConnectableNetworkJunctionContract>,
    val compatibilityEvidence: List<ConnectableNetworkCompatibilityEvidenceContract>,
    val provenance: SourceProvenance,
)

data class ConnectableNetworkMemberContract(
    val connectionId: StableSemanticIdentity,
    val fromPortId: StableSemanticIdentity,
    val toPortId: StableSemanticIdentity,
    val provenance: SourceProvenance,
)

data class ConnectableNetworkJunctionContract(
    val id: StableSemanticIdentity,
    val sharedPortId: StableSemanticIdentity,
    val memberConnectionIds: List<StableSemanticIdentity>,
    val provenance: SourceProvenance,
)

data class ConnectableNetworkCompatibilityEvidenceContract(
    val kind: String,
    val value: String,
    val provenance: SourceProvenance,
)

sealed interface ConnectableEntityContractCompilation {
    data class Success(
        val entities: List<ConnectableEntityContract>,
        val connections: List<ConnectableConnectionContract> = emptyList(),
        val networks: List<ConnectableNetworkContract> = emptyList(),
    ) : ConnectableEntityContractCompilation

    data class Failure(val diagnostics: List<ConnectableEntityContractDiagnostic>) : ConnectableEntityContractCompilation
}

data class ConnectableEntityContractDiagnostic(
    val code: String,
    val message: String,
    val provenance: SourceProvenance,
)

/**
 * Builds M36's narrow Connectable Entity Contract from compiler-owned Engineering IR.
 * It intentionally does not resolve product knowledge, SVG geometry, or rendered occurrences.
 */
class ConnectableEntityContractCompiler {
    fun compile(document: EngineeringDocument): ConnectableEntityContractCompilation {
        val diagnostics = mutableListOf<ConnectableEntityContractDiagnostic>()
        val connectableComponents = document.components.filter { component ->
            component.properties.symbolValues("connectable").contains("enabled")
        }
        val portsByOwner = document.ports.groupBy { it.ownerReference.resolvedIdentity }
        val contracts = connectableComponents.map { component ->
            val interfaceIds = component.properties
                .symbolValues("interface")
                .map(::ConnectableInterfaceId)
            val ports = portsByOwner[component.id].orEmpty().map { port ->
                port.toContract(component.id, interfaceIds.toSet(), diagnostics)
            }
            ConnectableEntityContract(
                id = component.id,
                name = component.name,
                kind = component.kind,
                interfaces = interfaceIds.map { ConnectableInterfaceContract(it, component.provenance) },
                ports = ports,
                provenance = component.provenance,
            )
        }

        contracts.flatMap { it.ports }.groupBy { it.id }.filterValues { it.size > 1 }.forEach { (_, ports) ->
            diagnostics += ConnectableEntityContractDiagnostic(
                code = "connectable.port.duplicate",
                message = "Connectable Port '${ports.first().id.value}' is declared more than once.",
                provenance = ports.first().provenance,
            )
        }

        val connectionDiagnostics = validateConnections(document)
        diagnostics += connectionDiagnostics
        val validConnections = if (connectionDiagnostics.isEmpty()) {
            document.connections.mapNotNull { connection -> connection.toContract(document.ports) }
        } else {
            emptyList()
        }
        val networks = buildNetworkContracts(document, diagnostics)

        return if (diagnostics.isEmpty()) {
            ConnectableEntityContractCompilation.Success(contracts, validConnections, networks)
        } else {
            ConnectableEntityContractCompilation.Failure(diagnostics)
        }
    }

    /** Validates authored connections that opt into M36 typed-connectivity semantics. */
    fun validateConnections(document: EngineeringDocument): List<ConnectableEntityContractDiagnostic> {
        val connectableOwners = document.components
            .filter { it.properties.symbolValues("connectable").contains("enabled") }
            .map { it.id }
            .toSet()
        val portsById = document.ports.associateBy { it.id }

        val connectableNames = document.components
            .filter { it.id in connectableOwners }
            .map { it.name }
            .toSet()
        return document.connections.flatMap { connection ->
            val source = connection.from.resolvedIdentity?.let(portsById::get)
            val target = connection.to.resolvedIdentity?.let(portsById::get)
            val participates = listOfNotNull(source, target).any { it.ownerReference.resolvedIdentity in connectableOwners } ||
                connection.from.authoredPath.firstOrNull() in connectableNames ||
                connection.to.authoredPath.firstOrNull() in connectableNames
            if (!participates) emptyList() else validateConnection(connection, source, target, connectableOwners)
        }
    }

    private fun buildNetworkContracts(
        document: EngineeringDocument,
        diagnostics: MutableList<ConnectableEntityContractDiagnostic>,
    ): List<ConnectableNetworkContract> {
        val connectionsById = document.connections.associateBy { it.id }
        val portsById = document.ports.associateBy { it.id }
        return document.connectionNetworks.map { network ->
            val members = network.members.mapNotNull { member ->
                val connection = member.connectionReference.resolvedIdentity?.let { resolvedIdentity ->
                    connectionsById[resolvedIdentity]
                }
                if (connection == null) {
                    diagnostics += ConnectableEntityContractDiagnostic(
                        code = "connectable.network.member.connection.unresolved",
                        message = "Connection network '${network.name}' references an unresolved connection member.",
                        provenance = network.provenance,
                    )
                    return@mapNotNull null
                }

                val fromPort = resolvePort(member.fromPortReference, portsById, diagnostics, network)
                val toPort = resolvePort(member.toPortReference, portsById, diagnostics, network)
                if (fromPort == null || toPort == null) {
                    return@mapNotNull null
                }

                ConnectableNetworkMemberContract(
                    connectionId = connection.id,
                    fromPortId = fromPort.id,
                    toPortId = toPort.id,
                    provenance = member.connectionReference.provenance,
                )
            }

            val junctionContracts = network.junctions.mapNotNull { junction ->
                val sharedPort = resolvePort(junction.sharedPortReference, portsById, diagnostics, network)
                if (sharedPort == null) {
                    return@mapNotNull null
                }

                val networkConnectionIds = members.map { it.connectionId }.toSet()
                val memberConnections = junction.memberConnectionReferences.mapNotNull { connectionReference ->
                    connectionReference.resolvedIdentity?.let { resolvedIdentity ->
                        val connectionId = connectionsById[resolvedIdentity]?.id
                        if (connectionId != null && connectionId !in networkConnectionIds) {
                            diagnostics += ConnectableEntityContractDiagnostic(
                                code = "connectable.network.junction.member.outside-network",
                                message = "Connection network '${network.name}' junction references a Connection outside the network.",
                                provenance = connectionReference.provenance,
                            )
                        }
                        connectionId?.takeIf { it in networkConnectionIds }
                    }
                }
                if (memberConnections.size < 2) {
                    diagnostics += ConnectableEntityContractDiagnostic(
                        code = "connectable.network.junction.membership.insufficient",
                        message = "Connection network '${network.name}' declares a junction with fewer than two member Connections.",
                        provenance = junction.provenance,
                    )
                }

                val sharedPortDirection = sharedPort.compatibility().direction
                val memberConnectionSet = memberConnections.toSet()
                val sharedPortUsage = members.filter { it.connectionId in memberConnectionSet }.mapNotNull { member ->
                    val fromShared = member.fromPortId.value == sharedPort.id.value
                    val toShared = member.toPortId.value == sharedPort.id.value
                    when {
                        fromShared && toShared -> SharedPortUsage.BOTH
                        fromShared -> SharedPortUsage.SOURCE
                        toShared -> SharedPortUsage.TARGET
                        else -> null
                    }
                }.toSet()
                val allowedDirections = when {
                    sharedPortUsage == setOf(SharedPortUsage.SOURCE) -> setOf(
                        ConnectablePortDirection.OUTPUT,
                        ConnectablePortDirection.BIDIRECTIONAL,
                        ConnectablePortDirection.PASSIVE,
                    )
                    sharedPortUsage == setOf(SharedPortUsage.TARGET) -> setOf(
                        ConnectablePortDirection.INPUT,
                        ConnectablePortDirection.BIDIRECTIONAL,
                        ConnectablePortDirection.PASSIVE,
                    )
                    else -> setOf(ConnectablePortDirection.PASSIVE, ConnectablePortDirection.BIDIRECTIONAL)
                }
                if (memberConnections.size > 1 && sharedPortDirection !in allowedDirections) {
                    diagnostics += ConnectableEntityContractDiagnostic(
                        code = "connectable.network.junction.incompatible",
                        message = "Connection network '${network.name}' declares incompatible shared Port direction '${sharedPortDirection}' for multi-member junction '${sharedPort.id.value}'.",
                        provenance = junction.provenance,
                    )
                }

                ConnectableNetworkJunctionContract(
                    id = junction.id,
                    sharedPortId = sharedPort.id,
                    memberConnectionIds = memberConnections,
                    provenance = junction.provenance,
                )
            }

            val compatibilityEvidence = network.compatibilityEvidence.map {
                ConnectableNetworkCompatibilityEvidenceContract(it.kind, it.value, it.provenance)
            }

            if (compatibilityEvidence.isEmpty()) {
                diagnostics += ConnectableEntityContractDiagnostic(
                    code = "connectable.network.compatibility.missing",
                    message = "Connection network '${network.name}' must carry compatibility evidence.",
                    provenance = network.provenance,
                )
            }

            if (members.size > 1 && !membersFormConnectedGraph(members)) {
                diagnostics += ConnectableEntityContractDiagnostic(
                    code = "connectable.network.members.disconnected",
                    message = "Connection network '${network.name}' contains disconnected member groups.",
                    provenance = network.provenance,
                )
            }

            ConnectableNetworkContract(
                id = network.id,
                name = network.name,
                members = members,
                junctions = junctionContracts,
                compatibilityEvidence = compatibilityEvidence,
                provenance = network.provenance,
            )
        }
    }

    private fun resolvePort(
        reference: EngineeringReference,
        portsById: Map<StableSemanticIdentity, EngineeringPort>,
        diagnostics: MutableList<ConnectableEntityContractDiagnostic>,
        network: EngineeringConnectionNetwork,
    ): EngineeringPort? {
        val resolvedIdentity = reference.resolvedIdentity
        if (resolvedIdentity == null) {
            diagnostics += ConnectableEntityContractDiagnostic(
                code = "connectable.network.port.unresolved",
                message = "Connection network '${network.name}' references an unresolved Port.",
                provenance = reference.provenance,
            )
            return null
        }
        return portsById[resolvedIdentity] ?: run {
            diagnostics += ConnectableEntityContractDiagnostic(
                code = "connectable.network.port.unresolved",
                message = "Connection network '${network.name}' references an unknown Port '${resolvedIdentity.value}'.",
                provenance = reference.provenance,
            )
            null
        }
    }

    private fun validateConnection(
        connection: EngineeringConnection,
        source: EngineeringPort?,
        target: EngineeringPort?,
        connectableOwners: Set<StableSemanticIdentity>,
    ): List<ConnectableEntityContractDiagnostic> {
        if (source == null || target == null) {
            return buildList {
                if (source == null) add(unresolvedEndpointDiagnostic(connection, connection.from))
                if (target == null) add(unresolvedEndpointDiagnostic(connection, connection.to))
            }
        }
        if (source.id == target.id) {
            return endpointDiagnostics("connectable.connection.self", "cannot connect a Port to itself", source, target)
        }
        if (source.ownerReference.resolvedIdentity !in connectableOwners || target.ownerReference.resolvedIdentity !in connectableOwners) {
            return endpointDiagnostics("connectable.connection.endpoint.untyped", "requires two connectable Ports", source, target)
        }

        val sourceCompatibility = source.compatibility()
        val targetCompatibility = target.compatibility()
        if (!sourceCompatibility.direction.allowsSource() || !targetCompatibility.direction.allowsTarget()) {
            return endpointDiagnostics("connectable.connection.direction.incompatible", "has incompatible Port directions", source, target)
        }
        if (sourceCompatibility.signal != null && targetCompatibility.signal != null && sourceCompatibility.signal != targetCompatibility.signal) {
            return endpointDiagnostics("connectable.connection.signal.incompatible", "has incompatible signal kinds", source, target)
        }
        if (sourceCompatibility.role != null && targetCompatibility.role != null && sourceCompatibility.role != targetCompatibility.role) {
            return endpointDiagnostics("connectable.connection.role.incompatible", "has incompatible Port roles", source, target)
        }
        val incompatibleParameter = sourceCompatibility.parameters.keys.intersect(targetCompatibility.parameters.keys)
            .firstOrNull { key -> sourceCompatibility.parameters[key] != targetCompatibility.parameters[key] }
        return if (incompatibleParameter == null) emptyList() else endpointDiagnostics(
            "connectable.connection.parameter.incompatible",
            "has incompatible compatibility parameter '$incompatibleParameter'",
            source,
            target,
        )
    }

    private fun endpointDiagnostics(
        code: String,
        reason: String,
        source: EngineeringPort,
        target: EngineeringPort,
    ) = listOf(
        ConnectableEntityContractDiagnostic(code, "Connection $reason: '${source.id.value}' -> '${target.id.value}'.", source.provenance),
        ConnectableEntityContractDiagnostic(code, "Connection $reason: '${source.id.value}' -> '${target.id.value}'.", target.provenance),
    )

    private fun EngineeringPort.toContract(
        ownerId: StableSemanticIdentity,
        declaredInterfaces: Set<ConnectableInterfaceId>,
        diagnostics: MutableList<ConnectableEntityContractDiagnostic>,
    ): ConnectablePortContract {
        val values = properties.symbolValuesByName()
        val directionValue = values["direction"]?.singleOrNull()
        val direction = directionValue.toConnectableDirection()
        if (directionValue.isNullOrBlank()) {
            diagnostics += ConnectableEntityContractDiagnostic(
                code = "connectable.port.direction.missing",
                message = "Connectable Port '$name' must declare a direction.",
                provenance = provenance,
            )
        } else if (direction == null) {
            diagnostics += ConnectableEntityContractDiagnostic(
                code = "connectable.port.direction.invalid",
                message = "Connectable Port '$name' declares unsupported direction '$directionValue'.",
                provenance = provenance,
            )
        }
        val multiplicityValue = values["multiplicity"]?.singleOrNull() ?: "single"
        val multiplicity = multiplicityValue.toConnectableMultiplicity()
        if (multiplicity == null) diagnostics += ConnectableEntityContractDiagnostic(
            code = "connectable.port.multiplicity.invalid",
            message = "Connectable Port '$name' declares unsupported multiplicity '$multiplicityValue'.",
            provenance = provenance,
        )
        val interfaceIds = values["interface"].orEmpty().map(::ConnectableInterfaceId)
        interfaceIds.filterNot { it in declaredInterfaces }.forEach { interfaceId ->
            diagnostics += ConnectableEntityContractDiagnostic(
                code = "connectable.port.interface.unknown",
                message = "Connectable Port '$name' references unknown interface '${interfaceId.value}'.",
                provenance = provenance,
            )
        }
        val compatibilityValues = values.filterKeys { it.startsWith("compatibility.") }
        compatibilityValues.filterValues { it.size != 1 }.forEach { (key, _) ->
            diagnostics += ConnectableEntityContractDiagnostic(
                code = "connectable.port.compatibility.duplicate",
                message = "Connectable Port '$name' declares compatibility parameter '$key' more than once.",
                provenance = provenance,
            )
        }
        return ConnectablePortContract(
            id = id,
            name = name,
            ownerId = ownerId,
            interfaceIds = interfaceIds,
            compatibility = ConnectablePortCompatibility(
                direction = direction ?: ConnectablePortDirection.PASSIVE,
                multiplicity = multiplicity ?: ConnectablePortMultiplicity.SINGLE,
                signalKind = values["signal"]?.singleOrNull(),
                role = values["role"]?.singleOrNull(),
                parameters = compatibilityValues
                    .filterValues { it.size == 1 }
                    .mapKeys { (key, _) -> key.removePrefix("compatibility.") }
                    .mapValues { (_, value) -> value.single() },
            ),
            provenance = provenance,
        )
    }
}

private fun EngineeringConnection.toContract(ports: List<EngineeringPort>): ConnectableConnectionContract? {
    val portsById = ports.associateBy { it.id }
    val fromPort = from.resolvedIdentity?.let(portsById::get) ?: return null
    val toPort = to.resolvedIdentity?.let(portsById::get) ?: return null
    return ConnectableConnectionContract(
        id = id,
        from = ConnectableConnectionEndpointContract(fromPort.id, from.provenance),
        to = ConnectableConnectionEndpointContract(toPort.id, to.provenance),
        provenance = provenance,
    )
}

private fun unresolvedEndpointDiagnostic(
    connection: EngineeringConnection,
    reference: EngineeringReference,
) = ConnectableEntityContractDiagnostic(
    code = "connectable.connection.endpoint.unresolved",
    message = "Connectable connection '${connection.id.value}' references unresolved Port '${reference.authoredPath.joinToString(".")}'.",
    provenance = reference.provenance,
)

private fun membersFormConnectedGraph(members: List<ConnectableNetworkMemberContract>): Boolean {
    if (members.size < 2) return true
    val remaining = members.toMutableList()
    val connectedPorts = mutableSetOf<StableSemanticIdentity>()
    val first = remaining.removeAt(0)
    connectedPorts += first.fromPortId
    connectedPorts += first.toPortId
    var advanced: Boolean
    do {
        advanced = false
        val iterator = remaining.iterator()
        while (iterator.hasNext()) {
            val member = iterator.next()
            if (member.fromPortId in connectedPorts || member.toPortId in connectedPorts) {
                connectedPorts += member.fromPortId
                connectedPorts += member.toPortId
                iterator.remove()
                advanced = true
            }
        }
    } while (advanced)
    return remaining.isEmpty()
}

private enum class SharedPortUsage {
    SOURCE,
    TARGET,
    BOTH,
}

private data class ConnectablePortValues(
    val direction: ConnectablePortDirection?,
    val signal: String?,
    val role: String?,
    val parameters: Map<String, String>,
)

private fun EngineeringPort.compatibility(): ConnectablePortValues {
    val values = properties.symbolValuesByName()
    return ConnectablePortValues(
        direction = values["direction"]?.singleOrNull().toConnectableDirection(),
        signal = values["signal"]?.singleOrNull(),
        role = values["role"]?.singleOrNull(),
        parameters = values.filterKeys { it.startsWith("compatibility.") }.mapValues { (_, value) -> value.singleOrNull().orEmpty() },
    )
}

private fun ConnectablePortDirection?.allowsSource(): Boolean =
    this in setOf(ConnectablePortDirection.OUTPUT, ConnectablePortDirection.BIDIRECTIONAL, ConnectablePortDirection.PASSIVE)

private fun ConnectablePortDirection?.allowsTarget(): Boolean =
    this in setOf(ConnectablePortDirection.INPUT, ConnectablePortDirection.BIDIRECTIONAL, ConnectablePortDirection.PASSIVE)

private fun String?.toConnectableDirection(): ConnectablePortDirection? = when (this?.lowercase()) {
    "in", "input" -> ConnectablePortDirection.INPUT
    "out", "output" -> ConnectablePortDirection.OUTPUT
    "bidirectional" -> ConnectablePortDirection.BIDIRECTIONAL
    "passive" -> ConnectablePortDirection.PASSIVE
    else -> null
}

private fun String.toConnectableMultiplicity(): ConnectablePortMultiplicity? = when (lowercase()) {
    "single" -> ConnectablePortMultiplicity.SINGLE
    "multiple", "many" -> ConnectablePortMultiplicity.MULTIPLE
    else -> null
}

private fun List<EngineeringProperty>.symbolValues(name: String): List<String> =
    filter { it.name == name }.mapNotNull { (it.value as? EngineeringPropertyValue.Symbol)?.text }

private fun List<EngineeringProperty>.symbolValuesByName(): Map<String, List<String>> =
    groupBy { it.name }.mapValues { (_, properties) ->
        properties.mapNotNull { (it.value as? EngineeringPropertyValue.Symbol)?.text }
    }
