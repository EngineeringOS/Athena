package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.RouteBundleId
import com.engineeringood.athena.routing.RouteBundleMember
import com.engineeringood.athena.routing.RouteIntentCompilation
import com.engineeringood.athena.routing.RouteIntentCompatibilityEvidence
import com.engineeringood.athena.routing.RouteIntentConstraint
import com.engineeringood.athena.routing.RouteIntentConstraintKind
import com.engineeringood.athena.routing.RouteIntentConstraintOwner
import com.engineeringood.athena.routing.RouteIntentConstraintStrength
import com.engineeringood.athena.routing.RouteIntentConstraintTarget
import com.engineeringood.athena.routing.RouteIntentDeclaration
import com.engineeringood.athena.routing.RouteIntentId
import com.engineeringood.athena.routing.RouteIntent
import com.engineeringood.athena.routing.RouteBundle

/** Lowers transient Connection IR into compiler-owned route intent and route bundle facts. */
class RouteIntentLowerer {
    fun lower(
        connectionIr: ConnectionIr,
        declarations: List<RouteIntentDeclaration> = emptyList(),
    ): RouteIntentCompilation {
        val declarationsByConnectionId = declarations.groupBy { declaration -> declaration.connectionId.value }
        val networkMemberships = connectionIr.networks.flatMap { network ->
            network.members.mapNotNull { member ->
                val connectionId = member.connectionReference.resolvedIdentity.value
                connectionId to network
            }
        }.groupBy({ it.first }, { it.second })

        val routeIntents = connectionIr.connections.sortedBy { connection -> connection.id.value }.map { connection ->
            val connectionId = connection.id.value
            val authoredConstraints = declarationsByConnectionId[connectionId]
                .orEmpty()
                .sortedBy(::declarationSortKey)
                .flatMap { declaration -> declaration.constraints }

            val authoredBundleConstraints = authoredConstraints
                .filter { constraint -> constraint.kind == RouteIntentConstraintKind.BUNDLE }
                .mapNotNull { constraint ->
                    when (val target = constraint.target) {
                        is RouteIntentConstraintTarget.Bundle -> constraint to target.bundleId
                        else -> null
                    }
                }

            val bundleTargets = buildList {
                authoredBundleConstraints
                    .mapTo(this) { (_, bundleId) -> bundleId }
                networkMemberships[connectionId].orEmpty()
                    .map { network -> RouteBundleId("bundle:${network.id.value}") }
                    .forEach { bundleId -> add(bundleId) }
            }.distinct()

            val bundleId = when {
                bundleTargets.isEmpty() -> RouteBundleId("bundle:${connectionId}")
                bundleTargets.size == 1 -> bundleTargets.single()
                else -> error("Route intent lowering found conflicting bundle targets for `$connectionId`.")
            }

            val bundleConstraint = authoredBundleConstraints
                .firstOrNull { (_, bundleTarget) -> bundleTarget == bundleId }
                ?.first
                ?: RouteIntentConstraint(
                    constraintId = com.engineeringood.athena.routing.RouteIntentConstraintId("route-intent:$connectionId:bundle:${bundleId.value}"),
                    kind = RouteIntentConstraintKind.BUNDLE,
                    owner = RouteIntentConstraintOwner.SEMANTIC,
                    strength = RouteIntentConstraintStrength.REQUIRED,
                    target = RouteIntentConstraintTarget.Bundle(bundleId),
                    provenance = networkMemberships[connectionId].orEmpty().firstOrNull()?.provenance ?: connection.provenance,
                )

            val nonBundleConstraints = authoredConstraints
                .filterNot { constraint -> constraint.kind == RouteIntentConstraintKind.BUNDLE }

            val compatibilityEvidence = networkMemberships[connectionId]
                .orEmpty()
                .flatMap { network ->
                    network.compatibilityEvidence.map { evidence ->
                        RouteIntentCompatibilityEvidence(
                            kind = evidence.kind,
                            value = evidence.value,
                            owner = RouteIntentConstraintOwner.SEMANTIC,
                            strength = RouteIntentConstraintStrength.REQUIRED,
                            provenance = evidence.provenance,
                        )
                    }
                }

            RouteIntent(
                intentId = RouteIntentId("route:$connectionId"),
                connectionId = ElectricalConnectionId(connectionId),
                sourcePortReference = connection.from.toEngineeringReference(),
                targetPortReference = connection.to.toEngineeringReference(),
                constraints = (nonBundleConstraints + bundleConstraint)
                    .sortedBy(::routeConstraintSortKey),
                compatibilityEvidence = compatibilityEvidence
                    .sortedBy(::routeCompatibilityEvidenceSortKey),
                provenance = connection.provenance,
            )
        }

        val bundles = routeIntents
            .flatMap { intent ->
                val bundleIds = intent.constraints
                    .filter { constraint -> constraint.kind == RouteIntentConstraintKind.BUNDLE }
                    .mapNotNull { constraint ->
                        when (val target = constraint.target) {
                            is RouteIntentConstraintTarget.Bundle -> target.bundleId
                            else -> null
                        }
                    }
                bundleIds.ifEmpty { listOf(RouteBundleId("bundle:${intent.connectionId.value}")) }
                    .map { bundleId -> bundleId to intent }
            }
            .groupBy({ it.first }, { it.second })
            .map { (bundleId, intents) ->
                val ordered = intents.sortedBy(::routeIntentSortKey)
                val memberConstraints = ordered.flatMap { intent ->
                    intent.constraints.filter { constraint ->
                        constraint.kind == RouteIntentConstraintKind.BUNDLE &&
                            (constraint.target as? RouteIntentConstraintTarget.Bundle)?.bundleId == bundleId
                    }
                }.distinctBy { constraint -> constraint.constraintId.value }
                RouteBundle(
                    bundleId = bundleId,
                    members = ordered.map { intent ->
                        RouteBundleMember(
                            routeIntentId = intent.intentId,
                            connectionId = intent.connectionId,
                            provenance = intent.provenance,
                        )
                    },
                    constraints = memberConstraints.sortedBy(::routeConstraintSortKey),
                    provenance = ordered.first().provenance,
                )
            }

        return RouteIntentCompilation.canonical(
            routeIntents = routeIntents,
            routeBundles = bundles,
            provenance = connectionIr.provenance,
        )
    }
}

