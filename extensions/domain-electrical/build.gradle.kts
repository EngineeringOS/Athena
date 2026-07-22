plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:authoring-model"))
    implementation(project(":kernel:plugins:plugin-api"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:presentation-model"))
    implementation(project(":kernel:semantic-scm"))
    implementation(project(":kernel:validation"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:interaction-model"))
    implementation(project(":kernel:component-model"))
    implementation(project(":kernel:part-model"))
    implementation(project(":kernel:connection-model"))
    implementation(project(":kernel:physical-model"))
    implementation(project(":kernel:runtime"))
    implementation(project(":kernel:representation-model"))
}

