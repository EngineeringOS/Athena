package com.engineeringood.athena.cli

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.PortDeclaration
import java.nio.file.Files
import java.nio.file.Path

class BootstrapCli(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun run(args: List<String>): String = when (args.firstOrNull()) {
        null, "help", "--help", "-h" -> help()
        "parse" -> parse(args.drop(1))
        else -> "Unknown command `${args.first()}`.\n\n${help()}"
    }

    private fun parse(arguments: List<String>): String {
        if (arguments.size != 1) {
            return "Usage: athena parse <source-file>"
        }
        val path = Path.of(arguments.single()).toAbsolutePath().normalize()
        if (!Files.isRegularFile(path)) {
            return "Athena source file does not exist: $path"
        }

        return when (val result = parser.parse(path.toString().replace('\\', '/'), Files.readString(path))) {
            is ParseSuccess -> {
                val declarations = result.ast.declarations
                val entities = declarations.filterIsInstance<EntityDeclaration>()
                val topLevelPorts = declarations.filterIsInstance<PortDeclaration>()
                val nestedPorts = entities.sumOf { entity ->
                    entity.nestedPorts.size + entity.nestedFunctions.sumOf { function -> function.nestedPorts.size }
                }
                buildString {
                    appendLine("Parse successful")
                    appendLine("System: ${result.ast.system.name}")
                    appendLine("Entities: ${entities.size}")
                    append("Ports: ${topLevelPorts.size + nestedPorts}")
                }
            }

            is ParseFailure -> buildString {
                appendLine("Syntax diagnostics")
                result.diagnostics.forEach { diagnostic ->
                    appendLine("${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}")
                }
                append("Pipeline stopped before engineering lowering.")
            }
        }
    }

    private fun help(): String = """
        Athena EngineeringOS CLI

        Commands:
          parse <source-file>  Parse Athena Source through the canonical ANTLR boundary
    """.trimIndent()
}
