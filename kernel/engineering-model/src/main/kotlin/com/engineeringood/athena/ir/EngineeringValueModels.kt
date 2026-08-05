package com.engineeringood.athena.ir

import java.math.BigInteger
import java.util.Collections

/** Exact normalized rational used at every engineering numeric boundary. */
class ExactNumber private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger,
) : Comparable<ExactNumber> {
    operator fun plus(other: ExactNumber): ExactNumber = of(
        numerator * other.denominator + other.numerator * denominator,
        denominator * other.denominator,
    )

    operator fun minus(other: ExactNumber): ExactNumber = of(
        numerator * other.denominator - other.numerator * denominator,
        denominator * other.denominator,
    )

    operator fun times(other: ExactNumber): ExactNumber = of(
        numerator * other.numerator,
        denominator * other.denominator,
    )

    operator fun div(other: ExactNumber): ExactNumber {
        if (other == ZERO) throw ArithmeticException("Cannot divide an exact number by zero")
        return of(numerator * other.denominator, denominator * other.numerator)
    }

    override fun compareTo(other: ExactNumber): Int =
        (numerator * other.denominator).compareTo(other.numerator * denominator)

    override fun equals(other: Any?): Boolean =
        this === other || (other is ExactNumber && numerator == other.numerator && denominator == other.denominator)

    override fun hashCode(): Int = 31 * numerator.hashCode() + denominator.hashCode()

    override fun toString(): String = if (denominator == BigInteger.ONE) {
        numerator.toString()
    } else {
        "$numerator/$denominator"
    }

    companion object {
        val ZERO: ExactNumber = ExactNumber(BigInteger.ZERO, BigInteger.ONE)
        val ONE: ExactNumber = ExactNumber(BigInteger.ONE, BigInteger.ONE)

        fun of(value: Int): ExactNumber = of(value.toLong())

        fun of(value: Long): ExactNumber = of(BigInteger.valueOf(value), BigInteger.ONE)

        fun of(numerator: Int, denominator: Int): ExactNumber = of(
            BigInteger.valueOf(numerator.toLong()),
            BigInteger.valueOf(denominator.toLong()),
        )

        fun of(numerator: Long, denominator: Long): ExactNumber = of(
            BigInteger.valueOf(numerator),
            BigInteger.valueOf(denominator),
        )

        fun of(numerator: BigInteger, denominator: BigInteger): ExactNumber {
            require(denominator != BigInteger.ZERO) { "Exact number denominator must not be zero" }
            if (numerator == BigInteger.ZERO) return ZERO

            val normalizedNumerator = if (denominator.signum() < 0) numerator.negate() else numerator
            val normalizedDenominator = denominator.abs()
            val divisor = normalizedNumerator.abs().gcd(normalizedDenominator)
            return ExactNumber(normalizedNumerator / divisor, normalizedDenominator / divisor)
        }
    }
}

/** Stable package identity used by package-qualified engineering definitions. */
@JvmInline
value class EngineeringPackageName(val value: String) {
    init {
        require(value.matches(Regex("[A-Za-z_][A-Za-z0-9_-]*(\\.[A-Za-z_][A-Za-z0-9_-]*)*"))) {
            "Engineering package name must be a non-blank dot-qualified name"
        }
    }

    override fun toString(): String = value
}

/** Package-qualified semantic definition identity. Package version is deliberately excluded. */
data class EngineeringDefinitionId(
    val packageName: EngineeringPackageName,
    val qualifiedName: String,
) : Comparable<EngineeringDefinitionId> {
    init {
        require(qualifiedName.matches(Regex("[A-Za-z_][A-Za-z0-9_-]*(\\.[A-Za-z_][A-Za-z0-9_-]*)*"))) {
            "Engineering definition name must be a non-blank dot-qualified name"
        }
    }

    override fun compareTo(other: EngineeringDefinitionId): Int =
        compareValuesBy(this, other, { it.packageName.value }, { it.qualifiedName })

    override fun toString(): String = "$packageName:$qualifiedName"
}

/** Authored definition reference before or after fail-closed package resolution. */
data class EngineeringDefinitionReference(
    val authoredName: List<String>,
    val resolvedId: EngineeringDefinitionId?,
    val provenance: SourceProvenance,
) {
    init {
        require(authoredName.isNotEmpty() && authoredName.all { segment -> segment.isNotBlank() }) {
            "Engineering definition reference requires a non-blank authored name"
        }
    }
}

