package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerLoweringResult
import com.engineeringood.athena.compiler.CompilerParseResult
import com.engineeringood.athena.compiler.diagnosticMessages

/** Shared runtime execution context bound to one active project and one typed service registry. */
class AthenaExecutionContext(
    val project: AthenaProjectRef,
    val services: AthenaServiceRegistry,
) {
    private var activeCompilationSnapshot: CompilerCompilationResult? = null
    private var activeProjectionSessionSnapshot: AthenaRuntimeProjectionSession? = null
    private var activeProjectionViewId: String? = null
    private var activeProjectionSheetId: String? = null

    /** Resolves the runtime-owned compiler capability for the active project. */
    fun compiler(): AthenaCompiler = services.compiler()

    /** Resolves the runtime-owned engineering-graph capability for the active project. */
    fun engineeringGraph(): AthenaEngineeringGraphService = services.engineeringGraph()

    /** Resolves the runtime-owned hosted plugin services for the active project. */
    fun pluginRuntimeServices(): AthenaPluginRuntimeServices = services.pluginRuntimeServices()

    /** Parses the active project's authored DSL through the runtime-owned compiler capability. */
    fun parseActiveProject(): CompilerParseResult = compiler().parse(project.sourcePath)

    /** Lowers the active project's authored DSL through the runtime-owned compiler capability. */
    fun lowerActiveProject(): CompilerLoweringResult = compiler().lower(project.sourcePath)

    /**
     * Resolves the active project's current runtime-owned canonical state.
     *
     * The first result is bootstrapped from the authored DSL path. Later command-backed mutations reuse the cached
     * canonical state instead of reparsing the source text for every projection request.
     */
    fun compileActiveProject(): CompilerCompilationResult {
        return activeCompilationSnapshot ?: compiler().compile(project.sourcePath).also { compilation ->
            activeCompilationSnapshot = compilation
        }
    }

    /**
     * Returns the runtime-visible semantic diagnostics for the active project.
     */
    fun activeDiagnosticsMessages(): List<String> {
        return compileActiveProject().diagnosticMessages()
    }

    /**
     * Returns the runtime-owned projection session for the active project.
     */
    fun projectProjectionSession(): AthenaRuntimeProjectionSession {
        return activeProjectionSessionSnapshot ?: buildProjectionSession().also { session ->
            activeProjectionSessionSnapshot = session
        }
    }

    /**
     * Builds one non-cached projection preview from the supplied in-memory [compilation].
     *
     * This path is intended for IDE-owned dirty buffers that must stay visually aligned with the
     * latest tracked editor state without mutating runtime-owned canonical cache.
     */
    fun previewProjectionSession(compilation: CompilerCompilationResult): AthenaRuntimeProjectionSession {
        return buildProjectionSession(compilation)
    }

    /**
     * Switches the runtime-owned active projection view for the active project.
     */
    fun switchActiveProjectionView(viewId: String): AthenaRuntimeProjectionSwitchResult {
        return switchProjectionView(viewId)
    }

    /** Projects the active project's canonical semantic state into a runtime-owned engineering graph. */
    fun projectEngineeringGraphProjection(): AthenaEngineeringGraphProjection {
        return engineeringGraph().projectProjection(this)
    }

    /**
     * Returns the current runtime-owned active projection view id when one has already been selected.
     */
    internal fun currentActiveProjectionViewId(): String? = activeProjectionViewId

    /**
     * Returns the current runtime-owned active projection sheet id when a document sheet has been selected.
     */
    internal fun currentActiveProjectionSheetId(): String? = activeProjectionSheetId

    /**
     * Replaces the runtime-owned active projection view id after a successful switch.
     */
    internal fun replaceActiveProjectionViewId(viewId: String) {
        activeProjectionViewId = viewId
    }

    /**
     * Replaces the runtime-owned active projection sheet id after a governed sheet switch.
     */
    internal fun replaceActiveProjectionSheetId(sheetId: String?) {
        activeProjectionSheetId = sheetId
    }

    /**
     * Clears the cached runtime-owned projection session after one canonical input transition.
     */
    internal fun invalidateProjectionSession() {
        activeProjectionSessionSnapshot = null
    }
}
