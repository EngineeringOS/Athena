package com.engineeringood.athena.reuse

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SemanticMacroContractTest {
    @Test
    fun `semantic macro contract keeps macro identity separate from package identity`() {
        val packageId = PackageIdentifier(name = "athena.motors", version = "1.0.0")
        val contract = SemanticMacroContract(
            macroId = SemanticMacroId("macro:motor-starter"),
            displayName = "Motor starter",
            summary = "Reusable semantic assembly for one governed motor starter.",
            packageBinding = SemanticMacroPackageBinding(
                packageId = packageId,
                definitionPath = "macros/motor-starter.yaml",
            ),
            parameters = listOf(
                SemanticMacroParameterDefinition(
                    name = SemanticMacroParameterName("starterMode"),
                    valueKind = SemanticMacroParameterValueKind.SYMBOL,
                    label = "Starter mode",
                    description = "Selects the governed starter arrangement.",
                    defaultValue = SemanticMacroParameterValue.Symbol("DOL"),
                    validationRules = SemanticMacroParameterValidationRules(
                        allowedValues = listOf("DOL", "REVERSING"),
                    ),
                ),
            ),
            classificationKeys = setOf("electrical", "starter"),
        )

        assertEquals("macro:motor-starter", contract.macroId.value)
        assertEquals("athena.motors", contract.packageBinding.packageId.name)
        assertNotEquals(contract.macroId.value, contract.packageBinding.packageId.name)
        assertEquals("macros/motor-starter.yaml", contract.packageBinding.definitionPath)
    }

    @Test
    fun `parameter schema stays surface agnostic and transport friendly`() {
        val parameter = SemanticMacroParameterDefinition(
            name = SemanticMacroParameterName("overloadAmps"),
            valueKind = SemanticMacroParameterValueKind.INTEGER,
            label = "Overload current",
            description = "Nominal overload relay current in amps.",
            required = true,
            defaultValue = SemanticMacroParameterValue.IntegerValue(12),
            validationRules = SemanticMacroParameterValidationRules(
                minInteger = 1,
                maxInteger = 32,
            ),
        )

        assertEquals("overloadAmps", parameter.name.value)
        assertEquals(SemanticMacroParameterValueKind.INTEGER, parameter.valueKind)
        assertTrue(parameter.required)
        assertEquals(12, (parameter.defaultValue as SemanticMacroParameterValue.IntegerValue).value)
        assertEquals(1, parameter.validationRules.minInteger)
        assertEquals(32, parameter.validationRules.maxInteger)
    }

    @Test
    fun `accepted expansion preserves semantic origin and membership`() {
        val preview = SemanticMacroPreview(
            previewId = SemanticMacroPreviewId("preview:motor-starter"),
            macroId = SemanticMacroId("macro:motor-starter"),
            instantiationId = SemanticMacroInstantiationId("instance:M1"),
            title = "Instantiate motor starter M1",
            changes = listOf(
                SemanticMacroPreviewChange(
                    kind = SemanticMacroPreviewChangeKind.CREATE,
                    title = "Create governed motor starter assembly",
                    affectedSubjectIdentities = setOf(
                        StableSemanticIdentity("component:M1.K1"),
                        StableSemanticIdentity("component:M1.F1"),
                    ),
                ),
            ),
        )
        val acceptedExpansion = SemanticMacroAcceptedExpansion(
            expansionId = SemanticMacroExpansionId("expansion:motor-starter:M1"),
            previewId = preview.previewId,
            origin = ExpansionOrigin(
                macroId = preview.macroId,
                instantiationId = preview.instantiationId,
                packageBinding = SemanticMacroPackageBinding(
                    packageId = PackageIdentifier("athena.motors", "1.0.0"),
                    definitionPath = "macros/motor-starter.yaml",
                ),
                parameterValues = mapOf(
                    SemanticMacroParameterName("starterMode") to SemanticMacroParameterValue.Symbol("DOL"),
                ),
            ),
            memberships = listOf(
                ExpansionMembership(
                    instantiationId = preview.instantiationId,
                    subjectId = StableSemanticIdentity("component:M1.K1"),
                    role = "contactor",
                ),
                ExpansionMembership(
                    instantiationId = preview.instantiationId,
                    subjectId = StableSemanticIdentity("component:M1.F1"),
                    role = "overload",
                ),
            ),
        )

        assertEquals("preview:motor-starter", acceptedExpansion.previewId.value)
        assertEquals("macro:motor-starter", acceptedExpansion.origin.macroId.value)
        assertEquals("instance:M1", acceptedExpansion.origin.instantiationId.value)
        assertEquals(2, acceptedExpansion.memberships.size)
        assertTrue(acceptedExpansion.memberships.all { membership -> membership.subjectId.value.startsWith("component:") })
    }
}
