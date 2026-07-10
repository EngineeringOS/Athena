package com.engineeringood.athena.cli

import com.engineeringood.athena.runtime.AthenaAiCommandProposal
import com.engineeringood.athena.runtime.AthenaAiProposalQueueSnapshot
import com.engineeringood.athena.runtime.AthenaCommandExecutionValidationFeedback
import com.engineeringood.athena.runtime.AthenaCommandExecutionRejected
import com.engineeringood.athena.runtime.AthenaCommandExecutionSuccess
import com.engineeringood.athena.runtime.AthenaCommandExecutionUnavailable
import com.engineeringood.athena.runtime.AthenaCommandHistoryMutationValidationFeedback
import com.engineeringood.athena.runtime.AthenaCommandHistoryMutationRejected
import com.engineeringood.athena.runtime.AthenaCommandHistoryMutationSuccess
import com.engineeringood.athena.runtime.AthenaCommandHistoryMutationUnavailable
import com.engineeringood.athena.runtime.AthenaMutationValidationFeedback
import com.engineeringood.athena.runtime.AthenaCommandOrigin
import com.engineeringood.athena.runtime.AthenaConnectPortsCommand
import com.engineeringood.athena.runtime.AthenaExecutionContext
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64

/**
 * CLI-owned session snapshot store that makes one-shot shell invocations deterministic for history-backed commands.
 */
internal class AthenaCliSessionStore {
    /**
     * Returns the deterministic sidecar file used to persist command-history state for one source file.
     */
    fun sessionFilePath(sourcePath: Path): Path {
        val normalizedSourcePath = sourcePath.toAbsolutePath().normalize()
        return normalizedSourcePath.resolveSibling("${normalizedSourcePath.fileName}.runtime-session")
    }

    /**
     * Restores persisted command-history state into [context] when a CLI session sidecar exists.
     */
    fun restore(context: AthenaExecutionContext): AthenaCliPersistedSessionRestoreResult {
        val sessionPath = sessionFilePath(context.project.sourcePath)
        if (!Files.exists(sessionPath)) {
            return AthenaCliPersistedSessionRestoreResult.NoSession
        }

        val snapshot = runCatching {
            parseSnapshot(Files.readAllLines(sessionPath))
        }.getOrElse { exception ->
            return AthenaCliPersistedSessionRestoreResult.Failed(
                "Persisted CLI session at `$sessionPath` is unreadable: ${exception.message ?: exception::class.simpleName}.",
            )
        }

        snapshot.records.forEach { record ->
            when (
                val replay = context.commandRuntime().execute(
                    context = context,
                    command = record.toCommand(),
                    origin = record.commandOrigin,
                )
            ) {
                is AthenaCommandExecutionSuccess -> Unit
                is AthenaCommandExecutionRejected -> {
                    return AthenaCliPersistedSessionRestoreResult.Failed(
                        "Persisted CLI session at `$sessionPath` could not be replayed: ${replay.reason}",
                    )
                }

                is AthenaCommandExecutionValidationFeedback -> {
                    return AthenaCliPersistedSessionRestoreResult.Failed(
                        "Persisted CLI session at `$sessionPath` produced validation feedback: ${replay.validationFeedback.renderedFeedback()}",
                    )
                }

                is AthenaCommandExecutionUnavailable -> {
                    return AthenaCliPersistedSessionRestoreResult.Failed(
                        "Persisted CLI session at `$sessionPath` is unavailable: ${replay.reason}",
                    )
                }
            }
        }

        repeat(snapshot.records.size - snapshot.appliedRecordCount) {
            when (val undo = context.commandRuntime().undo(context)) {
                is AthenaCommandHistoryMutationSuccess -> Unit
                is AthenaCommandHistoryMutationRejected -> {
                    return AthenaCliPersistedSessionRestoreResult.Failed(
                        "Persisted CLI session at `$sessionPath` could not restore undo state: ${undo.reason}",
                    )
                }

                is AthenaCommandHistoryMutationValidationFeedback -> {
                    return AthenaCliPersistedSessionRestoreResult.Failed(
                        "Persisted CLI session at `$sessionPath` produced history validation feedback: ${undo.validationFeedback.renderedFeedback()}",
                    )
                }

                is AthenaCommandHistoryMutationUnavailable -> {
                    return AthenaCliPersistedSessionRestoreResult.Failed(
                        "Persisted CLI session at `$sessionPath` is unavailable: ${undo.reason}",
                    )
                }
            }
        }

        context.aiProposalRuntime().restorePendingProposals(
            context = context,
            snapshot = snapshot.toProposalQueueSnapshot(),
        )

        return AthenaCliPersistedSessionRestoreResult.Restored(snapshot.latestDiffText)
    }

