package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.RealityDeclaration
import com.engineeringood.athena.ir.RealityIdentityRule
import com.engineeringood.athena.ir.RealityValidationIssue
import com.engineeringood.athena.ir.RealityValidationResult

object ProjectionReality {
    const val name: String = "Projection Reality"
    const val rootName: String = "ProjectionDocument"
    const val purpose: String = "Transforms engineering truth into a view-specific engineering document."
    const val authority: String = "projection compiler"

    val ownedFacts: List<String> = listOf(
        "view",
        "sheet",
        "occurrence",
        "occurrence port",
        "connection endpoint",
        "projection group",
        "reading order",
    )

    val identityRules: List<RealityIdentityRule> = listOf(
        RealityIdentityRule("view", "View identity comes from the selected projection view."),
        RealityIdentityRule("sheet", "Sheet identity is view-local and ordered by the projection compiler."),
        RealityIdentityRule("occurrence", "Occurrence identity traces to the engineering subject plus view-local occurrence id."),
        RealityIdentityRule("occurrence port", "Occurrence-port identity combines one projected Occurrence with one semantic engineering port."),
        RealityIdentityRule("connection", "Connection occurrence identity traces to the engineering connection plus view-local connection id."),
        RealityIdentityRule("connection endpoint", "Connection endpoints reference typed occurrence-port identity."),
    )

    val requiredFacts: List<String> = listOf(
        "view identity",
        "sheet identity",
        "occurrence source identity",
        "reading order",
    )

    val declaration: RealityDeclaration = RealityDeclaration(
        name = name,
        rootName = rootName,
        purpose = purpose,
        authority = authority,
        ownedFacts = ownedFacts,
        identityRules = identityRules,
        requiredFacts = requiredFacts,
    )

    fun validate(document: ProjectionDocument): RealityValidationResult {
        val issues = buildList {
            if (document.view.id.isBlank()) {
                add(RealityValidationIssue(name, "missing view identity"))
            }
            if (document.sheets.isEmpty()) {
                add(RealityValidationIssue(name, "missing sheet facts"))
                add(RealityValidationIssue(name, "missing reading order"))
            }
            if (document.sheets.any { sheet -> sheet.sheetId.value.isBlank() }) {
                add(RealityValidationIssue(name, "missing sheet identity"))
            }
            if (
                document.nodes.any { node -> node.semanticId.value.isBlank() } ||
                document.connections.any { connection -> connection.semanticId.value.isBlank() } ||
                document.occurrencePorts.any { port ->
                    port.occurrencePortId.occurrenceId.value.isBlank() || port.occurrencePortId.portId.value.isBlank()
                } ||
                document.sheets.any { sheet -> sheet.subjects.any { subject -> subject.semanticId.value.isBlank() } }
            ) {
                add(RealityValidationIssue(name, "missing occurrence source identity"))
            }
            if (document.sheets.map { sheet -> sheet.order }.distinct().size != document.sheets.size) {
                add(RealityValidationIssue(name, "missing reading order"))
            }
            val sheetIds = document.sheets.map { sheet -> sheet.sheetId.value }
            if (sheetIds.size != sheetIds.distinct().size) {
                add(RealityValidationIssue(name, "duplicate sheet identity"))
            }
            if (document.sheets.any { sheet -> sheet.subjects.isEmpty() }) {
                add(RealityValidationIssue(name, "empty sheet"))
            }
            if (document.sheets.any { sheet -> sheet.viewId != document.view.id }) {
                add(RealityValidationIssue(name, "missing sheet view membership"))
            }
        }
        return RealityValidationResult(issues)
    }
}
