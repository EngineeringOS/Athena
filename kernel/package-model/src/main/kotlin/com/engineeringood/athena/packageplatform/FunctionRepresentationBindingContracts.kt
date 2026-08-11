package com.engineeringood.athena.packageplatform

/** Source-owned representation selection for one Function in one projection/role. */
data class FunctionRepresentationBindingKey(
    val functionId: String,
    val projectionId: String,
    val bindingRole: String,
) {
    init {
        require(functionId.isNotBlank())
        require(projectionId.matches(IDENTIFIER))
        require(bindingRole.matches(IDENTIFIER))
    }

    val canonicalId: String = "$functionId#$projectionId#$bindingRole"
}

data class FunctionRepresentationBinding(
    val bindingId: String,
    val key: FunctionRepresentationBindingKey,
    val element: PackageItemIdentity,
    val variant: PackageItemIdentity? = null,
    val placeholderValues: Map<String, PackageItemValue> = emptyMap(),
    val compatibilityConstraints: Set<String> = emptySet(),
    val packageTrace: String = element.key,
    val provenance: PackageItemProvenance? = null,
) {
    init {
        require(bindingId.matches(IDENTIFIER))
        require(placeholderValues.keys.all { it.matches(IDENTIFIER) })
        require(compatibilityConstraints.all(String::isNotBlank))
        require(packageTrace.isNotBlank())
    }

    val canonicalPayload: String = buildString {
        append("athena-function-representation-binding-c14n-v1\n")
        append("bindingId=").append(bindingId).append('\n')
        append("functionId=").append(key.functionId).append('\n')
        append("projectionId=").append(key.projectionId).append('\n')
        append("bindingRole=").append(key.bindingRole).append('\n')
        append("element=").append(element.key).append('\n')
        variant?.let { append("variant=").append(it.key).append('\n') }
        placeholderValues.toSortedMap().forEach { (name, value) ->
            append("placeholder.").append(name).append('=').append(value.canonicalText()).append('\n')
        }
        compatibilityConstraints.sorted().forEach { append("constraint=").append(it).append('\n') }
        append("packageTrace=").append(packageTrace).append('\n')
    }
}

private fun PackageItemValue.canonicalText(): String = when (this) {
    is PackageItemValue.ObjectValue -> fields.toSortedMap().entries.joinToString(",", "{") { "${it.key}:${it.value.canonicalText()}" } + "}"
    is PackageItemValue.ListValue -> values.joinToString(",", "[") { it.canonicalText() } + "]"
    is PackageItemValue.DeclaredSetValue -> values.sorted().joinToString(",", "<") + ">"
    is PackageItemValue.TextValue -> "text:$value"
    is PackageItemValue.NumberValue -> "number:$canonicalText"
    is PackageItemValue.BooleanValue -> "boolean:$value"
}

private val IDENTIFIER = Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")
