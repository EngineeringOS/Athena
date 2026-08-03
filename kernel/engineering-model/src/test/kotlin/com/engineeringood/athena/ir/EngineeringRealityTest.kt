package com.engineeringood.athena.ir

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EngineeringRealityTest {
    @Test
    fun `engineering reality declares authority identity and required facts`() {
        assertEquals("Engineering Reality", EngineeringReality.declaration.name)
        assertEquals("EngineeringDocument", EngineeringReality.rootName)
        assertEquals("engineering compiler", EngineeringReality.authority)
        assertContains(EngineeringReality.ownedFacts, "system")
        assertContains(EngineeringReality.ownedFacts, "device")
        assertContains(EngineeringReality.ownedFacts, "port")
        assertContains(EngineeringReality.ownedFacts, "connection")
        assertContains(EngineeringReality.ownedFacts, "constraint")
        assertContains(EngineeringReality.requiredFacts, "system identity")
        assertContains(EngineeringReality.requiredFacts, "engineering source identity")
        assertTrue(EngineeringReality.identityRules.any { rule -> rule.fact == "connection" })
        assertFalse(EngineeringReality.ownedFacts.any { fact -> fact.contains("coordinate", ignoreCase = true) })
        assertFalse(EngineeringReality.ownedFacts.any { fact -> fact.contains("stroke", ignoreCase = true) })
    }

    @Test
    fun `engineering validation reports missing facts in plain language`() {
        val result = EngineeringReality.validate(
            EngineeringDocument(
                system = EngineeringSystem(
                    id = StableSemanticIdentity(""),
                    name = "",
                    provenance = SourceProvenance("", 1, 1, 1, 1),
                ),
                components = emptyList(),
                ports = emptyList(),
                connections = emptyList(),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.issues.any { issue ->
            issue.reality == "Engineering Reality" && issue.message == "missing system identity"
        })
        assertTrue(result.issues.any { issue ->
            issue.reality == "Engineering Reality" && issue.message == "missing engineering source identity"
        })
    }

    @Test
    fun `reality declaration files use current product names`() {
        val repo = repoRoot()
        val files = listOf(
            repo.resolve("kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringReality.kt"),
            repo.resolve("kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReality.kt"),
            repo.resolve("kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt"),
            repo.resolve("kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt"),
        )
        val banned = listOf("M39", "M38", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "compatibility")

        files.forEach { file ->
            val text = Files.readString(file)
            banned.forEach { token ->
                assertFalse(
                    text.contains(token),
                    "Reality declaration file ${file.fileName} must not contain `$token`.",
                )
            }
        }
    }

    private fun repoRoot(): Path {
        return generateSequence(Path.of("").toAbsolutePath()) { path -> path.parent }
            .first { path -> Files.exists(path.resolve("settings.gradle.kts")) }
    }
}
