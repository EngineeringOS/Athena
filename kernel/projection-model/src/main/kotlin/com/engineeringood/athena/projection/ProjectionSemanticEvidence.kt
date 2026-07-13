package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Minimal physical-size payload carried as downstream projection evidence.
 *
 * This record stays presentation-facing. It does not become layout, geometry, or renderer truth.
 */
data class ProjectionPhysicalSize(
    val widthMillimeters: Int,
    val heightMillimeters: Int,
    val depthMillimeters: Int,
)

/**
 * Inspectable downstream component-knowledge evidence attached to one projection document.
 *
 * The semantic source of truth remains in `Engineering IR`, while M14 contributes resolved concept,
 * implementation, and minimal physical-trait data for later projection and presentation consumers.
 */
data class ProjectionResolvedSubject(
    val semanticId: StableSemanticIdentity,
    val conceptId: String,
    val classificationKeys: Set<String> = emptySet(),
    val implementationId: String? = null,
    val vendorPartNumber: String? = null,
    val physicalSize: ProjectionPhysicalSize? = null,
    val mountingTypeId: String? = null,
    val installationMarkerIds: Set<String> = emptySet(),
)
