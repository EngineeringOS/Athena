plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation(project(":integrations:scm-git"))
    implementation(project(":kernel:authoring-model"))
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:component-model"))
    implementation(project(":kernel:connection-model"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:part-model"))
    implementation(project(":kernel:physical-model"))
    implementation(project(":kernel:presentation-model"))
    implementation(project(":kernel:repository-model"))
    implementation(project(":kernel:runtime"))
    implementation(project(":kernel:semantic-scm"))
    implementation(project(":kernel:validation"))
    implementation(project(":extensions:domain-dummy"))
    implementation(project(":extensions:domain-electrical"))
    implementation(libs.lsp4j)
}

application {
    applicationName = "athena-lsp-host"
    mainClass = "com.engineeringood.athena.ide.lsp.MainKt"
}

tasks.named("build") {
    dependsOn(tasks.named("installDist"))
}
