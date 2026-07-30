package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalMountingSurface
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalRail
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalTerminalGroup
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicMarkerKind
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleTokenId

data class CabinetCompositionPolicy(
    val documentId: String,
    val padding: Double,
)

data class CabinetCompositionRequest(
    val ir: PhysicalInstallationIR,
    val joins: List<CabinetOccurrenceVisualJoin>,
    val routes: List<CabinetRouteFact> = emptyList(),
    val policy: CabinetCompositionPolicy,
)

data class CabinetCompositionEvidence(
    val primitiveCount: Int,
    val boundsAuthority: String,
    val primitiveIds: List<String>,
)

data class CabinetCompositionDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

sealed interface CabinetCompositionResult {
    data class Success(
        val document: GraphicPrimitiveDocument,
        val evidence: CabinetCompositionEvidence,
    ) : CabinetCompositionResult

    data class Failure(val diagnostics: List<CabinetCompositionDiagnostic>) : CabinetCompositionResult
}

object CabinetCompositionCompiler {
    fun compile(request: CabinetCompositionRequest): CabinetCompositionResult {
        val diagnostics = validateJoins(request.ir, request.joins)
        if (diagnostics.isNotEmpty()) {
            return CabinetCompositionResult.Failure(diagnostics)
        }

        val primitives = mutableListOf<GraphicPrimitive>()
        val contentBounds = mutableListOf<GraphicBounds>()
        val enclosure = request.ir.space.enclosure
        val enclosureBounds = GraphicBounds(0.0, 0.0, enclosure.size.width.toDouble(), enclosure.size.height.toDouble())
        contentBounds += enclosureBounds

        val surfaceById = request.ir.space.surfaces.associateBy { surface -> surface.id }
        val ductById = request.ir.space.ducts.associateBy { duct -> duct.id }
        val mountedByKey = request.ir.space.mountedOccurrences.associateBy { occurrence -> occurrence.key }

        request.ir.space.surfaces.sortedBy { it.id.value }.forEach { surface ->
            contentBounds += surface.bounds()
        }
        request.ir.space.rails.sortedBy { it.id.value }.forEach { rail ->
            val bounds = rail.bounds(surfaceById)
            contentBounds += bounds
        }
        request.ir.space.ducts.sortedBy { it.id.value }.forEach { duct ->
            contentBounds += duct.bounds()
        }
        request.ir.space.channels.sortedBy { it.id.value }.forEach { channel ->
            contentBounds += channel.bounds(ductById)
        }
        request.ir.space.terminalGroups.sortedBy { it.id.value }.forEach { group ->
            contentBounds += group.bounds()
        }
        request.routes.sortedBy { route -> route.connectionAlias }.forEach { route ->
            contentBounds += route.routeBounds()
            contentBounds += markerBounds(route.from.point)
            contentBounds += markerBounds(route.to.point)
        }
        request.joins.sortedBy { join -> join.key.sortKey() }.forEach { join ->
            contentBounds += join.body.bounds.toGraphicBounds()
            contentBounds += labelBounds(join.body.bounds)
        }

        val documentBounds = contentBounds.union().inflate(request.policy.padding)
        primitives += rectangle("frame:${request.policy.documentId}", documentBounds, "cabinet.frame")
        primitives += rectangle("enclosure:${enclosure.id.value}", enclosureBounds, "cabinet.enclosure")
        primitives += request.ir.space.surfaces.sortedBy { it.id.value }.map { surface ->
            rectangle("surface:${surface.id.value}", surface.bounds(), "cabinet.surface")
        }
        primitives += request.ir.space.rails.sortedBy { it.id.value }.map { rail ->
            line("rail:${rail.id.value}", rail.bounds(surfaceById), "cabinet.rail")
        }
        primitives += request.ir.space.ducts.sortedBy { it.id.value }.map { duct ->
            rectangle("duct:${duct.id.value}", duct.bounds(), "cabinet.duct")
        }
        primitives += request.ir.space.channels.sortedBy { it.id.value }.map { channel ->
            rectangle("channel:${channel.id.value}", channel.bounds(ductById), "cabinet.channel")
        }
        primitives += request.ir.space.terminalGroups.sortedBy { it.id.value }.map { group ->
            rectangle("terminal-group:${group.id.value}", group.bounds(), "cabinet.terminal-group")
        }
        request.routes.sortedBy { route -> route.connectionAlias }.forEach { route ->
            primitives += polyline(
                id = "route:${route.connectionAlias}",
                points = route.routePoints(),
                style = "cabinet.route",
            )
            primitives += marker(
                id = "route-endpoint:${route.connectionAlias}:source",
                point = route.from.point,
                style = "cabinet.route-endpoint",
            )
            primitives += marker(
                id = "route-endpoint:${route.connectionAlias}:target",
                point = route.to.point,
                style = "cabinet.route-endpoint",
            )
        }
        routeCrossings(request.routes).forEachIndexed { index, point ->
            contentBounds += markerBounds(point)
            primitives += marker(
                id = "route-crossing:${request.policy.documentId}:$index",
                point = point,
                style = "cabinet.route-crossing",
                kind = GraphicMarkerKind.CROSSING,
            )
        }
        request.joins.sortedBy { join -> join.key.sortKey() }.forEach { join ->
            val occurrenceId = mountedByKey.getValue(join.key).occurrenceId.value
            primitives += rectangle("mounted-body:$occurrenceId", join.body.bounds.toGraphicBounds(), "cabinet.mounted")
            primitives += text(
                id = "mounted-label:$occurrenceId",
                bounds = labelBounds(join.body.bounds),
                text = occurrenceId,
                style = "cabinet.label",
            )
        }

        val document = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(request.policy.documentId),
            bounds = documentBounds,
            primitives = primitives,
            styleTokens = emptyList(),
            provenanceSources = listOf(
                "physical-installation-ir",
                "cabinet-occurrence-visual-join",
                "cabinet-composition-compiler",
            ),
            forbiddenAuthorityClaims = emptySet(),
        )
        return CabinetCompositionResult.Success(
            document = document,
            evidence = CabinetCompositionEvidence(
                primitiveCount = primitives.size,
                boundsAuthority = if (request.routes.isEmpty()) {
                    "physical-ir+joined-representation"
                } else {
                    "physical-ir+joined-representation+route-facts"
                },
                primitiveIds = primitives.map { primitive -> primitive.primitiveId.value },
            ),
        )
    }
}

