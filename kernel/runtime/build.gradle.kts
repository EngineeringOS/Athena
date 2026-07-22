plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:authoring-model"))
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:component-model"))
    implementation(project(":kernel:connection-model"))
    implementation(project(":kernel:reuse-model"))
    implementation(project(":kernel:template-model"))
    implementation(project(":kernel:plugins:plugin-api"))
    implementation(project(":kernel:plugins:plugin-host"))
    implementation(project(":kernel:repository-model"))
    implementation(project(":kernel:semantic-scm"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:geometry-model"))
    implementation(project(":kernel:interaction-model"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:part-model"))
    implementation(project(":kernel:physical-model"))
    implementation(project(":kernel:presentation-model"))
    implementation(project(":kernel:projection-model"))
    implementation(project(":kernel:svg-renderer"))
    implementation(project(":kernel:validation"))

    testImplementation(project(":integrations:scm-git"))
    testImplementation(project(":extensions:domain-dummy"))
    testImplementation(project(":extensions:domain-electrical"))
}

