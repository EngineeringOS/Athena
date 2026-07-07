package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.ir.EngineeringComponent
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
import kotlin.test.assertTrue

class EngineeringIrValidatorTest {
    private val validator = EngineeringIrValidator()

    @Test
    fun `marks valid m0 ir as semantically valid and downstream-continuable`() {
        val result = validator.validate(validDocument())

        assertTrue(result.isSemanticallyValid)
        assertEquals(emptyList(), result.diagnostics)
        assertEquals(SemanticContinuationDecision.CONTINUE, result.continuationDecision)
    }

    @Test
    fun `emits deterministic duplicate and ambiguity diagnostics`() {
        val document = duplicateAndAmbiguousDocument()

        val first = validator.validate(document)
        val second = validator.validate(document)

        assertEquals(first, second)
        assertEquals(
            listOf(
                DiagnosticExpectation(
                    ruleId = "uniqueness.component.duplicate-authored-key",
                    category = SemanticDiagnosticCategory.UNIQUENESS,
                    subjectIdentity = "component:PLC1",
                    provenance = duplicateSource(2, 3, 4, 4),
                ),
                DiagnosticExpectation(
                    ruleId = "uniqueness.component.duplicate-authored-key",
                    category = SemanticDiagnosticCategory.UNIQUENESS,
                    subjectIdentity = "component:PLC1#2",
                    provenance = duplicateSource(6, 3, 8, 4),
                ),
                DiagnosticExpectation(
                    ruleId = "reference.port-owner.ambiguous",
                    category = SemanticDiagnosticCategory.REFERENCE,
                    subjectIdentity = "port:PLC1.out",
                    provenance = duplicateSource(10, 8, 10, 16),
                ),
                DiagnosticExpectation(
                    ruleId = "reference.port-owner.ambiguous",
                    category = SemanticDiagnosticCategory.REFERENCE,
                    subjectIdentity = "port:PLC1.out#2",
                    provenance = duplicateSource(15, 8, 15, 16),
                ),
                DiagnosticExpectation(
                    ruleId = "uniqueness.port.duplicate-authored-key",
                    category = SemanticDiagnosticCategory.UNIQUENESS,
                    subjectIdentity = "port:PLC1.out",
                    provenance = duplicateSource(10, 3, 13, 4),
                ),
                DiagnosticExpectation(
                    ruleId = "uniqueness.port.duplicate-authored-key",
                    category = SemanticDiagnosticCategory.UNIQUENESS,
                    subjectIdentity = "port:PLC1.out#2",
                    provenance = duplicateSource(15, 3, 18, 4),
                ),
                DiagnosticExpectation(
                    ruleId = "reference.connection-endpoint.ambiguous",
                    category = SemanticDiagnosticCategory.REFERENCE,
                    subjectIdentity = "connection:PLC1.out->PLC1.out",
                    provenance = duplicateSource(20, 11, 20, 19),
                ),
                DiagnosticExpectation(
                    ruleId = "reference.connection-endpoint.ambiguous",
                    category = SemanticDiagnosticCategory.REFERENCE,
                    subjectIdentity = "connection:PLC1.out->PLC1.out",
                    provenance = duplicateSource(20, 23, 20, 31),
                ),
                DiagnosticExpectation(
                    ruleId = "reference.connection-endpoint.ambiguous",
                    category = SemanticDiagnosticCategory.REFERENCE,
                    subjectIdentity = "connection:PLC1.out->PLC1.out#2",
                    provenance = duplicateSource(21, 11, 21, 19),
                ),
                DiagnosticExpectation(
                    ruleId = "reference.connection-endpoint.ambiguous",
                    category = SemanticDiagnosticCategory.REFERENCE,
                    subjectIdentity = "connection:PLC1.out->PLC1.out#2",
                    provenance = duplicateSource(21, 23, 21, 31),
                ),
                DiagnosticExpectation(
                    ruleId = "uniqueness.connection.duplicate-authored-key",
                    category = SemanticDiagnosticCategory.UNIQUENESS,
                    subjectIdentity = "connection:PLC1.out->PLC1.out",
                    provenance = duplicateSource(20, 3, 20, 31),
                ),
                DiagnosticExpectation(
                    ruleId = "uniqueness.connection.duplicate-authored-key",
                    category = SemanticDiagnosticCategory.UNIQUENESS,
                    subjectIdentity = "connection:PLC1.out->PLC1.out#2",
                    provenance = duplicateSource(21, 3, 21, 31),
                ),
            ),
            first.diagnostics.map { diagnostic ->
                DiagnosticExpectation(
                    ruleId = diagnostic.ruleId.value,
                    category = diagnostic.category,
                    subjectIdentity = diagnostic.subjectIdentity?.value,
                    provenance = diagnostic.provenance,
                )
            },
        )
        assertEquals(SemanticContinuationDecision.STOP_DOWNSTREAM, first.continuationDecision)
        assertTrue(!first.isSemanticallyValid)
    }

