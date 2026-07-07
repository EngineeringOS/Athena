package com.engineeringood.athena.apps.composeviewer

import com.engineeringood.athena.composeruntime.AthenaComposeShellDescriptor
import com.engineeringood.athena.composeruntime.AthenaSemanticViewerComponentBox
import com.engineeringood.athena.composeruntime.AthenaSemanticViewerConnectionLine
import com.engineeringood.athena.composeruntime.AthenaSemanticViewerScene
import com.engineeringood.athena.runtime.AthenaRuntime
import com.engineeringood.athena.runtime.AthenaRuntimeViewerProjection
import com.engineeringood.athena.runtime.AthenaRuntimeViewerReadyProjection
import com.engineeringood.athena.runtime.AthenaRuntimeViewerUnavailableProjection
import com.engineeringood.athena.runtime.projectViewerProjection
import java.nio.file.Files
import java.nio.file.Path

/**
 * Centralizes bootstrap metadata for the desktop Compose viewer shell.
 */
object AthenaComposeViewerBootstrap {
    /**
     * Loads the default runtime-managed project snapshot used by the desktop viewer proof.
     */
    fun loadDefaultProjectSnapshot(repoRoot: Path = resolveRepoRoot()): AthenaComposeViewerProjectSnapshot {
        val normalizedRepoRoot = repoRoot.toAbsolutePath().normalize()
        val sourcePath = normalizedRepoRoot.resolve(DEFAULT_PROJECT_SOURCE).normalize()
        return loadProjectSnapshot(
            workspaceRoot = normalizedRepoRoot,
            projectName = DEFAULT_PROJECT_NAME,
            sourcePath = sourcePath,
        )
    }

    /**
     * Opens the default runtime-backed workbench session used by the desktop viewer application.
     */
    fun openDefaultWorkbenchSession(repoRoot: Path = resolveRepoRoot()): AthenaComposeViewerWorkbenchSession {
        val normalizedRepoRoot = repoRoot.toAbsolutePath().normalize()
        val sourcePath = normalizedRepoRoot.resolve(DEFAULT_PROJECT_SOURCE).normalize()
        return openWorkbenchSession(
            workspaceRoot = normalizedRepoRoot,
            projectName = DEFAULT_PROJECT_NAME,
            sourcePath = sourcePath,
        )
    }

    /**
     * Loads one runtime-managed project snapshot for the desktop viewer shell.
     */
    fun loadProjectSnapshot(
        workspaceRoot: Path,
        projectName: String,
        sourcePath: Path,
    ): AthenaComposeViewerProjectSnapshot {
        val normalizedWorkspaceRoot = workspaceRoot.toAbsolutePath().normalize()
        val normalizedSourcePath = sourcePath.toAbsolutePath().normalize()
        require(normalizedSourcePath.startsWith(normalizedWorkspaceRoot)) {
            "Compose viewer bootstrap source must stay inside the runtime workspace root: $normalizedSourcePath"
        }
        require(Files.exists(normalizedSourcePath)) {
            "Compose viewer bootstrap source does not exist: $normalizedSourcePath"
        }
        require(Files.isRegularFile(normalizedSourcePath)) {
            "Compose viewer bootstrap source is not a file: $normalizedSourcePath"
        }

        val runtime = AthenaRuntime()
        val workspace = runtime.openWorkspace(normalizedWorkspaceRoot)
        val context = workspace.activateProject(
            projectName = projectName,
            sourcePath = normalizedSourcePath,
        )
        return snapshotFromProjection(
            projection = context.projectViewerProjection(),
            sourcePath = normalizedWorkspaceRoot.relativize(normalizedSourcePath).toString().replace('\\', '/'),
            sourceText = Files.readString(normalizedSourcePath),
        )
    }

    /**
     * Opens one runtime-backed workbench session for the supplied source file.
     */
    fun openWorkbenchSession(
        workspaceRoot: Path,
        projectName: String,
        sourcePath: Path,
    ): AthenaComposeViewerWorkbenchSession {
        return AthenaComposeViewerWorkbenchSession.open(
            workspaceRoot = workspaceRoot,
            projectName = projectName,
            sourcePath = sourcePath,
        )
    }

