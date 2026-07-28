package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PhysicalInstallationTopologyCompilerTest {
    @Test
    fun `compiles typed physical installation ir without visual or route geometry`() {
        val result = PhysicalInstallationTopologyCompiler.compile(
            intent = validIntent(),
            contracts = listOf(contract("component:QF35"), contract("component:XT35")),
        )

        val ir = assertIs<PhysicalInstallationTopologyCompilation.Success>(result).ir

        assertEquals(PhysicalInstallationId("MainCabinet"), ir.installationId)
        assertEquals(PhysicalObjectId("ENC1"), ir.space.enclosure.id)
        assertEquals(listOf(PhysicalObjectId("Backplate")), ir.space.surfaces.map { it.id })
        assertEquals(listOf(PhysicalObjectId("DIN1"), PhysicalObjectId("DIN2")), ir.space.rails.map { it.id })
        assertEquals(listOf(PhysicalObjectId("D1")), ir.space.ducts.map { it.id })
        assertEquals(listOf(PhysicalObjectId("CH1")), ir.space.channels.map { it.id })
        assertEquals(listOf(PhysicalObjectId("XT1")), ir.space.terminalGroups.map { it.id })
        assertEquals(2, ir.space.mountedOccurrences.size)
        assertEquals(listOf(PhysicalObjectId("CH1")), ir.routes.single().channelIds)
        assertEquals(
            listOf("component:QF35", "component:XT35"),
            ir.space.mountedOccurrences.map { occurrence -> occurrence.contract.subjectIdentity.value },
        )
    }

    @Test
    fun `fails closed on duplicate ids orphans illegal mount targets and missing contracts`() {
        val result = PhysicalInstallationTopologyCompiler.compile(
            intent = validIntent().copy(
                enclosures = listOf(enclosure("ENC1"), enclosure("ENC2")),
                surfaces = listOf(surface("Backplate", "MissingEnclosure")),
                rails = listOf(rail("DIN1", "MissingSurface"), rail("DIN1", "MissingSurface")),
                channels = listOf(channel("CH1", "MissingDuct")),
                mounts = listOf(
                    mount("QF35Mount", "component:QF35", "D1", 100, 0),
                    mount("QF35DuplicateMount", "component:QF35", "DIN1", 130, 0),
                    mount("MissingContractMount", "component:K35", "DIN1", 160, 0),
                ),
                routes = listOf(route("MainSupply", listOf("CH2"))),
            ),
            contracts = listOf(contract("component:QF35")),
        )

        val failure = assertIs<PhysicalInstallationTopologyCompilation.Failure>(result)

        assertEquals(
            setOf(
                "physical.topology.enclosure.count",
                "physical.topology.duplicate_id",
                "physical.topology.duplicate_occurrence_subject",
                "physical.topology.orphan",
                "physical.topology.illegal_mount_target",
                "physical.topology.contract.missing",
                "physical.topology.route.channel_missing",
            ),
            failure.diagnostics.map { it.code }.toSet(),
        )
    }

    @Test
    fun `orders terminal group occurrences by along axis cross axis then key`() {
        val result = PhysicalInstallationTopologyCompiler.compile(
            intent = validIntent().copy(
                mounts = listOf(
                    mount("BMount", "component:B", "XT1", x = 40, y = 20),
                    mount("AMount", "component:A", "XT1", x = 10, y = 20),
                    mount("CMount", "component:C", "XT1", x = 10, y = 30),
                ),
            ),
            contracts = listOf(contract("component:A"), contract("component:B"), contract("component:C")),
        )

        val terminalGroup = assertIs<PhysicalInstallationTopologyCompilation.Success>(result)
            .ir
            .space
            .terminalGroups
            .single()

        assertEquals(
            listOf("component:A", "component:C", "component:B"),
            terminalGroup.orderedOccurrenceKeys.map { key -> key.canonicalSemanticSubjectId.value },
        )
    }

    @Test
    fun `derives horizontal and vertical rail frames with positive determinant`() {
        val rails = assertIs<PhysicalInstallationTopologyCompilation.Success>(
            PhysicalInstallationTopologyCompiler.compile(
                intent = validIntent(),
                contracts = listOf(contract("component:QF35"), contract("component:XT35")),
            ),
        ).ir.space.rails.associateBy { rail -> rail.id.value }

        assertEquals(1, rails.getValue("DIN1").frame.determinant)
        assertEquals(1, rails.getValue("DIN2").frame.determinant)
        assertEquals(PhysicalVector2i(1, 0), rails.getValue("DIN1").frame.alongAxis)
        assertEquals(PhysicalVector2i(0, 1), rails.getValue("DIN2").frame.alongAxis)
        assertEquals(PhysicalVector2i(0, 1), rails.getValue("DIN1").frame.normalAxis)
        assertEquals(PhysicalVector2i(-1, 0), rails.getValue("DIN2").frame.normalAxis)
    }

    private fun validIntent(): PhysicalInstallationIntentV0 = PhysicalInstallationIntentV0(
        sourceUnitId = PhysicalSourceUnitId("src/main.athena"),
        installationId = PhysicalInstallationId("MainCabinet"),
        enclosures = listOf(enclosure("ENC1")),
        surfaces = listOf(surface("Backplate", "ENC1")),
        rails = listOf(
            rail("DIN1", "Backplate", orientation = PhysicalInfrastructureOrientation.Horizontal),
            rail("DIN2", "Backplate", y = 220, orientation = PhysicalInfrastructureOrientation.Vertical),
        ),
        ducts = listOf(duct("D1", "ENC1")),
        channels = listOf(channel("CH1", "D1")),
        terminalGroups = listOf(terminalGroup("XT1", "ENC1")),
        mounts = listOf(
            mount("QF35Mount", "component:QF35", "DIN1", 100, 0),
            mount("XT35Mount", "component:XT35", "XT1", 10, 5),
        ),
        routes = listOf(route("MainSupply", listOf("CH1"))),
    )

    private fun enclosure(id: String): PhysicalEnclosureIntent = PhysicalEnclosureIntent(
        id = PhysicalObjectId(id),
        size = PhysicalInstallationSize3i(800, 600, 250),
        provenance = provenance(id),
    )

    private fun surface(id: String, enclosureId: String): PhysicalMountingSurfaceIntent = PhysicalMountingSurfaceIntent(
        id = PhysicalObjectId(id),
        enclosureId = PhysicalObjectId(enclosureId),
        at = PhysicalPoint2i(20, 20),
        size = PhysicalSize2i(760, 560),
        acceptedMountingTypes = setOf(PhysicalMountingTypeId("din35"), PhysicalMountingTypeId("screw")),
        provenance = provenance(id),
    )

    private fun rail(
        id: String,
        surfaceId: String,
        y: Int = 120,
        orientation: PhysicalInfrastructureOrientation = PhysicalInfrastructureOrientation.Horizontal,
    ): PhysicalRailIntent = PhysicalRailIntent(
        id = PhysicalObjectId(id),
        surfaceId = PhysicalObjectId(surfaceId),
        at = PhysicalPoint2i(60, y),
        length = PhysicalPositiveMillimeters.from(680)!!,
        orientation = orientation,
        mountingType = PhysicalMountingTypeId("din35"),
        provenance = provenance(id),
    )

    private fun duct(id: String, enclosureId: String): PhysicalDuctIntent = PhysicalDuctIntent(
        id = PhysicalObjectId(id),
        enclosureId = PhysicalObjectId(enclosureId),
        at = PhysicalPoint2i(30, 60),
        size = PhysicalSize2i(40, 480),
        orientation = PhysicalInfrastructureOrientation.Vertical,
        wall = PhysicalNonNegativeMillimeters.from(2)!!,
        provenance = provenance(id),
    )

    private fun channel(id: String, ductId: String): PhysicalRouteChannelIntent = PhysicalRouteChannelIntent(
        id = PhysicalObjectId(id),
        ductId = PhysicalObjectId(ductId),
        at = PhysicalPoint2i(0, 0),
        size = PhysicalSize2i(36, 476),
        lanes = 4,
        margin = PhysicalNonNegativeMillimeters.from(4)!!,
        provenance = provenance(id),
    )

    private fun terminalGroup(id: String, enclosureId: String): PhysicalTerminalGroupIntent =
        PhysicalTerminalGroupIntent(
            id = PhysicalObjectId(id),
            enclosureId = PhysicalObjectId(enclosureId),
            at = PhysicalPoint2i(520, 420),
            size = PhysicalSize2i(180, 50),
            orientation = PhysicalInfrastructureOrientation.Horizontal,
            acceptedMountingTypes = setOf(PhysicalMountingTypeId("terminal-snap")),
            provenance = provenance(id),
        )

    private fun mount(
        id: String,
        subject: String,
        target: String,
        x: Int,
        y: Int,
    ): PhysicalMountedOccurrenceIntent = PhysicalMountedOccurrenceIntent(
        occurrenceId = PhysicalObjectId(id),
        semanticSubjectId = StableSemanticIdentity(subject),
        targetId = PhysicalObjectId(target),
        at = PhysicalPoint2i(x, y),
        selectedOrientation = PhysicalInstallationOrientation.Deg0,
        provenance = provenance(id),
    )

    private fun route(alias: String, channelIds: List<String>): PhysicalRouteIntentSource =
        PhysicalRouteIntentSource(
            connectionAlias = alias,
            channelIds = channelIds.map(::PhysicalObjectId),
            provenance = provenance(alias),
        )

    private fun contract(subject: String): PhysicalInstallationContractV0 {
        val source = PhysicalContractSource(PhysicalContractSourceKind.Project, "project:$subject")
        val provenance = PhysicalInstallationContractField.entries.associateWith { field ->
            PhysicalInstallationContractFieldProvenance(field, source, null)
        }
        return PhysicalInstallationContractV0(
            subjectIdentity = StableSemanticIdentity(subject),
            size = PhysicalInstallationSizeV0(
                width = PhysicalPositiveMillimeters.from(20)!!,
                height = PhysicalPositiveMillimeters.from(40)!!,
                depth = PhysicalPositiveMillimeters.from(35)!!,
            ),
            mountingTypeId = PhysicalMountingTypeId("terminal-snap"),
            allowedOrientations = setOf(PhysicalInstallationOrientation.Deg0),
            clearance = PhysicalInstallationClearanceV0(
                top = PhysicalNonNegativeMillimeters.from(2)!!,
                right = PhysicalNonNegativeMillimeters.from(2)!!,
                bottom = PhysicalNonNegativeMillimeters.from(2)!!,
                left = PhysicalNonNegativeMillimeters.from(2)!!,
            ),
            compatibleContainerKinds = setOf(PhysicalContainerKindId("cabinet")),
            provenance = PhysicalInstallationContractProvenanceV0(
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

    private fun provenance(id: String): PhysicalSourceProvenance = PhysicalSourceProvenance(
        sourceUnitId = PhysicalSourceUnitId("src/main.athena"),
        declarationId = id,
        span = PhysicalSourceSpan("src/main.athena", line = 1, column = 1),
    )
}
