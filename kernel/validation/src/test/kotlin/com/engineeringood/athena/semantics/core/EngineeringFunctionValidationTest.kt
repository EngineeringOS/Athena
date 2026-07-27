package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringFunction
import com.engineeringood.athena.ir.EngineeringFunctionRole
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

class EngineeringFunctionValidationTest {
    private val validator = EngineeringIrValidator()

    @Test
    fun `valid function references existing owner ports without copying terminal truth`() {
        val document = document(
            functions = listOf(function("function:KM1.coil", "KM1", "coil", "coil", "A1", "A2")),
        )

        val result = validator.validate(document)

        assertTrue(result.isSemanticallyValid, result.diagnostics.toString())
        assertEquals(
            listOf("A1", "A2"),
            document.functions.single().portReferences.map { reference ->
                document.ports.single { port -> port.id == reference.resolvedIdentity }
                    .properties.single { property -> property.name == "terminal" }
                    .value.let { value -> (value as EngineeringPropertyValue.Text).text }
            },
        )
        assertEquals(StableSemanticIdentity("component:KM1"), document.functions.single().ownerReference.resolvedIdentity)
    }

    @Test
    fun `rejects duplicate function names`() {
        val result = validator.validate(
            document(
                functions = listOf(
                    function("function:KM1.coil", "KM1", "coil", "coil", "A1"),
                    function("function:KM1.coil#2", "KM1", "coil", "coil", "A2"),
                ),
            ),
        )

        assertEquals(2, result.diagnostics.count { it.ruleId.value == "uniqueness.function.duplicate-authored-key" })
    }

    @Test
    fun `rejects unresolved and ambiguous function owners`() {
        val unresolved = function("function:Missing.coil", "Missing", "coil", "coil", "A1").copy(
            ownerReference = reference(listOf("Missing"), null),
        )
        val ambiguous = function("function:KM1.coil", "KM1", "coil", "coil", "A1").copy(
            ownerReference = reference(listOf("KM1"), null),
        )
        val duplicateOwner = EngineeringComponent(
            StableSemanticIdentity("component:KM1#duplicate"),
            "KM1",
            "device",
            emptyList(),
            provenance(3),
        )
        val base = document(functions = listOf(unresolved, ambiguous))

        val result = validator.validate(base.copy(components = base.components + duplicateOwner))

        assertEquals(
            listOf("reference.function-owner.ambiguous", "reference.function-owner.unresolved"),
            result.diagnostics.map { it.ruleId.value }.filter { it.startsWith("reference.function-owner") }.sorted(),
        )
    }

    @Test
    fun `rejects missing and cross-device function port references`() {
        val missing = function("function:KM1.missing", "KM1", "missing", "coil", "missing")
        val crossDevice = function("function:KM1.cross", "KM1", "cross", "coil", "KM2.A1")

        val result = validator.validate(document(functions = listOf(missing, crossDevice)))

        assertEquals(
            listOf("reference.function-port.cross-owner", "reference.function-port.unresolved"),
            result.diagnostics.map { it.ruleId.value }.filter { it.startsWith("reference.function-port") }.sorted(),
        )
    }

    @Test
    fun `rejects duplicate references inside one function`() {
        val result = validator.validate(
            document(functions = listOf(function("function:KM1.coil", "KM1", "coil", "coil", "A1", "A1"))),
        )

        assertEquals(
            listOf("uniqueness.function-port.duplicate-reference"),
            result.diagnostics.map { it.ruleId.value },
        )
    }

    @Test
    fun `rejects one port assigned to multiple functions`() {
        val result = validator.validate(
            document(
                functions = listOf(
                    function("function:KM1.coil", "KM1", "coil", "coil", "A1"),
                    function("function:KM1.aux", "KM1", "aux", "normally-open-contact", "A1"),
                ),
            ),
        )

        assertEquals(2, result.diagnostics.count { it.ruleId.value == "ownership.function-port.multiple" })
    }

    private fun document(functions: List<EngineeringFunction>): EngineeringDocument = EngineeringDocument(
        system = EngineeringSystem(StableSemanticIdentity("system:Demo"), "Demo", provenance(1)),
        components = listOf(
            EngineeringComponent(StableSemanticIdentity("component:KM1"), "KM1", "device", emptyList(), provenance(2)),
            EngineeringComponent(StableSemanticIdentity("component:KM2"), "KM2", "device", emptyList(), provenance(3)),
        ),
        ports = listOf(port("KM1", "A1"), port("KM1", "A2"), port("KM2", "A1")),
        connections = emptyList(),
        functions = functions,
    )

    private fun port(owner: String, name: String): EngineeringPort = EngineeringPort(
        id = StableSemanticIdentity("port:$owner.$name"),
        ownerReference = reference(listOf(owner), "component:$owner"),
        name = name,
        properties = listOf(EngineeringProperty("terminal", EngineeringPropertyValue.Text(name))),
        provenance = provenance(4),
    )

    private fun function(
        id: String,
        owner: String,
        name: String,
        role: String,
        vararg ports: String,
    ): EngineeringFunction = EngineeringFunction(
        id = StableSemanticIdentity(id),
        ownerReference = reference(listOf(owner), "component:$owner"),
        name = name,
        role = EngineeringFunctionRole(role),
        portReferences = ports.map { authored ->
            val path = if ('.' in authored) authored.split('.') else listOf(owner, authored)
            val resolved = if (path in listOf(listOf("KM1", "A1"), listOf("KM1", "A2"), listOf("KM2", "A1"))) {
                "port:${path.joinToString(".")}"
            } else {
                null
            }
            reference(path, resolved)
        },
        provenance = provenance(5),
    )

    private fun reference(path: List<String>, resolved: String?): EngineeringReference = EngineeringReference(
        authoredPath = path,
        resolvedIdentity = resolved?.let(::StableSemanticIdentity),
        provenance = provenance(6),
    )

    private fun provenance(line: Int): SourceProvenance = SourceProvenance("function.athena", line, 1, line, 20)
}
