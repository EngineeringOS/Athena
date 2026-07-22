package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringPropertyName
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringValue
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.AuthoringSourceEditEvidence
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanned
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanner
import com.engineeringood.athena.compiler.BackendEntityCreationPlanningRequest
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.domain.electricalruntime.electricalEngineeringConceptTemplates
import com.engineeringood.athena.domain.electricalruntime.ElectricalEntityCreationProjectionAuthority
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.runtime.AthenaAuthoringSessionRecord
import com.engineeringood.athena.runtime.AthenaComponentKnowledgeReady
import com.engineeringood.athena.runtime.GovernedEntityCreationPreviewRequest
import com.engineeringood.athena.runtime.GovernedEntityCreationPreviewResult
import com.engineeringood.athena.runtime.GovernedEntityCreationPreviewService
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId

/**
 * One line/character position inside an Athena-authored source edit payload.
 *
 * Positions use the LSP-style zero-based convention.
 */
data class AthenaAuthoringSourcePositionPayload(
    val line: Int,
    val character: Int,
)

/**
 * One zero-width or replacement range carried by a backend-generated authoring source edit.
 */
data class AthenaAuthoringSourceRangePayload(
    val start: AthenaAuthoringSourcePositionPayload,
    val end: AthenaAuthoringSourcePositionPayload,
)

/**
 * One backend-generated source edit emitted after an accepted guided authoring preview.
 *
 * The first M15 insertion slice stays source-backed. The backend returns the canonical authored
 * text consequence and the frontend applies it into the active `.athena` buffer.
 */
data class AthenaAuthoringSourceEditPayload(
    val uri: String,
    val startOffset: Int? = null,
    val endOffset: Int? = null,
    val range: AthenaAuthoringSourceRangePayload,
    val newText: String,
    val selectionStartOffset: Int? = null,
    val selectionEndOffset: Int? = null,
    val selectionRange: AthenaAuthoringSourceRangePayload? = null,
    val suggestedSemanticId: String? = null,
    val revisionGuard: AthenaAuthoringRevisionGuardPayload? = null,
    val appliedByAuthority: Boolean = false,
)

data class AthenaAuthoringRevisionGuardPayload(
    val semanticSnapshotId: String,
    val sourceUri: String,
    val documentVersion: Int,
    val contentSha256: String,
)

internal fun acceptedCreateEntitySourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
    componentKnowledge: AthenaComponentKnowledgeReady?,
): AthenaAuthoringSourceEditPayload? {
    return createEntitySourceEdit(
        trackedDocument = trackedDocument,
        record = record,
        componentKnowledge = componentKnowledge,
        requiredStatus = AuthoringPreviewStatus.ACCEPTED,
    )
}

internal fun previewCreateEntitySourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
    componentKnowledge: AthenaComponentKnowledgeReady?,
): AthenaAuthoringSourceEditPayload? {
    return createEntitySourceEdit(
        trackedDocument = trackedDocument,
        record = record,
        componentKnowledge = componentKnowledge,
        requiredStatus = AuthoringPreviewStatus.PENDING_REVIEW,
    )
}

private fun createEntitySourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
    componentKnowledge: AthenaComponentKnowledgeReady?,
    requiredStatus: AuthoringPreviewStatus,
): AthenaAuthoringSourceEditPayload? {
    if (record.preview.status != requiredStatus) {
        return null
    }
    val intent = record.intent as? CreateSemanticEntityIntent ?: return null
    if (trackedDocument.compilation !is CompilerCompilationSuccess) return null
    record.preview.entityCreationEvidence?.sourceEdit?.let { evidence ->
        return evidence.toPayload(trackedDocument.text)
    }
    if (ElectricalEntityCreationProjectionAuthority.supports(intent.conceptTemplateId.value)) return null
    val template = electricalEngineeringConceptTemplates()
        .singleOrNull { candidate -> candidate.templateId == intent.conceptTemplateId }
        ?: return null
    val vendorPartNumber = componentKnowledge
        ?.availableComponents
        ?.asSequence()
        ?.flatMap { component -> component.implementations.asSequence() }
        ?.firstOrNull { implementation -> implementation.implementationId == intent.preferredImplementationId }
        ?.vendorPartNumber
        ?.value
    val plannedIntent = intent.copy(
        suggestedName = intent.suggestedName
            ?.takeIf(String::isNotBlank)
            ?: defaultSuggestedEntityName(intent.conceptId.value),
        properties = intent.properties + listOfNotNull(
            vendorPartNumber?.let { value ->
                AuthoringPropertyName("vendorPartNumber") to AuthoringValue.Text(value)
            },
        ),
    )
    val result = BackendAuthoringSourceEditPlanner().plan(
        BackendEntityCreationPlanningRequest(
            document = trackedDocument.toBackendSourceDocument(intent.revisionGuard) ?: return null,
            revisionGuard = intent.revisionGuard,
            intent = plannedIntent,
            template = template,
        ),
    )
    return (result as? BackendAuthoringSourceEditPlanned)
        ?.plan
        ?.toPayload(trackedDocument.text)
}

