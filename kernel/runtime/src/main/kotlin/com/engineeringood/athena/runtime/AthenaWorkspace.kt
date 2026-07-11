package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import java.nio.file.Path

/** In-memory runtime workspace that tracks one active project independently of compiler execution. */
class AthenaWorkspace internal constructor(
    val rootPath: Path,
    private val services: AthenaServiceRegistry,
    private val onProjectActivated: (AthenaExecutionContext) -> Unit,
    private val onRepositoryGraphSessionActivated: (RepositoryGraphSession) -> Unit,
) {
    private var isClosed: Boolean = false

    /** Current project selected by the runtime, or `null` when the workspace is open but idle. */
    var activeProject: AthenaProjectRef? = null
        private set

    /** Current authoritative repository graph session for this workspace, or `null` when not yet activated. */
    var activeRepositoryGraphSession: RepositoryGraphSession? = null
        private set

    /** Activates a path-backed project and exposes a shared execution context for downstream work. */
    fun activateProject(projectName: String, sourcePath: Path): AthenaExecutionContext {
        check(!isClosed) { "Workspace at $rootPath is closed." }
        val context = createExecutionContext(projectName, sourcePath)
        activeProject = context.project
        activeRepositoryGraphSession = null
        onProjectActivated(context)
        return context
    }

    /**
     * Activates the governed repository root into one authoritative runtime-owned repository graph session.
     */
    fun activateRepositoryGraphSession(projectName: String, sourcePath: Path): RepositoryGraphSession {
        check(!isClosed) { "Workspace at $rootPath is closed." }
        val context = createExecutionContext(projectName, sourcePath)
        val session = RepositoryGraphSession(
            repositoryRoot = rootPath,
            publication = repositoryGraphReport(),
            project = context.project,
            executionContext = context,
        )
        activeProject = context.project
        activeRepositoryGraphSession = session
        onProjectActivated(context)
        onRepositoryGraphSessionActivated(session)
        return session
    }

    /** Resolves the shared repository-report capability scoped to this workspace's runtime host. */
    fun repositoryReports(): AthenaRepositoryReportService {
        check(!isClosed) { "Workspace at $rootPath is closed." }
        return services.repositoryReports()
    }

    /** Publishes the canonical repository graph report for this workspace root through shared compiler authority. */
    fun repositoryGraphReport(): AthenaRepositoryReportPublicationResult {
        check(!isClosed) { "Workspace at $rootPath is closed." }
        return repositoryReports().publishRepositoryGraphReport(rootPath)
    }

    internal fun close() {
        isClosed = true
        activeProject = null
        activeRepositoryGraphSession = null
    }

    private fun createExecutionContext(projectName: String, sourcePath: Path): AthenaExecutionContext {
        val project = AthenaProjectRef(
            name = projectName,
            sourcePath = sourcePath,
            workspaceRoot = rootPath,
        )
        return AthenaExecutionContext(
            project = project,
            services = services,
        )
    }
}
