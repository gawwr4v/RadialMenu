# Changelog
All notable changes to RadialMenu are documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html)

## [Unreleased]

## [1.0.4] - 2026-03-13
### New Features
- Added `RadialMenuTriggerMode` with four modes:
  - `Auto` (default)
  - `LongPress(positionAware = true)`
  - `SecondaryClick(positionAware = false)`
  - `KeyboardHold(key)`
- `Auto` resolves per platform:
  - Android -> `LongPress(positionAware = true)`
  - Desktop -> `SecondaryClick(positionAware = false)`
- Added desktop right-click support via `SecondaryClick`.
- Added keyboard-hold trigger support in Compose wrapper via `KeyboardHold`.
- Added smart edge-hug gating:
  - edge-hug is automatically skipped for center-spawned keyboard menus.

### Bug Fixes
- Fixed incomplete `1.0.3` Maven Central publish. `1.0.4` is the first complete Android + Desktop + KMP metadata release.
- Removed external runtime dependencies from published POM.
  - POM now declares only `kotlin-stdlib`.
  - Compose and AndroidX are treated as provided by the consumer project.
- Fixed KeyboardHold trigger reliability on desktop focus/event flows.
- Fixed KeyboardHold selection flow:
  - key-down opens only
  - hover updates while key is held
  - key-up commits selection
- Switched center-spawned KeyboardHold to angle-based pie-slice selection.
- Added dead zone around KeyboardHold origin to reduce accidental picks.
- Fixed KeyboardHold calibration so flick direction is measured from cursor position at key-down, not from menu center.
- Updated SecondaryClick behavior:
  - menu opens at cursor
  - pie-style directional selection behavior
  - neutral default orientation (`positionAware = false`)

### API Changes
- `LongPress` and `SecondaryClick` changed from `object` to `data class` and now include `positionAware`.
- Added `triggerMode` parameter to `RadialMenuWrapper`.
- Added `triggerMode` property to `RadialMenuView`.
- Added `defaultTriggerMode` expect/actual.

## [1.0.3] - 2026-03-12
### Added
- Zone detection system (`detectZone`) for corner touches near screen edges.
- Edge-hug layout mode for corners with 4+ items.
- Nearest-item distance selection for edge-hug mode.
- `enableEdgeHugLayout` for `RadialMenuWrapper` and `RadialMenuView` (default `false`).
- Edge-hug test coverage (`EdgeHugLayoutTest`).

### Fixed
- Prevented clipping when opening from corners.
- Ensured menu overlays render above common UI layers.
- Improved true-corner detection using usable content area.

### No Breaking Changes
- Public API remained backward compatible.

## [1.0.2] - 2026-03-10
### Fixed
- Migrated publishing plugin to `com.gradleup.nmcp` for Maven Central Portal compatibility.

## [1.0.1] - 2026-03-09
### Fixed
- Migrated publishing from deprecated Sonatype endpoint to Maven Central Portal API.

## [1.0.0] - 2026-03-08
### Added
- Initial release for Android + Desktop (KMP).
