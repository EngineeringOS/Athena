package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Proves the packaging boundary Story `1.2` introduces and Epic 2 preserves:
 * `com.engineeringood.athena.language` is the only public syntax contract package.
 *
 * The handwritten `com.engineeringood.athena.language.parser` package has been removed on the
 * ANTLR path (Story 2.2); the generated ANTLR lexer/parser and the internal `ParseAdapter` now
 * live in `com.engineeringood.athena.language.antlr` and remain implementation detail that no
 * other module may depend on (AD-105/AD-106).
 */
class LanguageFacadeBoundaryTest {
    @Test
    fun `public contract package exposes exactly the intended facade allow-list`() {
        val expectedPublicTypeNames = setOf(
            "AthenaLanguageParser",
            "LanguageModuleMarker",
            "SourcePosition",
            "SourceSpan",
            "SourceFileAst",
            "ImportDeclaration",
            "PackageDeclaration",
            "SystemDeclaration",
            "Declaration",
            "DeviceDeclaration",
            "PortDeclaration",
            "ConnectionDeclaration",
            "QualifiedName",
            "PropertyAssignment",
            "ScalarValue",
            "ParseResult",
            "ParseSuccess",
            "ParseFailure",
            "SyntaxDiagnostic",
        )

        val actualPublicTypeNames = topLevelClassSimpleNames(AthenaLanguageParser::class.java.`package`.name)

        assertEquals(expectedPublicTypeNames, actualPublicTypeNames)
    }

    @Test
    fun `parser internals live under the internal antlr package and are absent from the public package`() {
        assertEquals(
            "com.engineeringood.athena.language",
            AthenaLanguageParser::class.java.`package`.name,
        )

        // The generated ANTLR lexer/parser are addressable only from their internal package.
        assertEquals(
            "com.engineeringood.athena.language.antlr",
            Class.forName("com.engineeringood.athena.language.antlr.AthenaLexer").`package`.name,
        )
        assertEquals(
            "com.engineeringood.athena.language.antlr",
            Class.forName("com.engineeringood.athena.language.antlr.AthenaParser").`package`.name,
        )

        // Neither the generated ANTLR types nor the internal adapter leak into the public package.
        val publicPackageClasses = topLevelClassSimpleNames("com.engineeringood.athena.language")
        assertTrue("AthenaLexer" !in publicPackageClasses)
        assertTrue("AthenaParser" !in publicPackageClasses)
        assertTrue("AthenaAntlrParseEngine" !in publicPackageClasses)
        assertTrue("AthenaAntlrAstAdapter" !in publicPackageClasses)

        // The handwritten recursive-descent parser package no longer exists on the ANTLR path.
        listOf(
            "com.engineeringood.athena.language.parser.AthenaTokenizer",
            "com.engineeringood.athena.language.parser.AthenaParser",
            "com.engineeringood.athena.language.AthenaTokenizer",
            "com.engineeringood.athena.language.Token",
            "com.engineeringood.athena.language.TokenKind",
            "com.engineeringood.athena.language.ParseException",
        ).forEach { absentName ->
            assertFailsWith<ClassNotFoundException> {
                Class.forName(absentName)
            }
        }
    }

    private fun topLevelClassSimpleNames(packageName: String): Set<String> {
        val packageRelativePath = packageName.replace('.', '/')
        val codeSourceUrl = AthenaLanguageParser::class.java.protectionDomain.codeSource.location
        val classesRoot = Path.of(codeSourceUrl.toURI())
        val packageDirectory = classesRoot.resolve(packageRelativePath)

        assertTrue(Files.isDirectory(packageDirectory), "Expected compiled output directory at $packageDirectory")

        return Files.list(packageDirectory).use { entries ->
            entries
                .map { it.fileName.toString() }
                .filter { it.endsWith(".class") }
                .filter { '$' !in it }
                .map { it.removeSuffix(".class") }
                .toList()
                .toSet()
        }
    }
}
