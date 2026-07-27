package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotRequest
import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStager
import com.engineeringood.athena.representation.RepresentationDefinition
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

class AthenaGeneratedRepresentationBoundaryVerifier(
    private val stager: RepresentationPackageSnapshotStager = RepresentationPackageSnapshotStager(),
    private val snapshotCompiler: AthenaRepresentationPackageSnapshotCompiler = AthenaRepresentationPackageSnapshotCompiler(),
) {
    fun verify(request: AthenaGeneratedRepresentationBoundaryRequest): AthenaGeneratedRepresentationBoundaryReport {
        val preflightForeignSchemaDiagnostics = foreignSchemaDiagnostics(request.repositoryRoot, request.packageRoots)
        if (preflightForeignSchemaDiagnostics.isNotEmpty()) {
            return AthenaGeneratedRepresentationBoundaryReport(
                definitions = emptyList(),
                diagnostics = preflightForeignSchemaDiagnostics.canonicalBoundaryDiagnostics(),
                proof = null,
            )
        }

        val staged = stager.stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = request.repositoryRoot,
                packageRoots = request.packageRoots,
                dependencyLockDigest = request.dependencyLockDigest,
                snapshotDirectory = request.snapshotDirectory,
            ),
        )
        val snapshotDiagnostics = staged.diagnostics.map { diagnostic ->
            AthenaGeneratedRepresentationBoundaryDiagnostic(
                code = diagnostic.code,
                subject = diagnostic.subject,
                message = diagnostic.message,
            )
        }
        val snapshot = staged.snapshot
            ?: return AthenaGeneratedRepresentationBoundaryReport(
                definitions = emptyList(),
                diagnostics = snapshotDiagnostics.canonicalBoundaryDiagnostics(),
                proof = null,
            )

        val compiled = snapshotCompiler.compile(snapshot)
        val compilerDiagnostics = compiled.diagnostics.map { diagnostic ->
            AthenaGeneratedRepresentationBoundaryDiagnostic(
                code = diagnostic.code,
                subject = diagnostic.subject,
                message = diagnostic.message,
            )
        }
        val diagnostics = (snapshotDiagnostics + compilerDiagnostics)
            .canonicalBoundaryDiagnostics()
        val proof = AthenaGeneratedRepresentationBoundaryProof(
            generatedSourceBoundary = "canonical-athena-source",
            snapshotId = compiled.proof.snapshotId,
            stagedSourcePaths = compiled.proof.stagedSourcePaths.map { it.replace('\\', '/') }.sorted(),
            qetRuntimeAuthorityAbsent = true,
            foreignRuntimeSchemaAbsent = true,
            xmlRuntimeAuthorityAbsent = compiled.proof.xmlRuntimeAuthorityAbsent,
            rawSvgTransportAbsent = compiled.proof.rawSvgTransportAbsent,
        )

        return AthenaGeneratedRepresentationBoundaryReport(
            definitions = if (diagnostics.isEmpty()) compiled.definitions else emptyList(),
            diagnostics = diagnostics,
            proof = proof,
        )
    }

    private fun foreignSchemaDiagnostics(
        repositoryRoot: Path,
        packageRoots: List<Path>,
    ): List<AthenaGeneratedRepresentationBoundaryDiagnostic> {
        val root = repositoryRoot.toAbsolutePath().normalize()
        return packageRoots.flatMap { packageRoot ->
            if (!Files.isDirectory(packageRoot, LinkOption.NOFOLLOW_LINKS)) {
                emptyList()
            } else {
                Files.walk(packageRoot).use { paths ->
                    paths
                        .filter { path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) }
                        .filter { path -> path.fileName.toString().isForeignRuntimeSchema() }
                        .map { path ->
                            AthenaGeneratedRepresentationBoundaryDiagnostic(
                                code = "generated.foreign-runtime-schema.forbidden",
                                subject = root.relativize(path.toAbsolutePath().normalize()).joinToString("/"),
                                message = "Generated representation packages must not contain QET, XML, XSD, or foreign runtime schema files.",
                            )
                        }
                        .toList()
                }
            }
        }
    }

}

data class AthenaGeneratedRepresentationBoundaryRequest(
    val repositoryRoot: Path,
    val packageRoots: List<Path>,
    val dependencyLockDigest: String,
    val snapshotDirectory: Path,
)

data class AthenaGeneratedRepresentationBoundaryReport(
    val definitions: List<RepresentationDefinition>,
    val diagnostics: List<AthenaGeneratedRepresentationBoundaryDiagnostic>,
    val proof: AthenaGeneratedRepresentationBoundaryProof?,
)

data class AthenaGeneratedRepresentationBoundaryDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

data class AthenaGeneratedRepresentationBoundaryProof(
    val generatedSourceBoundary: String,
    val snapshotId: String,
    val stagedSourcePaths: List<String>,
    val qetRuntimeAuthorityAbsent: Boolean,
    val foreignRuntimeSchemaAbsent: Boolean,
    val xmlRuntimeAuthorityAbsent: Boolean,
    val rawSvgTransportAbsent: Boolean,
)

private fun String.isForeignRuntimeSchema(): Boolean {
    val normalized = lowercase()
    return normalized.endsWith(".elmt") ||
        normalized.endsWith(".qet") ||
        normalized.endsWith(".xsd") ||
        normalized.endsWith(".xml")
}

private fun List<AthenaGeneratedRepresentationBoundaryDiagnostic>.canonicalBoundaryDiagnostics(): List<AthenaGeneratedRepresentationBoundaryDiagnostic> =
    distinct().sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))
