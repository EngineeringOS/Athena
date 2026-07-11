package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerIncrementalUpdateReport
import com.engineeringood.athena.compiler.CompilerRenderingBlocked
import com.engineeringood.athena.compiler.CompilerRenderingResult
import com.engineeringood.athena.compiler.CompilerRenderingSuccess
import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.layout.LayoutDocument
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import java.nio.file.Files
import java.nio.file.Path

/**
 * Inspectable result of one runtime-owned source mutation evaluation request.
 *
 * Source-originated edits stay preview-only in M8 Story 1.3, but they still converge on the same
 * mutation category, outcome, and validation vocabulary used by graph-originated mutation paths.
 */
sealed interface AthenaSourceMutationResult : AthenaMutationResult

/**
 * Accepted source-originated semantic change evaluated against the current canonical runtime state.
 */
data class AthenaSourceMutationAccepted(
    override val projectName: String,
    val beforeDocument: EngineeringDocument,
    val afterDocument: EngineeringDocument,
    val changedSemanticIds: List<String>,
    val inspection: AthenaSemanticDiffInspection,
    val semanticReview: AthenaSemanticMutationReview? = null,
) : AthenaSourceMutationResult {
    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.ACCEPTED
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

/**
 * Explicit rejection when the source evaluation request is not allowed to act on the active canonical project.
 */
data class AthenaSourceMutationRejected(
    override val projectName: String,
    val reason: String,
    val changedSemanticIds: List<String> = emptyList(),
) : AthenaSourceMutationResult {
    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.REJECTED
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

/**
 * Validation feedback emitted when a dirty source buffer cannot yet be treated as an accepted semantic mutation.
 */
data class AthenaSourceMutationValidationFeedbackResult(
    override val projectName: String,
    override val validationFeedback: List<AthenaMutationValidationFeedback>,
    val changedSemanticIds: List<String> = emptyList(),
) : AthenaSourceMutationResult {
    init {
        require(validationFeedback.isNotEmpty()) {
            "Validation feedback results must include at least one feedback item."
        }
    }

    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.VALIDATION_FEEDBACK
}

/**
 * Explicit runtime-unavailable result when Athena has no usable canonical state to compare dirty source against.
 */
data class AthenaSourceMutationUnavailable(
    override val projectName: String,
    val reason: String,
) : AthenaSourceMutationResult {
    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.UNAVAILABLE
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

/**
 * Runtime-owned evaluator that compares one dirty source compilation against the active canonical project state.
 *
 * The service does not write files, replace canonical cache, or append command history in Story 1.3. It only
 * normalizes dirty source meaning into the shared mutation-result language so later review and graph work can
 * consume one semantic boundary.
 */
class AthenaSourceMutationRuntimeService internal constructor() {
    /**
     * Evaluates [compilation] for [sourcePath] against the active canonical project state in [context].
     */
    fun evaluate(
        context: AthenaExecutionContext,
        sourcePath: Path,
        compilation: CompilerCompilationResult,
    ): AthenaSourceMutationResult {
        if (!sourcePath.referencesSameAuthoritativeSourceAs(context.project.sourcePath)) {
            return AthenaSourceMutationRejected(
                projectName = context.project.name,
                reason = "Source mutation evaluation only accepts the authoritative active source path `${context.project.sourcePath}`.",
            )
        }

        val canonicalCompilation = context.compileActiveProject()
        if (canonicalCompilation is CompilerCompilationParseFailure) {
            return AthenaSourceMutationUnavailable(
                projectName = context.project.name,
                reason = "Canonical runtime state is unavailable because `${context.project.sourcePath}` does not compile: " +
                    canonicalCompilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )
        }
        val canonicalSuccess = canonicalCompilation as CompilerCompilationSuccess
        if (!canonicalSuccess.semanticResult.isSemanticallyValid) {
            return AthenaSourceMutationUnavailable(
                projectName = context.project.name,
                reason = "Canonical runtime state is unavailable because `${context.project.sourcePath}` is not semantically valid: " +
                    canonicalSuccess.semanticResult.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )
        }

        return when (compilation) {
            is CompilerCompilationParseFailure -> AthenaSourceMutationValidationFeedbackResult(
                projectName = context.project.name,
                validationFeedback = compilation.diagnostics.map(CompilerSyntaxDiagnostic::toMutationValidationFeedback),
            )

            is CompilerCompilationSuccess -> {
                if (compilation.semanticResult.diagnostics.isNotEmpty()) {
                    AthenaSourceMutationValidationFeedbackResult(
                        projectName = context.project.name,
                        validationFeedback = compilation.semanticResult.diagnostics.map(SemanticDiagnostic::toMutationValidationFeedback),
                    )
                } else {
                    val beforeDocument = canonicalSuccess.document
                    val afterDocument = compilation.document
                    val changedSemanticIds = detectChangedSemanticIds(beforeDocument, afterDocument)
                    AthenaSourceMutationAccepted(
                        projectName = context.project.name,
                        beforeDocument = beforeDocument,
                        afterDocument = afterDocument,
                        changedSemanticIds = changedSemanticIds,
                        inspection = buildSemanticDiffInspection(
                            projectName = context.project.name,
                            source = AthenaSemanticDiffInspectionSource.SOURCE,
                            affectedCommandIds = emptyList(),
                            affectedSemanticIds = changedSemanticIds,
                            beforeDocument = beforeDocument,
                            afterDocument = afterDocument,
                            history = context.commandRuntime().history(context),
                            projectionConsequences = projectionConsequencesFor(
                                beforeCompilation = canonicalSuccess,
                                afterCompilation = compilation,
                                changedSemanticIds = changedSemanticIds,
                            ),
                        ),
                        semanticReview = context.semanticMutationReviews().summarizeAcceptedMutation(
                            context = context,
                            beforeDocument = beforeDocument,
                            afterDocument = afterDocument,
                            beforeValidationResult = canonicalSuccess.semanticResult,
                            afterValidationResult = compilation.semanticResult,
                        ),
                    )
                }
            }
        }
    }
}

private fun projectionConsequencesFor(
    beforeCompilation: CompilerCompilationSuccess,
    afterCompilation: CompilerCompilationSuccess,
    changedSemanticIds: List<String>,
): List<AthenaProjectionRefreshConsequence> {
    val normalizedSemanticIds = changedSemanticIds.distinct().sorted()
    if (normalizedSemanticIds.isEmpty()) {
        return emptyList()
    }

    val layoutViewIds = changedViewIds(
        beforeViews = beforeCompilation.layouts.associateBy { document -> document.view.id },
        afterViews = afterCompilation.layouts.associateBy { document -> document.view.id },
    )
    val geometryViewIds = changedViewIds(
        beforeViews = beforeCompilation.geometries.associateBy(GeometryDocument::viewId),
        afterViews = afterCompilation.geometries.associateBy(GeometryDocument::viewId),
    )
    val projectionViewIds = changedViewIds(
        beforeViews = beforeCompilation.projections.associateBy { document -> document.view.id },
        afterViews = afterCompilation.projections.associateBy { document -> document.view.id },
    )
    val renderingViewIds = changedRenderingViewIds(beforeCompilation.rendering, afterCompilation.rendering)

    return buildList {
        if (layoutViewIds.isNotEmpty()) {
            add(
                AthenaProjectionRefreshConsequence(
                    layer = AthenaProjectionRefreshConsequenceLayer.LAYOUT,
                    mode = null,
                    affectedViewIds = layoutViewIds,
                    affectedSemanticIds = normalizedSemanticIds,
                ),
            )
        }
        if (geometryViewIds.isNotEmpty() || projectionViewIds.isNotEmpty()) {
            add(
                AthenaProjectionRefreshConsequence(
                    layer = AthenaProjectionRefreshConsequenceLayer.GEOMETRY,
                    mode = null,
                    affectedViewIds = (geometryViewIds + projectionViewIds).distinct().sorted(),
                    affectedSemanticIds = normalizedSemanticIds,
                ),
            )
        }
        if (renderingViewIds.isNotEmpty()) {
            add(
                AthenaProjectionRefreshConsequence(
                    layer = AthenaProjectionRefreshConsequenceLayer.RENDERING,
                    mode = null,
                    affectedViewIds = renderingViewIds,
                    affectedSemanticIds = normalizedSemanticIds,
                ),
            )
        }
    }
}

private fun Path.referencesSameAuthoritativeSourceAs(authoritativePath: Path): Boolean {
    return runCatching {
        Files.isSameFile(this, authoritativePath)
    }.getOrDefault(false)
}

private fun detectChangedSemanticIds(
    beforeDocument: EngineeringDocument,
    afterDocument: EngineeringDocument,
): List<String> {
    val beforeSnapshots = beforeDocument.semanticSnapshotMap()
    val afterSnapshots = afterDocument.semanticSnapshotMap()
    val changedSemanticIds = (beforeSnapshots.keys + afterSnapshots.keys)
        .distinct()
        .filter { semanticId -> beforeSnapshots[semanticId] != afterSnapshots[semanticId] }
        .toMutableSet()

    beforeDocument.connections
        .asSequence()
        .filter { connection -> connection.id.value in changedSemanticIds }
        .flatMap { connection -> connection.relatedSemanticIds().asSequence() }
        .forEach(changedSemanticIds::add)
    afterDocument.connections
        .asSequence()
        .filter { connection -> connection.id.value in changedSemanticIds }
        .flatMap { connection -> connection.relatedSemanticIds().asSequence() }
        .forEach(changedSemanticIds::add)

    return changedSemanticIds.sorted()
}

private data class AthenaSemanticSnapshot(
    val kind: String,
    val signature: String,
)

private fun EngineeringDocument.semanticSnapshotMap(): Map<String, AthenaSemanticSnapshot> {
    return buildMap {
        put(
            system.id.value,
            AthenaSemanticSnapshot(
                kind = "system",
                signature = system.name,
            ),
        )
        components.forEach { component ->
            put(component.id.value, component.snapshot())
        }
        ports.forEach { port ->
            put(port.id.value, port.snapshot())
        }
        connections.forEach { connection ->
            put(connection.id.value, connection.snapshot())
        }
    }
}

private fun EngineeringComponent.snapshot(): AthenaSemanticSnapshot {
    return AthenaSemanticSnapshot(
        kind = "component",
        signature = listOf(
            name,
            kind,
            properties.normalizedSignature(),
        ).joinToString(separator = "|"),
    )
}

private fun EngineeringPort.snapshot(): AthenaSemanticSnapshot {
    return AthenaSemanticSnapshot(
        kind = "port",
        signature = listOf(
            ownerReference.authoredPath.joinToString("."),
            name,
            properties.normalizedSignature(),
        ).joinToString(separator = "|"),
    )
}

private fun EngineeringConnection.snapshot(): AthenaSemanticSnapshot {
    return AthenaSemanticSnapshot(
        kind = "connection",
        signature = listOf(
            from.authoredPath.joinToString("."),
            from.resolvedIdentity?.value.orEmpty(),
            to.authoredPath.joinToString("."),
            to.resolvedIdentity?.value.orEmpty(),
        ).joinToString(separator = "|"),
    )
}

private fun EngineeringConnection.relatedSemanticIds(): List<String> {
    return buildList {
        add(id.value)
        from.resolvedIdentity?.value?.let(::add)
        to.resolvedIdentity?.value?.let(::add)
    }.distinct()
}

private fun List<com.engineeringood.athena.ir.EngineeringProperty>.normalizedSignature(): String {
    return map { property ->
        "${property.name}=${property.value.normalizedValue()}"
    }.sorted().joinToString(separator = "|")
}

private fun EngineeringPropertyValue.normalizedValue(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> "symbol:$text"
        is EngineeringPropertyValue.Text -> "text:$text"
    }
}

private fun CompilerSyntaxDiagnostic.toMutationValidationFeedback(): AthenaMutationValidationFeedback {
    return AthenaMutationValidationFeedback(
        code = "compiler.syntax",
        message = message,
        severity = AthenaMutationValidationFeedbackSeverity.ERROR,
    )
}

private fun SemanticDiagnostic.toMutationValidationFeedback(): AthenaMutationValidationFeedback {
    return AthenaMutationValidationFeedback(
        code = ruleId.value,
        message = message,
        severity = severity.toMutationValidationFeedbackSeverity(),
        relatedSemanticIds = listOfNotNull(subjectIdentity?.value),
    )
}

private fun SemanticDiagnosticSeverity.toMutationValidationFeedbackSeverity(): AthenaMutationValidationFeedbackSeverity {
    return when (this) {
        SemanticDiagnosticSeverity.ERROR -> AthenaMutationValidationFeedbackSeverity.ERROR
        SemanticDiagnosticSeverity.WARNING -> AthenaMutationValidationFeedbackSeverity.WARNING
    }
}

private fun CompilerIncrementalUpdateReport.toProjectionConsequences(): List<AthenaProjectionRefreshConsequence> {
    val normalizedSemanticIds = affectedScope.changedSemanticIds.distinct().sorted()
    return buildList {
        add(
            AthenaProjectionRefreshConsequence(
                layer = AthenaProjectionRefreshConsequenceLayer.LAYOUT,
                mode = layoutMode.name.lowercase(),
                affectedViewIds = layoutScopedViewIds,
                affectedSemanticIds = normalizedSemanticIds,
            ),
        )
        add(
            AthenaProjectionRefreshConsequence(
                layer = AthenaProjectionRefreshConsequenceLayer.GEOMETRY,
                mode = geometryMode.name.lowercase(),
                affectedViewIds = geometryScopedViewIds,
                affectedSemanticIds = normalizedSemanticIds,
            ),
        )
        if (renderingViewIds.isNotEmpty()) {
            add(
                AthenaProjectionRefreshConsequence(
                    layer = AthenaProjectionRefreshConsequenceLayer.RENDERING,
                    mode = renderingMode.name.lowercase(),
                    affectedViewIds = renderingViewIds,
                    affectedSemanticIds = normalizedSemanticIds,
                ),
            )
        }
    }
}

private fun <T> changedViewIds(
    beforeViews: Map<String, T>,
    afterViews: Map<String, T>,
): List<String> {
    return (beforeViews.keys + afterViews.keys)
        .distinct()
        .filter { viewId -> beforeViews[viewId] != afterViews[viewId] }
        .sorted()
}

private fun changedRenderingViewIds(
    beforeRendering: CompilerRenderingResult,
    afterRendering: CompilerRenderingResult,
): List<String> {
    if (beforeRendering == afterRendering) {
        return emptyList()
    }
    return (beforeRendering.viewIds() + afterRendering.viewIds()).distinct().sorted()
}

private fun CompilerRenderingResult.viewIds(): List<String> {
    return when (this) {
        is CompilerRenderingSuccess -> listOf(viewId).filter(String::isNotBlank)
        is CompilerRenderingBlocked -> emptyList()
    }
}
