package com.engineeringood.athena.plugin

/** Parsed core version value used for plugin compatibility comparisons during activation. */
data class AthenaCoreVersion(
    val text: String,
    private val numericSegments: List<Int>,
    private val qualifier: String?,
) : Comparable<AthenaCoreVersion> {
    override fun compareTo(other: AthenaCoreVersion): Int {
        val maxSegments = maxOf(numericSegments.size, other.numericSegments.size)
        for (index in 0 until maxSegments) {
            val comparison = numericSegments.getOrElse(index) { 0 }.compareTo(other.numericSegments.getOrElse(index) { 0 })
            if (comparison != 0) {
                return comparison
            }
        }

        return when {
            qualifier == other.qualifier -> 0
            qualifier == null -> 1
            other.qualifier == null -> -1
            else -> qualifier.compareTo(other.qualifier, ignoreCase = true)
        }
    }

    override fun toString(): String = text

    companion object {
        /** Parses one Athena core version string into a comparable value, or returns null when invalid. */
        fun parse(text: String): AthenaCoreVersion? {
            if (text.isBlank()) {
                return null
            }

            val numericPart = text.substringBefore('-')
            val qualifier = text.substringAfter('-', missingDelimiterValue = "").ifBlank { null }
            val numericSegments = numericPart.split('.')
            if (numericSegments.isEmpty() || numericSegments.any { segment -> segment.isBlank() || segment.any { !it.isDigit() } }) {
                return null
            }

            return AthenaCoreVersion(
                text = text,
                numericSegments = numericSegments.map(String::toInt),
                qualifier = qualifier,
            )
        }

        /** Returns the current core version surface for the M3 plugin compatibility contract. */
        fun current(): AthenaCoreVersion = parse(ATHENA_CURRENT_CORE_VERSION)
            ?: error("Invalid Athena core version constant `$ATHENA_CURRENT_CORE_VERSION`.")
    }
}

/** Core-owned runtime version surface used when evaluating plugin activation compatibility. */
data class AthenaCoreRuntime(
    val version: AthenaCoreVersion = AthenaCoreVersion.current(),
) {
    /** Convenience constructor for tests and callers that only have the raw version text. */
    constructor(version: String) : this(
        version = AthenaCoreVersion.parse(version)
            ?: throw IllegalArgumentException("Invalid Athena core runtime version `$version`."),
    )

    companion object {
        /** Returns the current runtime surface exposed by the core plugin API. */
        fun current(): AthenaCoreRuntime = AthenaCoreRuntime()
    }
}

private const val ATHENA_CURRENT_CORE_VERSION = "0.0.1-SNAPSHOT"
