package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance

@JvmInline
value class DrawingProfileId(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing profile id must not be blank." }
    }
}

@JvmInline
value class ConnectionLineClassId(val value: String) {
    init {
        require(value.isNotBlank()) { "Connection presentation class id must not be blank." }
    }
}

@JvmInline
value class LineStyleId(val value: String) {
    init {
        require(value.isNotBlank()) { "Stroke class id must not be blank." }
    }
}

data class DrawingFramePolicy(
    val enabled: Boolean,
)

data class DrawingCoordinateGridPolicy(
    val enabled: Boolean,
    val spacing: Int,
) {
    init {
        require(spacing > 0) { "Drawing coordinate grid spacing must be positive." }
    }
}

data class DrawingTitleBlockRegion(
    val regionId: String,
) {
    init {
        require(regionId.isNotBlank()) { "Drawing title block region id must not be blank." }
    }
}

enum class DrawingStyle {
    SOLID,
    DASHED,
    DOTTED,
}

enum class DrawingEndpointBehavior {
    ATTACH_TO_ANCHOR,
    SHEET_REFERENCE,
}

enum class DrawingCrossingBehavior {
    JUNCTION_REQUIRED,
    DISCONNECTED_CROSSING,
    WIRE_HOP,
}

enum class DrawingLabelPolicy {
    NONE,
    TERMINAL_PAIR,
    REFERENCE_DESIGNATION,
}

enum class ConnectionLineKind {
    POWER,
    CONTROL,
    SAFETY,
    COMMUNICATION,
    PROTECTIVE_EARTH,
}

data class DrawingStrokeClass(
    val lineStyleId: LineStyleId,
    val weight: Double,
    val style: DrawingStyle,
    val colorKey: String,
) {
    init {
        require(colorKey.isNotBlank()) { "Drawing stroke color token must not be blank." }
    }
}

data class ConnectionPresentationClass(
    val classId: ConnectionLineClassId,
    val lineKind: ConnectionLineKind,
    val lineStyleId: LineStyleId,
    val endpointBehavior: DrawingEndpointBehavior,
    val labelPolicy: DrawingLabelPolicy,
    val crossingBehavior: DrawingCrossingBehavior,
    val provenance: SourceProvenance = SourceProvenance("drawing-profile.athena", 1, 1, 1, 1),
)

