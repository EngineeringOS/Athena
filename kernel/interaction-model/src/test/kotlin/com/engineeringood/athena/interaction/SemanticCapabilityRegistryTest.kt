package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SemanticCapabilityRegistryTest {
    @Test
    fun `registry builds subjects from semantic and projection facts`() {
        val input = InteractionRegistryInput(
            sourceContextId = "file:///workspace/main.athena",
            sourceRevision = "rev-1",
            subjects = listOf(
                InteractionRegistrySubjectFact(
                    canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                    subjectKind = InteractionSubjectKind.COMPONENT,
                    sourceRange = SourceRangeRef("file:///workspace/main.athena", 4, 3, 12, 4),
                    capabilities = listOf(
                        SemanticCapability("reveal-source", InteractionActionFamily.REVEAL, enabled = true),
                    ),
                ),
                InteractionRegistrySubjectFact(
                    canonicalSubjectId = StableSemanticIdentity("port:PLC1.power"),
                    subjectKind = InteractionSubjectKind.PORT,
                ),
            ),
            occurrences = listOf(
                InteractionRegistryOccurrenceFact(
                    canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                    subjectKind = InteractionSubjectKind.COMPONENT,
                    projectionViewId = "schematic",
                    sheetId = "sheet:control",
                    occurrenceId = "occurrence:PLC1:schematic",
                    presentationMetadata = mapOf("symbol" to "plc"),
                    standardMetadata = mapOf("profile" to "athena-industrial-control-v0"),
                    adapterMetadata = mapOf("svgNodeId" to "node-PLC1"),
                ),
            ),
        )

        val registry = SemanticCapabilityRegistry.build(input)
        val component = registry.requireSubject(
            InteractionSubjectKey(
                canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                subjectKind = InteractionSubjectKind.COMPONENT,
                sourceContextId = "file:///workspace/main.athena",
            ),
        )
        val port = registry.requireSubject(
            InteractionSubjectKey(
                canonicalSubjectId = StableSemanticIdentity("port:PLC1.power"),
                subjectKind = InteractionSubjectKind.PORT,
                sourceContextId = "file:///workspace/main.athena",
            ),
        )

        assertEquals("rev-1", registry.sourceRevision)
        assertEquals(2, registry.subjects.size)
        assertEquals("occurrence:PLC1:schematic", component.occurrences.single().occurrenceId)
        assertEquals("plc", component.presentationMetadata["symbol"])
        assertEquals("athena-industrial-control-v0", component.standardMetadata["profile"])
        assertEquals("node-PLC1", component.adapterMetadata["svgNodeId"])
        assertFalse(component.key.canonicalSubjectId.value in component.adapterMetadata.values)
        assertTrue(port.occurrences.isEmpty())
    }

    @Test
    fun `registry reports stale active source revisions`() {
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = "file:///workspace/main.athena",
                sourceRevision = "rev-1",
                subjects = listOf(
                    InteractionRegistrySubjectFact(
                        canonicalSubjectId = StableSemanticIdentity("route:power"),
                        subjectKind = InteractionSubjectKind.ROUTE,
                    ),
                ),
            ),
        )

        val diagnostics = registry.diagnosticsForActiveRevision("rev-2")

        assertEquals(InteractionDiagnosticCode.REGISTRY_STALE, diagnostics.single().code)
        assertEquals(InteractionDiagnosticSeverity.WARNING, diagnostics.single().severity)
    }
}
