package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutConstraintOwner
import com.engineeringood.athena.layout.LayoutConstraintStrength
import com.engineeringood.athena.layout.LayoutGraphBounds
import com.engineeringood.athena.layout.LayoutGraphConstraintKind
import com.engineeringood.athena.layout.LayoutGraphRelationshipKind
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.EngineeringPackageArtifactId
import com.engineeringood.athena.packageplatform.EngineeringPackageCoordinates
import com.engineeringood.athena.packageplatform.EngineeringPackageDescriptor
import com.engineeringood.athena.packageplatform.EngineeringPackageGroupId
import com.engineeringood.athena.packageplatform.EngineeringPackageId
import com.engineeringood.athena.packageplatform.EngineeringPackageKind
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycle
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycleState
import com.engineeringood.athena.packageplatform.EngineeringPackageProvenance
import com.engineeringood.athena.packageplatform.EngineeringPackageVersion
import com.engineeringood.athena.packageplatform.GraphicResourceId
import com.engineeringood.athena.packageplatform.GraphicResourceKind
import com.engineeringood.athena.packageplatform.GraphicResourceRef
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.PresentationProfileFallbackMode
import com.engineeringood.athena.packageplatform.PresentationProfileFallbackPolicy
import com.engineeringood.athena.packageplatform.PresentationProfileId
import com.engineeringood.athena.packageplatform.PresentationProfileProvenance
import com.engineeringood.athena.packageplatform.PresentationProfileVersion
import com.engineeringood.athena.packageplatform.PresentationStyleProfileId
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.packageplatform.RepresentationAnchorId
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationDescriptorBounds
import com.engineeringood.athena.packageplatform.RepresentationDescriptorId
import com.engineeringood.athena.packageplatform.RepresentationDescriptorResourceBinding
import com.engineeringood.athena.packageplatform.RepresentationHotspotDefinition
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotDefinition
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotId
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotPlacement
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotRole
import com.engineeringood.athena.packageplatform.RepresentationPackageArtifactId
import com.engineeringood.athena.packageplatform.RepresentationPackageCoordinates
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageId
import com.engineeringood.athena.packageplatform.RepresentationPackageLifecycle
import com.engineeringood.athena.packageplatform.RepresentationPackageLifecycleState
import com.engineeringood.athena.packageplatform.RepresentationPackageProvenance
import com.engineeringood.athena.packageplatform.RepresentationPackageVersion
import com.engineeringood.athena.packageplatform.RepresentationSupportedProfile
import com.engineeringood.athena.packageplatform.RepresentationVariantDefinition
import com.engineeringood.athena.packageplatform.RepresentationVariantId
import com.engineeringood.athena.packageplatform.RepresentationStyleTokenRef
import com.engineeringood.athena.packageplatform.RepresentationTransformDefinition
import com.engineeringood.athena.packageplatform.RepresentationTransformKind
import com.engineeringood.athena.packageplatform.PresentationProfileTag
import com.engineeringood.athena.packageplatform.RepresentationStandardTag
import com.engineeringood.athena.packageruntime.BindingResolution
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalContainerKindId
import com.engineeringood.athena.physical.PhysicalContractSource
import com.engineeringood.athena.physical.PhysicalContractSourceKind
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalEnclosure
import com.engineeringood.athena.physical.PhysicalInstallationClearance
import com.engineeringood.athena.physical.PhysicalInstallationContractField
import com.engineeringood.athena.physical.PhysicalInstallationContractFieldProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContractProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContract
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalInstallationSize3i
import com.engineeringood.athena.physical.PhysicalInstallationSpace
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalMountedOccurrence
import com.engineeringood.athena.physical.PhysicalMountTargetRef
import com.engineeringood.athena.physical.PhysicalMountingSurface
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalNonNegativeMillimeters
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalPositiveMillimeters
import com.engineeringood.athena.physical.PhysicalRail
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalRouteIntent
import com.engineeringood.athena.physical.PhysicalSize2i
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.PhysicalTerminalGroup
import com.engineeringood.athena.physical.PhysicalVector2i
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationVersion
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationVariantId as SymbolRepresentationVariantId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AthenaLayoutGraphLowererTest {
    @Test
    fun `lowers a transient layout graph with explicit owners strength provenance and route facts`() {
        val fixture = fixture()

        val first = AthenaLayoutGraphLowerer.lower(fixture.physical, listOf(fixture.material))
        val second = AthenaLayoutGraphLowerer.lower(fixture.physical, listOf(fixture.material))

        assertEquals(first, second)
        assertTrue(first.diagnostics.isEmpty())

        val graph = assertNotNull(first.graph)
        assertTrue(graph.snapshotId.value.startsWith("layout:src/main.athena:MainCabinet:"))
        assertTrue(graph.compilerSnapshotId.startsWith("athena-cabinet-compiler:"))
        assertEquals("src/main.athena", graph.sourceUnitId.value)
        assertEquals("MainCabinet", graph.installationId)
        assertEquals(1, graph.occurrences.size)
        assertEquals(6, graph.obstacles.size)
        assertEquals(3, graph.relationships.size)
        assertEquals(6, graph.constraints.size)

        val occurrence = graph.occurrences.single()
        assertEquals(LayoutGraphBounds(120, 140, 60, 40), occurrence.bounds)
        assertEquals(LayoutGraphBounds(0, 0, 120, 80), occurrence.representationBounds)
        assertEquals(1, occurrence.ports.size)
        assertEquals(1, occurrence.anchors.size)
        assertEquals(5, occurrence.constraints.size)
        assertTrue(occurrence.provenance.sourceUnitId.value.isNotBlank())
        assertTrue(occurrence.ports.single().required)
        assertEquals("anchor-l1", occurrence.ports.single().anchorId)
        assertEquals("terminal-l1", occurrence.anchors.single().geometryRef)
        assertEquals(LayoutGraphConstraintKind.PORT_ANCHOR_BINDING, occurrence.constraints.single { it.kind == LayoutGraphConstraintKind.PORT_ANCHOR_BINDING }.kind)
        assertEquals(setOf(
            LayoutGraphConstraintKind.REPRESENTATION_BOUNDS,
            LayoutGraphConstraintKind.PHYSICAL_MOUNT,
            LayoutGraphConstraintKind.PHYSICAL_CLEARANCE,
            LayoutGraphConstraintKind.PHYSICAL_ORIENTATION,
            LayoutGraphConstraintKind.PORT_ANCHOR_BINDING,
            LayoutGraphConstraintKind.ROUTE_CHANNEL,
        ), graph.constraints.map { it.kind }.toSet())
        assertEquals(setOf(LayoutConstraintOwner.SEMANTIC, LayoutConstraintOwner.REPRESENTATION, LayoutConstraintOwner.PHYSICAL, LayoutConstraintOwner.LAYOUT_PREFERENCE), graph.constraints.map { it.owner }.toSet())
        assertEquals(setOf(LayoutConstraintStrength.REQUIRED, LayoutConstraintStrength.PREFERRED), graph.constraints.map { it.strength }.toSet())
        assertEquals(
            setOf(LayoutGraphRelationshipKind.CONTAINMENT, LayoutGraphRelationshipKind.MOUNT, LayoutGraphRelationshipKind.ROUTE_CHANNEL),
            graph.relationships.map { it.kind }.toSet(),
        )
        assertTrue(graph.obstacles.any { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.ENCLOSURE })
        assertTrue(graph.obstacles.any { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.SURFACE })
        assertTrue(graph.obstacles.any { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.RAIL })
        assertTrue(graph.obstacles.any { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.DUCT })
        assertTrue(graph.obstacles.any { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.CHANNEL })
        assertTrue(graph.obstacles.any { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.TERMINAL_GROUP })
        assertEquals(
            LayoutGraphBounds(60, 100, 320, 1),
            graph.obstacles.single { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.RAIL }.bounds,
        )
        assertEquals(
            LayoutGraphBounds(26, 266, 110, 80),
            graph.obstacles.single { it.kind == com.engineeringood.athena.layout.LayoutGraphObstacleKind.CHANNEL }.bounds,
        )
    }

    @Test
    fun `rejects missing representation bounds before lowerer emits a graph`() {
        val fixture = fixture(materialBounds = null)

        val result = AthenaLayoutGraphLowerer.lower(fixture.physical, listOf(fixture.material))

        assertNull(result.graph)
        assertTrue(result.diagnostics.any { it.code == "layout.graph.bounds.missing" })
    }

    @Test
    fun `lowers rail mounted occurrences to absolute cabinet bounds`() {
        val fixture = fixture(occurrenceX = 0, occurrenceY = 0)
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
        val installationId = PhysicalInstallationId("MainCabinet")
        val secondSubject = StableSemanticIdentity("component:relay")
        val secondRail = PhysicalRail(
            id = PhysicalObjectId("DIN2"),
            surfaceId = PhysicalObjectId("Backplate"),
            at = PhysicalPoint2i(40, 180),
            length = PhysicalPositiveMillimeters.from(320)!!,
            orientation = PhysicalInfrastructureOrientation.Horizontal,
            mountingType = PhysicalMountingTypeId("din35"),
            frame = PhysicalVector2i(1, 0).toFrame(PhysicalPoint2i(40, 180)),
            provenance = provenance(sourceUnit, "DIN2"),
        )
        val firstRailOccurrence = fixture.physical.space.mountedOccurrences.single().copy(
            target = PhysicalMountTargetRef.Rail(PhysicalObjectId("DIN1")),
            at = PhysicalPoint2i(0, 0),
        )
        val secondRailOccurrence = firstRailOccurrence.copy(
            occurrenceId = PhysicalObjectId("M2"),
            key = InstallationOccurrenceKey(sourceUnit, installationId, secondSubject),
            semanticSubjectId = secondSubject,
            target = PhysicalMountTargetRef.Rail(PhysicalObjectId("DIN2")),
            contract = contract(secondSubject.value),
            provenance = provenance(sourceUnit, "M2"),
        )
        val physical = fixture.physical.copy(
            space = fixture.physical.space.copy(
                rails = fixture.physical.space.rails + secondRail,
                mountedOccurrences = listOf(firstRailOccurrence, secondRailOccurrence),
            ),
        )

        val result = AthenaLayoutGraphLowerer.lower(
            physical,
            listOf(fixture.material, material(secondSubject.value, GraphicBounds(0.0, 0.0, 60.0, 40.0))),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            setOf(
                LayoutGraphBounds(60, 100, 60, 40),
                LayoutGraphBounds(60, 200, 60, 40),
            ),
            assertNotNull(result.graph).occurrences.map { it.bounds }.toSet(),
        )
    }

    @Test
    fun `rejects invalid containment before downstream composition consumes layout graph`() {
        val fixture = fixture(occurrenceX = 480, occurrenceY = 380)

        val result = AthenaLayoutGraphLowerer.lower(fixture.physical, listOf(fixture.material))

        assertNull(result.graph)
        assertTrue(result.diagnostics.any { it.code == "layout.graph.containment.invalid" })
    }

    @Test
    fun `rejects unplaced occurrence evidence before lowerer emits a graph`() {
        val fixture = fixture()

        val result = AthenaLayoutGraphLowerer.lower(fixture.physical, emptyList())

        assertNull(result.graph)
        assertTrue(result.diagnostics.any { it.code == "layout.graph.material.missing" })
    }

    private fun fixture(
        occurrenceX: Int = 100,
        occurrenceY: Int = 120,
        materialBounds: GraphicBounds? = GraphicBounds(0.0, 0.0, 120.0, 80.0),
    ): Fixture {
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
        val installationId = PhysicalInstallationId("MainCabinet")
        val subject = StableSemanticIdentity("component:drive")
        val material = material(subject.value, materialBounds)
        return Fixture(
            physical = PhysicalInstallationIR(
                sourceUnitId = sourceUnit,
                installationId = installationId,
                space = PhysicalInstallationSpace(
                    enclosure = PhysicalEnclosure(
                        id = PhysicalObjectId("ENC1"),
                        size = PhysicalInstallationSize3i(500, 400, 250),
                        provenance = provenance(sourceUnit, "ENC1"),
                    ),
                    surfaces = listOf(
                        PhysicalMountingSurface(
                            id = PhysicalObjectId("Backplate"),
                            enclosureId = PhysicalObjectId("ENC1"),
                            at = PhysicalPoint2i(20, 20),
                            size = PhysicalSize2i(460, 360),
                            acceptedMountingTypes = setOf(PhysicalMountingTypeId("din35")),
                            provenance = provenance(sourceUnit, "Backplate"),
                        ),
                    ),
                    rails = listOf(
                        PhysicalRail(
                            id = PhysicalObjectId("DIN1"),
                            surfaceId = PhysicalObjectId("Backplate"),
                            at = PhysicalPoint2i(40, 80),
                            length = PhysicalPositiveMillimeters.from(320)!!,
                            orientation = PhysicalInfrastructureOrientation.Horizontal,
                            mountingType = PhysicalMountingTypeId("din35"),
                            frame = PhysicalVector2i(1, 0).toFrame(PhysicalPoint2i(40, 80)),
                            provenance = provenance(sourceUnit, "DIN1"),
                        ),
                    ),
                    ducts = listOf(
                        PhysicalDuct(
                            id = PhysicalObjectId("D1"),
                            enclosureId = PhysicalObjectId("ENC1"),
                            at = PhysicalPoint2i(20, 260),
                            size = PhysicalSize2i(120, 90),
                            orientation = PhysicalInfrastructureOrientation.Horizontal,
                            wall = PhysicalNonNegativeMillimeters.from(2)!!,
                            provenance = provenance(sourceUnit, "D1"),
                        ),
                    ),
                    channels = listOf(
                        PhysicalRouteChannel(
                            id = PhysicalObjectId("CH1"),
                            ductId = PhysicalObjectId("D1"),
                            at = PhysicalPoint2i(4, 4),
                            size = PhysicalSize2i(110, 80),
                            lanes = 2,
                            margin = PhysicalNonNegativeMillimeters.from(4)!!,
                            provenance = provenance(sourceUnit, "CH1"),
                        ),
                    ),
                    terminalGroups = listOf(
                        PhysicalTerminalGroup(
                            id = PhysicalObjectId("XT1"),
                            enclosureId = PhysicalObjectId("ENC1"),
                            at = PhysicalPoint2i(320, 300),
                            size = PhysicalSize2i(110, 45),
                            orientation = PhysicalInfrastructureOrientation.Horizontal,
                            acceptedMountingTypes = setOf(PhysicalMountingTypeId("terminal-snap")),
                            orderedOccurrenceKeys = emptyList(),
                            provenance = provenance(sourceUnit, "XT1"),
                        ),
                    ),
                    mountedOccurrences = listOf(
                        PhysicalMountedOccurrence(
                            occurrenceId = PhysicalObjectId("M1"),
                            key = InstallationOccurrenceKey(sourceUnit, installationId, subject),
                            semanticSubjectId = subject,
                            target = PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")),
                            at = PhysicalPoint2i(occurrenceX, occurrenceY),
                            selectedOrientation = PhysicalInstallationOrientation.Deg0,
                            contract = contract(subject.value),
                            provenance = provenance(sourceUnit, "M1"),
                        ),
                    ),
                ),
                routes = listOf(
                    PhysicalRouteIntent(
                        connectionAlias = "drive_power",
                        channelIds = listOf(PhysicalObjectId("CH1")),
                        provenance = provenance(sourceUnit, "route-1"),
                    ),
                ),
            ),
            material = material,
        )
    }

    private fun material(
        subject: String,
        bounds: GraphicBounds?,
    ): AthenaResolvedRepresentationMaterial {
        val anchorId = RepresentationAnchorId("anchor-l1")
        val definition = RepresentationDefinition(
            symbolId = RepresentationSymbolId("iec.drive.element"),
            libraryId = com.engineeringood.athena.representation.RepresentationLibraryId("library.cabinet"),
            version = RepresentationVersion("1.0.0"),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance("fixture"),
            ),
            kind = RepresentationSymbolKind.GENERIC,
            labelSlots = emptyList(),
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            graphicBody = GraphicPrimitiveDocument(
                documentId = GraphicPrimitiveDocumentId("fixture"),
                bounds = bounds,
                primitives = emptyList(),
                styleTokens = emptyList(),
                provenanceSources = listOf("fixture"),
            ),
            anchors = listOf(
                RepresentationAnchorContract(
                    anchorId = com.engineeringood.athena.representation.RepresentationAnchorId("anchor-l1"),
                    geometryRef = "terminal-l1",
                    primitiveId = GraphicPrimitiveId("body"),
                    point = GraphicPoint(10.0, 20.0),
                    role = RepresentationAnchorRole.TERMINAL,
                    required = true,
                ),
            ),
        )
        return AthenaResolvedRepresentationMaterial(
            semanticSubjectId = subject,
            physicalComponentId = "physical-drive",
            functionId = null,
            definition = definition,
            resolution = BindingResolution(
                semanticSubjectId = subject,
                engineeringPackageId = EngineeringPackageId("pkg.drive"),
                presentationProfileId = PresentationProfileId("cabinet"),
                representationPackageId = RepresentationPackageId("pkg.representation"),
                descriptorId = RepresentationDescriptorId("descriptor.drive"),
                variantId = RepresentationVariantId("default"),
                anchorMapping = mapOf(subjectPortId(subject) to anchorId),
                labelBinding = emptyMap(),
                styleProfile = PresentationStyleProfileId("default"),
            ),
            terminalBindings = mapOf(subjectPortId(subject) to "XT1.1"),
        )
    }

    private fun subjectPortId(subject: String): String = "$subject.port.L1"

    private fun contract(
        subject: String,
    ): PhysicalInstallationContract {
        val source = PhysicalContractSource(PhysicalContractSourceKind.Project, "project:$subject")
        val provenance = PhysicalInstallationContractField.entries.associateWith { field ->
            PhysicalInstallationContractFieldProvenance(
                field = field,
                source = source,
                span = PhysicalSourceSpan("src/main.athena", line = 1, column = 1),
            )
        }
        return PhysicalInstallationContract(
            subjectIdentity = StableSemanticIdentity(subject),
            size = com.engineeringood.athena.physical.PhysicalInstallationSize(
                width = PhysicalPositiveMillimeters.from(60)!!,
                height = PhysicalPositiveMillimeters.from(40)!!,
                depth = PhysicalPositiveMillimeters.from(50)!!,
            ),
            mountingTypeId = PhysicalMountingTypeId("din35"),
            allowedOrientations = setOf(PhysicalInstallationOrientation.Deg0),
            clearance = PhysicalInstallationClearance(
                top = PhysicalNonNegativeMillimeters.from(0)!!,
                right = PhysicalNonNegativeMillimeters.from(0)!!,
                bottom = PhysicalNonNegativeMillimeters.from(0)!!,
                left = PhysicalNonNegativeMillimeters.from(0)!!,
            ),
            compatibleContainerKinds = setOf(PhysicalContainerKindId("cabinet")),
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
                compatibleContainerKinds = provenance.getValue(PhysicalInstallationContractField.CompatibleContainerKinds),
            ),
        )
    }

    private fun provenance(sourceUnitId: PhysicalSourceUnitId, declarationId: String) = PhysicalSourceProvenance(
        sourceUnitId = sourceUnitId,
        declarationId = declarationId,
        span = PhysicalSourceSpan("src/main.athena", line = 1, column = 1),
    )

    private fun PhysicalVector2i.toFrame(origin: PhysicalPoint2i) = com.engineeringood.athena.physical.PhysicalRigidFrame2i(
        origin = origin,
        alongAxis = this,
        normalAxis = PhysicalVector2i(0, 1),
    )

    private data class Fixture(
        val physical: PhysicalInstallationIR,
        val material: AthenaResolvedRepresentationMaterial,
    )
}