    /**
     * Returns the shared descriptor used by the desktop shell bootstrap.
     */
    fun shellDescriptor(
        projectName: String,
        scene: AthenaSemanticViewerScene?,
        unavailableReason: String? = null,
    ): AthenaComposeShellDescriptor {
        val statusLine = if (scene == null) {
            "Viewing runtime project $projectName. Viewer unavailable: ${unavailableReason ?: "no semantic scene derived"}."
        } else {
            "Viewing ${scene.systemName} from runtime project $projectName."
        }
        return AthenaComposeShellDescriptor(
            windowTitle = "Athena",
            statusLine = statusLine,
        )
    }

    /**
     * Returns a deterministic message for non-interactive smoke verification.
     */
    fun smokeMessage(repoRoot: Path = resolveRepoRoot()): String {
        val snapshot = loadDefaultProjectSnapshot(repoRoot)
        return AthenaComposeViewerSmokeVerifier.verify(snapshot)
    }

    /**
     * Returns a deterministic message for the scripted operator-proof flow.
     */
    fun operatorProofMessage(repoRoot: Path = resolveRepoRoot()): String {
        val session = openDefaultWorkbenchSession(repoRoot)
        return AthenaComposeViewerOperatorProofVerifier.verify(session)
    }

    private fun snapshotFromProjection(
        projection: AthenaRuntimeViewerProjection,
        sourcePath: String,
        sourceText: String,
    ): AthenaComposeViewerProjectSnapshot {
        return when (projection) {
            is AthenaRuntimeViewerReadyProjection -> {
                val scene = AthenaSemanticViewerScene(
                    systemName = projection.scene.systemName,
                    canvasWidth = projection.scene.canvasWidth,
                    canvasHeight = projection.scene.canvasHeight,
                    components = projection.scene.components.map { box ->
                        AthenaSemanticViewerComponentBox(
                            semanticId = box.semanticId,
                            label = box.label,
                            x = box.x,
                            y = box.y,
                            width = box.width,
                            height = box.height,
                        )
                    },
                    connections = projection.scene.connections.map { connection ->
                        AthenaSemanticViewerConnectionLine(
                            semanticId = connection.semanticId,
                            x1 = connection.x1,
                            y1 = connection.y1,
                            x2 = connection.x2,
                            y2 = connection.y2,
                        )
                    },
                )
                AthenaComposeViewerProjectSnapshot(
                    projectName = projection.projectName,
                    descriptor = shellDescriptor(projectName = projection.projectName, scene = scene),
                    scene = scene,
                    sourcePath = sourcePath,
                    sourceText = sourceText,
                )
            }

            is AthenaRuntimeViewerUnavailableProjection -> AthenaComposeViewerProjectSnapshot(
                projectName = projection.projectName,
                descriptor = shellDescriptor(
                    projectName = projection.projectName,
                    scene = null,
                    unavailableReason = projection.reason,
                ),
                scene = null,
                sourcePath = sourcePath,
                sourceText = sourceText,
            )
        }
    }

    private fun resolveRepoRoot(): Path {
        val explicitRoot = System.getProperty(REPO_ROOT_PROPERTY)
            ?.takeIf(String::isNotBlank)
            ?.let(Path::of)
            ?.toAbsolutePath()
            ?.normalize()
        if (explicitRoot != null) {
            return explicitRoot
        }

        val environmentRoot = System.getenv(REPO_ROOT_ENV)
            ?.takeIf(String::isNotBlank)
            ?.let(Path::of)
            ?.toAbsolutePath()
            ?.normalize()
        if (environmentRoot != null) {
            return environmentRoot
        }

        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        check(Files.exists(current.resolve("settings.gradle.kts"))) {
            "Could not locate repository root. Set `$REPO_ROOT_PROPERTY` or `$REPO_ROOT_ENV` for desktop bootstrap."
        }
        return current
    }
}

private const val DEFAULT_PROJECT_NAME = "operator-proof"
private const val DEFAULT_PROJECT_SOURCE = "examples/m2/operator-proof.athena"
private const val REPO_ROOT_PROPERTY = "athena.repoRoot"
private const val REPO_ROOT_ENV = "ATHENA_REPO_ROOT"
