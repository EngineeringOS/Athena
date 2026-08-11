package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.MacroBindingReference
import com.engineeringood.athena.packageplatform.MacroChildPlacement
import com.engineeringood.athena.packageplatform.PackageItemIdentity
import com.engineeringood.athena.packageplatform.RepresentationMacro
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryLockedItem

object LockedRepresentationPackageRuntime {
    private val childAttribute = Regex("child\\.(\\d{3})\\.(element|x|y|rotation|functionSlot|bindingRole)")
    private val requiredChildFields = setOf("element", "x", "y", "rotation", "functionSlot", "bindingRole")

    fun decodeMacro(
        packageId: PackageIdentifier,
        item: RepositoryLockedItem,
        readyItems: Set<String>,
    ): RepresentationMacroAdmissionResult {
        if (item.kind != "MACRO") {
            return invalid(item.itemId, "Locked item is not a Macro.", "Select a locked MACRO item.", "macro.lock.kind.illegal")
        }

        val childFields = linkedMapOf<String, MutableMap<String, String>>()
        item.attributes.forEach { (key, value) ->
            val match = childAttribute.matchEntire(key) ?: return@forEach
            childFields.getOrPut(match.groupValues[1]) { linkedMapOf() }[match.groupValues[2]] = value
        }
        if (childFields.isEmpty()) {
            return invalid(item.itemId, "Locked Macro has no child payload.", "Materialize the Macro from an admitted native definition.", "macro.lock.children.missing")
        }

        val diagnostics = mutableListOf<MacroDiagnostic>()
        val children = mutableListOf<MacroChildPlacement>()
        val bindings = mutableListOf<MacroBindingReference>()
        childFields.toSortedMap().forEach { (index, fields) ->
            val missing = requiredChildFields - fields.keys
            if (missing.isNotEmpty()) {
                diagnostics += MacroDiagnostic(
                    subject = "${item.itemId}.child.$index",
                    problem = "Locked Macro child is missing ${missing.sorted().joinToString(", ")}.",
                    correction = "Materialize every canonical Macro child field.",
                    code = "macro.lock.child.incomplete",
                )
                return@forEach
            }
            val x = fields["x"]?.toIntOrNull()
            val y = fields["y"]?.toIntOrNull()
            val rotation = fields["rotation"]?.toIntOrNull()
            if (x == null || y == null || rotation == null || rotation !in 0..359) {
                diagnostics += MacroDiagnostic(
                    subject = "${item.itemId}.child.$index",
                    problem = "Locked Macro child geometry is invalid.",
                    correction = "Use integer x/y and rotation from 0 through 359.",
                    code = "macro.lock.child.geometry.invalid",
                )
                return@forEach
            }
            val childId = PackageItemIdentity(packageId, fields.getValue("element"), item.itemVersion)
            children += MacroChildPlacement(childId, x, y, rotation)
            bindings += MacroBindingReference(
                childId = childId,
                functionSlotId = fields.getValue("functionSlot"),
                bindingRole = fields.getValue("bindingRole"),
            )
        }
        if (diagnostics.isNotEmpty()) return RepresentationMacroAdmissionResult(null, diagnostics.sortedBy { it.subject })

        val macro = runCatching {
            RepresentationMacro(
                macroId = PackageItemIdentity(packageId, item.itemId, item.itemVersion),
                children = children,
                bindings = bindings,
            )
        }.getOrElse {
            return invalid(item.itemId, "Locked Macro payload violates the Macro contract.", "Materialize a valid admitted Macro.", "macro.lock.payload.invalid")
        }
        return RepresentationMacroAdmission.admit(macro, readyItems)
    }

    fun variantMatches(
        elementPackageId: PackageIdentifier,
        element: RepositoryLockedItem,
        variantPackageId: PackageIdentifier,
        variant: RepositoryLockedItem,
    ): Boolean {
        if (element.kind != "ELEMENT" || variant.kind != "VARIANT") return false
        if (elementPackageId != variantPackageId) return false
        if (variant.attributes["element"] != element.itemId) return false
        val elementFingerprint = element.attributes["interfaceFingerprint"] ?: return false
        return variant.attributes["interfaceFingerprint"] == elementFingerprint
    }

    private fun invalid(subject: String, problem: String, correction: String, code: String) =
        RepresentationMacroAdmissionResult(null, listOf(MacroDiagnostic(subject, problem, correction, code)))
}
