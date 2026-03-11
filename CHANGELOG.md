# Changelog
All notable changes to RadialMenu will be documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html)

## [Unreleased]

## [1.0.3] - 2026-03-12
### Added
- Zone detection system (`detectZone`) that identifies corner touches within `EDGE_THRESH_DP` (80dp) of screen edges
- Edge-hug layout mode: when 4+ items are triggered from a corner, items arrange in an L-shape along the two adjacent edges instead of a radial fan
- Nearest-item distance-based selection for edge-hug mode (replaces angle-based selection only in this mode)
- New constants: `EDGE_THRESH_DP`, `CORNER_ITEM_THRESHOLD`, `EDGE_HUG_GAP_DP`, `EDGE_HUG_PAD_DP` in `RadialMenuDefaults`
- `enableEdgeHugLayout` parameter added to `RadialMenuWrapper` (Compose) and `RadialMenuView` (Android View). Defaults to `false`. Edge-hug layout now requires explicit opt-in, existing integrations are unaffected
- Comprehensive test suite for all new functions (`EdgeHugLayoutTest`)
- Demo app: added item count slider (2-8 items) and edge-hug toggle switch. Demo now defaults to 4 items with edge-hug OFF

### Fixed
- Items no longer clip off-screen when the radial menu is triggered from a corner with 4+ items
- Fixed menu items rendering beneath UI elements (toolbars, bottom nav, FABs). Menu overlay now attaches to the window decor view, ensuring it always renders above all other UI
- Fixed false corner detection near in-app UI element boundaries. Zone detection now uses the true usable screen area (accounting for system bar insets) rather than raw display dimensions. Edge-hug only activates at real screen corners

### No Breaking Changes
- Public API is backwards compatible. New parameters have safe defaults.
- Radial layout and angle-based selection remain the default for center/edge touches and for 3 or fewer items.

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
