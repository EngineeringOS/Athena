plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:geometry-model"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:presentation-model"))
    implementation(project(":kernel:representation-model"))
}
