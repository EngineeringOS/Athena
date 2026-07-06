plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation(project(":kernel:runtime"))
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:language"))
    implementation(project(":extensions:domain-electrical"))
    implementation(project(":kernel:svg-renderer"))
}

application {
    mainClass = "com.engineeringood.athena.cli.MainKt"
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
