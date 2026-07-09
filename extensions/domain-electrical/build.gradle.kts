plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:plugins:plugin-api"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:semantic-scm"))
    implementation(project(":kernel:validation"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:runtime"))
}

