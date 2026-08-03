package com.engineeringood.athena.projection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectionConstructContractTest {

    @Test
    fun `contract carries identity source trace membership and domain-provided kind`() {
        val construct = TestConstruct(
            constructId = ProjectionConstructId("schematic/S1/rail:main"),
            kind = "rail",
            sourceTrace = "01-project.athena:12:5",
            memberNames = listOf("Supply.L1", "Breaker.line"),
        )

        assertEquals("schematic/S1/rail:main", construct.constructId.value)
        assertEquals("rail", construct.kind)
        assertEquals("01-project.athena:12:5", construct.sourceTrace)
        assertEquals(listOf("Supply.L1", "Breaker.line"), construct.memberNames)
        assertTrue(construct.validationIssues().isEmpty())
    }

    @Test
    fun `validation shape reports member problems in plain language`() {
        val construct = TestConstruct(
            constructId = ProjectionConstructId("schematic/S1/rung:1"),
            kind = "rung",
            sourceTrace = "01-project.athena:14:5",
            memberNames = emptyList(),
        )

        assertEquals(listOf("Rung 'rung:1' has no members. Add at least one occurrence."), construct.validationIssues())
    }

    private class TestConstruct(
        override val constructId: ProjectionConstructId,
        override val kind: String,
        override val sourceTrace: String,
        override val memberNames: List<String>,
    ) : ProjectionConstruct {
        override fun validationIssues(): List<String> =
            if (memberNames.isEmpty()) {
                listOf("${kind.replaceFirstChar { it.uppercase() }} '$kind:${constructId.value.substringAfterLast(':')}' has no members. Add at least one occurrence.")
            } else {
                emptyList()
            }
    }
}
