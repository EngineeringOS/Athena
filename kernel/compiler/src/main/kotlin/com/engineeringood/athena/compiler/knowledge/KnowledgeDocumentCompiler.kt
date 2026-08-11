package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.knowledge.EngineeringKnowledgeDocument
import com.engineeringood.athena.knowledge.FormulaContractValidator
import com.engineeringood.athena.knowledge.FormulaExpression
import com.engineeringood.athena.knowledge.KnowledgeCompilationDiagnostic
import com.engineeringood.athena.knowledge.KnowledgeCompilationResult
import com.engineeringood.athena.knowledge.KnowledgeDefinition
import com.engineeringood.athena.knowledge.KnowledgePackageIdentity
import com.engineeringood.athena.knowledge.RelationshipRoleDefinition
import com.engineeringood.athena.knowledge.SubjectLevel
import com.engineeringood.athena.language.KnowledgeBinaryOperator
import com.engineeringood.athena.language.KnowledgeCapabilityDeclaration
import com.engineeringood.athena.language.KnowledgeCapabilityDirection
import com.engineeringood.athena.language.KnowledgeConceptDeclaration
import com.engineeringood.athena.language.KnowledgeConstraintDeclaration
import com.engineeringood.athena.language.KnowledgeDeclaration
import com.engineeringood.athena.language.KnowledgeDimensionDeclaration
import com.engineeringood.athena.language.KnowledgeExpression
import com.engineeringood.athena.language.KnowledgeFlowDeclaration
import com.engineeringood.athena.language.KnowledgeFormulaDeclaration
import com.engineeringood.athena.language.KnowledgeFunction
import com.engineeringood.athena.language.KnowledgePartDeclaration
import com.engineeringood.athena.language.KnowledgePredicate
import com.engineeringood.athena.language.KnowledgeRelationshipDeclaration
import com.engineeringood.athena.language.KnowledgeSourceUnit
import com.engineeringood.athena.language.KnowledgeSubjectLevel
import com.engineeringood.athena.language.KnowledgeUnitDeclaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringDimensionSignature
import com.engineeringood.athena.ir.EngineeringPackageName
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.ExactNumber
import com.engineeringood.athena.ir.SourceProvenance
import java.math.BigDecimal
import java.math.BigInteger

data class KnowledgePackageSource(
    val packageIdentity: KnowledgePackageIdentity,
    val relativePath: String,
    val ast: SourceFileAst,
)

