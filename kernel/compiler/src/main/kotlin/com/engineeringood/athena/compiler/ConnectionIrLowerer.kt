package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.EngineeringConnectivityCompilation
import com.engineeringood.athena.connection.EngineeringConnectivityEvidenceReference
import com.engineeringood.athena.connection.EngineeringConnectivityPhysicalReference
import com.engineeringood.athena.connection.EngineeringConnectivityReferenceContract
import com.engineeringood.athena.connection.EngineeringConnectivityRepresentationReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Lowers validated canonical engineering connectivity into disposable Connection IR. */
class ConnectionIrLowerer {
    fun lower(
        connectivity: EngineeringConnectivityCompilation.Success,
        snapshot: ConnectionIrSnapshot,
    ): ConnectionIr {
        return ConnectionIr(
            entities = connectivity.contracts.map { component ->
                ConnectionIrEntity(
                    id = component.id,
                    name = component.name,
                    kind = component.kind,
                    provenance = component.provenance,
                    physicalInstallationReferences = component.physicalInstallationReferences.map { it.toConnectionIrReference() },
                    representationBindings = component.representationBindings.map { it.toConnectionIrReference() },
                    externalEvidenceReferences = component.externalEvidenceReferences.map { it.toConnectionIrReference() },
                )
            },
            ports = connectivity.contracts.flatMap { component -> component.ports }.map { port ->
                ConnectionIrPort(
                    id = port.id,
                    ownerId = port.ownerId,
                    name = port.name,
                    interfaceIds = port.interfaceIds,
                    compatibility = ConnectionIrPortCompatibility(
                        direction = port.compatibility.direction,
                        multiplicity = port.compatibility.multiplicity,
                        signalKind = port.compatibility.signalKind,
                        role = port.compatibility.role,
                        parameters = port.compatibility.parameters,
                        owner = port.compatibility.owner.toConnectionIrOwner(),
                        strength = port.compatibility.strength.toConnectionIrStrength(),
                    ),
                    provenance = port.provenance,
                )
            },
            connections = connectivity.connections.map { connection ->
                ConnectionIrConnection(
                    id = connection.id,
                    from = connection.from.port.toConnectionIrReference(),
                    to = connection.to.port.toConnectionIrReference(),
                    provenance = connection.provenance,
                )
            },
            networks = connectivity.networks.map { network ->
                ConnectionIrNetwork(
                    id = network.id,
                    name = network.name,
                    members = network.members.map { member ->
                        ConnectionIrNetworkMember(
                            connectionReference = member.connection.toConnectionIrReference(),
                            fromPortReference = member.fromPort.toConnectionIrReference(),
                            toPortReference = member.toPort.toConnectionIrReference(),
                        )
                    },
                    junctions = network.junctions.map { junction ->
                        ConnectionIrNetworkJunction(
                            id = junction.id,
                            sharedPortReference = junction.sharedPort.toConnectionIrReference(),
                            memberConnectionReferences = junction.memberConnections.map { it.toConnectionIrReference() },
                            provenance = junction.provenance,
                        )
                    },
                    compatibilityEvidence = network.compatibilityEvidence.map { evidence ->
                        ConnectionIrCompatibilityEvidence(
                            kind = evidence.kind,
                            value = evidence.value,
                            owner = ConnectionIrConstraintOwner.SEMANTIC,
                            strength = ConnectionIrConstraintStrength.REQUIRED,
                            provenance = evidence.provenance,
                        )
                    },
                    provenance = network.provenance,
                )
            },
            provenance = connectivity.provenance,
            snapshot = snapshot,
        )
    }
}

private fun EngineeringConnectivityReferenceContract.toConnectionIrReference(): ConnectionIrReference =
    connectionIrReference(authoredPath, targetId, provenance)

private fun EngineeringConnectivityPhysicalReference.toConnectionIrReference(): ConnectionIrReference =
    connectionIrReference(authoredPath, targetId, provenance)

private fun EngineeringConnectivityRepresentationReference.toConnectionIrReference(): ConnectionIrReference =
    connectionIrReference(authoredPath, targetId, provenance)

private fun EngineeringConnectivityEvidenceReference.toConnectionIrReference(): ConnectionIrReference =
    connectionIrReference(
        subject.authoredPath,
        requireNotNull(subject.targetId) { "Entity-scoped evidence must resolve to a target identity." },
        provenance,
    )

private fun connectionIrReference(
    authoredPath: List<String>,
    targetId: StableSemanticIdentity,
    provenance: SourceProvenance,
): ConnectionIrReference = ConnectionIrReference(authoredPath, targetId, provenance)
