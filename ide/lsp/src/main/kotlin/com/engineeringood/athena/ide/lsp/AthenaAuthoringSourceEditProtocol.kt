package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.CreateComponentIntent
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.runtime.AthenaAuthoringSessionRecord
import com.engineeringood.athena.runtime.AthenaComponentKnowledgeReady

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
    val range: AthenaAuthoringSourceRangePayload,
    val newText: String,
    val selectionRange: AthenaAuthoringSourceRangePayload? = null,
    val suggestedSemanticId: String? = null,
)

internal fun acceptedCreateComponentSourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
    componentKnowledge: AthenaComponentKnowledgeReady?,
): AthenaAuthoringSourceEditPayload? {
    return createComponentSourceEdit(
        trackedDocument = trackedDocument,
        record = record,
        componentKnowledge = componentKnowledge,
        requiredStatus = AuthoringPreviewStatus.ACCEPTED,
    )
}

internal fun previewCreateComponentSourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
    componentKnowledge: AthenaComponentKnowledgeReady?,
): AthenaAuthoringSourceEditPayload? {
    return createComponentSourceEdit(
        trackedDocument = trackedDocument,
        record = record,
        componentKnowledge = componentKnowledge,
        requiredStatus = AuthoringPreviewStatus.PENDING_REVIEW,
    )
}

private fun createComponentSourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
    componentKnowledge: AthenaComponentKnowledgeReady?,
    requiredStatus: AuthoringPreviewStatus,
): AthenaAuthoringSourceEditPayload? {
    if (record.preview.status != requiredStatus) {
        return null
    }
    val intent = record.intent as? CreateComponentIntent ?: return null
    val compilation = trackedDocument.compilation as? CompilerCompilationSuccess ?: return null
    val insertOffset = trackedDocument.text.lastIndexOf('}')
    if (insertOffset < 0) {
        return null
    }

    val existingDeviceNames = compilation.source.ast.declarations
        .filterIsInstance<DeviceDeclaration>()
        .map(DeviceDeclaration::name)
        .toSet()
    val requestedName = intent.suggestedName
        ?.takeIf(String::isNotBlank)
        ?: defaultSuggestedComponentName(intent.conceptId.value)
    val componentName = uniqueDeviceName(
        requestedName = requestedName,
        existingDeviceNames = existingDeviceNames,
    )
    val vendorPartNumber = componentKnowledge
        ?.availableComponents
        ?.asSequence()
        ?.flatMap { component -> component.implementations.asSequence() }
        ?.firstOrNull { implementation -> implementation.implementationId == intent.preferredImplementationId }
        ?.vendorPartNumber
        ?.value
    val newText = buildCreateComponentSnippet(
        componentName = componentName,
        conceptId = intent.conceptId.value,
        vendorPartNumber = vendorPartNumber,
        typeSymbol = defaultAuthoredDeviceType(intent.conceptId.value),
    )
    val selectionRange = trackedDocument.text.selectionRangeAfterInsert(
        insertOffset = insertOffset,
        insertedText = newText,
    )

    return AthenaAuthoringSourceEditPayload(
        uri = trackedDocument.uri,
        range = trackedDocument.text.zeroWidthRangeAt(insertOffset),
        newText = newText,
        selectionRange = selectionRange,
        suggestedSemanticId = "component:$componentName",
    )
}

private fun buildCreateComponentSnippet(
    componentName: String,
    conceptId: String,
    vendorPartNumber: String?,
    typeSymbol: String,
): String {
    val portTemplates = defaultPortTemplatesForConcept(
        conceptId = conceptId,
        componentName = componentName,
    )
    return buildString {
        appendLine()
        appendLine()
        append("  device ")
        append(componentName)
        appendLine(" {")
        append("    type ")
        append(typeSymbol)
        appendLine()
        append("    componentRef \"")
        append(conceptId)
        appendLine("\"")
        vendorPartNumber?.let { partNumber ->
            append("    vendorPartNumber \"")
            append(partNumber)
            appendLine("\"")
        }
        append("    label \"")
        append(componentName)
        appendLine("\"")
        portTemplates.forEach { template ->
            appendLine()
            append("    port ")
            append(template.name)
            appendLine(" {")
            append("      direction ")
            append(template.direction)
            appendLine()
            append("      signal ")
            append(template.signal)
            appendLine()
            template.protocol?.let { protocol ->
                append("      protocol ")
                append(protocol)
                appendLine()
            }
            appendLine("    }")
        }
        appendLine("  }")
    }
}

private data class DefaultPortTemplate(
    val name: String,
    val direction: String,
    val signal: String,
    val protocol: String? = null,
)

private fun defaultPortTemplatesForConcept(
    conceptId: String,
    componentName: String,
): List<DefaultPortTemplate> {
    @Suppress("UNUSED_PARAMETER")
    val ignored = componentName
    return when (conceptId) {
        "electrical.plc.cpu" -> listOf(
            DefaultPortTemplate("lplus", "in", "Power24"),
            DefaultPortTemplate("m", "in", "Common24"),
            DefaultPortTemplate("pe", "in", "ProtectiveEarth"),
            DefaultPortTemplate("mpi", "out", "MPIBus", protocol = "mpi"),
        )

        "electrical.power-supply.dc24" -> listOf(
            DefaultPortTemplate("out", "out", "Power24"),
            DefaultPortTemplate("m", "out", "Common24"),
            DefaultPortTemplate("pe", "out", "ProtectiveEarth"),
        )

        "electrical.motor.ac" -> listOf(
            DefaultPortTemplate("in", "in", "Digital"),
        )

        else -> emptyList()
    }
}

private fun defaultSuggestedComponentName(conceptId: String): String {
    val tokens = conceptId
        .split('.', '-', '_')
        .filter(String::isNotBlank)
        .filterNot { token -> token.equals("electrical", ignoreCase = true) }
    val base = tokens
        .joinToString(separator = "") { token ->
            token.replaceFirstChar { character -> character.uppercase() }
        }
        .replace(Regex("[^A-Za-z0-9]"), "")
        .ifBlank { "Component" }
    return if (base.firstOrNull()?.isLetter() == true) {
        base
    } else {
        "Component$base"
    }
}

internal fun uniqueDeviceName(
    requestedName: String,
    existingDeviceNames: Set<String>,
): String {
    val normalizedBase = requestedName
        .replace(Regex("[^A-Za-z0-9]"), "")
        .ifBlank { "Component" }
        .let { base ->
            if (base.firstOrNull()?.isLetter() == true) {
                base
            } else {
                "Component$base"
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

private fun defaultAuthoredDeviceType(conceptId: String): String {
    return when (conceptId) {
        "electrical.motor.ac" -> "Motor"
        "electrical.plc.cpu", "electrical.power-supply.dc24" -> "Switch"
        else -> "Switch"
    }
}

internal fun String.zeroWidthRangeAt(offset: Int): AthenaAuthoringSourceRangePayload {
    val position = positionAt(offset)
    return AthenaAuthoringSourceRangePayload(
        start = position,
        end = position,
    )
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
