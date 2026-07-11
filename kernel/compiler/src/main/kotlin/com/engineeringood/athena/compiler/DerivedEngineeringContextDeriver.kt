package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.DerivedEngineeringContext
import com.engineeringood.athena.ir.DerivedEngineeringInput
import com.engineeringood.athena.ir.DerivedEngineeringInputKind
import com.engineeringood.athena.ir.DerivedEngineeringInputTrace
import com.engineeringood.athena.ir.DerivedEngineeringQuantity
import com.engineeringood.athena.ir.DerivedEngineeringSubjectContext
import com.engineeringood.athena.ir.DerivedEngineeringValue
import com.engineeringood.athena.ir.DerivedEngineeringValueKind
import com.engineeringood.athena.ir.DerivedEngineeringValueTrace
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Derives the first narrow M9 engineering context values directly from canonical `EngineeringDocument`.
 *
 * This proof stays compiler-owned and deterministic. It does not widen parser grammar, does not emit capability
 * facts, and does not depend on layout, geometry, runtime, or renderer state.
 */
class DerivedEngineeringContextDeriver {
    /**
     * Builds the first-wave derived engineering context snapshot from canonical semantic state.
     */
    fun derive(document: EngineeringDocument): DerivedEngineeringContext {
        val subjects = document.components.mapNotNull { component ->
            deriveSubject(component)
        }
        return DerivedEngineeringContext.canonical(subjects)
    }

    private fun deriveSubject(component: EngineeringComponent): DerivedEngineeringSubjectContext? {
        val componentType = component.properties.symbolOrTextValue(TYPE_PROPERTY_NAME) ?: return null
        val normalizedInputs = when (componentType) {
            MOTOR_COMPONENT_TYPE -> deriveMotorInputs(component)
            else -> emptyList()
        }
        if (normalizedInputs.isEmpty()) {
            return null
        }

        val derivedValues = buildList {
            deriveFullLoadCurrent(component, normalizedInputs)?.let(::add)
            deriveThermalLoad(component, normalizedInputs)?.let(::add)
        }
        return DerivedEngineeringSubjectContext(
            subjectIdentity = component.id,
            inputs = normalizedInputs.map { input -> input.input },
            derivedValues = derivedValues,
        )
    }

    private fun deriveMotorInputs(component: EngineeringComponent): List<NormalizedDerivedInput> {
        return buildList {
            normalizeQuantityInput(
                component = component,
                propertyNames = listOf("power"),
                kind = DerivedEngineeringInputKind.MOTOR_POWER,
                unit = "kw",
                normalize = { quantity -> quantity.requireUnit("kw") },
            )?.let(::add)
            normalizeQuantityInput(
                component = component,
                propertyNames = listOf("voltage"),
                kind = DerivedEngineeringInputKind.VOLTAGE,
                unit = "V",
                normalize = { quantity -> quantity.requireUnit("v") },
            )?.let(::add)
            normalizeQuantityInput(
                component = component,
                propertyNames = listOf("powerFactor", "pf"),
                kind = DerivedEngineeringInputKind.POWER_FACTOR,
                unit = null,
                normalize = { quantity -> quantity.requireRatio() },
            )?.let(::add)
            normalizeQuantityInput(
                component = component,
                propertyNames = listOf("efficiency", "eta"),
                kind = DerivedEngineeringInputKind.EFFICIENCY,
                unit = null,
                normalize = { quantity -> quantity.requireRatio() },
            )?.let(::add)
            normalizeQuantityInput(
                component = component,
                propertyNames = listOf("breakerRatedCurrent", "breakerCurrent"),
                kind = DerivedEngineeringInputKind.BREAKER_RATED_CURRENT,
                unit = "A",
                normalize = { quantity -> quantity.requireUnit("a") },
            )?.let(::add)
            normalizeQuantityInput(
                component = component,
                propertyNames = listOf("cableAllowedCurrent", "cableCurrent"),
                kind = DerivedEngineeringInputKind.CABLE_ALLOWED_CURRENT,
                unit = "A",
                normalize = { quantity -> quantity.requireUnit("a") },
            )?.let(::add)
            normalizeQuantityInput(
                component = component,
                propertyNames = listOf("relayRatedCurrent", "relayCurrent"),
                kind = DerivedEngineeringInputKind.RELAY_RATED_CURRENT,
                unit = "A",
                normalize = { quantity -> quantity.requireUnit("a") },
            )?.let(::add)
        }
    }

    private fun deriveFullLoadCurrent(
        component: EngineeringComponent,
        inputs: List<NormalizedDerivedInput>,
    ): DerivedEngineeringValue? {
        val power = inputs.find { input -> input.input.kind == DerivedEngineeringInputKind.MOTOR_POWER } ?: return null
        val voltage = inputs.find { input -> input.input.kind == DerivedEngineeringInputKind.VOLTAGE } ?: return null
        val powerFactor = inputs.find { input -> input.input.kind == DerivedEngineeringInputKind.POWER_FACTOR } ?: return null
        val efficiency = inputs.find { input -> input.input.kind == DerivedEngineeringInputKind.EFFICIENCY } ?: return null
        val quantity = power.decimalValue
            .multiply(KILOWATTS_TO_WATTS)
            .divide(
                SQRT_THREE.multiply(voltage.decimalValue).multiply(powerFactor.decimalValue).multiply(efficiency.decimalValue),
                FORMULA_SCALE,
                RoundingMode.HALF_UP,
            )
        return DerivedEngineeringValue(
            kind = DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
            subjectIdentity = component.id,
            quantity = DerivedEngineeringQuantity.Decimal(
                canonicalText = quantity.toCanonicalDecimalText(),
                unitSymbol = "A",
            ),
            trace = DerivedEngineeringValueTrace(
                sourceInputs = listOf(
                    power.input.trace,
                    voltage.input.trace,
                    powerFactor.input.trace,
                    efficiency.input.trace,
                ),
            ),
        )
    }

