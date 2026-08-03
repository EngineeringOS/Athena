package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringConnectionNetwork
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringExternalEvidenceMapping
import com.engineeringood.athena.ir.EngineeringExternalEvidenceSubjectKind
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Typed, generic connectivity view derived from canonical Engineering IR. */
data class EngineeringConnectivityContract(
    val id: StableSemanticIdentity,
    val name: String,
    val kind: String,
    val interfaces: List<EngineeringConnectivityInterfaceContract>,
    val ports: List<EngineeringConnectivityPortContract>,
    val provenance: SourceProvenance,
    val physicalInstallationReferences: List<EngineeringConnectivityPhysicalReference> = emptyList(),
    val representationBindings: List<EngineeringConnectivityRepresentationReference> = emptyList(),
    val externalEvidenceReferences: List<EngineeringConnectivityEvidenceReference> = emptyList(),
)

data class EngineeringConnectivityPhysicalReference(
    val authoredPath: List<String>,
    val targetId: StableSemanticIdentity,
    val provenance: SourceProvenance,
)

data class EngineeringConnectivityRepresentationReference(
    val authoredPath: List<String>,
    val targetId: StableSemanticIdentity,
    val provenance: SourceProvenance,
)

data class EngineeringConnectivityEvidenceReference(
    val namespace: EngineeringConnectivityEvidenceNamespace,
    val reference: EngineeringConnectivityEvidenceValue,
    val subject: EngineeringConnectivityEvidenceSubject,
    val externalProvenance: String,
    val provenance: SourceProvenance,
)

enum class EngineeringConnectivityEvidenceNamespace(val sourceName: String) {
    IEC("iec"),
    CLASSIFICATION("classification"),
}

@JvmInline
value class EngineeringConnectivityEvidenceValue(val value: String)

data class EngineeringConnectivityEvidenceSubject(
    val kind: EngineeringConnectivityEvidenceSubjectKind,
    val authoredPath: List<String>,
    val targetId: StableSemanticIdentity?,
)

enum class EngineeringConnectivityEvidenceSubjectKind {
    CONTRACT,
    INTERFACE,
    PORT,
    RELATION_CONTRACT,
    ROUTE_POLICY,
}

data class EngineeringConnectivityReferenceContract(
    val authoredPath: List<String>,
    val targetId: StableSemanticIdentity,
    val provenance: SourceProvenance,
)

data class EngineeringConnectivityInterfaceContract(
    val id: EngineeringConnectivityInterfaceId,
    val provenance: SourceProvenance,
)

@JvmInline
value class EngineeringConnectivityInterfaceId(val value: String)

data class EngineeringConnectivityPortContract(
    val id: StableSemanticIdentity,
    val name: String,
    val ownerId: StableSemanticIdentity,
    val interfaceIds: List<EngineeringConnectivityInterfaceId>,
    val compatibility: EngineeringConnectivityPortCompatibility,
    val provenance: SourceProvenance,
)

data class EngineeringConnectivityPortCompatibility(
    val direction: EngineeringConnectivityPortDirection,
    val multiplicity: EngineeringConnectivityPortMultiplicity,
    val signalKind: String?,
    val role: String?,
    val parameters: Map<String, String>,
    val owner: EngineeringConnectivityConstraintOwner = EngineeringConnectivityConstraintOwner.SEMANTIC,
    val strength: EngineeringConnectivityConstraintStrength = EngineeringConnectivityConstraintStrength.REQUIRED,
)

enum class EngineeringConnectivityPortDirection {
    INPUT,
    OUTPUT,
    BIDIRECTIONAL,
    PASSIVE,
}

enum class EngineeringConnectivityPortMultiplicity {
    SINGLE,
    MULTIPLE,
}

enum class EngineeringConnectivityConstraintOwner {
    SEMANTIC,
    REPRESENTATION,
    PHYSICAL,
    LAYOUT_PREFERENCE,
}

enum class EngineeringConnectivityConstraintStrength {
    REQUIRED,
    PREFERRED,
    OPTIONAL,
}

data class EngineeringConnectivityConnectionEndpointContract(
    val port: EngineeringConnectivityReferenceContract,
)

data class EngineeringConnectivityConnectionContract(
    val id: StableSemanticIdentity,
    val from: EngineeringConnectivityConnectionEndpointContract,
    val to: EngineeringConnectivityConnectionEndpointContract,
    val provenance: SourceProvenance,
)

