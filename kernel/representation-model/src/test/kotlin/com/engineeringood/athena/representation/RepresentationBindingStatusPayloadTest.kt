package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepresentationBindingStatusPayloadTest {
    @Test
    fun `negative binding cases produce stable missing and ambiguous diagnostics`() {
        val diagnosticCodes = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(RepresentationLibraryId("athena.native.iec")),
                policies = listOf(
                    RepresentationPolicy(
                        policyId = RepresentationPolicyId("policy:missing"),
                        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                        subjectKind = RepresentationSubjectKind.COMPONENT,
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        symbolFamilyId = SymbolFamilyId("missing"),
                        symbolId = RepresentationSymbolId("missing.symbol"),
                        priority = RepresentationPolicyPriority(100),
                    ),
                    RepresentationPolicy(
                        policyId = RepresentationPolicyId("policy:unsupported"),
                        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                        subjectKind = RepresentationSubjectKind.COMPONENT,
                        occurrenceRole = RepresentationOccurrenceRole.COIL_ACTUATOR,
                        symbolFamilyId = SymbolFamilyId("iec.motor"),
                        symbolId = RepresentationSymbolId("iec.motor.compact"),
                        priority = RepresentationPolicyPriority(100),
                    ),
                    RepresentationPolicy(
                        policyId = RepresentationPolicyId("policy:motor"),
                        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                        subjectKind = RepresentationSubjectKind.COMPONENT,
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        symbolFamilyId = SymbolFamilyId("iec.motor"),
                        symbolId = RepresentationSymbolId("iec.motor.compact"),
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
                        portAnchorBindings = listOf(
                            RepresentationPortAnchorBinding(
                                bindingId = RepresentationPortAnchorBindingId("binding:MotorM1.u1:missing-terminal"),
                                semanticPortId = SemanticPortId("MotorM1.u1"),
                                anchorId = RepresentationAnchorId("missing-terminal"),
                                provenance = RepresentationProvenance("source:motor.athena:1:1"),
                            ),
                        ),
                    ),
                ),
                compatiblePortAnchorBindings = setOf(
                    RepresentationCompatiblePortAnchorBinding(
                        SemanticPortId("MotorM1.u1"),
                        RepresentationAnchorId("missing-terminal"),
                    ),
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

    private fun motorDefinition(): RepresentationDefinition = nativeLibrary()
        .definitions
        .single { definition -> definition.symbolId == RepresentationSymbolId("iec.motor.compact") }

    private fun nativeLibrary(): NativeRepresentationLibrary {
        val resource = requireNotNull(
            javaClass.classLoader.getResource("representation-libraries/athena-native-iec.properties"),
        ) { "Missing native symbol pack resource." }
        val result = NativeRepresentationLibraryLoader().load(java.nio.file.Path.of(resource.toURI()))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        return result.library
    }
}
