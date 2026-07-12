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

/**
 * Projection-local identifier for one connection in a derived projection document.
 *
 * The identifier is stable only within one projection view and remains secondary to canonical
 * semantic identity.
 */
@JvmInline
value class ProjectionConnectionId(val value: String) {
    override fun toString(): String = value
}

/**
 * Projection-local identifier for one label in a derived projection document.
 *
 * The identifier is stable only within one projection view and remains secondary to canonical
 * semantic identity.
 */
@JvmInline
value class ProjectionLabelId(val value: String) {
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
