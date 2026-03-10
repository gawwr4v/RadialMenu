---
title: Radial Menu vs Circular Menu vs Pie Menu — What's the Difference?
description: Radial menu, circular menu, pie menu, arc menu, and wheel menu all describe the same Android UI pattern. RadialMenu implements all of them for Jetpack Compose and Android Views.
tags:
  - radial menu
  - circular menu
  - pie menu
  - arc menu
  - wheel menu
  - android ui patterns
---

# Radial Menu, Circular Menu, Pie Menu — All the Same Thing

Radial menu, circular menu, pie menu, arc menu, and wheel menu all describe the exact same UI interaction pattern: a set of actions arranged in a circular formation around a central trigger point, typically revealed by a long-press or swipe gesture.

## What Makes a Good Radial Menu?

- **Gesture activation:** Seamlessly triggered by a continued touch or long-press without lifting the finger.
- **Equal spacing:** Actions divided evenly around the circle for predictable muscle memory.
- **Icon clarity:** Distinct visual markers since text doesn't fit elegantly in angled slices.
- **Animation feedback:** Snappy, physics-based expansion that reinforces the gesture.
- **Touch target size:** Sufficiently large arc sections to prevent accidental selections.

## How RadialMenu Implements This Pattern

RadialMenu brings this premium pattern to Android by offering a highly customizable, lightweight component that works natively in both Jetpack Compose and traditional Android Views. It mathematically guarantees equal spacing, handles all edge-bounding logic, and uses fluid spring animations.

```kotlin title="build.gradle.kts"
implementation("io.github.gawwr4v:radialmenu:1.0.2")
```

## Radial Menu vs Bottom Sheet vs Context Menu

| Pattern | Trigger | Best For | Android Support |
|---------|---------|----------|-----------------|
| Radial Menu | Long-press/Drag | Quick, muscle-memory actions | Via RadialMenu library |
| Bottom Sheet | Tap/Swipe | Complex forms or deep content | Native |
| Context Menu | Long-press | Long vertical lists of text actions | Native |
| Floating Action | Tap | Single primary screen action | Native |

## Ready to Build?

- [Getting Started](getting-started.md)
- [API Reference](api/index.html)
