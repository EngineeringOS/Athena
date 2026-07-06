package com.engineeringood.athena.compiler.boundary

import java.nio.file.Path

/** Compiler-owned source model listing the local external boundary descriptor roots for one compilation run. */
data class AthenaBoundaryDescriptorSource(
    val descriptorRoots: List<Path>,
) {
    companion object {
        /** Returns an empty boundary descriptor source when no local boundary descriptors are configured. */
        fun empty(): AthenaBoundaryDescriptorSource = AthenaBoundaryDescriptorSource(emptyList())
    }
}

/** Supported external boundary categories for the M0 descriptor proof. */
enum class AthenaBoundaryCategory {
    STANDARDS,
    RUNTIME,
    ENTERPRISE,
}

/** Declared interaction direction for one external boundary descriptor. */
enum class AthenaBoundaryDirection {
    INBOUND,
    OUTBOUND,
    BIDIRECTIONAL,
    REFERENCE,
    COMPATIBILITY,
}

/** Declared canonical semantic authority posture for one external boundary descriptor. */
enum class AthenaBoundarySemanticAuthority {
    ENGINEERING_IR,
    EXTERNAL_BOUNDARY,
}

/** Supported exchanged-form kinds that may cross an external boundary in M0. */
enum class AthenaBoundaryExchangeFormKind {
    XML_DOCUMENT,
    RUNTIME_SIGNAL,
    ENTERPRISE_RECORD,
}

/** Declared M0 execution posture for one external boundary descriptor. */
enum class AthenaBoundaryM0Mode {
    PASSIVE_METADATA,
    OPERATIONAL,
}

/** Supported compatibility assumptions that may be declared by passive M0 boundary descriptors. */
enum class AthenaBoundaryCompatibilityAssumption {
    REVIEWED_MAPPING_ONLY,
    NO_IMPORTER_OR_EXPORTER_IN_M0,
    PASSIVE_BOUNDARY_METADATA_ONLY,
    NO_LIVE_CONNECTOR_IN_M0,
}

/** Core-owned manifest model for one external boundary descriptor. */
data class AthenaBoundaryDescriptorManifest(
    val descriptorId: String,
    val category: AthenaBoundaryCategory,
    val direction: AthenaBoundaryDirection,
    val upstreamAuthority: AthenaBoundarySemanticAuthority,
    val exchangeForms: List<AthenaBoundaryExchangeFormKind>,
    val compatibilityAssumptions: List<AthenaBoundaryCompatibilityAssumption>,
    val m0Mode: AthenaBoundaryM0Mode,
)

/** Fully loaded external boundary descriptor rooted in one local directory. */
data class AthenaBoundaryDescriptor(
    val rootDirectory: Path,
    val manifest: AthenaBoundaryDescriptorManifest,
)

/** Result of loading and validating one external boundary descriptor manifest. */
data class AthenaBoundaryDescriptorLoadResult(
    val loadedDescriptor: AthenaBoundaryDescriptor?,
    val diagnostics: List<AthenaBoundaryDiagnostic>,
) {
    /** True when the descriptor loaded successfully and emitted no error diagnostics. */
    val isValid: Boolean
        get() = loadedDescriptor != null && diagnostics.none { it.severity == AthenaBoundarySeverity.ERROR }
}
