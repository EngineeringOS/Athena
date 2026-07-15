package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringPropertyName
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.AuthoringValue
import com.engineeringood.athena.authoring.UpdateComponentPropertiesIntent
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.runtime.AthenaAuthoringSessionRecord
import com.engineeringood.athena.runtime.AthenaComponentKnowledgeReady

/**
 * Computes the accepted "update component properties" source edit anchored to authored AST spans.
 *
 * M17 migration-continuity guardrail (AD-109 / AD-106): this edit is computed from
 * `DeviceDeclaration.span`, `PortDeclaration.qualifiedName.span`, and `ConnectionDeclaration.from`/`to`
 * spans on `compilation.source.ast`, then rewritten over the tracked document's raw text. It must
 * keep depending only on the authored `SourceFileAst` and its `SourceSpan`s, never on a
 * parser-generator's internal offset representation. Now that Epic 2 has replaced the handwritten
 * parser with ANTLR4-backed parsing, this function must keep working unchanged as long as the AST
 * spans are populated correctly; Epic 3's Tree-sitter integration must not become an alternative
 * source-edit range computation path.
 */
internal fun acceptedUpdateComponentPropertiesSourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
    componentKnowledge: AthenaComponentKnowledgeReady?,
): AthenaAuthoringSourceEditPayload? {
    if (record.preview.status != AuthoringPreviewStatus.ACCEPTED) {
        return null
    }
    val intent = record.intent as? UpdateComponentPropertiesIntent ?: return null
    val compilation = trackedDocument.compilation as? CompilerCompilationSuccess ?: return null
    val currentComponentName = intent.componentId.value.deviceNameOrNull() ?: return null
    val declaration = compilation.source.ast.declarations
        .filterIsInstance<DeviceDeclaration>()
        .firstOrNull { authoredDeclaration -> authoredDeclaration.name == currentComponentName }
        ?: return null

    val updatedName = intent.properties[AUTHORING_PROPERTY_NAME]
        ?.asRequestedText()
        ?.takeIf(String::isNotBlank)
        ?.let { requestedName ->
            uniqueDeviceName(
                requestedName = requestedName,
                existingDeviceNames = compilation.source.ast.declarations
                    .filterIsInstance<DeviceDeclaration>()
                    .map(DeviceDeclaration::name)
                    .filterNot { deviceName -> deviceName == declaration.name }
                    .toSet(),
            )
        }
        ?: declaration.name
    val rewrittenFields = declaration.fields
        .map(PropertyAssignment::toEditableField)
        .toMutableList()
    intent.properties[AUTHORING_PROPERTY_LABEL]
        ?.asRequestedText()
        ?.takeIf(String::isNotBlank)
        ?.let { labelText ->
            rewrittenFields.upsertField(
                fieldName = "label",
                valueText = labelText,
                valueKind = EditableFieldValueKind.STRING,
            )
        }
    intent.properties[AUTHORING_PROPERTY_DESCRIPTION]
        ?.asRequestedText()
        ?.takeIf(String::isNotBlank)
        ?.let { descriptionText ->
            rewrittenFields.upsertField(
                fieldName = rewrittenFields.descriptionFieldName(),
                valueText = descriptionText,
                valueKind = EditableFieldValueKind.STRING,
            )
        }
    intent.properties[AUTHORING_PROPERTY_IMPLEMENTATION]
        ?.asRequestedText()
        ?.takeIf(String::isNotBlank)
        ?.let { implementationId ->
            val vendorPartNumber = componentKnowledge
                ?.availableComponents
                ?.asSequence()
                ?.flatMap { component -> component.implementations.asSequence() }
                ?.firstOrNull { implementation -> implementation.implementationId.value == implementationId }
                ?.vendorPartNumber
                ?.value
                ?: return null
            rewrittenFields.upsertField(
                fieldName = rewrittenFields.implementationFieldName(),
                valueText = vendorPartNumber,
                valueKind = EditableFieldValueKind.STRING,
            )
        }

    val newText = buildDeviceDeclarationSnippet(
        declaration = declaration,
        componentName = updatedName,
        fields = rewrittenFields,
    )
    val existingDeclarationText = trackedDocument.text.substring(
        declaration.span.start.offset,
        declaration.span.end.offset,
    )
    if (newText == existingDeclarationText) {
        return null
    }

    val sourceEdit = trackedDocument.text.rewriteAffectedComponentSlice(
        declaration = declaration,
        updatedDeclarationText = newText,
        currentComponentName = declaration.name,
        updatedComponentName = updatedName,
        portDeclarations = compilation.source.ast.declarations.filterIsInstance<PortDeclaration>(),
        connectionDeclarations = compilation.source.ast.declarations.filterIsInstance<ConnectionDeclaration>(),
    ) ?: return null

    return AthenaAuthoringSourceEditPayload(
        uri = trackedDocument.uri,
        range = sourceEdit.range,
        newText = sourceEdit.newText,
        selectionRange = sourceEdit.selectionRange,
        suggestedSemanticId = "component:$updatedName",
    )
}

