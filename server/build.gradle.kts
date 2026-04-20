plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    application
}

group = "xyz.daaren.cheesse"
version = "0.0.1"
application {
    mainClass.set("xyz.daaren.cheesse.CheesseApplicationKt")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.spring.boot.starter.data.r2dbc)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.jvm.kotlin.reflect)
    implementation(libs.jvm.kotlinx.coroutines.core)
    implementation(libs.jvm.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.r2dbc.postgresql)
    testImplementation(libs.spring.boot.starter.data.r2dbc.test)
    testImplementation(libs.spring.boot.starter.webflux.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.r2dbc)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.jvm.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
    implementation(libs.chess.lib)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
