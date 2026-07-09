package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.runtime.AthenaRuntime
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class AthenaLspSessionHostTest {
    @Test
    fun `lsp host activation consumes the runtime owned repository graph session`() {
        val repositoryRoot = createTempDirectory("athena-lsp-session-host-")
        try {
            val sourceRoot = repositoryRoot.resolve("src").createDirectories()
            val sourcePath = sourceRoot.resolve("factory-line.athena")
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.factory.line
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            sourcePath.writeText("system FactoryLine { }")
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val host = AthenaLspSessionHost(runtime = runtime)

            val activation = host.activateRepository(repositoryRoot)

            val ready = assertIs<AthenaLspSessionHostReady>(activation)
            assertSame(runtime.activeRepositoryGraphSession, ready.session)
            assertSame(runtime.activeExecutionContext, ready.session.executionContext)
            assertEquals(repositoryRoot.toAbsolutePath().normalize(), ready.session.repositoryRoot)
            assertEquals("com.engineeringood.factory.line", ready.session.publication.report?.repository?.manifest?.primaryPackage?.id?.name)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
