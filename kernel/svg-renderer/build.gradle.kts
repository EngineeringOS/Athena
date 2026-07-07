plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:geometry-model"))
}
