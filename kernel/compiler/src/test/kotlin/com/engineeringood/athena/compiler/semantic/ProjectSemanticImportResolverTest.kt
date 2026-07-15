package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.language.ImportDeclaration
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectSemanticImportResolverTest {
    @Test
    fun `resolves longest available namespace prefixes and explains governed availability`() {
        val fixture = importFixture()

        val resolved = ProjectSemanticImportResolver().resolve(fixture.snapshot)
        val compilerResolved = AthenaCompiler().resolveProjectSemanticImports(fixture.snapshot)
        val resolutions = resolved.sourceUnits.single { it.packageKey == fixture.rootKey }.resolvedImports

        assertEquals(resolved.sourceUnits, compilerResolved.sourceUnits)
        assertEquals(
            listOf(
                "com.controls" to ProjectSemanticImportResolutionStatus.RESOLVED,
                "com.controls.PLC1" to ProjectSemanticImportResolutionStatus.RESOLVED,
                "com.missing" to ProjectSemanticImportResolutionStatus.UNAVAILABLE_NAMESPACE,
                "com.vendor" to ProjectSemanticImportResolutionStatus.UNAVAILABLE_PACKAGE,
            ),
            resolutions.map { it.importDeclaration.target.parts.joinToString(".") to it.status },
        )
        val exact = resolutions.first()
        assertEquals(listOf(fixture.controlsKey), exact.explanation.directDependencyKeys)
        assertEquals(setOf(fixture.rootKey, fixture.controlsKey), exact.explanation.availablePackageKeys.toSet())
        assertEquals(
            fixture.snapshot.sourceUnits.filter { it.packageKey in setOf(fixture.rootKey, fixture.controlsKey) }.map { it.sourceUnitId }.toSet(),
            exact.explanation.availableSourceUnitIds.toSet(),
        )
        assertEquals(listOf(fixture.controlsNamespaceId), exact.explanation.candidateNamespaceIds)
        assertEquals(fixture.controlsNamespaceId, exact.explanation.selectedNamespaceId)
        assertEquals(emptyList(), exact.explanation.unresolvedTargetSuffix)
        assertEquals(listOf("PLC1"), resolutions[1].explanation.unresolvedTargetSuffix)
    }

    @Test
    fun `keeps multi-version namespace matches deterministic and ambiguous`() {
        val rootId = PackageIdentifier("com.root", "1")
        val sharedV1 = PackageIdentifier("com.shared", "1")
        val sharedV2 = PackageIdentifier("com.shared", "2")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val sharedKeys = listOf(sharedV1, sharedV2).map(CanonicalSemanticIdentityBuilder::packageKey)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", sharedKeys.reversed()),
            ProjectSemanticPackage(sharedV2, sharedKeys[1], "packages/shared-v2/src", emptyList()),
            ProjectSemanticPackage(sharedV1, sharedKeys[0], "packages/shared-v1/src", emptyList()),
        )
        val rootImport = importDeclaration(listOf("com", "shared"), 0)
        val sourceUnits = listOf(
            sourceUnit(rootKey, "main.athena", "package com.root", listOf(rootImport)),
            sourceUnit(sharedKeys[0], "shared.athena", "package com.shared v1"),
            sourceUnit(sharedKeys[1], "shared.athena", "package com.shared v2"),
        )
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sourceUnits[0].sourceUnitId),
            namespace(sharedKeys[1], listOf("com", "shared"), sourceUnits[2].sourceUnitId),
            namespace(sharedKeys[0], listOf("com", "shared"), sourceUnits[1].sourceUnitId),
        )
        val snapshot = snapshot(rootKey, packages, sourceUnits, namespaces)

        val first = ProjectSemanticImportResolver().resolve(snapshot)
        val second = ProjectSemanticImportResolver().resolve(snapshot).sourceUnits
        val resolution = first.sourceUnits.single { it.packageKey == rootKey }.resolvedImports.single()

        assertEquals(ProjectSemanticImportResolutionStatus.AMBIGUOUS_NAMESPACE, resolution.status)
        assertEquals(sharedKeys.sortedBy { it.value }, resolution.explanation.availablePackageKeys.filter { it != rootKey })
        assertEquals(
            namespaces.filter { it.packageKey in sharedKeys }.map { it.namespaceId }.sortedBy { it.value },
            resolution.explanation.candidateNamespaceIds,
        )
        assertEquals(null, resolution.explanation.selectedNamespaceId)
        assertEquals(first.sourceUnits, second)
    }

    @Test
    fun `treats missing namespaces in an available package version as unavailable namespace`() {
        val rootId = PackageIdentifier("com.root", "1")
        val sharedV1 = PackageIdentifier("com.shared", "1")
        val sharedV2 = PackageIdentifier("com.shared", "2")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val sharedV1Key = CanonicalSemanticIdentityBuilder.packageKey(sharedV1)
        val sharedV2Key = CanonicalSemanticIdentityBuilder.packageKey(sharedV2)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(sharedV1Key)),
            ProjectSemanticPackage(sharedV1, sharedV1Key, "packages/shared-v1/src", emptyList()),
            ProjectSemanticPackage(sharedV2, sharedV2Key, "packages/shared-v2/src", emptyList()),
        )
        val rootImport = importDeclaration(listOf("com", "shared", "Missing"), 0)
        val sourceUnits = listOf(
            sourceUnit(rootKey, "main.athena", "package com.root", listOf(rootImport)),
            sourceUnit(sharedV1Key, "shared-v1.athena", "package com.shared v1"),
            sourceUnit(sharedV2Key, "shared-v2.athena", "package com.shared v2"),
        )
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sourceUnits[0].sourceUnitId),
            namespace(sharedV1Key, listOf("com", "shared", "Existing"), sourceUnits[1].sourceUnitId),
            namespace(sharedV2Key, listOf("com", "shared", "Other"), sourceUnits[2].sourceUnitId),
        )

        val resolution = ProjectSemanticImportResolver()
            .resolve(snapshot(rootKey, packages, sourceUnits, namespaces))
            .sourceUnits.single { it.packageKey == rootKey }
            .resolvedImports.single()

        assertEquals(ProjectSemanticImportResolutionStatus.UNAVAILABLE_NAMESPACE, resolution.status)
        assertEquals(emptyList(), resolution.explanation.candidateNamespaceIds)
        assertEquals(listOf("Missing"), resolution.explanation.unresolvedTargetSuffix)
    }

    @Test
    fun `longer unavailable package prefix overrides shorter available namespace prefixes`() {
        val rootId = PackageIdentifier("com.root", "1")
        val fooId = PackageIdentifier("com.foo", "1")
        val fooBarId = PackageIdentifier("com.foo.bar", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val fooKey = CanonicalSemanticIdentityBuilder.packageKey(fooId)
        val fooBarKey = CanonicalSemanticIdentityBuilder.packageKey(fooBarId)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(fooKey)),
            ProjectSemanticPackage(fooId, fooKey, "packages/foo/src", emptyList()),
            ProjectSemanticPackage(fooBarId, fooBarKey, "packages/foo-bar/src", emptyList()),
        )
        val rootImport = importDeclaration(listOf("com", "foo", "bar", "Symbol"), 0)
        val sourceUnits = listOf(
            sourceUnit(rootKey, "main.athena", "package com.root", listOf(rootImport)),
            sourceUnit(fooKey, "foo.athena", "package com.foo"),
            sourceUnit(fooBarKey, "foo-bar.athena", "package com.foo.bar"),
        )
        val unavailableNamespace = namespace(fooBarKey, listOf("com", "foo", "bar"), sourceUnits[2].sourceUnitId)
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sourceUnits[0].sourceUnitId),
            namespace(fooKey, listOf("com", "foo"), sourceUnits[1].sourceUnitId),
            unavailableNamespace,
        )

        val resolution = ProjectSemanticImportResolver()
            .resolve(snapshot(rootKey, packages, sourceUnits, namespaces))
            .sourceUnits.single { it.packageKey == rootKey }
            .resolvedImports.single()

        assertEquals(ProjectSemanticImportResolutionStatus.UNAVAILABLE_PACKAGE, resolution.status)
        assertEquals(listOf(unavailableNamespace.namespaceId), resolution.explanation.candidateNamespaceIds)
        assertEquals(null, resolution.explanation.selectedNamespaceId)
        assertEquals(listOf("Symbol"), resolution.explanation.unresolvedTargetSuffix)
    }

    @Test
    fun `selects namespaces only from the longest matching package name group`() {
        val rootId = PackageIdentifier("com.root", "1")
        val fooId = PackageIdentifier("com.foo", "1")
        val fooBarId = PackageIdentifier("com.foo.bar", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val fooKey = CanonicalSemanticIdentityBuilder.packageKey(fooId)
        val fooBarKey = CanonicalSemanticIdentityBuilder.packageKey(fooBarId)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(fooKey, fooBarKey)),
            ProjectSemanticPackage(fooId, fooKey, "packages/foo/src", emptyList()),
            ProjectSemanticPackage(fooBarId, fooBarKey, "packages/foo-bar/src", emptyList()),
        )
        val rootImport = importDeclaration(listOf("com", "foo", "bar", "Symbol"), 0)
        val sourceUnits = listOf(
            sourceUnit(rootKey, "main.athena", "package com.root", listOf(rootImport)),
            sourceUnit(fooKey, "foo.athena", "package com.foo"),
            sourceUnit(fooBarKey, "foo-bar.athena", "package com.foo.bar"),
        )
        val shorterPackageNamespace = namespace(fooKey, listOf("com", "foo", "bar"), sourceUnits[1].sourceUnitId)
        val longestPackageNamespace = namespace(fooBarKey, listOf("com", "foo", "bar"), sourceUnits[2].sourceUnitId)
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sourceUnits[0].sourceUnitId),
            shorterPackageNamespace,
            longestPackageNamespace,
        )

        val resolution = ProjectSemanticImportResolver()
            .resolve(snapshot(rootKey, packages, sourceUnits, namespaces))
            .sourceUnits.single { it.packageKey == rootKey }
            .resolvedImports.single()

        assertEquals(ProjectSemanticImportResolutionStatus.RESOLVED, resolution.status)
        assertEquals(listOf(longestPackageNamespace.namespaceId), resolution.explanation.candidateNamespaceIds)
        assertEquals(longestPackageNamespace.namespaceId, resolution.explanation.selectedNamespaceId)
        assertEquals(listOf("Symbol"), resolution.explanation.unresolvedTargetSuffix)
    }

    @Test
    fun `resolves equivalent snapshots from reversed raw collections identically`() {
        val rootId = PackageIdentifier("com.root", "1")
        val controlsId = PackageIdentifier("com.controls", "1")
        val auxId = PackageIdentifier("com.aux", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val controlsKey = CanonicalSemanticIdentityBuilder.packageKey(controlsId)
        val auxKey = CanonicalSemanticIdentityBuilder.packageKey(auxId)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(auxKey, controlsKey)),
            ProjectSemanticPackage(controlsId, controlsKey, "packages/controls/src", emptyList()),
            ProjectSemanticPackage(auxId, auxKey, "packages/aux/src", emptyList()),
        )
        val imports = listOf(
            importDeclaration(listOf("com", "controls", "PLC1"), 20),
            importDeclaration(listOf("com", "aux"), 0),
        )
        val sourceUnits = listOf(
            sourceUnit(rootKey, "main.athena", "package com.root", imports),
            sourceUnit(controlsKey, "controls.athena", "package com.controls"),
            sourceUnit(auxKey, "aux.athena", "package com.aux"),
        )
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sourceUnits[0].sourceUnitId),
            namespace(controlsKey, listOf("com", "controls"), sourceUnits[1].sourceUnitId),
            namespace(auxKey, listOf("com", "aux"), sourceUnits[2].sourceUnitId),
        )
        val forward = snapshot(rootKey, packages, sourceUnits, namespaces)
        val reversed = snapshot(
            rootKey,
            packages.reversed(),
            sourceUnits.reversed().map { sourceUnit ->
                if (sourceUnit.packageKey == rootKey) {
                    sourceUnit.copy(authoredImports = sourceUnit.authoredImports.reversed())
                } else {
                    sourceUnit
                }
            },
            namespaces.reversed(),
        )

        val forwardResolved = ProjectSemanticImportResolver().resolve(forward)
        val reversedResolved = ProjectSemanticImportResolver().resolve(reversed)

        assertEquals(forwardResolved.sourceUnits, reversedResolved.sourceUnits)
    }

    @Test
    fun `canonical snapshot rejects import explanations outside authored and governed indexes`() {
        val fixture = importFixture()
        val resolved = ProjectSemanticImportResolver().resolve(fixture.snapshot)
        val rootSource = resolved.sourceUnits.single { it.packageKey == fixture.rootKey }
        val resolution = rootSource.resolvedImports.first()
        val mutatedResolutions = rootSource.resolvedImports.map { existing ->
            if (existing == resolution) {
                existing.copy(
                    explanation = existing.explanation.copy(
                        availablePackageKeys = listOf(PackageKey("missing|")),
                    ),
                )
            } else {
                existing
            }
        }

        assertFailsWith<IllegalArgumentException> {
            rebuild(
                resolved,
                rootSource.copy(
                    resolvedImports = mutatedResolutions,
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            rebuild(
                resolved,
                rootSource.copy(
                    resolvedImports = rootSource.resolvedImports.map { existing ->
                        if (existing == resolution) {
                            existing.copy(importDeclaration = importDeclaration(listOf("com", "other"), 100))
                        } else {
                            existing
                        }
                    },
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            rebuild(
                resolved,
                rootSource.copy(
                    resolvedImports = rootSource.resolvedImports.dropLast(1),
                ),
            )
        }
    }

    private fun importFixture(): ImportFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val controlsId = PackageIdentifier("com.controls", "2")
        val vendorId = PackageIdentifier("com.vendor", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val controlsKey = CanonicalSemanticIdentityBuilder.packageKey(controlsId)
        val vendorKey = CanonicalSemanticIdentityBuilder.packageKey(vendorId)
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(controlsKey)),
            ProjectSemanticPackage(controlsId, controlsKey, "packages/controls/src", emptyList()),
            ProjectSemanticPackage(vendorId, vendorKey, "packages/vendor/src", emptyList()),
        )
        val imports = listOf(
            importDeclaration(listOf("com", "vendor"), 60),
            importDeclaration(listOf("com", "controls", "PLC1"), 20),
            importDeclaration(listOf("com", "missing"), 40),
            importDeclaration(listOf("com", "controls"), 0),
        )
        val sources = listOf(
            sourceUnit(rootKey, "main.athena", "root", imports),
            sourceUnit(controlsKey, "controls.athena", "controls"),
            sourceUnit(vendorKey, "vendor.athena", "vendor"),
        )
        val controlsNamespace = namespace(controlsKey, listOf("com", "controls"), sources[1].sourceUnitId)
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), sources[0].sourceUnitId),
            controlsNamespace,
            namespace(vendorKey, listOf("com", "vendor"), sources[2].sourceUnitId),
        )
        return ImportFixture(
            snapshot(rootKey, packages, sources, namespaces),
            rootKey,
            controlsKey,
            controlsNamespace.namespaceId,
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

    private fun rebuild(
        snapshot: ProjectSemanticGraphSnapshot,
        changedSource: ProjectSemanticSourceUnit,
    ): ProjectSemanticGraphSnapshot {
        return ProjectSemanticGraphSnapshot.canonical(
            snapshot.graphId,
            snapshot.rootPackageId,
            snapshot.packages,
            snapshot.sourceUnits.map { if (it.sourceUnitId == changedSource.sourceUnitId) changedSource else it },
            snapshot.namespaces,
            snapshot.declarations,
            snapshot.bindings,
            snapshot.diagnostics,
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

private data class ImportFixture(
    val snapshot: ProjectSemanticGraphSnapshot,
    val rootKey: PackageKey,
    val controlsKey: PackageKey,
    val controlsNamespaceId: NamespaceId,
)
