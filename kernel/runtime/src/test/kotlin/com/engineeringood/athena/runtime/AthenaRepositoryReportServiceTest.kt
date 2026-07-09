package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportLockState
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaRepositoryReportServiceTest {
    @Test
    fun `runtime workspace publishes repository graph report through shared compiler authority`() {
        val repositoryRoot = createTempDirectory("athena-runtime-repository-report-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val workspace = runtime.openWorkspace(repositoryRoot)
            val published = workspace.repositoryGraphReport()

            assertTrue(published.isValid)
            assertEquals(AthenaRepositoryReportLockState.CURRENT, published.lockState)
            assertEquals("com.engineeringood.root", published.report?.repository?.manifest?.primaryPackage?.id?.name)
            assertEquals("com.engineeringood.root", published.report?.repository?.lock?.primaryPackage?.name)
            assertSame(runtime.serviceRegistry.repositoryReports(), workspace.repositoryReports())
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private fun writeGovernedRepository(
    repositoryRoot: java.nio.file.Path,
    sourceFileName: String,
    manifestBody: String,
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(manifestBody)
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    sourceRoot.resolve(sourceFileName).writeText(
        "system ${sourceFileName.substringBefore('.').replaceFirstChar(Char::uppercase)} { }",
    )
}
