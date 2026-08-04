package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.spatial.SpatialDocument
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

internal data class DedicatedM41ExampleSource(
    val repositoryRoot: Path,
    val sampleRoot: Path,
    val sourcePath: Path,
    val repositoryRelativePath: String,
    val bytes: ByteArray,
)

internal data class DedicatedM41CompiledExample(
    val source: DedicatedM41ExampleSource,
    val projection: ProjectionDocument,
    val spatialDocument: SpatialDocument,
    val presentationDocument: PresentationDocument,
)

internal fun loadDedicatedM41ExampleSource(
    repositoryRoot: Path = resolveAthenaRepositoryRoot(),
): DedicatedM41ExampleSource {
    val normalizedRoot = repositoryRoot.toAbsolutePath().normalize()
    val sampleRoot = normalizedRoot.resolve("examples/m41/rolling-shutter")
    val sourcePath = sampleRoot.resolve(
        "src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena",
    )
    require(Files.isRegularFile(sampleRoot.resolve("athena.yaml"))) {
        "Dedicated M41 example manifest is missing: ${sampleRoot.resolve("athena.yaml")}"
    }
    require(Files.isRegularFile(sourcePath)) { "Dedicated M41 example source is missing: $sourcePath" }
    return DedicatedM41ExampleSource(
        repositoryRoot = normalizedRoot,
        sampleRoot = sampleRoot,
        sourcePath = sourcePath,
        repositoryRelativePath = normalizedRoot.relativize(sourcePath).toString().replace('\\', '/'),
        bytes = Files.readAllBytes(sourcePath),
    )
}

internal fun compileDedicatedM41Example(
    source: DedicatedM41ExampleSource = loadDedicatedM41ExampleSource(),
    compiler: AthenaCompiler = AthenaCompiler(),
): DedicatedM41CompiledExample {
    val sourceText = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(source.bytes))
            .toString()
    } catch (error: Exception) {
        throw IllegalArgumentException("Dedicated M41 source must contain valid UTF-8.", error)
    }
    val compilation = compiler.compile(source.sourcePath, sourceText)
    require(compilation is CompilerCompilationSuccess) {
        val diagnostics = (compilation as CompilerCompilationParseFailure).diagnostics.joinToString("\n") { diagnostic ->
            "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
        }
        "Dedicated M41 source did not compile:\n$diagnostics"
    }
    require(compilation.semanticResult.diagnostics.isEmpty()) {
        compilation.semanticResult.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message }
    }
    require(compilation.authoredProjectionDiagnostics.isEmpty()) {
        compilation.authoredProjectionDiagnostics.joinToString("\n")
    }
    require(compilation.realityTransformationDiagnostics.isEmpty()) {
        compilation.realityTransformationDiagnostics.joinToString("\n") { diagnostic -> diagnostic.message }
    }
    val projection = compilation.authoredProjectionViews.singleOrNull()
        ?: error("Dedicated M41 compilation must publish exactly one authored Projection document.")
    val spatialDocument = compilation.spatialDocuments.singleOrNull()
        ?: error("Dedicated M41 compilation must retain exactly one validated Spatial document.")
    val sheetIds = spatialDocument.sheets.map { sheet -> sheet.sheetId }.toSet()
    val presentationDocument = compilation.presentations.singleOrNull { presentation ->
        presentation.view.id == projection.view.id && presentation.drawingComposition?.sheetId in sheetIds
    } ?: error("Dedicated M41 compilation must publish exactly one matching Presentation document.")
    return DedicatedM41CompiledExample(source, projection, spatialDocument, presentationDocument)
}

internal fun sha256Digest(bytes: ByteArray): String = "sha256:" + MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

internal fun resolveAthenaRepositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath().normalize()
    while (current.parent != null) {
        if (Files.isRegularFile(current.resolve("settings.gradle.kts")) && Files.isDirectory(current.resolve("examples"))) {
            return current
        }
        current = current.parent
    }
    error("Could not locate Athena repository root.")
}
