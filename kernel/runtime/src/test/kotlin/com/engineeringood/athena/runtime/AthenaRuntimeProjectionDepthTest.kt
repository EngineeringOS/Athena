package com.engineeringood.athena.runtime

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaRuntimeProjectionDepthTest {
    @Test
    fun `dense m11 proof delivers repeated references through runtime projection sessions`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m11/dense-electrical-proof/src/assembly-line.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "assembly-line",
            sourcePath = sourcePath,
        )

        val cabinet = assertIs<AthenaRuntimeProjectionReadySnapshot>(context.projectProjectionSession().activeProjection)
        assertEquals("electrical/cabinet", cabinet.familyId)
        assertEquals(16, cabinet.scene.components.size)
        assertEquals(29, cabinet.scene.connections.size)

        val switchResult = context.switchActiveProjectionView("documentation")
        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        val documentation = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)

        assertEquals("documentation", documentation.viewId)
        assertEquals("electrical/documentation", documentation.familyId)
        assertEquals(
            listOf(
                "documentation/sheet/01-power-distribution",
                "documentation/sheet/02-control-and-plc-logic",
                "documentation/sheet/03-field-wiring-and-terminal-transition",
            ),
            documentation.sheets.map { sheet -> sheet.sheetId },
        )
        assertTrue(documentation.crossReferences.size >= 12)
        assertEquals(2, documentation.scene.components.count { component -> component.semanticId == "component:M1" })
        assertEquals("electrical-notation/documentation/default-v1", documentation.notationPack?.packId)
        assertTrue(documentation.crossReferences.any { crossReference -> crossReference.sheetIds.size >= 2 })
    }

    private fun resolveRepoRoot(): Path {
        return generateSequence(Path.of("").toAbsolutePath().normalize()) { candidate -> candidate.parent }
            .first { candidate ->
                candidate.resolve("settings.gradle.kts").toFile().exists() &&
                    candidate.resolve("examples").toFile().exists()
            }
    }
}
