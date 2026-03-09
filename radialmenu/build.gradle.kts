import java.net.URL
import org.gradle.plugins.signing.Sign
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("maven-publish")
    id("signing")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.dokka")
    id("com.gradleup.nmcp")
}

kotlin {
    androidTarget()
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
        publishLibraryVariants("release")
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            implementation("androidx.core:core-ktx:1.13.1")
            implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
            implementation("androidx.compose.ui:ui-tooling:1.7.3")
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation("androidx.test:core:1.5.0")
                implementation("androidx.test:runner:1.5.2")
                implementation("androidx.test.ext:junit:1.1.5")
                implementation("androidx.test.espresso:espresso-core:3.5.1")
            }
        }
    }
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:android-documentation-plugin:1.9.20")
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "io.github.gawwr4v.radialmenu"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        xmlReport = true
        htmlReport = true
        warningsAsErrors = false
        abortOnError = false
        lintConfig = file("lint.xml")
    }
}



detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/desktopMain/kotlin"
    )
}

apiValidation {
    ignoredClasses.add("io.github.gawwr4v.radialmenu.RadialMenuView")
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """
            {
                "footerMessage": "RadialMenu © 2026 gawwr4v — Apache 2.0"
            }
            """
        )
    )
    dokkaSourceSets {
        named("commonMain") {
            displayName.set("Common")
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(
                    uri("https://github.com/gawwr4v/RadialMenu/blob/main/radialmenu/src/commonMain/kotlin").toURL()
                )
                remoteLineSuffix.set("#L")
            }
        }
        named("androidMain") {
            displayName.set("Android")
            sourceLink {
                localDirectory.set(file("src/androidMain/kotlin"))
                remoteUrl.set(
                    uri("https://github.com/gawwr4v/RadialMenu/blob/main/radialmenu/src/androidMain/kotlin").toURL()
                )
                remoteLineSuffix.set("#L")
            }
        }
        named("desktopMain") {
            displayName.set("Desktop")
            sourceLink {
                localDirectory.set(file("src/desktopMain/kotlin"))
                remoteUrl.set(
                    uri("https://github.com/gawwr4v/RadialMenu/blob/main/radialmenu/src/desktopMain/kotlin").toURL()
                )
                remoteLineSuffix.set("#L")
            }
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

afterEvaluate {
    // Fix signing task ordering for KMP publications
    tasks.withType<PublishToMavenRepository>().configureEach {
        dependsOn(tasks.withType<Sign>())
    }
    tasks.withType<PublishToMavenLocal>().configureEach {
        dependsOn(tasks.withType<Sign>())
    }
    // Prevent signing tasks from running in parallel
    tasks.withType<Sign>().configureEach {
        mustRunAfter(tasks.withType<Sign>().filter { it.name != this.name })
    }

    publishing {
        publications.withType<MavenPublication> {
            groupId = (project.findProperty("GROUP_ID") as? String) ?: "io.github.gawwr4v"
            version = (project.findProperty("VERSION_NAME") as? String) ?: "1.0.0"
            artifactId = (project.findProperty("ARTIFACT_ID") as? String) ?: "radialmenu"
            artifact(javadocJar)
            configurePom()
        }
    }
    signing {
        val signingKeyId = project.findProperty("signing.keyId") as String?
        val signingKey = project.findProperty("signing.key") as String?
        val signingPassword = project.findProperty("signing.password") as String?
        
        if (signingKeyId != null && signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        }
        sign(publishing.publications)
    }
}

fun MavenPublication.configurePom() {
    pom {
        name.set((project.findProperty("LIBRARY_NAME") as? String) ?: "RadialMenu")
        description.set((project.findProperty("LIBRARY_DESCRIPTION") as? String) ?: "RadialMenu library")
        url.set((project.findProperty("LIBRARY_URL") as? String) ?: "https://github.com/gawwr4v/RadialMenu")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set((project.findProperty("DEVELOPER_ID") as? String) ?: "gawwr4v")
                name.set((project.findProperty("DEVELOPER_ID") as? String) ?: "gawwr4v")
                url.set((project.findProperty("DEVELOPER_URL") as? String) ?: "https://github.com/gawwr4v")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/gawwr4v/RadialMenu.git")
            developerConnection.set("scm:git:ssh://github.com/gawwr4v/RadialMenu.git")
            url.set("https://github.com/gawwr4v/RadialMenu")
        }
        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/gawwr4v/RadialMenu/issues")
        }
    }
}

nmcp {
    publishAllPublications {
        username.set((project.findProperty("ossrhUsername") as? String) ?: "")
        password.set((project.findProperty("ossrhPassword") as? String) ?: "")
        publicationType.set("USER_MANAGED")
    }
}