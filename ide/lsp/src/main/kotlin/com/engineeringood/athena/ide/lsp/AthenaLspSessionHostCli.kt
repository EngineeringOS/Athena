package com.engineeringood.athena.ide.lsp

import java.nio.file.Path

/**
 * CLI facade for the first `ide/lsp` session host used by the Theia product shell.
 */
class AthenaLspSessionHostCli(
    private val sessionHost: AthenaLspSessionHost = AthenaLspSessionHost(),
) {
    /**
     * Activates one repository-backed session and keeps the process alive until shutdown.
     */
    fun run(arguments: List<String>): Int {
        if (arguments.isEmpty() || arguments == listOf("--help") || arguments == listOf("-h")) {
            println(helpText())
            return 0
        }

        val repositoryRoot = parseRepositoryRoot(arguments)
            ?: run {
                System.err.println("Missing required argument: --repository-root <path>")
                return 1
            }

        val activation = sessionHost.activateRepository(repositoryRoot)
        println(activation.toEventLine())
        if (activation is AthenaLspSessionHostReady) {
            sessionHost.serveUntilShutdown()
            return 0
        }
        return 1
    }

    private fun parseRepositoryRoot(arguments: List<String>): Path? {
        val repositoryRootFlagIndex = arguments.indexOf("--repository-root")
        if (repositoryRootFlagIndex < 0 || repositoryRootFlagIndex == arguments.lastIndex) {
            return null
        }
        return Path.of(arguments[repositoryRootFlagIndex + 1])
    }

    private fun helpText(): String {
        return "Usage: athena-lsp-host --repository-root <path>"
    }
}
