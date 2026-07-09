package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.PackageDependency
import com.engineeringood.athena.repository.RepositoryResolutionDependency
import com.engineeringood.athena.repository.RepositoryResolutionInput

/**
 * Builds deterministic package-resolution input from a validated governed repository contract.
 */
class AthenaRepositoryResolutionInputBuilder {
    /**
     * Converts [validation] into deterministic resolver input without performing dependency resolution yet.
     */
    fun build(validation: AthenaRepositoryContractValidationResult): AthenaRepositoryResolutionInputResult {
        val repository = validation.repository
        if (!validation.isValid || repository == null) {
            return AthenaRepositoryResolutionInputResult(
                repositoryRoot = validation.repositoryRoot,
                manifestPath = validation.manifestPath,
                lockPath = validation.lockPath,
                manifestPresent = validation.manifestPresent,
                lockPresent = validation.lockPresent,
                repository = repository,
                diagnostics = validation.diagnostics,
            )
        }

        return AthenaRepositoryResolutionInputResult(
            repositoryRoot = validation.repositoryRoot,
            manifestPath = validation.manifestPath,
            lockPath = validation.lockPath,
            manifestPresent = validation.manifestPresent,
            lockPresent = validation.lockPresent,
            repository = repository,
            resolutionInput = repository.toResolutionInput(),
            diagnostics = validation.diagnostics,
        )
    }
}

private fun EngineeringRepository.toResolutionInput(): RepositoryResolutionInput {
    val manifest = manifest
    return RepositoryResolutionInput(
        rootPackage = manifest.primaryPackage.id,
        rootSourcePath = manifest.primaryPackage.sourceRoot,
        dependencies = manifest.dependencies
            .map { dependency -> dependency.toResolutionDependency() }
            .sortedWith(compareBy(::stableDependencyKey)),
    )
}

private fun PackageDependency.toResolutionDependency(): RepositoryResolutionDependency {
    return RepositoryResolutionDependency(
        packageId = packageId,
        source = source,
        locator = locator,
    )
}

private fun stableDependencyKey(dependency: RepositoryResolutionDependency): String {
    return listOf(
        dependency.packageId.name,
        dependency.packageId.version.orEmpty(),
        dependency.source.name,
        dependency.locator.orEmpty(),
    ).joinToString("|")
}
