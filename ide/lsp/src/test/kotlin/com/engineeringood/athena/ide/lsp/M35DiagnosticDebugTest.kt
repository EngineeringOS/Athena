package com.engineeringood.athena.ide.lsp

import java.nio.file.Files
import java.nio.file.Path
import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentItem
import kotlin.test.Test
import kotlin.test.assertTrue

class M35DiagnosticDebugTest {
    @Test
    @Suppress("DEPRECATION")
    fun `m35 sample source diagnostics are empty`() {
        val sampleRoot = repositoryRoot().resolve("examples/m35/physical-installation-cabinet")
        val source = sampleRoot.resolve("src/com/engineeringood/m35/physicalinstallationcabinet/01-physical-installation-cabinet.athena")
        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        try {
            AthenaCompiler().materializeRepositoryLock(sampleRoot)
            server.connect(client)
            server.initialize(InitializeParams().apply { rootUri = sampleRoot.toUri().toString() }).get()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(source.toUri().toString(), "athena", 1, Files.readString(source)),
                ),
            )

            val diagnostics = client.publishedDiagnostics.last().diagnostics
            assertTrue(
                diagnostics.isEmpty(),
                diagnostics.joinToString("\n") { diagnostic ->
                    "${diagnostic.source} ${diagnostic.code.left}: ${diagnostic.message}"
                },
            )
        } finally {
            server.shutdown().get()
        }
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}
