plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:package-model"))
    implementation(project(":kernel:repository-model"))
}
