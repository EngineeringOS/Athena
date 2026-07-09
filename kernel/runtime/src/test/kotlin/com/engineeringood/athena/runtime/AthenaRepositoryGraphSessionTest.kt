package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportLockState
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaRepositoryGraphSessionTest {
    @Test
    fun `activating a governed repository creates one runtime owned repository graph session`() {
        val repositoryRoot = createTempDirectory("athena-repository-graph-session-")
        try {
            val sourcePath = writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.factory.line",
                sourceFileName = "factory-line.athena",
            )
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val workspace = runtime.openWorkspace(repositoryRoot)
            val session = workspace.activateRepositoryGraphSession(
                projectName = "factory-line",
                sourcePath = sourcePath,
            )

            assertSame(session, runtime.activeRepositoryGraphSession)
            assertSame(session, workspace.activeRepositoryGraphSession)
            assertSame(session.executionContext, runtime.activeExecutionContext)
            assertEquals(AthenaRepositoryReportLockState.CURRENT, session.publication.lockState)
            assertEquals(
                "com.engineeringood.factory.line",
                session.publication.report?.repository?.manifest?.primaryPackage?.id?.name,
            )
            assertEquals(
                "com.engineeringood.factory.line",
                session.publication.report?.repository?.lock?.primaryPackage?.name,
            )
            assertTrue(session.publication.report?.diagnostics?.isEmpty() == true)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `opening another repository replaces the previous repository graph session`() {
        val firstRoot = createTempDirectory("athena-repository-graph-session-first-")
        val secondRoot = createTempDirectory("athena-repository-graph-session-second-")
        try {
            val firstSourcePath = writeGovernedRepository(
                repositoryRoot = firstRoot,
                packageName = "com.engineeringood.first",
                sourceFileName = "first.athena",
            )
            val secondSourcePath = writeGovernedRepository(
                repositoryRoot = secondRoot,
                packageName = "com.engineeringood.second",
                sourceFileName = "second.athena",
            )
            val compiler = AthenaCompiler()
            compiler.materializeRepositoryLock(firstRoot)
            compiler.materializeRepositoryLock(secondRoot)

            val runtime = AthenaRuntime()
            val firstWorkspace = runtime.openWorkspace(firstRoot)
            val firstSession = firstWorkspace.activateRepositoryGraphSession(
                projectName = "first",
                sourcePath = firstSourcePath,
            )

            val secondWorkspace = runtime.openWorkspace(secondRoot)
            val secondSession = secondWorkspace.activateRepositoryGraphSession(
                projectName = "second",
                sourcePath = secondSourcePath,
            )

            assertNotSame(firstSession, secondSession)
            assertSame(secondSession, runtime.activeRepositoryGraphSession)
            assertSame(secondSession, secondWorkspace.activeRepositoryGraphSession)
            assertEquals(secondRoot.toAbsolutePath().normalize(), secondSession.repositoryRoot)
            assertEquals("second", secondSession.project.name)
        } finally {
            firstRoot.toFile().deleteRecursively()
            secondRoot.toFile().deleteRecursively()
        }
    }
}

private fun writeGovernedRepository(
    repositoryRoot: java.nio.file.Path,
    packageName: String,
    sourceFileName: String,
): java.nio.file.Path {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(
        """
            primaryPackage:
              name: $packageName
              version: 1.0.0
              sourceRoot: src
        """.trimIndent(),
    )
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    val sourcePath = sourceRoot.resolve(sourceFileName)
    sourcePath.writeText(
        "system ${sourceFileName.substringBefore('.').replaceFirstChar(Char::uppercase)} { }",
    )
    return sourcePath
}
