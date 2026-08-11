package com.engineeringood.athena.knowledge

import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringDimensionSignature
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.ExactNumber
import com.engineeringood.athena.ir.SourceProvenance

/** Package identity. Version is provenance, never semantic identity. */
data class KnowledgePackageIdentity(val name: String, val version: String) {
    init { require(name.isNotBlank() && version.isNotBlank()) }
}

/** Immutable result of compiling one locked package set. */
data class EngineeringKnowledgeDocument(
    val packages: List<KnowledgePackageIdentity>,
    val definitions: List<KnowledgeDefinition>,
    val provenance: List<SourceProvenance>,
) {
    init {
        require(packages == packages.sortedWith(compareBy({ it.name }, { it.version })))
        require(definitions == definitions.sortedBy { it.id })
        require(definitions.map { it.id }.distinct().size == definitions.size)
    }

    /** Stable UTF-8-independent canonical contract used by later transport story. */
    fun canonicalText(): String = buildString {
        append("knowledge-document\n")
        packages.sortedWith(compareBy({ it.name }, { it.version })).forEach { append("package|").append(it.name).append('|').append(it.version).append('\n') }
        definitions.sortedBy { it.id }.forEach { definition ->
            val kind = when (definition) {
                is KnowledgeDefinition.Concept -> "concept"
                is KnowledgeDefinition.Part -> "part"
                is KnowledgeDefinition.Capability -> "capability"
                is KnowledgeDefinition.Relationship -> "relationship"
                is KnowledgeDefinition.Flow -> "flow"
                is KnowledgeDefinition.Dimension -> "dimension"
                is KnowledgeDefinition.Unit -> "unit"
                is KnowledgeDefinition.Formula -> "formula"
                is KnowledgeDefinition.Constraint -> "constraint"
            }
            append(definition.id).append('|').append(kind).append('|').append(definition.provenance.file).append('|')
                .append(definition.provenance.startLine).append(':').append(definition.provenance.startColumn).append('\n')
        }
    }
}

data class KnowledgeCompilationDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val provenance: SourceProvenance?,
)

sealed interface KnowledgeCompilationResult {
    data class Success(val document: EngineeringKnowledgeDocument) : KnowledgeCompilationResult
    data class Failure(val diagnostics: List<KnowledgeCompilationDiagnostic>) : KnowledgeCompilationResult {
        init { require(diagnostics.isNotEmpty()) }
    }
}

/** Closed definition surface authored by governed package source. */
sealed interface KnowledgeDefinition {
    val id: EngineeringDefinitionId
    val provenance: SourceProvenance

