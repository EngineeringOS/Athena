package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.packageplatform.MacroInsertionRequest
import com.engineeringood.athena.packageplatform.MacroTransform
import com.engineeringood.athena.packageplatform.PackageItemIdentity
import com.engineeringood.athena.packageruntime.LockedRepresentationPackageRuntime
import com.engineeringood.athena.packageruntime.RepresentationMacroResolver
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class M45NativePackageCatalogTest {
    @Test
    fun `checked m45 fixture contains complete native package item ecosystem`() {
        val root = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
            .resolve("../../examples/m45/rolling-shutter").normalize()
        val packages = Files.list(root.resolve("packages")).use { it.filter(Files::isDirectory).toList() }
        assertEquals(listOf("com.athena.iec", "com.community.elements", "com.vendor.siemens"), packages.map { it.fileName.toString() }.sorted())
        val itemFiles = Files.walk(root.resolve("packages")).use { it.filter { path -> path.toString().endsWith(".athena") }.toList() }
        assertTrue(itemFiles.size >= 3)
        val svgFiles = Files.walk(root.resolve("packages")).use { it.filter { path -> path.toString().endsWith(".svg") }.toList() }
        assertTrue(svgFiles.size >= 10)
        assertTrue(itemFiles.sumOf { Files.readString(it).lineSequence().count { line -> line.trimStart().startsWith("part ") } } >= 5)
        val lock = AthenaCompiler().materializeRepositoryLock(root)
        assertTrue(lock.isValid, lock.diagnostics.joinToString("\n") { it.message })
        assertTrue(Files.isRegularFile(root.resolve("athena.lock")))
        val snapshots = assertNotNull(lock.lock?.packageSnapshots)
        val items = snapshots.flatMap { it.itemDigests }
        assertTrue(items.count { it.kind == "SYMBOL" } >= 10)
        assertTrue(items.count { it.kind == "ELEMENT" } >= 11)
        assertTrue(items.count { it.kind == "PART" } >= 5)
        assertEquals(1, items.count { it.kind == "MACRO" })
        assertEquals(3, items.count { it.kind == "VARIANT" })
        assertEquals(1, items.count { it.kind == "PLACEHOLDER" })
        val macro = items.single { it.kind == "MACRO" }
        assertEquals("contactor_element", macro.attributes["child.000.element"])
        assertEquals("0", macro.attributes["child.000.x"])
        assertEquals("switching", macro.attributes["child.000.functionSlot"])
        assertEquals("motor_element", macro.attributes["child.001.element"])
        assertEquals("30", macro.attributes["child.001.x"])
        assertEquals("load", macro.attributes["child.001.functionSlot"])
        assertFalse("childCount" in macro.attributes)
        val iecSnapshot = snapshots.single { it.packageId.name == "com.athena.iec" }
        val readyItems = iecSnapshot.itemDigests.mapTo(linkedSetOf()) {
            PackageItemIdentity(iecSnapshot.packageId, it.itemId, it.itemVersion).key
        }
        val decodedMacro = LockedRepresentationPackageRuntime.decodeMacro(iecSnapshot.packageId, macro, readyItems)
        val admittedMacro = assertNotNull(decodedMacro.macro, decodedMacro.diagnostics.joinToString())
        val resolvedMacro = RepresentationMacroResolver.resolve(
            admittedMacro,
            MacroInsertionRequest(
                macroId = admittedMacro.macroId,
                sheetId = "main",
                functionIdsBySlot = mapOf(
                    "switching" to "function:KM1.switching",
                    "load" to "function:M1.drive",
                ),
                transform = MacroTransform(4, 8),
                operationId = "m45-lock-runtime-proof",
            ),
        )
        assertTrue(resolvedMacro.isValid, resolvedMacro.diagnostics.joinToString())
        assertEquals(
            mapOf(
                "contactor_element" to Triple(4, 8, "function:KM1.switching"),
                "motor_element" to Triple(34, 8, "function:M1.drive"),
            ),
            assertNotNull(resolvedMacro.occurrences).associate {
                it.childId.itemId to Triple(it.x, it.y, it.functionId)
            },
        )
        assertEquals(
            2,
            items.single { it.kind == "ELEMENT" && it.itemId == "starter_element" }.resourceReferences.size,
            "Composite Element must lock both child Symbol resources.",
        )
        assertTrue(
            items.filter { it.kind == "ELEMENT" && it.itemId != "starter_element" }
                .all { it.resourceReferences.size == 1 && it.resourceReferences.single().startsWith("resources/") },
            "Every leaf Element must lock its composed package-local SVG resource.",
        )
        val binding = Files.readString(
            root.resolve("src/com/engineeringood/m45/rollingshutter/rolling-shutter.binding.athena"),
        )
        assertTrue(binding.contains("variant \"com.vendor.siemens/contactor_main@1.0.0\""))
        assertTrue(binding.contains("placeholders"))
    }

    @Test
    fun `invalid native composition fails closed without replacing accepted lock`() {
        val source = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
            .resolve("../../examples/m45/rolling-shutter").normalize()
        val root = createTempDirectory("athena-m45-invalid-composition-")
        try {
            Files.walk(source).use { paths ->
                paths.forEach { path ->
                    val destination = root.resolve(source.relativize(path).toString())
                    if (Files.isDirectory(path)) Files.createDirectories(destination) else Files.copy(path, destination)
                }
            }
            val compiler = AthenaCompiler()
            val accepted = compiler.materializeRepositoryLock(root)
            assertTrue(accepted.isValid, accepted.diagnostics.joinToString("\n") { it.message })
            val lockBefore = Files.readString(root.resolve("athena.lock"))
            val reuse = root.resolve("packages/com.athena.iec/src/reuse.athena")
            Files.writeString(reuse, Files.readString(reuse).replace("symbol \"breaker\"", "symbol \"missing_breaker\""))

            val rejected = compiler.materializeRepositoryLock(root)

            assertFalse(rejected.isValid)
            assertTrue(rejected.diagnostics.any { it.code == "package.index.element.symbol-unresolved" })
            assertEquals(lockBefore, Files.readString(root.resolve("athena.lock")))
        } finally {
            root.toFile().deleteRecursively()
        }
    }
    @Test
    fun `m45 project resolves three direct native package children`() {
        val root = createTempDirectory("athena-m45-package-catalog-")
        try {
            root.resolve("athena.yaml").writeText(
                """
                primaryPackage:
                  name: com.engineeringood.m45.rollingshutter
                  version: 0.1.0
                  sourceRoot: src
                dependencies:
                  - name: com.athena.iec
                    version: 1.0.0
                    source: local-package
                  - name: com.vendor.siemens
                    version: 1.0.0
                    source: local-package
                  - name: com.community.elements
                    version: 1.0.0
                    source: local-package
                """.trimIndent(),
            )
            root.resolve("src/com/engineeringood/m45/rollingshutter").createDirectories().resolve("main.athena").writeText("package com.engineeringood.m45.rollingshutter\nsystem main { }")
            listOf("com.athena.iec", "com.vendor.siemens", "com.community.elements").forEach { name ->
                val packageRoot = root.resolve("packages/$name").createDirectories()
                packageRoot.resolve("package.yaml").writeText("packageId:\n  name: $name\n  version: 1.0.0\n")
            }
            val result = AthenaCompiler().resolveRepositoryGraph(root)
            assertTrue(result.isValid, result.diagnostics.joinToString("\n") { it.message })
            assertEquals(
                listOf("com.engineeringood.m45.rollingshutter", "com.athena.iec", "com.community.elements", "com.vendor.siemens"),
                result.graph?.packages?.map { it.packageId.name },
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}