    /**
     * Persists the current command-history state for [context] and the last rendered CLI diff text.
     */
    fun save(
        context: AthenaExecutionContext,
        latestDiffText: String?,
    ) {
        val history = context.commandRuntime().history(context)
        val proposalSnapshot = context.aiProposalRuntime().snapshot(context)
        val sessionPath = sessionFilePath(context.project.sourcePath)
        if (
            history.records.isEmpty() &&
            proposalSnapshot.proposals.isEmpty()
        ) {
            Files.deleteIfExists(sessionPath)
            return
        }

        val snapshot = AthenaCliSessionSnapshot(
            appliedRecordCount = history.appliedRecordCount,
            latestDiffText = latestDiffText,
            records = history.records.map { record -> record.toSessionRecord() },
            proposals = proposalSnapshot.proposals.map { proposal -> proposal.toSessionProposalRecord() },
            nextProposalOrdinal = proposalSnapshot.nextProposalOrdinal,
        )
        Files.writeString(sessionPath, snapshot.render())
    }

    private fun parseSnapshot(lines: List<String>): AthenaCliSessionSnapshot {
        val trimmedLines = lines.map(String::trim).filter(String::isNotEmpty)
        val headerValues = mutableMapOf<String, String>()
        val recordLines = mutableListOf<String>()
        val proposalLines = mutableListOf<String>()
        trimmedLines.forEach { line ->
            if (line.startsWith(RECORD_PREFIX)) {
                recordLines += line.removePrefix(RECORD_PREFIX)
            } else if (line.startsWith(PROPOSAL_PREFIX)) {
                proposalLines += line.removePrefix(PROPOSAL_PREFIX)
            } else {
                val separatorIndex = line.indexOf('=')
                require(separatorIndex > 0) { "Invalid persisted session line `$line`." }
                headerValues[line.substring(0, separatorIndex)] = line.substring(separatorIndex + 1)
            }
        }

        val version = headerValues[VERSION_KEY]
        require(version == SESSION_VERSION_V1 || version == SESSION_VERSION_V2) {
            "Unsupported persisted CLI session version `$version`."
        }
        val appliedRecordCount = headerValues[APPLIED_RECORD_COUNT_KEY]?.toIntOrNull()
            ?: error("Persisted CLI session is missing a valid applied record count.")
        require(appliedRecordCount in 0..recordLines.size) {
            "Persisted CLI session applied record count $appliedRecordCount is outside the valid range 0..${recordLines.size}."
        }

        return AthenaCliSessionSnapshot(
            appliedRecordCount = appliedRecordCount,
            latestDiffText = headerValues[LATEST_DIFF_KEY]?.takeIf(String::isNotEmpty)?.decodedValue(),
            records = recordLines.map { record -> record.toSessionRecord(version) },
            proposals = proposalLines.map { proposal -> proposal.toSessionProposalRecord() },
            nextProposalOrdinal = headerValues[NEXT_AI_PROPOSAL_ORDINAL_KEY]?.toIntOrNull()
                ?: if (version == SESSION_VERSION_V1) {
                    1
                } else {
                    error("Persisted CLI session is missing a valid next AI proposal ordinal.")
                },
        )
    }
}

