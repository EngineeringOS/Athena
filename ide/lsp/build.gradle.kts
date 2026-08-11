plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation(project(":integrations:scm-git"))
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:connection-model"))
    implementation(project(":kernel:document-projection-model"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:package-model"))
    implementation(project(":kernel:package-runtime"))
    implementation(project(":kernel:physical-model"))
    implementation(project(":kernel:projection-model"))
    implementation(project(":kernel:repository-model"))
    implementation(project(":kernel:runtime"))
    implementation(project(":kernel:presentation-model"))
    implementation(project(":kernel:interaction-model"))
    implementation(project(":kernel:semantic-scm"))
    implementation(project(":kernel:spatial-model"))
    implementation(project(":kernel:validation"))
    testImplementation(project(":kernel:knowledge-model"))
    testImplementation(project(":kernel:svg-renderer"))
    implementation(project(":extensions:domain-dummy"))
    implementation(project(":extensions:domain-electrical"))
    implementation(libs.lsp4j)
}

tasks.register<JavaExec>("generateM45SvgExportEvidence") {
    group = "verification"
    description = "Exports the active M45 READY Canonical Scene through the renderer-neutral SVG seam."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.engineeringood.athena.ide.lsp.M45SvgExportEvidenceGeneratorKt")
    args(rootProject.projectDir.absolutePath)
}

tasks.register<JavaExec>("generateM46SvgExportEvidence") {
    group = "verification"
    description = "Exports the active M46 READY Canonical Scene through the renderer-neutral SVG seam."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.engineeringood.athena.ide.lsp.M46SvgExportEvidenceGeneratorKt")
    args(rootProject.projectDir.absolutePath)
}

application {
    applicationName = "athena-lsp-host"
    mainClass = "com.engineeringood.athena.ide.lsp.MainKt"
}

tasks.named("build") {
    dependsOn(tasks.named("installDist"))
}
