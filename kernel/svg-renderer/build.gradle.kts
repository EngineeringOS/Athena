plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:presentation-model"))
    testImplementation(project(":kernel:compiler"))
    testImplementation(project(":kernel:projection-model"))
    testImplementation(project(":kernel:spatial-model"))
    testImplementation(project(":extensions:domain-dummy"))
    testImplementation(project(":extensions:domain-electrical"))
}

tasks.register<JavaExec>("generateRollingShutterSvgGolden") {
    group = "verification"
    description = "Regenerates the active M44 rolling-shutter SVG oracle from canonical scene facts."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.engineeringood.athena.svg.ActiveSceneSvgGoldenGeneratorKt")
    args(rootProject.projectDir.absolutePath)
}
