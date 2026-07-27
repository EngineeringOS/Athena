package com.engineeringood.athena.packageruntime

import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

internal object RepresentationPackageSnapshotCapture {
    fun collect(
        repositoryRoot: Path,
        packageRoot: Path,
        diagnostics: MutableList<RepresentationPackageSnapshotDiagnostic>,
    ): List<CapturedPackageFile> {
        val files = mutableListOf<CapturedPackageFile>()
        Files.walkFileTree(
            packageRoot,
            emptySet(),
            Int.MAX_VALUE,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val normalized = dir.toAbsolutePath().normalize()
                    if (!normalized.startsWith(packageRoot)) {
                        diagnostics += snapshotDiagnostic(
                            "package.snapshot.path.escape",
                            normalized.toString(),
                            "Package traversal must stay inside the package root.",
                        )
                        return FileVisitResult.SKIP_SUBTREE
                    }
                    if (Files.isSymbolicLink(dir)) {
                        diagnostics += snapshotDiagnostic(
                            "package.snapshot.file.symlink",
                            normalized.toString(),
                            "Package directories must not be symbolic links, junctions, or reparse points.",
                        )
                        return FileVisitResult.SKIP_SUBTREE
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val normalized = file.toAbsolutePath().normalize()
                    if (!normalized.startsWith(packageRoot)) {
                        diagnostics += snapshotDiagnostic(
                            "package.snapshot.path.escape",
                            normalized.toString(),
                            "Package file traversal must stay inside the package root.",
                        )
                        return FileVisitResult.CONTINUE
                    }
                    if (Files.isSymbolicLink(file)) {
                        diagnostics += snapshotDiagnostic(
                            "package.snapshot.file.symlink",
                            normalized.toString(),
                            "Package files must be regular files, not symbolic links.",
                        )
                        return FileVisitResult.CONTINUE
                    }
                    if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                        diagnostics += snapshotDiagnostic(
                            "package.snapshot.file.unsupported",
                            normalized.toString(),
                            "Package entries must be regular files.",
                        )
                        return FileVisitResult.CONTINUE
                    }
                    if (file.fileName.toString().endsWith(".athena") || file.fileName.toString().endsWith(".svg")) {
                        runCatching {
                            file.capture(repositoryRoot, packageRoot)
                        }.onSuccess { snapshotFile ->
                            files += snapshotFile
                        }.onFailure {
                            diagnostics += snapshotDiagnostic(
                                "package.snapshot.file.identity.changed",
                                normalized.toString(),
                                "Package file identity changed while staging.",
                            )
                        }
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: java.io.IOException): FileVisitResult {
                    diagnostics += snapshotDiagnostic(
                        "package.snapshot.file.unreadable",
                        file.toString(),
                        "Package file could not be read with no-follow traversal guarantees.",
                    )
                    return FileVisitResult.CONTINUE
                }
            },
        )
        return files
    }

    fun validate(
        files: List<CapturedPackageFile>,
        diagnostics: MutableList<RepresentationPackageSnapshotDiagnostic>,
    ) {
        validateAthenaPackageHierarchy(files, diagnostics)
    }

    private fun validateAthenaPackageHierarchy(
        files: List<CapturedPackageFile>,
        diagnostics: MutableList<RepresentationPackageSnapshotDiagnostic>,
    ) {
        files.filter { it.file.repositoryRelativePath.endsWith(".athena") }.forEach { captured ->
            val file = captured.file
            val source = captured.bytes.toString(StandardCharsets.UTF_8)
            val packageName = PACKAGE_PATTERN.find(source)?.groupValues?.get(1)
            if (packageName != null) {
                val expectedDirectory = packageName.replace('.', '/')
                val actualDirectory = file.packageRootRelativePath.substringBeforeLast('/', missingDelimiterValue = "")
                if (actualDirectory != expectedDirectory) {
                    diagnostics += snapshotDiagnostic(
                        "package.snapshot.package-path.mismatch",
                        file.repositoryRelativePath,
                        "Athena package declaration must match its package-root filesystem hierarchy.",
                    )
                }
            }
        }
    }

    private fun Path.capture(repositoryRoot: Path, packageRoot: Path): CapturedPackageFile {
        val before = Files.readAttributes(this, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        val bytes = Files.readAllBytes(this)
        val after = Files.readAttributes(this, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        val fileKey = stableFileKey(before)
        require(fileKey == stableFileKey(after) && before.size() == after.size() && before.lastModifiedTime() == after.lastModifiedTime()) {
            "Package file identity changed while staging: $this"
        }
        return CapturedPackageFile(
            file = RepresentationPackageSnapshotFile(
                repositoryRelativePath = repositoryRoot.relativize(toAbsolutePath().normalize()).toPortableString(),
                packageRootRelativePath = packageRoot.relativize(toAbsolutePath().normalize()).toPortableString(),
                originalPath = toAbsolutePath().normalize(),
                stagedPath = Path.of(""),
                contentHash = RepresentationPackageSnapshotDigest.sha256(bytes),
                sizeBytes = bytes.size.toLong(),
                fileKey = fileKey,
                modifiedTimeMillis = before.lastModifiedTime().toMillis(),
            ),
            bytes = bytes,
        )
    }

    private fun stableFileKey(attrs: BasicFileAttributes): String =
        attrs.fileKey()?.toString() ?: "${attrs.size()}:${attrs.lastModifiedTime().toMillis()}"
}

internal data class CapturedPackageFile(
    val file: RepresentationPackageSnapshotFile,
    val bytes: ByteArray,
)

internal fun snapshotDiagnostic(
    code: String,
    subject: String,
    message: String,
): RepresentationPackageSnapshotDiagnostic = RepresentationPackageSnapshotDiagnostic(
    code = code,
    severity = RepresentationPackageSnapshotDiagnosticSeverity.ERROR,
    subject = subject,
    message = message,
)

internal fun List<RepresentationPackageSnapshotDiagnostic>.sortedSnapshotDiagnostics(): List<RepresentationPackageSnapshotDiagnostic> =
    sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))

internal fun Path.toPortableString(): String = joinToString("/")

private val PACKAGE_PATTERN = Regex("""(?m)^\s*package\s+([A-Za-z][A-Za-z0-9_.-]*)\s*$""")
