package com.engineeringood.athena.compiler.knowledge

import java.nio.file.Path

/** Supported governed knowledge artifact kinds for the M0 package boundary. */
enum class AthenaKnowledgeArtifactKind {
    ONTOLOGY,
    STANDARDS_MAPPING,
    RULE,
    KNOWLEDGE_PACK,
}

/** Reviewable provenance carried by one governed knowledge artifact package. */
data class AthenaKnowledgeProvenance(
    val sources: List<String>,
    val reviewedBy: String,
)

/** Core-owned compatibility range for governed knowledge packages independent from plugin manifests. */
data class AthenaKnowledgeCoreCompatibilityRange(
    val minimumInclusive: String,
    val maximumInclusive: String? = null,
)

/** One typed payload entry declared by a governed knowledge artifact package. */
data class AthenaKnowledgePayloadEntry(
    val entryId: String,
    val entryKind: String,
    val relativePath: String,
    val resolvedPath: Path,
)

/** Core-owned manifest model for one governed knowledge artifact package. */
data class AthenaKnowledgeArtifactManifest(
    val artifactId: String,
    val artifactKind: AthenaKnowledgeArtifactKind,
    val packageFormatVersion: Int,
    val artifactVersion: String,
    val provenance: AthenaKnowledgeProvenance,
    val coreCompatibility: AthenaKnowledgeCoreCompatibilityRange,
)

/** Fully loaded governed knowledge artifact package rooted in one local directory. */
data class AthenaKnowledgeArtifactPackage(
    val rootDirectory: Path,
    val manifest: AthenaKnowledgeArtifactManifest,
    val payloadEntries: List<AthenaKnowledgePayloadEntry>,
)

/** Result of loading and validating one governed knowledge artifact package. */
data class AthenaKnowledgePackageLoadResult(
    val loadedPackage: AthenaKnowledgeArtifactPackage?,
    val diagnostics: List<AthenaKnowledgePackageDiagnostic>,
) {
    /** True when the package loaded successfully and no error diagnostics were emitted. */
    val isValid: Boolean
        get() = loadedPackage != null && diagnostics.none { it.severity == AthenaKnowledgePackageSeverity.ERROR }
}
