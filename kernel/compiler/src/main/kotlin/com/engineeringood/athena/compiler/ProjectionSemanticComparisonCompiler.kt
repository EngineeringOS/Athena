package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.EngineeringConnectivityCompilation
import com.engineeringood.athena.connection.EngineeringConnectivityContractCompiler
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.SourceProvenance
import java.security.MessageDigest

data class ProjectionSemanticComparison(
    val sharedSemantic: ProjectionSharedSemanticSnapshot,
    val policySnapshots: List<ProjectionPolicyDerivedSnapshot>,
)

data class ProjectionSharedSemanticSnapshot(
    val contractIds: List<String>,
    val interfaceIds: List<String>,
    val portIds: List<String>,
    val connectionIds: List<String>,
    val externalEvidenceIds: List<String>,
    val provenanceIds: List<String>,
    val digest: String,
)

data class ProjectionPolicyDerivedSnapshot(
    val policyName: String,
    val targetSurface: String,
    val layoutStrategy: String,
    val drawingProfile: String,
    val routeQualityPolicy: String,
    val proofObligations: List<String>,
    val materialProjectionContext: String,
    val semanticDigest: String,
    val derivedFactKinds: List<String>,
    val authorityPayloadsAbsent: Boolean,
)

data class ProjectionSemanticComparisonDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val provenance: SourceProvenance,
)

sealed interface ProjectionSemanticComparisonCompilation {
    data class Success(val comparison: ProjectionSemanticComparison) : ProjectionSemanticComparisonCompilation
    data class Failure(val diagnostics: List<ProjectionSemanticComparisonDiagnostic>) : ProjectionSemanticComparisonCompilation
}

class ProjectionSemanticComparisonCompiler(
    private val connectivityCompiler: EngineeringConnectivityContractCompiler = EngineeringConnectivityContractCompiler(),
    private val policyCompiler: AthenaProjectionPolicyCompiler = AthenaProjectionPolicyCompiler(),
) {
    fun compile(document: EngineeringDocument): ProjectionSemanticComparisonCompilation {
        val connectivity = when (val result = connectivityCompiler.compile(document)) {
            is EngineeringConnectivityCompilation.Success -> result
            is EngineeringConnectivityCompilation.Failure -> {
                return ProjectionSemanticComparisonCompilation.Failure(
                    result.diagnostics.map { diagnostic ->
                        ProjectionSemanticComparisonDiagnostic(
                            code = "projection.semantic.connectivity.${diagnostic.code}",
                            subject = "connectivity",
                            message = diagnostic.message,
                            provenance = diagnostic.provenance,
                        )
                    },
                )
            }
        }
        val policies = when (val result = policyCompiler.compile(document)) {
            is AthenaProjectionPolicyCompilation.Success -> result.policies
            is AthenaProjectionPolicyCompilation.Failure -> {
                return ProjectionSemanticComparisonCompilation.Failure(
                    result.diagnostics.map { diagnostic ->
                        ProjectionSemanticComparisonDiagnostic(
                            code = diagnostic.code,
                            subject = "projection",
                            message = diagnostic.message,
                            provenance = diagnostic.provenance,
                        )
                    },
                )
            }
        }
        val sharedSemantic = sharedSemantic(document, connectivity)
        return ProjectionSemanticComparisonCompilation.Success(
            ProjectionSemanticComparison(
                sharedSemantic = sharedSemantic,
                policySnapshots = policies
                    .sortedBy { policy -> policy.name }
                    .map { policy -> policy.toDerivedSnapshot(sharedSemantic.digest) },
            ),
        )
    }

    private fun sharedSemantic(
        document: EngineeringDocument,
        connectivity: EngineeringConnectivityCompilation.Success,
    ): ProjectionSharedSemanticSnapshot {
        val contractIds = connectivity.contracts.map { contract -> contract.id.value }.sorted()
        val interfaceIds = connectivity.contracts
            .flatMap { contract -> contract.interfaces.map { interfaceContract -> "${contract.name}.${interfaceContract.id.value}" } }
            .sorted()
        val portIds = connectivity.contracts
            .flatMap { contract -> contract.ports.map { port -> port.id.value } }
            .sorted()
        val connectionIds = connectivity.connections.map { connection -> connection.id.value }.sorted()
        val externalEvidenceIds = document.externalEvidence.map { evidence -> evidence.name }.sorted()
        val provenanceIds = buildList {
            add(document.system.provenance.identity())
            connectivity.contracts.forEach { contract -> add(contract.provenance.identity()) }
            connectivity.contracts.flatMap { contract -> contract.interfaces }.forEach { interfaceContract ->
                add(interfaceContract.provenance.identity())
            }
            connectivity.contracts.flatMap { contract -> contract.ports }.forEach { port ->
                add(port.provenance.identity())
            }
            connectivity.connections.forEach { connection ->
                add(connection.provenance.identity())
            }
            document.externalEvidence.forEach { evidence -> add(evidence.provenance.identity()) }
        }.distinct().sorted()
        val digest = digest(
            contractIds,
            interfaceIds,
            portIds,
            connectionIds,
            externalEvidenceIds,
            provenanceIds,
        )
        return ProjectionSharedSemanticSnapshot(
            contractIds = contractIds,
            interfaceIds = interfaceIds,
            portIds = portIds,
            connectionIds = connectionIds,
            externalEvidenceIds = externalEvidenceIds,
            provenanceIds = provenanceIds,
            digest = digest,
        )
    }

    private fun AthenaProjectionPolicySelection.toDerivedSnapshot(
        sharedDigest: String,
    ): ProjectionPolicyDerivedSnapshot = ProjectionPolicyDerivedSnapshot(
        policyName = name,
        targetSurface = targetSurface,
        layoutStrategy = layoutStrategy,
        drawingProfile = drawingProfile,
        routeQualityPolicy = routeQualityPolicy,
        proofObligations = proofObligations.sorted(),
        materialProjectionContext = materialProjectionContext,
        semanticDigest = sharedDigest,
        derivedFactKinds = listOf("placement", "routing", "presentation", "policy-output"),
        authorityPayloadsAbsent = authorityPayloadsAbsent(),
    )

    private fun AthenaProjectionPolicySelection.authorityPayloadsAbsent(): Boolean {
        val fields = listOf(targetSurface, layoutStrategy, drawingProfile, routeQualityPolicy) + proofObligations
        return fields.none { value ->
            value.contains("<svg", ignoreCase = true) ||
                value.contains("<xml", ignoreCase = true) ||
                value.contains("planner:", ignoreCase = true) ||
                value.contains("renderer:", ignoreCase = true)
        }
    }
}

private fun SourceProvenance.identity(): String =
    "$file:$startLine:$startColumn-$endLine:$endColumn"

private fun digest(vararg groups: List<String>): String {
    val input = groups.joinToString(separator = "\u001f") { group -> group.joinToString(separator = "\u001e") }
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