data class EngineeringConnectivityNetworkContract(
    val id: StableSemanticIdentity,
    val name: String,
    val members: List<EngineeringConnectivityNetworkMemberContract>,
    val junctions: List<EngineeringConnectivityNetworkJunctionContract>,
    val compatibilityEvidence: List<EngineeringConnectivityNetworkCompatibilityEvidenceContract>,
    val provenance: SourceProvenance,
)

data class EngineeringConnectivityNetworkMemberContract(
    val connection: EngineeringConnectivityReferenceContract,
    val fromPort: EngineeringConnectivityReferenceContract,
    val toPort: EngineeringConnectivityReferenceContract,
)

data class EngineeringConnectivityNetworkJunctionContract(
    val id: StableSemanticIdentity,
    val sharedPort: EngineeringConnectivityReferenceContract,
    val memberConnections: List<EngineeringConnectivityReferenceContract>,
    val provenance: SourceProvenance,
)

data class EngineeringConnectivityNetworkCompatibilityEvidenceContract(
    val kind: String,
    val value: String,
    val provenance: SourceProvenance,
)

sealed interface EngineeringConnectivityCompilation {
    data class Success(
        val contracts: List<EngineeringConnectivityContract>,
        val connections: List<EngineeringConnectivityConnectionContract> = emptyList(),
        val networks: List<EngineeringConnectivityNetworkContract> = emptyList(),
        val externalEvidence: List<EngineeringConnectivityEvidenceReference> = emptyList(),
        val provenance: SourceProvenance,
    ) : EngineeringConnectivityCompilation

    data class Failure(val diagnostics: List<EngineeringConnectivityDiagnostic>) : EngineeringConnectivityCompilation
}

data class EngineeringConnectivityDiagnostic(
    val code: String,
    val message: String,
    val provenance: SourceProvenance,
)

/**
 * Builds the projection-neutral Engineering Connectivity Contract from canonical Engineering IR.
 * Product knowledge, visual geometry, and rendered occurrences remain outside this contract.
 */
class EngineeringConnectivityContractCompiler {
    fun compile(document: EngineeringDocument): EngineeringConnectivityCompilation {
        val diagnostics = mutableListOf<EngineeringConnectivityDiagnostic>()
        val connectivityParticipants = document.components.filter { component ->
            component.properties.symbolValues("connectivity").contains("enabled")
        }
        val portsByOwner = document.ports.groupBy { it.ownerReference.resolvedIdentity }
        val contracts = connectivityParticipants.map { component ->
            val ownerPorts = portsByOwner[component.id].orEmpty()
            val declaredInterfaces = ownerPorts
                .flatMap { port ->
                    port.properties.symbolValues("interface").map { interfaceId ->
                        EngineeringConnectivityInterfaceId(interfaceId) to port.provenance
                    }
                }
                .distinctBy { (interfaceId, _) -> interfaceId }
            val interfaceIds = declaredInterfaces.map { (interfaceId, _) -> interfaceId }.toSet()
            val ports = ownerPorts.map { port ->
                port.toContract(component.id, interfaceIds.toSet(), diagnostics)
            }
            EngineeringConnectivityContract(
                id = component.id,
                name = component.name,
                kind = component.kind,
                interfaces = declaredInterfaces.map { (interfaceId, provenance) ->
                    EngineeringConnectivityInterfaceContract(interfaceId, provenance)
                },
                ports = ports,
                provenance = component.provenance,
            )
        }

        contracts.flatMap { it.ports }.groupBy { it.id }.filterValues { it.size > 1 }.forEach { (_, ports) ->
            diagnostics += EngineeringConnectivityDiagnostic(
                code = "connectivity.port.duplicate",
                message = "Engineering Connectivity Port '${ports.first().id.value}' is declared more than once.",
                provenance = ports.first().provenance,
            )
        }

        val connectionDiagnostics = validateConnections(document)
        diagnostics += connectionDiagnostics
        val connectivityPortIds = contracts.flatMap { contract -> contract.ports }.map { port -> port.id }.toSet()
        val validConnections = if (connectionDiagnostics.isEmpty()) {
            document.connections.mapNotNull { connection ->
                val endpointContract = connection.toEndpointContract(document.ports)?.takeIf { contract ->
                    contract.first.port.targetId in connectivityPortIds && contract.second.port.targetId in connectivityPortIds
                } ?: return@mapNotNull null
                EngineeringConnectivityConnectionContract(
                    id = connection.id,
                    from = endpointContract.first,
                    to = endpointContract.second,
                    provenance = connection.provenance,
                )
            }
        } else {
            emptyList()
        }
        val networks = buildNetworkContracts(
            document = document,
            connectivityPortIds = connectivityPortIds,
            connectivityConnectionIds = validConnections.map { connection -> connection.id }.toSet(),
            diagnostics = diagnostics,
        )
        val externalEvidence = compileExternalEvidence(
            document = document,
            contracts = contracts,
            connections = validConnections,
            diagnostics = diagnostics,
        )
        val evidenceByContract = externalEvidence.groupBy { evidence -> evidence.contractOwnerId(contracts) }
        val contractsWithEvidence = contracts.map { contract ->
            contract.copy(externalEvidenceReferences = evidenceByContract[contract.id].orEmpty())
        }

        return if (diagnostics.isEmpty()) {
            EngineeringConnectivityCompilation.Success(
                contracts = contractsWithEvidence,
                connections = validConnections,
                networks = networks,
                externalEvidence = externalEvidence,
                provenance = document.system.provenance,
            )
        } else {
            EngineeringConnectivityCompilation.Failure(diagnostics)
        }
    }

