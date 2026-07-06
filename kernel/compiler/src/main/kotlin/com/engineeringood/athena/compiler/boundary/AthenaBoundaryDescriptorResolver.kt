package com.engineeringood.athena.compiler.boundary

import java.nio.file.Path
import java.util.Locale

/** Resolves configured external boundary descriptors into deterministic valid and rejected metadata views. */
class AthenaBoundaryDescriptorResolver(
    private val loader: AthenaBoundaryDescriptorLoader = AthenaBoundaryDescriptorLoader(),
) {
    /** Resolves the descriptors from [source] into deterministic candidate, valid, and rejected views. */
    fun resolve(source: AthenaBoundaryDescriptorSource): AthenaBoundaryValidationReport {
        if (source.descriptorRoots.isEmpty()) {
            return AthenaBoundaryValidationReport.empty(source)
        }

        val normalizedRoots = source.descriptorRoots.map(::normalizeRoot).distinctBy(::stablePathKey).sortedBy(::stablePathKey)
        val candidates = mutableListOf<AthenaBoundaryCandidateDescriptor>()
        val validDescriptors = mutableListOf<AthenaValidBoundaryDescriptor>()
        val rejectedDescriptors = mutableListOf<AthenaRejectedBoundaryDescriptor>()
        val loadedDescriptors = mutableListOf<AthenaBoundaryCandidateDescriptor>()

        normalizedRoots.forEach { descriptorRoot ->
            val loadResult = loader.load(descriptorRoot)
            val loadedDescriptor = loadResult.loadedDescriptor

            if (!loadResult.isValid || loadedDescriptor == null) {
                rejectedDescriptors += AthenaRejectedBoundaryDescriptor(
                    packageRoot = descriptorRoot,
                    descriptorId = loadedDescriptor?.manifest?.descriptorId,
                    diagnostics = loadResult.diagnostics,
                )
                return@forEach
            }

            val candidate = AthenaBoundaryCandidateDescriptor(
                packageRoot = descriptorRoot,
                descriptor = loadedDescriptor,
            )
            candidates += candidate
            loadedDescriptors += candidate
        }

        val duplicateDescriptorIds = loadedDescriptors.groupBy { candidate -> candidate.descriptor.manifest.descriptorId }
            .filterValues { descriptors -> descriptors.size > 1 }
            .keys

        loadedDescriptors.forEach { candidate ->
            val manifest = candidate.descriptor.manifest
            val validationDiagnostics = buildList {
                if (manifest.descriptorId in duplicateDescriptorIds) {
                    add(
                        diagnostic(
                            ruleId = "boundary.descriptor.id.duplicate",
                            subject = "descriptor.id",
                            message = "External boundary descriptor id `${manifest.descriptorId}` must be unique across descriptor roots.",
                        ),
                    )
                }

                addAll(validateM0Posture(manifest))
            }

            if (validationDiagnostics.isEmpty()) {
                validDescriptors += AthenaValidBoundaryDescriptor(
                    packageRoot = candidate.packageRoot,
                    descriptorId = manifest.descriptorId,
                    category = manifest.category,
                    direction = manifest.direction,
                    upstreamAuthority = manifest.upstreamAuthority,
                    exchangeForms = manifest.exchangeForms,
                    compatibilityAssumptions = manifest.compatibilityAssumptions,
                )
            } else {
                rejectedDescriptors += AthenaRejectedBoundaryDescriptor(
                    packageRoot = candidate.packageRoot,
                    descriptorId = manifest.descriptorId,
                    diagnostics = validationDiagnostics,
                )
            }
        }

        return AthenaBoundaryValidationReport(
            source = AthenaBoundaryDescriptorSource(normalizedRoots),
            candidates = candidates.sortedWith(compareBy(
                { it.descriptor.manifest.descriptorId },
                { stablePathKey(it.packageRoot) },
            )),
            validDescriptors = validDescriptors.sortedWith(compareBy(
                AthenaValidBoundaryDescriptor::descriptorId,
                { stablePathKey(it.packageRoot) },
            )),
            rejectedDescriptors = rejectedDescriptors.sortedWith(compareBy(
                { it.descriptorId == null },
                { it.descriptorId ?: "" },
                { stablePathKey(it.packageRoot) },
            )),
        )
    }

    private fun validateM0Posture(manifest: AthenaBoundaryDescriptorManifest): List<AthenaBoundaryDiagnostic> {
        val diagnostics = mutableListOf<AthenaBoundaryDiagnostic>()

        if (manifest.upstreamAuthority != AthenaBoundarySemanticAuthority.ENGINEERING_IR) {
            diagnostics += diagnostic(
                ruleId = "boundary.descriptor.authority.external-canonical-forbidden",
                subject = "authority.upstream",
                message = "External boundary descriptors must keep `ENGINEERING_IR` as the upstream semantic authority in M0.",
            )
        }

        if (manifest.m0Mode != AthenaBoundaryM0Mode.PASSIVE_METADATA) {
            diagnostics += diagnostic(
                ruleId = "boundary.descriptor.mode.operational-not-supported",
                subject = "m0.mode",
                message = "External boundary descriptors may only declare passive metadata in M0; operational importer, exporter, or connector behavior is not supported.",
            )
        }

        if (manifest.category == AthenaBoundaryCategory.STANDARDS && manifest.direction !in setOf(
                AthenaBoundaryDirection.REFERENCE,
                AthenaBoundaryDirection.COMPATIBILITY,
            )
        ) {
            diagnostics += diagnostic(
                ruleId = "boundary.descriptor.standards.direction.unsupported",
                subject = "descriptor.direction",
                message = "Standards boundary descriptors must remain `REFERENCE` or `COMPATIBILITY` boundaries in M0.",
            )
        }

        return diagnostics
    }

    private fun diagnostic(
        ruleId: String,
        subject: String,
        message: String,
    ): AthenaBoundaryDiagnostic {
        return AthenaBoundaryDiagnostic(
            severity = AthenaBoundarySeverity.ERROR,
            ruleId = AthenaBoundaryRuleId(ruleId),
            subject = subject,
            message = message,
        )
    }
}

private fun normalizeRoot(path: Path): Path = runCatching { path.toRealPath() }.getOrElse { path.toAbsolutePath().normalize() }

private fun stablePathKey(path: Path): String {
    val normalizedPath = runCatching { path.toRealPath() }.getOrElse { path.toAbsolutePath().normalize() }
    val pathKey = normalizedPath.toString().replace('\\', '/')
    return if (System.getProperty("os.name").startsWith("Windows")) {
        pathKey.lowercase(Locale.ROOT)
    } else {
        pathKey
    }
}
