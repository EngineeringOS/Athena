package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePort
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRouteId
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpatialRouteCompilerTest {
    @Test
    fun `direct route publishes literal ordered points`() {
        assertEquals(
            listOf(
                SpatialPoint(80, 100),
                SpatialPoint(81, 100),
                SpatialPoint(239, 100),
                SpatialPoint(240, 100),
            ),
            compileRoute().routes.single().points,
        )
    }

    @Test
    fun `one bend route publishes literal ordered points`() {
        val source = occurrence(
            "projection/node/component:Supply",
            "component:Supply",
            SHEET_ID,
            SpatialRect(40, 80, 40, 40),
        )
        val target = occurrence(
            "projection/node/component:Q1",
            "component:Q1",
            SHEET_ID,
            SpatialRect(80, 101, 40, 40),
        )
        val result = SpatialRouteCompiler().compile(
            projection = projectionDocument(),
            sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
            occurrences = listOf(source, target),
            anchors = listOf(
                anchor(
                    "projection/node/component:Supply",
                    "port:Supply.L1",
                    SpatialBoundarySide.RIGHT,
                    SpatialPoint(80, 100),
                ),
                anchor(
                    "projection/node/component:Q1",
                    "port:Q1.1",
                    SpatialBoundarySide.TOP,
                    SpatialPoint(100, 101),
                ),
            ),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(
                SpatialPoint(80, 100),
                SpatialPoint(81, 100),
                SpatialPoint(100, 100),
                SpatialPoint(100, 101),
            ),
            result.routes.single().points,
        )
    }

    @Test
    fun `route detours along obstacle boundary without entering body interior`() {
        val occurrences = routingOccurrences() + occurrence(
            projectionId = "projection/node/component:Blocker",
            subjectId = "component:Blocker",
            sheetId = SHEET_ID,
            rectangle = SpatialRect(140, 60, 40, 80),
        )

        val result = SpatialRouteCompiler().compile(
            projection = projectionDocument(),
            sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
            occurrences = occurrences,
            anchors = routingAnchors(),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(
                SpatialPoint(80, 100),
                SpatialPoint(81, 100),
                SpatialPoint(81, 60),
                SpatialPoint(180, 60),
                SpatialPoint(180, 100),
                SpatialPoint(239, 100),
                SpatialPoint(240, 100),
            ),
            result.routes.single().points,
        )
        assertTrue(result.routes.single().segments.all { segment -> segment.isPositiveOrthogonal })
        assertTrue(result.routes.single().segments.none { segment -> segment.entersInterior(occurrences.last().rectangle) })
    }

    @Test
    fun `route rejects segment through body when both segment vertices remain outside`() {
        val blocker = occurrence(
            projectionId = "projection/node/component:Blocker",
            subjectId = "component:Blocker",
            sheetId = SHEET_ID,
            rectangle = SpatialRect(140, 60, 40, 80),
        )
        val direct = SpatialRouteCompiler().compile(
            projection = projectionDocument(),
            sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
            occurrences = routingOccurrences() + blocker,
            anchors = routingAnchors(),
        ).routes.single()

        assertTrue(direct.points.none { point -> point.x in 141..179 && point.y in 61..139 })
        assertTrue(direct.segments.none { segment -> segment.entersInterior(blocker.rectangle) })
        assertFalse(direct.points == listOf(SpatialPoint(80, 100), SpatialPoint(240, 100)))
    }

    @Test
    fun `route cannot reenter endpoint owner after correct outward stub`() {
        val baseProjection = projectionDocument()
        val axisNode = node("projection/node/component:Axis", "component:Axis")
        val projection = baseProjection.copy(
            nodes = baseProjection.nodes + axisNode,
            sheets = baseProjection.sheets.map { sheet ->
                sheet.copy(
                    subjects = sheet.subjects + ProjectionSheetSubject(
                        axisNode.semanticId,
                        nodeIds = listOf(axisNode.projectionId),
                    ),
                )
            },
        )
        val source = occurrence(
            "projection/node/component:Supply",
            "component:Supply",
            SHEET_ID,
            SpatialRect(10, 10, 10, 10),
        )
        val target = occurrence(
            "projection/node/component:Q1",
            "component:Q1",
            SHEET_ID,
            SpatialRect(-5, 20, 10, 10),
        )
        val axis = occurrence(
            "projection/node/component:Axis",
            "component:Axis",
            SHEET_ID,
            SpatialRect(30, 16, 1, 1),
        )
        val result = SpatialRouteCompiler().compile(
            projection = projection,
            sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(-10, 0, 50, 40))),
            occurrences = listOf(source, target, axis),
            anchors = listOf(
                anchor(
                    "projection/node/component:Supply",
                    "port:Supply.L1",
                    SpatialBoundarySide.RIGHT,
                    SpatialPoint(20, 15),
                ),
                anchor(
                    "projection/node/component:Q1",
                    "port:Q1.1",
                    SpatialBoundarySide.RIGHT,
                    SpatialPoint(5, 25),
                ),
            ),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.routes.single().segments.none { segment ->
            listOf(source, target).any { owner -> segment.entersInterior(owner.rectangle) }
        })
    }

    @Test
    fun `impossible obstacle returns complete failure without route or lane subset`() {
        val result = SpatialRouteCompiler().compile(
            projection = projectionDocument(),
            sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
            occurrences = routingOccurrences() + occurrence(
                projectionId = "projection/node/component:Blocker",
                subjectId = "component:Blocker",
                sheetId = SHEET_ID,
                rectangle = SpatialRect(0, 0, 320, 200),
            ),
            anchors = routingAnchors(),
        )

        assertTrue(result.routes.isEmpty())
        assertTrue(result.lanes.isEmpty())
        assertEquals(1, result.diagnostics.size)
        assertEquals(
            "has no obstacle-safe orthogonal Route inside Sheet $SHEET_ID Drawing Area; " +
                "blocking Occurrences: projection/node/component:Blocker",
            result.diagnostics.single().problem,
        )
        assertEquals(
            "Move or regroup the named blocking Occurrences so one in-area orthogonal path remains.",
            result.diagnostics.single().correction,
        )
        assertContains(
            requireNotNull(result.diagnostics.single().sourceTrace).projectionIds,
            "projection/node/component:Blocker",
        )
    }

    @Test
    fun `cross sheet endpoints fail before path search and defer continuation to M45`() {
        val targetSheetId = "engineering-projection/sheet/02-other"
        val base = projectionDocument()
        val baseSheet = base.sheets.single()
        val targetNodeId = ProjectionNodeId("projection/node/component:Q1")
        val targetSubjects = baseSheet.subjects.filter { subject -> targetNodeId in subject.nodeIds }
        val sourceSubjects = baseSheet.subjects.filterNot { subject -> targetNodeId in subject.nodeIds }
        val projection = base.copy(
            sheets = listOf(
                baseSheet.copy(subjects = sourceSubjects),
                ProjectionSheet(
                    sheetId = ProjectionSheetId(targetSheetId),
                    displayName = "Other",
                    order = 1,
                    subjects = targetSubjects,
                ),
            ),
        )
        val result = SpatialRouteCompiler().compile(
            projection = projection,
            sheets = listOf(
                SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200)),
                SpatialRoutingSheetInput(targetSheetId, SpatialRect(0, 0, 320, 200)),
            ),
            occurrences = routingOccurrences(targetSheetId),
            anchors = routingAnchors(targetSheetId),
        )

        assertTrue(result.routes.isEmpty())
        assertTrue(result.lanes.isEmpty())
        assertEquals(
            "connects Anchors on different Sheets '$SHEET_ID' and '$targetSheetId'",
            result.diagnostics.single().problem,
        )
        assertEquals(
            "Keep both endpoints on one Sheet or defer explicit multi-Sheet continuation to M45.",
            result.diagnostics.single().correction,
        )
    }

    @Test
    fun `route and lane facts are equal across connection obstacle and anchor permutations`() {
        val firstConnection = projectionDocument().connections.single()
        val secondConnection = firstConnection.copy(
            projectionId = ProjectionConnectionId("projection/connection/connection:Supply.L1-to-Q1.2"),
            semanticId = StableSemanticIdentity("connection:Supply.L1-to-Q1.2"),
            originGeometryElementId = GeometryElementId("origin:connection:second"),
        )
        val baseProjection = projectionDocument()
        val projection = baseProjection.copy(
            connections = listOf(secondConnection, firstConnection),
            sheets = baseProjection.sheets.map { sheet ->
                sheet.copy(
                    subjects = sheet.subjects.map { subject ->
                        if (firstConnection.projectionId in subject.connectionIds) {
                            subject.copy(connectionIds = subject.connectionIds + secondConnection.projectionId)
                        } else {
                            subject
                        }
                    },
                )
            },
        )
        val occurrences = routingOccurrences() + occurrence(
            projectionId = "projection/node/component:Blocker",
            subjectId = "component:Blocker",
            sheetId = SHEET_ID,
            rectangle = SpatialRect(140, 60, 40, 80),
        )
        val sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200)))

        val first = SpatialRouteCompiler().compile(projection, sheets, occurrences, routingAnchors())
        val second = SpatialRouteCompiler().compile(
            projection.copy(connections = projection.connections.reversed()),
            sheets.reversed(),
            occurrences.reversed(),
            routingAnchors().reversed(),
        )

        assertEquals(first, second)
        assertEquals(2, first.routes.size)
        assertEquals(1, first.lanes.size)
        assertEquals(first.routes.map { route -> route.routeId }, first.lanes.single().routeIds)
        assertTrue(first.routes.all { route -> route.laneId == first.lanes.single().laneId })
    }

    @Test
    fun `multiple unresolved endpoints block valid sibling Route under input permutations`() {
        val base = projectionDocument()
        val foreignSheetId = "engineering-projection/sheet/02-foreign"
        val unresolvedSource = node("projection/node/component:Q2", "component:Q2")
        val unresolvedTarget = node("projection/node/component:Q3", "component:Q3")
        val sourcePort = port(unresolvedSource, "port:Q2.1")
        val targetPort = port(unresolvedTarget, "port:Q3.1")
        val unresolved = ProjectionConnection(
            projectionId = ProjectionConnectionId("projection/connection/connection:Q2.1-to-Q3.1"),
            semanticId = StableSemanticIdentity("connection:Q2.1-to-Q3.1"),
            originGeometryElementId = GeometryElementId("origin:connection:Q2-Q3"),
            source = ProjectionConnectionEndpoint(sourcePort.occurrencePortId),
            target = ProjectionConnectionEndpoint(targetPort.occurrencePortId),
        )
        val projection = base.copy(
            nodes = base.nodes + unresolvedSource + unresolvedTarget,
            occurrencePorts = base.occurrencePorts + sourcePort + targetPort,
            connections = base.connections + unresolved,
            sheets = base.sheets.map { sheet ->
                sheet.copy(
                    subjects = sheet.subjects +
                        ProjectionSheetSubject(
                            unresolvedSource.semanticId,
                            nodeIds = listOf(unresolvedSource.projectionId),
                        ) +
                        ProjectionSheetSubject(
                            unresolvedTarget.semanticId,
                            nodeIds = listOf(unresolvedTarget.projectionId),
                        ) +
                        ProjectionSheetSubject(unresolved.semanticId, connectionIds = listOf(unresolved.projectionId)),
                )
            },
        )
        val sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 240)))
        val occurrences = routingOccurrences() + listOf(
            occurrence(
                unresolvedSource.projectionId.value,
                unresolvedSource.semanticId.value,
                SHEET_ID,
                SpatialRect(40, 160, 40, 40),
            ),
            occurrence(
                unresolvedTarget.projectionId.value,
                unresolvedTarget.semanticId.value,
                SHEET_ID,
                SpatialRect(240, 160, 40, 40),
            ),
        )
        val anchors = routingAnchors() + listOf(
            anchor(
                unresolvedSource.projectionId.value,
                sourcePort.occurrencePortId.portId.value,
                SpatialBoundarySide.RIGHT,
                SpatialPoint(80, 180),
                foreignSheetId,
            ),
            anchor(
                unresolvedTarget.projectionId.value,
                targetPort.occurrencePortId.portId.value,
                SpatialBoundarySide.LEFT,
                SpatialPoint(240, 180),
                foreignSheetId,
            ),
        )
        val first = SpatialRouteCompiler().compile(projection, sheets, occurrences, anchors)
        val permuted = SpatialRouteCompiler().compile(
            projection.copy(connections = projection.connections.reversed()),
            sheets.reversed(),
            occurrences.reversed(),
            anchors.reversed(),
        )

        assertEquals(first, permuted)
        assertTrue(first.routes.isEmpty())
        assertTrue(first.lanes.isEmpty())
        assertEquals(
            listOf(
                Triple(
                    "has no resolved source Anchor on owning Sheet $SHEET_ID; " +
                        "matching Anchor resolves on Sheet $foreignSheetId",
                    listOf(
                        SHEET_ID,
                        unresolved.projectionId.value,
                        unresolvedSource.projectionId.value,
                        sourcePort.occurrencePortId.portId.value,
                        foreignSheetId,
                    ),
                    listOf(
                        "geometry:projection/node/component:Q2:port:Q2.1",
                        "origin:connection:Q2-Q3",
                        "origin:port:Q2.1",
                    ),
                ),
                Triple(
                    "has no resolved target Anchor on owning Sheet $SHEET_ID; " +
                        "matching Anchor resolves on Sheet $foreignSheetId",
                    listOf(
                        SHEET_ID,
                        unresolved.projectionId.value,
                        unresolvedTarget.projectionId.value,
                        targetPort.occurrencePortId.portId.value,
                        foreignSheetId,
                    ),
                    listOf(
                        "geometry:projection/node/component:Q3:port:Q3.1",
                        "origin:connection:Q2-Q3",
                        "origin:port:Q3.1",
                    ),
                ),
            ),
            first.diagnostics.map { diagnostic ->
                assertEquals("Connection ${unresolved.projectionId.value}", diagnostic.subject)
                Triple(
                    diagnostic.problem,
                    diagnostic.sourceTrace.projectionIds,
                    diagnostic.sourceTrace.geometryElementIds.map { geometryId -> geometryId.value },
                )
            },
        )
    }

    @Test
    fun `connection owning Sheet selects complete typed Anchors when occurrences repeat`() {
        val projection = projectionDocument()
        val otherSheetId = "engineering-projection/sheet/02-repeat"
        val repeatedSubjects = projection.sheets.single().subjects
            .map { subject -> subject.copy(connectionIds = emptyList()) }
            .filter { subject -> subject.nodeIds.isNotEmpty() }
        val repeatedSheet = projection.sheets.single().copy(
            sheetId = ProjectionSheetId(otherSheetId),
            displayName = "Repeated Nodes",
            order = 1,
            subjects = repeatedSubjects,
        )
        val otherAnchors = listOf(
            anchor(
                "projection/node/component:Supply",
                "port:Supply.L1",
                SpatialBoundarySide.RIGHT,
                SpatialPoint(80, 100),
                otherSheetId,
            ),
            anchor(
                "projection/node/component:Q1",
                "port:Q1.1",
                SpatialBoundarySide.LEFT,
                SpatialPoint(240, 100),
                otherSheetId,
            ),
        )
        val otherOccurrences = listOf(
            occurrence(
                "projection/node/component:Supply",
                "component:Supply",
                otherSheetId,
                SpatialRect(40, 80, 40, 40),
            ),
            occurrence(
                "projection/node/component:Q1",
                "component:Q1",
                otherSheetId,
                SpatialRect(240, 80, 40, 40),
            ),
        )

        val result = SpatialRouteCompiler().compile(
            projection = projection.copy(sheets = projection.sheets + repeatedSheet),
            sheets = listOf(
                SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200)),
                SpatialRoutingSheetInput(otherSheetId, SpatialRect(0, 0, 320, 200)),
            ),
            occurrences = routingOccurrences() + otherOccurrences,
            anchors = routingAnchors() + otherAnchors,
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(SHEET_ID, result.routes.single().sheetId)
    }

    @Test
    fun `connection cannot route through foreign only Anchors`() {
        val otherSheetId = "engineering-projection/sheet/02-foreign"
        val foreignAnchors = listOf(
            anchor(
                "projection/node/component:Supply",
                "port:Supply.L1",
                SpatialBoundarySide.RIGHT,
                SpatialPoint(80, 100),
                otherSheetId,
            ),
            anchor(
                "projection/node/component:Q1",
                "port:Q1.1",
                SpatialBoundarySide.LEFT,
                SpatialPoint(240, 100),
                otherSheetId,
            ),
        )
        val foreignOccurrences = listOf(
            occurrence(
                "projection/node/component:Supply",
                "component:Supply",
                otherSheetId,
                SpatialRect(40, 80, 40, 40),
            ),
            occurrence(
                "projection/node/component:Q1",
                "component:Q1",
                otherSheetId,
                SpatialRect(240, 80, 40, 40),
            ),
        )

        val result = SpatialRouteCompiler().compile(
            projection = projectionDocument(),
            sheets = listOf(
                SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200)),
                SpatialRoutingSheetInput(otherSheetId, SpatialRect(0, 0, 320, 200)),
            ),
            occurrences = foreignOccurrences,
            anchors = foreignAnchors,
        )

        assertTrue(result.routes.isEmpty())
        assertTrue(result.lanes.isEmpty())
        assertEquals(
            listOf(
                "has no resolved source Anchor on owning Sheet $SHEET_ID; " +
                    "matching Anchor resolves on Sheet $otherSheetId",
                "has no resolved target Anchor on owning Sheet $SHEET_ID; " +
                    "matching Anchor resolves on Sheet $otherSheetId",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.problem },
        )
        assertTrue(result.diagnostics.all { diagnostic ->
            diagnostic.sourceTrace.projectionIds.first() == SHEET_ID &&
                diagnostic.sourceTrace.projectionIds.last() == otherSheetId
        })
    }

    @Test
    fun `missing or duplicate visible obstacle geometry fails closed`() {
        val base = projectionDocument()
        val blocker = node("projection/node/component:Blocker", "component:Blocker")
        val projection = base.copy(
            nodes = base.nodes + blocker,
            sheets = base.sheets.map { sheet ->
                sheet.copy(
                    subjects = sheet.subjects + ProjectionSheetSubject(
                        blocker.semanticId,
                        nodeIds = listOf(blocker.projectionId),
                    ),
                )
            },
        )
        val sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200)))
        val blockerGeometry = occurrence(
            blocker.projectionId.value,
            blocker.semanticId.value,
            SHEET_ID,
            SpatialRect(280, 140, 20, 20),
        )

        val missing = SpatialRouteCompiler().compile(projection, sheets, routingOccurrences(), routingAnchors())
        val duplicate = SpatialRouteCompiler().compile(
            projection,
            sheets,
            routingOccurrences() + blockerGeometry + blockerGeometry.copy(),
            routingAnchors(),
        )

        assertTrue(missing.routes.isEmpty())
        assertContains(missing.diagnostics.single().problem, "missing Occurrence geometry")
        assertTrue(duplicate.routes.isEmpty())
        assertContains(duplicate.diagnostics.single().problem, "duplicate Occurrence geometry")
    }

    @Test
    fun `validator rejects equal count Route facts with false Connection identity`() {
        val valid = compileRoute()
        val route = valid.routes.single()
        val unknownRouteId = SpatialRouteId(SHEET_ID, "projection/connection/connection:unknown")
        val mutations = listOf(
            listOf(route.copy(connectionId = StableSemanticIdentity("connection:wrong"))) to valid.lanes,
            listOf(route.copy(routeId = unknownRouteId)) to
                listOf(valid.lanes.single().copy(routeIds = listOf(unknownRouteId))),
        )

        mutations.forEach { (routes, lanes) ->
            val diagnostics = SpatialRouteValidator().validate(
                projectionDocument(),
                listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
                routingOccurrences(),
                routingAnchors(),
                routes,
                lanes,
            )
            assertTrue(diagnostics.any { diagnostic ->
                diagnostic.problem.contains("does not match exact Projection Connection identity")
            })
        }
    }

    @Test
    fun `validator rejects swapped valid Anchors that contradict Projection endpoint roles`() {
        val projection = projectionDocument()
        val valid = compileRoute()
        val route = valid.routes.single()
        val source = routingAnchors().first()
        val target = routingAnchors().last()
        val swapped = route.copy(
            sourceAnchorId = target.anchorId,
            targetAnchorId = source.anchorId,
            sourceTrace = canonicalRouteSourceTrace(
                SHEET_ID,
                projection.connections.single(),
                target,
                source,
            ),
            points = route.points.reversed(),
        )

        val diagnostics = SpatialRouteValidator().validate(
            projection,
            listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
            routingOccurrences(),
            routingAnchors(),
            listOf(swapped),
            valid.lanes,
        )

        assertContains(
            diagnostics.map { diagnostic -> diagnostic.problem },
            "typed endpoint Anchors do not match the Projection Connection source and target occurrence-ports",
        )
    }

    @Test
    fun `validator rejects every incomplete or reordered Route source trace component`() {
        val valid = compileRoute()
        val route = valid.routes.single()
        val projectionIds = route.sourceTrace.projectionIds
        val geometryIds = route.sourceTrace.geometryElementIds
        val mutations = buildList {
            projectionIds.indices.forEach { missingIndex ->
                add(
                    SpatialSourceTrace(
                        projectionIds = projectionIds.filterIndexed { index, _ -> index != missingIndex },
                        geometryElementIds = geometryIds,
                    ),
                )
            }
            add(SpatialSourceTrace(projectionIds.reversed(), geometryIds))
            add(SpatialSourceTrace(projectionIds + "projection/node/component:Extra", geometryIds))
            add(SpatialSourceTrace(projectionIds + projectionIds.last(), geometryIds))
            add(
                SpatialSourceTrace(
                    projectionIds.mapIndexed { index, id ->
                        if (index == 2) "projection/node/component:Foreign" else id
                    },
                    geometryIds,
                ),
            )
            geometryIds.indices.forEach { missingIndex ->
                add(
                    SpatialSourceTrace(
                        projectionIds = projectionIds,
                        geometryElementIds = geometryIds.filterIndexed { index, _ -> index != missingIndex },
                    ),
                )
            }
            add(SpatialSourceTrace(projectionIds, geometryIds.reversed()))
            add(SpatialSourceTrace(projectionIds, geometryIds + GeometryElementId("geometry:extra")))
            add(SpatialSourceTrace(projectionIds, geometryIds + geometryIds.last()))
            add(
                SpatialSourceTrace(
                    projectionIds,
                    geometryIds.mapIndexed { index, id ->
                        if (index == 0) GeometryElementId("geometry:foreign") else id
                    },
                ),
            )
        }

        mutations.forEach { sourceTrace ->
            val diagnostics = SpatialRouteValidator().validate(
                projectionDocument(),
                listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
                routingOccurrences(),
                routingAnchors(),
                listOf(route.copy(sourceTrace = sourceTrace)),
                valid.lanes,
            )

            assertTrue(diagnostics.any { diagnostic ->
                diagnostic.problem == "Source Trace does not match its exact Connection and endpoint Anchor derivation"
            })
        }
    }

    @Test
    fun `validator rejects Lane channel unrelated to assigned Route geometry`() {
        val valid = compileRoute()
        val falseLaneId = SpatialLaneId(SHEET_ID, SpatialLaneOrientation.VERTICAL, 999)
        val route = valid.routes.single().copy(laneId = falseLaneId)
        val lane = valid.lanes.single().copy(
            laneId = falseLaneId,
            orientation = falseLaneId.orientation,
            coordinate = falseLaneId.coordinate,
        )

        val diagnostics = SpatialRouteValidator().validate(
            projectionDocument(),
            listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
            routingOccurrences(),
            routingAnchors(),
            listOf(route),
            listOf(lane),
        )

        assertTrue(diagnostics.any { diagnostic ->
            diagnostic.problem.contains("does not describe the Route's canonical basic channel")
        })
    }

    @Test
    fun `spatial route compiler consumes exact anchors and publishes typed Lane`() {
        val result = compileRoute()

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(
                "lane:sheet=engineering-projection%2Fsheet%2F01-main:" +
                    "orientation=horizontal:coordinate=100",
            ),
            result.lanes.map { lane -> lane.laneId.value },
        )
        assertEquals(1, result.routes.size)
    }

    @Test
    fun `route endpoints are exact typed anchor points and identities`() {
        val source = routingAnchors()[0]
        val target = routingAnchors()[1]
        val route = compileRoute(listOf(source, target)).routes.single()

        assertEquals(source.anchorId, route.sourceAnchorId)
        assertEquals(target.anchorId, route.targetAnchorId)
        assertEquals(source.point, route.points.first())
        assertEquals(target.point, route.points.last())
        assertEquals("connection:Supply.L1-to-Q1.1", route.connectionId.value)
        assertEquals(
            "lane:sheet=engineering-projection%2Fsheet%2F01-main:orientation=horizontal:coordinate=100",
            route.laneId.value,
        )
        assertEquals(
            SpatialSourceTrace(
                projectionIds = listOf(
                    SHEET_ID,
                    "projection/connection/connection:Supply.L1-to-Q1.1",
                    "projection/node/component:Supply",
                    "port:Supply.L1",
                    "projection/node/component:Q1",
                    "port:Q1.1",
                ),
                geometryElementIds = listOf(
                    GeometryElementId("geometry:projection/node/component:Q1:port:Q1.1"),
                    GeometryElementId("geometry:projection/node/component:Supply:port:Supply.L1"),
                    GeometryElementId("origin:connection"),
                ),
            ),
            route.sourceTrace,
        )
    }

    @Test
    fun `Route trace preserves repeated required endpoint identities in role order`() {
        val occurrenceId = "projection/node/component:Loop"
        val source = anchor(
            occurrenceId,
            "port:Loop.out",
            SpatialBoundarySide.RIGHT,
            SpatialPoint(80, 90),
        )
        val target = anchor(
            occurrenceId,
            "port:Loop.in",
            SpatialBoundarySide.LEFT,
            SpatialPoint(40, 110),
        )
        val connection = projectionDocument().connections.single()

        assertEquals(
            listOf(
                SHEET_ID,
                connection.projectionId.value,
                occurrenceId,
                "port:Loop.out",
                occurrenceId,
                "port:Loop.in",
            ),
            canonicalRouteSourceTrace(SHEET_ID, connection, source, target).projectionIds,
        )
    }

    @Test
    fun `missing referenced anchor fails instead of dropping route`() {
        val result = compileRoute(routingAnchors().take(1))

        assertEndpointResolutionFailure(
            result = result,
            problem = "has no resolved target Anchor",
            occurrenceId = "projection/node/component:Q1",
            portId = "port:Q1.1",
            geometryIds = listOf("origin:connection", "origin:port:Q1.1"),
        )
    }

    @Test
    fun `missing source anchor fails with complete endpoint trace`() {
        val result = compileRoute(routingAnchors().drop(1))

        assertEndpointResolutionFailure(
            result = result,
            problem = "has no resolved source Anchor",
            occurrenceId = "projection/node/component:Supply",
            portId = "port:Supply.L1",
            geometryIds = listOf("origin:connection", "origin:port:Supply.L1"),
        )
    }

    @Test
    fun `duplicate source anchors fail with complete endpoint trace`() {
        val source = routingAnchors().first()
        val result = compileRoute(routingAnchors() + source.copy())

        assertEndpointResolutionFailure(
            result = result,
            problem = "has 2 resolved source Anchors on owning Sheet $SHEET_ID",
            occurrenceId = "projection/node/component:Supply",
            portId = "port:Supply.L1",
            geometryIds = listOf(
                "geometry:projection/node/component:Supply:port:Supply.L1",
                "origin:connection",
                "origin:port:Supply.L1",
            ),
        )
    }

    @Test
    fun `duplicate target anchors fail with complete endpoint trace`() {
        val target = routingAnchors().last()
        val result = compileRoute(routingAnchors() + target.copy())

        assertEndpointResolutionFailure(
            result = result,
            problem = "has 2 resolved target Anchors on owning Sheet $SHEET_ID",
            occurrenceId = "projection/node/component:Q1",
            portId = "port:Q1.1",
            geometryIds = listOf(
                "geometry:projection/node/component:Q1:port:Q1.1",
                "origin:connection",
                "origin:port:Q1.1",
            ),
        )
    }

    @Test
    fun `projection models do not own route endpoint geometry`() {
        val projectionPropertyNames = listOf(
            ProjectionDocument::class.java,
            ProjectionNode::class.java,
            ProjectionConnection::class.java,
            ProjectionOccurrencePort::class.java,
        ).flatMap { type -> type.declaredFields.map { field -> field.name } }
        val forbidden = listOf("routeStart", "routeEnd", "routePoints", "sourceAnchorId", "targetAnchorId", "x", "y", "side")

        forbidden.forEach { token ->
            assertFalse(
                projectionPropertyNames.any { name -> name.equals(token, ignoreCase = true) },
                "Projection model must not own route geometry field `$token`: $projectionPropertyNames",
            )
        }
    }

    @Test
    fun `new spatial route names avoid stale architecture terms`() {
        val names = listOf(
            SpatialRouteCompiler::class.simpleName.orEmpty(),
            SpatialRouteCompilationResult::class.simpleName.orEmpty(),
        )
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Spatial route names must not contain `$token`: $names",
            )
        }
    }

    private fun projectionDocument(): ProjectionDocument {
        val sourceNode = node("projection/node/component:Supply", "component:Supply")
        val targetNode = node("projection/node/component:Q1", "component:Q1")
        val sourcePort = port(sourceNode, "port:Supply.L1")
        val targetPort = port(targetNode, "port:Q1.1")
        val connection = ProjectionConnection(
            projectionId = ProjectionConnectionId("projection/connection/connection:Supply.L1-to-Q1.1"),
            semanticId = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
            originGeometryElementId = GeometryElementId("origin:connection"),
            source = ProjectionConnectionEndpoint(sourcePort.occurrencePortId),
            target = ProjectionConnectionEndpoint(targetPort.occurrencePortId),
        )
        val subjects = listOf(
            ProjectionSheetSubject(sourceNode.semanticId, nodeIds = listOf(sourceNode.projectionId)),
            ProjectionSheetSubject(targetNode.semanticId, nodeIds = listOf(targetNode.projectionId)),
            ProjectionSheetSubject(connection.semanticId, connectionIds = listOf(connection.projectionId)),
        )
        return ProjectionDocument(
            view = ViewDefinition(id = "engineering-projection", displayName = "Engineering Projection"),
            nodes = listOf(sourceNode, targetNode),
            occurrencePorts = listOf(sourcePort, targetPort),
            connections = listOf(connection),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = ProjectionSheetId(SHEET_ID),
                    displayName = "Engineering Projection Main",
                    order = 0,
                    subjects = subjects,
                ),
            ),
        )
    }

    private fun anchors(): List<SpatialAnchorPosition> = listOf(
        anchor(
            occurrenceId = "projection/node/component:Supply",
            portId = "port:Supply.L1",
            side = SpatialBoundarySide.RIGHT,
            point = SpatialPoint(80, 20),
        ),
        anchor(
            occurrenceId = "projection/node/component:Q1",
            portId = "port:Q1.1",
            side = SpatialBoundarySide.LEFT,
            point = SpatialPoint(140, 20),
        ),
    )

    private fun routingAnchors(targetSheetId: String = SHEET_ID): List<SpatialAnchorPosition> = listOf(
        anchor(
            occurrenceId = "projection/node/component:Supply",
            portId = "port:Supply.L1",
            side = SpatialBoundarySide.RIGHT,
            point = SpatialPoint(80, 100),
        ),
        anchor(
            occurrenceId = "projection/node/component:Q1",
            portId = "port:Q1.1",
            side = SpatialBoundarySide.LEFT,
            point = SpatialPoint(240, 100),
            sheetId = targetSheetId,
        ),
    )

    private fun routingOccurrences(targetSheetId: String = SHEET_ID): List<SpatialOccurrenceGeometry> = listOf(
        occurrence(
            projectionId = "projection/node/component:Supply",
            subjectId = "component:Supply",
            sheetId = SHEET_ID,
            rectangle = SpatialRect(40, 80, 40, 40),
        ),
        occurrence(
            projectionId = "projection/node/component:Q1",
            subjectId = "component:Q1",
            sheetId = targetSheetId,
            rectangle = SpatialRect(240, 80, 40, 40),
        ),
    )

    private fun compileRoute(
        anchors: List<SpatialAnchorPosition> = routingAnchors(),
    ): SpatialRouteCompilationResult = SpatialRouteCompiler().compile(
        projection = projectionDocument(),
        sheets = listOf(SpatialRoutingSheetInput(SHEET_ID, SpatialRect(0, 0, 320, 200))),
        occurrences = routingOccurrences(),
        anchors = anchors,
    )

    private fun assertEndpointResolutionFailure(
        result: SpatialRouteCompilationResult,
        problem: String,
        occurrenceId: String,
        portId: String,
        geometryIds: List<String>,
    ) {
        assertTrue(result.routes.isEmpty())
        assertTrue(result.lanes.isEmpty())
        assertEquals(1, result.diagnostics.size)
        val diagnostic = result.diagnostics.single()
        assertEquals("Connection projection/connection/connection:Supply.L1-to-Q1.1", diagnostic.subject)
        assertEquals(problem, diagnostic.problem)
        assertEquals(
            "Resolve port $portId on Occurrence $occurrenceId before routing this Connection.",
            diagnostic.correction,
        )
        assertEquals(
            listOf(
                SHEET_ID,
                "projection/connection/connection:Supply.L1-to-Q1.1",
                occurrenceId,
                portId,
            ),
            diagnostic.sourceTrace.projectionIds,
        )
        assertEquals(
            geometryIds,
            diagnostic.sourceTrace.geometryElementIds.map { geometryId -> geometryId.value },
        )
    }

    private fun occurrence(
        projectionId: String,
        subjectId: String,
        sheetId: String,
        rectangle: SpatialRect,
    ): SpatialOccurrenceGeometry = SpatialOccurrenceGeometry(
        occurrenceId = SpatialOccurrenceId(sheetId, projectionId),
        subjectId = StableSemanticIdentity(subjectId),
        sheetId = sheetId,
        regionId = "region:test",
        rectangle = rectangle,
        placementReason = SpatialPlacementReason(listOf("routing test")),
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, projectionId),
            geometryElementIds = listOf(GeometryElementId("geometry:$projectionId")),
        ),
    )

    private fun anchor(
        occurrenceId: String,
        portId: String,
        side: SpatialBoundarySide,
        point: SpatialPoint,
        sheetId: String = SHEET_ID,
    ): SpatialAnchorPosition {
        val occurrence = SpatialOccurrenceId(sheetId, occurrenceId)
        val port = StableSemanticIdentity(portId)
        return SpatialAnchorPosition(
            anchorId = SpatialAnchorId(sheetId, occurrence, port),
            sheetId = sheetId,
            subject = SpatialOccurrencePortSubject(occurrence, port),
            side = side,
            point = point,
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(sheetId, occurrenceId, portId),
                geometryElementIds = listOf(GeometryElementId("geometry:$occurrenceId:$portId")),
            ),
        )
    }

    private fun node(projectionId: String, semanticId: String): ProjectionNode = ProjectionNode(
        projectionId = ProjectionNodeId(projectionId),
        semanticId = StableSemanticIdentity(semanticId),
        label = semanticId,
        originGeometryElementId = GeometryElementId("origin:$projectionId"),
    )

    private fun port(node: ProjectionNode, semanticId: String): ProjectionOccurrencePort = ProjectionOccurrencePort(
        occurrencePortId = ProjectionOccurrencePortId(node.projectionId, StableSemanticIdentity(semanticId)),
        originGeometryElementId = GeometryElementId("origin:$semanticId"),
    )

    private companion object {
        const val SHEET_ID = "engineering-projection/sheet/01-main"
    }
}

private fun com.engineeringood.athena.spatial.SpatialRouteSegment.entersInterior(rectangle: SpatialRect): Boolean =
    when {
        start.y == end.y -> {
            start.y > rectangle.y && start.y < rectangle.bottom &&
                maxOf(start.x, end.x) > rectangle.x && minOf(start.x, end.x) < rectangle.right
        }
        start.x == end.x -> {
            start.x > rectangle.x && start.x < rectangle.right &&
                maxOf(start.y, end.y) > rectangle.y && minOf(start.y, end.y) < rectangle.bottom
        }
        else -> true
    }