    /** Validates authored connections whose owners participate in Engineering Connectivity. */
    fun validateConnections(document: EngineeringDocument): List<EngineeringConnectivityDiagnostic> {
        val connectivityOwners = document.components
            .filter { it.properties.symbolValues("connectivity").contains("enabled") }
            .map { it.id }
            .toSet()
        val portsById = document.ports.associateBy { it.id }

        val connectivityNames = document.components
            .filter { it.id in connectivityOwners }
            .map { it.name }
            .toSet()
        return document.connections.flatMap { connection ->
            val source = connection.from.resolvedIdentity?.let(portsById::get)
            val target = connection.to.resolvedIdentity?.let(portsById::get)
            val participates = listOfNotNull(source, target).any { it.ownerReference.resolvedIdentity in connectivityOwners } ||
                connection.from.authoredPath.firstOrNull() in connectivityNames ||
                connection.to.authoredPath.firstOrNull() in connectivityNames
            if (!participates) emptyList() else validateConnection(connection, source, target, connectivityOwners)
        }
    }

    private fun buildNetworkContracts(
        document: EngineeringDocument,
        connectivityPortIds: Set<StableSemanticIdentity>,
        connectivityConnectionIds: Set<StableSemanticIdentity>,
        diagnostics: MutableList<EngineeringConnectivityDiagnostic>,
    ): List<EngineeringConnectivityNetworkContract> {
        val connectionsById = document.connections.associateBy { it.id }
        val portsById = document.ports.associateBy { it.id }
        return document.connectionNetworks
            .filter { network ->
                network.members.any { member ->
                    member.connectionReference.resolvedIdentity in connectivityConnectionIds ||
                        member.fromPortReference.resolvedIdentity in connectivityPortIds ||
                        member.toPortReference.resolvedIdentity in connectivityPortIds
                }
            }
            .map { network ->
            val members = network.members.mapNotNull { member ->
                val connection = member.connectionReference.resolvedIdentity?.let { resolvedIdentity ->
                    connectionsById[resolvedIdentity]
                }
                if (connection == null) {
                    diagnostics += EngineeringConnectivityDiagnostic(
                        code = "connectivity.network.member.connection.unresolved",
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

                EngineeringConnectivityNetworkMemberContract(
                    connection = member.connectionReference.toConnectivityReference(connection.id),
                    fromPort = member.fromPortReference.toConnectivityReference(fromPort.id),
                    toPort = member.toPortReference.toConnectivityReference(toPort.id),
                )
            }

            val junctionContracts = network.junctions.mapNotNull { junction ->
                val sharedPort = resolvePort(junction.sharedPortReference, portsById, diagnostics, network)
                if (sharedPort == null) {
                    return@mapNotNull null
                }

                val networkConnectionIds = members.map { it.connection.targetId }.toSet()
                val memberConnections = junction.memberConnectionReferences.mapNotNull { connectionReference ->
                    connectionReference.resolvedIdentity?.let { resolvedIdentity ->
                        val connectionId = connectionsById[resolvedIdentity]?.id
                        if (connectionId != null && connectionId !in networkConnectionIds) {
                            diagnostics += EngineeringConnectivityDiagnostic(
                                code = "connectivity.network.junction.member.outside-network",
                                message = "Connection network '${network.name}' junction references a Connection outside the network.",
                                provenance = connectionReference.provenance,
                            )
                        }
                        connectionId
                            ?.takeIf { it in networkConnectionIds }
                            ?.let(connectionReference::toConnectivityReference)
                    }
                }
                if (memberConnections.size < 2) {
                    diagnostics += EngineeringConnectivityDiagnostic(
                        code = "connectivity.network.junction.membership.insufficient",
                        message = "Connection network '${network.name}' declares a junction with fewer than two member Connections.",
                        provenance = junction.provenance,
                    )
                }

                val sharedPortDirection = sharedPort.compatibility().direction
                val memberConnectionSet = memberConnections.map { it.targetId }.toSet()
                val sharedPortUsage = members.filter { it.connection.targetId in memberConnectionSet }.mapNotNull { member ->
                    val fromShared = member.fromPort.targetId.value == sharedPort.id.value
                    val toShared = member.toPort.targetId.value == sharedPort.id.value
                    when {
                        fromShared && toShared -> SharedPortUsage.BOTH
                        fromShared -> SharedPortUsage.SOURCE
                        toShared -> SharedPortUsage.TARGET
                        else -> null
                    }
                }.toSet()
                val allowedDirections = when {
                    sharedPortUsage == setOf(SharedPortUsage.SOURCE) -> setOf(
                        EngineeringConnectivityPortDirection.OUTPUT,
                        EngineeringConnectivityPortDirection.BIDIRECTIONAL,
                        EngineeringConnectivityPortDirection.PASSIVE,
                    )
                    sharedPortUsage == setOf(SharedPortUsage.TARGET) -> setOf(
                        EngineeringConnectivityPortDirection.INPUT,
                        EngineeringConnectivityPortDirection.BIDIRECTIONAL,
                        EngineeringConnectivityPortDirection.PASSIVE,
                    )
                    else -> setOf(EngineeringConnectivityPortDirection.PASSIVE, EngineeringConnectivityPortDirection.BIDIRECTIONAL)
                }
                if (memberConnections.size > 1 && sharedPortDirection !in allowedDirections) {
                    diagnostics += EngineeringConnectivityDiagnostic(
                        code = "connectivity.network.junction.incompatible",
                        message = "Connection network '${network.name}' declares incompatible shared Port direction '${sharedPortDirection}' for multi-member junction '${sharedPort.id.value}'.",
                        provenance = junction.provenance,
                    )
                }

                EngineeringConnectivityNetworkJunctionContract(
                    id = junction.id,
                    sharedPort = junction.sharedPortReference.toConnectivityReference(sharedPort.id),
                    memberConnections = memberConnections,
                    provenance = junction.provenance,
                )
            }

            val compatibilityEvidence = network.compatibilityEvidence.map {
                EngineeringConnectivityNetworkCompatibilityEvidenceContract(it.kind, it.value, it.provenance)
            }

            if (compatibilityEvidence.isEmpty()) {
                diagnostics += EngineeringConnectivityDiagnostic(
                    code = "connectivity.network.compatibility.missing",
                    message = "Connection network '${network.name}' must carry compatibility evidence.",
                    provenance = network.provenance,
                )
            }

            if (members.size > 1 && !membersFormConnectedGraph(members)) {
                diagnostics += EngineeringConnectivityDiagnostic(
                    code = "connectivity.network.members.disconnected",
                    message = "Connection network '${network.name}' contains disconnected member groups.",
                    provenance = network.provenance,
                )
            }

            EngineeringConnectivityNetworkContract(
                id = network.id,
                name = network.name,
                members = members,
                junctions = junctionContracts,
                compatibilityEvidence = compatibilityEvidence,
                provenance = network.provenance,
            )
            }
    }

    private fun compileExternalEvidence(
        document: EngineeringDocument,
        contracts: List<EngineeringConnectivityContract>,
        connections: List<EngineeringConnectivityConnectionContract>,
        diagnostics: MutableList<EngineeringConnectivityDiagnostic>,
    ): List<EngineeringConnectivityEvidenceReference> {
        val componentByName = document.components.associateBy { component -> component.name }
        val contractByComponentId = contracts.associateBy { contract -> contract.id }
        val portsByOwnerAndName = document.ports.associateBy { port ->
            val ownerName = port.ownerReference.resolvedIdentity
                ?.let(contractByComponentId::get)
                ?.name
                ?: port.ownerReference.authoredPath.singleOrNull().orEmpty()
            ownerName to port.name
        }
        val interfacesByOwnerAndName = contracts
            .flatMap { contract -> contract.interfaces.map { connectivityInterface -> contract.name to connectivityInterface.id.value } }
            .toSet()
        val connectionByAlias = connections.associateBy { connection -> connection.id.value.substringAfterLast(':') }
        val seen = mutableSetOf<List<String>>()
        return document.externalEvidence.mapNotNull { evidence ->
            val namespace = evidence.namespace.toEvidenceNamespace()
            if (namespace == null) {
                diagnostics += EngineeringConnectivityDiagnostic(
                    code = "connectivity.evidence.namespace.unknown",
                    message = "External Evidence '${evidence.name}' declares unsupported namespace '${evidence.namespace}'.",
                    provenance = evidence.provenance,
                )
                return@mapNotNull null
            }
            if (!namespace.accepts(evidence.reference)) {
                diagnostics += EngineeringConnectivityDiagnostic(
                    code = "connectivity.evidence.reference.invalid",
                    message = "External Evidence '${evidence.name}' declares invalid ${namespace.sourceName} reference '${evidence.reference}'.",
                    provenance = evidence.provenance,
                )
                return@mapNotNull null
            }
            val subject = evidence.resolveSubject(componentByName, interfacesByOwnerAndName, portsByOwnerAndName, connectionByAlias)
            if (subject == null) {
                diagnostics += EngineeringConnectivityDiagnostic(
                    code = "connectivity.evidence.subject.invalid",
                    message = "External Evidence '${evidence.name}' references an unknown or invalid ${evidence.subject.kind.name.lowercase()} subject '${evidence.subject.authoredPath.joinToString(".")}'.",
                    provenance = evidence.provenance,
                )
                return@mapNotNull null
            }
            val key = listOf(
                subject.kind.name,
                subject.authoredPath.joinToString("."),
                namespace.name,
                evidence.reference,
            )
            if (!seen.add(key)) {
                diagnostics += EngineeringConnectivityDiagnostic(
                    code = "connectivity.evidence.duplicate",
                    message = "External Evidence '${evidence.name}' duplicates an existing mapping for '${subject.authoredPath.joinToString(".")}'.",
                    provenance = evidence.provenance,
                )
                return@mapNotNull null
            }
            EngineeringConnectivityEvidenceReference(
                namespace = namespace,
                reference = EngineeringConnectivityEvidenceValue(evidence.reference),
                subject = subject,
                externalProvenance = evidence.externalProvenance,
                provenance = evidence.provenance,
            )
        }
    }

    private fun resolvePort(
        reference: EngineeringReference,
        portsById: Map<StableSemanticIdentity, EngineeringPort>,
        diagnostics: MutableList<EngineeringConnectivityDiagnostic>,
        network: EngineeringConnectionNetwork,
    ): EngineeringPort? {
        val resolvedIdentity = reference.resolvedIdentity
        if (resolvedIdentity == null) {
            diagnostics += EngineeringConnectivityDiagnostic(
                code = "connectivity.network.port.unresolved",
                message = "Connection network '${network.name}' references an unresolved Port.",
                provenance = reference.provenance,
            )
            return null
        }
        return portsById[resolvedIdentity] ?: run {
            diagnostics += EngineeringConnectivityDiagnostic(
                code = "connectivity.network.port.unresolved",
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
        connectivityOwners: Set<StableSemanticIdentity>,
    ): List<EngineeringConnectivityDiagnostic> {
        if (source == null || target == null) {
            return buildList {
                if (source == null) add(unresolvedEndpointDiagnostic(connection, connection.from))
                if (target == null) add(unresolvedEndpointDiagnostic(connection, connection.to))
            }
        }
        if (source.id == target.id) {
            return endpointDiagnostics("connectivity.connection.self", "cannot connect a Port to itself", source, target)
        }
        if (source.ownerReference.resolvedIdentity !in connectivityOwners || target.ownerReference.resolvedIdentity !in connectivityOwners) {
            return endpointDiagnostics("connectivity.connection.endpoint.untyped", "requires two Engineering Connectivity Ports", source, target)
        }

        val sourceCompatibility = source.compatibility()
        val targetCompatibility = target.compatibility()
        if (!sourceCompatibility.direction.allowsSource() || !targetCompatibility.direction.allowsTarget()) {
            return endpointDiagnostics("connectivity.connection.direction.incompatible", "has incompatible Port directions", source, target)
        }
        if (sourceCompatibility.signal != null && targetCompatibility.signal != null && sourceCompatibility.signal != targetCompatibility.signal) {
            return endpointDiagnostics("connectivity.connection.signal.incompatible", "has incompatible signal kinds", source, target)
        }
        if (sourceCompatibility.role != null && targetCompatibility.role != null && sourceCompatibility.role != targetCompatibility.role) {
            return endpointDiagnostics("connectivity.connection.role.incompatible", "has incompatible Port roles", source, target)
        }
        val incompatibleParameter = sourceCompatibility.parameters.keys.intersect(targetCompatibility.parameters.keys)
            .firstOrNull { key -> sourceCompatibility.parameters[key] != targetCompatibility.parameters[key] }
        return if (incompatibleParameter == null) emptyList() else endpointDiagnostics(
            "connectivity.connection.parameter.incompatible",
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
        EngineeringConnectivityDiagnostic(code, "Connection $reason: '${source.id.value}' -> '${target.id.value}'.", source.provenance),
        EngineeringConnectivityDiagnostic(code, "Connection $reason: '${source.id.value}' -> '${target.id.value}'.", target.provenance),
    )

    private fun EngineeringPort.toContract(
        ownerId: StableSemanticIdentity,
        declaredInterfaces: Set<EngineeringConnectivityInterfaceId>,
        diagnostics: MutableList<EngineeringConnectivityDiagnostic>,
    ): EngineeringConnectivityPortContract {
        val values = properties.symbolValuesByName()
        val directionValue = values["direction"]?.singleOrNull()
        val direction = directionValue.toConnectivityDirection()
        if (directionValue.isNullOrBlank()) {
            diagnostics += EngineeringConnectivityDiagnostic(
                code = "connectivity.port.direction.missing",
                message = "Engineering Connectivity Port '$name' must declare a direction.",
                provenance = provenance,
            )
        } else if (direction == null) {
            diagnostics += EngineeringConnectivityDiagnostic(
                code = "connectivity.port.direction.invalid",
                message = "Engineering Connectivity Port '$name' declares unsupported direction '$directionValue'.",
                provenance = provenance,
            )
        }
        val multiplicityValue = values["multiplicity"]?.singleOrNull() ?: "single"
	        val multiplicity = multiplicityValue.toConnectivityMultiplicity()
	        if (multiplicity == null) diagnostics += EngineeringConnectivityDiagnostic(
	            code = "connectivity.port.multiplicity.invalid",
	            message = "Engineering Connectivity Port '$name' declares unsupported multiplicity '$multiplicityValue'.",
	            provenance = provenance,
	        )
	        val ownerValue = values["owner"]?.singleOrNull()
	        val owner = ownerValue.toConnectivityConstraintOwner()
	        if (ownerValue != null && owner == null) diagnostics += EngineeringConnectivityDiagnostic(
	            code = "connectivity.port.owner.invalid",
	            message = "Engineering Connectivity Port '$name' declares unsupported constraint owner '$ownerValue'.",
	            provenance = provenance,
	        )
	        val strengthValue = values["strength"]?.singleOrNull()
	        val strength = strengthValue.toConnectivityConstraintStrength()
	        if (strengthValue != null && strength == null) diagnostics += EngineeringConnectivityDiagnostic(
	            code = "connectivity.port.strength.invalid",
	            message = "Engineering Connectivity Port '$name' declares unsupported constraint strength '$strengthValue'.",
	            provenance = provenance,
	        )
	        val interfaceIds = values["interface"].orEmpty().map(::EngineeringConnectivityInterfaceId)
        interfaceIds.filterNot { it in declaredInterfaces }.forEach { interfaceId ->
            diagnostics += EngineeringConnectivityDiagnostic(
                code = "connectivity.port.interface.unknown",
                message = "Engineering Connectivity Port '$name' references unknown interface '${interfaceId.value}'.",
                provenance = provenance,
            )
        }
        val compatibilityValues = values.filterKeys { it.startsWith("compatibility.") }
        compatibilityValues.filterValues { it.size != 1 }.forEach { (key, _) ->
            diagnostics += EngineeringConnectivityDiagnostic(
                code = "connectivity.port.compatibility.duplicate",
                message = "Engineering Connectivity Port '$name' declares compatibility parameter '$key' more than once.",
                provenance = provenance,
            )
        }
        return EngineeringConnectivityPortContract(
            id = id,
            name = name,
            ownerId = ownerId,
            interfaceIds = interfaceIds,
            compatibility = EngineeringConnectivityPortCompatibility(
                direction = direction ?: EngineeringConnectivityPortDirection.PASSIVE,
                multiplicity = multiplicity ?: EngineeringConnectivityPortMultiplicity.SINGLE,
	                signalKind = values["signal"]?.singleOrNull(),
	                role = values["role"]?.singleOrNull(),
	                parameters = compatibilityValues
	                    .filterValues { it.size == 1 }
	                    .mapKeys { (key, _) -> key.removePrefix("compatibility.") }
	                    .mapValues { (_, value) -> value.single() },
	                owner = owner ?: EngineeringConnectivityConstraintOwner.SEMANTIC,
	                strength = strength ?: EngineeringConnectivityConstraintStrength.REQUIRED,
	            ),
            provenance = provenance,
        )
    }
}

private fun String.toEvidenceNamespace(): EngineeringConnectivityEvidenceNamespace? =
    EngineeringConnectivityEvidenceNamespace.entries.firstOrNull { namespace ->
        namespace.sourceName == lowercase()
    }

private fun EngineeringConnectivityEvidenceNamespace.accepts(reference: String): Boolean =
    when (this) {
        EngineeringConnectivityEvidenceNamespace.IEC -> reference.startsWith("IEC:")
        EngineeringConnectivityEvidenceNamespace.CLASSIFICATION -> reference.startsWith("neutral:")
    }

private fun EngineeringExternalEvidenceMapping.resolveSubject(
    componentByName: Map<String, com.engineeringood.athena.ir.EngineeringComponent>,
    interfacesByOwnerAndName: Set<Pair<String, String>>,
    portsByOwnerAndName: Map<Pair<String, String>, EngineeringPort>,
    connectionByAlias: Map<String, EngineeringConnectivityConnectionContract>,
): EngineeringConnectivityEvidenceSubject? {
    val path = subject.authoredPath
    return when (subject.kind) {
        EngineeringExternalEvidenceSubjectKind.CONTRACT -> {
            if (path.size != 1) return null
            val component = componentByName[path[0]] ?: return null
            EngineeringConnectivityEvidenceSubject(
                kind = EngineeringConnectivityEvidenceSubjectKind.CONTRACT,
                authoredPath = path,
                targetId = component.id,
            )
        }
        EngineeringExternalEvidenceSubjectKind.INTERFACE -> {
            if (path.size != 2) return null
            val owner = componentByName[path[0]] ?: return null
            if ((path[0] to path[1]) !in interfacesByOwnerAndName) return null
            EngineeringConnectivityEvidenceSubject(
                kind = EngineeringConnectivityEvidenceSubjectKind.INTERFACE,
                authoredPath = path,
                targetId = StableSemanticIdentity("interface:${owner.name}.${path[1]}"),
            )
        }
        EngineeringExternalEvidenceSubjectKind.PORT -> {
            if (path.size != 2) return null
            val port = portsByOwnerAndName[path[0] to path[1]] ?: return null
            EngineeringConnectivityEvidenceSubject(
                kind = EngineeringConnectivityEvidenceSubjectKind.PORT,
                authoredPath = path,
                targetId = port.id,
            )
        }
        EngineeringExternalEvidenceSubjectKind.RELATION_CONTRACT -> {
            if (path.size != 1) return null
            val connection = connectionByAlias[path[0]] ?: return null
            EngineeringConnectivityEvidenceSubject(
                kind = EngineeringConnectivityEvidenceSubjectKind.RELATION_CONTRACT,
                authoredPath = path,
                targetId = connection.id,
            )
        }
        EngineeringExternalEvidenceSubjectKind.ROUTE_POLICY -> {
            if (path.size != 1) return null
            EngineeringConnectivityEvidenceSubject(
                kind = EngineeringConnectivityEvidenceSubjectKind.ROUTE_POLICY,
                authoredPath = path,
                targetId = null,
            )
        }
    }
}

private fun EngineeringConnectivityEvidenceReference.contractOwnerId(
    contracts: List<EngineeringConnectivityContract>,
): StableSemanticIdentity? {
    if (subject.kind == EngineeringConnectivityEvidenceSubjectKind.CONTRACT) return subject.targetId
    val ownerName = subject.authoredPath.firstOrNull() ?: return null
    return contracts.firstOrNull { contract -> contract.name == ownerName }?.id
}

private fun EngineeringConnection.toEndpointContract(
    ports: List<EngineeringPort>,
): Pair<EngineeringConnectivityConnectionEndpointContract, EngineeringConnectivityConnectionEndpointContract>? {
    val portsById = ports.associateBy { it.id }
    val fromPort = from.resolvedIdentity?.let(portsById::get) ?: return null
    val toPort = to.resolvedIdentity?.let(portsById::get) ?: return null
    return Pair(
        EngineeringConnectivityConnectionEndpointContract(from.toConnectivityReference(fromPort.id)),
        EngineeringConnectivityConnectionEndpointContract(to.toConnectivityReference(toPort.id)),
    )
}

private fun EngineeringReference.toConnectivityReference(
    targetId: StableSemanticIdentity,
): EngineeringConnectivityReferenceContract = EngineeringConnectivityReferenceContract(
    authoredPath = authoredPath,
    targetId = targetId,
    provenance = provenance,
)

private fun unresolvedEndpointDiagnostic(
    connection: EngineeringConnection,
    reference: EngineeringReference,
) = EngineeringConnectivityDiagnostic(
    code = "connectivity.connection.endpoint.unresolved",
    message = "Engineering connectivity connection '${connection.id.value}' references unresolved Port '${reference.authoredPath.joinToString(".")}'.",
    provenance = reference.provenance,
)

private fun membersFormConnectedGraph(members: List<EngineeringConnectivityNetworkMemberContract>): Boolean {
    if (members.size < 2) return true
    val remaining = members.toMutableList()
    val connectedPorts = mutableSetOf<StableSemanticIdentity>()
    val first = remaining.removeAt(0)
    connectedPorts += first.fromPort.targetId
    connectedPorts += first.toPort.targetId
    var advanced: Boolean
    do {
        advanced = false
        val iterator = remaining.iterator()
        while (iterator.hasNext()) {
            val member = iterator.next()
            if (member.fromPort.targetId in connectedPorts || member.toPort.targetId in connectedPorts) {
                connectedPorts += member.fromPort.targetId
                connectedPorts += member.toPort.targetId
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

private data class PortCompatibilityValues(
    val direction: EngineeringConnectivityPortDirection?,
    val signal: String?,
    val role: String?,
    val parameters: Map<String, String>,
)

private fun EngineeringPort.compatibility(): PortCompatibilityValues {
    val values = properties.symbolValuesByName()
    return PortCompatibilityValues(
        direction = values["direction"]?.singleOrNull().toConnectivityDirection(),
        signal = values["signal"]?.singleOrNull(),
        role = values["role"]?.singleOrNull(),
        parameters = values.filterKeys { it.startsWith("compatibility.") }.mapValues { (_, value) -> value.singleOrNull().orEmpty() },
    )
}

private fun EngineeringConnectivityPortDirection?.allowsSource(): Boolean =
    this in setOf(EngineeringConnectivityPortDirection.OUTPUT, EngineeringConnectivityPortDirection.BIDIRECTIONAL, EngineeringConnectivityPortDirection.PASSIVE)

private fun EngineeringConnectivityPortDirection?.allowsTarget(): Boolean =
    this in setOf(EngineeringConnectivityPortDirection.INPUT, EngineeringConnectivityPortDirection.BIDIRECTIONAL, EngineeringConnectivityPortDirection.PASSIVE)

private fun String?.toConnectivityDirection(): EngineeringConnectivityPortDirection? = when (this?.lowercase()) {
    "in", "input" -> EngineeringConnectivityPortDirection.INPUT
    "out", "output" -> EngineeringConnectivityPortDirection.OUTPUT
    "bidirectional" -> EngineeringConnectivityPortDirection.BIDIRECTIONAL
    "passive" -> EngineeringConnectivityPortDirection.PASSIVE
    else -> null
}

private fun String.toConnectivityMultiplicity(): EngineeringConnectivityPortMultiplicity? = when (lowercase()) {
    "single" -> EngineeringConnectivityPortMultiplicity.SINGLE
    "multiple", "many" -> EngineeringConnectivityPortMultiplicity.MULTIPLE
    else -> null
}

private fun String?.toConnectivityConstraintOwner(): EngineeringConnectivityConstraintOwner? = when (this?.lowercase()) {
    "semantic" -> EngineeringConnectivityConstraintOwner.SEMANTIC
    "representation" -> EngineeringConnectivityConstraintOwner.REPRESENTATION
    "physical" -> EngineeringConnectivityConstraintOwner.PHYSICAL
    "layout_preference" -> EngineeringConnectivityConstraintOwner.LAYOUT_PREFERENCE
    else -> null
}

private fun String?.toConnectivityConstraintStrength(): EngineeringConnectivityConstraintStrength? = when (this?.lowercase()) {
    "required" -> EngineeringConnectivityConstraintStrength.REQUIRED
    "preferred" -> EngineeringConnectivityConstraintStrength.PREFERRED
    "optional" -> EngineeringConnectivityConstraintStrength.OPTIONAL
    else -> null
}

private fun List<EngineeringProperty>.symbolValues(name: String): List<String> =
    filter { it.name == name }.mapNotNull { (it.value as? EngineeringPropertyValue.Symbol)?.text }

private fun List<EngineeringProperty>.symbolValuesByName(): Map<String, List<String>> =
    groupBy { it.name }.mapValues { (_, properties) ->
        properties.mapNotNull { (it.value as? EngineeringPropertyValue.Symbol)?.text }
    }
