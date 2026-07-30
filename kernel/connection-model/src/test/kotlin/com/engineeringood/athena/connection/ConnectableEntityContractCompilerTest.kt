package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringConnectionNetwork
import com.engineeringood.athena.ir.EngineeringConnectionNetworkMember
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringNetworkCompatibilityEvidence
import com.engineeringood.athena.ir.EngineeringNetworkJunction
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConnectableEntityContractCompilerTest {
    @Test
    fun `derives stable generic port contracts from canonical engineering IR`() {
        val result = ConnectableEntityContractCompiler().compile(
            EngineeringDocument(
                system = system(),
                components = listOf(component("drive-01", "Drive")),
                ports = listOf(
                    port("drive-01", "L1", "input", "power", "line"),
                    port("drive-01", "PE", "passive", "protective-earth", "earth"),
                ),
                connections = emptyList(),
            ),
        )

        val success = assertIs<ConnectableEntityContractCompilation.Success>(result)
        assertEquals("component:drive-01", success.entities.single().id.value)
        assertEquals("Drive", success.entities.single().kind)
        assertEquals(setOf("power-input"), success.entities.single().interfaces.map { it.id.value }.toSet())
        assertEquals(ConnectablePortDirection.INPUT, success.entities.single().ports[0].compatibility.direction)
        assertEquals(ConnectablePortMultiplicity.SINGLE, success.entities.single().ports[0].compatibility.multiplicity)
        assertEquals(listOf(ConnectableInterfaceId("power-input")), success.entities.single().ports[0].interfaceIds)
        assertEquals("power", success.entities.single().ports[0].compatibility.signalKind)
        assertEquals("line", success.entities.single().ports[0].compatibility.role)
        assertEquals("400V", success.entities.single().ports[0].compatibility.parameters["voltage"])
        assertEquals(ConnectableConstraintOwner.SEMANTIC, success.entities.single().ports[0].compatibility.owner)
        assertEquals(ConnectableConstraintStrength.REQUIRED, success.entities.single().ports[0].compatibility.strength)
        assertEquals("protective-earth", success.entities.single().ports[1].compatibility.signalKind)
    }

    @Test
    fun `rejects duplicate connectable port identity with source provenance`() {
        val result = ConnectableEntityContractCompiler().compile(
            EngineeringDocument(
                system = system(),
                components = listOf(component("drive-01", "Drive")),
                ports = listOf(
                    port("drive-01", "L1", "input", "power", "line"),
                    port("drive-01", "L1", "input", "power", "line"),
                ),
                connections = emptyList(),
            ),
        )

        val failure = assertIs<ConnectableEntityContractCompilation.Failure>(result)
        assertEquals("connectable.port.duplicate", failure.diagnostics.single().code)
        assertEquals("sample.athena", failure.diagnostics.single().provenance.file)
    }

    @Test
    fun `validates typed connection endpoints and preserves both port locations`() {
        val source = port("source", "out", "out", "power", "line")
        val target = port("target", "in", "in", "power", "line")
        val document = EngineeringDocument(
            system = system(),
            components = listOf(component("source", "Source"), component("target", "Target")),
            ports = listOf(source, target),
            connections = listOf(connection("power", source, target)),
        )

        val valid = assertIs<ConnectableEntityContractCompilation.Success>(ConnectableEntityContractCompiler().compile(document))
        assertEquals(listOf("connection:sample:power"), valid.connections.map { it.id.value })
        assertEquals(source.id, valid.connections.single().from.portId)
        assertEquals(target.id, valid.connections.single().to.portId)

        val incompatible = document.copy(
            ports = listOf(source, port("target", "in", "out", "power", "line")),
        )
        val diagnostics = assertIs<ConnectableEntityContractCompilation.Failure>(
            ConnectableEntityContractCompiler().compile(incompatible),
        ).diagnostics
        assertEquals(listOf("connectable.connection.direction.incompatible", "connectable.connection.direction.incompatible"), diagnostics.map { it.code })
        assertEquals(listOf("port:source.out", "port:target.in"), diagnostics.map { diagnostic ->
            if (diagnostic.provenance == source.provenance) source.id.value else target.id.value
        })
    }

    @Test
    fun `rejects connection between connectable and untyped port`() {
        val source = port("source", "out", "out", "power", "line")
        val target = port("legacy", "in", "in", "power", "line")
        val document = EngineeringDocument(
            system = system(),
            components = listOf(component("source", "Source"), legacyComponent("legacy", "Legacy")),
            ports = listOf(source, target),
            connections = listOf(connection("legacy", source, target)),
        )

        val failure = assertIs<ConnectableEntityContractCompilation.Failure>(
            ConnectableEntityContractCompiler().compile(document),
        )
        assertEquals(
            listOf("connectable.connection.endpoint.untyped", "connectable.connection.endpoint.untyped"),
            failure.diagnostics.map { it.code },
        )
    }

    @Test
    fun `rejects invalid typed port fields and duplicate compatibility parameters`() {
        val invalidPort = port(
            owner = "drive-01",
            name = "L1",
            direction = "sideways",
            signal = "power",
            role = "line",
            multiplicity = "several",
        ).copy(
            properties = listOf(
                property("direction", "sideways"),
                property("signal", "power"),
                property("role", "line"),
                property("interface", "missing-interface"),
                property("multiplicity", "several"),
                property("compatibility.voltage", "400V"),
                property("compatibility.voltage", "480V"),
            ),
        )
        val failure = assertIs<ConnectableEntityContractCompilation.Failure>(
            ConnectableEntityContractCompiler().compile(
                EngineeringDocument(
                    system = system(),
                    components = listOf(component("drive-01", "Drive")),
                    ports = listOf(invalidPort),
                    connections = emptyList(),
                ),
            ),
        )

        assertEquals(
            setOf(
                "connectable.port.direction.invalid",
                "connectable.port.interface.unknown",
                "connectable.port.multiplicity.invalid",
                "connectable.port.compatibility.duplicate",
            ),
            failure.diagnostics.map { it.code }.toSet(),
        )
    }

    @Test
    fun `unresolved connectable endpoints report both authored endpoint spans`() {
        val sourceSpan = SourceProvenance("sample.athena", 10, 5, 10, 22)
        val targetSpan = SourceProvenance("sample.athena", 10, 26, 10, 44)
        val unresolved = EngineeringConnection(
            id = StableSemanticIdentity("connection:sample:missing"),
            from = EngineeringReference(listOf("source", "missing"), null, sourceSpan),
            to = EngineeringReference(listOf("target", "missing"), null, targetSpan),
            provenance = provenance(),
        )
        val failure = assertIs<ConnectableEntityContractCompilation.Failure>(
            ConnectableEntityContractCompiler().compile(
                EngineeringDocument(
                    system = system(),
                    components = listOf(component("source", "Source"), component("target", "Target")),
                    ports = emptyList(),
                    connections = listOf(unresolved),
                ),
            ),
        )

        assertEquals(
            listOf("connectable.connection.endpoint.unresolved", "connectable.connection.endpoint.unresolved"),
            failure.diagnostics.map { it.code },
        )
        assertEquals(listOf(sourceSpan, targetSpan), failure.diagnostics.map { it.provenance })
    }

    @Test
    fun `compiles a grouped connection network into stable member and junction facts`() {
        val shared = port("source", "out", "out", "power", "line")
        val branchA = port("branchA", "inA", "in", "power", "line")
        val branchB = port("branchB", "inB", "in", "power", "line")
        val junctionReference = EngineeringReference(
            authoredPath = listOf("supply_group", "out"),
            resolvedIdentity = shared.id,
            provenance = shared.provenance,
        )
        val document = EngineeringDocument(
            system = system(),
            components = listOf(component("source", "Source"), component("branchA", "BranchA"), component("branchB", "BranchB")),
            ports = listOf(shared, branchA, branchB),
            connections = listOf(
                connection("feed_in", listOf("source", "out"), shared, listOf("branchA", "inA"), branchA),
                connection("relay_supply", listOf("source", "out"), shared, listOf("branchB", "inB"), branchB),
            ),
            connectionNetworks = listOf(
                EngineeringConnectionNetwork(
                    id = StableSemanticIdentity("network:sample:supply_group#1"),
                    name = "supply_group",
                    members = listOf(
                        EngineeringConnectionNetworkMember(
                            connectionReference = EngineeringReference(
                                authoredPath = listOf("supply_group", "feed_in"),
                                resolvedIdentity = StableSemanticIdentity("connection:sample:feed_in"),
                                provenance = provenance(),
                            ),
                            fromPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                            toPortReference = EngineeringReference(listOf("branchA", "inA"), branchA.id, branchA.provenance),
                        ),
                        EngineeringConnectionNetworkMember(
                            connectionReference = EngineeringReference(
                                authoredPath = listOf("supply_group", "relay_supply"),
                                resolvedIdentity = StableSemanticIdentity("connection:sample:relay_supply"),
                                provenance = provenance(),
                            ),
                            fromPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                            toPortReference = EngineeringReference(listOf("branchB", "inB"), branchB.id, branchB.provenance),
                        ),
                    ),
                    junctions = listOf(
                        EngineeringNetworkJunction(
                            id = StableSemanticIdentity("junction:sample:supply_group#1:port:source.out"),
                            sharedPortReference = junctionReference,
                            memberConnectionReferences = listOf(
                                EngineeringReference(listOf("supply_group", "feed_in"), StableSemanticIdentity("connection:sample:feed_in"), provenance()),
                                EngineeringReference(listOf("supply_group", "relay_supply"), StableSemanticIdentity("connection:sample:relay_supply"), provenance()),
                            ),
                            provenance = provenance(),
                        ),
                    ),
                    compatibilityEvidence = listOf(
                        EngineeringNetworkCompatibilityEvidence("member-count", "2", provenance()),
                        EngineeringNetworkCompatibilityEvidence("shared-direction", "out", shared.provenance),
                        EngineeringNetworkCompatibilityEvidence("shared-signal", "power", shared.provenance),
                        EngineeringNetworkCompatibilityEvidence("shared-role", "line", shared.provenance),
                    ),
                    provenance = provenance(),
                ),
            ),
        )

        val result = assertIs<ConnectableEntityContractCompilation.Success>(ConnectableEntityContractCompiler().compile(document))
        val network = result.networks.single()
        assertEquals("network:sample:supply_group#1", network.id.value)
        assertEquals("supply_group", network.name)
        assertEquals(2, network.members.size)
        assertEquals("connection:sample:feed_in", network.members[0].connectionId.value)
        assertEquals("port:source.out", network.junctions.single().sharedPortId.value)
        assertEquals(listOf("member-count", "shared-direction", "shared-signal", "shared-role"), network.compatibilityEvidence.map { it.kind })
    }

    @Test
    fun `rejects an unresolved member and an incompatible junction`() {
        val shared = port("source", "out", "out", "power", "line")
        val branchA = port("branchA", "inA", "in", "power", "line")
        val branchBIn = port("branchB", "inB", "in", "power", "line")
        val branchBOut = port("branchB", "outB", "out", "power", "line")
        val branchBLegacy = port("branchC", "inC", "in", "power", "line")
        val unresolvedGroup = EngineeringConnectionNetwork(
            id = StableSemanticIdentity("network:sample:unresolved#1"),
            name = "unresolved",
            members = listOf(
                EngineeringConnectionNetworkMember(
                    connectionReference = EngineeringReference(listOf("unresolved", "missing"), StableSemanticIdentity("connection:sample:missing"), provenance()),
                    fromPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                    toPortReference = EngineeringReference(listOf("branchA", "inA"), branchA.id, branchA.provenance),
                ),
            ),
            junctions = emptyList(),
            compatibilityEvidence = listOf(EngineeringNetworkCompatibilityEvidence("member-count", "1", provenance())),
            provenance = provenance(),
        )
        val disconnectedGroup = EngineeringConnectionNetwork(
            id = StableSemanticIdentity("network:sample:disconnected#1"),
            name = "disconnected",
            members = listOf(
                EngineeringConnectionNetworkMember(
                    connectionReference = EngineeringReference(listOf("disconnected", "feed_in"), StableSemanticIdentity("connection:sample:feed_in"), provenance()),
                    fromPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                    toPortReference = EngineeringReference(listOf("branchA", "inA"), branchA.id, branchA.provenance),
                ),
                EngineeringConnectionNetworkMember(
                    connectionReference = EngineeringReference(listOf("disconnected", "branch_local"), StableSemanticIdentity("connection:sample:branch_local"), provenance()),
                    fromPortReference = EngineeringReference(listOf("branchB", "outB"), branchBOut.id, branchBOut.provenance),
                    toPortReference = EngineeringReference(listOf("branchB", "inB"), branchBIn.id, branchBIn.provenance),
                ),
            ),
            junctions = emptyList(),
            compatibilityEvidence = listOf(EngineeringNetworkCompatibilityEvidence("member-count", "2", provenance())),
            provenance = provenance(),
        )
        val incompatibleGroup = EngineeringConnectionNetwork(
            id = StableSemanticIdentity("network:sample:incompatible#1"),
            name = "incompatible",
            members = listOf(
                EngineeringConnectionNetworkMember(
                    connectionReference = EngineeringReference(listOf("incompatible", "feed_in"), StableSemanticIdentity("connection:sample:feed_in"), provenance()),
                    fromPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                    toPortReference = EngineeringReference(listOf("branchA", "inA"), branchA.id, branchA.provenance),
                ),
                EngineeringConnectionNetworkMember(
                    connectionReference = EngineeringReference(listOf("incompatible", "relay_supply"), StableSemanticIdentity("connection:sample:relay_supply"), provenance()),
                    fromPortReference = EngineeringReference(listOf("branchB", "outB"), branchBOut.id, branchBOut.provenance),
                    toPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                ),
            ),
            junctions = listOf(
                EngineeringNetworkJunction(
                    id = StableSemanticIdentity("junction:sample:incompatible#1:port:source.out"),
                    sharedPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                    memberConnectionReferences = listOf(
                        EngineeringReference(listOf("incompatible", "feed_in"), StableSemanticIdentity("connection:sample:feed_in"), provenance()),
                        EngineeringReference(listOf("incompatible", "relay_supply"), StableSemanticIdentity("connection:sample:relay_supply"), provenance()),
                    ),
                    provenance = provenance(),
                ),
            ),
            compatibilityEvidence = listOf(
                EngineeringNetworkCompatibilityEvidence("member-count", "2", provenance()),
                EngineeringNetworkCompatibilityEvidence("shared-direction", "out", shared.provenance),
            ),
            provenance = provenance(),
        )
        val document = EngineeringDocument(
            system = system(),
            components = listOf(
                component("source", "Source"),
                component("branchA", "BranchA"),
                component("branchB", "BranchB"),
                component("branchC", "BranchC"),
            ),
            ports = listOf(shared, branchA, branchBIn, branchBOut, branchBLegacy),
            connections = listOf(
                connection("feed_in", listOf("source", "out"), shared, listOf("branchA", "inA"), branchA),
                connection("branch_local", listOf("branchB", "outB"), branchBOut, listOf("branchB", "inB"), branchBIn),
                connection("relay_supply", listOf("source", "out"), shared, listOf("branchC", "inC"), branchBLegacy),
            ),
            connectionNetworks = listOf(unresolvedGroup, disconnectedGroup, incompatibleGroup),
        )

        val failure = assertIs<ConnectableEntityContractCompilation.Failure>(ConnectableEntityContractCompiler().compile(document))
        assertEquals(
            listOf(
                "connectable.network.member.connection.unresolved",
                "connectable.network.members.disconnected",
                "connectable.network.junction.incompatible",
            ),
            failure.diagnostics.map { it.code }.distinct(),
        )
    }

    @Test
    fun `rejects junction members outside their network and disconnected from the shared port`() {
        val shared = port("source", "out", "out", "power", "line")
        val branchA = port("branchA", "inA", "in", "power", "line")
        val branchB = port("branchB", "inB", "in", "power", "line")
        val outsideSource = port("outside", "out", "out", "power", "line")
        val feed = connection("feed", listOf("source", "out"), shared, listOf("branchA", "inA"), branchA)
        val outside = connection("outside", listOf("outside", "out"), outsideSource, listOf("branchB", "inB"), branchB)
        val network = EngineeringConnectionNetwork(
            id = StableSemanticIdentity("network:sample:invalid-membership#1"),
            name = "invalid-membership",
            members = listOf(networkMember("invalid-membership", feed, shared, branchA)),
            junctions = listOf(
                EngineeringNetworkJunction(
                    id = StableSemanticIdentity("junction:sample:invalid-membership#1:${shared.id.value}"),
                    sharedPortReference = EngineeringReference(listOf("source", "out"), shared.id, shared.provenance),
                    memberConnectionReferences = listOf(
                        EngineeringReference(listOf("invalid-membership", "feed"), feed.id, feed.provenance),
                        EngineeringReference(listOf("invalid-membership", "outside"), outside.id, outside.provenance),
                    ),
                    provenance = provenance(),
                ),
            ),
            compatibilityEvidence = listOf(EngineeringNetworkCompatibilityEvidence("member-count", "1", provenance())),
            provenance = provenance(),
        )

        val failure = assertIs<ConnectableEntityContractCompilation.Failure>(
            ConnectableEntityContractCompiler().compile(
                EngineeringDocument(
                    system = system(),
                    components = listOf(
                        component("source", "Source"),
                        component("branchA", "BranchA"),
                        component("branchB", "BranchB"),
                        component("outside", "Outside"),
                    ),
                    ports = listOf(shared, branchA, branchB, outsideSource),
                    connections = listOf(feed, outside),
                    connectionNetworks = listOf(network),
                ),
            ),
        )

        assertTrue(failure.diagnostics.any { it.code == "connectable.network.junction.member.outside-network" })
    }

    private fun system() = EngineeringSystem(
        id = StableSemanticIdentity("system:sample"),
        name = "sample",
        provenance = provenance(),
    )

    private fun component(name: String, kind: String) = EngineeringComponent(
        id = StableSemanticIdentity("component:$name"),
        name = name,
        kind = kind,
        properties = listOf(
            property("connectable", "enabled"),
            property("interface", "power-input"),
        ),
        provenance = provenance(),
    )

    private fun legacyComponent(name: String, kind: String) = EngineeringComponent(
        id = StableSemanticIdentity("component:$name"),
        name = name,
        kind = kind,
        properties = emptyList(),
        provenance = provenance(),
    )

    private fun port(
        owner: String,
        name: String,
        direction: String,
        signal: String,
        role: String,
        multiplicity: String = "single",
    ) = EngineeringPort(
        id = StableSemanticIdentity("port:$owner.$name"),
        ownerReference = EngineeringReference(
            authoredPath = listOf(owner),
            resolvedIdentity = StableSemanticIdentity("component:$owner"),
            provenance = provenance(),
        ),
        name = name,
        properties = listOf(
            property("direction", direction),
            property("signal", signal),
            property("role", role),
            property("interface", "power-input"),
            property("multiplicity", multiplicity),
            property("compatibility.voltage", "400V"),
        ),
        provenance = SourceProvenance("sample.athena", if (owner == "source") 2 else 3, 1, if (owner == "source") 2 else 3, 10),
    )

    private fun property(name: String, value: String) = EngineeringProperty(
        name = name,
        value = EngineeringPropertyValue.Symbol(value),
    )

    private fun connection(alias: String, source: EngineeringPort, target: EngineeringPort) =
        connection(alias, listOf("source", "out"), source, listOf("target", "in"), target)

    private fun connection(
        alias: String,
        fromPath: List<String>,
        source: EngineeringPort,
        toPath: List<String>,
        target: EngineeringPort,
    ) = EngineeringConnection(
        id = StableSemanticIdentity("connection:sample:$alias"),
        from = EngineeringReference(fromPath, source.id, source.provenance),
        to = EngineeringReference(toPath, target.id, target.provenance),
        provenance = provenance(),
    )

    private fun networkMember(
        network: String,
        connection: EngineeringConnection,
        source: EngineeringPort,
        target: EngineeringPort,
    ) = EngineeringConnectionNetworkMember(
        connectionReference = EngineeringReference(listOf(network, connection.id.value.substringAfterLast(':')), connection.id, connection.provenance),
        fromPortReference = EngineeringReference(connection.from.authoredPath, source.id, source.provenance),
        toPortReference = EngineeringReference(connection.to.authoredPath, target.id, target.provenance),
    )

    private fun provenance() = SourceProvenance("sample.athena", 1, 1, 1, 10)
}
