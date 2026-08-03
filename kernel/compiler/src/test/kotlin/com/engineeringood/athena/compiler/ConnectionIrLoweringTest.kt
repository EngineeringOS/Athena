package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.EngineeringConnectivityCompilation
import com.engineeringood.athena.connection.EngineeringConnectivityContract
import com.engineeringood.athena.connection.EngineeringConnectivityEvidenceReference
import com.engineeringood.athena.connection.EngineeringConnectivityEvidenceNamespace
import com.engineeringood.athena.connection.EngineeringConnectivityEvidenceSubject
import com.engineeringood.athena.connection.EngineeringConnectivityEvidenceSubjectKind
import com.engineeringood.athena.connection.EngineeringConnectivityEvidenceValue
import com.engineeringood.athena.connection.EngineeringConnectivityPhysicalReference
import com.engineeringood.athena.connection.EngineeringConnectivityPortDirection
import com.engineeringood.athena.connection.EngineeringConnectivityPortMultiplicity
import com.engineeringood.athena.connection.EngineeringConnectivityRepresentationReference
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectionIrLoweringTest {
    @Test
    fun `lowers only validated connectivity facts and preserves typed references`() {
        val provenance = SourceProvenance("connectivity.athena", 1, 1, 1, 20)
        val contract = EngineeringConnectivityContract(
            id = StableSemanticIdentity("component:drive"),
            name = "drive",
            kind = "Drive",
            interfaces = emptyList(),
            ports = emptyList(),
            provenance = provenance,
            physicalInstallationReferences = listOf(
                EngineeringConnectivityPhysicalReference(
                    authoredPath = listOf("cabinet", "rail-1"),
                    targetId = StableSemanticIdentity("physical:rail-1"),
                    provenance = provenance,
                ),
            ),
            representationBindings = listOf(
                EngineeringConnectivityRepresentationReference(
                    authoredPath = listOf("representation", "drive"),
                    targetId = StableSemanticIdentity("representation:drive"),
                    provenance = provenance,
                ),
            ),
            externalEvidenceReferences = listOf(
                EngineeringConnectivityEvidenceReference(
                    namespace = EngineeringConnectivityEvidenceNamespace.IEC,
                    reference = EngineeringConnectivityEvidenceValue("IEC:60204-1:drive"),
                    subject = EngineeringConnectivityEvidenceSubject(
                        kind = EngineeringConnectivityEvidenceSubjectKind.CONTRACT,
                        authoredPath = listOf("evidence", "iec-drive"),
                        targetId = StableSemanticIdentity("evidence:iec-drive"),
                    ),
                    externalProvenance = "IEC citation",
                    provenance = provenance,
                ),
            ),
        )

        val connectionIr = ConnectionIrLowerer().lower(
            connectivity = EngineeringConnectivityCompilation.Success(
                contracts = listOf(contract),
                provenance = provenance,
            ),
            snapshot = ConnectionIrSnapshot("semantic:test", "package:test", "athena.compiler.connection-ir"),
        )

        val entity = connectionIr.entities.single()
        assertEquals("physical:rail-1", entity.physicalInstallationReferences.single().resolvedIdentity.value)
        assertEquals("representation:drive", entity.representationBindings.single().resolvedIdentity.value)
        assertEquals("evidence:iec-drive", entity.externalEvidenceReferences.single().resolvedIdentity.value)
    }

    @Test
    fun `lowers validated connectivity into transient connection ir with explicit owner and strength`() {
        val path = java.nio.file.Files.createTempFile("athena-connection-ir-", ".athena")
        path.writeText(sampleSource())

        try {
            val result = assertIs<CompilerCompilationSuccess>(compiler().compile(path))
            val connectionIr = assertNotNull(result.connectionIr)

            assertEquals(3, connectionIr.ports.size)
            assertEquals(3, connectionIr.entities.size)
            assertEquals(2, connectionIr.connections.size)
            assertEquals(1, connectionIr.networks.size)
            assertEquals(path.toString(), connectionIr.provenance.file)
            assertTrue(connectionIr.snapshot.semanticSnapshotId.isNotBlank())
            assertTrue(connectionIr.snapshot.packageSnapshotId.isNotBlank())
            assertEquals("athena.compiler.connection-ir", connectionIr.snapshot.compilerIdentity)
            assertTrue(connectionIr.ports.all { port -> connectionIr.entities.any { it.id == port.ownerId } })

            val sourcePort = connectionIr.ports.single { it.name == "out" }
            assertEquals(EngineeringConnectivityPortDirection.OUTPUT, sourcePort.compatibility.direction)
            assertEquals(EngineeringConnectivityPortMultiplicity.SINGLE, sourcePort.compatibility.multiplicity)
            assertEquals(emptyMap(), sourcePort.compatibility.parameters)
            assertEquals(listOf("power_input"), sourcePort.interfaceIds.map { it.value })
            assertEquals(ConnectionIrConstraintOwner.SEMANTIC, sourcePort.compatibility.owner)
            assertEquals(ConnectionIrConstraintStrength.REQUIRED, sourcePort.compatibility.strength)

            val network = connectionIr.networks.single()
            assertEquals("supply_group", network.name)
            assertEquals(listOf("member-count", "shared-direction", "shared-signal", "shared-role"), network.compatibilityEvidence.map { it.kind })
            assertTrue(network.compatibilityEvidence.all { it.owner == ConnectionIrConstraintOwner.SEMANTIC })
            assertTrue(network.compatibilityEvidence.all { it.strength == ConnectionIrConstraintStrength.REQUIRED })
            assertEquals("port:Source.out", network.junctions.single().sharedPortReference.resolvedIdentity.value)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `lowered connection ir is deterministic for the same source revision`() {
        val path = java.nio.file.Files.createTempFile("athena-connection-ir-deterministic-", ".athena")
        path.writeText(sampleSource())

        try {
            val first = assertNotNull(assertIs<CompilerCompilationSuccess>(compiler().compile(path)).connectionIr)
            val second = assertNotNull(assertIs<CompilerCompilationSuccess>(compiler().compile(path)).connectionIr)

            assertEquals(first, second)
            assertTrue(first.networks.isNotEmpty())
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `invalid connectivity does not lower into connection ir`() {
        val path = java.nio.file.Files.createTempFile("athena-invalid-connection-ir-", ".athena")
        path.writeText(sampleSource().replace("Source.out to Branch.in", "Source.missing to Branch.in"))

        try {
            val result = assertIs<CompilerCompilationSuccess>(compiler().compile(path))
            assertTrue(result.validationBreakdown.connectivityDiagnostics.isNotEmpty())
            assertNull(result.connectionIr)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `removed connectable marker cannot lower orphan connection topology`() {
        val path = java.nio.file.Files.createTempFile("athena-removed-connectable-marker-", ".athena")
        path.writeText(
            """
            system RemovedMarker {
              device Source { type Switch connectable enabled }
              device Target { type Switch connectable enabled }
              port Source.out { direction out signal power role line }
              port Target.in { direction in signal power role line }
              connect legacy Source.out to Target.in
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(compiler().compile(path))

            assertNull(result.connectionIr)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `discovers every shared port subset as a semantic junction`() {
        val path = java.nio.file.Files.createTempFile("athena-multi-junction-ir-", ".athena")
        path.writeText(multiJunctionSource())

        try {
            val parsed = assertIs<CompilerParseSuccess>(compiler().parse(path))
            val lowerer = EngineeringIrLowerer(
                AthenaDomainSemanticsCoordinator(listOf(GenericLoweringOnlyTestPlugin())),
            )
            val document = lowerer.lower(parsed.source)
            val contracts = assertIs<com.engineeringood.athena.connection.EngineeringConnectivityCompilation.Success>(
                compiler().compileEngineeringConnectivity(document),
            )
            val connectionIr = lowerer.lowerConnectionIr(
                contracts,
                ConnectionIrSnapshot("semantic:test", "package:test", "athena.compiler.connection-ir"),
            )
            assertEquals(
                listOf("port:Junctions.first", "port:Junctions.second"),
                connectionIr.networks.single().junctions.map { it.sharedPortReference.resolvedIdentity.value },
            )
            assertTrue(connectionIr.networks.single().junctions.all { it.memberConnectionReferences.size == 2 })
        } finally {
            path.deleteIfExists()
        }
    }

    private fun sampleSource(): String = """
        system EngineeringConnectionIr {
          device Source {
            type Switch
            connectivity enabled
            interface power_input
          }
          device Branch {
            type Switch
            connectivity enabled
            interface power_input
          }
          device Sink {
            type Switch
            connectivity enabled
            interface power_input
          }

          port Source.out {
            direction out
            signal power
            role line
            interface power_input
            multiplicity single
          }
          port Branch.in {
            direction in
            signal power
            role line
            interface power_input
            multiplicity single
          }
          port Sink.in {
            direction in
            signal power
            role line
            interface power_input
            multiplicity single
          }

          connect supply_group {
            feed_in Source.out to Branch.in
            relay_supply Source.out to Sink.in
          }
        }
    """.trimIndent()

    private fun multiJunctionSource(): String = """
        system MultiJunction {
          device Source {
            type Switch
            connectivity enabled
            interface power_input
          }
          device Junctions {
            type Terminal
            connectivity enabled
            interface power_input
          }
          device Sink {
            type Lamp
            connectivity enabled
            interface power_input
          }

          port Source.out {
            direction out
            signal power
            role line
          }
          port Junctions.first {
            direction passive
            signal power
            role line
          }
          port Junctions.second {
            direction passive
            signal power
            role line
          }
          port Sink.in {
            direction in
            signal power
            role line
          }

          connect chain {
            incoming Source.out to Junctions.first
            bridge Junctions.first to Junctions.second
            outgoing Junctions.second to Sink.in
          }
        }
    """.trimIndent()

    private fun compiler(): AthenaCompiler = AthenaCompiler(
        hostedDomainPlugins = listOf(GenericLoweringOnlyTestPlugin()),
    )
}
