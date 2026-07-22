package com.engineeringood.athena.document

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DocumentProjectionModelContractTest {
    @Test
    fun `occurrence index covers all occurrence roles and supports sheet view lookup`() {
        val sourceRange = DocumentSourceRange(2, 3, 2, 18)
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:index-coverage",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                    occurrenceRole = DocumentOccurrenceRole.COMPONENT,
                    detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC, SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION),
                    source = DocumentProjectionProvenance("source:system", sourceRange),
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("terminal:PLC1.Q0.0"),
                    occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                    detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1"),
                    occurrenceRole = DocumentOccurrenceRole.ROUTE,
                    detailRole = DocumentOccurrenceDetailRole.ROUTE,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("label:PLC1.device-tag"),
                    occurrenceRole = DocumentOccurrenceRole.LABEL,
                    detailRole = DocumentOccurrenceDetailRole.LABEL,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                ),
            ),
        )

        val snapshot = DocumentProjectionEntryPoint.projectWorkspace(workspace)
        val controlEntries = snapshot.occurrenceIndex.forSheetView(SheetViewId("sheet-view:control-and-plc-logic"))

        assertEquals(
            listOf(DocumentOccurrenceRole.COMPONENT, DocumentOccurrenceRole.ROUTE, DocumentOccurrenceRole.LABEL, DocumentOccurrenceRole.TERMINAL),
            controlEntries.map { entry -> entry.occurrence.occurrenceRole },
        )
        assertEquals(
            listOf("sheet-view:control-and-plc-logic", "sheet-view:field-wiring-and-terminal-transition"),
            snapshot.occurrenceIndex
                .forSubject(StableSemanticIdentity("component:PLC1"))
                .map { entry -> entry.location.sheetViewId.value },
        )
        assertEquals(
            sourceRange,
            snapshot.occurrenceIndex
                .forSubject(StableSemanticIdentity("component:PLC1"))
                .first()
                .occurrence
                .source
                ?.sourceRange,
        )
    }

    @Test
    fun `occurrence ids remain stable when source files are renamed or reordered`() {
        val subjects = listOf(
            DocumentProjectionSubjectSummary(
                canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                occurrenceRole = DocumentOccurrenceRole.COMPONENT,
                detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
                sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                source = DocumentProjectionProvenance("source:plc"),
            ),
            DocumentProjectionSubjectSummary(
                canonicalSubjectId = StableSemanticIdentity("label:PLC1.device-tag"),
                occurrenceRole = DocumentOccurrenceRole.LABEL,
                detailRole = DocumentOccurrenceDetailRole.LABEL,
                sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                source = DocumentProjectionProvenance("source:plc"),
            ),
        )
        val before = DocumentProjectionEntryPoint.projectWorkspace(
            DocumentProjectionWorkspaceSemanticSnapshot(
                semanticGraphId = "graph:rename-stable",
                sourceUnits = listOf(
                    DocumentProjectionSourceUnitSummary("source:plc", "src/01-plc.athena"),
                    DocumentProjectionSourceUnitSummary("source:shared", "src/00-shared.athena"),
                ),
                subjects = subjects,
            ),
        )
        val after = DocumentProjectionEntryPoint.projectWorkspace(
            DocumentProjectionWorkspaceSemanticSnapshot(
                semanticGraphId = "graph:rename-stable",
                sourceUnits = listOf(
                    DocumentProjectionSourceUnitSummary("source:shared", "src/99-renamed-shared.athena"),
                    DocumentProjectionSourceUnitSummary("source:plc", "src/88-renamed-plc.athena"),
                ),
                subjects = subjects,
            ),
        )

        assertEquals(
            before.occurrenceIndex.entries.map { entry -> entry.occurrence.occurrenceId },
            after.occurrenceIndex.entries.map { entry -> entry.occurrence.occurrenceId },
        )
    }

    @Test
    fun `document projection materializes deterministic occurrence membership across sheet views`() {
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:membership",
            sourceUnits = listOf(
                DocumentProjectionSourceUnitSummary("source:shared", "src/01-shared.athena"),
                DocumentProjectionSourceUnitSummary("source:system", "src/02-system.athena"),
            ),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                    occurrenceRole = DocumentOccurrenceRole.COMPONENT,
                    detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
                    sheetViewRoles = listOf(SheetViewRole.POWER_DISTRIBUTION, SheetViewRole.CONTROL_AND_PLC_LOGIC),
                    source = DocumentProjectionProvenance("source:shared"),
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("terminal:XT1.1"),
                    occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                    detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                    sheetViewRoles = listOf(SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION),
                    source = DocumentProjectionProvenance("source:system"),
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1"),
                    occurrenceRole = DocumentOccurrenceRole.ROUTE,
                    detailRole = DocumentOccurrenceDetailRole.ROUTE,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC, SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION),
                    source = DocumentProjectionProvenance("source:system"),
                ),
            ),
        )

        val first = DocumentProjectionEntryPoint.projectWorkspace(workspace)
        val second = DocumentProjectionEntryPoint.projectWorkspace(workspace.copy(sourceUnits = workspace.sourceUnits.reversed()))

        assertEquals(first.sheetViews, second.sheetViews)
        assertEquals(first.occurrenceIndex.entries.map { entry -> entry.occurrence.occurrenceId }, second.occurrenceIndex.entries.map { entry -> entry.occurrence.occurrenceId })
        assertEquals(
            mapOf(
                "sheet-view:control-and-plc-logic" to listOf(
                    "component:PLC1",
                    "connection:PLC1.Q0.0->XT1.1",
                ),
                "sheet-view:field-wiring-and-terminal-transition" to listOf(
                    "connection:PLC1.Q0.0->XT1.1",
                    "terminal:XT1.1",
                ),
                "sheet-view:power-distribution" to listOf("component:PLC1"),
            ),
            first.occurrenceIdsBySheetView.mapValues { (_, occurrenceIds) ->
                occurrenceIds.map { occurrenceId ->
                    first.occurrenceIndex.entries.single { entry -> entry.occurrence.occurrenceId == occurrenceId }
                        .occurrence.canonicalSubjectId.value
                }
            }.mapKeys { (sheetViewId, _) -> sheetViewId.value },
        )
        assertEquals(
            listOf("A1", "B2", "C3"),
            first.sheetViews.single { sheetView -> sheetView.role == SheetViewRole.CONTROL_AND_PLC_LOGIC }
                .zones
                .map { zone -> zone.zoneId.value },
        )
    }

    @Test
    fun `document projection entry point projects a single source workspace snapshot`() {
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:single-file",
            sourceUnits = listOf(
                DocumentProjectionSourceUnitSummary(
                    sourceUnitId = "source:main",
                    sourceRootRelativePath = "src/01-main.athena",
                ),
            ),
        )

        val snapshot = DocumentProjectionEntryPoint.projectWorkspace(workspace)

        assertEquals(
            DocumentProjectionId(
                policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
                policyVersion = DocumentProjectionPolicyVersion("0"),
                semanticGraphId = "graph:single-file",
            ),
            snapshot.documentProjectionId,
        )
        assertEquals(DocumentProjectionPolicyId("athena-document-projection-v0"), snapshot.policyId)
        assertEquals(DocumentProjectionPolicyVersion("0"), snapshot.policyVersion)
        assertEquals(
            listOf("sheet-view:power-distribution", "sheet-view:control-and-plc-logic", "sheet-view:field-wiring-and-terminal-transition"),
            snapshot.sheetViews.map { sheetView -> sheetView.sheetViewId.value },
        )
        assertEquals(emptyList(), snapshot.occurrenceIndex.entries)
        assertEquals(emptyList(), snapshot.referenceFacts.continuationFactIds)
        assertEquals(emptyList(), snapshot.referenceFacts.crossReferenceFactIds)
    }

    @Test
    fun `cross view route membership produces governed continuation facts`() {
        val routeIdentity = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1")
        val sourceTerminal = StableSemanticIdentity("terminal:PLC1.Q0.0")
        val targetTerminal = StableSemanticIdentity("terminal:XT1.1")
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:continuation",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = routeIdentity,
                    occurrenceRole = DocumentOccurrenceRole.ROUTE,
                    detailRole = DocumentOccurrenceDetailRole.ROUTE,
                    sheetViewRoles = listOf(
                        SheetViewRole.CONTROL_AND_PLC_LOGIC,
                        SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION,
                    ),
                    sourceTerminalIdentity = sourceTerminal,
                    targetTerminalIdentity = targetTerminal,
                ),
            ),
        )

        val snapshot = DocumentProjectionEntryPoint.projectWorkspace(workspace)
        val continuation = snapshot.referenceFacts.continuationFacts.single()

        assertEquals(routeIdentity, continuation.routeIdentity)
        assertEquals(sourceTerminal, continuation.sourceTerminalIdentity)
        assertEquals(targetTerminal, continuation.targetTerminalIdentity)
        assertEquals("sheet-view:control-and-plc-logic", continuation.sourceDocumentLocation.sheetViewId.value)
        assertEquals("sheet-view:field-wiring-and-terminal-transition", continuation.targetDocumentLocation.sheetViewId.value)
        assertEquals("sheet-view:field-wiring-and-terminal-transition A1", continuation.displayNotation)
        assertEquals(
            snapshot.occurrenceIndex
                .forSubject(routeIdentity)
                .first { entry -> entry.occurrence.sheetViewId.value == "sheet-view:control-and-plc-logic" }
                .occurrence
                .occurrenceId,
            continuation.sourceRouteOccurrenceId,
        )
        assertEquals(
            snapshot.occurrenceIndex
                .forSubject(routeIdentity)
                .first { entry -> entry.occurrence.sheetViewId.value == "sheet-view:field-wiring-and-terminal-transition" }
                .occurrence
                .occurrenceId,
            continuation.targetRouteOccurrenceId,
        )
    }

    @Test
    fun `single view route membership does not produce continuation facts`() {
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:no-continuation",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1"),
                    occurrenceRole = DocumentOccurrenceRole.ROUTE,
                    detailRole = DocumentOccurrenceDetailRole.ROUTE,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                ),
            ),
        )

        val snapshot = DocumentProjectionEntryPoint.projectWorkspace(workspace)

        assertTrue(snapshot.referenceFacts.continuationFacts.isEmpty())
        assertTrue(snapshot.referenceFacts.continuationFactIds.isEmpty())
    }

    @Test
    fun `repeated subjects produce compact typed cross reference facts`() {
        val subjectIdentity = StableSemanticIdentity("component:PLC1")
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:repeated-reference",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = subjectIdentity,
                    occurrenceRole = DocumentOccurrenceRole.COMPONENT,
                    detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
                    sheetViewRoles = listOf(
                        SheetViewRole.POWER_DISTRIBUTION,
                        SheetViewRole.CONTROL_AND_PLC_LOGIC,
                    ),
                ),
            ),
        )

        val snapshot = DocumentProjectionEntryPoint.projectWorkspace(workspace)
        val reference = snapshot.referenceFacts.crossReferenceFacts.single()

        assertEquals(CrossReferenceRelationType.REPEATED_SUBJECT, reference.relationType)
        assertEquals(subjectIdentity, reference.sourceIdentity)
        assertEquals(subjectIdentity, reference.targetIdentity)
        assertEquals("sheet-view:control-and-plc-logic A1", reference.displayNotation)
        assertFalse(reference.displayNotation.contains("component:PLC1"))
        assertEquals(
            snapshot.occurrenceIndex
                .forSubject(subjectIdentity)
                .first { entry -> entry.occurrence.sheetViewId.value == "sheet-view:power-distribution" }
                .occurrence
                .occurrenceId,
            reference.sourceOccurrenceId,
        )
        assertEquals(
            snapshot.occurrenceIndex
                .forSubject(subjectIdentity)
                .first { entry -> entry.occurrence.sheetViewId.value == "sheet-view:control-and-plc-logic" }
                .occurrence
                .occurrenceId,
            reference.targetOccurrenceId,
        )
    }

    @Test
    fun `continuation facts produce route and terminal cross reference facts`() {
        val routeIdentity = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1")
        val sourceTerminal = StableSemanticIdentity("terminal:PLC1.Q0.0")
        val targetTerminal = StableSemanticIdentity("terminal:XT1.1")
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:continuation-reference",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = routeIdentity,
                    occurrenceRole = DocumentOccurrenceRole.ROUTE,
                    detailRole = DocumentOccurrenceDetailRole.ROUTE,
                    sheetViewRoles = listOf(
                        SheetViewRole.CONTROL_AND_PLC_LOGIC,
                        SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION,
                    ),
                    sourceTerminalIdentity = sourceTerminal,
                    targetTerminalIdentity = targetTerminal,
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = sourceTerminal,
                    occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                    detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = targetTerminal,
                    occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                    detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                    sheetViewRoles = listOf(SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION),
                ),
            ),
        )

        val snapshot = DocumentProjectionEntryPoint.projectWorkspace(workspace)
        val referencesByType = snapshot.referenceFacts.crossReferenceFacts.associateBy(CrossReferenceFact::relationType)

        val routeReference = referencesByType.getValue(CrossReferenceRelationType.ROUTE_CONTINUATION)
        assertEquals(routeIdentity, routeReference.sourceIdentity)
        assertEquals(routeIdentity, routeReference.targetIdentity)
        assertEquals("sheet-view:field-wiring-and-terminal-transition A1", routeReference.displayNotation)
        assertFalse(routeReference.displayNotation.contains("connection:"))

        val terminalReference = referencesByType.getValue(CrossReferenceRelationType.TERMINAL_CONTINUATION)
        assertEquals(sourceTerminal, terminalReference.sourceIdentity)
        assertEquals(targetTerminal, terminalReference.targetIdentity)
        assertEquals("sheet-view:field-wiring-and-terminal-transition B2", terminalReference.displayNotation)
        assertFalse(terminalReference.displayNotation.contains("terminal:"))
    }

    @Test
    fun `source backed missing terminal continuation diagnostic can publish to problems`() {
        val routeIdentity = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1")
        val sourceTerminal = StableSemanticIdentity("terminal:PLC1.Q0.0")
        val missingTargetTerminal = StableSemanticIdentity("terminal:XT1.1")
        val sourceRange = DocumentSourceRange(8, 5, 8, 38)
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:missing-terminal-diagnostic",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = routeIdentity,
                    occurrenceRole = DocumentOccurrenceRole.ROUTE,
                    detailRole = DocumentOccurrenceDetailRole.ROUTE,
                    sheetViewRoles = listOf(
                        SheetViewRole.CONTROL_AND_PLC_LOGIC,
                        SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION,
                    ),
                    source = DocumentProjectionProvenance("source:system", sourceRange),
                    sourceTerminalIdentity = sourceTerminal,
                    targetTerminalIdentity = missingTargetTerminal,
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = sourceTerminal,
                    occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                    detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                ),
            ),
        )

        val diagnostic = DocumentProjectionEntryPoint.projectWorkspace(workspace).diagnostics.single()

        assertEquals(DocumentProjectionDiagnosticSeverity.ERROR, diagnostic.severity)
        assertEquals(DocumentProjectionDiagnosticCode("M26_REFERENCE_TARGET_MISSING"), diagnostic.code)
        assertEquals(CrossReferenceRelationType.TERMINAL_CONTINUATION, diagnostic.relationType)
        assertEquals(missingTargetTerminal, diagnostic.affectedIdentity)
        assertEquals(DocumentProjectionDiagnosticProvenanceKind.SOURCE, diagnostic.provenance.kind)
        assertEquals(sourceRange, diagnostic.provenance.source?.sourceRange)
        assertTrue(diagnostic.canPublishToProblems)
    }

    @Test
    fun `projection only ambiguous terminal continuation diagnostic stays out of problems`() {
        val routeIdentity = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1")
        val sourceTerminal = StableSemanticIdentity("terminal:PLC1.Q0.0")
        val repeatedTargetTerminal = StableSemanticIdentity("terminal:XT1.1")
        val workspace = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:ambiguous-terminal-diagnostic",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
            subjects = listOf(
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = routeIdentity,
                    occurrenceRole = DocumentOccurrenceRole.ROUTE,
                    detailRole = DocumentOccurrenceDetailRole.ROUTE,
                    sheetViewRoles = listOf(
                        SheetViewRole.CONTROL_AND_PLC_LOGIC,
                        SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION,
                    ),
                    sourceTerminalIdentity = sourceTerminal,
                    targetTerminalIdentity = repeatedTargetTerminal,
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = sourceTerminal,
                    occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                    detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                    sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                ),
                DocumentProjectionSubjectSummary(
                    canonicalSubjectId = repeatedTargetTerminal,
                    occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                    detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                    sheetViewRoles = listOf(
                        SheetViewRole.CONTROL_AND_PLC_LOGIC,
                        SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION,
                    ),
                ),
            ),
        )

        val diagnostic = DocumentProjectionEntryPoint.projectWorkspace(workspace).diagnostics.single()

        assertEquals(DocumentProjectionDiagnosticSeverity.WARNING, diagnostic.severity)
        assertEquals(DocumentProjectionDiagnosticCode("M26_REFERENCE_TARGET_AMBIGUOUS"), diagnostic.code)
        assertEquals(CrossReferenceRelationType.TERMINAL_CONTINUATION, diagnostic.relationType)
        assertEquals(repeatedTargetTerminal, diagnostic.affectedIdentity)
        assertEquals(DocumentProjectionDiagnosticProvenanceKind.DERIVED_VIEW, diagnostic.provenance.kind)
        assertEquals(SheetViewId("sheet-view:field-wiring-and-terminal-transition"), diagnostic.provenance.sheetViewId)
        assertFalse(diagnostic.canPublishToProblems)
    }

    @Test
    fun `document projection entry point ignores source filename order for sheet view identity`() {
        val firstOrder = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:multi-file",
            sourceUnits = listOf(
                DocumentProjectionSourceUnitSummary("source:shared", "src/01-shared-components.athena"),
                DocumentProjectionSourceUnitSummary("source:system", "src/02-system.athena"),
            ),
        )
        val secondOrder = firstOrder.copy(sourceUnits = firstOrder.sourceUnits.reversed())

        val firstSnapshot = DocumentProjectionEntryPoint.projectWorkspace(firstOrder)
        val secondSnapshot = DocumentProjectionEntryPoint.projectWorkspace(secondOrder)

        assertEquals(firstSnapshot.documentProjectionId, secondSnapshot.documentProjectionId)
        assertEquals(firstSnapshot.sheetViews, secondSnapshot.sheetViews)
        assertFalse(
            firstSnapshot.sheetViews.any { sheetView ->
                firstOrder.sourceUnits.any { sourceUnit ->
                    sheetView.sheetViewId.value.contains(sourceUnit.sourceRootRelativePath)
                }
            },
            "Source file paths must not become sheet view identities.",
        )
        assertEquals(
            listOf("src/01-shared-components.athena", "src/02-system.athena"),
            firstSnapshot.proofMetadata.sourceRootRelativePaths,
        )
        assertEquals(
            listOf("src/01-shared-components.athena", "src/02-system.athena"),
            secondSnapshot.proofMetadata.sourceRootRelativePaths,
        )
    }

    @Test
    fun `built in document projection policy has deterministic identity and role ordering`() {
        val first = BuiltInDocumentProjectionPolicies.athenaDocumentProjectionV0()
        val second = BuiltInDocumentProjectionPolicies.athenaDocumentProjectionV0()

        assertEquals(DocumentProjectionPolicyId("athena-document-projection-v0"), first.policyId)
        assertEquals(DocumentProjectionPolicyVersion("0"), first.policyVersion)
        assertEquals(first.deterministicIdentity, second.deterministicIdentity)
        assertEquals(
            listOf(
                SheetViewRole.POWER_DISTRIBUTION to "Power Distribution",
                SheetViewRole.CONTROL_AND_PLC_LOGIC to "Control And PLC Logic",
                SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION to "Field Wiring And Terminal Transition",
            ),
            first.supportedSheetViewRoles.map { role -> role.role to role.displayTitle },
        )
    }

    @Test
    fun `m31 customer projection policy exposes exactly control and field device sheet roles`() {
        val first = BuiltInDocumentProjectionPolicies.athenaM31CustomerProjectionV0()
        val second = BuiltInDocumentProjectionPolicies.athenaM31CustomerProjectionV0()

        assertEquals(DocumentProjectionPolicyId("athena-m31-customer-projection-v0"), first.policyId)
        assertEquals(DocumentProjectionPolicyVersion("0"), first.policyVersion)
        assertEquals(first.deterministicIdentity, second.deterministicIdentity)
        assertEquals(
            listOf(
                SheetViewRole.CONTROL_AND_PLC_LOGIC to "Control",
                SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION to "Field Device",
            ),
            first.supportedSheetViewRoles.map { role -> role.role to role.displayTitle },
        )
        assertEquals(listOf(0, 1), first.supportedSheetViewRoles.map { role -> role.order })
    }

    @Test
    fun `m31 customer projection sheet views do not derive from source file count`() {
        val policy = BuiltInDocumentProjectionPolicies.athenaM31CustomerProjectionV0()
        val singleSource = DocumentProjectionWorkspaceSemanticSnapshot(
            semanticGraphId = "graph:m31-customer-policy",
            sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:main", "src/01-main.athena")),
        )
        val multiSource = singleSource.copy(
            sourceUnits = listOf(
                DocumentProjectionSourceUnitSummary("source:main", "src/01-main.athena"),
                DocumentProjectionSourceUnitSummary("source:devices", "src/02-devices.athena"),
                DocumentProjectionSourceUnitSummary("source:relationships", "src/03-relationships.athena"),
            ),
        )

        val singleSourceSnapshot = DocumentProjectionEntryPoint.projectWorkspace(singleSource, policy)
        val multiSourceSnapshot = DocumentProjectionEntryPoint.projectWorkspace(multiSource, policy)

        assertEquals(
            listOf("sheet-view:control-and-plc-logic", "sheet-view:field-wiring-and-terminal-transition"),
            singleSourceSnapshot.sheetViews.map { sheetView -> sheetView.sheetViewId.value },
        )
        assertEquals(singleSourceSnapshot.sheetViews, multiSourceSnapshot.sheetViews)
        assertEquals(singleSourceSnapshot.policyDeterministicIdentity, multiSourceSnapshot.policyDeterministicIdentity)
        assertFalse(
            multiSourceSnapshot.sheetViews.any { sheetView ->
                multiSource.sourceUnits.any { sourceUnit ->
                    sheetView.sheetViewId.value.contains(sourceUnit.sourceRootRelativePath) ||
                        sheetView.title.contains(sourceUnit.sourceRootRelativePath)
                }
            },
            "M31 customer sheet views must not encode source file paths.",
        )
    }

    @Test
    fun `built in policy exposes supported and reserved artifact kinds explicitly`() {
        val policy = BuiltInDocumentProjectionPolicies.athenaDocumentProjectionV0()

        assertEquals(
            listOf(DocumentProjectionArtifactKind.SCHEMATIC_SHEET_VIEW),
            policy.supportedArtifactKinds
                .filter { artifact -> artifact.availability == DocumentProjectionArtifactAvailability.SUPPORTED }
                .map { artifact -> artifact.artifactKind },
        )
        assertEquals(
            listOf(DocumentProjectionArtifactKind.TERMINAL_REPORT_RESERVED),
            policy.supportedArtifactKinds
                .filter { artifact -> artifact.availability == DocumentProjectionArtifactAvailability.RESERVED }
                .map { artifact -> artifact.artifactKind },
        )
    }

    @Test
    fun `built in policy publishes the occurrence identity recipe`() {
        val policy = BuiltInDocumentProjectionPolicies.athenaDocumentProjectionV0()

        assertEquals(
            listOf(
                DocumentOccurrenceIdentityRecipeSegment.DOCUMENT_PROJECTION_ID,
                DocumentOccurrenceIdentityRecipeSegment.SHEET_VIEW_ID,
                DocumentOccurrenceIdentityRecipeSegment.CANONICAL_SUBJECT_ID,
                DocumentOccurrenceIdentityRecipeSegment.OCCURRENCE_ROLE,
                DocumentOccurrenceIdentityRecipeSegment.DETAIL_ROLE,
            ),
            policy.occurrenceIdentityRecipe.segments,
        )
    }

    @Test
    fun `document occurrence ids serialize deterministically for component terminal route and label occurrences`() {
        val projectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
            policyVersion = DocumentProjectionPolicyVersion("0"),
            semanticGraphId = "workspace:sample",
        )
        val viewId = SheetViewId("sheet-view:control-and-plc-logic")
        val component = DocumentOccurrence.identityOf(
            documentProjectionId = projectionId,
            sheetViewId = viewId,
            canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
            occurrenceRole = DocumentOccurrenceRole.COMPONENT,
            detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
        )
        val terminal = DocumentOccurrence.identityOf(
            documentProjectionId = projectionId,
            sheetViewId = viewId,
            canonicalSubjectId = StableSemanticIdentity("terminal:PLC1:Q0.0"),
            occurrenceRole = DocumentOccurrenceRole.TERMINAL,
            detailRole = DocumentOccurrenceDetailRole.TERMINAL,
        )
        val route = DocumentOccurrence.identityOf(
            documentProjectionId = projectionId,
            sheetViewId = viewId,
            canonicalSubjectId = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1"),
            occurrenceRole = DocumentOccurrenceRole.ROUTE,
            detailRole = DocumentOccurrenceDetailRole.ROUTE,
        )
        val label = DocumentOccurrence.identityOf(
            documentProjectionId = projectionId,
            sheetViewId = viewId,
            canonicalSubjectId = StableSemanticIdentity("label:PLC1:tag"),
            occurrenceRole = DocumentOccurrenceRole.LABEL,
            detailRole = DocumentOccurrenceDetailRole.LABEL,
        )

        assertEquals(
            stableKey(
                projectionId.value,
                viewId.value,
                "component:PLC1",
                DocumentOccurrenceRole.COMPONENT.name,
                DocumentOccurrenceDetailRole.REPRESENTATION.name,
            ),
            component.value,
        )
        assertEquals(
            stableKey(
                projectionId.value,
                viewId.value,
                "terminal:PLC1:Q0.0",
                DocumentOccurrenceRole.TERMINAL.name,
                DocumentOccurrenceDetailRole.TERMINAL.name,
            ),
            terminal.value,
        )
        assertEquals(
            stableKey(
                projectionId.value,
                viewId.value,
                "connection:PLC1.Q0.0->XT1.1",
                DocumentOccurrenceRole.ROUTE.name,
                DocumentOccurrenceDetailRole.ROUTE.name,
            ),
            route.value,
        )
        assertEquals(
            stableKey(
                projectionId.value,
                viewId.value,
                "label:PLC1:tag",
                DocumentOccurrenceRole.LABEL.name,
                DocumentOccurrenceDetailRole.LABEL.name,
            ),
            label.value,
        )
    }

    @Test
    fun `occurrence index is deterministic and supports canonical subject lookup`() {
        val projectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
            policyVersion = DocumentProjectionPolicyVersion("0"),
            semanticGraphId = "workspace:sample",
        )
        val powerView = SheetViewId("sheet-view:power-distribution")
        val controlView = SheetViewId("sheet-view:control-and-plc-logic")
        val plcSubject = StableSemanticIdentity("component:PLC1")
        val hmiSubject = StableSemanticIdentity("component:HMI1")
        val entries = listOf(
            occurrenceEntry(projectionId, controlView, hmiSubject, "B2"),
            occurrenceEntry(projectionId, powerView, plcSubject, "A1"),
            occurrenceEntry(projectionId, controlView, plcSubject, "C3"),
        )

        val index = DocumentOccurrenceIndex.canonical(entries)

        assertEquals(
            listOf(
                "sheet-view:control-and-plc-logic|component:HMI1|B2",
                "sheet-view:control-and-plc-logic|component:PLC1|C3",
                "sheet-view:power-distribution|component:PLC1|A1",
            ),
            index.entries.map { entry ->
                "${entry.occurrence.sheetViewId.value}|${entry.occurrence.canonicalSubjectId.value}|${entry.location.displayNotation}"
            },
        )
        assertEquals(
            listOf("sheet-view:control-and-plc-logic", "sheet-view:power-distribution"),
            index.forSubject(plcSubject).map { entry -> entry.location.sheetViewId.value },
        )
    }

    @Test
    fun `occurrence rejects identity recipe mismatches`() {
        val projectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
            policyVersion = DocumentProjectionPolicyVersion("0"),
            semanticGraphId = "workspace:sample",
        )

        assertFailsWith<IllegalArgumentException> {
            DocumentOccurrence(
                occurrenceId = DocumentOccurrenceId("wrong-occurrence"),
                documentProjectionId = projectionId,
                sheetViewId = SheetViewId("sheet-view:control-and-plc-logic"),
                canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                occurrenceRole = DocumentOccurrenceRole.COMPONENT,
                detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
            )
        }
    }

    @Test
    fun `identity serialization is delimiter safe`() {
        val projectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("policy|with@delimiters"),
            policyVersion = DocumentProjectionPolicyVersion("0|1"),
            semanticGraphId = "workspace|sample",
        )

        val occurrence = DocumentOccurrence.identityOf(
            documentProjectionId = projectionId,
            sheetViewId = SheetViewId("sheet|view"),
            canonicalSubjectId = StableSemanticIdentity("component|PLC1"),
            occurrenceRole = DocumentOccurrenceRole.COMPONENT,
            detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
        )

        assertEquals(
            stableKey(
                projectionId.value,
                "sheet|view",
                "component|PLC1",
                DocumentOccurrenceRole.COMPONENT.name,
                DocumentOccurrenceDetailRole.REPRESENTATION.name,
            ),
            occurrence.value,
        )
    }

    @Test
    fun `occurrence rejects incompatible occurrence and detail roles`() {
        val projectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
            policyVersion = DocumentProjectionPolicyVersion("0"),
            semanticGraphId = "workspace:sample",
        )
        val occurrenceId = DocumentOccurrence.identityOf(
            documentProjectionId = projectionId,
            sheetViewId = SheetViewId("sheet-view:control-and-plc-logic"),
            canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
            occurrenceRole = DocumentOccurrenceRole.COMPONENT,
            detailRole = DocumentOccurrenceDetailRole.TERMINAL,
        )

        assertFailsWith<IllegalArgumentException> {
            DocumentOccurrence(
                occurrenceId = occurrenceId,
                documentProjectionId = projectionId,
                sheetViewId = SheetViewId("sheet-view:control-and-plc-logic"),
                canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                occurrenceRole = DocumentOccurrenceRole.COMPONENT,
                detailRole = DocumentOccurrenceDetailRole.TERMINAL,
            )
        }
    }

    @Test
    fun `occurrence index rejects duplicate occurrence identities`() {
        val projectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
            policyVersion = DocumentProjectionPolicyVersion("0"),
            semanticGraphId = "workspace:sample",
        )
        val viewId = SheetViewId("sheet-view:control-and-plc-logic")
        val subjectId = StableSemanticIdentity("component:PLC1")
        val entry = occurrenceEntry(projectionId, viewId, subjectId, "A1")

        assertFailsWith<IllegalArgumentException> {
            DocumentOccurrenceIndex.canonical(listOf(entry, entry.copy(location = entry.location.copy(displayNotation = "A2"))))
        }
    }

    @Test
    fun `occurrence index rejects mixed document projections`() {
        val firstProjectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
            policyVersion = DocumentProjectionPolicyVersion("0"),
            semanticGraphId = "workspace:sample",
        )
        val secondProjectionId = DocumentProjectionId(
            policyId = DocumentProjectionPolicyId("athena-document-projection-v0"),
            policyVersion = DocumentProjectionPolicyVersion("1"),
            semanticGraphId = "workspace:sample",
        )

        assertFailsWith<IllegalArgumentException> {
            DocumentOccurrenceIndex.canonical(
                listOf(
                    occurrenceEntry(
                        firstProjectionId,
                        SheetViewId("sheet-view:control-and-plc-logic"),
                        StableSemanticIdentity("component:PLC1"),
                        "A1",
                    ),
                    occurrenceEntry(
                        secondProjectionId,
                        SheetViewId("sheet-view:control-and-plc-logic"),
                        StableSemanticIdentity("component:HMI1"),
                        "A2",
                    ),
                ),
            )
        }
    }

    @Test
    fun `document projection contracts expose no raw geometry authority fields`() {
        val forbidden = setOf("x", "y", "width", "height")
        val contractTypes = listOf(
            DocumentProjectionId::class,
            SheetViewId::class,
            LogicalZoneId::class,
            DocumentOccurrenceId::class,
            DocumentSourceRange::class,
            DocumentProjectionProvenance::class,
            LogicalZone::class,
            SheetView::class,
            DocumentLocation::class,
            DocumentOccurrence::class,
            DocumentOccurrenceIndexEntry::class,
            DocumentOccurrenceIndex::class,
            DocumentProjectionSourceUnitSummary::class,
            DocumentProjectionWorkspaceSemanticSnapshot::class,
            DocumentProjectionSubjectSummary::class,
            DocumentProjectionReferenceFactContainers::class,
            DocumentProjectionProofMetadata::class,
            DocumentProjectionSnapshot::class,
            ContinuationFactId::class,
            ContinuationFact::class,
            CrossReferenceFactId::class,
            CrossReferenceRelationType::class,
            CrossReferenceFact::class,
            DocumentProjectionDiagnosticCode::class,
            DocumentProjectionDiagnosticSeverity::class,
            DocumentProjectionDiagnosticProvenanceKind::class,
            DocumentProjectionDiagnosticProvenance::class,
            DocumentProjectionDiagnostic::class,
        )

        val fieldNames = contractTypes.flatMap { type -> type.java.declaredFields.map { field -> field.name } }

        assertFalse(fieldNames.any { name -> name in forbidden }, "Document Projection IR must not own raw geometry fields.")
    }

    private fun occurrenceEntry(
        projectionId: DocumentProjectionId,
        viewId: SheetViewId,
        subjectId: StableSemanticIdentity,
        zone: String,
    ): DocumentOccurrenceIndexEntry {
        val occurrenceId = DocumentOccurrence.identityOf(
            documentProjectionId = projectionId,
            sheetViewId = viewId,
            canonicalSubjectId = subjectId,
            occurrenceRole = DocumentOccurrenceRole.COMPONENT,
            detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
        )
        val occurrence = DocumentOccurrence(
            occurrenceId = occurrenceId,
            documentProjectionId = projectionId,
            sheetViewId = viewId,
            canonicalSubjectId = subjectId,
            occurrenceRole = DocumentOccurrenceRole.COMPONENT,
            detailRole = DocumentOccurrenceDetailRole.REPRESENTATION,
            source = DocumentProjectionProvenance(
                sourceUnitId = "src/system.athena",
                sourceRange = DocumentSourceRange(1, 1, 1, 10),
            ),
        )
        return DocumentOccurrenceIndexEntry(
            occurrence = occurrence,
            location = DocumentLocation(
                sheetViewId = viewId,
                zoneId = LogicalZoneId(zone),
                displayNotation = zone,
            ),
        )
    }

    private fun stableKey(vararg values: String): String =
        values.joinToString(separator = "|") { value -> "${value.length}:$value" }
}
