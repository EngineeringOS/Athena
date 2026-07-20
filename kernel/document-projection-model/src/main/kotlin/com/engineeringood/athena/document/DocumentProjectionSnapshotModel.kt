package com.engineeringood.athena.document

import com.engineeringood.athena.ir.StableSemanticIdentity
import java.util.Locale

data class DocumentProjectionSourceUnitSummary(
    val sourceUnitId: String,
    val sourceRootRelativePath: String,
) {
    init {
        require(sourceUnitId.isNotBlank()) { "Document projection source unit id must not be blank." }
        require(sourceRootRelativePath.isNotBlank()) {
            "Document projection source root relative path must not be blank."
        }
    }
}

data class DocumentProjectionWorkspaceSemanticSnapshot(
    val semanticGraphId: String,
    val sourceUnits: List<DocumentProjectionSourceUnitSummary>,
    val subjects: List<DocumentProjectionSubjectSummary> = emptyList(),
) {
    init {
        require(semanticGraphId.isNotBlank()) { "Document projection workspace semantic graph id must not be blank." }
        require(sourceUnits.isNotEmpty()) { "Document projection requires at least one source unit." }
        require(sourceUnits.map { sourceUnit -> sourceUnit.sourceUnitId }.distinct().size == sourceUnits.size) {
            "Document projection source unit ids must be unique."
        }
    }
}

data class DocumentProjectionSubjectSummary(
    val canonicalSubjectId: StableSemanticIdentity,
    val occurrenceRole: DocumentOccurrenceRole,
    val detailRole: DocumentOccurrenceDetailRole,
    val sheetViewRoles: List<SheetViewRole>,
    val source: DocumentProjectionProvenance? = null,
    val sourceTerminalIdentity: StableSemanticIdentity? = null,
    val targetTerminalIdentity: StableSemanticIdentity? = null,
) {
    init {
        require(canonicalSubjectId.value.isNotBlank()) { "Document projection subject id must not be blank." }
        require(sheetViewRoles.isNotEmpty()) { "Document projection subject must target at least one sheet view role." }
        require(sheetViewRoles.distinct().size == sheetViewRoles.size) {
            "Document projection subject sheet view roles must not contain duplicates."
        }
    }
}

data class DocumentProjectionReferenceFactContainers(
    val continuationFacts: List<ContinuationFact> = emptyList(),
    val crossReferenceFacts: List<CrossReferenceFact> = emptyList(),
) {
    val continuationFactIds: List<String>
        get() = continuationFacts.map { fact -> fact.continuationFactId.value }

    val crossReferenceFactIds: List<String>
        get() = crossReferenceFacts.map { fact -> fact.crossReferenceFactId.value }

    init {
        require(continuationFacts.map { fact -> fact.continuationFactId }.distinct().size == continuationFacts.size) {
            "Document projection continuation fact ids must be unique."
        }
        require(crossReferenceFacts.map { fact -> fact.crossReferenceFactId }.distinct().size == crossReferenceFacts.size) {
            "Document projection cross-reference fact ids must be unique."
        }
    }
}

data class DocumentProjectionProofMetadata(
    val policyDeterministicIdentity: DocumentProjectionPolicyDeterministicIdentity,
    val sourceUnitIds: List<String>,
    val sourceRootRelativePaths: List<String>,
) {
    init {
        require(sourceUnitIds.all(String::isNotBlank)) { "Document projection proof source unit ids must not be blank." }
        require(sourceRootRelativePaths.all(String::isNotBlank)) {
            "Document projection proof source root relative paths must not be blank."
        }
    }
}

data class DocumentProjectionSnapshot(
    val documentProjectionId: DocumentProjectionId,
    val policyId: DocumentProjectionPolicyId,
    val policyVersion: DocumentProjectionPolicyVersion,
    val policyDeterministicIdentity: DocumentProjectionPolicyDeterministicIdentity,
    val sheetViews: List<SheetView>,
    val occurrenceIndex: DocumentOccurrenceIndex,
    val occurrenceIdsBySheetView: Map<SheetViewId, List<DocumentOccurrenceId>>,
    val referenceFacts: DocumentProjectionReferenceFactContainers,
    val diagnostics: List<DocumentProjectionDiagnostic> = emptyList(),
    val proofMetadata: DocumentProjectionProofMetadata,
) {
    init {
        require(documentProjectionId.policyId == policyId) {
            "Document projection snapshot policy id must match document projection id."
        }
        require(documentProjectionId.policyVersion == policyVersion) {
            "Document projection snapshot policy version must match document projection id."
        }
        require(sheetViews.isNotEmpty()) { "Document projection snapshot must contain sheet views." }
        require(sheetViews.map { sheetView -> sheetView.sheetViewId }.distinct().size == sheetViews.size) {
            "Document projection snapshot sheet view ids must be unique."
        }
        require(occurrenceIdsBySheetView.keys.all { sheetViewId -> sheetViewId in sheetViews.map(SheetView::sheetViewId) }) {
            "Document projection occurrence memberships must target known sheet views."
        }
    }
}