data class DrawingStandardProfile(
    val profileId: DrawingProfileId,
    val frame: DrawingFramePolicy,
    val coordinateGrid: DrawingCoordinateGridPolicy,
    val titleBlockRegions: List<DrawingTitleBlockRegion>,
    val defaultTextScale: Double,
    val strokeClasses: List<DrawingStrokeClass>,
    val junctionRule: DrawingCrossingBehavior,
    val crossingRule: DrawingCrossingBehavior,
    val referenceDesignationLabelPolicy: DrawingLabelPolicy,
    val presentationClasses: List<ConnectionPresentationClass>,
    val provenance: SourceProvenance = SourceProvenance("drawing-profile.athena", 1, 1, 1, 1),
) {
    companion object {
        fun standardProfessional(): DrawingStandardProfile = DrawingStandardProfile(
            profileId = DrawingProfileId("control-drawing-iec"),
            frame = DrawingFramePolicy(enabled = true),
            coordinateGrid = DrawingCoordinateGridPolicy(enabled = true, spacing = 10),
            titleBlockRegions = listOf(DrawingTitleBlockRegion("title-block")),
            defaultTextScale = 1.0,
            strokeClasses = listOf(
                DrawingStrokeClass(
                    lineStyleId = LineStyleId("stroke:power"),
                    weight = 1.4,
                    style = DrawingStyle.SOLID,
                    colorKey = "drawing.power",
                ),
                DrawingStrokeClass(
                    lineStyleId = LineStyleId("stroke:control"),
                    weight = 1.0,
                    style = DrawingStyle.SOLID,
                    colorKey = "drawing.control",
                ),
                DrawingStrokeClass(
                    lineStyleId = LineStyleId("stroke:protective-earth"),
                    weight = 1.2,
                    style = DrawingStyle.SOLID,
                    colorKey = "drawing.protective-earth",
                ),
                DrawingStrokeClass(
                    lineStyleId = LineStyleId("stroke:safety"),
                    weight = 1.0,
                    style = DrawingStyle.DASHED,
                    colorKey = "drawing.safety",
                ),
                DrawingStrokeClass(
                    lineStyleId = LineStyleId("stroke:communication"),
                    weight = 0.8,
                    style = DrawingStyle.DOTTED,
                    colorKey = "drawing.communication",
                ),
            ),
            junctionRule = DrawingCrossingBehavior.JUNCTION_REQUIRED,
            crossingRule = DrawingCrossingBehavior.DISCONNECTED_CROSSING,
            referenceDesignationLabelPolicy = DrawingLabelPolicy.REFERENCE_DESIGNATION,
            presentationClasses = listOf(
                ConnectionPresentationClass(
                    classId = ConnectionLineClassId("line:power"),
                    lineKind = ConnectionLineKind.POWER,
                    lineStyleId = LineStyleId("stroke:power"),
                    endpointBehavior = DrawingEndpointBehavior.ATTACH_TO_ANCHOR,
                    labelPolicy = DrawingLabelPolicy.TERMINAL_PAIR,
                    crossingBehavior = DrawingCrossingBehavior.JUNCTION_REQUIRED,
                ),
                ConnectionPresentationClass(
                    classId = ConnectionLineClassId("line:control"),
                    lineKind = ConnectionLineKind.CONTROL,
                    lineStyleId = LineStyleId("stroke:control"),
                    endpointBehavior = DrawingEndpointBehavior.ATTACH_TO_ANCHOR,
                    labelPolicy = DrawingLabelPolicy.TERMINAL_PAIR,
                    crossingBehavior = DrawingCrossingBehavior.DISCONNECTED_CROSSING,
                ),
                ConnectionPresentationClass(
                    classId = ConnectionLineClassId("line:protective-earth"),
                    lineKind = ConnectionLineKind.PROTECTIVE_EARTH,
                    lineStyleId = LineStyleId("stroke:protective-earth"),
                    endpointBehavior = DrawingEndpointBehavior.ATTACH_TO_ANCHOR,
                    labelPolicy = DrawingLabelPolicy.TERMINAL_PAIR,
                    crossingBehavior = DrawingCrossingBehavior.DISCONNECTED_CROSSING,
                ),
                ConnectionPresentationClass(
                    classId = ConnectionLineClassId("line:safety"),
                    lineKind = ConnectionLineKind.SAFETY,
                    lineStyleId = LineStyleId("stroke:safety"),
                    endpointBehavior = DrawingEndpointBehavior.ATTACH_TO_ANCHOR,
                    labelPolicy = DrawingLabelPolicy.REFERENCE_DESIGNATION,
                    crossingBehavior = DrawingCrossingBehavior.DISCONNECTED_CROSSING,
                ),
                ConnectionPresentationClass(
                    classId = ConnectionLineClassId("line:communication"),
                    lineKind = ConnectionLineKind.COMMUNICATION,
                    lineStyleId = LineStyleId("stroke:communication"),
                    endpointBehavior = DrawingEndpointBehavior.ATTACH_TO_ANCHOR,
                    labelPolicy = DrawingLabelPolicy.REFERENCE_DESIGNATION,
                    crossingBehavior = DrawingCrossingBehavior.DISCONNECTED_CROSSING,
                ),
            ),
        )
    }
}

data class DrawingProfileDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val affectedRouteIds: List<String> = emptyList(),
    val provenance: SourceProvenance? = null,
) {
    init {
        require(code.isNotBlank()) { "Drawing profile diagnostic code must not be blank." }
        require(subject.isNotBlank()) { "Drawing profile diagnostic subject must not be blank." }
        require(message.isNotBlank()) { "Drawing profile diagnostic message must not be blank." }
    }
}

sealed interface DrawingProfileCompilation {
    data class Success(val profiles: List<DrawingStandardProfile>) : DrawingProfileCompilation
    data class Failure(val diagnostics: List<DrawingProfileDiagnostic>) : DrawingProfileCompilation
}

data class ConnectionPresentationLineEvidence(
    val routeId: SchematicRouteId,
    val connectionId: ElectricalConnectionId,
    val profileId: DrawingProfileId,
    val lineClassId: ConnectionLineClassId,
    val lineStyleId: LineStyleId,
    val weight: Double,
    val style: DrawingStyle,
    val colorKey: String,
    val endpointBehavior: DrawingEndpointBehavior,
    val labelPolicy: DrawingLabelPolicy,
    val crossingBehavior: DrawingCrossingBehavior,
    val selectedPolicyId: String,
    val compilerSnapshotId: String,
    val provenance: SourceProvenance,
)

