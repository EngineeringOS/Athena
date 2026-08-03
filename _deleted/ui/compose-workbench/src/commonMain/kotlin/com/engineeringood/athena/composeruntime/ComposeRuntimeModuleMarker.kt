package com.engineeringood.athena.composeruntime

/**
 * Identifies the shared Compose runtime module during bootstrap and tests.
 */
class ComposeRuntimeModuleMarker(
    val moduleName: String = "ui:compose-workbench",
)
