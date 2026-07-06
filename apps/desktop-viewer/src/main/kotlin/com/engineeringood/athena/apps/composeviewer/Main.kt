package com.engineeringood.athena.apps.composeviewer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.engineeringood.athena.composeruntime.AthenaComposeShell
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Launches the desktop Compose viewer shell or a non-interactive smoke path.
 */
fun main() {
    if (System.getProperty("athena.compose.bootstrap.smoke") == "true") {
        println(
            AthenaComposeViewerSmokeVerifier.verify(
                AthenaComposeViewerBootstrap.loadDefaultProjectSnapshot(),
            ),
        )
        return
    }

    val session = AthenaComposeViewerBootstrap.openDefaultWorkbenchSession()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = session.shellState().descriptor.windowTitle,
        ) {
            var shellState by remember { mutableStateOf(session.shellState()) }
            AthenaComposeShell(
                shellState = shellState,
                onIntent = { intent ->
                    shellState = session.dispatch(intent)
                },
            )
        }
    }
}
