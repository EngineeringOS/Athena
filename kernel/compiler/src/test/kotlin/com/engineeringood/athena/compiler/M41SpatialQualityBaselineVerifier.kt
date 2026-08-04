package com.engineeringood.athena.compiler

import java.time.Instant

internal object M41SpatialQualityBaselineVerifier {
    fun verify(
        artifactBytes: ByteArray,
        compiled: DedicatedM41CompiledExample,
        expectedGeneratedAt: Instant,
    ): M41SpatialQualityBaseline {
        val artifact = M41SpatialQualityBaselineCodec.decode(artifactBytes)
        require(artifact.generatedAt == expectedGeneratedAt) {
            "Baseline field 'generation.timestamp' is '${artifact.generatedAt}' but committed evidence expects " +
                "'$expectedGeneratedAt'; regenerate the M41 baseline intentionally."
        }
        val expectedCommand =
            ".\\gradlew.bat :kernel:compiler:generateM41SpatialQualityBaseline " +
                "-Pm41BaselineTimestamp=${artifact.generatedAt}"
        require(artifact.generationCommand == expectedCommand) {
            "Baseline field 'generation.command' must be '$expectedCommand'; regenerate through the Gradle task."
        }
        val actual = M41SpatialQualityBaselineProjector.project(
            compiled = compiled,
            generatedAt = artifact.generatedAt,
            generationCommand = expectedCommand,
        )
        val artifactFields = M41SpatialQualityBaselineCodec.fields(artifact)
        val actualFields = M41SpatialQualityBaselineCodec.fields(actual)
        val differingField = (artifactFields.keys + actualFields.keys).sorted().firstOrNull { key ->
            artifactFields[key] != actualFields[key]
        }
        require(differingField == null) {
            "Baseline field '$differingField' is '${artifactFields[differingField]}' but fresh compiler facts are " +
                "'${actualFields[differingField]}'; regenerate the M41 baseline."
        }
        return artifact
    }
}
