pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CW1"

// Redirect build directory for all projects (root and subprojects) outside of OneDrive to prevent Windows AccessDeniedException file locking issues
gradle.beforeProject {
    val relativePath = if (path == ":") "" else path.replace(':', '/')
    layout.buildDirectory.set(file("C:/AndroidBuilds/CW1/build$relativePath"))
}

include(":app")
 