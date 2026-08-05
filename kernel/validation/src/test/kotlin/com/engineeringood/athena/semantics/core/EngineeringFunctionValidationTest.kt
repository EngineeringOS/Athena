package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringEntity
import com.engineeringood.athena.ir.EngineeringEntityReference
import com.engineeringood.athena.ir.EngineeringFunction
import com.engineeringood.athena.ir.EngineeringFunctionReference
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPortCardinality
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.ir.EngineeringPortOwner
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringFunctionValidationTest {
    private val validator = EngineeringIrValidator()

    @Test
    fun `valid function owned port preserves exact function owner`() {
        val function = function("function:KM1.coil", "KM1", "coil")
        val port = port(
            id = "port:KM1.coil.A1",
            owner = EngineeringPortOwner.Function(
                EngineeringFunctionReference(reference(listOf("KM1", "coil"), function.id.value)),
            ),
            name = "A1",
        )
        val document = document(functions = listOf(function), ports = listOf(port))

        val result = validator.validate(document)

        assertTrue(result.isSemanticallyValid, result.diagnostics.toString())
        val owner = assertIs<EngineeringPortOwner.Function>(document.ports.single().owner)
        assertEquals(function.id, owner.function.reference.resolvedIdentity)
        assertEquals(listOf("KM1", "coil"), owner.function.reference.authoredPath)
        assertEquals(StableSemanticIdentity("entity:KM1"), document.functions.single().owner.reference.resolvedIdentity)
    }

    @Test
    fun `rejects duplicate function names`() {
        val result = validator.validate(
            document(
                functions = listOf(
                    function("function:KM1.coil", "KM1", "coil"),
                    function("function:KM1.coil#2", "KM1", "coil"),
                ),
            ),
        )

        assertEquals(2, result.diagnostics.count { it.ruleId.value == "uniqueness.function.duplicate-authored-key" })
    }

    @Test
    fun `rejects unresolved and ambiguous function entity owners`() {
        val unresolved = function("function:Missing.coil", "Missing", "coil").copy(
            owner = EngineeringEntityReference(reference(listOf("Missing"), null)),
        )
        val ambiguous = function("function:KM1.coil", "KM1", "coil").copy(
            owner = EngineeringEntityReference(reference(listOf("KM1"), null)),
        )
        val entities = listOf(
            entity("entity:KM1", "KM1"),
            entity("entity:KM1#duplicate", "KM1"),
            entity("entity:KM2", "KM2"),
        )

        val result = validator.validate(
            document(entities = entities, functions = listOf(unresolved, ambiguous)),
        )

        assertEquals(
            listOf("reference.function-owner.ambiguous", "reference.function-owner.unresolved"),
            result.diagnostics.map { it.ruleId.value }.filter { it.startsWith("reference.function-owner") }.sorted(),
        )
    }

    @Test
    fun `rejects unresolved and ambiguous function port owners`() {
        val functions = listOf(
            function("function:KM1.coil", "KM1", "coil"),
            function("function:KM1.coil#duplicate", "KM1", "coil"),
        )
        val ports = listOf(
            port(
                id = "port:Missing.function.A1",
                owner = EngineeringPortOwner.Function(
                    EngineeringFunctionReference(reference(listOf("Missing", "function"), null)),
                ),
                name = "A1",
            ),
            port(
                id = "port:KM1.coil.A1",
                owner = EngineeringPortOwner.Function(
                    EngineeringFunctionReference(reference(listOf("KM1", "coil"), null)),
                ),
                name = "A1",
            ),
        )

        val result = validator.validate(document(functions = functions, ports = ports))

        assertEquals(
            listOf("reference.port-owner.ambiguous", "reference.port-owner.unresolved"),
            result.diagnostics.map { it.ruleId.value }.filter { it.startsWith("reference.port-owner") }.sorted(),
        )
    }

    private fun document(
        entities: List<EngineeringEntity> = listOf(entity("entity:KM1", "KM1"), entity("entity:KM2", "KM2")),
        functions: List<EngineeringFunction>,
        ports: List<EngineeringPort> = emptyList(),
    ): EngineeringDocument = EngineeringDocument(
        system = EngineeringSystem(StableSemanticIdentity("system:Demo"), "Demo", provenance(1)),
        entities = entities,
        ports = ports,
        functions = functions,
    )

    private fun entity(id: String, name: String): EngineeringEntity = EngineeringEntity(
        id = StableSemanticIdentity(id),
        name = name,
        conceptReference = definitionReference("core", "entity"),
        properties = emptyList(),
        structureAssignments = emptyList(),
        provenance = provenance(2),
    )

    private fun port(
        id: String,
        owner: EngineeringPortOwner,
        name: String,
    ): EngineeringPort = EngineeringPort(
        id = StableSemanticIdentity(id),
        owner = owner,
        name = name,
        direction = EngineeringPortDirection.BIDIRECTIONAL,
        admittedFlowReferences = emptyList(),
        cardinality = EngineeringPortCardinality(minimum = 0, maximum = null),
        interfaceDesignation = null,
        properties = emptyList(),
        provenance = provenance(4),
    )

    private fun function(
        id: String,
        owner: String,
        name: String,
    ): EngineeringFunction = EngineeringFunction(
        id = StableSemanticIdentity(id),
        owner = EngineeringEntityReference(reference(listOf(owner), "entity:$owner")),
        name = name,
        roleReference = definitionReference("core", "function"),
        properties = emptyList(),
        provenance = provenance(5),
    )

    private fun definitionReference(vararg authoredName: String): EngineeringDefinitionReference =
        EngineeringDefinitionReference(
            authoredName = authoredName.toList(),
            resolvedId = null,
            provenance = provenance(6),
        )

    private fun reference(path: List<String>, resolved: String?): EngineeringReference = EngineeringReference(
        authoredPath = path,
        resolvedIdentity = resolved?.let(::StableSemanticIdentity),
        provenance = provenance(6),
    )

    private fun provenance(line: Int): SourceProvenance = SourceProvenance("function.athena", line, 1, line, 20)
}
