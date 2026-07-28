package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PhysicalInstallationContractResolverTest {
    private val subject = StableSemanticIdentity("component:QF35")

    @Test
    fun `resolves project scalar fields over trait fields independently with provenance`() {
        val result = PhysicalInstallationContractResolver.resolve(
            subjectIdentity = subject,
            facts = listOf(
                traitFact(
                    field = PhysicalInstallationContractField.Width,
                    value = PhysicalInstallationContractValue.LengthMillimeters(40),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.Height,
                    value = PhysicalInstallationContractValue.LengthMillimeters(90),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.Depth,
                    value = PhysicalInstallationContractValue.LengthMillimeters(70),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.MountingType,
                    value = PhysicalInstallationContractValue.MountingType(PhysicalMountingTypeId("screw")),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.ClearanceTop,
                    value = PhysicalInstallationContractValue.LengthMillimeters(5),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.ClearanceRight,
                    value = PhysicalInstallationContractValue.LengthMillimeters(5),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.ClearanceBottom,
                    value = PhysicalInstallationContractValue.LengthMillimeters(5),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.ClearanceLeft,
                    value = PhysicalInstallationContractValue.LengthMillimeters(5),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.Width,
                    value = PhysicalInstallationContractValue.LengthMillimeters(45),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.MountingType,
                    value = PhysicalInstallationContractValue.MountingType(PhysicalMountingTypeId("din35")),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.ClearanceTop,
                    value = PhysicalInstallationContractValue.LengthMillimeters(10),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.AllowedOrientations,
                    value = PhysicalInstallationContractValue.Orientations(
                        setOf(PhysicalInstallationOrientation.Deg0, PhysicalInstallationOrientation.Deg90),
                    ),
                ),
                traitFact(
                    field = PhysicalInstallationContractField.CompatibleContainerKinds,
                    value = PhysicalInstallationContractValue.ContainerKinds(
                        setOf(PhysicalContainerKindId("cabinet")),
                    ),
                ),
            ),
        )

        val contract = assertIs<PhysicalInstallationContractResolution.Success>(result).contract

        assertEquals(45, contract.size.width.value)
        assertEquals(90, contract.size.height.value)
        assertEquals(70, contract.size.depth.value)
        assertEquals("din35", contract.mountingTypeId.value)
        assertEquals(10, contract.clearance.top.value)
        assertEquals(5, contract.clearance.right.value)
        assertEquals(PhysicalContractSourceKind.Project, contract.provenance.width.source.kind)
        assertEquals(PhysicalContractSourceKind.Trait, contract.provenance.height.source.kind)
        assertEquals(PhysicalContractSourceKind.Project, contract.provenance.mountingType.source.kind)
        assertEquals(PhysicalContractSourceKind.Project, contract.provenance.clearanceTop.source.kind)
    }

    @Test
    fun `set fields replace atomically by precedence and digest uses canonical order`() {
        val result = PhysicalInstallationContractResolver.resolve(
            subjectIdentity = subject,
            facts = completeTraitFacts() + listOf(
                projectFact(
                    field = PhysicalInstallationContractField.AllowedOrientations,
                    value = PhysicalInstallationContractValue.Orientations(
                        setOf(PhysicalInstallationOrientation.Deg180, PhysicalInstallationOrientation.Deg0),
                    ),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.CompatibleContainerKinds,
                    value = PhysicalInstallationContractValue.ContainerKinds(
                        setOf(PhysicalContainerKindId("panel"), PhysicalContainerKindId("cabinet")),
                    ),
                ),
            ),
        )

        val contract = assertIs<PhysicalInstallationContractResolution.Success>(result).contract

        assertEquals(
            listOf(PhysicalInstallationOrientation.Deg0, PhysicalInstallationOrientation.Deg180),
            contract.allowedOrientations.toList(),
        )
        assertEquals(
            listOf(PhysicalContainerKindId("cabinet"), PhysicalContainerKindId("panel")),
            contract.compatibleContainerKinds.toList(),
        )
        assertEquals(
            "size.width=45;size.height=90;size.depth=70;mountingType=din35;" +
                "orientations=deg0,deg180;clearance.top=10;clearance.right=5;" +
                "clearance.bottom=10;clearance.left=5;containers=cabinet,panel",
            contract.canonicalDigestMaterial,
        )
    }

    @Test
    fun `fails closed on duplicate same-precedence facts missing required fields and empty sets`() {
        val result = PhysicalInstallationContractResolver.resolve(
            subjectIdentity = subject,
            facts = listOf(
                projectFact(
                    field = PhysicalInstallationContractField.Width,
                    value = PhysicalInstallationContractValue.LengthMillimeters(45),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.Width,
                    value = PhysicalInstallationContractValue.LengthMillimeters(50),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.Height,
                    value = PhysicalInstallationContractValue.LengthMillimeters(90),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.Depth,
                    value = PhysicalInstallationContractValue.LengthMillimeters(70),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.MountingType,
                    value = PhysicalInstallationContractValue.MountingType(PhysicalMountingTypeId("din35")),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.AllowedOrientations,
                    value = PhysicalInstallationContractValue.Orientations(emptySet()),
                ),
            ),
        )

        val failure = assertIs<PhysicalInstallationContractResolution.Failure>(result)
        assertEquals(
            setOf(
                "physical.contract.ambiguous",
                "physical.contract.empty_set",
                "physical.contract.missing",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertTrue(failure.diagnostics.all { diagnostic -> diagnostic.subjectIdentity == subject })
    }

    @Test
    fun `rejects invalid unchecked trait dimensions and negative clearance before contract construction`() {
        val result = PhysicalInstallationContractResolver.resolve(
            subjectIdentity = subject,
            facts = completeTraitFacts(width = 0) + listOf(
                projectFact(
                    field = PhysicalInstallationContractField.ClearanceLeft,
                    value = PhysicalInstallationContractValue.LengthMillimeters(-1),
                ),
            ),
        )

        val failure = assertIs<PhysicalInstallationContractResolution.Failure>(result)
        assertEquals(
            setOf("physical.contract.dimension.non_positive", "physical.contract.clearance.negative"),
            failure.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertTrue(failure.diagnostics.any { diagnostic -> diagnostic.expected == "positive millimetres" })
        assertTrue(failure.diagnostics.any { diagnostic -> diagnostic.expected == "non-negative millimetres" })
    }

    @Test
    fun `adapts existing resolved physical traits as unchecked trait facts only`() {
        val trait = ResolvedPhysicalTraitDefinition(
            semanticSubjectId = subject,
            definition = PhysicalTraitDefinition(
                displayName = "Breaker trait",
                size = PhysicalSize(widthMillimeters = 45, heightMillimeters = 90, depthMillimeters = 70),
                mountingTypeId = PhysicalMountingTypeId("din35"),
            ),
        )

        val result = PhysicalInstallationContractResolver.resolve(
            subjectIdentity = subject,
            facts = PhysicalInstallationContractResolver.factsFromResolvedTrait(trait) + listOf(
                projectFact(
                    field = PhysicalInstallationContractField.AllowedOrientations,
                    value = PhysicalInstallationContractValue.Orientations(setOf(PhysicalInstallationOrientation.Deg0)),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.ClearanceTop,
                    value = PhysicalInstallationContractValue.LengthMillimeters(10),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.ClearanceRight,
                    value = PhysicalInstallationContractValue.LengthMillimeters(5),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.ClearanceBottom,
                    value = PhysicalInstallationContractValue.LengthMillimeters(10),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.ClearanceLeft,
                    value = PhysicalInstallationContractValue.LengthMillimeters(5),
                ),
                projectFact(
                    field = PhysicalInstallationContractField.CompatibleContainerKinds,
                    value = PhysicalInstallationContractValue.ContainerKinds(setOf(PhysicalContainerKindId("cabinet"))),
                ),
            ),
        )

        val contract = assertIs<PhysicalInstallationContractResolution.Success>(result).contract
        assertEquals(45, contract.size.width.value)
        assertEquals(PhysicalContractSourceKind.Trait, contract.provenance.width.source.kind)
        assertEquals("trait:component:QF35:size.width", contract.provenance.width.source.id)
    }

    private fun completeTraitFacts(width: Int = 45): List<PhysicalInstallationContractFact> = listOf(
        traitFact(
            field = PhysicalInstallationContractField.Width,
            value = PhysicalInstallationContractValue.LengthMillimeters(width),
        ),
        traitFact(
            field = PhysicalInstallationContractField.Height,
            value = PhysicalInstallationContractValue.LengthMillimeters(90),
        ),
        traitFact(
            field = PhysicalInstallationContractField.Depth,
            value = PhysicalInstallationContractValue.LengthMillimeters(70),
        ),
        traitFact(
            field = PhysicalInstallationContractField.MountingType,
            value = PhysicalInstallationContractValue.MountingType(PhysicalMountingTypeId("din35")),
        ),
        traitFact(
            field = PhysicalInstallationContractField.AllowedOrientations,
            value = PhysicalInstallationContractValue.Orientations(setOf(PhysicalInstallationOrientation.Deg0)),
        ),
        traitFact(
            field = PhysicalInstallationContractField.ClearanceTop,
            value = PhysicalInstallationContractValue.LengthMillimeters(10),
        ),
        traitFact(
            field = PhysicalInstallationContractField.ClearanceRight,
            value = PhysicalInstallationContractValue.LengthMillimeters(5),
        ),
        traitFact(
            field = PhysicalInstallationContractField.ClearanceBottom,
            value = PhysicalInstallationContractValue.LengthMillimeters(10),
        ),
        traitFact(
            field = PhysicalInstallationContractField.ClearanceLeft,
            value = PhysicalInstallationContractValue.LengthMillimeters(5),
        ),
        traitFact(
            field = PhysicalInstallationContractField.CompatibleContainerKinds,
            value = PhysicalInstallationContractValue.ContainerKinds(setOf(PhysicalContainerKindId("cabinet"))),
        ),
    )

    private fun traitFact(
        field: PhysicalInstallationContractField,
        value: PhysicalInstallationContractValue,
    ): PhysicalInstallationContractFact = PhysicalInstallationContractFact(
        field = field,
        value = value,
        source = PhysicalContractSource(PhysicalContractSourceKind.Trait, "trait:$field"),
    )

    private fun projectFact(
        field: PhysicalInstallationContractField,
        value: PhysicalInstallationContractValue,
    ): PhysicalInstallationContractFact = PhysicalInstallationContractFact(
        field = field,
        value = value,
        source = PhysicalContractSource(PhysicalContractSourceKind.Project, "project:$field"),
        span = PhysicalSourceSpan(file = "src/main.athena", line = 12, column = 5),
    )
}
