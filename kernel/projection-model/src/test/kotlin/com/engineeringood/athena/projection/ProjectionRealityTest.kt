package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetSubject
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectionRealityTest {
    @Test
    fun `projection reality declares authority identity and required facts`() {
        assertEquals("Projection Reality", ProjectionReality.declaration.name)
        assertEquals("ProjectionDocument", ProjectionReality.rootName)
        assertEquals("projection compiler", ProjectionReality.authority)
        assertContains(ProjectionReality.ownedFacts, "view")
        assertContains(ProjectionReality.ownedFacts, "sheet")
        assertContains(ProjectionReality.ownedFacts, "occurrence")
        assertContains(ProjectionReality.ownedFacts, "projection group")
        assertContains(ProjectionReality.ownedFacts, "reading order")
        assertContains(ProjectionReality.requiredFacts, "view identity")
        assertContains(ProjectionReality.requiredFacts, "sheet identity")
        assertContains(ProjectionReality.requiredFacts, "occurrence source identity")
        assertTrue(ProjectionReality.identityRules.any { rule -> rule.fact == "occurrence" })
        assertFalse(ProjectionReality.ownedFacts.any { fact -> fact.contains("coordinate", ignoreCase = true) })
        assertFalse(ProjectionReality.ownedFacts.any { fact -> fact.contains("route", ignoreCase = true) })
        assertFalse(ProjectionReality.ownedFacts.any { fact -> fact.contains("stroke", ignoreCase = true) })
    }

    @Test
    fun `projection validation reports missing facts in plain language`() {
        val result = ProjectionReality.validate(
            ProjectionDocument(
                view = ViewDefinition(id = "", displayName = "Broken"),
                nodes = listOf(
                    ProjectionNode(
                        projectionId = ProjectionNodeId("node:blank"),
                        semanticId = StableSemanticIdentity(""),
                        label = "Blank",
                        originGeometryElementId = GeometryElementId("geometry:blank"),
                    ),
                ),
                connections = emptyList(),
                sheets = emptyList(),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.issues.any { issue ->
            issue.reality == "Projection Reality" && issue.message == "missing view identity"
        })
        assertTrue(result.issues.any { issue ->
            issue.reality == "Projection Reality" && issue.message == "missing sheet facts"
        })
        assertTrue(result.issues.any { issue ->
            issue.reality == "Projection Reality" && issue.message == "missing occurrence source identity"
        })
        assertTrue(result.issues.any { issue ->
            issue.reality == "Projection Reality" && issue.message == "missing reading order"
        })
    }

    @Test
    fun `projection validation rejects duplicate sheet identities`() {
        val view = ViewDefinition(id = "schematic", displayName = "Schematic")
        val sheet = ProjectionSheet(
            sheetId = ProjectionSheetId("schematic/sheet/01-main"),
            displayName = "Main",
            order = 0,
            subjects = listOf(ProjectionSheetSubject(StableSemanticIdentity("component:supply"))),
        )
        val result = ProjectionReality.validate(
            ProjectionDocument(
                view = view,
                nodes = emptyList(),
                connections = emptyList(),
                sheets = listOf(sheet, sheet.copy(order = 1)),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.issues.any { issue ->
            issue.reality == "Projection Reality" && issue.message == "duplicate sheet identity"
        })
    }

    @Test
    fun `projection validation rejects empty sheet`() {
        val view = ViewDefinition(id = "schematic", displayName = "Schematic")
        val result = ProjectionReality.validate(
            ProjectionDocument(
                view = view,
                nodes = emptyList(),
                connections = emptyList(),
                sheets = listOf(
                    ProjectionSheet(
                        sheetId = ProjectionSheetId("schematic/sheet/01-main"),
                        displayName = "Main",
                        order = 0,
                        subjects = emptyList(),
                    ),
                ),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.issues.any { issue ->
            issue.reality == "Projection Reality" && issue.message == "empty sheet"
        })
    }

    @Test
    fun `projection validation rejects sheet outside document view`() {
        val view = ViewDefinition(id = "schematic", displayName = "Schematic")
        val result = ProjectionReality.validate(
            ProjectionDocument(
                view = view,
                nodes = emptyList(),
                connections = emptyList(),
                sheets = listOf(
                    ProjectionSheet(
                        sheetId = ProjectionSheetId("other-view/sheet/01-main"),
                        displayName = "Main",
                        order = 0,
                        subjects = listOf(ProjectionSheetSubject(StableSemanticIdentity("component:supply"))),
                    ),
                ),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.issues.any { issue ->
            issue.reality == "Projection Reality" && issue.message == "missing sheet view membership"
        })
    }

    @Test
    fun `same view and sheets produce stable sheet identity and reading order`() {
        val view = ViewDefinition(id = "schematic", displayName = "Schematic")
        fun document(): ProjectionDocument =
            ProjectionDocument(
                view = view,
                nodes = emptyList(),
                connections = emptyList(),
                sheets = listOf(
                    ProjectionSheet(
                        sheetId = ProjectionSheetId("schematic/sheet/01-main"),
                        displayName = "Main",
                        order = 0,
                        subjects = listOf(ProjectionSheetSubject(StableSemanticIdentity("component:supply"))),
                    ),
                ),
            )

        val first = document()
        val second = document()

        assertEquals(
            first.sheets.map { sheet -> sheet.sheetId.value },
            second.sheets.map { sheet -> sheet.sheetId.value },
        )
        assertEquals(first.sheets.map { sheet -> sheet.order }, second.sheets.map { sheet -> sheet.order })
        assertTrue(ProjectionReality.validate(first).isValid)
        assertEquals("schematic", first.sheets.single().viewId)
    }

    @Test
    fun `projection reality naming stays product-clean`() {
        val names =
            listOf(ProjectionReality.name, ProjectionReality.rootName, ProjectionReality.authority) +
                ProjectionReality.ownedFacts +
                ProjectionReality.requiredFacts
        val forbidden = listOf("M40", "V0", "V1", "Evidence", "Compatibility", "ProfessionalDrawing")
        forbidden.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token, ignoreCase = true) },
                "Projection reality must not carry `$token`.",
            )
        }
    }
}