    data class Concept(
        override val id: EngineeringDefinitionId,
        val properties: Map<String, KnowledgePropertyType> = emptyMap(),
        val providedCapabilities: List<EngineeringDefinitionReference> = emptyList(),
        val requiredCapabilities: List<EngineeringDefinitionReference> = emptyList(),
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition

    data class Part(
        override val id: EngineeringDefinitionId,
        val concept: EngineeringDefinitionReference,
        val facts: Map<String, EngineeringValue> = emptyMap(),
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition

    data class Capability(
        override val id: EngineeringDefinitionId,
        val parameters: Map<String, KnowledgePropertyType> = emptyMap(),
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition

    data class Relationship(
        override val id: EngineeringDefinitionId,
        val roles: List<RelationshipRoleDefinition>,
        val connectivity: Boolean,
        val admittedFlows: List<EngineeringDefinitionReference> = emptyList(),
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition {
        init { require(roles.map { it.name }.distinct().size == roles.size) }
    }

    data class Flow(
        override val id: EngineeringDefinitionId,
        val sourceRole: String,
        val sinkRole: String,
        val medium: EngineeringDefinitionReference?,
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition

    data class Dimension(
        override val id: EngineeringDefinitionId,
        val bases: Map<EngineeringDefinitionId, Int>,
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition

    data class Unit(
        override val id: EngineeringDefinitionId,
        val dimension: EngineeringDefinitionReference,
        val scale: ExactNumber,
        val offset: ExactNumber = ExactNumber.ZERO,
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition {
        init { require(scale > ExactNumber.ZERO) { "Knowledge unit scale must be positive." } }
    }

    data class Formula(
        override val id: EngineeringDefinitionId,
        val expression: FormulaExpression,
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition

    data class Constraint(
        override val id: EngineeringDefinitionId,
        val predicate: ConstraintPredicate,
        override val provenance: SourceProvenance,
    ) : KnowledgeDefinition
}

data class RelationshipRoleDefinition(
    val name: String,
    val subjectLevel: SubjectLevel,
    val minimum: Int = 1,
    val maximum: Int? = 1,
    val requiredCapability: EngineeringDefinitionReference? = null,
) {
    init {
        require(name.isNotBlank())
        require(minimum >= 0 && (maximum == null || maximum >= minimum))
    }
}

enum class SubjectLevel { ENTITY, FUNCTION, PORT }

enum class KnowledgePropertyType { QUANTITY, INTEGER, BOOLEAN, TEXT, SYMBOL, REFERENCE }

sealed interface FormulaExpression {
    data class Literal(val value: EngineeringValue) : FormulaExpression
    data class Reference(val reference: EngineeringDefinitionReference) : FormulaExpression
    data class Add(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression
    data class Subtract(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression
    data class Multiply(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression
    data class Divide(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression
    data class Min(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression
    data class Max(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression
    data class RoundUp(val value: FormulaExpression, val quantum: FormulaExpression) : FormulaExpression
}

enum class ComparisonOperator { GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, EQUAL, NOT_EQUAL }

sealed interface ConstraintPredicate {
    data class Comparison(val operator: ComparisonOperator, val left: FormulaExpression, val right: FormulaExpression) : ConstraintPredicate
    data class InInterval(val value: FormulaExpression, val interval: ExactInterval) : ConstraintPredicate
    data class All(val predicates: List<ConstraintPredicate>) : ConstraintPredicate
    data class Any(val predicates: List<ConstraintPredicate>) : ConstraintPredicate
}

data class ExactInterval(
    val lower: FormulaExpression,
    val upper: FormulaExpression,
    val lowerInclusive: Boolean,
    val upperInclusive: Boolean,
)

data class FormulaDiagnostic(val code: String, val message: String)

/** Static checks for closed formula algebra; evaluator belongs later story. */
object FormulaContractValidator {
    fun validate(
        expression: FormulaExpression,
        dimensions: Map<EngineeringDefinitionId, EngineeringDimensionSignature> = emptyMap(),
    ): List<FormulaDiagnostic> {
        val diagnostics = mutableListOf<FormulaDiagnostic>()
        fun dimensionOf(value: FormulaExpression): EngineeringDimensionSignature? = when (value) {
            is FormulaExpression.Literal -> (value.value as? EngineeringValue.Quantity)?.let { quantity ->
                dimensions[quantity.unit.resolvedId]
            }
            is FormulaExpression.Reference -> dimensions[value.reference.resolvedId]
            is FormulaExpression.Add -> dimensionOf(value.left) ?: dimensionOf(value.right)
            is FormulaExpression.Subtract -> dimensionOf(value.left) ?: dimensionOf(value.right)
            is FormulaExpression.Min -> {
                val left = dimensionOf(value.left); val right = dimensionOf(value.right)
                if (left != null && right != null && left != right) diagnostics += FormulaDiagnostic("formula.dimension.mismatch", "min operands must have compatible dimensions")
                left ?: right
            }
            is FormulaExpression.Max -> {
                val left = dimensionOf(value.left); val right = dimensionOf(value.right)
                if (left != null && right != null && left != right) diagnostics += FormulaDiagnostic("formula.dimension.mismatch", "max operands must have compatible dimensions")
                left ?: right
            }
            is FormulaExpression.Multiply -> dimensionOf(value.left)?.let { left -> dimensionOf(value.right)?.let { right -> left + right } }
            is FormulaExpression.Divide -> dimensionOf(value.left)?.let { left -> dimensionOf(value.right)?.let { right -> left - right } }
            is FormulaExpression.RoundUp -> {
                val quantum = (value.quantum as? FormulaExpression.Literal)?.value
                if (quantum is EngineeringValue.Integer && quantum.value <= java.math.BigInteger.ZERO) diagnostics += FormulaDiagnostic("formula.round-up.quantum", "round-up quantum must be positive")
                dimensionOf(value.value)
            }
        }
        dimensionOf(expression)
        return diagnostics.toList()
    }
}
