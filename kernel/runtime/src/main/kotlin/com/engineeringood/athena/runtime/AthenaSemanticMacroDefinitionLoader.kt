package com.engineeringood.athena.runtime

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.connection.SemanticPortRoleId
import com.engineeringood.athena.reuse.SemanticMacroContract
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import com.engineeringood.athena.part.PartImplementationId
import com.engineeringood.athena.template.ComponentTemplate
import com.engineeringood.athena.template.ComponentTemplateId
import com.engineeringood.athena.template.ComponentTemplatePropertyName
import com.engineeringood.athena.template.ConnectionTemplate
import com.engineeringood.athena.template.ConnectionTemplateId
import com.engineeringood.athena.template.TemplateDefaultMetadata
import com.engineeringood.athena.template.TemplateDocumentationHint
import com.engineeringood.athena.template.TemplatePortReference
import com.engineeringood.athena.template.TemplatePresentationHint
import com.engineeringood.athena.template.TemplateValue
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

internal data class AthenaSemanticMacroTemplateDefinition(
    val componentTemplates: List<ComponentTemplate>,
    val connectionTemplates: List<ConnectionTemplate>,
    val presentationHints: List<TemplatePresentationHint> = emptyList(),
    val documentationHints: List<TemplateDocumentationHint> = emptyList(),
)

internal data class AthenaSemanticMacroDefinitionLoadResult(
    val template: AthenaSemanticMacroTemplateDefinition? = null,
    val diagnostics: List<AthenaSemanticMacroCatalogDiagnostic> = emptyList(),
)

internal class AthenaSemanticMacroDefinitionLoader {
    fun load(
        packageRoot: Path,
        contract: SemanticMacroContract,
    ): AthenaSemanticMacroDefinitionLoadResult {
        val definitionPath = contract.packageBinding.definitionPath
        if (definitionPath.isNullOrBlank()) {
            return AthenaSemanticMacroDefinitionLoadResult(
                diagnostics = listOf(
                    diagnostic(
                        code = "semantic.macro.definition-path.blank",
                        subject = contract.macroId.value,
                        message = "Semantic Macro `${contract.macroId.value}` does not declare a definition path.",
                    ),
                ),
            )
        }

        val normalizedPackageRoot = packageRoot.toAbsolutePath().normalize()
        val resolvedDefinitionPath = normalizedPackageRoot.resolve(definitionPath).normalize()
        if (!resolvedDefinitionPath.startsWith(normalizedPackageRoot)) {
            return AthenaSemanticMacroDefinitionLoadResult(
                diagnostics = listOf(
                    diagnostic(
                        code = "semantic.macro.definition-path.outside-root",
                        subject = definitionPath,
                        message = "Semantic Macro definition path `$definitionPath` must stay within the package root.",
                    ),
                ),
            )
        }
        if (!Files.isRegularFile(resolvedDefinitionPath)) {
            return AthenaSemanticMacroDefinitionLoadResult(
                diagnostics = listOf(
                    diagnostic(
                        code = "semantic.macro.definition-path.missing",
                        subject = definitionPath,
                        message = "Semantic Macro definition path `$definitionPath` is missing.",
                    ),
                ),
            )
        }

        val properties = runCatching { loadProperties(resolvedDefinitionPath) }.getOrElse { exception ->
            return AthenaSemanticMacroDefinitionLoadResult(
                diagnostics = listOf(
                    diagnostic(
                        code = "semantic.macro.definition.unreadable",
                        subject = resolvedDefinitionPath.toString(),
                        message = "Could not read Semantic Macro definition `${contract.macroId.value}`: ${exception.message ?: exception::class.simpleName}.",
                    ),
                ),
            )
        }

        val diagnostics = mutableListOf<AthenaSemanticMacroCatalogDiagnostic>()
        if (!properties.supportsDefinitionFormat(diagnostics)) {
            return AthenaSemanticMacroDefinitionLoadResult(diagnostics = diagnostics)
        }

        val components = loadComponentTemplates(properties, contract, diagnostics)
        val componentTemplateIds = components.map { template -> template.templateId.value }.toSet()
        val connections = loadConnectionTemplates(
            properties = properties,
            availableComponentTemplateIds = componentTemplateIds,
            diagnostics = diagnostics,
        )
        val presentationHints = loadPresentationHints(
            properties = properties,
            baseKey = "presentation",
            diagnostics = diagnostics,
        )
        val documentationHints = loadDocumentationHints(
            properties = properties,
            baseKey = "documentation",
            diagnostics = diagnostics,
        )

        return if (diagnostics.isEmpty()) {
            AthenaSemanticMacroDefinitionLoadResult(
                template = AthenaSemanticMacroTemplateDefinition(
                    componentTemplates = components,
                    connectionTemplates = connections,
                    presentationHints = presentationHints,
                    documentationHints = documentationHints,
                ),
            )
        } else {
            AthenaSemanticMacroDefinitionLoadResult(diagnostics = diagnostics)
        }
    }

