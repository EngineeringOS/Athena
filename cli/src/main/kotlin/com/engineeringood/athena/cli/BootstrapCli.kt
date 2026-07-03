package com.engineeringood.athena.cli

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerParseFailure
import com.engineeringood.athena.compiler.CompilerParseSuccess
import com.engineeringood.athena.compiler.CompilerModuleMarker
import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainMarker
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.renderer.svg.SvgRendererModuleMarker
import java.nio.file.Files
import java.nio.file.Path

/** Small CLI facade that exposes the current bootstrap and parse commands. */
class BootstrapCli(
    private val compilerModuleMarker: CompilerModuleMarker = CompilerModuleMarker(),
    private val domainMarker: ElectricalRuntimeDomainMarker = ElectricalRuntimeDomainMarker(),
    private val rendererMarker: SvgRendererModuleMarker = SvgRendererModuleMarker(),
    private val compiler: AthenaCompiler = AthenaCompiler(),
) {
    /** Executes the requested CLI command and returns plain-text output for the shell. */
    fun run(args: List<String>): String {
        if (args.isEmpty() || args == listOf("--help") || args == listOf("-h")) {
            return helpText()
        }

        if (args.firstOrNull() == "parse") {
            return parseCommand(args.drop(1))
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

        val path = Path.of(arguments.single())
        if (!Files.exists(path)) {
            return "File not found: $path"
        }

        return when (val result = compiler.parse(path)) {
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

    private fun helpText(): String = buildString {
        appendLine("Athena M0 compiler workspace")
        appendLine("Java 25")
        appendLine("Gradle 9.6.1")
        appendLine(
            "Modules: cli, language, semantics-core, ir, compiler, domain-electrical-runtime, renderer-svg",
        )
        appendLine(
            "Connected markers: ${compilerModuleMarker.moduleName}, ${domainMarker.moduleName}, ${rendererMarker.moduleName}",
        )
        append("Command surface: --help, parse <source-file>")
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
}

/** CLI-only declaration summary derived from the syntax-owned AST. */
private data class CliParseSummary(
    val systemName: String,
    val deviceDeclarations: Int,
    val portDeclarations: Int,
    val connectionDeclarations: Int,
)
