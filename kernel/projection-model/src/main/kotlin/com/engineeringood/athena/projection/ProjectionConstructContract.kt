package com.engineeringood.athena.projection

/**
 * View-local identifier for one projection construct.
 *
 * Construct identity is stable within one projection view and remains separate from canonical
 * engineering identity.
 */
@JvmInline
value class ProjectionConstructId(val value: String) {
    override fun toString(): String = value
}

/**
 * Kernel-owned projection construct contract (M40).
 *
 * The kernel defines identity, source trace, membership, and validation shape only. It names no
 * electrical, mechanical, or process vocabulary; domain packages provide concrete
 * implementations exactly like M39 domain relation verbs.
 */
interface ProjectionConstruct {
    /** Stable construct identity inside one projection view. */
    val constructId: ProjectionConstructId

    /** Domain-provided construct kind (e.g., "rail", "rung" registered by a domain package). */
    val kind: String

    /** Source trace for the authored construct declaration. */
    val sourceTrace: String

    /** Member occurrence names grouped by this construct. */
    val memberNames: List<String>

    /** Plain-language validation issues; empty when the construct is well-formed. */
    fun validationIssues(): List<String>
}
