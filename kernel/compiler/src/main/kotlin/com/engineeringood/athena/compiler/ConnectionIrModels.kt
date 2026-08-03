package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.EngineeringConnectivityConstraintOwner
import com.engineeringood.athena.connection.EngineeringConnectivityConstraintStrength
import com.engineeringood.athena.connection.EngineeringConnectivityInterfaceId
import com.engineeringood.athena.connection.EngineeringConnectivityPortDirection
import com.engineeringood.athena.connection.EngineeringConnectivityPortMultiplicity
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
    val physicalInstallationReferences: List<ConnectionIrReference> = emptyList(),
    val representationBindings: List<ConnectionIrReference> = emptyList(),
    val externalEvidenceReferences: List<ConnectionIrReference> = emptyList(),
)

data class ConnectionIrPort(
    val id: StableSemanticIdentity,
    val ownerId: StableSemanticIdentity,
    val name: String,
    val interfaceIds: List<EngineeringConnectivityInterfaceId> = emptyList(),
    val compatibility: ConnectionIrPortCompatibility,
    val provenance: SourceProvenance,
)

data class ConnectionIrPortCompatibility(
    val direction: EngineeringConnectivityPortDirection,
    val multiplicity: EngineeringConnectivityPortMultiplicity = EngineeringConnectivityPortMultiplicity.SINGLE,
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

internal fun EngineeringConnectivityConstraintOwner.toConnectionIrOwner(): ConnectionIrConstraintOwner = when (this) {
    EngineeringConnectivityConstraintOwner.SEMANTIC -> ConnectionIrConstraintOwner.SEMANTIC
    EngineeringConnectivityConstraintOwner.REPRESENTATION -> ConnectionIrConstraintOwner.REPRESENTATION
    EngineeringConnectivityConstraintOwner.PHYSICAL -> ConnectionIrConstraintOwner.PHYSICAL
    EngineeringConnectivityConstraintOwner.LAYOUT_PREFERENCE -> ConnectionIrConstraintOwner.LAYOUT_PREFERENCE
}

enum class ConnectionIrConstraintStrength {
    REQUIRED,
    PREFERRED,
    OPTIONAL,
}

internal fun EngineeringConnectivityConstraintStrength.toConnectionIrStrength(): ConnectionIrConstraintStrength = when (this) {
    EngineeringConnectivityConstraintStrength.REQUIRED -> ConnectionIrConstraintStrength.REQUIRED
    EngineeringConnectivityConstraintStrength.PREFERRED -> ConnectionIrConstraintStrength.PREFERRED
    EngineeringConnectivityConstraintStrength.OPTIONAL -> ConnectionIrConstraintStrength.OPTIONAL
}
