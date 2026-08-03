package com.engineeringood.athena.presentation

/**
 * Narrow backend descriptor above `Presentation IR`.
 *
 * The backend descriptor names the proof or production backend target without letting backend
 * internals redefine the presentation language.
 */
data class PresentationBackendDescriptor(
    val backendId: String,
    val displayName: String,
    val outputKind: String,
)
