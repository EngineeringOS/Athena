package com.engineeringood.athena.packageplatform

/** Non-overridable M35 admission budget shared by package capture, resources, SVG, evidence, and tests. */
data class PackageAdmissionLimits(
    val maxResolvedPackages: Int = 64,
    val maxGovernedSourceUnitsPerPackage: Int = 1_024,
    val maxDeclaredResourcesPerPackage: Int = 1_024,
    val maxSvgBytes: Long = 262_144,
    val maxAdmittedBytesPerPackage: Long = 32L * 1024L * 1024L,
    val maxAdmittedBytesPerRepository: Long = 256L * 1024L * 1024L,
    val maxSvgDomDepth: Int = 32,
    val maxSvgElements: Int = 512,
    val maxExpandedUseInstances: Int = 256,
    val maxSvgPathSegments: Int = 8_192,
    val maxEmittedPrimitives: Int = 256,
    val maxWorkUnitsPerPackage: Int = 100_000,
    val maxWorkUnitsPerRepository: Int = 1_000_000,
) {
    companion object {
        val STANDARD: PackageAdmissionLimits = PackageAdmissionLimits()
    }
}
