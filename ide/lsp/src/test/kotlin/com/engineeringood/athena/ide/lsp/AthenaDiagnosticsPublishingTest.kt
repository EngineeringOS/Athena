package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnostic
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticCode
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticSeverity
import com.engineeringood.athena.compiler.semantic.ProjectSemanticRelatedLocation
import com.engineeringood.athena.compiler.semantic.SourceUnitId
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import java.nio.file.Files
import java.nio.file.Path
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies that Athena LSP publishes JVM-sourced diagnostics for open and change events.
 */
class AthenaDiagnosticsPublishingTest {
    @Test
    @Suppress("DEPRECATION")
    fun `publish diagnostics for invalid open text and clear after valid change`() {
        val repository = createGovernedTestRepository("athena-lsp-diagnostics-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        "system FactoryLine {\n  connect Motor1.out -> Missing.in\n}",
                    ),
                ),
            )

            assertTrue(client.publishedDiagnostics.isNotEmpty())
            val invalidOpen = client.publishedDiagnostics.last()
            assertEquals(2, invalidOpen.diagnostics.size)
            assertTrue(invalidOpen.diagnostics.all { diagnostic -> diagnostic.source == "Athena semantic" })

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(sourcePath.toUri().toString(), 2),
                    listOf(
                        TextDocumentContentChangeEvent(
                            "system FactoryLine {\n  device Motor1 {\n    type Motor\n  }\n  device Missing {\n    type Motor\n  }\n  port Motor1.out {\n    direction out\n    signal Digital\n  }\n  port Missing.in {\n    direction in\n    signal Digital\n  }\n  connect Motor1.out -> Missing.in\n}",
                        ),
                    ),
                ),
            )

            val validChange = client.publishedDiagnostics.last()
            assertEquals(0, validChange.diagnostics.size)
            assertTrue(
                client.loggedMessages.any { message ->
                    message.contains("Athena diagnostics published from JVM stack")
                },
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `publish knowledge diagnostics for governed engineering insufficiency through normal lsp problems flow`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-knowledge-diagnostics-",
            sourceFileName = "motor-proof.athena",
            sourceText = m9KnowledgeProofSource,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        m9KnowledgeProofSource,
                    ),
                ),
            )

            val knowledgeOpen = client.publishedDiagnostics.last()
            assertEquals(3, knowledgeOpen.diagnostics.size)
            assertTrue(knowledgeOpen.diagnostics.all { diagnostic -> diagnostic.source == "Athena knowledge" })
            assertTrue(knowledgeOpen.diagnostics.any { diagnostic -> diagnostic.code.left == "knowledge.protection_sufficiency" })
            assertTrue(knowledgeOpen.diagnostics.any { diagnostic -> diagnostic.message.contains("Breaker current 10A is below required 18A") })
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `publish package aware project semantic diagnostics through normal lsp problems flow`() {
        val sourceText = """
            package com.engineeringood.factoryline
            import com.vendor

            system FactoryLine {
              device Local {}
              port Local.out {}
            }
        """.trimIndent()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-package-diagnostics-",
            packageName = "com.engineeringood.factoryline",
            sourceText = sourceText,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        AthenaCompiler().materializeRepositoryLock(repositoryRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val packageDiagnostics = client.publishedDiagnostics.last()
            val projectCodes = server.trackedDocument(sourcePath.toUri().toString())
                ?.projectSemanticDiagnostics
                ?.map { diagnostic -> diagnostic.code.value }
                .orEmpty()
            assertNotNull(server.trackedDocument(sourcePath.toUri().toString())?.projectSemanticGraphId)
            val codes = packageDiagnostics.diagnostics.map { diagnostic -> diagnostic.code.left }
            assertTrue(
                "semantic.import.namespace.unavailable" in codes,
                "Published diagnostic codes: $codes; project diagnostic codes: $projectCodes",
            )
            val diagnostic = packageDiagnostics.diagnostics.single { published ->
                published.code.left == "semantic.import.namespace.unavailable"
            }
            assertEquals("Athena package semantic", diagnostic.source)
            assertEquals(DiagnosticSeverity.Error, diagnostic.severity)
            assertTrue(diagnostic.message.contains("Import `com.vendor` does not match an available semantic namespace"))
            assertEquals(1, diagnostic.range.start.line)
            assertEquals(7, diagnostic.range.start.character)
            assertEquals(1, diagnostic.range.end.line)
            assertEquals(17, diagnostic.range.end.character)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `valid m23 layout block publishes no false lsp diagnostics`() {
        val sourceText = """
            package com.engineeringood.factoryline

            system FactoryLine {
              device PLC1 {
                type Switch
              }
              device HMI1 {
                type Switch
              }
              device XT1 {
                type Switch
              }

              layout schematic-sheet {
                place HMI1 near PLC1
                place XT1 below PLC1
                align HMI1 aligned-with PLC1 axis vertical
                group HMI1 grouped-with PLC1
              }
            }
        """.trimIndent()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-m23-valid-layout-",
            packageName = "com.engineeringood.factoryline",
            sourceText = sourceText,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        AthenaCompiler().materializeRepositoryLock(repositoryRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val diagnostics = client.publishedDiagnostics.last().diagnostics
            assertEquals(
                0,
                diagnostics.size,
                buildString {
                    appendLine("Published diagnostics:")
                    diagnostics.forEach { diagnostic ->
                        appendLine("- ${diagnostic.code?.left ?: "<no-code>"}: ${diagnostic.message}")
                    }
                },
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `invalid m23 layout syntax publishes normal syntax diagnostics through lsp`() {
        val invalidSources = listOf(
            "invalid relation" to """
                package com.engineeringood.factoryline

                system FactoryLine {
                  device PLC1 {}
                  device HMI1 {}

                  layout schematic-sheet {
                    place HMI1 beside PLC1
                  }
                }
            """.trimIndent(),
            "missing target" to """
                package com.engineeringood.factoryline

                system FactoryLine {
                  device PLC1 {}
                  device HMI1 {}

                  layout schematic-sheet {
                    place HMI1 near
                  }
                }
            """.trimIndent(),
            "invalid axis" to """
                package com.engineeringood.factoryline

                system FactoryLine {
                  device PLC1 {}
                  device HMI1 {}

                  layout schematic-sheet {
                    align HMI1 aligned-with PLC1 axis diagonal
                  }
                }
            """.trimIndent(),
        )

        invalidSources.forEach { (caseName, sourceText) ->
            val repository = createGovernedTestRepository(
                prefix = "athena-lsp-m23-invalid-layout-",
                packageName = "com.engineeringood.factoryline",
                sourceText = sourceText,
            )
            val repositoryRoot = repository.repositoryRoot
            val sourcePath = repository.seedSourcePath

            val client = AthenaRecordingLanguageClient()
            val server = AthenaLanguageServer()
            server.connect(client)

            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            sourcePath.toUri().toString(),
                            "athena",
                            1,
                            sourceText,
                        ),
                    ),
                )

                val diagnostics = client.publishedDiagnostics.last().diagnostics
                assertTrue(
                    diagnostics.any { diagnostic ->
                        diagnostic.source == "Athena syntax" &&
                            diagnostic.code.left == "syntax" &&
                            diagnostic.range.start.line >= 0
                    },
                    "$caseName diagnostics: ${diagnostics.map { diagnostic -> diagnostic.source to diagnostic.message }}",
                )
            } finally {
                server.shutdown().get()
                repositoryRoot.toFile().deleteRecursively()
            }
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `unknown m23 layout references publish project semantic diagnostics through lsp`() {
        val sourceText = """
            package com.engineeringood.factoryline

            system FactoryLine {
              device PLC1 {
                type Switch
              }

              layout schematic-sheet {
                place HMI1 near PLC1
                place PLC1 below XT1
              }
            }
        """.trimIndent()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-m23-layout-semantic-",
            packageName = "com.engineeringood.factoryline",
            sourceText = sourceText,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        AthenaCompiler().materializeRepositoryLock(repositoryRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val diagnostics = client.publishedDiagnostics.last().diagnostics
            val layoutDiagnostics = diagnostics.filter { diagnostic ->
                diagnostic.code.left == "semantic.layout.reference.unknown"
            }
            assertEquals(
                2,
                layoutDiagnostics.size,
                "Published diagnostics: ${diagnostics.map { diagnostic -> diagnostic.code.left to diagnostic.message }}",
            )
            assertTrue(layoutDiagnostics.all { diagnostic -> diagnostic.source == "Athena package semantic" })
            assertTrue(layoutDiagnostics.all { diagnostic -> diagnostic.severity == DiagnosticSeverity.Error })
            assertTrue(layoutDiagnostics.all { diagnostic -> diagnostic.range.start.line >= 0 })
            assertTrue(layoutDiagnostics.any { diagnostic -> diagnostic.message.contains("`HMI1`") })
            assertTrue(layoutDiagnostics.any { diagnostic -> diagnostic.message.contains("`XT1`") })
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `m20 sample project open publishes no diagnostics for boundary scope source`() {
        val repoRoot = resolveRepoRoot()
        val sampleProjectRoot = repoRoot.resolve("examples/m20/sample-project")
        val sourcePath = sampleProjectRoot.resolve("src/04-boundary-scope.athena")
        val sourceText = Files.readString(sourcePath)

        AthenaCompiler().materializeRepositoryLock(sampleProjectRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = sampleProjectRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val diagnostics = client.publishedDiagnostics.last().diagnostics
            assertEquals(
                0,
                diagnostics.size,
                buildString {
                    appendLine("Published diagnostics for ${sourcePath.fileName}:")
                    diagnostics.forEach { diagnostic ->
                        appendLine("- ${diagnostic.code?.left ?: "<no-code>"}: ${diagnostic.message}")
                    }
                },
            )
        } finally {
            server.shutdown().get()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `m20 sample project showcase files open without diagnostics`() {
        val repoRoot = resolveRepoRoot()
        val sampleProjectRoot = repoRoot.resolve("examples/m20/sample-project")
        val sourcePaths = listOf(
            sampleProjectRoot.resolve("src/01-schematic-sheet.athena"),
            sampleProjectRoot.resolve("src/02-dense-sheet.athena"),
            sampleProjectRoot.resolve("src/03-acceptance-sheet.athena"),
            sampleProjectRoot.resolve("src/04-boundary-scope.athena"),
        )

        AthenaCompiler().materializeRepositoryLock(sampleProjectRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = sampleProjectRoot.toUri().toString()
                },
            ).get()

            sourcePaths.forEach { sourcePath ->
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            sourcePath.toUri().toString(),
                            "athena",
                            1,
                            Files.readString(sourcePath),
                        ),
                    ),
                )

                val diagnostics = client.publishedDiagnostics.last().diagnostics
                assertEquals(
                    0,
                    diagnostics.size,
                    buildString {
                        appendLine("Published diagnostics for ${sourcePath.fileName}:")
                        diagnostics.forEach { diagnostic ->
                            appendLine("- ${diagnostic.code?.left ?: "<no-code>"}: ${diagnostic.message}")
                        }
                    },
                )
            }
        } finally {
            server.shutdown().get()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `m23 sample project layout source opens without diagnostics`() {
        val repoRoot = resolveRepoRoot()
        val sampleProjectRoot = repoRoot.resolve("examples/m23/sample-project")
        val sourcePath = sampleProjectRoot.resolve("src/01-layout-hints.athena")
        val sourceText = Files.readString(sourcePath)

        AthenaCompiler().materializeRepositoryLock(sampleProjectRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = sampleProjectRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val diagnostics = client.publishedDiagnostics.last().diagnostics
            assertEquals(
                0,
                diagnostics.size,
                buildString {
                    appendLine("Published diagnostics for ${sourcePath.fileName}:")
                    diagnostics.forEach { diagnostic ->
                        appendLine("- ${diagnostic.code?.left ?: "<no-code>"}: ${diagnostic.message}")
                    }
                },
            )
        } finally {
            server.shutdown().get()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `project semantic diagnostics use the authored package identity instead of always using the root package`() {
        val sourceText = """
            package com.vendor

            system Vendor {
              device Local {}
            }
        """.trimIndent()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-non-root-package-diagnostics-",
            packageName = "com.engineeringood.factoryline",
            sourceText = sourceText,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        AthenaCompiler().materializeRepositoryLock(repositoryRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val projectCodes = server.trackedDocument(sourcePath.toUri().toString())
                ?.projectSemanticDiagnostics
                ?.map { diagnostic -> diagnostic.code.value }
                .orEmpty()
            assertTrue("semantic.source.package.not-admitted" in projectCodes)
            assertTrue("semantic.source.package.mismatch" !in projectCodes)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `package aware diagnostics use governed sibling source units in the project snapshot`() {
        val repositoryRoot = createTempDirectory("athena-lsp-project-package-diagnostics-")
        val sourceRoot = repositoryRoot.resolve("src").createDirectories()
        val sourcePath = sourceRoot.resolve("consumer.athena")
        val sourceText = """
            package com.root

            system Consumer {
              device Local {}
              port Local.in {}
              connect Shared.out -> Local.in
            }
        """.trimIndent()
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.root
                      version: 0.1.0
                      sourceRoot: src
                """.trimIndent(),
            )
            sourcePath.writeText(sourceText)
            sourceRoot.resolve("shared.athena").writeText(
                """
                    package com.root

                    system SharedProvider {
                      device Shared {}
                      port Shared.out {}
                    }
                """.trimIndent(),
            )
            val compiler = AthenaCompiler()
            compiler.materializeRepositoryLock(repositoryRoot)

            val client = AthenaRecordingLanguageClient()
            val server = AthenaLanguageServer()
            server.connect(client)

            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            sourcePath.toUri().toString(),
                            "athena",
                            1,
                            sourceText,
                        ),
                    ),
                )

                val codes = client.publishedDiagnostics.last().diagnostics.map { diagnostic -> diagnostic.code.left }
                assertTrue("semantic.reference.unresolved" !in codes, "Published diagnostic codes: $codes")
                val projectCodes = server.trackedDocument(sourcePath.toUri().toString())
                    ?.projectSemanticDiagnostics
                    ?.map { diagnostic -> diagnostic.code.value }
                    .orEmpty()
                assertTrue("semantic.reference.unresolved" !in projectCodes, "Project diagnostic codes: $projectCodes")
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `package aware diagnostics keep current source unit when siblings are indexed`() {
        val sourceText = """
            package com.root
            import com.missing

            system Consumer {
              device Local {}
            }
        """.trimIndent()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-current-source-unit-diagnostics-",
            packageName = "com.root",
            sourceFileName = "consumer.athena",
            sourceText = sourceText,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourceRoot = repository.sourceRoot
        val sourcePath = repository.seedSourcePath
        try {
            sourceRoot.resolve("sibling.athena").writeText(
                """
                    package com.root

                    system Sibling {
                      device Other {}
                    }
                """.trimIndent(),
            )
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            assertTrue(repositoryRoot.resolve("athena.lock").isRegularFile())

            val client = AthenaRecordingLanguageClient()
            val server = AthenaLanguageServer()
            server.connect(client)

            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            sourcePath.toUri().toString(),
                            "athena",
                            1,
                            sourceText,
                        ),
                    ),
                )

                val tracked = server.trackedDocument(sourcePath.toUri().toString())
                assertNotNull(
                    tracked?.projectSemanticSourceUnitId,
                    "graph=${tracked?.projectSemanticGraphId}, diagnostics=${tracked?.projectSemanticDiagnostics?.map { diagnostic -> diagnostic.code.value to diagnostic.sourceUnitId?.value }}",
                )
                val codes = client.publishedDiagnostics.last().diagnostics.map { diagnostic -> diagnostic.code.left }
                assertTrue(
                    "semantic.import.namespace.unavailable" in codes,
                    "Published diagnostic codes: $codes",
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `project semantic related locations use the resolved source unit uri`() {
        val currentSourceUnitId = SourceUnitId("source:current")
        val relatedSourceUnitId = SourceUnitId("source:related")
        val diagnostic = ProjectSemanticDiagnostic(
            code = ProjectSemanticDiagnosticCode("semantic.reference.ambiguous"),
            severity = ProjectSemanticDiagnosticSeverity.ERROR,
            message = "Ambiguous authored reference `Shared.out`.",
            sourceUnitId = currentSourceUnitId,
            sourceSpan = sourceSpan(10, 1, 1, 1, 7),
            relatedLocations = listOf(
                ProjectSemanticRelatedLocation(
                    sourceUnitId = relatedSourceUnitId,
                    sourceSpan = sourceSpan(20, 2, 3, 2, 9),
                    message = "Candidate declaration.",
                ),
            ),
        )

        val lspDiagnostic = listOf(diagnostic).toLspDiagnostics(
            documentUri = "file:///workspace/src/current.athena",
            currentSourceUnitId = currentSourceUnitId,
            sourceUnitUris = mapOf(
                currentSourceUnitId to "file:///workspace/src/current.athena",
                relatedSourceUnitId to "file:///workspace/src/related.athena",
            ),
        ).single()

        val relatedInformation = lspDiagnostic.relatedInformation.single()
        assertEquals("file:///workspace/src/related.athena", relatedInformation.location.uri)
        assertEquals("Candidate declaration.", relatedInformation.message)
        assertEquals(1, relatedInformation.location.range.start.line)
        assertEquals(2, relatedInformation.location.range.start.character)
    }
}

private fun sourceSpan(
    startOffset: Int,
    startLine: Int,
    startColumn: Int,
    endLine: Int,
    endColumn: Int,
): SourceSpan {
    return SourceSpan(
        start = SourcePosition(startOffset, startLine, startColumn),
        end = SourcePosition(startOffset + endColumn - startColumn, endLine, endColumn),
    )
}

private fun resolveRepoRoot(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
        current = current.parent
    }
    check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
    return current
}

class AthenaRecordingLanguageClient : LanguageClient {
    val publishedDiagnostics = mutableListOf<PublishDiagnosticsParams>()
    val loggedMessages = mutableListOf<String>()

    override fun telemetryEvent(`object`: Any?) = Unit

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {
        publishedDiagnostics += diagnostics
    }

    override fun showMessage(messageParams: MessageParams) = Unit

    override fun showMessageRequest(requestParams: ShowMessageRequestParams): CompletableFuture<MessageActionItem> {
        return CompletableFuture.completedFuture(null)
    }

    override fun logMessage(message: MessageParams) {
        loggedMessages += message.message
    }
}

private val m9KnowledgeProofSource = """
    system MotorDerivedContext {
      device M1 {
        type Motor
        power "7.5kw"
        voltage "400V"
        powerFactor "0.86"
        efficiency "0.92"
        breakerRatedCurrent "10A"
        cableAllowedCurrent "12A"
        relayRatedCurrent "13A"
      }
    }
""".trimIndent()
