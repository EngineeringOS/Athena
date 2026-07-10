pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Athena"

include(
    ":apps:cli",
    ":apps:desktop-viewer",
    ":integrations:scm-git",
    ":ide:lsp",
    ":ui:compose-workbench",
    ":kernel:runtime",
    ":kernel:language",
    ":kernel:repository-model",
    ":kernel:semantic-scm",
    ":kernel:plugins:plugin-api",
    ":kernel:plugins:plugin-host",
    ":kernel:engineering-model",
    ":kernel:layout-model",
    ":kernel:geometry-model",
    ":kernel:projection-model",
    ":kernel:validation",
    ":kernel:compiler",
    ":extensions:domain-electrical",
    ":extensions:domain-dummy",
    ":kernel:svg-renderer",
)

