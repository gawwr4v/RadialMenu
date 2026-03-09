# How to Test the Library From mavenLocal

This guide lets you verify the library works correctly
when consumed by a real project, BEFORE publishing to Maven Central.

## Step 1 — Publish to mavenLocal
Run:
```bash
./gradlew :radialmenu:publishToMavenLocal
```

## Step 2 — Create a test project
1. Open Android Studio
2. File → New → New Project → Empty Activity
3. Name it anything (e.g. RadialMenuTest)

## Step 3 — Add mavenLocal to the test project
In the test project's settings.gradle.kts:
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()   // ← add this FIRST
        google()
        mavenCentral()
    }
}
```

## Step 4 — Add the dependency
In app/build.gradle.kts:
```kotlin
implementation("io.github.gawwr4v:radialmenu-android:1.0.0")
```

## Step 5 — Use it
```kotlin
import io.github.gawwr4v.radialmenu.RadialMenuView
import io.github.gawwr4v.radialmenu.RadialMenuItem
```

If it compiles and the import resolves, the library is
correctly packaged and ready to publish.
