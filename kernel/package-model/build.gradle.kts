plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:repository-model"))
}
