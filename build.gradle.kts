plugins {
    base
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

// The unified arc42 document is generated from the numbered chapter files.
val arc42Chapters = fileTree("docs/arc42") { include("0*_*.md", "1*_*.md") }
val arc42Unified = layout.projectDirectory.file("docs/arc42/cheesse-architecture-arc42.md")

tasks.register("arc42Assemble") {
    group = "documentation"
    description = "Regenerates docs/arc42/cheesse-architecture-arc42.md from the chapter files."
    val chapters = arc42Chapters
    val unified = arc42Unified.asFile
    inputs.files(chapters)
    outputs.file(unified)
    doLast {
        check(!chapters.isEmpty) { "No arc42 chapter files found" }
        unified.writeText(chapters.files.sortedBy { it.name }.joinToString("") { it.readText() })
    }
}

tasks.register("arc42Check") {
    group = "verification"
    description = "Fails if the unified arc42 document is out of sync with the chapter files."
    val chapters = arc42Chapters
    val unified = arc42Unified.asFile
    inputs.files(chapters, unified)
    mustRunAfter("arc42Assemble")
    doLast {
        check(!chapters.isEmpty) { "No arc42 chapter files found" }
        val expected = chapters.files.sortedBy { it.name }.joinToString("") { it.readText() }
        if (unified.readText() != expected) {
            throw GradleException("${unified.name} is stale. Run ./gradlew arc42Assemble")
        }
    }
}

tasks.named("check") { dependsOn("arc42Check") }
