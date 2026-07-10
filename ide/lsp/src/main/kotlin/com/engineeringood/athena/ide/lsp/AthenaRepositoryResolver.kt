package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoadOptions
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractValidationResult
import com.engineeringood.athena.repository.RepositoryDiagnostic
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

/**
 * Resolves one governed repository root into a contract-aware open seed for the current IDE path.
 *
 * The compiler remains the owner of repository validation. This adapter consumes that JVM-owned
 * result and derives one deterministic authored-source seed only because the temporary runtime path
 * still activates a single project/source pair until `RepositoryGraphSession` lands later in M5.
 */
class AthenaRepositoryResolver(
    private val compiler: AthenaCompiler = AthenaCompiler(),
) {
    /**
     * Resolves the supplied repository root into contract-aware session seed state.
     */
    fun resolve(repositoryRoot: Path): AthenaRepositoryResolutionResult {
        val normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize()
        val validation = compiler.validateRepositoryContract(
            normalizedRepositoryRoot,
            AthenaRepositoryContractLoadOptions(
                requireLockFile = false,
            ),
        )
        if (!validation.isValid || validation.repository == null) {
            return AthenaRepositoryResolutionFailure(
                reason = renderContractFailure(validation),
                diagnostics = validation.diagnostics,
            )
        }

        val repository = validation.repository
            ?: return AthenaRepositoryResolutionFailure(
                reason = renderContractFailure(validation),
                diagnostics = validation.diagnostics,
            )
        val manifest = repository.manifest
        val sourceRootPath = normalizedRepositoryRoot.resolve(manifest.primaryPackage.sourceRoot)
        val sourceFiles = findAthenaSources(sourceRootPath)
        if (sourceFiles.isEmpty()) {
            return AthenaRepositoryResolutionFailure(
                reason = "Governed source root `${manifest.primaryPackage.sourceRoot}/` does not contain an authored `.athena` source for the current IDE seed.",
            )
        }

        val sourcePath = sourceFiles.first()
        return AthenaRepositoryResolutionSuccess(
            AthenaRepositoryDescriptor(
                repositoryRoot = normalizedRepositoryRoot,
                manifestPath = validation.manifestPath,
                lockPath = validation.lockPath,
                sourceRootPath = sourceRootPath,
                projectName = manifest.primaryPackage.id.name.substringAfterLast('.'),
                primaryPackageName = manifest.primaryPackage.id.name,
                sourcePath = sourcePath,
            ),
        )
    }

    private fun findAthenaSources(searchRoot: Path): List<Path> {
        Files.walk(searchRoot).use { paths ->
            return paths
                .filter { candidate -> candidate.isRegularFile() }
                .filter { candidate -> candidate.extension.equals("athena", ignoreCase = true) }
                .sorted()
                .collect(Collectors.toList())
        }
    }
}

/**
 * Result of resolving a repository root into contract-aware IDE seed state.
 */
sealed interface AthenaRepositoryResolutionResult

/**
 * Indicates that the repository root resolved successfully.
 */
data class AthenaRepositoryResolutionSuccess(
    val descriptor: AthenaRepositoryDescriptor,
) : AthenaRepositoryResolutionResult

/**
 * Indicates that the repository root could not be resolved into a valid contract-aware seed.
 */
data class AthenaRepositoryResolutionFailure(
    val reason: String,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
) : AthenaRepositoryResolutionResult

private fun Path.toDisplayPath(): String = toString().replace('\\', '/')

private fun renderContractFailure(validation: AthenaRepositoryContractValidationResult): String {
    if (validation.diagnostics.isEmpty()) {
        return "Repository contract validation failed for ${validation.repositoryRoot.toDisplayPath()}."
    }

    val renderedDiagnostics = validation.diagnostics.joinToString(separator = "\n") { diagnostic ->
        "- [${diagnostic.code}] ${diagnostic.message}"
    }
    return buildString {
        append("Repository contract validation failed for ")
        append(validation.repositoryRoot.toDisplayPath())
        append(':')
        append('\n')
        append(renderedDiagnostics)
    }
}
