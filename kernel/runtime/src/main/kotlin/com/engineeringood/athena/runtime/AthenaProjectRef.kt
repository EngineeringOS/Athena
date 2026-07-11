package com.engineeringood.athena.runtime

import java.nio.file.Path

/** Path-backed project handle tracked by the runtime while M1 remains local and in-memory. */
data class AthenaProjectRef(
    val name: String,
    val sourcePath: Path,
    val workspaceRoot: Path,
)
