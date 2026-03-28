# RadialMenu: Radial Menu, Circular Menu, and Pie Menu for Android & Desktop (Kotlin Multiplatform)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.gawwr4v/radialmenu.svg)](https://search.maven.org/artifact/io.github.gawwr4v/radialmenu)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blueviolet.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Desktop](https://img.shields.io/badge/Platform-Desktop_JVM-orange.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A lightweight Kotlin Multiplatform radial context menu library for Android and Desktop JVM.  
RadialMenu supports Compose APIs (`RadialMenuWrapper` + `RadialMenuOverlay`) and Android View APIs (`RadialMenuView`), with trigger modes for long press, right click, and keyboard hold interactions.

## Compatibility

| RadialMenu | Kotlin | Compose Multiplatform | AGP | Android Min SDK | Desktop Min JDK |
|------------|--------|-----------------------|-----|------------------|------------------|
| 1.0.5 | 2.1.20 | 1.7+ | 8.5+ | 21 | 17 |

> RadialMenu follows [Semantic Versioning](https://semver.org).  
> Minor versions add features without breaking public API; major versions may include breaking changes (see [CHANGELOG.md](CHANGELOG.md)).

## Preview

### Android
<img src="assets/demo1.gif" width="700" alt="RadialMenu Android preview">

### Desktop
<img src="assets/demo2.gif" width="700" alt="RadialMenu Desktop preview">

## Platform Support

| Platform | Status |
|----------|--------|
| Android (Compose + View) | Supported |
| Desktop JVM (Compose) | Supported |

## Features

- Kotlin Multiplatform support for Android and Desktop JVM.
- Multiple trigger modes with platform defaults via `RadialMenuTriggerMode.Auto`.
- Trigger options: `LongPress`, `SecondaryClick`, `KeyboardHold(key)`.
- Position-aware rotation controls for touch and pointer modes.
- Drag/flick directional selection with smooth scaling feedback.
- Edge-hug corner layout (opt-in) for constrained corner spawns.
- Badge support (`badgeCount` and `badgeText`).
- Active/inactive icon state support.
- Compose overlay color and animation customization.
- Android View support with XML attributes and runtime configuration.
- POM declares only `kotlin-stdlib` (consumer provides Compose/AndroidX).

## Installation

> Full docs: [gawwr4v.github.io/RadialMenu](https://gawwr4v.github.io/RadialMenu/)

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

    // Required only if you use the Painter-based item constructor directly.
    // Not required when using DrawableRes/Drawable overloads.
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

## Quick Start (Compose Multiplatform)

```kotlin
import io.github.gawwr4v.radialmenu.*

val items = listOf(
    RadialMenuItem(id = 1, icon = sharePainter, label = "Share"),
    RadialMenuItem(id = 2, icon = likePainter, label = "Like"),
    RadialMenuItem(id = 3, icon = savePainter, label = "Save")
)

Box {
    RadialMenuWrapper(
        items = items,
        onItemSelected = { item -> handleSelection(item) }
    ) {
        Content()
    }

    RadialMenuOverlay(items = items)
}
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

`KeyboardHold` behavior:

- Menu opens at screen center.
- Selection is angle-based pie-slice style.
- Flick direction is calibrated from cursor position at key down.
- Selection commits on key release.
- Edge-hug is automatically skipped for center-spawned menus.

## Quick Start (Android View)

### XML layout

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <io.github.gawwr4v.radialmenu.RadialMenuView
        android:id="@+id/radialMenu"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:rm_menuRadius="90dp"
        app:rm_iconSize="32dp"
        app:rm_accentColor="@android:color/white" />

</FrameLayout>
```

### Kotlin setup

```kotlin
val radialMenu = findViewById<RadialMenuView>(R.id.radialMenu)

val items = listOf(
    RadialMenuItem(context = this, id = 1, iconRes = R.drawable.ic_share, label = "Share"),
    RadialMenuItem(context = this, id = 2, iconRes = R.drawable.ic_like, label = "Like"),
    RadialMenuItem(context = this, id = 3, iconRes = R.drawable.ic_save, label = "Save")
)

radialMenu.setItems(items)
radialMenu.enableEdgeHugLayout = true
radialMenu.triggerMode = RadialMenuTriggerMode.LongPress(positionAware = true)
radialMenu.onItemSelected = { item -> handleSelection(item) }
```

> `RadialMenuView` supports `LongPress` and `SecondaryClick`.  
> `KeyboardHold` is accepted for API symmetry but not implemented in the View system.

## Icon Setup

Default API:

```kotlin
RadialMenuItem(id = 1, icon = painter, label = "Share")
```

Android resource overload:

```kotlin
RadialMenuItem(context = this, id = 1, iconRes = R.drawable.ic_share, label = "Share")
```

Android drawable overload:

```kotlin
RadialMenuItem(id = 1, icon = drawable, label = "Share")
```

Desktop uses `Painter` icons.

## Edge-Hug Layout

Edge-hug is opt-in:

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    enableEdgeHugLayout = true
) { Content() }
```

When the menu opens in a true corner with 4+ items, RadialMenu can switch to corner-aware edge-hug placement.  
For center-spawned keyboard menus, edge-hug is automatically bypassed.

## Performance Notes

- Optimal: `4-8` items.
- Maximum recommended: `8` items for touch ergonomics.
- No hard item-count limit is enforced by the library.

## Troubleshooting

### Imports unresolved after adding dependency

Symptom: `Unresolved reference: RadialMenuView`.

Fix:

```kotlin
implementation("io.github.gawwr4v:radialmenu:1.0.5@aar")
```

Verify with:

```bash
./gradlew :app:dependencies
./gradlew :app:dependencyInsight --dependency radialmenu --configuration releaseRuntimeClasspath
```

### UnsupportedClassVersionError on Desktop

Symptom: `class file version 65.0`.

Cause: pre-`1.0.5` artifact on Java 17 runtime.

Fix: upgrade to `1.0.5` or run Java 21+ for older artifacts.

### Cannot access `Painter` in View-only integration

Fix option 1:

```kotlin
implementation("androidx.compose.ui:ui:1.8.0")
```

Fix option 2: use `iconRes`/`Drawable` overloads instead of direct `Painter`.

## Comparison

| Feature | RadialMenu | Typical Android-only alternatives |
|---|---|---|
| Android + Desktop JVM | Yes | Usually Android only |
| Kotlin Multiplatform | Yes | Usually no |
| Compose + View API mix | Yes | Usually one UI stack |
| Trigger modes | Auto / LongPress / SecondaryClick / KeyboardHold | Usually long press only |
| Edge-aware corner strategy | Yes | Often missing |
| Maven Central publish | Yes | Often JitPack-only |

## Why RadialMenu and, Current Limits

### Why this over existing options

- Kotlin Multiplatform-first packaging for Android and Desktop JVM.
- Two integration paths in one library: Compose APIs and Android View APIs.
- Better platform defaults via `Auto`: long press on Android, right click on Desktop.
- Production-focused publish shape: `radialmenu`, `radialmenu-android`, and `radialmenu-desktop`.
- Lean dependency surface in published metadata (`kotlin-stdlib` only).

### Known limitations (verified for v1.0.5)

- `KeyboardHold` is Compose-only; `RadialMenuView` supports `LongPress` and `SecondaryClick`.
- Best UX is typically `4-8` items; more is supported but less ergonomic on small touch screens.
- Desktop target is Compose Desktop JVM (Java 17+), not Swing/JavaFX-native widgets.
- If you use `Painter` constructors directly in View-only modules, add `androidx.compose.ui:ui` (or use `iconRes`/`Drawable` overloads).


## Documentation

- Website: [gawwr4v.github.io/RadialMenu](https://gawwr4v.github.io/RadialMenu/)
- Getting Started: [docs/getting-started.md](docs/getting-started.md)
- Customization: [docs/customization.md](docs/customization.md)
- Compatibility: [docs/compatibility.md](docs/compatibility.md)
- Troubleshooting: [docs/troubleshooting.md](docs/troubleshooting.md)
- Changelog: [CHANGELOG.md](CHANGELOG.md)

## Community

- Discussions: [GitHub Discussions](https://github.com/gawwr4v/RadialMenu/discussions)
- Issues: [GitHub Issues](https://github.com/gawwr4v/RadialMenu/issues)
- Contributing: [CONTRIBUTING.md](CONTRIBUTING.md)

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Also Known As

Developers often discover RadialMenu through searches like:

- Android radial menu
- Kotlin Multiplatform radial menu
- Compose Multiplatform circular menu
- Android pie menu
- Desktop pie menu
- KMP context menu
- Jetpack Compose radial menu
