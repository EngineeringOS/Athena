package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Story 4.3 continuity contract: pins compiler output shape, identity scheme, and pipeline structure
 * across the whole currently-supported valid `examples/m0` corpus, extended to cross-check the same
 * shape against the checked-in `examples/m17` parser-parity corpus (Story 5.1).
 *
 * These assertions are parser-implementation-neutral (AD-106 / AD-110): they must keep passing
 * byte-for-shape unchanged once Epic 2 replaces the handwritten parser with an ANTLR4-backed compiler
 * path, because `EngineeringIrLowerer` reads only the authored `SourceFileAst`, never generated
 * parse-tree/CST types. They reuse the same identity-naming expectations that Story 5.1's
 * `AthenaM17ParserParityProofTest` pins for the M17 corpus, so both stories reinforce one shared
 * parity definition.
 */
class AthenaParserContinuityTest {
    private data class ValidFixture(
        val relativePath: String,
        val components: Int,
        val ports: Int,
        val connections: Int,
    )

    private val validCorpus = listOf(
        ValidFixture("examples/m0/demo-cabinet.athena", components = 2, ports = 2, connections = 1),
        ValidFixture("examples/m0/dual-drive-cabinet.athena", components = 3, ports = 4, connections = 2),
        // M17 parity corpus (Story 5.1): same shape and identity scheme as the M0 baseline above,
        // proving continuity holds identically for both milestones' fixtures.
        ValidFixture("examples/m17/parser-parity-proof/parity-cabinet.athena", components = 2, ports = 2, connections = 1),
        ValidFixture("examples/m17/parser-parity-proof/dense-qualified-names.athena", components = 2, ports = 2, connections = 1),
    )

    @Test
    fun `valid m0 corpus lowers to the expected canonical engineering ir shape and identity scheme`() {
        val repoRoot = resolveRepoRoot()
        validCorpus.forEach { fixture ->
            val result = AthenaCompiler().compile(repoRoot.resolve(fixture.relativePath))
            val success = assertIs<CompilerCompilationSuccess>(result, "Expected `${fixture.relativePath}` to compile.")
            val document = success.document

            assertEquals(fixture.components, document.components.size, "component count for ${fixture.relativePath}")
            assertEquals(fixture.ports, document.ports.size, "port count for ${fixture.relativePath}")
            assertEquals(fixture.connections, document.connections.size, "connection count for ${fixture.relativePath}")

            assertTrue(
                document.system.id.value.startsWith("system:"),
                "system identity must use the `system:` scheme for ${fixture.relativePath}",
            )
            document.components.forEach { component ->
                assertEquals(
                    "component:${component.name}",
                    component.id.value,
                    "component identity scheme for ${fixture.relativePath}",
                )
            }
            document.ports.forEach { port ->
                val owner = port.ownerReference.authoredPath.joinToString(".")
                assertEquals(
                    "port:$owner.${port.name}",
                    port.id.value,
                    "port identity scheme for ${fixture.relativePath}",
                )
            }
            document.connections.forEach { connection ->
                val from = connection.from.authoredPath.joinToString(".")
                val to = connection.to.authoredPath.joinToString(".")
                assertEquals(
                    "connection:$from->$to",
                    connection.id.value,
                    "connection identity scheme for ${fixture.relativePath}",
                )
            }
        }
    }

    @Test
    fun `valid m0 corpus re-lowers deterministically to structurally equal documents`() {
        val repoRoot = resolveRepoRoot()
        validCorpus.forEach { fixture ->
            val path = repoRoot.resolve(fixture.relativePath)
            val compiler = AthenaCompiler()
            val first: EngineeringDocument = assertIs<CompilerLoweringSuccess>(compiler.lower(path)).document
            val second: EngineeringDocument = assertIs<CompilerLoweringSuccess>(compiler.lower(path)).document
            assertEquals(first, second, "lowering must be deterministic for ${fixture.relativePath}")
        }
    }

    @Test
    fun `compiler pipeline reports the six named passes in order for supported source`() {
        val repoRoot = resolveRepoRoot()
        val expectedPassOrder = listOf(
            CompilerPassId.PARSE,
            CompilerPassId.LOWER,
            CompilerPassId.SEMANTIC_ENRICHMENT,
            CompilerPassId.VALIDATE,
            CompilerPassId.BACKEND_PREPARATION,
            CompilerPassId.BACKEND_EMISSION,
        )
        validCorpus.forEach { fixture ->
            val success = assertIs<CompilerCompilationSuccess>(
                AthenaCompiler().compile(repoRoot.resolve(fixture.relativePath)),
            )
            assertEquals(
                expectedPassOrder,
                success.pipeline.passes.map { record -> record.pass.id },
                "pass order for ${fixture.relativePath}",
            )
            assertEquals(
                List(expectedPassOrder.size) { CompilerPassExecutionStatus.SUCCEEDED },
                success.pipeline.passes.map { record -> record.status },
                "pass status for ${fixture.relativePath}",
            )
        }
    }

    @Test
    fun `m0 and m17 fixtures of the same authored shape lower to structurally equivalent engineering ir`() {
        val repoRoot = resolveRepoRoot()

        val m0 = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(repoRoot.resolve("examples/m0/demo-cabinet.athena")),
        ).document
        val m17 = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(repoRoot.resolve("examples/m17/parser-parity-proof/parity-cabinet.athena")),
        ).document

        // `demo-cabinet.athena` and `parity-cabinet.athena` author the identical system/device/port/
        // connect shape (two components, two ports, one connection) under different names, so the
        // canonical `EngineeringDocument` shape -- component/port/connection counts, property counts,
        // and identity scheme -- must match one-for-one across the two milestones' fixtures.
        assertEquals(m0.components.size, m17.components.size, "component count parity between m0 and m17")
        assertEquals(m0.ports.size, m17.ports.size, "port count parity between m0 and m17")
        assertEquals(m0.connections.size, m17.connections.size, "connection count parity between m0 and m17")
        assertEquals(
            m0.components.map { it.properties.size },
            m17.components.map { it.properties.size },
            "component property-count shape parity between m0 and m17",
        )
        assertEquals(
            m0.components.map { it.kind },
            m17.components.map { it.kind },
            "component kind shape parity between m0 and m17",
        )
        assertEquals(
            m0.ports.map { port -> port.ownerReference.authoredPath.size },
            m17.ports.map { port -> port.ownerReference.authoredPath.size },
            "port owner-reference arity parity between m0 and m17",
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
