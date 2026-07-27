package com.engineeringood.athena.packageruntime

import java.nio.file.Path
import java.security.MessageDigest

data class RepresentationPackageSnapshotRequest(
    val repositoryRoot: Path,
    val packageRoots: List<Path>,
    val dependencyLockDigest: String,
    val compilerSchemaVersion: String = RepresentationPackageSnapshotDefaults.COMPILER_SCHEMA_VERSION,
    val snapshotDirectory: Path,
)

data class RepresentationPackageSnapshot(
    val snapshotId: String,
    val snapshotRoot: Path,
    val repositoryRoot: Path,
    val packageRoots: List<String>,
    val dependencyLockDigest: String,
    val compilerSchemaVersion: String,
    val files: List<RepresentationPackageSnapshotFile>,
)

data class RepresentationPackageSnapshotFile(
    val repositoryRelativePath: String,
    val packageRootRelativePath: String,
    val originalPath: Path,
    val stagedPath: Path,
    val contentHash: String,
    val sizeBytes: Long,
    val fileKey: String,
    val modifiedTimeMillis: Long,
)

data class RepresentationPackageSnapshotResult(
    val snapshot: RepresentationPackageSnapshot?,
    val diagnostics: List<RepresentationPackageSnapshotDiagnostic>,
) {
    val isValid: Boolean
        get() = snapshot != null && diagnostics.none { it.severity == RepresentationPackageSnapshotDiagnosticSeverity.ERROR }
}

enum class RepresentationPackageSnapshotDiagnosticSeverity {
    ERROR,
}

data class RepresentationPackageSnapshotDiagnostic(
    val code: String,
    val severity: RepresentationPackageSnapshotDiagnosticSeverity,
    val subject: String,
    val message: String,
)

object RepresentationPackageSnapshotDefaults {
    const val COMPILER_SCHEMA_VERSION: String = "m34-representation-snapshot/v1"
    const val MAX_FILES: Int = 512
    const val MAX_AGGREGATE_BYTES: Long = 50L * 1024L * 1024L
}

object RepresentationPackageSnapshotDigest {
    fun sha256(bytes: ByteArray): String = "sha256:" + MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { byte -> "%02x".format(byte) }
}
