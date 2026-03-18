# Changelog

All notable changes to RadialMenu are documented here.  
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)  
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html)

## [Unreleased]

## [1.0.4] - 2026-03-19

### New Features
- Added `RadialMenuTriggerMode` with:
  - `Auto` (default)
  - `LongPress(positionAware = true)`
  - `SecondaryClick(positionAware = false)`
  - `KeyboardHold(key)`
- `Auto` resolves by platform:
  - Android -> `LongPress(positionAware = true)`
  - Desktop -> `SecondaryClick(positionAware = false)`
- Added desktop right-click trigger support (`SecondaryClick`).
- Added keyboard hold trigger support (`KeyboardHold`) for Compose wrapper.
- Added smart edge-hug gating: center-spawned keyboard menus skip edge-hug automatically.

### Bug Fixes
- Fixed incomplete `1.0.3` Maven Central publish; `1.0.4` is the first complete Android + Desktop + KMP metadata release.
- Removed unintended runtime dependency declarations from published POM.
  - POM now declares only `kotlin-stdlib`.
  - Compose/AndroidX are treated as provided by consuming apps.
- Fixed desktop KeyboardHold key capture reliability in Compose flows.
- Fixed KeyboardHold hover behavior:
  - no selection on key-down
  - continuous hover updates while key is held
  - commit on key-up
- Switched center-spawned KeyboardHold selection to angle-based pie-slice logic.
- Added center dead zone to prevent accidental hover near origin.
- Fixed KeyboardHold directional calibration:
  - flick direction is computed from cursor position at key-down (flick origin), not menu center.
- Updated SecondaryClick desktop behavior:
  - spawns at cursor position
  - uses pie-style directional selection
  - defaults to neutral orientation (`positionAware = false`)

### API Changes
- `LongPress` and `SecondaryClick` changed from `object` to `data class` with `positionAware`.
- Added `triggerMode` parameter on `RadialMenuWrapper`.
- Added `triggerMode` property on `RadialMenuView`.
- Added `defaultTriggerMode` expect/actual.

## [1.0.3] - 2026-03-12

### Added
- Corner zone detection.
- Edge-hug L-shaped layout for corners when item count is 4+.
- Nearest-item distance selection in edge-hug mode.
- `enableEdgeHugLayout` for Compose wrapper and Android View (opt-in, default `false`).

### Fixed
- Prevented item clipping in tight corner scenarios.
- Ensured overlay draws above common UI surfaces (toolbars/FAB/navigation).
- Improved true corner detection using usable content area.

## [1.0.2] - 2026-03-10

### Fixed
- Migrated publishing plugin to official `com.gradleup.nmcp` for Maven Central Portal compatibility.

## [1.0.1] - 2026-03-09

### Fixed
- Migrated publishing from deprecated Sonatype endpoint to Maven Central Portal API.

## [1.0.0] - 2026-03-08

### Added
- Initial release for Android + Desktop (KMP).
