package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.projection.ProjectionConstruct
import com.engineeringood.athena.projection.ProjectionConstructId

/**
 * Electrical package contribution of projection construct implementations (M40).
 *
 * The kernel owns only the [ProjectionConstruct] contract; this package provides the seven
 * electrical construct kinds, exactly like M39 domain relation verbs.
 */
object ElectricalProjectionConstructKinds {
    val supportedKinds: Set<String> = setOf(
        "power-rail",
        "rung",
        "branch",
        "wire-bundle",
        "terminal-strip",
        "contact-group",
        "coil-group",
    )

    fun implementationsFor(view: String, sheet: String): List<ProjectionConstruct> = supportedKinds.map { kind ->
        ElectricalProjectionConstruct(
            constructId = ProjectionConstructId("$view/$sheet/$kind"),
            kind = kind,
            sourceTrace = "electrical-package:$kind",
            memberNames = emptyList(),
        )
    }
}

/** Concrete contract implementation shared by all electrical construct kinds. */
data class ElectricalProjectionConstruct(
    override val constructId: ProjectionConstructId,
    override val kind: String,
    override val sourceTrace: String,
    override val memberNames: List<String>,
) : ProjectionConstruct {
    override fun validationIssues(): List<String> =
        if (memberNames.isEmpty()) {
            listOf("$kind construct has no members. Add at least one occurrence.")
        } else {
            emptyList()
        }
}
