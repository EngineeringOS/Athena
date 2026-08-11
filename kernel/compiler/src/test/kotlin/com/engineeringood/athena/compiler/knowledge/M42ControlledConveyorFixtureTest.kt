package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseSuccess
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class M42ControlledConveyorFixtureTest {
    @Test
    fun `controlled conveyor package sources parse through shared language authority`() {
        var root = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (!Files.exists(root.resolve("examples/m42/controlled-conveyor")) && root.parent != null) root = root.parent
        root = root.resolve("examples/m42/controlled-conveyor")
        val files = Files.walk(root.resolve("packages")).use { stream -> stream.filter { it.toString().endsWith(".athena") }.sorted().toList() }
        assertTrue(files.size >= 4)
        files.forEach { file -> assertIs<ParseSuccess>(AthenaLanguageParser().parse(root.relativize(file).toString(), Files.readString(file))) }
    }
}
