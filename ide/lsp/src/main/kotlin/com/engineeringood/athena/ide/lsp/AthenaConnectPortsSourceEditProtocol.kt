package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.ConnectPortsIntent
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
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
    val intent = record.intent as? ConnectPortsIntent ?: return null
    val compilation = trackedDocument.compilation as? CompilerCompilationSuccess ?: return null
    val insertOffset = trackedDocument.text.lastIndexOf('}')
    if (insertOffset < 0) {
        return null
    }

    val portsBySemanticId = compilation.document.ports.associateBy { port -> port.id.value }
    val sourcePort = portsBySemanticId[intent.sourcePortId.value] ?: return null
    val targetPort = portsBySemanticId[intent.targetPortId.value] ?: return null
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

private fun EngineeringPort.authoredPath(): String = (ownerReference.authoredPath + name).joinToString(".")
