plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:engineering-model"))
    testImplementation(kotlin("test"))
}
