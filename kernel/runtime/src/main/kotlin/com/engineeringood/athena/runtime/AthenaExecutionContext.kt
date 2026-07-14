package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerAffectedScope
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerLoweringResult
import com.engineeringood.athena.compiler.CompilerParseResult
import com.engineeringood.athena.compiler.diagnosticMessages
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.renderer.svg.SvgRenderer

/** Shared runtime execution context bound to one active project and one typed service registry. */
class AthenaExecutionContext(
    val project: AthenaProjectRef,
    val services: AthenaServiceRegistry,
) {
    private var activeCompilationSnapshot: CompilerCompilationResult? = null
    private var activeProjectionSessionSnapshot: AthenaRuntimeProjectionSession? = null
    private var activeProjectionViewId: String? = null
    private var projectionMetadataState: AthenaProjectionMetadataState = AthenaProjectionMetadataState()
    private var commandHistoryState: AthenaCommandHistoryState = AthenaCommandHistoryState()
    private var authoringSessionState: AthenaAuthoringSessionState = AthenaAuthoringSessionState()
    private var semanticMacroPreviewSessionState: AthenaSemanticMacroPreviewSessionState = AthenaSemanticMacroPreviewSessionState()
    private var aiProposalState: AthenaAiProposalState = AthenaAiProposalState()
    private var aiReasoningProposalState: AthenaAiReasoningProposalState = AthenaAiReasoningProposalState()
    private var aiReasoningSessionState: AthenaAiReasoningSessionState = AthenaAiReasoningSessionState()
    private var latestSemanticDiffInspection: AthenaSemanticDiffInspection? = null

    /** Resolves the runtime-owned compiler capability for the active project. */
    fun compiler(): AthenaCompiler = services.compiler()

    /** Resolves the runtime-owned renderer capability for the active project. */
    fun renderer(): SvgRenderer = services.renderer()

    /** Resolves the runtime-owned engineering-graph capability for the active project. */
    fun engineeringGraph(): AthenaEngineeringGraphService = services.engineeringGraph()

    /** Resolves the runtime-owned command capability for the active project. */
    fun commandRuntime(): AthenaCommandRuntimeService = services.commandRuntime()

    /** Resolves the runtime-owned source-mutation evaluation capability for the active project. */
    fun sourceMutationRuntime(): AthenaSourceMutationRuntimeService = services.sourceMutationRuntime()

    /** Resolves the runtime-owned guided authoring session capability for the active project. */
    fun authoringSessions(): AthenaAuthoringSessionRuntimeService = services.authoringSessions()

    /** Resolves the runtime-owned Semantic Macro seam capability for the active project. */
    fun reuseRuntime(): AthenaSemanticMacroRuntimeService = services.reuseRuntime()

    /** Resolves the runtime-owned component-knowledge inspection capability for the active project. */
    fun componentKnowledgeRuntime(): AthenaComponentKnowledgeRuntimeService = services.componentKnowledgeRuntime()

    /** Resolves the runtime-owned graph command-intent capability for the active project. */
    fun graphCommandIntentRuntime(): AthenaGraphCommandIntentRuntimeService = services.graphCommandIntentRuntime()

    /** Resolves the runtime-owned accepted-mutation review capability for the active project. */
    fun semanticMutationReviews(): AthenaSemanticMutationReviewService = services.semanticMutationReviews()

    /** Resolves the runtime-owned optional AI proposal capability for the active project. */
    fun aiProposalRuntime(): AthenaAiProposalRuntimeService = services.aiProposalRuntime()

    /** Resolves the runtime-owned optional AI reasoning capability for the active project. */
    fun aiReasoningRuntime(): AthenaAiReasoningRuntimeService = services.aiReasoningRuntime()

    /** Resolves the runtime-owned AI reasoning session capability for the active project. */
    fun aiReasoningSessions(): AthenaAiReasoningSessionRuntimeService = services.aiReasoningSessions()

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
     * Returns the latest runtime-visible incremental recompute report when the active project has been mutated.
     */
    fun incrementalUpdateReport(): AthenaRuntimeIncrementalUpdateReport? {
        val compilation = compileActiveProject() as? CompilerCompilationSuccess ?: return null
        val report = compilation.incrementalUpdateReport ?: return null
        return AthenaRuntimeIncrementalUpdateReport(
            changedSemanticIds = report.affectedScope.changedSemanticIds,
            validationSemanticIds = report.affectedScope.validationSemanticIds,
            renderComponentSemanticIds = report.affectedScope.renderComponentSemanticIds,
            renderConnectionSemanticIds = report.affectedScope.renderConnectionSemanticIds,
            validationMode = report.validationMode.name.lowercase(),
            layoutMode = report.layoutMode.name.lowercase(),
            layoutScopedViewIds = report.layoutScopedViewIds,
            geometryMode = report.geometryMode.name.lowercase(),
            geometryScopedViewIds = report.geometryScopedViewIds,
            renderingMode = report.renderingMode.name.lowercase(),
            renderingViewIds = report.renderingViewIds,
        )
    }

    /**
     * Returns the latest runtime-owned semantic diff inspection captured after a mutation or history operation.
     */
    fun latestSemanticDiffInspection(): AthenaSemanticDiffInspection? = latestSemanticDiffInspection

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
     * Replaces the cached canonical project state after a successful runtime-owned semantic mutation.
     */
    internal fun replaceActiveProjectDocument(
        document: EngineeringDocument,
        changedSemanticIds: List<String>,
    ): CompilerCompilationSuccess {
        val currentCompilation = compileActiveProject()
        check(currentCompilation is CompilerCompilationSuccess) {
            "Cannot replace canonical project state when the active project is not in a compilable semantic state."
        }
        val affectedScope = document.planAffectedScope(changedSemanticIds)

        return compiler().recompute(
            source = currentCompilation.source,
            document = document,
            affectedScope = affectedScope,
            previousLayouts = currentCompilation.layouts,
            previousGeometries = currentCompilation.geometries,
            previousRendering = currentCompilation.rendering,
        ).also { recomputed ->
            activeCompilationSnapshot = recomputed
            invalidateProjectionSession()
        }
    }

    /**
     * Returns the current internal command-history state for runtime-owned mutation services.
     */
    internal fun commandHistoryState(): AthenaCommandHistoryState = commandHistoryState

    /**
     * Replaces the internal command-history state after one runtime-owned history transition.
     */
    internal fun replaceCommandHistoryState(historyState: AthenaCommandHistoryState) {
        commandHistoryState = historyState
    }

    /**
     * Returns the current internal guided authoring preview state for runtime-owned authoring services.
     */
    internal fun authoringSessionState(): AthenaAuthoringSessionState = authoringSessionState

    /**
     * Replaces the internal guided authoring preview state after one preview submission or decision.
     */
    internal fun replaceAuthoringSessionState(state: AthenaAuthoringSessionState) {
        authoringSessionState = state
    }

    /**
     * Returns the current internal Semantic Macro preview state for runtime-owned acceptance handoff.
     */
    internal fun semanticMacroPreviewSessionState(): AthenaSemanticMacroPreviewSessionState = semanticMacroPreviewSessionState

    /**
     * Replaces the internal Semantic Macro preview state after one preview refresh or acceptance decision.
     */
    internal fun replaceSemanticMacroPreviewSessionState(state: AthenaSemanticMacroPreviewSessionState) {
        semanticMacroPreviewSessionState = state
    }

    /**
     * Returns the current internal pending AI proposal state for runtime-owned optional AI surfaces.
     */
    internal fun aiProposalState(): AthenaAiProposalState = aiProposalState

    /**
     * Replaces the internal pending AI proposal state after one optional AI queue transition.
     */
    internal fun replaceAiProposalState(state: AthenaAiProposalState) {
        aiProposalState = state
    }

    /**
     * Returns the current internal AI reasoning proposal state for runtime-owned reasoning services.
     */
    internal fun aiReasoningProposalState(): AthenaAiReasoningProposalState = aiReasoningProposalState

    /**
     * Replaces the internal AI reasoning proposal state after one runtime-owned reasoning transition.
     */
    internal fun replaceAiReasoningProposalState(state: AthenaAiReasoningProposalState) {
        aiReasoningProposalState = state
    }

    /**
     * Returns the current internal AI reasoning session state for runtime-owned reasoning orchestration.
     */
    internal fun aiReasoningSessionState(): AthenaAiReasoningSessionState = aiReasoningSessionState

    /**
     * Replaces the internal AI reasoning session state after one runtime-owned reasoning submission.
     */
    internal fun replaceAiReasoningSessionState(state: AthenaAiReasoningSessionState) {
        aiReasoningSessionState = state
    }

    /**
     * Replaces the latest runtime-owned semantic diff inspection after one mutation or history transition.
     */
    internal fun replaceLatestSemanticDiffInspection(inspection: AthenaSemanticDiffInspection?) {
        latestSemanticDiffInspection = inspection
    }

    /**
     * Returns the current runtime-owned active projection view id when one has already been selected.
     */
    internal fun currentActiveProjectionViewId(): String? = activeProjectionViewId

    /**
     * Replaces the runtime-owned active projection view id after a successful switch.
     */
    internal fun replaceActiveProjectionViewId(viewId: String) {
        activeProjectionViewId = viewId
    }

    /**
     * Returns the current runtime-owned placement overrides for one projection view.
     */
    internal fun projectionPlacementOverrides(viewId: String): Map<String, AthenaGraphPlacement> {
        return projectionMetadataState.placementOverridesByView[viewId].orEmpty()
    }

    /**
     * Replaces one runtime-owned placement override for a projection-scoped semantic subject.
     */
    internal fun replaceProjectionPlacementOverride(
        viewId: String,
        semanticId: String,
        placement: AthenaGraphPlacement,
    ) {
        val currentOverrides = projectionMetadataState.placementOverridesByView[viewId].orEmpty()
        val nextOverrides = currentOverrides.toMutableMap().apply {
            put(semanticId, placement)
        }.toMap()
        projectionMetadataState = projectionMetadataState.copy(
            placementOverridesByView = projectionMetadataState.placementOverridesByView.toMutableMap().apply {
                put(viewId, nextOverrides)
            }.toMap(),
        )
        invalidateProjectionSession()
    }

    /**
     * Clears the cached runtime-owned projection session after one canonical input transition.
     */
    internal fun invalidateProjectionSession() {
        activeProjectionSessionSnapshot = null
    }
}

