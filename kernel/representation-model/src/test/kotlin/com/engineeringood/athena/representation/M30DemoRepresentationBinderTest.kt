package com.engineeringood.athena.representation

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class M30DemoRepresentationBinderTest {
    @Test
    fun `demo devices and ports bind to native symbols and terminal anchors`() {
        val proof = M30DemoRepresentationBinder().bind(M30DemoRepresentationSample.controlSheet(), nativeLibrary())

        assertTrue(proof.diagnostics.isEmpty(), proof.diagnostics.toString())
        assertEquals(
            setOf(
                RepresentationOccurrenceRole.SUPPLY_REFERENCE,
                RepresentationOccurrenceRole.TERMINAL,
                RepresentationOccurrenceRole.SWITCH_CONTACT,
                RepresentationOccurrenceRole.COIL_ACTUATOR,
                RepresentationOccurrenceRole.LAMP_INDICATOR,
                RepresentationOccurrenceRole.LOAD_SYMBOL,
                RepresentationOccurrenceRole.PROTECTIVE_DEVICE,
            ),
            proof.deviceOccurrences.map { occurrence -> occurrence.occurrenceRole }.toSet(),
        )
        assertTrue(proof.deviceOccurrences.all { occurrence -> occurrence.terminalBindings.isNotEmpty() })
        assertTrue(proof.deviceOccurrences.all { occurrence -> occurrence.labelBindings.isNotEmpty() })
        assertTrue(proof.deviceOccurrences.all { occurrence ->
            nativeLibrary().definitions.any { definition -> definition.symbolId == occurrence.symbolId }
        })
    }

    @Test
    fun `demo reference facts produce continuation representation occurrence`() {
        val proof = M30DemoRepresentationBinder().bind(M30DemoRepresentationSample.controlSheet(), nativeLibrary())

        assertTrue(proof.diagnostics.isEmpty(), proof.diagnostics.toString())
        val referenceOccurrence = proof.referenceOccurrences.single()
        assertEquals(RepresentationOccurrenceRole.FOLIO_REFERENCE, referenceOccurrence.occurrenceRole)
        assertEquals(
            setOf(RepresentationReferenceKind.FOLIO_CONTINUATION),
            referenceOccurrence.referenceBindings.map { binding -> binding.kind }.toSet(),
        )
        assertEquals(RepresentationSubjectId("connection:ControlRelayK1.a1->MotorM1.u1"), referenceOccurrence.canonicalSemanticId)
    }

    private fun nativeLibrary(): NativeRepresentationLibrary {
        val resource = requireNotNull(
            javaClass.classLoader.getResource("representation-libraries/athena-native-iec-v0.properties"),
        ) { "Missing native M30 symbol pack resource." }
        val result = NativeRepresentationLibraryLoader().load(Path.of(resource.toURI()))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        return result.library
    }
}
