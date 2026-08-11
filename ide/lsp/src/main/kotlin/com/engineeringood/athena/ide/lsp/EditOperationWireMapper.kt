package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.*
import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.SceneId

/** Closed JSON-RPC mapping for the generated Edit Operation schema. */
internal fun editOperationFromWire(payload: Map<String, Any?>): EditOperationEnvelope {
    payload.requireKeys("schemaVersion", "operationId", "sceneId", "authorityClass", "sourceRevision", "target", "sourceTrace", "requestedWritableFiles", "body")
    require(payload.int("schemaVersion") == 1) { "Edit Operation schemaVersion must be 1." }
    val body = operationBody(payload.map("body"))
    val declaredAuthority = EditAuthorityClass.valueOf(payload.string("authorityClass"))
    require(declaredAuthority == body.kind.authorityClass) {
        "${body.kind} authorityClass must be ${body.kind.authorityClass}."
    }
    val target = payload.map("target").also { it.requireKeys("identities") }
    val trace = payload.map("sourceTrace").also { it.requireKeys("traceId", "subjectId") }
    return EditOperationEnvelope(
        operationId = payload.string("operationId"),
        sceneId = SceneId(payload.string("sceneId")),
        sourceRevision = sourceRevision(payload.map("sourceRevision")),
        target = OperationTarget(target.stringList("identities")),
        sourceTrace = SourceTraceContext(trace.string("traceId"), trace.string("subjectId")),
        requestedWritableFiles = payload.stringList("requestedWritableFiles"),
        body = body,
    )
}

private fun sourceRevision(value: Map<String, Any?>): SourceRevision {
    value.requireKeys("sceneInputRevision", "sourceRootIdentity", "engineeringSourceDigest", "sheetDigest", "styleDigest", "lockDigest", "packageItemDigests", "compilerVersion", "sceneSchemaVersion", "profileVersion")
    val packages = value.list("packageItemDigests").map { item ->
        val mapped = item.objectMap("packageItemDigests").also { it.requireKeys("packageId", "itemId", "sha256") }
        PackageItemDigest(mapped.string("packageId"), mapped.string("itemId"), mapped.string("sha256"))
    }
    return SourceRevision(
        sceneInputRevision = InputRevision(value.string("sceneInputRevision")),
        sourceRootIdentity = value.string("sourceRootIdentity"),
        engineeringSourceDigest = value.string("engineeringSourceDigest"),
        sheetDigest = value.string("sheetDigest"),
        styleDigest = revisionDigest(value.string("styleDigest")),
        lockDigest = revisionDigest(value.string("lockDigest")),
        packageItemDigests = packages,
        compilerVersion = value.string("compilerVersion"),
        sceneSchemaVersion = value.string("sceneSchemaVersion"),
        profileVersion = value.string("profileVersion"),
    )
}

private fun revisionDigest(value: String): RevisionDigest =
    if (value == "absent") RevisionDigest.absent() else RevisionDigest.present(value)

