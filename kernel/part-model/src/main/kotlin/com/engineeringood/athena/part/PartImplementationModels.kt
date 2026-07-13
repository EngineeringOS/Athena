package com.engineeringood.athena.part

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Vendor-specific realization of one vendor-neutral engineering concept.
 *
 * The concept id remains the semantic target. Vendor identity and part number only describe one
 * implementation path for that concept.
 */
data class PartImplementationDefinition(
    val implementationId: PartImplementationId,
    val conceptId: EngineeringConceptId,
    val vendorId: VendorId,
    val vendorPartNumber: VendorPartNumber,
    val displayName: String,
    val summary: String? = null,
)

/**
 * Read-only resolved implementation selected for one canonical semantic subject.
 *
 * The `semanticSubjectId` still points at the canonical authored object in `Engineering IR`.
 * Vendor mapping adds implementation metadata above that canonical state and does not become a new
 * mutation path.
 */
data class ResolvedPartImplementation(
    val semanticSubjectId: StableSemanticIdentity,
    val implementation: PartImplementationDefinition,
)
