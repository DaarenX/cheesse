plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    application
}

group = "xyz.daaren.cheesse"
version = "1.0.0"
application {
    mainClass.set("xyz.daaren.cheesse.ApplicationKt")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson.kotlin)
    testImplementation(libs.kotlin.testJunit)
}