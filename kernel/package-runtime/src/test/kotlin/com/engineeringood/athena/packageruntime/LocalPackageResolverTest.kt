package com.engineeringood.athena.packageruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.nio.file.Path

class LocalPackageResolverTest {
    @Test
    fun `resolver emits facts for valid engineering and representation packages`() {
        val projectRoot = LocalPackageRegistryRoot(Path.of("sample-project/packages"), PackageRegistryRootKind.PROJECT_LOCAL)
        val request = PackageResolutionRequest(
            roots = listOf(projectRoot),
            requirements = listOf(
                PackageRequirement("com.athena.example.engineering.drive.compact-vfd", PackageResolutionPackageKind.ENGINEERING),
                PackageRequirement("com.athena.example.representation.drive.iec", PackageResolutionPackageKind.REPRESENTATION),
            ),
            candidates = listOf(
                candidate(
                    packageId = "com.athena.example.engineering.drive.compact-vfd",
                    kind = PackageResolutionPackageKind.ENGINEERING,
                    descriptorPath = projectRoot.path.resolve("engineering/drive/package.yaml"),
                    dependencies = listOf(PackageDependency("com.athena.example.engineering.core", PackageResolutionPackageKind.ENGINEERING, "1.0.0")),
                ),
                candidate(
                    packageId = "com.athena.example.representation.drive.iec",
                    kind = PackageResolutionPackageKind.REPRESENTATION,
                    descriptorPath = projectRoot.path.resolve("representation/drive/package.yaml"),
                ),
            ),
        )

        val result = LocalPackageResolver().resolve(request)

        assertTrue(result.isValid)
        assertFalse(result.rendererFallbackUsed)
        assertEquals(emptyList(), result.diagnostics)
        assertEquals(
            listOf(
                "ENGINEERING:com.athena.example.engineering.drive.compact-vfd:1.0.0",
                "REPRESENTATION:com.athena.example.representation.drive.iec:1.0.0",
            ),
            result.resolvedPackages.map { "${it.kind.name}:${it.packageId}:${it.version}" },
        )
        assertEquals(projectRoot.path.toAbsolutePath().normalize(), result.resolvedPackages.first().selectedRoot.path)
        assertTrue(stablePathKey(result.resolvedPackages.first().descriptorPath).endsWith("engineering/drive/package.yaml"))
        assertEquals(
            listOf("com.athena.example.engineering.core"),
            result.resolvedPackages.first().dependencies.map { it.packageId },
        )
    }

    @Test
    fun `resolver fails closed for missing ambiguous incompatible and invalid packages`() {
        val projectRoot = LocalPackageRegistryRoot(Path.of("sample-project/packages"), PackageRegistryRootKind.PROJECT_LOCAL)
        val request = PackageResolutionRequest(
            roots = listOf(projectRoot),
            requirements = listOf(
                PackageRequirement("com.athena.missing", PackageResolutionPackageKind.ENGINEERING),
                PackageRequirement("com.athena.ambiguous", PackageResolutionPackageKind.REPRESENTATION),
                PackageRequirement("com.athena.incompatible", PackageResolutionPackageKind.ENGINEERING),
                PackageRequirement("com.athena.invalid", PackageResolutionPackageKind.REPRESENTATION),
            ),
            candidates = listOf(
                candidate("com.athena.ambiguous", PackageResolutionPackageKind.REPRESENTATION, projectRoot.path.resolve("a/package.yaml")),
                candidate("com.athena.ambiguous", PackageResolutionPackageKind.REPRESENTATION, projectRoot.path.resolve("b/package.yaml")),
                candidate(
                    "com.athena.incompatible",
                    PackageResolutionPackageKind.ENGINEERING,
                    projectRoot.path.resolve("incompatible/package.yaml"),
                    compatible = false,
                ),
                candidate(
                    "com.athena.invalid",
                    PackageResolutionPackageKind.REPRESENTATION,
                    projectRoot.path.resolve("invalid/package.yaml"),
                    validationDiagnostics = listOf(
                        PackageResolutionDiagnostic(
                            code = PackageResolutionDiagnosticCode("package.validation.failed"),
                            severity = PackageResolutionDiagnosticSeverity.ERROR,
                            subject = "invalid/package.yaml",
                            message = "Invalid representation package.",
                        ),
                    ),
                ),
            ),
        )

        val result = LocalPackageResolver().resolve(request)

        assertFalse(result.isValid)
        assertFalse(result.rendererFallbackUsed)
        assertEquals(emptyList(), result.resolvedPackages)
        assertEquals(
            listOf(
                "package.resolution.missing",
                "package.resolution.ambiguous",
                "package.resolution.incompatible",
                "package.resolution.invalid",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    @Test
    fun `resolver emits deterministic facts for identical inputs`() {
        val projectRoot = LocalPackageRegistryRoot(Path.of("sample-project/packages"), PackageRegistryRootKind.PROJECT_LOCAL)
        val request = PackageResolutionRequest(
            roots = listOf(projectRoot),
            requirements = listOf(
                PackageRequirement("com.athena.z", PackageResolutionPackageKind.REPRESENTATION),
                PackageRequirement("com.athena.a", PackageResolutionPackageKind.ENGINEERING),
            ),
            candidates = listOf(
                candidate("com.athena.z", PackageResolutionPackageKind.REPRESENTATION, projectRoot.path.resolve("z/package.yaml")),
                candidate("com.athena.a", PackageResolutionPackageKind.ENGINEERING, projectRoot.path.resolve("a/package.yaml")),
            ),
        )

        val first = LocalPackageResolver().resolve(request)
        val second = LocalPackageResolver().resolve(request)

        assertEquals(first, second)
        assertEquals(
            listOf(
                "ENGINEERING:com.athena.a",
                "REPRESENTATION:com.athena.z",
            ),
            first.resolvedPackages.map { "${it.kind.name}:${it.packageId}" },
        )
    }

    private fun candidate(
        packageId: String,
        kind: PackageResolutionPackageKind,
        descriptorPath: Path,
        version: String = "1.0.0",
        dependencies: List<PackageDependency> = emptyList(),
        compatible: Boolean = true,
        validationDiagnostics: List<PackageResolutionDiagnostic> = emptyList(),
    ): PackageDescriptorCandidate = PackageDescriptorCandidate(
        packageId = packageId,
        kind = kind,
        version = version,
        descriptorPath = descriptorPath,
        dependencies = dependencies,
        compatible = compatible,
        validationDiagnostics = validationDiagnostics,
    )
}
