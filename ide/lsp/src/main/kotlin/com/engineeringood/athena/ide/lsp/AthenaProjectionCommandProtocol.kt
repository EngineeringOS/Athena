package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSwitchRejected
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSwitchSuccess

internal const val SWITCH_ACTIVE_VIEW_COMMAND_ID = "switch-active-view"

internal fun AthenaLspSessionHostReady.executeProjectionCommand(
    params: AthenaProjectionCommandParams,
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures? = null,
): AthenaProjectionCommandPayload {
    if (params.commandId != SWITCH_ACTIVE_VIEW_COMMAND_ID) {
        return AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "rejected",
            reason = "Projection command `${params.commandId}` is not in the Athena allowlist.",
        )
    }

    val requestedViewId = params.viewId
        ?: return AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "rejected",
            reason = "Projection command `${params.commandId}` requires `viewId`.",
        )

    return when (val result = context.switchActiveProjectionView(requestedViewId)) {
        is AthenaRuntimeProjectionSwitchSuccess -> AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "applied",
            session = currentProjectionSession(
                snapshot = snapshot,
                languageFeatures = languageFeatures,
            ).toPayload(
                semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
            ),
        )

        is AthenaRuntimeProjectionSwitchRejected -> AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "rejected",
            reason = result.reason,
            session = currentProjectionSession(
                snapshot = snapshot,
                languageFeatures = languageFeatures,
            ).toPayload(
                semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
            ),
        )
    }
}
