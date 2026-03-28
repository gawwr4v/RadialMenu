---
title: Getting Started - RadialMenu
description: Install and configure RadialMenu for Android View, Android Compose, and Desktop.
---

# Getting Started

## Compatibility Matrix

| Platform | Min runtime | Kotlin | Compose | AGP |
|---|---|---|---|---|
| Android | API 21 | 2.x | 1.7+ (optional for View-only setups) | 8.5+ |
| Desktop JVM | Java 17 | 2.x | Compose Desktop | N/A |

## Installation

```kotlin
dependencies {
    implementation("io.github.gawwr4v:radialmenu:1.0.5")
}
```

## Platform Setup

### Android View setup

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("io.github.gawwr4v:radialmenu:1.0.5")

    // Required only if you directly use Painter-based RadialMenuItem constructors.
    // Not required for DrawableRes/Drawable overload usage.
    implementation("androidx.compose.ui:ui:1.8.0")
}
```

### Android Compose setup (Kotlin 2.x)

```kotlin
// app/build.gradle.kts
plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("io.github.gawwr4v:radialmenu:1.0.5")
    implementation("androidx.compose.ui:ui:1.8.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.activity:activity-compose:1.9.0")
}
```

### Desktop Compose setup

```kotlin
dependencies {
    implementation("io.github.gawwr4v:radialmenu:1.0.5")
    implementation(compose.desktop.currentOs)
}
```

Desktop minimum runtime: Java 17.

Desktop verification commands:

```bash
# Verify dependency resolution on desktop runtime classpath
./gradlew :desktopApp:dependencyInsight --dependency radialmenu --configuration runtimeClasspath

# Verify desktop bytecode target (Java 17 = major version 61)
javap -verbose -classpath ~/.m2/repository/io/github/gawwr4v/radialmenu-desktop/1.0.5/radialmenu-desktop-1.0.5.jar io.github.gawwr4v.radialmenu.RadialMenuTriggerMode
```

## Icon Setup

Default API (`Painter`):

```kotlin
RadialMenuItem(id = 1, icon = painter, label = "Share")
```

Android shortcut (`@DrawableRes`):

```kotlin
RadialMenuItem(context = this, id = 1, iconRes = R.drawable.ic_share, label = "Share")
```

Android drawable overload:

```kotlin
RadialMenuItem(id = 1, icon = drawable, label = "Share")
```

Desktop uses `Painter` icons.

## Compose Quick Start

```kotlin
val items = listOf(
    RadialMenuItem(id = 1, icon = sharePainter, label = "Share"),
    RadialMenuItem(id = 2, icon = likePainter, label = "Like"),
    RadialMenuItem(id = 3, icon = savePainter, label = "Save")
)

Box {
    RadialMenuWrapper(
        items = items,
        onItemSelected = { item -> println(item.label) }
    ) {
        Content()
    }
    RadialMenuOverlay(items = items)
}
```

## Android View Quick Start

```kotlin
val radialMenu = findViewById<RadialMenuView>(R.id.radial_menu)
radialMenu.setItems(
    listOf(
        RadialMenuItem(context = this, id = 1, iconRes = R.drawable.ic_share, label = "Share"),
        RadialMenuItem(context = this, id = 2, iconRes = R.drawable.ic_save, label = "Save")
    )
)
```

## Troubleshooting

### Imports are unresolved after adding the dependency

Symptom: `Unresolved reference: RadialMenuView` or similar despite dependency being present.

Cause: Gradle variant resolution may not automatically select the Android artifact in some project configurations.

Fix:

```kotlin
implementation("io.github.gawwr4v:radialmenu:1.0.5@aar")
```

Then verify:

```bash
./gradlew :app:dependencies
```

### UnsupportedClassVersionError on Desktop

Symptom: `compiled by a more recent version of the Java Runtime (class file version 65.0)`.

Cause: Older pre-`1.0.5` artifact built for Java 21 is used on Java 17 runtime.

Fix: upgrade to `1.0.5` (Java 17 bytecode), or run Java 21+ with older artifacts.

### Compose type errors when using RadialMenuView

Symptom: `Cannot access class 'Painter'`.

Fix:

```kotlin
implementation("androidx.compose.ui:ui:1.8.0")
```

Or use the Android overloads with `iconRes`/`Drawable` and avoid direct Painter usage.