private fun validateJoins(
    ir: PhysicalInstallationIR,
    joins: List<CabinetOccurrenceVisualJoin>,
): List<CabinetCompositionDiagnostic> {
    val mountedKeys = ir.space.mountedOccurrences.map { occurrence -> occurrence.key }.toSet()
    return joins
        .filterNot { join -> join.key in mountedKeys }
        .map { join ->
            CabinetCompositionDiagnostic(
                code = "cabinet.composition.join_without_physical",
                subject = join.key.sortKey(),
                message = "Cabinet join must reference one mounted physical occurrence.",
            )
        }
}

private fun PhysicalMountingSurface.bounds(): GraphicBounds = GraphicBounds(
    x = at.x.toDouble(),
    y = at.y.toDouble(),
    width = size.width.toDouble(),
    height = size.height.toDouble(),
)

private fun PhysicalRail.bounds(surfaces: Map<PhysicalObjectId, PhysicalMountingSurface>): GraphicBounds {
    val surface = surfaces.getValue(surfaceId)
    val x = surface.at.x + at.x
    val y = surface.at.y + at.y
    return when (orientation) {
        com.engineeringood.athena.physical.PhysicalInfrastructureOrientation.Horizontal ->
            GraphicBounds(x.toDouble(), y.toDouble(), length.value.toDouble(), 0.0)
        com.engineeringood.athena.physical.PhysicalInfrastructureOrientation.Vertical ->
            GraphicBounds(x.toDouble(), y.toDouble(), 0.0, length.value.toDouble())
    }
}

private fun PhysicalDuct.bounds(): GraphicBounds = GraphicBounds(
    x = at.x.toDouble(),
    y = at.y.toDouble(),
    width = size.width.toDouble(),
    height = size.height.toDouble(),
)

private fun PhysicalRouteChannel.bounds(ducts: Map<PhysicalObjectId, PhysicalDuct>): GraphicBounds {
    val duct = ducts.getValue(ductId)
    val offset = duct.wall.value
    return GraphicBounds(
        x = (duct.at.x + offset + at.x).toDouble(),
        y = (duct.at.y + offset + at.y).toDouble(),
        width = size.width.toDouble(),
        height = size.height.toDouble(),
    )
}

private fun PhysicalTerminalGroup.bounds(): GraphicBounds = GraphicBounds(
    x = at.x.toDouble(),
    y = at.y.toDouble(),
    width = size.width.toDouble(),
    height = size.height.toDouble(),
)

private fun rectangle(id: String, bounds: GraphicBounds, style: String): GraphicPrimitive.Rectangle =
    GraphicPrimitive.Rectangle(
        primitiveId = GraphicPrimitiveId(id),
        bounds = bounds,
        cornerRadius = 0.0,
        styleTokenId = GraphicStyleTokenId(style),
    )

private fun line(id: String, bounds: GraphicBounds, style: String): GraphicPrimitive.Line =
    GraphicPrimitive.Line(
        primitiveId = GraphicPrimitiveId(id),
        bounds = bounds,
        start = GraphicPoint(bounds.x, bounds.y),
        end = GraphicPoint(bounds.right, bounds.bottom),
        styleTokenId = GraphicStyleTokenId(style),
    )

private fun polyline(id: String, points: List<GraphicPoint>, style: String): GraphicPrimitive.Polyline =
    GraphicPrimitive.Polyline(
        primitiveId = GraphicPrimitiveId(id),
        bounds = points.bounds(),
        points = points,
        styleTokenId = GraphicStyleTokenId(style),
    )

