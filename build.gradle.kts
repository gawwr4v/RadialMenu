// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("multiplatform") version "2.1.20" apply false
    kotlin("android") version "2.1.20" apply false
    kotlin("plugin.compose") version "2.1.20" apply false
    id("org.jetbrains.compose") version "1.7.3" apply false
    id("com.android.library") version "8.5.2" apply false
    id("com.android.application") version "8.5.2" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.16.3" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("com.gradleup.nmcp") version "1.4.4" apply false
    id("com.gradleup.nmcp.aggregation") version "1.4.4"
}

nmcpAggregation {
    centralPortal {
        username = (project.findProperty("ossrhUsername") as? String) ?: ""
        password = (project.findProperty("ossrhPassword") as? String) ?: ""
        publishingType = "USER_MANAGED"
        publishingTimeout = java.time.Duration.ofMinutes(15)
        validationTimeout = java.time.Duration.ofMinutes(10)
    }
}

dependencies {
    nmcpAggregation(project(":radialmenu"))
}
