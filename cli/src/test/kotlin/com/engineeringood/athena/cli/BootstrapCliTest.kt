package com.engineeringood.athena.cli

import kotlin.test.Test
import kotlin.test.assertContains

class BootstrapCliTest {
    @Test
    fun `renders bootstrap help for the m0 compiler workspace`() {
        val help = BootstrapCli().run(listOf("--help"))

        assertContains(help, "Athena M0 compiler workspace")
        assertContains(help, "Java 25")
        assertContains(help, "cli, language, semantics-core, ir, compiler, domain-electrical-runtime, renderer-svg")
    }
}
