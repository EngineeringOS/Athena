package com.engineeringood.athena.compiler

import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AthenaGroupedConnectLoweringTest {
    @Test
    fun `grouped connect syntax lowers to the same flat canonical connections as single-line syntax`() {
        val flatSource =
            """
            system Demo {
              device MainPowerSupplyPS30 {
                type Switch
              }
              device MainBreakerQF30 {
                type Switch
              }
              device ControlRelayK30 {
                type Switch
              }

              port MainPowerSupplyPS30.lplus {
                direction out
                signal Digital
              }
              port MainBreakerQF30.line {
                direction in
                signal Digital
              }
              port MainBreakerQF30.load {
                direction out
                signal Digital
              }
              port ControlRelayK30.supply {
                direction in
                signal Digital
              }

              connect MainPowerSupplyPS30.lplus -> MainBreakerQF30.line
              connect MainBreakerQF30.load -> ControlRelayK30.supply
            }
            """.trimIndent()
        val groupedSource =
            """
            system Demo {
              device MainPowerSupplyPS30 {
                type Switch
              }
              device MainBreakerQF30 {
                type Switch
              }
              device ControlRelayK30 {
                type Switch
              }

              port MainPowerSupplyPS30.lplus {
                direction out
                signal Digital
              }
              port MainBreakerQF30.line {
                direction in
                signal Digital
              }
              port MainBreakerQF30.load {
                direction out
                signal Digital
              }
              port ControlRelayK30.supply {
                direction in
                signal Digital
              }

              connect con_01 {
                MainPowerSupplyPS30.lplus -> MainBreakerQF30.line
                MainBreakerQF30.load -> ControlRelayK30.supply
              }
            }
            """.trimIndent()

        val renamedGroupSource = groupedSource.replace("connect con_01", "connect customer_readability_group")
        val flatConnections = lowerConnections("flat-connect.athena", flatSource)
        val groupedConnections = lowerConnections("grouped-connect.athena", groupedSource)
        val renamedGroupConnections = lowerConnections("renamed-group-connect.athena", renamedGroupSource)

        assertEquals(flatConnections, groupedConnections)
        assertEquals(groupedConnections, renamedGroupConnections)
        assertEquals(
            listOf(
                ConnectionProof(
                    id = "connection:MainPowerSupplyPS30.lplus->MainBreakerQF30.line",
                    sourceId = "port:MainPowerSupplyPS30.lplus",
                    targetId = "port:MainBreakerQF30.line",
                ),
                ConnectionProof(
                    id = "connection:MainBreakerQF30.load->ControlRelayK30.supply",
                    sourceId = "port:MainBreakerQF30.load",
                    targetId = "port:ControlRelayK30.supply",
                ),
            ),
            groupedConnections,
        )
        groupedConnections.forEach { connection ->
            kotlin.test.assertFalse(connection.id.contains("con_01"))
            kotlin.test.assertFalse(connection.id.contains("customer_readability_group"))
        }
    }

    private fun lowerConnections(fileName: String, source: String): List<ConnectionProof> {
        val directory = Files.createTempDirectory("athena-grouped-connect-")
        val path = directory.resolve(fileName)
        path.writeText(source)
        return try {
            val result = assertIs<CompilerLoweringSuccess>(AthenaCompiler().lower(path))
            result.document.connections.map { connection ->
                ConnectionProof(
                    id = connection.id.value,
                    sourceId = checkNotNull(connection.from.resolvedIdentity).value,
                    targetId = checkNotNull(connection.to.resolvedIdentity).value,
                )
            }
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    private data class ConnectionProof(
        val id: String,
        val sourceId: String,
        val targetId: String,
    )
}
