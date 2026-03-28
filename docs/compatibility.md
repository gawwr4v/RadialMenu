---
title: Compatibility - RadialMenu
description: Runtime/toolchain compatibility and consumer diagnostics.
---

# Compatibility

## Matrix

| Platform | Artifact | Min runtime | Kotlin | Compose | AGP |
|---|---|---|---|---|---|
| Android (Compose + View) | `io.github.gawwr4v:radialmenu` | API 21 | 2.x | 1.7+ (optional for View-only) | 8.5+ |
| Desktop JVM (Compose) | `io.github.gawwr4v:radialmenu` | Java 17 | 2.x | Compose Desktop | N/A |

## Consumer Diagnostics

Use these commands in the consuming app to inspect dependency resolution:

```bash
# Verify artifact resolution
./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep radialmenu

# Check selected variant / artifact metadata
./gradlew :app:dependencyInsight --dependency radialmenu --configuration releaseRuntimeClasspath

# Desktop consumer check: confirm desktop artifact selection
./gradlew :desktopApp:dependencyInsight --dependency radialmenu --configuration runtimeClasspath

# Desktop bytecode check: Java 17 should report major version 61
javap -verbose -classpath ~/.m2/repository/io/github/gawwr4v/radialmenu-desktop/1.0.5/radialmenu-desktop-1.0.5.jar io.github.gawwr4v.radialmenu.RadialMenuTriggerMode
```

## Known Issues By Release

### 1.0.5

- Desktop bytecode target fixed to Java 17.
- KMP target publication mapping corrected (`radialmenu` metadata points to `radialmenu-android` and `radialmenu-desktop`).

### Pre-1.0.5 releases

- Some consumers reported Android artifact resolution requiring explicit `@aar` in specific configurations.
- Desktop artifacts may require Java 21 in older releases.

### 1.0.3

- Incomplete release: desktop and KMP metadata artifacts were not fully published.

Recommendation: use `1.0.5` or newer.
