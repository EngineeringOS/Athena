package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance

enum class EndpointAttachmentKind {
    ANCHOR,
    TERMINAL,
    BUS,
    JUNCTION,
    SHEET_REFERENCE,
    FALLBACK,
}

data class EndpointProjectionTransform(
    val offsetX: Int = 0,
    val offsetY: Int = 0,
) {
    fun apply(point: SchematicRoutePoint): SchematicRoutePoint = SchematicRoutePoint(
        x = point.x + offsetX,
        y = point.y + offsetY,
    )
}

data class EndpointAttachmentFact(
    val endpointId: String,
    val kind: EndpointAttachmentKind,
    val point: SchematicRoutePoint,
    val provenance: SourceProvenance?,
) {
    init {
        require(endpointId.isNotBlank()) { "Endpoint attachment id must not be blank." }
    }

    companion object {
        fun fromAnchor(anchor: TerminalAnchorFact): EndpointAttachmentFact = EndpointAttachmentFact(
            endpointId = anchor.anchorId.value,
            kind = EndpointAttachmentKind.ANCHOR,
            point = anchor.gridPoint,
            provenance = null,
        )
    }
}

data class EndpointAnnotationLine(
    val annotationId: String,
    val start: SchematicRoutePoint,
    val end: SchematicRoutePoint,
    val provenance: SourceProvenance,
) {
    init {
        require(annotationId.isNotBlank()) { "Endpoint annotation id must not be blank." }
        require(start != end) { "Endpoint annotation line must not be zero-length." }
    }
}

data class EndpointAttachmentRequest(
    val routeFact: RouteFact?,
    val sourceCandidates: List<EndpointAttachmentFact>,
    val targetCandidates: List<EndpointAttachmentFact>,
    val transform: EndpointProjectionTransform,
    val componentBounds: List<SchematicComponentBounds>,
    val annotation: EndpointAnnotationLine?,
) {
    companion object {
        fun engineeringRoute(
            routeFact: RouteFact,
            sourceCandidates: List<EndpointAttachmentFact>,
            targetCandidates: List<EndpointAttachmentFact>,
            transform: EndpointProjectionTransform = EndpointProjectionTransform(),
            componentBounds: List<SchematicComponentBounds> = emptyList(),
        ): EndpointAttachmentRequest = EndpointAttachmentRequest(
            routeFact = routeFact,
            sourceCandidates = sourceCandidates,
            targetCandidates = targetCandidates,
            transform = transform,
            componentBounds = componentBounds,
            annotation = null,
        )

        fun annotationLine(
            annotationId: String,
            start: SchematicRoutePoint,
            end: SchematicRoutePoint,
            provenance: SourceProvenance,
        ): EndpointAttachmentRequest = EndpointAttachmentRequest(
            routeFact = null,
            sourceCandidates = emptyList(),
            targetCandidates = emptyList(),
            transform = EndpointProjectionTransform(),
            componentBounds = emptyList(),
            annotation = EndpointAnnotationLine(annotationId, start, end, provenance),
        )
    }
}

data class ResolvedEndpointAttachment(
    val endpointId: String,
    val kind: EndpointAttachmentKind,
    val point: SchematicRoutePoint,
    val renderedPoint: SchematicRoutePoint,
)

data class RouteEndpointAttachment(
    val routeId: SchematicRouteId,
    val connectionId: ElectricalConnectionId,
    val source: ResolvedEndpointAttachment,
    val target: ResolvedEndpointAttachment,
    val compilerSnapshotId: String,
    val provenance: SourceProvenance,
)

data class EndpointAttachmentDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val affectedRouteIds: List<String>,
    val provenance: SourceProvenance?,
) {
    init {
        require(code.isNotBlank()) { "Endpoint attachment diagnostic code must not be blank." }
        require(subject.isNotBlank()) { "Endpoint attachment diagnostic subject must not be blank." }
        require(message.isNotBlank()) { "Endpoint attachment diagnostic message must not be blank." }
    }
}

data class EndpointAttachmentResult(
    val attachments: List<RouteEndpointAttachment>,
    val annotations: List<EndpointAnnotationLine>,
    val diagnostics: List<EndpointAttachmentDiagnostic>,
) {
    val successful: Boolean
        get() = diagnostics.isEmpty()
}

data class EndpointAttachmentPayloadLine(
    val routeId: String,
    val connectionId: String,
    val sourceEndpointId: String,
    val targetEndpointId: String,
    val sourcePoint: SchematicRoutePoint,
    val targetPoint: SchematicRoutePoint,
    val compilerSnapshotId: String,
)

data class EndpointAttachmentPayload(
    val authority: String,
    val attachments: List<EndpointAttachmentPayloadLine>,
    val rendererRepairs: List<String> = emptyList(),
    val rawGeometryFragments: List<String> = emptyList(),
)

