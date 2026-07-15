package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportLockState
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.language.ImportDeclaration
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectSemanticDiagnosticProjectorTest {
    @Test
    fun `emits stable diagnostics for unresolved import statuses with import target spans`() {
        val fixture = diagnosticFixture()
        val resolved = ProjectSemanticImportResolver().resolve(fixture.snapshot)

        val projected = ProjectSemanticDiagnosticProjector().project(resolved)
        val compilerProjected = AthenaCompiler().emitProjectSemanticDiagnostics(resolved)
        val diagnosticsByCode = projected.diagnostics.associateBy { it.code.value }

        assertEquals(projected.diagnostics, compilerProjected.diagnostics)
        assertEquals(
            listOf(
                "semantic.import.namespace.ambiguous",
                "semantic.import.namespace.unavailable",
                "semantic.import.package.unavailable",
            ),
            projected.diagnostics.map { it.code.value },
        )
        diagnosticsByCode.values.forEach { diagnostic ->
            assertEquals(ProjectSemanticDiagnosticSeverity.ERROR, diagnostic.severity)
            assertEquals(fixture.rootSourceUnitId, diagnostic.sourceUnitId)
        }
        assertEquals(
            fixture.imports.getValue("com.shared").target.span,
            diagnosticsByCode.getValue("semantic.import.namespace.ambiguous").sourceSpan,
        )
        assertEquals(
            fixture.imports.getValue("com.missing").target.span,
            diagnosticsByCode.getValue("semantic.import.namespace.unavailable").sourceSpan,
        )
        assertEquals(
            fixture.imports.getValue("com.vendor").target.span,
            diagnosticsByCode.getValue("semantic.import.package.unavailable").sourceSpan,
        )
    }

    @Test
    fun `does not emit diagnostics for resolved imports`() {
        val rootId = PackageIdentifier("com.root", "1")
        val controlsId = PackageIdentifier("com.controls", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val controlsKey = CanonicalSemanticIdentityBuilder.packageKey(controlsId)
        val rootImport = importDeclaration(listOf("com", "controls"), 0)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(controlsKey)),
            ProjectSemanticPackage(controlsId, controlsKey, "packages/controls/src", emptyList()),
        )
        val sources = listOf(
            sourceUnit(rootKey, "main.athena", "package com.root", listOf(rootImport)),
            sourceUnit(controlsKey, "controls.athena", "package com.controls"),
        )
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sources[0].sourceUnitId),
            namespace(controlsKey, listOf("com", "controls"), sources[1].sourceUnitId),
        )
        val resolved = ProjectSemanticImportResolver().resolve(snapshot(rootKey, packages, sources, namespaces))

        val projected = ProjectSemanticDiagnosticProjector().project(resolved)

        assertEquals(emptyList(), projected.diagnostics)
    }

    @Test
    fun `keeps projected diagnostics deterministic from reversed raw graph collections`() {
        val fixture = diagnosticFixture()
        val reversed = snapshot(
            fixture.rootKey,
            fixture.packages.reversed(),
            fixture.sourceUnits.reversed().map { sourceUnit ->
                if (sourceUnit.sourceUnitId == fixture.rootSourceUnitId) {
                    sourceUnit.copy(authoredImports = sourceUnit.authoredImports.reversed())
                } else {
                    sourceUnit
                }
            },
            fixture.namespaces.reversed(),
        )

        val forwardProjected = ProjectSemanticDiagnosticProjector()
            .project(ProjectSemanticImportResolver().resolve(fixture.snapshot))
        val reversedProjected = ProjectSemanticDiagnosticProjector()
            .project(ProjectSemanticImportResolver().resolve(reversed))

        assertEquals(forwardProjected.diagnostics, reversedProjected.diagnostics)
    }

    @Test
    fun `projecting diagnostics is idempotent for the same resolved snapshot`() {
        val fixture = diagnosticFixture()
        val resolved = ProjectSemanticImportResolver().resolve(fixture.snapshot)
        val once = ProjectSemanticDiagnosticProjector().project(resolved)

        val twice = ProjectSemanticDiagnosticProjector().project(once)

        assertEquals(once.diagnostics, twice.diagnostics)
    }

    @Test
    fun `preserves graph-invalid diagnostics when no semantic snapshot can be published`() {
        val invalidPublication = AthenaRepositoryReportPublicationResult(
            repositoryRoot = Path.of("repo"),
            manifestPath = Path.of("repo/athena.yaml"),
            lockPath = Path.of("repo/athena.lock"),
            manifestPresent = false,
            lockPresent = false,
            lockState = AthenaRepositoryReportLockState.MISSING,
        )

        val result = GovernedProjectSemanticGraphBuilder().build(invalidPublication, emptyList())

        assertEquals(null, result.snapshot)
        assertEquals(
            listOf("semantic.repository.publication.invalid"),
            result.diagnostics.map { it.code.value },
        )
    }

    private fun diagnosticFixture(): DiagnosticFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val sharedV1 = PackageIdentifier("com.shared", "1")
        val sharedV2 = PackageIdentifier("com.shared", "2")
        val vendorId = PackageIdentifier("com.vendor", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val sharedV1Key = CanonicalSemanticIdentityBuilder.packageKey(sharedV1)
        val sharedV2Key = CanonicalSemanticIdentityBuilder.packageKey(sharedV2)
        val vendorKey = CanonicalSemanticIdentityBuilder.packageKey(vendorId)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(sharedV2Key, sharedV1Key)),
            ProjectSemanticPackage(sharedV2, sharedV2Key, "packages/shared-v2/src", emptyList()),
            ProjectSemanticPackage(vendorId, vendorKey, "packages/vendor/src", emptyList()),
            ProjectSemanticPackage(sharedV1, sharedV1Key, "packages/shared-v1/src", emptyList()),
        )
        val imports = listOf(
            importDeclaration(listOf("com", "vendor"), 40),
            importDeclaration(listOf("com", "missing"), 20),
            importDeclaration(listOf("com", "shared"), 0),
        )
        val sourceUnits = listOf(
            sourceUnit(rootKey, "main.athena", "package com.root", imports),
            sourceUnit(sharedV1Key, "shared-v1.athena", "package com.shared"),
            sourceUnit(vendorKey, "vendor.athena", "package com.vendor"),
            sourceUnit(sharedV2Key, "shared-v2.athena", "package com.shared"),
        )
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sourceUnits[0].sourceUnitId),
            namespace(vendorKey, listOf("com", "vendor"), sourceUnits[2].sourceUnitId),
            namespace(sharedV2Key, listOf("com", "shared"), sourceUnits[3].sourceUnitId),
            namespace(sharedV1Key, listOf("com", "shared"), sourceUnits[1].sourceUnitId),
        )
        return DiagnosticFixture(
            snapshot(rootKey, packages, sourceUnits, namespaces),
            rootKey,
            sourceUnits[0].sourceUnitId,
            packages,
            sourceUnits,
            namespaces,
            imports.associateBy { it.target.parts.joinToString(".") },
        )
    }

    private fun sourceUnit(
        packageKey: PackageKey,
        path: String,
        content: String,
        imports: List<ImportDeclaration> = emptyList(),
    ): ProjectSemanticSourceUnit {
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, path)
        return ProjectSemanticSourceUnit(
            sourceUnitId,
            packageKey,
            path,
            CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, content),
            imports,
        )
    }

    private fun namespace(
        packageKey: PackageKey,
        qualifiedName: List<String>,
        sourceUnitId: SourceUnitId,
    ): ProjectSemanticNamespace {
        return ProjectSemanticNamespace(
            CanonicalSemanticIdentityBuilder.namespaceId(packageKey, qualifiedName),
            packageKey,
            qualifiedName,
            listOf(sourceUnitId),
            emptyList(),
        )
    }

    private fun snapshot(
        rootKey: PackageKey,
        packages: List<ProjectSemanticPackage>,
        sourceUnits: List<ProjectSemanticSourceUnit>,
        namespaces: List<ProjectSemanticNamespace>,
    ): ProjectSemanticGraphSnapshot {
        val graphId = CanonicalSemanticIdentityBuilder.graphId(
            rootKey,
            packages.map { GraphPackageIdentity(it.packageKey, it.sourceRoot, it.directDependencies) },
            sourceUnits.map { it.contentIdentity },
        )
        return ProjectSemanticGraphSnapshot.canonical(
            graphId,
            rootKey,
            packages,
            sourceUnits,
            namespaces,
            emptyList(),
            emptyList(),
            emptyList(),
        )
    }

    private fun importDeclaration(parts: List<String>, offset: Int): ImportDeclaration {
        val targetLength = parts.joinToString(".").length
        val declarationSpan = SourceSpan(
            SourcePosition(offset, 1, offset + 1),
            SourcePosition(offset + 7 + targetLength, 1, offset + 8 + targetLength),
        )
        return ImportDeclaration(
            QualifiedName(
                parts,
                SourceSpan(
                    SourcePosition(offset + 7, 1, offset + 8),
                    SourcePosition(offset + 7 + targetLength, 1, offset + 8 + targetLength),
                ),
            ),
            declarationSpan,
        )
    }
}

private data class DiagnosticFixture(
    val snapshot: ProjectSemanticGraphSnapshot,
    val rootKey: PackageKey,
    val rootSourceUnitId: SourceUnitId,
    val packages: List<ProjectSemanticPackage>,
    val sourceUnits: List<ProjectSemanticSourceUnit>,
    val namespaces: List<ProjectSemanticNamespace>,
    val imports: Map<String, ImportDeclaration>,
)