/** Sparse, deterministic dimensional exponent map with no kernel-owned dimension names. */
class EngineeringDimensionSignature private constructor(
    val exponents: Map<EngineeringDefinitionId, Int>,
) {
    operator fun plus(other: EngineeringDimensionSignature): EngineeringDimensionSignature = of(
        (exponents.keys + other.exponents.keys).associateWith { dimension ->
            exponents.getOrDefault(dimension, 0) + other.exponents.getOrDefault(dimension, 0)
        },
    )

    operator fun minus(other: EngineeringDimensionSignature): EngineeringDimensionSignature = of(
        (exponents.keys + other.exponents.keys).associateWith { dimension ->
            exponents.getOrDefault(dimension, 0) - other.exponents.getOrDefault(dimension, 0)
        },
    )

    override fun equals(other: Any?): Boolean =
        this === other || (other is EngineeringDimensionSignature && exponents == other.exponents)

    override fun hashCode(): Int = exponents.hashCode()

    override fun toString(): String = if (exponents.isEmpty()) {
        "dimensionless"
    } else {
        exponents.entries.joinToString(separator = " * ") { (dimension, exponent) -> "$dimension^$exponent" }
    }

    companion object {
        val DIMENSIONLESS: EngineeringDimensionSignature = EngineeringDimensionSignature(emptyMap())

        fun of(exponents: Map<EngineeringDefinitionId, Int>): EngineeringDimensionSignature {
            val canonical = exponents
                .filterValues { exponent -> exponent != 0 }
                .toSortedMap()
            return if (canonical.isEmpty()) {
                DIMENSIONLESS
            } else {
                EngineeringDimensionSignature(Collections.unmodifiableMap(LinkedHashMap(canonical)))
            }
        }
    }
}

/** Resolved mathematical semantics for one package-defined unit. */
data class ResolvedEngineeringUnit(
    val id: EngineeringDefinitionId,
    val scale: ExactNumber,
    val offset: ExactNumber = ExactNumber.ZERO,
    val dimension: EngineeringDimensionSignature,
) {
    init {
        require(scale > ExactNumber.ZERO) { "Resolved engineering unit scale must be positive" }
    }

    val isAffine: Boolean
        get() = offset != ExactNumber.ZERO
}

/** Closed typed engineering value surface. */
sealed interface EngineeringValue {
    data class Quantity(
        val value: ExactNumber,
        val unit: EngineeringDefinitionReference,
        val provenance: SourceProvenance,
    ) : EngineeringValue

    data class Integer(val value: BigInteger) : EngineeringValue

    data class Boolean(val value: kotlin.Boolean) : EngineeringValue

    data class Text(val text: String) : EngineeringValue

    data class Symbol(val text: String) : EngineeringValue

    data class Reference(val reference: EngineeringReference) : EngineeringValue
}

/** Typed result of converting one exact Quantity to a target unit. */
sealed interface EngineeringQuantityConversion {
    data class Success(val quantity: EngineeringValue.Quantity) : EngineeringQuantityConversion

    data class IncompatibleDimensions(
        val expected: EngineeringDimensionSignature,
        val actual: EngineeringDimensionSignature,
        val provenance: SourceProvenance,
        val message: String,
    ) : EngineeringQuantityConversion

    data class UnitReferenceMismatch(
        val expected: EngineeringDefinitionId,
        val actual: EngineeringDefinitionId?,
        val provenance: SourceProvenance,
    ) : EngineeringQuantityConversion

