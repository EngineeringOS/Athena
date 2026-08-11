package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringEntity
import com.engineeringood.athena.ir.EngineeringEntityReference
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringParticipant
import com.engineeringood.athena.ir.EngineeringPortCardinality
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.ir.EngineeringPortOwner
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringRelationship
import com.engineeringood.athena.ir.EngineeringSubjectReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.projection.ProjectionDocument
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringToProjectionTransformationTest {
    @Test
    fun `engineering to projection emits projection document through typed transformation`() {
        val transformation: RealityTransformation<EngineeringDocument, ProjectionDocument> =
            EngineeringToProjectionTransformation()

        val result = transformation.transform(engineeringDocument())

        val success = assertIs<RealityTransformationResult.Success<ProjectionDocument>>(result)
        assertEquals("engineering-projection", success.output.view.id)
        assertEquals(listOf("entity:Supply", "entity:Q1"), success.output.nodes.map {
            node -> node.semanticId.value
        })
        assertEquals(
            listOf(
                "projection/node/entity:Supply" to "port:Supply.L1",
                "projection/node/entity:Q1" to "port:Q1.1",
            ),
            success.output.occurrencePorts.map { port ->
                port.occurrencePortId.occurrenceId.value to port.occurrencePortId.portId.value
            },
        )
        assertTrue(success.output.connections.isEmpty())
        assertEquals(1, success.output.sheets.size)
        assertEquals(0, success.output.sheets.single().order)
        assertEquals("engineering-projection/sheet/01-main", success.output.sheets.single().sheetId.value)
    }

    @Test
    fun `engineering to projection keeps identity trace into sheet subjects`() {
        val result = EngineeringToProjectionTransformation().transform(engineeringDocument())
        val output = assertIs<RealityTransformationResult.Success<ProjectionDocument>>(result).output
        val subjects = output.sheets.single().subjects

        assertContains(subjects.map { subject -> subject.semanticId.value }, "entity:Supply")
        assertTrue(subjects.any { subject ->
            subject.semanticId.value == "entity:Supply" &&
                subject.nodeIds.any { nodeId -> nodeId.value == "projection/node/entity:Supply" }
        })
        assertFalse(subjects.any { subject -> subject.connectionIds.isNotEmpty() })
    }

    @Test
    fun `engineering to projection reports plain diagnostics at reality boundaries`() {
        val result = EngineeringToProjectionTransformation().transform(
            engineeringDocument(
                systemId = "",
                systemName = "",
                sourceFile = "",
            ),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertFalse(failure.diagnostics.isEmpty())
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Engineering Reality" &&
                diagnostic.message == "missing system identity"
        })
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Engineering Reality" &&
                diagnostic.message == "missing engineering source identity"
        })
    }

    @Test
    fun `new transformation names avoid stale architecture terms`() {
        val names = listOf(
            RealityTransformation::class.simpleName.orEmpty(),
            RealityTransformationResult::class.simpleName.orEmpty(),
            RealityTransformationDiagnostic::class.simpleName.orEmpty(),
            EngineeringToProjectionTransformation::class.simpleName.orEmpty(),
        )
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Transformation names must not contain `$token`: $names",
            )
        }
    }

    private fun engineeringDocument(
        systemId: String = "system:Demo",
        systemName: String = "Demo",
        sourceFile: String = "demo.athena",
    ): EngineeringDocument {
        val source = SourceProvenance(sourceFile, 1, 1, 1, 10)
        val supply = EngineeringEntity(
            id = StableSemanticIdentity("entity:Supply"),
            name = "Supply",
            conceptReference = definitionReference("PowerSupply", source),
            properties = emptyList<EngineeringProperty>(),
            structureAssignments = emptyList(),
            provenance = source,
        )
        val breaker = EngineeringEntity(
            id = StableSemanticIdentity("entity:Q1"),
            name = "Q1",
            conceptReference = definitionReference("Breaker", source),
            properties = emptyList<EngineeringProperty>(),
            structureAssignments = emptyList(),
            provenance = source,
        )
        val supplyPort = com.engineeringood.athena.ir.EngineeringPort(
            id = StableSemanticIdentity("port:Supply.L1"),
            owner = EngineeringPortOwner.Entity(EngineeringEntityReference(reference("entity:Supply", source))),
            name = "L1",
            direction = EngineeringPortDirection.OUTPUT,
            admittedFlowReferences = emptyList(),
            cardinality = EngineeringPortCardinality(0, null),
            interfaceDesignation = null,
            properties = emptyList(),
            provenance = source,
        )
        val breakerPort = com.engineeringood.athena.ir.EngineeringPort(
            id = StableSemanticIdentity("port:Q1.1"),
            owner = EngineeringPortOwner.Entity(EngineeringEntityReference(reference("entity:Q1", source))),
            name = "1",
            direction = EngineeringPortDirection.INPUT,
            admittedFlowReferences = emptyList(),
            cardinality = EngineeringPortCardinality(0, null),
            interfaceDesignation = null,
            properties = emptyList(),
            provenance = source,
        )

        return EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity(systemId),
                name = systemName,
                provenance = source,
            ),
            entities = listOf(supply, breaker),
            ports = listOf(supplyPort, breakerPort),
            relationships = listOf(
                EngineeringRelationship(
                    id = StableSemanticIdentity("relationship:Supply.L1-to-Q1.1"),
                    definitionReference = definitionReference("supplies", source),
                    participants = listOf(
                        EngineeringParticipant("source", EngineeringSubjectReference.Port(reference("port:Supply.L1", source)), source),
                        EngineeringParticipant("target", EngineeringSubjectReference.Port(reference("port:Q1.1", source)), source),
                    ),
                    properties = emptyList(),
                    provenance = source,
                ),
            ),
        )
    }

    private fun reference(identity: String, source: SourceProvenance): EngineeringReference =
        EngineeringReference(
            authoredPath = identity.removePrefix("entity:").removePrefix("port:").split("."),
            resolvedIdentity = StableSemanticIdentity(identity),
            provenance = source,
        )

    private fun definitionReference(name: String, source: SourceProvenance): EngineeringDefinitionReference =
        EngineeringDefinitionReference(
            authoredName = listOf(name),
            resolvedId = null,
            provenance = source,
        )
}
