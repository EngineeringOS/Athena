package com.engineeringood.athena.projection

/**
 * Projection-local identifier for one node in a derived projection document.
 *
 * The identifier is stable only within one projection view and remains secondary to canonical
 * semantic identity.
 */
@JvmInline
value class ProjectionNodeId(val value: String) {
    override fun toString(): String = value
}

/** Projection-local identifier for one ConnectionProjection in a derived view. */
@JvmInline
value class ConnectionProjectionId(val value: String) {
    override fun toString(): String = value
}

/**
 * Projection-local identifier for one governed sheet inside a derived projection document.
 *
 * Sheet identity is stable within one projection view and remains separate from canonical
 * engineering identity.
 */
@JvmInline
value class ProjectionSheetId(val value: String) {
    override fun toString(): String = value
}
