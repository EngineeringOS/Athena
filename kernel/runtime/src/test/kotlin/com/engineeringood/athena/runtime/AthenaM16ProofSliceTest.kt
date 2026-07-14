package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportLockState
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.reuse.SemanticMacroId
import com.engineeringood.athena.reuse.SemanticMacroInstantiationId
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM16ProofSliceTest {
    @Test
    fun `m16 proof slice publishes three governed semantic macros from a real repository`() {
        val repositoryRoot = resolveRepoRoot().resolve("examples/m16/semantic-reuse-proof")
        val sourcePath = repositoryRoot.resolve("src/semantic-reuse-proof.athena")
        check(Files.exists(sourcePath)) { "Expected M16 proof source at `$sourcePath`." }

        val compiler = AthenaCompiler()
        val contractValidation = compiler.validateRepositoryContract(repositoryRoot)
        assertTrue(contractValidation.isValid, contractValidation.diagnostics.joinToString("\n") { it.message })

        val lockValidation = compiler.validateRepositoryLock(repositoryRoot)
        assertTrue(lockValidation.isValid, lockValidation.diagnostics.joinToString("\n") { it.message })

        val runtime = AthenaRuntime()
        val workspace = runtime.openWorkspace(repositoryRoot)
        val session = workspace.activateRepositoryGraphSession(
            projectName = "semantic-reuse-proof",
            sourcePath = sourcePath,
        )
        assertEquals(AthenaRepositoryReportLockState.CURRENT, session.publication.lockState)
        val context = session.executionContext

        val catalog = assertIs<AthenaSemanticMacroCatalogReady>(
            context.reuseRuntime().catalog(
                context = context,
                request = AthenaSemanticMacroCatalogRequest(),
            ),
        )
        assertEquals(
            listOf("24V Distribution Unit", "DOL Starter", "PLC Rack"),
            catalog.entries.map { entry -> entry.displayName },
        )
        assertEquals(
            listOf("macro:24v-distribution-unit", "macro:dol-starter", "macro:plc-rack"),
            catalog.entries.map { entry -> entry.macroId.value },
        )

        val starterPreview = assertIs<AthenaSemanticMacroPreviewReady>(
            context.reuseRuntime().preview(
                context = context,
                request = AthenaSemanticMacroPreviewRequest(
                    macroId = SemanticMacroId("macro:dol-starter"),
                    instantiationId = SemanticMacroInstantiationId("instance:M1"),
                    parameterValues = mapOf(
                        SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("7.5kW"),
                        SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("M1"),
                    ),
                ),
            ),
        )
        val plcPreview = assertIs<AthenaSemanticMacroPreviewReady>(
            context.reuseRuntime().preview(
                context = context,
                request = AthenaSemanticMacroPreviewRequest(
                    macroId = SemanticMacroId("macro:plc-rack"),
                    instantiationId = SemanticMacroInstantiationId("instance:PLC1"),
                    parameterValues = mapOf(
                        SemanticMacroParameterName("rackTag") to SemanticMacroParameterValue.Symbol("PLC1"),
                    ),
                ),
            ),
        )
        val supplyPreview = assertIs<AthenaSemanticMacroPreviewReady>(
            context.reuseRuntime().preview(
                context = context,
                request = AthenaSemanticMacroPreviewRequest(
                    macroId = SemanticMacroId("macro:24v-distribution-unit"),
                    instantiationId = SemanticMacroInstantiationId("instance:PS1"),
                    parameterValues = mapOf(
                        SemanticMacroParameterName("supplyTag") to SemanticMacroParameterValue.Symbol("PS1"),
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("electrical.contactor.power", "electrical.relay.overload"),
            starterPreview.preview.components.map { component -> component.conceptId },
        )
        assertEquals(
            listOf(
                "impl/electrical/contactor/siemens-proof-3pole",
                "impl/electrical/relay/siemens-proof-overload",
            ),
            starterPreview.preview.components.map { component -> component.implementationId },
        )
        assertEquals(listOf("electrical.plc.cpu"), plcPreview.preview.components.map { component -> component.conceptId })
        assertEquals(
            listOf("impl/electrical/plc-cpu/siemens-proof-cpu313c"),
            plcPreview.preview.components.map { component -> component.implementationId },
        )
        assertEquals(
            listOf("electrical.power-supply.dc24"),
            supplyPreview.preview.components.map { component -> component.conceptId },
        )
        assertEquals(
            listOf("impl/electrical/power-supply/siemens-proof-24vdc"),
            supplyPreview.preview.components.map { component -> component.implementationId },
        )
        assertEquals(1, starterPreview.preview.connections.size)
    }

    @Test
    fun `m16 proof path stays deterministic across repeated repository reruns`() {
        val repositoryRoot = resolveRepoRoot().resolve("examples/m16/semantic-reuse-proof")
        val sourcePath = repositoryRoot.resolve("src/semantic-reuse-proof.athena")
        check(Files.exists(sourcePath)) { "Expected M16 proof source at `$sourcePath`." }

        val first = executeDolStarterProof(repositoryRoot, sourcePath)
        val second = executeDolStarterProof(repositoryRoot, sourcePath)

        assertEquals(first.preview, second.preview)
        assertEquals(first.acceptance.bundle, second.acceptance.bundle)
        assertEquals(first.acceptance.changedSemanticIds, second.acceptance.changedSemanticIds)
        assertEquals(first.acceptance.commandId, second.acceptance.commandId)
        assertEquals(first.acceptance.inspection, second.acceptance.inspection)
        assertEquals(first.acceptance.semanticReview, second.acceptance.semanticReview)
        assertEquals(first.origin.acceptedExpansion, second.origin.acceptedExpansion)
        assertEquals(first.origin.matchedMembership, second.origin.matchedMembership)
        assertEquals(first.origin.bundleId, second.origin.bundleId)
        assertEquals(first.origin.commandId, second.origin.commandId)
    }
}

private data class M16ProofRun(
    val preview: com.engineeringood.athena.reuse.SemanticMacroPreview,
    val acceptance: AthenaSemanticMacroAcceptanceCommitted,
    val origin: AthenaSemanticMacroOriginInspectionReady,
)

private fun executeDolStarterProof(
    repositoryRoot: Path,
    sourcePath: Path,
): M16ProofRun {
    val runtime = AthenaRuntime()
    val context = runtime.openWorkspace(repositoryRoot).activateProject(
        projectName = "semantic-reuse-proof",
        sourcePath = sourcePath,
    )
    val previewResult = context.reuseRuntime().preview(
        context = context,
        request = AthenaSemanticMacroPreviewRequest(
            macroId = SemanticMacroId("macro:dol-starter"),
            instantiationId = SemanticMacroInstantiationId("instance:M1"),
            parameterValues = mapOf(
                SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("7.5kW"),
                SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("M1"),
            ),
        ),
    )
    val preview = assertIs<AthenaSemanticMacroPreviewReady>(previewResult, previewResult.toString())
    val acceptance = assertIs<AthenaSemanticMacroAcceptanceCommitted>(
        context.reuseRuntime().accept(
            context = context,
            request = AthenaSemanticMacroAcceptanceRequest(
                previewId = preview.preview.previewId,
                macroId = preview.preview.macroId,
                instantiationId = preview.preview.instantiationId,
            ),
        ),
    )
    val origin = assertIs<AthenaSemanticMacroOriginInspectionReady>(
        context.reuseRuntime().inspectOrigin(
            context = context,
            request = AthenaSemanticMacroOriginInspectionRequest(
                subjectId = StableSemanticIdentity("component:instance:M1:template:starter.contactor"),
            ),
        ),
    )
    return M16ProofRun(
        preview = preview.preview,
        acceptance = acceptance,
        origin = origin,
    )
}

private fun resolveRepoRoot(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
        current = current.parent
    }
    check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
    return current
}
