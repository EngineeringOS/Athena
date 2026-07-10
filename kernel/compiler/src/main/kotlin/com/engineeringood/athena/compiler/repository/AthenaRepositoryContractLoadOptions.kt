package com.engineeringood.athena.compiler.repository

/**
 * Controls optional repository-contract validation behavior for compiler-owned internal flows.
 *
 * Default validation remains strict. Graph-resolution flows can opt in to allowing nested
 * governed subrepositories underneath the current repository root without treating them as
 * layout violations of the owning repository.
 */
data class AthenaRepositoryContractLoadOptions(
    val allowNestedGovernedSubrepositories: Boolean = false,
    val requireLockFile: Boolean = true,
)
