package com.engineeringood.athena.ide.lsp

import kotlin.system.exitProcess

/**
 * Process entry point for the first Athena IDE session host.
 */
fun main(args: Array<String>) {
    val arguments = args.toList()
    val exitCode = if (arguments.contains("--stdio")) {
        AthenaLspServerCli().run(arguments)
    } else {
        AthenaLspSessionHostCli().run(arguments)
    }
    exitProcess(exitCode)
}
