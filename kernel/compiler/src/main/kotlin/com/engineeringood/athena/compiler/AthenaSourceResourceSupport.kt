package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.RepresentationResourceDeclaration
import com.engineeringood.athena.language.RepresentationResourceKind
import com.engineeringood.athena.packageplatform.GraphicResourceId
import com.engineeringood.athena.packageplatform.GraphicResourceKind
import com.engineeringood.athena.packageplatform.PackageResourceDeclaration
import com.engineeringood.athena.packageplatform.PackageResourceKey
import java.nio.file.Files
import java.nio.file.Path

internal data class ResolvedSourceResource(
    val declaration: RepresentationResourceDeclaration,
    val packageResource: PackageResourceDeclaration,
    val resolvedPath: Path,
)

internal fun RepresentationResourceDeclaration.resolveSourceLocalSvg(
    sourceFile: String,
): ResolvedSourceResource? {
    if (kind != RepresentationResourceKind.SVG) return null
    val resolvedPath = resolvePackageLocalSvgPath(sourceFile, path.value) ?: return null
    return ResolvedSourceResource(
        declaration = this,
        packageResource = PackageResourceDeclaration(
            key = PackageResourceKey(
                sourceUnitId = sourceFile,
                resourceId = GraphicResourceId(id),
            ),
            kind = GraphicResourceKind.VECTOR_DOCUMENT,
            path = path.value,
        ),
        resolvedPath = resolvedPath,
    )
}

internal fun resolvePackageLocalSvgPath(
    sourceFile: String,
    authoredPath: String,
): Path? {
    if (!authoredPath.isSafeRelativeSvgPath()) return null
    val sourcePath = Path.of(sourceFile)
    val root = (sourcePath.parent ?: Path.of(".")).toAbsolutePath().normalize()
    val resolved = root.resolve(authoredPath).normalize()
    if (!resolved.startsWith(root)) return null
    if (!Files.exists(resolved)) return resolved

    val realRoot = runCatching { root.toRealPath() }.getOrNull() ?: return null
    val realResolved = runCatching { resolved.toRealPath() }.getOrNull() ?: return null
    if (!realResolved.startsWith(realRoot)) return null

    var current = root
    root.relativize(resolved).forEach { segment ->
        current = current.resolve(segment)
        if (Files.isSymbolicLink(current)) return null
    }
    return resolved
}
