package com.engineeringood.athena.layout

@JvmInline
value class LayoutGraphSnapshotId(val value: String) {
    init {
        require(value.isNotBlank()) { "Layout graph snapshot id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class LayoutGraphSourceUnitId(val value: String) {
    init {
        require(value.isNotBlank()) { "Layout graph source unit id must not be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class LayoutGraphObjectId(val value: String) {
    init {
        require(value.isNotBlank()) { "Layout graph object id must not be blank." }
    }

    override fun toString(): String = value
}

data class LayoutGraphSourceSpan(
    val sourceUnitId: LayoutGraphSourceUnitId,
    val declarationId: String,
    val line: Int,
    val column: Int,
) {
    init {
        require(declarationId.isNotBlank()) { "Layout graph declaration id must not be blank." }
        require(line > 0) { "Layout graph source span line must be positive." }
        require(column > 0) { "Layout graph source span column must be positive." }
    }
}

data class LayoutGraphProvenance(
    val sourceUnitId: LayoutGraphSourceUnitId,
    val declarationId: String,
    val span: LayoutGraphSourceSpan? = null,
) {
    init {
        require(declarationId.isNotBlank()) { "Layout graph provenance declaration id must not be blank." }
    }
}

data class LayoutGraphPoint(
    val x: Int,
    val y: Int,
)

data class LayoutGraphBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0) { "Layout graph bounds width must be positive." }
        require(height > 0) { "Layout graph bounds height must be positive." }
    }

    fun contains(other: LayoutGraphBounds): Boolean =
        other.x >= x &&
            other.y >= y &&
            other.x + other.width <= x + width &&
            other.y + other.height <= y + height

    fun intersects(other: LayoutGraphBounds): Boolean =
        x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y
}

enum class LayoutConstraintOwner {
    SEMANTIC,
    REPRESENTATION,
    PHYSICAL,
    LAYOUT_PREFERENCE,
}

enum class LayoutConstraintStrength {
    REQUIRED,
    PREFERRED,
    OPTIONAL,
}

enum class LayoutGraphConstraintKind {
    PORT_ANCHOR_BINDING,
    REPRESENTATION_BOUNDS,
    PHYSICAL_MOUNT,
    PHYSICAL_CLEARANCE,
    PHYSICAL_ORIENTATION,
    ROUTE_CHANNEL,
    OCCURRENCE_CONTAINMENT,
}

data class LayoutGraphConstraint(
    val constraintId: LayoutGraphObjectId,
    val owner: LayoutConstraintOwner,
    val strength: LayoutConstraintStrength,
    val kind: LayoutGraphConstraintKind,
    val subjectId: String,
    val targetId: String? = null,
    val note: String? = null,
    val provenance: LayoutGraphProvenance,
) {
    init {
        require(subjectId.isNotBlank()) { "Layout graph constraint subject id must not be blank." }
        require(note == null || note.isNotBlank()) { "Layout graph constraint note must be null or non-blank." }
    }
}

data class LayoutGraphAnchor(
    val anchorId: String,
    val geometryRef: String,
    val primitiveId: String,
    val point: LayoutGraphPoint,
    val role: String,
    val required: Boolean,
    val acceptedDirections: List<String>,
    val acceptedSignals: List<String>,
    val provenance: LayoutGraphProvenance,
) {
    init {
        require(anchorId.isNotBlank()) { "Layout graph anchor id must not be blank." }
        require(geometryRef.isNotBlank()) { "Layout graph geometry ref must not be blank." }
        require(primitiveId.isNotBlank()) { "Layout graph primitive id must not be blank." }
        require(role.isNotBlank()) { "Layout graph anchor role must not be blank." }
    }
}

data class LayoutGraphPort(
    val portSemanticId: String,
    val terminalIdentity: String,
    val anchorId: String,
    val direction: String? = null,
    val signal: String? = null,
    val required: Boolean = true,
    val provenance: LayoutGraphProvenance,
) {
    init {
        require(portSemanticId.isNotBlank()) { "Layout graph port semantic id must not be blank." }
        require(terminalIdentity.isNotBlank()) { "Layout graph terminal identity must not be blank." }
        require(anchorId.isNotBlank()) { "Layout graph port anchor id must not be blank." }
    }
}

enum class LayoutGraphObstacleKind {
    ENCLOSURE,
    SURFACE,
    RAIL,
    DUCT,
    CHANNEL,
    TERMINAL_GROUP,
}

data class LayoutGraphObstacle(
    val obstacleId: LayoutGraphObjectId,
    val kind: LayoutGraphObstacleKind,
    val bounds: LayoutGraphBounds,
    val provenance: LayoutGraphProvenance,
)

enum class LayoutGraphRelationshipKind {
    CONTAINMENT,
    MOUNT,
    ROUTE_CHANNEL,
}

data class LayoutGraphRelationship(
    val relationshipId: LayoutGraphObjectId,
    val kind: LayoutGraphRelationshipKind,
    val sourceId: String,
    val targetId: String,
    val provenance: LayoutGraphProvenance,
) {
    init {
        require(sourceId.isNotBlank()) { "Layout graph relationship source id must not be blank." }
        require(targetId.isNotBlank()) { "Layout graph relationship target id must not be blank." }
    }
}

data class LayoutGraphOccurrence(
    val occurrenceId: LayoutGraphObjectId,
    val semanticSubjectId: String,
    val physicalOccurrenceId: String,
    val bounds: LayoutGraphBounds,
    val representationBounds: LayoutGraphBounds?,
    val ports: List<LayoutGraphPort>,
    val anchors: List<LayoutGraphAnchor>,
    val constraints: List<LayoutGraphConstraint>,
    val provenance: LayoutGraphProvenance,
) {
    init {
        require(semanticSubjectId.isNotBlank()) { "Layout graph semantic subject id must not be blank." }
        require(physicalOccurrenceId.isNotBlank()) { "Layout graph physical occurrence id must not be blank." }
    }
}

data class LayoutGraph(
    val snapshotId: LayoutGraphSnapshotId,
    val sourceUnitId: LayoutGraphSourceUnitId,
    val installationId: String,
    val occurrences: List<LayoutGraphOccurrence>,
    val obstacles: List<LayoutGraphObstacle>,
    val relationships: List<LayoutGraphRelationship>,
    val constraints: List<LayoutGraphConstraint>,
    val compilerSnapshotId: String = "",
) {
    init {
        require(installationId.isNotBlank()) { "Layout graph installation id must not be blank." }
    }

    companion object {
        fun canonical(
            snapshotId: LayoutGraphSnapshotId,
            sourceUnitId: LayoutGraphSourceUnitId,
            installationId: String,
            occurrences: List<LayoutGraphOccurrence>,
            obstacles: List<LayoutGraphObstacle>,
            relationships: List<LayoutGraphRelationship>,
            constraints: List<LayoutGraphConstraint>,
            compilerSnapshotId: String = "",
        ): LayoutGraph = LayoutGraph(
            snapshotId = snapshotId,
            sourceUnitId = sourceUnitId,
            installationId = installationId,
            occurrences = occurrences.sortedBy { occurrence -> occurrence.occurrenceId.value },
            obstacles = obstacles.sortedBy { obstacle -> obstacle.obstacleId.value },
            relationships = relationships.sortedBy { relationship -> relationship.relationshipId.value },
            constraints = constraints.sortedBy { constraint -> constraint.constraintId.value },
            compilerSnapshotId = compilerSnapshotId,
        )
    }
}
