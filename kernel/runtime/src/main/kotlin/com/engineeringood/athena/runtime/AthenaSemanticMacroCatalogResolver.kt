package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.reuse.SemanticMacroContract
import com.engineeringood.athena.reuse.SemanticMacroId
import com.engineeringood.athena.reuse.SemanticMacroPackageBinding
import com.engineeringood.athena.reuse.SemanticMacroParameterDefinition
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValidationRules
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import com.engineeringood.athena.reuse.SemanticMacroParameterValueKind
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/**
 * Resolves governed Semantic Macro contracts strictly from the existing repository graph.
 *
 * This resolver reuses `athena.lock` and the canonical package graph as the reproducibility anchor.
 * It does not introduce a second lockfile or ad hoc filesystem discovery outside allowed packages.
 */
internal class AthenaSemanticMacroCatalogResolver(
    private val packageLoader: AthenaSemanticMacroPackageLoader = AthenaSemanticMacroPackageLoader(),
) {
    fun resolve(
        repositoryRoot: Path,
        publication: AthenaRepositoryReportPublicationResult,
    ): AthenaSemanticMacroCatalogReady {
        val resolution = resolveContracts(repositoryRoot, publication)
        return AthenaSemanticMacroCatalogReady(
            entries = resolution.contracts.map { contract -> contract.toCatalogEntry() },
            diagnostics = resolution.diagnostics,
        )
    }

    fun resolveContracts(
        repositoryRoot: Path,
        publication: AthenaRepositoryReportPublicationResult,
    ): AthenaSemanticMacroContractResolution {
        val report = publication.report ?: return AthenaSemanticMacroContractResolution(emptyList())
        val graph = report.graph ?: return AthenaSemanticMacroContractResolution(emptyList())
        val contracts = mutableListOf<SemanticMacroContract>()
        val diagnostics = mutableListOf<AthenaSemanticMacroCatalogDiagnostic>()

        graph.packages.forEach { resolvedPackage ->
            val packageRoot = repositoryRoot.resolve(resolvedPackage.sourceRoot).normalize().parent ?: return@forEach
            val loaded = packageLoader.load(
                packageId = resolvedPackage.packageId,
                packageRoot = packageRoot,
            )
            contracts += loaded.contracts
            diagnostics += loaded.diagnostics
        }

        return AthenaSemanticMacroContractResolution(
            contracts = contracts.sortedWith(
                compareBy<SemanticMacroContract>(
                    { contract -> contract.packageBinding.packageId.name },
                    { contract -> contract.packageBinding.packageId.version.orEmpty() },
                    { contract -> contract.macroId.value },
                    { contract -> contract.displayName },
                    { contract -> contract.packageBinding.definitionPath.orEmpty() },
                ),
            ),
            diagnostics = diagnostics.sortedWith(
                compareBy<AthenaSemanticMacroCatalogDiagnostic>(
                    { diagnostic -> diagnostic.code },
                    { diagnostic -> diagnostic.subject },
                    { diagnostic -> diagnostic.message },
                ),
            ),
        )
    }
}

internal data class AthenaSemanticMacroContractResolution(
    val contracts: List<SemanticMacroContract>,
    val diagnostics: List<AthenaSemanticMacroCatalogDiagnostic> = emptyList(),
)

internal data class AthenaSemanticMacroPackageLoadResult(
    val contracts: List<SemanticMacroContract>,
    val diagnostics: List<AthenaSemanticMacroCatalogDiagnostic>,
)

/**
 * Loads one governed package-scoped Semantic Macro manifest when present.
 *
 * Packages without a macro manifest simply contribute no contracts. Packages with malformed
 * manifests contribute diagnostics and only their valid contracts.
 */