object DocumentProjectionEntryPoint {
    fun projectWorkspace(
        workspace: DocumentProjectionWorkspaceSemanticSnapshot,
        policy: DocumentProjectionPolicy = BuiltInDocumentProjectionPolicies.athenaDocumentProjectionV0(),
    ): DocumentProjectionSnapshot {
        val documentProjectionId = DocumentProjectionId(
            policyId = policy.policyId,
            policyVersion = policy.policyVersion,
            semanticGraphId = workspace.semanticGraphId,
        )
        val sourceUnits = workspace.sourceUnits.sortedWith(
            compareBy<DocumentProjectionSourceUnitSummary>(
                { sourceUnit -> sourceUnit.sourceUnitId },
                { sourceUnit -> sourceUnit.sourceRootRelativePath },
            ),
        )

        val sheetViews = policy.supportedSheetViewRoles
            .sortedBy { role -> role.order }
            .map { role ->
                SheetView(
                    sheetViewId = SheetViewId("sheet-view:${role.role.toSheetViewSlug()}"),
                    role = role.role,
                    title = role.displayTitle,
                    order = role.order,
                    artifactKind = DocumentProjectionArtifactKind.SCHEMATIC_SHEET_VIEW,
                    zones = defaultLogicalZones(),
                )
            }
        val entries = occurrenceIndexEntries(documentProjectionId, sheetViews, workspace.subjects)
        val occurrenceIndex = DocumentOccurrenceIndex.canonical(entries)
        val occurrenceIdsBySheetView = occurrenceIndex.entries
            .groupBy { entry -> entry.occurrence.sheetViewId }
            .mapValues { (_, groupedEntries) -> groupedEntries.map { entry -> entry.occurrence.occurrenceId } }
        val continuationFacts = continuationFacts(occurrenceIndex, sheetViews, workspace.subjects)
        val diagnostics = documentReferenceDiagnostics(occurrenceIndex, continuationFacts)
        val referenceFacts = DocumentProjectionReferenceFactContainers(
            continuationFacts = continuationFacts,
            crossReferenceFacts = crossReferenceFacts(occurrenceIndex, sheetViews, continuationFacts),
        )

        return DocumentProjectionSnapshot(
            documentProjectionId = documentProjectionId,
            policyId = policy.policyId,
            policyVersion = policy.policyVersion,
            policyDeterministicIdentity = policy.deterministicIdentity,
            sheetViews = sheetViews,
            occurrenceIndex = occurrenceIndex,
            occurrenceIdsBySheetView = occurrenceIdsBySheetView,
            referenceFacts = referenceFacts,
            diagnostics = diagnostics,
            proofMetadata = DocumentProjectionProofMetadata(
                policyDeterministicIdentity = policy.deterministicIdentity,
                sourceUnitIds = sourceUnits.map { sourceUnit -> sourceUnit.sourceUnitId },
                sourceRootRelativePaths = sourceUnits.map { sourceUnit -> sourceUnit.sourceRootRelativePath },
            ),
        )
    }
}

