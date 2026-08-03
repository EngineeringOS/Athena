package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.ir.EngineeringConnectionNetwork
import com.engineeringood.athena.ir.EngineeringConnectionNetworkMember
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringExternalEvidenceMapping
import com.engineeringood.athena.ir.EngineeringExternalEvidenceSubject
import com.engineeringood.athena.ir.EngineeringExternalEvidenceSubjectKind
import com.engineeringood.athena.ir.EngineeringFunction
import com.engineeringood.athena.ir.EngineeringFunctionRole
import com.engineeringood.athena.ir.EngineeringNetworkCompatibilityEvidence
import com.engineeringood.athena.ir.EngineeringNetworkJunction
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProjectionForbiddenTruth
import com.engineeringood.athena.ir.EngineeringProjectionConstruct
import com.engineeringood.athena.ir.EngineeringProjectionGrid
import com.engineeringood.athena.ir.EngineeringProjectionPolicy
import com.engineeringood.athena.ir.EngineeringProjectionRegion
import com.engineeringood.athena.ir.EngineeringProjectionSheet
import com.engineeringood.athena.ir.EngineeringProjectionView
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.connection.EngineeringConnectivityCompilation
import com.engineeringood.athena.language.ConnectionGroupDeclaration
import com.engineeringood.athena.language.ExternalEvidenceDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.ProjectionPolicyDeclaration
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.RelationDeclaration
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.ViewDeclaration
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory

/** Lowers the syntax-only AST into the first canonical Engineering IR document.
 *
 * Declaration classification itself is performed by domain plugins via exhaustive `when`
 * over Athena's sealed [com.engineeringood.athena.language.Declaration] hierarchy (not by
 * parse-tree types). Adding a future sealed variant must break those plugin `when` sites
 * and the Story `1.3` extensibility tests at compile time.
 */
