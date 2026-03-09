# Changelog
All notable changes to RadialMenu will be documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html)

## [Unreleased]

## [1.0.2] - 2026-03-10
### Fixed
- Migrated publishing plugin to official `com.gradleup.nmcp` publisher for Maven Central Portal Portal API compatibility

## [1.0.1] - 2026-03-09
### Fixed
- Migrated publishing from deprecated s01.oss.sonatype.org to new Maven Central Portal API

## [1.0.0], 2026-03-08
### Added
- Initial release of RadialMenu for Android and Desktop
- Long-press to activate, drag-to-select gesture system
- Smart edge-aware angle calculation (never obscured by finger)
- Support for unlimited items (2 to 8 recommended)
- Item badge support (count and custom text)
- Toggle icon states (active/inactive per item)
- Haptic feedback on activation and selection
- Dynamic item scaling on hover (configurable)
- Drag direction indicator line
- RadialMenuAnimationConfig with 4 presets (default, snappy, bouncy, slow)
- Spring physics animation option
- RadialMenuColors theming system
- RadialMenuView for XML/View-based Android apps
- RadialMenuWrapper Composable for Compose Multiplatform (Android + Desktop)
- Accessibility support (contentDescription, announceForAccessibility, semantics)
- Zero external dependencies
- Kotlin-first API with Java compatibility via @JvmOverloads
- Full KDoc documentation on all public API
- ProGuard/R8 consumer rules
- Dark/light/auto theme support via RadialMenuColors.dark(), .light(), .autoTheme()
- RTL (right-to-left) layout direction support
- Android Studio @Preview composables for RadialMenuCanvas
- RadialMenuMath is now public API for custom implementations
- Snapshot release support
- SECURITY.md, CODE_OF_CONDUCT.md, SUPPORT.md
- Min SDK 21 (Android 5.0)
