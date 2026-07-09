package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import java.nio.file.Path

/**
 * Runtime-owned façade over compiler-owned repository report publication authority.
 *
 * The runtime does not resolve repository state independently. It only exposes the shared compiler
 * publication seam through workspace-friendly entry points.
 */
class AthenaRepositoryReportService(
    private val compilerProvider: () -> AthenaCompiler,
) {
    /** Publishes the canonical repository graph report for [repositoryRoot] through shared compiler authority. */
    fun publishRepositoryGraphReport(repositoryRoot: Path): AthenaRepositoryReportPublicationResult {
        return compilerProvider().publishRepositoryGraphReport(repositoryRoot)
    }
}
