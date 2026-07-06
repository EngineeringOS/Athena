import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    base
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
}

group = "com.engineeringood.athena"
version = "0.0.1-SNAPSHOT"

val leafSubprojects = subprojects.filter { it.childProjects.isEmpty() }

val verifyJava25 = tasks.register("verifyJava25") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Verifies that Athena is running on Java 25."

    doLast {
        check(JavaVersion.current() == JavaVersion.VERSION_25) {
            "Athena M0 requires Java 25. Current runtime: ${System.getProperty("java.version")} at ${System.getProperty("java.home")}"
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(25)
        }

        dependencies {
            add("testImplementation", libs.kotlin.testJunit5)
            add("testRuntimeOnly", libs.junit.platform.launcher)
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(25)
        }
    }
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(verifyJava25)
    dependsOn(leafSubprojects.map { "${it.path}:${LifecycleBasePlugin.CHECK_TASK_NAME}" })
}

tasks.named(LifecycleBasePlugin.BUILD_TASK_NAME) {
    dependsOn(verifyJava25)
    dependsOn(leafSubprojects.map { "${it.path}:${LifecycleBasePlugin.BUILD_TASK_NAME}" })
}

tasks.register("test") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs tests for all Athena modules."
    dependsOn(verifyJava25)
    dependsOn(leafSubprojects.map { "${it.path}:test" })
}

tasks.wrapper {
    gradleVersion = "9.6.1"
}
