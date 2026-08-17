// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.10.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

detekt {
    config.setFrom(files(rootDir.resolve("conf/detekt.yml")))
    buildUponDefaultConfig = false
    parallel = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.8")
    detektPlugins("com.twitter.compose.rules:detekt:0.0.26")
}

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
    description = "Runs detekt over the whole code base."
    setSource(files(rootDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**", "**/build/**", "**/bin/**")
    config.setFrom(files(rootDir.resolve("conf/detekt.yml")))
    buildUponDefaultConfig = false
    parallel = true
    jvmTarget = "17"
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(true)
        sarif.required.set(false)
    }
}
