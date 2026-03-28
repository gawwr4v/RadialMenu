---
title: Troubleshooting - RadialMenu
description: Common integration issues and fixes for Android and Desktop consumers.
---

# Troubleshooting

## Imports are unresolved after adding the dependency

### Symptom

`Unresolved reference: RadialMenuView` (or similar), even after adding:

```kotlin
implementation("io.github.gawwr4v:radialmenu:1.0.5")
```

### Cause

In some builds, Gradle variant resolution may not automatically select the Android artifact from metadata.

### Fix

```kotlin
implementation("io.github.gawwr4v:radialmenu:1.0.5@aar")
```

Diagnostics:

```bash
./gradlew :app:dependencies --configuration releaseRuntimeClasspath
./gradlew :app:dependencyInsight --dependency radialmenu --configuration releaseRuntimeClasspath
```

## UnsupportedClassVersionError on Desktop

### Symptom

`compiled by a more recent version of the Java Runtime (class file version 65.0)`

### Cause

Older pre-`1.0.5` artifacts were compiled to Java 21 bytecode.

### Fix

- Upgrade to `1.0.5` (desktop artifact now targets Java 17 bytecode).
- Or run Java 21+ if you intentionally stay on older releases.

## Compose type errors when using RadialMenuView

### Symptom

`Cannot access class 'Painter'` while integrating `RadialMenuView`.

### Cause

`RadialMenuItem` includes a `Painter`-based API surface; if your module has no Compose UI dependency and you use only that constructor, Kotlin may fail type resolution.

### Fix options

Option 1: add Compose UI dependency:

```kotlin
implementation("androidx.compose.ui:ui:1.8.0")
```

Option 2: use Android overloads:

```kotlin
RadialMenuItem(context = this, id = 1, iconRes = R.drawable.ic_share, label = "Share")
RadialMenuItem(id = 2, icon = drawable, label = "Like")
```
