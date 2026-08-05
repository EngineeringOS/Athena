package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.runtime.AthenaSemanticHistoryBaselineRequest
import com.engineeringood.athena.runtime.AthenaSemanticHistoryState
import com.engineeringood.athena.runtime.AthenaSemanticHistoryStateStatus
import com.engineeringood.athena.runtime.AthenaSemanticScmState
import com.engineeringood.athena.runtime.AthenaSemanticScmStateStatus
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticCommitEntry
import com.engineeringood.athena.scm.SemanticCommitFactReference
import com.engineeringood.athena.scm.SemanticCommitIntent
import com.engineeringood.athena.scm.SemanticDependencyMovement
import com.engineeringood.athena.scm.SemanticHistoryEntry
import com.engineeringood.athena.scm.SemanticHistorySummary
import com.engineeringood.athena.scm.SemanticPackageVersionMeaning
import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewEntry
import com.engineeringood.athena.scm.SemanticReviewFactReference
import com.engineeringood.athena.scm.SemanticReviewSummary
import com.engineeringood.athena.scm.SemanticValidationMovement
import com.engineeringood.athena.semantics.core.SemanticDiagnostic

/**
 * Parameters for the Athena-owned semantic SCM request.
 *
 * The request keeps baseline selection explicit at the LSP boundary while staying independent from
 * Git-native or Theia-native types.
 */
