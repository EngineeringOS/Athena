package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.ir.EngineeringEntity
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.RelationDeclaration
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.ir.SourceProvenance

internal fun validateElectricalRuntime(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
    val ownedEntities = context.document.entities.filter { entity -> entity.isElectricalOwned() }
    val ownedEntityIds = ownedEntities.map { entity -> entity.id }.toSet()
    val ownedPorts = context.document.ports.filter { port -> port.owner.reference.resolvedIdentity in ownedEntityIds }
    val portsById = ownedPorts.associateBy { it.id }
    val ownedRelationships = context.document.relationships.filter { relationship ->
        relationship.participants.size == 2 && relationship.participants.all { participant ->
            val subject = participant.subject
            subject is com.engineeringood.athena.ir.EngineeringSubjectReference.Port &&
                subject.port.resolvedIdentity in portsById
        }
    }
		    val diagnostics = buildList {
		        addAll(relationDiagnostics(context))
		        addAll(groupedInterfaceDiagnostics(context))
		        addAll(entityConceptDiagnostics(ownedEntities, context))
        addAll(relationshipCompatibilityDiagnostics(ownedRelationships, portsById, context))
	    }
    return AthenaPluginValidationResult(
        contributions = listOf(
            context.emitValidationContribution(
                contributionId = ELECTRICAL_VALIDATION_CONTRIBUTION_ID,
                diagnostics = diagnostics,
            ),
        ),
    )
}

private fun relationDiagnostics(context: AthenaPluginValidationContext): List<SemanticDiagnostic> {
    val source = context.source ?: return emptyList()
    return source.ast.declarations.filterIsInstance<RelationDeclaration>().mapNotNull { relation ->
        if (relation.word.value in ELECTRICAL_RELATION_WORDS) {
            null
        } else {
            context.domainDiagnostic(
                ruleId = "relation.word.unknown",
                category = SemanticDiagnosticCategory.RELATIONSHIP,
                provenance = source.file.provenance(relation.word.span),
                message = "Unknown relation `${relation.word.value}` for domain plugin `${ELECTRICAL_RUNTIME_MANIFEST.pluginId}`. Available relations: ${ELECTRICAL_RELATION_WORD_LIST.joinToString(", ")}.",
            )
        }
    }
}

private fun entityConceptDiagnostics(
    entities: List<EngineeringEntity>,
    context: AthenaPluginValidationContext,
): List<SemanticDiagnostic> {
    return entities.mapNotNull { entity ->
        val concept = entity.conceptReference.authoredName.joinToString(".")
        if (entity.conceptReference.authoredName.last() in VALID_DEVICE_TYPES) {
            null
        } else {
            context.domainDiagnostic(
                ruleId = "entity.concept.unsupported",
                category = SemanticDiagnosticCategory.PROPERTY,
                subjectIdentity = entity.id,
                provenance = entity.provenance,
                message = "Entity `${entity.name}` declares unsupported electrical Concept `$concept`.",
            )
        }
    }
}

private fun groupedInterfaceDiagnostics(context: AthenaPluginValidationContext): List<SemanticDiagnostic> {
    val source = context.source ?: return emptyList()
    return source.ast.declarations.filterIsInstance<EntityDeclaration>().flatMap { entity ->
        buildList {
            entity.interfaces.groupBy { connectivityInterface -> connectivityInterface.name }
                .filterValues { duplicates -> duplicates.size > 1 }
                .forEach { (name, duplicates) ->
                    duplicates.drop(1).forEach { duplicate ->
                        add(
                            context.domainDiagnostic(
                                ruleId = "connectivity.interface.duplicate",
                                category = SemanticDiagnosticCategory.RELATIONSHIP,
                                provenance = source.file.provenance(duplicate.span),
                                message = "Entity `${entity.name}` declares duplicate connectivity Interface `$name`.",
                            ),
                        )
                    }
                }

            entity.interfaces.forEach { connectivityInterface ->
                connectivityInterface.ports.groupBy { port -> port.name }
                    .filterValues { duplicates -> duplicates.size > 1 }
                    .forEach { (name, duplicates) ->
                        duplicates.drop(1).forEach { duplicate ->
                            add(
                                context.domainDiagnostic(
                                    ruleId = "connectivity.port.duplicate",
                                    category = SemanticDiagnosticCategory.RELATIONSHIP,
                                    provenance = source.file.provenance(duplicate.span),
                                    message = "Connectivity Interface `${connectivityInterface.name}` declares duplicate member Port `$name`.",
                                ),
                            )
                        }
                    }
                addAll(validateInterfaceFields(entity.name, connectivityInterface.name, connectivityInterface.fields, source.file, context, true))
                connectivityInterface.ports.forEach { member ->
                    addAll(validateInterfaceFields(entity.name, member.name, member.fields, source.file, context, false))
                    addAll(requiredDefaultConflictDiagnostics(entity.name, connectivityInterface.name, connectivityInterface.fields, member.name, member.fields, source.file, context))
                }
            }
        }
    }
}

