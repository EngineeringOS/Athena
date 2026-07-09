package com.engineeringood.athena.runtime

import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticHistoryComparison
import com.engineeringood.athena.scm.SemanticHistoryRequest
import com.engineeringood.athena.scm.SemanticHistorySummary
import com.engineeringood.athena.scm.SemanticHistorySummaryGenerator
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity

/**
 * One baseline request consumed by the runtime-owned semantic-history projection flow.
 *
 * Pairing the semantic descriptor with the locator keeps runtime, LSP, and IDE seams aligned on
 * one vendor-neutral baseline request shape for package-history inspection.
 */
data class AthenaSemanticHistoryBaselineRequest(
    val descriptor: SemanticBaselineDescriptor,
    val locator: SemanticBaselineLocator,
)

/**
 * Runtime-owned semantic history state status published above baseline resolution.
 */
enum class AthenaSemanticHistoryStateStatus {
    READY,
    BASELINE_UNRESOLVED,
}

/**
 * Typed runtime-owned semantic history projection for one package and one baseline sequence.
 *
 * The state keeps baseline diagnostics and the generated package-history summary together so
 * downstream LSP and IDE seams can inspect package evolution without rebuilding semantic meaning.
 */
data class AthenaSemanticHistoryState(
    val packageId: PackageIdentifier,
    val baselineRequests: List<AthenaSemanticHistoryBaselineRequest>,
    val status: AthenaSemanticHistoryStateStatus,
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
    val historySummary: SemanticHistorySummary? = null,
) {
    /** True when the baseline sequence resolved without error diagnostics and history is available. */
    val isReady: Boolean
        get() = status == AthenaSemanticHistoryStateStatus.READY &&
            diagnostics.none { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.ERROR } &&
            historySummary != null
}

/**
 * Runtime-owned facade that resolves a baseline sequence and projects package-aware history.
 *
 * The service reuses the existing baseline and diff services plus the shared history summarizer so
 * package evolution stays anchored in JVM semantic authority instead of LSP or frontend logic.
 */
class AthenaSemanticHistoryStateService(
    private val baselineService: AthenaSemanticBaselineService = AthenaSemanticBaselineService(),
    private val diffService: AthenaSemanticDiffService = AthenaSemanticDiffService(),
    private val historySummaryGenerator: SemanticHistorySummaryGenerator = SemanticHistorySummaryGenerator(),
) {
    /**
     * Resolves one semantic history projection for [session], [packageId], and [baselineRequests].
     */
    fun inspect(
        session: RepositoryGraphSession,
        packageId: PackageIdentifier,
        baselineRequests: List<AthenaSemanticHistoryBaselineRequest>,
    ): AthenaSemanticHistoryState {
        val resolutions = baselineRequests.map { baselineRequest ->
            baselineRequest to baselineService.resolveBaseline(
                session = session,
                descriptor = baselineRequest.descriptor,
                locator = baselineRequest.locator,
            )
        }
        val diagnostics = resolutions
            .flatMap { (_, resolution) -> resolution.diagnostics }
            .distinct()
            .sortedWith(semanticDiagnosticComparator())
        if (resolutions.any { (_, resolution) -> !resolution.isResolved || resolution.snapshot == null }) {
            return AthenaSemanticHistoryState(
                packageId = packageId,
                baselineRequests = baselineRequests,
                status = AthenaSemanticHistoryStateStatus.BASELINE_UNRESOLVED,
                diagnostics = diagnostics,
            )
        }

        val comparisons = resolutions.map { (_, resolution) ->
            val baseline = checkNotNull(resolution.snapshot) {
                "Resolved baseline snapshot must be available before semantic history is summarized."
            }
            SemanticHistoryComparison(
                baseline = baseline,
                diff = diffService.compareAgainstBaseline(
                    session = session,
                    baseline = baseline,
                ),
            )
        }
        val request = SemanticHistoryRequest(
            packageId = packageId,
            baselineSequence = baselineRequests.map { baselineRequest -> baselineRequest.descriptor },
        )
        return AthenaSemanticHistoryState(
            packageId = packageId,
            baselineRequests = baselineRequests,
            status = AthenaSemanticHistoryStateStatus.READY,
            diagnostics = diagnostics,
            historySummary = historySummaryGenerator.summarize(
                request = request,
                comparisons = comparisons,
            ),
        )
    }
}

private fun semanticDiagnosticComparator(): Comparator<SemanticDiagnostic> {
    return compareBy<SemanticDiagnostic>(
        { diagnostic -> diagnostic.ruleId.value },
        { diagnostic -> diagnostic.provenance.file },
        { diagnostic -> diagnostic.provenance.startLine },
        { diagnostic -> diagnostic.provenance.startColumn },
        { diagnostic -> diagnostic.message },
    )
}
