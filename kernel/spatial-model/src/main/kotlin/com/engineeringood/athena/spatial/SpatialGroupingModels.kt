package com.engineeringood.athena.spatial

import java.util.Collections

data class SpatialRegionId(
    val sheetId: String,
    val projectionId: String,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial Region Sheet identity must not be blank." }
        require(projectionId.isNotBlank()) { "Spatial Region Projection identity must not be blank." }
    }
}

data class SpatialConstructId(
    val sheetId: String,
    val projectionId: String,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial Construct Sheet identity must not be blank." }
        require(projectionId.isNotBlank()) { "Spatial Construct Projection identity must not be blank." }
    }
}

sealed interface SpatialAlignmentSource {
    val sheetId: String

    data class Region(val regionId: SpatialRegionId) : SpatialAlignmentSource {
        override val sheetId: String = regionId.sheetId
    }

    data class Construct(val constructId: SpatialConstructId) : SpatialAlignmentSource {
        override val sheetId: String = constructId.sheetId
    }
}

data class SpatialAlignmentId(
    val sheetId: String,
    val source: SpatialAlignmentSource,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial alignment Sheet identity must not be blank." }
        require(source.sheetId == sheetId) { "Spatial alignment identity source must belong to its Sheet." }
    }
}

class SpatialRegionGeometry(
    val regionId: SpatialRegionId,
    val sheetId: String,
    memberOccurrenceIds: List<SpatialOccurrenceId>,
    val bounds: SpatialRect,
    val sourceTrace: SpatialSourceTrace,
) {
    val memberOccurrenceIds: List<SpatialOccurrenceId> = memberOccurrenceIds.immutableGroupingCopy()

    init {
        require(regionId.sheetId == sheetId) { "Spatial Region identity must name its owning Sheet." }
        require(memberOccurrenceIds.isNotEmpty()) { "Spatial Region must contain at least one Occurrence." }
        require(memberOccurrenceIds.all { member -> member.sheetId == sheetId }) {
            "Spatial Region members must belong to its owning Sheet."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialRegionGeometry &&
            regionId == other.regionId &&
            sheetId == other.sheetId &&
            memberOccurrenceIds == other.memberOccurrenceIds &&
            bounds == other.bounds &&
            sourceTrace == other.sourceTrace

    override fun hashCode(): Int = listOf(regionId, sheetId, memberOccurrenceIds, bounds, sourceTrace).hashCode()

    override fun toString(): String =
        "SpatialRegionGeometry(regionId=$regionId, sheetId=$sheetId, " +
            "memberOccurrenceIds=$memberOccurrenceIds, bounds=$bounds, sourceTrace=$sourceTrace)"
}

class SpatialConstructGeometry(
    val constructId: SpatialConstructId,
    val sheetId: String,
    val kind: String,
    val name: String?,
    memberOccurrenceIds: List<SpatialOccurrenceId>,
    val envelope: SpatialRect,
    val sourceTrace: SpatialSourceTrace,
) {
    val memberOccurrenceIds: List<SpatialOccurrenceId> = memberOccurrenceIds.immutableGroupingCopy()

    init {
        require(constructId.sheetId == sheetId) { "Spatial Construct identity must name its owning Sheet." }
        require(kind.isNotBlank()) { "Spatial Construct kind must not be blank." }
        require(memberOccurrenceIds.isNotEmpty()) { "Spatial Construct must contain at least one Occurrence." }
        require(memberOccurrenceIds.all { member -> member.sheetId == sheetId }) {
            "Spatial Construct members must belong to its owning Sheet."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialConstructGeometry &&
            constructId == other.constructId &&
            sheetId == other.sheetId &&
            kind == other.kind &&
            name == other.name &&
            memberOccurrenceIds == other.memberOccurrenceIds &&
            envelope == other.envelope &&
            sourceTrace == other.sourceTrace

    override fun hashCode(): Int =
        listOf(constructId, sheetId, kind, name, memberOccurrenceIds, envelope, sourceTrace).hashCode()

    override fun toString(): String =
        "SpatialConstructGeometry(constructId=$constructId, sheetId=$sheetId, kind=$kind, name=$name, " +
            "memberOccurrenceIds=$memberOccurrenceIds, envelope=$envelope, sourceTrace=$sourceTrace)"
}

class SpatialAlignment(
    val alignmentId: SpatialAlignmentId,
    val sheetId: String,
    val constraintSource: SpatialAlignmentSource,
    occurrenceIds: List<SpatialOccurrenceId>,
    val sourceTrace: SpatialSourceTrace,
) {
    val occurrenceIds: List<SpatialOccurrenceId> = occurrenceIds.immutableGroupingCopy()

    init {
        require(alignmentId.sheetId == sheetId) { "Spatial alignment identity must name its owning Sheet." }
        require(constraintSource.sheetId == sheetId) { "Spatial alignment source must belong to its owning Sheet." }
        require(alignmentId.source == constraintSource) { "Spatial alignment identity must retain its constraint source." }
        require(occurrenceIds.isNotEmpty()) { "Spatial alignment must contain at least one Occurrence." }
        require(occurrenceIds.all { occurrence -> occurrence.sheetId == sheetId }) {
            "Spatial alignment Occurrences must belong to its owning Sheet."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialAlignment &&
            alignmentId == other.alignmentId &&
            sheetId == other.sheetId &&
            constraintSource == other.constraintSource &&
            occurrenceIds == other.occurrenceIds &&
            sourceTrace == other.sourceTrace

    override fun hashCode(): Int =
        listOf(alignmentId, sheetId, constraintSource, occurrenceIds, sourceTrace).hashCode()

    override fun toString(): String =
        "SpatialAlignment(alignmentId=$alignmentId, sheetId=$sheetId, constraintSource=$constraintSource, " +
            "occurrenceIds=$occurrenceIds, sourceTrace=$sourceTrace)"
}

private fun <T> List<T>.immutableGroupingCopy(): List<T> = Collections.unmodifiableList(toList())