    companion object {
        fun convert(
            quantity: EngineeringValue.Quantity,
            sourceUnit: ResolvedEngineeringUnit,
            targetUnit: ResolvedEngineeringUnit,
        ): EngineeringQuantityConversion {
            if (quantity.unit.resolvedId != sourceUnit.id) {
                return UnitReferenceMismatch(sourceUnit.id, quantity.unit.resolvedId, quantity.provenance)
            }
            if (sourceUnit.dimension != targetUnit.dimension) {
                return IncompatibleDimensions(
                    expected = targetUnit.dimension,
                    actual = sourceUnit.dimension,
                    provenance = quantity.provenance,
                    message = "Quantity at ${quantity.provenance.location()} has dimension ${sourceUnit.dimension}; " +
                        "expected ${targetUnit.dimension}. Use a unit with the expected dimension.",
                )
            }

            val canonicalValue = quantity.value * sourceUnit.scale + sourceUnit.offset
            val targetValue = (canonicalValue - targetUnit.offset) / targetUnit.scale
            return Success(
                EngineeringValue.Quantity(
                    value = targetValue,
                    unit = EngineeringDefinitionReference(
                        authoredName = listOf(targetUnit.id.packageName.value, targetUnit.id.qualifiedName),
                        resolvedId = targetUnit.id,
                        provenance = quantity.provenance,
                    ),
                    provenance = quantity.provenance,
                ),
            )
        }

        fun compare(
            left: EngineeringValue.Quantity,
            leftUnit: ResolvedEngineeringUnit,
            right: EngineeringValue.Quantity,
            rightUnit: ResolvedEngineeringUnit,
        ): EngineeringQuantityComparison {
            return when (val converted = convert(left, leftUnit, rightUnit)) {
                is Success -> EngineeringQuantityComparison.Comparable(converted.quantity.value.compareTo(right.value))
                is IncompatibleDimensions -> EngineeringQuantityComparison.IncompatibleDimensions(
                    expected = converted.expected,
                    actual = converted.actual,
                    provenance = converted.provenance,
                    message = converted.message,
                )
                is UnitReferenceMismatch -> EngineeringQuantityComparison.UnitReferenceMismatch(
                    expected = converted.expected,
                    actual = converted.actual,
                    provenance = converted.provenance,
                )
            }
        }
    }
}

sealed interface EngineeringQuantityComparison {
    data class Comparable(val order: Int) : EngineeringQuantityComparison

    data class IncompatibleDimensions(
        val expected: EngineeringDimensionSignature,
        val actual: EngineeringDimensionSignature,
        val provenance: SourceProvenance,
        val message: String,
    ) : EngineeringQuantityComparison

    data class UnitReferenceMismatch(
        val expected: EngineeringDefinitionId,
        val actual: EngineeringDefinitionId?,
        val provenance: SourceProvenance,
    ) : EngineeringQuantityComparison
}

sealed interface EngineeringQuantityOperation {
    data class Success(
        val value: ExactNumber,
        val dimension: EngineeringDimensionSignature,
    ) : EngineeringQuantityOperation

    data class AffineUnitNotAllowed(
        val operation: String,
        val units: List<EngineeringDefinitionId>,
    ) : EngineeringQuantityOperation
}

object EngineeringQuantityArithmetic {
    fun multiply(
        left: EngineeringValue.Quantity,
        leftUnit: ResolvedEngineeringUnit,
        right: EngineeringValue.Quantity,
        rightUnit: ResolvedEngineeringUnit,
    ): EngineeringQuantityOperation = calculate("multiply", left, leftUnit, right, rightUnit) {
        EngineeringQuantityOperation.Success(
            value = (left.value * leftUnit.scale) * (right.value * rightUnit.scale),
            dimension = leftUnit.dimension + rightUnit.dimension,
        )
    }

    fun divide(
        left: EngineeringValue.Quantity,
        leftUnit: ResolvedEngineeringUnit,
        right: EngineeringValue.Quantity,
        rightUnit: ResolvedEngineeringUnit,
    ): EngineeringQuantityOperation = calculate("divide", left, leftUnit, right, rightUnit) {
        EngineeringQuantityOperation.Success(
            value = (left.value * leftUnit.scale) / (right.value * rightUnit.scale),
            dimension = leftUnit.dimension - rightUnit.dimension,
        )
    }

    private inline fun calculate(
        operation: String,
        left: EngineeringValue.Quantity,
        leftUnit: ResolvedEngineeringUnit,
        right: EngineeringValue.Quantity,
        rightUnit: ResolvedEngineeringUnit,
        result: () -> EngineeringQuantityOperation.Success,
    ): EngineeringQuantityOperation {
        require(left.unit.resolvedId == leftUnit.id) { "Left Quantity unit does not match resolved unit semantics" }
        require(right.unit.resolvedId == rightUnit.id) { "Right Quantity unit does not match resolved unit semantics" }
        val affineUnits = listOf(leftUnit, rightUnit).filter { unit -> unit.isAffine }.map { unit -> unit.id }
        return if (affineUnits.isNotEmpty()) {
            EngineeringQuantityOperation.AffineUnitNotAllowed(operation, affineUnits)
        } else {
            result()
        }
    }
}

private fun SourceProvenance.location(): String = "$file:$startLine:$startColumn"