internal fun governedCreateEntityPreview(
    trackedDocument: AthenaTrackedDocument,
    intent: CreateSemanticEntityIntent,
    previewId: AuthoringPreviewId,
    capabilityEvidence: AuthoringCapabilityEvidence,
): GovernedEntityCreationPreviewResult? {
    if (!ElectricalEntityCreationProjectionAuthority.supports(intent.conceptTemplateId.value)) return null
    val document = trackedDocument.toBackendSourceDocument(intent.revisionGuard) ?: return null
    return GovernedEntityCreationPreviewService(
        templates = electricalEngineeringConceptTemplates(),
        projectionAuthority = ElectricalEntityCreationProjectionAuthority(),
    ).preview(
        GovernedEntityCreationPreviewRequest(
            transactionId = SemanticAuthoringTransactionId("transaction:${intent.intentId.value}"),
            previewId = previewId,
            intent = intent,
            capabilityEvidence = capabilityEvidence,
            document = document,
        ),
    )
}

private fun defaultSuggestedEntityName(conceptId: String): String {
    val tokens = conceptId
        .split('.', '-', '_')
        .filter(String::isNotBlank)
        .filterNot { token -> token.equals("electrical", ignoreCase = true) }
    val base = tokens
        .joinToString(separator = "") { token ->
            token.replaceFirstChar { character -> character.uppercase() }
        }
        .replace(Regex("[^A-Za-z0-9]"), "")
        .ifBlank { "Entity" }
    return if (base.firstOrNull()?.isLetter() == true) {
        base
    } else {
        "Entity$base"
    }
}

internal fun uniqueAuthoredEntityName(
    requestedName: String,
    existingDeviceNames: Set<String>,
): String {
    val normalizedBase = requestedName
        .replace(Regex("[^A-Za-z0-9]"), "")
        .ifBlank { "Entity" }
        .let { base ->
            if (base.firstOrNull()?.isLetter() == true) {
                base
            } else {
                "Entity$base"
            }
        }
    if (normalizedBase !in existingDeviceNames) {
        return normalizedBase
    }
    var ordinal = 2
    while (true) {
        val candidate = "$normalizedBase$ordinal"
        if (candidate !in existingDeviceNames) {
            return candidate
        }
        ordinal += 1
    }
}

internal fun AthenaTrackedDocument.toBackendSourceDocument(
    revisionGuard: AuthoringRevisionGuard,
): BackendAuthoringSourceDocument? {
    val compilation = compilation as? CompilerCompilationSuccess ?: return null
    return BackendAuthoringSourceDocument(
        sourceUri = uri,
        documentVersion = version,
        semanticSnapshotId = revisionGuard.semanticSnapshotId,
        sourceText = text,
        ast = compilation.source.ast,
    )
}

internal fun BackendAuthoringSourceEditPlan.toPayload(sourceText: String): AthenaAuthoringSourceEditPayload {
    return AthenaAuthoringSourceEditPayload(
        uri = sourceUri,
        startOffset = replacement.startOffset,
        endOffset = replacement.endOffset,
        range = AthenaAuthoringSourceRangePayload(
            start = sourceText.positionAt(replacement.startOffset),
            end = sourceText.positionAt(replacement.endOffset),
        ),
        newText = admittedText,
        selectionStartOffset = selection?.startOffset,
        selectionEndOffset = selection?.endOffset,
        selectionRange = selection?.let { selected ->
            val updatedText = sourceText.substring(0, replacement.startOffset) +
                admittedText + sourceText.substring(replacement.endOffset)
            AthenaAuthoringSourceRangePayload(
                start = updatedText.positionAt(selected.startOffset),
                end = updatedText.positionAt(selected.endOffset),
            )
        },
        suggestedSemanticId = affectedSemanticIds.firstOrNull { semanticId -> semanticId.startsWith("component:") }
            ?: affectedSemanticIds.singleOrNull(),
        revisionGuard = revisionGuard.toPayload(),
    )
}

