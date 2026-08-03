package com.engineeringood.athena.compiler

import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringConnectivityNetworkCompilationTest {
    @Test
    fun `lowers grouped connections into a semantic network and junction fact`() {
        val path = Files.createTempFile("athena-connectivity-network-", ".athena")
        path.writeText(
            """
            system EngineeringNetwork {
              device Source {
                type Switch
                connectivity enabled
                interface power_input
              }
              device BranchA {
                type Switch
                connectivity enabled
                interface power_input
              }
              device BranchB {
                type Switch
                connectivity enabled
                interface power_input
              }

              port Source.out {
                direction out
                signal power
                role line
              }
              port BranchA.in {
                direction in
                signal power
                role line
              }
              port BranchB.in {
                direction in
                signal power
                role line
              }

              connect supply_group {
                feed_in Source.out to BranchA.in
                relay_supply Source.out to BranchB.in
              }
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val network = result.document.connectionNetworks.single()
            assertEquals("supply_group", network.name)
            assertEquals(2, network.members.size)
            assertEquals(1, network.junctions.size)
            assertEquals("port:Source.out", network.junctions.single().sharedPortReference.resolvedIdentity?.value)
            assertTrue(result.semanticResult.diagnostics.isEmpty(), "diagnostics=${result.semanticResult.diagnostics}")
            assertTrue(result.semanticResult.diagnostics.none { it.ruleId.value.startsWith("connectivity.network.") })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `reports a network diagnostic when a grouped junction uses a one-way shared port`() {
        val path = Files.createTempFile("athena-connectivity-network-invalid-", ".athena")
        path.writeText(
            """
            system EngineeringNetworkInvalid {
              device Source {
                type Switch
                connectivity enabled
                interface power_input
              }
              device BranchA {
                type Switch
                connectivity enabled
                interface power_input
              }
              device BranchB {
                type Switch
                connectivity enabled
                interface power_input
              }

              port Source.out {
                direction out
                signal power
                role line
              }
              port BranchA.in {
                direction in
                signal power
                role line
              }
              port BranchB.in {
                direction in
                signal power
                role line
              }

              connect supply_group {
                feed_in Source.out to BranchA.in
                relay_supply BranchB.in to Source.out
              }
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            assertContains(
                result.semanticResult.diagnostics.map { it.ruleId.value },
                "connectivity.network.junction.incompatible",
                message = "diagnostics=${result.semanticResult.diagnostics}",
            )
        } finally {
            path.deleteIfExists()
        }
    }
}
