package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.BindingDeclaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.ScalarValue
import java.nio.file.Path

/** Source-owned implementation selections share the existing binding companion authority. */
internal fun AthenaLspSessionHostReady.functionPartBindingCompanionPath(): Path =
    representationBindingCompanionPath()

internal class FunctionPartBindingCompanionEditor(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun validate(path: Path, source: String) {
        read(path, source)
    }

    fun upsert(
        path: Path,
        source: String,
        functionId: String,
        implementationRole: String,
        partRef: String,
    ): String {
        val separator = partRef.lastIndexOf('@')
        require(separator > 0 && separator < partRef.lastIndex) {
            "Part reference must use package/item@version."
        }
        val part = partRef.substring(0, separator)
        val version = partRef.substring(separator + 1)
        require(part.matches(Regex("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*/[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$"))) {
            "Part reference contains unsupported characters."
        }
        require(version.matches(Regex("^[A-Za-z0-9][A-Za-z0-9.+_-]*$"))) {
            "Part version contains unsupported characters."
        }
        val bindings = read(path, source)
        val existing = bindings.singleOrNull { it.functionId == functionId && it.implementationRole == implementationRole }
        val replacement = render(functionId, implementationRole, part, version)
        return when {
            existing == null -> listOf(source.trimEnd(), replacement).filter(String::isNotBlank).joinToString("\n\n", postfix = "\n")
            existing.part == "$part@$version" -> source
            else -> source.replaceRange(existing.span.start.offset, existing.span.end.offset, replacement)
        }
    }

    private fun read(path: Path, source: String): List<FunctionPartBindingSource> {
        if (source.isBlank()) return emptyList()
        val parsed = parser.parse(path.toString(), source) as? ParseSuccess
            ?: throw IllegalArgumentException("Function Part Binding Companion does not parse.")
        val unit = parsed.ast.unit as? RepresentationSourceUnit
            ?: throw IllegalArgumentException("Function Part Binding Companion must contain binding declarations.")
        val bindings = unit.declarations.filterIsInstance<BindingDeclaration>().mapNotNull { binding ->
            val functionId = binding.selectorFacts.singleOrNull { it.name == "subject" }?.value?.asText()
                ?: return@mapNotNull null
            val role = binding.implementationRole?.value ?: return@mapNotNull null
            val part = binding.usePart?.value ?: return@mapNotNull null
            val version = binding.partVersion?.value ?: return@mapNotNull null
            FunctionPartBindingSource(functionId, role, "$part@$version", binding.span)
        }
        require(bindings.map { it.functionId to it.implementationRole }.distinct().size == bindings.size) {
            "Function Part Binding Companion declares a Function role more than once."
        }
        return bindings
    }

    private fun render(functionId: String, role: String, part: String, version: String): String =
        """
        binding ${functionId.removePrefix("function:").replace('.', '_')}_${role} {
          select function where { subject "$functionId" }
          role $role
          use part "$part" version "$version"
        }
        """.trimIndent()
}

private data class FunctionPartBindingSource(
    val functionId: String,
    val implementationRole: String,
    val part: String,
    val span: com.engineeringood.athena.language.SourceSpan,
)

private fun ScalarValue.asText(): String? = when (this) {
    is ScalarValue.Text -> text
    is ScalarValue.Symbol -> text
    else -> null
}