private fun validateInterfaceFields(
    ownerName: String,
    subjectName: String,
    fields: List<PropertyAssignment>,
    file: String,
    context: AthenaPluginValidationContext,
    interfaceDefault: Boolean,
): List<SemanticDiagnostic> {
    val prefix = if (interfaceDefault) "connectivity.interface" else "connectivity.port"
    val subject = if (interfaceDefault) "Interface `$subjectName` on `$ownerName`" else "Port `$ownerName.$subjectName`"
    val allowedValues = mapOf(
        "direction" to VALID_DIRECTIONS.keys,
        "multiplicity" to VALID_MULTIPLICITIES,
        "owner" to VALID_CONSTRAINT_OWNERS,
        "strength" to VALID_CONSTRAINT_STRENGTHS,
    )
    return fields.mapNotNull { field ->
        val allowed = allowedValues[field.name] ?: return@mapNotNull null
        val value = field.scalarIdentifierText()
        if (value in allowed) {
            null
        } else {
            context.domainDiagnostic(
                ruleId = "$prefix.${field.name}.invalid",
                category = SemanticDiagnosticCategory.RELATIONSHIP,
                provenance = file.provenance(field.span),
                message = "$subject declares unsupported `${field.name}` value `${value ?: field.value.renderedValue()}`.",
            )
        }
    }
}

private fun requiredDefaultConflictDiagnostics(
    ownerName: String,
    interfaceName: String,
    defaultFields: List<PropertyAssignment>,
    memberName: String,
    memberFields: List<PropertyAssignment>,
    file: String,
    context: AthenaPluginValidationContext,
): List<SemanticDiagnostic> {
    val defaultStrength = defaultFields.singleSymbolValue("strength") ?: "required"
    val memberStrength = memberFields.singleSymbolValue("strength") ?: defaultStrength
    if (defaultStrength != "required" || memberStrength != "required") {
        return emptyList()
    }
    val overrideFields = memberFields.associateBy { field -> field.name }
    return defaultFields
        .filter { field -> field.name in REQUIRED_DEFAULT_FIELD_NAMES }
        .mapNotNull { defaultField ->
            val memberField = overrideFields[defaultField.name] ?: return@mapNotNull null
            val defaultValue = defaultField.scalarIdentifierText() ?: return@mapNotNull null
            val memberValue = memberField.scalarIdentifierText() ?: return@mapNotNull null
            if (defaultValue == memberValue) {
                null
            } else {
                context.domainDiagnostic(
                    ruleId = "connectivity.port.default.conflict",
                    category = SemanticDiagnosticCategory.RELATIONSHIP,
                    provenance = file.provenance(memberField.span),
                    message = "Port `$ownerName.$memberName` conflicts with required default `${defaultField.name}` from Interface `$interfaceName`.",
                )
            }
        }
}

private fun String.provenance(span: SourceSpan): SourceProvenance {
    return SourceProvenance(
        file = this,
        startLine = span.start.line,
        startColumn = span.start.column,
        endLine = span.end.line,
        endColumn = span.end.column,
    )
}

private fun PropertyAssignment.scalarIdentifierText(): String? {
    return when (val value = value) {
        is ScalarValue.Symbol -> value.text
        is ScalarValue.Text,
        is ScalarValue.Quantity,
        is ScalarValue.Integer,
        is ScalarValue.Boolean,
        is ScalarValue.Reference -> null
    }
}

private fun List<PropertyAssignment>.singleSymbolValue(name: String): String? {
    return filter { field -> field.name == name }.singleOrNull()?.scalarIdentifierText()
}

private fun ScalarValue.renderedValue(): String {
    return when (this) {
        is ScalarValue.Quantity -> "$exactText ${unit.parts.joinToString(".")}"
        is ScalarValue.Integer -> exactText
        is ScalarValue.Boolean -> value.toString()
        is ScalarValue.Text -> "\"$text\""
        is ScalarValue.Symbol -> text
        is ScalarValue.Reference -> "@${target.parts.joinToString(".")}"
    }
}