private fun marker(
    id: String,
    point: CabinetPointD,
    style: String,
    kind: GraphicMarkerKind = GraphicMarkerKind.TERMINAL,
): GraphicPrimitive.Marker =
    GraphicPrimitive.Marker(
        primitiveId = GraphicPrimitiveId(id),
        bounds = markerBounds(point),
        origin = GraphicPoint(point.x, point.y),
        markerKind = kind,
        styleTokenId = GraphicStyleTokenId(style),
    )

private fun text(id: String, bounds: GraphicBounds, text: String, style: String): GraphicPrimitive.Text =
    GraphicPrimitive.Text(
        primitiveId = GraphicPrimitiveId(id),
        bounds = bounds,
        origin = GraphicPoint(bounds.x, bounds.y + bounds.height),
        text = text,
        styleTokenId = GraphicStyleTokenId(style),
    )

private fun labelBounds(body: CabinetRectD): GraphicBounds = GraphicBounds(
    x = body.x,
    y = body.y - 12.0,
    width = body.width,
    height = 10.0,
)

private fun markerBounds(point: CabinetPointD): GraphicBounds = GraphicBounds(
    x = point.x - 3.0,
    y = point.y - 3.0,
    width = 6.0,
    height = 6.0,
)

private fun CabinetRouteFact.routePoints(): List<GraphicPoint> {
    val points = segments.flatMap { segment -> listOf(segment.from, segment.to) }
        .fold(emptyList<CabinetPointD>()) { acc, point ->
            if (acc.lastOrNull() == point) acc else acc + point
        }
    return points.map { point -> GraphicPoint(point.x, point.y) }
}

private fun CabinetRouteFact.routeBounds(): GraphicBounds = routePoints().bounds()

private fun routeCrossings(routes: List<CabinetRouteFact>): List<CabinetPointD> {
    val segmentsByRoute = routes.map { route -> route.connectionAlias to route.segments }
    val crossings = mutableListOf<CabinetPointD>()
    segmentsByRoute.forEachIndexed { routeIndex, (routeId, segments) ->
        segmentsByRoute.drop(routeIndex + 1).forEach { (otherRouteId, otherSegments) ->
            segments.forEach { segment ->
                otherSegments.forEach { other ->
                    val crossing = segment.crossingWith(other)
                    if (crossing != null && crossing !in crossings && routeId != otherRouteId) {
                        crossings += crossing
                    }
                }
            }
        }
    }
    return crossings.sortedWith(compareBy<CabinetPointD>({ it.y }, { it.x }))
}

private fun CabinetRouteSegment.crossingWith(other: CabinetRouteSegment): CabinetPointD? {
    val thisVertical = from.x == to.x
    val thisHorizontal = from.y == to.y
    val otherVertical = other.from.x == other.to.x
    val otherHorizontal = other.from.y == other.to.y
    if (thisVertical && otherHorizontal) {
        val point = CabinetPointD(from.x, other.from.y)
        return point.takeIf { it.isStrictlyInside(this) && it.isStrictlyInside(other) }
    }
    if (thisHorizontal && otherVertical) {
        val point = CabinetPointD(other.from.x, from.y)
        return point.takeIf { it.isStrictlyInside(this) && it.isStrictlyInside(other) }
    }
    return null
}

private fun CabinetPointD.isStrictlyInside(segment: CabinetRouteSegment): Boolean {
    val minX = minOf(segment.from.x, segment.to.x)
    val maxX = maxOf(segment.from.x, segment.to.x)
    val minY = minOf(segment.from.y, segment.to.y)
    val maxY = maxOf(segment.from.y, segment.to.y)
    return x > minX && x < maxX && y > minY && y < maxY
}

private fun List<GraphicPoint>.bounds(): GraphicBounds {
    val minX = minOf { point -> point.x }
    val minY = minOf { point -> point.y }
    val maxX = maxOf { point -> point.x }
    val maxY = maxOf { point -> point.y }
    return GraphicBounds(minX, minY, maxX - minX, maxY - minY)
}

private fun CabinetRectD.toGraphicBounds(): GraphicBounds = GraphicBounds(x, y, width, height)

private fun List<GraphicBounds>.union(): GraphicBounds {
    val minX = minOf { bounds -> bounds.x }
    val minY = minOf { bounds -> bounds.y }
    val maxX = maxOf { bounds -> bounds.right }
    val maxY = maxOf { bounds -> bounds.bottom }
    return GraphicBounds(minX, minY, maxX - minX, maxY - minY)
}

private fun GraphicBounds.inflate(amount: Double): GraphicBounds = GraphicBounds(
    x = x - amount,
    y = y - amount,
    width = width + (amount * 2.0),
    height = height + (amount * 2.0),
)

private val GraphicBounds.right: Double
    get() = x + width

private val GraphicBounds.bottom: Double
    get() = y + height

private fun InstallationOccurrenceKey.sortKey(): String =
    "${sourceUnitId.value}:${installationId.value}:${canonicalSemanticSubjectId.value}"
