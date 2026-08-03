package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringProjectionPolicy
import com.engineeringood.athena.ir.SourceProvenance

data class AthenaProjectionPolicySelection(
    val name: String,
    val targetSurface: String,
    val layoutStrategy: String,
    val drawingProfile: String,
    val routeQualityPolicy: String,
    val proofObligations: List<String>,
    val provenance: SourceProvenance,
) {
    val materialProjectionContext: String
        get() = when (targetSurface) {
            "professional-connection-drawing" -> "schematic"
            "connection-drawing" -> "schematic"
            "cabinet" -> "cabinet"
            else -> targetSurface
        }
}

data class AthenaProjectionPolicyDiagnostic(
    val code: String,
    val message: String,
    val provenance: SourceProvenance,
)

sealed interface AthenaProjectionPolicyCompilation {
    data class Success(
        val policies: List<AthenaProjectionPolicySelection>,
    ) : AthenaProjectionPolicyCompilation

    data class Failure(
        val diagnostics: List<AthenaProjectionPolicyDiagnostic>,
    ) : AthenaProjectionPolicyCompilation
}

class AthenaProjectionPolicyCompiler {
    private val supportedTargets = setOf("connection-drawing", "professional-connection-drawing", "cabinet")
    private val supportedLayouts = setOf("orthogonal-grid", "cabinet-layout")

    fun compile(document: EngineeringDocument): AthenaProjectionPolicyCompilation {
        val diagnostics = mutableListOf<AthenaProjectionPolicyDiagnostic>()
        document.projectionPolicies
            .groupBy { policy -> policy.name }
            .filterValues { policies -> policies.size > 1 }
            .values
            .forEach { duplicatePolicies ->
                duplicatePolicies.forEach { policy ->
                    diagnostics += diagnostic(
                        "projection.policy.duplicate",
                        "Projection Policy '${policy.name}' is declared more than once.",
                        policy.provenance,
                    )
                }
            }

        val selections = document.projectionPolicies.mapNotNull { policy ->
            policy.forbiddenEngineeringTruth.forEach { forbidden ->
                diagnostics += diagnostic(
                    "projection.policy.engineering-truth.forbidden",
                    "Projection Policy '${policy.name}' cannot declare '${forbidden.kind}' engineering truth.",
                    forbidden.provenance,
                )
            }
            val target = policy.targetSurface
            if (target == null) {
                diagnostics += diagnostic(
                    "projection.policy.target.missing",
                    "Projection Policy '${policy.name}' requires target.",
                    policy.provenance,
                )
            } else if (target !in supportedTargets) {
                diagnostics += diagnostic(
                    "projection.policy.target.unknown",
                    "Projection Policy '${policy.name}' targets unsupported surface '$target'.",
                    policy.provenance,
                )
            }

            val layout = policy.layoutStrategy
            if (layout == null) {
                diagnostics += diagnostic(
                    "projection.policy.layout.missing",
                    "Projection Policy '${policy.name}' requires layout.",
                    policy.provenance,
                )
            } else if (layout !in supportedLayouts) {
                diagnostics += diagnostic(
                    "projection.policy.layout.unknown",
                    "Projection Policy '${policy.name}' uses unsupported layout '$layout'.",
                    policy.provenance,
                )
            }

            val drawingProfile = policy.drawingProfile
            if (drawingProfile == null) {
                diagnostics += diagnostic(
                    "projection.policy.profile.missing",
                    "Projection Policy '${policy.name}' requires drawingProfile.",
                    policy.provenance,
                )
            }

            val routeQualityPolicy = policy.routeQualityPolicy
            if (routeQualityPolicy == null) {
                diagnostics += diagnostic(
                    "projection.policy.route-quality.missing",
                    "Projection Policy '${policy.name}' requires routeQuality.",
                    policy.provenance,
                )
            }

            if (target == "cabinet" && layout == "orthogonal-grid") {
                diagnostics += diagnostic(
                    "projection.policy.incompatible",
                    "Cabinet Projection Policy '${policy.name}' cannot use orthogonal-grid layout.",
                    policy.provenance,
                )
            }

            if (target == null || layout == null || drawingProfile == null || routeQualityPolicy == null) {
                null
            } else {
                AthenaProjectionPolicySelection(
                    name = policy.name,
                    targetSurface = target,
                    layoutStrategy = layout,
                    drawingProfile = drawingProfile,
                    routeQualityPolicy = routeQualityPolicy,
                    proofObligations = policy.proofObligations,
                    provenance = policy.provenance,
                )
            }
        }

        return if (diagnostics.isEmpty()) {
            AthenaProjectionPolicyCompilation.Success(selections)
        } else {
            AthenaProjectionPolicyCompilation.Failure(diagnostics.sortedBy { diagnostic -> diagnostic.provenance.startLine })
        }
    }

    private fun diagnostic(
        code: String,
        message: String,
        provenance: SourceProvenance,
    ): AthenaProjectionPolicyDiagnostic = AthenaProjectionPolicyDiagnostic(code, message, provenance)
}
