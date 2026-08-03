package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.ConnectionGroupDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
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
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution

internal fun lowerElectricalRuntime(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
    // Exhaustive partition over Declaration: a future sealed variant (e.g. ImportDeclaration)
    // must break this when at compile time rather than being silently dropped.
    val deviceDeclarations = mutableListOf<DeviceDeclaration>()
    val portDeclarations = mutableListOf<PortDeclaration>()
    val connectionDeclarations = mutableListOf<ConnectionDeclaration>()
    val relationDeclarations = mutableListOf<RelationDeclaration>()
    for (declaration in context.source.ast.declarations) {
        when (declaration) {
            is DeviceDeclaration -> {
                deviceDeclarations += declaration
                portDeclarations += declaration.nestedPorts
            }
            is PortDeclaration -> portDeclarations += declaration
            is ConnectionDeclaration -> connectionDeclarations += declaration
            is ConnectionGroupDeclaration -> connectionDeclarations += declaration.connections
            is RelationDeclaration -> relationDeclarations += declaration
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

    val explicitForeignDeviceNames = deviceDeclarations
        .filter { declaration -> declaration.isExplicitForeignDomain() }
        .map { declaration -> declaration.name }
        .toSet()
    val ownedDeviceNames = deviceDeclarations
        .filter { declaration -> declaration.isElectricalOwned() }
        .map { declaration -> declaration.name }
        .toSet()
    val components = deviceDeclarations
        .filter { declaration -> declaration.name in ownedDeviceNames }
        .map { declaration ->
            context.component(
                name = declaration.name,
                kind = "device",
                properties = context.lowerProperties(declaration.fields),
                provenance = context.provenance(declaration.span),
            )
        }
    val groupedInterfacePorts = deviceDeclarations
        .filter { declaration -> declaration.name !in explicitForeignDeviceNames }
        .flatMap { device -> device.groupedInterfacePorts() }
    val ports = (portDeclarations + groupedInterfacePorts)
        .filter { declaration -> declaration.qualifiedName.parts.firstOrNull() !in explicitForeignDeviceNames }
        .map { declaration ->
            context.port(
                ownerPath = declaration.qualifiedName.parts.dropLast(1),
                ownerProvenance = context.provenance(declaration.qualifiedName.span),
                name = declaration.qualifiedName.parts.last(),
                properties = context.lowerProperties(declaration.fields),
                provenance = context.provenance(declaration.span),
            )
        }
    val connections = connectionDeclarations
        .filter { declaration ->
            declaration.from.parts.firstOrNull() !in explicitForeignDeviceNames &&
                declaration.to.parts.firstOrNull() !in explicitForeignDeviceNames
        }
            .map { declaration ->
            context.connection(
                alias = declaration.alias,
                fromPath = declaration.from.parts,
                fromProvenance = context.provenance(declaration.from.span),
                toPath = declaration.to.parts,
                toProvenance = context.provenance(declaration.to.span),
                provenance = context.provenance(declaration.span),
            )
        } + relationDeclarations
        .filter { declaration -> declaration.word.value in ELECTRICAL_RELATION_WORDS }
        .flatMap { declaration -> declaration.toElectricalRelationConnections(context, explicitForeignDeviceNames) }
    val functions = deviceDeclarations
        .filter { declaration -> declaration.name in ownedDeviceNames }
        .flatMap { device ->
            device.nestedFunctions.map { function ->
                context.function(
                    ownerPath = listOf(device.name),
                    ownerProvenance = context.provenance(device.span),
                    name = function.name,
                    role = function.role.value,
                    portReferences = function.portReferences.map { reference ->
                        context.functionPort(
                            path = if (reference.parts.size == 1) listOf(device.name) + reference.parts else reference.parts,
                            provenance = context.provenance(reference.span),
                        )
                    },
                    provenance = context.provenance(function.span),
                )
            }
        }

    return AthenaDomainLoweringContribution(
        components = components,
        ports = ports,
        connections = connections,
        functions = functions,
    )
}

private fun PropertyAssignment.scalarIdentifierText(): String? {
    return when (val value = value) {
        is ScalarValue.Identifier -> value.text
        is ScalarValue.StringLiteral -> value.text
    }
}

private fun DeviceDeclaration.groupedInterfacePorts(): List<PortDeclaration> {
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

private fun RelationDeclaration.toElectricalRelationConnections(
    context: AthenaDomainLoweringContext,
    explicitForeignDeviceNames: Set<String>,
): List<com.engineeringood.athena.plugin.AthenaDomainConnectionBlueprint> {
    if (from.parts.firstOrNull() in explicitForeignDeviceNames) return emptyList()
    return targets
        .filter { target -> target.parts.firstOrNull() !in explicitForeignDeviceNames }
        .map { target ->
            context.connection(
                alias = relationMemberAlias(word.value, from, target),
                fromPath = from.parts,
                fromProvenance = context.provenance(from.span),
                toPath = target.parts,
                toProvenance = context.provenance(target.span),
                provenance = context.provenance(span),
                properties = listOf(relationKindProperty(word.value)),
            )
        }
}

internal fun relationMemberAlias(relationWord: String, from: QualifiedName, target: QualifiedName): String =
    "${relationWord}_${from.parts.joinToString("_")}_to_${target.parts.joinToString("_")}"

internal fun relationNetworkName(relationWord: String, from: QualifiedName): String =
    "${relationWord}_${from.parts.joinToString("_")}"

private fun relationKindProperty(relationWord: String): EngineeringProperty =
    EngineeringProperty(
        name = "relation.kind",
        value = EngineeringPropertyValue.Symbol(relationWord),
    )

private fun interfaceMembership(interfaceName: String, span: com.engineeringood.athena.language.SourceSpan): PropertyAssignment {
    return PropertyAssignment(
        name = "interface",
        value = ScalarValue.Identifier(interfaceName, span),
        span = span,
    )
}

private fun DeviceDeclaration.isElectricalOwned(): Boolean {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText().let { domain ->
        domain == null || domain == ELECTRICAL_DOMAIN_ID
    }
}

private fun DeviceDeclaration.isExplicitForeignDomain(): Boolean {
    return fields.firstOrNull { field -> field.name == "domain" }?.scalarIdentifierText().let { domain ->
        domain != null && domain != ELECTRICAL_DOMAIN_ID
    }
}