internal class AthenaSemanticMacroPackageLoader {
    fun load(
        packageId: PackageIdentifier,
        packageRoot: Path,
    ): AthenaSemanticMacroPackageLoadResult {
        val manifestPath = packageRoot.resolve(MANIFEST_FILE_NAME)
        if (!Files.exists(manifestPath)) {
            return AthenaSemanticMacroPackageLoadResult(
                contracts = emptyList(),
                diagnostics = emptyList(),
            )
        }
        if (!Files.isRegularFile(manifestPath)) {
            return AthenaSemanticMacroPackageLoadResult(
                contracts = emptyList(),
                diagnostics = listOf(
                    diagnostic(
                        code = "semantic.macro.manifest.not-file",
                        subject = manifestPath.toString(),
                        message = "Semantic Macro manifest `$MANIFEST_FILE_NAME` must be a file inside the package root.",
                    ),
                ),
            )
        }

        val properties = runCatching { loadProperties(manifestPath) }.getOrElse { exception ->
            return AthenaSemanticMacroPackageLoadResult(
                contracts = emptyList(),
                diagnostics = listOf(
                    diagnostic(
                        code = "semantic.macro.manifest.unreadable",
                        subject = manifestPath.toString(),
                        message = "Could not read Semantic Macro manifest: ${exception.message ?: exception::class.simpleName}",
                    ),
                ),
            )
        }

        val diagnostics = mutableListOf<AthenaSemanticMacroCatalogDiagnostic>()
        val formatVersionText = properties.requiredValue("package.format.version")
        val formatVersion = formatVersionText.toIntOrNull()
        when {
            formatVersionText.isBlank() -> diagnostics += diagnostic(
                code = "semantic.macro.manifest.format-version.blank",
                subject = "package.format.version",
                message = "Semantic Macro package format version must not be blank.",
            )

            formatVersion == null -> diagnostics += diagnostic(
                code = "semantic.macro.manifest.format-version.invalid",
                subject = "package.format.version",
                message = "Semantic Macro package format version `$formatVersionText` is invalid.",
            )

            formatVersion != SUPPORTED_PACKAGE_FORMAT_VERSION -> diagnostics += diagnostic(
                code = "semantic.macro.manifest.format-version.unsupported",
                subject = "package.format.version",
                message = "Semantic Macro package format version `$formatVersionText` is not supported.",
            )
        }

        val entryIds = properties.entryIds()
        val contracts = entryIds.mapNotNull { entryId ->
            loadContract(
                packageId = packageId,
                packageRoot = packageRoot,
                properties = properties,
                entryId = entryId,
                diagnostics = diagnostics,
            )
        }

        return AthenaSemanticMacroPackageLoadResult(
            contracts = contracts,
            diagnostics = diagnostics,
        )
    }

    private fun loadContract(
        packageId: PackageIdentifier,
        packageRoot: Path,
        properties: Properties,
        entryId: String,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): SemanticMacroContract? {
        val macroIdText = properties.requiredValue("macro.$entryId.id")
        if (macroIdText.isBlank()) {
            diagnostics += diagnostic(
                code = "semantic.macro.entry.id.blank",
                subject = "macro.$entryId.id",
                message = "Semantic Macro id must not be blank.",
            )
        }

        val displayName = properties.requiredValue("macro.$entryId.displayName")
        if (displayName.isBlank()) {
            diagnostics += diagnostic(
                code = "semantic.macro.entry.display-name.blank",
                subject = "macro.$entryId.displayName",
                message = "Semantic Macro display name must not be blank.",
            )
        }

        val definitionPath = properties.requiredValue("macro.$entryId.definitionPath")
        if (definitionPath.isBlank()) {
            diagnostics += diagnostic(
                code = "semantic.macro.entry.definition-path.blank",
                subject = "macro.$entryId.definitionPath",
                message = "Semantic Macro definition path must not be blank.",
            )
        }

        if (macroIdText.isBlank() || displayName.isBlank() || definitionPath.isBlank()) {
            return null
        }

        val resolvedDefinitionPath = packageRoot.resolve(definitionPath).normalize()
        val normalizedPackageRoot = packageRoot.toAbsolutePath().normalize()
        if (!resolvedDefinitionPath.startsWith(normalizedPackageRoot)) {
            diagnostics += diagnostic(
                code = "semantic.macro.entry.definition-path.outside-root",
                subject = "macro.$entryId.definitionPath",
                message = "Semantic Macro definition path `$definitionPath` must stay within the package root.",
            )
            return null
        }
        if (!Files.isRegularFile(resolvedDefinitionPath)) {
            diagnostics += diagnostic(
                code = "semantic.macro.entry.definition-path.missing",
                subject = "macro.$entryId.definitionPath",
                message = "Semantic Macro definition path `$definitionPath` does not exist within the package root.",
            )
            return null
        }

        return SemanticMacroContract(
            macroId = SemanticMacroId(macroIdText),
            displayName = displayName,
            summary = properties.optionalValue("macro.$entryId.summary").orEmpty().ifBlank { displayName },
            packageBinding = SemanticMacroPackageBinding(
                packageId = packageId,
                definitionPath = definitionPath,
            ),
            parameters = loadParameters(
                entryId = entryId,
                properties = properties,
                diagnostics = diagnostics,
            ),
            classificationKeys = properties.csvValue("macro.$entryId.classificationKeys").toSet(),
        )
    }