private fun operationBody(body: Map<String, Any?>): EditOperationBody = when (body.string("kind")) {
    "MOVE_OCCURRENCE" -> {
        body.requireKeys("kind", "sheetId", "occurrenceId", "point", "lockAction")
        MoveOccurrence(body.string("sheetId"), body.string("occurrenceId"), point(body.map("point")), LockAction.valueOf(body.string("lockAction")))
    }
    "ALIGN_OCCURRENCES" -> {
        body.requireKeys("kind", "sheetId", "occurrenceIds", "axis")
        AlignOccurrences(body.string("sheetId"), body.stringList("occurrenceIds"), AlignmentAxis.valueOf(body.string("axis")))
    }
    "DISTRIBUTE_OCCURRENCES" -> {
        body.requireKeys("kind", "sheetId", "occurrenceIds", "axis")
        DistributeOccurrences(body.string("sheetId"), body.stringList("occurrenceIds"), DistributionAxis.valueOf(body.string("axis")))
    }
    "SNAP_OCCURRENCE_TO_GRID" -> {
        body.requireKeys("kind", "sheetId", "occurrenceId", "point")
        SnapOccurrenceToGrid(body.string("sheetId"), body.string("occurrenceId"), point(body.map("point")))
    }
    "SET_STYLE" -> {
        body.requireKeys("kind", "sheetId", "target", "fields")
        val target = body.map("target").also { it.requireKeys("kind", "id") }
        SetStyle(body.string("sheetId"), StyleTarget(StyleTargetKind.valueOf(target.string("kind")), target.string("id")), styleFields(body.map("fields")))
    }
    "CHANGE_SYMBOL" -> {
        body.requireKeys("kind", "occurrenceId", "representationRef", "variantRef", "placeholderValues")
        val placeholderValues = body.map("placeholderValues").mapValues { (name, raw) ->
            val value = raw.objectMap(name).also { it.requireKeys("kind", "value") }
            RepresentationPlaceholderEditValue(
                kind = RepresentationPlaceholderValueKind.valueOf(value.string("kind")),
                value = value.string("value"),
            )
        }
        ChangeSymbol(
            occurrenceId = body.string("occurrenceId"),
            representationRef = body.string("representationRef"),
            variantRef = body.optionalString("variantRef"),
            placeholderValues = placeholderValues,
        )
    }
    "CONNECT_PORTS" -> {
        body.requireKeys("kind", "connectionKind", "endpoints", "requirements")
        ConnectPorts(
            connectionKind = ConnectionEditKind.valueOf(body.string("connectionKind")),
            endpoints = body.list("endpoints").map { raw ->
                val endpoint = raw.objectMap("endpoints").also { it.requireKeys("role", "portId") }
                ConnectionEditEndpoint(
                    role = ConnectionEditEndpointRole.valueOf(endpoint.string("role")),
                    portId = endpoint.string("portId"),
                )
            },
            requirements = body.list("requirements").map { raw -> connectionRequirement(raw.objectMap("requirements")) },
        )
    }
    "RECONNECT_CONNECTION_ENDPOINT" -> {
        body.requireKeys("kind", "connectionId", "endpointRole", "replacementPortId")
        ReconnectConnectionEndpoint(
            connectionId = body.string("connectionId"),
            endpointRole = ConnectionEditEndpointRole.valueOf(body.string("endpointRole")),
            replacementPortId = body.string("replacementPortId"),
        )
    }
    "ADJUST_CONNECTION_ROUTE" -> {
        body.requireKeys("kind", "sheetId", "connectionId", "projectionId", "target", "point")
        val target = body.map("target").also { it.requireKeys("kind", "ordinal") }
        val point = body.map("point").also { it.requireKeys("column", "row") }
        AdjustConnectionRoute(
            sheetId = body.string("sheetId"),
            connectionId = body.string("connectionId"),
            projectionId = body.string("projectionId"),
            target = LogicalRouteTarget(LogicalRouteTargetKind.valueOf(target.string("kind")), target.int("ordinal")),
            point = LogicalRoutePoint(point.int("column"), point.int("row")),
        )
    }
    "BIND_PART" -> {
        body.requireKeys("kind", "functionId", "implementationRole", "partRef")
        BindPart(body.string("functionId"), body.string("implementationRole"), body.string("partRef"))
    }
    "ADD_PACKAGE_DEPENDENCY" -> {
        body.requireKeys("kind", "packageName", "packageVersion")
        AddPackageDependency(body.string("packageName"), body.string("packageVersion"))
    }
    "INSERT_ELEMENT_OCCURRENCE" -> {
        body.requireKeys("kind", "sheetId", "functionId", "elementRef", "column", "row", "subGridX", "subGridY")
        InsertElementOccurrence(body.string("sheetId"), body.string("functionId"), body.string("elementRef"), body.int("column"), body.int("row"), body.int("subGridX"), body.int("subGridY"))
    }
    "INSERT_MACRO_OCCURRENCES" -> {
        body.requireKeys("kind", "sheetId", "macroRef", "functionIdsBySlot", "column", "row")
        val functions = body.map("functionIdsBySlot").mapValues { (slot, value) ->
            value as? String ?: throw IllegalArgumentException("Edit Operation Macro slot `$slot` must reference a Function identity.")
        }
        InsertMacroOccurrences(body.string("sheetId"), body.string("macroRef"), functions, body.int("column"), body.int("row"))
    }
    "UNDO" -> {
        body.requireKeys("kind", "journalEntryId")
        Undo(body.string("journalEntryId"))
    }
    "REDO" -> {
        body.requireKeys("kind", "journalEntryId")
        Redo(body.string("journalEntryId"))
    }
    else -> throw IllegalArgumentException("Unknown Edit Operation kind `${body["kind"]}`.")
}

