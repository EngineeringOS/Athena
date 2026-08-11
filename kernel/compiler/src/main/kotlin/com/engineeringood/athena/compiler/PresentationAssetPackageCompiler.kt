package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.AdmittedSceneAsset
import com.engineeringood.athena.presentation.AssetAdmissionInput
import com.engineeringood.athena.presentation.AssetAdmissionResult
import com.engineeringood.athena.presentation.AssetMediaKind
import com.engineeringood.athena.presentation.PresentationAssetCompiler
import com.engineeringood.athena.presentation.SceneDiagnostic
import com.engineeringood.athena.repository.RepositoryLockedPackage
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.security.MessageDigest

data class PresentationAssetPackageCompilationResult(
    val admitted: List<AdmittedSceneAsset>,
    val admittedByPackageResource: Map<PackageResourceKey, AdmittedSceneAsset>,
    val diagnostics: List<SceneDiagnostic>,
)

data class PackageResourceKey(
    val packageId: PackageIdentifier,
    val resourcePath: String,
)

/** Materializes only lock-admitted package resources into renderer-neutral assets. */
class PresentationAssetPackageCompiler {
    fun compile(
        repositoryRoot: Path,
        packageSnapshots: List<RepositoryLockedPackage>,
    ): PresentationAssetPackageCompilationResult {
        val admitted = mutableListOf<AdmittedSceneAsset>()
        val admittedByPackageResource = linkedMapOf<PackageResourceKey, AdmittedSceneAsset>()
        val diagnostics = mutableListOf<SceneDiagnostic>()
        packageSnapshots.sortedWith(compareBy({ it.packageId.name }, { it.packageId.version.orEmpty() })).forEach { snapshot ->
            val packageRoot = resolveWithin(repositoryRoot, snapshot.sourceRoot)
            if (packageRoot == null || !Files.isDirectory(packageRoot, LinkOption.NOFOLLOW_LINKS)) {
                diagnostics += diagnostic(snapshot.sourceRoot, "Locked package source root is missing or outside repository root.", "Restore the locked package source root before opening the engineering document.", "asset.resource.root-invalid")
                return@forEach
            }
            snapshot.resourceHashes.sortedBy { it.path }.forEach { resource ->
                if (!resource.path.split('/').any { it.equals("resources", ignoreCase = true) }) {
                    diagnostics += diagnostic(resource.path, "Locked presentation resource is outside the package resources directory.", "Keep presentation resources under a package resources directory.", "asset.resource.path-invalid")
                    return@forEach
                }
                val resourcePath = resolveWithin(packageRoot, resource.path)
                if (resourcePath == null || !resourcePath.startsWith(packageRoot) || !isRegularFileWithoutLinks(packageRoot, resourcePath)) {
                    diagnostics += diagnostic(resource.path, "Locked presentation resource is missing or outside its package root.", "Restore the exact locked resource path under the package resources directory.", "asset.resource.missing")
                    return@forEach
                }
                val bytes = Files.readAllBytes(resourcePath)
                val actualHash = "sha256:${sha256(bytes)}"
                if (actualHash != resource.hash) {
                    diagnostics += diagnostic(resource.path, "Locked presentation resource bytes do not match their admitted digest.", "Restore the locked bytes or rematerialize the repository lock.", "asset.resource.digest-mismatch")
                    return@forEach
                }
                val kind = mediaKind(resource.path)
                if (kind == null) {
                    diagnostics += diagnostic(resource.path, "Locked resource extension is not a supported presentation asset.", "Use `.svg`, `.png`, or `.woff2` under the package resources directory.", "asset.resource.type-invalid")
                    return@forEach
                }
                when (val result = PresentationAssetCompiler.admit(AssetAdmissionInput(resource.path, kind, bytes))) {
                    is AssetAdmissionResult.Admitted -> {
                        admitted += result.asset
                        admittedByPackageResource[PackageResourceKey(snapshot.packageId, resource.path)] = result.asset
                    }
                    is AssetAdmissionResult.Rejected -> diagnostics += result.diagnostic
                }
            }
        }
        return PresentationAssetPackageCompilationResult(admitted, admittedByPackageResource, diagnostics)
    }

    private fun mediaKind(path: String): AssetMediaKind? = when (path.substringAfterLast('.', "").lowercase()) {
        "svg" -> AssetMediaKind.SVG
        "png" -> AssetMediaKind.PNG
        "woff2" -> AssetMediaKind.WOFF2
        else -> null
    }

    private fun resolveWithin(root: Path, relative: String): Path? {
        val candidate = runCatching { Path.of(relative) }.getOrNull() ?: return null
        if (candidate.isAbsolute || candidate.any { it.toString() == ".." }) return null
        val normalizedRoot = root.toAbsolutePath().normalize()
        val normalized = normalizedRoot.resolve(candidate).normalize()
        return normalized.takeIf { it.startsWith(normalizedRoot) }
    }

    private fun isRegularFileWithoutLinks(root: Path, candidate: Path): Boolean {
        if (!Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)) return false
        var current = root
        for (part in root.relativize(candidate)) {
            current = current.resolve(part)
            if (Files.isSymbolicLink(current)) return false
        }
        return true
    }

    private fun diagnostic(subject: String, problem: String, correction: String, code: String) =
        SceneDiagnostic(subject, problem, correction, code)
}

private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString("") { "%02x".format(it) }
