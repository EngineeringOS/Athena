package com.engineeringood.athena.packageplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class RepresentationBindingRuleModelsTest {
    @Test
    fun `representation binding rule is typed package-model selector input`() {
        val rule = RepresentationBindingRule(
            ruleId = RepresentationBindingRuleId("binding.cabinet.protective-device"),
            profileId = PresentationProfileId("CabinetIEC"),
            projectionContext = ProjectionContextId("cabinet"),
            conceptId = EngineeringConceptId("ProtectiveDevice"),
            selectorFacts = listOf(
                RepresentationBindingSelectorFact("type", "Switch"),
                RepresentationBindingSelectorFact("model", "GV2"),
            ),
            target = RepresentationBindingTarget(
                representationPackageId = RepresentationPackageId("com.athena.cabinet.iec"),
                descriptorId = RepresentationDescriptorId("element.protective-device.gv2"),
                packageVersion = RepresentationPackageVersion("1.0.0"),
                variantId = RepresentationVariantId("standard"),
            ),
            priority = RepresentationBindingPriority(100),
            lifecycle = RepresentationBindingRuleLifecycle(RepresentationBindingRuleLifecycleState.ACTIVE),
            provenance = RepresentationBindingRuleProvenance(
                sources = listOf("packages/representation/athena/cabinet/bindings.athena"),
                reviewedBy = "Athena M34",
            ),
        )

        assertEquals("CabinetIEC", rule.profileId.value)
        assertEquals("cabinet", rule.projectionContext.value)
        assertEquals("ProtectiveDevice", rule.conceptId.value)
        assertEquals("com.athena.cabinet.iec", rule.target.representationPackageId.value)
        assertEquals("element.protective-device.gv2", rule.target.descriptorId.value)
        assertEquals("1.0.0", rule.target.packageVersion.value)
        assertEquals("standard", rule.target.variantId?.value)
        assertEquals(100, rule.priority.value)
        assertEquals(RepresentationBindingRuleLifecycleState.ACTIVE, rule.lifecycle.state)
        assertEquals(listOf("type" to "Switch", "model" to "GV2"), rule.selectorFacts.map { it.name to it.value })
    }
}
