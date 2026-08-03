package com.engineeringood.athena.compiler

import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringConnectivityCompilationTest {
    @Test
    fun `compiles grouped connectivity interfaces with defaults and member overrides`() {
        val path = Files.createTempFile("athena-grouped-interface-", ".athena")
        path.writeText(
            """
            system EngineeringConnectivity {
              device Drive {
                type MotorDrive
                connectivity enabled

                interface powerInput {
                  type power
                  direction in
                  signal PowerAC
                  role line
                  multiplicity single
                  owner physical
                  strength preferred

                  ports {
                    L1
                    L2
                    PE {
                      signal ProtectiveEarth
                      role protective_earth
                      direction passive
                      strength required
                    }
                  }
                }
              }
            }
            """.trimIndent(),
        )

        try {
            val compiler = AthenaCompiler()
            val success = assertIs<CompilerCompilationSuccess>(compiler.compile(path))
            val connectivity = assertIs<com.engineeringood.athena.connection.EngineeringConnectivityCompilation.Success>(
                compiler.compileEngineeringConnectivity(success.document),
            )
            val contract = connectivity.contracts.single()
            assertEquals(listOf("powerInput"), contract.interfaces.map { it.id.value })
            assertEquals(listOf("L1", "L2", "PE"), contract.ports.map { it.name })
            assertEquals(listOf("powerInput"), contract.ports[0].interfaceIds.map { it.value })
            assertEquals("PowerAC", contract.ports[0].compatibility.signalKind)
            assertEquals(com.engineeringood.athena.connection.EngineeringConnectivityConstraintOwner.PHYSICAL, contract.ports[0].compatibility.owner)
            assertEquals(com.engineeringood.athena.connection.EngineeringConnectivityConstraintStrength.PREFERRED, contract.ports[0].compatibility.strength)
            assertEquals("ProtectiveEarth", contract.ports[2].compatibility.signalKind)
            assertEquals("protective_earth", contract.ports[2].compatibility.role)
            assertEquals(com.engineeringood.athena.connection.EngineeringConnectivityConstraintStrength.REQUIRED, contract.ports[2].compatibility.strength)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `reports a typed source diagnostic for an admitted connectivity port without direction`() {
        val path = Files.createTempFile("athena-connectivity-", ".athena")
        path.writeText(
            """
            system EngineeringConnectivity {
              device Drive {
                type Switch
                connectivity enabled
                interface power_input
              }

              port Drive.L1 {
                signal Digital
                role line
              }
            }
            """.trimIndent(),
        )

        try {
            val result = AthenaCompiler().compile(path)

            val success = assertIs<CompilerCompilationSuccess>(result)
            assertContains(
                success.semanticResult.diagnostics.map { it.ruleId.value },
                "connectivity.port.direction.missing",
                message = "components=${success.document.components}; diagnostics=${success.semanticResult.diagnostics}",
            )
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `reports grouped interface validation diagnostics from source spans`() {
        val path = Files.createTempFile("athena-grouped-interface-invalid-", ".athena")
        path.writeText(
            """
            system EngineeringConnectivity {
              device Drive {
                type MotorDrive
                connectivity enabled

                interface powerInput {
                  direction sideways
                  signal PowerAC
                  strength required
                  ports {
                    L1
                    L1 {
                      signal Digital
                    }
                  }
                }

                interface powerInput {
                  direction in
                  ports { PE }
                }
              }
            }
            """.trimIndent(),
        )

        try {
            val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val diagnosticCodes = success.semanticResult.diagnostics.map { it.ruleId.value }

            assertContains(diagnosticCodes, "connectivity.interface.duplicate")
            assertContains(diagnosticCodes, "connectivity.port.duplicate")
            assertContains(diagnosticCodes, "connectivity.interface.direction.invalid")
            assertContains(diagnosticCodes, "connectivity.port.default.conflict")
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `compiles natural relation source without authored connection intent`() {
        val path = Files.createTempFile("athena-relation-source-", ".athena")
        path.writeText(
            """
            system EngineeringConnectivityRelation {
              device Supply {
                type Switch
                connectivity enabled
                port L1 { direction out signal PowerAC role line }
              }
              device Drive {
                type MotorDrive
                connectivity enabled
                interface powerInput {
                  ports {
                    L1 { direction in signal PowerAC role line }
                    L2 { direction in signal PowerAC role line }
                  }
                }
              }

              power Supply.L1 to [Drive.L1, Drive.L2]
            }
            """.trimIndent(),
        )

        try {
            val compiler = AthenaCompiler()
            val success = assertIs<CompilerCompilationSuccess>(compiler.compile(path))
            val connectivity = assertIs<com.engineeringood.athena.connection.EngineeringConnectivityCompilation.Success>(
                compiler.compileEngineeringConnectivity(success.document),
            )
            assertEquals(
                listOf("power_Supply_L1_to_Drive_L1", "power_Supply_L1_to_Drive_L2"),
                connectivity.connections.map { it.id.value.substringAfterLast(':') },
            )
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `compiles typed external evidence as non authoritative connectivity evidence`() {
        val path = Files.createTempFile("athena-external-evidence-", ".athena")
        path.writeText(
            """
            system EngineeringEvidence {
              device Supply {
                type Switch
                connectivity enabled
                port L1 { direction out signal PowerAC role line }
              }
              device Drive {
                type MotorDrive
                connectivity enabled
                interface powerInput {
                  ports {
                    L1 { direction in signal PowerAC role line }
                  }
                }
              }

              power Supply.L1 to Drive.L1

              evidence DriveContractIec {
                namespace iec
                reference "IEC:60204-1:clause-13"
                subject contract Drive
                provenance "IEC clause citation"
              }
              evidence DriveInterfaceClass {
                namespace classification
                reference "neutral:drive.power-input"
                subject interface Drive.powerInput
                provenance "Neutral classification"
              }
              evidence DrivePortIec {
                namespace iec
                reference "IEC:60204-1:protective-conductor"
                subject port Drive.L1
                provenance "IEC terminal citation"
              }
              evidence DriveRelationIec {
                namespace iec
                reference "IEC:60204-1:routing"
                subject relation-contract power_Supply_L1_to_Drive_L1
                provenance "IEC relation contract citation"
              }
            }
            """.trimIndent(),
        )

        try {
            val compiler = AthenaCompiler()
            val success = assertIs<CompilerCompilationSuccess>(compiler.compile(path))
            val connectivity = assertIs<com.engineeringood.athena.connection.EngineeringConnectivityCompilation.Success>(
                compiler.compileEngineeringConnectivity(success.document),
            )

            val evidence = connectivity.externalEvidence
            assertEquals(4, evidence.size)
            assertEquals(
                setOf("CONTRACT", "INTERFACE", "PORT", "RELATION_CONTRACT"),
                evidence.map { it.subject.kind.name }.toSet(),
            )
            assertEquals("iec", evidence.first().namespace.sourceName)
            assertEquals("IEC:60204-1:clause-13", evidence.first().reference.value)
            assertEquals("IEC clause citation", evidence.first().externalProvenance)
            assertEquals(path.toString(), evidence.first().provenance.file)
            assertEquals(listOf("Drive"), evidence.first().subject.authoredPath)
            assertEquals("component:Drive", evidence.first().subject.targetId?.value)
            assertEquals(listOf("Supply", "Drive"), connectivity.contracts.map { it.name })
            assertEquals(listOf("power_Supply_L1_to_Drive_L1"), connectivity.connections.map { it.id.value.substringAfterLast(':') })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `reports typed external evidence diagnostics from source spans`() {
        val path = Files.createTempFile("athena-external-evidence-invalid-", ".athena")
        path.writeText(
            """
            system EngineeringEvidenceInvalid {
              device Drive {
                type MotorDrive
                connectivity enabled
                port L1 { direction in signal PowerAC role line }
              }

              evidence UnknownNamespace {
                namespace aml
                reference "AML:drive"
                subject contract Drive
                provenance "Bad namespace"
              }
              evidence BadReference {
                namespace iec
                reference "not-iec"
                subject port Drive.L1
                provenance "Bad reference"
              }
              evidence DuplicateA {
                namespace classification
                reference "neutral:drive"
                subject contract Drive
                provenance "Duplicate one"
              }
              evidence DuplicateB {
                namespace classification
                reference "neutral:drive"
                subject contract Drive
                provenance "Duplicate two"
              }
              evidence MissingSubject {
                namespace iec
                reference "IEC:60204-1:missing"
                subject port Drive.Missing
                provenance "Missing subject"
              }
            }
            """.trimIndent(),
        )

        try {
            val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val diagnostics = success.semanticResult.diagnostics
            val codes = diagnostics.map { it.ruleId.value }.toSet()
            assertContains(codes, "connectivity.evidence.namespace.unknown")
            assertContains(codes, "connectivity.evidence.reference.invalid")
            assertContains(codes, "connectivity.evidence.duplicate")
            assertContains(codes, "connectivity.evidence.subject.invalid")
            val evidenceDiagnostics = diagnostics.filter { it.ruleId.value.startsWith("connectivity.evidence.") }
            assertTrue(evidenceDiagnostics.all { it.provenance.file == path.toString() })
            assertTrue(evidenceDiagnostics.all { it.provenance.startLine > 0 && it.provenance.startColumn > 0 })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `compiles typed projection policy selection as non authoritative compiler input`() {
        val path = Files.createTempFile("athena-projection-policy-", ".athena")
        path.writeText(
            """
            system ProjectionPolicyValid {
              device Drive {
                type MotorDrive
                connectivity enabled
                port L1 { direction in signal PowerAC role line }
              }

              projection ControlDrawingProjection {
                target professional-connection-drawing
                layout orthogonal-grid
                drawingProfile ControlDrawingIEC
                routeQuality ControlDrawingRouteQuality
                proof exact-endpoints
                proof source-trace
              }
            }
            """.trimIndent(),
        )

        try {
            val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val policy = success.document.projectionPolicies.single()
            assertEquals("ControlDrawingProjection", policy.name)
            assertEquals("professional-connection-drawing", policy.targetSurface)
            assertEquals("orthogonal-grid", policy.layoutStrategy)
            assertEquals("ControlDrawingIEC", policy.drawingProfile)
            assertEquals("ControlDrawingRouteQuality", policy.routeQualityPolicy)
            assertEquals(listOf("exact-endpoints", "source-trace"), policy.proofObligations)
            assertTrue(policy.forbiddenEngineeringTruth.isEmpty())
            assertTrue(success.semanticResult.diagnostics.none { it.ruleId.value.startsWith("projection.policy.") })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `reports typed projection policy diagnostics from source spans`() {
        val path = Files.createTempFile("athena-projection-policy-invalid-", ".athena")
        path.writeText(
            """
            system ProjectionPolicyInvalid {
              device Drive {
                type MotorDrive
                connectivity enabled
                port L1 { direction in signal PowerAC role line }
              }

              projection BrokenProjection {
                target unknown-target
                layout unknown-layout
                drawingProfile ControlDrawingIEC
                port Drive.L1 input
              }

              projection BrokenProjection {
                target cabinet
                layout orthogonal-grid
                drawingProfile CabinetDrawing
                routeQuality CabinetRouteQuality
              }
            }
            """.trimIndent(),
        )

        try {
            val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val diagnostics = success.semanticResult.diagnostics
            val codes = diagnostics.map { it.ruleId.value }.toSet()
            assertContains(codes, "projection.policy.target.unknown")
            assertContains(codes, "projection.policy.layout.unknown")
            assertContains(codes, "projection.policy.route-quality.missing")
            assertContains(codes, "projection.policy.duplicate")
            assertContains(codes, "projection.policy.engineering-truth.forbidden")
            val policyDiagnostics = diagnostics.filter { it.ruleId.value.startsWith("projection.policy.") }
            assertTrue(policyDiagnostics.all { it.provenance.file == path.toString() })
            assertTrue(policyDiagnostics.all { it.provenance.startLine > 0 && it.provenance.startColumn > 0 })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `compares projection policies without duplicating semantic truth`() {
        val path = Files.createTempFile("athena-projection-neutral-", ".athena")
        path.writeText(
            """
            system ProjectionNeutrality {
              device Supply {
                type Switch
                connectivity enabled
                port L1 { direction out signal PowerAC role line }
              }
              device Drive {
                type MotorDrive
                connectivity enabled
                interface powerInput {
                  ports {
                    L1 { direction in signal PowerAC role line }
                  }
                }
              }

              power Supply.L1 to Drive.L1

              evidence DriveIec {
                namespace iec
                reference "IEC:60204-1:clause-13"
                subject contract Drive
                provenance "IEC clause citation"
              }

              projection ControlDrawingProjection {
                target professional-connection-drawing
                layout orthogonal-grid
                drawingProfile ControlDrawingIEC
                routeQuality ControlDrawingRouteQuality
                proof semantic-neutrality
                proof source-trace
              }

              projection CabinetProjection {
                target cabinet
                layout cabinet-layout
                drawingProfile ControlDrawingIEC
                routeQuality ControlDrawingRouteQuality
                proof semantic-neutrality
              }
            }
            """.trimIndent(),
        )

        try {
            val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val comparison = assertIs<ProjectionSemanticComparisonCompilation.Success>(
                ProjectionSemanticComparisonCompiler().compile(success.document),
            ).comparison

            assertEquals(setOf("CabinetProjection", "ControlDrawingProjection"), comparison.policySnapshots.map { it.policyName }.toSet())
            assertEquals(setOf("component:Drive", "component:Supply"), comparison.sharedSemantic.contractIds.toSet())
            assertEquals(setOf("Drive.powerInput"), comparison.sharedSemantic.interfaceIds.toSet())
            assertEquals(setOf("port:Drive.L1", "port:Supply.L1"), comparison.sharedSemantic.portIds.toSet())
            assertEquals(setOf("power_Supply_L1_to_Drive_L1"), comparison.sharedSemantic.connectionIds.map { it.substringAfterLast(':') }.toSet())
            assertEquals(setOf("DriveIec"), comparison.sharedSemantic.externalEvidenceIds.toSet())
            assertTrue(comparison.policySnapshots.all { snapshot -> snapshot.semanticDigest == comparison.sharedSemantic.digest })
            assertEquals(setOf("schematic", "cabinet"), comparison.policySnapshots.map { it.materialProjectionContext }.toSet())
            assertTrue(comparison.policySnapshots.all { snapshot -> snapshot.authorityPayloadsAbsent })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `rejects projection semantic comparison when policy owns engineering truth`() {
        val path = Files.createTempFile("athena-projection-neutral-invalid-", ".athena")
        path.writeText(
            """
            system ProjectionNeutralityInvalid {
              device Drive {
                type MotorDrive
                connectivity enabled
                port L1 { direction in signal PowerAC role line }
              }

              projection CabinetProjection {
                target cabinet
                layout cabinet-layout
                drawingProfile ControlDrawingIEC
                routeQuality ControlDrawingRouteQuality
                port Drive.L1 input
              }
            }
            """.trimIndent(),
        )

        try {
            val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val diagnostics = assertIs<ProjectionSemanticComparisonCompilation.Failure>(
                ProjectionSemanticComparisonCompiler().compile(success.document),
            ).diagnostics

            assertContains(diagnostics.map { it.code }, "projection.policy.engineering-truth.forbidden")
            assertTrue(diagnostics.all { it.provenance.file == path.toString() })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `reports both typed port spans for an incompatible admitted connection`() {
        val path = Files.createTempFile("athena-engineering-connection-", ".athena")
        path.writeText(
            """
            system EngineeringConnection {
              device Source { type Switch connectivity enabled }
              device Target { type Switch connectivity enabled }
              port Source.out { direction in signal Digital role control }
              port Target.in { direction in signal Digital role control }
              connect invalid Source.out to Target.in
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val diagnostics = result.semanticResult.diagnostics.filter {
                it.ruleId.value == "connectivity.connection.direction.incompatible"
            }
            assertContains(diagnostics.map { it.category.name }, "CONNECTION")
            assertContains(diagnostics.map { it.provenance.startLine }, 4)
            assertContains(diagnostics.map { it.provenance.startLine }, 5)
        } finally {
            path.deleteIfExists()
        }
    }
}