private fun buildDeviceDeclarationSnippet(
    declaration: DeviceDeclaration,
    componentName: String,
    fields: List<EditableDeclarationField>,
): String {
    val declarationIndent = " ".repeat((declaration.span.start.column - 1).coerceAtLeast(0))
    val fieldIndent = declaration.fields.firstOrNull()
        ?.let { field -> " ".repeat((field.span.start.column - 1).coerceAtLeast(0)) }
        ?: "$declarationIndent  "
    return buildString {
        append(declarationIndent)
        append("device ")
        append(componentName)
        appendLine(" {")
        fields.forEach { field ->
            append(fieldIndent)
            append(field.name)
            append(' ')
            append(field.renderValue())
            appendLine()
        }
        append(declarationIndent)
        append("}")
    }
}

private data class EditableDeclarationField(
    val name: String,
    val valueText: String,
    val valueKind: EditableFieldValueKind,
) {
    fun renderValue(): String {
        return when (valueKind) {
            EditableFieldValueKind.IDENTIFIER -> valueText
            EditableFieldValueKind.STRING -> "\"${valueText.escapeForAuthoringString()}\""
        }
    }
}

private enum class EditableFieldValueKind {
    IDENTIFIER,
    STRING,
}

private fun PropertyAssignment.toEditableField(): EditableDeclarationField {
    return EditableDeclarationField(
        name = name,
        valueText = when (val propertyValue = value) {
            is ScalarValue.Identifier -> propertyValue.text
            is ScalarValue.StringLiteral -> propertyValue.text
        },
        valueKind = when (value) {
            is ScalarValue.Identifier -> EditableFieldValueKind.IDENTIFIER
            is ScalarValue.StringLiteral -> EditableFieldValueKind.STRING
        },
    )
}

private fun MutableList<EditableDeclarationField>.upsertField(
    fieldName: String,
    valueText: String,
    valueKind: EditableFieldValueKind,
) {
    val fieldIndex = indexOfFirst { field -> field.name == fieldName }
    if (fieldIndex >= 0) {
        val existing = this[fieldIndex]
        this[fieldIndex] = existing.copy(
            valueText = valueText,
            valueKind = valueKind,
        )
        return
    }
    add(
        EditableDeclarationField(
            name = fieldName,
            valueText = valueText,
            valueKind = valueKind,
        ),
    )
}

private fun MutableList<EditableDeclarationField>.descriptionFieldName(): String {
    return when {
        any { field -> field.name == "description" } -> "description"
        any { field -> field.name == "note" } -> "note"
        else -> "description"
    }
}

private fun MutableList<EditableDeclarationField>.implementationFieldName(): String {
    return firstNotNullOfOrNull { field ->
        field.name.takeIf { fieldName -> fieldName in IMPLEMENTATION_REFERENCE_FIELD_NAMES }
    } ?: "vendorPartNumber"
}

private fun <T, R : Any> Iterable<T>.firstNotNullOfOrNull(transform: (T) -> R?): R? {
    for (element in this) {
        transform(element)?.let { return it }
    }
    return null
}

private fun AuthoringValue.asRequestedText(): String? {
    return when (this) {
        is AuthoringValue.Text -> text
        is AuthoringValue.Symbol -> text
        is AuthoringValue.BooleanValue -> value.toString()
        is AuthoringValue.IntegerValue -> value.toString()
    }
}

private fun String.escapeForAuthoringString(): String {
    return replace("\\", "\\\\").replace("\"", "\\\"")
}

private fun String.deviceNameOrNull(): String? {
    return removePrefix("component:").takeIf { componentName -> componentName.isNotBlank() }
}

private fun SourceSpan.toAuthoringRangePayload(): AthenaAuthoringSourceRangePayload {
    return AthenaAuthoringSourceRangePayload(
        start = AthenaAuthoringSourcePositionPayload(
            line = (start.line - 1).coerceAtLeast(0),
            character = (start.column - 1).coerceAtLeast(0),
        ),
        end = AthenaAuthoringSourcePositionPayload(
            line = (end.line - 1).coerceAtLeast(0),
            character = (end.column - 1).coerceAtLeast(0),
        ),
    )
}