/** Compiles package-local source into one fail-closed deterministic Knowledge Document. */
class KnowledgeDocumentCompiler {
    fun compile(sources: List<KnowledgePackageSource>): KnowledgeCompilationResult {
        val diagnostics = mutableListOf<KnowledgeCompilationDiagnostic>()
        val definitions = mutableListOf<KnowledgeDefinition>()
        val packages = sources.map { it.packageIdentity }.distinct().sortedWith(compareBy({ it.name }, { it.version }))
        sources.sortedWith(compareBy({ it.packageIdentity.name }, { it.relativePath })).forEach { source ->
            val unit = source.ast.unit as? KnowledgeSourceUnit ?: run {
                diagnostics += diagnostic("knowledge.source.kind", source, "Source must contain one default domain.")
                return@forEach
            }
            val packageName = runCatching { EngineeringPackageName(source.packageIdentity.name) }.getOrElse {
                diagnostics += diagnostic("knowledge.package.identity", source, "Knowledge package identity is invalid.")
                return@forEach
            }
            unit.declarations.forEach { declaration ->
                val id = EngineeringDefinitionId(packageName, "${unit.domain}.${declaration.name}")
                if (definitions.any { it.id == id }) {
                    diagnostics += KnowledgeCompilationDiagnostic(
                        code = "knowledge.definition.duplicate",
                        subject = id.toString(),
                        message = "Knowledge definition `$id` appears more than once.",
                        provenance = provenance(source, declaration),
                    )
                    return@forEach
                }
                runCatching { convert(declaration, id, packageName, unit.domain, source) }
                    .onSuccess { definitions += it }
                    .onFailure { failure ->
                        diagnostics += KnowledgeCompilationDiagnostic(
                            code = "knowledge.definition.invalid",
                            subject = id.toString(),
                            message = failure.message ?: "Knowledge definition is invalid.",
                            provenance = provenance(source, declaration),
                        )
                    }
            }
        }
        val sortedDiagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.provenance?.file.orEmpty() }))
        if (sortedDiagnostics.isNotEmpty()) return KnowledgeCompilationResult.Failure(sortedDiagnostics)
        return KnowledgeCompilationResult.Success(
            EngineeringKnowledgeDocument(
                packages = packages,
                definitions = definitions.sortedBy { it.id },
                provenance = sources.sortedBy { it.relativePath }.mapNotNull { source -> source.ast.knowledgeDeclarations.firstOrNull()?.let { provenance(source, it) } },
            ),
        )
    }

    private fun convert(
        declaration: KnowledgeDeclaration,
        id: EngineeringDefinitionId,
        packageName: EngineeringPackageName,
        domain: String,
        source: KnowledgePackageSource,
    ): KnowledgeDefinition {
        val provenance = provenance(source, declaration)
        fun ref(parts: List<String>) = EngineeringDefinitionReference(
            authoredName = parts,
            resolvedId = EngineeringDefinitionId(packageName, if (parts.size == 1) "$domain.${parts.single()}" else parts.joinToString(".")),
            provenance = provenance,
        )
        return when (declaration) {
            is KnowledgeConceptDeclaration -> KnowledgeDefinition.Concept(id, declaration.properties.associate { it.name to propertyType(it.value) }, provenance = provenance)
            is KnowledgePartDeclaration -> KnowledgeDefinition.Part(id, ref(declaration.concept.parts), declaration.properties.associate { it.name to engineeringValue(it.value, provenance, ::ref) }, provenance)
            is KnowledgeCapabilityDeclaration -> KnowledgeDefinition.Capability(id, declaration.properties.associate { it.name to propertyType(it.value) }, provenance)
            is KnowledgeRelationshipDeclaration -> KnowledgeDefinition.Relationship(
                id = id,
                roles = declaration.roles.map { role ->
                    RelationshipRoleDefinition(role.name, role.level.toModel(), requiredCapability = role.requiredCapability?.let { ref(it.parts) })
                },
                connectivity = declaration.connectivity,
                provenance = provenance,
            )
            is KnowledgeFlowDeclaration -> KnowledgeDefinition.Flow(id, declaration.sourceRole, declaration.sinkRole, declaration.medium?.let { ref(it.parts) }, provenance)
            is KnowledgeDimensionDeclaration -> KnowledgeDefinition.Dimension(id, declaration.bases.associate { base -> EngineeringDefinitionId(packageName, base.parts.joinToString(".")) to 1 }, provenance)
            is KnowledgeUnitDeclaration -> KnowledgeDefinition.Unit(id, ref(declaration.dimension.parts), exactNumber(declaration.scale), declaration.offset?.let(::exactNumber) ?: ExactNumber.ZERO, provenance)
            is KnowledgeFormulaDeclaration -> KnowledgeDefinition.Formula(id, formula(declaration.expression, packageName, domain, provenance), provenance)
            is KnowledgeConstraintDeclaration -> KnowledgeDefinition.Constraint(id, predicate(declaration.predicate, packageName, domain, provenance), provenance)
        }
    }

    private fun propertyType(value: ScalarValue) = when (value) {
        is ScalarValue.Quantity -> com.engineeringood.athena.knowledge.KnowledgePropertyType.QUANTITY
        is ScalarValue.Integer -> com.engineeringood.athena.knowledge.KnowledgePropertyType.INTEGER
        is ScalarValue.Boolean -> com.engineeringood.athena.knowledge.KnowledgePropertyType.BOOLEAN
        is ScalarValue.Text -> com.engineeringood.athena.knowledge.KnowledgePropertyType.TEXT
        is ScalarValue.Symbol -> com.engineeringood.athena.knowledge.KnowledgePropertyType.SYMBOL
        is ScalarValue.Reference -> com.engineeringood.athena.knowledge.KnowledgePropertyType.REFERENCE
    }

    private fun engineeringValue(value: ScalarValue, provenance: SourceProvenance, ref: (List<String>) -> EngineeringDefinitionReference): EngineeringValue = when (value) {
        is ScalarValue.Integer -> EngineeringValue.Integer(BigInteger(value.exactText))
        is ScalarValue.Boolean -> EngineeringValue.Boolean(value.value)
        is ScalarValue.Text -> EngineeringValue.Text(value.text)
        is ScalarValue.Symbol -> EngineeringValue.Symbol(value.text)
        is ScalarValue.Reference -> EngineeringValue.Reference(com.engineeringood.athena.ir.EngineeringReference(value.target.parts, null, provenance))
        is ScalarValue.Quantity -> EngineeringValue.Quantity(exactNumber(value), ref(value.unit.parts), provenance)
    }

    private fun exactNumber(value: ScalarValue): ExactNumber = when (value) {
        is ScalarValue.Quantity -> exactNumber(value.exactText)
        is ScalarValue.Integer -> exactNumber(value.exactText)
        else -> error("Knowledge numeric value must be an exact integer or quantity")
    }

    private fun exactNumber(text: String): ExactNumber {
        val decimal = BigDecimal(text)
        return ExactNumber.of(decimal.unscaledValue(), BigInteger.TEN.pow(decimal.scale().coerceAtLeast(0)))
    }

    private fun formula(expression: KnowledgeExpression, packageName: EngineeringPackageName, domain: String, provenance: SourceProvenance): FormulaExpression = when (expression) {
        is KnowledgeExpression.Number -> FormulaExpression.Literal(EngineeringValue.Integer(expression.value.toBigDecimal().toBigInteger()))
        is KnowledgeExpression.Reference -> FormulaExpression.Reference(EngineeringDefinitionReference(expression.name.parts, EngineeringDefinitionId(packageName, if (expression.name.parts.size == 1) "$domain.${expression.name.parts.single()}" else expression.name.parts.joinToString(".")), provenance))
        is KnowledgeExpression.Binary -> when (expression.operator) {
            KnowledgeBinaryOperator.ADD -> FormulaExpression.Add(formula(expression.left, packageName, domain, provenance), formula(expression.right, packageName, domain, provenance))
            KnowledgeBinaryOperator.SUBTRACT -> FormulaExpression.Subtract(formula(expression.left, packageName, domain, provenance), formula(expression.right, packageName, domain, provenance))
            KnowledgeBinaryOperator.MULTIPLY -> FormulaExpression.Multiply(formula(expression.left, packageName, domain, provenance), formula(expression.right, packageName, domain, provenance))
            KnowledgeBinaryOperator.DIVIDE -> FormulaExpression.Divide(formula(expression.left, packageName, domain, provenance), formula(expression.right, packageName, domain, provenance))
        }
        is KnowledgeExpression.Call -> when (expression.function) {
            KnowledgeFunction.MIN -> FormulaExpression.Min(formula(expression.arguments[0], packageName, domain, provenance), formula(expression.arguments[1], packageName, domain, provenance))
            KnowledgeFunction.MAX -> FormulaExpression.Max(formula(expression.arguments[0], packageName, domain, provenance), formula(expression.arguments[1], packageName, domain, provenance))
            KnowledgeFunction.ROUND_UP -> FormulaExpression.RoundUp(formula(expression.arguments[0], packageName, domain, provenance), formula(expression.arguments[1], packageName, domain, provenance))
        }
    }

    private fun predicate(predicate: KnowledgePredicate, packageName: EngineeringPackageName, domain: String, provenance: SourceProvenance): com.engineeringood.athena.knowledge.ConstraintPredicate = when (predicate) {
        is KnowledgePredicate.Comparison -> com.engineeringood.athena.knowledge.ConstraintPredicate.Comparison(com.engineeringood.athena.knowledge.ComparisonOperator.valueOf(predicate.operator.name), formula(predicate.left, packageName, domain, provenance), formula(predicate.right, packageName, domain, provenance))
        is KnowledgePredicate.Membership -> com.engineeringood.athena.knowledge.ConstraintPredicate.InInterval(formula(predicate.value, packageName, domain, provenance), com.engineeringood.athena.knowledge.ExactInterval(formula(predicate.interval.lower, packageName, domain, provenance), formula(predicate.interval.upper, packageName, domain, provenance), predicate.interval.lowerInclusive, predicate.interval.upperInclusive))
        is KnowledgePredicate.All -> com.engineeringood.athena.knowledge.ConstraintPredicate.All(predicate.predicates.map { p -> this.predicate(p, packageName, domain, provenance) })
        is KnowledgePredicate.Any -> com.engineeringood.athena.knowledge.ConstraintPredicate.Any(predicate.predicates.map { p -> this.predicate(p, packageName, domain, provenance) })
    }

    private fun KnowledgeSubjectLevel.toModel() = when (this) {
        KnowledgeSubjectLevel.ENTITY -> SubjectLevel.ENTITY
        KnowledgeSubjectLevel.FUNCTION -> SubjectLevel.FUNCTION
        KnowledgeSubjectLevel.PORT -> SubjectLevel.PORT
    }

    private fun provenance(source: KnowledgePackageSource, declaration: KnowledgeDeclaration) = SourceProvenance(source.relativePath, declaration.span.start.line, declaration.span.start.column, declaration.span.end.line, declaration.span.end.column)

    private fun diagnostic(code: String, source: KnowledgePackageSource, message: String) = KnowledgeCompilationDiagnostic(code, source.relativePath, message, null)
}
