package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectableConstraintOwner
import com.engineeringood.athena.connection.ConnectableConstraintStrength
import com.engineeringood.athena.connection.ConnectableInterfaceId
import com.engineeringood.athena.connection.ConnectablePortDirection
import com.engineeringood.athena.connection.ConnectablePortMultiplicity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Compiler-owned transient connectivity IR derived from validated engineering facts. */
data class ConnectionIr(
    val entities: List<ConnectionIrEntity>,
    val ports: List<ConnectionIrPort>,
    val connections: List<ConnectionIrConnection>,
    val networks: List<ConnectionIrNetwork>,
    val provenance: SourceProvenance,
    val snapshot: ConnectionIrSnapshot = ConnectionIrSnapshot("", "", "athena.compiler.connection-ir"),
)

data class ConnectionIrSnapshot(
    val semanticSnapshotId: String,
    val packageSnapshotId: String,
    val compilerIdentity: String,
)

data class ConnectionIrEntity(
    val id: StableSemanticIdentity,
    val name: String,
    val kind: String,
    val provenance: SourceProvenance,
)

data class ConnectionIrPort(
    val id: StableSemanticIdentity,
    val ownerId: StableSemanticIdentity,
    val name: String,
    val interfaceIds: List<ConnectableInterfaceId> = emptyList(),
    val compatibility: ConnectionIrPortCompatibility,
    val provenance: SourceProvenance,
)

data class ConnectionIrPortCompatibility(
    val direction: ConnectablePortDirection,
    val multiplicity: ConnectablePortMultiplicity = ConnectablePortMultiplicity.SINGLE,
    val signalKind: String?,
    val role: String?,
    val parameters: Map<String, String> = emptyMap(),
    val owner: ConnectionIrConstraintOwner = ConnectionIrConstraintOwner.SEMANTIC,
    val strength: ConnectionIrConstraintStrength = ConnectionIrConstraintStrength.REQUIRED,
)

data class ConnectionIrConnection(
    val id: StableSemanticIdentity,
    val from: ConnectionIrReference,
    val to: ConnectionIrReference,
    val provenance: SourceProvenance,
)

data class ConnectionIrNetwork(
    val id: StableSemanticIdentity,
    val name: String,
    val members: List<ConnectionIrNetworkMember>,
    val junctions: List<ConnectionIrNetworkJunction>,
    val compatibilityEvidence: List<ConnectionIrCompatibilityEvidence>,
    val provenance: SourceProvenance,
)

data class ConnectionIrNetworkMember(
    val connectionReference: ConnectionIrReference,
    val fromPortReference: ConnectionIrReference,
    val toPortReference: ConnectionIrReference,
)

data class ConnectionIrNetworkJunction(
    val id: StableSemanticIdentity,
    val sharedPortReference: ConnectionIrReference,
    val memberConnectionReferences: List<ConnectionIrReference>,
    val provenance: SourceProvenance,
)

data class ConnectionIrCompatibilityEvidence(
    val kind: String,
    val value: String,
    val owner: ConnectionIrConstraintOwner,
    val strength: ConnectionIrConstraintStrength,
    val provenance: SourceProvenance,
)

data class ConnectionIrReference(
    val authoredPath: List<String>,
    val resolvedIdentity: StableSemanticIdentity,
    val provenance: SourceProvenance,
)

enum class ConnectionIrConstraintOwner {
    SEMANTIC,
    REPRESENTATION,
    PHYSICAL,
    LAYOUT_PREFERENCE,
}

internal fun ConnectableConstraintOwner.toConnectionIrOwner(): ConnectionIrConstraintOwner = when (this) {
    ConnectableConstraintOwner.SEMANTIC -> ConnectionIrConstraintOwner.SEMANTIC
    ConnectableConstraintOwner.REPRESENTATION -> ConnectionIrConstraintOwner.REPRESENTATION
    ConnectableConstraintOwner.PHYSICAL -> ConnectionIrConstraintOwner.PHYSICAL
    ConnectableConstraintOwner.LAYOUT_PREFERENCE -> ConnectionIrConstraintOwner.LAYOUT_PREFERENCE
}

enum class ConnectionIrConstraintStrength {
    REQUIRED,
    PREFERRED,
    OPTIONAL,
}

internal fun ConnectableConstraintStrength.toConnectionIrStrength(): ConnectionIrConstraintStrength = when (this) {
    ConnectableConstraintStrength.REQUIRED -> ConnectionIrConstraintStrength.REQUIRED
    ConnectableConstraintStrength.PREFERRED -> ConnectionIrConstraintStrength.PREFERRED
    ConnectableConstraintStrength.OPTIONAL -> ConnectionIrConstraintStrength.OPTIONAL
}
