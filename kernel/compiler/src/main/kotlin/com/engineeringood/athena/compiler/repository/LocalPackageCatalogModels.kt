package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Path

/** Direct project-local native package candidate. Catalog is compiler-owned discovery input. */
data class LocalPackageCatalogEntry(
    val packageId: PackageIdentifier,
    val packageRoot: Path,
    val manifestPath: Path,
)