    private fun deriveThermalLoad(
        component: EngineeringComponent,
        inputs: List<NormalizedDerivedInput>,
    ): DerivedEngineeringValue? {
        val power = inputs.find { input -> input.input.kind == DerivedEngineeringInputKind.MOTOR_POWER } ?: return null
        val efficiency = inputs.find { input -> input.input.kind == DerivedEngineeringInputKind.EFFICIENCY } ?: return null
        val quantity = power.decimalValue
            .multiply(BigDecimal.ONE.divide(efficiency.decimalValue, FORMULA_SCALE, RoundingMode.HALF_UP).subtract(BigDecimal.ONE))
        return DerivedEngineeringValue(
            kind = DerivedEngineeringValueKind.THERMAL_LOAD,
            subjectIdentity = component.id,
            quantity = DerivedEngineeringQuantity.Decimal(
                canonicalText = quantity.toCanonicalDecimalText(),
                unitSymbol = "kW",
            ),
            trace = DerivedEngineeringValueTrace(
                sourceInputs = listOf(
                    power.input.trace,
                    efficiency.input.trace,
                ),
            ),
        )
    }

    private fun normalizeQuantityInput(
        component: EngineeringComponent,
        propertyNames: List<String>,
        kind: DerivedEngineeringInputKind,
        unit: String?,
        normalize: (ParsedAuthoredQuantity) -> BigDecimal?,
    ): NormalizedDerivedInput? {
        val property = component.properties.singleProperty(propertyNames) ?: return null
        val parsedValue = parseAuthoredQuantity(property.value) ?: return null
        val normalizedValue = normalize(parsedValue) ?: return null
        val matchedPropertyName = propertyNames.first { candidate -> candidate.equals(property.name, ignoreCase = true) }
        return NormalizedDerivedInput(
            input = DerivedEngineeringInput(
                kind = kind,
                trace = DerivedEngineeringInputTrace(
                    subjectIdentity = component.id,
                    propertyName = matchedPropertyName,
                    provenance = component.provenance,
                ),
                authoredValue = property.value,
            ),
            decimalValue = normalizedValue,
            unit = unit,
        )
    }
}

private data class NormalizedDerivedInput(
    val input: DerivedEngineeringInput,
    val decimalValue: BigDecimal,
    val unit: String?,
)

private data class ParsedAuthoredQuantity(
    val numericValue: BigDecimal,
    val unit: String?,
)

private fun List<EngineeringProperty>.singleProperty(propertyNames: List<String>): EngineeringProperty? {
    val matches = filter { property ->
        propertyNames.any { candidate -> candidate.equals(property.name, ignoreCase = true) }
    }
    return matches.singleOrNull()
}

private fun List<EngineeringProperty>.symbolOrTextValue(name: String): String? {
    val property = singleProperty(listOf(name)) ?: return null
    return when (val value = property.value) {
        is EngineeringPropertyValue.Symbol -> value.text
        is EngineeringPropertyValue.Text -> value.text
    }
}

private fun parseAuthoredQuantity(value: EngineeringPropertyValue): ParsedAuthoredQuantity? {
    val rawText = when (value) {
        is EngineeringPropertyValue.Symbol -> value.text
        is EngineeringPropertyValue.Text -> value.text
    }.trim()
    if (rawText.isEmpty()) {
        return null
    }
    val compact = rawText.replace(" ", "")
    val match = AUTHORED_QUANTITY_REGEX.matchEntire(compact) ?: return null
    return ParsedAuthoredQuantity(
        numericValue = match.groupValues[1].toBigDecimalOrNull() ?: return null,
        unit = match.groupValues[2].takeIf { it.isNotEmpty() }?.lowercase(),
    )
}

private fun ParsedAuthoredQuantity.requireUnit(expectedUnit: String): BigDecimal? {
    return if (unit == expectedUnit) {
        numericValue
    } else {
        null
    }
}

private fun ParsedAuthoredQuantity.requireRatio(): BigDecimal? {
    return when (unit) {
        null -> numericValue.takeIf { it > BigDecimal.ZERO && it <= BigDecimal.ONE }
        "%" -> numericValue
            .divide(HUNDRED, FORMULA_SCALE, RoundingMode.HALF_UP)
            .takeIf { normalized -> normalized > BigDecimal.ZERO && normalized <= BigDecimal.ONE }
        else -> null
    }
}

private fun BigDecimal.toCanonicalDecimalText(): String {
    return setScale(OUTPUT_SCALE, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

private const val TYPE_PROPERTY_NAME = "type"
private const val MOTOR_COMPONENT_TYPE = "Motor"
private const val FORMULA_SCALE = 12
private const val OUTPUT_SCALE = 4

private val AUTHORED_QUANTITY_REGEX = Regex("""^([0-9]+(?:\.[0-9]+)?)([A-Za-z%]+)?$""")
private val SQRT_THREE = BigDecimal("1.7320508075688772")
private val KILOWATTS_TO_WATTS = BigDecimal("1000")
private val HUNDRED = BigDecimal("100")
