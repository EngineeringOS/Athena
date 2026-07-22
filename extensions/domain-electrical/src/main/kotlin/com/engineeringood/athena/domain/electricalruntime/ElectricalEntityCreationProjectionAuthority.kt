package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.component.EngineeringConceptTemplate
import com.engineeringood.athena.representation.CompositionIntentMembershipId
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.NativeRepresentationLibraryLoader
import com.engineeringood.athena.representation.RepresentationBindingCompiler
import com.engineeringood.athena.representation.RepresentationBindingRequest
import com.engineeringood.athena.representation.RepresentationFallbackBehavior
import com.engineeringood.athena.representation.RepresentationLabelSlotId
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationPolicy
import com.engineeringood.athena.representation.RepresentationPolicyId
import com.engineeringood.athena.representation.RepresentationPolicyPriority
import com.engineeringood.athena.representation.RepresentationProjectionKind
import com.engineeringood.athena.representation.RepresentationProjectionOccurrenceId
import com.engineeringood.athena.representation.RepresentationSemanticRole
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSubjectKind
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationVariantId
import com.engineeringood.athena.representation.SchematicCompositionFactKind
import com.engineeringood.athena.representation.SchematicCompositionInput
import com.engineeringood.athena.representation.SchematicCompositionIntentCompiler
import com.engineeringood.athena.representation.SchematicSpatialIntentReference
import com.engineeringood.athena.representation.SymbolFamilyId
import com.engineeringood.athena.runtime.GovernedEntityCreationCompositionUnsatisfied
import com.engineeringood.athena.runtime.GovernedEntityCreationProjectionAuthority
import com.engineeringood.athena.runtime.GovernedEntityCreationProjectionResolved
import com.engineeringood.athena.runtime.GovernedEntityCreationProjectionResult
import com.engineeringood.athena.runtime.GovernedEntityCreationRepresentationUnresolved

/** Electrical representation policy adapter for the first governed motor-creation proof. */
class ElectricalEntityCreationProjectionAuthority(
    private val libraryLoader: NativeRepresentationLibraryLoader = NativeRepresentationLibraryLoader(),
    private val bindingCompiler: RepresentationBindingCompiler = RepresentationBindingCompiler(),
    private val compositionCompiler: SchematicCompositionIntentCompiler = SchematicCompositionIntentCompiler(),
) : GovernedEntityCreationProjectionAuthority {
    override fun resolve(
        intent: CreateSemanticEntityIntent,
        template: EngineeringConceptTemplate,
        canonicalTag: String,
    ): GovernedEntityCreationProjectionResult {
        if (!supports(template.templateId.value)) {
            return GovernedEntityCreationRepresentationUnresolved(
                "No governed electrical representation policy is registered for `${template.templateId.value}`.",
            )
        }
        val libraryResult = libraryLoader.loadBundled()
        val library = libraryResult.libraryOrNull
            ?: return GovernedEntityCreationRepresentationUnresolved(
                libraryResult.diagnostics.joinToString("; ") { diagnostic -> diagnostic.message },
            )
        val symbolId = RepresentationSymbolId(MOTOR_SYMBOL_ID)
        val definition = library.definitions.singleOrNull { candidate -> candidate.symbolId == symbolId }
            ?: return GovernedEntityCreationRepresentationUnresolved(
                "Native representation `$MOTOR_SYMBOL_ID` is unavailable.",
            )
        val projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/component:$canonicalTag")
        val compositionMembership = CompositionIntentMembershipId("composition:alignment_group")
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:electrical.motor.ac"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("power-load"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolFamilyId = SymbolFamilyId("iec.motor"),
            symbolId = symbolId,
            variant = RepresentationVariantId("compact"),
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val binding = bindingCompiler.bind(
            RepresentationBindingRequest(
                canonicalSemanticId = RepresentationSubjectId("component:$canonicalTag"),
                projectionOccurrenceId = projectionOccurrenceId,
                subjectKind = RepresentationSubjectKind.COMPONENT,
                semanticRole = policy.semanticRole,
                projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                policy = policy,
                definition = definition,
                labelValues = mapOf(RepresentationLabelSlotId("device-tag") to LabelValue(canonicalTag)),
                terminalPorts = emptyMap(),
                priority = policy.priority,
                compositionIntentMembership = listOf(compositionMembership),
            ),
        )
        val occurrence = binding.occurrenceOrNull
            ?: return GovernedEntityCreationRepresentationUnresolved(
                binding.diagnostics.joinToString("; ") { diagnostic -> diagnostic.message },
            )
        val composition = compositionCompiler.plan(
            SchematicCompositionInput(
                occurrences = listOf(occurrence),
                boundsByOccurrence = mapOf(occurrence.occurrenceId to definition.anatomy.bounds),
                terminalAnchorCountByOccurrence = mapOf(
                    occurrence.occurrenceId to definition.anatomy.terminals.size,
                ),
                spatialIntentReferences = listOf(SchematicSpatialIntentReference("m27:sheet:control")),
            ),
        )
        val target = composition.facts.singleOrNull { fact ->
            fact.kind == SchematicCompositionFactKind.ALIGNMENT_GROUP &&
                occurrence.occurrenceId in fact.occurrenceIds
        } ?: return GovernedEntityCreationCompositionUnsatisfied(
            representationId = symbolId.value,
            reason = "M30 composition planning produced no alignment target for `component:$canonicalTag`.",
        )
        return GovernedEntityCreationProjectionResolved(
            representationId = symbolId.value,
            compositionTargetId = target.membershipId.value,
            projectionOccurrenceIds = listOf(projectionOccurrenceId.value),
        )
    }

    companion object {
        const val MOTOR_TEMPLATE_ID = "electrical.motor.ac.default"
        private const val MOTOR_SYMBOL_ID = "iec.motor.compact"

        fun supports(templateId: String): Boolean = templateId == MOTOR_TEMPLATE_ID
    }
}
