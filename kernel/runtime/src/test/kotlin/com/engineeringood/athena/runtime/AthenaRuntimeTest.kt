package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaRuntimeTest {
    @Test
    fun `opening workspace does not instantiate compiler`() {
        var compilerCreations = 0
        val runtime = AthenaRuntime(
            AthenaServiceRegistry(
                compilerProvider = {
                    compilerCreations += 1
                    AthenaCompiler()
                },
            ),
        )

        val workspace = runtime.openWorkspace(Path.of("workspace"))

        assertEquals(Path.of("workspace"), workspace.rootPath)
        assertNull(workspace.activeProject)
        assertEquals(0, compilerCreations)
    }

    @Test
    fun `active project shares runtime compiler and engineering graph`() {
        val compiler = AthenaCompiler()
        val runtime = AthenaRuntime(AthenaServiceRegistry(compilerProvider = { compiler }))
        val context = runtime.openWorkspace(Path.of("workspace")).activateProject(
            projectName = "plant",
            sourcePath = Path.of("workspace/plant.athena"),
        )

        assertSame(context, runtime.activeExecutionContext)
        assertSame(compiler, context.compiler())
        assertSame(runtime.serviceRegistry.engineeringGraph(), context.engineeringGraph())
    }

    @Test
    fun `service registry caches current services`() {
        val services = AthenaServiceRegistry()

        assertSame(services.compiler(), services.compiler())
        assertSame(services.repositoryReports(), services.repositoryReports())
        assertSame(services.semanticBaselines(), services.semanticBaselines())
        assertSame(services.semanticDiffs(), services.semanticDiffs())
        assertSame(services.semanticReviews(), services.semanticReviews())
        assertSame(services.semanticCommits(), services.semanticCommits())
        assertSame(services.semanticScmStates(), services.semanticScmStates())
        assertSame(services.semanticHistoryStates(), services.semanticHistoryStates())
        assertSame(services.engineeringGraph(), services.engineeringGraph())
        assertSame(services.pluginRuntimeServices(), services.pluginRuntimeServices())
    }

    @Test
    fun `runtime compilation exposes syntax diagnostics`() {
        val brokenPath = Files.createTempFile("athena-runtime-broken-", ".athena")
        Files.writeString(brokenPath, "system Broken { entity")
        try {
            val context = AthenaRuntime().openWorkspace(brokenPath.parent).activateProject("broken", brokenPath)

            val failure = assertIs<CompilerCompilationParseFailure>(context.compileActiveProject())

            assertTrue(failure.diagnostics.isNotEmpty())
            assertTrue(failure.diagnostics.first().file.endsWith(brokenPath.fileName.toString()))
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    @Test
    fun `closing runtime invalidates workspace`() {
        val runtime = AthenaRuntime()
        val workspace = runtime.openWorkspace(Path.of("workspace"))

        runtime.closeWorkspace()

        assertNull(runtime.activeWorkspace)
        assertNull(runtime.activeExecutionContext)
        assertFailsWith<IllegalStateException> {
            workspace.activateProject("plant", Path.of("workspace/plant.athena"))
        }
    }
}
