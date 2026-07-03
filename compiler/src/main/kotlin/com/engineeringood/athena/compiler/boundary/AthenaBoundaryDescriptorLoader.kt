package com.engineeringood.athena.compiler.boundary

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** Loads and validates one local directory-backed external boundary descriptor for the M0 proof boundary. */
class AthenaBoundaryDescriptorLoader {
    /** Loads the external boundary descriptor rooted at [descriptorRoot] and returns either the loaded descriptor or diagnostics. */
    fun load(descriptorRoot: Path): AthenaBoundaryDescriptorLoadResult {
        if (!Files.isDirectory(descriptorRoot)) {
            return AthenaBoundaryDescriptorLoadResult(
                loadedDescriptor = null,
                diagnostics = listOf(
                    diagnostic(
                        ruleId = "boundary.descriptor.root.not-directory",
                        subject = "descriptorRoot",
                        message = "External boundary descriptors must be loaded from a directory root.",
                    ),
                ),
            )
        }

        val manifestPath = descriptorRoot.resolve(MANIFEST_FILE_NAME)
        if (!Files.exists(manifestPath)) {
            return AthenaBoundaryDescriptorLoadResult(
                loadedDescriptor = null,
                diagnostics = listOf(
                    diagnostic(
                        ruleId = "boundary.descriptor.manifest.missing",
                        subject = "manifest",
                        message = "External boundary descriptor manifest `$MANIFEST_FILE_NAME` is missing.",
                    ),
                ),
            )
        }

        val properties = runCatching { loadProperties(manifestPath) }.getOrElse { exception ->
            return AthenaBoundaryDescriptorLoadResult(
                loadedDescriptor = null,
                diagnostics = listOf(
                    diagnostic(
                        ruleId = "boundary.descriptor.manifest.unreadable",
                        subject = "manifest",
                        message = "Could not read external boundary descriptor manifest: ${exception.message ?: exception::class.simpleName}",
                    ),
                ),
            )
        }

        val diagnostics = mutableListOf<AthenaBoundaryDiagnostic>()

        val descriptorId = properties.requiredValue("descriptor.id").also { descriptorId ->
            if (descriptorId.isBlank()) {
                diagnostics += diagnostic(
                    ruleId = "boundary.descriptor.id.blank",
                    subject = "descriptor.id",
                    message = "External boundary descriptor id must not be blank.",
                )
            }
        }

        val category = parseEnum<AthenaBoundaryCategory>(
            value = properties.requiredValue("descriptor.category"),
            blankRuleId = "boundary.descriptor.category.blank",
            unsupportedRuleId = "boundary.descriptor.category.unsupported",
            subject = "descriptor.category",
            label = "External boundary category",
            diagnostics = diagnostics,
        )

        val direction = parseEnum<AthenaBoundaryDirection>(
            value = properties.requiredValue("descriptor.direction"),
            blankRuleId = "boundary.descriptor.direction.blank",
            unsupportedRuleId = "boundary.descriptor.direction.unsupported",
            subject = "descriptor.direction",
            label = "External boundary direction",
            diagnostics = diagnostics,
        )

        val authority = parseEnum<AthenaBoundarySemanticAuthority>(
            value = properties.requiredValue("authority.upstream"),
            blankRuleId = "boundary.descriptor.authority.blank",
            unsupportedRuleId = "boundary.descriptor.authority.unsupported",
            subject = "authority.upstream",
            label = "External boundary upstream authority",
            diagnostics = diagnostics,
        )

        val exchangeForms = properties.csvValues("exchange.forms")
        if (exchangeForms.isEmpty()) {
            diagnostics += diagnostic(
                ruleId = "boundary.descriptor.exchange.forms.missing",
                subject = "exchange.forms",
                message = "At least one exchanged form kind must be declared.",
            )
        }
        val parsedExchangeForms = exchangeForms.mapNotNull { form ->
            parseEnum<AthenaBoundaryExchangeFormKind>(
                value = form,
                blankRuleId = "boundary.descriptor.exchange.forms.blank",
                unsupportedRuleId = "boundary.descriptor.exchange.forms.unsupported",
                subject = "exchange.forms",
                label = "External boundary exchanged form kind",
                diagnostics = diagnostics,
            )
        }

        val compatibilityAssumptions = properties.csvValues("compatibility.assumptions")
        if (compatibilityAssumptions.isEmpty()) {
            diagnostics += diagnostic(
                ruleId = "boundary.descriptor.compatibility.assumptions.missing",
                subject = "compatibility.assumptions",
                message = "At least one compatibility assumption must be declared.",
            )
        }
        val parsedCompatibilityAssumptions = compatibilityAssumptions.mapNotNull { assumption ->
            parseEnum<AthenaBoundaryCompatibilityAssumption>(
                value = assumption,
                blankRuleId = "boundary.descriptor.compatibility.assumptions.blank",
                unsupportedRuleId = "boundary.descriptor.compatibility.assumptions.invalid",
                subject = "compatibility.assumptions",
                label = "External boundary compatibility assumption",
                diagnostics = diagnostics,
            )
        }

        val m0Mode = parseEnum<AthenaBoundaryM0Mode>(
            value = properties.requiredValue("m0.mode"),
            blankRuleId = "boundary.descriptor.mode.blank",
            unsupportedRuleId = "boundary.descriptor.mode.unsupported",
            subject = "m0.mode",
            label = "External boundary M0 mode",
            diagnostics = diagnostics,
        )

        if (diagnostics.isNotEmpty()) {
            return AthenaBoundaryDescriptorLoadResult(
                loadedDescriptor = null,
                diagnostics = diagnostics,
            )
        }

        return AthenaBoundaryDescriptorLoadResult(
            loadedDescriptor = AthenaBoundaryDescriptor(
                rootDirectory = descriptorRoot,
                manifest = AthenaBoundaryDescriptorManifest(
                    descriptorId = descriptorId,
                    category = checkNotNull(category),
                    direction = checkNotNull(direction),
                    upstreamAuthority = checkNotNull(authority),
                    exchangeForms = parsedExchangeForms,
                    compatibilityAssumptions = parsedCompatibilityAssumptions,
                    m0Mode = checkNotNull(m0Mode),
                ),
            ),
            diagnostics = emptyList(),
        )
    }

