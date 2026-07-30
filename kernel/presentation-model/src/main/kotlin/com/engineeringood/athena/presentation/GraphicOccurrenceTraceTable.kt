package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveId

@JvmInline
value class GraphicOccurrenceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Graphic occurrence id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class GraphicOccurrenceTraceTableVersion(val value: Int) {
    init {
        require(value > 0) { "Graphic occurrence trace table version must be positive." }
    }
}

@JvmInline
value class TraceDigest(val value: String) {
    init {
        require(value.isNotBlank()) { "Trace digest must not be blank." }
    }
}

data class TraceSourceSpan(
    val file: String,
    val line: Int,
    val column: Int,
) {
    init {
        require(file.isNotBlank()) { "Trace source span file must not be blank." }
        require(line > 0 && column > 0) { "Trace source span must be one-based." }
    }
}

data class TraceSourceRef(
    val declarationId: String,
    val span: TraceSourceSpan,
    val digest: TraceDigest,
) {
    init {
        require(declarationId.isNotBlank()) { "Trace source declaration id must not be blank." }
    }
}

data class GraphicOccurrenceSourceChain(
    val installationDeclaration: TraceSourceRef,
    val mountedOccurrence: TraceSourceRef,
    val bindingRule: TraceSourceRef,
    val representationDefinition: TraceSourceRef,
    val resourceSnapshot: TraceDigest,
    val owningDeclarations: List<String>,
) {
    init {
        require(owningDeclarations.isNotEmpty()) { "Trace source chain requires owning declarations." }
        require(owningDeclarations.all(String::isNotBlank)) { "Trace owning declarations must not be blank." }
    }
}

data class GraphicOccurrenceTraceEntry(
    val occurrenceId: GraphicOccurrenceId,
    val semanticSubjectId: StableSemanticIdentity,
    val sourceChain: GraphicOccurrenceSourceChain,
)

data class GraphicSelectablePrimitiveTraceRef(
    val primitiveId: GraphicPrimitiveId,
    val occurrenceId: GraphicOccurrenceId,
    val semanticSubjectId: StableSemanticIdentity,
)

data class GraphicOccurrenceTraceEvidence(
    val selectablePrimitiveCount: Int,
    val decorativePrimitiveCount: Int,
    val traceEntryCount: Int,
    val missingTraceCount: Int,
    val unusedTraceCount: Int,
    val duplicatePrimitiveOwnerCount: Int,
)

data class GraphicOccurrenceTraceTable(
    val version: GraphicOccurrenceTraceTableVersion,
    val selectablePrimitives: List<GraphicSelectablePrimitiveTraceRef>,
    val decorativePrimitiveIds: List<GraphicPrimitiveId>,
    val entries: List<GraphicOccurrenceTraceEntry>,
    val evidence: GraphicOccurrenceTraceEvidence,
) {
    fun toStableTransportString(): String = buildString {
        append("version=").append(version.value).append('\n')
        append("selectable=")
        append(
            selectablePrimitives.joinToString("|") { ref ->
                listOf(ref.primitiveId.value, ref.occurrenceId.value, ref.semanticSubjectId.value).joinToString(">")
            },
        )
        append('\n')
        append("decorative=")
        append(decorativePrimitiveIds.joinToString("|") { id -> id.value })
        append('\n')
        entries.forEach { entry ->
            append("entry=")
            append(entry.occurrenceId.value).append(';')
            append(entry.semanticSubjectId.value).append(';')
            append(entry.sourceChain.installationDeclaration.transport()).append(';')
            append(entry.sourceChain.mountedOccurrence.transport()).append(';')
            append(entry.sourceChain.bindingRule.transport()).append(';')
            append(entry.sourceChain.representationDefinition.transport()).append(';')
            append(entry.sourceChain.resourceSnapshot.value).append(';')
            append(entry.sourceChain.owningDeclarations.joinToString(","))
            append('\n')
        }
        append("evidence=")
        append(
            listOf(
                evidence.selectablePrimitiveCount,
                evidence.decorativePrimitiveCount,
                evidence.traceEntryCount,
                evidence.missingTraceCount,
                evidence.unusedTraceCount,
                evidence.duplicatePrimitiveOwnerCount,
            ).joinToString(","),
        )
    }
}

data class GraphicOccurrenceTraceRequest(
    val document: GraphicPrimitiveDocument,
    val selectablePrimitives: List<GraphicSelectablePrimitiveTraceRef>,
    val decorativePrimitiveIds: List<GraphicPrimitiveId>,
    val traceEntries: List<GraphicOccurrenceTraceEntry>,
)

data class GraphicOccurrenceTraceDiagnostic(
    val code: String,
    val subject: String,
    val measured: String?,
    val expected: String,
)

sealed interface GraphicOccurrenceTraceCompilation {
    data class Success(val table: GraphicOccurrenceTraceTable) : GraphicOccurrenceTraceCompilation

    data class Failure(val diagnostics: List<GraphicOccurrenceTraceDiagnostic>) : GraphicOccurrenceTraceCompilation
}

