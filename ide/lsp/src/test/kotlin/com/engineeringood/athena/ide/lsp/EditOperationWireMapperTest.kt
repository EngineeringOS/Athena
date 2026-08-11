package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.EditAuthorityClass
import com.engineeringood.athena.interaction.SetStyle
import com.engineeringood.athena.interaction.AddPackageDependency
import com.engineeringood.athena.interaction.AdjustConnectionRoute
import com.engineeringood.athena.interaction.ConnectPorts
import com.engineeringood.athena.interaction.ReconnectConnectionEndpoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class EditOperationWireMapperTest {
    @Test
    fun `maps closed set style payload with full revision`() {
        val operation = editOperationFromWire(validPayload())
        val body = assertIs<SetStyle>(operation.body)
        assertEquals(EditAuthorityClass.PRESENTATION, operation.authorityClass)
        assertEquals(3, body.fields.strokeWidth)
        assertEquals("absent", operation.sourceRevision.styleDigest.canonicalValue())
    }

    @Test
    fun `rejects spoofed authority legacy reconnect unknown fields and fractional integers`() {
        assertFailsWith<IllegalArgumentException> { editOperationFromWire(validPayload(authority = "ENGINEERING")) }
        assertFailsWith<IllegalArgumentException> {
            editOperationFromWire(validPayload(authority = "ENGINEERING", body = mapOf(
                "kind" to "RECONNECT_PORT",
                "relationshipId" to "relationship:power",
                "endpointRole" to "SINK",
                "portId" to "port:Breaker.line",
            )))
        }
        assertFailsWith<IllegalArgumentException> {
            editOperationFromWire(validPayload(body = styleBody(mapOf("strokeWidth" to 3.5))))
        }
        assertFailsWith<IllegalArgumentException> {
            editOperationFromWire(validPayload() + ("sourceText" to "forbidden"))
        }
    }

    @Test
    fun `maps typed connect reconnect and logical route operations`() {
        val connect = assertIs<ConnectPorts>(
            editOperationFromWire(
                validPayload(
                    authority = "ENGINEERING",
                    body = mapOf(
                        "kind" to "CONNECT_PORTS",
                        "connectionKind" to "WIRE",
                        "endpoints" to listOf(
                            mapOf("role" to "SOURCE", "portId" to "port:Supply.L1"),
                            mapOf("role" to "SINK", "portId" to "port:Breaker.line"),
                        ),
                        "requirements" to listOf(
                            mapOf("kind" to "CROSS_SECTION", "value" to mapOf("kind" to "QUANTITY", "value" to "1.5", "unit" to "mm2")),
                        ),
                    ),
                ) + ("requestedWritableFiles" to listOf("src/project.athena")),
            ).body,
        )
        assertEquals("port:Supply.L1", connect.endpoints.first().portId)

        val reconnect = assertIs<ReconnectConnectionEndpoint>(
            editOperationFromWire(
                validPayload(
                    authority = "ENGINEERING",
                    body = mapOf(
                        "kind" to "RECONNECT_CONNECTION_ENDPOINT",
                        "connectionId" to "connection:src/project.athena:wire:Supply.L1:Breaker.line",
                        "endpointRole" to "SINK",
                        "replacementPortId" to "port:Contactor.L1",
                    ),
                ) + ("requestedWritableFiles" to listOf("src/project.athena")),
            ).body,
        )
        assertEquals("port:Contactor.L1", reconnect.replacementPortId)

        val route = assertIs<AdjustConnectionRoute>(
            editOperationFromWire(
                validPayload(
                    body = mapOf(
                        "kind" to "ADJUST_CONNECTION_ROUTE",
                        "sheetId" to "sheet-main",
                        "connectionId" to "connection:src/project.athena:wire:Supply.L1:Breaker.line",
                        "projectionId" to "view/sheet-main/connection/Supply-Breaker",
                        "target" to mapOf("kind" to "SEGMENT", "ordinal" to 2.0),
                        "point" to mapOf("column" to 17.0, "row" to 8.0),
                    ),
                ) + ("requestedWritableFiles" to listOf("src/project.sheet.athena")),
            ).body,
        )
        assertEquals(17, route.point.column)
    }

    @Test
    fun `connection operations fail closed on raw pixels malformed identities and unknown fields`() {
        val route = mapOf(
            "kind" to "ADJUST_CONNECTION_ROUTE",
            "sheetId" to "sheet-main",
            "connectionId" to "connection:main",
            "projectionId" to "view/sheet-main/connection/main",
            "target" to mapOf("kind" to "BEND", "ordinal" to 1.0),
            "point" to mapOf("column" to 4.0, "row" to 8.0),
        )
        assertFailsWith<IllegalArgumentException> {
            editOperationFromWire(validPayload(body = route + ("viewportX" to 120.0)))
        }
        assertFailsWith<IllegalArgumentException> {
            editOperationFromWire(validPayload(authority = "ENGINEERING", body = mapOf(
                "kind" to "RECONNECT_CONNECTION_ENDPOINT",
                "connectionId" to "connection:main",
                "endpointRole" to "SINK",
                "replacementPortId" to "Breaker.line",
            )))
        }
    }

    @Test
    fun `maps typed local package dependency intent`() {
        val operation = editOperationFromWire(
            validPayload(
                authority = "ENGINEERING",
                body = mapOf(
                    "kind" to "ADD_PACKAGE_DEPENDENCY",
                    "packageName" to "com.vendor.drive",
                    "packageVersion" to "1.0.0",
                ),
            ) + ("target" to mapOf("identities" to listOf("com.vendor.drive"))) +
                ("requestedWritableFiles" to listOf("athena.yaml")),
        )
        val body = assertIs<AddPackageDependency>(operation.body)
        assertEquals("com.vendor.drive", body.packageName)
        assertEquals("1.0.0", body.packageVersion)
    }

    private fun validPayload(
        authority: String = "PRESENTATION",
        body: Map<String, Any?> = styleBody(mapOf("strokeWidth" to 3.0, "routeMarker" to "END_ARROW")),
    ): Map<String, Any?> = mapOf(
        "schemaVersion" to 1.0,
        "operationId" to "00000000-0000-4000-8000-000000000044",
        "sceneId" to "scene:sha256:${"1".repeat(64)}",
        "authorityClass" to authority,
        "sourceRevision" to mapOf(
            "sceneInputRevision" to "input:sha256:${"2".repeat(64)}",
            "sourceRootIdentity" to "source-root:sha256:${"3".repeat(64)}",
            "engineeringSourceDigest" to "4".repeat(64),
            "sheetDigest" to "5".repeat(64),
            "styleDigest" to "absent",
            "lockDigest" to "6".repeat(64),
            "packageItemDigests" to listOf(mapOf("packageId" to "pkg", "itemId" to "item", "sha256" to "7".repeat(64))),
            "compilerVersion" to "compiler-1",
            "sceneSchemaVersion" to "scene-1",
            "profileVersion" to "profile-1",
        ),
        "target" to mapOf("identities" to listOf("role:connection", "sheet-main")),
        "sourceTrace" to mapOf("traceId" to "trace:sha256:${"8".repeat(64)}", "subjectId" to "sheet-main"),
        "requestedWritableFiles" to listOf("src/project.sheet.style.athena"),
        "body" to body,
    )

    private fun styleBody(fields: Map<String, Any?>): Map<String, Any?> = mapOf(
        "kind" to "SET_STYLE",
        "sheetId" to "sheet-main",
        "target" to mapOf("kind" to "ROLE", "id" to "connection"),
        "fields" to fields,
    )
}