internal fun AuthoringSourceEditEvidence.toPayload(sourceText: String): AthenaAuthoringSourceEditPayload {
    val updatedText = sourceText.substring(0, startOffset) + admittedText + sourceText.substring(endOffset)
    val selectedStart = selectionStartOffset
    val selectedEnd = selectionEndOffset
    return AthenaAuthoringSourceEditPayload(
        uri = sourceUri,
        startOffset = startOffset,
        endOffset = endOffset,
        range = AthenaAuthoringSourceRangePayload(
            start = sourceText.positionAt(startOffset),
            end = sourceText.positionAt(endOffset),
        ),
        newText = admittedText,
        selectionStartOffset = selectedStart,
        selectionEndOffset = selectedEnd,
        selectionRange = if (selectedStart == null || selectedEnd == null) {
            null
        } else {
            AthenaAuthoringSourceRangePayload(
                start = updatedText.positionAt(selectedStart),
                end = updatedText.positionAt(selectedEnd),
            )
        },
        suggestedSemanticId = affectedSemanticIds.firstOrNull { semanticId -> semanticId.startsWith("component:") }
            ?: affectedSemanticIds.singleOrNull(),
        revisionGuard = revisionGuard.toPayload(),
    )
}

internal fun AuthoringRevisionGuard.toPayload(): AthenaAuthoringRevisionGuardPayload =
    AthenaAuthoringRevisionGuardPayload(
        semanticSnapshotId = semanticSnapshotId,
        sourceUri = sourceUri,
        documentVersion = documentVersion,
        contentSha256 = contentSha256,
    )

internal fun String.zeroWidthRangeAt(offset: Int): AthenaAuthoringSourceRangePayload {
    val position = positionAt(offset)
    return AthenaAuthoringSourceRangePayload(
        start = position,
        end = position,
    )
}

internal fun AuthoringSurface.toInteractionOriginSurface(): InteractionOriginSurface = when (this) {
    AuthoringSurface.GRAPH -> InteractionOriginSurface.GRAPH
    AuthoringSurface.DSL -> InteractionOriginSurface.SOURCE
    AuthoringSurface.INSPECTOR -> InteractionOriginSurface.INSPECTOR
    AuthoringSurface.FORM -> InteractionOriginSurface.PROBLEMS
    AuthoringSurface.PALETTE,
    AuthoringSurface.TEMPLATE,
    -> InteractionOriginSurface.PALETTE
    AuthoringSurface.AI -> InteractionOriginSurface.AI
    AuthoringSurface.API -> InteractionOriginSurface.API
}

internal fun String.selectionRangeAfterInsert(
    insertOffset: Int,
    insertedText: String,
): AthenaAuthoringSourceRangePayload? {
    val contentStart = insertedText.indexOfFirst { character -> !character.isWhitespace() }
    if (contentStart < 0) {
        return null
    }
    val contentEndExclusive = insertedText.indexOfLast { character -> !character.isWhitespace() } + 1
    val updatedText = substring(0, insertOffset) + insertedText + substring(insertOffset)
    return AthenaAuthoringSourceRangePayload(
        start = updatedText.positionAt(insertOffset + contentStart),
        end = updatedText.positionAt(insertOffset + contentEndExclusive),
    )
}

internal fun String.selectionRangeAfterReplace(
    replaceStartOffset: Int,
    replaceEndOffset: Int,
    replacementText: String,
): AthenaAuthoringSourceRangePayload? {
    val contentStart = replacementText.indexOfFirst { character -> !character.isWhitespace() }
    if (contentStart < 0) {
        return null
    }
    val contentEndExclusive = replacementText.indexOfLast { character -> !character.isWhitespace() } + 1
    val updatedText = substring(0, replaceStartOffset) + replacementText + substring(replaceEndOffset)
    return AthenaAuthoringSourceRangePayload(
        start = updatedText.positionAt(replaceStartOffset + contentStart),
        end = updatedText.positionAt(replaceStartOffset + contentEndExclusive),
    )
}

internal fun String.positionAt(offset: Int): AthenaAuthoringSourcePositionPayload {
    var line = 0
    var character = 0
    var index = 0
    val clampedOffset = offset.coerceIn(0, length)
    while (index < clampedOffset) {
        if (this[index] == '\n') {
            line += 1
            character = 0
        } else {
            character += 1
        }
        index += 1
    }
    return AthenaAuthoringSourcePositionPayload(
        line = line,
        character = character,
    )
}
