package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId

/** Rejects malformed authored anatomy before invalid facts can enter Engineering Reality. */
internal object EngineeringAnatomySourceValidator {
    fun validate(source: CompilerSourceDocument): List<SemanticDiagnostic> {
        val entities = source.ast.declarations.filterIsInstance<EntityDeclaration>()
        val ports = buildList {
            addAll(source.ast.declarations.filterIsInstance<PortDeclaration>())
            entities.forEach { entity ->
                addAll(entity.nestedPorts)
                entity.nestedFunctions.forEach { function -> addAll(function.nestedPorts) }
            }
        }
        val ownerPaths = buildSet {
            entities.forEach { entity ->
                add(listOf(entity.name))
                entity.nestedFunctions.forEach { function -> add(listOf(entity.name, function.name)) }
            }
        }
        return buildList {
            entities.forEach { entity -> addAll(validateEntity(source.file, entity)) }
            ports.forEach { port ->
                addAll(validatePort(source.file, port))
                val authoredOwner = port.qualifiedName.parts.dropLast(1)
                if (authoredOwner !in ownerPaths) {
                    val path = port.qualifiedName.parts.joinToString(".")
                    add(
                        diagnostic(
                            code = "reference.port-owner.unresolved",
                            subject = StableSemanticIdentity("port:$path"),
                            provenance = source.file.provenance(port.qualifiedName.span),
                            message = "Port owner `${authoredOwner.joinToString(".")}` does not resolve to any canonical semantic object.",
                        ),
                    )
                }
            }
        }
    }

    private fun validateEntity(file: String, entity: EntityDeclaration): List<SemanticDiagnostic> {
        val concepts = entity.fields.filter { field -> field.name == "concept" }
        return when {
            concepts.isEmpty() -> listOf(
                diagnostic(
                    code = "engineering.entity.concept.missing",
                    subject = StableSemanticIdentity("entity:${entity.name}"),
                    provenance = file.provenance(entity.span),
                    message = "Entity `${entity.name}` requires one Concept Symbol.",
                ),
            )
            concepts.size > 1 -> listOf(
                diagnostic(
                    code = "engineering.entity.concept.duplicate",
                    subject = StableSemanticIdentity("entity:${entity.name}"),
                    provenance = file.provenance(concepts.last().span),
                    message = "Entity `${entity.name}` declares Concept more than once; keep one Concept Symbol.",
                ),
            )
            concepts.single().value !is ScalarValue.Symbol -> listOf(
                diagnostic(
                    code = "engineering.entity.concept.invalid",
                    subject = StableSemanticIdentity("entity:${entity.name}"),
                    provenance = file.provenance(concepts.single().span),
                    message = "Entity `${entity.name}` Concept must be a Symbol, not ${concepts.single().value.kindName()}.",
                ),
            )
            else -> emptyList()
        }
    }

    private fun validatePort(file: String, port: PortDeclaration): List<SemanticDiagnostic> = buildList {
        val path = port.qualifiedName.parts.joinToString(".")
        val subject = StableSemanticIdentity("port:$path")
        val directions = port.fields.filter { field -> field.name == "direction" }
        when {
            directions.isEmpty() -> add(
                diagnostic(
                    code = "connectivity.port.direction.missing",
                    subject = subject,
                    provenance = file.provenance(port.span),
                    message = "Port `$path` requires Direction `in`, `out`, or `bidirectional`.",
                ),
            )
            directions.size > 1 -> add(
                diagnostic(
                    code = "engineering.port.direction.duplicate",
                    subject = subject,
                    provenance = file.provenance(directions.last().span),
                    message = "Port `$path` declares Direction more than once; keep one Direction.",
                ),
            )
            directions.single().value !is ScalarValue.Symbol ||
                (directions.single().value as ScalarValue.Symbol).text !in PORT_DIRECTIONS -> add(
                diagnostic(
                    code = "engineering.port.direction.invalid",
                    subject = subject,
                    provenance = file.provenance(directions.single().span),
                    message = "Port `$path` Direction must be `in`, `out`, or `bidirectional`.",
                ),
            )
        }

        port.fields.filter { field -> field.name == "flow" && field.value !is ScalarValue.Symbol }
            .forEach { flow ->
                add(
                    diagnostic(
                        code = "engineering.port.flow.invalid",
                        subject = subject,
                        provenance = file.provenance(flow.span),
                        message = "Port `$path` admitted Flow must be a Symbol, not ${flow.value.kindName()}.",
                    ),
                )
            }

        addAll(cardinalityDiagnostics(file, port, path, subject))
        addAll(designationDiagnostics(file, port, path, subject))
    }