private fun String.rewriteAffectedComponentSlice(
    declaration: DeviceDeclaration,
    updatedDeclarationText: String,
    currentComponentName: String,
    updatedComponentName: String,
    portDeclarations: List<PortDeclaration>,
    connectionDeclarations: List<ConnectionDeclaration>,
): RewrittenComponentSlice? {
    val replacements = buildList {
        add(
            SourceTextReplacement(
                startOffset = declaration.span.start.offset,
                endOffset = declaration.span.end.offset,
                replacementText = updatedDeclarationText,
            ),
        )
        if (updatedComponentName != currentComponentName) {
            portDeclarations
                .filter { port -> port.qualifiedName.parts.firstOrNull() == currentComponentName }
                .forEach { port ->
                    add(
                        SourceTextReplacement(
                            startOffset = port.qualifiedName.ownerSpan().start.offset,
                            endOffset = port.qualifiedName.ownerSpan().end.offset,
                            replacementText = updatedComponentName,
                        ),
                    )
                }
            connectionDeclarations.forEach { connection ->
                if (connection.from.parts.firstOrNull() == currentComponentName) {
                    add(
                        SourceTextReplacement(
                            startOffset = connection.from.ownerSpan().start.offset,
                            endOffset = connection.from.ownerSpan().end.offset,
                            replacementText = updatedComponentName,
                        ),
                    )
                }
                if (connection.to.parts.firstOrNull() == currentComponentName) {
                    add(
                        SourceTextReplacement(
                            startOffset = connection.to.ownerSpan().start.offset,
                            endOffset = connection.to.ownerSpan().end.offset,
                            replacementText = updatedComponentName,
                        ),
                    )
                }
            }
        }
    }
    val rangeStart = replacements.minOfOrNull(SourceTextReplacement::startOffset) ?: return null
    val rangeEnd = replacements.maxOfOrNull(SourceTextReplacement::endOffset) ?: return null
    val affectedSlice = substring(rangeStart, rangeEnd)
    val rewrittenSlice = replacements
        .sortedByDescending(SourceTextReplacement::startOffset)
        .fold(affectedSlice) { currentSlice, replacement ->
            val relativeStart = replacement.startOffset - rangeStart
            val relativeEnd = replacement.endOffset - rangeStart
            currentSlice.substring(0, relativeStart) +
                replacement.replacementText +
                currentSlice.substring(relativeEnd)
        }
    val updatedDocumentText = substring(0, rangeStart) + rewrittenSlice + substring(rangeEnd)
    val selectionOffset = updatedDocumentText.indexOf(updatedDeclarationText, startIndex = rangeStart)
    return RewrittenComponentSlice(
        range = rangeBetween(rangeStart, rangeEnd),
        newText = rewrittenSlice,
        selectionRange = if (selectionOffset >= 0) {
            updatedDocumentText.selectionRangeForSnippet(
                snippetOffset = selectionOffset,
                snippetText = updatedDeclarationText,
            )
        } else {
            null
        },
    )
}

private data class SourceTextReplacement(
    val startOffset: Int,
    val endOffset: Int,
    val replacementText: String,
)

private data class RewrittenComponentSlice(
    val range: AthenaAuthoringSourceRangePayload,
    val newText: String,
    val selectionRange: AthenaAuthoringSourceRangePayload?,
)

private fun String.rangeBetween(
    startOffset: Int,
    endOffset: Int,
): AthenaAuthoringSourceRangePayload {
    return AthenaAuthoringSourceRangePayload(
        start = positionAt(startOffset),
        end = positionAt(endOffset),
    )
}

private fun String.selectionRangeForSnippet(
    snippetOffset: Int,
    snippetText: String,
): AthenaAuthoringSourceRangePayload? {
    val contentStart = snippetText.indexOfFirst { character -> !character.isWhitespace() }
    if (contentStart < 0) {
        return null
    }
    val contentEndExclusive = snippetText.indexOfLast { character -> !character.isWhitespace() } + 1
    return AthenaAuthoringSourceRangePayload(
        start = positionAt(snippetOffset + contentStart),
        end = positionAt(snippetOffset + contentEndExclusive),
    )
}

private fun QualifiedName.ownerSpan(): SourceSpan {
    val owner = parts.first()
    return SourceSpan(
        start = span.start,
        end = span.start.advanceBy(owner),
    )
}

private fun SourcePosition.advanceBy(text: String): SourcePosition {
    return copy(
        offset = offset + text.length,
        column = column + text.length,
    )
}

private val IMPLEMENTATION_REFERENCE_FIELD_NAMES = setOf(
    "vendorPartNumber",
    "vendorPart",
    "partNumber",
    "model",
    "part",
)

private val AUTHORING_PROPERTY_NAME = AuthoringPropertyName("name")
private val AUTHORING_PROPERTY_LABEL = AuthoringPropertyName("label")
private val AUTHORING_PROPERTY_DESCRIPTION = AuthoringPropertyName("description")
private val AUTHORING_PROPERTY_IMPLEMENTATION = AuthoringPropertyName("preferredImplementationId")