    @Test
    fun `leaves property and connection semantics for plugin-owned validation`() {
        val result = validator.validate(propertyAndCompatibilityDocument())

        assertEquals(emptyList(), result.diagnostics)
        assertEquals(SemanticContinuationDecision.CONTINUE, result.continuationDecision)
    }

    @Test
    fun `does not classify domain-owned symbolic properties without an active domain plugin`() {
        val result = validator.validate(textValuedPropertyDocument())

        assertEquals(emptyList(), result.diagnostics)
        assertEquals(SemanticContinuationDecision.CONTINUE, result.continuationDecision)
    }

    private fun validDocument(): EngineeringDocument {
        return EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity("system:DemoCabinet"),
                name = "DemoCabinet",
                provenance = validSource(1, 1, 22, 2),
            ),
            components = listOf(
                component(
                    id = "component:PLC1",
                    name = "PLC1",
                    type = "PLC",
                    provenance = validSource(2, 3, 5, 4),
                ),
                component(
                    id = "component:M1",
                    name = "M1",
                    type = "Motor",
                    provenance = validSource(7, 3, 9, 4),
                ),
                component(
                    id = "component:M2",
                    name = "M2",
                    type = "Motor",
                    provenance = validSource(11, 3, 13, 4),
                ),
            ),
            ports = listOf(
                port(
                    id = "port:PLC1.out",
                    ownerPath = listOf("PLC1"),
                    ownerIdentity = "component:PLC1",
                    name = "out",
                    direction = "out",
                    signal = "Digital",
                    provenance = validSource(15, 3, 18, 4),
                    ownerProvenance = validSource(15, 8, 15, 16),
                ),
                port(
                    id = "port:M1.in",
                    ownerPath = listOf("M1"),
                    ownerIdentity = "component:M1",
                    name = "in",
                    direction = "in",
                    signal = "Digital",
                    provenance = validSource(20, 3, 23, 4),
                    ownerProvenance = validSource(20, 8, 20, 13),
                ),
            ),
            connections = listOf(
                connection(
                    id = "connection:PLC1.out->M1.in",
                    fromPath = listOf("PLC1", "out"),
                    fromIdentity = "port:PLC1.out",
                    toPath = listOf("M1", "in"),
                    toIdentity = "port:M1.in",
                    provenance = validSource(25, 3, 25, 28),
                    fromProvenance = validSource(25, 11, 25, 19),
                    toProvenance = validSource(25, 23, 25, 28),
                ),
            ),
        )
    }

    private fun duplicateAndAmbiguousDocument(): EngineeringDocument {
        return EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity("system:DuplicateIdentity"),
                name = "DuplicateIdentity",
                provenance = duplicateSource(1, 1, 21, 2),
            ),
            components = listOf(
                component(
                    id = "component:PLC1",
                    name = "PLC1",
                    type = "PLC",
                    provenance = duplicateSource(2, 3, 4, 4),
                ),
                component(
                    id = "component:PLC1#2",
                    name = "PLC1",
                    type = "PLC",
                    provenance = duplicateSource(6, 3, 8, 4),
                ),
            ),
            ports = listOf(
                port(
                    id = "port:PLC1.out",
                    ownerPath = listOf("PLC1"),
                    ownerIdentity = null,
                    name = "out",
                    direction = "out",
                    signal = "Digital",
                    provenance = duplicateSource(10, 3, 13, 4),
                    ownerProvenance = duplicateSource(10, 8, 10, 16),
                ),
                port(
                    id = "port:PLC1.out#2",
                    ownerPath = listOf("PLC1"),
                    ownerIdentity = null,
                    name = "out",
                    direction = "out",
                    signal = "Digital",
                    provenance = duplicateSource(15, 3, 18, 4),
                    ownerProvenance = duplicateSource(15, 8, 15, 16),
                ),
            ),
            connections = listOf(
                connection(
                    id = "connection:PLC1.out->PLC1.out",
                    fromPath = listOf("PLC1", "out"),
                    fromIdentity = null,
                    toPath = listOf("PLC1", "out"),
                    toIdentity = null,
                    provenance = duplicateSource(20, 3, 20, 31),
                    fromProvenance = duplicateSource(20, 11, 20, 19),
                    toProvenance = duplicateSource(20, 23, 20, 31),
                ),
                connection(
                    id = "connection:PLC1.out->PLC1.out#2",
                    fromPath = listOf("PLC1", "out"),
                    fromIdentity = null,
                    toPath = listOf("PLC1", "out"),
                    toIdentity = null,
                    provenance = duplicateSource(21, 3, 21, 31),
                    fromProvenance = duplicateSource(21, 11, 21, 19),
                    toProvenance = duplicateSource(21, 23, 21, 31),
                ),
            ),
        )
    }

    private fun propertyAndCompatibilityDocument(): EngineeringDocument {
        return EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity("system:PropertyChecks"),
                name = "PropertyChecks",
                provenance = propertySource(1, 1, 28, 2),
            ),
            components = listOf(
                component(
                    id = "component:Untyped",
                    name = "Untyped",
                    type = null,
                    provenance = propertySource(2, 3, 3, 4),
                ),
                component(
                    id = "component:PLC1",
                    name = "PLC1",
                    type = "PLC",
                    provenance = propertySource(5, 3, 7, 4),
                ),
                component(
                    id = "component:M1",
                    name = "M1",
                    type = "Motor",
                    provenance = propertySource(9, 3, 11, 4),
                ),
                component(
                    id = "component:M2",
                    name = "M2",
                    type = "Motor",
                    provenance = propertySource(13, 3, 15, 4),
                ),
                component(
                    id = "component:Bad",
                    name = "Bad",
                    type = "Motor",
                    provenance = propertySource(17, 3, 19, 4),
                ),
            ),
            ports = listOf(
                port(
                    id = "port:PLC1.out",
                    ownerPath = listOf("PLC1"),
                    ownerIdentity = "component:PLC1",
                    name = "out",
                    direction = "out",
                    signal = "Digital",
                    provenance = propertySource(14, 3, 17, 4),
                    ownerProvenance = propertySource(14, 8, 14, 16),
                ),
                port(
                    id = "port:M1.out",
                    ownerPath = listOf("M1"),
                    ownerIdentity = "component:M1",
                    name = "out",
                    direction = "out",
                    signal = "Digital",
                    provenance = propertySource(18, 3, 21, 4),
                    ownerProvenance = propertySource(18, 8, 18, 14),
                ),
                port(
                    id = "port:M2.in",
                    ownerPath = listOf("M2"),
                    ownerIdentity = "component:M2",
                    name = "in",
                    direction = "in",
                    signal = "Analog",
                    provenance = propertySource(22, 3, 25, 4),
                    ownerProvenance = propertySource(22, 8, 22, 13),
                ),
                port(
                    id = "port:Bad.invalid",
                    ownerPath = listOf("Bad"),
                    ownerIdentity = "component:Bad",
                    name = "invalid",
                    direction = "sideways",
                    signal = "Digital",
                    provenance = propertySource(14, 3, 17, 4),
                    ownerProvenance = propertySource(14, 8, 14, 19),
                ),
            ),
            connections = listOf(
                connection(
                    id = "connection:PLC1.out->M1.out",
                    fromPath = listOf("PLC1", "out"),
                    fromIdentity = "port:PLC1.out",
                    toPath = listOf("M1", "out"),
                    toIdentity = "port:M1.out",
                    provenance = propertySource(26, 3, 26, 27),
                    fromProvenance = propertySource(26, 11, 26, 19),
                    toProvenance = propertySource(26, 23, 26, 29),
                ),
                connection(
                    id = "connection:PLC1.out->M2.in",
                    fromPath = listOf("PLC1", "out"),
                    fromIdentity = "port:PLC1.out",
                    toPath = listOf("M2", "in"),
                    toIdentity = "port:M2.in",
                    provenance = propertySource(27, 3, 27, 26),
                    fromProvenance = propertySource(27, 11, 27, 19),
                    toProvenance = propertySource(27, 23, 27, 28),
                ),
            ),
        )
    }

    private fun textValuedPropertyDocument(): EngineeringDocument {
        return EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity("system:TextProperties"),
                name = "TextProperties",
                provenance = textPropertySource(1, 1, 10, 2),
            ),
            components = listOf(
                EngineeringComponent(
                    id = StableSemanticIdentity("component:QuotedDevice"),
                    name = "QuotedDevice",
                    kind = "device",
                    properties = listOf(
                        EngineeringProperty("type", EngineeringPropertyValue.Text("PLC")),
                    ),
                    provenance = textPropertySource(2, 3, 4, 4),
                ),
            ),
            ports = listOf(
                EngineeringPort(
                    id = StableSemanticIdentity("port:QuotedDevice.out"),
                    ownerReference = EngineeringReference(
                        authoredPath = listOf("QuotedDevice"),
                        resolvedIdentity = StableSemanticIdentity("component:QuotedDevice"),
                        provenance = textPropertySource(6, 8, 6, 24),
                    ),
                    name = "out",
                    properties = listOf(
                        EngineeringProperty("direction", EngineeringPropertyValue.Text("out")),
                        EngineeringProperty("signal", EngineeringPropertyValue.Symbol("Digital")),
                    ),
                    provenance = textPropertySource(6, 3, 9, 4),
                ),
            ),
            connections = emptyList(),
        )
    }

    private fun component(
        id: String,
        name: String,
        type: String?,
        provenance: SourceProvenance,
    ): EngineeringComponent {
        val properties = buildList {
            if (type != null) {
                add(EngineeringProperty("type", EngineeringPropertyValue.Symbol(type)))
            }
        }

        return EngineeringComponent(
            id = StableSemanticIdentity(id),
            name = name,
            kind = "device",
            properties = properties,
            provenance = provenance,
        )
    }

    private fun port(
        id: String,
        ownerPath: List<String>,
        ownerIdentity: String?,
        name: String,
        direction: String,
        signal: String,
        provenance: SourceProvenance,
        ownerProvenance: SourceProvenance,
    ): EngineeringPort {
        return EngineeringPort(
            id = StableSemanticIdentity(id),
            ownerReference = EngineeringReference(
                authoredPath = ownerPath,
                resolvedIdentity = ownerIdentity?.let(::StableSemanticIdentity),
                provenance = ownerProvenance,
            ),
            name = name,
            properties = listOf(
                EngineeringProperty("direction", EngineeringPropertyValue.Symbol(direction)),
                EngineeringProperty("signal", EngineeringPropertyValue.Symbol(signal)),
            ),
            provenance = provenance,
        )
    }

    private fun connection(
        id: String,
        fromPath: List<String>,
        fromIdentity: String?,
        toPath: List<String>,
        toIdentity: String?,
        provenance: SourceProvenance,
        fromProvenance: SourceProvenance,
        toProvenance: SourceProvenance,
    ): EngineeringConnection {
        return EngineeringConnection(
            id = StableSemanticIdentity(id),
            from = EngineeringReference(
                authoredPath = fromPath,
                resolvedIdentity = fromIdentity?.let(::StableSemanticIdentity),
                provenance = fromProvenance,
            ),
            to = EngineeringReference(
                authoredPath = toPath,
                resolvedIdentity = toIdentity?.let(::StableSemanticIdentity),
                provenance = toProvenance,
            ),
            provenance = provenance,
        )
    }

    private fun validSource(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int): SourceProvenance {
        return SourceProvenance("examples/m0/demo-cabinet.athena", startLine, startColumn, endLine, endColumn)
    }

    private fun duplicateSource(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int): SourceProvenance {
        return SourceProvenance("examples/m0/duplicate-identity.athena", startLine, startColumn, endLine, endColumn)
    }

    private fun propertySource(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int): SourceProvenance {
        return SourceProvenance("examples/m0/property-checks.athena", startLine, startColumn, endLine, endColumn)
    }

    private fun textPropertySource(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int): SourceProvenance {
        return SourceProvenance("examples/m0/text-property-checks.athena", startLine, startColumn, endLine, endColumn)
    }

    private data class DiagnosticExpectation(
        val ruleId: String,
        val category: SemanticDiagnosticCategory,
        val subjectIdentity: String?,
        val provenance: SourceProvenance,
    )
}
