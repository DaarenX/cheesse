plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
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
    implementation(libs.spring.boot.h2console)
    implementation(libs.spring.boot.starter.data.r2dbc)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.jvm.kotlin.reflect)
    implementation(libs.jvm.kotlinx.coroutines.core)
    implementation(libs.jvm.kotlinx.coroutines.reactor)
    implementation(libs.jackson.kotlin)
    runtimeOnly(libs.h2)
    runtimeOnly(libs.r2dbc.h2)
    testImplementation(libs.spring.boot.starter.data.r2dbc.test)
    testImplementation(libs.spring.boot.starter.webflux.test)
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
