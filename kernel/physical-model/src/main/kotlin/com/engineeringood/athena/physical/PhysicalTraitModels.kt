package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Minimal reusable physical size contract expressed in millimetres.
 *
 * This size stays component-knowledge oriented. It does not imply layout placement, projection
 * coordinates, or renderer-owned scene bounds.
 */
data class PhysicalSize(
    val widthMillimeters: Int,
    val heightMillimeters: Int,
    val depthMillimeters: Int,
)

/**
 * Stable identifier for one mounting type used by a reusable physical trait definition.
 */
@JvmInline
value class PhysicalMountingTypeId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one basic installation marker published by a physical trait definition.
 */
@JvmInline
value class PhysicalInstallationMarkerId(val value: String) {
    override fun toString(): String = value
}

/**
 * Minimal reusable physical-trait contract for one semantic subject family or implementation.
 *
 * This definition covers size, mounting type, and basic installation markers only. It does not
 * replace layout ownership, scene calculation, or renderer geometry.
 */
data class PhysicalTraitDefinition(
    val displayName: String,
    val size: PhysicalSize,
    val mountingTypeId: PhysicalMountingTypeId,
    val installationMarkerIds: Set<PhysicalInstallationMarkerId> = emptySet(),
    val summary: String? = null,
)

/**
 * Read-only resolved physical-trait knowledge for one canonical semantic subject.
 *
 * The `semanticSubjectId` still points at the canonical authored object in `Engineering IR`.
 * Physical-trait resolution adds reusable installation knowledge above that canonical state and
 * does not become layout or geometry truth.
 */
data class ResolvedPhysicalTraitDefinition(
    val semanticSubjectId: StableSemanticIdentity,
    val definition: PhysicalTraitDefinition,
)