    private fun loadParameters(
        entryId: String,
        properties: Properties,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): List<SemanticMacroParameterDefinition> {
        return properties.parameterIds(entryId).mapNotNull { parameterId ->
            val baseKey = "macro.$entryId.parameter.$parameterId"
            val kind = properties.parseValueKind("$baseKey.kind", diagnostics) ?: return@mapNotNull null
            val label = properties.requiredValue("$baseKey.label")
            if (label.isBlank()) {
                diagnostics += diagnostic(
                    code = "semantic.macro.parameter.label.blank",
                    subject = "$baseKey.label",
                    message = "Semantic Macro parameter label must not be blank.",
                )
                return@mapNotNull null
            }

            val required = properties.parseBooleanValue("$baseKey.required", diagnostics) ?: false
            val validationRules = properties.parseValidationRules(
                baseKey = baseKey,
                parameterKind = kind,
                diagnostics = diagnostics,
            )

            SemanticMacroParameterDefinition(
                name = SemanticMacroParameterName(parameterId),
                valueKind = kind,
                label = label,
                description = properties.optionalValue("$baseKey.description"),
                required = required,
                defaultValue = properties.parseDefaultValue(
                    baseKey = baseKey,
                    parameterKind = kind,
                    diagnostics = diagnostics,
                ),
                validationRules = validationRules,
            )
        }
    }

    private fun Properties.parseValueKind(
        key: String,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): SemanticMacroParameterValueKind? {
        val raw = requiredValue(key)
        if (raw.isBlank()) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.kind.blank",
                subject = key,
                message = "Semantic Macro parameter kind must not be blank.",
            )
            return null
        }
        return when (raw.lowercase()) {
            "text" -> SemanticMacroParameterValueKind.TEXT
            "symbol" -> SemanticMacroParameterValueKind.SYMBOL
            "boolean" -> SemanticMacroParameterValueKind.BOOLEAN
            "integer" -> SemanticMacroParameterValueKind.INTEGER
            else -> {
                diagnostics += diagnostic(
                    code = "semantic.macro.parameter.kind.invalid",
                    subject = key,
                    message = "Semantic Macro parameter kind `$raw` is invalid.",
                )
                null
            }
        }
    }

    private fun Properties.parseDefaultValue(
        baseKey: String,
        parameterKind: SemanticMacroParameterValueKind,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): SemanticMacroParameterValue? {
        return when (parameterKind) {
            SemanticMacroParameterValueKind.TEXT -> optionalValue("$baseKey.defaultText")
                ?.takeIf(String::isNotBlank)
                ?.let(SemanticMacroParameterValue::Text)

            SemanticMacroParameterValueKind.SYMBOL -> optionalValue("$baseKey.defaultSymbol")
                ?.takeIf(String::isNotBlank)
                ?.let(SemanticMacroParameterValue::Symbol)

            SemanticMacroParameterValueKind.BOOLEAN -> parseBooleanValue("$baseKey.defaultBoolean", diagnostics)
                ?.let(SemanticMacroParameterValue::BooleanValue)

            SemanticMacroParameterValueKind.INTEGER -> {
                val raw = optionalValue("$baseKey.defaultInteger") ?: return null
                val value = raw.toIntOrNull()
                if (value == null) {
                    diagnostics += diagnostic(
                        code = "semantic.macro.parameter.default-integer.invalid",
                        subject = "$baseKey.defaultInteger",
                        message = "Semantic Macro integer default `$raw` is invalid.",
                    )
                }
                value?.let(SemanticMacroParameterValue::IntegerValue)
            }
        }
    }

    private fun Properties.parseValidationRules(
        baseKey: String,
        parameterKind: SemanticMacroParameterValueKind,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): SemanticMacroParameterValidationRules {
        val allowedValues = csvValue("$baseKey.allowedValues")
        val pattern = optionalValue("$baseKey.pattern")?.takeIf(String::isNotBlank)
        val minLength = parseOptionalInt("$baseKey.minLength", diagnostics)
        val maxLength = parseOptionalInt("$baseKey.maxLength", diagnostics)
        val minInteger = parseOptionalInt("$baseKey.minInteger", diagnostics)
        val maxInteger = parseOptionalInt("$baseKey.maxInteger", diagnostics)

        if (allowedValues.isNotEmpty() && parameterKind !in setOf(SemanticMacroParameterValueKind.TEXT, SemanticMacroParameterValueKind.SYMBOL)) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.allowed-values.unsupported-kind",
                subject = "$baseKey.allowedValues",
                message = "Allowed values currently support only text or symbol parameters.",
            )
        }
        if (pattern != null && parameterKind !in setOf(SemanticMacroParameterValueKind.TEXT, SemanticMacroParameterValueKind.SYMBOL)) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.pattern.unsupported-kind",
                subject = "$baseKey.pattern",
                message = "Pattern rules currently support only text or symbol parameters.",
            )
        }
        if (pattern != null) {
            runCatching { Regex(pattern) }.onFailure {
                diagnostics += diagnostic(
                    code = "semantic.macro.parameter.pattern.invalid",
                    subject = "$baseKey.pattern",
                    message = "Semantic Macro parameter pattern `$pattern` is invalid.",
                )
            }
        }
        if ((minLength != null || maxLength != null) && parameterKind !in setOf(SemanticMacroParameterValueKind.TEXT, SemanticMacroParameterValueKind.SYMBOL)) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.length.unsupported-kind",
                subject = baseKey,
                message = "Length bounds currently support only text or symbol parameters.",
            )
        }
        if ((minInteger != null || maxInteger != null) && parameterKind != SemanticMacroParameterValueKind.INTEGER) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.integer-bounds.unsupported-kind",
                subject = baseKey,
                message = "Integer bounds currently support only integer parameters.",
            )
        }
        if (minLength != null && maxLength != null && minLength > maxLength) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.length-bounds.invalid",
                subject = baseKey,
                message = "Semantic Macro length bounds are invalid because minLength exceeds maxLength.",
            )
        }
        if (minInteger != null && maxInteger != null && minInteger > maxInteger) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.integer-bounds.invalid",
                subject = baseKey,
                message = "Semantic Macro integer bounds are invalid because minInteger exceeds maxInteger.",
            )
        }

        return SemanticMacroParameterValidationRules(
            allowedValues = allowedValues,
            pattern = pattern,
            minLength = minLength,
            maxLength = maxLength,
            minInteger = minInteger,
            maxInteger = maxInteger,
        )
    }

    private fun Properties.parseBooleanValue(
        key: String,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): Boolean? {
        val raw = optionalValue(key) ?: return null
        return when (raw.lowercase()) {
            "true" -> true
            "false" -> false
            else -> {
                diagnostics += diagnostic(
                    code = "semantic.macro.parameter.boolean.invalid",
                    subject = key,
                    message = "Semantic Macro boolean value `$raw` is invalid.",
                )
                null
            }
        }
    }

    private fun Properties.parseOptionalInt(
        key: String,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): Int? {
        val raw = optionalValue(key) ?: return null
        val value = raw.toIntOrNull()
        if (value == null) {
            diagnostics += diagnostic(
                code = "semantic.macro.parameter.integer.invalid",
                subject = key,
                message = "Semantic Macro integer value `$raw` is invalid.",
            )
        }
        return value
    }

    private fun loadProperties(manifestPath: Path): Properties {
        return Properties().apply {
            Files.newInputStream(manifestPath).use(::load)
        }
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): AthenaSemanticMacroCatalogDiagnostic {
        return AthenaSemanticMacroCatalogDiagnostic(
            code = code,
            subject = subject,
            message = message,
        )
    }
}

