package com.engineeringood.athena.reuse

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticMacroPreviewContractTest {
    @Test
    fun `preview stays review first and canonical identity aware`() {
        val preview = SemanticMacroPreview(
            previewId = SemanticMacroPreviewId("preview:panel-meter"),
            macroId = SemanticMacroId("macro:panel-meter"),
            instantiationId = SemanticMacroInstantiationId("instance:panel-meter-1"),
            title = "Instantiate panel meter",
            changes = listOf(
                SemanticMacroPreviewChange(
                    kind = SemanticMacroPreviewChangeKind.CREATE,
                    title = "Create panel meter assembly",
                    summary = "Adds the meter body and one measurement connection.",
                    affectedSubjectIdentities = setOf(
                        StableSemanticIdentity("component:PM1"),
                        StableSemanticIdentity("connection:PM1.measurement"),
                    ),
                ),
            ),
            warnings = listOf("Terminal assignment remains pending package validation."),
        )

        assertEquals(SemanticMacroPreviewStatus.PENDING_REVIEW, preview.status)
        assertEquals("macro:panel-meter", preview.macroId.value)
        assertEquals("instance:panel-meter-1", preview.instantiationId.value)
        assertEquals(1, preview.changes.size)
        assertTrue(preview.changes.single().affectedSubjectIdentities.any { identity -> identity.value == "component:PM1" })
    }
}
