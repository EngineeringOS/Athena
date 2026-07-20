plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:document-projection-model"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:routing-model"))
    implementation(project(":kernel:representation-model"))
}
