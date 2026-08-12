package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.EditOperationAcceptance
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.interaction.EditOperationStatus
import com.engineeringood.athena.interaction.OperationDiagnostic
import com.engineeringood.athena.interaction.OperationJournalEntry
import com.engineeringood.athena.interaction.OperationRejectionReason
import com.engineeringood.athena.interaction.SourceFilePatch
import com.engineeringood.athena.interaction.SourcePatchSet
import com.engineeringood.athena.interaction.SourceRevision
import com.engineeringood.athena.interaction.StagedCompileResult
import com.engineeringood.athena.presentation.PublicationState
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID
import kotlin.io.path.createTempDirectory

/** Applies one validated source transaction and appends history only after accepted scene publication. */
class SourceTransactionEngine(
    private val workspace: TransactionWorkspace,
    private val currentRevision: () -> SourceRevision,
    private val currentPublication: () -> com.engineeringood.athena.presentation.AthenaScenePublication,
    private val journal: SessionOperationJournal,
    private val refreshDerivedState: (SourcePatchSet) -> Unit = {},
    private val augmentPatchSet: (SourcePatchSet) -> SourcePatchSet = { it },
) {
    constructor(
        host: AthenaLspSessionHostReady,
        publicationService: AthenaDiagramPublicationService,
        revisionService: SourceRevisionService,
        journal: SessionOperationJournal,
    ) : this(
        NioTransactionWorkspace(host.repositoryRoot),
        revisionService::current,
        publicationService::current,
        journal,
        augmentPatchSet = { patchSet -> host.stageCanonicalLockPatch(patchSet) },
    )

    fun execute(
        operation: EditOperationEnvelope,
        patchSet: SourcePatchSet,
        stagedRevision: SourceRevision,
        validateStaged: () -> Unit,
    ): EditOperationResult {
        val previousRevision = currentRevision()
        if (previousRevision != operation.sourceRevision) {
            return rejected(
                operation,
                previousRevision,
                OperationRejectionReason.STALE,
                diagnostic(
                    operation.kind.name,
                    "Source Revision changed before transaction execution.",
                    "Refresh current engineering source and retry the edit.",
                    "edit.operation.revision-stale",
                ),
            )
        }
        val patchFailure = validateCurrentFiles(patchSet)
        if (patchFailure != null) {
            return rejected(operation, previousRevision, OperationRejectionReason.STALE, patchFailure)
        }
        try {
            validateStaged()
        } catch (failure: StagedOperationFailure) {
            return rejected(operation, previousRevision, failure.reason, failure.diagnostic)
        } catch (failure: Exception) {
            val cause = generateSequence<Throwable>(failure) { it.cause }.lastOrNull() ?: failure
            val context = listOfNotNull(
                failure::class.simpleName,
                cause::class.simpleName?.takeIf { it != failure::class.simpleName },
                cause.message?.takeIf(String::isNotBlank),
            ).joinToString(": ")
            return rejected(
                operation,
                previousRevision,
                OperationRejectionReason.COMPILATION_FAILURE,
                diagnostic(
                    operation.kind.name,
                    context.ifBlank { "Staged source validation failed." },
                    "Correct authored intent and retry from current source.",
                    "edit.operation.staging-failed",
                ),
            )
        }

        val committedPatchSet = try {
            augmentPatchSet(patchSet)
        } catch (failure: Exception) {
            return rejected(
                operation,
                previousRevision,
                OperationRejectionReason.COMPILATION_FAILURE,
                diagnostic(
                    operation.kind.name,
                    failure.message ?: "Derived repository state could not be staged.",
                    "Correct package-affecting authored intent and retry.",
                    "edit.operation.derived-state-invalid",
                ),
            )
        }
        val committedPatchFailure = validateCurrentFiles(committedPatchSet)
        if (committedPatchFailure != null) {
            return rejected(operation, previousRevision, OperationRejectionReason.STALE, committedPatchFailure)
        }

        return try {
            workspace.publish(committedPatchSet)
            refreshDerivedState(committedPatchSet)
            val resultingRevision = currentRevision()
            val publication = currentPublication()
            check(publication.state == PublicationState.READY) { "Published source did not produce a READY Canonical Scene." }
            val publicationCorrelationId = "publication:${UUID.randomUUID()}"
            val sequence = journal.nextSequence()
            val entryId = journalEntryId(operation, sequence, resultingRevision, patchSet)
            val entry = OperationJournalEntry(
                journalEntryId = entryId,
                sequence = sequence,
                operationId = operation.operationId,
                operationKind = operation.kind,
                authorityClass = operation.authorityClass,
                target = operation.target,
                sourceTrace = operation.sourceTrace,
                previousSourceRevision = previousRevision,
                resultingSourceRevision = resultingRevision,
                writableFiles = committedPatchSet.writableFiles,
                forwardPatchSet = committedPatchSet,
                inversePatchSet = committedPatchSet.inverse(),
                stagedCompileResult = StagedCompileResult.ACCEPTED,
                publicationCorrelationId = publicationCorrelationId,
            )
            val result = EditOperationResult(
                status = EditOperationStatus.ACCEPTED,
                operationId = operation.operationId,
                currentSourceRevision = resultingRevision,
                acceptance = EditOperationAcceptance(
                    previousSourceRevision = previousRevision,
                    resultingSourceRevision = resultingRevision,
                    acceptedPatchSet = committedPatchSet,
                    publicationCorrelationId = publicationCorrelationId,
                    journalEntryId = entryId,
                ),
            )
            journal.append(operation, entry, result)
            result
        } catch (failure: Exception) {
            val rollbackFailure = runCatching {
                workspace.publish(committedPatchSet.inverse())
                currentPublication()
            }.exceptionOrNull()
            val detail = listOfNotNull(failure.message, rollbackFailure?.message).joinToString("; ")
            rejected(
                operation,
                currentRevision(),
                OperationRejectionReason.IO_FAILURE,
                diagnostic(
                    operation.kind.name,
                    "Source transaction was not accepted${detail.takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()}.",
                    "Check repository file access, refresh current source, and retry.",
                    "edit.operation.transaction-failed",
                ),
            )
        }
    }

    private fun validateCurrentFiles(patchSet: SourcePatchSet): OperationDiagnostic? {
        patchSet.files.forEach { patch ->
            val current = workspace.read(patch.relativePath)
            if (current != patch.beforeUtf8) {
                return diagnostic(
                    patch.relativePath,
                    "Source changed after this operation was staged.",
                    "Refresh current source and retry the edit.",
                    "edit.operation.patch-stale",
                )
            }
        }
        return null
    }

}

