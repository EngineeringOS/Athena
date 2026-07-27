package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaM34CabinetRenderPathDeletionGateTest {
    @Test
    fun `active M34 cabinet path has no raw markup fallback or PresentationPrimitive producer`() {
        val root = repositoryRootForDeletionGate()
        val activeM34Sources = listOf(
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialBinder.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialModels.kt"),
            root.resolve("kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialResolver.kt"),
            root.resolve("kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M34CabinetRenderPathProof.kt"),
            root.resolve("kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveTransport.kt"),
        )

        val forbidden = listOf(
            "PresentationPrimitive",
            "PackageBackedRepresentationOccurrenceFactory",
            "stageRepository(",
            "innerHTML",
            "descriptor-bounds",
            "fallback rectangle",
        )
        activeM34Sources.forEach { source ->
            val text = source.readText()
            forbidden.forEach { token ->
                assertFalse(text.contains(token), "${source.fileName} must not contain active M34 token `$token`.")
            }
        }
        val transportText = root
            .resolve("kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveTransport.kt")
            .readText()
        assertFalse(transportText.contains("rawMarkup"), "GraphicPrimitive transport must not carry raw markup.")
    }

    @Test
    fun `remaining PresentationPrimitive references are ledgered as compatibility only`() {
        val root = repositoryRootForDeletionGate()
        val ledger = root.resolve("_bmad-output/implementation-artifacts/m34/representation-migration-ledger.md").readText()
        assertTrue(ledger.contains("`PresentationPrimitive` body | Replace"))
        assertTrue(ledger.contains("compatibility-only"))

        val activeMainReferences = Files.walk(root.resolve("kernel/compiler/src/main/kotlin")).use { paths ->
            paths
                .filter { Files.isRegularFile(it) && it.extension == "kt" }
                .filter { it.readText().contains("PresentationPrimitive") }
                .map { root.relativize(it).toString().replace('\\', '/') }
                .sorted()
                .toList()
        }

        assertEquals(
            listOf(
                "kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt",
                "kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt",
                "kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/M32PackageBackedPresentationFactDeriver.kt",
                "kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt",
            ),
            activeMainReferences,
        )
    }
}

private fun repositoryRootForDeletionGate(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !current.resolve("settings.gradle.kts").toFile().exists()) {
        current = current.parent
    }
    return current
}
