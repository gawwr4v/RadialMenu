---
title: Getting Started — RadialMenu
description: How to install and integrate RadialMenu into your Android project in under 5 minutes.
---

# Getting Started

## Requirements

- **Min SDK:** 21 (Android 5.0 Lollipop) or higher
- **Kotlin:** 1.9.0 or higher
- Jetpack Compose (if using the Compose wrapper)

## Installation

Add the dependency to your app's `build.gradle` file:

=== "Kotlin DSL (build.gradle.kts)"

    ```kotlin
    dependencies {
        implementation("io.github.gawwr4v:radialmenu:1.0.2")
    }
    ```

=== "Groovy DSL (build.gradle)"

    ```groovy
    dependencies {
        implementation 'io.github.gawwr4v:radialmenu:1.0.2'
    }
    ```

## Basic Setup

### Jetpack Compose

```kotlin
import androidx.compose.runtime.Composable
import io.github.gawwr4v.radialmenu.RadialMenu

@Composable
fun MyScreen() {
    RadialMenu(
        items = listOf(
            RadialMenuItem(id = "home", icon = R.drawable.ic_home),
            RadialMenuItem(id = "search", icon = R.drawable.ic_search),
            RadialMenuItem(id = "settings", icon = R.drawable.ic_settings)
        ),
        onItemClick = { item ->
            println("Clicked: ${item.id}")
        }
    ) {
        // Your content here that triggers the menu on long press
    }
}
```

### XML Layouts

```xml
<io.github.gawwr4v.radialmenu.RadialMenuView
    android:id="@+id/radial_menu"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## Your First Menu

```kotlin
val radialMenu = findViewById<RadialMenuView>(R.id.radial_menu)

val items = listOf(
    RadialMenuItem("1", R.drawable.ic_action_1),
    RadialMenuItem("2", R.drawable.ic_action_2),
    RadialMenuItem("3", R.drawable.ic_action_3)
)

radialMenu.setItems(items)

radialMenu.setOnItemClickListener { item ->
    Toast.makeText(context, "Selected: ${item.id}", Toast.LENGTH_SHORT).show()
}

// Show the menu at specific coordinates (e.g., from a touch event)
radialMenu.showAt(x = 500f, y = 800f)
```

## Next Steps

- Explore [Customization](customization.md) to theme the menu to your app.
- Check out the full [API Reference](api/io.github.gawwr4v.radialmenu/index.html) for advanced usage.
