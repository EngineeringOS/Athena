package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseSuccess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM36GovernedGraphicEditIntentTest {
    @Test
    fun `layout move preview is source first and compiler owned`() {
        assertEquals("athena/graphicEdit/preview", ATHENA_GOVERNED_GRAPHIC_EDIT_PREVIEW_METHOD)
        val document = sourceDocument(CABINET_SOURCE)

        val request = AthenaGovernedGraphicEditIntentRequest(
            revisionGuard = document.revisionGuard.toPayload(),
            edit = AthenaGovernedGraphicEditPayload.LayoutMove(
                occurrenceId = "cabinet:DriveMount",
                dx = 24.0,
                dy = 12.0,
                surface = AthenaGovernedGraphicEditSurface.CabinetProjection,
            ),
        )

        val preview = AthenaGovernedGraphicEditIntentCompiler.preview(request, document)

        assertEquals(AthenaGovernedGraphicEditDecision.PreviewReady, preview.decision)
        assertEquals(AthenaGovernedGraphicEditAuthority.SourceMutation, preview.authority)
        val sourceEdit = assertNotNull(preview.sourceEdit)
        assertEquals(request.revisionGuard.sourceUri, sourceEdit.uri)
        assertEquals(request.revisionGuard, sourceEdit.revisionGuard)
        assertEquals("(44mm, 12mm)", sourceEdit.newText)
        assertTrue(
            sourceEdit.applyTo(document.sourceText)
                .contains("mount Drive as DriveMount on HorizontalRail at (44mm, 12mm)"),
        )
        assertTrue(preview.validation.diagnostics.isEmpty())
        assertTrue(preview.recompileRequired)
        assertTrue(preview.rerenderRequired)
        assertEquals(listOf("compiler.validation", "source.mutation.preview", "projection.rerender"), preview.evidence)
    }

    @Test
    fun `direct graphic authority mutation requests are rejected without source edit`() {
        val document = sourceDocument(CABINET_SOURCE)
        val forbiddenTargets = listOf(
            AthenaGovernedGraphicMutationTarget.SvgResource,
            AthenaGovernedGraphicMutationTarget.DomNode,
            AthenaGovernedGraphicMutationTarget.GraphicPrimitiveIr,
            AthenaGovernedGraphicMutationTarget.PlacementFact,
        )

        forbiddenTargets.forEach { target ->
            val preview = AthenaGovernedGraphicEditIntentCompiler.preview(
                AthenaGovernedGraphicEditIntentRequest(
                    revisionGuard = document.revisionGuard.toPayload(),
                    edit = AthenaGovernedGraphicEditPayload.DirectMutation(
                        target = target,
                        subject = "drive.main",
                    ),
                ),
                document,
            )

            assertEquals(AthenaGovernedGraphicEditDecision.Rejected, preview.decision)
            assertEquals(AthenaGovernedGraphicEditAuthority.SourceMutation, preview.authority)
            assertEquals(null, preview.sourceEdit)
            assertFalse(preview.recompileRequired)
            assertFalse(preview.rerenderRequired)
            assertNotNull(preview.validation.diagnostics.singleOrNull())
            assertEquals("athena.graphicEdit.directMutation.forbidden", preview.validation.diagnostics.single().code)
            assertTrue(preview.validation.diagnostics.single().message.contains(target.name))
        }
    }

    @Test
    fun `unknown occurrence and stale source revisions are rejected without source edits`() {
        val document = sourceDocument(CABINET_SOURCE)
        val unknown = AthenaGovernedGraphicEditIntentCompiler.preview(
            AthenaGovernedGraphicEditIntentRequest(
                revisionGuard = document.revisionGuard.toPayload(),
                edit = AthenaGovernedGraphicEditPayload.LayoutMove(
                    occurrenceId = "cabinet:MissingMount",
                    dx = 10.0,
                    dy = 0.0,
                    surface = AthenaGovernedGraphicEditSurface.CabinetProjection,
                ),
            ),
            document,
        )
        assertEquals(AthenaGovernedGraphicEditDecision.Rejected, unknown.decision)
        assertEquals(null, unknown.sourceEdit)

        val staleGuard = AuthoringRevisionGuard.from(
            semanticSnapshotId = document.semanticSnapshotId,
            sourceUri = document.sourceUri,
            documentVersion = document.documentVersion,
            sourceText = "${document.sourceText}\n// stale",
        ).toPayload()
        val stale = AthenaGovernedGraphicEditIntentCompiler.preview(
            AthenaGovernedGraphicEditIntentRequest(
                revisionGuard = staleGuard,
                edit = AthenaGovernedGraphicEditPayload.LayoutMove(
                    occurrenceId = "cabinet:DriveMount",
                    dx = 10.0,
                    dy = 0.0,
                    surface = AthenaGovernedGraphicEditSurface.CabinetProjection,
                ),
            ),
            document,
        )
        assertEquals(AthenaGovernedGraphicEditDecision.Rejected, stale.decision)
        assertEquals("authoring.source.conflict", stale.validation.diagnostics.single().code)
    }

    private fun sourceDocument(source: String): BackendAuthoringSourceDocument {
        val parse = kotlin.test.assertIs<ParseSuccess>(AthenaLanguageParser().parse("cabinet.athena", source))
        return BackendAuthoringSourceDocument(
            sourceUri = "file:///workspace/cabinet.athena",
            documentVersion = 7,
            semanticSnapshotId = "snapshot:m36",
            sourceText = source,
            ast = parse.ast,
        )
    }

    private fun AthenaAuthoringSourceEditPayload.applyTo(source: String): String {
        val start = assertNotNull(startOffset)
        val end = assertNotNull(endOffset)
        return source.substring(0, start) + newText + source.substring(end)
    }

    private companion object {
        const val CABINET_SOURCE = """system CabinetEdit {
  device Drive {
    type Drive
  }
  installation cabinet MainCabinet {
    enclosure ENC size (600mm, 800mm, 200mm)
    surface Backplate in ENC at (10mm, 10mm) size (580mm, 780mm) accepts [din35]
    rail HorizontalRail on Backplate at (20mm, 100mm) length 400mm orientation horizontal mounting din35
    mount Drive as DriveMount on HorizontalRail at (20mm, 0mm) {
      footprint (80mm, 120mm, 60mm)
      mounting din35
      orientation deg0
      allowed-orientations [deg0]
      clearance (0mm, 0mm, 0mm, 0mm)
      compatible-containers [cabinet]
    }
  }
}"""
    }
}
