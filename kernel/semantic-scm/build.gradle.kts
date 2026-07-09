plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:repository-model"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:validation"))
}
