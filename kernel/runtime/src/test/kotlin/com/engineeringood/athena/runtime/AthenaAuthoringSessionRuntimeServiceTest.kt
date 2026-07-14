package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.ConnectPortsIntent
import com.engineeringood.athena.authoring.CreateComponentIntent
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.part.PartImplementationId
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaAuthoringSessionRuntimeServiceTest {
    @Test
    fun `submitting a guided component creation records preview state without semantic mutation`() {
        val sourcePath = writeProject(authoringFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = context.compileActiveProject()

            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = CreateComponentIntent(
                        intentId = AuthoringIntentId("intent-0001"),
                        origin = AuthoringOrigin(AuthoringSurface.PALETTE),
                        parentIdentity = StableSemanticIdentity("system:AuthoringRuntime"),
                        conceptId = EngineeringConceptId("electrical.plc.cpu"),
                        preferredImplementationId = PartImplementationId("electrical.plc.cpu.siemens.cpu313c"),
                        suggestedName = "PLC2",
                    ),
                ),
            )

            assertEquals("authoring-preview-0001", submitted.record.preview.previewId.value)
            assertEquals("intent-0001", submitted.record.preview.intentId.value)
            assertEquals(AuthoringPreviewStatus.PENDING_REVIEW, submitted.record.preview.status)
            assertEquals(listOf("system:AuthoringRuntime"), submitted.record.preview.changes.single().affectedSubjectIdentities.map { it.value })
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertEquals(1, context.authoringSessions().state(context).pendingPreviewCount)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `accepted guided preview updates runtime owned decision state without mutating canonical state`() {
        val sourcePath = writeProject(authoringFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = context.compileActiveProject()
            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = ConnectPortsIntent(
                        intentId = AuthoringIntentId("intent-0002"),
                        origin = AuthoringOrigin(AuthoringSurface.GRAPH),
                        sourcePortId = StableSemanticIdentity("port:PLC1.out"),
                        targetPortId = StableSemanticIdentity("port:M1.in"),
                    ),
                ),
            )

            val updated = assertIs<AthenaAuthoringPreviewDecisionUpdated>(
                context.authoringSessions().applyDecision(
                    context = context,
                    decision = AcceptAuthoringPreviewDecision(
                        previewId = submitted.record.preview.previewId,
                        intentId = submitted.record.preview.intentId,
                        note = "Ready for later M8 handoff.",
                    ),
                ),
            )

            assertEquals(AuthoringPreviewStatus.ACCEPTED, updated.record.preview.status)
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertEquals(0, context.authoringSessions().state(context).pendingPreviewCount)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `guided authoring preview state can be snapshotted and restored through runtime session state`() {
        val sourcePath = writeProject(authoringFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )

            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = CreateComponentIntent(
                        intentId = AuthoringIntentId("intent-0003"),
                        origin = AuthoringOrigin(AuthoringSurface.PALETTE, detail = "components"),
                        parentIdentity = StableSemanticIdentity("system:AuthoringRuntime"),
                        conceptId = EngineeringConceptId("electrical.power-supply.dc24"),
                        suggestedName = "PS1",
                    ),
                ),
            )

            val snapshot = context.authoringSessions().snapshot(context)
            val restoredContext = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime-restored",
                sourcePath = sourcePath,
            )
            val restoredService = AthenaAuthoringSessionRuntimeService()
            restoredService.restoreSession(restoredContext, snapshot)

            val restoredState = restoredService.state(restoredContext)
            assertEquals(1, restoredState.records.size)
            assertEquals(submitted.record.preview.previewId, restoredState.records.single().preview.previewId)
            assertEquals(1, restoredState.pendingPreviewCount)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-authoring-session-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun authoringFixture(): String {
        return """
            system AuthoringRuntime {
              device PLC1 {
                type Switch
              }

              device M1 {
                type Motor
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              port M1.in {
                direction in
                signal Digital
              }

              connect PLC1.out -> M1.in
            }
        """.trimIndent()
    }
}
