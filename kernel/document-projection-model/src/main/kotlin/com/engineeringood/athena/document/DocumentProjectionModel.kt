package com.engineeringood.athena.document

import com.engineeringood.athena.ir.StableSemanticIdentity

@JvmInline
value class DocumentProjectionPolicyId(val value: String) {
    init {
        require(value.isNotBlank()) { "Document projection policy id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class DocumentProjectionPolicyVersion(val value: String) {
    init {
        require(value.isNotBlank()) { "Document projection policy version must not be blank." }
    }

    override fun toString(): String = value
}

data class DocumentProjectionId(
    val policyId: DocumentProjectionPolicyId,
    val policyVersion: DocumentProjectionPolicyVersion,
    val semanticGraphId: String,
) {
    init {
        require(semanticGraphId.isNotBlank()) { "Document projection semantic graph id must not be blank." }
    }

    val value: String
        get() = stableDocumentProjectionKey(policyId.value, policyVersion.value, semanticGraphId)

    override fun toString(): String = value
}

@JvmInline
value class SheetViewId(val value: String) {
    init {
        require(value.isNotBlank()) { "Sheet view id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class LogicalZoneId(val value: String) {
    init {
        require(value.isNotBlank()) { "Logical zone id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class DocumentOccurrenceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Document occurrence id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class ContinuationFactId(val value: String) {
    init {
        require(value.isNotBlank()) { "Continuation fact id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class CrossReferenceFactId(val value: String) {
    init {
        require(value.isNotBlank()) { "Cross-reference fact id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class DocumentProjectionDiagnosticCode(val value: String) {
    init {
        require(value.isNotBlank()) { "Document projection diagnostic code must not be blank." }
    }

    override fun toString(): String = value
}

enum class DocumentProjectionArtifactKind {
    SCHEMATIC_SHEET_VIEW,
    TERMINAL_REPORT_RESERVED,
}

enum class SheetViewRole {
    POWER_DISTRIBUTION,
    CONTROL_AND_PLC_LOGIC,
    FIELD_WIRING_AND_TERMINAL_TRANSITION,
}

enum class DocumentOccurrenceRole {
    COMPONENT,
    TERMINAL,
    ROUTE,
    LABEL,
    SHEET_VIEW,
}

enum class DocumentOccurrenceDetailRole {
    REPRESENTATION,
    TERMINAL,
    ROUTE,
    LABEL,
    VIEW_CONTAINER,
}

enum class CrossReferenceRelationType {
    REPEATED_SUBJECT,
    TERMINAL_CONTINUATION,
    ROUTE_CONTINUATION,
}

enum class DocumentProjectionDiagnosticSeverity {
    INFO,
    WARNING,
    ERROR,
}

enum class DocumentProjectionDiagnosticProvenanceKind {
    SOURCE,
    PROJECTION_POLICY,
    DERIVED_VIEW,
}

data class DocumentSourceRange(
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
) {
    init {
        require(startLine > 0) { "Document source range start line must be positive." }
        require(startColumn > 0) { "Document source range start column must be positive." }
        require(endLine >= startLine) { "Document source range end line must not precede start line." }
        require(endColumn > 0) { "Document source range end column must be positive." }
        require(endLine > startLine || endColumn >= startColumn) {
            "Document source range end column must not precede start column on the same line."
        }
    }
}

data class DocumentProjectionProvenance(
    val sourceUnitId: String,
    val sourceRange: DocumentSourceRange? = null,
) {
    init {
        require(sourceUnitId.isNotBlank()) { "Document projection provenance source unit id must not be blank." }
    }
}

data class LogicalZone(
    val zoneId: LogicalZoneId,
    val label: String,
    val order: Int,
) {
    init {
        require(label.isNotBlank()) { "Logical zone label must not be blank." }
    }
}

data class SheetView(
    val sheetViewId: SheetViewId,
    val role: SheetViewRole,
    val title: String,
    val order: Int,
    val artifactKind: DocumentProjectionArtifactKind = DocumentProjectionArtifactKind.SCHEMATIC_SHEET_VIEW,
    val zones: List<LogicalZone> = emptyList(),
) {
    init {
        require(title.isNotBlank()) { "Sheet view title must not be blank." }
    }
}

data class DocumentLocation(
    val sheetViewId: SheetViewId,
    val zoneId: LogicalZoneId,
    val displayNotation: String,
) {
    init {
        require(displayNotation.isNotBlank()) { "Document location display notation must not be blank." }
    }
}

data class DocumentOccurrence(
    val occurrenceId: DocumentOccurrenceId,
    val documentProjectionId: DocumentProjectionId,
    val sheetViewId: SheetViewId,
    val canonicalSubjectId: StableSemanticIdentity,
    val occurrenceRole: DocumentOccurrenceRole,
    val detailRole: DocumentOccurrenceDetailRole,
    val source: DocumentProjectionProvenance? = null,
) {
    init {
        require(canonicalSubjectId.value.isNotBlank()) { "Document occurrence canonical subject id must not be blank." }
        require(occurrenceId == identityOf(documentProjectionId, sheetViewId, canonicalSubjectId, occurrenceRole, detailRole)) {
            "Document occurrence id must match the occurrence identity recipe."
        }
        require(detailRole in occurrenceRole.allowedDetailRoles) {
            "Document occurrence detail role $detailRole is not valid for occurrence role $occurrenceRole."
        }
    }

    val stableKey: String
        get() = listOf(
            documentProjectionId.value,
            sheetViewId.value,
            canonicalSubjectId.value,
            occurrenceRole.name,
            detailRole.name,
        ).toStableKey()

    companion object {
        fun identityOf(
            documentProjectionId: DocumentProjectionId,
            sheetViewId: SheetViewId,
            canonicalSubjectId: StableSemanticIdentity,
            occurrenceRole: DocumentOccurrenceRole,
            detailRole: DocumentOccurrenceDetailRole,
        ): DocumentOccurrenceId = DocumentOccurrenceId(
            listOf(
                documentProjectionId.value,
                sheetViewId.value,
                canonicalSubjectId.value,
                occurrenceRole.name,
                detailRole.name,
            ).toStableKey(),
        )
    }
}

data class DocumentOccurrenceIndexEntry(
    val occurrence: DocumentOccurrence,
    val location: DocumentLocation,
) {
    init {
        require(occurrence.sheetViewId == location.sheetViewId) {
            "Document occurrence index entry location must belong to the occurrence sheet view."
        }
    }

    val stableKey: String
        get() = listOf(
            occurrence.stableKey,
            occurrence.occurrenceId.value,
            location.zoneId.value,
            location.displayNotation,
        ).toStableKey()
}

data class ContinuationFact(
    val continuationFactId: ContinuationFactId,
    val routeIdentity: StableSemanticIdentity,
    val sourceRouteOccurrenceId: DocumentOccurrenceId,
    val targetRouteOccurrenceId: DocumentOccurrenceId,
    val sourceDocumentLocation: DocumentLocation,
    val targetDocumentLocation: DocumentLocation,
    val sourceTerminalIdentity: StableSemanticIdentity? = null,
    val targetTerminalIdentity: StableSemanticIdentity? = null,
    val displayNotation: String,
    val provenance: DocumentProjectionProvenance? = null,
) {
    init {
        require(routeIdentity.value.isNotBlank()) { "Continuation route identity must not be blank." }
        require(sourceRouteOccurrenceId != targetRouteOccurrenceId) {
            "Continuation source and target route occurrence ids must differ."
        }
        require(displayNotation.isNotBlank()) { "Continuation display notation must not be blank." }
    }

    companion object {
        fun identityOf(
            routeIdentity: StableSemanticIdentity,
            sourceRouteOccurrenceId: DocumentOccurrenceId,
            targetRouteOccurrenceId: DocumentOccurrenceId,
        ): ContinuationFactId = ContinuationFactId(
            listOf(
                routeIdentity.value,
                sourceRouteOccurrenceId.value,
                targetRouteOccurrenceId.value,
            ).toStableKey(),
        )
    }
}

data class CrossReferenceFact(
    val crossReferenceFactId: CrossReferenceFactId,
    val sourceIdentity: StableSemanticIdentity,
    val targetIdentity: StableSemanticIdentity,
    val sourceOccurrenceId: DocumentOccurrenceId,
    val targetOccurrenceId: DocumentOccurrenceId,
    val relationType: CrossReferenceRelationType,
    val sourceDocumentLocation: DocumentLocation,
    val targetDocumentLocation: DocumentLocation,
    val displayNotation: String,
    val provenance: DocumentProjectionProvenance? = null,
) {
    init {
        require(sourceIdentity.value.isNotBlank()) { "Cross-reference source identity must not be blank." }
        require(targetIdentity.value.isNotBlank()) { "Cross-reference target identity must not be blank." }
        require(sourceOccurrenceId != targetOccurrenceId) {
            "Cross-reference source and target occurrence ids must differ."
        }
        require(displayNotation.isNotBlank()) { "Cross-reference display notation must not be blank." }
    }

    companion object {
        fun identityOf(
            sourceIdentity: StableSemanticIdentity,
            targetIdentity: StableSemanticIdentity,
            sourceOccurrenceId: DocumentOccurrenceId,
            targetOccurrenceId: DocumentOccurrenceId,
            relationType: CrossReferenceRelationType,
        ): CrossReferenceFactId = CrossReferenceFactId(
            listOf(
                relationType.name,
                sourceIdentity.value,
                targetIdentity.value,
                sourceOccurrenceId.value,
                targetOccurrenceId.value,
            ).toStableKey(),
        )
    }
}

data class DocumentProjectionDiagnosticProvenance(
    val kind: DocumentProjectionDiagnosticProvenanceKind,
    val source: DocumentProjectionProvenance? = null,
    val policyId: DocumentProjectionPolicyId? = null,
    val sheetViewId: SheetViewId? = null,
) {
    init {
        require(kind != DocumentProjectionDiagnosticProvenanceKind.SOURCE || source != null) {
            "Source diagnostics must include source provenance."
        }
    }
}

data class DocumentProjectionDiagnostic(
    val severity: DocumentProjectionDiagnosticSeverity,
    val code: DocumentProjectionDiagnosticCode,
    val relationType: CrossReferenceRelationType?,
    val affectedIdentity: StableSemanticIdentity,
    val message: String,
    val provenance: DocumentProjectionDiagnosticProvenance,
    val canPublishToProblems: Boolean,
) {
    init {
        require(affectedIdentity.value.isNotBlank()) { "Document projection diagnostic identity must not be blank." }
        require(message.isNotBlank()) { "Document projection diagnostic message must not be blank." }
        require(!canPublishToProblems || provenance.kind == DocumentProjectionDiagnosticProvenanceKind.SOURCE) {
            "Only source-backed document projection diagnostics may publish to Problems."
        }
    }
}

class DocumentOccurrenceIndex private constructor(
    val entries: List<DocumentOccurrenceIndexEntry>,
) {
    fun forSubject(subjectId: StableSemanticIdentity): List<DocumentOccurrenceIndexEntry> =
        entries.filter { entry -> entry.occurrence.canonicalSubjectId == subjectId }

    fun forSheetView(sheetViewId: SheetViewId): List<DocumentOccurrenceIndexEntry> =
        entries.filter { entry -> entry.occurrence.sheetViewId == sheetViewId }

    companion object {
        fun canonical(entries: List<DocumentOccurrenceIndexEntry>): DocumentOccurrenceIndex {
            val projectionIds = entries.map { entry -> entry.occurrence.documentProjectionId }.distinct()
            require(projectionIds.size <= 1) {
                "Document occurrence index entries must belong to a single document projection."
            }

            val duplicateOccurrenceIds = entries
                .groupBy { entry -> entry.occurrence.occurrenceId }
                .filterValues { matches -> matches.size > 1 }
                .keys
            require(duplicateOccurrenceIds.isEmpty()) {
                "Document occurrence index must not contain duplicate occurrence ids: $duplicateOccurrenceIds"
            }

            val duplicateStableKeys = entries
                .groupBy(DocumentOccurrenceIndexEntry::stableKey)
                .filterValues { matches -> matches.size > 1 }
                .keys
            require(duplicateStableKeys.isEmpty()) {
                "Document occurrence index must not contain duplicate stable keys: $duplicateStableKeys"
            }

            return DocumentOccurrenceIndex(entries.sortedWith(documentOccurrenceEntryComparator))
        }
    }
}

private val DocumentOccurrenceRole.allowedDetailRoles: Set<DocumentOccurrenceDetailRole>
    get() = when (this) {
        DocumentOccurrenceRole.COMPONENT -> setOf(DocumentOccurrenceDetailRole.REPRESENTATION)
        DocumentOccurrenceRole.TERMINAL -> setOf(DocumentOccurrenceDetailRole.TERMINAL)
        DocumentOccurrenceRole.ROUTE -> setOf(DocumentOccurrenceDetailRole.ROUTE)
        DocumentOccurrenceRole.LABEL -> setOf(DocumentOccurrenceDetailRole.LABEL)
        DocumentOccurrenceRole.SHEET_VIEW -> setOf(DocumentOccurrenceDetailRole.VIEW_CONTAINER)
    }

private val documentOccurrenceEntryComparator = compareBy<DocumentOccurrenceIndexEntry>(
    { entry -> entry.occurrence.documentProjectionId.value },
    { entry -> entry.occurrence.sheetViewId.value },
    { entry -> entry.occurrence.canonicalSubjectId.value },
    { entry -> entry.occurrence.occurrenceRole.name },
    { entry -> entry.occurrence.detailRole.name },
    { entry -> entry.location.zoneId.value },
    { entry -> entry.location.displayNotation },
)

internal fun stableDocumentProjectionKey(
    policyId: String,
    policyVersion: String,
    semanticGraphId: String,
): String = listOf(policyId, policyVersion, semanticGraphId).toStableKey()

internal fun List<String>.toStableKey(): String =
    joinToString(separator = "|") { value -> "${value.length}:$value" }
