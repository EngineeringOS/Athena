package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionConstructId
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePort
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionRegion
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetConstruct
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Compiles authored `view` declarations into Projection Reality documents (M40 Story 1.1).
 *
 * Views are the sole M40 authoring surface for projection selection. The compiler owns view and
 * sheet identity; validation rejects a view with no sheets and duplicate sheet identities.
 * Empty-sheet validation is enforced by [ProjectionReality.validate] once sheets carry
 * occurrences (Story 1.2).
 */
object AuthoredProjectionViewCompiler {
    fun compile(document: EngineeringDocument): AuthoredProjectionCompilation {
        val views = document.projectionViews
        if (views.isEmpty()) {
            return AuthoredProjectionCompilation.Success(emptyList())
        }
        if (document.projectionPolicies.isNotEmpty()) {
            return AuthoredProjectionCompilation.Failure(
                listOf(
                    ProjectionViewDiagnostic(
                        view = views.first().name,
                        message = "View declarations are the sole M40 projection selection surface; 'projection' policy declarations are retired for M40 source. Remove the projection policy block.",
                    ),
                ),
            )
        }
        val componentNames = document.components.map { component -> component.name }.toSet()
        val documents = mutableListOf<ProjectionDocument>()
        val diagnostics = mutableListOf<ProjectionViewDiagnostic>()
        for (view in views) {
            if (view.sheets.isEmpty()) {
                diagnostics += ProjectionViewDiagnostic(
                    view = view.name,
                    message = "View '${view.name}' has no sheets. Declare at least one sheet inside the view block.",
                )
                continue
            }
            if (view.regions.any { region -> region.sheetName.isBlank() }) {
                diagnostics += ProjectionViewDiagnostic(
                    view = view.name,
                    message = "View '${view.name}' declares a region before any sheet. Declare a sheet before each region.",
                )
                continue
            }
            if (view.constructs.any { construct -> construct.sheetName.isBlank() }) {
                diagnostics += ProjectionViewDiagnostic(
                    view = view.name,
                    message = "View '${view.name}' declares a construct before any sheet. Declare a sheet before each construct.",
                )
                continue
            }
            val duplicateConstruct = view.constructs
                .groupBy { construct -> construct.kind + ":" + construct.name }
                .entries
                .firstOrNull { entry -> entry.value.size > 1 }
            if (duplicateConstruct != null) {
                diagnostics += ProjectionViewDiagnostic(
                    view = view.name,
                    message = "View '${view.name}' declares duplicate construct '${duplicateConstruct.key}'. Each construct identity must be unique.",
                )
                continue
            }
            val constructNames = view.constructs.mapNotNull { construct -> construct.name }.toSet()
            val nestedConstructMember = view.constructs
                .flatMap { construct -> construct.occurrences.map { member -> construct to member } }
                .firstOrNull { (_, member) -> member.substringBefore('.') in constructNames }
            if (nestedConstructMember != null) {
                val (construct, member) = nestedConstructMember
                diagnostics += ProjectionViewDiagnostic(
                    view = view.name,
                    message = "View '${view.name}' ${construct.kind} construct references construct '$member' as a member. Constructs must not contain other constructs.",
                )
                continue
            }
            val declaredSheetNames = view.sheets.map { sheet -> sheet.name }
            if (view.readingOrder.isNotEmpty()) {
                val unknownSheets = view.readingOrder.filterNot { name -> name in declaredSheetNames }
                val duplicateSheets = view.readingOrder
                    .groupBy { name -> name }
                    .entries
                    .firstOrNull { entry -> entry.value.size > 1 }
                if (unknownSheets.isNotEmpty()) {
                    diagnostics += ProjectionViewDiagnostic(
                        view = view.name,
                        message = "View '${view.name}' reading order references unknown sheet(s): ${unknownSheets.joinToString(", ")}.",
                    )
                    continue
                }
                if (duplicateSheets != null) {
                    diagnostics += ProjectionViewDiagnostic(
                        view = view.name,
                        message = "View '${view.name}' reading order lists sheet '${duplicateSheets.key}' more than once.",
                    )
                    continue
                }
                if (view.readingOrder.toSet() != declaredSheetNames.toSet()) {
                    diagnostics += ProjectionViewDiagnostic(
                        view = view.name,
                        message = "View '${view.name}' reading order must be a permutation of the declared sheets.",
                    )
                    continue
                }
            }
            val duplicateId = view.sheets
                .groupBy { sheet -> sheet.name }
                .entries
                .firstOrNull { entry -> entry.value.size > 1 }
            if (duplicateId != null) {
                diagnostics += ProjectionViewDiagnostic(
                    view = view.name,
                    message = "View '${view.name}' declares duplicate sheet '${duplicateId.key}'. Each sheet must have a unique name.",
                )
                continue
            }
            val grid = view.grid?.let { declaredGrid ->
                ProjectionSheetGrid(
                    gridId = "${view.name}/${declaredGrid.name}",
                    rows = declaredGrid.rows,
                    columns = declaredGrid.columns,
                )
            }
            val sheets = mutableListOf<ProjectionSheet>()
            val projectionNodes = mutableListOf<ProjectionNode>()
            val sheetOrderByName = if (view.readingOrder.isNotEmpty()) {
                view.readingOrder.mapIndexed { index, name -> name to (index + 1) }.toMap()
            } else {
                view.sheets.mapIndexed { index, sheet -> sheet.name to (index + 1) }.toMap()
            }
            view.sheets.forEach { sheet ->
                val sheetRegions = view.regions.filter { region -> region.sheetName == sheet.name }
                val occurrenceNames = sheetRegions.flatMap { region -> region.occurrences }
                val sheetConstructs = view.constructs.filter { construct -> construct.sheetName == sheet.name }
                val missingSources = occurrenceNames.filterNot { name -> name in componentNames }.distinct()
                val duplicateOccurrences = occurrenceNames
                    .groupBy { name -> name }
                    .entries
                    .firstOrNull { entry -> entry.value.size > 1 }
                val emptyRegion = sheetRegions.firstOrNull { region -> region.occurrences.isEmpty() }
                val emptyConstruct = sheetConstructs.firstOrNull { construct -> construct.occurrences.isEmpty() }
                val unresolvedConstructMember = sheetConstructs
                    .flatMap { construct -> construct.occurrences.map { member -> construct to member } }
                    .firstOrNull { (_, member) -> member.substringBefore('.') !in occurrenceNames }
                when {
                    missingSources.isNotEmpty() -> {
                        diagnostics += ProjectionViewDiagnostic(
                            view = view.name,
                            message = "View '${view.name}' occurrence(s) without engineering source: ${missingSources.joinToString(", ")}. Declare the devices first.",
                        )
                    }
                    duplicateOccurrences != null -> {
                        diagnostics += ProjectionViewDiagnostic(
                            view = view.name,
                            message = "View '${view.name}' declares duplicate occurrence '${duplicateOccurrences.key}'. Each occurrence must appear once per sheet.",
                        )
                    }
                    emptyRegion != null -> {
                        diagnostics += ProjectionViewDiagnostic(
                            view = view.name,
                            message = "View '${view.name}' declares empty region '${emptyRegion.name}'. Add at least one occurrence.",
                        )
                    }
                    emptyConstruct != null -> {
                        diagnostics += ProjectionViewDiagnostic(
                            view = view.name,
                            message = "View '${view.name}' declares empty ${emptyConstruct.kind} construct '${emptyConstruct.name.orEmpty()}'. Add at least one member.",
                        )
                    }
                    unresolvedConstructMember != null -> {
                        val (construct, member) = unresolvedConstructMember
                        diagnostics += ProjectionViewDiagnostic(
                            view = view.name,
                            message = "View '${view.name}' ${construct.kind} construct references member '$member' that is not an occurrence on sheet '${sheet.name}'.",
                        )
                    }
                    occurrenceNames.isEmpty() -> {
                        diagnostics += ProjectionViewDiagnostic(
                            view = view.name,
                            message = "View '${view.name}' sheet '${sheet.name}' has no occurrences. Add a region with occurrences.",
                        )
                    }
                    else -> {
                        val sheetNodes = occurrenceNames.map { name ->
                            ProjectionNode(
                                projectionId = ProjectionNodeId(
                                    "${view.name}/sheet/${sheet.name}/occurrence/$name",
                                ),
                                semanticId = StableSemanticIdentity("component:$name"),
                                label = name,
                                originGeometryElementId = GeometryElementId(
                                    "projection:${view.name}:sheet:${sheet.name}:occurrence:$name",
                                ),
                            )
                        }
                        projectionNodes += sheetNodes
                        sheets += ProjectionSheet(
                            sheetId = ProjectionSheetId("${view.name}/sheet/${sheet.name}"),
                            displayName = sheet.name,
                            order = sheetOrderByName.getValue(sheet.name),
                            subjects = sheetNodes.map { node ->
                                ProjectionSheetSubject(
                                    semanticId = node.semanticId,
                                    nodeIds = listOf(node.projectionId),
                                )
                            },
                            regions = sheetRegions.map { region ->
                                ProjectionRegion(
                                    regionId = "${view.name}/${sheet.name}/${region.name}",
                                    name = region.name,
                                    occurrenceNames = region.occurrences,
                                )
                            },
                            constructs = sheetConstructs.map { construct ->
                                ProjectionSheetConstruct(
                                    constructId = ProjectionConstructId("${view.name}/${sheet.name}/${construct.kind}:${construct.name.orEmpty()}"),
                                    kind = construct.kind,
                                    name = construct.name,
                                    memberNames = construct.occurrences
                                        .map { member -> member.substringBefore('.') }
                                        .distinct(),
                                )
                            },
                            grid = grid,
                        )
                    }
                }
            }
            if (diagnostics.isEmpty()) {
                val nodesBySemanticId = projectionNodes.groupBy(ProjectionNode::semanticId)
                val engineeringPortsById = document.ports.associateBy { port -> port.id }
                val occurrencePorts = projectionNodes.flatMap { node ->
                    document.ports
                        .filter { port -> port.ownerReference.resolvedIdentity == node.semanticId }
                        .map { port ->
                            ProjectionOccurrencePort(
                                occurrencePortId = ProjectionOccurrencePortId(node.projectionId, port.id),
                                originGeometryElementId = GeometryElementId(
                                    "projection:${view.name}:${node.projectionId.value}:port:${port.id.value}",
                                ),
                            )
                        }
                }
                val connections = buildList {
                    document.connections.forEach { connection ->
                        val sourcePortId = connection.from.resolvedIdentity ?: return@forEach
                        val targetPortId = connection.to.resolvedIdentity ?: return@forEach
                        val sourceOwnerId = engineeringPortsById[sourcePortId]?.ownerReference?.resolvedIdentity
                        val targetOwnerId = engineeringPortsById[targetPortId]?.ownerReference?.resolvedIdentity
                        val sourceCandidates = sourceOwnerId?.let(nodesBySemanticId::get).orEmpty()
                        val targetCandidates = targetOwnerId?.let(nodesBySemanticId::get).orEmpty()
                        if (sourceCandidates.isEmpty() || targetCandidates.isEmpty()) return@forEach
                        if (sourceCandidates.size != 1 || targetCandidates.size != 1) {
                            diagnostics += ProjectionViewDiagnostic(
                                view = view.name,
                                message = "View '${view.name}' Connection '${connection.id.value}' does not resolve each endpoint " +
                                    "to exactly one projected Occurrence. Place each endpoint owner once in this view.",
                            )
                            return@forEach
                        }
                        val source = sourceCandidates.single()
                        val target = targetCandidates.single()
                        add(
                            ProjectionConnection(
                                projectionId = ProjectionConnectionId("${view.name}/connection/${connection.id.value}"),
                                semanticId = connection.id,
                                originGeometryElementId = GeometryElementId("projection:${view.name}:${connection.id.value}"),
                                source = ProjectionConnectionEndpoint(
                                    ProjectionOccurrencePortId(source.projectionId, sourcePortId),
                                ),
                                target = ProjectionConnectionEndpoint(
                                    ProjectionOccurrencePortId(target.projectionId, targetPortId),
                                ),
                            ),
                        )
                    }
                }
                if (diagnostics.isEmpty()) {
                    val sheetsWithConnections = sheets.map { sheet ->
                        val ownedOccurrences = sheet.subjects.flatMap { subject -> subject.nodeIds }.toSet()
                        val ownedConnections = connections.filter { connection ->
                            connection.source?.occurrencePortId?.occurrenceId?.let(ownedOccurrences::contains) == true &&
                                connection.target?.occurrencePortId?.occurrenceId?.let(ownedOccurrences::contains) == true
                        }
                        sheet.copy(
                            subjects = sheet.subjects + ownedConnections.map { connection ->
                                ProjectionSheetSubject(
                                    semanticId = connection.semanticId,
                                    connectionIds = listOf(connection.projectionId),
                                )
                            },
                        )
                    }
                    documents += ProjectionDocument(
                        view = ViewDefinition(
                            id = view.name,
                            displayName = view.name,
                        ),
                        nodes = projectionNodes,
                        connections = connections,
                        occurrencePorts = occurrencePorts,
                        sheets = sheetsWithConnections,
                    )
                }
            }
        }
        if (diagnostics.isEmpty()) {
            documents.forEach { document ->
                ProjectionBoundaryValidator.report(document).forEach { issue ->
                    diagnostics += ProjectionViewDiagnostic(
                        view = document.view.id,
                        message = issue.message,
                    )
                }
            }
        }
        return if (diagnostics.isEmpty()) {
            AuthoredProjectionCompilation.Success(documents)
        } else {
            AuthoredProjectionCompilation.Failure(diagnostics)
        }
    }
}

sealed interface AuthoredProjectionCompilation {
    data class Success(val documents: List<ProjectionDocument>) : AuthoredProjectionCompilation
    data class Failure(val diagnostics: List<ProjectionViewDiagnostic>) : AuthoredProjectionCompilation
}

/** Plain-language view/sheet diagnostic naming the subject and the problem. */
data class ProjectionViewDiagnostic(
    val view: String,
    val message: String,
)
