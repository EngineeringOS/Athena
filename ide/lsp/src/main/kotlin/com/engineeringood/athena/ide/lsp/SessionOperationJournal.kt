package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.interaction.InMemoryOperationJournal
import com.engineeringood.athena.interaction.OperationJournalEntry
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Session-owned accepted transaction history plus exact accepted-operation replay. */
class SessionOperationJournal {
    private val journal = InMemoryOperationJournal()
    private val accepted = linkedMapOf<String, AcceptedOperation>()

    @Synchronized
    fun replay(operation: EditOperationEnvelope): ReplayLookup {
        val previous = accepted[operation.operationId] ?: return ReplayLookup.Missing
        return if (previous.fingerprint == operation.fingerprint()) {
            ReplayLookup.Exact(previous.result)
        } else {
            ReplayLookup.Conflict
        }
    }

    @Synchronized
    fun append(
        operation: EditOperationEnvelope,
        entry: OperationJournalEntry,
        result: EditOperationResult,
    ): OperationJournalEntry {
        val appended = journal.append(entry)
        accepted[operation.operationId] = AcceptedOperation(operation.fingerprint(), result)
        return appended
    }

    @Synchronized
    fun entries(): List<OperationJournalEntry> = journal.entries()

    @Synchronized
    fun find(entryId: String): OperationJournalEntry? = journal.entries().singleOrNull { it.journalEntryId == entryId }

    @Synchronized
    fun nextSequence(): Long = journal.entries().size.toLong() + 1

    private data class AcceptedOperation(val fingerprint: String, val result: EditOperationResult)
}

sealed interface ReplayLookup {
    data object Missing : ReplayLookup
    data object Conflict : ReplayLookup
    data class Exact(val result: EditOperationResult) : ReplayLookup
}

private fun EditOperationEnvelope.fingerprint(): String {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(toString().toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    return "operation:sha256:$digest"
}
