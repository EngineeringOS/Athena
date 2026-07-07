package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.plugin.AthenaCoreVersion
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** Loads and validates one local directory-backed governed knowledge package for the M0 boundary proof. */
class AthenaKnowledgePackageLoader {
    /** Loads the governed knowledge package rooted at [packageRoot] and returns either the loaded package or diagnostics. */
    fun load(packageRoot: Path): AthenaKnowledgePackageLoadResult {
        if (!Files.isDirectory(packageRoot)) {
            return AthenaKnowledgePackageLoadResult(
                loadedPackage = null,
                diagnostics = listOf(
                    diagnostic(
                        ruleId = "knowledge.package.root.not-directory",
                        subject = "packageRoot",
                        message = "Governed knowledge packages must be loaded from a directory root.",
                    ),
                ),
            )
        }

        val manifestPath = packageRoot.resolve(MANIFEST_FILE_NAME)
        if (!Files.exists(manifestPath)) {
            return AthenaKnowledgePackageLoadResult(
                loadedPackage = null,
                diagnostics = listOf(
                    diagnostic(
                        ruleId = "knowledge.package.manifest.missing",
                        subject = "manifest",
                        message = "Governed knowledge package manifest `$MANIFEST_FILE_NAME` is missing.",
                    ),
                ),
            )
        }

        val properties = runCatching { loadProperties(manifestPath) }.getOrElse { exception ->
            return AthenaKnowledgePackageLoadResult(
                loadedPackage = null,
                diagnostics = listOf(
                    diagnostic(
                        ruleId = "knowledge.package.manifest.unreadable",
                        subject = "manifest",
                        message = "Could not read governed knowledge package manifest: ${exception.message ?: exception::class.simpleName}",
                    ),
                ),
            )
        }

        val diagnostics = mutableListOf<AthenaKnowledgePackageDiagnostic>()

        val artifactId = properties.requiredValue("artifact.id").also { artifactId ->
            if (artifactId.isBlank()) {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.id.blank",
                    subject = "artifact.id",
                    message = "Governed knowledge artifact id must not be blank.",
                )
            }
        }

        val artifactKindText = properties.requiredValue("artifact.kind")
        val artifactKind = when {
            artifactKindText.isBlank() -> {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.kind.blank",
                    subject = "artifact.kind",
                    message = "Governed knowledge artifact kind must not be blank.",
                )
                null
            }

