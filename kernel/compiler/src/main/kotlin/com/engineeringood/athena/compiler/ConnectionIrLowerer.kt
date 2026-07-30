package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectableEntityContractCompilation
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringConnectionNetwork
import com.engineeringood.athena.ir.EngineeringConnectionNetworkMember
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringNetworkCompatibilityEvidence
import com.engineeringood.athena.ir.EngineeringNetworkJunction
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringReference

/** Lowers validated canonical engineering connectivity into disposable Connection IR. */
class ConnectionIrLowerer {
    fun lower(
        document: EngineeringDocument,
        contracts: ConnectableEntityContractCompilation.Success,
        snapshot: ConnectionIrSnapshot,
    ): ConnectionIr {
        val contractByPortId = contracts.entities.flatMap { it.ports }.associateBy { it.id }
        val connectionIds = contracts.connections.map { it.id }.toSet()
        return ConnectionIr(
            entities = contracts.entities.map { component ->
                ConnectionIrEntity(
                    id = component.id,
                    name = component.name,
                    kind = component.kind,
                    provenance = component.provenance,
                )
            },
            ports = document.ports.filter { it.id in contractByPortId }.map { port ->
                val contract = requireNotNull(contractByPortId[port.id]) {
                    "Validated Connection IR cannot contain an untyped Port '${port.id.value}'."
                }
                ConnectionIrPort(
                    id = port.id,
                    ownerId = requireNotNull(port.ownerReference.resolvedIdentity) {
                        "Validated Connection IR cannot contain unresolved Port owner '${port.id.value}'."
                    },
                    name = port.name,
                    interfaceIds = contract.interfaceIds,
                    compatibility = ConnectionIrPortCompatibility(
                        direction = contract.compatibility.direction,
                        multiplicity = contract.compatibility.multiplicity,
                        signalKind = contract.compatibility.signalKind,
                        role = contract.compatibility.role,
                        parameters = contract.compatibility.parameters,
                        owner = contract.compatibility.owner.toConnectionIrOwner(),
                        strength = contract.compatibility.strength.toConnectionIrStrength(),
                    ),
                    provenance = port.provenance,
                )
            },
            connections = document.connections.filter { it.id in connectionIds }.map { connection ->
                ConnectionIrConnection(
                    id = connection.id,
                    from = connection.from.toConnectionIrReference(),
                    to = connection.to.toConnectionIrReference(),
                    provenance = connection.provenance,
                )
            },
            networks = document.connectionNetworks.map { network ->
                ConnectionIrNetwork(
                    id = network.id,
                    name = network.name,
                    members = network.members.map { it.toConnectionIrMember() },
                    junctions = network.junctions.map { it.toConnectionIrJunction() },
                    compatibilityEvidence = network.compatibilityEvidence.map { it.toConnectionIrEvidence() },
                    provenance = network.provenance,
                )
            },
            provenance = document.system.provenance,
            snapshot = snapshot,
        )
    }
}

private fun EngineeringReference.toConnectionIrReference(): ConnectionIrReference =
    ConnectionIrReference(
        authoredPath = authoredPath,
        resolvedIdentity = requireNotNull(resolvedIdentity) {
            "Validated Connection IR cannot contain unresolved reference '${authoredPath.joinToString(".")}'."
        },
        provenance = provenance,
    )

private fun EngineeringConnectionNetworkMember.toConnectionIrMember(): ConnectionIrNetworkMember =
    ConnectionIrNetworkMember(
        connectionReference = connectionReference.toConnectionIrReference(),
        fromPortReference = fromPortReference.toConnectionIrReference(),
        toPortReference = toPortReference.toConnectionIrReference(),
    )

private fun EngineeringNetworkJunction.toConnectionIrJunction(): ConnectionIrNetworkJunction =
    ConnectionIrNetworkJunction(
        id = id,
        sharedPortReference = sharedPortReference.toConnectionIrReference(),
        memberConnectionReferences = memberConnectionReferences.map(EngineeringReference::toConnectionIrReference),
        provenance = provenance,
    )

private fun EngineeringNetworkCompatibilityEvidence.toConnectionIrEvidence(): ConnectionIrCompatibilityEvidence =
    ConnectionIrCompatibilityEvidence(
        kind = kind,
        value = value,
        owner = ConnectionIrConstraintOwner.SEMANTIC,
        strength = ConnectionIrConstraintStrength.REQUIRED,
        provenance = provenance,
    )