private fun continuationFacts(
    occurrenceIndex: DocumentOccurrenceIndex,
    sheetViews: List<SheetView>,
    subjects: List<DocumentProjectionSubjectSummary>,
): List<ContinuationFact> {
    val sheetOrderById = sheetViews.associate { sheetView -> sheetView.sheetViewId to sheetView.order }
    val routeSubjectsByIdentity = subjects
        .filter { subject -> subject.occurrenceRole == DocumentOccurrenceRole.ROUTE }
        .associateBy { subject -> subject.canonicalSubjectId }

    return occurrenceIndex.entries
        .filter { entry -> entry.occurrence.occurrenceRole == DocumentOccurrenceRole.ROUTE }
        .groupBy { entry -> entry.occurrence.canonicalSubjectId }
        .toSortedMap(compareBy { identity -> identity.value })
        .flatMap { (routeIdentity, routeEntries) ->
            val orderedRouteEntries = routeEntries.sortedWith(
                compareBy<DocumentOccurrenceIndexEntry>(
                    { entry -> sheetOrderById.getValue(entry.occurrence.sheetViewId) },
                    { entry -> entry.occurrence.occurrenceId.value },
                ),
            )
            if (orderedRouteEntries.size < 2) {
                emptyList()
            } else {
                val routeSubject = routeSubjectsByIdentity.getValue(routeIdentity)
                orderedRouteEntries
                    .zipWithNext()
                    .map { (source, target) ->
                        ContinuationFact(
                            continuationFactId = ContinuationFact.identityOf(
                                routeIdentity = routeIdentity,
                                sourceRouteOccurrenceId = source.occurrence.occurrenceId,
                                targetRouteOccurrenceId = target.occurrence.occurrenceId,
                            ),
                            routeIdentity = routeIdentity,
                            sourceRouteOccurrenceId = source.occurrence.occurrenceId,
                            targetRouteOccurrenceId = target.occurrence.occurrenceId,
                            sourceDocumentLocation = source.location,
                            targetDocumentLocation = target.location,
                            sourceTerminalIdentity = routeSubject.sourceTerminalIdentity,
                            targetTerminalIdentity = routeSubject.targetTerminalIdentity,
                            displayNotation = "${target.location.sheetViewId.value} ${target.location.displayNotation}",
                            provenance = routeSubject.source,
                        )
                    }
            }
        }
}

private fun crossReferenceFacts(
    occurrenceIndex: DocumentOccurrenceIndex,
    sheetViews: List<SheetView>,
    continuationFacts: List<ContinuationFact>,
): List<CrossReferenceFact> {
    val sheetOrderById = sheetViews.associate { sheetView -> sheetView.sheetViewId to sheetView.order }
    val sameSubjectReferences = occurrenceIndex.entries
        .groupBy { entry -> entry.occurrence.canonicalSubjectId }
        .toSortedMap(compareBy { identity -> identity.value })
        .flatMap { (identity, entries) ->
            val orderedEntries = entries.sortedWith(
                compareBy<DocumentOccurrenceIndexEntry>(
                    { entry -> sheetOrderById.getValue(entry.occurrence.sheetViewId) },
                    { entry -> entry.occurrence.occurrenceId.value },
                ),
            )
            orderedEntries.zipWithNext().map { (source, target) ->
                CrossReferenceFact(
                    crossReferenceFactId = CrossReferenceFact.identityOf(
                        sourceIdentity = identity,
                        targetIdentity = identity,
                        sourceOccurrenceId = source.occurrence.occurrenceId,
                        targetOccurrenceId = target.occurrence.occurrenceId,
                        relationType = CrossReferenceRelationType.REPEATED_SUBJECT,
                    ),
                    sourceIdentity = identity,
                    targetIdentity = identity,
                    sourceOccurrenceId = source.occurrence.occurrenceId,
                    targetOccurrenceId = target.occurrence.occurrenceId,
                    relationType = CrossReferenceRelationType.REPEATED_SUBJECT,
                    sourceDocumentLocation = source.location,
                    targetDocumentLocation = target.location,
                    displayNotation = compactReferenceNotation(target.location),
                    provenance = source.occurrence.source,
                )
            }
        }

    val routeContinuationReferences = continuationFacts.map { continuation ->
        CrossReferenceFact(
            crossReferenceFactId = CrossReferenceFact.identityOf(
                sourceIdentity = continuation.routeIdentity,
                targetIdentity = continuation.routeIdentity,
                sourceOccurrenceId = continuation.sourceRouteOccurrenceId,
                targetOccurrenceId = continuation.targetRouteOccurrenceId,
                relationType = CrossReferenceRelationType.ROUTE_CONTINUATION,
            ),
            sourceIdentity = continuation.routeIdentity,
            targetIdentity = continuation.routeIdentity,
            sourceOccurrenceId = continuation.sourceRouteOccurrenceId,
            targetOccurrenceId = continuation.targetRouteOccurrenceId,
            relationType = CrossReferenceRelationType.ROUTE_CONTINUATION,
            sourceDocumentLocation = continuation.sourceDocumentLocation,
            targetDocumentLocation = continuation.targetDocumentLocation,
            displayNotation = compactReferenceNotation(continuation.targetDocumentLocation),
            provenance = continuation.provenance,
        )
    }

    val terminalContinuationReferences = continuationFacts.mapNotNull { continuation ->
        val sourceTerminalIdentity = continuation.sourceTerminalIdentity ?: return@mapNotNull null
        val targetTerminalIdentity = continuation.targetTerminalIdentity ?: return@mapNotNull null
        val sourceTerminal = occurrenceIndex.forSubject(sourceTerminalIdentity).singleOrNull() ?: return@mapNotNull null
        val targetTerminal = occurrenceIndex.forSubject(targetTerminalIdentity).singleOrNull() ?: return@mapNotNull null
        CrossReferenceFact(
            crossReferenceFactId = CrossReferenceFact.identityOf(
                sourceIdentity = sourceTerminalIdentity,
                targetIdentity = targetTerminalIdentity,
                sourceOccurrenceId = sourceTerminal.occurrence.occurrenceId,
                targetOccurrenceId = targetTerminal.occurrence.occurrenceId,
                relationType = CrossReferenceRelationType.TERMINAL_CONTINUATION,
            ),
            sourceIdentity = sourceTerminalIdentity,
            targetIdentity = targetTerminalIdentity,
            sourceOccurrenceId = sourceTerminal.occurrence.occurrenceId,
            targetOccurrenceId = targetTerminal.occurrence.occurrenceId,
            relationType = CrossReferenceRelationType.TERMINAL_CONTINUATION,
            sourceDocumentLocation = sourceTerminal.location,
            targetDocumentLocation = targetTerminal.location,
            displayNotation = compactReferenceNotation(targetTerminal.location),
            provenance = continuation.provenance,
        )
    }

    return (sameSubjectReferences + routeContinuationReferences + terminalContinuationReferences)
        .sortedWith(
            compareBy<CrossReferenceFact>(
                { fact -> fact.relationType.name },
                { fact -> fact.sourceIdentity.value },
                { fact -> fact.targetIdentity.value },
                { fact -> fact.sourceOccurrenceId.value },
                { fact -> fact.targetOccurrenceId.value },
            ),
        )
}

