plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    api(project(":kernel:engineering-model"))
    api(project(":kernel:geometry-model"))
    testImplementation(kotlin("test"))
}