sealed interface DrawingProfileResolution {
    data class Success(val lines: List<ConnectionPresentationLineEvidence>) : DrawingProfileResolution
    data class Failure(val diagnostics: List<DrawingProfileDiagnostic>) : DrawingProfileResolution
}

data class ConnectionPresentationLinePayload(
    val routeId: String,
    val connectionId: String,
    val profileId: String,
    val lineClassId: String,
    val lineStyleId: String,
    val weight: Double,
    val style: String,
    val colorKey: String,
    val endpointBehavior: String,
    val labelPolicy: String,
    val crossingBehavior: String,
    val selectedPolicyId: String,
    val compilerSnapshotId: String,
    val sourceFile: String,
)

data class DrawingProfilePayload(
    val authority: String,
    val lines: List<ConnectionPresentationLinePayload>,
    val rendererInferences: List<String> = emptyList(),
    val rawMarkupFragments: List<String> = emptyList(),
)

class DrawingProfileCompiler {
    fun compile(profiles: List<DrawingStandardProfile>): DrawingProfileCompilation {
        val diagnostics = mutableListOf<DrawingProfileDiagnostic>()
        profiles.groupBy { profile -> profile.profileId }.filterValues { duplicates -> duplicates.size > 1 }.forEach { (id, duplicates) ->
            diagnostics += diagnostic("drawing.profile.duplicate", id.value, "Drawing profile id is declared more than once.", duplicates.first().provenance)
        }
        profiles.forEach { profile ->
            diagnostics += validateProfile(profile)
        }
        return if (diagnostics.isEmpty()) {
            DrawingProfileCompilation.Success(profiles.sortedBy { profile -> profile.profileId.value })
        } else {
            DrawingProfileCompilation.Failure(diagnostics.sortedWith(compareBy({ it.code }, { it.subject })))
        }
    }

    fun resolveRoutes(
        profile: DrawingStandardProfile,
        routeFacts: List<RouteFact>,
        selectedPolicyId: String,
    ): DrawingProfileResolution {
        require(selectedPolicyId.isNotBlank()) { "Selected drawing policy id must not be blank." }
        val compilation = compile(listOf(profile))
        if (compilation is DrawingProfileCompilation.Failure) {
            return DrawingProfileResolution.Failure(compilation.diagnostics)
        }
        val strokeById = profile.strokeClasses.associateBy { stroke -> stroke.lineStyleId }
        val diagnostics = mutableListOf<DrawingProfileDiagnostic>()
        val lines = routeFacts.mapNotNull { route ->
            val lineKind = route.connectionRole?.toLineKind()
            val matches = profile.presentationClasses.filter { lineClass -> lineClass.lineKind == lineKind }
            when {
                lineKind == null || matches.isEmpty() -> {
                    diagnostics += diagnostic(
                        code = "drawing.profile.line-class.unclassified",
                        subject = route.routeId.value,
                        message = "Route has no matching Connection Presentation Class.",
                        provenance = route.provenance,
                        affectedRouteIds = listOf(route.routeId.value),
                    )
                    null
                }
                matches.size > 1 -> {
                    diagnostics += diagnostic(
                        code = "drawing.profile.line-class.ambiguous",
                        subject = route.routeId.value,
                        message = "Route matches more than one Connection Presentation Class.",
                        provenance = route.provenance,
                        affectedRouteIds = listOf(route.routeId.value),
                    )
                    null
                }
                else -> {
                    val lineClass = matches.single()
                    val stroke = strokeById.getValue(lineClass.lineStyleId)
                    ConnectionPresentationLineEvidence(
                        routeId = route.routeId,
                        connectionId = route.connectionId,
                        profileId = profile.profileId,
                        lineClassId = lineClass.classId,
                        lineStyleId = stroke.lineStyleId,
                        weight = stroke.weight,
                        style = stroke.style,
                        colorKey = stroke.colorKey,
                        endpointBehavior = lineClass.endpointBehavior,
                        labelPolicy = lineClass.labelPolicy,
                        crossingBehavior = lineClass.crossingBehavior,
                        selectedPolicyId = selectedPolicyId,
                        compilerSnapshotId = route.compilerSnapshotId,
                        provenance = route.provenance,
                    )
                }
            }
        }
        return if (diagnostics.isEmpty()) {
            DrawingProfileResolution.Success(lines.sortedBy { line -> line.routeId.value })
        } else {
            DrawingProfileResolution.Failure(diagnostics.sortedWith(compareBy({ it.code }, { it.subject })))
        }
    }

