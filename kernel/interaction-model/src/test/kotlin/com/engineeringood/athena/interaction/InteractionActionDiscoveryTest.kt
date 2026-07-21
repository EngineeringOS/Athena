package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InteractionActionDiscoveryTest {
    @Test
    fun `action discovery creates semantic action intents from enabled capabilities`() {
        val key = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
            subjectKind = InteractionSubjectKind.COMPONENT,
            sourceContextId = "file:///workspace/main.athena",
        )
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = "file:///workspace/main.athena",
                subjects = listOf(
                    InteractionRegistrySubjectFact(
                        canonicalSubjectId = key.canonicalSubjectId,
                        subjectKind = key.subjectKind,
                        capabilities = listOf(
                            SemanticCapability("reveal-source", InteractionActionFamily.REVEAL, enabled = true),
                            SemanticCapability("select-graph", InteractionActionFamily.SELECT, enabled = true),
                        ),
                    ),
                ),
            ),
        )

        val result = InteractionActionDiscovery.discover(
            registry = registry,
            subjectKey = key,
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
        )

        assertEquals(emptyList(), result.diagnostics)
        assertEquals(listOf(InteractionActionFamily.REVEAL, InteractionActionFamily.SELECT), result.actions.map { it.actionFamily })
        assertTrue(result.actions.all { action -> action.subject == key })
    }

    @Test
    fun `action discovery returns diagnostics for disabled capabilities and missing subjects`() {
        val key = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("port:PLC1.power"),
            subjectKind = InteractionSubjectKind.PORT,
            sourceContextId = "file:///workspace/main.athena",
        )
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = "file:///workspace/main.athena",
                subjects = listOf(
                    InteractionRegistrySubjectFact(
                        canonicalSubjectId = key.canonicalSubjectId,
                        subjectKind = key.subjectKind,
                        capabilities = listOf(
                            SemanticCapability("mutate-relationship", InteractionActionFamily.MUTATE, enabled = false),
                        ),
                    ),
                ),
            ),
        )

        val disabled = InteractionActionDiscovery.discover(
            registry = registry,
            subjectKey = key,
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
        )
        val missing = InteractionActionDiscovery.discover(
            registry = registry,
            subjectKey = key.copy(canonicalSubjectId = StableSemanticIdentity("port:Missing.power")),
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
        )

        assertEquals(emptyList(), disabled.actions)
        assertEquals(InteractionDiagnosticCode.ACTION_UNSUPPORTED, disabled.diagnostics.single().code)
        assertEquals(InteractionDiagnosticCode.SUBJECT_UNRESOLVED, missing.diagnostics.single().code)
    }

    @Test
    fun `action discovery exposes semantic entity creation without screen coordinate authority`() {
        val key = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("system:FactoryLine"),
            subjectKind = InteractionSubjectKind.WORKSPACE,
            sourceContextId = "file:///workspace/main.athena",
        )
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = "file:///workspace/main.athena",
                sourceRevision = "rev-entity-action",
                subjects = listOf(
                    InteractionRegistrySubjectFact(
                        canonicalSubjectId = key.canonicalSubjectId,
                        subjectKind = key.subjectKind,
                        capabilities = listOf(
                            SemanticCapability(
                                capabilityId = "create-semantic-entity",
                                actionFamily = InteractionActionFamily.MUTATE,
                                enabled = true,
                                parameters = mapOf(
                                    "entityKind" to "component",
                                    "parentIdentity" to "system:FactoryLine",
                                    "componentConceptId" to "electrical.motor.ac",
                                ),
                            ),
                        ),
                    ),
                ),
                occurrences = listOf(
                    InteractionRegistryOccurrenceFact(
                        canonicalSubjectId = key.canonicalSubjectId,
                        subjectKind = key.subjectKind,
                        projectionViewId = "schematic",
                        adapterMetadata = mapOf(
                            "screenX" to "640",
                            "screenY" to "360",
                        ),
                    ),
                ),
            ),
        )

        val result = InteractionActionDiscovery.discover(
            registry = registry,
            subjectKey = key,
            requestedBy = InteractionProvenance(
                originSurface = InteractionOriginSurface.GRAPH,
                reason = "insert semantic entity",
            ),
        )
        val action = result.actions.single()
        val proof = InteractionEnvelope(
            requestId = "proof:entity-creation-action-discovery",
            activeSourceUri = "file:///workspace/main.athena",
            activeSourceRevision = "rev-entity-action",
            payloadKind = InteractionPayloadKind.PROOF,
            payload = mapOf(
                "proofKind" to "entity-creation-action-discovery",
                "actionIntentId" to action.actionIntentId,
                "parentIdentity" to action.parameters.getValue("parentIdentity"),
                "componentConceptId" to action.parameters.getValue("componentConceptId"),
            ),
            adapterMetadata = registry.requireSubject(key).adapterMetadata,
        )

        assertEquals(emptyList(), result.diagnostics)
        assertEquals(InteractionActionFamily.MUTATE, action.actionFamily)
        assertEquals("system:FactoryLine", action.parameters["parentIdentity"])
        assertEquals("component", action.parameters["entityKind"])
        assertEquals("electrical.motor.ac", action.parameters["componentConceptId"])
        assertFalse(action.parameters.containsKey("screenX"))
        assertEquals("entity-creation-action-discovery", proof.payload["proofKind"])
        assertEquals("640", proof.adapterMetadata["screenX"])
    }
}
