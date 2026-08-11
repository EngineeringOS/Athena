package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.StableSemanticIdentity
import java.util.Collections

/** Explicit engineering value selected for visible Sheet annotation. */
enum class ConnectionAnnotationDisplayRole {
    KIND,
    POTENTIAL_OR_SIGNAL,
    SPECIFICATION,
}

data class ConnectionAnnotationId(
    val sheetId: String,
    val semanticId: String,
    val displayRole: ConnectionAnnotationDisplayRole,
) {
    init {
        require(sheetId.isNotBlank()) { "Connection annotation Sheet identity must not be blank." }
        require(semanticId.isNotBlank()) { "Connection annotation semantic identity must not be blank." }
    }

    val value: String
        get() = "annotation:sheet=$sheetId:semantic=$semanticId:role=${displayRole.name.lowercase()}"
}

data class ConnectionAnnotationSelection(
    val sheetId: String,
    val semanticId: StableSemanticIdentity,
    val displayRole: ConnectionAnnotationDisplayRole,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(sheetId.isNotBlank()) { "Connection annotation selection Sheet identity must not be blank." }
        require(semanticId.value.isNotBlank()) { "Connection annotation selection semantic identity must not be blank." }
    }
}

data class ConnectionAnnotation(
    val id: ConnectionAnnotationId,
    val sheetId: String,
    val semanticId: StableSemanticIdentity,
    val displayRole: ConnectionAnnotationDisplayRole,
    val value: String,
    val anchor: SpatialPoint,
    val bounds: SpatialRect,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(sheetId.isNotBlank()) { "Connection annotation Sheet identity must not be blank." }
        require(id.sheetId == sheetId) { "Connection annotation identity must name its owning Sheet." }
        require(id.semanticId == semanticId.value) { "Connection annotation identity must name its semantic subject." }
        require(id.displayRole == displayRole) { "Connection annotation identity must name its display role." }
        require(value.isNotBlank()) { "Connection annotation value must not be blank." }
        require(anchor.x in bounds.x until bounds.right && anchor.y in bounds.y until bounds.bottom) {
            "Connection annotation anchor must be inside its label bounds."
        }
    }
}

class ConnectionAnnotationPlan(annotations: List<ConnectionAnnotation> = emptyList()) {
    val annotations: List<ConnectionAnnotation> = Collections.unmodifiableList(annotations.sortedBy { it.id.value })

    init {
        require(this.annotations.map { it.id }.distinct().size == this.annotations.size) {
            "Connection annotation identities must be unique."
        }
        require(this.annotations.all { it.sheetId == this.annotations.firstOrNull()?.sheetId ?: it.sheetId }) {
            "Connection annotation plan cannot mix Sheet identities."
        }
    }

    override fun equals(other: Any?): Boolean = this === other || other is ConnectionAnnotationPlan && annotations == other.annotations
    override fun hashCode(): Int = annotations.hashCode()
    override fun toString(): String = "ConnectionAnnotationPlan(annotations=$annotations)"
}

sealed interface ConnectionAnnotationPlanning {
    data class Success(val plan: ConnectionAnnotationPlan) : ConnectionAnnotationPlanning
    data class Failure(val diagnostics: List<SpatialDiagnostic>) : ConnectionAnnotationPlanning
}
