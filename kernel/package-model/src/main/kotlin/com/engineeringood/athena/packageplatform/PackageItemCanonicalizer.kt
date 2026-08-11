package com.engineeringood.athena.packageplatform

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.Normalizer

object PackageItemCanonicalizer {
    const val SCHEMA = "athena-package-item-c14n-v1"

    fun canonicalBytes(metadata: PackageItemMetadata, payload: PackageItemValue.ObjectValue): ByteArray {
        val value = PackageItemValue.ObjectValue(
            mapOf(
                "schema" to PackageItemValue.TextValue(SCHEMA),
                "identity" to PackageItemValue.ObjectValue(
                    mapOf(
                        "packageName" to PackageItemValue.TextValue(metadata.identity.packageId.name),
                        "packageVersion" to PackageItemValue.TextValue(metadata.identity.packageId.version!!),
                        "itemId" to PackageItemValue.TextValue(metadata.identity.itemId),
                        "itemVersion" to PackageItemValue.TextValue(metadata.identity.itemVersion),
                    ),
                ),
                "kind" to PackageItemValue.TextValue(metadata.kind.name),
                "provenance" to PackageItemValue.ObjectValue(
                    mapOf(
                        "source" to PackageItemValue.TextValue(normalizePath(metadata.provenance.source)),
                        "sourceDigest" to PackageItemValue.TextValue(metadata.provenance.sourceDigest),
                        "license" to PackageItemValue.TextValue(metadata.provenance.license),
                    ),
                ),
                "payloadReference" to PackageItemValue.TextValue(normalizePath(metadata.payloadReference)),
                "payload" to payload,
            ),
        )
        return render(value).toByteArray(StandardCharsets.UTF_8)
    }

    fun digest(metadata: PackageItemMetadata, payload: PackageItemValue.ObjectValue): String =
        MessageDigest.getInstance("SHA-256").digest(canonicalBytes(metadata, payload)).toHex()

    fun normalize(payload: PackageItemValue.ObjectValue): PackageItemValue.ObjectValue =
        normalizeValue(payload) as PackageItemValue.ObjectValue

    private fun normalizeValue(value: PackageItemValue): PackageItemValue = when (value) {
        is PackageItemValue.ObjectValue -> PackageItemValue.ObjectValue(
            value.fields.toSortedMap().mapValues { (_, child) -> normalizeValue(child) },
        )
        is PackageItemValue.ListValue -> PackageItemValue.ListValue(value.values.map(::normalizeValue))
        is PackageItemValue.DeclaredSetValue -> PackageItemValue.DeclaredSetValue(value.values.map(::nfc).toSortedSet())
        is PackageItemValue.TextValue -> PackageItemValue.TextValue(nfc(value.value))
        is PackageItemValue.NumberValue -> PackageItemValue.NumberValue(canonicalNumber(value.canonicalText))
        is PackageItemValue.BooleanValue -> value
    }

    private fun render(value: PackageItemValue): String = when (val normalized = normalizeValue(value)) {
        is PackageItemValue.ObjectValue -> normalized.fields.toSortedMap().entries.joinToString(",", "{", "}") { (key, child) -> "${quote(key)}:${render(child)}" }
        is PackageItemValue.ListValue -> normalized.values.joinToString(",", "[", "]", transform = ::render)
        is PackageItemValue.DeclaredSetValue -> normalized.values.sorted().joinToString(",", "[", "]") { quote(it) }
        is PackageItemValue.TextValue -> quote(normalized.value)
        is PackageItemValue.NumberValue -> normalized.canonicalText
        is PackageItemValue.BooleanValue -> normalized.value.toString()
    }

    private fun quote(value: String): String = buildString {
        append('"')
        nfc(value).forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) append("\\u%04x".format(character.code)) else append(character)
            }
        }
        append('"')
    }

    private fun canonicalNumber(value: String): String = value.toBigDecimal().stripTrailingZeros().toPlainString()
    private fun normalizePath(path: String): String = nfc(path.replace('\\', '/'))
    private fun nfc(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFC)
    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
