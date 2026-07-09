package com.engineeringood.athena.runtime

import java.nio.file.Path

/** Runtime host that owns workspace lifecycle, active project state, and shared execution context creation. */
class AthenaRuntime(
    val serviceRegistry: AthenaServiceRegistry = AthenaServiceRegistry(),
) {
    /** Currently opened workspace, or `null` when the runtime is still at the welcome boundary. */
    var activeWorkspace: AthenaWorkspace? = null
        private set

    /** Currently active authoritative repository graph session, or `null` when none is active. */
    var activeRepositoryGraphSession: RepositoryGraphSession? = null
        private set

    /** Currently active execution context, or `null` when no project has been activated yet. */
    var activeExecutionContext: AthenaExecutionContext? = null
        private set

    /** Opens a workspace without starting compiler execution or touching downstream services. */
    fun openWorkspace(rootPath: Path): AthenaWorkspace {
        activeWorkspace?.close()
        val workspace = AthenaWorkspace(
            rootPath = rootPath,
            services = serviceRegistry,
            onProjectActivated = ::setActiveExecutionContext,
            onRepositoryGraphSessionActivated = ::setActiveRepositoryGraphSession,
        )
        activeWorkspace = workspace
        activeRepositoryGraphSession = null
        activeExecutionContext = null
        return workspace
    }

    /** Closes the active workspace and clears all runtime-owned active state. */
    fun closeWorkspace() {
        activeWorkspace?.close()
        activeWorkspace = null
        activeRepositoryGraphSession = null
        activeExecutionContext = null
    }

    private fun setActiveExecutionContext(context: AthenaExecutionContext) {
        activeExecutionContext = context
        activeRepositoryGraphSession = null
    }

    private fun setActiveRepositoryGraphSession(session: RepositoryGraphSession) {
        activeRepositoryGraphSession = session
        activeExecutionContext = session.executionContext
    }
}
