---
title: Radial Menu vs Circular Menu vs Pie Menu
description: Terminology and practical interaction differences, plus where RadialMenu fits.
---

# Radial Menu, Circular Menu, and Pie Menu

In practice these terms are usually used for the same interaction family:
- Actions are laid out around a center point
- Users select by directional movement rather than scanning a long list

The naming varies by community, but the core model is direction-first selection.

## What RadialMenu Supports

- Radial fan behavior (`LongPress`, `positionAware` options)
- Pie-style directional behavior (`SecondaryClick`, `KeyboardHold`)
- Corner fallback (`enableEdgeHugLayout`) for constrained space

## Trigger Patterns by Platform

### Android

- Default: long press (`Auto -> LongPress(positionAware = true)`)
- Optional: explicit long press with or without position-aware angle adjustment

### Desktop

- Default: right-click (`Auto -> SecondaryClick(positionAware = false)`)
- Optional: keyboard hold (`KeyboardHold(Key.Q)`)

For keyboard hold:
- Menu appears at center
- Selection is angle-based pie-slice
- Flick direction starts from cursor position at key-down
- Selection commits on key release

## Radial Menu vs Other UI Patterns

| Pattern | Trigger | Best for | Typical tradeoff |
|---|---|---|---|
| Radial/Pie menu | Directional gesture | Repeated quick actions | Requires directional learnability |
| Bottom sheet | Tap/swipe | Dense content/forms | Slower repeated action selection |
| Linear context menu | Tap/long press | Text-heavy action lists | More visual scanning |
| Single FAB action | Tap | One primary action | Limited action density |

## Implementation Tip

If your users are mostly desktop:
- Keep `Auto` or set `SecondaryClick(positionAware = false)` explicitly.

If your users are mostly touch:
- Keep `Auto` or set `LongPress(positionAware = true)`.
