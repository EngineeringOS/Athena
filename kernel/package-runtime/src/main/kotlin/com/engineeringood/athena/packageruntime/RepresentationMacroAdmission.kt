package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import java.security.MessageDigest

data class RepresentationMacroAdmissionResult(
    val macro: RepresentationMacro?,
    val diagnostics: List<MacroDiagnostic>,
) {
    val isValid get() = macro != null && diagnostics.isEmpty()
}

data class MacroDiagnostic(val subject: String, val problem: String, val correction: String, val code: String)

object RepresentationMacroAdmission {
    fun admit(macro: RepresentationMacro, readyItems: Set<String>): RepresentationMacroAdmissionResult {
        val diagnostics = macro.children.filter { it.childId.key !in readyItems }.map { child ->
            MacroDiagnostic(child.childId.key, "Macro child is not PACKAGE_READY.", "Admit `${child.childId.key}` before publishing this Macro.", "macro.child.not-ready")
        }
        return if (diagnostics.isEmpty()) RepresentationMacroAdmissionResult(macro, emptyList())
        else RepresentationMacroAdmissionResult(null, diagnostics.sortedBy { it.subject })
    }
}

data class RepresentationMacroResolutionResult(
    val occurrences: List<ResolvedMacroOccurrence>?,
    val diagnostics: List<MacroDiagnostic>,
) { val isValid get() = occurrences != null && diagnostics.isEmpty() }

object RepresentationMacroResolver {
    fun resolve(macro: RepresentationMacro, request: MacroInsertionRequest): RepresentationMacroResolutionResult {
        if (macro.macroId != request.macroId) return reject(request.macroId.key, "Requested Macro identity does not match the definition.", "Select the admitted Macro identified by `${request.macroId.key}`.", "macro.identity.mismatch")
        val missingSlots = macro.bindings.map { it.functionSlotId }.toSet() - request.functionIdsBySlot.keys
        if (missingSlots.isNotEmpty()) return reject(request.operationId, "Macro insertion is missing explicit Function ids for slots ${missingSlots.sorted().joinToString(", ")}.", "Provide an existing Function id for every declared Macro slot.", "macro.function.missing")
        val occurrences = macro.children.sortedBy { it.childId.key }.map { child ->
            val binding = macro.bindings.firstOrNull { it.childId == child.childId }
            ResolvedMacroOccurrence(
                occurrenceId = stableId(request.operationId, request.sheetId, child.childId.key),
                childId = child.childId,
                functionId = binding?.let { request.functionIdsBySlot[it.functionSlotId] },
                x = request.transform.offsetX + child.x,
                y = request.transform.offsetY + child.y,
                rotationDegrees = (request.transform.rotationDegrees + child.rotationDegrees) % 360,
            )
        }
        return RepresentationMacroResolutionResult(occurrences, emptyList())
    }

    private fun reject(subject: String, problem: String, correction: String, code: String) = RepresentationMacroResolutionResult(null, listOf(MacroDiagnostic(subject, problem, correction, code)))
    private fun stableId(operationId: String, sheetId: String, child: String): String = "occurrence:macro:sha256:" + sha256("$operationId|$sheetId|$child")
    private fun sha256(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
