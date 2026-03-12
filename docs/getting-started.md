---
title: Getting Started — RadialMenu
description: How to install and integrate RadialMenu — a radial, circular, and pie menu library — into your Android project in under 5 minutes.
---

# Getting Started

RadialMenu is an Android library for building radial menus — sometimes called circular menus,
pie menus, or arc menus — with full support for Jetpack Compose and traditional Android Views.

## Requirements

- **Min SDK:** 21 (Android 5.0 Lollipop) or higher
- **Kotlin:** 1.9.0 or higher
- Jetpack Compose (if using the Compose wrapper)

## Installation

Add the dependency to your app's `build.gradle` file:

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

## Basic Setup

### Jetpack Compose (Multiplatform)

RadialMenu requires two parts in Compose: a `RadialMenuWrapper` to intercept the long-press gesture around your content, and a `RadialMenuOverlay` placed at the root of your layout to draw the actual menu on top of everything else.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.github.gawwr4v.radialmenu.*
import androidx.compose.foundation.layout.Box

@Composable
fun MyScreen() {
    val items = listOf(
        RadialMenuItem(id = 1, icon = painterResource(R.drawable.ic_home), label = "Home"),
        RadialMenuItem(id = 2, icon = painterResource(R.drawable.ic_search), label = "Search"),
        RadialMenuItem(id = 3, icon = painterResource(R.drawable.ic_settings), label = "Settings")
    )

    Box {
        // 1. Wrap your content to detect gestures
        RadialMenuWrapper(
            items = items,
            onItemSelected = { item ->
                println("Selected: ${item.label}")
            }
        ) {
            // Your normal screen content goes here
            // Long pressing anywhere in this wrapper triggers the menu
            Text("Long press me!")
        }
        
        // 2. Add the overlay at the highest level of your Box/Z-Index
        RadialMenuOverlay(items = items)
    }
}
```

!!! warning "Important"
    `RadialMenuOverlay` must be placed at the **root** of your Compose hierarchy
    inside `setContent {}` — not inside a nested composable. If it is constrained
    by a parent layout, the menu will not render at full screen dimensions.

### Legacy Android View (XML)

If you are not using Compose, you can add `RadialMenuView` directly to your XML layout as an overlay.

```xml
<io.github.gawwr4v.radialmenu.RadialMenuView
    android:id="@+id/radial_menu"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:rm_menuRadius="90dp"
    app:rm_iconSize="32dp"
    app:rm_accentColor="@color/black" />
```

### Desktop (JVM)

The Compose Multiplatform examples above work identically on Desktop JVM (Windows, macOS, Linux). No additional setup is required - just use the same `RadialMenuWrapper` and `RadialMenuOverlay` composables inside your Desktop `application { Window { ... } }` block. Mouse long-press triggers the menu the same way touch does on Android.

## Your First Menu (Android Views)

In your Activity or Fragment, supply the items and the selection listener. The library automatically handles the long-press gesture, edge-aware coordinate geometry, and the open/close animations. Use the `toPainter()` extension function to bridge native Android `Drawable` resources into the Compose `Painter` objects required by the library.

```kotlin
import io.github.gawwr4v.radialmenu.RadialMenuItem
import io.github.gawwr4v.radialmenu.toPainter
import androidx.appcompat.content.res.AppCompatResources

val radialMenu = findViewById<RadialMenuView>(R.id.radial_menu)

// toPainter() is a RadialMenu extension function that bridges Android Drawables
// into the Compose Painter interface used internally by the library
val homeIcon = AppCompatResources.getDrawable(context, R.drawable.ic_home)!!.toPainter()
val searchIcon = AppCompatResources.getDrawable(context, R.drawable.ic_search)!!.toPainter()
val settingsIcon = AppCompatResources.getDrawable(context, R.drawable.ic_settings)!!.toPainter()

val items = listOf(
    RadialMenuItem(id = 1, icon = homeIcon, label = "Home"),
    RadialMenuItem(id = 2, icon = searchIcon, label = "Search"),
    RadialMenuItem(id = 3, icon = settingsIcon, label = "Settings")
)

radialMenu.setItems(items)

radialMenu.onItemSelected = { item ->
    Toast.makeText(context, "Selected: ${item.label}", Toast.LENGTH_SHORT).show()
}

// Optional: Intercept standard single and double taps 
// that happen when the menu is NOT open
radialMenu.onTap = { /* Handle normal click */ }
```

## Next Steps

- Explore [Customization](customization.md) to theme the menu to your app.
- Check out the full [API Reference](api/index.html) for advanced usage.

<div class="seo-keywords" aria-hidden="true">
  install radial menu android, add circular menu jetpack compose, radial menu android view setup,
  kotlin pie menu integration, how to implement arc menu android
</div>
