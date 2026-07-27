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

              connect feed_in MainPowerSupplyPS30.lplus -> MainBreakerQF30.line
              connect relay_supply MainBreakerQF30.load -> ControlRelayK30.supply
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
                feed_in MainPowerSupplyPS30.lplus -> MainBreakerQF30.line
                relay_supply MainBreakerQF30.load -> ControlRelayK30.supply
              }
            }
            """.trimIndent()

        val renamedGroupSource = groupedSource.replace("connect con_01", "connect customer_readability_group")
        val directory = Files.createTempDirectory("athena-grouped-connect-")
        val fileName = "connections.athena"
        val flatConnections = lowerConnections(directory, fileName, flatSource)
        val groupedConnections = lowerConnections(directory, fileName, groupedSource)
        val renamedGroupConnections = lowerConnections(directory, fileName, renamedGroupSource)

        assertEquals(flatConnections, groupedConnections)
        assertEquals(groupedConnections, renamedGroupConnections)
        assertEquals(
            listOf(
                ConnectionProof(
                    id = "connection:${directory.resolve(fileName)}:feed_in",
                    sourceId = "port:MainPowerSupplyPS30.lplus",
                    targetId = "port:MainBreakerQF30.line",
                ),
                ConnectionProof(
                    id = "connection:${directory.resolve(fileName)}:relay_supply",
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

    private fun lowerConnections(directory: java.nio.file.Path, fileName: String, source: String): List<ConnectionProof> {
        val path = directory.resolve(fileName)
        path.writeText(source)
        val result = assertIs<CompilerLoweringSuccess>(AthenaCompiler().lower(path))
        return result.document.connections.map { connection ->
            ConnectionProof(
                id = connection.id.value,
                sourceId = checkNotNull(connection.from.resolvedIdentity).value,
                targetId = checkNotNull(connection.to.resolvedIdentity).value,
            )
        }
    }

    private data class ConnectionProof(
        val id: String,
        val sourceId: String,
        val targetId: String,
    )
}
