package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InteractionModelContractTest {
    @Test
    fun `interaction subject separates canonical identity from occurrence and adapter metadata`() {
        val subjectKey = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("component:MotorM101"),
            subjectKind = InteractionSubjectKind.COMPONENT,
            sourceContextId = "file:///workspace/main.athena",
        )
        val occurrenceKey = InteractionOccurrenceKey(
            subjectKey = subjectKey,
            projectionViewId = "schematic",
            sheetId = "sheet:control",
            documentProjectionId = "document:design",
            occurrenceId = "occurrence:MotorM101:schematic",
            sourceRevision = "rev-1",
        )
        val subject = InteractionSubject(
            key = subjectKey,
            occurrences = listOf(occurrenceKey),
            adapterMetadata = mapOf("svgNodeId" to "node-42"),
            provenance = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
        )

        assertEquals("component:MotorM101", subject.key.canonicalSubjectId.value)
        assertEquals("occurrence:MotorM101:schematic", subject.occurrences.single().occurrenceId)
        assertEquals("node-42", subject.adapterMetadata["svgNodeId"])
        assertFalse(subject.adapterMetadata.containsValue(subject.key.canonicalSubjectId.value))
    }

    @Test
    fun `interaction model includes command preview reveal diagnostic lifecycle and provenance`() {
        val subjectKey = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("port:PLC1.power"),
            subjectKind = InteractionSubjectKind.PORT,
        )
        val intent = SemanticActionIntent(
            actionIntentId = "action:connect-power",
            actionFamily = InteractionActionFamily.MUTATE,
            subject = subjectKey,
            targetSubjects = listOf(
                InteractionSubjectKey(
                    canonicalSubjectId = StableSemanticIdentity("port:PS1.lplus"),
                    subjectKind = InteractionSubjectKind.PORT,
                ),
            ),
            requestedBy = InteractionProvenance(
                actor = "user:Aaron",
                originSurface = InteractionOriginSurface.GRAPH,
                reason = "relationship authoring",
                confidence = 1.0,
            ),
            parameters = mapOf("relationshipType" to "ElectricalConnectionRelationship"),
        )
        val diagnostic = InteractionDiagnostic(
            code = InteractionDiagnosticCode.MUTATION_INELIGIBLE,
            severity = InteractionDiagnosticSeverity.ERROR,
            message = "Mutation is blocked",
            subject = subjectKey,
            retryable = false,
        )
        val command = InteractionCommand(
            commandId = "command:connect-power",
            actionIntent = intent,
            lifecycleState = InteractionLifecycleState.PREVIEWING,
            preview = InteractionPreview(
                previewId = "preview:connect-power",
                commandId = "command:connect-power",
                status = InteractionPreviewStatus.BLOCKED,
                affectedSubjects = intent.targetSubjects + subjectKey,
                diagnostics = listOf(diagnostic),
            ),
            diagnostics = listOf(diagnostic),
            undoable = false,
        )
        val reveal = InteractionRevealResult(
            subject = subjectKey,
            targets = listOf(
                InteractionRevealTarget(
                    target = InteractionRevealSurface.SOURCE,
                    sourceRange = SourceRangeRef("file:///workspace/main.athena", 10, 3, 14, 4),
                ),
            ),
            diagnostics = emptyList(),
            partial = false,
        )

        val preview = command.preview ?: error("Expected interaction preview")

        assertEquals(InteractionActionFamily.MUTATE, command.actionIntent.actionFamily)
        assertEquals(InteractionLifecycleState.PREVIEWING, command.lifecycleState)
        assertEquals(InteractionPreviewStatus.BLOCKED, preview.status)
        assertEquals(InteractionDiagnosticCode.MUTATION_INELIGIBLE, command.diagnostics.single().code)
        assertEquals(InteractionRevealSurface.SOURCE, reveal.targets.single().target)
        assertTrue(preview.transient)
        assertFalse(preview.persisted)
    }
}
