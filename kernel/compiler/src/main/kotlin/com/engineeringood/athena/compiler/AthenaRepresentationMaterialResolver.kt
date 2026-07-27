package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoader
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.packageruntime.BindingResolver
import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotDigest
import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotRequest
import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStager
import java.nio.file.Files
import java.nio.file.Path

/** Compiles and resolves Athena-authored representation material without renderer or fixture selection. */
class AthenaRepresentationMaterialResolver(
    private val repositoryLoader: AthenaRepositoryContractLoader = AthenaRepositoryContractLoader(),
    private val stager: RepresentationPackageSnapshotStager = RepresentationPackageSnapshotStager(),
    private val snapshotCompiler: AthenaRepresentationPackageSnapshotCompiler = AthenaRepresentationPackageSnapshotCompiler(),
    bindingResolver: BindingResolver = BindingResolver(),
) {
    private val materialBinder = AthenaRepresentationMaterialBinder(bindingResolver)

    fun resolve(
        repositoryRoot: Path,
        document: EngineeringDocument,
        projectionContext: ProjectionContextId,
    ): AthenaRepresentationMaterialResolutionResult {
        val diagnostics = mutableListOf<AthenaRepresentationMaterialDiagnostic>()
        val repository = repositoryLoader.load(repositoryRoot)
        diagnostics += repository.diagnostics.map { issue ->
            materialDiagnostic(issue.code, repositoryRoot.toString(), issue.message)
        }
        val primaryPackage = repository.repository?.manifest?.primaryPackage
        if (primaryPackage == null || diagnostics.isNotEmpty()) return failure(diagnostics)
        if (repository.representationPackageRoots.isEmpty()) {
            diagnostics += materialDiagnostic(
                "material.package-roots.missing",
                repository.manifestPath.toString(),
                "Repository contract must declare at least one representationPackageRoots entry.",
            )
            return failure(diagnostics)
        }

        val staged = stager.stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repository.repositoryRoot,
                packageRoots = repository.representationPackageRoots.sortedBy(Path::toString),
                dependencyLockDigest = RepresentationPackageSnapshotDigest.sha256(Files.readAllBytes(repository.lockPath)),
                snapshotDirectory = repository.repositoryRoot.resolve(".athena/snapshots/material-resolution"),
            ),
        )
        diagnostics += staged.diagnostics.map { issue -> materialDiagnostic(issue.code, issue.subject, issue.message) }
        val snapshot = staged.snapshot ?: return failure(diagnostics)
        val compiled = snapshotCompiler.compile(snapshot)
        diagnostics += compiled.diagnostics.map { issue -> materialDiagnostic(issue.code, issue.subject, issue.message) }
        if (diagnostics.isNotEmpty()) return failure(diagnostics, compiled.proof.stagedSourcePaths)

        val profileCandidates = compiled.profiles.filter { profile -> projectionContext in profile.projectionContexts }
        if (profileCandidates.size != 1) {
            diagnostics += materialDiagnostic(
                "material.profile.selection.invalid",
                projectionContext.value,
                "Exactly one compiled Presentation Profile must support the requested projection context.",
            )
            return failure(diagnostics, compiled.proof.stagedSourcePaths)
        }

        val packageName = primaryPackage.id.name
        val packageVersion = primaryPackage.id.version ?: "0.0.0"
        val engineeringPackage = AthenaRepresentationMaterialContractFactory.engineeringPackage(
            document,
            packageName,
            packageVersion,
            repositoryRoot,
        )
        val activeProfile = AthenaRepresentationMaterialContractFactory.compatibleProfile(
            profileCandidates.single(),
            packageName,
            packageVersion,
        )
        val representationPackages = AthenaRepresentationMaterialContractFactory.representationPackages(
            compiled,
            compiled.profiles,
            diagnostics,
        )
        if (diagnostics.isNotEmpty()) return failure(diagnostics, compiled.proof.stagedSourcePaths)

        val subjects = AthenaRepresentationMaterialSubjectDeriver.derive(document, diagnostics)
        val definitionsByKey = compiled.definitions.associateBy { definition ->
            definition.libraryId.value to definition.symbolId.value
        }
        val materials = subjects.mapNotNull { subject ->
            materialBinder.resolve(
                subject = subject,
                projectionContext = projectionContext,
                engineeringPackage = engineeringPackage,
                activeProfile = activeProfile,
                representationPackages = representationPackages,
                descriptors = compiled.descriptors,
                bindingRules = compiled.bindingRules,
                definitionsByKey = definitionsByKey,
                diagnostics = diagnostics,
            )
        }.sortedBy { material -> material.semanticSubjectId }

        return AthenaRepresentationMaterialResolutionResult(
            definitions = compiled.definitions,
            materials = materials.takeIf { diagnostics.isEmpty() }.orEmpty(),
            diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
            proof = AthenaRepresentationMaterialProof(
                stagedSourcePaths = compiled.proof.stagedSourcePaths,
            ),
        )
    }

    private fun failure(
        diagnostics: List<AthenaRepresentationMaterialDiagnostic>,
        stagedSourcePaths: List<String> = emptyList(),
    ) = AthenaRepresentationMaterialResolutionResult(
        diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
        proof = AthenaRepresentationMaterialProof(stagedSourcePaths = stagedSourcePaths),
    )
}
