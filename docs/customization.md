---
title: Customization — RadialMenu
description: Full customization guide for RadialMenu — Android's radial, circular, and pie menu library. Control colors, icons, item count, angles, and animations.
---

# Customization

## Overview

RadialMenu provides extensive customization options for every visual and behavioral aspect
of the component. Whether you call it a radial menu, pie menu, or circular menu, you have
full control over colors, radii, icons, item count, and animations.

## Colors & Theming

You can easily adapt RadialMenu to light and dark modes or to match your brand colors.

### Compose Multiplatform

Colors are controlled via the `RadialMenuColors` class. The library provides convenient dark, light, and auto-theme factories.

```kotlin
import io.github.gawwr4v.radialmenu.RadialMenuColors

// Auto-theme follows the system dark mode setting
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    colors = RadialMenuColors.autoTheme() 
) {
    // Content...
}
```

If you need a completely custom palette:

```kotlin
RadialMenuWrapper(
    colors = RadialMenuColors(
        itemBackground = Color(0xFF424242),
        itemBackgroundSelected = Color.White,
        iconTint = Color.White,
        iconTintSelected = Color.Black,
        overlayColor = Color.Black.copy(alpha = 0.5f),
        centerIndicatorColor = Color.White.copy(alpha = 0.3f),
        badgeColor = Color(0xFFFF4444),
        badgeTextColor = Color.White
    )
) { ... }
```

## Icons and Labels

Icons are defined per `RadialMenuItem`. We recommend using vector drawables (SVG/XML) for sharp rendering. The `id` is an integer, and the icon must be converted to a Compose `Painter`.

```kotlin
val items = listOf(
    RadialMenuItem(
        id = 1, 
        icon = painterResource(R.drawable.ic_copy),
        label = "Copy" // Label is used for accessibility descriptions
    ),
    RadialMenuItem(
        id = 2, 
        icon = painterResource(R.drawable.ic_paste),
        label = "Paste"
    )
)
```

## Item Count & Angles

The menu automatically divides 360 degrees evenly among the items you provide with a default 45° spacing. There is no hard limit to the number of items, but for usability on mobile touchscreens, we recommend **3 to 8 items**. The library will emit a `Logcat` warning if you exceed 8 items, so you will see a `W/RadialMenu` message in your console if this threshold is crossed.

## Animation

The open and close animations are highly customizable. You can use a preset (default, snappy, bouncy, slow) or provide your own physics.

```kotlin
import io.github.gawwr4v.radialmenu.RadialMenuAnimationConfig

// Example: using the bouncy physics preset
RadialMenuWrapper(
    animationConfig = RadialMenuAnimationConfig.bouncy()
) { ... }
```

Customizing everything, including turning on Spring physics overrides:

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

---

## Full XML Attribute Reference (Android Views)

If you are using `RadialMenuView` inside a traditional Android XML layout, these are the exposed custom attributes you can define dynamically in your layout file:

| XML Attribute | Format | Default Value | Description |
|-----------|------|---------|-------------|
| `app:rm_accentColor` | `color` | `White` | The background color of the currently selected/hovered item. |
| `app:rm_overlayColor` | `color` | `Black 50%` | Background dimming scrim color drawn behind the menu. |
| `app:rm_badgeColor` | `color` | `#FF4444` | The color of notification badges attached to items. |
| `app:rm_menuRadius` | `dimension` | `90dp` | The distance from the center touch point to the outer radial items. |
| `app:rm_iconSize` | `dimension` | `32dp` | The width and height of the drawables inside the slices. |
| `app:rm_animationDurationMs` | `integer` | `100` | Duration in milliseconds for the scale "pop" animation when you hover an item. |

<div class="seo-keywords" aria-hidden="true">
  customize radial menu colors, android pie menu styling, circular menu animation physics,
  radial menu item icons, compose custom menu appearance
</div>