data class AthenaSemanticScmStateParams(
    val adapterId: String,
    val locator: String,
    val locatorLabel: String? = null,
    val baselineId: String? = null,
    val baselineLabel: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * One baseline request projected for package-history inspection through Athena LSP.
 */
data class AthenaSemanticHistoryBaselineParams(
    val adapterId: String,
    val locator: String,
    val locatorLabel: String? = null,
    val baselineId: String? = null,
    val baselineLabel: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Parameters for the Athena-owned semantic history request.
 */
data class AthenaSemanticHistoryStateParams(
    val packageName: String,
    val packageVersion: String? = null,
    val baselines: List<AthenaSemanticHistoryBaselineParams> = emptyList(),
)

/**
 * One semantic package reference projected through the Athena LSP boundary.
 */
data class AthenaSemanticPackagePayload(
    val name: String,
    val version: String? = null,
)

/**
 * One inspectable fact reference projected for review and commit entries.
 */
data class AthenaSemanticFactReferencePayload(
    val kind: String,
    val identifier: String,
    val affectedPackage: AthenaSemanticPackagePayload? = null,
    val subjectIdentity: String? = null,
)

/**
 * One inspectable semantic diagnostic projected through the Athena LSP boundary.
 */
data class AthenaSemanticScmDiagnosticPayload(
    val severity: String,
    val ruleId: String,
    val message: String,
    val provenance: String,
)

/**
 * One inspectable review entry projected through the Athena LSP boundary.
 */
data class AthenaSemanticReviewEntryPayload(
    val kind: String,
    val message: String,
    val affectedPackage: AthenaSemanticPackagePayload? = null,
    val subjectIdentity: String? = null,
    val factReferences: List<AthenaSemanticFactReferencePayload> = emptyList(),
)

/**
 * One inspectable additive review enrichment projected through the Athena LSP boundary.
 */
data class AthenaSemanticReviewEnrichmentPayload(
    val pluginId: String,
    val kind: String,
    val message: String,
    val affectedPackage: AthenaSemanticPackagePayload? = null,
    val subjectIdentity: String? = null,
    val factReferences: List<AthenaSemanticFactReferencePayload> = emptyList(),
)

/**
 * Review summary projection returned through the Athena LSP boundary.
 */
data class AthenaSemanticReviewPayload(
    val baselineId: String,
    val baselineLabel: String,
    val affectedPackages: List<AthenaSemanticPackagePayload>,
    val entryCount: Int,
    val enrichmentCount: Int,
    val entries: List<AthenaSemanticReviewEntryPayload>,
    val enrichments: List<AthenaSemanticReviewEnrichmentPayload>,
)

/**
 * One inspectable commit-preparation entry projected through the Athena LSP boundary.
 */
data class AthenaSemanticCommitEntryPayload(
    val kind: String,
    val message: String,
    val affectedPackage: AthenaSemanticPackagePayload? = null,
    val subjectIdentity: String? = null,
    val factReferences: List<AthenaSemanticFactReferencePayload> = emptyList(),
)

/**
 * Commit-intent projection returned through the Athena LSP boundary.
 */
data class AthenaSemanticCommitPayload(
    val baselineId: String,
    val baselineLabel: String,
    val affectedPackages: List<AthenaSemanticPackagePayload>,
    val summary: String? = null,
    val entryCount: Int,
    val entries: List<AthenaSemanticCommitEntryPayload>,
)

/**
 * Runtime-owned semantic SCM projection returned through the Athena LSP boundary.
 */
data class AthenaSemanticScmStatePayload(
    val status: String,
    val adapterId: String,
    val locator: String,
    val locatorLabel: String? = null,
    val baselineId: String,
    val baselineLabel: String,
    val semanticPath: String,
    val diagnostics: List<AthenaSemanticScmDiagnosticPayload>,
    val review: AthenaSemanticReviewPayload? = null,
    val commit: AthenaSemanticCommitPayload? = null,
)

/**
 * Echoed baseline request payload for package-history projections.
 */
data class AthenaSemanticHistoryBaselinePayload(
    val adapterId: String,
    val locator: String,
    val locatorLabel: String? = null,
    val baselineId: String,
    val baselineLabel: String,
)

/**
 * One package-version meaning projection returned through Athena LSP.
 */
data class AthenaSemanticPackageVersionPayload(
    val packageId: AthenaSemanticPackagePayload,
    val baselineVersion: String? = null,
    val currentVersion: String? = null,
    val changeKind: String,
)

/**
 * One dependency-movement projection returned through Athena LSP.
 */
data class AthenaSemanticDependencyMovementPayload(
    val packageId: AthenaSemanticPackagePayload,
    val kind: String,
    val baselineVersion: String? = null,
    val currentVersion: String? = null,
    val message: String,
)

/**
 * One validation-movement projection returned through Athena LSP.
 */
data class AthenaSemanticValidationMovementPayload(
    val baselineErrorCount: Int,
    val baselineWarningCount: Int,
    val currentErrorCount: Int,
    val currentWarningCount: Int,
    val baselineContinuationDecision: String? = null,
    val currentContinuationDecision: String? = null,
    val message: String,
)

/**
 * One package-history entry projection returned through Athena LSP.
 */
data class AthenaSemanticHistoryEntryPayload(
    val kind: String,
    val baselineId: String,
    val baselineLabel: String,
    val packageVersion: AthenaSemanticPackageVersionPayload,
    val changeCategory: String? = null,
    val releaseRelevance: String,
    val contractBreakRisk: String,
    val message: String,
    val dependencyMovements: List<AthenaSemanticDependencyMovementPayload> = emptyList(),
    val validationMovement: AthenaSemanticValidationMovementPayload? = null,
    val authoredChangeCount: Int,
    val derivedConsequenceCount: Int,
)

/**
 * Package-history summary projection returned through Athena LSP.
 */
data class AthenaSemanticHistoryPayload(
    val packageId: AthenaSemanticPackagePayload,
    val baselineCount: Int,
    val packageLineage: List<AthenaSemanticPackageVersionPayload>,
    val validationMovements: List<AthenaSemanticValidationMovementPayload>,
    val entryCount: Int,
    val releaseRelevance: String,
    val contractBreakRisk: String,
    val summary: String? = null,
    val entries: List<AthenaSemanticHistoryEntryPayload>,
)

/**
 * Runtime-owned semantic history state projected through the Athena LSP boundary.
 */
data class AthenaSemanticHistoryStatePayload(
    val status: String,
    val semanticPath: String,
    val packageId: AthenaSemanticPackagePayload,
    val baselines: List<AthenaSemanticHistoryBaselinePayload>,
    val diagnostics: List<AthenaSemanticScmDiagnosticPayload>,
    val history: AthenaSemanticHistoryPayload? = null,
)

internal fun AthenaSemanticScmStateParams.toBaselineDescriptor(): SemanticBaselineDescriptor {
    return semanticBaselineDescriptor(
        adapterId = adapterId,
        locator = locator,
        baselineId = baselineId,
        baselineLabel = baselineLabel,
        metadata = metadata,
    )
}

internal fun AthenaSemanticScmStateParams.toBaselineLocator(): SemanticBaselineLocator {
    return semanticBaselineLocator(
        adapterId = adapterId,
        locator = locator,
        locatorLabel = locatorLabel,
        metadata = metadata,
    )
}

internal fun AthenaSemanticHistoryStateParams.toPackageId(): PackageIdentifier {
    val normalizedPackageName = packageName.trim()
    require(normalizedPackageName.isNotEmpty()) { "Semantic history request requires a packageName." }
    return PackageIdentifier(
        name = normalizedPackageName,
        version = packageVersion?.trim()?.takeIf { value -> value.isNotEmpty() },
    )
}

internal fun AthenaSemanticHistoryStateParams.toBaselineRequests(): List<AthenaSemanticHistoryBaselineRequest> {
    return baselines.map { baseline ->
        AthenaSemanticHistoryBaselineRequest(
            descriptor = semanticBaselineDescriptor(
                adapterId = baseline.adapterId,
                locator = baseline.locator,
                baselineId = baseline.baselineId,
                baselineLabel = baseline.baselineLabel,
                metadata = baseline.metadata,
            ),
            locator = semanticBaselineLocator(
                adapterId = baseline.adapterId,
                locator = baseline.locator,
                locatorLabel = baseline.locatorLabel,
                metadata = baseline.metadata,
            ),
        )
    }
}

internal fun AthenaSemanticScmState.toPayload(semanticPath: String): AthenaSemanticScmStatePayload {
    return AthenaSemanticScmStatePayload(
        status = status.toProtocolValue(),
        adapterId = locator.adapterId,
        locator = locator.locator,
        locatorLabel = locator.label,
        baselineId = descriptor.baselineId,
        baselineLabel = descriptor.label,
        semanticPath = semanticPath,
        diagnostics = diagnostics.map { diagnostic -> diagnostic.toPayload() },
        review = reviewSummary?.toPayload(),
        commit = commitIntent?.toPayload(),
    )
}

internal fun AthenaSemanticHistoryState.toPayload(semanticPath: String): AthenaSemanticHistoryStatePayload {
    return AthenaSemanticHistoryStatePayload(
        status = status.toProtocolValue(),
        semanticPath = semanticPath,
        packageId = packageId.toPayload(),
        baselines = baselineRequests.map { baselineRequest ->
            AthenaSemanticHistoryBaselinePayload(
                adapterId = baselineRequest.locator.adapterId,
                locator = baselineRequest.locator.locator,
                locatorLabel = baselineRequest.locator.label,
                baselineId = baselineRequest.descriptor.baselineId,
                baselineLabel = baselineRequest.descriptor.label,
            )
        },
        diagnostics = diagnostics.map { diagnostic -> diagnostic.toPayload() },
        history = historySummary?.toPayload(),
    )
}

internal fun SemanticReviewSummary.toPayload(): AthenaSemanticReviewPayload {
    return AthenaSemanticReviewPayload(
        baselineId = baseline.baselineId,
        baselineLabel = baseline.label,
        affectedPackages = affectedPackages.map { packageId -> packageId.toPayload() },
        entryCount = entries.size,
        enrichmentCount = enrichments.size,
        entries = entries.map { entry -> entry.toPayload() },
        enrichments = enrichments.map { enrichment -> enrichment.toPayload() },
    )
}

internal fun SemanticCommitIntent.toPayload(): AthenaSemanticCommitPayload {
    return AthenaSemanticCommitPayload(
        baselineId = baseline.baselineId,
        baselineLabel = baseline.label,
        affectedPackages = affectedPackages.map { packageId -> packageId.toPayload() },
        summary = summary,
        entryCount = entries.size,
        entries = entries.map { entry -> entry.toPayload() },
    )
}

private fun SemanticHistorySummary.toPayload(): AthenaSemanticHistoryPayload {
    return AthenaSemanticHistoryPayload(
        packageId = packageId.toPayload(),
        baselineCount = baselineSequence.size,
        packageLineage = packageLineage.map { lineage -> lineage.toPayload() },
        validationMovements = validationMovements.map { movement -> movement.toPayload() },
        entryCount = entries.size,
        releaseRelevance = releaseRelevance.toProtocolValue(),
        contractBreakRisk = contractBreakRisk.toProtocolValue(),
        summary = summary,
        entries = entries.map { entry -> entry.toPayload() },
    )
}

private fun PackageIdentifier.toPayload(): AthenaSemanticPackagePayload {
    return AthenaSemanticPackagePayload(
        name = name,
        version = version,
    )
}

private fun SemanticDiagnostic.toPayload(): AthenaSemanticScmDiagnosticPayload {
    return AthenaSemanticScmDiagnosticPayload(
        severity = severity.name.lowercase(),
        ruleId = ruleId.value,
        message = message,
        provenance = "${provenance.file}:${provenance.startLine}:${provenance.startColumn}",
    )
}

private fun AthenaSemanticScmStateStatus.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun AthenaSemanticHistoryStateStatus.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun SemanticReviewEntry.toPayload(): AthenaSemanticReviewEntryPayload {
    return AthenaSemanticReviewEntryPayload(
        kind = kind.name.lowercase().replace('_', '-'),
        message = message,
        affectedPackage = affectedPackage?.toPayload(),
        subjectIdentity = subjectIdentity?.value,
        factReferences = factReferences.map { reference -> reference.toPayload() },
    )
}

private fun SemanticReviewEnrichment.toPayload(): AthenaSemanticReviewEnrichmentPayload {
    return AthenaSemanticReviewEnrichmentPayload(
        pluginId = pluginId,
        kind = kind.name.lowercase().replace('_', '-'),
        message = message,
        affectedPackage = affectedPackage?.toPayload(),
        subjectIdentity = subjectIdentity?.value,
        factReferences = factReferences.map { reference -> reference.toPayload() },
    )
}

private fun SemanticCommitEntry.toPayload(): AthenaSemanticCommitEntryPayload {
    return AthenaSemanticCommitEntryPayload(
        kind = kind.name.lowercase().replace('_', '-'),
        message = message,
        affectedPackage = affectedPackage?.toPayload(),
        subjectIdentity = subjectIdentity?.value,
        factReferences = factReferences.map { reference -> reference.toPayload() },
    )
}

private fun SemanticReviewFactReference.toPayload(): AthenaSemanticFactReferencePayload {
    return AthenaSemanticFactReferencePayload(
        kind = factKind.name.lowercase().replace('_', '-'),
        identifier = identifier,
        affectedPackage = affectedPackage?.toPayload(),
        subjectIdentity = subjectIdentity?.value,
    )
}

private fun SemanticCommitFactReference.toPayload(): AthenaSemanticFactReferencePayload {
    return AthenaSemanticFactReferencePayload(
        kind = factKind.name.lowercase().replace('_', '-'),
        identifier = identifier,
        affectedPackage = affectedPackage?.toPayload(),
        subjectIdentity = subjectIdentity?.value,
    )
}

private fun SemanticPackageVersionMeaning.toPayload(): AthenaSemanticPackageVersionPayload {
    return AthenaSemanticPackageVersionPayload(
        packageId = packageId.toPayload(),
        baselineVersion = baselineVersion,
        currentVersion = currentVersion,
        changeKind = changeKind.toProtocolValue(),
    )
}

private fun SemanticDependencyMovement.toPayload(): AthenaSemanticDependencyMovementPayload {
    return AthenaSemanticDependencyMovementPayload(
        packageId = packageId.toPayload(),
        kind = kind.toProtocolValue(),
        baselineVersion = baselineVersion,
        currentVersion = currentVersion,
        message = message,
    )
}

private fun SemanticValidationMovement.toPayload(): AthenaSemanticValidationMovementPayload {
    return AthenaSemanticValidationMovementPayload(
        baselineErrorCount = baselineErrorCount,
        baselineWarningCount = baselineWarningCount,
        currentErrorCount = currentErrorCount,
        currentWarningCount = currentWarningCount,
        baselineContinuationDecision = baselineContinuationDecision?.name?.lowercase()?.replace('_', '-'),
        currentContinuationDecision = currentContinuationDecision?.name?.lowercase()?.replace('_', '-'),
        message = message,
    )
}

private fun SemanticHistoryEntry.toPayload(): AthenaSemanticHistoryEntryPayload {
    return AthenaSemanticHistoryEntryPayload(
        kind = kind.toProtocolValue(),
        baselineId = baseline.baselineId,
        baselineLabel = baseline.label,
        packageVersion = packageVersion.toPayload(),
        changeCategory = changeCategory?.toProtocolValue(),
        releaseRelevance = releaseRelevance.toProtocolValue(),
        contractBreakRisk = contractBreakRisk.toProtocolValue(),
        message = message,
        dependencyMovements = dependencyMovements.map { movement -> movement.toPayload() },
        validationMovement = validationMovement?.toPayload(),
        authoredChangeCount = authoredChanges.size,
        derivedConsequenceCount = derivedConsequences.size,
    )
}

private fun Enum<*>.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun semanticBaselineDescriptor(
    adapterId: String,
    locator: String,
    baselineId: String?,
    baselineLabel: String?,
    metadata: Map<String, String>,
): SemanticBaselineDescriptor {
    val normalizedAdapterId = adapterId.trim()
    val normalizedLocator = locator.trim()
    return SemanticBaselineDescriptor(
        baselineId = baselineId?.takeIf { value -> value.isNotBlank() }
            ?: "$normalizedAdapterId:$normalizedLocator",
        label = baselineLabel?.takeIf { value -> value.isNotBlank() }
            ?: "Semantic comparison baseline",
        metadata = metadata
            .filterKeys { key -> key.isNotBlank() }
            .mapValues { (_, value) -> value.trim() },
    )
}

private fun semanticBaselineLocator(
    adapterId: String,
    locator: String,
    locatorLabel: String?,
    metadata: Map<String, String>,
): SemanticBaselineLocator {
    return SemanticBaselineLocator(
        adapterId = adapterId.trim(),
        locator = locator.trim(),
        label = locatorLabel?.takeIf { value -> value.isNotBlank() },
        metadata = metadata
            .filterKeys { key -> key.isNotBlank() }
            .mapValues { (_, value) -> value.trim() },
    )
}
