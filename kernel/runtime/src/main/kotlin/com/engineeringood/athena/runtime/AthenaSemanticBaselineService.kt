package com.engineeringood.athena.runtime

import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticBaselineResolutionRequest
import com.engineeringood.athena.scm.SemanticBaselineResolutionResult
import com.engineeringood.athena.scm.SemanticBaselineResolver

/**
 * Runtime-owned facade over the semantic baseline-resolution seam.
 *
 * The runtime remains responsible for the active `RepositoryGraphSession`, while the semantic SCM
 * core owns the baseline-loading contract and adapter orchestration.
 */
class AthenaSemanticBaselineService(
    private val baselineResolver: SemanticBaselineResolver = SemanticBaselineResolver(),
) {
    /** Resolves one comparison baseline for the supplied active runtime-owned repository session. */
    fun resolveBaseline(
        session: RepositoryGraphSession,
        descriptor: SemanticBaselineDescriptor,
        locator: SemanticBaselineLocator,
    ): SemanticBaselineResolutionResult {
        return baselineResolver.resolve(
            SemanticBaselineResolutionRequest(
                descriptor = descriptor,
                locator = locator,
                currentRepositoryRoot = session.repositoryRoot,
            ),
        )
    }
}
