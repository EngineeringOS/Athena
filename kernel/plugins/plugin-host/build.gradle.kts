plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:plugins:plugin-api"))
    implementation(project(":kernel:layout-model"))

    testImplementation(project(":extensions:domain-dummy"))
    testImplementation(project(":extensions:domain-electrical"))
    testImplementation(project(":kernel:runtime"))
}

