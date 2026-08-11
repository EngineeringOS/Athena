package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringNet
import com.engineeringood.athena.ir.EngineeringConnectionSpecification
import com.engineeringood.athena.ir.ConnectionSpecificationScope
import com.engineeringood.athena.ir.ConnectionEndpoint
import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.ConnectionKind
import com.engineeringood.athena.ir.EngineeringEntity
import com.engineeringood.athena.ir.EngineeringEntityReference
import com.engineeringood.athena.ir.EngineeringExternalEvidenceMapping
import com.engineeringood.athena.ir.EngineeringExternalEvidenceSubject
import com.engineeringood.athena.ir.EngineeringExternalEvidenceSubjectKind
import com.engineeringood.athena.ir.EngineeringFunction
import com.engineeringood.athena.ir.EngineeringFunctionReference
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.ir.EngineeringPortOwner
import com.engineeringood.athena.ir.EngineeringProjectionForbiddenTruth
import com.engineeringood.athena.ir.EngineeringProjectionConstruct
import com.engineeringood.athena.ir.EngineeringProjectionGrid
import com.engineeringood.athena.ir.EngineeringProjectionPolicy
import com.engineeringood.athena.ir.EngineeringProjectionRegion
import com.engineeringood.athena.ir.EngineeringProjectionSheet
import com.engineeringood.athena.ir.EngineeringProjectionView
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringFlow
import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringPackageName
import com.engineeringood.athena.ir.EngineeringParticipant
import com.engineeringood.athena.ir.EngineeringRelationship
import com.engineeringood.athena.ir.EngineeringSubjectReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.ExternalEvidenceDeclaration
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.NetDeclaration
import com.engineeringood.athena.language.NetEndpointRole
import com.engineeringood.athena.language.ConnectionSpecificationDeclaration
import com.engineeringood.athena.language.ProjectionPolicyDeclaration
import com.engineeringood.athena.language.RelationDeclaration
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.ViewDeclaration
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaSourceDocument

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
     * (identity scheme `system:`/`entity:`/`function:`/`port:`/`connection:` and `SourceProvenance` mapping) for
     * the current supported syntax subset, as pinned by the parser parity regression tests.
     */
    fun lower(source: CompilerSourceDocument, sourceUnitId: String = source.file): EngineeringDocument {
        val contribution = domainSemantics.lower(source)
        val portableSourceUnitId = sourceUnitId.toPortableSourceUnitId()

        val entities = contribution.entities.withDuplicateOrdinals { it.name }.map { (blueprint, duplicateOrdinal) ->
            EngineeringEntity(
                id = entityIdentity(blueprint.name, duplicateOrdinal),
                name = blueprint.name,
                conceptReference = blueprint.conceptReference,
                properties = blueprint.properties,
                structureAssignments = blueprint.structureAssignments,
                provenance = blueprint.provenance,
            )
        }
        val entityIdsByAuthoredPath = entities.uniqueResolutionMap(keySelector = { it.name }, idSelector = { it.id })

        val functions = contribution.functions.withDuplicateOrdinals {
            pathKey(it.ownerPath + it.name)
        }.map { (blueprint, duplicateOrdinal) ->
            EngineeringFunction(
                id = functionIdentity(blueprint.ownerPath, blueprint.name, duplicateOrdinal),
                owner = EngineeringEntityReference(
                    EngineeringReference(
                        authoredPath = blueprint.ownerPath,
                        resolvedIdentity = entityIdsByAuthoredPath[pathKey(blueprint.ownerPath)],
                        provenance = blueprint.ownerProvenance,
                    ),
                ),
                name = blueprint.name,
                roleReference = blueprint.roleReference,
                properties = blueprint.properties,
                provenance = blueprint.provenance,
            )
        }
        val functionIdsByAuthoredPath = functions.uniqueResolutionMap(
            keySelector = { pathKey(it.owner.reference.authoredPath + it.name) },
            idSelector = { it.id },
        )

        val ports = contribution.ports.withDuplicateOrdinals { pathKey(it.ownerPath + it.name) }.map { (blueprint, duplicateOrdinal) ->
            val ownerReference = EngineeringReference(
                authoredPath = blueprint.ownerPath,
                resolvedIdentity = when (blueprint.ownerPath.size) {
                    1 -> entityIdsByAuthoredPath[pathKey(blueprint.ownerPath)]
                    2 -> functionIdsByAuthoredPath[pathKey(blueprint.ownerPath)]
                    else -> error("Port owner must be an Entity or Function path: ${blueprint.ownerPath.joinToString(".")}")
                },
                provenance = blueprint.ownerProvenance,
            )
            EngineeringPort(
                id = portIdentity(blueprint.ownerPath + blueprint.name, duplicateOrdinal),
                owner = when (blueprint.ownerPath.size) {
                    1 -> EngineeringPortOwner.Entity(EngineeringEntityReference(ownerReference))
                    2 -> EngineeringPortOwner.Function(EngineeringFunctionReference(ownerReference))
                    else -> error("Port owner must be an Entity or Function path: ${blueprint.ownerPath.joinToString(".")}")
                },
                name = blueprint.name,
                direction = blueprint.direction,
                admittedFlowReferences = blueprint.admittedFlowReferences,
                cardinality = blueprint.cardinality,
                interfaceDesignation = blueprint.interfaceDesignation,
                properties = blueprint.properties,
                provenance = blueprint.provenance,
            )
        }
        val portIdsByAuthoredPath = ports.uniqueResolutionMap(
            keySelector = { pathKey(it.owner.reference().authoredPath + it.name) },
            idSelector = { it.id },
        )
        val loweringContext = AthenaDomainLoweringContext(AthenaSourceDocument(source.file, source.ast))
        val connections = source.ast.declarations
            .filterIsInstance<ConnectionDeclaration>()
            .filter { declaration ->
                portIdsByAuthoredPath[pathKey(declaration.source.parts)] != null &&
                    portIdsByAuthoredPath[pathKey(declaration.target.parts)] != null &&
                    runCatching {
                        EngineeringConnectionFactRules.violations(
                            ConnectionKind.valueOf(declaration.kind.value.uppercase().replace('-', '_')),
                            declaration.properties,
                        ).isEmpty()
                    }.getOrDefault(false)
            }
            .map { declaration ->
                val kind = ConnectionKind.valueOf(declaration.kind.value.uppercase().replace('-', '_'))
                val sourcePath = declaration.source.parts
                val targetPath = declaration.target.parts
                EngineeringConnection(
                    id = StableSemanticIdentity(
                        "connection:$portableSourceUnitId:${kind.name.lowercase()}:${pathKey(sourcePath)}->${pathKey(targetPath)}",
                    ),
                    kind = kind,
                    endpoints = listOf(
                        ConnectionEndpoint(
                            port = EngineeringReference(
                                authoredPath = sourcePath,
                                resolvedIdentity = portIdsByAuthoredPath[pathKey(sourcePath)],
                                provenance = declaration.source.span.toProvenance(source.file),
                            ),
                            role = ConnectionEndpointRole.SOURCE,
                            provenance = declaration.source.span.toProvenance(source.file),
                        ),
                        ConnectionEndpoint(
                            port = EngineeringReference(
                                authoredPath = targetPath,
                                resolvedIdentity = portIdsByAuthoredPath[pathKey(targetPath)],
                                provenance = declaration.target.span.toProvenance(source.file),
                            ),
                            role = ConnectionEndpointRole.SINK,
                            provenance = declaration.target.span.toProvenance(source.file),
                        ),
                    ),
                    properties = loweringContext.lowerProperties(declaration.properties),
                    provenance = declaration.span.toProvenance(source.file),
                )
            }
        val nets = source.ast.declarations.filterIsInstance<NetDeclaration>().mapNotNull { declaration ->
            val endpoints = declaration.endpoints.map { endpoint ->
                ConnectionEndpoint(
                    port = EngineeringReference(
                        authoredPath = endpoint.port.parts,
                        resolvedIdentity = portIdsByAuthoredPath[pathKey(endpoint.port.parts)],
                        provenance = endpoint.port.span.toProvenance(source.file),
                    ),
                    role = when (endpoint.role) {
                        NetEndpointRole.SOURCE -> ConnectionEndpointRole.SOURCE
                        NetEndpointRole.SINK -> ConnectionEndpointRole.SINK
                        NetEndpointRole.PASS -> ConnectionEndpointRole.PASS
                    },
                    provenance = endpoint.span.toProvenance(source.file),
                )
            }
            val kind = runCatching { ConnectionKind.valueOf(declaration.kind.value.uppercase().replace('-', '_')) }.getOrNull()
            if (kind == null || EngineeringConnectionFactRules.violations(kind, declaration.properties).isNotEmpty() ||
                endpoints.size < 2 || endpoints.map { it.port.authoredPath }.distinct().size != endpoints.size ||
                endpoints.any { it.port.resolvedIdentity == null }) return@mapNotNull null
            EngineeringNet(
                id = StableSemanticIdentity("net:$portableSourceUnitId:${declaration.name}"),
                name = declaration.name,
                kind = kind,
                endpoints = endpoints,
                potentialOrSignal = declaration.potentialOrSignal?.let { reference ->
                    EngineeringReference(reference.parts, null, reference.span.toProvenance(source.file))
                },
                properties = loweringContext.lowerProperties(declaration.properties),
                provenance = declaration.span.toProvenance(source.file),
            )
        }
        val connectionSpecifications = source.ast.declarations.filterIsInstance<ConnectionSpecificationDeclaration>().map { declaration ->
            EngineeringConnectionSpecification(
                id = StableSemanticIdentity("connection-spec:$portableSourceUnitId:${declaration.scope.name.lowercase()}:${declaration.subject?.parts?.joinToString(".") ?: "project"}"),
                scope = when (declaration.scope) {
                    com.engineeringood.athena.language.ConnectionSpecificationScope.PROJECT -> ConnectionSpecificationScope.PROJECT
                    com.engineeringood.athena.language.ConnectionSpecificationScope.POTENTIAL -> ConnectionSpecificationScope.POTENTIAL
                    com.engineeringood.athena.language.ConnectionSpecificationScope.SIGNAL -> ConnectionSpecificationScope.SIGNAL
                    com.engineeringood.athena.language.ConnectionSpecificationScope.NET -> ConnectionSpecificationScope.NET
                    com.engineeringood.athena.language.ConnectionSpecificationScope.CONNECTION -> ConnectionSpecificationScope.CONNECTION
                },
                subject = declaration.subject?.let { reference -> EngineeringReference(reference.parts, null, reference.span.toProvenance(source.file)) },
                properties = loweringContext.lowerProperties(declaration.properties),
                provenance = declaration.span.toProvenance(source.file),
            )
        }
        val specificationResolution = ConnectionSpecificationResolver.resolve(
            specifications = connectionSpecifications,
            nets = nets,
            connections = connections,
        )
        val resolvedConnections = connections
            .filterNot { it.id.value in specificationResolution.invalidSubjects }
            .map { connection ->
                connection.copy(effectiveProperties = specificationResolution.connectionProperties[connection.id.value].orEmpty())
            }
        val resolvedNets = nets
            .filterNot { it.id.value in specificationResolution.invalidSubjects }
            .map { net -> net.copy(effectiveProperties = specificationResolution.netProperties[net.id.value].orEmpty()) }

        val subjectReferencesByPath = buildMap {
            entities.forEach { entity ->
                put(listOf(entity.name), EngineeringSubjectReference.Entity(
                    EngineeringEntityReference(EngineeringReference(listOf(entity.name), entity.id, entity.provenance)),
                ))
            }
            functions.forEach { function ->
                put(function.owner.reference.authoredPath + function.name, EngineeringSubjectReference.Function(
                    EngineeringFunctionReference(EngineeringReference(
                        function.owner.reference.authoredPath + function.name,
                        function.id,
                        function.provenance,
                    )),
                ))
            }
            ports.forEach { port ->
                put(port.owner.reference().authoredPath + port.name, EngineeringSubjectReference.Port(
                    EngineeringReference(port.owner.reference().authoredPath + port.name, port.id, port.provenance),
                ))
            }
        }
        val relationships = source.ast.declarations
            .filterIsInstance<RelationDeclaration>()
            .map { relation ->
                val participantPaths = listOf(relation.source) + relation.targets
                val usedRoles = mutableSetOf<String>()
                val participants = participantPaths.mapIndexed { index, path ->
                    val role = authoredParticipantRole(path, index, ports, usedRoles)
                    usedRoles += role
                    val provenance = path.span.toProvenance(source.file)
                    val subject = subjectReferencesByPath[path.parts]
                        ?: EngineeringSubjectReference.Unresolved(
                            EngineeringReference(
                                authoredPath = path.parts,
                                resolvedIdentity = null,
                                provenance = provenance,
                            ),
                        )
                    EngineeringParticipant(role, subject, provenance)
                }
                val identityParticipants = participants
                    .map { participant -> "${participant.role}:${participant.subject.reference.authoredPath.joinToString(".")}" }
                    .sorted()
                    .joinToString("|")
                EngineeringRelationship(
                    id = StableSemanticIdentity("relationship:$portableSourceUnitId:${relation.word.value}:$identityParticipants"),
                    definitionReference = EngineeringDefinitionReference(
                        authoredName = listOf(relation.word.value),
                        resolvedId = EngineeringDefinitionId(
                            packageName = EngineeringPackageName(
                                source.ast.packageDeclaration?.name?.parts?.joinToString(".") ?: "project",
                            ),
                            qualifiedName = relation.word.value,
                        ),
                        provenance = relation.word.span.toProvenance(source.file),
                    ),
                    participants = participants,
                    properties = emptyList(),
                    provenance = relation.span.toProvenance(source.file),
                )
            }
        val flows = emptyList<EngineeringFlow>()


        return EngineeringDocument(
            system = EngineeringSystem(
                id = systemIdentity(source.ast.system.name),
                name = source.ast.system.name,
                provenance = source.ast.system.span.toProvenance(source.file),
            ),
            entities = entities,
            ports = ports,
            relationships = relationships,
            flows = flows,
            functions = functions,
            connections = resolvedConnections,
            nets = resolvedNets,
            connectionSpecifications = connectionSpecifications,
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

    private fun systemIdentity(name: String): StableSemanticIdentity = StableSemanticIdentity("system:$name")

    private fun entityIdentity(name: String, duplicateOrdinal: Int): StableSemanticIdentity {
        return StableSemanticIdentity(withDuplicateSuffix("entity:$name", duplicateOrdinal))
    }

    private fun portIdentity(path: List<String>, duplicateOrdinal: Int): StableSemanticIdentity {
        return StableSemanticIdentity(withDuplicateSuffix("port:${pathKey(path)}", duplicateOrdinal))
    }

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

private fun authoredParticipantRole(
    path: com.engineeringood.athena.language.QualifiedName,
    index: Int,
    ports: List<EngineeringPort>,
    usedRoles: Set<String>,
): String {
    val baseRole = ports.firstOrNull { port ->
        port.owner.reference().authoredPath + port.name == path.parts
    }?.properties?.firstOrNull { property -> property.name == "role" }
        ?.value
        ?.let { value -> (value as? EngineeringValue.Symbol)?.text }
        ?: "participant-${index + 1}"
    if (baseRole !in usedRoles) return baseRole
    var suffix = 2
    while ("$baseRole-$suffix" in usedRoles) suffix++
    return "$baseRole-$suffix"
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
        direction = when (direction) {
            EngineeringPortDirection.INPUT -> "in"
            EngineeringPortDirection.OUTPUT -> "out"
            EngineeringPortDirection.BIDIRECTIONAL -> "bidirectional"
        },
        signalKind = admittedFlowReferences.singleOrNull()?.authoredName?.joinToString("."),
        role = values["role"]?.singleOrNull(),
    )
}

private fun List<EngineeringProperty>.symbolValuesByName(): Map<String, List<String>> =
    groupBy { it.name }.mapValues { (_, properties) ->
        properties.mapNotNull { (it.value as? EngineeringValue.Symbol)?.text }
    }

private fun EngineeringPortOwner.reference(): EngineeringReference = when (this) {
    is EngineeringPortOwner.Entity -> entity.reference
    is EngineeringPortOwner.Function -> function.reference
}
