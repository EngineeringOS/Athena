package com.engineeringood.athena.runtime

import java.nio.file.Path

/** In-memory runtime workspace that tracks one active project independently of compiler execution. */
class AthenaWorkspace internal constructor(
    val rootPath: Path,
    private val services: AthenaServiceRegistry,
    private val onProjectActivated: (AthenaExecutionContext) -> Unit,
) {
    private var isClosed: Boolean = false

    /** Current project selected by the runtime, or `null` when the workspace is open but idle. */
    var activeProject: AthenaProjectRef? = null
        private set

    /** Activates a path-backed project and exposes a shared execution context for downstream work. */
    fun activateProject(projectName: String, sourcePath: Path): AthenaExecutionContext {
        check(!isClosed) { "Workspace at $rootPath is closed." }
        val project = AthenaProjectRef(
            name = projectName,
            sourcePath = sourcePath,
        )
        val context = AthenaExecutionContext(
            project = project,
            services = services,
        )
        activeProject = project
        onProjectActivated(context)
        return context
    }

    internal fun close() {
        isClosed = true
        activeProject = null
    }
}
