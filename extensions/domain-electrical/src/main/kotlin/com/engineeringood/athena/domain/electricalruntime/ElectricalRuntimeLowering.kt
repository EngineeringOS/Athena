package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution

internal fun lowerElectricalRuntime(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
    val deviceDeclarations = context.source.ast.declarations.filterIsInstance<DeviceDeclaration>()
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
    val ports = context.source.ast.declarations
        .filterIsInstance<PortDeclaration>()
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
    val connections = context.source.ast.declarations
        .filterIsInstance<ConnectionDeclaration>()
        .filter { declaration ->
            declaration.from.parts.firstOrNull() !in explicitForeignDeviceNames &&
                declaration.to.parts.firstOrNull() !in explicitForeignDeviceNames
        }
        .map { declaration ->
            context.connection(
                fromPath = declaration.from.parts,
                fromProvenance = context.provenance(declaration.from.span),
                toPath = declaration.to.parts,
                toProvenance = context.provenance(declaration.to.span),
                provenance = context.provenance(declaration.span),
            )
        }

    return AthenaDomainLoweringContribution(
        components = components,
        ports = ports,
        connections = connections,
    )
}

private fun PropertyAssignment.scalarIdentifierText(): String? {
    return when (val value = value) {
        is ScalarValue.Identifier -> value.text
        is ScalarValue.StringLiteral -> value.text
    }
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
