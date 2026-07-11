package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import java.nio.file.Files
import java.nio.file.Path

/**
 * Resolves the default governed knowledge-package source for product-facing JVM compiler surfaces.
 *
 * The current product proof scans the repository `extensions/` directory for reviewed
 * `knowledge-*` packages so runtime and LSP flows can consume the same fixed M9 knowledge packs as
 * compiler tests without hard-coding one package id into frontend code.
 */
fun defaultAthenaKnowledgePackageSource(
    startingDirectory: Path = Path.of("").toAbsolutePath().normalize(),
): AthenaKnowledgePackageSource {
    val repositoryRoot = resolveAthenaRepositoryRoot(startingDirectory) ?: return AthenaKnowledgePackageSource.empty()
    val extensionsDirectory = repositoryRoot.resolve("extensions")
    if (!Files.isDirectory(extensionsDirectory)) {
        return AthenaKnowledgePackageSource.empty()
    }

    val packageRoots = Files.newDirectoryStream(extensionsDirectory) { candidate ->
        Files.isDirectory(candidate) &&
            candidate.fileName.toString().startsWith("knowledge-") &&
            Files.exists(candidate.resolve("athena-knowledge.properties"))
    }.use { candidates ->
        candidates
            .map { candidate -> candidate.toAbsolutePath().normalize() }
            .sortedBy { candidate -> candidate.fileName.toString() }
            .toList()
    }

    return if (packageRoots.isEmpty()) {
        AthenaKnowledgePackageSource.empty()
    } else {
        AthenaKnowledgePackageSource(packageRoots)
    }
}

private fun resolveAthenaRepositoryRoot(
    startingDirectory: Path,
): Path? {
    var current = if (Files.isDirectory(startingDirectory)) {
        startingDirectory
    } else {
        startingDirectory.parent ?: return null
    }.toAbsolutePath().normalize()

    while (true) {
        if (Files.exists(current.resolve("settings.gradle.kts"))) {
            return current
        }
        current = current.parent ?: return null
    }
}
