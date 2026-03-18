# RadialMenu: Radial Menu & Circular Menu for Android and Desktop | Kotlin Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/io.github.gawwr4v/radialmenu.svg)](https://search.maven.org/artifact/io.github.gawwr4v/radialmenu)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blueviolet.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Desktop](https://img.shields.io/badge/Platform-Desktop_JVM-orange.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![GitHub license](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Compatibility

| RadialMenu | Kotlin | Compose Multiplatform | AGP | Min SDK |
|------------|--------|-----------------------|-----|---------|
| 1.0.4      | 2.1.20 | 1.7.3                 | 8.5.x | 21    |

> **Note:** RadialMenu follows [Semantic Versioning](https://semver.org).
> Minor versions add features without breaking the API.
> Major versions may contain breaking changes documented in [CHANGELOG.md](CHANGELOG.md).

## Preview
### Android
<img src="assets/demo1.gif" width="700">

### Desktop
<img src="assets/demo2.gif" width="700">

A lightweight **Compose Multiplatform radial menu** (also known as a **circular menu** or **pie menu**) targeting **Android and Desktop JVM**. It supports multiple trigger modes including **long press**, **right click**, and **keyboard hold**, with **drag to select** interaction. The menu is built entirely with continuous physics-based selection and is highly inspired by the famous **Pinterest context menu**.

It features **cross-platform haptic feedback**, smooth **animated menu** transitions, and an edge-aware placement geometry ensuring the **gesture menu** is never clipped off-screen or covered by the user's thumb.

## Platform Support

| Platform | Status |
|----------|--------|
| Android (Compose + View) | ✅ Supported |
| Desktop JVM (Compose) | ✅ Supported |

## Features

- **Compose Multiplatform** (supports Android and Desktop).
- **Multiple trigger modes**: long press, right click, and keyboard hold with automatic per-platform defaults.
- **Fully generic items**: define your own actions with any number of items (recommended 2 to 8 items).
- **Drag to select gesture** based item choosing.
- **Smart edge-aware angle calculation** (ensures menu is never obscured by the finger nor clipped by screen edges).
- **Corner edge-hug layout**: opt-in L-shaped arrangement when the menu opens in a screen corner with 4+ items.
- **Haptic feedback** on activation, hover over action, and selection.
- **Toggle icon states** (for example tracking liked or saved variables and updating icons).
- **Item badges**: show counts or custom text on each menu item.
- **Customizable animations**: choose from presets (`default`, `snappy`, `bouncy`, `slow`) or create your own configuration.
- **Dynamic icon scaling** on hover for clear visibility.
- **Drag direction indicator** to help guide the finger.
- **RTL layout support**: center angle logic mirrors correctly for right-to-left layouts.
- **Accessibility**: content descriptions, screen reader support, and announcements.
- **Zero external dependencies** (the published POM declares only `kotlin-stdlib`; Compose and AndroidX are provided by the consumer).
- **ProGuard and R8 safe**: includes consumer rules and `@Keep` annotations.

## Installation

> **Full documentation:** [gawwr4v.github.io/RadialMenu](https://gawwr4v.github.io/RadialMenu)

```kotlin
// Android + Desktop (Compose Multiplatform)
implementation("io.github.gawwr4v:radialmenu:1.0.4")
```

> **Recommendation:** Use `1.0.4` or newer. `1.0.4` is the first complete Maven Central release with Android, Desktop, and KMP metadata all published correctly.

### Important Dependency Note

The published POM only declares `kotlin-stdlib`. Compose and AndroidX are intentionally treated as provided by the consumer project. If you use the Compose API, include your own Compose dependencies in your app or module.

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

## Trigger Modes

`RadialMenuWrapper` supports configurable trigger behavior via `RadialMenuTriggerMode`.

### Default: `Auto`

Automatically selects the appropriate trigger per platform:
- Android: `LongPress(positionAware = true)`
- Desktop: `SecondaryClick(positionAware = false)`

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ }
) { Content() }
```

### `LongPress`

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    triggerMode = RadialMenuTriggerMode.LongPress(positionAware = true)
) { Content() }
```

