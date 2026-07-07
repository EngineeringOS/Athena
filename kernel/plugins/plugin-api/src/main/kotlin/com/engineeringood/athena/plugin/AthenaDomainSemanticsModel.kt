package com.engineeringood.athena.plugin

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.PropertyAssignment
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

/** Compiler-owned blueprint for one domain-contributed component before core identity assignment and resolution. */
data class AthenaDomainComponentBlueprint(
    val name: String,
    val kind: String,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

/** Compiler-owned blueprint for one domain-contributed port before core identity assignment and resolution. */
data class AthenaDomainPortBlueprint(
    val ownerPath: List<String>,
    val ownerProvenance: SourceProvenance,
    val name: String,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

/** Compiler-owned blueprint for one domain-contributed connection before core identity assignment and resolution. */
data class AthenaDomainConnectionBlueprint(
    val fromPath: List<String>,
    val fromProvenance: SourceProvenance,
    val toPath: List<String>,
    val toProvenance: SourceProvenance,
    val provenance: SourceProvenance,
)

/** Domain-owned lowering contribution aggregated by the compiler inside the declared lowering stage. */
data class AthenaDomainLoweringContribution(
    val components: List<AthenaDomainComponentBlueprint> = emptyList(),
    val ports: List<AthenaDomainPortBlueprint> = emptyList(),
    val connections: List<AthenaDomainConnectionBlueprint> = emptyList(),
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
                value = when (val value = assignment.value) {
                    is ScalarValue.Identifier -> EngineeringPropertyValue.Symbol(value.text)
                    is ScalarValue.StringLiteral -> EngineeringPropertyValue.Text(value.text)
                },
            )
        }
    }

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

    /** Creates a domain component blueprint using the provided authored semantics. */
    fun component(
        name: String,
        kind: String,
        properties: List<EngineeringProperty>,
        provenance: SourceProvenance = provenance(source.ast.system.span),
    ): AthenaDomainComponentBlueprint {
        return AthenaDomainComponentBlueprint(
            name = name,
            kind = kind,
            properties = properties,
            provenance = provenance,
        )
    }

    /** Creates a domain port blueprint using the provided authored semantics. */
    fun port(
        ownerPath: List<String>,
        ownerProvenance: SourceProvenance = provenance(source.ast.system.span),
        name: String,
        properties: List<EngineeringProperty>,
        provenance: SourceProvenance = provenance(source.ast.system.span),
    ): AthenaDomainPortBlueprint {
        return AthenaDomainPortBlueprint(
            ownerPath = ownerPath,
            ownerProvenance = ownerProvenance,
            name = name,
            properties = properties,
            provenance = provenance,
        )
    }

    /** Creates a domain connection blueprint using the provided authored semantics. */
    fun connection(
        fromPath: List<String>,
        fromProvenance: SourceProvenance = provenance(source.ast.system.span),
        toPath: List<String>,
        toProvenance: SourceProvenance = provenance(source.ast.system.span),
        provenance: SourceProvenance = provenance(source.ast.system.span),
    ): AthenaDomainConnectionBlueprint {
        return AthenaDomainConnectionBlueprint(
            fromPath = fromPath,
            fromProvenance = fromProvenance,
            toPath = toPath,
            toProvenance = toProvenance,
            provenance = provenance,
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
