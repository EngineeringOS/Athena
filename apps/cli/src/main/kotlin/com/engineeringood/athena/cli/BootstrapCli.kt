package com.engineeringood.athena.cli

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerParseFailure
import com.engineeringood.athena.compiler.CompilerParseSuccess
import com.engineeringood.athena.compiler.CompilerModuleMarker
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainMarker
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.renderer.svg.SvgRendererModuleMarker
import com.engineeringood.athena.runtime.AthenaAiCommandProposalAccepted
import com.engineeringood.athena.runtime.AthenaAiCommandProposalAcceptanceRejected
import com.engineeringood.athena.runtime.AthenaAiCommandProposalAcceptanceUnavailable
import com.engineeringood.athena.runtime.AthenaAiCommandProposalDraft
import com.engineeringood.athena.runtime.AthenaAiCommandProposalRejected
import com.engineeringood.athena.runtime.AthenaAiCommandProposalRejectionRejected
import com.engineeringood.athena.runtime.AthenaAiCommandProposalSubmitted
import com.engineeringood.athena.runtime.AthenaCommandExecutionRejected
import com.engineeringood.athena.runtime.AthenaCommandExecutionSuccess
import com.engineeringood.athena.runtime.AthenaCommandExecutionUnavailable
import com.engineeringood.athena.runtime.AthenaCommandHistoryMutationRejected
import com.engineeringood.athena.runtime.AthenaCommandHistoryMutationSuccess
import com.engineeringood.athena.runtime.AthenaCommandHistoryMutationUnavailable
import com.engineeringood.athena.runtime.AthenaConnectPortsCommand
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntime
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandExecutionRejected
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandExecutionSuccess
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandExecutionUnavailable
import com.engineeringood.athena.runtime.AthenaRuntimeViewerReadyProjection
import com.engineeringood.athena.runtime.AthenaSemanticDiffInspection
import com.engineeringood.athena.runtime.RuntimeModuleMarker
import com.engineeringood.athena.runtime.projectViewerProjection
import java.nio.file.Files
import java.nio.file.Path

