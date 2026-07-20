package com.engineeringood.athena.authoring

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SemanticRelationshipCompatibilityValidatorTest {
    @Test
    fun `compatible output to input electrical subjects may proceed to preview and persistence`() {
        val result = validator.validate(
            SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:M1.in"),
                document = document(
                    port("port:PLC1.out", owner = "component:PLC1", direction = "out", signal = "Digital"),
                    port("port:M1.in", owner = "component:M1", direction = "in", signal = "Digital"),
                ),
            ),
        )

        assertTrue(result.previewEligible)
        assertTrue(result.persistenceEligible)
        assertTrue(result.diagnostics.isEmpty())
    }

    @Test
    fun `rejects invalid electrical relationship candidates with governed diagnostics`() {
        val cases = listOf(
            "output-to-output" to SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:TERM1.out"),
                document = document(
                    port("port:PLC1.out", owner = "component:PLC1", direction = "out", signal = "Digital"),
                    port("port:TERM1.out", owner = "component:TERM1", direction = "out", signal = "Digital"),
                ),
            ) to "semantic.relationship.electrical.direction",
            "input-to-input" to SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:TERM1.in", "port:M1.in"),
                document = document(
                    port("port:TERM1.in", owner = "component:TERM1", direction = "in", signal = "Digital"),
                    port("port:M1.in", owner = "component:M1", direction = "in", signal = "Digital"),
                ),
            ) to "semantic.relationship.electrical.direction",
            "signal-mismatch" to SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:VFD1.analog"),
                document = document(
                    port("port:PLC1.out", owner = "component:PLC1", direction = "out", signal = "Digital"),
                    port("port:VFD1.analog", owner = "component:VFD1", direction = "in", signal = "Analog"),
                ),
            ) to "semantic.relationship.electrical.signal",
            "duplicate-connection" to SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:M1.in"),
                document = document(
                    port("port:PLC1.out", owner = "component:PLC1", direction = "out", signal = "Digital"),
                    port("port:M1.in", owner = "component:M1", direction = "in", signal = "Digital"),
                    connection("port:PLC1.out", "port:M1.in"),
                ),
            ) to "semantic.relationship.duplicate",
            "ambiguous-owner" to SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:M1.in"),
                document = document(
                    port("port:PLC1.out", owner = null, direction = "out", signal = "Digital"),
                    port("port:M1.in", owner = "component:M1", direction = "in", signal = "Digital"),
                ),
            ) to "semantic.relationship.owner.ambiguous",
            "dirty-source" to SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:M1.in"),
                document = document(
                    port("port:PLC1.out", owner = "component:PLC1", direction = "out", signal = "Digital"),
                    port("port:M1.in", owner = "component:M1", direction = "in", signal = "Digital"),
                ),
                sourceState = SemanticRelationshipSourceState.DIRTY,
            ) to "semantic.relationship.source.dirty",
            "invalid-source" to SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:M1.in"),
                document = document(
                    port("port:PLC1.out", owner = "component:PLC1", direction = "out", signal = "Digital"),
                    port("port:M1.in", owner = "component:M1", direction = "in", signal = "Digital"),
                ),
                sourceState = SemanticRelationshipSourceState.INVALID,
            ) to "semantic.relationship.source.invalid",
        )

        cases.forEach { (namedRequest, expectedCode) ->
            val (caseName, request) = namedRequest
            val result = validator.validate(request)

            assertFalse(result.previewEligible, caseName)
            assertFalse(result.persistenceEligible, caseName)
            assertEquals(expectedCode, result.diagnostics.firstOrNull()?.code, caseName)
        }
    }

    @Test
    fun `rejected relationship validation leaves source text unchanged`() {
        val sourceBefore = """
            system Authoring {
              device PLC1 {
                type PLC
                port out {
                  direction out
                  signal Digital
                }
              }
            }
        """.trimIndent()

        val result = validator.validate(
            SemanticRelationshipValidationRequest(
                intent = electricalIntent("port:PLC1.out", "port:PLC1.missing"),
                document = document(
                    port("port:PLC1.out", owner = "component:PLC1", direction = "out", signal = "Digital"),
                ),
                sourceText = sourceBefore,
            ),
        )

        assertFalse(result.previewEligible)
        assertEquals("semantic.relationship.subject.missing", result.diagnostics.single().code)
        assertEquals(sourceBefore, result.sourceTextAfterValidation)
    }

    private val validator = ElectricalSemanticRelationshipCompatibilityValidator()

    private fun electricalIntent(
        source: String,
        target: String,
    ): SemanticRelationshipIntent {
        return SemanticRelationshipIntent(
            intentId = AuthoringIntentId("intent:test"),
            origin = AuthoringOrigin(AuthoringSurface.GRAPH),
            relationshipType = ElectricalConnectionRelationship,
            sourceSubjectId = StableSemanticIdentity(source),
            targetSubjectId = StableSemanticIdentity(target),
            persistenceTarget = SemanticRelationshipPersistenceTarget(sourceUri = "main.athena"),
        )
    }

    private fun document(
        vararg items: Any,
    ): EngineeringDocument {
        return EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity("system:Authoring"),
                name = "Authoring",
                provenance = provenance(),
            ),
            components = emptyList(),
            ports = items.filterIsInstance<EngineeringPort>(),
            connections = items.filterIsInstance<EngineeringConnection>(),
        )
    }

    private fun connection(source: String, target: String): EngineeringConnection {
        return EngineeringConnection(
            id = StableSemanticIdentity("connection:$source->$target"),
            from = EngineeringReference(listOf("source"), StableSemanticIdentity(source), provenance()),
            to = EngineeringReference(listOf("target"), StableSemanticIdentity(target), provenance()),
            provenance = provenance(),
        )
    }

    private fun port(
        id: String,
        owner: String?,
        direction: String,
        signal: String,
    ): EngineeringPort {
        return EngineeringPort(
            id = StableSemanticIdentity(id),
            ownerReference = EngineeringReference(
                authoredPath = listOf(id.substringAfter("port:").substringBefore(".")),
                resolvedIdentity = owner?.let(::StableSemanticIdentity),
                provenance = provenance(),
            ),
            name = id.substringAfterLast("."),
            properties = listOf(
                EngineeringProperty("direction", EngineeringPropertyValue.Symbol(direction)),
                EngineeringProperty("signal", EngineeringPropertyValue.Symbol(signal)),
            ),
            provenance = provenance(),
        )
    }

    private fun provenance(): SourceProvenance = SourceProvenance(
        file = "main.athena",
        startLine = 1,
        startColumn = 1,
        endLine = 1,
        endColumn = 1,
    )
}
