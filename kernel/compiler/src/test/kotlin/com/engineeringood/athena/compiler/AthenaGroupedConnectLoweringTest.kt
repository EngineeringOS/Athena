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

        val flatConnections = lowerConnections("flat-connect.athena", flatSource)
        val groupedConnections = lowerConnections("grouped-connect.athena", groupedSource)

        assertEquals(flatConnections, groupedConnections)
        assertEquals(
            listOf(
                "connection:MainPowerSupplyPS30.lplus->MainBreakerQF30.line",
                "connection:MainBreakerQF30.load->ControlRelayK30.supply",
            ),
            groupedConnections,
        )
    }

    private fun lowerConnections(fileName: String, source: String): List<String> {
        val directory = Files.createTempDirectory("athena-grouped-connect-")
        val path = directory.resolve(fileName)
        path.writeText(source)
        return try {
            val result = assertIs<CompilerLoweringSuccess>(AthenaCompiler().lower(path))
            result.document.connections.map { connection -> connection.id.value }
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
