package com.engineeringood.athena.packageruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import java.nio.file.Path

class PackageCacheIdentityTest {
    @Test
    fun `cache identity includes package descriptor resource registry policy and profile inputs`() {
        val identity = PackageCacheIdentityCalculator.compute(baseInput())

        assertEquals("com.athena.example.representation.drive.iec", identity.components["packageId"])
        assertEquals("1.0.0", identity.components["version"])
        assertEquals("descriptor-sha256:abc", identity.components["descriptorContentIdentity"])
        assertEquals("resource.frequency-drive.vector=resource-sha256:def", identity.components["resourceIdentities"])
        assertEquals(
            Path.of("sample-project/packages").toAbsolutePath().normalize().toString().replace('\\', '/'),
            identity.components["registryRoot"],
        )
        assertEquals("binding-policy:iec-default", identity.components["bindingPolicyIdentity"])
        assertEquals("iec60617", identity.components["activeProfile"])
        assertEquals(64, identity.digest.length)
    }

    @Test
    fun `cache identity changes when descriptor resource policy or profile changes`() {
        val base = PackageCacheIdentityCalculator.compute(baseInput())

        assertNotEquals(base.digest, PackageCacheIdentityCalculator.compute(baseInput(descriptorContentIdentity = "descriptor-sha256:changed")).digest)
        assertNotEquals(
            base.digest,
            PackageCacheIdentityCalculator.compute(
                baseInput(resourceIdentities = listOf(PackageResourceIdentity("resource.frequency-drive.vector", "resource-sha256:changed"))),
            ).digest,
        )
        assertNotEquals(base.digest, PackageCacheIdentityCalculator.compute(baseInput(bindingPolicyIdentity = "binding-policy:compact")).digest)
        assertNotEquals(base.digest, PackageCacheIdentityCalculator.compute(baseInput(activeProfile = "customer-compact")).digest)
    }

    @Test
    fun `cache identity is deterministic for identical inputs`() {
        val first = PackageCacheIdentityCalculator.compute(baseInput())
        val second = PackageCacheIdentityCalculator.compute(baseInput())

        assertEquals(first, second)
    }

    private fun baseInput(
        descriptorContentIdentity: String = "descriptor-sha256:abc",
        resourceIdentities: List<PackageResourceIdentity> = listOf(
            PackageResourceIdentity("resource.frequency-drive.vector", "resource-sha256:def"),
        ),
        bindingPolicyIdentity: String = "binding-policy:iec-default",
        activeProfile: String = "iec60617",
    ): PackageCacheIdentityInput = PackageCacheIdentityInput(
        resolvedPackage = ResolvedPackageFact(
            packageId = "com.athena.example.representation.drive.iec",
            kind = PackageResolutionPackageKind.REPRESENTATION,
            version = "1.0.0",
            descriptorPath = Path.of("sample-project/packages/representation/drive/package.yaml").toAbsolutePath().normalize(),
            dependencies = emptyList(),
            validationStatus = PackageValidationStatus.VALID,
            diagnostics = emptyList(),
            selectedRoot = LocalPackageRegistryRoot(
                path = Path.of("sample-project/packages").toAbsolutePath().normalize(),
                kind = PackageRegistryRootKind.PROJECT_LOCAL,
            ),
        ),
        descriptorContentIdentity = descriptorContentIdentity,
        resourceIdentities = resourceIdentities,
        bindingPolicyIdentity = bindingPolicyIdentity,
        activeProfile = activeProfile,
    )
}
