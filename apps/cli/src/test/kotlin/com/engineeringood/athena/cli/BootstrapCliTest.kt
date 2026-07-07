package com.engineeringood.athena.cli

import kotlin.test.Test
import kotlin.test.assertContains

class BootstrapCliTest {
    @Test
    fun `renders bootstrap help for the runtime host above the compiler workspace`() {
        val help = BootstrapCli().run(listOf("--help"))

        assertContains(help, "Athena M1 runtime host")
        assertContains(help, "Java 25")
        assertContains(help, "apps:cli, kernel:runtime, kernel:language, kernel:validation, kernel:engineering-model, kernel:compiler, extensions:domain-electrical, extensions:domain-dummy, kernel:svg-renderer")
        assertContains(help, "kernel:runtime")
        assertContains(help, "plugins")
        assertContains(help, "plugin-command <source-file> <contribution-id>")
        assertContains(help, "ai-propose-connect <source-file> <source-port-path> <target-port-path> <summary>")
        assertContains(help, "ai-accept <source-file> <proposal-id>")
    }
}
