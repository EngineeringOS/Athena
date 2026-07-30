package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PhysicalConstraintEvaluatorTest {
    @Test
    fun `accepts valid cabinet fit and edge contact without solving`() {
        val ir = validIr(
            occurrences = listOf(
                occurrence("A", "component:A", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 100, y = 100),
                occurrence("B", "component:B", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 120, y = 100),
                occurrence("RailA", "component:RailA", PhysicalMountTargetRef.Rail(PhysicalObjectId("DIN1")), x = 10, y = 0),
                occurrence("TerminalA", "component:TerminalA", PhysicalMountTargetRef.TerminalGroup(PhysicalObjectId("XT1")), x = 10, y = 5),
            ),
            contractOverrides = mapOf(
                "component:TerminalA" to contract("component:TerminalA", mounting = "terminal-snap"),
            ),
        )

        val result = PhysicalConstraintEvaluator.evaluate(ir)

        val evidence = assertIs<PhysicalConstraintEvaluation.Success>(result).evidence
        assertEquals(4, evidence.occurrenceCount)
        assertEquals(0, evidence.diagnosticCount)
        assertEquals(PhysicalConstraintEvaluationMode.ValidationOnly, evidence.mode)
    }

    @Test
    fun `interprets rail mount coordinates as along and normal offsets`() {
        val verticalRail = rail(
            id = "DIN_VERTICAL",
            x = 700,
            y = 20,
            length = 500,
            orientation = PhysicalInfrastructureOrientation.Vertical,
        )
        val ir = validIr(
            rails = listOf(verticalRail),
            occurrences = listOf(
                occurrence(
                    occurrenceId = "VerticalRailMount",
                    subject = "component:VerticalRailMount",
                    target = PhysicalMountTargetRef.Rail(verticalRail.id),
                    x = 100,
                    y = 0,
                ),
            ),
        )

        val result = PhysicalConstraintEvaluator.evaluate(ir)

        assertIs<PhysicalConstraintEvaluation.Success>(result)
    }

    @Test
    fun `rejects mounted occurrences that occupy route ducts`() {
        val ir = validIr(
            ducts = listOf(duct("D1", x = 100, y = 100, width = 40, height = 120)),
            channels = listOf(channel("CH1", x = 0, y = 0, width = 36, height = 116)),
            occurrences = listOf(
                occurrence(
                    occurrenceId = "DuctOverlap",
                    subject = "component:DuctOverlap",
                    target = PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")),
                    x = 80,
                    y = 80,
                ),
            ),
        )

        val failure = assertIs<PhysicalConstraintEvaluation.Failure>(PhysicalConstraintEvaluator.evaluate(ir))

        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.code == "physical.constraint.occurrence.infrastructure_collision" &&
                diagnostic.subject == "DuctOverlap,D1"
        })
    }

    @Test
    fun `rejects containment orientation depth mounting container collision clearance and rail failures`() {
        val ir = validIr(
            surfaces = listOf(surface("Backplate", x = 100, y = 100, width = 900, height = 560)),
            ducts = listOf(duct("D1", x = 770, y = 20, width = 60, height = 120)),
            channels = listOf(channel("CH1", ductId = "D1", x = 0, y = 0, width = 58, height = 118)),
            rails = listOf(
                rail("DIN1", x = 60, y = 120, length = 80),
                rail("DIN_BAD", x = 850, y = 120, length = 80),
            ),
            occurrences = listOf(
                occurrence("BadOrientation", "component:BadOrientation", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 20, y = 20, orientation = PhysicalInstallationOrientation.Deg90),
                occurrence("BadDepth", "component:BadDepth", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 90, y = 20),
                occurrence("BadMounting", "component:BadMounting", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 160, y = 20),
                occurrence("BadContainer", "component:BadContainer", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 230, y = 20),
                occurrence("CollisionA", "component:CollisionA", PhysicalMountTargetRef.TerminalGroup(PhysicalObjectId("XT1")), x = 10, y = 5),
                occurrence("CollisionB", "component:CollisionB", PhysicalMountTargetRef.TerminalGroup(PhysicalObjectId("XT1")), x = 20, y = 5),
                occurrence("ClearanceA", "component:ClearanceA", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 300, y = 20),
                occurrence("ClearanceB", "component:ClearanceB", PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")), x = 325, y = 20),
                occurrence("RailNormal", "component:RailNormal", PhysicalMountTargetRef.Rail(PhysicalObjectId("DIN1")), x = 10, y = 2),
                occurrence("RailAlong", "component:RailAlong", PhysicalMountTargetRef.Rail(PhysicalObjectId("DIN1")), x = 70, y = 0),
            ),
            contractOverrides = mapOf(
                "component:BadOrientation" to contract("component:BadOrientation", allowed = setOf(PhysicalInstallationOrientation.Deg0)),
                "component:BadDepth" to contract("component:BadDepth", depth = 300),
                "component:BadMounting" to contract("component:BadMounting", mounting = "terminal-snap"),
                "component:BadContainer" to contract("component:BadContainer", containers = setOf("panel")),
                "component:ClearanceA" to contract("component:ClearanceA", clearance = PhysicalInstallationClearance(mm(2), mm(20), mm(2), mm(2))),
            ),
        )

        val failure = assertIs<PhysicalConstraintEvaluation.Failure>(PhysicalConstraintEvaluator.evaluate(ir))

        assertEquals(
            setOf(
                "physical.constraint.surface.outside_enclosure",
                "physical.constraint.duct.outside_enclosure",
                "physical.constraint.channel.outside_duct_interior",
                "physical.constraint.rail.outside_surface",
                "physical.constraint.orientation.not_allowed",
                "physical.constraint.depth.exceeds_enclosure",
                "physical.constraint.mounting.incompatible",
                "physical.constraint.container.incompatible",
                "physical.constraint.occurrence.collision",
                "physical.constraint.clearance.collision",
                "physical.constraint.rail.normal_offset",
                "physical.constraint.rail.along_outside",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertTrue(failure.diagnostics.all { diagnostic -> diagnostic.subject.isNotBlank() })
        assertTrue(failure.diagnostics.all { diagnostic -> diagnostic.measured != null })
        assertTrue(failure.diagnostics.all { diagnostic -> diagnostic.expected.isNotBlank() })
        assertTrue(failure.diagnostics.any { diagnostic -> diagnostic.span != null })
    }

    private fun validIr(
        surfaces: List<PhysicalMountingSurface> = listOf(surface("Backplate")),
        rails: List<PhysicalRail> = listOf(rail("DIN1")),
        ducts: List<PhysicalDuct> = listOf(duct("D1")),
        channels: List<PhysicalRouteChannel> = listOf(channel("CH1")),
        terminalGroups: List<PhysicalTerminalGroup> = listOf(terminalGroup("XT1")),
        occurrences: List<PhysicalMountedOccurrence>,
        contractOverrides: Map<String, PhysicalInstallationContract> = emptyMap(),
    ): PhysicalInstallationIR {
        val contracts = occurrences.associate { occurrence ->
            occurrence.semanticSubjectId.value to contractOverrides.getOrDefault(
                occurrence.semanticSubjectId.value,
                contract(occurrence.semanticSubjectId.value),
            )
        }
        return PhysicalInstallationIR(
            sourceUnitId = sourceUnit,
            installationId = installationId,
            space = PhysicalInstallationSpace(
                enclosure = PhysicalEnclosure(
                    id = PhysicalObjectId("ENC1"),
                    size = PhysicalInstallationSize3i(800, 600, 250),
                    provenance = provenance("ENC1"),
                ),
                surfaces = surfaces,
                rails = rails,
                ducts = ducts,
                channels = channels,
                terminalGroups = terminalGroups,
                mountedOccurrences = occurrences.map { occurrence ->
                    occurrence.copy(contract = contracts.getValue(occurrence.semanticSubjectId.value))
                },
            ),
            routes = emptyList(),
        )
    }

    private fun surface(
        id: String,
        x: Int = 20,
        y: Int = 20,
        width: Int = 760,
        height: Int = 560,
    ): PhysicalMountingSurface = PhysicalMountingSurface(
        id = PhysicalObjectId(id),
        enclosureId = PhysicalObjectId("ENC1"),
        at = PhysicalPoint2i(x, y),
        size = PhysicalSize2i(width, height),
        acceptedMountingTypes = setOf(PhysicalMountingTypeId("din35"), PhysicalMountingTypeId("screw")),
        provenance = provenance(id),
    )

    private fun rail(
        id: String,
        x: Int = 60,
        y: Int = 120,
        length: Int = 680,
        orientation: PhysicalInfrastructureOrientation = PhysicalInfrastructureOrientation.Horizontal,
    ): PhysicalRail =
        PhysicalRail(
            id = PhysicalObjectId(id),
            surfaceId = PhysicalObjectId("Backplate"),
            at = PhysicalPoint2i(x, y),
            length = PhysicalPositiveMillimeters.from(length)!!,
            orientation = orientation,
            mountingType = PhysicalMountingTypeId("din35"),
            frame = when (orientation) {
                PhysicalInfrastructureOrientation.Horizontal -> PhysicalRigidFrame2i(
                    PhysicalPoint2i(x, y),
                    PhysicalVector2i(1, 0),
                    PhysicalVector2i(0, 1),
                )
                PhysicalInfrastructureOrientation.Vertical -> PhysicalRigidFrame2i(
                    PhysicalPoint2i(x, y),
                    PhysicalVector2i(0, 1),
                    PhysicalVector2i(-1, 0),
                )
            },
            provenance = provenance(id),
        )

    private fun duct(id: String, x: Int = 30, y: Int = 60, width: Int = 40, height: Int = 480): PhysicalDuct =
        PhysicalDuct(
            id = PhysicalObjectId(id),
            enclosureId = PhysicalObjectId("ENC1"),
            at = PhysicalPoint2i(x, y),
            size = PhysicalSize2i(width, height),
            orientation = PhysicalInfrastructureOrientation.Vertical,
            wall = PhysicalNonNegativeMillimeters.from(2)!!,
            provenance = provenance(id),
        )

    private fun channel(
        id: String,
        ductId: String = "D1",
        x: Int = 0,
        y: Int = 0,
        width: Int = 36,
        height: Int = 476,
    ): PhysicalRouteChannel = PhysicalRouteChannel(
        id = PhysicalObjectId(id),
        ductId = PhysicalObjectId(ductId),
        at = PhysicalPoint2i(x, y),
        size = PhysicalSize2i(width, height),
        lanes = 4,
        margin = PhysicalNonNegativeMillimeters.from(4)!!,
        provenance = provenance(id),
    )

    private fun terminalGroup(id: String): PhysicalTerminalGroup = PhysicalTerminalGroup(
        id = PhysicalObjectId(id),
        enclosureId = PhysicalObjectId("ENC1"),
        at = PhysicalPoint2i(520, 420),
        size = PhysicalSize2i(180, 50),
        orientation = PhysicalInfrastructureOrientation.Horizontal,
        acceptedMountingTypes = setOf(PhysicalMountingTypeId("terminal-snap")),
        orderedOccurrenceKeys = emptyList(),
        provenance = provenance(id),
    )

    private fun occurrence(
        occurrenceId: String,
        subject: String,
        target: PhysicalMountTargetRef,
        x: Int,
        y: Int,
        orientation: PhysicalInstallationOrientation = PhysicalInstallationOrientation.Deg0,
    ): PhysicalMountedOccurrence = PhysicalMountedOccurrence(
        occurrenceId = PhysicalObjectId(occurrenceId),
        key = InstallationOccurrenceKey(sourceUnit, installationId, StableSemanticIdentity(subject)),
        semanticSubjectId = StableSemanticIdentity(subject),
        target = target,
        at = PhysicalPoint2i(x, y),
        selectedOrientation = orientation,
        contract = contract(subject),
        provenance = provenance(occurrenceId),
    )

    private fun contract(
        subject: String,
        width: Int = 20,
        height: Int = 40,
        depth: Int = 35,
        mounting: String = "din35",
        allowed: Set<PhysicalInstallationOrientation> = setOf(PhysicalInstallationOrientation.Deg0),
        clearance: PhysicalInstallationClearance = PhysicalInstallationClearance(mm(0), mm(0), mm(0), mm(0)),
        containers: Set<String> = setOf("cabinet"),
    ): PhysicalInstallationContract {
        val source = PhysicalContractSource(PhysicalContractSourceKind.Project, "project:$subject")
        val provenance = PhysicalInstallationContractField.entries.associateWith { field ->
            PhysicalInstallationContractFieldProvenance(field, source, provenance(subject).span)
        }
        return PhysicalInstallationContract(
            subjectIdentity = StableSemanticIdentity(subject),
            size = PhysicalInstallationSize(
                width = PhysicalPositiveMillimeters.from(width)!!,
                height = PhysicalPositiveMillimeters.from(height)!!,
                depth = PhysicalPositiveMillimeters.from(depth)!!,
            ),
            mountingTypeId = PhysicalMountingTypeId(mounting),
            allowedOrientations = allowed,
            clearance = clearance,
            compatibleContainerKinds = containers.map(::PhysicalContainerKindId).toSet(),
            provenance = PhysicalInstallationContractProvenance(
                width = provenance.getValue(PhysicalInstallationContractField.Width),
                height = provenance.getValue(PhysicalInstallationContractField.Height),
                depth = provenance.getValue(PhysicalInstallationContractField.Depth),
                mountingType = provenance.getValue(PhysicalInstallationContractField.MountingType),
                allowedOrientations = provenance.getValue(PhysicalInstallationContractField.AllowedOrientations),
                clearanceTop = provenance.getValue(PhysicalInstallationContractField.ClearanceTop),
                clearanceRight = provenance.getValue(PhysicalInstallationContractField.ClearanceRight),
                clearanceBottom = provenance.getValue(PhysicalInstallationContractField.ClearanceBottom),
                clearanceLeft = provenance.getValue(PhysicalInstallationContractField.ClearanceLeft),
                compatibleContainerKinds = provenance.getValue(
                    PhysicalInstallationContractField.CompatibleContainerKinds,
                ),
            ),
        )
    }

    private fun mm(value: Int): PhysicalNonNegativeMillimeters = PhysicalNonNegativeMillimeters.from(value)!!

    private fun provenance(id: String): PhysicalSourceProvenance = PhysicalSourceProvenance(
        sourceUnitId = sourceUnit,
        declarationId = id,
        span = PhysicalSourceSpan("src/main.athena", line = 1, column = 1),
    )

    private companion object {
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
        val installationId = PhysicalInstallationId("MainCabinet")
    }
}
