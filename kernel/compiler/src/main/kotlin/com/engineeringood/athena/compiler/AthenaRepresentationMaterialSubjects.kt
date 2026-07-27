package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.RepresentationBindingSubjectKind

internal object AthenaRepresentationMaterialSubjectDeriver {
    fun derive(
        document: EngineeringDocument,
        diagnostics: MutableList<AthenaRepresentationMaterialDiagnostic>,
    ): List<MaterialSubject> {
        val componentsById = document.components.associateBy { component -> component.id.value }
        val portsById = document.ports.associateBy { port -> port.id.value }
        val portsByOwner = document.ports.groupBy { port -> port.ownerReference.resolvedIdentity?.value }
        val functionsByOwner = document.functions.groupBy { function -> function.ownerReference.resolvedIdentity?.value }

        val deviceSubjects = document.components
            .filter { component -> functionsByOwner[component.id.value].isNullOrEmpty() }
            .mapNotNull { component ->
            val facts = component.materialSemanticFacts()
            val concept = facts["type"]
            if (concept == null) {
                diagnostics += materialDiagnostic(
                    "material.subject.type.missing",
                    component.id.value,
                    "Representable semantic devices require an authored engineering type.",
                )
                return@mapNotNull null
            }
            MaterialSubject(
                semanticSubjectId = component.id.value,
                physicalComponentId = component.id.value,
                functionId = null,
                conceptId = EngineeringConceptId(concept),
                semanticFacts = facts,
                ports = portsByOwner[component.id.value].orEmpty(),
                label = component.name,
                subjectKind = RepresentationBindingSubjectKind.DEVICE,
            )
        }
        val functionSubjects = document.functions.mapNotNull { function ->
            val ownerId = function.ownerReference.resolvedIdentity?.value
            val owner = ownerId?.let(componentsById::get)
            if (ownerId == null || owner == null) {
                diagnostics += materialDiagnostic(
                    "material.function.owner.unresolved",
                    function.id.value,
                    "Function owner must resolve to one canonical physical component.",
                )
                return@mapNotNull null
            }
            val facts = owner.materialSemanticFacts() + ("role" to function.role.value)
            val concept = facts["type"]
            if (concept == null) {
                diagnostics += materialDiagnostic(
                    "material.subject.type.missing",
                    function.id.value,
                    "Representable engineering functions require an authored physical-device type.",
                )
                return@mapNotNull null
            }
            val functionPorts = function.portReferences.mapIndexedNotNull { index, reference ->
                val resolvedPortId = reference.resolvedIdentity?.value
                val port = resolvedPortId?.let(portsById::get)
                if (resolvedPortId == null || port == null) {
                    diagnostics += materialDiagnostic(
                        "material.function.port.unresolved",
                        "${function.id.value}:${reference.authoredPath.joinToString(".").ifBlank { index.toString() }}",
                        "Function port reference must resolve to one canonical project port.",
                    )
                    null
                } else {
                    port
                }
            }
            if (functionPorts.size != function.portReferences.size) return@mapNotNull null
            MaterialSubject(
                semanticSubjectId = function.id.value,
                physicalComponentId = ownerId,
                functionId = function.id.value,
                conceptId = EngineeringConceptId(concept),
                semanticFacts = facts,
                ports = functionPorts,
                label = owner.name,
                subjectKind = RepresentationBindingSubjectKind.FUNCTION,
            )
        }
        return (deviceSubjects + functionSubjects).sortedBy { subject -> subject.semanticSubjectId }
    }
}

internal fun EngineeringComponent.materialSemanticFacts(): Map<String, String> = properties.associate { property ->
    property.name to property.value.materialText()
}.toSortedMap()

internal fun EngineeringComponent.materialPropertyValue(name: String): String? =
    properties.firstOrNull { property -> property.name == name }?.value?.materialText()

internal fun EngineeringPort.materialPropertyValue(name: String): String? =
    properties.firstOrNull { property -> property.name == name }?.value?.materialText()

private fun EngineeringPropertyValue.materialText(): String = when (this) {
    is EngineeringPropertyValue.Symbol -> text
    is EngineeringPropertyValue.Text -> text
}
