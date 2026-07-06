plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:svg-renderer"))

    testImplementation(project(":extensions:domain-electrical"))
}