private fun documentReferenceDiagnostics(
    occurrenceIndex: DocumentOccurrenceIndex,
    continuationFacts: List<ContinuationFact>,
): List<DocumentProjectionDiagnostic> =
    continuationFacts.flatMap { continuation ->
        val sourceDiagnostics = terminalReferenceDiagnostics(
            terminalIdentity = continuation.sourceTerminalIdentity,
            terminalOccurrences = continuation.sourceTerminalIdentity?.let(occurrenceIndex::forSubject).orEmpty(),
            continuation = continuation,
            targetSide = false,
        )
        val targetDiagnostics = terminalReferenceDiagnostics(
            terminalIdentity = continuation.targetTerminalIdentity,
            terminalOccurrences = continuation.targetTerminalIdentity?.let(occurrenceIndex::forSubject).orEmpty(),
            continuation = continuation,
            targetSide = true,
        )
        sourceDiagnostics + targetDiagnostics
    }.sortedWith(
        compareBy<DocumentProjectionDiagnostic>(
            { diagnostic -> diagnostic.code.value },
            { diagnostic -> diagnostic.affectedIdentity.value },
            { diagnostic -> diagnostic.provenance.sheetViewId?.value.orEmpty() },
        ),
    )

private fun terminalReferenceDiagnostics(
    terminalIdentity: StableSemanticIdentity?,
    terminalOccurrences: List<DocumentOccurrenceIndexEntry>,
    continuation: ContinuationFact,
    targetSide: Boolean,
): List<DocumentProjectionDiagnostic> {
    if (terminalIdentity == null || terminalOccurrences.size == 1) {
        return emptyList()
    }

    return if (terminalOccurrences.isEmpty()) {
        listOf(
            DocumentProjectionDiagnostic(
                severity = DocumentProjectionDiagnosticSeverity.ERROR,
                code = DocumentProjectionDiagnosticCode("M26_REFERENCE_TARGET_MISSING"),
                relationType = CrossReferenceRelationType.TERMINAL_CONTINUATION,
                affectedIdentity = terminalIdentity,
                message = "Terminal continuation reference target has no document occurrence.",
                provenance = sourceOrDerivedProvenance(continuation, targetSide),
                canPublishToProblems = continuation.provenance?.sourceRange != null,
            ),
        )
    } else {
        listOf(
            DocumentProjectionDiagnostic(
                severity = DocumentProjectionDiagnosticSeverity.WARNING,
                code = DocumentProjectionDiagnosticCode("M26_REFERENCE_TARGET_AMBIGUOUS"),
                relationType = CrossReferenceRelationType.TERMINAL_CONTINUATION,
                affectedIdentity = terminalIdentity,
                message = "Terminal continuation reference target resolves to multiple document occurrences.",
                provenance = DocumentProjectionDiagnosticProvenance(
                    kind = DocumentProjectionDiagnosticProvenanceKind.DERIVED_VIEW,
                    sheetViewId = if (targetSide) {
                        continuation.targetDocumentLocation.sheetViewId
                    } else {
                        continuation.sourceDocumentLocation.sheetViewId
                    },
                ),
                canPublishToProblems = false,
            ),
        )
    }
}

