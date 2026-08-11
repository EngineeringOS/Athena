package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.knowledge.KnowledgeCompilationResult
import com.engineeringood.athena.knowledge.KnowledgePackageIdentity
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseSuccess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KnowledgeDocumentCompilerTest {
    private val parser = AthenaLanguageParser()

    @Test
    fun `compilation is deterministic across package source order`() {
        val first = source("packages/z.athena", "zeta", "concept motor { kind rotating }")
        val second = source("packages/a.athena", "alpha", "unit ampere dimension current scale 1")
        val compiler = KnowledgeDocumentCompiler()
        val one = assertIs<KnowledgeCompilationResult.Success>(compiler.compile(listOf(first, second)))
        val two = assertIs<KnowledgeCompilationResult.Success>(compiler.compile(listOf(second, first)))
        assertEquals(one.document.canonicalText(), two.document.canonicalText())
        assertEquals(listOf("alpha", "zeta"), one.document.packages.map { it.name })
    }

    @Test
    fun `duplicate identity fails closed without document`() {
        val first = source("a.athena", "alpha", "concept motor { kind rotating }")
        val second = source("b.athena", "alpha", "concept motor { kind rotating }")
        val result = assertIs<KnowledgeCompilationResult.Failure>(KnowledgeDocumentCompiler().compile(listOf(first, second)))
        assertEquals("knowledge.definition.duplicate", result.diagnostics.single().code)
    }

    private fun source(path: String, packageName: String, declaration: String): KnowledgePackageSource {
        val parsed = assertIs<ParseSuccess>(parser.parse(path, "domain electrical { $declaration }"))
        return KnowledgePackageSource(KnowledgePackageIdentity(packageName, "1.0.0"), path, parsed.ast)
    }
}
