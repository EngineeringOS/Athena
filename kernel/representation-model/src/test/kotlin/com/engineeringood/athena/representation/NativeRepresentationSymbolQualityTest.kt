package com.engineeringood.athena.representation

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NativeRepresentationSymbolQualityTest {
    @Test
    fun `route attached demo symbols expose named terminal anchors`() {
        val definitions = nativeDefinitions()

        definitions.forEach { definition ->
            assertTrue(
                definition.anatomy.terminals.isNotEmpty(),
                "Route-attached symbol `${definition.symbolId.value}` must expose at least one terminal anchor.",
            )
            definition.anatomy.terminals.forEach { terminal ->
                assertTrue(terminal.terminalId.value.isNotBlank())
                assertTrue(terminal.notation.number.value.isNotBlank())
            }
        }
    }

    @Test
    fun `demo symbols carry required metadata slots bounds hotspot lifecycle and version`() {
        val definitions = nativeDefinitions()

        definitions.forEach { definition ->
            assertTrue(definition.version.value.matches(Regex("""\d+\.\d+\.\d+""")))
            assertEquals(RepresentationLifecycleState.ACTIVE, definition.lifecycle.state)
            assertTrue(definition.lifecycle.provenance.source.isNotBlank())
            assertTrue(definition.anatomy.bounds.width.value > 0)
            assertTrue(definition.anatomy.bounds.height.value > 0)
            assertTrue(definition.anatomy.hotspot.point.x.value >= 0)
            assertTrue(definition.anatomy.hotspot.point.y.value >= 0)
            assertTrue(definition.labelSlots.isNotEmpty())
            assertTrue(definition.variants.isNotEmpty())
            assertTrue(definition.styleTokens.isNotEmpty())
        }
    }

    @Test
    fun `primitive emission is deterministic across repeated loads`() {
        val first = NativeRepresentationPrimitiveEmitter.emit(nativeLibrary())
        val second = NativeRepresentationPrimitiveEmitter.emit(nativeLibrary())

        assertEquals(first, second)
        assertEquals(first.sorted(), first)
    }

    private fun nativeDefinitions(): List<RepresentationDefinition> = nativeLibrary().definitions

    private fun nativeLibrary(): NativeRepresentationLibrary {
        val resource = requireNotNull(
            javaClass.classLoader.getResource("representation-libraries/athena-native-iec-v0.properties"),
        ) { "Missing native M30 symbol pack resource." }
        val result = NativeRepresentationLibraryLoader().load(Path.of(resource.toURI()))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        return result.library
    }
}
