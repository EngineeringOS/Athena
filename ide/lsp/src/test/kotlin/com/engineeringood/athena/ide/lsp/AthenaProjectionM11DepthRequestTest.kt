package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier

class AthenaProjectionM11DepthRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `projection session transports serious m11 electrical depth through lsp`() {
        val repositoryRoot = resolveRepoRoot().resolve("examples/m11/dense-electrical-proof")

        val server = AthenaLanguageServer()
        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            val payload = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
            assertEquals(
                listOf("cabinet", "documentation", "schematic", "wiring"),
                payload.supportedViews.map { view -> view.viewId }.sorted(),
            )
            assertEquals("cabinet", payload.activeViewId)
            val cabinet = assertNotNull(payload.readyProjection)
            assertEquals(16, cabinet.components.size)
            assertEquals(29, cabinet.connections.size)
            assertTrue(cabinet.crossReferences.isEmpty())
            assertTrue(cabinet.electricalAnchors.isNotEmpty())
            assertEquals(cabinet.connections.size * 2, cabinet.electricalConnectionEndpoints.size)
            assertEquals(cabinet.connections.size, cabinet.electricalRoutingCorridors.size)
            assertTrue(cabinet.electricalConnectionEndpoints.any { endpoint -> endpoint.endpointRole == "source" })
            assertTrue(cabinet.electricalAnchors.all { anchor ->
                anchor.side in setOf("left", "right", "top", "bottom")
            })

            val wiringCommandPayload = assertNotNull(
                server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "wiring",
                    ),
                ).get(),
            )
            val wiring = assertNotNull(wiringCommandPayload.session?.readyProjection)
            assertEquals("electrical/wiring", wiring.familyId)
            assertTrue(wiring.electricalRoutingCorridors.all { corridor ->
                corridor.routingStyle == "orthogonal" &&
                    corridor.sourceAnchorId.isNotBlank() &&
                    corridor.targetAnchorId.isNotBlank()
            })

            val commandPayload = assertNotNull(
                server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "documentation",
                    ),
                ).get(),
            )
            val session = assertNotNull(commandPayload.session)
            val documentation = assertNotNull(session.readyProjection)
            assertEquals("documentation", session.activeViewId)
            assertEquals("electrical/documentation", documentation.familyId)
            assertEquals(
                listOf(
                    "documentation/sheet/01-control",
                    "documentation/sheet/02-field-device",
                ),
                documentation.sheets.map { sheet -> sheet.sheetId },
            )
            assertTrue(documentation.crossReferences.size >= 12)
            assertEquals(1, documentation.components.count { component -> component.semanticId == "component:M1" })
            assertTrue(
                documentation.components.none { component -> component.projectionId.endsWith("_reference") },
                "LSP documentation payload must not transport duplicate off-sheet reference components.",
            )
            assertTrue(documentation.crossReferences.any { crossReference -> crossReference.sheetIds.size >= 2 })
        } finally {
            server.shutdown().get()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `source mutation keeps review knowledge and repeated references coherent on dense m11 proof`() {
        val repository = createDenseProofRepositoryCopy()
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.sourcePath
        val baselineSource = sourcePath.readText()
        val changedSource = baselineSource.replace("power \"7.5kw\"", "power \"9kw\"")

        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = sourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            baselineSource,
                        ),
                    ),
                )
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams(
                        VersionedTextDocumentIdentifier(documentUri, 2),
                        listOf(
                            TextDocumentContentChangeEvent(
                                changedSource,
                            ),
                        ),
                    ),
                )

                val payload = assertNotNull(
                    server.sourceMutationEvaluation(
                        AthenaSourceMutationParams(
                            textDocument = AthenaSourceMutationTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals("accepted", payload.outcome)
                val inspection = assertNotNull(payload.inspection)
                assertEquals(3, inspection.knowledgeDiagnostics.size)
                assertEquals(1, inspection.impactConsequences.size)
                assertEquals("component:M1", inspection.impactConsequences.single().affectedSubjectIdentity)
                val semanticReview = assertNotNull(payload.semanticReview)
                assertEquals(1, semanticReview.engineeringImpactCount)
                assertTrue(semanticReview.reviewSummary.entries.any { entry -> entry.kind == "engineering-impact" })
                assertTrue(semanticReview.commitIntent.entries.any { entry -> entry.kind == "engineering-impact" })

                val documentationPayload = assertNotNull(
                    server.projectionCommand(
                        AthenaProjectionCommandParams(
                            commandId = "switch-active-view",
                            viewId = "documentation",
                        ),
                    ).get(),
                )
                val documentation = assertNotNull(documentationPayload.session?.readyProjection)
                assertTrue(documentation.crossReferences.any { crossReference -> crossReference.semanticId == "component:M1" })
                assertEquals(1, documentation.components.count { component -> component.semanticId == "component:M1" })
                assertTrue(
                    documentation.components.none { component -> component.projectionId.endsWith("_reference") },
                    "Source mutation reprojection must not reintroduce duplicate off-sheet reference components.",
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun createDenseProofRepositoryCopy(): DenseProofRepository {
        val sourceRoot = resolveRepoRoot().resolve("examples/m11/dense-electrical-proof")
        val repositoryRoot = createTempDirectory("athena-lsp-m11-dense-proof-")
        repositoryRoot.resolve("athena.yaml").writeText(sourceRoot.resolve("athena.yaml").readText())
        repositoryRoot.resolve("athena.lock").writeText(sourceRoot.resolve("athena.lock").readText())
        val copiedSourceRoot = repositoryRoot.resolve("src").createDirectories()
        val sourcePath = copiedSourceRoot.resolve("assembly-line.athena")
        sourcePath.writeText(sourceRoot.resolve("src/assembly-line.athena").readText())
        return DenseProofRepository(
            repositoryRoot = repositoryRoot,
            sourcePath = sourcePath,
        )
    }

    private fun resolveRepoRoot(): Path {
        return generateSequence(Path.of("").toAbsolutePath().normalize()) { candidate -> candidate.parent }
            .first { candidate ->
                candidate.resolve("settings.gradle.kts").toFile().exists() &&
                    candidate.resolve("examples").toFile().exists()
            }
    }
}

private data class DenseProofRepository(
    val repositoryRoot: Path,
    val sourcePath: Path,
)
