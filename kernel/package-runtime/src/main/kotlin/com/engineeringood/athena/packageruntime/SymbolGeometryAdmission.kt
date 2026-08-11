package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.PortCompatibilityContract
import com.engineeringood.athena.packageplatform.SymbolGeometry

data class SymbolGeometryAdmissionResult(
    val geometry: SymbolGeometry?,
    val diagnostics: List<SymbolGeometryDiagnostic>,
) {
    val isValid: Boolean get() = geometry != null && diagnostics.isEmpty()
}

data class SymbolGeometryDiagnostic(
    val subject: String,
    val problem: String,
    val correction: String,
    val code: String,
)

object SymbolGeometryAdmission {
    fun admit(
        geometry: SymbolGeometry,
        declaredResourcePaths: Set<String> = setOf(geometry.resource),
        sourceOwnedPortKeys: Set<String> = emptySet(),
    ): SymbolGeometryAdmissionResult {
        val diagnostics = buildList {
            if (geometry.resource !in declaredResourcePaths) {
                add(SymbolGeometryDiagnostic(
                    geometry.resource,
                    "Symbol resource is not declared by package resources.",
                    "Declare the package-local SVG resource before admitting Symbol geometry.",
                    "symbol.resource.undeclared",
                ))
            }
            if (geometry.anchors.map { it.key }.distinct().size != geometry.anchors.size) {
                add(SymbolGeometryDiagnostic(
                    "symbol",
                    "Symbol anchor keys are duplicated.",
                    "Use one stable anchor key for each connection point.",
                    "symbol.anchor.duplicate",
                ))
            }
            geometry.anchors.forEach { anchor ->
                val compatibility: PortCompatibilityContract = anchor.compatibility
                if (compatibility.key != anchor.key) {
                    add(SymbolGeometryDiagnostic(
                        anchor.key,
                        "Anchor compatibility key does not match anchor key.",
                        "Use the same stable key for anchor and PortCompatibilityContract.",
                        "symbol.anchor.compatibility-key-mismatch",
                    ))
                }
                if (anchor.key in sourceOwnedPortKeys) {
                    add(SymbolGeometryDiagnostic(
                        anchor.key,
                        "Symbol anchor attempts to own EngineeringPort semantics.",
                        "Keep EngineeringPort identity and meaning in Athena source; keep package anchor compatibility only.",
                        "symbol.anchor.engineering-authority",
                    ))
                }
            }
        }
        return if (diagnostics.isEmpty()) {
            SymbolGeometryAdmissionResult(geometry, emptyList())
        } else {
            SymbolGeometryAdmissionResult(null, diagnostics)
        }
    }
}
