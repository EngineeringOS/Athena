package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InteractionRevealServiceTest {
    @Test
    fun `reveal returns all available targets for a registered subject`() {
        val key = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
            subjectKind = InteractionSubjectKind.COMPONENT,
            sourceContextId = "file:///workspace/main.athena",
        )
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = "file:///workspace/main.athena",
                subjects = listOf(
                    InteractionRegistrySubjectFact(
                        canonicalSubjectId = key.canonicalSubjectId,
                        subjectKind = key.subjectKind,
                        sourceRange = SourceRangeRef("file:///workspace/main.athena", 4, 3, 12, 4),
                        diagnosticId = "diagnostic:PLC1",
                    ),
                ),
                occurrences = listOf(
                    InteractionRegistryOccurrenceFact(
                        canonicalSubjectId = key.canonicalSubjectId,
                        subjectKind = key.subjectKind,
                        projectionViewId = "schematic",
                        occurrenceId = "occurrence:PLC1",
                    ),
                ),
            ),
        )

        val result = InteractionRevealService.reveal(
            registry = registry,
            request = InteractionRevealRequest(
                subject = key,
                preferredTargets = setOf(
                    InteractionRevealSurface.SOURCE,
                    InteractionRevealSurface.GRAPH,
                    InteractionRevealSurface.PROBLEMS,
                ),
            ),
        )

        assertEquals(false, result.partial)
        assertEquals(
            setOf(InteractionRevealSurface.SOURCE, InteractionRevealSurface.GRAPH, InteractionRevealSurface.PROBLEMS),
            result.targets.map { target -> target.target }.toSet(),
        )
        assertEquals(emptyList(), result.diagnostics)
    }

    @Test
    fun `reveal reports missing targets without guessing`() {
        val key = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("port:PLC1.power"),
            subjectKind = InteractionSubjectKind.PORT,
            sourceContextId = "file:///workspace/main.athena",
        )
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = "file:///workspace/main.athena",
                subjects = listOf(
                    InteractionRegistrySubjectFact(
                        canonicalSubjectId = key.canonicalSubjectId,
                        subjectKind = key.subjectKind,
                    ),
                ),
            ),
        )

        val result = InteractionRevealService.reveal(
            registry = registry,
            request = InteractionRevealRequest(
                subject = key,
                preferredTargets = setOf(InteractionRevealSurface.SOURCE, InteractionRevealSurface.GRAPH),
            ),
        )

        assertTrue(result.partial)
        assertEquals(emptyList(), result.targets)
        assertEquals(
            listOf(InteractionDiagnosticCode.REVEAL_MISSING_TARGET, InteractionDiagnosticCode.REVEAL_MISSING_TARGET),
            result.diagnostics.map { diagnostic -> diagnostic.code },
        )
    }
}
