plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:plugins:plugin-api"))
    implementation(project(":kernel:plugins:plugin-host"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:geometry-model"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:svg-renderer"))

    testImplementation(project(":extensions:domain-dummy"))
    testImplementation(project(":extensions:domain-electrical"))
}

