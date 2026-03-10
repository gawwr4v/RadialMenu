---
title: Customization — RadialMenu
description: Full customization guide for RadialMenu — Android's radial, circular, and pie menu library. Control colors, icons, item count, angles, and animations.
tags:
  - customization
  - radial menu colors
  - circular menu android
  - pie menu styling
  - android ui customization
---

# Customization

## Overview

RadialMenu provides extensive customization options for every visual and behavioral aspect
of the component. Whether you call it a radial menu, pie menu, or circular menu, you have
full control over colors, radii, icons, item count, and animations.

## Colors & Theming

You can easily adapt RadialMenu to light and dark modes or to match your brand colors.

### Kotlin (Programmatic)
```kotlin
radialMenu.apply {
    innerColor = Color.parseColor("#161B22")
    outerColor = Color.parseColor("#0D1117")
    selectedColor = Color.parseColor("#4F8EF7")
    centerIconColor = Color.WHITE
}
```

### Options inside Compose
```kotlin
RadialMenu(
    colors = RadialMenuDefaults.colors(
        innerRoundColor = Color(0xFF161B22),
        outerRoundColor = Color(0xFF0D1117),
        selectedItemColor = Color(0xFF4F8EF7)
    ),
    // ...
)
```

## Icons

Icons are defined per `RadialMenuItem`. We recommend using vector drawables (SVG/XML) for sharp rendering.

```kotlin
val items = listOf(
    RadialMenuItem(id = "copy", icon = R.drawable.ic_content_copy),
    RadialMenuItem(id = "paste", icon = R.drawable.ic_content_paste)
)
```

## Item Count & Angles

The menu automatically divides 360 degrees evenly among the items you provide.
For example, 4 items = 90 degrees each. 5 items = 72 degrees each.

There is no hard limit to the number of items, but for usability on mobile touchscreens, we recommend **3 to 8 items**.

## Animation

The open and close animations are spring-based and fluid. You can adjust the raw duration if needed:

```kotlin
radialMenu.animationDurationMs = 250L
```

---

## Full Attribute Reference

Here are the primary properties exposed for customization on the `RadialMenuView`. 
*(Note: The defaults shown below are the library's raw built-in defaults before any custom theming is applied).*

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `innerColor` | `Int` (Color) | `#424242` | Background color of the central ring |
| `outerColor` | `Int` (Color) | `#212121` | Background color of the outer ring holding items |
| `selectedColor` | `Int` (Color) | `#1976D2` | Highlight color when an item is hovered/selected |
| `iconTint` | `Int` (Color) | `#FFFFFF` | Tint applied to all menu icons |
| `centerIconTint` | `Int` (Color) | `#FFFFFF` | Tint applied to the icon in the center of the menu |
| `innerRadius` | `Float` | `60f` | Radius of the central inactive area |
| `outerRadius` | `Float` | `150f` | Outer boundary of the entire menu |
| `iconSize` | `Float` | `24f` | Size of the drawables inside the slices |
| `animationDuration`| `Long` | `300L` | Duration in milliseconds for open/close anims |