private const val MANIFEST_FILE_NAME = "athena-semantic-macros.properties"
private const val SUPPORTED_PACKAGE_FORMAT_VERSION = 1
private val ENTRY_KEY_PATTERN = Regex("""macro\.([^.]+)\.(id|displayName|summary|definitionPath|classificationKeys)""")
private val PARAMETER_KEY_PATTERN = Regex(
    """macro\.([^.]+)\.parameter\.([^.]+)\.(kind|label|description|required|defaultText|defaultSymbol|defaultBoolean|defaultInteger|allowedValues|pattern|minLength|maxLength|minInteger|maxInteger)""",
)

private fun SemanticMacroContract.toCatalogEntry(): AthenaSemanticMacroCatalogEntry {
    return AthenaSemanticMacroCatalogEntry(
        macroId = macroId,
        displayName = displayName,
        summary = summary,
        packageId = packageBinding.packageId,
        definitionPath = packageBinding.definitionPath.orEmpty(),
        classificationKeys = classificationKeys,
    )
}

private fun Properties.requiredValue(key: String): String = getProperty(key)?.trim().orEmpty()

private fun Properties.optionalValue(key: String): String? = getProperty(key)?.trim()

private fun Properties.csvValue(key: String): List<String> {
    return requiredValue(key)
        .split(',')
        .map(String::trim)
        .filter(String::isNotEmpty)
}

private fun Properties.entryIds(): Set<String> {
    return stringPropertyNames()
        .mapNotNull { key ->
            ENTRY_KEY_PATTERN.matchEntire(key)?.groupValues?.get(1)
                ?: PARAMETER_KEY_PATTERN.matchEntire(key)?.groupValues?.get(1)
        }
        .toSortedSet()
}

private fun Properties.parameterIds(entryId: String): Set<String> {
    return stringPropertyNames()
        .mapNotNull { key ->
            val match = PARAMETER_KEY_PATTERN.matchEntire(key) ?: return@mapNotNull null
            if (match.groupValues[1] != entryId) {
                return@mapNotNull null
            }
            match.groupValues[2]
        }
        .toSortedSet()
}
