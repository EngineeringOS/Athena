package com.engineeringood.athena.ide.lsp

import java.nio.file.Path

/**
 * Describes the governed repository contract plus the deterministic editor seed for the current IDE path.
 *
 * This remains an `ide/lsp` transport and open-flow adapter. It consumes compiler-owned repository
 * validation and derives one temporary authored-source seed for the still-narrow runtime session.
 */
data class AthenaRepositoryDescriptor(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val sourceRootPath: Path,
    val projectName: String,
    val primaryPackageName: String,
    val sourcePath: Path,
)
