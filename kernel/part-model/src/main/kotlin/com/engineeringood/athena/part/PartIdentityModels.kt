package com.engineeringood.athena.part

/**
 * Stable identifier for one vendor namespace.
 *
 * Vendors publish implementations of engineering concepts. They do not define the semantic type
 * system owned by Athena.
 */
@JvmInline
value class VendorId(val value: String) {
    override fun toString(): String = value
}

/**
 * Vendor-facing part number or catalog number.
 *
 * This value records how one implementation is identified in one vendor catalog. It is not a
 * replacement for `EngineeringConceptId`.
 */
@JvmInline
value class VendorPartNumber(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable Athena-owned identifier for one implementation mapping entry.
 *
 * This identity stays separate from vendor part numbers so one concept can support multiple
 * implementations without treating the vendor catalog as the concept model.
 */
@JvmInline
value class PartImplementationId(val value: String) {
    override fun toString(): String = value
}
