package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.presentation.AthenaScenePublication
import com.engineeringood.athena.presentation.PublicationState

/** Session-scoped atomic owner for accepted and attempted Presentation revisions. */
class AthenaDiagramPublicationService(private val host: AthenaLspSessionHostReady) {
    private var lastAccepted: AthenaScenePublication? = null

    @Synchronized
    fun current(): AthenaScenePublication {
        val attempted = host.diagramScenePublication()
        if (attempted.state == PublicationState.READY) {
            lastAccepted = attempted
            return attempted
        }
        if (attempted.requiresUnavailable()) return attempted
        val accepted = lastAccepted ?: return attempted
        return AthenaScenePublication(
            state = PublicationState.STALE,
            attemptedInputRevision = attempted.attemptedInputRevision,
            acceptedInputRevision = accepted.acceptedInputRevision,
            scene = accepted.scene,
            assetBundle = accepted.assetBundle,
            diagnostics = attempted.diagnostics,
        )
    }

    private fun AthenaScenePublication.requiresUnavailable(): Boolean = diagnostics.any { diagnostic ->
        diagnostic.code in setOf(
            "sheet.companion.missing",
            "sheet.companion.ambiguous",
            "sheet.style.companion.ambiguous",
            "sheet.style.companion.invalid",
            "repository.lock.unavailable",
            "asset.resource.root-invalid",
            "asset.resource.missing",
            "asset.resource.digest-mismatch",
        )
    }
}
