package com.engineeringood.athena.presentation

import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringSourceEditEvidence
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.interaction.InteractionActionFamily
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionProvenance
import com.engineeringood.athena.interaction.InteractionRegistryInput
import com.engineeringood.athena.interaction.InteractionRegistrySubjectFact
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.interaction.SemanticActionIntent
import com.engineeringood.athena.interaction.SemanticCapability
import com.engineeringood.athena.interaction.SemanticCapabilityRegistry
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CabinetSelectionAndEditBoundaryTest {
    @Test
    fun `resolves selected occurrence through trace table and capability registry without prefix fallback`() {
        val result = CabinetGraphicSelectionResolver.resolve(
            CabinetGraphicSelectionRequest(
                selectedOccurrenceId = GraphicOccurrenceId("occ:SRC"),
                traceTable = traceTable(),
                registry = registry(),
                requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
            ),
        )

        val success = assertIs<CabinetGraphicSelectionResolution.Success>(result)

        assertEquals(StableSemanticIdentity("component:Source"), success.subject.key.canonicalSubjectId)
        assertEquals(InteractionSubjectKind.COMPONENT, success.subject.key.subjectKind)
        assertEquals(
            CabinetSelectionEvidence(
                occurrenceId = GraphicOccurrenceId("occ:SRC"),
                subjectAuthority = "graphic-occurrence-trace-table",
                capabilityAuthority = "semantic-capability-registry",
                revealAuthority = "trace-source-span",
                fallbackUsed = false,
            ),
            success.evidence,
        )
        assertEquals("src/main.athena", success.revealTarget.sourceRange?.sourceUri)
        assertEquals(listOf("cabinet.reveal-source"), success.subject.capabilities.map { capability -> capability.capabilityId })
    }

    @Test
    fun `rejects selection fallback and direct graphic mutation while accepting governed edit evidence`() {
        val fallback = CabinetGraphicSelectionResolver.resolve(
            CabinetGraphicSelectionRequest(
                selectedOccurrenceId = null,
                traceTable = traceTable(),
                registry = registry(),
                requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
                attemptedFallbacks = setOf(CabinetForbiddenSelectionFallback.DOM_ID, CabinetForbiddenSelectionFallback.LABEL_TEXT),
            ),
        )
        val directMutation = CabinetGovernedEditBoundary.rejectDirectMutation(CabinetForbiddenMutationTarget.GRAPHIC_PRIMITIVE_IR)
        val governed = CabinetGovernedEditBoundary.acceptGovernedPath(
            CabinetGovernedEditEvidence(
                semanticActionIntent = SemanticActionIntent(
                    actionIntentId = "action:move-src",
                    actionFamily = InteractionActionFamily.MUTATE,
                    subject = registry().subjects.single().key,
                    requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
                    parameters = mapOf("intent" to "cabinet.move"),
                ),
                authoringIntentId = AuthoringIntentId("authoring:move-src"),
                transactionId = SemanticAuthoringTransactionId("transaction:move-src"),
                previewId = AuthoringPreviewId("preview:move-src"),
                sourceEditEvidence = AuthoringSourceEditEvidence(
                    revisionGuard = revisionGuard,
                    sourceUri = revisionGuard.sourceUri,
                    startOffset = 12,
                    endOffset = 12,
                    admittedText = "move source",
                    affectedSemanticIds = listOf("component:Source"),
                ),
                target = CabinetGovernedEditTarget.INSTALLATION_DECLARATION,
                compileLintRequired = true,
                rerenderRequired = true,
            ),
        )

        assertEquals(
            setOf("cabinet.selection.fallback_forbidden"),
            assertIs<CabinetGraphicSelectionResolution.Failure>(fallback).diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertEquals(
            setOf("cabinet.edit.direct_mutation_forbidden"),
            assertIs<CabinetGovernedEditBoundaryResult.Failure>(directMutation).diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertEquals(
            CabinetGovernedEditPathEvidence(
                orderedStages = listOf(
                    "SemanticActionIntent",
                    "AuthoringIntent",
                    "SemanticAuthoringTransaction",
                    "AuthoringPreview",
                    "AuthoringSourceEditEvidence",
                    "compile-lint",
                    "rerender",
                ),
                target = CabinetGovernedEditTarget.INSTALLATION_DECLARATION,
            ),
            assertIs<CabinetGovernedEditBoundaryResult.Accepted>(governed).evidence,
        )
    }

    private fun registry(): SemanticCapabilityRegistry = SemanticCapabilityRegistry.build(
        InteractionRegistryInput(
            sourceContextId = "src/main.athena",
            sourceRevision = "rev-1",
            subjects = listOf(
                InteractionRegistrySubjectFact(
                    canonicalSubjectId = StableSemanticIdentity("component:Source"),
                    subjectKind = InteractionSubjectKind.COMPONENT,
                    capabilities = listOf(
                        SemanticCapability(
                            capabilityId = "cabinet.reveal-source",
                            actionFamily = InteractionActionFamily.REVEAL,
                            enabled = true,
                        ),
                    ),
                ),
            ),
        ),
    )

    private fun traceTable(): GraphicOccurrenceTraceTable = GraphicOccurrenceTraceTable(
        version = GraphicOccurrenceTraceTableVersion(1),
        selectablePrimitives = listOf(
            GraphicSelectablePrimitiveTraceRef(
                primitiveId = com.engineeringood.athena.representation.GraphicPrimitiveId("mounted-body:SRC"),
                occurrenceId = GraphicOccurrenceId("occ:SRC"),
                semanticSubjectId = StableSemanticIdentity("component:Source"),
            ),
        ),
        decorativePrimitiveIds = emptyList(),
        entries = listOf(
            GraphicOccurrenceTraceEntry(
                occurrenceId = GraphicOccurrenceId("occ:SRC"),
                semanticSubjectId = StableSemanticIdentity("component:Source"),
                sourceChain = GraphicOccurrenceSourceChain(
                    installationDeclaration = TraceSourceRef("installation:MainCabinet", TraceSourceSpan("src/main.athena", 10, 3), TraceDigest("sha256:install")),
                    mountedOccurrence = TraceSourceRef("mount:Source", TraceSourceSpan("src/main.athena", 20, 5), TraceDigest("sha256:mount")),
                    bindingRule = TraceSourceRef("binding:iec", TraceSourceSpan("packages/binding.athena", 4, 1), TraceDigest("sha256:binding")),
                    representationDefinition = TraceSourceRef("element:source", TraceSourceSpan("packages/element.athena", 8, 1), TraceDigest("sha256:representation")),
                    resourceSnapshot = TraceDigest("sha256:resource"),
                    owningDeclarations = listOf("source:src/main.athena"),
                ),
            ),
        ),
        evidence = GraphicOccurrenceTraceEvidence(
            selectablePrimitiveCount = 1,
            decorativePrimitiveCount = 0,
            traceEntryCount = 1,
            missingTraceCount = 0,
            unusedTraceCount = 0,
            duplicatePrimitiveOwnerCount = 0,
        ),
    )

    private companion object {
        val revisionGuard: AuthoringRevisionGuard = AuthoringRevisionGuard.from(
            semanticSnapshotId = "snapshot-1",
            sourceUri = "src/main.athena",
            documentVersion = 1,
            sourceText = "source",
        )
    }
}