private fun List<AthenaMutationValidationFeedback>.renderedFeedback(): String {
    return joinToString(separator = "; ") { feedback ->
        "${feedback.severity.name.lowercase()}: ${feedback.message}"
    }
}

/**
 * Outcome of attempting to restore one persisted CLI history snapshot into a fresh runtime context.
 */
internal sealed interface AthenaCliPersistedSessionRestoreResult {
    /**
     * No session sidecar exists yet for the requested source file.
     */
    data object NoSession : AthenaCliPersistedSessionRestoreResult

    /**
     * Session state was restored successfully.
     */
    data class Restored(
        val latestDiffText: String?,
    ) : AthenaCliPersistedSessionRestoreResult

    /**
     * Session state exists but could not be restored safely.
     */
    data class Failed(
        val reason: String,
    ) : AthenaCliPersistedSessionRestoreResult
}

private data class AthenaCliSessionSnapshot(
    val appliedRecordCount: Int,
    val latestDiffText: String?,
    val records: List<AthenaCliSessionRecord>,
    val proposals: List<AthenaCliSessionProposalRecord> = emptyList(),
    val nextProposalOrdinal: Int = 1,
) {
    fun render(): String {
        return buildString {
            appendLine("$VERSION_KEY=$SESSION_VERSION_V2")
            appendLine("$APPLIED_RECORD_COUNT_KEY=$appliedRecordCount")
            appendLine("$NEXT_AI_PROPOSAL_ORDINAL_KEY=$nextProposalOrdinal")
            appendLine("$LATEST_DIFF_KEY=${latestDiffText?.encodedValue().orEmpty()}")
            records.forEach { record ->
                appendLine("$RECORD_PREFIX${record.render()}")
            }
            proposals.forEach { proposal ->
                appendLine("$PROPOSAL_PREFIX${proposal.render()}")
            }
        }.trimEnd()
    }

    fun toProposalQueueSnapshot(): AthenaAiProposalQueueSnapshot {
        return AthenaAiProposalQueueSnapshot(
            proposals = proposals.map { proposal -> proposal.toProposal() },
            nextProposalOrdinal = nextProposalOrdinal,
        )
    }
}

private data class AthenaCliSessionRecord(
    val commandOrigin: AthenaCommandOrigin,
    val sourcePortSemanticId: String,
    val targetPortSemanticId: String,
) {
    fun render(): String {
        return listOf(
            COMMAND_KIND_CONNECT_PORTS,
            commandOrigin.name,
            sourcePortSemanticId.encodedValue(),
            targetPortSemanticId.encodedValue(),
        ).joinToString(SESSION_FIELD_SEPARATOR)
    }

    fun toCommand(): AthenaConnectPortsCommand {
        return AthenaConnectPortsCommand(
            sourcePortSemanticId = sourcePortSemanticId,
            targetPortSemanticId = targetPortSemanticId,
        )
    }
}

private data class AthenaCliSessionProposalRecord(
    val proposalId: String,
    val summary: String,
    val rationale: String,
    val sourcePortSemanticId: String,
    val targetPortSemanticId: String,
) {
    fun render(): String {
        return listOf(
            proposalId.encodedValue(),
            summary.encodedValue(),
            rationale.encodedValue(),
            COMMAND_KIND_CONNECT_PORTS,
            sourcePortSemanticId.encodedValue(),
            targetPortSemanticId.encodedValue(),
        ).joinToString(SESSION_FIELD_SEPARATOR)
    }

    fun toProposal(): AthenaAiCommandProposal {
        return AthenaAiCommandProposal(
            proposalId = proposalId,
            summary = summary,
            rationale = rationale,
            command = AthenaConnectPortsCommand(
                sourcePortSemanticId = sourcePortSemanticId,
                targetPortSemanticId = targetPortSemanticId,
            ),
        )
    }
}

