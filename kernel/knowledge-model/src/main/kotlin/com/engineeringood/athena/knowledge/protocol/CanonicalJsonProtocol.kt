package com.engineeringood.athena.knowledge.protocol

import com.engineeringood.athena.knowledge.EngineeringKnowledgeDocument
import com.engineeringood.athena.knowledge.KnowledgeDefinition
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

enum class AthenaSchema(val id: String, val version: Int) {
    KNOWLEDGE("https://athena.dev/schema/knowledge-document.json", 1),
    VALIDATION("https://athena.dev/schema/validation-document.json", 1),
}

object AthenaSchemaVersionGuard {
    fun requireSupported(schema: AthenaSchema, version: Int) {
        require(version == schema.version) { "Unsupported ${schema.name.lowercase()} schemaVersion $version; expected ${schema.version}." }
    }
}

data class CanonicalDocumentBytes(val schema: AthenaSchema, val bytes: ByteArray) {
    val digest: String get() = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

/** Small dependency-free canonical JSON writer. Objects use unsigned UTF-16 key order. */
object CanonicalJsonProtocol {
    fun knowledge(document: EngineeringKnowledgeDocument): CanonicalDocumentBytes =
        encode(AthenaSchema.KNOWLEDGE, mapOf(
            "\$schema" to AthenaSchema.KNOWLEDGE.id,
            "schemaVersion" to AthenaSchema.KNOWLEDGE.version,
            "packages" to document.packages.map { mapOf("name" to it.name, "version" to it.version) },
            "definitions" to document.definitions.sortedBy { it.id }.map { definition ->
                mapOf("id" to definition.id.toString(), "kind" to kind(definition), "source" to definition.provenance.file)
            },
        ))

    private fun kind(definition: KnowledgeDefinition) = when (definition) {
        is KnowledgeDefinition.Concept -> "concept"; is KnowledgeDefinition.Part -> "part"; is KnowledgeDefinition.Capability -> "capability"
        is KnowledgeDefinition.Relationship -> "relationship"; is KnowledgeDefinition.Flow -> "flow"; is KnowledgeDefinition.Dimension -> "dimension"
        is KnowledgeDefinition.Unit -> "unit"; is KnowledgeDefinition.Formula -> "formula"; is KnowledgeDefinition.Constraint -> "constraint"
    }

    private fun encode(schema: AthenaSchema, value: Any?): CanonicalDocumentBytes =
        CanonicalDocumentBytes(schema, write(value).toByteArray(StandardCharsets.UTF_8))

    private fun write(value: Any?): String = when (value) {
        null -> "null"
        is String -> quote(value)
        is Number, is Boolean -> value.toString()
        is Map<*, *> -> value.entries.sortedWith { left, right -> compareUnsignedUtf16(left.key.toString(), right.key.toString()) }.joinToString("", "{", "}") { "${quote(it.key.toString())}:${write(it.value)}" }
        is Iterable<*> -> value.joinToString("", "[", "]") { write(it) }
        else -> error("Unsupported canonical JSON value: ${value::class.qualifiedName}")
    }

    private fun quote(value: String): String {
        val out = StringBuilder("\"")
        var index = 0
        while (index < value.length) {
            val c = value[index]
            if (Character.isHighSurrogate(c)) {
                require(index + 1 < value.length && Character.isLowSurrogate(value[index + 1])) { "Unpaired surrogate is not allowed" }
                out.append(c).append(value[index + 1]); index += 2; continue
            }
            require(!Character.isLowSurrogate(c)) { "Unpaired surrogate is not allowed" }
            when (c) {
            '"' -> out.append("\\\""); '\\' -> out.append("\\\\"); '\b' -> out.append("\\b"); '\t' -> out.append("\\t"); '\n' -> out.append("\\n"); '\u000C' -> out.append("\\f"); '\r' -> out.append("\\r")
            in '\u0000'..'\u001F' -> out.append("\\u%04x".format(c.code)); else -> out.append(c)
            }
            index++
        }
        return out.append('"').toString()
    }

    private fun compareUnsignedUtf16(left: String, right: String): Int {
        val length = minOf(left.length, right.length)
        for (index in 0 until length) {
            val order = left[index].code.compareTo(right[index].code)
            if (order != 0) return order
        }
        return left.length.compareTo(right.length)
    }
}
