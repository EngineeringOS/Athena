package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaDiagramSceneCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.interaction.OperationRejectionReason
import com.engineeringood.athena.interaction.SetStyle
import com.engineeringood.athena.interaction.SourceFilePatch
import com.engineeringood.athena.interaction.SourcePatchSet
import com.engineeringood.athena.interaction.StyleTargetKind
import com.engineeringood.athena.language.AthenaSheetStyleCompanionParser
import com.engineeringood.athena.language.SheetCompanionAmbiguous
import com.engineeringood.athena.language.SheetCompanionFound
import com.engineeringood.athena.language.SheetStyleCompanionEditor
import com.engineeringood.athena.language.SheetStyleCompanionLocator
import com.engineeringood.athena.language.SheetStyleCompanionParseSuccess
import com.engineeringood.athena.language.SheetStyleFields
import com.engineeringood.athena.presentation.PublicationState
import java.nio.file.Files
import java.nio.file.Path

/** Executes current supported Presentation transaction without widening operation routing. */
class SetStyleOperationHandler(
    private val host: AthenaLspSessionHostReady,
    private val publicationService: AthenaDiagramPublicationService,
    private val revisionService: SourceRevisionService,
    private val transactionEngine: SourceTransactionEngine,
) {
    fun apply(operation: EditOperationEnvelope, setStyle: SetStyle): EditOperationResult {
        val currentRevision = revisionService.current()
        val publication = publicationService.current()
        val scene = publication.scene
        if (publication.state != PublicationState.READY || scene == null || setStyle.sheetId != scene.snapGrid.sheetId) {
            return rejected(operation, currentRevision, OperationRejectionReason.INVALID, diagnostic(
                setStyle.sheetId,
                "Style edit does not target current accepted Sheet.",
                "Select current Sheet and retry the style edit.",
                "edit.operation.style.sheet-unknown",
            ))
        }
        if (!styleTargetExists(scene, setStyle, operation.sourceTrace.traceId)) {
            return rejected(operation, currentRevision, OperationRejectionReason.INVALID, diagnostic(
                setStyle.target.id,
                "Style target does not match current Source Trace.",
                "Select one visible role or occurrence from accepted engineering document.",
                "edit.operation.style.target-unknown",
            ))
        }
        val sheet = host.sheetCompanionLocation() as? SheetCompanionFound
            ?: return rejected(operation, currentRevision, OperationRejectionReason.UNAVAILABLE, diagnostic(
                host.sourcePath.fileName.toString(),
                "Required same-basename Sheet Companion is unavailable.",
                "Restore exact same-basename Sheet Companion before editing style.",
                "sheet.companion.unavailable",
            ))
        val styleLocation = SheetStyleCompanionLocator.locate(sheet.path)
        if (styleLocation is SheetCompanionAmbiguous) {
            return rejected(operation, currentRevision, OperationRejectionReason.UNAVAILABLE, diagnostic(
                styleLocation.expectedPath.fileName.toString(),
                "Style Companion is ambiguous or uses different filename casing.",
                "Keep zero or one exact same-basename Style Companion.",
                "sheet.style.companion.ambiguous",
            ))
        }
        val stylePath = styleLocation.expectedPath
        val existingText = (styleLocation as? SheetCompanionFound)?.let { Files.readString(it.path) }
        val targetName = when (setStyle.target.kind) {
            StyleTargetKind.ROLE -> setStyle.target.id
            StyleTargetKind.OCCURRENCE -> "occurrence:${setStyle.target.id}"
        }
        val proposedText = runCatching {
            SheetStyleCompanionEditor().setStyle(existingText, targetName, setStyle.fields.toLanguageFields())
        }.getOrElse { failure ->
            return rejected(operation, currentRevision, OperationRejectionReason.INVALID, diagnostic(
                setStyle.target.id,
                "Style intent is invalid: ${failure.message ?: "unsupported value"}.",
                "Use supported presentation fields and values.",
                "edit.operation.style-invalid",
            ))
        }
        val parsed = AthenaSheetStyleCompanionParser().parse(stylePath.toString(), proposedText)
            as? SheetStyleCompanionParseSuccess
            ?: return rejected(operation, currentRevision, OperationRejectionReason.INVALID, diagnostic(
                stylePath.fileName.toString(),
                "Proposed Style Companion does not parse.",
                "Correct style fields before solidifying.",
                "sheet.style.companion.invalid",
            ))
        val stagedRevision = revisionService.current(styleCompanionBytesOverride = proposedText.toByteArray(Charsets.UTF_8))
        val patchSet = SourcePatchSet(listOf(SourceFilePatch(relative(stylePath), existingText, proposedText)))
        return transactionEngine.execute(operation, patchSet, stagedRevision) {
            val compilation = host.executionContext.compiler().compile(host.sourcePath) as? CompilerCompilationSuccess
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(
                    host.sourcePath.fileName.toString(),
                    "Engineering source cannot be compiled for style validation.",
                    "Correct source diagnostics before solidifying style.",
                    "edit.operation.source-invalid",
                ))
            if (AthenaDiagramSceneCompiler().compile(
                    compilation,
                    stagedRevision.sceneInputRevision,
                    styleCompanion = parsed.source,
                ).scene == null
            ) {
                throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(
                    setStyle.target.id,
                    "Proposed style does not produce a complete Canonical Scene.",
                    "Correct style and current engineering document before retrying.",
                    "edit.operation.style-compilation",
                ))
            }
        }
    }

    private fun relative(path: Path): String = host.repositoryRoot.toAbsolutePath().normalize()
        .relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/')
}

private fun styleTargetExists(
    scene: com.engineeringood.athena.presentation.AthenaDiagramScene,
    setStyle: SetStyle,
    traceId: String,
): Boolean = when (setStyle.target.kind) {
    StyleTargetKind.ROLE -> when (setStyle.target.id) {
        "default" -> scene.decorations.any { it.traceId.value == traceId }
        "symbol" -> scene.occurrences.any { it.traceId.value == traceId }
        "connection" -> scene.connections.any { it.traceId.value == traceId }
        "label" -> scene.occurrences.flatMap { it.labels }.any { it.traceId.value == traceId }
        "port" -> scene.occurrences.flatMap { it.ports }.any { it.traceId.value == traceId }
        else -> false
    }
    StyleTargetKind.OCCURRENCE -> scene.occurrences.any {
        it.occurrenceId == setStyle.target.id && it.traceId.value == traceId
    }
}

private fun com.engineeringood.athena.interaction.StyleFields.toLanguageFields() = SheetStyleFields(
    strokeRgba = strokeRgba,
    fillRgba = fillRgba,
    strokeWidth = strokeWidth,
    dash = dash,
    lineCap = lineCap,
    lineJoin = lineJoin,
    opacity = opacity,
    fontSize = fontSize,
    fontWeight = fontWeight,
    routeMarker = routeMarker?.name?.lowercase()?.replace('_', '-'),
    portDisplay = portDisplay?.name?.lowercase()?.replace('_', '-'),
)
