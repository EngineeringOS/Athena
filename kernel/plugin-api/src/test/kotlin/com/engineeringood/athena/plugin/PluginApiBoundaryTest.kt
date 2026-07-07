package com.engineeringood.athena.plugin

import kotlin.test.Test
import kotlin.test.assertEquals

class PluginApiBoundaryTest {
    @Test
    fun `exposes stable manifest and domain source contracts from the plugin api package`() {
        val manifest = AthenaPluginManifest(
            pluginId = "com.engineeringood.athena.domain.sample",
            pluginVersion = "0.0.1-SNAPSHOT",
            pluginType = AthenaPluginType.DOMAIN,
            coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
            requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
        )
        val source = AthenaSourceDocument(
            file = "sample.eos",
            ast = com.engineeringood.athena.language.SourceFileAst(
                system = com.engineeringood.athena.language.SystemDeclaration(
                    name = "Sample",
                    span = com.engineeringood.athena.language.SourceSpan(
                        start = com.engineeringood.athena.language.SourcePosition(offset = 0, line = 1, column = 1),
                        end = com.engineeringood.athena.language.SourcePosition(offset = 6, line = 1, column = 7),
                    ),
                ),
                declarations = emptyList(),
                span = com.engineeringood.athena.language.SourceSpan(
                    start = com.engineeringood.athena.language.SourcePosition(offset = 0, line = 1, column = 1),
                    end = com.engineeringood.athena.language.SourcePosition(offset = 6, line = 1, column = 7),
                ),
            ),
        )

        assertEquals("com.engineeringood.athena.domain.sample", manifest.pluginId)
        assertEquals("sample.eos", source.file)
    }
}
