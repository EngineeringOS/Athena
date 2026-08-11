package com.engineeringood.athena.interaction

private val JOURNAL_ID = Regex("^journal:sha256:[0-9a-f]{64}$")
private val PUBLICATION_ID = Regex("^publication:[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

data class SourceFilePatch(val relativePath: String, val beforeUtf8: String?, val afterUtf8: String?) {
    init {
        requireRepositoryRelativePath(relativePath)
        require(beforeUtf8 != afterUtf8)
    }

    fun inverse() = SourceFilePatch(relativePath, afterUtf8, beforeUtf8)
}

data class SourcePatchSet(val files: List<SourceFilePatch>) {
    init {
        require(files.isNotEmpty())
        require(files == files.sortedBy(SourceFilePatch::relativePath))
        require(files.map(SourceFilePatch::relativePath).distinct().size == files.size)
    }

    val writableFiles: List<String> get() = files.map(SourceFilePatch::relativePath)
    fun inverse() = SourcePatchSet(files.map(SourceFilePatch::inverse))
}

enum class StagedCompileResult { ACCEPTED }

data class OperationJournalEntry(
    val journalEntryId: String,
    val sequence: Long,
    val operationId: String,
    val operationKind: EditOperationKind,
    val authorityClass: EditAuthorityClass,
    val target: OperationTarget,
    val sourceTrace: SourceTraceContext,
    val previousSourceRevision: SourceRevision,
    val resultingSourceRevision: SourceRevision,
    val writableFiles: List<String>,
    val forwardPatchSet: SourcePatchSet,
    val inversePatchSet: SourcePatchSet,
    val stagedCompileResult: StagedCompileResult,
    val publicationCorrelationId: String,
) {
    init {
        require(JOURNAL_ID.matches(journalEntryId) && sequence > 0)
        require(operationKind.authorityClass == authorityClass)
        require(writableFiles == writableFiles.sorted() && writableFiles.distinct().size == writableFiles.size)
        require(writableFiles == forwardPatchSet.writableFiles && writableFiles == inversePatchSet.writableFiles)
        require(forwardPatchSet == inversePatchSet.inverse())
        require(PUBLICATION_ID.matches(publicationCorrelationId))
    }
}

interface OperationJournal {
    fun append(entry: OperationJournalEntry): OperationJournalEntry
    fun findByOperationId(operationId: String): OperationJournalEntry?
    fun entries(): List<OperationJournalEntry>
}

class InMemoryOperationJournal : OperationJournal {
    private val accepted = mutableListOf<OperationJournalEntry>()
    private val byOperationId = linkedMapOf<String, OperationJournalEntry>()

    @Synchronized
    override fun append(entry: OperationJournalEntry): OperationJournalEntry {
        byOperationId[entry.operationId]?.let { previous ->
            require(previous == entry) { "Operation id was already accepted with different transaction content." }
            return previous
        }
        require(entry.sequence == accepted.size.toLong() + 1) { "Journal sequence must be monotonic." }
        accepted += entry
        byOperationId[entry.operationId] = entry
        return entry
    }

    @Synchronized
    override fun findByOperationId(operationId: String): OperationJournalEntry? = byOperationId[operationId]

    @Synchronized
    override fun entries(): List<OperationJournalEntry> = accepted.toList()
}
