package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Minimal physical-size payload carried as downstream presentation evidence.
 *
 * This record remains inspectable metadata for presentation consumers. It does not replace any
 * projection, layout, or renderer-owned geometry.
 */
data class PresentationPhysicalSize(
    val widthMillimeters: Int,
    val heightMillimeters: Int,
    val depthMillimeters: Int,
)

/**
 * Inspectable downstream component-knowledge evidence attached to one presentation document.
 *
 * This keeps resolved concept identity and minimal physical traits available to downstream packs
 * and UI consumers without promoting presentation into semantic authority.
 */
data class PresentationResolvedSubject(
    val semanticId: StableSemanticIdentity,
    val conceptId: String,
    val classificationKeys: Set<String> = emptySet(),
    val implementationId: String? = null,
    val vendorPartNumber: String? = null,
    val physicalSize: PresentationPhysicalSize? = null,
    val mountingTypeId: String? = null,
    val installationMarkerIds: Set<String> = emptySet(),
)
