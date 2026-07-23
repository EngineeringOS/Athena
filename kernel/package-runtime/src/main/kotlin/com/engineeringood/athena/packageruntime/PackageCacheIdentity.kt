package com.engineeringood.athena.packageruntime

import java.security.MessageDigest

data class PackageResourceIdentity(
    val resourceId: String,
    val contentIdentity: String,
)

data class PackageCacheIdentityInput(
    val resolvedPackage: ResolvedPackageFact,
    val descriptorContentIdentity: String,
    val resourceIdentities: List<PackageResourceIdentity>,
    val bindingPolicyIdentity: String,
    val activeProfile: String,
)

data class PackageCacheIdentity(
    val digest: String,
    val components: Map<String, String>,
)

object PackageCacheIdentityCalculator {
    fun compute(input: PackageCacheIdentityInput): PackageCacheIdentity {
        val components = linkedMapOf(
            "packageId" to input.resolvedPackage.packageId,
            "packageKind" to input.resolvedPackage.kind.name,
            "version" to input.resolvedPackage.version,
            "descriptorPath" to stablePathKey(input.resolvedPackage.descriptorPath),
            "descriptorContentIdentity" to input.descriptorContentIdentity,
            "resourceIdentities" to input.resourceIdentities
                .sortedBy { it.resourceId }
                .joinToString(";") { "${it.resourceId}=${it.contentIdentity}" },
            "registryRoot" to stablePathKey(input.resolvedPackage.selectedRoot.path),
            "bindingPolicyIdentity" to input.bindingPolicyIdentity,
            "activeProfile" to input.activeProfile,
        )
        val canonical = components.entries.joinToString("\n") { (key, value) -> "$key=$value" }
        return PackageCacheIdentity(
            digest = sha256(canonical),
            components = components,
        )
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}
