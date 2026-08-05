package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringPortOwner
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.ExactNumber
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import java.math.BigInteger
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class EngineeringAnatomyLoweringTest {
    @Test
    fun `lowers six exact value variants and entity function port structure anatomy`() {
        val success = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("src/conveyor.athena"), source(structure = "Drive")),
        )
        val entity = success.document.entities.single { entity -> entity.name == "M1" }
        val values = entity.properties.associate { property -> property.name to property.value }

        assertEquals(StableSemanticIdentity("entity:M1"), entity.id)
        assertEquals(listOf("motor"), entity.conceptReference.authoredName)
        assertEquals(ExactNumber.of(15, 2), assertIs<EngineeringValue.Quantity>(values.getValue("ratedPower")).value)
        assertEquals(listOf("unit", "kilowatt"), assertIs<EngineeringValue.Quantity>(values.getValue("ratedPower")).unit.authoredName)
        assertNull(assertIs<EngineeringValue.Quantity>(values.getValue("ratedPower")).unit.resolvedId)
        assertEquals(BigInteger.valueOf(4), assertIs<EngineeringValue.Integer>(values.getValue("poleCount")).value)
        assertEquals(true, assertIs<EngineeringValue.Boolean>(values.getValue("enabled")).value)
        assertEquals("Drive motor", assertIs<EngineeringValue.Text>(values.getValue("label")).text)
        assertEquals("main", assertIs<EngineeringValue.Symbol>(values.getValue("mode")).text)
        assertEquals(
            listOf("Supply", "main"),
            assertIs<EngineeringValue.Reference>(values.getValue("source")).reference.authoredPath,
        )
        assertEquals("Drive", assertIs<EngineeringValue.Text>(entity.structureAssignments.single().value).text)
        assertEquals("=", entity.structureAssignments.single().displayDesignation)

        val function = success.document.functions.single()
        assertEquals(StableSemanticIdentity("function:M1.electricalInput"), function.id)
        assertEquals(entity.id, function.owner.reference.resolvedIdentity)
        assertEquals(listOf("input"), function.roleReference.authoredName)

        val entityPort = success.document.ports.single { port -> port.name == "shaft" }
        val functionPort = success.document.ports.single { port -> port.name == "supply" }
        assertIs<EngineeringPortOwner.Entity>(entityPort.owner)
        assertIs<EngineeringPortOwner.Function>(functionPort.owner)
        assertEquals(StableSemanticIdentity("port:M1.electricalInput.supply"), functionPort.id)
        assertEquals(1, functionPort.cardinality.minimum)
        assertEquals(1, functionPort.cardinality.maximum)
        assertEquals(listOf("electricalPower"), functionPort.admittedFlowReferences.single().authoredName)
        assertEquals("U1", assertIs<EngineeringValue.Text>(functionPort.interfaceDesignation?.value).text)
    }

    @Test
    fun `omitted port cardinality maximum remains unbounded`() {
        val success = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/conveyor.athena"),
                """
                system Conveyor {
                  entity M1 {
                    concept motor
                    port shaft {
                      direction out
                      flow mechanicalPower
                    }
                  }
                }
                """.trimIndent(),
            ),
        )

        assertEquals(0, success.document.ports.single().cardinality.minimum)
        assertNull(success.document.ports.single().cardinality.maximum)
    }

    @Test
    fun `structure and declaration order do not form semantic identities`() {
        val compiler = AthenaCompiler()
        val before = assertIs<CompilerCompilationSuccess>(
            compiler.compile(Path.of("src/conveyor.athena"), source(structure = "Drive", auxiliaryFirst = false)),
        ).document
        val after = assertIs<CompilerCompilationSuccess>(
            compiler.compile(Path.of("src/conveyor.athena"), source(structure = "Process", auxiliaryFirst = true)),
        ).document

        assertEquals(before.entities.map { it.id }.sortedBy { it.value }, after.entities.map { it.id }.sortedBy { it.value })
        assertEquals(before.functions.map { it.id }.sortedBy { it.value }, after.functions.map { it.id }.sortedBy { it.value })
        assertEquals(before.ports.map { it.id }.sortedBy { it.value }, after.ports.map { it.id }.sortedBy { it.value })
        assertEquals(before.relationships, after.relationships)
    }

    @Test
    fun `unresolved authored port owner reports exact plain diagnostic and provenance`() {
        val sourcePath = Path.of("src/conveyor.athena")
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                sourcePath,
                """
                system Conveyor {
                  port Missing.input {
                    direction in
                  }
                }
                """.trimIndent(),
            ),
        )

        val diagnostic = result.semanticResult.diagnostics.single {
            it.ruleId.value == "reference.port-owner.unresolved"
        }
        assertEquals(StableSemanticIdentity("port:Missing.input"), diagnostic.subjectIdentity)
        assertEquals(
            "Port owner `Missing` does not resolve to any canonical semantic object.",
            diagnostic.message,
        )
        assertEquals(
            SourceProvenance(sourcePath.toString(), startLine = 2, startColumn = 8, endLine = 2, endColumn = 21),
            diagnostic.provenance,
        )
    }

    private fun source(structure: String, auxiliaryFirst: Boolean = false): String {
        val auxiliary = "entity AUX { concept sensor port feedback { direction out flow information minimum 0 maximum 1 } }"
        val motor =
            """
            entity M1 {
              concept motor
              ratedPower 7.5 [unit.kilowatt]
              poleCount 4
              enabled true
              label "Drive motor"
              mode main
              source @Supply.main
              structure functional "$structure" display "="
              port shaft {
                direction out
                flow mechanicalPower
                minimum 0
                maximum unbounded
              }
              function electricalInput {
                role input
                port supply {
                  direction in
                  flow electricalPower
                  minimum 1
                  maximum 1
                  designationType terminal
                  designation "U1"
                }
              }
            }
            """.trimIndent()
        val declarations = if (auxiliaryFirst) "$auxiliary\n$motor" else "$motor\n$auxiliary"
        return "system Conveyor {\n$declarations\n}"
    }
}
