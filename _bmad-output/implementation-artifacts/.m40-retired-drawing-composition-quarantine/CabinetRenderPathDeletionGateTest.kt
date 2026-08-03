package com.engineeringood.athena.compiler

import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertFalse

class CabinetRenderPathDeletionGateTest {
    @Test
    fun `active cabinet path has no raw markup fallback`() {
        val root = repositoryRootForDeletionGate()
        val activeCabinetSources = listOf(
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialBinder.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialModels.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialResolver.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt"),
            root.resolve("kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveTransport.kt"),
        )

        val forbidden = listOf(
            "PackageBackedRepresentationOccurrenceFactory",
            "stageRepository(",
            "innerHTML",
            "descriptor-bounds",
            "fallback rectangle",
        )
        activeCabinetSources.forEach { source ->
            val text = source.readText()
            forbidden.forEach { token ->
                assertFalse(text.contains(token), "${source.fileName} must not contain active cabinet token `$token`.")
            }
        }
        val transportText = root
            .resolve("kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveTransport.kt")
            .readText()
        assertFalse(transportText.contains("rawMarkup"), "GraphicPrimitive transport must not carry raw markup.")
    }
}

private fun repositoryRootForDeletionGate(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !current.resolve("settings.gradle.kts").toFile().exists()) {
        current = current.parent
    }
    return current
}