            else -> AthenaKnowledgeArtifactKind.entries.firstOrNull { it.name == artifactKindText }
                ?: run {
                    diagnostics += diagnostic(
                        ruleId = "knowledge.package.manifest.kind.unsupported",
                        subject = "artifact.kind",
                        message = "Governed knowledge artifact kind `$artifactKindText` is not supported in M0.",
                    )
                    null
                }
        }

        val packageFormatVersionText = properties.requiredValue("package.format.version")
        val packageFormatVersion = when {
            packageFormatVersionText.isBlank() -> {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.format-version.blank",
                    subject = "package.format.version",
                    message = "Package format version must not be blank.",
                )
                null
            }

            else -> packageFormatVersionText.toIntOrNull()?.also { formatVersion ->
                if (formatVersion != SUPPORTED_PACKAGE_FORMAT_VERSION) {
                    diagnostics += diagnostic(
                        ruleId = "knowledge.package.manifest.format-version.unsupported",
                        subject = "package.format.version",
                        message = "Package format version `$packageFormatVersionText` is not supported.",
                    )
                }
            } ?: run {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.format-version.invalid",
                    subject = "package.format.version",
                    message = "Package format version `$packageFormatVersionText` is invalid.",
                )
                null
            }
        }

        val artifactVersion = properties.requiredValue("artifact.version").also { version ->
            if (version.isBlank()) {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.version.blank",
                    subject = "artifact.version",
                    message = "Governed knowledge artifact version must not be blank.",
                )
            }
        }

        val provenanceSources = properties.csvValue("provenance.sources")
        if (provenanceSources.isEmpty()) {
            diagnostics += diagnostic(
                ruleId = "knowledge.package.manifest.provenance.sources.missing",
                subject = "provenance.sources",
                message = "At least one provenance source must be declared.",
            )
        }

        val reviewedBy = properties.requiredValue("provenance.reviewedBy").also { reviewer ->
            if (reviewer.isBlank()) {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.provenance.reviewed-by.blank",
                    subject = "provenance.reviewedBy",
                    message = "Provenance reviewer must not be blank.",
                )
            }
        }

        val minimumCompatibilityText = properties.requiredValue("compatibility.core.minimum")
        val minimumCompatibility = when {
            minimumCompatibilityText.isBlank() -> {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.compatibility.minimum.blank",
                    subject = "compatibility.core.minimum",
                    message = "Core compatibility minimum version must not be blank.",
                )
                null
            }

            else -> AthenaCoreVersion.parse(minimumCompatibilityText) ?: run {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.compatibility.minimum.invalid",
                    subject = "compatibility.core.minimum",
                    message = "Core compatibility minimum version `$minimumCompatibilityText` is invalid.",
                )
                null
            }
        }

        val maximumCompatibilityText = properties.optionalValue("compatibility.core.maximum")
        val maximumCompatibility = when {
            maximumCompatibilityText == null -> null
            maximumCompatibilityText.isBlank() -> {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.compatibility.maximum.blank",
                    subject = "compatibility.core.maximum",
                    message = "Core compatibility maximum version must not be blank when declared.",
                )
                null
            }

            else -> AthenaCoreVersion.parse(maximumCompatibilityText) ?: run {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.compatibility.maximum.invalid",
                    subject = "compatibility.core.maximum",
                    message = "Core compatibility maximum version `$maximumCompatibilityText` is invalid.",
                )
                null
            }
        }

        if (minimumCompatibility != null && maximumCompatibility != null && minimumCompatibility > maximumCompatibility) {
            diagnostics += diagnostic(
                ruleId = "knowledge.package.manifest.compatibility.range.invalid",
                subject = "compatibility.core",
                message = "Core compatibility minimum must not exceed the declared maximum.",
            )
        }

        val payloadEntries = loadPayloadEntries(properties, packageRoot, diagnostics)

        if (diagnostics.isNotEmpty()) {
            return AthenaKnowledgePackageLoadResult(
                loadedPackage = null,
                diagnostics = diagnostics,
            )
        }

        return AthenaKnowledgePackageLoadResult(
            loadedPackage = AthenaKnowledgeArtifactPackage(
                rootDirectory = packageRoot,
                manifest = AthenaKnowledgeArtifactManifest(
                    artifactId = artifactId,
                    artifactKind = checkNotNull(artifactKind),
                    packageFormatVersion = checkNotNull(packageFormatVersion),
                    artifactVersion = artifactVersion,
                    provenance = AthenaKnowledgeProvenance(
                        sources = provenanceSources,
                        reviewedBy = reviewedBy,
                    ),
                    coreCompatibility = AthenaKnowledgeCoreCompatibilityRange(
                        minimumInclusive = minimumCompatibilityText,
                        maximumInclusive = maximumCompatibilityText,
                    ),
                ),
                payloadEntries = payloadEntries,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun loadPayloadEntries(
        properties: Properties,
        packageRoot: Path,
        diagnostics: MutableList<AthenaKnowledgePackageDiagnostic>,
    ): List<AthenaKnowledgePayloadEntry> {
        val normalizedPackageRoot = packageRoot.toAbsolutePath().normalize()
        val payloadKeys = properties.stringPropertyNames()
            .mapNotNull { key -> PAYLOAD_KEY_PATTERN.matchEntire(key) }
            .map { match -> match.groupValues[1] }
            .toSortedSet()

        if (payloadKeys.isEmpty()) {
            diagnostics += diagnostic(
                ruleId = "knowledge.package.manifest.payload.missing",
                subject = "payload",
                message = "At least one typed payload entry must be declared.",
            )
            return emptyList()
        }

        return payloadKeys.mapNotNull { entryId ->
            val entryKind = properties.requiredValue("payload.$entryId.kind")
            if (entryKind.isBlank()) {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.payload.kind.blank",
                    subject = "payload.$entryId.kind",
                    message = "Payload entry kind must not be blank.",
                )
            }

            val entryPath = properties.requiredValue("payload.$entryId.path")
            if (entryPath.isBlank()) {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.manifest.payload.path.blank",
                    subject = "payload.$entryId.path",
                    message = "Payload entry path must not be blank.",
                )
            }

            if (entryKind.isBlank() || entryPath.isBlank()) {
                return@mapNotNull null
            }

            val resolvedPath = normalizedPackageRoot.resolve(entryPath).normalize()
            if (!resolvedPath.startsWith(normalizedPackageRoot)) {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.payload.path.outside-root",
                    subject = "payload.$entryId.path",
                    message = "Declared payload path `$entryPath` must stay within the package root.",
                )
                return@mapNotNull null
            }

            if (!Files.isRegularFile(resolvedPath)) {
                diagnostics += diagnostic(
                    ruleId = "knowledge.package.payload.path.missing",
                    subject = "payload.$entryId.path",
                    message = "Declared payload path `$entryPath` does not exist within the package root.",
                )
                return@mapNotNull null
            }

            AthenaKnowledgePayloadEntry(
                entryId = entryId,
                entryKind = entryKind,
                relativePath = entryPath,
                resolvedPath = resolvedPath,
            )
        }
    }

    private fun loadProperties(manifestPath: Path): Properties {
        return Properties().apply {
            Files.newInputStream(manifestPath).use(::load)
        }
    }

    private fun diagnostic(
        ruleId: String,
        subject: String,
        message: String,
    ): AthenaKnowledgePackageDiagnostic {
        return AthenaKnowledgePackageDiagnostic(
            severity = AthenaKnowledgePackageSeverity.ERROR,
            ruleId = AthenaKnowledgePackageRuleId(ruleId),
            subject = subject,
            message = message,
        )
    }
}

private const val MANIFEST_FILE_NAME = "athena-knowledge.properties"
private const val SUPPORTED_PACKAGE_FORMAT_VERSION = 1
private val PAYLOAD_KEY_PATTERN = Regex("""payload\.([^.]+)\.(kind|path)""")

private fun Properties.requiredValue(key: String): String = getProperty(key)?.trim().orEmpty()

private fun Properties.optionalValue(key: String): String? = getProperty(key)?.trim()

private fun Properties.csvValue(key: String): List<String> {
    return requiredValue(key)
        .split(',')
        .map(String::trim)
        .filter(String::isNotEmpty)
}
