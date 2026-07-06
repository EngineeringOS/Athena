package com.engineeringood.athena.composeruntime

/**
 * Describes the initial shell surface exposed by the Compose runtime module.
 */
data class AthenaComposeShellDescriptor(
    val windowTitle: String = "Athena",
    val statusLine: String = "Compose runtime shell bootstrap ready.",
)
