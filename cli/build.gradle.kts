plugins {
    application
}

dependencies {
    implementation(project(":compiler"))
    implementation(project(":language"))
    implementation(project(":domain-electrical-runtime"))
    implementation(project(":renderer-svg"))
}

application {
    mainClass = "com.engineeringood.athena.cli.MainKt"
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
