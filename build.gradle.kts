import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    base
    kotlin("jvm") version "2.4.0" apply false
}

group = "com.engineeringood.athena"
version = "0.0.1-SNAPSHOT"

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

    apply(plugin = "org.jetbrains.kotlin.jvm")

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        add("testImplementation", kotlin("test-junit5"))
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(verifyJava25)
    dependsOn(subprojects.map { "${it.path}:${LifecycleBasePlugin.CHECK_TASK_NAME}" })
}

tasks.named(LifecycleBasePlugin.BUILD_TASK_NAME) {
    dependsOn(verifyJava25)
    dependsOn(subprojects.map { "${it.path}:${LifecycleBasePlugin.BUILD_TASK_NAME}" })
}

tasks.register("test") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs tests for all Athena modules."
    dependsOn(verifyJava25)
    dependsOn(subprojects.map { "${it.path}:test" })
}

tasks.wrapper {
    gradleVersion = "9.6.1"
}