class EngineeringIrLowerer(
    private val domainSemantics: AthenaDomainSemanticsCoordinator = AthenaDomainSemanticsCoordinator(
        AthenaApprovedPluginInventory.EMPTY,
    ),
) {
    /** Lowers [source] deterministically into the canonical semantic document used by later compiler passes.
     *
     * Lowering-continuity guardrail: the only legal input is the authored `SourceFileAst`
     * carried by [CompilerSourceDocument] (read directly as `source.ast.system` and, through
     * [AthenaDomainSemanticsCoordinator], as `SourceFileAst.declarations`). This function must never be
     * changed to accept or read an ANTLR4 parse-tree/visitor result (Epic 2) or a Tree-sitter CST node
     * (Epic 3) directly. Parser migration must preserve the same canonical `EngineeringDocument` shape
     * (identity scheme `system:`/`component:`/`port:`/`connection:` and `SourceProvenance` mapping) for
     * the current supported syntax subset, as pinned by the parser parity regression tests.
     */
    fun lower(source: CompilerSourceDocument, sourceUnitId: String = source.file): EngineeringDocument {
        val contribution = domainSemantics.lower(source)
        val portableSourceUnitId = sourceUnitId.toPortableSourceUnitId()

        val components = contribution.components.withDuplicateOrdinals { it.name }.map { (blueprint, duplicateOrdinal) ->
            EngineeringComponent(
                id = componentIdentity(blueprint.name, duplicateOrdinal),
                name = blueprint.name,
                kind = blueprint.kind,
                properties = blueprint.properties,
                provenance = blueprint.provenance,
            )
        }
        val componentIdsByAuthoredPath = components.uniqueResolutionMap(keySelector = { it.name }, idSelector = { it.id })

        val ports = contribution.ports.withDuplicateOrdinals { pathKey(it.ownerPath + it.name) }.map { (blueprint, duplicateOrdinal) ->
            EngineeringPort(
                id = portIdentity(blueprint.ownerPath + blueprint.name, duplicateOrdinal),
                ownerReference = EngineeringReference(
                    authoredPath = blueprint.ownerPath,
                    resolvedIdentity = componentIdsByAuthoredPath[pathKey(blueprint.ownerPath)],
                    provenance = blueprint.ownerProvenance,
                ),
                name = blueprint.name,
                properties = blueprint.properties,
                provenance = blueprint.provenance,
            )
        }
        val portIdsByAuthoredPath = ports.uniqueResolutionMap(
            keySelector = { pathKey(it.ownerReference.authoredPath + it.name) },
            idSelector = { it.id },
        )
        val portsById = ports.associateBy { it.id }

        val connections = contribution.connections.map { blueprint ->
            EngineeringConnection(
                id = connectionIdentity(portableSourceUnitId, blueprint.alias),
                from = EngineeringReference(
                    authoredPath = blueprint.fromPath,
                    resolvedIdentity = portIdsByAuthoredPath[pathKey(blueprint.fromPath)],
                    provenance = blueprint.fromProvenance,
                ),
                to = EngineeringReference(
                    authoredPath = blueprint.toPath,
                    resolvedIdentity = portIdsByAuthoredPath[pathKey(blueprint.toPath)],
                    provenance = blueprint.toProvenance,
                ),
                provenance = blueprint.provenance,
                properties = blueprint.properties,
            )
        }
        val connectionsByAlias = contribution.connections.zip(connections).associate { (blueprint, connection) ->
            blueprint.alias to connection
        }
        val connectionGroupNetworks = source.ast.declarations
            .filterIsInstance<ConnectionGroupDeclaration>()
            .withDuplicateOrdinals { it.name }
            .map { (group, duplicateOrdinal) ->
                val members = group.connections.mapNotNull { connectionDeclaration ->
                    val connection = connectionsByAlias[connectionDeclaration.alias] ?: return@mapNotNull null
                    EngineeringConnectionNetworkMember(
                        connectionReference = EngineeringReference(
                            authoredPath = listOf(group.name, connectionDeclaration.alias),
                            resolvedIdentity = connection.id,
                            provenance = connectionDeclaration.span.toProvenance(source.file),
                        ),
                        fromPortReference = connection.from,
                        toPortReference = connection.to,
                    )
                }
                val sharedPortKeys = members
                    .flatMap { member -> listOf(member.fromPortReference.identityKey(), member.toPortReference.identityKey()) }
                    .groupingBy { it }
                    .eachCount()
                    .filterValues { count -> count >= 2 }
                    .keys
                    .sorted()
                val sharedPortReferences = sharedPortKeys.mapNotNull { key ->
                    members.asSequence()
                        .flatMap { member -> sequenceOf(member.fromPortReference, member.toPortReference) }
                        .firstOrNull { reference -> reference.identityKey() == key }
                }
                val junctions = if (members.size < 2) {
                    emptyList()
                } else {
                    sharedPortReferences.map { sharedPortReference ->
                        val memberConnectionReferences = members
                            .filter { member ->
                                member.fromPortReference.identityKey() == sharedPortReference.identityKey() ||
                                    member.toPortReference.identityKey() == sharedPortReference.identityKey()
                            }
                            .map { member -> member.connectionReference }
                        EngineeringNetworkJunction(
                            id = StableSemanticIdentity(
                                "junction:$portableSourceUnitId:${group.name}#${duplicateOrdinal}:${sharedPortReference.identityKey()}",
                            ),
                            sharedPortReference = sharedPortReference,
                            memberConnectionReferences = memberConnectionReferences,
                            provenance = group.span.toProvenance(source.file),
                        )
                    }
                }
                val compatibilityEvidence = buildList {
                    add(
                        EngineeringNetworkCompatibilityEvidence(
                            kind = "member-count",
                            value = members.size.toString(),
                            provenance = group.span.toProvenance(source.file),
                        ),
                    )
                    sharedPortReferences.firstOrNull()?.let { sharedPortReference ->
                        val sharedPort = sharedPortReference.resolvedIdentity?.let(portsById::get)
                        sharedPort?.let { port ->
                            val compatibility = port.compatibility()
                            add(
                                EngineeringNetworkCompatibilityEvidence(
                                    kind = "shared-direction",
                                    value = compatibility.direction,
                                    provenance = port.provenance,
                                ),
                            )
                            compatibility.signalKind?.let { signal ->
                                add(
                                    EngineeringNetworkCompatibilityEvidence(
                                        kind = "shared-signal",
                                        value = signal,
                                        provenance = port.provenance,
                                    ),
                                )
                            }
                            compatibility.role?.let { role ->
                                add(
                                    EngineeringNetworkCompatibilityEvidence(
                                        kind = "shared-role",
                                        value = role,
                                        provenance = port.provenance,
                                    ),
                                )
                            }
                        }
                    }
                }
                EngineeringConnectionNetwork(
                    id = StableSemanticIdentity("network:$portableSourceUnitId:${group.name}#${duplicateOrdinal}"),
                    name = group.name,
                    members = members,
                    junctions = junctions,
                    compatibilityEvidence = compatibilityEvidence,
                    provenance = group.span.toProvenance(source.file),
                )
            }
        val relationNetworks = source.ast.declarations
            .filterIsInstance<RelationDeclaration>()
            .filter { relation -> relation.targets.size > 1 }
            .withDuplicateOrdinals { relation -> relationNetworkName(relation.word.value, relation.from) }
            .mapNotNull { (relation, duplicateOrdinal) ->
                val members = relation.targets.map { target ->
                    val connection = connectionsByAlias[relationMemberAlias(relation.word.value, relation.from, target)]
                        ?: return@mapNotNull null
                    EngineeringConnectionNetworkMember(
                        connectionReference = EngineeringReference(
                            authoredPath = listOf(relationNetworkName(relation.word.value, relation.from), connection.id.value.substringAfterLast(':')),
                            resolvedIdentity = connection.id,
                            provenance = relation.span.toProvenance(source.file),
                        ),
                        fromPortReference = connection.from,
                        toPortReference = connection.to,
                    )
                }
                EngineeringConnectionNetwork(
                    id = StableSemanticIdentity(
                        "network:$portableSourceUnitId:${relationNetworkName(relation.word.value, relation.from)}#$duplicateOrdinal",
                    ),
                    name = relationNetworkName(relation.word.value, relation.from),
                    members = members,
                    junctions = emptyList(),
                    compatibilityEvidence = listOf(
                        EngineeringNetworkCompatibilityEvidence(
                            kind = "member-count",
                            value = members.size.toString(),
                            provenance = relation.span.toProvenance(source.file),
                        ),
                    ),
                    provenance = relation.span.toProvenance(source.file),
                    properties = listOf(
                        EngineeringProperty(
                            name = "relation.kind",
                            value = EngineeringPropertyValue.Symbol(relation.word.value),
                        ),
                    ),
                )
            }

        val functions = contribution.functions.withDuplicateOrdinals {
            pathKey(it.ownerPath + it.name)
        }.map { (blueprint, duplicateOrdinal) ->
            EngineeringFunction(
                id = functionIdentity(blueprint.ownerPath, blueprint.name, duplicateOrdinal),
                ownerReference = EngineeringReference(
                    authoredPath = blueprint.ownerPath,
                    resolvedIdentity = componentIdsByAuthoredPath[pathKey(blueprint.ownerPath)],
                    provenance = blueprint.ownerProvenance,
                ),
                name = blueprint.name,
                role = EngineeringFunctionRole(blueprint.role),
                portReferences = blueprint.portReferences.map { reference ->
                    EngineeringReference(
                        authoredPath = reference.path,
                        resolvedIdentity = portIdsByAuthoredPath[pathKey(reference.path)],
                        provenance = reference.provenance,
                    )
                },
                provenance = blueprint.provenance,
            )
        }

        return EngineeringDocument(
            system = EngineeringSystem(
                id = systemIdentity(source.ast.system.name),
                name = source.ast.system.name,
                provenance = source.ast.system.span.toProvenance(source.file),
            ),
            components = components,
            ports = ports,
            connections = connections,
            functions = functions,
            connectionNetworks = connectionGroupNetworks + relationNetworks,
            externalEvidence = source.ast.declarations
                .filterIsInstance<ExternalEvidenceDeclaration>()
                .map { evidence -> evidence.toExternalEvidence(source.file) },
            projectionPolicies = source.ast.declarations
                .filterIsInstance<ProjectionPolicyDeclaration>()
                .map { policy -> policy.toProjectionPolicy(source.file) },
            projectionViews = source.ast.declarations
                .filterIsInstance<ViewDeclaration>()
                .map { view -> view.toProjectionView(source.file) },
        )
    }

    /** Lowers validated canonical connectivity into compiler-owned transient Connection IR. */
    fun lowerConnectionIr(
        connectivity: EngineeringConnectivityCompilation.Success,
        snapshot: ConnectionIrSnapshot,
    ): ConnectionIr {
        return ConnectionIrLowerer().lower(connectivity, snapshot)
    }

    private fun systemIdentity(name: String): StableSemanticIdentity = StableSemanticIdentity("system:$name")

    private fun componentIdentity(name: String, duplicateOrdinal: Int): StableSemanticIdentity {
        return StableSemanticIdentity(withDuplicateSuffix("component:$name", duplicateOrdinal))
    }

    private fun portIdentity(path: List<String>, duplicateOrdinal: Int): StableSemanticIdentity {
        return StableSemanticIdentity(withDuplicateSuffix("port:${pathKey(path)}", duplicateOrdinal))
    }

    private fun connectionIdentity(sourceUnitId: String, alias: String): StableSemanticIdentity =
        StableSemanticIdentity("connection:$sourceUnitId:$alias")

    private fun functionIdentity(owner: List<String>, name: String, duplicateOrdinal: Int): StableSemanticIdentity =
        StableSemanticIdentity(withDuplicateSuffix("function:${pathKey(owner + name)}", duplicateOrdinal))

    private fun pathKey(parts: List<String>): String = parts.joinToString(".")

    private fun EngineeringReference.identityKey(): String {
        return resolvedIdentity?.value ?: authoredPath.joinToString(".")
    }

    private fun withDuplicateSuffix(baseIdentity: String, duplicateOrdinal: Int): String {
        return if (duplicateOrdinal == 1) baseIdentity else "$baseIdentity#$duplicateOrdinal"
    }
}

