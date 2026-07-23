package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import org.eclipse.lsp4j.ApplyWorkspaceEditParams
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.TextDocumentEdit
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import java.nio.file.Files

internal sealed interface AthenaAuthoringWorkspaceMutationResult

internal data object AthenaAuthoringWorkspaceMutationApplied : AthenaAuthoringWorkspaceMutationResult

internal data class AthenaAuthoringWorkspaceMutationRejected(
    val reason: String,
) : AthenaAuthoringWorkspaceMutationResult

internal fun applyAuthoringWorkspaceMutation(
    client: LanguageClient?,
    trackedDocument: AthenaTrackedDocument,
    sourceEdit: AthenaAuthoringSourceEditPayload,
    proposedSource: String,
    validatePersistedSource: Boolean = false,
): AthenaAuthoringWorkspaceMutationResult {
    val guard = sourceEdit.revisionGuard
        ?: return AthenaAuthoringWorkspaceMutationRejected("Governed source mutation requires a Revision Guard.")
    val activeGuard = trackedDocument.toAuthoringRevisionGuard()
    if (guard.sourceUri != trackedDocument.uri ||
        guard.documentVersion != trackedDocument.version ||
        guard.contentSha256 != activeGuard.contentSha256
    ) {
        return AthenaAuthoringWorkspaceMutationRejected(
            "Active source no longer matches the governed Revision Guard; refresh the preview.",
        )
    }

    return runCatching {
        if (client == null) {
            if (validatePersistedSource) {
                val persistedSource = Files.readString(trackedDocument.path)
                val persistedGuard = AuthoringRevisionGuard.from(
                    semanticSnapshotId = guard.semanticSnapshotId,
                    sourceUri = trackedDocument.uri,
                    documentVersion = guard.documentVersion,
                    sourceText = persistedSource,
                )
                check(persistedGuard.contentSha256 == guard.contentSha256) {
                    "Canonical source changed on disk after preview; refresh the governed preview."
                }
            }
            Files.writeString(trackedDocument.path, proposedSource)
        } else {
            val workspaceEdit = WorkspaceEdit(
                listOf(
                    Either.forLeft(
                        TextDocumentEdit(
                            VersionedTextDocumentIdentifier(sourceEdit.uri, guard.documentVersion),
                            listOf(TextEdit(sourceEdit.range.toLspRange(), sourceEdit.newText)),
                        ),
                    ),
                ),
            )
            val response = client.applyEdit(ApplyWorkspaceEditParams(workspaceEdit)).get()
            check(response.isApplied) {
                response.failureReason ?: "The connected editor rejected Athena's governed workspace edit."
            }
        }
        AthenaAuthoringWorkspaceMutationApplied
    }.getOrElse { failure ->
        AthenaAuthoringWorkspaceMutationRejected(
            failure.message ?: "The authoritative source mutation failed.",
        )
    }
}

private fun AthenaAuthoringSourceRangePayload.toLspRange(): org.eclipse.lsp4j.Range =
    org.eclipse.lsp4j.Range(
        org.eclipse.lsp4j.Position(start.line, start.character),
        org.eclipse.lsp4j.Position(end.line, end.character),
    )
