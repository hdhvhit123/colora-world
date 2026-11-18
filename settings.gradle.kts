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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Google's Maven Central mirror first: large artifacts on repo1 redirect to a CDN
        // host some sandboxed build environments cannot reach.
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
    }
}
rootProject.name = "colora-world"

include(":core:engine", ":core:storage", ":content-tools")

// The Android app needs the Android SDK. JVM modules (engine, storage, content
// validator) build and test without it, so CI/sessions lacking the SDK can still
// run `./gradlew :core:engine:test :core:storage:test :content-tools:validateContent`.
val sdkAvailable = System.getenv("ANDROID_HOME") != null || System.getenv("ANDROID_SDK_ROOT") != null ||
    file("local.properties").let { it.exists() && it.readText().contains("sdk.dir") }
if (sdkAvailable || providers.gradleProperty("colora.forceApp").isPresent) {
    include(":app")
} else {
    logger.lifecycle("Colora: Android SDK not found, building JVM modules only (see README)")
}
