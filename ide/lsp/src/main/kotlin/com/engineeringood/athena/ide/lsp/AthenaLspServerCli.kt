package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.launch.LSPLauncher

/**
 * CLI entry point for the stdio-based Athena LSP server.
 */
class AthenaLspServerCli(
    private val serverFactory: () -> AthenaLanguageServer = { AthenaLanguageServer() },
) {
    /**
     * Runs the Athena LSP server on stdio until the client closes the connection.
     */
    fun run(arguments: List<String>): Int {
        if (arguments == listOf("--help") || arguments == listOf("-h")) {
            println(helpText())
            return 0
        }

        if (!arguments.contains("--stdio")) {
            System.err.println("Missing required argument: --stdio")
            return 1
        }

        val server = serverFactory()
        val launcher = LSPLauncher.createServerLauncher(server, System.`in`, System.out)
        server.connect(launcher.remoteProxy)
        launcher.startListening().get()
        return 0
    }

    private fun helpText(): String {
        return "Usage: athena-lsp-host --stdio | athena-lsp-host --repository-root <path>"
    }
}
