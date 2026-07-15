package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.ImportDeclaration
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectSemanticGraphSnapshotTest {
    @Test
    fun `canonical snapshot exposes one deterministically ordered semantic workspace`() {
        val fixture = semanticGraphFixture()

        val snapshot = ProjectSemanticGraphSnapshot.canonical(
            graphId = fixture.graphId,
            rootPackageId = fixture.rootKey,
            packages = fixture.packages.reversed().map { it.copy(sourceRoot = "./${it.sourceRoot}") },
            sourceUnits = fixture.sourceUnits.reversed().map { sourceUnit ->
                if (sourceUnit.packageKey == fixture.rootKey) {
                    sourceUnit.copy(sourceRootRelativePath = "draft/../main.athena")
                } else {
                    sourceUnit
                }
            },
            namespaces = listOf(
                fixture.namespace.copy(
                    sourceUnitIds = fixture.namespace.sourceUnitIds.reversed(),
                    declarationIds = listOf(fixture.declaration.declarationId, fixture.declaration.declarationId),
                    admittedCapabilities = listOf("component-knowledge", "authoring", "component-knowledge"),
                ),
            ),
            declarations = listOf(fixture.declaration),
            bindings = listOf(fixture.binding),
            diagnostics = listOf(fixture.lateDiagnostic, fixture.earlyDiagnostic),
        )

        assertEquals(fixture.graphId, snapshot.graphId)
        assertEquals(fixture.rootKey, snapshot.rootPackageId)
        assertEquals(fixture.packages.map { it.packageKey }.sortedBy { it.value }, snapshot.packages.map { it.packageKey })
        assertEquals(fixture.sourceUnits.map { it.sourceUnitId }.sortedBy { it.value }, snapshot.sourceUnits.map { it.sourceUnitId })
        assertEquals(listOf("src", "src"), snapshot.packages.map { it.sourceRoot })
        assertEquals("main.athena", snapshot.sourceUnits.single { it.packageKey == fixture.rootKey }.sourceRootRelativePath)
        assertEquals(listOf("authoring", "component-knowledge"), snapshot.namespaces.single().admittedCapabilities)
        assertEquals(fixture.namespace.sourceUnitIds.sortedBy { it.value }, snapshot.namespaces.single().sourceUnitIds)
        assertEquals(listOf(fixture.declaration.declarationId), snapshot.namespaces.single().declarationIds)
        assertEquals(listOf("import.missing", "symbol.unresolved"), snapshot.diagnostics.map { it.code.value })
        assertEquals(
            fixture.sourceUnits.map { it.sourceUnitId }.sortedBy { it.value },
            snapshot.diagnostics.last().relatedLocations.map { it.sourceUnitId },
        )
    }

    @Test
    fun `canonical snapshot rejects duplicate and dangling semantic relationships`() {
        val fixture = semanticGraphFixture()

        assertFailsWith<IllegalArgumentException> { fixture.snapshot(rootPackageId = PackageKey("missing|")) }
        assertFailsWith<IllegalArgumentException> { fixture.snapshot(graphId = GraphId("graph:${"0".repeat(64)}")) }
        assertFailsWith<IllegalArgumentException> { fixture.snapshot(packages = fixture.packages + fixture.packages.first()) }
        assertFailsWith<IllegalArgumentException> { fixture.snapshot(sourceUnits = fixture.sourceUnits + fixture.sourceUnits.first()) }
        assertFailsWith<IllegalArgumentException> { fixture.snapshot(namespaces = listOf(fixture.namespace, fixture.namespace)) }
        assertFailsWith<IllegalArgumentException> { fixture.snapshot(declarations = listOf(fixture.declaration, fixture.declaration)) }
        assertFailsWith<IllegalArgumentException> { fixture.snapshot(bindings = listOf(fixture.binding, fixture.binding)) }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(
                packages = fixture.packages.map { semanticPackage ->
                    if (semanticPackage.packageKey == fixture.rootKey) {
                        semanticPackage.copy(directDependencies = listOf(PackageKey("missing|")))
                    } else {
                        semanticPackage
                    }
                },
            )
        }
        assertFailsWith<IllegalArgumentException> {
            val missingPackage = PackageKey("missing|")
            val missingUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(missingPackage, "missing.athena")
            fixture.snapshot(
                sourceUnits = fixture.sourceUnits + ProjectSemanticSourceUnit(
                    missingUnitId,
                    missingPackage,
                    "missing.athena",
                    CanonicalSemanticIdentityBuilder.sourceContentIdentity(missingUnitId, "system Missing {}"),
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(namespaces = listOf(fixture.namespace.copy(sourceUnitIds = listOf(SourceUnitId("missing")))))
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(declarations = listOf(fixture.declaration.copy(namespaceId = NamespaceId("missing"))))
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(declarations = listOf(fixture.declaration.copy(authoredSpan = span(10, 2))))
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(namespaces = listOf(fixture.namespace.copy(sourceUnitIds = emptyList())))
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(bindings = listOf(fixture.binding.copy(resolvedDeclarationId = DeclarationId("missing"))))
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(diagnostics = listOf(fixture.earlyDiagnostic.copy(sourceUnitId = SourceUnitId("missing"))))
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(diagnostics = listOf(fixture.earlyDiagnostic.copy(sourceSpan = span(8, 2))))
        }
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(
                diagnostics = listOf(
                    fixture.earlyDiagnostic.copy(
                        relatedLocations = listOf(
                            ProjectSemanticRelatedLocation(fixture.sourceUnits.first().sourceUnitId, span(4, 1)),
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun `canonical snapshot orders namespaces declarations and bindings by canonical identity`() {
        val fixture = semanticGraphFixture()
        val dependencySource = fixture.sourceUnits.single { it.packageKey != fixture.rootKey }
        val dependencyNamespaceId = CanonicalSemanticIdentityBuilder.namespaceId(
            dependencySource.packageKey,
            listOf("com", "controls"),
        )
        val dependencyDeclarationId = CanonicalSemanticIdentityBuilder.declarationId(
            dependencySource.sourceUnitId,
            "system",
            listOf("Controls"),
        )
        val dependencyDeclaration = ProjectSemanticDeclaration(
            dependencyDeclarationId,
            dependencyNamespaceId,
            dependencySource.sourceUnitId,
            "system",
            listOf("Controls"),
            span(0, 15),
        )
        val dependencyBindingSpan = span(20, 28)
        val dependencyBinding = ProjectSemanticBinding(
            CanonicalSemanticIdentityBuilder.bindingId(
                dependencySource.sourceUnitId,
                dependencyBindingSpan,
                dependencyDeclarationId,
            ),
            dependencySource.sourceUnitId,
            dependencyBindingSpan,
            dependencyDeclarationId,
        )
        val dependencyNamespace = ProjectSemanticNamespace(
            dependencyNamespaceId,
            dependencySource.packageKey,
            listOf("com", "controls"),
            listOf(dependencySource.sourceUnitId),
            listOf(dependencyDeclarationId),
        )

        val snapshot = fixture.snapshot(
            namespaces = listOf(dependencyNamespace, fixture.namespace).sortedByDescending { it.namespaceId.value },
            declarations = listOf(dependencyDeclaration, fixture.declaration).sortedByDescending { it.declarationId.value },
            bindings = listOf(dependencyBinding, fixture.binding).sortedByDescending { it.bindingId.value },
        )

        assertEquals(snapshot.namespaces.map { it.namespaceId.value }.sorted(), snapshot.namespaces.map { it.namespaceId.value })
        assertEquals(snapshot.declarations.map { it.declarationId.value }.sorted(), snapshot.declarations.map { it.declarationId.value })
        assertEquals(snapshot.bindings.map { it.bindingId.value }.sorted(), snapshot.bindings.map { it.bindingId.value })
    }

    @Test
    fun `canonical snapshot uses complete diagnostic and related-location ordering keys`() {
        val fixture = semanticGraphFixture()
        val sourceUnitId = fixture.sourceUnits.first().sourceUnitId
        val sharedSpan = span(4, 8)
        val firstLocation = ProjectSemanticRelatedLocation(
            sourceUnitId,
            SourceSpan(SourcePosition(1, 1, 2), SourcePosition(2, 1, 3)),
            "Candidate",
        )
        val secondLocation = ProjectSemanticRelatedLocation(
            sourceUnitId,
            SourceSpan(SourcePosition(1, 2, 1), SourcePosition(2, 2, 2)),
            "Candidate",
        )
        val error = ProjectSemanticDiagnostic(
            ProjectSemanticDiagnosticCode("symbol.same"),
            ProjectSemanticDiagnosticSeverity.ERROR,
            "Same diagnostic",
            sourceUnitId,
            sharedSpan,
            listOf(secondLocation, firstLocation),
        )
        val warning = error.copy(
            severity = ProjectSemanticDiagnosticSeverity.WARNING,
            relatedLocations = listOf(firstLocation.copy(message = "")),
        )

        val first = fixture.snapshot(diagnostics = listOf(warning, error)).diagnostics
        val second = fixture.snapshot(diagnostics = listOf(error, warning)).diagnostics

        assertEquals(first, second)
        assertEquals(listOf(ProjectSemanticDiagnosticSeverity.ERROR, ProjectSemanticDiagnosticSeverity.WARNING), first.map { it.severity })
        assertEquals(listOf(firstLocation, secondLocation), first.first().relatedLocations)
        assertEquals(null, first.last().relatedLocations.single().message)
    }

    @Test
    fun `canonical snapshot fully orders imports and rejects targets outside their declaration span`() {
        val fixture = semanticGraphFixture()
        val sourceUnit = fixture.sourceUnits.first()
        val declarationSpan = SourceSpan(SourcePosition(0, 1, 1), SourcePosition(20, 2, 10))
        val firstImport = ImportDeclaration(
            QualifiedName(listOf("com", "controls"), SourceSpan(SourcePosition(7, 1, 8), SourcePosition(19, 1, 20))),
            declarationSpan,
        )
        val secondImport = ImportDeclaration(
            QualifiedName(listOf("com", "controls"), SourceSpan(SourcePosition(7, 2, 1), SourcePosition(19, 2, 13))),
            declarationSpan,
        )
        val first = fixture.snapshot(
            sourceUnits = fixture.sourceUnits.map {
                if (it.sourceUnitId == sourceUnit.sourceUnitId) it.copy(authoredImports = listOf(secondImport, firstImport)) else it
            },
        )
        val second = fixture.snapshot(
            sourceUnits = fixture.sourceUnits.map {
                if (it.sourceUnitId == sourceUnit.sourceUnitId) it.copy(authoredImports = listOf(firstImport, secondImport)) else it
            },
        )

        assertEquals(first.sourceUnits, second.sourceUnits)
        assertFailsWith<IllegalArgumentException> {
            fixture.snapshot(
                sourceUnits = fixture.sourceUnits.map {
                    if (it.sourceUnitId == sourceUnit.sourceUnitId) {
                        it.copy(
                            authoredImports = listOf(
                                firstImport.copy(
                                    target = firstImport.target.copy(
                                        span = SourceSpan(SourcePosition(21, 3, 1), SourcePosition(30, 3, 10)),
                                    ),
                                ),
                            ),
                        )
                    } else {
                        it
                    }
                },
            )
        }
    }

    private fun semanticGraphFixture(): SemanticGraphFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val dependencyId = PackageIdentifier("com.controls", "2")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val dependencyKey = CanonicalSemanticIdentityBuilder.packageKey(dependencyId)
        val rootUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(rootKey, "main.athena")
        val dependencyUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(dependencyKey, "controls.athena")
        val rootContent = CanonicalSemanticIdentityBuilder.sourceContentIdentity(rootUnitId, "system Root {}")
        val dependencyContent = CanonicalSemanticIdentityBuilder.sourceContentIdentity(dependencyUnitId, "system Controls {}")
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(dependencyKey)),
            ProjectSemanticPackage(dependencyId, dependencyKey, "src", emptyList()),
        )
        val sourceUnits = listOf(
            ProjectSemanticSourceUnit(rootUnitId, rootKey, "main.athena", rootContent),
            ProjectSemanticSourceUnit(dependencyUnitId, dependencyKey, "controls.athena", dependencyContent),
        )
        val namespaceId = CanonicalSemanticIdentityBuilder.namespaceId(rootKey, listOf("com", "root"))
        val declarationId = CanonicalSemanticIdentityBuilder.declarationId(rootUnitId, "system", listOf("Root"))
        val declarationSpan = span(0, 11)
        val declaration = ProjectSemanticDeclaration(
            declarationId,
            namespaceId,
            rootUnitId,
            "system",
            listOf("Root"),
            declarationSpan,
        )
        val referenceSpan = span(20, 24)
        val binding = ProjectSemanticBinding(
            CanonicalSemanticIdentityBuilder.bindingId(rootUnitId, referenceSpan, declarationId),
            rootUnitId,
            referenceSpan,
            declarationId,
        )
        val namespace = ProjectSemanticNamespace(
            namespaceId,
            rootKey,
            listOf("com", "root"),
            listOf(rootUnitId),
            listOf(declarationId),
            listOf("authoring"),
        )
        val earlyDiagnostic = ProjectSemanticDiagnostic(
            ProjectSemanticDiagnosticCode("import.missing"),
            ProjectSemanticDiagnosticSeverity.ERROR,
            "Missing import",
            rootUnitId,
            span(2, 8),
        )
        val lateDiagnostic = ProjectSemanticDiagnostic(
            ProjectSemanticDiagnosticCode("symbol.unresolved"),
            ProjectSemanticDiagnosticSeverity.WARNING,
            "Unresolved symbol",
            dependencyUnitId,
            span(12, 18),
            sourceUnits.reversed().map { sourceUnit ->
                ProjectSemanticRelatedLocation(sourceUnit.sourceUnitId, span(0, 1), "Candidate")
            },
        )
        val graphId = CanonicalSemanticIdentityBuilder.graphId(
            rootKey,
            packages.map { semanticPackage ->
                GraphPackageIdentity(semanticPackage.packageKey, semanticPackage.sourceRoot, semanticPackage.directDependencies)
            },
            sourceUnits.map { it.contentIdentity },
        )
        return SemanticGraphFixture(
            graphId,
            rootKey,
            packages,
            sourceUnits,
            namespace,
            declaration,
            binding,
            earlyDiagnostic,
            lateDiagnostic,
        )
    }

    private fun span(start: Int, end: Int): SourceSpan {
        return SourceSpan(SourcePosition(start, 1, start + 1), SourcePosition(end, 1, end + 1))
    }
}

private data class SemanticGraphFixture(
    val graphId: GraphId,
    val rootKey: PackageKey,
    val packages: List<ProjectSemanticPackage>,
    val sourceUnits: List<ProjectSemanticSourceUnit>,
    val namespace: ProjectSemanticNamespace,
    val declaration: ProjectSemanticDeclaration,
    val binding: ProjectSemanticBinding,
    val earlyDiagnostic: ProjectSemanticDiagnostic,
    val lateDiagnostic: ProjectSemanticDiagnostic,
) {
    fun snapshot(
        graphId: GraphId = this.graphId,
        rootPackageId: PackageKey = rootKey,
        packages: List<ProjectSemanticPackage> = this.packages,
        sourceUnits: List<ProjectSemanticSourceUnit> = this.sourceUnits,
        namespaces: List<ProjectSemanticNamespace> = listOf(namespace),
        declarations: List<ProjectSemanticDeclaration> = listOf(declaration),
        bindings: List<ProjectSemanticBinding> = listOf(binding),
        diagnostics: List<ProjectSemanticDiagnostic> = listOf(earlyDiagnostic, lateDiagnostic),
    ): ProjectSemanticGraphSnapshot {
        return ProjectSemanticGraphSnapshot.canonical(
            graphId,
            rootPackageId,
            packages,
            sourceUnits,
            namespaces,
            declarations,
            bindings,
            diagnostics,
        )
    }
}