/**
 * Derives the minimal runtime-visible recompute scope for the current M1 command mutation path.
 */
private fun EngineeringDocument.planAffectedScope(changedSemanticIds: List<String>): CompilerAffectedScope {
    val normalizedChangedIds = changedSemanticIds.distinct().sorted()
    val portsById = ports.associateBy { port -> port.id.value }
    val connectionsById = connections.associateBy { connection -> connection.id.value }

    val validationSemanticIds = linkedSetOf<String>()
    val renderComponentSemanticIds = linkedSetOf<String>()
    val renderConnectionSemanticIds = linkedSetOf<String>()

    normalizedChangedIds.forEach { semanticId ->
        validationSemanticIds += semanticId
        when {
            semanticId.startsWith(CONNECTION_SEMANTIC_PREFIX) -> renderConnectionSemanticIds += semanticId
            semanticId.startsWith(COMPONENT_SEMANTIC_PREFIX) -> renderComponentSemanticIds += semanticId
        }

        portsById[semanticId]?.let { port ->
            port.includeOwnerScope(
                validationSemanticIds = validationSemanticIds,
                renderComponentSemanticIds = renderComponentSemanticIds,
            )
        }

        connectionsById[semanticId]?.let { connection ->
            renderConnectionSemanticIds += connection.id.value
            listOfNotNull(connection.from.resolvedIdentity?.value, connection.to.resolvedIdentity?.value)
                .forEach { portSemanticId ->
                    validationSemanticIds += portSemanticId
                    portsById[portSemanticId]?.includeOwnerScope(
                        validationSemanticIds = validationSemanticIds,
                        renderComponentSemanticIds = renderComponentSemanticIds,
                    )
                }
        }
    }

    return CompilerAffectedScope(
        changedSemanticIds = normalizedChangedIds,
        validationSemanticIds = validationSemanticIds.toList().sorted(),
        renderComponentSemanticIds = renderComponentSemanticIds.toList().sorted(),
        renderConnectionSemanticIds = renderConnectionSemanticIds.toList().sorted(),
    )
}

