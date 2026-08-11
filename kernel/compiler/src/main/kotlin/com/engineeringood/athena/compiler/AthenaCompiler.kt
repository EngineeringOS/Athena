package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoadOptions
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoader
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractValidationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryGraphResolutionResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryGraphResolver
import com.engineeringood.athena.compiler.repository.AthenaRepositoryLockMaterializationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryLockMaterializer
import com.engineeringood.athena.compiler.repository.AthenaRepositoryLockValidationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublisher
import com.engineeringood.athena.compiler.repository.AthenaRepositoryResolutionInputBuilder
import com.engineeringood.athena.compiler.repository.AthenaRepositoryResolutionInputResult
import com.engineeringood.athena.compiler.semantic.GovernedProjectSemanticGraphBuilder
import com.engineeringood.athena.compiler.semantic.PackageKey
import com.engineeringood.athena.compiler.semantic.ProjectSemanticCapabilityProvenanceProjector
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexer
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticProjector
import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphBuildResult
import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphSnapshot
import com.engineeringood.athena.compiler.semantic.ProjectSemanticImportResolver
import com.engineeringood.athena.compiler.semantic.ProjectSemanticLayoutHintBinder
import com.engineeringood.athena.compiler.semantic.ProjectSemanticLinkedLowerer
import com.engineeringood.athena.compiler.semantic.ProjectSemanticLinkedLoweringResult
import com.engineeringood.athena.compiler.semantic.ProjectSemanticReferenceLinker
import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceInput
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseResult
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.SheetCompanionSource
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory
import com.engineeringood.athena.plugin.host.AthenaPluginDiscovery
import com.engineeringood.athena.plugin.host.AthenaPluginDiscoveryReport
import com.engineeringood.athena.semantics.core.EngineeringIrValidator
import java.nio.file.Files
import java.nio.file.Path

