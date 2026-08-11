package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageplatform.*

data class PackageUsageTraceDiagnostic(
    val subject: String,
    val problem: String,
    val correction: String,
    val code: String,
)

data class PackageUsageTraceCompilationResult(
    val view: PackageUsageTraceView?,
    val diagnostics: List<PackageUsageTraceDiagnostic>,
) {
    val isValid: Boolean get() = view != null && diagnostics.isEmpty()
}

/** Compiler-owned derived join from source bindings to visible scene occurrence ids. */
object PackageUsageTraceCompiler {
    fun compile(
        bindings: List<FunctionRepresentationBinding>,
        readyItems: Map<String, AdmittedPackageItem>,
        occurrenceBindingIds: Map<String, List<String>>,
        visibleOccurrenceIds: Set<String>,
    ): PackageUsageTraceCompilationResult {
        val diagnostics = mutableListOf<PackageUsageTraceDiagnostic>()
        val traces = mutableListOf<FunctionRepresentationUsageTrace>()
        val seenOccurrences = mutableSetOf<String>()
        occurrenceBindingIds.keys.filter { it !in visibleOccurrenceIds }.sorted().forEach { occurrence ->
            diagnostics += diagnostic(occurrence, "Usage trace references an unknown scene occurrence.", "Publish usage only for an occurrence in the current Canonical Scene.", "package.usage.occurrence.unknown")
        }
        bindings.sortedBy { it.bindingId }.forEach { binding ->
            val item = readyItems[binding.element.key]
            if (item == null || item.admissionState != PackageItemAdmissionState.PACKAGE_READY) {
                diagnostics += diagnostic(binding.bindingId, "Binding references a PackageItem that is not PACKAGE_READY.", "Admit `${binding.element.key}` before publishing usage trace.", "package.usage.item.not-ready")
                return@forEach
            }
            val occurrences = occurrenceBindingIds.filterValues { binding.bindingId in it }.keys.filter { it in visibleOccurrenceIds }.sorted()
            if (occurrences.isEmpty()) {
                diagnostics += diagnostic(binding.bindingId, "Binding has no visible scene occurrence.", "Publish an occurrence using binding `${binding.bindingId}` before exporting usage trace.", "package.usage.occurrence.missing")
                return@forEach
            }
            occurrences.forEach { occurrence ->
                if (!seenOccurrences.add(occurrence)) diagnostics += diagnostic(occurrence, "Scene occurrence is linked to more than one binding.", "Link each occurrence to exactly one FunctionRepresentationBinding.", "package.usage.occurrence.duplicate")
            }
            traces += FunctionRepresentationUsageTrace(
                bindingId = binding.bindingId,
                functionId = binding.key.functionId,
                projectionId = binding.key.projectionId,
                bindingRole = binding.key.bindingRole,
                item = binding.element,
                itemDigest = item.canonicalDigest,
                provenance = PackageProvenanceView(item.metadata.identity, item.metadata.provenance),
                occurrenceIds = occurrences,
            )
        }
        occurrenceBindingIds.toSortedMap().forEach { (occurrence, ids) ->
            if (ids.isEmpty()) diagnostics += diagnostic(occurrence, "Scene occurrence has no binding identity.", "Persist one FunctionRepresentationBinding id for the occurrence.", "package.usage.binding.missing")
            if (ids.distinct().size != ids.size) diagnostics += diagnostic(occurrence, "Scene occurrence repeats a binding identity.", "Persist each binding id once per occurrence.", "package.usage.binding.duplicate")
        }
        val orderedDiagnostics = diagnostics.sortedWith(compareBy(PackageUsageTraceDiagnostic::subject, PackageUsageTraceDiagnostic::code))
        return if (orderedDiagnostics.isEmpty()) PackageUsageTraceCompilationResult(PackageUsageTraceView(traces), emptyList())
        else PackageUsageTraceCompilationResult(null, orderedDiagnostics)
    }

    private fun diagnostic(subject: String, problem: String, correction: String, code: String) = PackageUsageTraceDiagnostic(subject, problem, correction, code)
}