private fun String.toPortableSourceUnitId(): String {
    val normalized = replace('\\', '/')
    val examplesIndex = normalized.indexOf("/examples/")
    if (examplesIndex >= 0) {
        return normalized.substring(examplesIndex + 1)
    }
    val srcIndex = normalized.indexOf("/src/")
    if (srcIndex >= 0) {
        return normalized.substring(srcIndex + 1)
    }
    return normalized
}

private fun relationMemberAlias(relationWord: String, from: QualifiedName, target: QualifiedName): String =
    "${relationWord}_${from.parts.joinToString("_")}_to_${target.parts.joinToString("_")}"

private fun relationNetworkName(relationWord: String, from: QualifiedName): String =
    "${relationWord}_${from.parts.joinToString("_")}"

/** Converts a syntax-layer span into stable provenance carried by canonical semantic objects. */
private fun SourceSpan.toProvenance(file: String): SourceProvenance {
    return SourceProvenance(
        file = file,
        startLine = start.line,
        startColumn = start.column,
        endLine = end.line,
        endColumn = end.column,
    )
}

private fun ExternalEvidenceDeclaration.toExternalEvidence(file: String): EngineeringExternalEvidenceMapping =
    EngineeringExternalEvidenceMapping(
        name = name,
        namespace = namespace.value,
        reference = reference.value,
        subject = EngineeringExternalEvidenceSubject(
            kind = when (subject.kind) {
                com.engineeringood.athena.language.ExternalEvidenceSubjectKind.CONTRACT ->
                    EngineeringExternalEvidenceSubjectKind.CONTRACT
                com.engineeringood.athena.language.ExternalEvidenceSubjectKind.INTERFACE ->
                    EngineeringExternalEvidenceSubjectKind.INTERFACE
                com.engineeringood.athena.language.ExternalEvidenceSubjectKind.PORT ->
                    EngineeringExternalEvidenceSubjectKind.PORT
                com.engineeringood.athena.language.ExternalEvidenceSubjectKind.RELATION_CONTRACT ->
                    EngineeringExternalEvidenceSubjectKind.RELATION_CONTRACT
                com.engineeringood.athena.language.ExternalEvidenceSubjectKind.ROUTE_POLICY ->
                    EngineeringExternalEvidenceSubjectKind.ROUTE_POLICY
            },
            authoredPath = subject.target.parts,
        ),
        externalProvenance = provenance.value,
        provenance = span.toProvenance(file),
    )

