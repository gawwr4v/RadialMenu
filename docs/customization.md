---
title: Customization - RadialMenu
description: Configure colors, animations, trigger modes, edge-hug behavior, and Android XML attributes.
---

# Customization

This page covers the configurable parts of the library and where each configuration belongs.

## Compose API Responsibilities

- `RadialMenuWrapper`: trigger handling, gesture detection, selection behavior
- `RadialMenuOverlay`: visual rendering, colors, animation configuration

## Trigger Mode Customization

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    triggerMode = RadialMenuTriggerMode.Auto
) { Content() }
```

Available trigger modes:
- `Auto`
- `LongPress(positionAware: Boolean = true)`
- `SecondaryClick(positionAware: Boolean = false)`
- `KeyboardHold(key: Key = Key.Q)`

Notes:
- `positionAware` applies to `LongPress` and `SecondaryClick`.
- `KeyboardHold` always uses center spawn and angle-based pie-slice selection.
- Keyboard hold flick direction is measured from cursor position at key-down.

## Edge-Hug Layout

```kotlin
RadialMenuWrapper(
    items = items,
    onItemSelected = { /* ... */ },
    enableEdgeHugLayout = true
) { Content() }
```

Behavior:
- Active only when enabled
- Triggers in corners with 4+ items
- Uses L-shaped placement + nearest-item selection
- Automatically skipped for center-spawned keyboard menus

## Colors (Compose)

Pass color config to `RadialMenuOverlay`:

```kotlin
RadialMenuOverlay(
    items = items,
    colors = RadialMenuColors.autoTheme()
)
```

Custom colors:

```kotlin
RadialMenuOverlay(
    items = items,
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
)
```

## Animations (Compose)

Pass animation config to `RadialMenuOverlay`:

```kotlin
RadialMenuOverlay(
    items = items,
    animationConfig = RadialMenuAnimationConfig.bouncy()
)
```

Preset options:
- `RadialMenuAnimationConfig.default()`
- `RadialMenuAnimationConfig.snappy()`
- `RadialMenuAnimationConfig.bouncy()`
- `RadialMenuAnimationConfig.slow()`

Custom example:

```kotlin
RadialMenuOverlay(
    items = items,
    animationConfig = RadialMenuAnimationConfig(
        openDurationMs = 250,
        closeDurationMs = 150,
        itemScaleDurationMs = 80,
        selectedItemScale = 1.6f,
        enableSpringAnimation = true,
        springDampingRatio = Spring.DampingRatioMediumBouncy,
        springStiffness = Spring.StiffnessMedium
    )
)
```

## Item and Badge Customization

```kotlin
val items = listOf(
    RadialMenuItem(
        id = 1,
        icon = copyPainter,
        label = "Copy",
        badgeCount = 5
    ),
    RadialMenuItem(
        id = 2,
        icon = savePainter,
        iconActive = savedPainter,
        label = "Save",
        isActive = false,
        badgeText = "NEW"
    )
)
```

Android overloads for View-friendly icon setup:

```kotlin
// Drawable resource overload
RadialMenuItem(
    context = this,
    id = 3,
    iconRes = R.drawable.ic_share,
    label = "Share"
)

// Drawable overload
RadialMenuItem(
    id = 4,
    icon = drawable,
    label = "Bookmark"
)
```

## Android View XML Attributes

| XML Attribute | Format | Default | Description |
|---|---|---|---|
| `app:rm_accentColor` | `color` | `White` | Selected item background color |
| `app:rm_overlayColor` | `color` | `Black 50%` | Background scrim color |
| `app:rm_badgeColor` | `color` | `#FF4444` | Badge background color |
| `app:rm_menuRadius` | `dimension` | `90dp` | Distance from center to icon |
| `app:rm_iconSize` | `dimension` | `32dp` | Icon size |
| `app:rm_animationDurationMs` | `integer` | `100` | Item scale animation duration |

## Android View Trigger Notes

`RadialMenuView` supports:
- `LongPress(positionAware = true/false)`
- `SecondaryClick(positionAware = true/false)`

`KeyboardHold` is accepted on the property for API symmetry but not executed in `RadialMenuView`.
