package com.engineeringood.athena.svg

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.AthenaDiagramSceneCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.presentation.AssetBundle
import com.engineeringood.athena.presentation.InputRevision
import java.nio.file.Files
import java.nio.file.Path

/** Deterministically refreshes only the active M44 rolling-shutter SVG contract artifact. */
fun main(args: Array<String>) {
    val root = Path.of(args.single()).toAbsolutePath().normalize()
    val source = root.resolve("examples/m44/rolling-shutter/src/com/engineeringood/m44/rollingshutter/rolling-shutter.athena")
    val revision = InputRevision("input:sha256:${"8".repeat(64)}")
    val compilation = AthenaCompiler().compile(source) as? CompilerCompilationSuccess
        ?: error("Active M44 rolling-shutter source did not compile.")
    val scene = AthenaDiagramSceneCompiler().compile(
        compilation.projections.single(),
        compilation.spatialDocuments.single(),
        revision,
    ).scene ?: error("Active M44 rolling-shutter scene did not compile.")
    Files.writeString(
        root.resolve("contracts/presentation/v1/render/rolling-shutter.svg"),
        AthenaSvgRenderer.render(scene, AssetBundle(revision, emptyList())),
    )
}
