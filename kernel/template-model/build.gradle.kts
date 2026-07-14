plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:component-model"))
    implementation(project(":kernel:part-model"))
    implementation(project(":kernel:connection-model"))
    implementation(project(":kernel:reuse-model"))
}
