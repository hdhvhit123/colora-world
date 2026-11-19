plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.colora.world"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.colora.world"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("offlineFull") {
            dimension = "distribution"
            isDefault = true
            buildConfigField("boolean", "ALL_UNLOCKED", "true")
            buildConfigField("boolean", "BILLING", "false")
        }
        create("store") {
            dimension = "distribution"
            applicationIdSuffix = ".store"
            buildConfigField("boolean", "ALL_UNLOCKED", "false")
            buildConfigField("boolean", "BILLING", "true")
            // Base64 RSA licence key from Play Console (Monetize > Licensing). Empty = no grants (K-05).
            buildConfigField("String", "PLAY_LICENSE_KEY", "\"${(project.findProperty("colora.playLicenseKey") ?: "")}\"")
        }
    }

    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // No signing config: release signing needs the owner's upload key (KNOWN_ISSUES K-05).
        }
    }

    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    androidResources { noCompress += listOf("ogg", "json") }
    testOptions { unitTests { isIncludeAndroidResources = true; all { it.maxHeapSize = "3g"; it.forkEvery = 1 /* fresh JVM per class: Robolectric sandboxes otherwise share Compose state between activities */; it.systemProperty("colora.shots", rootProject.file("screenshots").absolutePath)
        it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
        it.systemProperty("robolectric.dependency.repo.url", "https://maven-central.storage-download.googleapis.com/maven2/") } } }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    lint { abortOnError = false; checkReleaseBuilds = false }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    implementation(project(":core:storage"))
    implementation(project(":core:engine"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    "storeImplementation"(libs.play.billing)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
