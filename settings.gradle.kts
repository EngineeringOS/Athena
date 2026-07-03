pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "Athena"

include(
    ":cli",
    ":language",
    ":semantics-core",
    ":ir",
    ":compiler",
    ":domain-electrical-runtime",
    ":renderer-svg",
)
