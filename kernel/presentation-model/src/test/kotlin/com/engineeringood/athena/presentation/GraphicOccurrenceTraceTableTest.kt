package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleTokenId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class GraphicOccurrenceTraceTableTest {
    @Test
    fun `builds normalized trace table for selectable primitives and stable transport`() {
        val result = GraphicOccurrenceTraceTableCompiler.compile(
            GraphicOccurrenceTraceRequest(
                document = document(),
                selectablePrimitives = listOf(
                    selectable("mounted-body:SRC", "occ:SRC", "component:Source"),
                    selectable("mounted-label:SRC", "occ:SRC", "component:Source"),
                    selectable("route:Main", "route:Main", "connection:Main"),
                ),
                decorativePrimitiveIds = listOf(GraphicPrimitiveId("frame:main")),
                traceEntries = listOf(
                    trace("occ:SRC", "component:Source"),
                    trace("route:Main", "connection:Main"),
                ),
            ),
        )

        val table = assertIs<GraphicOccurrenceTraceCompilation.Success>(result).table

        assertEquals(GraphicOccurrenceTraceTableVersion(1), table.version)
        assertEquals(
            listOf(
                GraphicSelectablePrimitiveTraceRef(
                    primitiveId = GraphicPrimitiveId("mounted-body:SRC"),
                    occurrenceId = GraphicOccurrenceId("occ:SRC"),
                    semanticSubjectId = StableSemanticIdentity("component:Source"),
                ),
                GraphicSelectablePrimitiveTraceRef(
                    primitiveId = GraphicPrimitiveId("mounted-label:SRC"),
                    occurrenceId = GraphicOccurrenceId("occ:SRC"),
                    semanticSubjectId = StableSemanticIdentity("component:Source"),
                ),
                GraphicSelectablePrimitiveTraceRef(
                    primitiveId = GraphicPrimitiveId("route:Main"),
                    occurrenceId = GraphicOccurrenceId("route:Main"),
                    semanticSubjectId = StableSemanticIdentity("connection:Main"),
                ),
            ),
            table.selectablePrimitives,
        )
        assertEquals(listOf(GraphicPrimitiveId("frame:main")), table.decorativePrimitiveIds)
        assertEquals(listOf(GraphicOccurrenceId("occ:SRC"), GraphicOccurrenceId("route:Main")), table.entries.map { it.occurrenceId })
        assertNotEquals(table.selectablePrimitives.first().primitiveId.value, table.selectablePrimitives.first().occurrenceId.value)
        assertEquals(
            GraphicOccurrenceTraceEvidence(
                selectablePrimitiveCount = 3,
                decorativePrimitiveCount = 1,
                traceEntryCount = 2,
                missingTraceCount = 0,
                unusedTraceCount = 0,
                duplicatePrimitiveOwnerCount = 0,
            ),
            table.evidence,
        )

        val firstBytes = table.toStableTransportString()
        val secondBytes = table.toStableTransportString()

        assertEquals(firstBytes, secondBytes)
        assertFalse(firstBytes.contains("sourceBytes", ignoreCase = true))
        assertFalse(firstBytes.contains("filesystem", ignoreCase = true))
        assertFalse(firstBytes.contains("dom", ignoreCase = true))
        assertFalse(firstBytes.contains("svgNode", ignoreCase = true))
    }

    @Test
    fun `fails closed on duplicate missing synthetic and mismatched traces`() {
        val duplicatePrimitive = assertIs<GraphicOccurrenceTraceCompilation.Failure>(
            GraphicOccurrenceTraceTableCompiler.compile(
                GraphicOccurrenceTraceRequest(
                    document = document(),
                    selectablePrimitives = listOf(
                        selectable("mounted-body:SRC", "occ:A", "component:A"),
                        selectable("mounted-body:SRC", "occ:B", "component:B"),
                    ),
                    decorativePrimitiveIds = emptyList(),
                    traceEntries = listOf(trace("occ:A", "component:A"), trace("occ:B", "component:B")),
                ),
            ),
        )
        val missingTrace = assertIs<GraphicOccurrenceTraceCompilation.Failure>(
            GraphicOccurrenceTraceTableCompiler.compile(
                GraphicOccurrenceTraceRequest(
                    document = document(),
                    selectablePrimitives = listOf(selectable("mounted-body:SRC", "occ:Missing", "component:Missing")),
                    decorativePrimitiveIds = emptyList(),
                    traceEntries = emptyList(),
                ),
            ),
        )
        val syntheticTrace = assertIs<GraphicOccurrenceTraceCompilation.Failure>(
            GraphicOccurrenceTraceTableCompiler.compile(
                GraphicOccurrenceTraceRequest(
                    document = document(),
                    selectablePrimitives = emptyList(),
                    decorativePrimitiveIds = emptyList(),
                    traceEntries = listOf(trace("occ:Synthetic", "synthetic:ghost")),
                ),
            ),
        )
        val mismatchedTrace = assertIs<GraphicOccurrenceTraceCompilation.Failure>(
            GraphicOccurrenceTraceTableCompiler.compile(
                GraphicOccurrenceTraceRequest(
                    document = document(),
                    selectablePrimitives = listOf(selectable("mounted-body:SRC", "occ:SRC", "component:Source")),
                    decorativePrimitiveIds = emptyList(),
                    traceEntries = listOf(trace("occ:SRC", "component:Other")),
                ),
            ),
        )

        assertEquals(
            setOf(
                "graphic.trace.primitive.duplicate_owner",
                "graphic.trace.entry.missing",
                "graphic.trace.entry.unused",
                "graphic.trace.subject.synthetic",
                "graphic.trace.subject.mismatch",
            ),
            listOf(duplicatePrimitive, missingTrace, syntheticTrace, mismatchedTrace)
                .flatMap { failure -> failure.diagnostics }
                .map { diagnostic -> diagnostic.code }
                .toSet(),
        )
    }

    private fun document(): GraphicPrimitiveDocument = GraphicPrimitiveDocument(
        documentId = GraphicPrimitiveDocumentId("cabinet"),
        bounds = GraphicBounds(0.0, 0.0, 200.0, 160.0),
        primitives = listOf(
            rectangle("frame:main"),
            rectangle("mounted-body:SRC"),
            text("mounted-label:SRC"),
            line("route:Main"),
        ),
        styleTokens = emptyList(),
    )

    private fun selectable(
        primitiveId: String,
        occurrenceId: String,
        subjectId: String,
    ): GraphicSelectablePrimitiveTraceRef = GraphicSelectablePrimitiveTraceRef(
        primitiveId = GraphicPrimitiveId(primitiveId),
        occurrenceId = GraphicOccurrenceId(occurrenceId),
        semanticSubjectId = StableSemanticIdentity(subjectId),
    )

    private fun trace(
        occurrenceId: String,
        subjectId: String,
    ): GraphicOccurrenceTraceEntry = GraphicOccurrenceTraceEntry(
        occurrenceId = GraphicOccurrenceId(occurrenceId),
        semanticSubjectId = StableSemanticIdentity(subjectId),
        sourceChain = GraphicOccurrenceSourceChain(
            installationDeclaration = TraceSourceRef("installation:MainCabinet", TraceSourceSpan("src/main.athena", 10, 3), TraceDigest("sha256:install")),
            mountedOccurrence = TraceSourceRef("mount:$subjectId", TraceSourceSpan("src/main.athena", 20, 5), TraceDigest("sha256:mount")),
            bindingRule = TraceSourceRef("binding:iec", TraceSourceSpan("packages/binding.athena", 4, 1), TraceDigest("sha256:binding")),
            representationDefinition = TraceSourceRef("element:source", TraceSourceSpan("packages/element.athena", 8, 1), TraceDigest("sha256:representation")),
            resourceSnapshot = TraceDigest("sha256:resource"),
            owningDeclarations = listOf("package:com.engineeringood.m35", "source:src/main.athena"),
        ),
    )

    private fun rectangle(id: String): GraphicPrimitive.Rectangle = GraphicPrimitive.Rectangle(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(0.0, 0.0, 10.0, 10.0),
        cornerRadius = 0.0,
        styleTokenId = GraphicStyleTokenId("cabinet"),
    )

    private fun text(id: String): GraphicPrimitive.Text = GraphicPrimitive.Text(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(0.0, 0.0, 10.0, 10.0),
        origin = GraphicPoint(0.0, 10.0),
        text = "SRC",
        styleTokenId = GraphicStyleTokenId("cabinet.label"),
    )

    private fun line(id: String): GraphicPrimitive.Line = GraphicPrimitive.Line(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(0.0, 0.0, 10.0, 0.0),
        start = GraphicPoint(0.0, 0.0),
        end = GraphicPoint(10.0, 0.0),
        styleTokenId = GraphicStyleTokenId("cabinet.route"),
    )
}
