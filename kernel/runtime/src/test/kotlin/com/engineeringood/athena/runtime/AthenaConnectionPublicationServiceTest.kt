package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AthenaConnectionPublicationServiceTest {
    @Test
    fun `invalid replacement is stale after accepted publication and unavailable before`() {
        val compiler = AthenaCompiler()
        val source = """
            system Control {
              entity PLC1 { concept controller }
              entity KM1 { concept contactor }
              port PLC1.out { direction out flow control }
              port KM1.coil { direction in flow control }
              connect signal PLC1.out to KM1.coil
            }
        """.trimIndent()
        val valid = compiler.compile(Path.of("src/control.athena"), source)
        val invalid = compiler.compile(Path.of("src/control.athena"), source.replace("direction out", "direction in"))
        val service = AthenaConnectionPublicationService()

        assertEquals(ConnectionPublicationState.UNAVAILABLE, service.publish(invalid).state)
        assertEquals(ConnectionPublicationState.ACCEPTED, service.publish(valid).state)
        assertNotNull(service.current())
        assertEquals(ConnectionPublicationState.STALE, service.publish(invalid).state)
        assertNotNull(service.publish(invalid).document)
    }
}