    private fun loadComponentTemplates(
        properties: Properties,
        contract: SemanticMacroContract,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): List<ComponentTemplate> {
        val declaredParameters = contract.parameters.map { parameter -> parameter.name.value }.toSet()
        return properties.entryIds(COMPONENT_ID_PATTERN).mapNotNull { entryId ->
            val baseKey = "component.$entryId"
            val templateId = properties.requiredValue("$baseKey.id")
            val conceptId = properties.requiredValue("$baseKey.conceptId")
            if (templateId.isBlank()) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.component.id.blank",
                    subject = "$baseKey.id",
                    message = "Component template id must not be blank.",
                )
            }
            if (conceptId.isBlank()) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.component.concept-id.blank",
                    subject = "$baseKey.conceptId",
                    message = "Component template concept id must not be blank.",
                )
            }
            if (templateId.isBlank() || conceptId.isBlank()) {
                return@mapNotNull null
            }

            val propertiesMap = properties.loadTemplateProperties(
                baseKey = baseKey,
                declaredParameters = declaredParameters,
                diagnostics = diagnostics,
            )
            ComponentTemplate(
                templateId = ComponentTemplateId(templateId),
                conceptId = EngineeringConceptId(conceptId),
                implementationId = properties.optionalValue("$baseKey.implementationId")
                    ?.takeIf(String::isNotBlank)
                    ?.let(::PartImplementationId),
                defaultMetadata = TemplateDefaultMetadata(
                    displayName = properties.optionalValue("$baseKey.displayName")?.takeIf(String::isNotBlank),
                    summary = properties.optionalValue("$baseKey.summary")?.takeIf(String::isNotBlank),
                    tags = properties.csvValue("$baseKey.tags").toSet(),
                ),
                properties = propertiesMap,
                presentationHints = loadPresentationHints(properties, "$baseKey.presentation", diagnostics),
                documentationHints = loadDocumentationHints(properties, "$baseKey.documentation", diagnostics),
            )
        }
    }

    private fun loadConnectionTemplates(
        properties: Properties,
        availableComponentTemplateIds: Set<String>,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): List<ConnectionTemplate> {
        return properties.entryIds(CONNECTION_ID_PATTERN).mapNotNull { entryId ->
            val baseKey = "connection.$entryId"
            val templateId = properties.requiredValue("$baseKey.id")
            val fromComponentTemplateId = properties.requiredValue("$baseKey.from.componentTemplateId")
            val fromPortRoleId = properties.requiredValue("$baseKey.from.portRoleId")
            val toComponentTemplateId = properties.requiredValue("$baseKey.to.componentTemplateId")
            val toPortRoleId = properties.requiredValue("$baseKey.to.portRoleId")
            if (templateId.isBlank()) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.connection.id.blank",
                    subject = "$baseKey.id",
                    message = "Connection template id must not be blank.",
                )
            }
            if (fromComponentTemplateId.isBlank() || fromPortRoleId.isBlank() || toComponentTemplateId.isBlank() || toPortRoleId.isBlank()) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.connection.endpoint.blank",
                    subject = baseKey,
                    message = "Connection template `$entryId` must declare complete `from` and `to` semantic endpoints.",
                )
                return@mapNotNull null
            }
            if (fromComponentTemplateId !in availableComponentTemplateIds) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.connection.from-component.unknown",
                    subject = "$baseKey.from.componentTemplateId",
                    message = "Connection template `$entryId` references unknown source component template `$fromComponentTemplateId`.",
                )
                return@mapNotNull null
            }
            if (toComponentTemplateId !in availableComponentTemplateIds) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.connection.to-component.unknown",
                    subject = "$baseKey.to.componentTemplateId",
                    message = "Connection template `$entryId` references unknown target component template `$toComponentTemplateId`.",
                )
                return@mapNotNull null
            }

            ConnectionTemplate(
                templateId = ConnectionTemplateId(templateId),
                from = TemplatePortReference(
                    componentTemplateId = ComponentTemplateId(fromComponentTemplateId),
                    portRoleId = SemanticPortRoleId(fromPortRoleId),
                ),
                to = TemplatePortReference(
                    componentTemplateId = ComponentTemplateId(toComponentTemplateId),
                    portRoleId = SemanticPortRoleId(toPortRoleId),
                ),
                defaultMetadata = TemplateDefaultMetadata(
                    displayName = properties.optionalValue("$baseKey.displayName")?.takeIf(String::isNotBlank),
                    summary = properties.optionalValue("$baseKey.summary")?.takeIf(String::isNotBlank),
                    tags = properties.csvValue("$baseKey.tags").toSet(),
                ),
                presentationHints = loadPresentationHints(properties, "$baseKey.presentation", diagnostics),
                documentationHints = loadDocumentationHints(properties, "$baseKey.documentation", diagnostics),
            )
        }
    }

    private fun Properties.loadTemplateProperties(
        baseKey: String,
        declaredParameters: Set<String>,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): Map<ComponentTemplatePropertyName, TemplateValue> {
        return propertyIds(baseKey, PROPERTY_PATTERN).mapNotNull { propertyName ->
            val raw = requiredValue("$baseKey.property.$propertyName")
            if (raw.isBlank()) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.component.property.blank",
                    subject = "$baseKey.property.$propertyName",
                    message = "Component template property `$propertyName` must not be blank.",
                )
                return@mapNotNull null
            }

            parseTemplateValue(
                key = "$baseKey.property.$propertyName",
                raw = raw,
                declaredParameters = declaredParameters,
                diagnostics = diagnostics,
            )?.let { templateValue ->
                ComponentTemplatePropertyName(propertyName) to templateValue
            }
        }.sortedBy { (propertyName, _) -> propertyName.value }
            .toMap(linkedMapOf())
    }

    private fun parseTemplateValue(
        key: String,
        raw: String,
        declaredParameters: Set<String>,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): TemplateValue? {
        val separatorIndex = raw.indexOf(':')
        if (separatorIndex <= 0) {
            diagnostics += diagnostic(
                code = "semantic.macro.definition.template-value.invalid",
                subject = key,
                message = "Template value `$raw` must use `<kind>:<value>` syntax.",
            )
            return null
        }

        val kind = raw.substring(0, separatorIndex).lowercase()
        val value = raw.substring(separatorIndex + 1)
        return when (kind) {
            "param" -> {
                if (value.isBlank()) {
                    diagnostics += diagnostic(
                        code = "semantic.macro.definition.template-value.parameter.blank",
                        subject = key,
                        message = "Template parameter references must name one declared Semantic Macro parameter.",
                    )
                    null
                } else if (value !in declaredParameters) {
                    diagnostics += diagnostic(
                        code = "semantic.macro.definition.template-value.parameter.unknown",
                        subject = key,
                        message = "Template parameter reference `$value` is not declared by the Semantic Macro contract.",
                    )
                    null
                } else {
                    TemplateValue.ParameterReference(SemanticMacroParameterName(value))
                }
            }

            "text" -> TemplateValue.Literal(SemanticMacroParameterValue.Text(value))
            "symbol" -> TemplateValue.Literal(SemanticMacroParameterValue.Symbol(value))
            "boolean" -> value.toBooleanStrictOrNull()?.let { parsed ->
                TemplateValue.Literal(SemanticMacroParameterValue.BooleanValue(parsed))
            } ?: run {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.template-value.boolean.invalid",
                    subject = key,
                    message = "Template boolean literal `$value` is invalid.",
                )
                null
            }

            "integer" -> value.toIntOrNull()?.let { parsed ->
                TemplateValue.Literal(SemanticMacroParameterValue.IntegerValue(parsed))
            } ?: run {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.template-value.integer.invalid",
                    subject = key,
                    message = "Template integer literal `$value` is invalid.",
                )
                null
            }

            else -> {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.template-value.kind.invalid",
                    subject = key,
                    message = "Template value kind `$kind` is invalid.",
                )
                null
            }
        }
    }

    private fun loadPresentationHints(
        properties: Properties,
        baseKey: String,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): List<TemplatePresentationHint> {
        return loadHints(
            properties = properties,
            baseKey = baseKey,
            diagnostics = diagnostics,
        ) { hintType, attributes ->
            TemplatePresentationHint(
                hintType = hintType,
                attributes = attributes,
            )
        }
    }

    private fun loadDocumentationHints(
        properties: Properties,
        baseKey: String,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): List<TemplateDocumentationHint> {
        return loadHints(
            properties = properties,
            baseKey = baseKey,
            diagnostics = diagnostics,
        ) { hintType, attributes ->
            TemplateDocumentationHint(
                hintType = hintType,
                attributes = attributes,
            )
        }
    }

    private fun <THint> loadHints(
        properties: Properties,
        baseKey: String,
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
        factory: (hintType: String, attributes: Map<String, String>) -> THint,
    ): List<THint> {
        return properties.entryIds(hintTypePattern(baseKey)).mapNotNull { entryId ->
            val prefix = "$baseKey.$entryId"
            val hintType = properties.requiredValue("$prefix.type")
            if (hintType.isBlank()) {
                diagnostics += diagnostic(
                    code = "semantic.macro.definition.hint.type.blank",
                    subject = "$prefix.type",
                    message = "Hint type for `$prefix` must not be blank.",
                )
                return@mapNotNull null
            }

            val attributes = properties.propertyIds(prefix, HINT_ATTRIBUTE_PATTERN)
                .associateWith { attributeName -> properties.requiredValue("$prefix.attribute.$attributeName") }
                .toSortedMap()
            factory(hintType, attributes)
        }
    }

    private fun Properties.supportsDefinitionFormat(
        diagnostics: MutableList<AthenaSemanticMacroCatalogDiagnostic>,
    ): Boolean {
        val raw = requiredValue(DEFINITION_FORMAT_VERSION_KEY)
        val value = raw.toIntOrNull()
        when {
            raw.isBlank() -> diagnostics += diagnostic(
                code = "semantic.macro.definition.format-version.blank",
                subject = DEFINITION_FORMAT_VERSION_KEY,
                message = "Semantic Macro definition format version must not be blank.",
            )

            value == null -> diagnostics += diagnostic(
                code = "semantic.macro.definition.format-version.invalid",
                subject = DEFINITION_FORMAT_VERSION_KEY,
                message = "Semantic Macro definition format version `$raw` is invalid.",
            )

            value != SUPPORTED_DEFINITION_FORMAT_VERSION -> diagnostics += diagnostic(
                code = "semantic.macro.definition.format-version.unsupported",
                subject = DEFINITION_FORMAT_VERSION_KEY,
                message = "Semantic Macro definition format version `$raw` is not supported.",
            )
        }
        return diagnostics.isEmpty()
    }

    private fun Properties.entryIds(pattern: Regex): List<String> {
        return stringPropertyNames()
            .mapNotNull { key -> pattern.matchEntire(key)?.groupValues?.get(1) }
            .distinct()
            .sorted()
    }

    private fun Properties.propertyIds(baseKey: String, pattern: Regex): List<String> {
        val prefix = "$baseKey."
        return stringPropertyNames()
            .filter { key -> key.startsWith(prefix) }
            .mapNotNull { key -> pattern.matchEntire(key)?.groupValues?.get(1) }
            .distinct()
            .sorted()
    }

    private fun Properties.requiredValue(key: String): String = getProperty(key)?.trim().orEmpty()

    private fun Properties.optionalValue(key: String): String? = getProperty(key)?.trim()

    private fun Properties.csvValue(key: String): List<String> {
        return optionalValue(key)
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotBlank)
            .orEmpty()
    }

    private fun loadProperties(path: Path): Properties {
        return Properties().apply {
            Files.newInputStream(path).buffered().use(::load)
        }
    }

    private fun hintTypePattern(baseKey: String): Regex {
        return Regex("^${Regex.escape(baseKey)}\\.([^.]+)\\.type$")
    }
}

private const val DEFINITION_FORMAT_VERSION_KEY = "template.format.version"
private const val SUPPORTED_DEFINITION_FORMAT_VERSION = 1
private val COMPONENT_ID_PATTERN = Regex("^component\\.([^.]+)\\.id$")
private val CONNECTION_ID_PATTERN = Regex("^connection\\.([^.]+)\\.id$")
private val PROPERTY_PATTERN = Regex("^.+\\.property\\.([^.]+)$")
private val HINT_ATTRIBUTE_PATTERN = Regex("^.+\\.attribute\\.([^.]+)$")

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
