package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.EngineeringPackageId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.nio.file.Path

class LocalPackageRegistryTest {
    @Test
    fun `registry evaluates explicit project and athena roots in deterministic priority order`() {
        val projectRoot = Path.of("sample-project/.athena/packages")
        val athenaRoot = Path.of("athena/packages")
        val registry = LocalPackageRegistry(
            roots = listOf(
                LocalPackageRegistryRoot(athenaRoot, PackageRegistryRootKind.ATHENA_OWNED),
                LocalPackageRegistryRoot(projectRoot, PackageRegistryRootKind.PROJECT_LOCAL),
            ),
        )

        val result = registry.discover()

        assertTrue(result.isValid)
        assertEquals(
            listOf(
                projectRoot.toAbsolutePath().normalize(),
                athenaRoot.toAbsolutePath().normalize(),
            ),
            result.roots.map { it.path },
        )
        assertEquals(
            listOf(PackageRegistryRootKind.PROJECT_LOCAL, PackageRegistryRootKind.ATHENA_OWNED),
            result.roots.map { it.kind },
        )
    }

    @Test
    fun `registry ignores arbitrary workspace folders not declared as governed roots`() {
        val governedRoot = Path.of("sample-project/packages")
        val arbitraryWorkspaceFolder = Path.of("reference/qelectrotech-source-mirror")
        val registry = LocalPackageRegistry(
            roots = listOf(LocalPackageRegistryRoot(governedRoot, PackageRegistryRootKind.PROJECT_LOCAL)),
            workspaceFolders = listOf(governedRoot, arbitraryWorkspaceFolder),
        )

        val result = registry.discover()

        assertTrue(result.isValid)
        assertEquals(listOf(governedRoot.toAbsolutePath().normalize()), result.roots.map { it.path })
        assertEquals(listOf(arbitraryWorkspaceFolder.toAbsolutePath().normalize()), result.ignoredWorkspaceFolders)
    }

    @Test
    fun `registry diagnoses or resolves duplicate package ids according to policy`() {
        val packageId = EngineeringPackageId("com.athena.example.engineering.drive.compact-vfd")
        val projectRoot = LocalPackageRegistryRoot(
            path = Path.of("sample-project/packages"),
            kind = PackageRegistryRootKind.PROJECT_LOCAL,
        )
        val athenaRoot = LocalPackageRegistryRoot(
            path = Path.of("athena/packages"),
            kind = PackageRegistryRootKind.ATHENA_OWNED,
        )
        val candidates = listOf(
            LocalPackageCandidate(packageId, athenaRoot.path.resolve("drive/package.yaml")),
            LocalPackageCandidate(packageId, projectRoot.path.resolve("drive/package.yaml")),
        )

        val ambiguous = LocalPackageRegistry(
            roots = listOf(athenaRoot, projectRoot),
            packageCandidates = candidates,
            policy = LocalPackageRegistryPolicy(DuplicatePackageIdPolicy.DIAGNOSE_AMBIGUITY),
        ).discover()

        assertFalse(ambiguous.isValid)
        assertEquals(
            listOf("package.registry.package-id.ambiguous"),
            ambiguous.diagnostics.map { it.code.wireValue },
        )
        assertEquals(emptyList(), ambiguous.selectedPackages)

        val precedence = LocalPackageRegistry(
            roots = listOf(athenaRoot, projectRoot),
            packageCandidates = candidates,
            policy = LocalPackageRegistryPolicy(DuplicatePackageIdPolicy.PREFER_HIGHEST_PRIORITY_ROOT),
        ).discover()

        assertTrue(precedence.isValid)
        assertEquals(projectRoot.path.toAbsolutePath().normalize(), precedence.selectedPackages.single().root.path)
        assertEquals(
            listOf("package.registry.package-id.precedence-applied"),
            precedence.diagnostics.map { it.code.wireValue },
        )
    }
}
