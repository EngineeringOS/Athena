package com.engineeringood.athena.ir

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class EngineeringValueModelsTest {
    private val source = SourceProvenance("src/system.athena", 4, 3, 4, 19)
    private val packageName = EngineeringPackageName("com.example.core")
    private val lengthDimension = EngineeringDefinitionId(packageName, "dimension.length")
    private val timeDimension = EngineeringDefinitionId(packageName, "dimension.time")
    private val metre = EngineeringDefinitionId(packageName, "unit.metre")
    private val millimetre = EngineeringDefinitionId(packageName, "unit.millimetre")
    private val second = EngineeringDefinitionId(packageName, "unit.second")

    @Test
    fun `exact numbers normalize signs reduce fractions and preserve exact arithmetic`() {
        assertEquals(ExactNumber.of(1, 2), ExactNumber.of(2, 4))
        assertEquals(ExactNumber.of(-1, 2), ExactNumber.of(1, -2))
        assertEquals(ExactNumber.ZERO, ExactNumber.of(0, -9))
        assertEquals(ExactNumber.of(1, 2), ExactNumber.of(1, 3) + ExactNumber.of(1, 6))
        assertEquals(ExactNumber.of(1, 6), ExactNumber.of(1, 2) - ExactNumber.of(1, 3))
        assertEquals(ExactNumber.of(2, 3), ExactNumber.of(4, 9) * ExactNumber.of(3, 2))
        assertEquals(ExactNumber.of(8, 15), ExactNumber.of(4, 5) / ExactNumber.of(3, 2))
        assertEquals(0, ExactNumber.of(BigInteger("9007199254740993"), BigInteger.ONE).compareTo(
            ExactNumber.of(BigInteger("18014398509481986"), BigInteger.TWO),
        ))
        assertFailsWith<IllegalArgumentException> { ExactNumber.of(1, 0) }
        assertFailsWith<ArithmeticException> { ExactNumber.ONE / ExactNumber.ZERO }
    }

    @Test
    fun `compatible quantity conversion and comparison lose no precision`() {
        val dimension = EngineeringDimensionSignature.of(mapOf(lengthDimension to 1))
        val metreSemantics = ResolvedEngineeringUnit(
            id = metre,
            scale = ExactNumber.ONE,
            dimension = dimension,
        )
        val millimetreSemantics = ResolvedEngineeringUnit(
            id = millimetre,
            scale = ExactNumber.of(1, 1_000),
            dimension = dimension,
        )
        val quantity = EngineeringValue.Quantity(ExactNumber.of(1_500), resolvedReference(millimetre), source)

        val converted = assertIs<EngineeringQuantityConversion.Success>(
            EngineeringQuantityConversion.convert(quantity, millimetreSemantics, metreSemantics),
        )
        assertEquals(ExactNumber.of(3, 2), converted.quantity.value)
        assertEquals(metre, converted.quantity.unit.resolvedId)

        val comparison = assertIs<EngineeringQuantityComparison.Comparable>(
            EngineeringQuantityConversion.compare(
                left = quantity,
                leftUnit = millimetreSemantics,
                right = EngineeringValue.Quantity(ExactNumber.of(3, 2), resolvedReference(metre), source),
                rightUnit = metreSemantics,
            ),
        )
        assertEquals(0, comparison.order)
    }

    @Test
    fun `incompatible dimensions fail with exact expected actual and source provenance`() {
        val length = ResolvedEngineeringUnit(
            id = metre,
            scale = ExactNumber.ONE,
            dimension = EngineeringDimensionSignature.of(mapOf(lengthDimension to 1)),
        )
        val duration = ResolvedEngineeringUnit(
            id = second,
            scale = ExactNumber.ONE,
            dimension = EngineeringDimensionSignature.of(mapOf(timeDimension to 1)),
        )
        val quantity = EngineeringValue.Quantity(ExactNumber.of(4), resolvedReference(metre), source)

        val failure = assertIs<EngineeringQuantityConversion.IncompatibleDimensions>(
            EngineeringQuantityConversion.convert(quantity, length, duration),
        )
        assertEquals(duration.dimension, failure.expected)
        assertEquals(length.dimension, failure.actual)
        assertEquals(source, failure.provenance)
        assertEquals(
            "Quantity at src/system.athena:4:3 has dimension com.example.core:dimension.length^1; " +
                "expected com.example.core:dimension.time^1. Use a unit with the expected dimension.",
            failure.message,
        )
    }

    @Test
    fun `affine units convert exactly but reject multiplication and division`() {
        val dimension = EngineeringDimensionSignature.of(mapOf(lengthDimension to 1))
        val affine = ResolvedEngineeringUnit(
            id = metre,
            scale = ExactNumber.of(5, 9),
            offset = ExactNumber.of(32),
            dimension = dimension,
        )
        val ratio = ResolvedEngineeringUnit(
            id = millimetre,
            scale = ExactNumber.ONE,
            dimension = dimension,
        )

        assertIs<EngineeringQuantityOperation.AffineUnitNotAllowed>(
            EngineeringQuantityArithmetic.multiply(
                EngineeringValue.Quantity(ExactNumber.ONE, resolvedReference(metre), source),
                affine,
                EngineeringValue.Quantity(ExactNumber.ONE, resolvedReference(millimetre), source),
                ratio,
            ),
        )
        assertIs<EngineeringQuantityOperation.AffineUnitNotAllowed>(
            EngineeringQuantityArithmetic.divide(
                EngineeringValue.Quantity(ExactNumber.ONE, resolvedReference(millimetre), source),
                ratio,
                EngineeringValue.Quantity(ExactNumber.ONE, resolvedReference(metre), source),
                affine,
            ),
        )
    }

    @Test
    fun `engineering value is closed over six typed variants`() {
        val values: List<EngineeringValue> = listOf(
            EngineeringValue.Quantity(ExactNumber.of(7, 2), resolvedReference(metre), source),
            EngineeringValue.Integer(BigInteger("9007199254740993")),
            EngineeringValue.Boolean(true),
            EngineeringValue.Text("drive motor"),
            EngineeringValue.Symbol("main"),
            EngineeringValue.Reference(
                EngineeringReference(
                    authoredPath = listOf("M1", "main"),
                    resolvedIdentity = StableSemanticIdentity("function:M1.main"),
                    provenance = source,
                ),
            ),
        )

        assertEquals(
            listOf("Quantity", "Integer", "Boolean", "Text", "Symbol", "Reference"),
            values.map { value -> value::class.simpleName },
        )
    }

    private fun resolvedReference(id: EngineeringDefinitionId): EngineeringDefinitionReference =
        EngineeringDefinitionReference(
            authoredName = listOf(id.packageName.value, id.qualifiedName),
            resolvedId = id,
            provenance = source,
        )
}
