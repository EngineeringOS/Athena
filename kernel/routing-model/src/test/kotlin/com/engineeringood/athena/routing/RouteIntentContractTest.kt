package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteIntentContractTest {
    @Test
    fun `route intent supports through avoid bundle and priority constraints without final geometry`() {
        val provenance = provenance("file.athena", 3, 5, 3, 42)
        val routeIntent = RouteIntent(
            intentId = RouteIntentId("route:connection:1"),
            connectionId = ElectricalConnectionId("connection:1"),
            sourcePortReference = reference("port:source", provenance),
            targetPortReference = reference("port:target", provenance),
            constraints = listOf(
                RouteIntentConstraint(
                    constraintId = RouteIntentConstraintId("constraint:through"),
                    kind = RouteIntentConstraintKind.THROUGH,
                    owner = RouteIntentConstraintOwner.REPRESENTATION,
                    strength = RouteIntentConstraintStrength.PREFERRED,
                    target = RouteIntentConstraintTarget.Reference(reference("channel:main", provenance)),
                    provenance = provenance,
                ),
                RouteIntentConstraint(
                    constraintId = RouteIntentConstraintId("constraint:avoid"),
                    kind = RouteIntentConstraintKind.AVOID,
                    owner = RouteIntentConstraintOwner.PHYSICAL,
                    strength = RouteIntentConstraintStrength.REQUIRED,
                    target = RouteIntentConstraintTarget.Reference(reference("zone:hot", provenance)),
                    provenance = provenance,
                ),
                RouteIntentConstraint(
                    constraintId = RouteIntentConstraintId("constraint:bundle"),
                    kind = RouteIntentConstraintKind.BUNDLE,
                    owner = RouteIntentConstraintOwner.SEMANTIC,
                    strength = RouteIntentConstraintStrength.REQUIRED,
                    target = RouteIntentConstraintTarget.Bundle(RouteBundleId("bundle:main")),
                    provenance = provenance,
                ),
                RouteIntentConstraint(
                    constraintId = RouteIntentConstraintId("constraint:priority"),
                    kind = RouteIntentConstraintKind.PRIORITY,
                    owner = RouteIntentConstraintOwner.LAYOUT_PREFERENCE,
                    strength = RouteIntentConstraintStrength.PREFERRED,
                    target = RouteIntentConstraintTarget.Priority(2),
                    provenance = provenance,
                ),
            ),
            compatibilityEvidence = emptyList(),
            provenance = provenance,
        )

        val fieldNames = RouteIntent::class.java.declaredFields.map { it.name }.toSet()

        assertEquals(4, routeIntent.constraints.size)
        assertTrue(routeIntent.constraints.any { it.kind == RouteIntentConstraintKind.THROUGH })
        assertTrue(routeIntent.constraints.any { it.kind == RouteIntentConstraintKind.AVOID })
        assertTrue(routeIntent.constraints.any { it.kind == RouteIntentConstraintKind.BUNDLE })
        assertTrue(routeIntent.constraints.any { it.kind == RouteIntentConstraintKind.PRIORITY })
        assertFalse(fieldNames.contains("segments"))
        assertFalse(fieldNames.contains("routePoints"))
        assertFalse(fieldNames.contains("coordinates"))
    }

    @Test
    fun `canonical route intent compilation sorts intents and bundles deterministically`() {
        val provenance = provenance("file.athena", 1, 1, 1, 10)
        val late = RouteIntent(
            intentId = RouteIntentId("route:late"),
            connectionId = ElectricalConnectionId("connection:late"),
            sourcePortReference = reference("port:late:a", provenance),
            targetPortReference = reference("port:late:b", provenance),
            constraints = emptyList(),
            compatibilityEvidence = emptyList(),
            provenance = provenance,
        )
        val early = RouteIntent(
            intentId = RouteIntentId("route:early"),
            connectionId = ElectricalConnectionId("connection:early"),
            sourcePortReference = reference("port:early:a", provenance),
            targetPortReference = reference("port:early:b", provenance),
            constraints = emptyList(),
            compatibilityEvidence = emptyList(),
            provenance = provenance,
        )
        val bundleZ = RouteBundle(
            bundleId = RouteBundleId("bundle:z"),
            members = listOf(RouteBundleMember(late.intentId, late.connectionId, provenance)),
            constraints = emptyList(),
            provenance = provenance,
        )
        val bundleA = RouteBundle(
            bundleId = RouteBundleId("bundle:a"),
            members = listOf(RouteBundleMember(early.intentId, early.connectionId, provenance)),
            constraints = emptyList(),
            provenance = provenance,
        )

        val compilation = RouteIntentCompilation.canonical(
            routeIntents = listOf(late, early),
            routeBundles = listOf(bundleZ, bundleA),
            provenance = provenance,
        )

        assertEquals(listOf("route:early", "route:late"), compilation.routeIntents.map { it.intentId.value })
        assertEquals(listOf("bundle:a", "bundle:z"), compilation.routeBundles.map { it.bundleId.value })
    }

    private fun reference(id: String, provenance: SourceProvenance): EngineeringReference {
        return EngineeringReference(
            authoredPath = id.split(':').filter(String::isNotBlank),
            resolvedIdentity = StableSemanticIdentity(id),
            provenance = provenance,
        )
    }

    private fun provenance(
        file: String,
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
    ): SourceProvenance = SourceProvenance(file, startLine, startColumn, endLine, endColumn)
}
