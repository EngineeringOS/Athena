package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PresentationRealityTest {
    @Test
    fun `presentation reality declares authority identity and required facts`() {
        assertEquals("Presentation Reality", PresentationReality.declaration.name)
        assertEquals("PresentationDocument", PresentationReality.rootName)
        assertEquals("presentation compiler", PresentationReality.authority)
        assertContains(PresentationReality.ownedFacts, "shape")
        assertContains(PresentationReality.ownedFacts, "connector visual")
        assertContains(PresentationReality.ownedFacts, "stroke")
        assertContains(PresentationReality.ownedFacts, "label")
        assertContains(PresentationReality.ownedFacts, "visibility")
        assertContains(PresentationReality.ownedFacts, "paint order")
        assertContains(PresentationReality.requiredFacts, "paint target")
        assertContains(PresentationReality.requiredFacts, "visibility")
        assertContains(PresentationReality.requiredFacts, "paint order")
        assertTrue(PresentationReality.identityRules.any { rule -> rule.fact == "paint order" })
        assertFalse(PresentationReality.ownedFacts.any { fact -> fact.contains("engineering truth", ignoreCase = true) })
        assertFalse(PresentationReality.ownedFacts.any { fact -> fact.contains("route authority", ignoreCase = true) })
    }

    @Test
    fun `presentation validation reports missing facts in plain language`() {
        val result = PresentationReality.validate(
            PresentationDocument(
                view = ViewDefinition(id = "view:control", displayName = "Control"),
                canvasWidth = 100,
                canvasHeight = 100,
                primitivePacks = emptyList(),
                compositePacks = emptyList(),
                occurrences = listOf(
                    PresentationOccurrence(
                        occurrenceId = PresentationOccurrenceId("occurrence:broken"),
                        semanticId = StableSemanticIdentity(""),
                        reference = PresentationPrimitiveOccurrenceReference(PresentationPrimitiveId("primitive:broken")),
                        bounds = PresentationBounds(0, 0, 10, 10),
                        layer = PresentationLayer.DEVICE,
                    ),
                ),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.issues.any { issue ->
            issue.reality == "Presentation Reality" && issue.message == "missing paint target"
        })
        assertTrue(result.issues.any { issue ->
            issue.reality == "Presentation Reality" && issue.message == "missing visibility and paint order"
        })
    }
}
