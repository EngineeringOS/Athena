package com.engineeringood.athena.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AthenaEngineeringGraphProjectionTest {
    @Test
    fun `runtime projects the demo cabinet into a queryable engineering graph`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val projection = context.projectEngineeringGraphProjection()

        val ready = assertIs<AthenaEngineeringGraphReadyProjection>(projection)
        assertEquals("demo-cabinet", ready.projectName)
        assertEquals("system:DemoCabinet", ready.graph.systemSemanticId)
        assertEquals(6, ready.graph.nodes.size)
        assertEquals(6, ready.graph.relationships.size)

        val connection = ready.graph.node("connection:PLC1.out->M1.in")
        assertEquals(AthenaEngineeringGraphNodeKind.CONNECTION, connection?.kind)
        assertEquals(
            listOf("port:PLC1.out", "port:M1.in"),
            ready.graph.referencedNodes("connection:PLC1.out->M1.in").map { it.semanticId },
        )
        assertEquals(
            listOf("component:M1", "component:PLC1"),
            ready.graph.dependenciesOf("system:DemoCabinet").map { it.semanticId }.sorted(),
        )
        assertEquals(
            listOf("component:PLC1", "connection:PLC1.out->M1.in"),
            ready.graph.neighbors("port:PLC1.out").map { it.semanticId }.sorted(),
        )
        assertEquals(
            listOf("component:PLC1", "connection:PLC1.out->M1.in"),
            ready.graph.affectedRelationships("port:PLC1.out").map { it.sourceSemanticId }.sorted(),
        )
        assertEquals(
            listOf("component:M1", "component:PLC1"),
            ready.graph.nodesOfKind(AthenaEngineeringGraphNodeKind.COMPONENT).map { it.semanticId }.sorted(),
        )
        assertEquals(
            listOf("connection:PLC1.out->M1.in->port:M1.in", "connection:PLC1.out->M1.in->port:PLC1.out"),
            ready.graph.relationshipsOfKind(AthenaEngineeringGraphRelationshipKind.CONNECTION_REFERENCE)
                .map { "${it.sourceSemanticId}->${it.targetSemanticId}" }
                .sorted(),
        )
    }

    @Test
    fun `runtime graph remains available when semantic validation blocks rendering`() {
        val brokenPath = Files.createTempFile("athena-runtime-graph-semantic-", ".athena")
        Files.writeString(
            brokenPath,
            """
                system Broken {
                  device PLC1 {
                    type PLC
                  }

                  port Missing.out {
                    direction out
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(brokenPath.parent).activateProject(
                projectName = "broken",
                sourcePath = brokenPath,
            )

            val projection = context.projectEngineeringGraphProjection()

            val ready = assertIs<AthenaEngineeringGraphReadyProjection>(projection)
            val portNode = ready.graph.node("port:Missing.out")
            assertEquals(AthenaEngineeringGraphNodeKind.PORT, portNode?.kind)
            assertEquals(1, portNode?.references?.size)
            assertEquals(listOf("Missing"), portNode?.references?.single()?.authoredPath)
            assertNull(portNode?.references?.single()?.resolvedSemanticId)
            assertTrue(ready.graph.referencedNodes("port:Missing.out").isEmpty())
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    @Test
    fun `runtime surfaces parse failures as unavailable engineering graph projections`() {
        val brokenPath = Files.createTempFile("athena-runtime-graph-parse-", ".athena")
        Files.writeString(
            brokenPath,
            """
                system Broken {
                  connect P1.out P2.in
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(brokenPath.parent).activateProject(
                projectName = "broken",
                sourcePath = brokenPath,
            )

            val projection = context.projectEngineeringGraphProjection()

            val unavailable = assertIs<AthenaEngineeringGraphUnavailableProjection>(projection)
            assertEquals("broken", unavailable.projectName)
            assertContains(unavailable.reason, "Expected")
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
