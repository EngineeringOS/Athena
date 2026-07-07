package com.engineeringood.athena.composeruntime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Renders the shared semantic viewer stage with disposable viewport interaction state.
 */
@Composable
fun AthenaSemanticViewerStage(
    scene: AthenaSemanticViewerScene,
    selectedSemanticId: String? = null,
    onSelectionChanged: (String?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var interactionState by remember(scene) {
        mutableStateOf(
            AthenaSemanticViewerInteractionState(
                selection = selectedSemanticId?.let(::AthenaSemanticViewerSelection),
            ),
        )
    }
    LaunchedEffect(scene, selectedSemanticId) {
        val nextSelection = selectedSemanticId?.let(::AthenaSemanticViewerSelection)
        if (interactionState.selection != nextSelection) {
            interactionState = interactionState.copy(selection = nextSelection)
        }
    }
    val density = LocalDensity.current
    val connectionColor = MaterialTheme.colorScheme.primary
    val selectedConnectionColor = MaterialTheme.colorScheme.secondary
    val stageBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val componentSurfaceColor = MaterialTheme.colorScheme.surfaceBright
    val selectedComponentSurfaceColor = MaterialTheme.colorScheme.secondaryContainer
    val componentOutlineColor = MaterialTheme.colorScheme.outlineVariant
    val selectedComponentOutlineColor = MaterialTheme.colorScheme.secondary
    val stageWidth = remember(scene.canvasWidth, density) { with(density) { scene.canvasWidth.toDp() } }
    val stageHeight = remember(scene.canvasHeight, density) { with(density) { scene.canvasHeight.toDp() } }
    val selectedSemanticId = interactionState.selection?.semanticId
    val controlFocusX = if (interactionState.viewport.width > 0) {
        interactionState.viewport.width / 2f
    } else {
        scene.canvasWidth / 2f
    }
    val controlFocusY = if (interactionState.viewport.height > 0) {
        interactionState.viewport.height / 2f
    } else {
        scene.canvasHeight / 2f
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "System: ${scene.systemName}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Components: ${scene.componentCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Connections: ${scene.connectionCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ViewerStatusPill(
                text = "Zoom ${(interactionState.camera.zoom * 100f).roundToInt()}%",
            )
            ViewerStatusPill(
                text = "Selection ${selectedSemanticId ?: "None"}",
            )
            ViewerStatusPill(
                text = "Viewport ${interactionState.viewport.width}x${interactionState.viewport.height}",
            )
            OutlinedButton(
                onClick = {
                    interactionState = interactionState.zoomBy(
                        focusX = controlFocusX,
                        focusY = controlFocusY,
                        zoomFactor = 1f / ZOOM_STEP,
                    )
                },
                shape = CircleShape,
            ) {
                Text("-")
            }
            OutlinedButton(
                onClick = {
                    interactionState = interactionState.zoomBy(
                        focusX = controlFocusX,
                        focusY = controlFocusY,
                        zoomFactor = ZOOM_STEP,
                    )
                },
                shape = CircleShape,
            ) {
                Text("+")
            }
            OutlinedButton(
                onClick = {
                    interactionState = interactionState.resetCamera()
                },
            ) {
                Text("Reset View")
            }
        }
        Text(
            text = "Click to select. Drag to pan. Use the controls to zoom without changing semantic truth.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(stageBackgroundColor)
                .padding(16.dp)
                .onSizeChanged { size ->
                    val nextState = interactionState.withViewport(
                        width = size.width,
                        height = size.height,
                    )
                    if (nextState.viewport != interactionState.viewport) {
                        interactionState = nextState
                    }
                }
                .pointerInput(scene, interactionState.camera) {
                    detectTapGestures { offset ->
                        val nextState = interactionState.selectAt(
                            scene = scene,
                            screenX = offset.x,
                            screenY = offset.y,
                        )
                        interactionState = nextState
                        onSelectionChanged(nextState.selection?.semanticId)
                    }
                }
                .pointerInput(scene, interactionState.camera) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        interactionState = interactionState.panBy(
                            deltaX = dragAmount.x,
                            deltaY = dragAmount.y,
                        )
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = interactionState.camera.zoom
                        scaleY = interactionState.camera.zoom
                        translationX = interactionState.camera.offsetX
                        translationY = interactionState.camera.offsetY
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .width(stageWidth)
                    .height(stageHeight),
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    scene.connections.forEach { connection ->
                        drawLine(
                            color = if (connection.semanticId == selectedSemanticId) {
                                selectedConnectionColor
                            } else {
                                connectionColor
                            },
                            start = androidx.compose.ui.geometry.Offset(connection.x1.toFloat(), connection.y1.toFloat()),
                            end = androidx.compose.ui.geometry.Offset(connection.x2.toFloat(), connection.y2.toFloat()),
                            strokeWidth = if (connection.semanticId == selectedSemanticId) 6f else 4f,
                        )
                    }
                }

                scene.components.forEach { component ->
                    val componentX = with(density) { component.x.toDp() }
                    val componentY = with(density) { component.y.toDp() }
                    val componentWidth = with(density) { component.width.toDp() }
                    val componentHeight = with(density) { component.height.toDp() }
                    val isSelected = component.semanticId == selectedSemanticId
                    Surface(
                        modifier = Modifier
                            .padding(start = componentX, top = componentY)
                            .size(componentWidth, componentHeight)
                            .shadow(4.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) selectedComponentSurfaceColor else componentSurfaceColor,
                        tonalElevation = 2.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                drawRoundRect(
                                    color = if (isSelected) {
                                        selectedComponentOutlineColor
                                    } else {
                                        componentOutlineColor
                                    },
                                    style = Stroke(width = if (isSelected) 4f else 2f),
                                )
                            }
                            Text(
                                text = component.label,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewerStatusPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceBright,
        tonalElevation = 1.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private const val ZOOM_STEP = 1.2f
