# RadialMenu: Kotlin Multiplatform Circular Context Menu

[![Maven Central](https://img.shields.io/maven-central/v/io.github.gawwr4v/radialmenu.svg)](https://search.maven.org/artifact/io.github.gawwr4v/radialmenu)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blueviolet.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Desktop](https://img.shields.io/badge/Platform-Desktop_JVM-orange.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![GitHub license](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Compatibility

| RadialMenu | Kotlin | Compose Multiplatform | AGP | Min SDK |
|------------|--------|-----------------------|-----|---------|
| 1.0.0      | 2.1.20 | 1.7.3                 | 8.x | 21      |

> **Note:** RadialMenu follows [Semantic Versioning](https://semver.org).
> Minor versions add features without breaking the API.
> Major versions may contain breaking changes documented in [CHANGELOG.md](CHANGELOG.md).

## Preview

<!-- Add a GIF or screenshot here showing the menu in action -->
<!-- Recommended: record on a real Android device using ScreenToGif or ADB -->
![RadialMenu Demo](assets/demo.gif)

> *Long press anywhere → drag to select → release to confirm*

A lightweight **Compose Multiplatform radial menu** (also known as a **circular menu** or **pie menu**) targeting **Android and Desktop JVM**. It operates as a **long press menu** overlay equipped with **drag to select** interaction. The menu is built entirely with continuous physics-based selection and is highly inspired by the famous **Pinterest context menu**.

It features **cross-platform haptic feedback**, smooth **animated menu** transitions, and an edge-aware placement geometry ensuring the **gesture menu** is never clipped off-screen or covered by the user's thumb.

## Platform Support

| Platform | Status |
|----------|--------|
| Android (Compose + View) | ✅ Supported |
| Desktop JVM | ✅ Supported |

## Features

- **Compose Multiplatform** (supports Android and Desktop).
- **Fully generic items**, define your own actions with any number of items (recommended 2 to 8 items).
- **Long press to activate**, **drag to select gesture** based item choosing.
- **Smart edge aware angle calculation** (ensures menu is never obscured by the finger nor clipped by screen edges).
- **Haptic feedback** on activation, hover over action, and selection.
- **Toggle icon states** (for example tracking liked or saved variables and updating icons).
- **Item badges**, show counts or custom text on each menu item.
- **Customizable animations**, choose from presets or create your own configuration.
- **Dynamic icon scaling** on hover for clear visibility.
- **Drag direction indicator** to help guide the finger.
- **Accessibility**, content descriptions, screen reader support, and announcements.
- **Zero external dependencies** (relies purely on `std-lib` and Compose or Canvas).
- **ProGuard and R8 safe**, includes consumer rules and `@Keep` annotations.

## Installation

```kotlin
// Android + Desktop (Compose Multiplatform)
implementation("io.github.gawwr4v:radialmenu:1.0.0")
```

## Quick Start (Compose Multiplatform)

```kotlin
import io.github.gawwr4v.radialmenu.*

val items = listOf(
    RadialMenuItem(id = 1, icon = painterResource(R.drawable.ic_share), label = "Share"),
    RadialMenuItem(id = 2, icon = painterResource(R.drawable.ic_heart), label = "Like"),
    RadialMenuItem(id = 3, icon = painterResource(R.drawable.ic_bookmark), label = "Save")
)

// Wrap your layout to intercept gestures
Box {
    RadialMenuWrapper(
        items = items,
        onItemSelected = { item ->
            println("Selected: ${item.label}")
        }
    ) {
        Text("Long press me to open radial menu!")
    }

    // Place overlay at root level for fullscreen rendering
    RadialMenuOverlay(items = items)
}
```

## Quick Start (Legacy Android View)

### 1. Add to XML Layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Your Main Content Here -->

    <!-- Radial Menu Overlay -->
    <io.github.gawwr4v.radialmenu.RadialMenuView
        android:id="@+id/radialMenu"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:rm_menuRadius="90dp"
        app:rm_iconSize="32dp"
        app:rm_accentColor="@color/white" />

</FrameLayout>
```

### 2. Configure Items & Listeners

```kotlin
import io.github.gawwr4v.radialmenu.*

val radialMenu = findViewById<RadialMenuView>(R.id.radialMenu)

val items = listOf(
    RadialMenuItem(id = 1, icon = shareDrawable.toPainter(), label = "Share"),
    RadialMenuItem(id = 2, icon = heartDrawable.toPainter(), label = "Like"),
    RadialMenuItem(id = 3, icon = bookmarkDrawable.toPainter(), label = "Save")
)

radialMenu.setItems(items)
radialMenu.onItemSelected = { item ->
    when (item.id) {
        1 -> { /* trigger share */ }
        2 -> { radialMenu.setItemActive(2, !item.isActive) }
        3 -> { radialMenu.setItemActive(3, !item.isActive) }
    }
}
```

## Java Interop

`RadialMenuView` is fully usable from Java via xml or programmatic initialization:

```java
// Java usage example
RadialMenuView menu = findViewById(R.id.radialMenu);

// Create generic Action list
List<RadialMenuItem> items = new ArrayList<>();
items.add(new RadialMenuItem(1, new BitmapPainter(...), ...));
menu.setItems(items);

// Use Kotlin Function1 equivalent using Java lambdas
menu.setOnItemSelected(item -> {
    Toast.makeText(context, "Selected: " + item.getLabel(), Toast.LENGTH_SHORT).show();
    return kotlin.Unit.INSTANCE;
});

// Menu automatically opens on long press. Listen for taps:
menu.setOnTap(() -> {
    Toast.makeText(context, "Tapped!", Toast.LENGTH_SHORT).show();
    return kotlin.Unit.INSTANCE;
});
```

> The Compose API (`RadialMenuWrapper`) is Kotlin-only.
> For Java projects, use `RadialMenuView` in your XML layouts.

## Unlimited Items

The menu dynamically supports any number of items. Items are evenly fanned around the center angle with `ICON_SPREAD_DEGREES` (45°) spacing.

```kotlin
val items = listOf(
    RadialMenuItem(id = 1, icon = shareIcon, label = "Share"),
    RadialMenuItem(id = 2, icon = heartIcon, label = "Like"),
    RadialMenuItem(id = 3, icon = bookmarkIcon, label = "Save"),
    RadialMenuItem(id = 4, icon = flagIcon, label = "Flag"),
    RadialMenuItem(id = 5, icon = reportIcon, label = "Report")
)
```

> **Note:** More than 8 items may cause poor UX on small screens. A warning is logged at runtime.

## Customization

### Theme Support

RadialMenu includes built-in dark and light theme colors.

```kotlin
import io.github.gawwr4v.radialmenu.RadialMenuColors

// Automatic theme-aware colors (follows system dark mode)
RadialMenuWrapper(
    colors = RadialMenuColors.autoTheme()
) { /* ... */ }

// Force dark
RadialMenuWrapper(
    colors = RadialMenuColors.dark()
) { /* ... */ }

// Force light  
RadialMenuWrapper(
    colors = RadialMenuColors.light()
) { /* ... */ }
```

### Animation Presets

Pass a `RadialMenuAnimationConfig` to control timing and physics:

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    animationConfig = RadialMenuAnimationConfig.bouncy()
) { /* content */ }
```

| Preset | Description |
|--------|-------------|
| `RadialMenuAnimationConfig.default()` | Smooth, balanced |
| `RadialMenuAnimationConfig.snappy()` | Fast and responsive |
| `RadialMenuAnimationConfig.bouncy()` | Spring physics |
| `RadialMenuAnimationConfig.slow()` | Deliberate, cinematic |

### Custom Configuration

```kotlin
RadialMenuAnimationConfig(
    openDurationMs = 250,
    closeDurationMs = 150,
    itemScaleDurationMs = 80,
    selectedItemScale = 1.6f,
    enableSpringAnimation = true,
    springDampingRatio = Spring.DampingRatioMediumBouncy,
    springStiffness = Spring.StiffnessMedium
)
```

## Badges

Show a count or custom text badge on any item:

```kotlin
RadialMenuItem(id = 1, icon = notifIcon, label = "Notifications", badgeCount = 5)
RadialMenuItem(id = 2, icon = updateIcon, label = "Updates", badgeText = "NEW")
```

Badges automatically display as a small red circle at the top-right of the item icon. Counts > 99 display as "99+".

## View Attributes (XML)

| XML Attribute        | Format      | Default Value | Description                                      |
| -------------------- | ----------- | ------------- | ------------------------------------------------ |
| `rm_accentColor`     | `color`     | `White`       | The background color of the selected item.       |
| `rm_menuRadius`      | `dimension` | `90dp`        | Radius distance from center to icons.            |
| `rm_iconSize`        | `dimension` | `32dp`        | Icon width and height.                           |
| `rm_overlayColor`    | `color`     | `Black 50%`   | Background dimming scrim color.                  |
| `rm_badgeColor`      | `color`     | `#FF4444`     | Badge background color.                          |
| `rm_animationDurationMs` | `integer` | `100`       | Item scale animation duration in ms.             |

## How It Works (Angle Algorithm)

A naive radial menu typically spawns symmetrically around the touch point. This can lead to menu items being cut off by the screen edges or pointing directly "downward," thereby obscuring the icons underneath the user's thumb/wrist.

**The Solution:** `RadialMenu` calculates a dynamic "center angle" based on the absolute spatial (`x,y`) location of the touch relative to screen dimensions.
- The `base angle` smoothly interpolates from 330° (far left edge) → 270° (center) → 210° (far right edge).
- Aggressive edge-boosting ensures menu nodes steeply fan outwards or entirely inward near the absolute edge.
- The top-adjust variable keeps the top 25% boundary clear.
- Overall calculation ensures icons **never exceed downwards bounds** (only `195°` to `345°` are permitted).

## Snapshot Releases

Snapshot builds are published automatically on every push to `main`.
To use a snapshot in your project:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        google()
        mavenCentral()
    }
}

// app/build.gradle.kts
implementation("io.github.gawwr4v:radialmenu-android:1.0.1-SNAPSHOT")
```

> ⚠️ Snapshot releases may contain unstable or breaking changes.
> Use only for testing upcoming features.

## Community

- 💬 [GitHub Discussions](https://github.com/gawwr4v/RadialMenu/discussions), questions, ideas, show and tell
- 🐛 [Issues](https://github.com/gawwr4v/RadialMenu/issues), bug reports and feature requests
- 🤝 [Contributing](CONTRIBUTING.md), how to contribute

## License

```
Copyright 2026 gawwr4v

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