private fun ProjectionPolicyDeclaration.toProjectionPolicy(file: String): EngineeringProjectionPolicy =
    EngineeringProjectionPolicy(
        name = name,
        targetSurface = target?.value,
        layoutStrategy = layoutStrategy?.value,
        drawingProfile = drawingProfile?.value,
        routeQualityPolicy = routeQualityPolicy?.value,
        proofObligations = proofObligations.map { proof -> proof.value },
        forbiddenEngineeringTruth = forbiddenEngineeringTruth.map { truth ->
            EngineeringProjectionForbiddenTruth(
                kind = truth.kind,
                provenance = truth.span.toProvenance(file),
            )
        },
        provenance = span.toProvenance(file),
    )

private fun ViewDeclaration.toProjectionView(file: String): EngineeringProjectionView =
    EngineeringProjectionView(
        name = name,
        sheets = sheets.mapIndexed { index, sheet ->
            EngineeringProjectionSheet(
                name = sheet.name,
                order = index + 1,
                provenance = sheet.span.toProvenance(file),
            )
        },
        regions = regions.map { region ->
            EngineeringProjectionRegion(
                name = region.name,
                sheetName = sheets.lastOrNull { sheet -> sheet.span.start.offset < region.span.start.offset }?.name.orEmpty(),
                occurrences = region.occurrences,
                provenance = region.span.toProvenance(file),
            )
        },
        constructs = constructs.map { construct ->
            EngineeringProjectionConstruct(
                name = construct.name.orEmpty(),
                kind = construct.kind,
                sheetName = sheets.lastOrNull { sheet -> sheet.span.start.offset < construct.span.start.offset }?.name.orEmpty(),
                occurrences = construct.occurrences,
                provenance = construct.span.toProvenance(file),
            )
        },
        readingOrder = readingOrder,
        grid = grid?.let { declaredGrid ->
            EngineeringProjectionGrid(
                name = declaredGrid.name,
                rows = declaredGrid.rows,
                columns = declaredGrid.columns,
                provenance = declaredGrid.span.toProvenance(file),
            )
        },
        provenance = span.toProvenance(file),
    )

