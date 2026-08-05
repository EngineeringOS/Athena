package com.engineeringood.athena.plugin

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringInterfaceDesignation
import com.engineeringood.athena.ir.EngineeringPortCardinality
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringStructureAssignment
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.ExactNumber
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.EngineeringFunctionDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId

/** Syntax-owned source document exposed to plugin contracts without depending on compiler implementation packages. */
data class AthenaSourceDocument(
    val file: String,
    val ast: SourceFileAst,
)

/** Compiler-owned blueprint for one domain-contributed Entity before core identity assignment and resolution. */
data class AthenaDomainEntityBlueprint(
    val name: String,
    val conceptReference: EngineeringDefinitionReference,
    val properties: List<EngineeringProperty>,
    val structureAssignments: List<EngineeringStructureAssignment>,
    val provenance: SourceProvenance,
)

/** Compiler-owned blueprint for one domain-contributed port before core identity assignment and resolution. */
data class AthenaDomainPortBlueprint(
    val ownerPath: List<String>,
    val ownerProvenance: SourceProvenance,
    val name: String,
    val direction: EngineeringPortDirection,
    val admittedFlowReferences: List<EngineeringDefinitionReference>,
    val cardinality: EngineeringPortCardinality,
    val interfaceDesignation: EngineeringInterfaceDesignation?,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

data class AthenaDomainFunctionBlueprint(
    val ownerPath: List<String>,
    val ownerProvenance: SourceProvenance,
    val name: String,
    val roleReference: EngineeringDefinitionReference,
    val properties: List<EngineeringProperty> = emptyList(),
    val provenance: SourceProvenance,
)

/** Domain-owned lowering contribution aggregated by the compiler inside the declared lowering stage. */
data class AthenaDomainLoweringContribution(
    val entities: List<AthenaDomainEntityBlueprint> = emptyList(),
    val ports: List<AthenaDomainPortBlueprint> = emptyList(),
    val functions: List<AthenaDomainFunctionBlueprint> = emptyList(),
) {
    companion object {
        /** Empty contribution used when a plugin does not participate in lowering. */
        val EMPTY: AthenaDomainLoweringContribution = AthenaDomainLoweringContribution()
    }
}

/** Inspectable note emitted by a semantic-enrichment contribution without mutating canonical semantic authority. */
data class AthenaSemanticEnrichmentNote(
    val message: String,
)

/** Domain-owned semantic-enrichment contribution aggregated by the compiler inside the declared enrichment stage. */
data class AthenaDomainSemanticEnrichmentContribution(
    val notes: List<AthenaSemanticEnrichmentNote> = emptyList(),
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
) {
    companion object {
        /** Empty semantic-enrichment contribution used when a plugin does not participate in enrichment. */
        val EMPTY: AthenaDomainSemanticEnrichmentContribution = AthenaDomainSemanticEnrichmentContribution()
    }
}

/** Plugin-facing helper context passed to a domain plugin during lowering. */
data class AthenaDomainLoweringContext(
    val source: AthenaSourceDocument,
) {
    /** Converts authored property assignments into the canonical typed property surface. */
    fun lowerProperties(assignments: List<PropertyAssignment>): List<EngineeringProperty> {
        return assignments.map { assignment ->
            EngineeringProperty(
                name = assignment.name,
                value = lowerValue(assignment.value),
                provenance = provenance(assignment.span),
            )
        }
    }

    /** Converts all six syntax value variants without binary floating-point or stringly evaluator boundaries. */
    fun lowerValue(value: ScalarValue): EngineeringValue = when (value) {
        is ScalarValue.Quantity -> EngineeringValue.Quantity(
            value = exactNumber(value.exactText),
            unit = definitionReference(value.unit),
            provenance = provenance(value.span),
        )
        is ScalarValue.Integer -> EngineeringValue.Integer(java.math.BigInteger(value.exactText))
        is ScalarValue.Boolean -> EngineeringValue.Boolean(value.value)
        is ScalarValue.Text -> EngineeringValue.Text(value.text)
        is ScalarValue.Symbol -> EngineeringValue.Symbol(value.text)
        is ScalarValue.Reference -> EngineeringValue.Reference(
            EngineeringReference(value.target.parts, null, provenance(value.target.span)),
        )
    }

    fun definitionReference(name: QualifiedName): EngineeringDefinitionReference =
        EngineeringDefinitionReference(name.parts, null, provenance(name.span))

    /** Converts a syntax-layer span into canonical authored provenance. */
    fun provenance(span: SourceSpan): SourceProvenance {
        return SourceProvenance(
            file = source.file,
            startLine = span.start.line,
            startColumn = span.start.column,
            endLine = span.end.line,
            endColumn = span.end.column,
        )
    }

    /** Creates one exact Entity blueprint, or declines invalid anatomy for source validation to diagnose. */
    fun entityOrNull(declaration: EntityDeclaration): AthenaDomainEntityBlueprint? {
        val concept = declaration.fields.singleOrNull { field -> field.name == "concept" }
            ?: return null
        val conceptValue = concept.value as? ScalarValue.Symbol
            ?: return null
        return AthenaDomainEntityBlueprint(
            name = declaration.name,
            conceptReference = EngineeringDefinitionReference(
                authoredName = listOf(conceptValue.text),
                resolvedId = null,
                provenance = provenance(conceptValue.span),
            ),
            properties = lowerProperties(declaration.fields.filterNot { field -> field.name == "concept" }),
            structureAssignments = declaration.structureAssignments.map { assignment ->
                EngineeringStructureAssignment(
                    aspectReference = definitionReference(assignment.aspect),
                    value = lowerValue(assignment.value),
                    displayDesignation = assignment.displayDesignation,
                    provenance = provenance(assignment.span),
                )
            },
            provenance = provenance(declaration.span),
        )
    }

    /** Creates one exact Port blueprint, or declines invalid anatomy for source validation to diagnose. */
    fun portOrNull(declaration: PortDeclaration): AthenaDomainPortBlueprint? {
        val fieldsByName = declaration.fields.groupBy { field -> field.name }
        val directionField = fieldsByName["direction"]?.singleOrNull() ?: return null
        val direction = when ((directionField.value as? ScalarValue.Symbol)?.text) {
            "in" -> EngineeringPortDirection.INPUT
            "out" -> EngineeringPortDirection.OUTPUT
            "bidirectional" -> EngineeringPortDirection.BIDIRECTIONAL
            else -> return null
        }
        val minimum = fieldsByName["minimum"].exactCardinalityOrNull(default = 0) ?: return null
        val maximum = when (val fields = fieldsByName["maximum"].orEmpty()) {
            emptyList<PropertyAssignment>() -> null
            else -> {
                val value = fields.singleOrNull()?.value ?: return null
                if (value is ScalarValue.Symbol && value.text == "unbounded") {
                    null
                } else {
                    value.cardinalityIntegerOrNull() ?: return null
                }
            }
        }
        if (minimum < 0 || maximum != null && maximum < minimum) return null
        val flowFields = fieldsByName["flow"].orEmpty()
        if (flowFields.any { field -> field.value !is ScalarValue.Symbol }) return null
        val fields = declaration.fields.associateBy { field -> field.name }
        val designationType = fields["designationType"]
        val designation = fields["designation"]
        if ((designationType == null) != (designation == null)) return null
        if (designationType != null && designationType.value !is ScalarValue.Symbol) return null
        return AthenaDomainPortBlueprint(
            ownerPath = declaration.qualifiedName.parts.dropLast(1),
            ownerProvenance = provenance(declaration.qualifiedName.span),
            name = declaration.qualifiedName.parts.last(),
            direction = direction,
            admittedFlowReferences = flowFields.map { field ->
                val symbol = field.value as ScalarValue.Symbol
                EngineeringDefinitionReference(listOf(symbol.text), null, provenance(symbol.span))
            },
            cardinality = EngineeringPortCardinality(
                minimum = minimum,
                maximum = maximum,
            ),
            interfaceDesignation = if (designationType == null && designation == null) {
                null
            } else {
                val designationTypeSymbol = designationType!!.value as ScalarValue.Symbol
                EngineeringInterfaceDesignation(
                    definitionReference = EngineeringDefinitionReference(
                        listOf(designationTypeSymbol.text),
                        null,
                        provenance(designationTypeSymbol.span),
                    ),
                    value = lowerValue(designation!!.value),
                    provenance = provenance(designation.span),
                )
            },
            properties = lowerProperties(declaration.fields.filterNot { field -> field.name in PORT_ANATOMY_FIELDS }),
            provenance = provenance(declaration.span),
        )
    }

    fun function(entity: EntityDeclaration, function: EngineeringFunctionDeclaration): AthenaDomainFunctionBlueprint =
        AthenaDomainFunctionBlueprint(
            ownerPath = listOf(entity.name),
            ownerProvenance = provenance(entity.span),
            name = function.name,
            roleReference = definitionReference(function.role),
            provenance = provenance(function.span),
        )

    private fun exactNumber(text: String): ExactNumber {
        val decimal = java.math.BigDecimal(text)
        return ExactNumber.of(decimal.unscaledValue(), java.math.BigInteger.TEN.pow(decimal.scale()))
    }

    private fun List<PropertyAssignment>?.exactCardinalityOrNull(default: Int): Int? {
        val fields = orEmpty()
        if (fields.isEmpty()) return default
        return fields.singleOrNull()?.value?.cardinalityIntegerOrNull()
    }

    private fun ScalarValue.cardinalityIntegerOrNull(): Int? =
        (this as? ScalarValue.Integer)?.exactText?.toIntOrNull()

    private companion object {
        val PORT_ANATOMY_FIELDS = setOf(
            "direction",
            "flow",
            "minimum",
            "maximum",
            "designationType",
            "designation",
        )
    }
}

/** Plugin-facing context passed to active domain plugins during the semantic-enrichment stage. */
data class AthenaSemanticEnrichmentContext(
    val document: EngineeringDocument,
    val source: AthenaSourceDocument? = null,
    val approvedPluginIds: List<String> = emptyList(),
) {
    /** Creates an inspectable enrichment note without changing canonical semantic ownership. */
    fun note(message: String): AthenaSemanticEnrichmentNote = AthenaSemanticEnrichmentNote(message)

    /** Creates a domain-scoped semantic diagnostic owned by the active enrichment contribution. */
    fun domainDiagnostic(
        ruleId: String,
        message: String,
        subjectIdentity: StableSemanticIdentity? = document.system.id,
        provenance: SourceProvenance = document.system.provenance,
        severity: SemanticDiagnosticSeverity = SemanticDiagnosticSeverity.WARNING,
        category: SemanticDiagnosticCategory = SemanticDiagnosticCategory.DOMAIN,
    ): SemanticDiagnostic {
        return SemanticDiagnostic(
            severity = severity,
            ruleId = SemanticRuleId(ruleId),
            category = category,
            subjectIdentity = subjectIdentity,
            provenance = provenance,
            message = message,
        )
    }
}

/** Plugin-facing validation context passed to active domain plugins during the validation stage. */
data class AthenaPluginValidationContext(
    val document: EngineeringDocument,
    val source: AthenaSourceDocument? = null,
    val approvedPluginIds: List<String> = emptyList(),
) {
    /** Creates a domain-scoped semantic diagnostic owned by the active plugin contribution. */
    fun domainDiagnostic(
        ruleId: String,
        message: String,
        subjectIdentity: StableSemanticIdentity? = document.system.id,
        provenance: SourceProvenance = document.system.provenance,
        severity: SemanticDiagnosticSeverity = SemanticDiagnosticSeverity.ERROR,
        category: SemanticDiagnosticCategory = SemanticDiagnosticCategory.DOMAIN,
    ): SemanticDiagnostic {
        return SemanticDiagnostic(
            severity = severity,
            ruleId = SemanticRuleId(ruleId),
            category = category,
            subjectIdentity = subjectIdentity,
            provenance = provenance,
            message = message,
        )
    }

    /** Emits one typed validation contribution tied to a declared validation contribution id. */
    fun emitValidationContribution(
        contributionId: String,
        diagnostics: List<SemanticDiagnostic>,
    ): AthenaPluginValidationEmission {
        return AthenaPluginValidationEmission(
            contributionId = contributionId,
            diagnostics = diagnostics,
        )
    }

    /** Emits one typed validation contribution tied to a declared validation contribution id. */
    fun emitValidationContribution(
        contributionId: String,
        vararg diagnostics: SemanticDiagnostic,
    ): AthenaPluginValidationEmission {
        return emitValidationContribution(
            contributionId = contributionId,
            diagnostics = diagnostics.toList(),
        )
    }
}

/** Plugin-owned validation diagnostics emitted under one declared validation contribution id. */
data class AthenaPluginValidationEmission(
    val contributionId: String,
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
)

/** Plugin-owned validation result returned from the compiler-owned validation stage. */
data class AthenaPluginValidationResult(
    val contributions: List<AthenaPluginValidationEmission> = emptyList(),
) {
    /** Flattened diagnostics preserved for callers that only need semantic diagnostics. */
    val diagnostics: List<SemanticDiagnostic>
        get() = contributions.flatMap { contribution -> contribution.diagnostics }

    companion object {
        /** Empty validation result used when a plugin does not emit validation diagnostics. */
        val EMPTY: AthenaPluginValidationResult = AthenaPluginValidationResult()
    }
}

/** Compiler-attributed plugin-owned validation diagnostics grouped by plugin id and contribution id. */
data class AthenaDomainValidationAttribution(
    val pluginId: String,
    val contributionId: String,
    val diagnostics: List<SemanticDiagnostic>,
) {
    /** Rule ids emitted by this plugin-owned validation attribution in diagnostic order. */
    val ruleIds: List<SemanticRuleId>
        get() = diagnostics.map { diagnostic -> diagnostic.ruleId }
}

/** Domain validation diagnostics aggregated by the compiler in deterministic approved-plugin order. */
data class AthenaDomainValidationContribution(
    val attributions: List<AthenaDomainValidationAttribution> = emptyList(),
) {
    /** Flattened domain diagnostics preserved for compiler and runtime consumers. */
    val diagnostics: List<SemanticDiagnostic>
        get() = attributions.flatMap { attribution -> attribution.diagnostics }

    companion object {
        /** Empty validation contribution used when no plugin emits diagnostics. */
        val EMPTY: AthenaDomainValidationContribution = AthenaDomainValidationContribution()
    }
}
