package com.engineeringood.athena.compiler.repository

import java.nio.file.Path

/**
 * Publishes the compiler-owned canonical repository graph report for one governed repository root.
 *
 * This keeps the compiler as the only authority that reconciles manifest intent, resolved package
 * graph state, and derived lock status before runtime or IDE layers observe the repository model.
 */
class AthenaRepositoryReportPublisher(
    private val lockMaterializer: AthenaRepositoryLockMaterializer = AthenaRepositoryLockMaterializer(),
) {
    /**
     * Validates the current repository lock against canonical resolver output and publishes one
     * inspectable report payload for downstream runtime and future IDE consumers.
     */
    fun publish(repositoryRoot: Path): AthenaRepositoryReportPublicationResult {
        val validation = lockMaterializer.validate(repositoryRoot)
        return AthenaRepositoryReportPublicationResult(
            repositoryRoot = validation.repositoryRoot,
            manifestPath = validation.manifestPath,
            lockPath = validation.lockPath,
            manifestPresent = validation.manifestPresent,
            lockPresent = validation.lockPresent,
            repository = validation.repository,
            resolutionInput = validation.resolutionInput,
            graph = validation.graph,
            expectedLock = validation.expectedLock,
            actualLock = validation.actualLock,
            lockState = determineLockState(validation),
            diagnostics = validation.diagnostics,
        )
    }

    private fun determineLockState(
        validation: AthenaRepositoryLockValidationResult,
    ): AthenaRepositoryReportLockState {
        if (!validation.lockPresent) {
            return AthenaRepositoryReportLockState.MISSING
        }

        if (validation.actualLock == null) {
            return AthenaRepositoryReportLockState.INVALID
        }

        val diagnosticCodes = validation.diagnostics.map { diagnostic -> diagnostic.code }.toSet()
        if ("repository.lock.content.out-of-date" in diagnosticCodes || "repository.lock.content.noncanonical" in diagnosticCodes) {
            return AthenaRepositoryReportLockState.STALE
        }

        return AthenaRepositoryReportLockState.CURRENT
    }
}