private fun List<PropertyAssignment>.toEngineeringProperties(): List<EngineeringProperty> {
    return map { assignment ->
        EngineeringProperty(
            name = assignment.name,
            value = when (val value = assignment.value) {
                is ScalarValue.Identifier -> EngineeringPropertyValue.Symbol(value.text)
                is ScalarValue.StringLiteral -> EngineeringPropertyValue.Text(value.text)
            },
        )
    }
}

/** Tags authored declarations deterministically when duplicate semantic keys occur in one source. */
private fun <T> List<T>.withDuplicateOrdinals(keySelector: (T) -> String): List<Pair<T, Int>> {
    val countsByKey = mutableMapOf<String, Int>()
    return map { value ->
        val key = keySelector(value)
        val duplicateOrdinal = countsByKey.getOrDefault(key, 0) + 1
        countsByKey[key] = duplicateOrdinal
        value to duplicateOrdinal
    }
}

/** Resolves authored paths only when they map to a single canonical semantic identity. */
private fun <T> List<T>.uniqueResolutionMap(
    keySelector: (T) -> String,
    idSelector: (T) -> StableSemanticIdentity,
): Map<String, StableSemanticIdentity> {
    return groupBy(keySelector)
        .mapNotNull { (key, values) ->
            values.singleOrNull()?.let { key to idSelector(it) }
        }
        .toMap()
}

private data class LoweredPortCompatibility(
    val direction: String,
    val signalKind: String?,
    val role: String?,
)

private fun EngineeringPort.compatibility(): LoweredPortCompatibility {
    val values = properties.symbolValuesByName()
    return LoweredPortCompatibility(
        direction = values["direction"]?.singleOrNull().orEmpty(),
        signalKind = values["signal"]?.singleOrNull(),
        role = values["role"]?.singleOrNull(),
    )
}

private fun List<EngineeringProperty>.symbolValuesByName(): Map<String, List<String>> =
    groupBy { it.name }.mapValues { (_, properties) ->
        properties.mapNotNull { (it.value as? EngineeringPropertyValue.Symbol)?.text }
    }
