package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.TopologyOperatorKind
import com.engineeringood.athena.ir.ConnectionKind
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class M46RollingShutterConnectionProjectTest {
    @Test
    fun `M46 package symbols use normalized IEC SVG viewports without source-tool guides`() {
        val repository = repositoryRoot().resolve("examples/m46/rolling-shutter")
        val assets = listOf(
            repository.resolve("packages/com.community.elements/resources/plc.svg"),
            repository.resolve("packages/com.athena.iec/resources/motor.svg"),
        )

        assets.forEach { asset ->
            val svg = Files.readString(asset)
            assertTrue(svg.contains("viewBox=\"0 0 64 48\""), "$asset must declare the normalized 64x48 viewport.")
            assertTrue(!svg.contains("#0000FF") && !svg.contains("#FF0000") && !svg.contains("#00FF00"), "$asset must not retain source-tool terminal guides.")
        }
    }

    @Test
    fun `independent rolling shutter project compiles all semantic connection kinds`() {
        val repository = repositoryRoot().resolve("examples/m46/rolling-shutter")
        assertTrue(Files.isDirectory(repository), "M46 rolling-shutter repository must exist independently.")
        assertTrue(
            Files.walk(repository).use { paths -> paths.noneMatch { it.normalize().startsWith(repositoryRoot().resolve("examples/m45")) } },
            "M46 repository must not read a prior milestone example.",
        )

        val compiler = AthenaCompiler()
        val lock = compiler.materializeRepositoryLock(repository)
        assertTrue(lock.isValid, lock.diagnostics.joinToString("\n") { it.message })
        assertEquals("athena-lock-v3", assertNotNull(lock.lock).schema)

        val source = repository.resolve(
            "src/com/engineeringood/m46/rollingshutter/rolling-shutter.athena",
        )
        val first = assertIs<CompilerCompilationSuccess>(compiler.compile(source))
        val second = assertIs<CompilerCompilationSuccess>(compiler.compile(source))
        assertTrue(first.semanticResult.diagnostics.isEmpty(), first.semanticResult.diagnostics.joinToString("\n") { it.message })
        assertEquals(
            ConnectionKind.entries.toSet(),
            (first.document.connections.map { it.kind } + first.document.nets.map { it.kind }).toSet(),
        )
        assertEquals(2, first.document.nets.size)
        val controlNet = first.document.nets.single { it.name == "Control24V" }
        assertEquals(3, controlNet.endpoints.size)
        assertEquals(controlNet.id, second.document.nets.single { it.name == "Control24V" }.id)

        val connectionIr = assertNotNull(first.connectionIr, first.connectionIrDiagnostics.joinToString("\n"))
        assertEquals("athena-connection-ir-c14n-v1", connectionIr.canonicalization)
        assertEquals(connectionIr.digest, assertNotNull(second.connectionIr).digest)
        assertTrue(connectionIr.topologyOperators.any { it.kind == TopologyOperatorKind.BRANCH })
        assertTrue(connectionIr.topologyOperators.any { it.kind == TopologyOperatorKind.INTERRUPTION })
        assertTrue(first.projections.isNotEmpty(), first.projectionDiagnostics.joinToString("\n"))
        val sparePort = "port:X2.powerTerminal.spare"
        assertTrue(
            first.projections.flatMap { projection -> projection.occurrencePorts }
                .any { port -> port.occurrencePortId.portId.value == sparePort },
            "An unconnected package-backed Port must remain a projected semantic Port.",
        )
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}
