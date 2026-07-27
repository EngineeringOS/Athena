package com.engineeringood.athena.packageruntime

import java.nio.file.Files
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RepresentationPackageSnapshotStagerTest {
    @Test
    fun `stages package local athena and svg resources into deterministic immutable snapshot`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val vendorRoot = packageRoot.resolve("athena/vendor").createDirectories()
        val source = vendorRoot.resolve("vendor-drive.athena")
        val svg = vendorRoot.resolve("vendor-drive.svg")
        source.writeText(VALID_SOURCE)
        svg.writeText(VALID_SVG)

        val first = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/first"),
            ),
        )
        val second = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/second"),
            ),
        )

        assertTrue(first.diagnostics.isEmpty(), first.diagnostics.toString())
        assertTrue(second.diagnostics.isEmpty(), second.diagnostics.toString())
        val snapshot = assertNotNull(first.snapshot)
        assertEquals(snapshot.snapshotId, second.snapshot?.snapshotId)
        assertEquals("lock:abc", snapshot.dependencyLockDigest)
        assertEquals(
            listOf("packages/representation/athena/vendor/vendor-drive.athena", "packages/representation/athena/vendor/vendor-drive.svg"),
            snapshot.files.map { it.repositoryRelativePath },
        )
        assertTrue(snapshot.files.all { it.stagedPath.startsWith(snapshot.snapshotRoot) })
        assertTrue(snapshot.files.all { it.contentHash.startsWith("sha256:") })
        assertTrue(snapshot.files.all { file -> file.contentHash == sha256(Files.readAllBytes(file.stagedPath)) })
    }

    @Test
    fun `reusing a snapshot cache keeps old content immutable and excludes removed files`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-history")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val vendorRoot = packageRoot.resolve("athena/vendor").createDirectories()
        val source = vendorRoot.resolve("vendor-drive.athena")
        val svg = vendorRoot.resolve("vendor-drive.svg")
        val snapshotCache = repositoryRoot.resolve(".athena/snapshots/material-resolution")
        source.writeText(VALID_SOURCE)
        svg.writeText(VALID_SVG)

        val first = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = snapshotCache,
            ),
        ).snapshot

        source.writeText(VALID_NATIVE_SOURCE)
        Files.delete(svg)
        val second = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = snapshotCache,
            ),
        ).snapshot

        val firstSnapshot = assertNotNull(first)
        val secondSnapshot = assertNotNull(second)
        assertTrue(firstSnapshot.snapshotRoot != secondSnapshot.snapshotRoot)
        assertTrue(firstSnapshot.snapshotId != secondSnapshot.snapshotId)
        assertEquals(VALID_SOURCE, Files.readString(firstSnapshot.files.single { it.repositoryRelativePath.endsWith(".athena") }.stagedPath))
        assertEquals(VALID_SVG, Files.readString(firstSnapshot.files.single { it.repositoryRelativePath.endsWith(".svg") }.stagedPath))
        assertEquals(listOf("packages/representation/athena/vendor/vendor-drive.athena"), secondSnapshot.files.map { it.repositoryRelativePath })
        assertTrue(Files.notExists(secondSnapshot.snapshotRoot.resolve("packages/representation/athena/vendor/vendor-drive.svg")))
    }

    @Test
    fun `stages roots declared by repository contract and lock digest`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-repository-contract")
        repositoryRoot.resolve("athena.yaml").writeText(
            """
            primaryPackage:
              name: com.engineeringood.m34.sample
              version: 1.0.0
              sourceRoot: src

            representationPackageRoots:
              - packages/representation
            """.trimIndent(),
        )
        repositoryRoot.resolve("athena.lock").writeText("m34-lock")
        val vendorRoot = repositoryRoot.resolve("packages/representation/athena/vendor").createDirectories()
        vendorRoot.resolve("vendor-drive.athena").writeText(VALID_SOURCE)
        vendorRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)

        val result = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = repositoryRoot,
            snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/from-contract"),
        )

        val snapshot = assertNotNull(result.snapshot, result.diagnostics.toString())
        assertEquals(listOf("packages/representation"), snapshot.packageRoots)
        assertTrue(snapshot.dependencyLockDigest.startsWith("sha256:"))
        assertEquals(2, snapshot.files.size)
    }

    @Test
    fun `repository staging accepts the same quoted package root as repository validation`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-repository-quoted-root")
        repositoryRoot.resolve("athena.yaml").writeText(
            """
            primaryPackage:
              name: com.engineeringood.m34.sample
              version: 1.0.0
              sourceRoot: src

            representationPackageRoots:
              - "packages/representation"
            """.trimIndent(),
        )
        repositoryRoot.resolve("athena.lock").writeText("m34-lock")
        val vendorRoot = repositoryRoot.resolve("packages/representation/athena/vendor").createDirectories()
        vendorRoot.resolve("vendor-drive.athena").writeText(VALID_SOURCE)
        vendorRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)

        val result = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = repositoryRoot,
            snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/from-quoted-contract"),
        )

        val snapshot = assertNotNull(result.snapshot, result.diagnostics.toString())
        assertEquals(listOf("packages/representation"), snapshot.packageRoots)
        assertEquals(2, snapshot.files.size)
    }

    @Test
    fun `rejects path escapes and symlink resources before parse admission`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-invalid")
        val outside = Files.createTempDirectory("athena-m34-outside")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val vendorRoot = packageRoot.resolve("athena/vendor").createDirectories()
        vendorRoot.resolve("vendor-drive.athena").writeText(VALID_SOURCE)
        val link = vendorRoot.resolve("vendor-drive.svg")
        try {
            Files.createSymbolicLink(link, outside.resolve("vendor-drive.svg"))
        } catch (_: Exception) {
            link.writeText(VALID_SVG)
        }

        val result = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot, outside),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/invalid"),
            ),
        )

        assertTrue(result.snapshot == null)
        assertTrue(result.diagnostics.any { it.code == "package.snapshot.root.escape" || it.code == "package.snapshot.file.symlink" }, result.diagnostics.toString())
    }

    @Test
    fun `rejects athena package declarations outside matching filesystem hierarchy`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-package-path")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val wrongRoot = packageRoot.resolve("athena/wrong").createDirectories()
        wrongRoot.resolve("vendor-drive.athena").writeText(VALID_SOURCE)
        wrongRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)

        val result = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/wrong-package"),
            ),
        )

        assertTrue(result.snapshot == null)
        val diagnostic = assertNotNull(result.diagnostics.singleOrNull { it.code == "package.snapshot.package-path.mismatch" })
        assertEquals("packages/representation/athena/wrong/vendor-drive.athena", diagnostic.subject)
    }

    @Test
    fun `stages typed resource source without inspecting authored svg syntax`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-locality")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val vendorRoot = packageRoot.resolve("athena/vendor").createDirectories()
        vendorRoot.resolve("vendor-drive.athena").writeText(INVALID_ESCAPING_SVG_SOURCE)
        packageRoot.resolve("athena/vendor-drive.svg").writeText(VALID_SVG)

        val result = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/locality"),
            ),
        )

        assertTrue(result.snapshot != null, result.diagnostics.toString())
        assertTrue(result.diagnostics.none { it.code == "package.snapshot.graphic-svg.path.invalid" }, result.diagnostics.toString())
    }

    companion object {
        private val VALID_SOURCE = """
            package athena.vendor

            element vendor_drive {
              identity "athena.vendor.drive"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()

        private val INVALID_ESCAPING_SVG_SOURCE = """
            package athena.vendor

            element vendor_drive {
              identity "athena.vendor.drive"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "../vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()

        private val VALID_NATIVE_SOURCE = """
            package athena.vendor

            symbol native_drive {
              identity "athena.vendor.native-drive"
              version "1.0.0"
              graphic {
                bounds (0, 0, 10, 10)
                rectangle body at (1, 1) size (8, 8) style symbol
              }
            }
        """.trimIndent()

        private val VALID_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10">
              <rect id="body" x="1" y="1" width="8" height="8"/>
            </svg>
        """.trimIndent()
    }
}

private fun sha256(bytes: ByteArray): String = "sha256:" + MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString("") { byte -> "%02x".format(byte) }
