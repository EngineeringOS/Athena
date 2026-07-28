plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:document-projection-model"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:routing-model"))
    implementation(project(":kernel:representation-model"))
    implementation(project(":kernel:interaction-model"))
    implementation(project(":kernel:authoring-model"))
}
