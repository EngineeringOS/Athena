plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:validation"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:runtime"))
}
