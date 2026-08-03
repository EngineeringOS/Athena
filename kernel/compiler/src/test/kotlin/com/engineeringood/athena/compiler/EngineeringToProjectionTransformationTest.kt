package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringReference
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
        assertEquals(listOf("component:Supply", "component:Q1", "port:Supply.L1", "port:Q1.1"), success.output.nodes.map {
            node -> node.semanticId.value
        })
        assertEquals(listOf("connection:Supply.L1-to-Q1.1"), success.output.connections.map { connection ->
            connection.semanticId.value
        })
        assertEquals(1, success.output.sheets.size)
        assertEquals(0, success.output.sheets.single().order)
        assertEquals("engineering-projection/sheet/01-main", success.output.sheets.single().sheetId.value)
    }

    @Test
    fun `engineering to projection keeps identity trace into sheet subjects`() {
        val result = EngineeringToProjectionTransformation().transform(engineeringDocument())
        val output = assertIs<RealityTransformationResult.Success<ProjectionDocument>>(result).output
        val subjects = output.sheets.single().subjects

        assertContains(subjects.map { subject -> subject.semanticId.value }, "component:Supply")
        assertContains(subjects.map { subject -> subject.semanticId.value }, "connection:Supply.L1-to-Q1.1")
        assertTrue(subjects.any { subject ->
            subject.semanticId.value == "component:Supply" &&
                subject.nodeIds.any { nodeId -> nodeId.value == "projection/node/component:Supply" }
        })
        assertTrue(subjects.any { subject ->
            subject.semanticId.value == "connection:Supply.L1-to-Q1.1" &&
                subject.connectionIds.any { connectionId ->
                    connectionId.value == "projection/connection/connection:Supply.L1-to-Q1.1"
                }
        })
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
        val supply = EngineeringComponent(
            id = StableSemanticIdentity("component:Supply"),
            name = "Supply",
            kind = "PowerSupply",
            properties = emptyList<EngineeringProperty>(),
            provenance = source,
        )
        val breaker = EngineeringComponent(
            id = StableSemanticIdentity("component:Q1"),
            name = "Q1",
            kind = "Breaker",
            properties = emptyList<EngineeringProperty>(),
            provenance = source,
        )
        val supplyPort = com.engineeringood.athena.ir.EngineeringPort(
            id = StableSemanticIdentity("port:Supply.L1"),
            ownerReference = reference("component:Supply", source),
            name = "L1",
            properties = emptyList(),
            provenance = source,
        )
        val breakerPort = com.engineeringood.athena.ir.EngineeringPort(
            id = StableSemanticIdentity("port:Q1.1"),
            ownerReference = reference("component:Q1", source),
            name = "1",
            properties = emptyList(),
            provenance = source,
        )

        return EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity(systemId),
                name = systemName,
                provenance = source,
            ),
            components = listOf(supply, breaker),
            ports = listOf(supplyPort, breakerPort),
            connections = listOf(
                EngineeringConnection(
                    id = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
                    from = reference("port:Supply.L1", source),
                    to = reference("port:Q1.1", source),
                    provenance = source,
                ),
            ),
        )
    }

    private fun reference(identity: String, source: SourceProvenance): EngineeringReference =
        EngineeringReference(
            authoredPath = identity.removePrefix("component:").removePrefix("port:").split("."),
            resolvedIdentity = StableSemanticIdentity(identity),
            provenance = source,
        )
}

