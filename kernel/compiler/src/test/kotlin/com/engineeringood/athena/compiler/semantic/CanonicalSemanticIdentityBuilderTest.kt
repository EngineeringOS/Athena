package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CanonicalSemanticIdentityBuilderTest {
    @Test
    fun `package and source unit identities preserve governed compatibility and normalize relative paths`() {
        val packageKey = CanonicalSemanticIdentityBuilder.packageKey(PackageIdentifier("com.controls", "1.2.0"))

        assertEquals("com.controls|1.2.0", packageKey.value)
        assertEquals(
            CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/main.athena"),
            CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels\\draft/../main.athena"),
        )
        assertEquals(
            "source:18:com.controls|1.2.018:panels/main.athena",
            CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/main.athena").value,
        )
        assertTrue(CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/Main.athena").value.endsWith("Main.athena"))
        assertNotEquals(
            CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/Main.athena"),
            CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/main.athena"),
        )
        assertFailsWith<IllegalArgumentException> { CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "../outside.athena") }
        assertFailsWith<IllegalArgumentException> { CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "C:\\outside.athena") }
        assertFailsWith<IllegalArgumentException> { CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "C:outside.athena") }
        assertFailsWith<IllegalArgumentException> { CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "/outside.athena") }
        assertFailsWith<IllegalArgumentException> { CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/") }
        assertFailsWith<IllegalArgumentException> { CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/.") }
        assertFailsWith<IllegalArgumentException> { CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "panels/main.athena/..") }
    }

    @Test
    fun `declaration namespace and binding identities change with canonical components`() {
        val packageKey = CanonicalSemanticIdentityBuilder.packageKey(PackageIdentifier("com.controls"))
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "main.athena")
        val declaration = CanonicalSemanticIdentityBuilder.declarationId(sourceUnitId, "DEVICE", listOf("Demo", "PLC1"))
        val namespace = CanonicalSemanticIdentityBuilder.namespaceId(packageKey, listOf("com", "controls"))
        val span = SourceSpan(SourcePosition(10, 2, 3), SourcePosition(18, 2, 11))
        val binding = CanonicalSemanticIdentityBuilder.bindingId(sourceUnitId, span, declaration)
        val otherSourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "other.athena")
        val otherDeclaration = CanonicalSemanticIdentityBuilder.declarationId(sourceUnitId, "device", listOf("Demo", "PLC2"))

        assertEquals("declaration:37:source:13:com.controls|11:main.athena6:device9:Demo.PLC1", declaration.value)
        assertEquals("namespace:13:com.controls|12:com.controls", namespace.value)
        assertEquals(
            "binding:37:source:13:com.controls|11:main.athena2:102:1871:" +
                "declaration:37:source:13:com.controls|11:main.athena6:device9:Demo.PLC1",
            binding.value,
        )
        assertEquals(declaration, CanonicalSemanticIdentityBuilder.declarationId(sourceUnitId, "device", listOf("Demo", "PLC1")))
        assertNotEquals(declaration, CanonicalSemanticIdentityBuilder.declarationId(sourceUnitId, "port", listOf("Demo", "PLC1")))
        assertNotEquals(declaration, otherDeclaration)
        assertNotEquals(declaration, CanonicalSemanticIdentityBuilder.declarationId(otherSourceUnitId, "device", listOf("Demo", "PLC1")))
        assertNotEquals(namespace, CanonicalSemanticIdentityBuilder.namespaceId(packageKey, listOf("com", "drives")))
        assertNotEquals(
            namespace,
            CanonicalSemanticIdentityBuilder.namespaceId(
                CanonicalSemanticIdentityBuilder.packageKey(PackageIdentifier("com.other")),
                listOf("com", "controls"),
            ),
        )
        assertEquals(binding, CanonicalSemanticIdentityBuilder.bindingId(sourceUnitId, span, declaration))
        assertNotEquals(binding, CanonicalSemanticIdentityBuilder.bindingId(otherSourceUnitId, span, declaration))
        assertNotEquals(binding, CanonicalSemanticIdentityBuilder.bindingId(sourceUnitId, span, otherDeclaration))
        assertNotEquals(
            binding,
            CanonicalSemanticIdentityBuilder.bindingId(
                sourceUnitId,
                SourceSpan(SourcePosition(11, 2, 4), SourcePosition(18, 2, 11)),
                declaration,
            ),
        )
        assertNotEquals(
            binding,
            CanonicalSemanticIdentityBuilder.bindingId(
                sourceUnitId,
                SourceSpan(SourcePosition(10, 2, 3), SourcePosition(19, 2, 12)),
                declaration,
            ),
        )
        assertEquals(
            "09c1ccbca7477e82c6fd6d43269f54d733a127665efb35495766b4411d15fe9a",
            CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, "system Root {}").contentHash,
        )
    }

    @Test
    fun `graph identity is order independent and changes only with graph or source content identity`() {
        val root = CanonicalSemanticIdentityBuilder.packageKey(PackageIdentifier("com.root", "1"))
        val dependency = CanonicalSemanticIdentityBuilder.packageKey(PackageIdentifier("com.controls", "2"))
        val rootUnit = CanonicalSemanticIdentityBuilder.sourceUnitId(root, "main.athena")
        val dependencyUnit = CanonicalSemanticIdentityBuilder.sourceUnitId(dependency, "controls.athena")
        val packages = listOf(
            GraphPackageIdentity(root, "src", listOf(dependency)),
            GraphPackageIdentity(dependency, "vendor/controls/src", emptyList()),
        )
        val sourceContents = listOf(
            CanonicalSemanticIdentityBuilder.sourceContentIdentity(rootUnit, "system Root {}"),
            CanonicalSemanticIdentityBuilder.sourceContentIdentity(dependencyUnit, "system Controls {}"),
        )

        val first = CanonicalSemanticIdentityBuilder.graphId(root, packages, sourceContents)
        val reordered = CanonicalSemanticIdentityBuilder.graphId(root, packages.reversed(), sourceContents.reversed())
        val changedContent = CanonicalSemanticIdentityBuilder.graphId(
            root,
            packages,
            listOf(sourceContents[0], CanonicalSemanticIdentityBuilder.sourceContentIdentity(dependencyUnit, "system Controls { device PLC1 {} }")),
        )
        val changedGraph = CanonicalSemanticIdentityBuilder.graphId(
            root,
            packages.map { if (it.packageKey == root) it.copy(directDependencies = emptyList()) else it },
            sourceContents,
        )
        val changedRoot = CanonicalSemanticIdentityBuilder.graphId(dependency, packages, sourceContents)
        val changedSourceRoot = CanonicalSemanticIdentityBuilder.graphId(
            root,
            packages.map { if (it.packageKey == dependency) it.copy(sourceRoot = "vendor/controls/source") else it },
            sourceContents,
        )
        val changedSourceUnit = CanonicalSemanticIdentityBuilder.graphId(
            root,
            packages,
            listOf(
                sourceContents[0],
                CanonicalSemanticIdentityBuilder.sourceContentIdentity(
                    CanonicalSemanticIdentityBuilder.sourceUnitId(dependency, "renamed.athena"),
                    "system Controls {}",
                ),
            ),
        )

        assertEquals(first, reordered)
        assertNotEquals(first, changedContent)
        assertNotEquals(first, changedGraph)
        assertNotEquals(first, changedRoot)
        assertNotEquals(first, changedSourceRoot)
        assertNotEquals(first, changedSourceUnit)
        assertTrue(first.value.matches(Regex("graph:[0-9a-f]{64}")))
    }

    @Test
    fun `graph identity rejects dependency edges outside the resolved package graph`() {
        val root = CanonicalSemanticIdentityBuilder.packageKey(PackageIdentifier("com.root", "1"))
        val missing = CanonicalSemanticIdentityBuilder.packageKey(PackageIdentifier("com.missing", "1"))

        assertFailsWith<IllegalArgumentException> {
            CanonicalSemanticIdentityBuilder.graphId(
                rootPackageKey = root,
                packages = listOf(GraphPackageIdentity(root, "src", listOf(missing))),
                sourceContents = emptyList(),
            )
        }
    }
}
