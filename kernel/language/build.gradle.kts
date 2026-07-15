import org.gradle.api.plugins.antlr.AntlrTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    antlr
}

dependencies {
    antlr(libs.antlr)
    implementation(libs.antlr.runtime)
}

val generatedAntlrSources = layout.buildDirectory.dir("generated-src/antlr/main")

tasks.named<AntlrTask>("generateGrammarSource") {
    // Package ownership is declared in Athena.g4 via `@header { package ...; }`.
    // Do not also pass `-package`/`packageName` here, or ANTLR nests the package path twice.
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-no-listener")
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.named("generateGrammarSource"))
    source(generatedAntlrSources)
}
