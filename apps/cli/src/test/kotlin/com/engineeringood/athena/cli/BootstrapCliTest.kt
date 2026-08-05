package com.engineeringood.athena.cli

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class BootstrapCliTest {
    @Test
    fun `help exposes only current read-only source command`() {
        val help = BootstrapCli().run(emptyList())

        assertContains(help, "Athena EngineeringOS CLI")
        assertContains(help, "parse <source-file>")
        assertFalse("connect" in help)
        assertFalse("render" in help)
        assertFalse("ai-propose" in help)
    }
}
