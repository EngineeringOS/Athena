package com.engineeringood.athena.document

@JvmInline
value class DocumentProjectionPolicyDeterministicIdentity(val value: String) {
    init {
        require(value.isNotBlank()) { "Document projection policy deterministic identity must not be blank." }
    }

    override fun toString(): String = value
}

enum class DocumentProjectionArtifactAvailability {
    SUPPORTED,
    RESERVED,
}

enum class DocumentOccurrenceIdentityRecipeSegment {
    DOCUMENT_PROJECTION_ID,
    SHEET_VIEW_ID,
    CANONICAL_SUBJECT_ID,
    OCCURRENCE_ROLE,
    DETAIL_ROLE,
}

data class DocumentOccurrenceIdentityRecipe(
    val segments: List<DocumentOccurrenceIdentityRecipeSegment>,
) {
    init {
        require(segments.isNotEmpty()) { "Document occurrence identity recipe must not be empty." }
        require(segments.distinct().size == segments.size) {
            "Document occurrence identity recipe must not contain duplicate segments."
        }
    }
}

data class DocumentProjectionSheetViewRole(
    val role: SheetViewRole,
    val displayTitle: String,
    val order: Int,
) {
    init {
        require(displayTitle.isNotBlank()) { "Document projection sheet view role display title must not be blank." }
    }
}

data class DocumentProjectionArtifactSupport(
    val artifactKind: DocumentProjectionArtifactKind,
    val availability: DocumentProjectionArtifactAvailability,
)

data class DocumentProjectionPolicy(
    val policyId: DocumentProjectionPolicyId,
    val policyVersion: DocumentProjectionPolicyVersion,
    val supportedSheetViewRoles: List<DocumentProjectionSheetViewRole>,
    val supportedArtifactKinds: List<DocumentProjectionArtifactSupport>,
    val occurrenceIdentityRecipe: DocumentOccurrenceIdentityRecipe,
) {
    init {
        require(supportedSheetViewRoles.isNotEmpty()) {
            "Document projection policy must define at least one sheet view role."
        }
        require(supportedSheetViewRoles.map { role -> role.role }.distinct().size == supportedSheetViewRoles.size) {
            "Document projection policy must not contain duplicate sheet view roles."
        }
        require(supportedArtifactKinds.isNotEmpty()) {
            "Document projection policy must define at least one artifact kind."
        }
        require(supportedArtifactKinds.map { artifact -> artifact.artifactKind }.distinct().size == supportedArtifactKinds.size) {
            "Document projection policy must not contain duplicate artifact kinds."
        }
    }

    val deterministicIdentity: DocumentProjectionPolicyDeterministicIdentity
        get() = DocumentProjectionPolicyDeterministicIdentity(
            listOf(
                policyId.value,
                policyVersion.value,
                supportedSheetViewRoles
                    .sortedBy { role -> role.order }
                    .joinToString(separator = ";") { role ->
                        listOf(role.order.toString(), role.role.name, role.displayTitle).toStableKey()
                    },
                supportedArtifactKinds
                    .sortedBy { artifact -> artifact.artifactKind.name }
                    .joinToString(separator = ";") { artifact ->
                        listOf(artifact.artifactKind.name, artifact.availability.name).toStableKey()
                    },
                occurrenceIdentityRecipe.segments.joinToString(separator = ",") { segment -> segment.name },
            ).toStableKey(),
        )
}

object BuiltInDocumentProjectionPolicies {
    fun athenaDocumentProjectionV0(): DocumentProjectionPolicy = DocumentProjectionPolicy(
        policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
        policyVersion = DocumentProjectionPolicyVersion("0"),
        supportedSheetViewRoles = listOf(
            DocumentProjectionSheetViewRole(
                role = SheetViewRole.POWER_DISTRIBUTION,
                displayTitle = "Power Distribution",
                order = 0,
            ),
            DocumentProjectionSheetViewRole(
                role = SheetViewRole.CONTROL_AND_PLC_LOGIC,
                displayTitle = "Control And PLC Logic",
                order = 1,
            ),
            DocumentProjectionSheetViewRole(
                role = SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION,
                displayTitle = "Field Wiring And Terminal Transition",
                order = 2,
            ),
        ),
        supportedArtifactKinds = listOf(
            DocumentProjectionArtifactSupport(
                artifactKind = DocumentProjectionArtifactKind.SCHEMATIC_SHEET_VIEW,
                availability = DocumentProjectionArtifactAvailability.SUPPORTED,
            ),
            DocumentProjectionArtifactSupport(
                artifactKind = DocumentProjectionArtifactKind.TERMINAL_REPORT_RESERVED,
                availability = DocumentProjectionArtifactAvailability.RESERVED,
            ),
        ),
        occurrenceIdentityRecipe = DocumentOccurrenceIdentityRecipe(
            segments = listOf(
                DocumentOccurrenceIdentityRecipeSegment.DOCUMENT_PROJECTION_ID,
                DocumentOccurrenceIdentityRecipeSegment.SHEET_VIEW_ID,
                DocumentOccurrenceIdentityRecipeSegment.CANONICAL_SUBJECT_ID,
                DocumentOccurrenceIdentityRecipeSegment.OCCURRENCE_ROLE,
                DocumentOccurrenceIdentityRecipeSegment.DETAIL_ROLE,
            ),
        ),
    )
}
