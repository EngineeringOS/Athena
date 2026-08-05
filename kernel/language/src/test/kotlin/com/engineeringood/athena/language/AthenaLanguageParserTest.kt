package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaLanguageParserTest {
    @Test
    fun `parses ordered package and symbol target imports with exact spans`() {
        val packageImport = "com.engineeringood.controls"
        val symbolImport = "com.engineeringood.controls.Switch"
        val source =
            """
            package com.engineeringood.root
            import $packageImport
            import $symbolImport
            system Demo {
              entity PLC1 {
                type Switch
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("imports.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(2, success.ast.imports.size)
        val firstStart = source.indexOf("import $packageImport")
        val secondStart = source.indexOf("import $symbolImport")
        assertEquals(
            ImportDeclaration(
                target = QualifiedName(
                    parts = listOf("com", "engineeringood", "controls"),
                    span = SourceSpan(
                        SourcePosition(firstStart + 7, 2, 8),
                        SourcePosition(firstStart + 7 + packageImport.length, 2, 8 + packageImport.length),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(firstStart, 2, 1),
                    SourcePosition(firstStart + 7 + packageImport.length, 2, 8 + packageImport.length),
                ),
            ),
            success.ast.imports[0],
        )
        assertEquals(
            ImportDeclaration(
                target = QualifiedName(
                    parts = listOf("com", "engineeringood", "controls", "Switch"),
                    span = SourceSpan(
                        SourcePosition(secondStart + 7, 3, 8),
                        SourcePosition(secondStart + 7 + symbolImport.length, 3, 8 + symbolImport.length),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(secondStart, 3, 1),
                    SourcePosition(secondStart + 7 + symbolImport.length, 3, 8 + symbolImport.length),
                ),
            ),
            success.ast.imports[1],
        )
        assertEquals("PLC1", assertIs<EntityDeclaration>(success.ast.declarations.single()).name)
    }

    @Test
    fun `parses a package-free hyphenated import with an exact file span`() {
        val target = "com.engineeringood.m18-controls.Switch2"
        val source = "import $target\nsystem Demo {}"

        val result = AthenaLanguageParser().parse("package-free-import.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(null, success.ast.packageDeclaration)
        assertEquals(
            ImportDeclaration(
                target = QualifiedName(
                    parts = listOf("com", "engineeringood", "m18-controls", "Switch2"),
                    span = SourceSpan(
                        SourcePosition(offset = 7, line = 1, column = 8),
                        SourcePosition(offset = 7 + target.length, line = 1, column = 8 + target.length),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(offset = 0, line = 1, column = 1),
                    SourcePosition(offset = 7 + target.length, line = 1, column = 8 + target.length),
                ),
            ),
            success.ast.imports.single(),
        )
        assertEquals(
            SourceSpan(
                SourcePosition(offset = 0, line = 1, column = 1),
                SourcePosition(offset = source.length, line = 2, column = 15),
            ),
            success.ast.span,
        )
    }

    @Test
    fun `parses package declaration into authored ast with exact spans`() {
        val packageName = "com.engineeringood.m18.factory-line"
        val source =
            """
            package $packageName
            system Demo {
              entity PLC1 {
                type Switch
              }
              port PLC1.out {
                direction out
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("package-demo.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertTrue(success.ast.imports.isEmpty())
        val packageNameEnd = 8 + packageName.length
        val packageDeclaration = assertNotNull(success.ast.packageDeclaration)
        assertEquals(
            PackageDeclaration(
                name = QualifiedName(
                    parts = listOf("com", "engineeringood", "m18", "factory-line"),
                    span = SourceSpan(
                        SourcePosition(offset = 8, line = 1, column = 9),
                        SourcePosition(offset = packageNameEnd, line = 1, column = packageNameEnd + 1),
                    ),
                ),
                span = SourceSpan(
                    SourcePosition(offset = 0, line = 1, column = 1),
                    SourcePosition(offset = packageNameEnd, line = 1, column = packageNameEnd + 1),
                ),
            ),
            packageDeclaration,
        )
        assertEquals(
            SourceSpan(
                SourcePosition(offset = 0, line = 1, column = 1),
                SourcePosition(offset = source.length, line = source.lines().size, column = 2),
            ),
            success.ast.span,
        )
        assertEquals(2, success.ast.declarations.size)
        assertEquals("PLC1", assertIs<EntityDeclaration>(success.ast.declarations[0]).name)
        assertEquals(listOf("PLC1", "out"), assertIs<PortDeclaration>(success.ast.declarations[1]).qualifiedName.parts)
    }

    @Test
    fun `parses nested Entity owned Ports as first class anatomy`() {
        val source =
            """
            system Demo {
              entity SpareTerminalXT99 {
                concept Switch
                model "SPARE-XT"

                port in1 {
                  direction in
                  flow Digital
                }
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("nested-port.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val entity = assertIs<EntityDeclaration>(success.ast.declarations.single())
        assertEquals("SpareTerminalXT99", entity.name)
        assertEquals(2, entity.fields.size)
        val nestedPort = entity.nestedPorts.single()
        assertEquals(listOf("SpareTerminalXT99", "in1"), nestedPort.qualifiedName.parts)
        assertEquals(2, nestedPort.fields.size)
        assertEquals("direction", nestedPort.fields[0].name)
        assertEquals("flow", nestedPort.fields[1].name)
        assertTrue(nestedPort.span.start.offset > entity.span.start.offset)
        assertTrue(nestedPort.span.end.offset < entity.span.end.offset)
    }

    @Test
    fun `parses grouped connectivity interfaces with defaults and member port overrides`() {
        val source =
            """
            system Demo {
              entity Drive {
                concept MotorDrive
                connectivity enabled

                interface powerInput {
                  type power
                  direction in
                  flow PowerAC
                  role line
                  multiplicity single

                  ports {
                    L1
                    L2
                    PE {
                      flow ProtectiveEarth
                      role protective_earth
                      direction bidirectional
                    }
                  }
                }
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("grouped-interface.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val entity = assertIs<EntityDeclaration>(success.ast.declarations.single())
        val groupedInterface = entity.interfaces.single()
        assertEquals("powerInput", groupedInterface.name)
        assertEquals(listOf("type", "direction", "flow", "role", "multiplicity"), groupedInterface.fields.map { it.name })
        assertEquals(listOf("L1", "L2", "PE"), groupedInterface.ports.map { it.name })
        assertTrue(groupedInterface.ports[0].fields.isEmpty())
        assertEquals(listOf("flow", "role", "direction"), groupedInterface.ports[2].fields.map { it.name })
        assertTrue(groupedInterface.span.start.offset > entity.span.start.offset)
        assertTrue(groupedInterface.span.end.offset < entity.span.end.offset)
    }

    @Test
    fun `parses domain relation verbs as human first relationship declarations`() {
        val source =
            """
            system Demo {
              entity Supply { type PowerSource }
              entity Breaker { type Breaker }
              port Supply.L1 { direction out flow power role line }
              port Breaker.input { direction in flow power role line }

              power Supply.L1 to Breaker.input
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("domain-relation.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val relation = assertIs<RelationDeclaration>(success.ast.declarations.last())
        assertEquals("power", relation.word.value)
        assertEquals(listOf("Supply", "L1"), relation.source.parts)
        assertEquals(listOf(listOf("Breaker", "input")), relation.targets.map { target -> target.parts })
        assertTrue(relation.word.span.start.offset < relation.source.span.start.offset)
        assertTrue(relation.targets.single().span.start.offset > relation.source.span.end.offset)
    }

    @Test
    fun `parses arrow as relation separator alias`() {
        val source =
            """
            system Demo {
              entity Supply { type PowerSource }
              entity Breaker { type Breaker }
              port Supply.L1 { direction out flow power role line }
              port Breaker.input { direction in flow power role line }

              power Supply.L1 -> Breaker.input
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("domain-relation-arrow.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val relation = assertIs<RelationDeclaration>(success.ast.declarations.last())
        assertEquals("power", relation.word.value)
        assertEquals(listOf("Supply", "L1"), relation.source.parts)
        assertEquals(listOf("Breaker", "input"), relation.targets.single().parts)
    }


    @Test
    fun `parses domain relation target lists with ordered targets and one source span`() {
        val source =
            """
            system Demo {
              entity EarthBar { type ProtectiveEarth }
              entity Motor { type Motor }
              entity Cabinet { type Terminal }
              port EarthBar.PE { direction bidirectional flow pe role protective_earth }
              port Motor.PE { direction bidirectional flow pe role protective_earth }
              port Cabinet.PE { direction bidirectional flow pe role protective_earth }

              earth EarthBar.PE to [Motor.PE, Cabinet.PE]
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("domain-relation-group.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val relation = assertIs<RelationDeclaration>(success.ast.declarations.last())
        assertEquals("earth", relation.word.value)
        assertEquals(listOf("EarthBar", "PE"), relation.source.parts)
        assertEquals(
            listOf(listOf("Motor", "PE"), listOf("Cabinet", "PE")),
            relation.targets.map { target -> target.parts },
        )
        assertTrue(relation.targets[0].span.start.offset < relation.targets[1].span.start.offset)
        assertTrue(relation.span.start.offset < relation.targets[1].span.end.offset)
    }

    @Test
    fun `rejects removed authored intent source blocks`() {
        val parser = AthenaLanguageParser()
        val cases = mapOf(
            "flat-connect-intent.athena" to
                """
                system Demo {
                  connect supply Supply.L1 to Drive.L1 intent { class power }
                }
                """.trimIndent(),
            "group-connect-intent.athena" to
                """
                system Demo {
                  connect supply {
                    intent { class power }
                    drive_l1 Supply.L1 to Drive.L1
                  }
                }
                """.trimIndent(),
            "group-edge-intent.athena" to
                """
                system Demo {
                  connect supply {
                    drive_l1 Supply.L1 to Drive.L1 intent { class power }
                  }
                }
                """.trimIndent(),
            "interface-intent.athena" to
                """
                system Demo {
                  entity Drive {
                    interface powerInput {
                      intent default { class power }
                      ports { L1 }
                    }
                  }
                }
                """.trimIndent(),
            "profile-intent.athena" to
                """
                profile ControlDrawingIEC {
                  projection schematic
                  intent default { class control }
                }
                """.trimIndent(),
        )

        cases.forEach { (name, source) ->
            val failure = assertIs<ParseFailure>(parser.parse(name, source), "Expected $name to reject removed intent syntax")
            assertTrue(failure.diagnostics.isNotEmpty(), "Expected diagnostics for $name")
        }
    }

    @Test
    fun `parses typed external evidence declarations without creating engineering facts`() {
        val source =
            """
            system EvidenceDemo {
              evidence DriveContractIec {
                namespace iec
                reference "IEC:60204-1:clause-13"
                subject contract Drive
                provenance "IEC 60204-1 clause 13 citation"
              }
              evidence DriveInterfaceClass {
                namespace classification
                reference "neutral:drive.power-input"
                subject interface Drive.powerInput
                provenance "Neutral classification registry"
              }
              evidence DrivePortIec {
                namespace iec
                reference "IEC:60204-1:protective-conductor"
                subject port Drive.PE
                provenance "IEC 60204-1 PE evidence"
              }
              evidence DriveRelationIec {
                namespace iec
                reference "IEC:60204-1:routing"
                subject relation-contract drive_pe
                provenance "IEC relation contract citation"
              }
              evidence PowerRoutePolicyClass {
                namespace classification
                reference "neutral:route.power"
                subject route-policy powerRoutes
                provenance "Neutral route policy class"
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("external-evidence.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val declarations = success.ast.declarations.filterIsInstance<ExternalEvidenceDeclaration>()
        assertEquals(5, declarations.size)
        assertEquals("DriveContractIec", declarations[0].name)
        assertEquals("iec", declarations[0].namespace.value)
        assertEquals("IEC:60204-1:clause-13", declarations[0].reference.value)
        assertEquals(ExternalEvidenceSubjectKind.CONTRACT, declarations[0].subject.kind)
        assertEquals(listOf("Drive"), declarations[0].subject.target.parts)
        assertEquals(ExternalEvidenceSubjectKind.INTERFACE, declarations[1].subject.kind)
        assertEquals(listOf("Drive", "powerInput"), declarations[1].subject.target.parts)
        assertEquals(ExternalEvidenceSubjectKind.PORT, declarations[2].subject.kind)
        assertEquals(ExternalEvidenceSubjectKind.RELATION_CONTRACT, declarations[3].subject.kind)
        assertEquals(ExternalEvidenceSubjectKind.ROUTE_POLICY, declarations[4].subject.kind)
        assertTrue(declarations.all { declaration -> declaration.span.start.line < declaration.span.end.line })
        assertTrue(declarations.all { declaration -> declaration.provenance.value.isNotBlank() })
    }

    @Test
    fun `parses typed projection policy declarations without engineering truth`() {
        val source =
            """
            system ProjectionPolicyDemo {
              projection ControlDrawingProjection {
                target professional-connection-drawing
                layout orthogonal-grid
                drawingProfile ControlDrawingIEC
                routeQuality ControlDrawingRouteQuality
                proof exact-endpoints
                proof source-trace
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("projection-policy.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val policy = assertIs<ProjectionPolicyDeclaration>(success.ast.declarations.single())
        assertEquals("ControlDrawingProjection", policy.name)
        assertEquals("professional-connection-drawing", assertNotNull(policy.target).value)
        assertEquals("orthogonal-grid", assertNotNull(policy.layoutStrategy).value)
        assertEquals("ControlDrawingIEC", assertNotNull(policy.drawingProfile).value)
        assertEquals("ControlDrawingRouteQuality", assertNotNull(policy.routeQualityPolicy).value)
        assertEquals(listOf("exact-endpoints", "source-trace"), policy.proofObligations.map { it.value })
        assertTrue(policy.forbiddenEngineeringTruth.isEmpty())
        assertTrue(policy.span.start.line < policy.span.end.line)
    }

    @Test
    fun `parses projection owned engineering truth for compiler rejection`() {
        val source =
            """
            system ProjectionPolicyInvalid {
              projection BadProjection {
                target professional-connection-drawing
                layout orthogonal-grid
                drawingProfile ControlDrawingIEC
                routeQuality ControlDrawingRouteQuality
                port Drive.L1 input
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("projection-policy-invalid.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val policy = assertIs<ProjectionPolicyDeclaration>(success.ast.declarations.single())
        assertEquals(listOf("port"), policy.forbiddenEngineeringTruth.map { it.kind })
        assertTrue(policy.forbiddenEngineeringTruth.all { truth -> truth.span.start.offset > policy.span.start.offset })
    }

    @Test
    fun `parses a single segment package name`() {
        val result = AthenaLanguageParser().parse(
            "single-package.athena",
            """
            package controls
            system Demo {}
            """.trimIndent(),
        )

        val success = assertIs<ParseSuccess>(result)
        assertEquals(listOf("controls"), assertNotNull(success.ast.packageDeclaration).name.parts)
    }

    @Test
    fun `parses the demo cabinet example into a syntax-only ast`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val source = Files.readString(examplePath)

        val result = AthenaLanguageParser().parse(examplePath.toString(), source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(null, success.ast.packageDeclaration)
        val deviceOne = success.ast.declarations[0] as EntityDeclaration
        val deviceTwo = success.ast.declarations[1] as EntityDeclaration
        val portOne = success.ast.declarations[2] as PortDeclaration
        val portTwo = success.ast.declarations[3] as PortDeclaration
        val relation = success.ast.declarations[4] as RelationDeclaration
        assertEquals(
            SourceFileAst(
                system = SystemDeclaration("DemoCabinet", success.ast.system.span),
                declarations = listOf(
                    EntityDeclaration(
                        name = "PLC1",
                        fields = listOf(
                            PropertyAssignment("concept", ScalarValue.Symbol("Switch", deviceOne.fields[0].value.span), deviceOne.fields[0].span),
                            PropertyAssignment("model", ScalarValue.Text("S7-1200", deviceOne.fields[1].value.span), deviceOne.fields[1].span),
                        ),
                        span = deviceOne.span,
                    ),
                    EntityDeclaration(
                        name = "M1",
                        fields = listOf(
                            PropertyAssignment("concept", ScalarValue.Symbol("Motor", deviceTwo.fields[0].value.span), deviceTwo.fields[0].span),
                        ),
                        span = deviceTwo.span,
                    ),
                    PortDeclaration(
                        qualifiedName = QualifiedName(listOf("PLC1", "out"), portOne.qualifiedName.span),
                        fields = listOf(
                            PropertyAssignment("direction", ScalarValue.Symbol("out", portOne.fields[0].value.span), portOne.fields[0].span),
                            PropertyAssignment("flow", ScalarValue.Symbol("Digital", portOne.fields[1].value.span), portOne.fields[1].span),
                        ),
                        span = portOne.span,
                    ),
                    PortDeclaration(
                        qualifiedName = QualifiedName(listOf("M1", "in"), portTwo.qualifiedName.span),
                        fields = listOf(
                            PropertyAssignment("direction", ScalarValue.Symbol("in", portTwo.fields[0].value.span), portTwo.fields[0].span),
                            PropertyAssignment("flow", ScalarValue.Symbol("Digital", portTwo.fields[1].value.span), portTwo.fields[1].span),
                        ),
                        span = portTwo.span,
                    ),
                    RelationDeclaration(
                        word = SymbolIdentifierField("power", relation.word.span),
                        source = QualifiedName(listOf("PLC1", "out"), relation.source.span),
                        targets = listOf(QualifiedName(listOf("M1", "in"), relation.targets.single().span)),
                        span = relation.span,
                    ),
                ),
                span = success.ast.span,
            ),
            success.ast,
        )
    }

    @Test
    fun `parses m23 layout block into authored syntax-only ast`() {
        val source = Files.readString(resolveRepoRoot().resolve("examples/m23/parser-parity-proof/valid-layout-block.athena"))

        val result = AthenaLanguageParser().parse("valid-layout-block.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(2, success.ast.declarations.size)
        val layout = assertIs<LayoutDeclaration>(success.ast.declarations[1])
        assertEquals("schematic-sheet", layout.viewFamily)
        assertEquals(
            listOf(
                LayoutStatement.PlaceNear(subject = "HMI1", target = "PLC1", span = layout.statements[0].span),
                LayoutStatement.PlaceBelow(subject = "XT1", target = "PLC1", span = layout.statements[1].span),
                LayoutStatement.AlignWith(subject = "HMI1", target = "PLC1", axis = LayoutAxis.Vertical, span = layout.statements[2].span),
                LayoutStatement.AlignWith(subject = "HMI2", target = "PLC1", axis = LayoutAxis.Horizontal, span = layout.statements[3].span),
                LayoutStatement.GroupWith(subject = "HMI1", target = "PLC1", span = layout.statements[4].span),
            ),
            layout.statements,
        )
        assertTrue(layout.span.start.line < layout.span.end.line)
    }

    @Test
    fun `parses cabinet installation block into typed authored source model with spans`() {
        val source =
            """
            system Demo {
              entity QF1 {
                type Breaker
              }
              entity M1 {
                type Motor
              }
              port QF1.line {
                direction out
              }
              port M1.line {
                direction in
              }
              power QF1.line to M1.line

              installation cabinet MainCabinet {
                enclosure ENC1 size (800mm, 600mm, 250mm)
                surface Backplate in ENC1 at (20mm, 20mm) size (760mm, 560mm) accepts [din35, screw]
                rail DIN1 on Backplate at (60mm, 120mm) length 680mm orientation horizontal mounting din35
                duct D1 in ENC1 at (30mm, 60mm) size (40mm, 480mm) orientation vertical wall 2mm
                channel C1 in D1 at (4mm, 8mm) size (32mm, 464mm) lanes 4 margin 2mm
                terminal-group XT1 in ENC1 at (560mm, 420mm) size (160mm, 80mm) orientation horizontal accepts [terminal]
                mount QF1 as QF1Mount on DIN1 at (0mm, 0mm) {
                  footprint (45mm, 90mm, 70mm)
                  mounting din35
                  orientation deg0
                  allowed-orientations [deg0, deg180]
                  clearance (10mm, 5mm, 10mm, 5mm)
                  compatible-containers [cabinet]
                }
                mount M1 as M1Mount on DIN1 at (100mm, 0mm) {
                  footprint (120mm, 160mm, 100mm)
                  mounting din35
                  orientation deg0
                  allowed-orientations [deg0]
                  clearance (5mm, 5mm, 5mm, 5mm)
                  compatible-containers [cabinet]
                }
                route feeder through [C1]
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("cabinet-installation.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val installation = assertIs<InstallationDeclaration>(success.ast.declarations.last())
        assertEquals("MainCabinet", installation.name)
        assertEquals(InstallationKind.Cabinet, installation.kind)
        assertEquals("ENC1", installation.enclosures.single().id)
        assertEquals(800.0, installation.enclosures.single().size.width.value)
        assertEquals("mm", installation.enclosures.single().size.width.unit)
        assertEquals("Backplate", installation.surfaces.single().id)
        assertEquals(listOf("din35", "screw"), installation.surfaces.single().acceptedMountingTypes)
        assertEquals("DIN1", installation.rails.single().id)
        assertEquals(InstallationOrientation.Horizontal, installation.rails.single().orientation)
        assertEquals("D1", installation.ducts.single().id)
        assertEquals(InstallationOrientation.Vertical, installation.ducts.single().orientation)
        assertEquals("C1", installation.channels.single().id)
        assertEquals(4, installation.channels.single().lanes)
        assertEquals("XT1", installation.terminalGroups.single().id)
        assertEquals("QF1Mount", installation.mounts[0].id)
        assertEquals("QF1", installation.mounts[0].deviceId)
        assertEquals("DIN1", installation.mounts[0].targetId)
        assertEquals(45.0, installation.mounts[0].footprint.width.value)
        assertEquals(90.0, installation.mounts[0].footprint.height.value)
        assertEquals(70.0, installation.mounts[0].footprint.depth.value)
        assertEquals("din35", installation.mounts[0].mountingType)
        assertEquals(InstallationMountOrientation.Deg0, installation.mounts[0].orientation)
        assertEquals(
            listOf(InstallationMountOrientation.Deg0, InstallationMountOrientation.Deg180),
            installation.mounts[0].allowedOrientations,
        )
        assertEquals(10.0, installation.mounts[0].clearance.top.value)
        assertEquals(5.0, installation.mounts[0].clearance.right.value)
        assertEquals(10.0, installation.mounts[0].clearance.bottom.value)
        assertEquals(5.0, installation.mounts[0].clearance.left.value)
        assertEquals(listOf("cabinet"), installation.mounts[0].compatibleContainerKinds)
        assertEquals("M1Mount", installation.mounts[1].id)
        assertEquals("feeder", installation.routes.single().relationshipId)
        assertEquals(listOf("C1"), installation.routes.single().channelIds)
        assertTrue(installation.span.start.line < installation.span.end.line)
        assertTrue(installation.enclosures.single().span.start.offset > installation.span.start.offset)
        assertTrue(installation.routes.single().span.end.offset < installation.span.end.offset)
    }

    @Test
    fun `rejects non cabinet installation kinds and implementation vocabulary in public syntax`() {
        val cases = listOf(
            "panel-kind" to """
                system Demo {
                  installation panel MainPanel {
                  }
                }
            """.trimIndent(),
            "renderer-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    renderer HtmlCanvas
                  }
                }
            """.trimIndent(),
            "graphic-ir-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    GraphicPrimitive RoutePreview
                  }
                }
            """.trimIndent(),
            "missing-required-member-fields" to """
                system Demo {
                  installation cabinet MainCabinet {
                    enclosure ENC1
                  }
                }
            """.trimIndent(),
            "ir-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    IR CabinetModel
                  }
                }
            """.trimIndent(),
            "occurrence-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    occurrence QF1Mount
                  }
                }
            """.trimIndent(),
            "descriptor-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    descriptor CabinetShape
                  }
                }
            """.trimIndent(),
            "snapshot-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    snapshot CabinetState
                  }
                }
            """.trimIndent(),
            "pixel-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    pixel CabinetDot
                  }
                }
            """.trimIndent(),
            "dom-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    DOM CabinetNode
                  }
                }
            """.trimIndent(),
            "transport-term" to """
                system Demo {
                  installation cabinet MainCabinet {
                    transport CabinetPayload
                  }
                }
            """.trimIndent(),
        )

        cases.forEach { (name, invalidSource) ->
            val result = AthenaLanguageParser().parse("$name.athena", invalidSource)
            val failure = assertIs<ParseFailure>(result, "Expected $name to fail")
            assertTrue(failure.diagnostics.single().message.isNotBlank(), "Expected diagnostic for $name")
        }
    }

    @Test
    fun `rejects duplicate installation member ids at authored source boundary`() {
        val source =
            """
            system Demo {
              installation cabinet MainCabinet {
                enclosure ENC1 size (800mm, 600mm, 250mm)
                surface ENC1 in ENC1 at (20mm, 20mm) size (760mm, 560mm) accepts [din35]
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("duplicate-installation-member.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.single().message.contains("Duplicate installation member id"))
    }

    @Test
    fun `parses deterministically for identical source input`() {
        val source = """
            system DemoCabinet {
              entity PLC1 {
                type PLC
                model "S7-1200"
              }
            
              port PLC1.out {
                direction out
                flow Digital
              }
            
              connect plc_self PLC1.out to PLC1.out
            }
        """.trimIndent()

        val parser = AthenaLanguageParser()

        val first = parser.parse("demo.athena", source)
        val second = parser.parse("demo.athena", source)

        assertEquals(first, second)
    }

    @Test
    fun `reports syntax diagnostics with file line and column provenance`() {
        val source = """
            system DemoCabinet {
              entity PLC1 {
                type PLC
              }
            
              connect bad PLC1.out M1.in
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("broken.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("broken.athena", failure.diagnostics.single().file)
        assertEquals(6, failure.diagnostics.single().line)
        assertTrue(failure.diagnostics.single().column > 0)
        assertTrue(failure.diagnostics.single().message.contains("to"))
    }

    @Test
    fun `reports a typed diagnostic for an unterminated string literal without crashing`() {
        val source = """
            system DemoCabinet {
              entity PLC1 {
                model "S7-1200
              }
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("unterminated-string.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.isNotEmpty())
        val diagnostic = failure.diagnostics.first()
        assertEquals("unterminated-string.athena", diagnostic.file)
        assertTrue(diagnostic.line > 0, "Expected a real line, got ${diagnostic.line}")
        assertTrue(diagnostic.column > 0, "Expected a real column, got ${diagnostic.column}")
        assertTrue(diagnostic.message.isNotBlank())
    }

    @Test
    fun `reports a typed diagnostic for a missing closing brace without crashing`() {
        val source = """
            system DemoCabinet {
              entity PLC1 {
                type Switch
        """.trimIndent()

        val result = AthenaLanguageParser().parse("missing-brace.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.isNotEmpty())
        val diagnostic = failure.diagnostics.first()
        assertEquals("missing-brace.athena", diagnostic.file)
        assertTrue(diagnostic.line > 0, "Expected a real line, got ${diagnostic.line}")
        assertTrue(diagnostic.column > 0, "Expected a real column, got ${diagnostic.column}")
        assertTrue(diagnostic.message.isNotBlank())
    }

    @Test
    fun `reports failures deterministically for identical malformed source input`() {
        val source = """
            system DemoCabinet {
              connect PLC1.out M1.in
            }
        """.trimIndent()

        val parser = AthenaLanguageParser()

        val first = parser.parse("broken.athena", source)
        val second = parser.parse("broken.athena", source)

        assertIs<ParseFailure>(first)
        assertEquals(first, second)
    }

    @Test
    fun `reports malformed package declarations as deterministic positioned diagnostics`() {
        val cases = listOf(
            MalformedPackageCase(
                name = "missing-name",
                source = """
                    package
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 8,
                expectedSpanStartOffset = 15,
                expectedSpanLength = 4,
            ),
            MalformedPackageCase(
                name = "duplicate",
                source = """
                    package com.one
                    package com.two
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 1,
                expectedSpanStartOffset = 16,
                expectedSpanLength = 7,
            ),
            MalformedPackageCase(
                name = "misplaced",
                source = """
                    system Demo {}
                    package com.one
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 1,
                expectedSpanStartOffset = 15,
                expectedSpanLength = 7,
            ),
            MalformedPackageCase(
                name = "malformed",
                source = """
                    package com..broken
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 13,
                expectedSpanStartOffset = 12,
                expectedSpanLength = 1,
            ),
            MalformedPackageCase(
                name = "leading-hyphen",
                source = """
                    package com.-broken
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 13,
                expectedSpanStartOffset = 12,
                expectedSpanLength = 1,
            ),
            MalformedPackageCase(
                name = "trailing-hyphen",
                source = """
                    package com.broken-
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 2,
                expectedErrorColumn = 8,
                expectedSpanStartOffset = 27,
                expectedSpanLength = 4,
            ),
            MalformedPackageCase(
                name = "repeated-hyphen",
                source = """
                    package com.broken--name
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 20,
                expectedSpanStartOffset = 19,
                expectedSpanLength = 1,
            ),
            MalformedPackageCase(
                name = "spaced-hyphen",
                source = """
                    package com.factory - line
                    system Demo {}
                """.trimIndent(),
                expectedErrorLine = 1,
                expectedErrorColumn = 20,
                expectedSpanStartOffset = 19,
                expectedSpanLength = 1,
            ),
        )

        cases.forEach { case ->
            val file = "${case.name}.athena"
            val parser = AthenaLanguageParser()
            val first = parser.parse(file, case.source)
            val second = parser.parse(file, case.source)

            val failure = assertIs<ParseFailure>(first, "Expected ${case.name} to fail")
            assertEquals(first, second, "Expected ${case.name} failure to be deterministic")
            assertEquals(1, failure.diagnostics.size, "diagnostic count for ${case.name}")
            val diagnostic = failure.diagnostics.single()
            assertEquals(file, diagnostic.file)
            assertEquals(case.expectedErrorLine, diagnostic.line, "diagnostic line for ${case.name}")
            assertEquals(case.expectedErrorColumn, diagnostic.column, "diagnostic column for ${case.name}")
            assertTrue(diagnostic.message.isNotBlank() && diagnostic.message != "Syntax error", "diagnostic message for ${case.name}")
            assertEquals(
                SourceSpan(
                    start = SourcePosition(
                        offset = case.expectedSpanStartOffset,
                        line = case.expectedErrorLine,
                        column = case.expectedErrorColumn,
                    ),
                    end = SourcePosition(
                        offset = case.expectedSpanStartOffset + case.expectedSpanLength,
                        line = case.expectedErrorLine,
                        column = case.expectedErrorColumn + case.expectedSpanLength,
                    ),
                ),
                diagnostic.span,
                "diagnostic span for ${case.name}",
            )
        }
    }

    @Test
    fun `rejects unsupported and malformed import forms deterministically`() {
        val cases = listOf(
            malformedImportCase(
                name = "missing",
                source = "import\nsystem Demo {}",
                marker = "system",
                expectedMessageFragment = "import target",
            ),
            malformedImportCase(
                name = "next-line-target",
                source = "import\ncontrols\nsystem Demo {}",
                marker = "controls",
                expectedMessageFragment = "import target",
            ),
            malformedImportCase(
                name = "alias",
                source = "import com.engineeringood.controls as controls\nsystem Demo {}",
                marker = "as",
            ),
            malformedImportCase(
                name = "wildcard",
                source = "import com.engineeringood.controls.*\nsystem Demo {}",
                marker = "*",
            ),
            malformedImportCase(
                name = "misplaced",
                source = "system Demo {}\nimport com.engineeringood.controls",
                marker = "import",
            ),
            run {
                val source = "import com.controls\nimport com.controls\nsystem Demo {}"
                malformedImportCase(
                    name = "duplicate",
                    source = source,
                    marker = "import",
                    expectedOffset = source.lastIndexOf("import"),
                    expectedMessageFragment = "Duplicate import",
                )
            },
            malformedImportCase(
                name = "split-dot",
                source = "import com.engineeringood . controls\nsystem Demo {}",
                marker = " .",
                expectedSpanLength = 1,
            ),
        )

        cases.forEach { case ->
            val parser = AthenaLanguageParser()
            val file = "${case.name}.athena"
            val first = parser.parse(file, case.source)
            val second = parser.parse(file, case.source)
            val failure = assertIs<ParseFailure>(first, "Expected ${case.name} import syntax to fail")
            assertEquals(first, second, "Expected ${case.name} import failure to be deterministic")
            assertEquals(1, failure.diagnostics.size, "diagnostic count for ${case.name}")
            val diagnostic = failure.diagnostics.single()
            assertEquals(file, diagnostic.file)
            assertEquals(case.expectedLine, diagnostic.line, "diagnostic line for ${case.name}")
            assertEquals(case.expectedColumn, diagnostic.column, "diagnostic column for ${case.name}")
            assertTrue(diagnostic.message.isNotBlank())
            case.expectedMessageFragment?.let { assertTrue(diagnostic.message.contains(it)) }
            assertEquals(case.expectedSpan, diagnostic.span, "diagnostic span for ${case.name}")
        }
    }

    private fun malformedImportCase(
        name: String,
        source: String,
        marker: String,
        expectedOffset: Int = source.indexOf(marker),
        expectedSpanLength: Int = marker.length,
        expectedMessageFragment: String? = null,
    ): MalformedImportCase {
        val offset = expectedOffset
        check(offset >= 0) { "Marker '$marker' not found in malformed import case '$name'" }
        val beforeMarker = source.substring(0, offset)
        val line = beforeMarker.count { it == '\n' } + 1
        val column = offset - beforeMarker.lastIndexOf('\n')
        return MalformedImportCase(
            name = name,
            source = source,
            expectedLine = line,
            expectedColumn = column,
            expectedSpan = SourceSpan(
                SourcePosition(offset = offset, line = line, column = column),
                SourcePosition(offset = offset + expectedSpanLength, line = line, column = column + expectedSpanLength),
            ),
            expectedMessageFragment = expectedMessageFragment,
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root from ${Path.of("").toAbsolutePath().name}")
        return current
    }

    private data class MalformedPackageCase(
        val name: String,
        val source: String,
        val expectedErrorLine: Int,
        val expectedErrorColumn: Int,
        val expectedSpanStartOffset: Int,
        val expectedSpanLength: Int,
    )

    private data class MalformedImportCase(
        val name: String,
        val source: String,
        val expectedLine: Int,
        val expectedColumn: Int,
        val expectedSpan: SourceSpan,
        val expectedMessageFragment: String?,
    )
}