    private fun loadProperties(manifestPath: Path): Properties {
        return Properties().apply {
            Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8).use(::load)
        }
    }

    private inline fun <reified T : Enum<T>> parseEnum(
        value: String,
        blankRuleId: String,
        unsupportedRuleId: String,
        subject: String,
        label: String,
        diagnostics: MutableList<AthenaBoundaryDiagnostic>,
    ): T? {
        if (value.isBlank()) {
            diagnostics += diagnostic(
                ruleId = blankRuleId,
                subject = subject,
                message = "$label must not be blank.",
            )
            return null
        }

        return enumValues<T>().firstOrNull { candidate -> candidate.name == value } ?: run {
            diagnostics += diagnostic(
                ruleId = unsupportedRuleId,
                subject = subject,
                message = "$label `$value` is not supported in M0.",
            )
            null
        }
    }

    private fun diagnostic(
        ruleId: String,
        subject: String,
        message: String,
    ): AthenaBoundaryDiagnostic {
        return AthenaBoundaryDiagnostic(
            severity = AthenaBoundarySeverity.ERROR,
            ruleId = AthenaBoundaryRuleId(ruleId),
            subject = subject,
            message = message,
        )
    }
}

private const val MANIFEST_FILE_NAME = "athena-boundary.properties"

private fun Properties.requiredValue(key: String): String = getProperty(key)?.trim().orEmpty()

private fun Properties.csvValues(key: String): List<String> {
    val rawValue = getProperty(key) ?: return emptyList()
    return rawValue
        .split(',', ignoreCase = false, limit = Int.MAX_VALUE)
        .map(String::trim)
}
