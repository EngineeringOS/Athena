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
    //":apps:desktop-viewer",
    ":integrations:scm-git",
    ":ide:lsp",
     //":ui:compose-workbench",
    ":kernel:runtime",
    ":kernel:language",
    ":kernel:repository-model",
    ":kernel:semantic-scm",
    ":kernel:plugins:plugin-api",
    ":kernel:plugins:plugin-host",
    ":kernel:engineering-model",
    ":kernel:component-model",
    ":kernel:part-model",
    ":kernel:connection-model",
    ":kernel:physical-model",
    ":kernel:authoring-model",
    ":kernel:interaction-model",
    ":kernel:reuse-model",
    ":kernel:template-model",
    ":kernel:spatial-model",
    ":kernel:layout-model",
    ":kernel:layout-engine",
    ":kernel:routing-model",
    ":kernel:document-projection-model",
    ":kernel:representation-model",
    ":kernel:package-model",
    ":kernel:package-runtime",
    ":kernel:presentation-policy-model",
    ":kernel:geometry-model",
    ":kernel:projection-model",
    ":kernel:presentation-model",
    ":kernel:validation",
    ":kernel:compiler",
    ":extensions:domain-electrical",
    ":extensions:domain-dummy",
    ":kernel:svg-renderer",
)