/** Compiler authority for source parsing, Engineering Reality, validation, Projection, and Spatial facts. */
class AthenaCompiler(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
    private val validator: EngineeringIrValidator = EngineeringIrValidator(),
    private val pluginDiscovery: AthenaPluginDiscovery = AthenaPluginDiscovery(),
    hostedPluginDiscoveryReport: AthenaPluginDiscoveryReport? = null,
    hostedDomainPlugins: List<AthenaDomainPlugin>? = null,
    private val repositoryContractLoader: AthenaRepositoryContractLoader = AthenaRepositoryContractLoader(),
    private val repositoryResolutionInputBuilder: AthenaRepositoryResolutionInputBuilder = AthenaRepositoryResolutionInputBuilder(),
    private val repositoryGraphResolver: AthenaRepositoryGraphResolver = AthenaRepositoryGraphResolver(
        contractLoader = repositoryContractLoader,
        resolutionInputBuilder = repositoryResolutionInputBuilder,
    ),
    private val repositoryLockMaterializer: AthenaRepositoryLockMaterializer = AthenaRepositoryLockMaterializer(
        graphResolver = repositoryGraphResolver,
    ),
    lowerer: EngineeringIrLowerer? = null,
    private val projectSemanticGraphBuilder: GovernedProjectSemanticGraphBuilder = GovernedProjectSemanticGraphBuilder(parser),
    private val projectSemanticImportResolver: ProjectSemanticImportResolver = ProjectSemanticImportResolver(),
    private val projectSemanticDiagnosticProjector: ProjectSemanticDiagnosticProjector = ProjectSemanticDiagnosticProjector(),
    private val projectSemanticDeclarationIndexer: ProjectSemanticDeclarationIndexer = ProjectSemanticDeclarationIndexer(),
    private val projectSemanticLayoutHintBinder: ProjectSemanticLayoutHintBinder = ProjectSemanticLayoutHintBinder(),
    private val projectSemanticReferenceLinker: ProjectSemanticReferenceLinker = ProjectSemanticReferenceLinker(),
    private val projectSemanticCapabilityProvenanceProjector: ProjectSemanticCapabilityProvenanceProjector =
        ProjectSemanticCapabilityProvenanceProjector(),
) {
    val pluginDiscoveryReport: AthenaPluginDiscoveryReport = hostedPluginDiscoveryReport ?: pluginDiscovery.discover()
    val pluginInventory: AthenaApprovedPluginInventory = pluginDiscoveryReport.approvedInventory

    private val domainSemanticsCoordinator = hostedDomainPlugins?.let { plugins ->
        AthenaDomainSemanticsCoordinator(plugins)
    } ?: AthenaDomainSemanticsCoordinator(pluginInventory)
    private val repositoryReportPublisher = AthenaRepositoryReportPublisher(repositoryLockMaterializer)
    private val lowerer = lowerer ?: EngineeringIrLowerer(domainSemanticsCoordinator)
    private val compilationSupport = AthenaCompilerCompilationSupport(
        lowerer = this.lowerer,
        validator = validator,
        domainSemanticsCoordinator = domainSemanticsCoordinator,
    )

    fun parse(path: Path): CompilerParseResult = parseSource(path)

    fun parse(path: Path, sourceText: String): CompilerParseResult = parseSource(path.toString(), sourceText)

    fun compile(path: Path, sourceText: String): CompilerCompilationResult = compileParsedSource(
        parseResult = parseSource(path.toString(), sourceText),
    )

    fun compile(path: Path, sheetCompanionOverride: SheetCompanionSource): CompilerCompilationResult = compileParsedSource(
        parseResult = parseSource(path),
        sheetCompanionOverride = sheetCompanionOverride,
    )

    fun lower(path: Path): CompilerLoweringResult {
        return when (val parseResult = parseSource(path)) {
            is CompilerParseFailure -> CompilerLoweringFailure(parseResult.diagnostics)
            is CompilerParseSuccess -> {
                if (parseResult.source.ast.unit is RepresentationSourceUnit) {
                    CompilerLoweringFailure(listOf(representationSourceCompilerBoundaryDiagnostic(parseResult.source)))
                } else {
                    val document = lowerer.lower(parseResult.source)
                    val diagnostics = compilationSupport.domainSemanticsUnavailableDiagnostics(parseResult.source, document)
                    if (diagnostics.isEmpty()) {
                        CompilerLoweringSuccess(parseResult.source, document)
                    } else {
                        CompilerLoweringSemanticFailure(parseResult.source, document, diagnostics)
                    }
                }
            }
        }
    }

    fun validateRepositoryContract(
        repositoryRoot: Path,
        options: AthenaRepositoryContractLoadOptions = AthenaRepositoryContractLoadOptions(),
    ): AthenaRepositoryContractValidationResult = repositoryContractLoader.load(repositoryRoot, options)

    fun buildRepositoryResolutionInput(repositoryRoot: Path): AthenaRepositoryResolutionInputResult =
        repositoryResolutionInputBuilder.build(validateRepositoryContract(repositoryRoot))

    fun resolveRepositoryGraph(repositoryRoot: Path): AthenaRepositoryGraphResolutionResult =
        repositoryGraphResolver.resolve(repositoryRoot)

    fun materializeRepositoryLock(repositoryRoot: Path): AthenaRepositoryLockMaterializationResult =
        repositoryLockMaterializer.materialize(repositoryRoot)

    fun compilePresentationAssets(
        repositoryRoot: Path,
        packageSnapshots: List<com.engineeringood.athena.repository.RepositoryLockedPackage>,
    ): PresentationAssetPackageCompilationResult =
        PresentationAssetPackageCompiler().compile(repositoryRoot, packageSnapshots)

    fun validateRepositoryLock(repositoryRoot: Path): AthenaRepositoryLockValidationResult =
        repositoryLockMaterializer.validate(repositoryRoot)

    fun publishRepositoryGraphReport(repositoryRoot: Path): AthenaRepositoryReportPublicationResult =
        repositoryReportPublisher.publish(repositoryRoot)

    fun buildProjectSemanticGraph(
        publication: AthenaRepositoryReportPublicationResult,
        sources: List<ProjectSemanticSourceInput>,
    ): ProjectSemanticGraphBuildResult = projectSemanticGraphBuilder.build(publication, sources)

    fun resolveProjectSemanticImports(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot =
        projectSemanticImportResolver.resolve(snapshot)

    fun emitProjectSemanticDiagnostics(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot =
        projectSemanticDiagnosticProjector.project(snapshot)

    fun indexProjectSemanticDeclarations(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot =
        projectSemanticDeclarationIndexer.index(snapshot)

    fun bindProjectSemanticLayoutHints(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot =
        projectSemanticLayoutHintBinder.bind(snapshot)

    fun linkProjectSemanticReferences(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot =
        projectSemanticReferenceLinker.link(snapshot)

    fun preserveProjectSemanticCapabilities(
        snapshot: ProjectSemanticGraphSnapshot,
        capabilitiesByPackage: Map<PackageKey, List<String>>,
    ): ProjectSemanticGraphSnapshot = projectSemanticCapabilityProvenanceProjector.project(snapshot, capabilitiesByPackage)

    fun lowerLinkedProjectSemanticSources(
        snapshot: ProjectSemanticGraphSnapshot,
        documentsBySourceUnit: Map<com.engineeringood.athena.compiler.semantic.SourceUnitId, CompilerSourceDocument>,
    ): ProjectSemanticLinkedLoweringResult = ProjectSemanticLinkedLowerer(lowerer).lower(snapshot, documentsBySourceUnit)

    fun compile(path: Path): CompilerCompilationResult = compileParsedSource(
        parseResult = parseSource(path),
    )

    private fun compileParsedSource(
        parseResult: CompilerParseResult,
        sheetCompanionOverride: SheetCompanionSource? = null,
    ): CompilerCompilationResult =
        compilationSupport.compileParsedSource(parseResult, sheetCompanionOverride)

    private fun parseSource(path: Path): CompilerParseResult {
        val sourceText = runCatching { Files.readString(path) }.getOrElse { exception ->
            return CompilerParseFailure(
                listOf(
                    CompilerSyntaxDiagnostic(
                        file = path.toString(),
                        line = 0,
                        column = 0,
                        endLine = 0,
                        endColumn = 1,
                        message = "Could not read source file: ${exception.message ?: exception::class.simpleName}",
                    ),
                ),
            )
        }
        return parseSource(path.toString(), sourceText)
    }

    private fun parseSource(file: String, sourceText: String): CompilerParseResult {
        return when (val result: ParseResult = parser.parse(file, sourceText)) {
            is ParseSuccess -> CompilerParseSuccess(CompilerSourceDocument(file, result.ast))
            is ParseFailure -> CompilerParseFailure(
                result.diagnostics.map { diagnostic ->
                    CompilerSyntaxDiagnostic(
                        file = diagnostic.file,
                        line = diagnostic.line,
                        column = diagnostic.column,
                        endLine = diagnostic.span.end.line,
                        endColumn = diagnostic.span.end.column,
                        message = diagnostic.message,
                    )
                },
            )
        }
    }
}

private fun representationSourceCompilerBoundaryDiagnostic(source: CompilerSourceDocument): CompilerSyntaxDiagnostic {
    val span = source.ast.span
    return CompilerSyntaxDiagnostic(
        file = source.file,
        line = span.start.line,
        column = span.start.column,
        endLine = span.end.line,
        endColumn = span.end.column,
        message = "Representation source is outside the M42 engineering source contract.",
    )
}
