package com.engineeringood.athena.apps.composeviewer

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.engineeringood.athena.composeruntime.AthenaComposeShell
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Off-screen smoke verifier that exercises the desktop Compose viewer composition path.
 */
object AthenaComposeViewerSmokeVerifier {
    /**
     * Renders one off-screen frame for the supplied snapshot and returns a deterministic summary.
     */
    fun verify(snapshot: AthenaComposeViewerProjectSnapshot): String {
        val scene = ImageComposeScene(
            width = SMOKE_RENDER_WIDTH,
            height = SMOKE_RENDER_HEIGHT,
            density = Density(1f),
            coroutineContext = EmptyCoroutineContext,
        )

        val renderedImage = try {
            scene.setContent {
                AthenaComposeShell(
                    shellState = snapshot.toShellState(),
                )
            }
            scene.render(0L)
        } finally {
            scene.close()
        }

        check(renderedImage.width > 0 && renderedImage.height > 0) {
            "Compose viewer smoke render produced an empty frame."
        }

        return if (snapshot.scene == null) {
            "Athena semantic viewer rendered unavailable state for ${snapshot.projectName} at ${renderedImage.width}x${renderedImage.height}."
        } else {
            "Athena semantic viewer rendered ${snapshot.projectName} (${snapshot.scene.systemName}) at ${renderedImage.width}x${renderedImage.height}."
        }
    }
}

private const val SMOKE_RENDER_WIDTH = 1280
private const val SMOKE_RENDER_HEIGHT = 800
