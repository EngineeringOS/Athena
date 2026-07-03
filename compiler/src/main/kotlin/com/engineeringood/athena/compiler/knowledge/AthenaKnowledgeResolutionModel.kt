package com.engineeringood.athena.compiler.knowledge

import java.nio.file.Path

/** Compiler-owned source model that lists the local governed knowledge package roots to evaluate for one compilation run. */
data class AthenaKnowledgePackageSource(
    val packageRoots: List<Path>,
) {
    companion object {
        /** Returns an empty governed knowledge source when no reviewed packages are configured for the compilation run. */
        fun empty(): AthenaKnowledgePackageSource = AthenaKnowledgePackageSource(emptyList())
    }
}

/** Valid governed knowledge package candidate loaded successfully and eligible for compatibility evaluation. */
data class AthenaKnowledgeCandidatePackage(
    val packageRoot: Path,
    val artifactPackage: AthenaKnowledgeArtifactPackage,
)

/** Inspectable active governed knowledge artifact admitted into the effective compilation context. */
data class AthenaActiveKnowledgeArtifact(
    val packageRoot: Path,
    val artifactId: String,
    val artifactKind: AthenaKnowledgeArtifactKind,
    val artifactVersion: String,
    val provenance: AthenaKnowledgeProvenance,
)

/** Inspectable rejected governed knowledge package with the responsible package identity hint and rejection diagnostics. */
data class AthenaRejectedKnowledgePackage(
    val packageRoot: Path,
    val artifactId: String?,
    val artifactVersion: String?,
    val artifactKind: AthenaKnowledgeArtifactKind?,
    val diagnostics: List<AthenaKnowledgePackageDiagnostic>,
)

/** Effective governed knowledge context attached to compiler-facing results for one compilation run. */
data class AthenaCompilationKnowledgeContext(
    val source: AthenaKnowledgePackageSource,
    val candidates: List<AthenaKnowledgeCandidatePackage>,
    val activeArtifacts: List<AthenaActiveKnowledgeArtifact>,
    val rejectedPackages: List<AthenaRejectedKnowledgePackage>,
) {
    companion object {
        /** Returns an empty governed knowledge context for compiler runs that do not evaluate any reviewed packages. */
        fun empty(
            source: AthenaKnowledgePackageSource = AthenaKnowledgePackageSource.empty(),
        ): AthenaCompilationKnowledgeContext {
            return AthenaCompilationKnowledgeContext(
                source = source,
                candidates = emptyList(),
                activeArtifacts = emptyList(),
                rejectedPackages = emptyList(),
            )
        }
    }
}