class EndpointAttachmentValidator {
    fun validate(requests: List<EndpointAttachmentRequest>): EndpointAttachmentResult {
        val attachments = mutableListOf<RouteEndpointAttachment>()
        val annotations = mutableListOf<EndpointAnnotationLine>()
        val diagnostics = mutableListOf<EndpointAttachmentDiagnostic>()
        requests.forEach { request ->
            val annotation = request.annotation
            if (annotation != null) {
                annotations += annotation
                return@forEach
            }
            val route = requireNotNull(request.routeFact) { "Endpoint attachment request requires route fact or annotation." }
            val routeDiagnostics = validateRoute(request, route)
            diagnostics += routeDiagnostics
            if (routeDiagnostics.isEmpty()) {
                attachments += route.toAttachment(
                    source = request.sourceCandidates.single(),
                    target = request.targetCandidates.single(),
                    transform = request.transform,
                )
            }
        }
        val sortedDiagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }))
        return EndpointAttachmentResult(
            attachments = if (sortedDiagnostics.isEmpty()) attachments.sortedBy { it.routeId.value } else emptyList(),
            annotations = annotations.sortedBy { it.annotationId },
            diagnostics = sortedDiagnostics,
        )
    }

    fun normalize(result: EndpointAttachmentResult): EndpointAttachmentPayload {
        return EndpointAttachmentPayload(
            authority = "athena",
            attachments = result.attachments.map { attachment ->
                EndpointAttachmentPayloadLine(
                    routeId = attachment.routeId.value,
                    connectionId = attachment.connectionId.value,
                    sourceEndpointId = attachment.source.endpointId,
                    targetEndpointId = attachment.target.endpointId,
                    sourcePoint = attachment.source.renderedPoint,
                    targetPoint = attachment.target.renderedPoint,
                    compilerSnapshotId = attachment.compilerSnapshotId,
                )
            },
        )
    }

    private fun validateRoute(
        request: EndpointAttachmentRequest,
        route: RouteFact,
    ): List<EndpointAttachmentDiagnostic> {
        val diagnostics = mutableListOf<EndpointAttachmentDiagnostic>()
        diagnostics += validateCandidateCount(route, "source", request.sourceCandidates)
        diagnostics += validateCandidateCount(route, "target", request.targetCandidates)
        val source = request.sourceCandidates.singleOrNull()
        val target = request.targetCandidates.singleOrNull()
        listOfNotNull(source, target).forEach { endpoint ->
            if (endpoint.kind == EndpointAttachmentKind.FALLBACK) {
                diagnostics += diagnostic(
                    code = "drawing.endpoint.fallback",
                    subject = endpoint.endpointId,
                    message = "Engineering endpoint cannot use fallback attachment.",
                    route = route,
                )
            }
            if (endpoint.isInsideAny(request.componentBounds)) {
                diagnostics += diagnostic(
                    code = "drawing.endpoint.body-interior",
                    subject = endpoint.endpointId,
                    message = "Engineering endpoint cannot attach to component body interior.",
                    route = route,
                )
            }
        }
        if (source != null && source.point != route.segments.first().start) {
            diagnostics += diagnostic(
                code = "drawing.endpoint.detached",
                subject = source.endpointId,
                message = "Source endpoint does not equal first route segment coordinate.",
                route = route,
            )
        }
        if (target != null && target.point != route.segments.last().end) {
            diagnostics += diagnostic(
                code = "drawing.endpoint.detached",
                subject = target.endpointId,
                message = "Target endpoint does not equal last route segment coordinate.",
                route = route,
            )
        }
        return diagnostics
    }

    private fun validateCandidateCount(
        route: RouteFact,
        side: String,
        candidates: List<EndpointAttachmentFact>,
    ): List<EndpointAttachmentDiagnostic> = when (candidates.size) {
        0 -> listOf(
            diagnostic(
                code = "drawing.endpoint.unresolved",
                subject = "${route.routeId.value}:$side",
                message = "Engineering endpoint did not resolve to an attachment fact.",
                route = route,
            ),
        )
        1 -> emptyList()
        else -> listOf(
            diagnostic(
                code = "drawing.endpoint.ambiguous",
                subject = "${route.routeId.value}:$side",
                message = "Engineering endpoint resolved to more than one attachment fact.",
                route = route,
            ),
        )
    }

    private fun EndpointAttachmentFact.isInsideAny(bounds: List<SchematicComponentBounds>): Boolean {
        return bounds.any { bound ->
            point.x > bound.topLeft.x &&
                point.x < bound.topLeft.x + bound.width &&
                point.y > bound.topLeft.y &&
                point.y < bound.topLeft.y + bound.height
        }
    }

    private fun RouteFact.toAttachment(
        source: EndpointAttachmentFact,
        target: EndpointAttachmentFact,
        transform: EndpointProjectionTransform,
    ): RouteEndpointAttachment = RouteEndpointAttachment(
        routeId = routeId,
        connectionId = connectionId,
        source = source.resolve(transform),
        target = target.resolve(transform),
        compilerSnapshotId = compilerSnapshotId,
        provenance = provenance,
    )

    private fun EndpointAttachmentFact.resolve(transform: EndpointProjectionTransform): ResolvedEndpointAttachment =
        ResolvedEndpointAttachment(
            endpointId = endpointId,
            kind = kind,
            point = point,
            renderedPoint = transform.apply(point),
        )

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
        route: RouteFact,
    ): EndpointAttachmentDiagnostic = EndpointAttachmentDiagnostic(
        code = code,
        subject = subject,
        message = message,
        affectedRouteIds = listOf(route.routeId.value),
        provenance = route.provenance,
    )
}
