package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.DerivedEngineeringInputKind
import com.engineeringood.athena.ir.DerivedEngineeringQuantity
import com.engineeringood.athena.ir.DerivedEngineeringValueKind
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DerivedEngineeringContextDeriverTest {
    @Test
    fun `derive computes deterministic motor context values from canonical engineering state`() {
        val context = DerivedEngineeringContextDeriver().derive(
            EngineeringDocument(
                system = EngineeringSystem(
                    id = StableSemanticIdentity("system:MotorContextProof"),
                    name = "MotorContextProof",
                    provenance = proofProvenance(),
                ),
                components = listOf(
                    EngineeringComponent(
                        id = StableSemanticIdentity("component:M1"),
                        name = "M1",
                        kind = "device",
                        properties = listOf(
                            EngineeringProperty("type", EngineeringPropertyValue.Symbol("Motor")),
                            EngineeringProperty("power", EngineeringPropertyValue.Text("7.5kw")),
                            EngineeringProperty("voltage", EngineeringPropertyValue.Text("400V")),
                            EngineeringProperty("pf", EngineeringPropertyValue.Text("0.86")),
                            EngineeringProperty("efficiency", EngineeringPropertyValue.Text("92%")),
                        ),
                        provenance = proofProvenance(),
                    ),
                ),
                ports = emptyList(),
                connections = emptyList(),
            ),
        )

        val subject = context.subjects.single()
        assertEquals(StableSemanticIdentity("component:M1"), subject.subjectIdentity)
        assertEquals(
            listOf(
                DerivedEngineeringInputKind.MOTOR_POWER,
                DerivedEngineeringInputKind.VOLTAGE,
                DerivedEngineeringInputKind.POWER_FACTOR,
                DerivedEngineeringInputKind.EFFICIENCY,
            ),
            subject.inputs.map { input -> input.kind },
        )
        assertEquals(
            listOf(
                DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                DerivedEngineeringValueKind.THERMAL_LOAD,
            ),
            subject.derivedValues.map { value -> value.kind },
        )
        assertEquals(
            DerivedEngineeringQuantity.Decimal(
                canonicalText = "13.6822",
                unitSymbol = "A",
            ),
            subject.derivedValues.first { value -> value.kind == DerivedEngineeringValueKind.FULL_LOAD_CURRENT }.quantity,
        )
        assertEquals(
            DerivedEngineeringQuantity.Decimal(
                canonicalText = "0.6522",
                unitSymbol = "kW",
            ),
            subject.derivedValues.first { value -> value.kind == DerivedEngineeringValueKind.THERMAL_LOAD }.quantity,
        )
        assertTrue(
            subject.derivedValues.all { value ->
                value.trace.sourceInputs.all { trace ->
                    trace.subjectIdentity == StableSemanticIdentity("component:M1") &&
                        trace.provenance == proofProvenance()
                }
            },
        )
    }

    @Test
    fun `compile exposes derived engineering context through compiler compilation success`() {
        val examplePath = resolveRepoRoot().resolve("examples/m9/motor-derived-context.athena")

        val result = AthenaCompiler().compile(examplePath)

        val success = assertIs<CompilerCompilationSuccess>(result)
        val subject = success.derivedContext.subjects.single()
        assertEquals(StableSemanticIdentity("component:M1"), subject.subjectIdentity)
        assertEquals(
            listOf(
                DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                DerivedEngineeringValueKind.THERMAL_LOAD,
            ),
            subject.derivedValues.map { value -> value.kind },
        )
        assertEquals(
            listOf(
                "power",
                "voltage",
                "powerFactor",
                "efficiency",
                "breakerRatedCurrent",
                "cableAllowedCurrent",
                "relayRatedCurrent",
            ),
            subject.inputs.map { input -> input.trace.propertyName },
        )
        assertEquals(
            "13.6822",
            (subject.derivedValues.first { value -> value.kind == DerivedEngineeringValueKind.FULL_LOAD_CURRENT }.quantity
                as DerivedEngineeringQuantity.Decimal).canonicalText,
        )
    }

    private fun proofProvenance(): SourceProvenance {
        return SourceProvenance(
            file = "examples/m9/motor-derived-context.athena",
            startLine = 1,
            startColumn = 1,
            endLine = 13,
            endColumn = 1,
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
