package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.StableSemanticIdentity

data class ProjectionPhysicalSize(
    val widthMillimeters: Int,
    val heightMillimeters: Int,
    val depthMillimeters: Int,
)

/**
 * Inspectable downstream component knowledge attached to one projection document.
 *
 * The semantic source of truth remains in Engineering Reality. This record carries resolved concept,
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
