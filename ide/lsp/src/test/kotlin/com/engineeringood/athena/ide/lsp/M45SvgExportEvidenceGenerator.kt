package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.presentation.PublicationState
import com.engineeringood.athena.svg.AthenaSvgRenderer
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    require(args.size == 1) { "Expected repository root argument." }
    val root = Path.of(args.single()).toAbsolutePath().normalize()
    val example = root.resolve("examples/m45/rolling-shutter")
    val lock = AthenaCompiler().validateRepositoryLock(example)
    require(lock.isValid) { lock.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" } }

    val host = AthenaLspSessionHost()
    try {
        val ready = host.activateRepository(example) as? AthenaLspSessionHostReady
            ?: error("M45 repository did not activate READY.")
        val publication = ready.diagramScenePublication()
        require(publication.state == PublicationState.READY) { publication.diagnostics.joinToString() }
        val scene = requireNotNull(publication.scene)
        val bundle = requireNotNull(publication.assetBundle)
        val first = AthenaSvgRenderer.render(scene, bundle)
        val second = AthenaSvgRenderer.render(scene, bundle)
        require(first == second) { "Repeated M45 SVG export is not deterministic." }

        val exportRoot = root.resolve("_bmad-output/implementation-artifacts/m45/exports")
        Files.createDirectories(exportRoot)
        Files.writeString(exportRoot.resolve("m45-rolling-shutter.svg"), first)
    } finally {
        host.shutdown()
    }
}
