package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.BindingDeclaration
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.packageplatform.FunctionRepresentationBinding
import com.engineeringood.athena.packageplatform.FunctionRepresentationBindingKey
import com.engineeringood.athena.packageplatform.PackageItemIdentity
import com.engineeringood.athena.packageplatform.PackageItemValue
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Path

/** Project-local representation choices; library assets remain package authority. */
internal fun AthenaLspSessionHostReady.representationBindingCompanionPath(): Path =
    sourcePath.resolveSibling(sourcePath.fileName.toString().removeSuffix(".athena") + ".binding.athena")

internal data class RepresentationBindingSource(
    val bindings: List<FunctionRepresentationBinding>,
)

internal class RepresentationBindingCompanionEditor(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun read(path: Path, source: String): RepresentationBindingSource {
        val parsed = parser.parse(path.toString(), source) as? ParseSuccess
            ?: throw IllegalArgumentException("Representation Binding Companion does not parse.")
        val unit = parsed.ast.unit as? RepresentationSourceUnit
            ?: throw IllegalArgumentException("Representation Binding Companion must contain binding declarations.")
        val bindings = unit.declarations.filterIsInstance<BindingDeclaration>()
        val representations = bindings.mapNotNull { binding ->
            if (binding.useElement == null) return@mapNotNull null
            val subject = binding.selectorFacts.singleOrNull { it.name == "subject" }
                ?.value
                ?.asText()
                ?: return@mapNotNull null
            val element = binding.useElement?.value ?: return@mapNotNull null
            val version = binding.useVersion?.value ?: return@mapNotNull null
            val bindingId = binding.bindingId?.value
                ?: throw IllegalArgumentException("Representation binding $binding requires id.")
            val projection = binding.projection?.value
                ?: throw IllegalArgumentException("Representation binding $binding requires projection.")
            val role = binding.implementationRole?.value
                ?: throw IllegalArgumentException("Representation binding $binding requires role.")
            val placeholderAssignments = binding.placeholderValues?.values.orEmpty()
            if (placeholderAssignments.map { it.name }.distinct().size != placeholderAssignments.size) {
                throw IllegalArgumentException("Representation binding `$bindingId` assigns one Placeholder more than once.")
            }
            FunctionRepresentationBinding(
                bindingId = bindingId,
                key = FunctionRepresentationBindingKey(subject, projection, role),
                element = elementReference(element, version),
                variant = binding.variant?.value?.let(::itemReference),
                placeholderValues = placeholderAssignments.associate { assignment ->
                    assignment.name to assignment.value.toPackageItemValue()
                },
            )
        }
        if (representations.map { it.key.canonicalId }.distinct().size != representations.size) {
            throw IllegalArgumentException("Representation Binding Companion declares Function/projection/role more than once.")
        }
        if (representations.map(FunctionRepresentationBinding::bindingId).distinct().size != representations.size) {
            throw IllegalArgumentException("Representation Binding Companion declares binding id more than once.")
        }
        return RepresentationBindingSource(representations.sortedBy { it.key.canonicalId })
    }

    fun changeSymbol(
        path: Path,
        source: String,
        subjectId: String,
        representationRef: String,
        variantRef: String? = null,
        placeholderValues: Map<String, PackageItemValue> = emptyMap(),
    ): String {
        val parsed = parser.parse(path.toString(), source) as? ParseSuccess
            ?: throw IllegalArgumentException("Representation Binding Companion does not parse.")
        val unit = parsed.ast.unit as? RepresentationSourceUnit
            ?: throw IllegalArgumentException("Representation Binding Companion must contain binding declarations.")
        val entityName = subjectId.removePrefix("entity:")
        val binding = unit.declarations.filterIsInstance<BindingDeclaration>().filter { candidate ->
            val subject = candidate.selectorFacts.singleOrNull { it.name == "subject" }?.value?.asText()
            candidate.useElement != null && (subject == subjectId || subject?.startsWith("function:$entityName.") == true)
        }.singleOrNull() ?: throw IllegalArgumentException("No declared Function representation binding exists for $subjectId.")
        val element = requireNotNull(binding.useElement) { "Representation binding $subjectId has no element." }
        val version = requireNotNull(binding.useVersion) { "Representation binding $subjectId has no version." }
        val separator = representationRef.lastIndexOf('@')
        require(separator > 0 && separator < representationRef.lastIndex) {
            "Representation reference must use element@version."
        }
        val newElement = representationRef.substring(0, separator)
        val newVersion = representationRef.substring(separator + 1)
        require(newElement.matches(Regex("^[a-z0-9][a-z0-9._/-]*$")) && newVersion.matches(Regex("^[A-Za-z0-9._-]+$"))) {
            "Representation reference contains unsupported characters."
        }
        val replacements = mutableListOf(
            element.span.start.offset..<(element.span.end.offset) to "\"$newElement\"",
            version.span.start.offset..<(version.span.end.offset) to "\"$newVersion\"",
        )
        val insertions = buildString {
            if (variantRef != null && binding.variant == null) append("  variant \"$variantRef\"\n")
            if (placeholderValues.isNotEmpty() && binding.placeholderValues == null) {
                append(placeholderBlock(placeholderValues, "  "))
            }
        }
        if (insertions.isNotEmpty()) {
            replacements += (binding.span.end.offset - 1)..<(binding.span.end.offset - 1) to insertions
        }

        binding.variant?.let { existing ->
            if (variantRef == null) {
                replacements += source.wholeLineRange(existing.span.start.offset, existing.span.end.offset) to ""
            } else {
                itemReference(variantRef)
                replacements += existing.span.start.offset..<existing.span.end.offset to "\"$variantRef\""
            }
        }
        if (variantRef != null) itemReference(variantRef)

        binding.placeholderValues?.let { existing ->
            replacements += if (placeholderValues.isEmpty()) {
                source.wholeLineRange(existing.span.start.offset, existing.span.end.offset) to ""
            } else {
                source.wholeLineRange(existing.span.start.offset, existing.span.end.offset) to
                    placeholderBlock(placeholderValues, "  ")
            }
        }
        return replacements.sortedByDescending { it.first.first }.fold(source) { updated, replacement ->
            updated.replaceRange(replacement.first, replacement.second)
        }.also { updated ->
            require(parser.parse(path.toString(), updated) is ParseSuccess) {
                "Updated Representation Binding Companion does not parse."
            }
        }
    }

    fun insertElement(path: Path, source: String, primaryPackageName: String, functionId: String, elementRef: String): String {
        val base = if (source.isBlank()) "package $primaryPackageName\n" else source
        val existing = if (source.isBlank()) emptyList() else {
            val parsed = parser.parse(path.toString(), base) as? ParseSuccess
                ?: throw IllegalArgumentException("Representation Binding Companion does not parse.")
            val unit = parsed.ast.unit as? RepresentationSourceUnit
                ?: throw IllegalArgumentException("Representation Binding Companion must contain binding declarations.")
            unit.declarations.filterIsInstance<BindingDeclaration>().mapNotNull { binding ->
                binding.selectorFacts.singleOrNull { it.name == "subject" }?.value?.asText()
            }
        }
        require(functionId !in existing) { "Function `$functionId` already has a representation binding." }
        val separator = elementRef.lastIndexOf('@')
        require(separator > 0 && separator < elementRef.lastIndex) { "Representation reference must use element@version." }
        val element = elementRef.substring(0, separator)
        val version = elementRef.substring(separator + 1)
        require(element.matches(Regex("^[a-z0-9][a-z0-9._/-]*$")) && version.matches(Regex("^[A-Za-z0-9._-]+$"))) {
            "Representation reference contains unsupported characters."
        }
        val block = """

binding ${bindingName(functionId)} {
  id "binding-${bindingIdSuffix(functionId)}"
  projection electrical
  role primary
  select function where { subject "$functionId" }
  use element "${element}" version "${version}"
}""".trimIndent()
        val updated = base.trimEnd() + "\n\n" + block + "\n"
        require(parser.parse(path.toString(), updated) is ParseSuccess) {
            "Generated Representation Binding Companion does not parse."
        }
        return updated
    }

    fun parseFailure(path: Path, source: String): ParseFailure? = parser.parse(path.toString(), source) as? ParseFailure

    private fun elementReference(reference: String, version: String): PackageItemIdentity {
        val separator = reference.indexOf('/')
        require(separator > 0 && separator < reference.lastIndex) { "Element reference must use package/item." }
        return PackageItemIdentity(
            packageId = PackageIdentifier(reference.substring(0, separator), version),
            itemId = reference.substring(separator + 1),
            itemVersion = version,
        )
    }

    private fun itemReference(reference: String): PackageItemIdentity {
        val versionSeparator = reference.lastIndexOf('@')
        require(versionSeparator > 0 && versionSeparator < reference.lastIndex) {
            "Package item reference must use package/item@version."
        }
        return elementReference(reference.substring(0, versionSeparator), reference.substring(versionSeparator + 1))
    }

    private fun bindingName(functionId: String): String = functionId
        .removePrefix("function:")
        .replace(Regex("[^A-Za-z0-9_]"), "_")

    private fun bindingIdSuffix(functionId: String): String = functionId
        .removePrefix("function:")
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

private fun placeholderBlock(values: Map<String, PackageItemValue>, indent: String = ""): String = buildString {
    append(indent).append("placeholders {\n")
    values.toSortedMap().forEach { (name, value) ->
        append(indent).append("  ").append(name).append(' ').append(value.render()).append('\n')
    }
    append(indent).append("}")
    if (indent.isNotEmpty()) append('\n')
}

private fun String.wholeLineRange(startOffset: Int, endOffset: Int): IntRange {
    val lineStart = lastIndexOf('\n', startOffset - 1).let { if (it < 0) 0 else it + 1 }
    val lineEnd = indexOf('\n', endOffset).let { if (it < 0) length else it + 1 }
    return lineStart..<lineEnd
}

private fun ScalarValue.asText(): String? = when (this) {
    is ScalarValue.Text -> text
    is ScalarValue.Symbol -> text
    else -> null
}

private fun ScalarValue.toPackageItemValue(): PackageItemValue = when (this) {
    is ScalarValue.Text -> PackageItemValue.TextValue(text)
    is ScalarValue.Symbol -> PackageItemValue.TextValue(text)
    is ScalarValue.Integer -> PackageItemValue.NumberValue(exactText)
    is ScalarValue.Boolean -> PackageItemValue.BooleanValue(value)
    is ScalarValue.Quantity -> throw IllegalArgumentException("Representation Placeholder values cannot carry engineering units.")
    is ScalarValue.Reference -> throw IllegalArgumentException("Representation Placeholder values cannot reference engineering subjects.")
}

private fun PackageItemValue.render(): String = when (this) {
    is PackageItemValue.TextValue -> "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
    is PackageItemValue.NumberValue -> canonicalText
    is PackageItemValue.BooleanValue -> value.toString()
    else -> throw IllegalArgumentException("Representation Placeholder edit values must be scalar.")
}
