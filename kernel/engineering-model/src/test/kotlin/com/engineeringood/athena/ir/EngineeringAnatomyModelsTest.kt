package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class EngineeringAnatomyModelsTest {
    private val source = SourceProvenance("src/conveyor.athena", 2, 1, 18, 2)
    private val packageName = EngineeringPackageName("com.example.machine")
    private val concept = definition("concept.motor")
    private val mainFunction = definition("function.main")
    private val auxiliaryFunction = definition("function.auxiliary")
    private val flow = definition("flow.energy")
    private val terminalDesignation = definition("interface.terminal-designation")
    private val functionalStructure = definition("structure.functional")

    @Test
    fun `entity function and port anatomy preserves exact typed ownership`() {
        val entityId = StableSemanticIdentity("entity:M1")
        val mainFunctionId = StableSemanticIdentity("function:M1.main")
        val entity = EngineeringEntity(
            id = entityId,
            name = "M1",
            conceptReference = reference(concept, "motor"),
            properties = listOf(property("enabled", EngineeringValue.Boolean(true))),
            structureAssignments = emptyList(),
            provenance = source,
        )
        val functions = listOf(
            EngineeringFunction(
                id = mainFunctionId,
                owner = EngineeringEntityReference(resolvedReference("M1", entityId)),
                name = "main",
                roleReference = reference(mainFunction, "main"),
                properties = emptyList(),
                provenance = source,
            ),
            EngineeringFunction(
                id = StableSemanticIdentity("function:M1.aux"),
                owner = EngineeringEntityReference(resolvedReference("M1", entityId)),
                name = "aux",
                roleReference = reference(auxiliaryFunction, "auxiliary"),
                properties = emptyList(),
                provenance = source,
            ),
        )
        val ports = listOf(
            EngineeringPort(
                id = StableSemanticIdentity("port:M1.shaft"),
                owner = EngineeringPortOwner.Entity(EngineeringEntityReference(resolvedReference("M1", entityId))),
                name = "shaft",
                direction = EngineeringPortDirection.OUTPUT,
                admittedFlowReferences = listOf(reference(flow, "energy")),
                cardinality = EngineeringPortCardinality(0, null),
                interfaceDesignation = null,
                properties = emptyList(),
                provenance = source,
            ),
            EngineeringPort(
                id = StableSemanticIdentity("port:M1.main.input"),
                owner = EngineeringPortOwner.Function(
                    EngineeringFunctionReference(resolvedReference("M1.main", mainFunctionId)),
                ),
                name = "input",
                direction = EngineeringPortDirection.INPUT,
                admittedFlowReferences = listOf(reference(flow, "energy")),
                cardinality = EngineeringPortCardinality(1, 1),
                interfaceDesignation = EngineeringInterfaceDesignation(
                    definitionReference = reference(terminalDesignation, "terminal-designation"),
                    value = EngineeringValue.Text("A1"),
                    provenance = source,
                ),
                properties = emptyList(),
                provenance = source,
            ),
        )

        val document = EngineeringDocument(
            system = EngineeringSystem(StableSemanticIdentity("system:Conveyor"), "Conveyor", source),
            entities = listOf(entity),
            functions = functions,
            ports = ports,
            relationships = emptyList(),
        )

        assertEquals(concept, document.entities.single().conceptReference.resolvedId)
        assertEquals(listOf(mainFunction, auxiliaryFunction), document.functions.map { it.roleReference.resolvedId })
        assertIs<EngineeringPortOwner.Entity>(document.ports.first().owner)
        assertIs<EngineeringPortOwner.Function>(document.ports.last().owner)
        assertEquals(listOf(flow), document.ports.last().admittedFlowReferences.map { it.resolvedId })
        assertEquals(EngineeringPortCardinality(1, 1), document.ports.last().cardinality)
        assertEquals("A1", (document.ports.last().interfaceDesignation?.value as EngineeringValue.Text).text)
        assertNull(document.ports.first().cardinality.maximum)
    }

    @Test
    fun `structure assignment changes display context without changing semantic identities`() {
        val entityId = StableSemanticIdentity("entity:M1")
        val functionId = StableSemanticIdentity("function:M1.main")
        val portId = StableSemanticIdentity("port:M1.main.input")
        val before = anatomy(entityId, functionId, portId, "Drive", "=")
        val after = anatomy(entityId, functionId, portId, "Process", "=")

        assertEquals(before.entities.single().id, after.entities.single().id)
        assertEquals(before.entities.single().conceptReference, after.entities.single().conceptReference)
        assertEquals(before.functions.single().id, after.functions.single().id)
        assertEquals(before.ports.single().id, after.ports.single().id)
        assertEquals(before.relationships, after.relationships)
        assertEquals("Drive", (before.entities.single().structureAssignments.single().value as EngineeringValue.Text).text)
        assertEquals("Process", (after.entities.single().structureAssignments.single().value as EngineeringValue.Text).text)
    }

    @Test
    fun `port cardinality rejects impossible bounds`() {
        assertFailsWith<IllegalArgumentException> { EngineeringPortCardinality(-1, 1) }
        assertFailsWith<IllegalArgumentException> { EngineeringPortCardinality(2, 1) }
    }

    private fun anatomy(
        entityId: StableSemanticIdentity,
        functionId: StableSemanticIdentity,
        portId: StableSemanticIdentity,
        structureValue: String,
        displayDesignation: String,
    ): EngineeringDocument {
        val entityReference = EngineeringEntityReference(resolvedReference("M1", entityId))
        return EngineeringDocument(
            system = EngineeringSystem(StableSemanticIdentity("system:Conveyor"), "Conveyor", source),
            entities = listOf(
                EngineeringEntity(
                    id = entityId,
                    name = "M1",
                    conceptReference = reference(concept, "motor"),
                    properties = emptyList(),
                    structureAssignments = listOf(
                        EngineeringStructureAssignment(
                            aspectReference = reference(functionalStructure, "functional"),
                            value = EngineeringValue.Text(structureValue),
                            displayDesignation = displayDesignation,
                            provenance = source,
                        ),
                    ),
                    provenance = source,
                ),
            ),
            functions = listOf(
                EngineeringFunction(
                    id = functionId,
                    owner = entityReference,
                    name = "main",
                    roleReference = reference(mainFunction, "main"),
                    properties = emptyList(),
                    provenance = source,
                ),
            ),
            ports = listOf(
                EngineeringPort(
                    id = portId,
                    owner = EngineeringPortOwner.Function(
                        EngineeringFunctionReference(resolvedReference("M1.main", functionId)),
                    ),
                    name = "input",
                    direction = EngineeringPortDirection.INPUT,
                    admittedFlowReferences = listOf(reference(flow, "energy")),
                    cardinality = EngineeringPortCardinality(1, 1),
                    interfaceDesignation = null,
                    properties = emptyList(),
                    provenance = source,
                ),
            ),
            relationships = emptyList(),
        )
    }

    private fun property(name: String, value: EngineeringValue): EngineeringProperty =
        EngineeringProperty(name, value, source)

    private fun definition(name: String): EngineeringDefinitionId = EngineeringDefinitionId(packageName, name)

    private fun reference(id: EngineeringDefinitionId, authoredName: String): EngineeringDefinitionReference =
        EngineeringDefinitionReference(listOf(authoredName), id, source)

    private fun resolvedReference(path: String, id: StableSemanticIdentity): EngineeringReference =
        EngineeringReference(path.split('.'), id, source)
}