/** Small CLI facade that exposes the current bootstrap and parse commands. */
class BootstrapCli(
    private val runtime: AthenaRuntime = AthenaRuntime(),
    private val runtimeMarker: RuntimeModuleMarker = RuntimeModuleMarker(),
    private val compilerModuleMarker: CompilerModuleMarker = CompilerModuleMarker(),
    private val domainMarker: ElectricalRuntimeDomainMarker = ElectricalRuntimeDomainMarker(),
    private val rendererMarker: SvgRendererModuleMarker = SvgRendererModuleMarker(),
) {
    private val sessionStore: AthenaCliSessionStore = AthenaCliSessionStore()

    /** Executes the requested CLI command and returns plain-text output for the shell. */
    fun run(args: List<String>): String {
        if (args.isEmpty() || args == listOf("--help") || args == listOf("-h")) {
            return helpText()
        }

        if (args.firstOrNull() == "parse") {
            return parseCommand(args.drop(1))
        }

        if (args.firstOrNull() == "connect") {
            return connectCommand(args.drop(1))
        }

        if (args.firstOrNull() == "ai-propose-connect") {
            return aiProposeConnectCommand(args.drop(1))
        }

        if (args.firstOrNull() == "ai-proposals") {
            return aiProposalsCommand(args.drop(1))
        }

        if (args.firstOrNull() == "ai-accept") {
            return aiAcceptCommand(args.drop(1))
        }

        if (args.firstOrNull() == "ai-reject") {
            return aiRejectCommand(args.drop(1))
        }

        if (args.firstOrNull() == "history") {
            return historyCommand(args.drop(1))
        }

        if (args.firstOrNull() == "plugins") {
            return pluginsCommand(args.drop(1))
        }

        if (args.firstOrNull() == "plugin-command") {
            return pluginCommand(args.drop(1))
        }

        if (args.firstOrNull() == "serialize-history") {
            return serializeHistoryCommand(args.drop(1))
        }

        if (args.firstOrNull() == "diff") {
            return diffCommand(args.drop(1))
        }

        if (args.firstOrNull() == "history-consequences") {
            return historyConsequencesCommand(args.drop(1))
        }

        if (args.firstOrNull() == "undo") {
            return undoCommand(args.drop(1))
        }

        if (args.firstOrNull() == "redo") {
            return redoCommand(args.drop(1))
        }

        if (args.firstOrNull() == "replay") {
            return replayCommand(args.drop(1))
        }

        return buildString {
            appendLine("Unknown arguments: ${args.joinToString(" ")}")
            appendLine()
            append(helpText())
        }
    }

    private fun parseCommand(arguments: List<String>): String {
        if (arguments.size != 1) {
            return "Usage: parse <source-file>"
        }

        val path = normalizedExistingPath(arguments.single()) ?: return "File not found: ${Path.of(arguments.single())}"

        return when (val result = runtime.serviceRegistry.compiler().parse(path)) {
            is CompilerParseSuccess -> {
                val summary = summarize(result.source.ast)
                buildString {
                    appendLine("Parse successful")
                    appendLine("System: ${summary.systemName}")
                    appendLine("device declarations: ${summary.deviceDeclarations}")
                    appendLine("port declarations: ${summary.portDeclarations}")
                    append("connection declarations: ${summary.connectionDeclarations}")
                }
            }

            is CompilerParseFailure -> {
                buildString {
                    appendLine("Syntax diagnostics")
                    result.diagnostics.forEach { diagnostic ->
                        appendLine("${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}")
                    }
                    append("Pipeline stopped before semantic validation and rendering")
                }
            }
        }
    }

    private fun connectCommand(arguments: List<String>): String {
        if (arguments.size != 3) {
            return "Usage: connect <source-file> <source-port-path> <target-port-path>"
        }

        val session = when (val sessionResult = openExecutionSession(arguments[0])) {
            is AthenaCliExecutionSessionReady -> sessionResult.session
            is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
        }
        val context = session.context

        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationParseFailure -> unavailableCommandOutput(
                reason = "Command runtime is unavailable for the current project.",
                compilation = compilation,
            )

            is CompilerCompilationSuccess -> {
                val sourceSemanticId = resolvePortSemanticId(compilation, arguments[1])
                    ?: return explicitConnectLookupRejection(
                        projectName = context.project.name,
                        authoredPortPath = arguments[1],
                        endpointRole = "Source",
                    )
                val targetSemanticId = resolvePortSemanticId(compilation, arguments[2])
                    ?: return explicitConnectLookupRejection(
                        projectName = context.project.name,
                        authoredPortPath = arguments[2],
                        endpointRole = "Target",
                    )

                when (
                    val result = context.commandRuntime().execute(
                        context = context,
                        command = AthenaConnectPortsCommand(
                            sourcePortSemanticId = sourceSemanticId,
                            targetPortSemanticId = targetSemanticId,
                        ),
                    )
                ) {
                    is AthenaCommandExecutionSuccess -> {
                        persistExecutionSession(context)
                        val viewerProjection = context.projectViewerProjection()
                        val viewerConnectionCount = if (viewerProjection is AthenaRuntimeViewerReadyProjection) {
                            viewerProjection.scene.connections.size
                        } else {
                            0
                        }

                        buildString {
                            appendLine("Command successful")
                            appendLine("Project: ${result.projectName}")
                            appendLine("Command: ${result.commandKind}")
                            appendLine("Changed semantic ids: ${result.changedSemanticIds.sorted().joinToString(", ")}")
                            appendLine("connections before: ${result.beforeDocument.connections.size}")
                            appendLine("connections after: ${result.afterDocument.connections.size}")
                            append("viewer connections: $viewerConnectionCount")
                        }
                    }

                    is AthenaCommandExecutionRejected -> buildString {
                        appendLine("Command rejected")
                        appendLine("Project: ${result.projectName}")
                        appendLine("Command: ${result.commandKind}")
                        append(result.reason)
                    }

                    is AthenaCommandExecutionUnavailable -> unavailableCommandOutput(
                        reason = result.reason,
                        compilation = context.compileActiveProject() as? CompilerCompilationParseFailure,
                    )
                }
            }
        }
    }

    private fun aiProposeConnectCommand(arguments: List<String>): String {
        if (arguments.size != 4) {
            return "Usage: ai-propose-connect <source-file> <source-port-path> <target-port-path> <summary>"
        }

        val session = when (val sessionResult = openExecutionSession(arguments[0])) {
            is AthenaCliExecutionSessionReady -> sessionResult.session
            is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
        }
        val context = session.context

        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationParseFailure -> unavailableCommandOutput(
                reason = "AI proposal runtime is unavailable for the current project.",
                compilation = compilation,
            )

            is CompilerCompilationSuccess -> {
                val sourceSemanticId = resolvePortSemanticId(compilation, arguments[1])
                    ?: return explicitConnectLookupRejection(
                        projectName = context.project.name,
                        authoredPortPath = arguments[1],
                        endpointRole = "Source",
                    )
                val targetSemanticId = resolvePortSemanticId(compilation, arguments[2])
                    ?: return explicitConnectLookupRejection(
                        projectName = context.project.name,
                        authoredPortPath = arguments[2],
                        endpointRole = "Target",
                    )

                val submission = context.aiProposalRuntime().submit(
                    context = context,
                    draft = AthenaAiCommandProposalDraft(
                        summary = arguments[3],
                        rationale = "Submitted through the CLI optional AI proposal surface.",
                        command = AthenaConnectPortsCommand(
                            sourcePortSemanticId = sourceSemanticId,
                            targetPortSemanticId = targetSemanticId,
                        ),
                    ),
                )
                val submitted = submission as AthenaAiCommandProposalSubmitted
                persistExecutionSession(context)

                buildString {
                    appendLine("AI proposal queued")
                    appendLine("Project: ${context.project.name}")
                    appendLine("Proposal: ${submitted.proposal.proposalId}")
                    appendLine("Command: ${submitted.proposal.command.commandKind}")
                    appendLine("Summary: ${submitted.proposal.summary}")
                    append("Pending AI proposals: ${context.aiProposalRuntime().pendingProposals(context).size}")
                }
            }
        }
    }

    private fun aiProposalsCommand(arguments: List<String>): String {
        if (arguments.size > 1) {
            return "Usage: ai-proposals [source-file]"
        }

        val session = when (arguments.singleOrNull()) {
            null -> AthenaCliExecutionSession(
                context = runtime.activeExecutionContext ?: return noActiveProjectMessage(),
                persistedLatestDiffText = null,
            )

            else -> when (val sessionResult = openExecutionSession(arguments.single())) {
                is AthenaCliExecutionSessionReady -> sessionResult.session
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        val proposals = session.context.aiProposalRuntime().pendingProposals(session.context)

        return buildString {
            appendLine("Pending AI proposals: ${proposals.size}")
            proposals.forEachIndexed { index, proposal ->
                append("${proposal.proposalId} ${proposal.command.commandKind} ${proposal.summary}")
                if (index < proposals.lastIndex) {
                    appendLine()
                }
            }
        }
    }

    private fun aiAcceptCommand(arguments: List<String>): String {
        if (arguments.size != 2) {
            return "Usage: ai-accept <source-file> <proposal-id>"
        }

        val session = when (val sessionResult = openExecutionSession(arguments[0])) {
            is AthenaCliExecutionSessionReady -> sessionResult.session
            is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
        }
        val context = session.context

        return when (val result = context.aiProposalRuntime().acceptProposal(context, arguments[1])) {
            is AthenaAiCommandProposalAccepted -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("AI proposal accepted")
                    appendLine("Proposal: ${result.proposal.proposalId}")
                    appendLine("Command: ${result.proposal.command.commandKind}")
                    appendLine("Command id: ${result.execution.commandId}")
                    appendLine("Origin: ${result.execution.commandOrigin}")
                    appendLine("connections before: ${result.execution.beforeDocument.connections.size}")
                    append("connections after: ${result.execution.afterDocument.connections.size}")
                }
            }

            is AthenaAiCommandProposalAcceptanceRejected -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("AI proposal acceptance rejected")
                    appendLine("Proposal: ${result.proposalId}")
                    append(result.reason)
                }
            }

            is AthenaAiCommandProposalAcceptanceUnavailable -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("AI proposal acceptance unavailable")
                    appendLine("Proposal: ${result.proposalId}")
                    append(result.reason)
                }
            }
        }
    }

    private fun aiRejectCommand(arguments: List<String>): String {
        if (arguments.size != 2) {
            return "Usage: ai-reject <source-file> <proposal-id>"
        }

        val session = when (val sessionResult = openExecutionSession(arguments[0])) {
            is AthenaCliExecutionSessionReady -> sessionResult.session
            is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
        }
        val context = session.context

        return when (val result = context.aiProposalRuntime().rejectProposal(context, arguments[1])) {
            is AthenaAiCommandProposalRejected -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("AI proposal rejected")
                    appendLine("Proposal: ${result.proposal.proposalId}")
                    append("Pending AI proposals: ${context.aiProposalRuntime().pendingProposals(context).size}")
                }
            }

            is AthenaAiCommandProposalRejectionRejected -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("AI proposal rejection rejected")
                    appendLine("Proposal: ${result.proposalId}")
                    append(result.reason)
                }
            }
        }
    }

    private fun historyCommand(arguments: List<String>): String {
        if (arguments.size > 1) {
            return "Usage: history [source-file]"
        }

        val session = when (arguments.singleOrNull()) {
            null -> AthenaCliExecutionSession(
                context = runtime.activeExecutionContext ?: return noActiveProjectMessage(),
                persistedLatestDiffText = null,
            )

            else -> when (val sessionResult = openExecutionSession(arguments.single())) {
                is AthenaCliExecutionSessionReady -> sessionResult.session
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        val history = session.context.commandRuntime().history(session.context)

        return buildString {
            appendLine("History entries: ${history.records.size}")
            appendLine("Applied entries: ${history.appliedRecordCount}")
            history.records.forEachIndexed { index, record ->
                append("${record.commandId} ${record.commandKind} ${record.status} ${record.commandOrigin}")
                if (index < history.records.lastIndex) {
                    appendLine()
                }
            }
        }
    }

    private fun pluginsCommand(arguments: List<String>): String {
        if (arguments.isNotEmpty()) {
            return "Usage: plugins"
        }

        val pluginServices = runtime.serviceRegistry.pluginRuntimeServices()
        val hostedPlugins = pluginServices.hostedPlugins()
        val commandContributions = pluginServices.commandContributions()

        return buildString {
            appendLine("Hosted plugins: ${hostedPlugins.size}")
            hostedPlugins.forEachIndexed { index, plugin ->
                appendLine("${plugin.pluginId} ${plugin.pluginVersion}")
                appendLine("  implementation: ${plugin.implementationClassName}")
                appendLine(
                    "  domain capabilities: ${plugin.domainCapabilities.sorted().joinToString(", ").ifBlank { "none" }}",
                )
                appendLine(
                    "  command contributions: ${plugin.commandContributionIds.joinToString(", ").ifBlank { "none" }}",
                )
                append("  view contributions: ${plugin.viewContributionCount}")
                if (index < hostedPlugins.lastIndex) {
                    appendLine()
                }
            }
            if (hostedPlugins.isNotEmpty()) {
                appendLine()
            }
            appendLine("Hosted runtime command contributions: ${commandContributions.size}")
            commandContributions.forEachIndexed { index, contribution ->
                append("${contribution.contributionId} (${contribution.pluginId})")
                if (index < commandContributions.lastIndex) {
                    appendLine()
                }
            }
        }
    }

    private fun pluginCommand(arguments: List<String>): String {
        if (arguments.size != 2) {
            return "Usage: plugin-command <source-file> <contribution-id>"
        }

        val session = when (val sessionResult = openExecutionSession(arguments[0])) {
            is AthenaCliExecutionSessionReady -> sessionResult.session
            is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
        }
        val context = session.context
        return when (
            val result = context.pluginRuntimeServices().executeCommandContribution(
                context = context,
                contributionId = arguments[1],
            )
        ) {
            is AthenaRuntimePluginCommandExecutionSuccess -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("Plugin command successful")
                    appendLine("Plugin: ${result.pluginId}")
                    appendLine("Contribution: ${result.contributionId}")
                    appendLine("Project: ${result.result.projectName}")
                    appendLine("Command: ${result.result.commandKind}")
                    appendLine("Changed semantic ids: ${result.result.changedSemanticIds.sorted().joinToString(", ")}")
                    appendLine("connections before: ${result.result.beforeDocument.connections.size}")
                    append("connections after: ${result.result.afterDocument.connections.size}")
                }
            }

            is AthenaRuntimePluginCommandExecutionRejected -> buildString {
                appendLine("Plugin command rejected")
                appendLine("Plugin: ${result.pluginId}")
                appendLine("Contribution: ${result.contributionId}")
                append(result.reason)
            }

            is AthenaRuntimePluginCommandExecutionUnavailable -> buildString {
                appendLine("Plugin command unavailable")
                if (result.pluginId.isNotBlank()) {
                    appendLine("Plugin: ${result.pluginId}")
                }
                appendLine("Contribution: ${result.contributionId}")
                append(result.reason)
            }
        }
    }

    private fun serializeHistoryCommand(arguments: List<String>): String {
        if (arguments.size > 1) {
            return "Usage: serialize-history [source-file]"
        }

        val context = when (arguments.singleOrNull()) {
            null -> runtime.activeExecutionContext ?: return noActiveProjectMessage()
            else -> when (val sessionResult = openExecutionSession(arguments.single())) {
                is AthenaCliExecutionSessionReady -> sessionResult.session.context
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        return context.commandRuntime().serializeHistory(context)
    }

    private fun diffCommand(arguments: List<String>): String {
        if (arguments.size > 1) {
            return "Usage: diff [source-file]"
        }

        val session = when (arguments.singleOrNull()) {
            null -> AthenaCliExecutionSession(
                context = runtime.activeExecutionContext ?: return noActiveProjectMessage(),
                persistedLatestDiffText = null,
            )

            else -> when (val sessionResult = openExecutionSession(arguments.single())) {
                is AthenaCliExecutionSessionReady -> sessionResult.session
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        val inspection = session.context.commandRuntime().latestInspection(session.context)
        return session.persistedLatestDiffText
            ?: inspection?.renderLatestDiff()
            ?: "No latest semantic diff is available. Execute a command, undo, redo, or replay first."
    }

    private fun historyConsequencesCommand(arguments: List<String>): String {
        if (arguments.size !in 1..2) {
            return "Usage: history-consequences <command-id> | history-consequences <source-file> <command-id>"
        }

        val (context, commandId) = if (arguments.size == 1) {
            runtime.activeExecutionContext?.let { activeContext ->
                activeContext to arguments.single()
            } ?: return noActiveProjectMessage()
        } else {
            when (val sessionResult = openExecutionSession(arguments[0])) {
                is AthenaCliExecutionSessionReady -> sessionResult.session.context to arguments[1]
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        val inspection = context.commandRuntime().inspectCommandHistoryConsequence(
            context = context,
            commandId = commandId,
        ) ?: return "No recorded command matches `$commandId`."
        return inspection.renderHistoryConsequence()
    }

    private fun undoCommand(arguments: List<String>): String {
        if (arguments.size > 1) {
            return "Usage: undo [source-file]"
        }

        val context = when (arguments.singleOrNull()) {
            null -> runtime.activeExecutionContext ?: return noActiveProjectMessage()
            else -> when (val sessionResult = openExecutionSession(arguments.single())) {
                is AthenaCliExecutionSessionReady -> sessionResult.session.context
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        return when (val result = context.commandRuntime().undo(context)) {
            is AthenaCommandHistoryMutationSuccess -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("Undo successful")
                    appendLine("Project: ${result.projectName}")
                    appendLine("Command: ${result.affectedCommandIds.singleOrNull() ?: result.affectedCommandIds.joinToString(", ")}")
                    appendLine("connections before: ${result.beforeDocument.connections.size}")
                    append("connections after: ${result.afterDocument.connections.size}")
                }
            }

            is AthenaCommandHistoryMutationRejected -> buildString {
                appendLine("Undo rejected")
                append(result.reason)
            }

            is AthenaCommandHistoryMutationUnavailable -> buildString {
                appendLine("Undo unavailable")
                append(result.reason)
            }
        }
    }

    private fun redoCommand(arguments: List<String>): String {
        if (arguments.size > 1) {
            return "Usage: redo [source-file]"
        }

        val context = when (arguments.singleOrNull()) {
            null -> runtime.activeExecutionContext ?: return noActiveProjectMessage()
            else -> when (val sessionResult = openExecutionSession(arguments.single())) {
                is AthenaCliExecutionSessionReady -> sessionResult.session.context
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        return when (val result = context.commandRuntime().redo(context)) {
            is AthenaCommandHistoryMutationSuccess -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("Redo successful")
                    appendLine("Project: ${result.projectName}")
                    appendLine("Command: ${result.affectedCommandIds.singleOrNull() ?: result.affectedCommandIds.joinToString(", ")}")
                    appendLine("connections before: ${result.beforeDocument.connections.size}")
                    append("connections after: ${result.afterDocument.connections.size}")
                }
            }

            is AthenaCommandHistoryMutationRejected -> buildString {
                appendLine("Redo rejected")
                append(result.reason)
            }

            is AthenaCommandHistoryMutationUnavailable -> buildString {
                appendLine("Redo unavailable")
                append(result.reason)
            }
        }
    }

    private fun replayCommand(arguments: List<String>): String {
        if (arguments.size > 1) {
            return "Usage: replay [source-file]"
        }

        val context = when (arguments.singleOrNull()) {
            null -> runtime.activeExecutionContext ?: return noActiveProjectMessage()
            else -> when (val sessionResult = openExecutionSession(arguments.single())) {
                is AthenaCliExecutionSessionReady -> sessionResult.session.context
                is AthenaCliExecutionSessionUnavailable -> return sessionResult.message
            }
        }
        return when (val result = context.commandRuntime().replay(context)) {
            is AthenaCommandHistoryMutationSuccess -> {
                persistExecutionSession(context)
                buildString {
                    appendLine("Replay successful")
                    appendLine("Project: ${result.projectName}")
                    appendLine("Commands: ${result.affectedCommandIds.joinToString(", ")}")
                    appendLine("connections before: ${result.beforeDocument.connections.size}")
                    append("connections after: ${result.afterDocument.connections.size}")
                }
            }

            is AthenaCommandHistoryMutationRejected -> buildString {
                appendLine("Replay rejected")
                append(result.reason)
            }

            is AthenaCommandHistoryMutationUnavailable -> buildString {
                appendLine("Replay unavailable")
                append(result.reason)
            }
        }
    }

    private fun helpText(): String = buildString {
        appendLine("Athena M1 runtime host")
        appendLine("Java 25")
        appendLine("Gradle 9.6.1")
        appendLine(
            "Modules: apps:cli, kernel:runtime, kernel:language, kernel:validation, kernel:engineering-model, kernel:compiler, extensions:domain-electrical, kernel:svg-renderer",
        )
        appendLine(
            "Connected markers: ${runtimeMarker.moduleName}, ${compilerModuleMarker.moduleName}, ${domainMarker.moduleName}, ${rendererMarker.moduleName}",
        )
        append(
            "Command surface: --help, parse <source-file>, connect <source-file> <source-port-path> <target-port-path>, ai-propose-connect <source-file> <source-port-path> <target-port-path> <summary>, ai-proposals [source-file], ai-accept <source-file> <proposal-id>, ai-reject <source-file> <proposal-id>, plugins, plugin-command <source-file> <contribution-id>, history [source-file], serialize-history [source-file], diff [source-file], history-consequences <command-id> | <source-file> <command-id>, undo [source-file], redo [source-file], replay [source-file]",
        )
    }

    /** Builds a lightweight parse summary only for CLI output rendering. */
    private fun summarize(ast: SourceFileAst): CliParseSummary {
        return CliParseSummary(
            systemName = ast.system.name,
            deviceDeclarations = ast.declarations.filterIsInstance<DeviceDeclaration>().size,
            portDeclarations = ast.declarations.filterIsInstance<PortDeclaration>().size,
            connectionDeclarations = ast.declarations.filterIsInstance<ConnectionDeclaration>().size,
        )
    }

    /** Resolves a user-facing authored port path like `PLC1.out` to the canonical semantic id used by commands. */
    private fun resolvePortSemanticId(
        compilation: CompilerCompilationSuccess,
        authoredPortPath: String,
    ): String? {
        return compilation.document.ports.firstOrNull { port ->
            port.authoredPath() == authoredPortPath
        }?.id?.value
    }

    /** Restores persisted CLI session state when necessary before a one-shot command touches runtime-owned history. */
    private fun openExecutionSession(sourceFileArgument: String): AthenaCliExecutionSessionResult {
        val sourcePath = normalizedExistingPath(sourceFileArgument)
            ?: return AthenaCliExecutionSessionUnavailable("File not found: ${Path.of(sourceFileArgument)}")
        val normalizedSourcePath = sourcePath.toAbsolutePath().normalize()
        val activeContext = runtime.activeExecutionContext
        val activeMatchesPath = activeContext?.project?.sourcePath?.toAbsolutePath()?.normalize() == normalizedSourcePath
        val context = if (activeMatchesPath) {
            requireNotNull(activeContext)
        } else {
            runtime.openWorkspace(normalizedSourcePath.parent ?: Path.of(".").toAbsolutePath()).activateProject(
                projectName = normalizedSourcePath.fileName.toString().substringBeforeLast('.'),
                sourcePath = normalizedSourcePath,
            )
        }
        val requiresRestore = !activeMatchesPath ||
            (context.commandRuntime().history(context).records.isEmpty() && context.latestSemanticDiffInspection() == null)
        if (!requiresRestore) {
            return AthenaCliExecutionSessionReady(
                AthenaCliExecutionSession(context = context, persistedLatestDiffText = null),
            )
        }

        return when (val restore = sessionStore.restore(context)) {
            AthenaCliPersistedSessionRestoreResult.NoSession -> AthenaCliExecutionSessionReady(
                AthenaCliExecutionSession(
                    context = context,
                    persistedLatestDiffText = null,
                ),
            )

            is AthenaCliPersistedSessionRestoreResult.Restored -> AthenaCliExecutionSessionReady(
                AthenaCliExecutionSession(
                    context = context,
                    persistedLatestDiffText = restore.latestDiffText,
                ),
            )

            is AthenaCliPersistedSessionRestoreResult.Failed -> AthenaCliExecutionSessionUnavailable(restore.reason)
        }
    }

    /** Persists command-history state after one successful runtime mutation or history operation. */
    private fun persistExecutionSession(context: AthenaExecutionContext) {
        sessionStore.save(
            context = context,
            latestDiffText = context.latestSemanticDiffInspection()?.renderLatestDiff(),
        )
    }

    /** Renders a direct authored-port lookup failure without fabricating canonical semantic ids. */
    private fun explicitConnectLookupRejection(
        projectName: String,
        authoredPortPath: String,
        endpointRole: String,
    ): String {
        return buildString {
            appendLine("Command rejected")
            appendLine("Project: $projectName")
            appendLine("Command: CONNECT_PORTS")
            append("$endpointRole port `$authoredPortPath` does not exist.")
        }
    }

    /** Renders command unavailability with syntax diagnostics when the source file itself is not compilable. */
    private fun unavailableCommandOutput(
        reason: String,
        compilation: CompilerCompilationParseFailure?,
    ): String {
        return buildString {
            appendLine("Command unavailable")
            compilation?.diagnostics?.forEach { diagnostic ->
                appendLine("${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}")
            }
            append(reason)
        }
    }

    /** Normalizes and validates the source file argument before the CLI opens a runtime-managed project. */
    private fun normalizedExistingPath(pathArgument: String): Path? {
        val path = Path.of(pathArgument)
        if (!Files.exists(path)) {
            return null
        }
        return path.toAbsolutePath().normalize()
    }

    /** Shared message for commands that require an already active runtime-managed project. */
    private fun noActiveProjectMessage(): String {
        return "No active project. Run connect <source-file> <source-port-path> <target-port-path>, plugin-command <source-file> <contribution-id>, or pass <source-file> to history-backed commands."
    }
}

/** CLI-owned active command session derived from either the in-memory runtime or a persisted one-shot sidecar. */
private data class AthenaCliExecutionSession(
    val context: AthenaExecutionContext,
    val persistedLatestDiffText: String?,
)

/** Result wrapper for opening a runtime-backed execution session from CLI arguments. */
private sealed interface AthenaCliExecutionSessionResult

private data class AthenaCliExecutionSessionReady(
    val session: AthenaCliExecutionSession,
) : AthenaCliExecutionSessionResult

private data class AthenaCliExecutionSessionUnavailable(
    val message: String,
) : AthenaCliExecutionSessionResult

/** CLI-only declaration summary derived from the syntax-owned AST. */
private data class CliParseSummary(
    val systemName: String,
    val deviceDeclarations: Int,
    val portDeclarations: Int,
    val connectionDeclarations: Int,
)

/** Builds the user-facing authored path for one canonical port. */
private fun EngineeringPort.authoredPath(): String = (ownerReference.authoredPath + name).joinToString(".")

private fun AthenaSemanticDiffInspection.renderLatestDiff(): String {
    return buildString {
        appendLine("Latest semantic diff")
        appendLine("Project: $projectName")
        appendLine("Source: ${source.name}")
        appendLine("Commands: ${affectedCommandIds.joinToString(", ")}")
        entries.forEachIndexed { index, entry ->
            append("${entry.changeKind.name} ${entry.semanticId}")
            val summary = entry.afterSummary ?: entry.beforeSummary
            if (!summary.isNullOrBlank()) {
                append(" => $summary")
            }
            if (index < entries.lastIndex) {
                appendLine()
            }
        }
    }
}

private fun AthenaSemanticDiffInspection.renderHistoryConsequence(): String {
    val consequence = historyConsequences.firstOrNull()
    return buildString {
        appendLine("History consequence")
        appendLine("Project: $projectName")
        appendLine("Command: ${affectedCommandIds.joinToString(", ")}")
        appendLine("Status: ${consequence?.status?.name ?: "UNKNOWN"}")
        entries.forEachIndexed { index, entry ->
            append("${entry.changeKind.name} ${entry.semanticId}")
            val summary = entry.afterSummary ?: entry.beforeSummary
            if (!summary.isNullOrBlank()) {
                append(" => $summary")
            }
            if (index < entries.lastIndex) {
                appendLine()
            }
        }
    }
}