private fun com.engineeringood.athena.runtime.AthenaCommandHistoryRecord.toSessionRecord(): AthenaCliSessionRecord {
    val recordedCommand = command
    val connectCommand = recordedCommand as? AthenaConnectPortsCommand
        ?: error("CLI session persistence does not yet support `${recordedCommand.commandKind}`.")
    return AthenaCliSessionRecord(
        commandOrigin = commandOrigin,
        sourcePortSemanticId = connectCommand.sourcePortSemanticId,
        targetPortSemanticId = connectCommand.targetPortSemanticId,
    )
}

private fun AthenaAiCommandProposal.toSessionProposalRecord(): AthenaCliSessionProposalRecord {
    val connectCommand = command as? AthenaConnectPortsCommand
        ?: error("CLI session persistence does not yet support `${command.commandKind}` AI proposals.")
    return AthenaCliSessionProposalRecord(
        proposalId = proposalId,
        summary = summary,
        rationale = rationale,
        sourcePortSemanticId = connectCommand.sourcePortSemanticId,
        targetPortSemanticId = connectCommand.targetPortSemanticId,
    )
}

private fun String.toSessionRecord(version: String): AthenaCliSessionRecord {
    val fields = split(SESSION_FIELD_SEPARATOR)
    return when (version) {
        SESSION_VERSION_V1 -> {
            require(fields.size == 3) { "Invalid persisted command record `$this`." }
            require(fields.first() == COMMAND_KIND_CONNECT_PORTS) {
                "Unsupported persisted command kind `${fields.first()}`."
            }
            AthenaCliSessionRecord(
                commandOrigin = AthenaCommandOrigin.STANDARD,
                sourcePortSemanticId = fields[1].decodedValue(),
                targetPortSemanticId = fields[2].decodedValue(),
            )
        }

        SESSION_VERSION_V2 -> {
            require(fields.size == 4) { "Invalid persisted command record `$this`." }
            require(fields.first() == COMMAND_KIND_CONNECT_PORTS) {
                "Unsupported persisted command kind `${fields.first()}`."
            }
            AthenaCliSessionRecord(
                commandOrigin = AthenaCommandOrigin.valueOf(fields[1]),
                sourcePortSemanticId = fields[2].decodedValue(),
                targetPortSemanticId = fields[3].decodedValue(),
            )
        }

        else -> error("Unsupported persisted CLI session version `$version`.")
    }
}

private fun String.toSessionProposalRecord(): AthenaCliSessionProposalRecord {
    val fields = split(SESSION_FIELD_SEPARATOR)
    require(fields.size == 6) { "Invalid persisted AI proposal record `$this`." }
    require(fields[3] == COMMAND_KIND_CONNECT_PORTS) {
        "Unsupported persisted AI proposal command kind `${fields[3]}`."
    }
    return AthenaCliSessionProposalRecord(
        proposalId = fields[0].decodedValue(),
        summary = fields[1].decodedValue(),
        rationale = fields[2].decodedValue(),
        sourcePortSemanticId = fields[4].decodedValue(),
        targetPortSemanticId = fields[5].decodedValue(),
    )
}

private fun String.encodedValue(): String {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(toByteArray(StandardCharsets.UTF_8))
}

private fun String.decodedValue(): String {
    return String(Base64.getUrlDecoder().decode(this), StandardCharsets.UTF_8)
}

private const val SESSION_VERSION_V1 = "1"
private const val SESSION_VERSION_V2 = "2"
private const val VERSION_KEY = "version"
private const val APPLIED_RECORD_COUNT_KEY = "appliedRecordCount"
private const val NEXT_AI_PROPOSAL_ORDINAL_KEY = "nextAiProposalOrdinal"
private const val LATEST_DIFF_KEY = "latestDiffBase64"
private const val RECORD_PREFIX = "record="
private const val PROPOSAL_PREFIX = "proposal="
private const val SESSION_FIELD_SEPARATOR = "|"
private const val COMMAND_KIND_CONNECT_PORTS = "CONNECT_PORTS"
