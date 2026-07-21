package com.engineeringood.athena.representation

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class M30ControlSheetCompositionProofTest {
    @Test
    fun `control sheet composition proof is dense wrapperless and label safe`() {
        val bindingProof = M30DemoRepresentationBinder().bind(M30DemoRepresentationSample.controlSheet(), nativeLibrary())
        val proof = M30ControlSheetCompositionProofCompiler().compile(bindingProof, nativeLibrary())

        assertEquals(
            mapOf(
                "accepted" to "true",
                "compositionFactCount" to "7",
                "normalWrapperVisible" to "false",
                "labelOverlapCount" to "0",
                "routeChannelCount" to "1",
                "referenceZoneCount" to "1",
            ),
            proof.toTransportPayload(),
        )
    }

    private fun nativeLibrary(): NativeRepresentationLibrary {
        val resource = requireNotNull(
            javaClass.classLoader.getResource("representation-libraries/athena-native-iec-v0.properties"),
        ) { "Missing native M30 symbol pack resource." }
        val result = NativeRepresentationLibraryLoader().load(Path.of(resource.toURI()))
        return result.library
    }
}