object GraphicOccurrenceTraceTableCompiler {
    fun compile(request: GraphicOccurrenceTraceRequest): GraphicOccurrenceTraceCompilation {
        val diagnostics = mutableListOf<GraphicOccurrenceTraceDiagnostic>()
        val primitiveIds = request.document.primitives.flatMap { primitive -> primitive.flattenIds() }.toSet()
        val selectablePrimitiveOwners = request.selectablePrimitives.groupBy { ref -> ref.primitiveId }
        val selectableOccurrenceIds = request.selectablePrimitives.map { ref -> ref.occurrenceId }.toSet()
        val traceEntriesByOccurrence = request.traceEntries.groupBy { entry -> entry.occurrenceId }

        selectablePrimitiveOwners
            .filterValues { owners -> owners.map { it.occurrenceId }.toSet().size > 1 }
            .forEach { (primitiveId, owners) ->
                diagnostics += diagnostic(
                    "graphic.trace.primitive.duplicate_owner",
                    primitiveId.value,
                    owners.joinToString(",") { owner -> owner.occurrenceId.value },
                    "one GraphicOccurrenceId per selectable primitive",
                )
            }

        request.selectablePrimitives
            .filterNot { ref -> ref.primitiveId in primitiveIds }
            .forEach { ref ->
                diagnostics += diagnostic(
                    "graphic.trace.primitive.missing",
                    ref.primitiveId.value,
                    null,
                    "selectable primitive id exists in GraphicPrimitiveDocument",
                )
            }

        request.decorativePrimitiveIds
            .filterNot { id -> id in primitiveIds }
            .forEach { id ->
                diagnostics += diagnostic(
                    "graphic.trace.decorative.missing",
                    id.value,
                    null,
                    "decorative primitive id exists in GraphicPrimitiveDocument",
                )
            }

        traceEntriesByOccurrence
            .filterValues { entries -> entries.size > 1 }
            .forEach { (occurrenceId, entries) ->
                diagnostics += diagnostic(
                    "graphic.trace.entry.duplicate",
                    occurrenceId.value,
                    entries.size.toString(),
                    "one normalized trace entry per GraphicOccurrenceId",
                )
            }

        request.selectablePrimitives
            .filterNot { ref -> ref.occurrenceId in traceEntriesByOccurrence }
            .forEach { ref ->
                diagnostics += diagnostic(
                    "graphic.trace.entry.missing",
                    ref.occurrenceId.value,
                    ref.primitiveId.value,
                    "one trace entry for each selectable occurrence id",
                )
            }

        request.traceEntries
            .filterNot { entry -> entry.occurrenceId in selectableOccurrenceIds }
            .forEach { entry ->
                diagnostics += diagnostic(
                    "graphic.trace.entry.unused",
                    entry.occurrenceId.value,
                    entry.semanticSubjectId.value,
                    "trace entry referenced by at least one selectable primitive",
                )
            }

        request.traceEntries
            .filter { entry -> entry.semanticSubjectId.value.startsWith("synthetic:") }
            .forEach { entry ->
                diagnostics += diagnostic(
                    "graphic.trace.subject.synthetic",
                    entry.occurrenceId.value,
                    entry.semanticSubjectId.value,
                    "authoritative semantic subject id",
                )
            }

        request.selectablePrimitives.forEach { ref ->
            val trace = traceEntriesByOccurrence[ref.occurrenceId]?.singleOrNull()
            if (trace != null && trace.semanticSubjectId != ref.semanticSubjectId) {
                diagnostics += diagnostic(
                    "graphic.trace.subject.mismatch",
                    ref.occurrenceId.value,
                    "${ref.semanticSubjectId.value} != ${trace.semanticSubjectId.value}",
                    "selectable primitive subject equals trace entry subject",
                )
            }
        }

        if (diagnostics.isNotEmpty()) {
            return GraphicOccurrenceTraceCompilation.Failure(
                diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.measured.orEmpty() })),
            )
        }

        val selectable = request.selectablePrimitives.sortedWith(
            compareBy<GraphicSelectablePrimitiveTraceRef> { ref -> ref.primitiveId.value }
                .thenBy { ref -> ref.occurrenceId.value },
        )
        val decorative = request.decorativePrimitiveIds.distinctBy { id -> id.value }.sortedBy { id -> id.value }
        val entries = request.traceEntries.sortedBy { entry -> entry.occurrenceId.value }
        val evidence = GraphicOccurrenceTraceEvidence(
            selectablePrimitiveCount = selectable.size,
            decorativePrimitiveCount = decorative.size,
            traceEntryCount = entries.size,
            missingTraceCount = 0,
            unusedTraceCount = 0,
            duplicatePrimitiveOwnerCount = 0,
        )

        return GraphicOccurrenceTraceCompilation.Success(
            GraphicOccurrenceTraceTable(
                version = GraphicOccurrenceTraceTableVersion(1),
                selectablePrimitives = selectable,
                decorativePrimitiveIds = decorative,
                entries = entries,
                evidence = evidence,
            ),
        )
    }
}

private fun GraphicPrimitive.flattenIds(): List<GraphicPrimitiveId> = when (this) {
    is GraphicPrimitive.Group -> listOf(primitiveId) + children.flatMap { child -> child.flattenIds() }
    is GraphicPrimitive.Transformed -> listOf(primitiveId) + child.flattenIds()
    else -> listOf(primitiveId)
}

private fun TraceSourceRef.transport(): String =
    "${declarationId}@${span.file}:${span.line}:${span.column}#${digest.value}"

private fun diagnostic(
    code: String,
    subject: String,
    measured: String?,
    expected: String,
): GraphicOccurrenceTraceDiagnostic = GraphicOccurrenceTraceDiagnostic(code, subject, measured, expected)
