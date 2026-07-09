package com.engineeringood.athena.ide.lsp

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

/**
 * Creates a governed M5 repository fixture for LSP tests.
 */
fun createGovernedTestRepository(
    prefix: String,
    packageName: String = "com.engineeringood.factory-line",
    sourceFileName: String = "factory-line.athena",
    sourceText: String = "system FactoryLine { }",
): AthenaLspTestRepository {
    val repositoryRoot = createTempDirectory(prefix)
    repositoryRoot.resolve("athena.yaml").writeText(
        """
            primaryPackage:
              name: $packageName
              version: 0.1.0
              sourceRoot: src
        """.trimIndent(),
    )
    repositoryRoot.resolve("athena.lock").writeText(
        """
            version: 1
            primaryPackage:
              name: $packageName
              version: 0.1.0
            packages:
              - name: $packageName
                version: 0.1.0
                sourceRoot: src
                dependencies: []
        """.trimIndent(),
    )
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    val seedSourcePath = sourceRoot.resolve(sourceFileName)
    seedSourcePath.writeText(sourceText)
    return AthenaLspTestRepository(
        repositoryRoot = repositoryRoot,
        sourceRoot = sourceRoot,
        seedSourcePath = seedSourcePath,
    )
}

/**
 * Holds the temporary filesystem paths for one governed LSP test repository.
 */
data class AthenaLspTestRepository(
    val repositoryRoot: Path,
    val sourceRoot: Path,
    val seedSourcePath: Path,
)
