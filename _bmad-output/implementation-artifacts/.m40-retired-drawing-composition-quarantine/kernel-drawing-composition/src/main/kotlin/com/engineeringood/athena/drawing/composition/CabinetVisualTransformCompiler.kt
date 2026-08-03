package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalInstallationOrientation

object CabinetVisualTransformCompiler {
    fun compile(
        physicalOccurrences: List<CabinetPhysicalOccurrenceInput>,
        representationOccurrences: List<CabinetRepresentationOccurrenceInput>,
        enclosureToDrawing: CabinetTargetFrame,
    ): CabinetVisualTransformCompilation {
        val diagnostics = mutableListOf<CabinetVisualTransformDiagnostic>()
        val physicalByKey = physicalOccurrences.groupBy { occurrence -> occurrence.key }
        val representationByKey = representationOccurrences.groupBy { occurrence -> occurrence.key }
        val allKeys = (physicalByKey.keys + representationByKey.keys).sortedBy { key -> key.sortKey() }

        allKeys.forEach { key ->
            val physicalCount = physicalByKey[key].orEmpty().size
            val representationCount = representationByKey[key].orEmpty().size
            if (physicalCount == 0) {
                diagnostics += diagnostic("cabinet.join.missing_physical", key, "one physical occurrence is required")
            }
            if (physicalCount > 1) {
                diagnostics += diagnostic("cabinet.join.duplicate_physical", key, "one physical occurrence is required")
            }
            if (representationCount == 0) {
                diagnostics += diagnostic(
                    "cabinet.join.missing_representation",
                    key,
                    "one representation occurrence is required",
                )
            }
            if (representationCount > 1) {
                diagnostics += diagnostic(
                    "cabinet.join.duplicate_representation",
                    key,
                    "one representation occurrence is required",
                )
            }
        }

        if (diagnostics.isNotEmpty()) {
            return CabinetVisualTransformCompilation.Failure(
                diagnostics.sortedWith(compareBy({ it.code }, { it.subject })),
            )
        }

        return CabinetVisualTransformCompilation.Success(
            allKeys.map { key ->
                val physical = physicalByKey.getValue(key).single()
                val representation = representationByKey.getValue(key).single()
                val transform = CabinetVisualTransform(
                    id = CabinetTransformId("cabinet-transform:${key.canonicalSemanticSubjectId.value}"),
                    key = key,
                    targetFrame = physical.targetFrame,
                    intrinsicBounds = representation.intrinsicBounds,
                    footprint = physical.footprint,
                    orientation = physical.orientation,
                )
                val transformedBody = transformBody(
                    transform = transform,
                    targetLocalPosition = physical.targetLocalPosition,
                    enclosureToDrawing = enclosureToDrawing,
                )
                CabinetOccurrenceVisualJoin(
                    key = key,
                    physicalOccurrenceId = physical.occurrenceId,
                    representationOccurrenceId = representation.representationOccurrenceId,
                    transform = transform,
                    body = CabinetTransformedBody(transform.id, transformedBody),
                    anchors = representation.anchors.map { anchor ->
                        CabinetTransformedAnchor(
                            id = anchor.id,
                            transformId = transform.id,
                            point = transformPoint(
                                point = anchor.point,
                                transform = transform,
                                targetLocalPosition = physical.targetLocalPosition,
                                enclosureToDrawing = enclosureToDrawing,
                            ),
                        )
                    },
                )
            },
        )
    }
}

private fun transformBody(
    transform: CabinetVisualTransform,
    targetLocalPosition: CabinetPointD,
    enclosureToDrawing: CabinetTargetFrame,
): CabinetRectD {
    val corners = listOf(
        CabinetPointD(transform.intrinsicBounds.x, transform.intrinsicBounds.y),
        CabinetPointD(transform.intrinsicBounds.right, transform.intrinsicBounds.y),
        CabinetPointD(transform.intrinsicBounds.right, transform.intrinsicBounds.bottom),
        CabinetPointD(transform.intrinsicBounds.x, transform.intrinsicBounds.bottom),
    ).map { point ->
        transformPoint(point, transform, targetLocalPosition, enclosureToDrawing)
    }
    return corners.bounds()
}

