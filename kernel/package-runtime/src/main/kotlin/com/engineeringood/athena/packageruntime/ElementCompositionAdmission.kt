package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.ElementComposition
import com.engineeringood.athena.packageplatform.PackageItemIdentity

data class ElementCompositionAdmissionResult(
    val composition: ElementComposition?,
    val diagnostics: List<SymbolGeometryDiagnostic>,
) {
    val isValid: Boolean get() = composition != null && diagnostics.isEmpty()
}

object ElementCompositionAdmission {
    fun admit(
        composition: ElementComposition,
        admittedChildren: Set<PackageItemIdentity>,
        declaredAnchorKeys: Map<String, Set<String>>,
    ): ElementCompositionAdmissionResult {
        val diagnostics = buildList {
            composition.children
                .filter { it.childId !in admittedChildren }
                .forEach { child ->
                    add(SymbolGeometryDiagnostic(
                        child.childId.key,
                        "Element child PackageItem is not admitted.",
                        "Admit child `${child.childId.key}` before publishing this Element.",
                        "element.child.unresolved",
                    ))
                }
            composition.ports.forEach { port ->
                val allowed = declaredAnchorKeys[port.childItemId.key].orEmpty()
                if (port.anchorKey !in allowed) {
                    add(SymbolGeometryDiagnostic(
                        port.portKey,
                        "Element port maps to an undeclared child Symbol anchor.",
                        "Map `${port.portKey}` to an admitted anchor from `${port.childItemId.key}`.",
                        "element.port.anchor-unresolved",
                    ))
                }
            }
        }
        return if (diagnostics.isEmpty()) ElementCompositionAdmissionResult(composition, emptyList())
        else ElementCompositionAdmissionResult(null, diagnostics)
    }
}
