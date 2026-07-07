import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val java25Launcher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(25))
}
val java25Home = java25Launcher.map { launcher ->
    launcher.metadata.installationPath.asFile.absolutePath
}

dependencies {
    implementation(project(":ui:compose-workbench"))
    implementation(project(":extensions:domain-dummy"))
    implementation(project(":extensions:domain-electrical"))
    implementation(project(":kernel:runtime"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.engineeringood.athena.apps.composeviewer.MainKt"
        javaHome = java25Home.get()
        // Keep the first desktop proof stable on Windows and Java 25.
        jvmArgs(
            "--enable-native-access=ALL-UNNAMED",
            "-Dskiko.renderApi=SOFTWARE",
        )
    }
}

tasks.register<JavaExec>("bootstrapSmoke") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Launches the Compose viewer entrypoint in non-interactive bootstrap smoke mode."
    dependsOn("jar")
    classpath(files(tasks.named("jar"), configurations.named("runtimeClasspath")))
    mainClass.set("com.engineeringood.athena.apps.composeviewer.MainKt")
    javaLauncher.set(java25Launcher)
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "-Dskiko.renderApi=SOFTWARE",
    )
    systemProperty("athena.compose.bootstrap.smoke", "true")
    systemProperty("athena.repoRoot", rootProject.projectDir.absolutePath)
    workingDir = rootProject.projectDir
}

tasks.register<JavaExec>("operatorProofSmoke") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Launches the Compose viewer entrypoint in scripted operator-proof smoke mode."
    dependsOn("jar")
    classpath(files(tasks.named("jar"), configurations.named("runtimeClasspath")))
    mainClass.set("com.engineeringood.athena.apps.composeviewer.MainKt")
    javaLauncher.set(java25Launcher)
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "-Dskiko.renderApi=SOFTWARE",
    )
    systemProperty("athena.compose.operatorProof.smoke", "true")
    systemProperty("athena.repoRoot", rootProject.projectDir.absolutePath)
    workingDir = rootProject.projectDir
}