private fun relationshipCompatibilityDiagnostics(
    relationships: List<com.engineeringood.athena.ir.EngineeringRelationship>,
    portsById: Map<StableSemanticIdentity, EngineeringPort>,
    context: AthenaPluginValidationContext,
): List<SemanticDiagnostic> {
    return buildList {
        relationships.forEach { relationship ->
            val ports = relationship.participants.mapNotNull { participant ->
                (participant.subject as? com.engineeringood.athena.ir.EngineeringSubjectReference.Port)
                    ?.port?.resolvedIdentity?.let(portsById::get)
            }
            if (ports.size != 2) return@forEach
            val fromPort = ports[0]
            val toPort = ports[1]
            if (fromPort == null || toPort == null) {
                return@forEach
            }

            if (!fromPort.direction.allowsSourceConnection(toPort.direction)) {
                add(
                    context.domainDiagnostic(
                        ruleId = "relationship.direction.illegal",
                        category = SemanticDiagnosticCategory.RELATIONSHIP,
                        subjectIdentity = relationship.id,
                        provenance = relationship.provenance,
                        message = "Relationship `${relationship.participants.joinToString(" -> ") { participant -> authoredPath(participant.subject.reference) }}` must flow from `out` to `in`.",
                    ),
                )
            }

            val fromFlows = fromPort.admittedFlowReferences.map { it.authoredName }.toSet()
            val toFlows = toPort.admittedFlowReferences.map { it.authoredName }.toSet()
            if (fromFlows.isNotEmpty() && toFlows.isNotEmpty() && fromFlows.intersect(toFlows).isEmpty()
            ) {
                add(
                    context.domainDiagnostic(
                        ruleId = "relationship.flow.incompatible",
                        category = SemanticDiagnosticCategory.RELATIONSHIP,
                        subjectIdentity = relationship.id,
                        provenance = relationship.provenance,
                        message = "Relationship `${relationship.participants.joinToString(" -> ") { participant -> authoredPath(participant.subject.reference) }}` has no admitted Flow shared by both Ports.",
                    ),
                )
            }
        }
    }
}

private fun EngineeringPortDirection.allowsSourceConnection(targetDirection: EngineeringPortDirection): Boolean {
    return this in setOf(EngineeringPortDirection.OUTPUT, EngineeringPortDirection.BIDIRECTIONAL) &&
        targetDirection in setOf(EngineeringPortDirection.INPUT, EngineeringPortDirection.BIDIRECTIONAL)
}

private fun List<EngineeringProperty>.requiredSymbolValue(name: String): PropertySymbolValue = propertySymbolValue(name)

private fun List<EngineeringProperty>.optionalSymbolValue(name: String): PropertySymbolValue = propertySymbolValue(name)

private fun List<EngineeringProperty>.propertySymbolValue(name: String): PropertySymbolValue {
    val matchingProperties = filter { it.name == name }
    if (matchingProperties.isEmpty()) {
        return PropertySymbolValue.Missing
    }
    if (matchingProperties.size > 1) {
        return PropertySymbolValue.Duplicate(matchingProperties.map { it.value.renderedValue() })
    }

    return when (val value = matchingProperties.single().value) {
        is EngineeringValue.Symbol -> PropertySymbolValue.SymbolText(value.text)
        else -> PropertySymbolValue.Invalid(value.renderedValue())
    }
}

private fun authoredPortPath(port: EngineeringPort): String = authoredPath(port.owner.reference.authoredPath + port.name)

private fun authoredPath(reference: EngineeringReference): String = authoredPath(reference.authoredPath)

private fun authoredPath(parts: List<String>): String = parts.joinToString(".")

private fun EngineeringEntity.isElectricalOwned(): Boolean {
    val domain = properties.domainMarkerValue()
    return domain == ELECTRICAL_DOMAIN_ID || conceptReference.authoredName.last() in VALID_DEVICE_TYPES
}

private fun List<EngineeringProperty>.domainMarkerValue(): String? {
    val matchingProperties = filter { property -> property.name == "domain" }
    if (matchingProperties.size != 1) {
        return null
    }
    return when (val value = matchingProperties.single().value) {
        is EngineeringValue.Symbol -> value.text
        is EngineeringValue.Text -> value.text
        else -> null
    }
}

private fun EngineeringValue.renderedValue(): String {
    return when (this) {
        is EngineeringValue.Quantity -> "$value ${unit.authoredName.joinToString(".")}"
        is EngineeringValue.Integer -> value.toString()
        is EngineeringValue.Boolean -> value.toString()
        is EngineeringValue.Text -> "\"$text\""
        is EngineeringValue.Symbol -> text
        is EngineeringValue.Reference -> "@${reference.authoredPath.joinToString(".")}"
    }
}

private val VALID_DIRECTIONS = mapOf(
    "in" to EngineeringPortDirection.INPUT,
    "out" to EngineeringPortDirection.OUTPUT,
    "bidirectional" to EngineeringPortDirection.BIDIRECTIONAL,
)

private val VALID_MULTIPLICITIES = setOf("single", "multiple", "many")
private val VALID_CONSTRAINT_OWNERS = setOf("semantic", "representation", "physical", "layout_preference")
private val VALID_CONSTRAINT_STRENGTHS = setOf("required", "preferred", "optional")
private val REQUIRED_DEFAULT_FIELD_NAMES = setOf("direction", "flow", "role", "multiplicity", "owner")

private sealed interface PropertySymbolValue {
    data object Missing : PropertySymbolValue

    data class SymbolText(val value: String) : PropertySymbolValue

    data class Invalid(val value: String) : PropertySymbolValue

    data class Duplicate(val values: List<String>) : PropertySymbolValue
}
