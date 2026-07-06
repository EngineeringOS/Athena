plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:svg-renderer"))
    implementation(project(":kernel:validation"))
    testImplementation(project(":kernel:runtime"))
    testImplementation(project(":extensions:domain-electrical"))
}
