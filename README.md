# RadialMenu

[![Maven Central](https://img.shields.io/maven-central/v/io.github.gawwr4v/radialmenu.svg)](https://search.maven.org/artifact/io.github.gawwr4v/radialmenu)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

RadialMenu is a Kotlin Multiplatform radial/pie context menu library for Android and Desktop JVM.
It supports Compose (`RadialMenuWrapper` + `RadialMenuOverlay`) and Android Views (`RadialMenuView`).

## Compatibility

| Platform | Min SDK/Runtime | Kotlin | Compose |
|---|---|---|---|
| Android | API 21 | 2.x | 1.7+ (optional for View-only setups) |
| Desktop JVM | Java 17 | 2.x | Compose Desktop |

## Installation

```kotlin
dependencies {
    implementation("io.github.gawwr4v:radialmenu:1.0.5")
}
```

The published POM declares only `kotlin-stdlib`. Compose and AndroidX are intentionally treated as consumer-provided.

## Platform Setup

### Android View setup (complete)

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("io.github.gawwr4v:radialmenu:1.0.5")

    // Required only if you directly construct RadialMenuItem with Painter
    // (not required when using DrawableRes/Drawable overloads)
    implementation("androidx.compose.ui:ui:1.8.0")
}
```

### Android Compose setup (complete, Kotlin 2.x)

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

### Desktop (Compose Desktop) setup

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.gawwr4v:radialmenu:1.0.5")
    implementation(compose.desktop.currentOs)
}
```

Minimum Java runtime: `17`.

## Quick Start (Compose)

```kotlin
val items = listOf(
    RadialMenuItem(id = 1, icon = sharePainter, label = "Share"),
    RadialMenuItem(id = 2, icon = likePainter, label = "Like"),
    RadialMenuItem(id = 3, icon = savePainter, label = "Save")
)

Box {
    RadialMenuWrapper(
        items = items,
        onItemSelected = { item -> println("Selected ${item.label}") }
    ) {
        Content()
    }

    RadialMenuOverlay(items = items)
}
```

## Quick Start (Android View)

```kotlin
val radialMenu = findViewById<RadialMenuView>(R.id.radialMenu)

val items = listOf(
    RadialMenuItem(context = this, id = 1, iconRes = R.drawable.ic_share, label = "Share"),
    RadialMenuItem(context = this, id = 2, iconRes = R.drawable.ic_like, label = "Like"),
    RadialMenuItem(context = this, id = 3, iconRes = R.drawable.ic_save, label = "Save")
)

radialMenu.setItems(items)
radialMenu.onItemSelected = { item -> println("Selected ${item.label}") }
```

## Trigger Modes

`RadialMenuWrapper` supports:

- `RadialMenuTriggerMode.Auto` (default)
- `RadialMenuTriggerMode.LongPress(positionAware = true)`
- `RadialMenuTriggerMode.SecondaryClick(positionAware = false)`
- `RadialMenuTriggerMode.KeyboardHold(key = Key.Q)`

`Auto` resolves to:
- Android: `LongPress(positionAware = true)`
- Desktop: `SecondaryClick(positionAware = false)`

`KeyboardHold` opens the menu at screen center, tracks hover while key is held, and commits on key release.

## Performance Notes

- Optimal item count: `4-8` items.
- Maximum recommended: `8` items. Beyond this, touch targets become too small for reliable selection on most devices.
- The library enforces no hard limit, so you can render more items when your UX can support it.

## Icon Setup

Default API:

```kotlin
RadialMenuItem(id = 1, icon = painter, label = "Share")
```

Android resource overload (View-friendly):

```kotlin
RadialMenuItem(context = this, id = 1, iconRes = R.drawable.ic_share, label = "Share")
```

Android drawable overload:

```kotlin
RadialMenuItem(id = 1, icon = drawable, label = "Share")
```

Desktop uses `Painter` icons.

## Troubleshooting

### Imports are unresolved after adding the dependency

Symptom: `Unresolved reference: RadialMenuView` or similar despite dependency being present.

Cause: Gradle variant resolution may not automatically select the Android artifact in some project configurations.

Fix:

```kotlin
implementation("io.github.gawwr4v:radialmenu:1.0.5@aar")
```

Then verify with:

```bash
./gradlew :app:dependencies
```

### UnsupportedClassVersionError on Desktop

Symptom: `compiled by a more recent version of the Java Runtime (class file version 65.0)`.

Cause: Using an older pre-`1.0.5` artifact built for Java 21 on Java 17 runtimes.

Fix: upgrade to `1.0.5` (desktop artifact is Java 17 bytecode), or run Java 21+ when using older artifacts.

### Compose type errors when using RadialMenuView

Symptom: `Cannot access class 'Painter'` in View-only integration.

Fix option 1:

```kotlin
implementation("androidx.compose.ui:ui:1.8.0")
```

Fix option 2: use Android overloads:
- `RadialMenuItem(context = ..., iconRes = ...)`
- `RadialMenuItem(icon = Drawable, ...)`

## Documentation

- Website: [gawwr4v.github.io/RadialMenu](https://gawwr4v.github.io/RadialMenu)
- Getting started: [docs/getting-started.md](docs/getting-started.md)
- Customization: [docs/customization.md](docs/customization.md)
- Compatibility: [docs/compatibility.md](docs/compatibility.md)
- Troubleshooting: [docs/troubleshooting.md](docs/troubleshooting.md)
- Changelog: [CHANGELOG.md](CHANGELOG.md)
