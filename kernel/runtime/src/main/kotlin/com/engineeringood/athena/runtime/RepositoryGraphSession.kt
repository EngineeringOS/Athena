package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.repository.RepositoryGraphReport
import java.nio.file.Path

/**
 * Runtime-owned authoritative repository/package session for one Athena product window.
 *
 * The session carries the canonical manifest, lock, resolved package graph, and diagnostics that
 * were published through the compiler-owned repository report seam, plus the active execution
 * context used by existing runtime-backed project operations.
 */
data class RepositoryGraphSession(
    val repositoryRoot: Path,
    val publication: AthenaRepositoryReportPublicationResult,
    val project: AthenaProjectRef,
    val executionContext: AthenaExecutionContext,
) {
    /** Canonical repository graph report projected from compiler/runtime authority for downstream consumers. */
    val report: RepositoryGraphReport?
        get() = publication.report
}
