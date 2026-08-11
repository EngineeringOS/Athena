package com.engineeringood.athena.packageplatform

enum class PlaceholderValueType { TEXT, NUMBER, BOOLEAN }

data class RepresentationPlaceholder(
    val name: String,
    val targetPath: String,
    val valueType: PlaceholderValueType,
) {
    init {
        require(name.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")))
        require(targetPath.matches(Regex("^(label|style|property)(\\.[a-z][a-z0-9_-]*)*$")))
    }
}

data class PlaceholderSchema(val placeholders: List<RepresentationPlaceholder>) {
    init { require(placeholders.map(RepresentationPlaceholder::name).distinct().size == placeholders.size) }
}

data class PlaceholderAssignment(val name: String, val value: PackageItemValue) {
    init { require(name.isNotBlank()) }
}

