package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.BindingDeclaration
import com.engineeringood.athena.language.BindingSelectorKind
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.ProfileDeclaration
import com.engineeringood.athena.language.ProjectSourceUnit
import com.engineeringood.athena.language.RepresentationDeclaration
import com.engineeringood.athena.language.RepresentationResourceDeclaration
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.SymbolStringField
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.GraphicResourceId
import com.engineeringood.athena.packageplatform.GraphicResourceKind
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.PresentationProfileFallbackMode
import com.engineeringood.athena.packageplatform.PresentationProfileFallbackPolicy
import com.engineeringood.athena.packageplatform.PresentationProfileId
import com.engineeringood.athena.packageplatform.PresentationProfileProvenance
import com.engineeringood.athena.packageplatform.PresentationProfileVersion
import com.engineeringood.athena.packageplatform.PresentationStyleProfileId
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.packageplatform.RepresentationBindingPriority
import com.engineeringood.athena.packageplatform.RepresentationBindingRule
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleId
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleLifecycle
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleLifecycleState
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleProvenance
import com.engineeringood.athena.packageplatform.RepresentationBindingSelectorFact
import com.engineeringood.athena.packageplatform.RepresentationBindingTarget
import com.engineeringood.athena.packageplatform.RepresentationBindingSubjectKind
import com.engineeringood.athena.packageplatform.PackageResourceDeclaration
import com.engineeringood.athena.packageplatform.PackageResourceKey
import com.engineeringood.athena.packageplatform.RepresentationDescriptorId
import com.engineeringood.athena.packageplatform.RepresentationPackageId
import com.engineeringood.athena.packageplatform.RepresentationPackageVersion
import com.engineeringood.athena.packageplatform.RepresentationStandardTag
import com.engineeringood.athena.packageplatform.RepresentationVariantId
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationLibraryId

