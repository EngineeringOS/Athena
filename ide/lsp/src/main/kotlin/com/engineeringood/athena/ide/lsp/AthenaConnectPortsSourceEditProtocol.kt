package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.ConnectPortsIntent
import com.engineeringood.athena.authoring.ElectricalConnectionRelationship
import com.engineeringood.athena.authoring.ElectricalSemanticRelationshipCompatibilityValidator
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.authoring.SemanticRelationshipValidationRequest
import com.engineeringood.athena.authoring.SemanticRelationshipSourceState
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.runtime.AthenaAuthoringSessionRecord

/**
 * Builds the first source-backed edit for one accepted guided connect preview.
 *
 * The first M15 proof keeps connection acceptance canonical and source-backed: the backend emits the
 * authoritative authored `connect` snippet and the workbench applies it into the tracked `.athena`
 * buffer before semantic rebuild refreshes every surface.
 */
internal fun acceptedConnectPortsSourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
): AthenaAuthoringSourceEditPayload? {
    if (record.preview.status != AuthoringPreviewStatus.ACCEPTED) {
        return null
    }
    val relationship = record.intent.toElectricalRelationshipSubjects() ?: return null
    val compilation = trackedDocument.compilation as? CompilerCompilationSuccess ?: return null
    val insertOffset = trackedDocument.text.lastIndexOf('}')
    if (insertOffset < 0) {
        return null
    }

    val portsBySemanticId = compilation.document.ports.associateBy { port -> port.id.value }
    val sourcePort = portsBySemanticId[relationship.sourcePortId.value] ?: return null
    val targetPort = portsBySemanticId[relationship.targetPortId.value] ?: return null
    val relationshipIntent = record.intent.toSemanticRelationshipIntentForValidation() ?: return null
    val validation = ElectricalSemanticRelationshipCompatibilityValidator().validate(
        SemanticRelationshipValidationRequest(
            intent = relationshipIntent,
            document = compilation.document,
            sourceState = SemanticRelationshipSourceState.VALID,
            sourceText = trackedDocument.text,
        ),
    )
    if (!validation.persistenceEligible) {
        return null
    }

    val sourcePath = sourcePort.authoredPath()
    val targetPath = targetPort.authoredPath()
    val suggestedConnectionSemanticId = "connection:$sourcePath->$targetPath"
    if (compilation.document.connections.any { connection -> connection.id.value == suggestedConnectionSemanticId }) {
        return null
    }

    val newText = buildConnectPortsSnippet(
        sourcePath = sourcePath,
        targetPath = targetPath,
    )
    return AthenaAuthoringSourceEditPayload(
        uri = trackedDocument.uri,
        range = trackedDocument.text.zeroWidthRangeAt(insertOffset),
        newText = newText,
        selectionRange = trackedDocument.text.selectionRangeAfterInsert(
            insertOffset = insertOffset,
            insertedText = newText,
        ),
        suggestedSemanticId = suggestedConnectionSemanticId,
    )
}

private fun buildConnectPortsSnippet(
    sourcePath: String,
    targetPath: String,
): String {
    return buildString {
        appendLine()
        appendLine()
        append("  connect ")
        append(sourcePath)
        append(" -> ")
        append(targetPath)
    }
}

private data class ElectricalRelationshipSubjects(
    val sourcePortId: StableSemanticIdentity,
    val targetPortId: StableSemanticIdentity,
)

private fun Any.toElectricalRelationshipSubjects(): ElectricalRelationshipSubjects? {
    return when (this) {
        is ConnectPortsIntent -> ElectricalRelationshipSubjects(sourcePortId, targetPortId)
        is SemanticRelationshipIntent -> {
            if (relationshipType != ElectricalConnectionRelationship) {
                return null
            }
            ElectricalRelationshipSubjects(sourceSubjectId, targetSubjectId)
        }
        else -> null
    }
}

private fun Any.toSemanticRelationshipIntentForValidation(): SemanticRelationshipIntent? {
    return when (this) {
        is ConnectPortsIntent -> toSemanticRelationshipIntent()
        is SemanticRelationshipIntent -> this
        else -> null
    }
}

private fun EngineeringPort.authoredPath(): String = (ownerReference.authoredPath + name).joinToString(".")
