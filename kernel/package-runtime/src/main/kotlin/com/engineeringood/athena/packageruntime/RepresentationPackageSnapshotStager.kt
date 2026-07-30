package com.engineeringood.athena.packageruntime

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories

class RepresentationPackageSnapshotStager {
    /** Compatibility entry point. Compiler product paths should pass roots from the canonical repository loader to [stage]. */
    fun stageRepository(
        repositoryRoot: Path,
        snapshotDirectory: Path,
        compilerSchemaVersion: String = RepresentationPackageSnapshotDefaults.COMPILER_SCHEMA_VERSION,
    ): RepresentationPackageSnapshotResult {
        val root = repositoryRoot.toAbsolutePath().normalize()
        val manifest = root.resolve("athena.yaml")
        val lock = root.resolve("athena.lock")
        val diagnostics = mutableListOf<RepresentationPackageSnapshotDiagnostic>()
        if (!Files.isRegularFile(manifest, LinkOption.NOFOLLOW_LINKS)) {
            diagnostics += snapshotDiagnostic(
                "package.snapshot.repository-manifest.missing",
                manifest.toString(),
                "Repository root must contain athena.yaml before package snapshot staging.",
            )
        }
        if (!Files.isRegularFile(lock, LinkOption.NOFOLLOW_LINKS)) {
            diagnostics += snapshotDiagnostic(
                "package.snapshot.repository-lock.missing",
                lock.toString(),
                "Repository root must contain athena.lock before package snapshot staging.",
            )
        }
        if (diagnostics.isNotEmpty()) {
            return RepresentationPackageSnapshotResult(null, diagnostics.sortedSnapshotDiagnostics())
        }

        val packageRoots = representationPackageRoots(Files.readString(manifest))
        if (packageRoots.isEmpty()) {
            return RepresentationPackageSnapshotResult(
                null,
                listOf(
                    snapshotDiagnostic(
                        "package.snapshot.repository-roots.missing",
                        manifest.toString(),
                        "athena.yaml must declare representationPackageRoots for representation package staging.",
                    ),
                ),
            )
        }

        return stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = root,
                packageRoots = packageRoots.map { relativeRoot -> root.resolve(relativeRoot).normalize() },
                dependencyLockDigest = RepresentationPackageSnapshotDigest.sha256(Files.readAllBytes(lock)),
                compilerSchemaVersion = compilerSchemaVersion,
                snapshotDirectory = snapshotDirectory,
            ),
        )
    }

    fun stage(request: RepresentationPackageSnapshotRequest): RepresentationPackageSnapshotResult {
        val repositoryRoot = request.repositoryRoot.toAbsolutePath().normalize()
        val snapshotCacheRoot = request.snapshotDirectory.toAbsolutePath().normalize()
        val packageRoots = request.packageRoots.map { root -> root.toAbsolutePath().normalize() }
        val diagnostics = mutableListOf<RepresentationPackageSnapshotDiagnostic>()

        if (!snapshotCacheRoot.startsWith(repositoryRoot)) {
            diagnostics += snapshotDiagnostic(
                "package.snapshot.root.escape",
                snapshotCacheRoot.toString(),
                "Snapshot directory must stay inside the repository root.",
            )
        }

        packageRoots.forEach { packageRoot ->
            if (!packageRoot.startsWith(repositoryRoot)) {
                diagnostics += snapshotDiagnostic(
                    "package.snapshot.root.escape",
                    packageRoot.toString(),
                    "Package root must stay inside the repository root.",
                )
            }
            if (!Files.isDirectory(packageRoot, LinkOption.NOFOLLOW_LINKS)) {
                diagnostics += snapshotDiagnostic(
                    "package.snapshot.root.missing",
                    packageRoot.toString(),
                    "Package root must exist and be a directory.",
                )
            }
            if (snapshotCacheRoot.startsWith(packageRoot)) {
                diagnostics += snapshotDiagnostic(
                    "package.snapshot.root.inside-package",
                    snapshotCacheRoot.toString(),
                    "Snapshot directory must not be inside a staged package root.",
                )
            }
        }
        if (diagnostics.isNotEmpty()) {
            return RepresentationPackageSnapshotResult(null, diagnostics.sortedSnapshotDiagnostics())
        }

        val files = packageRoots.flatMap { packageRoot ->
            RepresentationPackageSnapshotCapture.collect(repositoryRoot, packageRoot, diagnostics)
        }.sortedBy { it.file.repositoryRelativePath }
        RepresentationPackageSnapshotCapture.validate(files, diagnostics)

        if (files.size > RepresentationPackageSnapshotDefaults.MAX_FILES) {
            diagnostics += snapshotDiagnostic(
                "package.snapshot.budget.files.exceeded",
                "package.files",
                "Representation package snapshot exceeds the maximum file count.",
            )
        }
        val totalBytes = files.sumOf { it.file.sizeBytes }
        if (totalBytes > RepresentationPackageSnapshotDefaults.MAX_AGGREGATE_BYTES) {
            diagnostics += snapshotDiagnostic(
                "package.snapshot.budget.bytes.exceeded",
                "package.bytes",
                "Representation package snapshot exceeds the maximum aggregate byte budget.",
            )
        }
        if (diagnostics.isNotEmpty()) {
            return RepresentationPackageSnapshotResult(null, diagnostics.sortedSnapshotDiagnostics())
        }

        val snapshotId = snapshotIdentity(
            compilerSchemaVersion = request.compilerSchemaVersion,
            dependencyLockDigest = request.dependencyLockDigest,
            files = files.map(CapturedPackageFile::file),
        )
        val snapshotRoot = snapshotCacheRoot.resolve(snapshotId.removePrefix("sha256:")).normalize()
        Files.createDirectories(snapshotRoot)
        val stagedFiles = files.map { captured ->
            val file = captured.file
            val stagedPath = snapshotRoot.resolve(file.repositoryRelativePath).normalize()
            if (!stagedPath.startsWith(snapshotRoot)) {
                diagnostics += snapshotDiagnostic(
                    "package.snapshot.path.escape",
                    file.repositoryRelativePath,
                    "Staged file path must stay inside the immutable snapshot root.",
                )
                file
            } else {
                stagedPath.parent?.createDirectories()
                stageCapturedBytes(stagedPath, captured.bytes, file.contentHash, diagnostics)
                file.copy(stagedPath = stagedPath)
            }
        }
        if (diagnostics.isNotEmpty()) {
            return RepresentationPackageSnapshotResult(null, diagnostics.sortedSnapshotDiagnostics())
        }

        return RepresentationPackageSnapshotResult(
            snapshot = RepresentationPackageSnapshot(
                snapshotId = snapshotId,
                snapshotRoot = snapshotRoot,
                repositoryRoot = repositoryRoot,
                packageRoots = packageRoots.map { root -> repositoryRoot.relativize(root).toPortableString() }.sorted(),
                dependencyLockDigest = request.dependencyLockDigest,
                compilerSchemaVersion = request.compilerSchemaVersion,
                files = stagedFiles,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun stageCapturedBytes(
        stagedPath: Path,
        bytes: ByteArray,
        expectedHash: String,
        diagnostics: MutableList<RepresentationPackageSnapshotDiagnostic>,
    ) {
        if (Files.notExists(stagedPath, LinkOption.NOFOLLOW_LINKS)) {
            runCatching {
                Files.write(stagedPath, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
            }.onFailure { failure ->
                if (Files.notExists(stagedPath, LinkOption.NOFOLLOW_LINKS)) {
                    diagnostics += snapshotDiagnostic(
                        "package.snapshot.file.write-failed",
                        stagedPath.toString(),
                        "Captured package bytes could not be written: ${failure.message ?: failure::class.simpleName}",
                    )
                    return
                }
            }
        }
        val actualHash = runCatching {
            RepresentationPackageSnapshotDigest.sha256(Files.readAllBytes(stagedPath))
        }.getOrNull()
        if (actualHash != expectedHash) {
            diagnostics += snapshotDiagnostic(
                "package.snapshot.cache.identity-mismatch",
                stagedPath.toString(),
                "Existing content-addressed snapshot bytes do not match the captured source hash.",
            )
        }
    }

    private fun snapshotIdentity(
        compilerSchemaVersion: String,
        dependencyLockDigest: String,
        files: List<RepresentationPackageSnapshotFile>,
    ): String {
        val material = buildString {
            appendLine(compilerSchemaVersion)
            appendLine(dependencyLockDigest)
            files.sortedBy { it.repositoryRelativePath }.forEach { file ->
                append(file.repositoryRelativePath)
                append('|')
                append(file.contentHash)
                append('|')
                append(file.sizeBytes)
                appendLine()
            }
        }
        return RepresentationPackageSnapshotDigest.sha256(material.toByteArray(StandardCharsets.UTF_8))
    }

    private fun representationPackageRoots(manifest: String): List<String> {
        val lines = manifest.lineSequence().toList()
        val headerIndex = lines.indexOfFirst { line -> line.trim() == "representationPackageRoots:" }
        if (headerIndex < 0) return emptyList()
        return lines.drop(headerIndex + 1)
            .takeWhile { line -> line.startsWith(" ") || line.isBlank() }
            .mapNotNull { line ->
                line.trim().removePrefix("-").trim().unquote().takeIf(String::isNotBlank)
            }
            .filterNot { root ->
                root.startsWith("/") || root.contains('\\') || root.split('/').any { it == ".." || it.isBlank() }
            }
            .distinct()
            .sorted()
    }
}

private fun String.unquote(): String = when {
    length >= 2 && first() == '"' && last() == '"' -> substring(1, length - 1)
    length >= 2 && first() == '\'' && last() == '\'' -> substring(1, length - 1)
    else -> this
}
