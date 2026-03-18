---
title: Getting Started - RadialMenu
description: Install and integrate RadialMenu in Compose Multiplatform or Android Views.
---

# Getting Started

RadialMenu supports:
- Compose Multiplatform (Android + Desktop JVM)
- Android View system (`RadialMenuView`)

## Requirements

- Min SDK: 21 (Android)
- Kotlin: 2.1.20+ recommended
- Compose dependencies in your app/module if you use Compose API

## Installation

=== "Kotlin DSL (build.gradle.kts)"

    ```kotlin
    dependencies {
        implementation("io.github.gawwr4v:radialmenu:1.0.4")
    }
    ```

=== "Groovy DSL (build.gradle)"

    ```groovy
    dependencies {
        implementation 'io.github.gawwr4v:radialmenu:1.0.4'
    }
    ```

## Compose Dependency Setup (Required)

RadialMenu does not export Compose transitively. If you use the Compose API, add Compose dependencies in your own module.

Example (KMP source-set style):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
```

## Compose Setup

`RadialMenuWrapper` handles trigger detection.  
`RadialMenuOverlay` renders the menu above your UI.

```kotlin
import io.github.gawwr4v.radialmenu.*

@Composable
fun DemoScreen() {
    val items = listOf(
        RadialMenuItem(id = 1, icon = homePainter, label = "Home"),
        RadialMenuItem(id = 2, icon = searchPainter, label = "Search"),
        RadialMenuItem(id = 3, icon = settingsPainter, label = "Settings")
    )

    Box {
        RadialMenuWrapper(
            items = items,
            onItemSelected = { item ->
                println("Selected: ${item.label}")
            }
        ) {
            Content()
        }

        RadialMenuOverlay(items = items)
    }
}
```

## Trigger Modes (Compose)

`Auto` is the default and recommended:

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ }
) { Content() }
```

Auto resolves to:
- Android: `LongPress(positionAware = true)`
- Desktop: `SecondaryClick(positionAware = false)`

Explicit examples:

```kotlin
// Mobile-first long press
triggerMode = RadialMenuTriggerMode.LongPress(positionAware = true)

// Desktop right-click
triggerMode = RadialMenuTriggerMode.SecondaryClick(positionAware = false)

// Keyboard hold (menu at center, commit on key up)
triggerMode = RadialMenuTriggerMode.KeyboardHold(Key.Q)
```

## Desktop Notes

- Default desktop trigger is right-click (`SecondaryClick`).
- Keyboard hold opens menu at center.
- Keyboard hold directional selection is based on cursor position at key-down (flick origin), not menu center.

## Edge-Hug Layout

Enable edge-hug only if you want corner L-shape behavior:

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    enableEdgeHugLayout = true
) { Content() }
```

Rules:
- Applies in corners with 4+ items.
- Uses nearest-item distance selection in edge-hug mode.
- Skipped for center-spawned keyboard menus.

## Android View Setup (XML)

```xml
<io.github.gawwr4v.radialmenu.RadialMenuView
    android:id="@+id/radial_menu"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:rm_menuRadius="90dp"
    app:rm_iconSize="32dp"
    app:rm_accentColor="@color/black" />
```

```kotlin
val radialMenu = findViewById<RadialMenuView>(R.id.radial_menu)
radialMenu.setItems(items)
radialMenu.enableEdgeHugLayout = true
radialMenu.triggerMode = RadialMenuTriggerMode.LongPress(positionAware = true)
radialMenu.onItemSelected = { item ->
    // handle selection
}
```

`RadialMenuView` supports `LongPress` and `SecondaryClick`.  
`KeyboardHold` is not implemented in `RadialMenuView`; use Compose wrapper for that trigger.

## Next Steps

- [Customization](customization.md)
- [Changelog](changelog.md)
