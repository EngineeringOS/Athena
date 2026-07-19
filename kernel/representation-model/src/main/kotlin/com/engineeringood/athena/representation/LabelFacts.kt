package com.engineeringood.athena.representation

data class SourceSpanRef(
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
) {
    init {
        require(startLine > 0) { "Source span start line must be positive." }
        require(startColumn > 0) { "Source span start column must be positive." }
        require(endLine >= startLine) { "Source span end line must not precede start line." }
        require(endColumn > 0) { "Source span end column must be positive." }
    }
}

data class PresentationSourceIdentity(
    val sourceUnit: String,
    val span: SourceSpanRef,
) {
    init {
        require(sourceUnit.isNotBlank()) { "Presentation source unit must not be blank." }
    }
}

data class LabelFact(
    val labelId: LabelFactId,
    val subjectId: RepresentationSubjectId,
    val occurrenceId: RepresentationOccurrenceId,
    val role: PresentationLabelRole,
    val value: LabelValue,
    val anchor: PresentationLabelAnchor,
    val sourceIdentity: PresentationSourceIdentity? = null,
) {
    init {
        require(anchor.role == role) { "Label fact role must match anchor role." }
    }

    val rendererTextAuthority: Boolean
        get() = false
}

data class LabelPolicy(
    val anchorsByRole: Map<PresentationLabelRole, PresentationPoint>,
) {
    init {
        require(anchorsByRole.isNotEmpty()) { "Label policy requires at least one role anchor." }
    }

    fun anchorFor(
        role: PresentationLabelRole,
        subjectId: RepresentationSubjectId,
        occurrenceId: RepresentationOccurrenceId,
    ): PresentationLabelAnchor {
        val point = anchorsByRole[role] ?: error("No label anchor for role `$role`.")
        return PresentationLabelAnchor(
            anchorId = PresentationLabelAnchorId("${occurrenceId.value}:${subjectId.value}:${role.name.lowercase()}"),
            role = role,
            point = point,
        )
    }

    companion object {
        fun defaultIndustrialControl(): LabelPolicy = LabelPolicy(
            anchorsByRole = linkedMapOf(
                PresentationLabelRole.DEVICE_TAG to PresentationPoint(GridUnit(0), GridUnit(-12)),
                PresentationLabelRole.COMPONENT_LABEL to PresentationPoint(GridUnit(0), GridUnit(60)),
                PresentationLabelRole.TERMINAL_LABEL to PresentationPoint(GridUnit(12), GridUnit(0)),
                PresentationLabelRole.ROUTE_LABEL to PresentationPoint(GridUnit(24), GridUnit(-8)),
            ),
        )
    }
}