private fun sourceOrDerivedProvenance(
    continuation: ContinuationFact,
    targetSide: Boolean,
): DocumentProjectionDiagnosticProvenance =
    if (continuation.provenance?.sourceRange != null) {
        DocumentProjectionDiagnosticProvenance(
            kind = DocumentProjectionDiagnosticProvenanceKind.SOURCE,
            source = continuation.provenance,
        )
    } else {
        DocumentProjectionDiagnosticProvenance(
            kind = DocumentProjectionDiagnosticProvenanceKind.DERIVED_VIEW,
            sheetViewId = if (targetSide) {
                continuation.targetDocumentLocation.sheetViewId
            } else {
                continuation.sourceDocumentLocation.sheetViewId
            },
        )
    }

private fun occurrenceIndexEntries(
    documentProjectionId: DocumentProjectionId,
    sheetViews: List<SheetView>,
    subjects: List<DocumentProjectionSubjectSummary>,
): List<DocumentOccurrenceIndexEntry> {
    val sheetViewsByRole = sheetViews.associateBy(SheetView::role)
    return sheetViews
        .flatMap { sheetView ->
            subjects
                .filter { subject -> sheetView.role in subject.sheetViewRoles }
                .sortedWith(
                    compareBy<DocumentProjectionSubjectSummary>(
                        { subject -> subject.canonicalSubjectId.value },
                        { subject -> subject.occurrenceRole.name },
                        { subject -> subject.detailRole.name },
                    ),
                )
                .mapIndexed { index, subject ->
                    val targetSheetView = sheetViewsByRole.getValue(sheetView.role)
                    val occurrenceId = DocumentOccurrence.identityOf(
                        documentProjectionId = documentProjectionId,
                        sheetViewId = targetSheetView.sheetViewId,
                        canonicalSubjectId = subject.canonicalSubjectId,
                        occurrenceRole = subject.occurrenceRole,
                        detailRole = subject.detailRole,
                    )
                    val zone = targetSheetView.zones[index % targetSheetView.zones.size]
                    DocumentOccurrenceIndexEntry(
                        occurrence = DocumentOccurrence(
                            occurrenceId = occurrenceId,
                            documentProjectionId = documentProjectionId,
                            sheetViewId = targetSheetView.sheetViewId,
                            canonicalSubjectId = subject.canonicalSubjectId,
                            occurrenceRole = subject.occurrenceRole,
                            detailRole = subject.detailRole,
                            source = subject.source,
                        ),
                        location = DocumentLocation(
                            sheetViewId = targetSheetView.sheetViewId,
                            zoneId = zone.zoneId,
                            displayNotation = zone.zoneId.value,
                        ),
                    )
                }
        }
}

private fun defaultLogicalZones(): List<LogicalZone> = listOf(
    LogicalZone(LogicalZoneId("A1"), label = "A1", order = 0),
    LogicalZone(LogicalZoneId("B2"), label = "B2", order = 1),
    LogicalZone(LogicalZoneId("C3"), label = "C3", order = 2),
)

private fun SheetViewRole.toSheetViewSlug(): String =
    name.lowercase(Locale.ROOT).replace('_', '-')

private fun compactReferenceNotation(location: DocumentLocation): String =
    "${location.sheetViewId.value} ${location.displayNotation}"
