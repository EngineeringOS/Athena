package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.integrations.scm.git.GitSemanticBaselineAdapter
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntime
import com.engineeringood.athena.runtime.AthenaSemanticBaselineService
import com.engineeringood.athena.runtime.AthenaServiceRegistry
import com.engineeringood.athena.runtime.RepositoryGraphSession
import com.engineeringood.athena.scm.SemanticBaselineResolver
import java.nio.file.Path

/**
 * Owns one runtime-backed repository graph session for the current IDE path.
 *
 * Repository/package authority already lives downstream of Theia presentation code. The host keeps
 * the current transport summary small while consuming the runtime-owned session directly.
 */
class AthenaLspSessionHost(
    private val runtime: AthenaRuntime = defaultAthenaLspRuntime(),
    private val repositoryResolver: AthenaRepositoryResolver = AthenaRepositoryResolver(),
) {
    /**
     * Activates one repository-backed runtime session and returns a transport-safe summary.
     */
    fun activateRepository(repositoryRoot: Path): AthenaLspSessionHostResult {
        return when (val resolution = repositoryResolver.resolve(repositoryRoot)) {
            is AthenaRepositoryResolutionSuccess -> {
                val descriptor = resolution.descriptor
                val session = runtime.openWorkspace(descriptor.repositoryRoot).activateRepositoryGraphSession(
                    projectName = descriptor.projectName,
                    sourcePath = descriptor.sourcePath,
                )
                AthenaLspSessionHostReady(
                    repositoryRoot = descriptor.repositoryRoot,
                    manifestPath = descriptor.manifestPath,
                    lockPath = descriptor.lockPath,
                    sourceRootPath = descriptor.sourceRootPath,
                    sourcePath = descriptor.sourcePath,
                    projectName = session.project.name,
                    primaryPackageName = descriptor.primaryPackageName,
                    session = session,
                )
            }

            is AthenaRepositoryResolutionFailure -> AthenaLspSessionHostUnavailable(
                repositoryRoot = repositoryRoot.toAbsolutePath().normalize(),
                reason = resolution.reason,
            )
        }
    }

    /**
     * Blocks until the host is told to stop or its stdin closes.
     */
    fun serveUntilShutdown() {
        while (true) {
            val nextLine = readlnOrNull() ?: return
            if (nextLine.trim().equals("shutdown", ignoreCase = true)) {
                shutdown()
                return
            }
        }
    }

    /**
     * Releases the currently active runtime workspace, if any.
     */
    fun shutdown() {
        runtime.closeWorkspace()
    }
}

private fun defaultAthenaLspRuntime(): AthenaRuntime {
    return AthenaRuntime(
        serviceRegistry = AthenaServiceRegistry(
            semanticBaselineServiceProvider = {
                AthenaSemanticBaselineService(
                    baselineResolver = SemanticBaselineResolver(
                        adapters = listOf(
                            GitSemanticBaselineAdapter { AthenaCompiler() },
                        ),
                    ),
                )
            },
        ),
    )
}

/**
 * Result of activating the first LSP-owned repository session host.
 */
sealed interface AthenaLspSessionHostResult {
    /**
     * Renders the host result as one JSON line for the Node-side session manager.
     */
    fun toEventLine(): String
}

/**
 * Indicates that the host activated one repository-backed runtime session successfully.
 */
data class AthenaLspSessionHostReady(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val sourceRootPath: Path,
    val sourcePath: Path,
    val projectName: String,
    val primaryPackageName: String,
    val session: RepositoryGraphSession,
) : AthenaLspSessionHostResult {
    /** Backward-compatible active execution context for existing M4/M5 runtime consumers. */
    val context: AthenaExecutionContext
        get() = session.executionContext

    override fun toEventLine(): String {
        return """{"event":"session-ready","repositoryRoot":${repositoryRoot.toJsonString()},"manifestPath":${manifestPath.toJsonString()},"lockPath":${lockPath.toJsonString()},"sourceRootPath":${sourceRootPath.toJsonString()},"sourcePath":${sourcePath.toJsonString()},"projectName":${projectName.toJsonString()},"primaryPackageName":${primaryPackageName.toJsonString()}}"""
    }
}

/**
 * Indicates that the host could not activate a repository-backed runtime session.
 */
data class AthenaLspSessionHostUnavailable(
    val repositoryRoot: Path,
    val reason: String,
) : AthenaLspSessionHostResult {
    override fun toEventLine(): String {
        return """{"event":"session-unavailable","repositoryRoot":${repositoryRoot.toJsonString()},"message":${reason.toJsonString()}}"""
    }
}

private fun Path.toJsonString(): String = toString().toJsonString()

private fun String.toJsonString(): String {
    val escaped = buildString(length + 8) {
        append('"')
        for (character in this@toJsonString) {
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(character)
            }
        }
        append('"')
    }
    return escaped
}