    fun normalize(resolution: DrawingProfileResolution.Success): DrawingProfilePayload {
        return DrawingProfilePayload(
            authority = "athena",
            lines = resolution.lines.map { line ->
                ConnectionPresentationLinePayload(
                    routeId = line.routeId.value,
                    connectionId = line.connectionId.value,
                    profileId = line.profileId.value,
                    lineClassId = line.lineClassId.value,
                    lineStyleId = line.lineStyleId.value,
                    weight = line.weight,
                    style = line.style.name,
                    colorKey = line.colorKey,
                    endpointBehavior = line.endpointBehavior.name,
                    labelPolicy = line.labelPolicy.name,
                    crossingBehavior = line.crossingBehavior.name,
                    selectedPolicyId = line.selectedPolicyId,
                    compilerSnapshotId = line.compilerSnapshotId,
                    sourceFile = line.provenance.file,
                )
            },
        )
    }

    private fun validateProfile(profile: DrawingStandardProfile): List<DrawingProfileDiagnostic> {
        val diagnostics = mutableListOf<DrawingProfileDiagnostic>()
        if (profile.titleBlockRegions.isEmpty()) {
            diagnostics += diagnostic("drawing.profile.missing", profile.profileId.value, "Drawing profile requires at least one title-block region.", profile.provenance)
        }
        if (profile.defaultTextScale <= 0.0) {
            diagnostics += diagnostic("drawing.profile.missing", profile.profileId.value, "Drawing profile text scale must be positive.", profile.provenance)
        }
        val duplicateStrokeIds = profile.strokeClasses.groupBy { stroke -> stroke.lineStyleId }.filterValues { duplicates -> duplicates.size > 1 }
        duplicateStrokeIds.forEach { (id, duplicates) ->
            diagnostics += diagnostic("drawing.profile.stroke-class.invalid", id.value, "Drawing stroke class id is declared more than once.", duplicates.first().let { profile.provenance })
        }
        profile.strokeClasses.filter { stroke -> stroke.weight <= 0.0 || stroke.colorKey.isBlank() }.forEach { stroke ->
            diagnostics += diagnostic("drawing.profile.stroke-class.invalid", stroke.lineStyleId.value, "Drawing stroke class requires positive weight and color token.", profile.provenance)
        }
        val strokeIds = profile.strokeClasses.map { stroke -> stroke.lineStyleId }.toSet()
        val duplicateClassIds = profile.presentationClasses.groupBy { lineClass -> lineClass.classId }.filterValues { duplicates -> duplicates.size > 1 }
        duplicateClassIds.forEach { (id, duplicates) ->
            diagnostics += diagnostic("drawing.profile.line-class.duplicate", id.value, "Connection Presentation Class id is declared more than once.", duplicates.first().provenance)
        }
        val duplicateLineKinds = profile.presentationClasses.groupBy { lineClass -> lineClass.lineKind }.filterValues { duplicates -> duplicates.size > 1 }
        duplicateLineKinds.forEach { (lineKind, duplicates) ->
            diagnostics += diagnostic("drawing.profile.line-class.ambiguous", lineKind.name, "Connection line kind maps to more than one presentation class.", duplicates.first().provenance)
        }
        profile.presentationClasses.filter { lineClass -> lineClass.lineStyleId !in strokeIds }.forEach { lineClass ->
            diagnostics += diagnostic("drawing.profile.stroke-class.invalid", lineClass.classId.value, "Connection Presentation Class references missing stroke class.", lineClass.provenance)
        }
        return diagnostics
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
        provenance: SourceProvenance?,
        affectedRouteIds: List<String> = emptyList(),
    ): DrawingProfileDiagnostic = DrawingProfileDiagnostic(code, subject, message, affectedRouteIds, provenance)
}

private fun ElectricalConnectionRole.toLineKind(): ConnectionLineKind? = when (this) {
    ElectricalConnectionRole.POWER_FEED,
    ElectricalConnectionRole.LOAD_CONNECTION,
    -> ConnectionLineKind.POWER
    ElectricalConnectionRole.GROUND_REFERENCE -> ConnectionLineKind.PROTECTIVE_EARTH
    ElectricalConnectionRole.CONTROL_SIGNAL,
    ElectricalConnectionRole.TERMINAL_TRANSITION,
    -> ConnectionLineKind.CONTROL
    ElectricalConnectionRole.UNKNOWN -> null
}
