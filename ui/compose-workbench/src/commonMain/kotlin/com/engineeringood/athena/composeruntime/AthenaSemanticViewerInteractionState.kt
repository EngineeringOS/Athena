package com.engineeringood.athena.composeruntime

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Pixel viewport currently available to the semantic viewer surface.
 */
data class AthenaSemanticViewerViewport(
    val width: Int,
    val height: Int,
) {
    init {
        require(width >= 0) { "Viewport width must be non-negative." }
        require(height >= 0) { "Viewport height must be non-negative." }
    }
}

/**
 * One two-dimensional viewer point used for screen or world coordinates.
 */
data class AthenaSemanticViewerPoint(
    val x: Float,
    val y: Float,
)

/**
 * Disposable camera transform for the semantic viewer surface.
 */
data class AthenaSemanticViewerCamera(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val zoom: Float = 1f,
) {
    init {
        require(zoom > 0f) { "Viewer zoom must stay positive." }
    }

    /**
     * Converts one screen-space point into world-space viewer coordinates.
     */
    fun screenToWorld(
        screenX: Float,
        screenY: Float,
    ): AthenaSemanticViewerPoint {
        return AthenaSemanticViewerPoint(
            x = (screenX - offsetX) / zoom,
            y = (screenY - offsetY) / zoom,
        )
    }
}

/**
 * Current semantic object selection for viewer inspection.
 */
data class AthenaSemanticViewerSelection(
    val semanticId: String,
)

/**
 * Session-local interaction state for one semantic viewer surface.
 */
data class AthenaSemanticViewerInteractionState(
    val viewport: AthenaSemanticViewerViewport = AthenaSemanticViewerViewport(width = 0, height = 0),
    val camera: AthenaSemanticViewerCamera = AthenaSemanticViewerCamera(),
    val selection: AthenaSemanticViewerSelection? = null,
) {
    /**
     * Returns a copy with the latest viewport dimensions.
     */
    fun withViewport(
        width: Int,
        height: Int,
    ): AthenaSemanticViewerInteractionState {
        return copy(
            viewport = AthenaSemanticViewerViewport(
                width = width,
                height = height,
            ),
        )
    }

    /**
     * Pans the camera by the supplied screen-space delta.
     */
    fun panBy(
        deltaX: Float,
        deltaY: Float,
    ): AthenaSemanticViewerInteractionState {
        return copy(
            camera = camera.copy(
                offsetX = camera.offsetX + deltaX,
                offsetY = camera.offsetY + deltaY,
            ),
        )
    }

    /**
     * Zooms around one screen-space focus point while keeping that world point anchored.
     */
    fun zoomBy(
        focusX: Float,
        focusY: Float,
        zoomFactor: Float,
        minZoom: Float = MIN_ZOOM,
        maxZoom: Float = MAX_ZOOM,
    ): AthenaSemanticViewerInteractionState {
        require(zoomFactor > 0f) { "Zoom factor must stay positive." }

        val focusWorld = camera.screenToWorld(screenX = focusX, screenY = focusY)
        val nextZoom = (camera.zoom * zoomFactor).coerceIn(minimumValue = minZoom, maximumValue = maxZoom)
        return copy(
            camera = AthenaSemanticViewerCamera(
                offsetX = focusX - (focusWorld.x * nextZoom),
                offsetY = focusY - (focusWorld.y * nextZoom),
                zoom = nextZoom,
            ),
        )
    }

    /**
     * Updates selection from one screen-space pointer position.
     */
    fun selectAt(
        scene: AthenaSemanticViewerScene,
        screenX: Float,
        screenY: Float,
    ): AthenaSemanticViewerInteractionState {
        return copy(selection = hitTest(scene = scene, screenX = screenX, screenY = screenY))
    }

    /**
     * Clears any current selection while preserving viewport and camera.
     */
    fun clearSelection(): AthenaSemanticViewerInteractionState {
        return copy(selection = null)
    }

    /**
     * Resets the camera to the default untransformed view while preserving viewport and selection.
     */
    fun resetCamera(): AthenaSemanticViewerInteractionState {
        return copy(camera = AthenaSemanticViewerCamera())
    }

    /**
     * Performs deterministic hit-testing over the current semantic viewer scene.
     */
    fun hitTest(
        scene: AthenaSemanticViewerScene,
        screenX: Float,
        screenY: Float,
    ): AthenaSemanticViewerSelection? {
        val worldPoint = camera.screenToWorld(screenX = screenX, screenY = screenY)

        scene.components.firstOrNull { component ->
            worldPoint.x >= component.x &&
                worldPoint.x <= component.x + component.width &&
                worldPoint.y >= component.y &&
                worldPoint.y <= component.y + component.height
        }?.let { component ->
            return AthenaSemanticViewerSelection(semanticId = component.semanticId)
        }

        val connectionTolerance = CONNECTION_HIT_TOLERANCE / camera.zoom
        scene.connections.firstOrNull { connection ->
            distanceToSegment(
                pointX = worldPoint.x,
                pointY = worldPoint.y,
                startX = connection.x1.toFloat(),
                startY = connection.y1.toFloat(),
                endX = connection.x2.toFloat(),
                endY = connection.y2.toFloat(),
            ) <= connectionTolerance
        }?.let { connection ->
            return AthenaSemanticViewerSelection(semanticId = connection.semanticId)
        }

        return null
    }
}

private fun distanceToSegment(
    pointX: Float,
    pointY: Float,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
): Float {
    val deltaX = endX - startX
    val deltaY = endY - startY

    if (abs(deltaX) < EPSILON && abs(deltaY) < EPSILON) {
        return sqrt((pointX - startX).pow(2) + (pointY - startY).pow(2))
    }

    val projection = (
        ((pointX - startX) * deltaX) +
            ((pointY - startY) * deltaY)
        ) / ((deltaX * deltaX) + (deltaY * deltaY))
    val clampedProjection = projection.coerceIn(0f, 1f)
    val closestX = startX + (clampedProjection * deltaX)
    val closestY = startY + (clampedProjection * deltaY)
    return sqrt((pointX - closestX).pow(2) + (pointY - closestY).pow(2))
}

private const val EPSILON = 0.001f
private const val CONNECTION_HIT_TOLERANCE = 10f
private const val MIN_ZOOM = 0.5f
private const val MAX_ZOOM = 4f
