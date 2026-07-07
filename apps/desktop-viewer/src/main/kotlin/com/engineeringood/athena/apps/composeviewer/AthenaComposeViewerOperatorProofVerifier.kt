package com.engineeringood.athena.apps.composeviewer

import com.engineeringood.athena.composeruntime.AthenaComposeShellIntent

/**
 * Replays the final M2 desktop proof so the workbench can be verified without a human operator.
 */
object AthenaComposeViewerOperatorProofVerifier {
    /**
     * Executes the scripted projection-switching and command flow against one runtime-backed session.
     */
    fun verify(session: AthenaComposeViewerWorkbenchSession): String {
        val initialState = session.shellState()
        require(initialState.workspaceName == "operator-proof") {
            "Expected the default desktop workspace to use the operator-proof seed."
        }
        require(initialState.projectName == "OperatorProof") {
            "Expected the default desktop project to render the OperatorProof system."
        }
        require(initialState.scene?.connectionCount == 0) {
            "Expected the operator proof to start without derived connections."
        }
        require(initialState.projectionSession?.activeViewId == "cabinet") {
            "Expected the operator proof to start from the cabinet view."
        }

        session.dispatch(
            AthenaComposeShellIntent.SelectRenderedSemantic(
                semanticId = OPERATOR_COMPONENT_SEMANTIC_ID,
            ),
        )
        val selectedCabinetState = session.shellState()
        require(selectedCabinetState.projectionSession?.selectedSemanticId == OPERATOR_COMPONENT_SEMANTIC_ID) {
            "Expected the canonical component selection to be captured in cabinet."
        }

        session.dispatch(
            AthenaComposeShellIntent.SwitchProjectionView(
                viewId = "wiring",
            ),
        )
        val selectedWiringState = session.shellState()
        require(selectedWiringState.projectionSession?.activeViewId == "wiring") {
            "Expected the operator proof to switch into the wiring view."
        }
        require(selectedWiringState.projectionSession?.selectedSemanticId == OPERATOR_COMPONENT_SEMANTIC_ID) {
            "Expected the canonical component selection to survive the view switch."
        }
        require(selectedWiringState.projectionSession?.selectedSemanticVisibleInActiveView == true) {
            "Expected the canonical component selection to stay visible in the active wiring view."
        }

        session.dispatch(
            AthenaComposeShellIntent.SelectSourcePort(
                semanticId = SOURCE_PORT_SEMANTIC_ID,
            ),
        )
        session.dispatch(
            AthenaComposeShellIntent.SelectTargetPort(
                semanticId = TARGET_PORT_SEMANTIC_ID,
            ),
        )
        session.dispatch(AthenaComposeShellIntent.ExecuteConnectPorts)

        val connectedWiringState = session.shellState()
        require(connectedWiringState.scene?.connectionCount == 1) {
            "Expected the wiring projection to refresh after the runtime command created one connection."
        }
        require(connectedWiringState.commandPanel?.statusMessage?.contains(CONNECTION_SEMANTIC_ID) == true) {
            "Expected the command panel to report the runtime-created connection identity."
        }
        require(connectedWiringState.diagnosticsEntries.any { entry -> entry.contains("history: command-0001 APPLIED") }) {
            "Expected the runtime diagnostics to record the applied command history consequence."
        }

        session.dispatch(
            AthenaComposeShellIntent.SwitchProjectionView(
                viewId = "cabinet",
            ),
        )
        val connectedCabinetState = session.shellState()
        require(connectedCabinetState.projectionSession?.activeViewId == "cabinet") {
            "Expected the operator proof to return to the cabinet view."
        }
        require(connectedCabinetState.scene?.connectionCount == 1) {
            "Expected the cabinet projection to reflect the runtime-created connection."
        }
        require(connectedCabinetState.projectionSession?.selectedSemanticId == OPERATOR_COMPONENT_SEMANTIC_ID) {
            "Expected the canonical component selection to remain stable after returning to cabinet."
        }
        require(connectedCabinetState.projectionSession?.selectedSemanticVisibleInActiveView == true) {
            "Expected the canonical component selection to remain visible after returning to cabinet."
        }

        return buildString {
            append("Athena desktop operator-proof completed for operator-proof (OperatorProof): ")
            append("runtime-owned selection ")
            append(OPERATOR_COMPONENT_SEMANTIC_ID)
            append(" stayed visible across cabinet and wiring, then created ")
            append(CONNECTION_SEMANTIC_ID)
            append(" through the command runtime.")
        }
    }
}

private const val OPERATOR_COMPONENT_SEMANTIC_ID = "component:PLC1"
private const val SOURCE_PORT_SEMANTIC_ID = "port:PLC1.out"
private const val TARGET_PORT_SEMANTIC_ID = "port:M1.in"
private const val CONNECTION_SEMANTIC_ID = "connection:PLC1.out->M1.in"
