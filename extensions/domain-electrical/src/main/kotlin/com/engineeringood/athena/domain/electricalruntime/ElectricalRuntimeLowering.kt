package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.ExternalEvidenceDeclaration
import com.engineeringood.athena.language.GridDeclaration
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.ProjectionConstructDeclaration
import com.engineeringood.athena.language.ProjectionPolicyDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.RegionDeclaration
import com.engineeringood.athena.language.RelationDeclaration
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SheetDeclaration
import com.engineeringood.athena.language.ViewDeclaration
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution

internal fun lowerElectricalRuntime(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
    // Exhaustive partition over Declaration: a future sealed variant (e.g. ImportDeclaration)
    // must break this when at compile time rather than being silently dropped.
    val entityDeclarations = mutableListOf<EntityDeclaration>()
    val portDeclarations = mutableListOf<PortDeclaration>()
    for (declaration in context.source.ast.declarations) {
        when (declaration) {
            is EntityDeclaration -> {
                entityDeclarations += declaration
                portDeclarations += declaration.nestedPorts
                portDeclarations += declaration.nestedFunctions.flatMap { function -> function.nestedPorts }
            }
            is PortDeclaration -> portDeclarations += declaration
            is RelationDeclaration -> Unit
            is ExternalEvidenceDeclaration -> Unit
            is ProjectionPolicyDeclaration -> Unit
            is LayoutDeclaration -> Unit
            is InstallationDeclaration -> Unit
            is ViewDeclaration -> Unit
            is SheetDeclaration -> Unit
            is GridDeclaration -> Unit
            is RegionDeclaration -> Unit
            is ProjectionConstructDeclaration -> Unit
        }
    }

    val explicitForeignEntityNames = entityDeclarations
        .filter { declaration -> declaration.isExplicitForeignDomain() }
        .map { declaration -> declaration.name }
        .toSet()
    val ownedEntityNames = entityDeclarations
        .filter { declaration -> declaration.isElectricalOwned() }
        .map { declaration -> declaration.name }
        .toSet()
    val entities = entityDeclarations
        .filter { declaration -> declaration.name in ownedEntityNames }
        .mapNotNull(context::entityOrNull)
    val lowerableEntityNames = entities.map { entity -> entity.name }.toSet()
    val groupedInterfacePorts = entityDeclarations
        .filter { declaration -> declaration.name !in explicitForeignEntityNames }
        .flatMap { entity -> entity.groupedInterfacePorts() }
    val ports = (portDeclarations + groupedInterfacePorts)
        .filter { declaration -> declaration.qualifiedName.parts.firstOrNull() in lowerableEntityNames }
        .mapNotNull(context::portOrNull)
    val functions = entityDeclarations
        .filter { declaration -> declaration.name in ownedEntityNames }
        .filter { declaration -> declaration.name in lowerableEntityNames }
        .flatMap { entity ->
            entity.nestedFunctions.map { function ->
                context.function(entity, function)
            }
        }

    return AthenaDomainLoweringContribution(
        entities = entities,
        ports = ports,
        functions = functions,
    )
}

private fun PropertyAssignment.scalarIdentifierText(): String? {
    return when (val value = value) {
        is ScalarValue.Symbol -> value.text
        is ScalarValue.Text -> value.text
        is ScalarValue.Boolean,
        is ScalarValue.Integer,
        is ScalarValue.Quantity,
        is ScalarValue.Reference,
            -> null
    }
}

private fun EntityDeclaration.groupedInterfacePorts(): List<PortDeclaration> {
    return interfaces.flatMap { connectivityInterface ->
        val interfaceDefaults = connectivityInterface.fields.filterNot { field ->
            field.name == "type" || field.name == "class"
        }
        connectivityInterface.ports.map { member ->
            val memberFieldNames = member.fields.map { field -> field.name }.toSet()
            val defaultFields = interfaceDefaults.filter { field -> field.name !in memberFieldNames }
            PortDeclaration(
                qualifiedName = com.engineeringood.athena.language.QualifiedName(
                    parts = listOf(name, member.name),
                    span = member.span,
                ),
                fields = defaultFields +
                    interfaceMembership(connectivityInterface.name, connectivityInterface.span) +
                    member.fields,
                span = member.span,
            )
        }
    }
}

private fun interfaceMembership(interfaceName: String, span: com.engineeringood.athena.language.SourceSpan): PropertyAssignment {
    return PropertyAssignment(
        name = "interface",
        value = ScalarValue.Symbol(interfaceName, span),
        span = span,
    )
}

private fun EntityDeclaration.isElectricalOwned(): Boolean {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText().let { domain ->
        domain == null || domain == ELECTRICAL_DOMAIN_ID
    }
}

private fun EntityDeclaration.isExplicitForeignDomain(): Boolean {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText().let { domain ->
        domain != null && domain != ELECTRICAL_DOMAIN_ID
    }
}
