package com.engineeringood.athena.representation

import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NativeRepresentationDemoSymbolPackTest {
    @Test
    fun `native iec v0 pack contains focused industrial control demo symbols`() {
        val assetPath = nativePackPath()

        val result = NativeRepresentationLibraryLoader().load(assetPath)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            setOf(
                RepresentationSymbolKind.SUPPLY_REFERENCE,
                RepresentationSymbolKind.TERMINAL,
                RepresentationSymbolKind.SWITCH_CONTACT,
                RepresentationSymbolKind.COIL_ACTUATOR,
                RepresentationSymbolKind.LAMP_INDICATOR,
                RepresentationSymbolKind.MOTOR_LOAD,
                RepresentationSymbolKind.PROTECTIVE_DEVICE,
                RepresentationSymbolKind.FOLIO_REFERENCE,
            ),
            result.library.definitions.map { definition -> definition.kind }.toSet(),
        )
        assertTrue(result.library.definitions.all { definition -> definition.anatomy.primitives.isNotEmpty() })
        assertTrue(result.library.definitions.all { definition -> definition.labelSlots.isNotEmpty() })
        assertTrue(result.library.definitions.all { definition ->
            definition.anatomy.terminals.isNotEmpty() || definition.anatomy.labelAnchors.isNotEmpty()
        })
    }

    @Test
    fun `native iec v0 pack has no qet xml theia or svg snippet source truth`() {
        val assetPath = nativePackPath()
        val text = assetPath.readText()

        listOf("<definition", ".elmt", "qet", "theia", "<svg", "pathData").forEach { forbidden ->
            assertTrue(
                text.contains(forbidden, ignoreCase = true).not(),
                "Native symbol pack must not contain runtime source truth marker `$forbidden`.",
            )
        }
    }

    private fun nativePackPath(): Path {
        val resource = requireNotNull(
            javaClass.classLoader.getResource("representation-libraries/athena-native-iec-v0.properties"),
        ) { "Missing native M30 symbol pack resource." }
        return Path.of(resource.toURI())
    }
}