/**
 * Adds the owning component scope implied by one changed or referenced port.
 */
private fun EngineeringPort.includeOwnerScope(
    validationSemanticIds: MutableSet<String>,
    renderComponentSemanticIds: MutableSet<String>,
) {
    ownerReference.resolvedIdentity?.value?.let { ownerSemanticId ->
        validationSemanticIds += ownerSemanticId
        renderComponentSemanticIds += ownerSemanticId
    }
}

private const val COMPONENT_SEMANTIC_PREFIX = "component:"
private const val CONNECTION_SEMANTIC_PREFIX = "connection:"

/**
 * Runtime-owned projection metadata state for the current active project.
 *
 * This state is intentionally separate from canonical engineering truth so projection mutations can
 * remain governed without redefining the authored semantic document.
 */
private data class AthenaProjectionMetadataState(
    val placementOverridesByView: Map<String, Map<String, AthenaGraphPlacement>> = emptyMap(),
)

/**
 * Runtime-owned view of one incremental recompute cycle after a semantic command mutation.
 */
data class AthenaRuntimeIncrementalUpdateReport(
    val changedSemanticIds: List<String>,
    val validationSemanticIds: List<String>,
    val renderComponentSemanticIds: List<String>,
    val renderConnectionSemanticIds: List<String>,
    val validationMode: String,
    val layoutMode: String,
    val layoutScopedViewIds: List<String>,
    val geometryMode: String,
    val geometryScopedViewIds: List<String>,
    val renderingMode: String,
    val renderingViewIds: List<String>,
)
