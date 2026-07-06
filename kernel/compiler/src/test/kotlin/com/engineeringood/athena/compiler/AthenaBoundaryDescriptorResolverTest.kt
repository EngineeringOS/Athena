package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorResolver
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaBoundaryDescriptorResolverTest {
    @Test
    fun `resolves valid passive boundary descriptors into deterministic order and rejects sovereign or operational descriptors`() {
        val repoRoot = resolveRepoRoot()
        val source = AthenaBoundaryDescriptorSource(
            descriptorRoots = listOf(
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/opc-ua-runtime"),
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/automationml-reference"),
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/external-authority"),
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/invalid-assumption"),
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/operational-execution"),
            ),
        )

        val first = AthenaBoundaryDescriptorResolver().resolve(source)
        val second = AthenaBoundaryDescriptorResolver().resolve(source)

        assertEquals(first, second)
        assertEquals(
            listOf("automationml.reference", "opcua.runtime.bridge"),
            first.validDescriptors.map { it.descriptorId },
        )
        assertEquals(
            listOf(
                "boundary.descriptor.authority.external-canonical-forbidden",
                "boundary.descriptor.mode.operational-not-supported",
                "boundary.descriptor.compatibility.assumptions.invalid",
            ),
            first.rejectedDescriptors.flatMap { rejected -> rejected.diagnostics.map { it.ruleId.value } },
        )
    }

    @Test
    fun `deduplicates repeated descriptor roots before validation`() {
        val repoRoot = resolveRepoRoot()
        val duplicatedPath = repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/automationml-reference")
        val source = AthenaBoundaryDescriptorSource(
            descriptorRoots = listOf(
                duplicatedPath,
                duplicatedPath,
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/opc-ua-runtime"),
            ),
        )

        val result = AthenaBoundaryDescriptorResolver().resolve(source)

        assertEquals(
            listOf("automationml.reference", "opcua.runtime.bridge"),
            result.validDescriptors.map { it.descriptorId },
        )
        assertTrue(result.rejectedDescriptors.isEmpty())
    }

    @Test
    fun `rejects malformed boundary csv fields duplicate descriptor ids and non-reference standards posture`() {
        val repoRoot = resolveRepoRoot()
        val source = AthenaBoundaryDescriptorSource(
            descriptorRoots = listOf(
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/malformed-exchange-forms"),
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/duplicate-id-a"),
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/duplicate-id-b"),
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/automationml-outbound"),
            ),
        )

        val result = AthenaBoundaryDescriptorResolver().resolve(source)

        assertTrue(result.validDescriptors.isEmpty())
        assertContentEquals(
            listOf(
                "boundary.descriptor.standards.direction.unsupported",
                "boundary.descriptor.id.duplicate",
                "boundary.descriptor.id.duplicate",
                "boundary.descriptor.exchange.forms.blank",
            ),
            result.rejectedDescriptors.flatMap { rejected -> rejected.diagnostics.map { it.ruleId.value } },
        )
    }

    @Test
    fun `deduplicates descriptor roots by canonical directory identity on windows aliases`() {
        if (!isWindows()) {
            return
        }

        val repoRoot = resolveRepoRoot()
        val descriptorPath = repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/automationml-reference")
        val aliasedPath = Path.of(descriptorPath.toString().uppercase())
        val source = AthenaBoundaryDescriptorSource(
            descriptorRoots = listOf(
                descriptorPath,
                aliasedPath,
            ),
        )

        val result = AthenaBoundaryDescriptorResolver().resolve(source)

        assertEquals(listOf("automationml.reference"), result.validDescriptors.map { it.descriptorId })
        assertTrue(result.rejectedDescriptors.isEmpty())
    }

    @Test
    fun `loads utf8 boundary manifests without mangling descriptor identity`() {
        val repoRoot = resolveRepoRoot()
        val source = AthenaBoundaryDescriptorSource(
            descriptorRoots = listOf(
                repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/utf8-identity"),
            ),
        )

        val result = AthenaBoundaryDescriptorResolver().resolve(source)

        assertEquals(listOf("boundary.测试"), result.validDescriptors.map { it.descriptorId })
        assertTrue(result.rejectedDescriptors.isEmpty())
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }

    private fun isWindows(): Boolean = System.getProperty("os.name").startsWith("Windows")
}
