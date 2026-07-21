package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractionSubjectResolutionTest {
    @Test
    fun `resolver resolves governed selection payloads to registered interaction subjects`() {
        val key = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("route:power"),
            subjectKind = InteractionSubjectKind.ROUTE,
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

        val result = InteractionSubjectResolver.resolve(
            registry = registry,
            selection = InteractionSelectionPayload(
                subjectKey = key,
                adapterMetadata = mapOf("svgPathId" to "edge-1"),
            ),
        )

        assertEquals(key, result.subject?.key)
        assertEquals(emptyList(), result.diagnostics)
    }

    @Test
    fun `resolver rejects selections without governed subject keys`() {
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = "file:///workspace/main.athena",
                subjects = emptyList(),
            ),
        )

        val result = InteractionSubjectResolver.resolve(
            registry = registry,
            selection = InteractionSelectionPayload(
                subjectKey = null,
                adapterMetadata = mapOf("text" to "PLC1", "svgX" to "100"),
            ),
        )

        assertEquals(null, result.subject)
        assertEquals(InteractionDiagnosticCode.SUBJECT_UNRESOLVED, result.diagnostics.single().code)
    }
}
