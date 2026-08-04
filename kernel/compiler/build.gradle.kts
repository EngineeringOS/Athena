plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":kernel:authoring-model"))
    implementation(project(":kernel:plugins:plugin-api"))
    implementation(project(":kernel:plugins:plugin-host"))
    implementation(project(":kernel:component-model"))
    implementation(project(":kernel:connection-model"))
    implementation(project(":kernel:repository-model"))
    implementation(project(":kernel:engineering-model"))
    implementation(project(":kernel:geometry-model"))
    implementation(project(":kernel:layout-engine"))
    implementation(project(":kernel:layout-model"))
    implementation(project(":kernel:part-model"))
    implementation(project(":kernel:physical-model"))
    implementation(project(":kernel:spatial-model"))
    implementation(project(":kernel:package-model"))
    implementation(project(":kernel:document-projection-model"))
    implementation(project(":kernel:presentation-model"))
    implementation(project(":kernel:presentation-policy-model"))
    implementation(project(":kernel:package-runtime"))
    implementation(project(":kernel:projection-model"))
    implementation(project(":kernel:representation-model"))
    implementation(project(":kernel:routing-model"))
    implementation(project(":kernel:language"))
    implementation(project(":kernel:svg-renderer"))
    implementation(project(":kernel:validation"))
    testImplementation(project(":kernel:runtime"))
    testImplementation(project(":extensions:domain-dummy"))
    testImplementation(project(":extensions:domain-electrical"))
}

tasks.register<JavaExec>("generateM41SpatialQualityBaseline") {
    group = "verification"
    description = "Generates the canonical M41 Spatial quality baseline from the dedicated fixture."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.engineeringood.athena.compiler.M41SpatialQualityBaselineGenerator")
    doFirst {
        val timestamp = providers.gradleProperty("m41BaselineTimestamp").orNull
            ?: throw GradleException(
                "Missing -Pm41BaselineTimestamp=<UTC ISO-8601 instant>; ambient time is not reproducible.",
            )
        setArgs(listOf(rootProject.projectDir.absolutePath, timestamp))
    }
}

