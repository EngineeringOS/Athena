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
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EngineeringConnectivityContractCompilerTest {
    @Test
    fun `derives stable generic port contracts from canonical engineering IR`() {
        val result = EngineeringConnectivityContractCompiler().compile(
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

        val success = assertIs<EngineeringConnectivityCompilation.Success>(result)
        val contract = success.contracts.single()
        assertEquals("component:drive-01", contract.id.value)
        assertEquals("Drive", contract.kind)
        assertEquals(setOf("power-input"), contract.interfaces.map { it.id.value }.toSet())
        assertEquals(EngineeringConnectivityPortDirection.INPUT, contract.ports[0].compatibility.direction)
        assertEquals(EngineeringConnectivityPortMultiplicity.SINGLE, contract.ports[0].compatibility.multiplicity)
        assertEquals(listOf(EngineeringConnectivityInterfaceId("power-input")), contract.ports[0].interfaceIds)
        assertEquals("power", contract.ports[0].compatibility.signalKind)
        assertEquals("line", contract.ports[0].compatibility.role)
        assertEquals("400V", contract.ports[0].compatibility.parameters["voltage"])
        assertEquals(EngineeringConnectivityConstraintOwner.SEMANTIC, contract.ports[0].compatibility.owner)
        assertEquals(EngineeringConnectivityConstraintStrength.REQUIRED, contract.ports[0].compatibility.strength)
        assertEquals("protective-earth", contract.ports[1].compatibility.signalKind)
        assertEquals(emptyList<EngineeringConnectivityPhysicalReference>(), contract.physicalInstallationReferences)
        assertEquals(emptyList<EngineeringConnectivityRepresentationReference>(), contract.representationBindings)
        assertEquals(emptyList<EngineeringConnectivityEvidenceReference>(), contract.externalEvidenceReferences)
    }

    @Test
    fun `legacy connectable marker does not opt a component into engineering connectivity`() {
        val legacy = component("legacy", "Legacy").copy(
            properties = listOf(property("connectable", "enabled")),
        )

        val result = assertIs<EngineeringConnectivityCompilation.Success>(
            EngineeringConnectivityContractCompiler().compile(
                EngineeringDocument(
                    system = system(),
                    components = listOf(legacy),
                    ports = emptyList(),
                    connections = emptyList(),
                ),
            ),
        )

        assertTrue(result.contracts.isEmpty())
    }

    @Test
    fun `component level interface property does not create connectivity interface membership`() {
        val result = assertIs<EngineeringConnectivityCompilation.Success>(
            EngineeringConnectivityContractCompiler().compile(
                EngineeringDocument(
                    system = system(),
                    components = listOf(component("drive-01", "Drive")),
                    ports = listOf(
                        port("drive-01", "L1", "input", "power", "line").copy(
                            properties = listOf(
                                property("direction", "input"),
                                property("signal", "power"),
                                property("role", "line"),
                            ),
                        ),
                    ),
                    connections = emptyList(),
                ),
            ),
        )

        val contract = result.contracts.single()
        assertTrue(contract.interfaces.isEmpty())
        assertTrue(contract.ports.single().interfaceIds.isEmpty())
    }

    @Test
    fun `rejects duplicate connectivity port identity with source provenance`() {
        val result = EngineeringConnectivityContractCompiler().compile(
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

        val failure = assertIs<EngineeringConnectivityCompilation.Failure>(result)
        assertEquals("connectivity.port.duplicate", failure.diagnostics.single().code)
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

        val valid = assertIs<EngineeringConnectivityCompilation.Success>(EngineeringConnectivityContractCompiler().compile(document))
        assertEquals(listOf("connection:sample:power"), valid.connections.map { it.id.value })
        assertEquals(source.id, valid.connections.single().from.port.targetId)
        assertEquals(target.id, valid.connections.single().to.port.targetId)

        val incompatible = document.copy(
            ports = listOf(source, port("target", "in", "out", "power", "line")),
        )
        val diagnostics = assertIs<EngineeringConnectivityCompilation.Failure>(
            EngineeringConnectivityContractCompiler().compile(incompatible),
        ).diagnostics
        assertEquals(listOf("connectivity.connection.direction.incompatible", "connectivity.connection.direction.incompatible"), diagnostics.map { it.code })
        assertEquals(listOf("port:source.out", "port:target.in"), diagnostics.map { diagnostic ->
            if (diagnostic.provenance == source.provenance) source.id.value else target.id.value
        })
    }

    @Test
    fun `rejects connection between connectivity and untyped port`() {
        val source = port("source", "out", "out", "power", "line")
        val target = port("legacy", "in", "in", "power", "line")
        val document = EngineeringDocument(
            system = system(),
            components = listOf(component("source", "Source"), legacyComponent("legacy", "Legacy")),
            ports = listOf(source, target),
            connections = listOf(connection("legacy", source, target)),
        )

        val failure = assertIs<EngineeringConnectivityCompilation.Failure>(
            EngineeringConnectivityContractCompiler().compile(document),
        )
        assertEquals(
            listOf("connectivity.connection.endpoint.untyped", "connectivity.connection.endpoint.untyped"),
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
        val failure = assertIs<EngineeringConnectivityCompilation.Failure>(
            EngineeringConnectivityContractCompiler().compile(
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
                "connectivity.port.direction.invalid",
                "connectivity.port.multiplicity.invalid",
                "connectivity.port.compatibility.duplicate",
            ),
            failure.diagnostics.map { it.code }.toSet(),
        )
    }

    @Test
    fun `attaches typed external evidence without creating connectivity facts`() {
        val document = EngineeringDocument(
            system = system(),
            components = listOf(component("drive-01", "Drive")),
            ports = listOf(port("drive-01", "L1", "input", "power", "line")),
            connections = emptyList(),
            externalEvidence = listOf(
                com.engineeringood.athena.ir.EngineeringExternalEvidenceMapping(
                    name = "DriveEvidence",
                    namespace = "iec",
                    reference = "IEC:60204-1:clause-13",
                    subject = com.engineeringood.athena.ir.EngineeringExternalEvidenceSubject(
                        kind = com.engineeringood.athena.ir.EngineeringExternalEvidenceSubjectKind.CONTRACT,
                        authoredPath = listOf("drive-01"),
                    ),
                    externalProvenance = "IEC citation",
                    provenance = provenance(),
                ),
            ),
        )

        val success = assertIs<EngineeringConnectivityCompilation.Success>(
            EngineeringConnectivityContractCompiler().compile(document),
        )

        val evidence = success.externalEvidence.single()
        assertEquals(EngineeringConnectivityEvidenceNamespace.IEC, evidence.namespace)
        assertEquals("IEC:60204-1:clause-13", evidence.reference.value)
        assertEquals(EngineeringConnectivityEvidenceSubjectKind.CONTRACT, evidence.subject.kind)
        assertEquals("component:drive-01", evidence.subject.targetId?.value)
        assertEquals("IEC citation", evidence.externalProvenance)
        assertEquals(1, success.contracts.size)
        assertTrue(success.connections.isEmpty())
        assertTrue(success.networks.isEmpty())
    }

    @Test
    fun `rejects invalid external evidence without accepting external authority`() {
        val document = EngineeringDocument(
            system = system(),
            components = listOf(component("drive-01", "Drive")),
            ports = listOf(port("drive-01", "L1", "input", "power", "line")),
            connections = emptyList(),
            externalEvidence = listOf(
                externalEvidence("BadNamespace", "aml", "AML:drive", "contract", listOf("drive-01")),
                externalEvidence("BadReference", "iec", "not-iec", "port", listOf("drive-01", "L1")),
                externalEvidence("DuplicateA", "classification", "neutral:drive", "contract", listOf("drive-01")),
                externalEvidence("DuplicateB", "classification", "neutral:drive", "contract", listOf("drive-01")),
                externalEvidence("MissingPort", "iec", "IEC:60204-1:missing", "port", listOf("drive-01", "missing")),
            ),
        )

        val failure = assertIs<EngineeringConnectivityCompilation.Failure>(
            EngineeringConnectivityContractCompiler().compile(document),
        )

        val codes = failure.diagnostics.map { it.code }.toSet()
        assertContains(codes, "connectivity.evidence.namespace.unknown")
        assertContains(codes, "connectivity.evidence.reference.invalid")
        assertContains(codes, "connectivity.evidence.duplicate")
        assertContains(codes, "connectivity.evidence.subject.invalid")
    }

    @Test
    fun `unresolved connectivity endpoints report both authored endpoint spans`() {
        val sourceSpan = SourceProvenance("sample.athena", 10, 5, 10, 22)
        val targetSpan = SourceProvenance("sample.athena", 10, 26, 10, 44)
        val unresolved = EngineeringConnection(
            id = StableSemanticIdentity("connection:sample:missing"),
            from = EngineeringReference(listOf("source", "missing"), null, sourceSpan),
            to = EngineeringReference(listOf("target", "missing"), null, targetSpan),
            provenance = provenance(),
        )
        val failure = assertIs<EngineeringConnectivityCompilation.Failure>(
            EngineeringConnectivityContractCompiler().compile(
                EngineeringDocument(
                    system = system(),
                    components = listOf(component("source", "Source"), component("target", "Target")),
                    ports = emptyList(),
                    connections = listOf(unresolved),
                ),
            ),
        )

        assertEquals(
            listOf("connectivity.connection.endpoint.unresolved", "connectivity.connection.endpoint.unresolved"),
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

        val result = assertIs<EngineeringConnectivityCompilation.Success>(EngineeringConnectivityContractCompiler().compile(document))
        val network = result.networks.single()
        assertEquals("network:sample:supply_group#1", network.id.value)
        assertEquals("supply_group", network.name)
        assertEquals(2, network.members.size)
        assertEquals("connection:sample:feed_in", network.members[0].connection.targetId.value)
        assertEquals("port:source.out", network.junctions.single().sharedPort.targetId.value)
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

        val failure = assertIs<EngineeringConnectivityCompilation.Failure>(EngineeringConnectivityContractCompiler().compile(document))
        assertEquals(
            listOf(
                "connectivity.network.member.connection.unresolved",
                "connectivity.network.members.disconnected",
                "connectivity.network.junction.incompatible",
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

        val failure = assertIs<EngineeringConnectivityCompilation.Failure>(
            EngineeringConnectivityContractCompiler().compile(
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

        assertTrue(failure.diagnostics.any { it.code == "connectivity.network.junction.member.outside-network" })
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
            property("connectivity", "enabled"),
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

    private fun externalEvidence(
        name: String,
        namespace: String,
        reference: String,
        subjectKind: String,
        subjectPath: List<String>,
    ) = com.engineeringood.athena.ir.EngineeringExternalEvidenceMapping(
        name = name,
        namespace = namespace,
        reference = reference,
        subject = com.engineeringood.athena.ir.EngineeringExternalEvidenceSubject(
            kind = com.engineeringood.athena.ir.EngineeringExternalEvidenceSubjectKind.valueOf(subjectKind.uppercase()),
            authoredPath = subjectPath,
        ),
        externalProvenance = "test",
        provenance = provenance(),
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
        properties = emptyList(),
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