private fun AthenaLspSessionHostReady.stageCanonicalLockPatch(patchSet: SourcePatchSet): SourcePatchSet {
    val authoredPathChanged = patchSet.writableFiles.any { path ->
        path == "athena.yaml" ||
            path.startsWith("packages/") ||
            (path.startsWith("src/") &&
                !path.endsWith(".sheet.athena") &&
                !path.endsWith(".sheet.style.athena") &&
                !path.endsWith(".binding.athena") &&
                !path.endsWith(".part-binding.athena"))
    }
    if (!authoredPathChanged) return patchSet

    val lockPath = repositoryRoot.resolve("athena.lock").normalize()
    val beforeLock = lockPath.takeIf(Files::exists)?.let(Files::readString)
    val stagingRoot = createTempDirectory("athena-lock-stage-")
    try {
        Files.walk(repositoryRoot).use { paths ->
            paths.forEach { path ->
                val relative = repositoryRoot.relativize(path)
                val target = stagingRoot.resolve(relative.toString())
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target)
                } else if (relative.toString() != "athena.lock") {
                    Files.createDirectories(target.parent)
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        patchSet.files.forEach { patch ->
            val target = stagingRoot.resolve(patch.relativePath).normalize()
            require(target.startsWith(stagingRoot)) { "Staged source path escapes repository root." }
            if (patch.afterUtf8 == null) {
                Files.deleteIfExists(target)
            } else {
                Files.createDirectories(target.parent)
                Files.writeString(target, patch.afterUtf8, StandardCharsets.UTF_8)
            }
        }
        val result = executionContext.compiler().materializeRepositoryLock(stagingRoot)
        check(result.isValid && result.renderedLock != null) {
            result.diagnostics.joinToString("; ") { diagnostic -> diagnostic.message }
        }
        val lockPatch = SourceFilePatch(
            relativePath = "athena.lock",
            beforeUtf8 = beforeLock,
            afterUtf8 = result.renderedLock,
        )
        return SourcePatchSet((patchSet.files + lockPatch).sortedBy(SourceFilePatch::relativePath))
    } finally {
        stagingRoot.toFile().deleteRecursively()
    }
}

interface TransactionWorkspace {
    fun read(relativePath: String): String?
    fun publish(patchSet: SourcePatchSet)
}

private class NioTransactionWorkspace(repositoryRoot: Path) : TransactionWorkspace {
    private val root = repositoryRoot.toAbsolutePath().normalize()

    override fun read(relativePath: String): String? {
        val path = resolve(relativePath)
        return if (Files.exists(path)) Files.readString(path) else null
    }

    override fun publish(patchSet: SourcePatchSet) {
        val staged = linkedMapOf<SourceFilePatch, Path>()
        try {
            patchSet.files.forEach { patch ->
                val target = resolve(patch.relativePath)
                patch.afterUtf8?.let { content ->
                    Files.createDirectories(target.parent)
                    val temp = Files.createTempFile(target.parent, ".${target.fileName}.", ".stage")
                    Files.writeString(temp, content, StandardCharsets.UTF_8)
                    staged[patch] = temp
                }
            }
            patchSet.files.forEach { patch ->
                val target = resolve(patch.relativePath)
                val temp = staged[patch]
                if (temp == null) {
                    Files.deleteIfExists(target)
                } else {
                    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        } catch (failure: Exception) {
            runCatching { restoreBefore(patchSet) }
            throw failure
        } finally {
            staged.values.forEach(Files::deleteIfExists)
        }
    }

    private fun restoreBefore(patchSet: SourcePatchSet) {
        patchSet.files.forEach { patch ->
            val target = resolve(patch.relativePath)
            val before = patch.beforeUtf8
            if (before == null) {
                Files.deleteIfExists(target)
            } else {
                Files.createDirectories(target.parent)
                val temp = Files.createTempFile(target.parent, ".${target.fileName}.", ".rollback")
                try {
                    Files.writeString(temp, before, StandardCharsets.UTF_8)
                    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                } finally {
                    Files.deleteIfExists(temp)
                }
            }
        }
    }

    private fun resolve(relativePath: String): Path {
        val resolved = root.resolve(relativePath).normalize()
        require(resolved.startsWith(root)) { "Writable source path escapes repository root." }
        return resolved
    }
}

class StagedOperationFailure(
    val reason: OperationRejectionReason,
    val diagnostic: OperationDiagnostic,
) : RuntimeException(diagnostic.problem)

internal fun rejected(
    operation: EditOperationEnvelope,
    revision: SourceRevision,
    reason: OperationRejectionReason,
    diagnostic: OperationDiagnostic,
): EditOperationResult = EditOperationResult.rejected(operation.operationId, revision, reason, listOf(diagnostic))

internal fun diagnostic(subject: String, problem: String, correction: String, code: String) =
    OperationDiagnostic(subject, problem, correction, code)

private fun journalEntryId(
    operation: EditOperationEnvelope,
    sequence: Long,
    revision: SourceRevision,
    patchSet: SourcePatchSet,
): String {
    val canonical = listOf(operation.operationId, sequence.toString(), revision.token, patchSet.toString()).joinToString("\u0000")
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(canonical.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    return "journal:sha256:$digest"
}
