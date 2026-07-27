package com.engineeringood.athena.ide.lsp

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import com.engineeringood.athena.compiler.AthenaCompiler

/**
 * Creates a governed M5 repository fixture for LSP tests.
 */
fun createGovernedTestRepository(
    prefix: String,
    packageName: String = "com.engineeringood.factoryline",
    sourceFileName: String = "factoryline.athena",
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
    val packageDirectory = sourceRoot.resolve(packageName.replace('.', '/')).createDirectories()
    val seedSourcePath = packageDirectory.resolve(sourceFileName)
    seedSourcePath.writeText(governedAthenaSource(sourceText, packageName))
    AthenaCompiler().materializeRepositoryLock(repositoryRoot)
    return AthenaLspTestRepository(
        repositoryRoot = repositoryRoot,
        sourceRoot = sourceRoot,
        seedSourcePath = seedSourcePath,
    )
}

fun governedAthenaSource(
    sourceText: String,
    packageName: String = "com.engineeringood.factoryline",
): String {
    return if (sourceText.trimStart().startsWith("package ")) {
        sourceText
    } else {
        "package $packageName\n\n$sourceText"
    }
}

/**
 * Holds the temporary filesystem paths for one governed LSP test repository.
 */
data class AthenaLspTestRepository(
    val repositoryRoot: Path,
    val sourceRoot: Path,
    val seedSourcePath: Path,
)
