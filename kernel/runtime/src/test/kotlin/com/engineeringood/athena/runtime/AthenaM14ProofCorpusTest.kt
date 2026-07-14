package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportLockState
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM14ProofCorpusTest {
    @Test
    fun `m14 siemens proof corpus resolves deterministically from a real governed repository`() {
        val repositoryRoot = resolveRepoRoot().resolve("examples/m14/siemens-proof-corpus")
        val sourcePath = repositoryRoot.resolve("src/siemens-proof-corpus.athena")
        check(Files.exists(sourcePath)) { "Expected M14 proof corpus source at `$sourcePath`." }

        val compiler = AthenaCompiler()
        val contractValidation = compiler.validateRepositoryContract(repositoryRoot)
        assertTrue(contractValidation.isValid, contractValidation.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message })

        val lockValidation = compiler.validateRepositoryLock(repositoryRoot)
        assertTrue(lockValidation.isValid, lockValidation.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message })

        val runtime = AthenaRuntime()
        val workspace = runtime.openWorkspace(repositoryRoot)
        val session = workspace.activateRepositoryGraphSession(
            projectName = "siemens-proof-corpus",
            sourcePath = sourcePath,
        )
        assertEquals(AthenaRepositoryReportLockState.CURRENT, session.publication.lockState)

        val first = assertIs<AthenaComponentKnowledgeReady>(
            session.executionContext.componentKnowledgeRuntime().inspect(session.executionContext),
        )
        val second = assertIs<AthenaComponentKnowledgeReady>(
            session.executionContext.componentKnowledgeRuntime().inspect(session.executionContext),
        )

        assertEquals(first, second)
        assertEquals(listOf("com.engineeringood.athena.domain.electrical-runtime"), first.contributingPluginIds)
        assertEquals(5, first.activeConceptCount)
        assertEquals(6, first.activeImplementationCount)
        assertEquals(
            listOf("component:FR1", "component:KM1", "component:M1", "component:PLC1", "component:PS1"),
            first.components.map { entry -> entry.resolvedComponent.semanticSubjectId.value },
        )
        assertEquals(
            listOf(
                "electrical.relay.overload",
                "electrical.contactor.power",
                "electrical.motor.ac",
                "electrical.plc.cpu",
                "electrical.power-supply.dc24",
            ),
            first.components.map { entry -> entry.resolvedComponent.concept.conceptId.value },
        )
        assertEquals(
            listOf(
                "proof.relay.overload",
                "proof.contactor.3pole",
                "proof.motor.ac",
                "proof.cpu.313c",
                "proof.power-supply.24vdc",
            ),
            first.components.mapNotNull { entry -> entry.resolvedImplementation?.implementation?.vendorPartNumber?.value },
        )
        assertEquals(4, first.semanticPorts.size)
        assertEquals(listOf("component:PLC1"), first.physicalTraits.map { trait -> trait.semanticSubjectId.value })
        assertTrue(first.diagnostics.isEmpty())
    }
}

private fun resolveRepoRoot(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
        current = current.parent
    }
    check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
    return current
}
