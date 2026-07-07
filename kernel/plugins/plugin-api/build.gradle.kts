plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:language"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:validation"))
}