    private fun cardinalityDiagnostics(
        file: String,
        port: PortDeclaration,
        path: String,
        subject: StableSemanticIdentity,
    ): List<SemanticDiagnostic> {
        val minimum = port.fields.singleCardinalityOrNull("minimum", 0)
        val maximum = port.fields.singleMaximumOrNull(minimum)
        val invalidField = when {
            port.fields.count { field -> field.name == "minimum" } > 1 -> port.fields.last { field -> field.name == "minimum" }
            port.fields.count { field -> field.name == "maximum" } > 1 -> port.fields.last { field -> field.name == "maximum" }
            minimum == null || minimum < 0 -> port.fields.firstOrNull { field -> field.name == "minimum" }
            maximum === INVALID_MAXIMUM || maximum is Int && maximum < minimum ->
                port.fields.firstOrNull { field -> field.name == "maximum" }
            else -> null
        } ?: return emptyList()
        return listOf(
            diagnostic(
                code = "engineering.port.cardinality.invalid",
                subject = subject,
                provenance = file.provenance(invalidField.span),
                message = "Port `$path` cardinality must use non-negative Integers with maximum at least minimum, or `unbounded`.",
            ),
        )
    }

    private fun designationDiagnostics(
        file: String,
        port: PortDeclaration,
        path: String,
        subject: StableSemanticIdentity,
    ): List<SemanticDiagnostic> {
        val types = port.fields.filter { field -> field.name == "designationType" }
        val values = port.fields.filter { field -> field.name == "designation" }
        val invalid = types.size > 1 || values.size > 1 || (types.isEmpty() != values.isEmpty()) ||
            types.singleOrNull()?.value?.let { value -> value !is ScalarValue.Symbol } == true
        if (!invalid) return emptyList()
        val field = (types + values).lastOrNull() ?: return emptyList()
        return listOf(
            diagnostic(
                code = "engineering.port.designation.invalid",
                subject = subject,
                provenance = file.provenance(field.span),
                message = "Port `$path` interface designation requires one designationType Symbol and one designation value.",
            ),
        )
    }

    private fun diagnostic(
        code: String,
        subject: StableSemanticIdentity,
        provenance: SourceProvenance,
        message: String,
    ): SemanticDiagnostic = SemanticDiagnostic(
        severity = SemanticDiagnosticSeverity.ERROR,
        ruleId = SemanticRuleId(code),
        category = SemanticDiagnosticCategory.PROPERTY,
        subjectIdentity = subject,
        provenance = provenance,
        message = message,
    )

    private fun String.provenance(span: SourceSpan): SourceProvenance = SourceProvenance(
        file = this,
        startLine = span.start.line,
        startColumn = span.start.column,
        endLine = span.end.line,
        endColumn = span.end.column,
    )

    private fun ScalarValue.kindName(): String = when (this) {
        is ScalarValue.Quantity -> "Quantity"
        is ScalarValue.Integer -> "Integer"
        is ScalarValue.Boolean -> "Boolean"
        is ScalarValue.Text -> "Text"
        is ScalarValue.Symbol -> "Symbol"
        is ScalarValue.Reference -> "Reference"
    }

    private fun List<PropertyAssignment>.singleCardinalityOrNull(name: String, default: Int): Int? {
        val matching = filter { field -> field.name == name }
        if (matching.isEmpty()) return default
        return (matching.singleOrNull()?.value as? ScalarValue.Integer)?.exactText?.toIntOrNull()
    }

    private fun List<PropertyAssignment>.singleMaximumOrNull(minimum: Int?): Any? {
        val matching = filter { field -> field.name == "maximum" }
        if (matching.isEmpty()) return minimum
        val value = matching.singleOrNull()?.value ?: return INVALID_MAXIMUM
        return if (value is ScalarValue.Symbol && value.text == "unbounded") {
            null
        } else {
            (value as? ScalarValue.Integer)?.exactText?.toIntOrNull() ?: INVALID_MAXIMUM
        }
    }

    private val PORT_DIRECTIONS = setOf("in", "out", "bidirectional")
    private val INVALID_MAXIMUM = Any()
}