private fun com.engineeringood.athena.compiler.ConnectionIrReference.toEngineeringReference(): EngineeringReference =
    EngineeringReference(
        authoredPath = authoredPath,
        resolvedIdentity = resolvedIdentity,
        provenance = provenance,
    )

private fun declarationSortKey(declaration: RouteIntentDeclaration): String = listOf(
    declaration.provenance.file,
    declaration.provenance.startLine.toString().padStart(6, '0'),
    declaration.provenance.startColumn.toString().padStart(6, '0'),
    declaration.provenance.endLine.toString().padStart(6, '0'),
    declaration.provenance.endColumn.toString().padStart(6, '0'),
    declaration.connectionId.value,
).joinToString(separator = "|")

private fun routeIntentSortKey(intent: RouteIntent): String = listOf(
    intent.intentId.value,
    intent.connectionId.value,
    intent.sourcePortReference.resolvedIdentity?.value ?: intent.sourcePortReference.authoredPath.joinToString("."),
    intent.targetPortReference.resolvedIdentity?.value ?: intent.targetPortReference.authoredPath.joinToString("."),
    intent.provenance.file,
    intent.provenance.startLine.toString().padStart(6, '0'),
    intent.provenance.startColumn.toString().padStart(6, '0'),
).joinToString(separator = "|")

private fun routeConstraintSortKey(constraint: RouteIntentConstraint): String = listOf(
    constraint.kind.ordinal.toString().padStart(2, '0'),
    constraint.constraintId.value,
    constraint.owner.ordinal.toString().padStart(2, '0'),
    constraint.strength.ordinal.toString().padStart(2, '0'),
    when (val target = constraint.target) {
        is RouteIntentConstraintTarget.Reference ->
            target.reference.resolvedIdentity?.value ?: target.reference.authoredPath.joinToString(".")
        is RouteIntentConstraintTarget.Bundle -> target.bundleId.value
        is RouteIntentConstraintTarget.Priority -> target.value.toString().padStart(4, '0')
    },
    constraint.provenance.file,
    constraint.provenance.startLine.toString().padStart(6, '0'),
    constraint.provenance.startColumn.toString().padStart(6, '0'),
    constraint.provenance.endLine.toString().padStart(6, '0'),
    constraint.provenance.endColumn.toString().padStart(6, '0'),
).joinToString(separator = "|")

private fun routeCompatibilityEvidenceSortKey(evidence: RouteIntentCompatibilityEvidence): String = listOf(
    evidence.kind,
    evidence.value,
    evidence.owner.ordinal.toString().padStart(2, '0'),
    evidence.strength.ordinal.toString().padStart(2, '0'),
    evidence.provenance.file,
    evidence.provenance.startLine.toString().padStart(6, '0'),
    evidence.provenance.startColumn.toString().padStart(6, '0'),
    evidence.provenance.endLine.toString().padStart(6, '0'),
    evidence.provenance.endColumn.toString().padStart(6, '0'),
).joinToString(separator = "|")
