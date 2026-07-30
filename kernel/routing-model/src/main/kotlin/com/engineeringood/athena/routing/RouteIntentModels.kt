package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance

/** Stable identity for one compiler-owned route intent. */
@JvmInline
value class RouteIntentId(val value: String) {
    init {
        require(value.isNotBlank()) { "Route intent id must not be blank." }
    }

    override fun toString(): String = value
}

/** Stable identity for one compiler-owned route-intent constraint. */
@JvmInline
value class RouteIntentConstraintId(val value: String) {
    init {
        require(value.isNotBlank()) { "Route intent constraint id must not be blank." }
    }

    override fun toString(): String = value
}

/** Constraint ownership for one route intent or route bundle. */
enum class RouteIntentConstraintOwner {
    SEMANTIC,
    REPRESENTATION,
    PHYSICAL,
    LAYOUT_PREFERENCE,
}

/** Strength attached to one route intent or bundle constraint. */
enum class RouteIntentConstraintStrength {
    REQUIRED,
    PREFERRED,
    OPTIONAL,
}

/** Constraint families carried by route intent before route facts exist. */
enum class RouteIntentConstraintKind {
    THROUGH,
    AVOID,
    BUNDLE,
    PRIORITY,
}

/** Typed payload for one route-intent constraint. */
sealed interface RouteIntentConstraintTarget {
    data class Reference(val reference: EngineeringReference) : RouteIntentConstraintTarget

    data class Bundle(val bundleId: RouteBundleId) : RouteIntentConstraintTarget

    data class Priority(val value: Int) : RouteIntentConstraintTarget {
        init {
            require(value >= 0) { "Route intent priority must not be negative." }
        }
    }
}

/** One route-intent constraint with ownership, strength, and provenance. */
data class RouteIntentConstraint(
    val constraintId: RouteIntentConstraintId,
    val kind: RouteIntentConstraintKind,
    val owner: RouteIntentConstraintOwner,
    val strength: RouteIntentConstraintStrength,
    val target: RouteIntentConstraintTarget,
    val provenance: SourceProvenance,
) {
    init {
        requireMatchesKind()
    }

    private fun requireMatchesKind() {
        when (kind) {
            RouteIntentConstraintKind.THROUGH,
            RouteIntentConstraintKind.AVOID,
                -> require(target is RouteIntentConstraintTarget.Reference) {
                "Through and avoid constraints must point at a reference target."
            }

            RouteIntentConstraintKind.BUNDLE -> require(target is RouteIntentConstraintTarget.Bundle) {
                "Bundle constraints must point at a bundle target."
            }

            RouteIntentConstraintKind.PRIORITY -> require(target is RouteIntentConstraintTarget.Priority) {
                "Priority constraints must point at a priority target."
            }
        }
    }

    internal fun stableKey(): String = listOf(
        kind.ordinal.toString().padStart(2, '0'),
        constraintId.value,
        owner.ordinal.toString().padStart(2, '0'),
        strength.ordinal.toString().padStart(2, '0'),
        when (target) {
            is RouteIntentConstraintTarget.Reference ->
                target.reference.resolvedIdentity?.value
                    ?: target.reference.authoredPath.joinToString(".")
            is RouteIntentConstraintTarget.Bundle -> target.bundleId.value
            is RouteIntentConstraintTarget.Priority -> target.value.toString().padStart(4, '0')
        },
        provenance.file,
        provenance.startLine.toString().padStart(6, '0'),
        provenance.startColumn.toString().padStart(6, '0'),
        provenance.endLine.toString().padStart(6, '0'),
        provenance.endColumn.toString().padStart(6, '0'),
    ).joinToString(separator = "|")
}