private fun connectionRequirement(value: Map<String, Any?>): ConnectionRequirementIntent {
    value.requireKeys("kind", "value")
    val typed = value.map("value")
    val requirementValue = when (typed.string("kind")) {
        "QUANTITY" -> {
            typed.requireKeys("kind", "value", "unit")
            ConnectionRequirementValue.Quantity(typed.string("value"), typed.string("unit"))
        }
        "SYMBOL" -> {
            typed.requireKeys("kind", "value")
            ConnectionRequirementValue.Symbol(typed.string("value"))
        }
        "BOOLEAN" -> {
            typed.requireKeys("kind", "value")
            ConnectionRequirementValue.Boolean(typed.boolean("value"))
        }
        else -> throw IllegalArgumentException("Unknown Connection requirement value kind `${typed["kind"]}`.")
    }
    return ConnectionRequirementIntent(ConnectionRequirementKind.valueOf(value.string("kind")), requirementValue)
}

private fun point(value: Map<String, Any?>): SheetPoint {
    value.requireKeys("x", "y")
    return SheetPoint(value.int("x"), value.int("y"))
}

private fun styleFields(fields: Map<String, Any?>): StyleFields {
    fields.requireOnlyKeys("strokeRgba", "fillRgba", "strokeWidth", "dash", "lineCap", "lineJoin", "opacity", "fontSize", "fontWeight", "routeMarker", "portDisplay")
    return StyleFields(
        strokeRgba = fields.optionalString("strokeRgba"), fillRgba = fields.optionalString("fillRgba"),
        strokeWidth = fields.optionalInt("strokeWidth"), dash = fields["dash"]?.let { fields.list("dash").map { value -> value.exactInt("dash") } },
        lineCap = fields.optionalString("lineCap"), lineJoin = fields.optionalString("lineJoin"), opacity = fields.optionalInt("opacity"),
        fontSize = fields.optionalInt("fontSize"), fontWeight = fields.optionalInt("fontWeight"),
        routeMarker = fields.optionalString("routeMarker")?.let(RouteMarker::valueOf),
        portDisplay = fields.optionalString("portDisplay")?.let(PortDisplay::valueOf),
    )
}

private fun Map<String, Any?>.requireKeys(vararg expected: String) {
    require(keys == expected.toSet()) { "Edit Operation fields must be exactly ${expected.sorted().joinToString()}" }
}

private fun Map<String, Any?>.requireOnlyKeys(vararg allowed: String) {
    require(keys.isNotEmpty() && keys.all { it in allowed }) { "Style fields contain unknown or empty content." }
}

private fun Map<String, Any?>.string(key: String): String = this[key] as? String
    ?: throw IllegalArgumentException("Edit Operation field `$key` must be text.")
private fun Map<String, Any?>.optionalString(key: String): String? = this[key]?.let { it as? String ?: throw IllegalArgumentException("Edit Operation field `$key` must be text.") }
private fun Map<String, Any?>.int(key: String): Int = this[key].exactInt(key)
private fun Map<String, Any?>.optionalInt(key: String): Int? = this[key]?.exactInt(key)
private fun Map<String, Any?>.boolean(key: String): Boolean = this[key] as? Boolean
    ?: throw IllegalArgumentException("Edit Operation field `$key` must be true or false.")
private fun Any?.exactInt(key: String): Int {
    val number = this as? Number ?: throw IllegalArgumentException("Edit Operation field `$key` must be a number.")
    val double = number.toDouble()
    require(double.isFinite() && double % 1.0 == 0.0 && double in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble()) {
        "Edit Operation field `$key` must be an integer."
    }
    return double.toInt()
}
@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.map(key: String): Map<String, Any?> = this[key] as? Map<String, Any?>
    ?: throw IllegalArgumentException("Edit Operation field `$key` must be an object.")
private fun Map<String, Any?>.list(key: String): List<*> = this[key] as? List<*>
    ?: throw IllegalArgumentException("Edit Operation field `$key` must be a list.")
private fun Map<String, Any?>.stringList(key: String): List<String> = list(key).map { it as? String ?: throw IllegalArgumentException("Edit Operation field `$key` must contain text.") }
@Suppress("UNCHECKED_CAST")
private fun Any?.objectMap(key: String): Map<String, Any?> = this as? Map<String, Any?>
    ?: throw IllegalArgumentException("Edit Operation field `$key` must contain objects.")
