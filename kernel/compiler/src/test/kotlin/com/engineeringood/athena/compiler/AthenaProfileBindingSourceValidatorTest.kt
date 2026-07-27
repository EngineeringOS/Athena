package com.engineeringood.athena.compiler

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaProfileBindingSourceValidatorTest {
    private val compiler = AthenaRepresentationSourceCompiler()

    @Test
    fun `missing profile and binding fields fail closed without default contracts`() {
        val result = compiler.compile("missing-contracts.athena", MISSING_CONTRACTS)

        assertTrue(result.profiles.isEmpty())
        assertTrue(result.bindingRules.isEmpty())
        assertEquals(
            setOf(
                "binding.priority.missing",
                "binding.profile.missing",
                "binding.selector.kind.missing",
                "binding.selector.type.missing",
                "binding.target.element.missing",
                "binding.target.version.missing",
                "profile.fallback.missing",
                "profile.projection.missing",
                "profile.standard.missing",
                "profile.style.missing",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertTrue(result.diagnostics.all { diagnostic -> diagnostic.span.end.offset > diagnostic.span.start.offset })
    }

    @Test
    fun `duplicate names invalid priority and unresolved references fail deterministically`() {
        val inputs = listOf(
            AthenaRepresentationSourceInput("a.athena", INVALID_REFERENCES),
            AthenaRepresentationSourceInput("b.athena", DUPLICATE_NAMES),
        )

        val first = compiler.compile(inputs)
        val second = compiler.compile(inputs.reversed())

        assertTrue(first.profiles.isEmpty())
        assertTrue(first.bindingRules.isEmpty())
        assertEquals(
            setOf(
                "binding.name.duplicate",
                "binding.priority.invalid",
                "binding.profile.unresolved",
                "binding.selector.type.duplicate",
                "binding.target.element.unresolved",
                "binding.target.version.invalid",
                "profile.name.duplicate",
            ),
            first.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertEquals(
            first.diagnostics.map { diagnostic -> diagnostic.code to diagnostic.subject },
            second.diagnostics.map { diagnostic -> diagnostic.code to diagnostic.subject },
        )
    }

    @Test
    fun `function binding selector requires exactly one role fact`() {
        val result = compiler.compile("function-role.athena", FUNCTION_WITHOUT_ROLE)

        assertTrue(result.bindingRules.isEmpty())
        assertEquals(
            listOf("binding.selector.role.missing"),
            result.diagnostics.map { diagnostic -> diagnostic.code },
        )
    }
}

private val MISSING_CONTRACTS = """
    package com.engineeringood.m34.invalid

    profile MissingProfile {
    }

    binding MissingBinding {
    }
""".trimIndent()

private val INVALID_REFERENCES = """
    package com.engineeringood.m34.invalid.a

    profile SharedProfile {
      projection schematic
      standard IEC
      style athena-industrial-iec-v1
      fallback fail-closed
    }

    binding SharedBinding {
      profile UnknownProfile
      priority 10.5
      select device where { type Breaker type Motor }
      use element "missing.element" version "not-semver"
    }
""".trimIndent()

private val DUPLICATE_NAMES = """
    package com.engineeringood.m34.invalid.b

    profile SharedProfile {
      projection schematic
      standard IEC
      style athena-industrial-iec-v1
      fallback fail-closed
    }

    binding SharedBinding {
      profile SharedProfile
      priority 10
      select device where { type Breaker }
      use element "also.missing" version "1.0.0"
    }
""".trimIndent()

private val FUNCTION_WITHOUT_ROLE = """
    package com.engineeringood.m34.invalid.function

    symbol coil_symbol {
      identity "iec.coil.symbol"
      version "1.0.0"
      graphic {
        bounds (0, 0, 10, 10)
        line body from (0, 5) to (10, 5) style symbol
      }
    }

    element coil_element {
      identity "iec.coil.element"
      version "1.0.0"
      bounds (0, 0, 10, 10)
      child glyph { symbol "iec.coil.symbol" translate (0, 0) rotate 0 scale (1, 1) zOrder 0 }
    }

    profile ControlDrawingIEC {
      projection schematic
      standard IEC
      style athena-industrial-iec-v1
      fallback fail-closed
    }

    binding CoilWithoutRole {
      profile ControlDrawingIEC
      priority 100
      select function where { type Contactor }
      use element "iec.coil.element" version "1.0.0"
    }
""".trimIndent()
