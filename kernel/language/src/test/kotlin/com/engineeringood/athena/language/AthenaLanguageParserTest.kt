package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaLanguageParserTest {
    @Test
    fun `parses ordered package and symbol target imports with exact spans`() {
        val packageImport = "com.engineeringood.controls"
        val symbolImport = "com.engineeringood.controls.Switch"
        val source =
            """
            package com.engineeringood.root
            import $packageImport
            import $symbolImport
            system Demo {
              device PLC1 {
                type Switch
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("imports.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(2, success.ast.imports.size)
        val firstStart = source.indexOf("import $packageImport")
        val secondStart = source.indexOf("import $symbolImport")
        assertEquals(
            ImportDeclaration(
                target = QualifiedName(
                    parts = listOf("com", "engineeringood", "controls"),
                    span = SourceSpan(
                        SourcePosition(firstStart + 7, 2, 8),
                        SourcePosition(firstStart + 7 + packageImport.length, 2, 8 + packageImport.length),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(firstStart, 2, 1),
                    SourcePosition(firstStart + 7 + packageImport.length, 2, 8 + packageImport.length),
                ),
            ),
            success.ast.imports[0],
        )
        assertEquals(
            ImportDeclaration(
                target = QualifiedName(
                    parts = listOf("com", "engineeringood", "controls", "Switch"),
                    span = SourceSpan(
                        SourcePosition(secondStart + 7, 3, 8),
                        SourcePosition(secondStart + 7 + symbolImport.length, 3, 8 + symbolImport.length),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(secondStart, 3, 1),
                    SourcePosition(secondStart + 7 + symbolImport.length, 3, 8 + symbolImport.length),
                ),
            ),
            success.ast.imports[1],
        )
        assertEquals("PLC1", assertIs<DeviceDeclaration>(success.ast.declarations.single()).name)
    }

    @Test
    fun `parses a package-free hyphenated import with an exact file span`() {
        val target = "com.engineeringood.m18-controls.Switch2"
        val source = "import $target\nsystem Demo {}"

        val result = AthenaLanguageParser().parse("package-free-import.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(null, success.ast.packageDeclaration)
        assertEquals(
            ImportDeclaration(
                target = QualifiedName(
                    parts = listOf("com", "engineeringood", "m18-controls", "Switch2"),
                    span = SourceSpan(
                        SourcePosition(offset = 7, line = 1, column = 8),
                        SourcePosition(offset = 7 + target.length, line = 1, column = 8 + target.length),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(offset = 0, line = 1, column = 1),
                    SourcePosition(offset = 7 + target.length, line = 1, column = 8 + target.length),
                ),
            ),
            success.ast.imports.single(),
        )
        assertEquals(
            SourceSpan(
                SourcePosition(offset = 0, line = 1, column = 1),
                SourcePosition(offset = source.length, line = 2, column = 15),
            ),
            success.ast.span,
        )
    }

    @Test
    fun `parses package declaration into authored ast with exact spans`() {
        val packageName = "com.engineeringood.m18.factory-line"
        val source =
            """
            package $packageName
            system Demo {
              device PLC1 {
                type Switch
              }
              port PLC1.out {
                direction out
              }
              connect PLC1.out -> PLC1.out
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("package-demo.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertTrue(success.ast.imports.isEmpty())
        val packageNameEnd = 8 + packageName.length
        val packageDeclaration = assertNotNull(success.ast.packageDeclaration)
        assertEquals(
            PackageDeclaration(
                name = QualifiedName(
                    parts = listOf("com", "engineeringood", "m18", "factory-line"),
                    span = SourceSpan(
                        SourcePosition(offset = 8, line = 1, column = 9),
                        SourcePosition(offset = packageNameEnd, line = 1, column = packageNameEnd + 1),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(offset = 0, line = 1, column = 1),
                    SourcePosition(offset = packageNameEnd, line = 1, column = packageNameEnd + 1),
                ),
            ),
            packageDeclaration,
        )
        assertEquals(
            SourceSpan(
                SourcePosition(offset = 0, line = 1, column = 1),
                SourcePosition(offset = source.length, line = source.lines().size, column = 2),
            ),
            success.ast.span,
        )
        assertEquals(3, success.ast.declarations.size)
        assertEquals("PLC1", assertIs<DeviceDeclaration>(success.ast.declarations[0]).name)
        assertEquals(listOf("PLC1", "out"), assertIs<PortDeclaration>(success.ast.declarations[1]).qualifiedName.parts)
        val connection = assertIs<ConnectionDeclaration>(success.ast.declarations[2])
        assertEquals(listOf("PLC1", "out"), connection.from.parts)
        assertEquals(listOf("PLC1", "out"), connection.to.parts)
    }

    @Test
    fun `parses nested device owned ports as first class component anatomy`() {
        val source =
            """
            system Demo {
              device SpareTerminalXT99 {
                type Switch
                model "SPARE-XT"

                port in1 {
                  direction in
                  signal Digital
                }
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("nested-port.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val device = assertIs<DeviceDeclaration>(success.ast.declarations.single())
        assertEquals("SpareTerminalXT99", device.name)
        assertEquals(2, device.fields.size)
        val nestedPort = device.nestedPorts.single()
        assertEquals(listOf("SpareTerminalXT99", "in1"), nestedPort.qualifiedName.parts)
        assertEquals(2, nestedPort.fields.size)
        assertEquals("direction", nestedPort.fields[0].name)
        assertEquals("signal", nestedPort.fields[1].name)
        assertTrue(nestedPort.span.start.offset > device.span.start.offset)
        assertTrue(nestedPort.span.end.offset < device.span.end.offset)
    }

    @Test
    fun `parses a single segment package name`() {
        val result = AthenaLanguageParser().parse(
            "single-package.athena",
            """
            package controls
            system Demo {}
            """.trimIndent(),
        )

        val success = assertIs<ParseSuccess>(result)
        assertEquals(listOf("controls"), assertNotNull(success.ast.packageDeclaration).name.parts)
    }

    @Test
    fun `parses the demo cabinet example into a syntax-only ast`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val source = Files.readString(examplePath)

        val result = AthenaLanguageParser().parse(examplePath.toString(), source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(null, success.ast.packageDeclaration)
        val deviceOne = success.ast.declarations[0] as DeviceDeclaration
        val deviceTwo = success.ast.declarations[1] as DeviceDeclaration
        val portOne = success.ast.declarations[2] as PortDeclaration
        val portTwo = success.ast.declarations[3] as PortDeclaration
        val connection = success.ast.declarations[4] as ConnectionDeclaration
        assertEquals(
            SourceFileAst(
                system = SystemDeclaration("DemoCabinet", success.ast.system.span),
                declarations = listOf(
                    DeviceDeclaration(
                        name = "PLC1",
                        fields = listOf(
                            PropertyAssignment("type", ScalarValue.Identifier("Switch", deviceOne.fields[0].value.span), deviceOne.fields[0].span),
                            PropertyAssignment("model", ScalarValue.StringLiteral("S7-1200", deviceOne.fields[1].value.span), deviceOne.fields[1].span),
                        ),
                        span = deviceOne.span,
                    ),
                    DeviceDeclaration(
                        name = "M1",
                        fields = listOf(
                            PropertyAssignment("type", ScalarValue.Identifier("Motor", deviceTwo.fields[0].value.span), deviceTwo.fields[0].span),
                        ),
                        span = deviceTwo.span,
                    ),
                    PortDeclaration(
                        qualifiedName = QualifiedName(listOf("PLC1", "out"), portOne.qualifiedName.span),
                        fields = listOf(
                            PropertyAssignment("direction", ScalarValue.Identifier("out", portOne.fields[0].value.span), portOne.fields[0].span),
                            PropertyAssignment("signal", ScalarValue.Identifier("Digital", portOne.fields[1].value.span), portOne.fields[1].span),
                        ),
                        span = portOne.span,
                    ),
                    PortDeclaration(
                        qualifiedName = QualifiedName(listOf("M1", "in"), portTwo.qualifiedName.span),
                        fields = listOf(
                            PropertyAssignment("direction", ScalarValue.Identifier("in", portTwo.fields[0].value.span), portTwo.fields[0].span),
                            PropertyAssignment("signal", ScalarValue.Identifier("Digital", portTwo.fields[1].value.span), portTwo.fields[1].span),
                        ),
                        span = portTwo.span,
                    ),
                    ConnectionDeclaration(
                        from = QualifiedName(listOf("PLC1", "out"), connection.from.span),
                        to = QualifiedName(listOf("M1", "in"), connection.to.span),
                        span = connection.span,
                    ),
                ),
                span = success.ast.span,
            ),
            success.ast,
        )
    }

    @Test
    fun `parses m23 layout block into authored syntax-only ast`() {
        val source = Files.readString(resolveRepoRoot().resolve("examples/m23/parser-parity-proof/valid-layout-block.athena"))

        val result = AthenaLanguageParser().parse("valid-layout-block.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(2, success.ast.declarations.size)
        val layout = assertIs<LayoutDeclaration>(success.ast.declarations[1])
        assertEquals("schematic-sheet", layout.viewFamily)
        assertEquals(
            listOf(
                LayoutStatement.PlaceNear(subject = "HMI1", target = "PLC1", span = layout.statements[0].span),
                LayoutStatement.PlaceBelow(subject = "XT1", target = "PLC1", span = layout.statements[1].span),
                LayoutStatement.AlignWith(subject = "HMI1", target = "PLC1", axis = LayoutAxis.Vertical, span = layout.statements[2].span),
                LayoutStatement.AlignWith(subject = "HMI2", target = "PLC1", axis = LayoutAxis.Horizontal, span = layout.statements[3].span),
                LayoutStatement.GroupWith(subject = "HMI1", target = "PLC1", span = layout.statements[4].span),
            ),
            layout.statements,
        )
        assertTrue(layout.span.start.line < layout.span.end.line)
    }

    @Test
    fun `parses deterministically for identical source input`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                type PLC
                model "S7-1200"
              }
            
              port PLC1.out {
                direction out
                signal Digital
              }
            
              connect PLC1.out -> PLC1.out
            }
        """.trimIndent()

        val parser = AthenaLanguageParser()

        val first = parser.parse("demo.athena", source)
        val second = parser.parse("demo.athena", source)

        assertEquals(first, second)
    }

    @Test
    fun `reports syntax diagnostics with file line and column provenance`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                type PLC
              }
            
              connect PLC1.out M1.in
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("broken.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("broken.athena", failure.diagnostics.single().file)
        assertEquals(6, failure.diagnostics.single().line)
        assertTrue(failure.diagnostics.single().column > 0)
        assertTrue(failure.diagnostics.single().message.contains("->"))
    }

    @Test
    fun `reports a typed diagnostic for an unterminated string literal without crashing`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                model "S7-1200
              }
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("unterminated-string.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.isNotEmpty())
        val diagnostic = failure.diagnostics.first()
        assertEquals("unterminated-string.athena", diagnostic.file)
        assertTrue(diagnostic.line > 0, "Expected a real line, got ${diagnostic.line}")
        assertTrue(diagnostic.column > 0, "Expected a real column, got ${diagnostic.column}")
        assertTrue(diagnostic.message.isNotBlank())
    }

    @Test
    fun `reports a typed diagnostic for a missing closing brace without crashing`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                type Switch
        """.trimIndent()

        val result = AthenaLanguageParser().parse("missing-brace.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.isNotEmpty())
        val diagnostic = failure.diagnostics.first()
        assertEquals("missing-brace.athena", diagnostic.file)
        assertTrue(diagnostic.line > 0, "Expected a real line, got ${diagnostic.line}")
        assertTrue(diagnostic.column > 0, "Expected a real column, got ${diagnostic.column}")
        assertTrue(diagnostic.message.isNotBlank())
    }

    @Test
    fun `reports failures deterministically for identical malformed source input`() {
        val source = """
            system DemoCabinet {
              connect PLC1.out M1.in
            }
        """.trimIndent()

        val parser = AthenaLanguageParser()

        val first = parser.parse("broken.athena", source)
        val second = parser.parse("broken.athena", source)

        assertIs<ParseFailure>(first)
        assertEquals(first, second)
    }

    @Test
    fun `reports malformed package declarations as deterministic positioned diagnostics`() {
        val cases = listOf(
            MalformedPackageCase(
                name = "missing-name",
                source = """
                    package
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 8,
                expectedSpanStartOffset = 15,
                expectedSpanLength = 4,
            ),
            MalformedPackageCase(
                name = "duplicate",
                source = """
                    package com.one
                    package com.two
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 1,
                expectedSpanStartOffset = 16,
                expectedSpanLength = 7,
            ),
            MalformedPackageCase(
                name = "misplaced",
                source = """
                    system Demo {}
                    package com.one
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 1,
                expectedSpanStartOffset = 15,
                expectedSpanLength = 7,
            ),
            MalformedPackageCase(
                name = "malformed",
                source = """
                    package com..broken
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 13,
                expectedSpanStartOffset = 12,
                expectedSpanLength = 1,
            ),
            MalformedPackageCase(
                name = "leading-hyphen",
                source = """
                    package com.-broken
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 13,
                expectedSpanStartOffset = 12,
                expectedSpanLength = 1,
            ),
            MalformedPackageCase(
                name = "trailing-hyphen",
                source = """
                    package com.broken-
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 8,
                expectedSpanStartOffset = 27,
                expectedSpanLength = 4,
            ),
            MalformedPackageCase(
                name = "repeated-hyphen",
                source = """
                    package com.broken--name
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 20,
                expectedSpanStartOffset = 19,
                expectedSpanLength = 1,
            ),
            MalformedPackageCase(
                name = "spaced-hyphen",
                source = """
                    package com.factory - line
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 20,
                expectedSpanStartOffset = 19,
                expectedSpanLength = 1,
            ),
        )

        cases.forEach { case ->
            val file = "${case.name}.athena"
            val parser = AthenaLanguageParser()
            val first = parser.parse(file, case.source)
            val second = parser.parse(file, case.source)

            val failure = assertIs<ParseFailure>(first, "Expected ${case.name} to fail")
            assertEquals(first, second, "Expected ${case.name} failure to be deterministic")
            assertEquals(1, failure.diagnostics.size, "diagnostic count for ${case.name}")
            val diagnostic = failure.diagnostics.single()
            assertEquals(file, diagnostic.file)
            assertEquals(case.expectedErrorLine, diagnostic.line, "diagnostic line for ${case.name}")
            assertEquals(case.expectedErrorColumn, diagnostic.column, "diagnostic column for ${case.name}")
            assertTrue(diagnostic.message.isNotBlank() && diagnostic.message != "Syntax error", "diagnostic message for ${case.name}")
            assertEquals(
                SourceSpan(
                    start = SourcePosition(
                        offset = case.expectedSpanStartOffset,
                        line = case.expectedErrorLine,
                        column = case.expectedErrorColumn,
                    ),
                    end = SourcePosition(
                        offset = case.expectedSpanStartOffset + case.expectedSpanLength,
                        line = case.expectedErrorLine,
                        column = case.expectedErrorColumn + case.expectedSpanLength,
                    ),
                ),
                diagnostic.span,
                "diagnostic span for ${case.name}",
            )
        }
    }

    @Test
    fun `rejects unsupported and malformed import forms deterministically`() {
        val cases = listOf(
            malformedImportCase(
                name = "missing",
                source = "import\nsystem Demo {}",
                marker = "system",
                expectedMessageFragment = "import target",
            ),
            malformedImportCase(
                name = "next-line-target",
                source = "import\ncontrols\nsystem Demo {}",
                marker = "controls",
                expectedMessageFragment = "import target",
            ),
            malformedImportCase(
                name = "alias",
                source = "import com.engineeringood.controls as controls\nsystem Demo {}",
                marker = "as",
            ),
            malformedImportCase(
                name = "wildcard",
                source = "import com.engineeringood.controls.*\nsystem Demo {}",
                marker = "*",
            ),
            malformedImportCase(
                name = "misplaced",
                source = "system Demo {}\nimport com.engineeringood.controls",
                marker = "import",
            ),
            run {
                val source = "import com.controls\nimport com.controls\nsystem Demo {}"
                malformedImportCase(
                    name = "duplicate",
                    source = source,
                    marker = "import",
                    expectedOffset = source.lastIndexOf("import"),
                    expectedMessageFragment = "Duplicate import",
                )
            },
            malformedImportCase(
                name = "split-dot",
                source = "import com.engineeringood . controls\nsystem Demo {}",
                marker = " .",
                expectedSpanLength = 1,
            ),
        )

        cases.forEach { case ->
            val parser = AthenaLanguageParser()
            val file = "${case.name}.athena"
            val first = parser.parse(file, case.source)
            val second = parser.parse(file, case.source)
            val failure = assertIs<ParseFailure>(first, "Expected ${case.name} import syntax to fail")
            assertEquals(first, second, "Expected ${case.name} import failure to be deterministic")
            assertEquals(1, failure.diagnostics.size, "diagnostic count for ${case.name}")
            val diagnostic = failure.diagnostics.single()
            assertEquals(file, diagnostic.file)
            assertEquals(case.expectedLine, diagnostic.line, "diagnostic line for ${case.name}")
            assertEquals(case.expectedColumn, diagnostic.column, "diagnostic column for ${case.name}")
            assertTrue(diagnostic.message.isNotBlank())
            case.expectedMessageFragment?.let { assertTrue(diagnostic.message.contains(it)) }
            assertEquals(case.expectedSpan, diagnostic.span, "diagnostic span for ${case.name}")
        }
    }

    private fun malformedImportCase(
        name: String,
        source: String,
        marker: String,
        expectedOffset: Int = source.indexOf(marker),
        expectedSpanLength: Int = marker.length,
        expectedMessageFragment: String? = null,
    ): MalformedImportCase {
        val offset = expectedOffset
        check(offset >= 0) { "Marker '$marker' not found in malformed import case '$name'" }
        val beforeMarker = source.substring(0, offset)
        val line = beforeMarker.count { it == '\n' } + 1
        val column = offset - beforeMarker.lastIndexOf('\n')
        return MalformedImportCase(
            name = name,
            source = source,
            expectedLine = line,
            expectedColumn = column,
            expectedSpan = SourceSpan(
                SourcePosition(offset = offset, line = line, column = column),
                SourcePosition(offset = offset + expectedSpanLength, line = line, column = column + expectedSpanLength),
            ),
            expectedMessageFragment = expectedMessageFragment,
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root from ${Path.of("").toAbsolutePath().name}")
        return current
    }

    private data class MalformedPackageCase(
        val name: String,
        val source: String,
        val expectedErrorLine: Int,
        val expectedErrorColumn: Int,
        val expectedSpanStartOffset: Int,
        val expectedSpanLength: Int,
    )

    private data class MalformedImportCase(
        val name: String,
        val source: String,
        val expectedLine: Int,
        val expectedColumn: Int,
        val expectedSpan: SourceSpan,
        val expectedMessageFragment: String?,
    )
}