/** Compatibility evidence preserved while lowering connectivity into route intent. */
data class RouteIntentCompatibilityEvidence(
    val kind: String,
    val value: String,
    val owner: RouteIntentConstraintOwner,
    val strength: RouteIntentConstraintStrength,
    val provenance: SourceProvenance,
) {
    init {
        require(kind.isNotBlank()) { "Route intent compatibility evidence kind must not be blank." }
        require(value.isNotBlank()) { "Route intent compatibility evidence value must not be blank." }
    }

    internal fun stableKey(): String = listOf(
        kind,
        value,
        owner.ordinal.toString().padStart(2, '0'),
        strength.ordinal.toString().padStart(2, '0'),
        provenance.file,
        provenance.startLine.toString().padStart(6, '0'),
        provenance.startColumn.toString().padStart(6, '0'),
        provenance.endLine.toString().padStart(6, '0'),
        provenance.endColumn.toString().padStart(6, '0'),
    ).joinToString(separator = "|")
}

/** Compiler-owned source declaration used to attach authored route constraints to one connection. */
data class RouteIntentDeclaration(
    val connectionId: ElectricalConnectionId,
    val constraints: List<RouteIntentConstraint> = emptyList(),
    val provenance: SourceProvenance,
)

/** One compiler-owned route intent before route geometry exists. */
data class RouteIntent(
    val intentId: RouteIntentId,
    val connectionId: ElectricalConnectionId,
    val sourcePortReference: EngineeringReference,
    val targetPortReference: EngineeringReference,
    val constraints: List<RouteIntentConstraint>,
    val compatibilityEvidence: List<RouteIntentCompatibilityEvidence>,
    val provenance: SourceProvenance,
) {
    internal fun stableKey(): String = listOf(
        intentId.value,
        connectionId.value,
        sourcePortReference.resolvedIdentity?.value ?: sourcePortReference.authoredPath.joinToString("."),
        targetPortReference.resolvedIdentity?.value ?: targetPortReference.authoredPath.joinToString("."),
        provenance.file,
        provenance.startLine.toString().padStart(6, '0'),
        provenance.startColumn.toString().padStart(6, '0'),
    ).joinToString(separator = "|")
}

/** One ordered route bundle derived from route intent. */
data class RouteBundle(
    val bundleId: RouteBundleId,
    val members: List<RouteBundleMember>,
    val constraints: List<RouteIntentConstraint>,
    val provenance: SourceProvenance,
) {
    init {
        require(members.isNotEmpty()) { "Route bundles require at least one member." }
        require(members.map(RouteBundleMember::routeIntentId).distinct().size == members.size) {
            "Route bundles must not contain duplicate route intent ids."
        }
    }

    internal fun stableKey(): String = listOf(
        bundleId.value,
        members.joinToString(separator = ",") { member -> member.routeIntentId.value },
        provenance.file,
        provenance.startLine.toString().padStart(6, '0'),
        provenance.startColumn.toString().padStart(6, '0'),
    ).joinToString(separator = "|")
}

/** Bundle membership preserved while lowering route intent into grouped bundles. */
data class RouteBundleMember(
    val routeIntentId: RouteIntentId,
    val connectionId: ElectricalConnectionId,
    val provenance: SourceProvenance,
)

/** Immutable compiler-owned route-intent snapshot with deterministic ordering. */
data class RouteIntentCompilation(
    val routeIntents: List<RouteIntent>,
    val routeBundles: List<RouteBundle>,
    val provenance: SourceProvenance,
) {
    init {
        require(routeIntents.map(RouteIntent::intentId).distinct().size == routeIntents.size) {
            "Route intent compilations must not contain duplicate route intent ids."
        }
    }

    companion object {
        fun canonical(
            routeIntents: List<RouteIntent>,
            routeBundles: List<RouteBundle>,
            provenance: SourceProvenance,
        ): RouteIntentCompilation = RouteIntentCompilation(
            routeIntents = routeIntents.sortedBy(RouteIntent::stableKey),
            routeBundles = routeBundles.sortedBy(RouteBundle::stableKey),
            provenance = provenance,
        )
    }
}
