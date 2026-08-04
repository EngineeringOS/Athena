package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.format.DateTimeParseException

internal object M41SpatialQualityBaselineGenerator {
    private const val ARTIFACT_PATH =
        "_bmad-output/implementation-artifacts/m41/m41-spatial-quality-baseline.properties"

    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 2) {
            "Expected repository root and m41BaselineTimestamp arguments."
        }
        val repositoryRoot = Path.of(args[0]).toAbsolutePath().normalize()
        val generatedAt = parseTimestamp(args[1])
        val command =
            ".\\gradlew.bat :kernel:compiler:generateM41SpatialQualityBaseline " +
                "-Pm41BaselineTimestamp=$generatedAt"
        val compiled = compileDedicatedM41Example(loadDedicatedM41ExampleSource(repositoryRoot))
        val output = writeArtifact(repositoryRoot, generate(compiled, generatedAt, command))
        println("Generated ${repositoryRoot.relativize(output).toString().replace('\\', '/')} at $generatedAt")
    }

    fun writeArtifact(repositoryRoot: Path, bytes: ByteArray): Path {
        val normalizedRoot = repositoryRoot.toAbsolutePath().normalize()
        val output = normalizedRoot.resolve(ARTIFACT_PATH).normalize()
        require(output.startsWith(normalizedRoot)) { "M41 baseline output must remain at $ARTIFACT_PATH." }
        Files.createDirectories(requireNotNull(output.parent))
        val realRoot = normalizedRoot.toRealPath()
        val realParent = output.parent.toRealPath()
        require(realParent.startsWith(realRoot)) {
            "M41 baseline output parent must remain inside the repository and must not use a symlink or junction."
        }
        require(!Files.exists(output, LinkOption.NOFOLLOW_LINKS) || !Files.isSymbolicLink(output)) {
            "M41 baseline output must not be a symbolic link."
        }

        val temporary = Files.createTempFile(output.parent, ".m41-spatial-quality-baseline-", ".tmp")
        try {
            Files.write(temporary, bytes)
            Files.move(
                temporary,
                output,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } finally {
            Files.deleteIfExists(temporary)
        }
        return output
    }

    fun generate(
        compiled: DedicatedM41CompiledExample,
        generatedAt: Instant,
        generationCommand: String,
    ): ByteArray = M41SpatialQualityBaselineCodec.encode(
        M41SpatialQualityBaselineProjector.project(compiled, generatedAt, generationCommand),
    )

    fun parseTimestamp(value: String): Instant {
        require(value.isNotBlank() && value.endsWith("Z")) {
            "m41BaselineTimestamp must be an explicit UTC ISO-8601 instant ending in Z."
        }
        val instant = try {
            Instant.parse(value)
        } catch (error: DateTimeParseException) {
            throw IllegalArgumentException("m41BaselineTimestamp must be an explicit UTC ISO-8601 instant.", error)
        }
        require(instant.toString() == value) {
            "m41BaselineTimestamp must use canonical Instant text '$instant'."
        }
        return instant
    }
}
