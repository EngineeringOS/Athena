package com.engineeringood.athena.ir

/** Role of one Port within an authored Engineering Connection. Independent from Port direction. */
enum class ConnectionEndpointRole {
    SOURCE,
    SINK,
    PASS,
}

/** Closed electrical connection vocabulary admitted by M46 source semantics. */
enum class ConnectionKind {
    CONDUCTOR,
    WIRE,
    CABLE_CORE,
    JUMPER,
    BUSBAR,
    SIGNAL,
}

/** Source-owned multi-endpoint connectivity equivalence. Never flattened into binary connections. */
data class EngineeringNet(
    val id: StableSemanticIdentity,
    val name: String,
    val kind: ConnectionKind,
    val endpoints: List<ConnectionEndpoint>,
    val potentialOrSignal: EngineeringReference?,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
    val effectiveProperties: List<EngineeringProperty> = emptyList(),
) {
    init {
        require(id.value.isNotBlank()) { "Engineering Net requires a nonblank identity" }
        require(name.isNotBlank()) { "Engineering Net requires a nonblank name" }
        require(endpoints.size >= 2) { "Engineering Net requires at least two endpoints" }
        require(endpoints.map { it.port.authoredPath }.distinct().size == endpoints.size) {
            "Engineering Net endpoints must reference distinct Port paths"
        }
    }
}

enum class ConnectionSpecificationScope { PROJECT, POTENTIAL, SIGNAL, NET, CONNECTION }

data class EngineeringConnectionSpecification(
    val id: StableSemanticIdentity,
    val scope: ConnectionSpecificationScope,
    val subject: EngineeringReference?,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

/** One source-owned Port endpoint in a binary Engineering Connection. */
data class ConnectionEndpoint(
    val port: EngineeringReference,
    val role: ConnectionEndpointRole,
    val provenance: SourceProvenance,
) {
    init {
        require(port.authoredPath.isNotEmpty()) { "Connection Endpoint requires an authored Port path" }
        require(port.authoredPath.all { it.isNotBlank() }) { "Connection Endpoint requires nonblank authored Port path segments" }
    }
}

/** Source-owned engineering connectivity fact between exactly two Ports. */
data class EngineeringConnection(
    val id: StableSemanticIdentity,
    val kind: ConnectionKind,
    val endpoints: List<ConnectionEndpoint>,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
    val effectiveProperties: List<EngineeringProperty> = emptyList(),
) {
    init {
        require(id.value.isNotBlank()) { "Engineering Connection requires a nonblank identity" }
        require(endpoints.size == 2) { "Engineering Connection requires exactly two endpoints" }
        require(endpoints.map { it.role }.distinct().size == endpoints.size) {
            "Engineering Connection endpoint roles must be unique"
        }
        require(endpoints.map { it.port.authoredPath }.distinct().size == endpoints.size) {
            "Engineering Connection endpoints must reference distinct Port paths"
        }
    }
}
