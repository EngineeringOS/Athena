plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:compiler"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:repository-model"))
    implementation(project(":kernel:semantic-scm"))
    implementation(project(":kernel:validation"))
}
