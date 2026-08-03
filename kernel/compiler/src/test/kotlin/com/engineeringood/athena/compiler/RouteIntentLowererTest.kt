package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.EngineeringConnectivityPortDirection
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.RouteIntentConstraint
import com.engineeringood.athena.routing.RouteIntentConstraintKind
import com.engineeringood.athena.routing.RouteIntentConstraintOwner
import com.engineeringood.athena.routing.RouteIntentConstraintStrength
import com.engineeringood.athena.routing.RouteIntentConstraintTarget
import com.engineeringood.athena.routing.RouteIntentDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteIntentLowererTest {
    @Test
    fun `lowers typed connections into deterministic route intents and bundles`() {
        val provenance = provenance("sample.athena", 1, 1, 1, 10)
        val connectionIr = connectionIr(provenance)
        val lowerer = RouteIntentLowerer()
        val declaration = RouteIntentDeclaration(
            connectionId = ElectricalConnectionId("connection:beta"),
            constraints = listOf(
                RouteIntentConstraint(
                    constraintId = com.engineeringood.athena.routing.RouteIntentConstraintId("constraint:beta:through"),
                    kind = RouteIntentConstraintKind.THROUGH,
                    owner = RouteIntentConstraintOwner.REPRESENTATION,
                    strength = RouteIntentConstraintStrength.PREFERRED,
                    target = RouteIntentConstraintTarget.Reference(engineeringReference("channel:main", provenance)),
                    provenance = provenance,
                ),
                RouteIntentConstraint(
                    constraintId = com.engineeringood.athena.routing.RouteIntentConstraintId("constraint:beta:avoid"),
                    kind = RouteIntentConstraintKind.AVOID,
                    owner = RouteIntentConstraintOwner.PHYSICAL,
                    strength = RouteIntentConstraintStrength.REQUIRED,
                    target = RouteIntentConstraintTarget.Reference(engineeringReference("zone:hot", provenance)),
                    provenance = provenance,
                ),
                RouteIntentConstraint(
                    constraintId = com.engineeringood.athena.routing.RouteIntentConstraintId("constraint:beta:priority"),
                    kind = RouteIntentConstraintKind.PRIORITY,
                    owner = RouteIntentConstraintOwner.LAYOUT_PREFERENCE,
                    strength = RouteIntentConstraintStrength.PREFERRED,
                    target = RouteIntentConstraintTarget.Priority(1),
                    provenance = provenance,
                ),
            ),
            provenance = provenance,
        )

        val compilation = lowerer.lower(connectionIr, listOf(declaration))

        assertEquals(
            listOf("route:connection:alpha", "route:connection:beta"),
            compilation.routeIntents.map { it.intentId.value },
        )
        assertEquals(listOf("bundle:network:main"), compilation.routeBundles.map { it.bundleId.value })

        val beta = compilation.routeIntents.single { it.connectionId.value == "connection:beta" }
        assertTrue(beta.constraints.any { it.kind == RouteIntentConstraintKind.THROUGH })
        assertTrue(beta.constraints.any { it.kind == RouteIntentConstraintKind.AVOID })
        assertTrue(beta.constraints.any { it.kind == RouteIntentConstraintKind.PRIORITY })
        assertTrue(beta.constraints.any { it.kind == RouteIntentConstraintKind.BUNDLE })
        assertTrue(beta.compatibilityEvidence.isNotEmpty())
        assertEquals("sample.athena", beta.provenance.file)

        val bundle = compilation.routeBundles.single()
        assertEquals(
            listOf("route:connection:alpha", "route:connection:beta"),
            bundle.members.map { it.routeIntentId.value },
        )
    }

    private fun connectionIr(provenance: SourceProvenance): ConnectionIr {
        val alphaFrom = connectionReference("port:alpha:from", provenance)
        val alphaTo = connectionReference("port:alpha:to", provenance)
        val betaFrom = connectionReference("port:beta:from", provenance)
        val betaTo = connectionReference("port:beta:to", provenance)
        val alpha = StableSemanticIdentity("connection:alpha")
        val beta = StableSemanticIdentity("connection:beta")
        val network = ConnectionIrNetwork(
            id = StableSemanticIdentity("network:main"),
            name = "main",
            members = listOf(
                ConnectionIrNetworkMember(
                    connectionReference = connectionReference("connection:alpha", provenance),
                    fromPortReference = alphaFrom,
                    toPortReference = alphaTo,
                ),
                ConnectionIrNetworkMember(
                    connectionReference = connectionReference("connection:beta", provenance),
                    fromPortReference = betaFrom,
                    toPortReference = betaTo,
                ),
            ),
            junctions = listOf(
                ConnectionIrNetworkJunction(
                    id = StableSemanticIdentity("junction:main"),
                    sharedPortReference = alphaTo,
                    memberConnectionReferences = listOf(
                        connectionReference("connection:alpha", provenance),
                        connectionReference("connection:beta", provenance),
                    ),
                    provenance = provenance,
                ),
            ),
            compatibilityEvidence = listOf(
                ConnectionIrCompatibilityEvidence(
                    kind = "shared-direction",
                    value = "output",
                    owner = ConnectionIrConstraintOwner.SEMANTIC,
                    strength = ConnectionIrConstraintStrength.REQUIRED,
                    provenance = provenance,
                ),
            ),
            provenance = provenance,
        )

        return ConnectionIr(
            entities = listOf(
                entity("component:alpha", "Alpha"),
                entity("component:beta", "Beta"),
            ),
            ports = listOf(
                port("port:alpha:from", "component:alpha", provenance),
                port("port:alpha:to", "component:alpha", provenance),
                port("port:beta:from", "component:beta", provenance),
                port("port:beta:to", "component:beta", provenance),
            ),
            connections = listOf(
                ConnectionIrConnection(
                    id = alpha,
                    from = alphaFrom,
                    to = alphaTo,
                    provenance = provenance,
                ),
                ConnectionIrConnection(
                    id = beta,
                    from = betaFrom,
                    to = betaTo,
                    provenance = provenance,
                ),
            ),
            networks = listOf(network),
            provenance = provenance,
        )
    }

    private fun entity(id: String, name: String): ConnectionIrEntity = ConnectionIrEntity(
        id = StableSemanticIdentity(id),
        name = name,
        kind = "Switch",
        provenance = provenance("sample.athena", 1, 1, 1, 10),
    )

    private fun port(id: String, ownerId: String, provenance: SourceProvenance): ConnectionIrPort = ConnectionIrPort(
        id = StableSemanticIdentity(id),
        ownerId = StableSemanticIdentity(ownerId),
        name = id.substringAfterLast(':'),
        compatibility = ConnectionIrPortCompatibility(
            direction = EngineeringConnectivityPortDirection.BIDIRECTIONAL,
            signalKind = "control",
            role = "line",
        ),
        provenance = provenance,
    )

    private fun engineeringReference(id: String, provenance: SourceProvenance): EngineeringReference {
        return EngineeringReference(
            authoredPath = id.split(':').filter(String::isNotBlank),
            resolvedIdentity = StableSemanticIdentity(id),
            provenance = provenance,
        )
    }

    private fun connectionReference(id: String, provenance: SourceProvenance): ConnectionIrReference {
        return ConnectionIrReference(
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
