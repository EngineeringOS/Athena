package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DerivedEngineeringContextContractTest {
    @Test
    fun `publishes the first narrow electrical input vocabulary`() {
        assertEquals(
            listOf(
                DerivedEngineeringInputKind.MOTOR_POWER,
                DerivedEngineeringInputKind.VOLTAGE,
                DerivedEngineeringInputKind.POWER_FACTOR,
                DerivedEngineeringInputKind.EFFICIENCY,
                DerivedEngineeringInputKind.BREAKER_RATED_CURRENT,
                DerivedEngineeringInputKind.CABLE_ALLOWED_CURRENT,
                DerivedEngineeringInputKind.RELAY_RATED_CURRENT,
            ),
            DerivedEngineeringInputKind.entries,
        )
    }

    @Test
    fun `derived engineering input preserves canonical identity authored property and provenance`() {
        val provenance = SourceProvenance(
            file = "examples/m9/factory-line.athena",
            startLine = 8,
            startColumn = 5,
            endLine = 8,
            endColumn = 24,
        )
        val trace = DerivedEngineeringInputTrace(
            subjectIdentity = StableSemanticIdentity("component:M1"),
            propertyName = "power",
            provenance = provenance,
        )

        val input = DerivedEngineeringInput(
            kind = DerivedEngineeringInputKind.MOTOR_POWER,
            trace = trace,
            authoredValue = EngineeringPropertyValue.Symbol("7.5kw"),
        )

        assertEquals(StableSemanticIdentity("component:M1"), input.trace.subjectIdentity)
        assertEquals("power", input.trace.propertyName)
        assertEquals(provenance, input.trace.provenance)
        assertEquals(EngineeringPropertyValue.Symbol("7.5kw"), input.authoredValue)
    }

    @Test
    fun `canonical context sorts subjects inputs and derived values deterministically`() {
        val breakerTrace = DerivedEngineeringInputTrace(
            subjectIdentity = StableSemanticIdentity("component:M1"),
            propertyName = "breakerCurrent",
            provenance = SourceProvenance("design.athena", 12, 3, 12, 28),
        )
        val powerTrace = DerivedEngineeringInputTrace(
            subjectIdentity = StableSemanticIdentity("component:M1"),
            propertyName = "power",
            provenance = SourceProvenance("design.athena", 10, 3, 10, 18),
        )

        val context = DerivedEngineeringContext.canonical(
            listOf(
                DerivedEngineeringSubjectContext(
                    subjectIdentity = StableSemanticIdentity("component:M2"),
                    inputs = emptyList(),
                    derivedValues = emptyList(),
                ),
                DerivedEngineeringSubjectContext(
                    subjectIdentity = StableSemanticIdentity("component:M1"),
                    inputs = listOf(
                        DerivedEngineeringInput(
                            kind = DerivedEngineeringInputKind.BREAKER_RATED_CURRENT,
                            trace = breakerTrace,
                            authoredValue = EngineeringPropertyValue.Symbol("18A"),
                        ),
                        DerivedEngineeringInput(
                            kind = DerivedEngineeringInputKind.MOTOR_POWER,
                            trace = powerTrace,
                            authoredValue = EngineeringPropertyValue.Symbol("7.5kw"),
                        ),
                    ),
                    derivedValues = listOf(
                        DerivedEngineeringValue(
                            kind = DerivedEngineeringValueKind.THERMAL_LOAD,
                            subjectIdentity = StableSemanticIdentity("component:M1"),
                            quantity = DerivedEngineeringQuantity.Decimal("5.7", "kW"),
                            trace = DerivedEngineeringValueTrace(listOf(powerTrace)),
                        ),
                        DerivedEngineeringValue(
                            kind = DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                            subjectIdentity = StableSemanticIdentity("component:M1"),
                            quantity = DerivedEngineeringQuantity.Decimal("14.1", "A"),
                            trace = DerivedEngineeringValueTrace(listOf(powerTrace, breakerTrace)),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("component:M1", "component:M2"),
            context.subjects.map { subject -> subject.subjectIdentity.value },
        )
        assertEquals(
            listOf(
                DerivedEngineeringInputKind.MOTOR_POWER,
                DerivedEngineeringInputKind.BREAKER_RATED_CURRENT,
            ),
            context.subjects.first().inputs.map { input -> input.kind },
        )
        assertEquals(
            listOf(
                DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                DerivedEngineeringValueKind.THERMAL_LOAD,
            ),
            context.subjects.first().derivedValues.map { value -> value.kind },
        )
    }

    @Test
    fun `derived value trace keeps the exact source inputs that explain one context value`() {
        val voltageTrace = DerivedEngineeringInputTrace(
            subjectIdentity = StableSemanticIdentity("component:M1"),
            propertyName = "voltage",
            provenance = SourceProvenance("design.athena", 11, 3, 11, 19),
        )
        val powerFactorTrace = DerivedEngineeringInputTrace(
            subjectIdentity = StableSemanticIdentity("component:M1"),
            propertyName = "powerFactor",
            provenance = SourceProvenance("design.athena", 12, 3, 12, 24),
        )

        val value = DerivedEngineeringValue(
            kind = DerivedEngineeringValueKind.STARTING_CURRENT,
            subjectIdentity = StableSemanticIdentity("component:M1"),
            quantity = DerivedEngineeringQuantity.Decimal("84", "A"),
            trace = DerivedEngineeringValueTrace(
                listOf(voltageTrace, powerFactorTrace),
            ),
        )

        assertEquals(2, value.trace.sourceInputs.size)
        assertTrue(value.trace.sourceInputs.contains(voltageTrace))
        assertTrue(value.trace.sourceInputs.contains(powerFactorTrace))
    }
}
