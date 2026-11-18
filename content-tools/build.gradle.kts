plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
dependencies {
    implementation(project(":core:engine"))
    testImplementation(libs.junit)
}
application { mainClass.set("com.colora.world.tools.ValidateContentKt") }

val contentDir = rootProject.layout.projectDirectory.dir("app/src/main/assets/content")
tasks.register<JavaExec>("validateContent") {
    group = "verification"
    description = "Validates the bundled content pack (counts, refs, geometry, locales, hashes)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.colora.world.tools.ValidateContentKt")
    args(contentDir.asFile.absolutePath, "FULL")
}
tasks.test {
    systemProperty("colora.content", contentDir.asFile.absolutePath)
}