`positionAware = true` uses touch-position-aware center angle logic to fan items away from the finger.
`positionAware = false` uses a neutral angle (`0f`).

### `SecondaryClick` (Desktop)

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    triggerMode = RadialMenuTriggerMode.SecondaryClick(positionAware = false)
) { Content() }
```

- Spawns at cursor position.
- Uses pie-style directional selection.
- With `positionAware = false`, orientation is neutral and stable.

### `KeyboardHold`

```kotlin
import androidx.compose.ui.input.key.Key

RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    triggerMode = RadialMenuTriggerMode.KeyboardHold(Key.Q)
) { Content() }
```

- Menu spawns at screen center.
- Selection is angle-based pie-slice style.
- Flick direction is calibrated from cursor position at key-down (not from menu center).
- Selection commits on key release.
- Edge-hug layout is automatically skipped for center-spawned menus.

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
radialMenu.enableEdgeHugLayout = true
radialMenu.triggerMode = RadialMenuTriggerMode.LongPress(positionAware = true)

radialMenu.onItemSelected = { item ->
    when (item.id) {
        1 -> { /* trigger share */ }
        2 -> { radialMenu.setItemActive(2, !item.isActive) }
        3 -> { radialMenu.setItemActive(3, !item.isActive) }
    }
}
```

> **Note:** `RadialMenuView` supports `LongPress` (default) and `SecondaryClick` triggers. `KeyboardHold` is accepted for API symmetry but not implemented in the View system; use Compose `RadialMenuWrapper` for keyboard hold behavior.

## Java Interop

`RadialMenuView` is fully usable from Java via XML or programmatic initialization:

```java
// Java usage example
RadialMenuView menu = findViewById(R.id.radialMenu);

List<RadialMenuItem> items = new ArrayList<>();
items.add(new RadialMenuItem(1, new BitmapPainter(...), ...));
menu.setItems(items);

menu.setOnItemSelected(item -> {
    Toast.makeText(context, "Selected: " + item.getLabel(), Toast.LENGTH_SHORT).show();
    return kotlin.Unit.INSTANCE;
});

menu.setOnTap(() -> {
    Toast.makeText(context, "Tapped!", Toast.LENGTH_SHORT).show();
    return kotlin.Unit.INSTANCE;
});
```

> The Compose API (`RadialMenuWrapper`) is Kotlin-only.
> For Java projects, use `RadialMenuView` in your XML layouts.

## Unlimited Items

The menu dynamically supports any number of items. Items are evenly fanned around the center angle with `ICON_SPREAD_DEGREES` (45 degrees) spacing.

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

## Edge-Hug Layout

Edge-hug is **opt-in**. You must pass `enableEdgeHugLayout = true` to `RadialMenuWrapper` or set `radialMenuView.enableEdgeHugLayout = true` on the View to activate it. The default is `false`.

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    enableEdgeHugLayout = true
) { Content() }
```

When the user triggers the menu within `EDGE_THRESH_DP` (default: 80dp) of **two** screen edges simultaneously (i.e., a corner), and the menu has **4 or more items**, the radial fan cannot fit in the available space. In this case, RadialMenu switches to **edge-hug mode**:

- Items arrange in an **L-shape** along the two adjacent edges of the corner.
- The primary edge gets `ceil(n/2)` items, the secondary edge gets the remainder.
- The **corner cell** (the exact intersection point) is always left **vacant**: items start one full step away.
- Selection switches from angle-based to **nearest-item distance** to match the non-radial arrangement.

If item count is **3 or fewer**, the standard radial layout is always used, even in a corner, because 3 items can fit comfortably in a single quadrant.

**Configuration** (in `RadialMenuDefaults`):

| Constant | Default | Description |
|----------|---------|-------------|
| `EDGE_THRESH_DP` | `80f` | Distance from screen edge (dp) that defines the corner zone |
| `CORNER_ITEM_THRESHOLD` | `3` | Max items that still use radial layout in a corner (4+ triggers edge-hug) |

## Customization

### Theme Support

RadialMenu includes built-in dark and light theme colors.

```kotlin
import io.github.gawwr4v.radialmenu.RadialMenuColors

