package com.engineeringood.athena.cli

/** Process entry point for the Athena CLI wrapper. */
fun main(args: Array<String>) {
    println(BootstrapCli().run(args.toList()))
}
