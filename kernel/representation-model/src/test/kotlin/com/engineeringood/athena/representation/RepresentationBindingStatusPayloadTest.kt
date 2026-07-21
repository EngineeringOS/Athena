package com.engineeringood.athena.representation

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentationBindingStatusPayloadTest {
    @Test
    fun `negative binding cases produce stable missing and ambiguous diagnostics`() {
        val diagnosticCodes = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(RepresentationLibraryId("athena.native.iec-v0")),
                policies = listOf(
                    RepresentationPolicy(
                        policyId = RepresentationPolicyId("policy:missing"),
                        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                        subjectKind = RepresentationSubjectKind.COMPONENT,
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        symbolFamilyId = SymbolFamilyId("missing"),
                        symbolId = RepresentationSymbolId("missing.symbol"),
                        fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
                        priority = RepresentationPolicyPriority(100),
                    ),
                    RepresentationPolicy(
                        policyId = RepresentationPolicyId("policy:unsupported"),
                        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                        subjectKind = RepresentationSubjectKind.COMPONENT,
                        occurrenceRole = RepresentationOccurrenceRole.COIL_ACTUATOR,
                        symbolFamilyId = SymbolFamilyId("iec.motor"),
                        symbolId = RepresentationSymbolId("iec.motor.compact"),
                        fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
                        priority = RepresentationPolicyPriority(100),
                    ),
                    RepresentationPolicy(
                        policyId = RepresentationPolicyId("policy:motor"),
                        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                        subjectKind = RepresentationSubjectKind.COMPONENT,
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        symbolFamilyId = SymbolFamilyId("iec.motor"),
                        symbolId = RepresentationSymbolId("iec.motor.compact"),
                        fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
                        priority = RepresentationPolicyPriority(100),
                    ),
                ),
                definitions = listOf(motorDefinition(), motorDefinition()),
                occurrences = listOf(
                    RepresentationOccurrence(
                        occurrenceId = RepresentationOccurrenceId("occurrence:bad"),
                        canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
                        projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/motor"),
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        symbolId = RepresentationSymbolId("iec.motor.compact"),
                        labelBindings = listOf(
                            RepresentationLabelBinding(RepresentationLabelSlotId("missing-slot"), LabelValue("M1")),
                        ),
                        terminalBindings = listOf(
                            RepresentationTerminalBinding(PresentationTerminalId("missing-terminal"), SemanticPortId("MotorM1.u1")),
                        ),
                    ),
                ),
                compatibleTerminalBindings = setOf(
                    RepresentationCompatibleTerminalBinding(PresentationTerminalId("missing-terminal"), SemanticPortId("MotorM1.u1")),
                ),
            ),
        ).diagnostics.map { diagnostic -> diagnostic.code.wireValue }.distinct()

        assertEquals(
            listOf(
                "representation.anchor.missing",
                "representation.binding.ambiguous",
                "representation.label-slot.missing",
                "representation.symbol.missing",
                "representation.symbol.unsupported-role",
            ),
            diagnosticCodes,
        )
    }

    @Test
    fun `accepted demo proof exposes clean binding status payload`() {
        val proof = M30DemoRepresentationBinder().bind(M30DemoRepresentationSample.controlSheet(), nativeLibrary())

        assertTrue(proof.diagnostics.isEmpty(), proof.diagnostics.toString())
        assertEquals(
            mapOf(
                "accepted" to "true",
                "deviceOccurrenceCount" to "7",
                "referenceOccurrenceCount" to "1",
                "diagnosticCount" to "0",
                "missingBindingDiagnosticCount" to "0",
            ),
            proof.toBindingStatusPayload(),
        )
    }

    @Test
    fun `renderer fallback is rejected when no binding diagnostic exists`() {
        assertFalse(
            RepresentationFallbackGuard.acceptsRendererFallback(
                fallbackUsed = true,
                diagnostics = emptyList(),
            ),
        )
    }

    private fun nativeLibrary(): NativeRepresentationLibrary {
        val resource = requireNotNull(
            javaClass.classLoader.getResource("representation-libraries/athena-native-iec-v0.properties"),
        ) { "Missing native M30 symbol pack resource." }
        val result = NativeRepresentationLibraryLoader().load(Path.of(resource.toURI()))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        return result.library
    }

    private fun motorDefinition(): RepresentationDefinition = nativeLibrary()
        .definitions
        .single { definition -> definition.symbolId == RepresentationSymbolId("iec.motor.compact") }
}