private fun transformPoint(
    point: CabinetPointD,
    transform: CabinetVisualTransform,
    targetLocalPosition: CabinetPointD,
    enclosureToDrawing: CabinetTargetFrame,
): CabinetPointD {
    val normalized = CabinetPointD(
        x = point.x - transform.intrinsicBounds.x,
        y = point.y - transform.intrinsicBounds.y,
    )
    val scale = minOf(
        transform.footprint.width / transform.intrinsicBounds.width,
        transform.footprint.height / transform.intrinsicBounds.height,
    )
    val scaledSize = CabinetSizeD(
        width = transform.intrinsicBounds.width * scale,
        height = transform.intrinsicBounds.height * scale,
    )
    val centered = CabinetPointD(
        x = normalized.x * scale + ((transform.footprint.width - scaledSize.width) / 2.0),
        y = normalized.y * scale + ((transform.footprint.height - scaledSize.height) / 2.0),
    )
    val rotated = rotateAroundFootprintCenter(centered, transform.footprint, transform.orientation)
    val rotatedBodyMinimum = rotatedBodyMinimum(transform.footprint, transform.orientation)
    val placed = CabinetPointD(
        x = rotated.x - rotatedBodyMinimum.x + targetLocalPosition.x,
        y = rotated.y - rotatedBodyMinimum.y + targetLocalPosition.y,
    )
    return enclosureToDrawing.apply(transform.targetFrame.apply(placed))
}

private fun rotateAroundFootprintCenter(
    point: CabinetPointD,
    footprint: CabinetSizeD,
    orientation: PhysicalInstallationOrientation,
): CabinetPointD {
    val center = CabinetPointD(footprint.width / 2.0, footprint.height / 2.0)
    val dx = point.x - center.x
    val dy = point.y - center.y
    return when (orientation) {
        PhysicalInstallationOrientation.Deg0 -> point
        PhysicalInstallationOrientation.Deg90 -> CabinetPointD(center.x + dy, center.y - dx)
        PhysicalInstallationOrientation.Deg180 -> CabinetPointD(center.x - dx, center.y - dy)
        PhysicalInstallationOrientation.Deg270 -> CabinetPointD(center.x - dy, center.y + dx)
    }
}

private fun rotatedBodyMinimum(
    footprint: CabinetSizeD,
    orientation: PhysicalInstallationOrientation,
): CabinetPointD {
    val corners = listOf(
        CabinetPointD(0.0, 0.0),
        CabinetPointD(footprint.width, 0.0),
        CabinetPointD(footprint.width, footprint.height),
        CabinetPointD(0.0, footprint.height),
    ).map { corner -> rotateAroundFootprintCenter(corner, footprint, orientation) }
    return CabinetPointD(
        x = corners.minOf { it.x },
        y = corners.minOf { it.y },
    )
}

private fun CabinetTargetFrame.apply(point: CabinetPointD): CabinetPointD = CabinetPointD(
    x = origin.x + (alongAxis.x * point.x) + (normalAxis.x * point.y),
    y = origin.y + (alongAxis.y * point.x) + (normalAxis.y * point.y),
)

private fun List<CabinetPointD>.bounds(): CabinetRectD {
    val minX = minOf { it.x }
    val minY = minOf { it.y }
    val maxX = maxOf { it.x }
    val maxY = maxOf { it.y }
    return CabinetRectD(minX, minY, maxX - minX, maxY - minY)
}

private fun InstallationOccurrenceKey.sortKey(): String =
    "${sourceUnitId.value}:${installationId.value}:${canonicalSemanticSubjectId.value}"

private fun diagnostic(
    code: String,
    key: InstallationOccurrenceKey,
    message: String,
): CabinetVisualTransformDiagnostic = CabinetVisualTransformDiagnostic(
    code = code,
    subject = key.sortKey(),
    message = message,
)