class AthenaRepresentationSourceCompiler(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun compile(file: String, source: String): AthenaRepresentationSourceCompilationResult =
        compile(listOf(AthenaRepresentationSourceInput(file, source)))

    fun compile(inputs: List<AthenaRepresentationSourceInput>): AthenaRepresentationSourceCompilationResult {
        val parsed = inputs.map(::parseOne)
        val units = parsed.mapNotNull { result -> result.unit }
        val unitByFile = units.associateBy { it.file }
        val declarations = units.flatMap(ParsedRepresentationUnit::declarations)
        val resources = units.flatMap(ParsedRepresentationUnit::resources)
        val identities = declarations.mapNotNull(::identityOccurrence)
        val diagnostics = buildList {
            addAll(parsed.flatMap { result -> result.diagnostics })
            addAll(duplicateIdentityDiagnostics(identities))
            declarations.filter { it.declaration is SymbolDeclaration || it.declaration is ElementDeclaration }.forEach { occurrence ->
                addAll(
                    when (val declaration = occurrence.declaration) {
                        is SymbolDeclaration -> AthenaSymbolSourceValidator.validate(occurrence.file, declaration)
                        is ElementDeclaration -> AthenaElementSourceValidator.validateBasic(occurrence.file, declaration)
                        else -> emptyList()
                    },
                )
            }
            addAll(AthenaProfileBindingSourceValidator.validate(declarations, identities))
            addAll(AthenaElementSourceValidator.validateReferences(declarations, identities))
        }.canonicalRepresentationDiagnostics()
        if (diagnostics.isNotEmpty()) {
            return AthenaRepresentationSourceCompilationResult(
                definitions = emptyList(),
                diagnostics = diagnostics,
            )
        }

        val loweredSymbols = declarations
            .filter { occurrence -> occurrence.declaration is SymbolDeclaration }
            .map { occurrence ->
                AthenaSymbolSourceLowerer.lower(
                    occurrence.file,
                    occurrence.libraryId,
                    occurrence.declaration as SymbolDeclaration,
                    requireNotNull(unitByFile[occurrence.file]).resourcesById,
                )
            }
        val symbolDefinitions = loweredSymbols.mapNotNull { result -> result.definition }
            .associateBy { definition -> DefinitionKey(definition.libraryId, definition.symbolId.value) }
        val loweredElements = declarations
            .filter { occurrence -> occurrence.declaration is ElementDeclaration }
            .map { occurrence ->
                AthenaElementSourceLowerer.lower(
                    occurrence.file,
                    occurrence.libraryId,
                    occurrence.declaration as ElementDeclaration,
                    symbolDefinitions,
                    requireNotNull(unitByFile[occurrence.file]).resourcesById,
                )
            }
        val loweringDiagnostics = (loweredSymbols + loweredElements)
            .flatMap { result -> result.diagnostics }
            .canonicalRepresentationDiagnostics()
        val profiles = declarations.mapNotNull { occurrence ->
            (occurrence.declaration as? ProfileDeclaration)?.let { profile -> lowerProfile(occurrence.file, profile) }
        }
        val profilesByName = profiles.associateBy { profile -> profile.profileId.value }
        val bindingRules = declarations.mapNotNull { occurrence ->
            (occurrence.declaration as? BindingDeclaration)?.let { binding ->
                lowerBinding(occurrence, binding, profilesByName, identities)
            }
        }
        val definitions = if (loweringDiagnostics.isEmpty()) {
            (loweredSymbols + loweredElements).mapNotNull { result -> result.definition }
                .sortedWith(compareBy({ it.libraryId.value }, { it.symbolId.value }, { it.version.value }))
        } else {
            emptyList()
        }
        return AthenaRepresentationSourceCompilationResult(
            definitions = definitions,
            profiles = profiles.sortedBy { it.profileId.value },
            bindingRules = bindingRules.sortedBy { it.ruleId.value },
            resources = resources.sortedBy { it.key.sourceUnitId + "/" + it.key.resourceId.value },
            diagnostics = loweringDiagnostics,
        )
    }

    fun lint(file: String, source: String): List<AthenaRepresentationSourceDiagnostic> = compile(file, source).diagnostics

    fun lintSvg(file: String, source: String): List<AthenaRepresentationSourceDiagnostic> {
        if (source.toByteArray().size > AthenaSvgGraphicBodySupport.MAX_SVG_BYTES) {
            return listOf(
                representationDiagnostic(
                    code = "svg.budget.bytes.exceeded",
                    file = file,
                    span = AthenaSvgGraphicBodySupport.sourceSpan(source, "<svg"),
                    subject = "svg.bytes",
                    message = "SVG graphic body exceeds the maximum source byte budget.",
                ),
            )
        }
        val parsed = AthenaSvgGraphicBodySupport.parseSvg(file, source)
        if (parsed.diagnostics.isNotEmpty()) return parsed.diagnostics
        val root = requireNotNull(parsed.root)
        val metrics = AthenaSvgGraphicBodySupport.treeMetrics(root)
        return buildList {
            addAll(AthenaSvgGraphicBodySupport.validateRoot(file, source, root))
            if (metrics.elements > AthenaSvgGraphicBodySupport.MAX_ELEMENTS) {
                add(
                    representationDiagnostic(
                        code = "svg.budget.elements.exceeded",
                        file = file,
                        span = AthenaSvgGraphicBodySupport.sourceSpan(source, "<svg"),
                        subject = "svg.elements",
                        message = "SVG graphic body exceeds the maximum element budget.",
                    ),
                )
            }
            if (metrics.maxDepth > AthenaSvgGraphicBodySupport.MAX_DEPTH) {
                add(
                    representationDiagnostic(
                        code = "svg.budget.depth.exceeded",
                        file = file,
                        span = AthenaSvgGraphicBodySupport.sourceSpan(source, "<svg"),
                        subject = "svg.depth",
                        message = "SVG graphic body exceeds the maximum DOM depth budget.",
                    ),
                )
            }
            AthenaSvgGraphicBodySupport.walkElements(root)
                .filter { element -> element !== root }
                .forEach { element -> addAll(AthenaSvgGraphicBodySupport.validateElement(file, source, element)) }
        }.canonicalRepresentationDiagnostics()
    }

    fun format(file: String, source: String): AthenaRepresentationSourceFormatResult {
        return when (val parsed = parser.parse(file, source)) {
            is ParseFailure -> AthenaRepresentationSourceFormatResult(
                formattedSource = null,
                diagnostics = parsed.diagnostics.map { diagnostic ->
                    representationDiagnostic(
                        code = "representation.syntax.invalid",
                        file = file,
                        span = diagnostic.span,
                        subject = "source",
                        message = diagnostic.message,
                    )
                }.canonicalRepresentationDiagnostics(),
                failure = "Representation source contains syntax errors.",
            )
            is ParseSuccess -> {
                val compiled = compile(file, source)
                if (compiled.diagnostics.isNotEmpty() || parsed.ast.unit !is RepresentationSourceUnit) {
                    AthenaRepresentationSourceFormatResult(
                        formattedSource = null,
                        diagnostics = compiled.diagnostics,
                        failure = "Representation source contains validation errors.",
                    )
                } else {
                    AthenaRepresentationSourceFormatResult(
                        formattedSource = AthenaRepresentationSourceFormatter.format(parsed.ast),
                        diagnostics = emptyList(),
                    )
                }
            }
        }
    }

    private fun parseOne(input: AthenaRepresentationSourceInput): ParsedRepresentationResult {
        return when (val parsed = parser.parse(input.file, input.source)) {
            is ParseFailure -> ParsedRepresentationResult(
                unit = null,
                diagnostics = parsed.diagnostics.map { syntax ->
                    representationDiagnostic(
                        code = "representation.syntax.invalid",
                        file = input.file,
                        span = syntax.span,
                        subject = "source",
                        message = syntax.message,
                    )
                }.canonicalRepresentationDiagnostics(),
            )
            is ParseSuccess -> validateSourceUnit(input.file, parsed.ast)
        }
    }

    private fun validateSourceUnit(file: String, ast: SourceFileAst): ParsedRepresentationResult {
        if (ast.unit is ProjectSourceUnit) {
            return ParsedRepresentationResult(
                null,
                listOf(
                    representationDiagnostic(
                        "representation.source-unit.project-forbidden",
                        file,
                        ast.span,
                        "source",
                        "Representation compilation requires a standalone representation source unit.",
                    ),
                ),
            )
        }
        val packageDeclaration = ast.packageDeclaration ?: return ParsedRepresentationResult(
            null,
            listOf(
                representationDiagnostic(
                    "representation.package.missing",
                    file,
                    ast.span,
                    "package",
                    "Representation source requires a package declaration.",
                ),
            ),
        )
        val libraryId = RepresentationLibraryId(packageDeclaration.name.parts.joinToString("."))
        val unit = ast.unit as RepresentationSourceUnit
        val resources = buildResourceIndex(file, unit.declarations)
        if (resources.diagnostics.isNotEmpty()) {
            return ParsedRepresentationResult(
                null,
                resources.diagnostics,
            )
        }
        return ParsedRepresentationResult(
            unit = ParsedRepresentationUnit(
                file,
                libraryId,
                unit.declarations.map { declaration -> AuthoredRepresentationDeclaration(file, libraryId, declaration) },
                resourcesById = resources.resourcesById,
                resources = resources.resources,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun buildResourceIndex(
        file: String,
        declarations: List<RepresentationDeclaration>,
    ): ParsedResourceResult {
        val occurrences = buildList {
            declarations.forEach { declaration ->
                val resources = when (declaration) {
                    is SymbolDeclaration -> declaration.resources
                    is ElementDeclaration -> declaration.resources
                    else -> emptyList()
                }
                resources.forEach { resource ->
                    add(
                        AuthoredResourceOccurrence(
                            declarationName = declaration.name,
                            resource = resource,
                        ),
                    )
                }
            }
        }
        val diagnostics = buildList {
            occurrences.groupBy { occurrence -> occurrence.resource.id }
                .filterValues { duplicates -> duplicates.size > 1 }
                .values
                .flatten()
                .forEach { occurrence ->
                    add(
                        representationDiagnostic(
                            code = "resource.id.duplicate",
                            file = file,
                            span = occurrence.resource.span,
                            subject = "resource.${occurrence.resource.id}",
                            message = "Resource ids must be unique within one source unit.",
                        ),
                    )
                }
            occurrences.forEach { occurrence ->
                val resource = occurrence.resource
                if (resource.kind != com.engineeringood.athena.language.RepresentationResourceKind.SVG) {
                    add(
                        representationDiagnostic(
                            code = "resource.kind.unsupported",
                            file = file,
                            span = resource.span,
                            subject = "resource.${resource.id}.kind",
                            message = "Resource kind `${resource.kind}` is not supported yet.",
                        ),
                    )
                }
                val resolvedPath = resolvePackageLocalSvgPath(file, resource.path.value)
                if (resolvedPath == null) {
                    add(
                        representationDiagnostic(
                            code = "resource.path.invalid",
                            file = file,
                            span = resource.path.span,
                            subject = "resource.${resource.id}.path",
                            message = "Resource path must stay inside the source unit directory and point to a local SVG file.",
                        ),
                    )
                    return@forEach
                }
                if (!java.nio.file.Files.isRegularFile(resolvedPath) || java.nio.file.Files.isSymbolicLink(resolvedPath)) {
                    add(
                        representationDiagnostic(
                            code = "resource.file.missing",
                            file = file,
                            span = resource.path.span,
                            subject = "resource.${resource.id}.path",
                            message = "Resource file does not exist as a regular local file.",
                        ),
                    )
                    return@forEach
                }
            }
        }.canonicalRepresentationDiagnostics()
        if (diagnostics.isNotEmpty()) {
            return ParsedResourceResult(diagnostics = diagnostics)
        }
        val packageResources = occurrences.map { occurrence ->
            val resolvedPath = requireNotNull(resolvePackageLocalSvgPath(file, occurrence.resource.path.value))
            val packageResource = PackageResourceDeclaration(
                key = PackageResourceKey(
                    sourceUnitId = file,
                    resourceId = GraphicResourceId(occurrence.resource.id),
                ),
                kind = GraphicResourceKind.VECTOR_DOCUMENT,
                path = occurrence.resource.path.value,
            )
            ResolvedSourceResource(
                declaration = occurrence.resource,
                packageResource = packageResource,
                resolvedPath = resolvedPath,
            )
        }
        return ParsedResourceResult(
            resourcesById = packageResources.associateBy { it.packageResource.key.resourceId.value },
            resources = packageResources.map { it.packageResource },
            diagnostics = emptyList(),
        )
    }

    private fun identityOccurrence(occurrence: AuthoredRepresentationDeclaration): RepresentationIdentityOccurrence? {
        val identity = occurrence.declaration.identityField() ?: return null
        return RepresentationIdentityOccurrence(
            occurrence.libraryId,
            identity.value,
            occurrence.file,
            identity.span,
            "${occurrence.declaration.subjectPrefix()}.identity",
            occurrence.declaration,
        )
    }

    private fun duplicateIdentityDiagnostics(
        occurrences: List<RepresentationIdentityOccurrence>,
    ): List<AthenaRepresentationSourceDiagnostic> = occurrences
        .groupBy { occurrence -> DefinitionKey(occurrence.libraryId, occurrence.identity) }
        .filterValues { duplicates -> duplicates.size > 1 }
        .values
        .flatten()
        .map { occurrence ->
            representationDiagnostic(
                code = "representation.identity.duplicate",
                file = occurrence.file,
                span = occurrence.span,
                subject = occurrence.subject,
                message = "Representation identity `${occurrence.identity}` is duplicated in this compilation batch.",
            )
        }
}

private fun lowerProfile(file: String, profile: ProfileDeclaration): PresentationProfileDescriptor {
    return PresentationProfileDescriptor(
        profileId = PresentationProfileId(profile.name),
        version = PresentationProfileVersion("1.0.0"),
        projectionContexts = listOf(ProjectionContextId(requireNotNull(profile.projection).value)),
        styleProfile = PresentationStyleProfileId(requireNotNull(profile.style).value),
        standardTags = listOf(RepresentationStandardTag(requireNotNull(profile.standard).value)),
        fallbackPolicy = PresentationProfileFallbackPolicy(PresentationProfileFallbackMode.FAIL_CLOSED),
        provenance = PresentationProfileProvenance(sources = listOf(file), reviewedBy = "Athena representation compiler"),
    )
}

private fun lowerBinding(
    occurrence: AuthoredRepresentationDeclaration,
    binding: BindingDeclaration,
    profilesByName: Map<String, PresentationProfileDescriptor>,
    identities: List<RepresentationIdentityOccurrence>,
): RepresentationBindingRule {
    val profileId = requireNotNull(binding.profile).value
    val projection = requireNotNull(profilesByName[profileId]).projectionContexts.single()
    val selectorFacts = binding.selectorFacts.map { fact ->
        RepresentationBindingSelectorFact(fact.name, fact.value.asBindingText())
    }
    val concept = selectorFacts.single { it.name == "type" }.value
    val targetIdentity = requireNotNull(binding.useElement).value
    val localCandidates = identities.filter { candidate ->
        candidate.libraryId == occurrence.libraryId &&
            candidate.identity == targetIdentity &&
            candidate.declaration is ElementDeclaration
    }
    val targetLibraryId = localCandidates.ifEmpty {
        identities.filter { candidate ->
            candidate.identity == targetIdentity && candidate.declaration is ElementDeclaration
        }
    }.single().libraryId
    return RepresentationBindingRule(
        ruleId = RepresentationBindingRuleId(binding.name),
        profileId = PresentationProfileId(profileId),
        projectionContext = projection,
        conceptId = EngineeringConceptId(concept),
        selectorFacts = selectorFacts,
        target = RepresentationBindingTarget(
            representationPackageId = RepresentationPackageId(targetLibraryId.value),
            descriptorId = RepresentationDescriptorId(targetIdentity),
            packageVersion = RepresentationPackageVersion(requireNotNull(binding.useVersion).value),
            variantId = binding.variant?.value?.let(::RepresentationVariantId),
        ),
        priority = RepresentationBindingPriority(requireNotNull(binding.priority).value.toInt()),
        lifecycle = RepresentationBindingRuleLifecycle(RepresentationBindingRuleLifecycleState.ACTIVE),
        provenance = RepresentationBindingRuleProvenance(sources = listOf(occurrence.file), reviewedBy = "Athena representation compiler"),
        subjectKind = when (binding.selectorKind) {
            BindingSelectorKind.Function -> RepresentationBindingSubjectKind.FUNCTION
            BindingSelectorKind.Device -> RepresentationBindingSubjectKind.DEVICE
            null -> error("Validated Binding selector kind was absent during lowering.")
        },
    )
}

private fun ScalarValue.asBindingText(): String = when (this) {
    is ScalarValue.Identifier -> text
    is ScalarValue.StringLiteral -> text
}

internal data class DefinitionKey(
    val libraryId: RepresentationLibraryId,
    val identity: String,
)

internal data class AuthoredRepresentationDeclaration(
    val file: String,
    val libraryId: RepresentationLibraryId,
    val declaration: RepresentationDeclaration,
)

internal data class RepresentationIdentityOccurrence(
    val libraryId: RepresentationLibraryId,
    val identity: String,
    val file: String,
    val span: SourceSpan,
    val subject: String,
    val declaration: RepresentationDeclaration,
)

private data class ParsedRepresentationUnit(
    val file: String,
    val libraryId: RepresentationLibraryId,
    val declarations: List<AuthoredRepresentationDeclaration>,
    val resourcesById: Map<String, ResolvedSourceResource>,
    val resources: List<PackageResourceDeclaration>,
)

private data class ParsedRepresentationResult(
    val unit: ParsedRepresentationUnit?,
    val diagnostics: List<AthenaRepresentationSourceDiagnostic>,
)

private data class ParsedResourceResult(
    val resourcesById: Map<String, ResolvedSourceResource> = emptyMap(),
    val resources: List<PackageResourceDeclaration> = emptyList(),
    val diagnostics: List<AthenaRepresentationSourceDiagnostic> = emptyList(),
)

private data class AuthoredResourceOccurrence(
    val declarationName: String,
    val resource: RepresentationResourceDeclaration,
)

internal fun RepresentationDeclaration.identityField(): SymbolStringField? = when (this) {
    is SymbolDeclaration -> identity
    is ElementDeclaration -> identity
    is ProfileDeclaration -> null
    is BindingDeclaration -> null
}

internal fun RepresentationDeclaration.subjectPrefix(): String = when (this) {
    is SymbolDeclaration -> "symbol.$name"
    is ElementDeclaration -> "element.$name"
    is ProfileDeclaration -> "profile.$name"
    is BindingDeclaration -> "binding.$name"
}