// Automatic theme-aware colors (follows system dark mode)
RadialMenuOverlay(
    items = items,
    colors = RadialMenuColors.autoTheme()
)

// Force dark
RadialMenuOverlay(
    items = items,
    colors = RadialMenuColors.dark()
)

// Force light
RadialMenuOverlay(
    items = items,
    colors = RadialMenuColors.light()
)
```

### Animation Presets

Pass a `RadialMenuAnimationConfig` to control timing and physics:

```kotlin
RadialMenuOverlay(
    items = items,
    animationConfig = RadialMenuAnimationConfig.bouncy()
)
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

Badges automatically display as a small red circle at the top-right of the item icon. Counts above 99 display as "99+".

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
- The `base angle` smoothly interpolates from 330 degrees (far left edge) to 270 degrees (center) to 210 degrees (far right edge).
- Aggressive edge-boosting ensures menu nodes steeply fan outwards or entirely inward near the absolute edge.
- The top-adjust variable keeps the top 30% boundary clear by flipping the fan downward.
- Overall calculation ensures icons **never exceed the safe bounds** (only `195` to `345` degrees are permitted for the lower region, `15` to `165` degrees for the upper region).

### Z-Order and Rendering Layer

Menu items always render above all other UI elements, including toolbars, navigation bars, FABs, and bottom sheets. This is handled automatically and requires no setup from the developer.

- **Compose:** The `RadialMenuOverlay` renders inside a `Popup`, which sits above all other composables in the window.
- **Android View:** When the menu opens, an overlay is attached to the window's decor view, ensuring it draws above the entire view hierarchy. This requires an Activity context. If the context is not an Activity (e.g., Dialog or Service), the overlay falls back to rendering within the current view hierarchy.

Edge-hug zone detection uses the **usable content area** (excluding system bars) rather than raw screen dimensions, so it only activates at true screen corners, not near in-app UI element edges.

## Comparison

| Feature | RadialMenu | Others           |
|---|---|------------------|
| Platforms | ✅ Android + Desktop JVM | ❌ Android only   |
| Kotlin Multiplatform | ✅ | ❌                |
| Compose Multiplatform API | ✅ Android + Desktop | ❌                |
| Traditional View API | ✅ Android | ✅ Android        |
| Multiple trigger modes | ✅ Long press, right click, keyboard hold | ❌ Long press only |
| Edge-aware positioning | ✅ Never clips | ❌                |
| Drag-to-select gesture | ✅ | ❌ Click only     |
| Haptic feedback | ✅ | ❌                |
| Badge support | ✅ | ❌                |
| RTL support | ✅ | ❌                |
| Dark/light/auto theme | ✅ | ❌                |
| Spring physics animations | ✅ | ❌                |
| External dependencies | 0 | JitPack required |
| Published on Maven Central | ✅ | ❌                |

## Community

- 💬 [GitHub Discussions](https://github.com/gawwr4v/RadialMenu/discussions): questions, ideas, show and tell
- 🐛 [Issues](https://github.com/gawwr4v/RadialMenu/issues): bug reports and feature requests
- 🤝 [Contributing](CONTRIBUTING.md): how to contribute

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

## Also known as

RadialMenu is what developers are looking for when they search for:

- Android radial menu
- Desktop radial menu
- Kotlin Multiplatform radial menu
- Android circular menu
- Compose Multiplatform circular menu
- Android pie menu
- Desktop pie menu
- KMP context menu
- Pinterest long press menu Android
- Kotlin circular menu
- Compose radial menu
- Android floating action menu
- Multiplatform gesture menu
- KMP radial menu
- Compose Desktop circular menu
- Jetpack Compose radial menu
