# Contributing to RadialMenu

Thank you for your interest in contributing!

## Community Standards
- Read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing
- Report security issues via [SECURITY.md](SECURITY.md), not public issues
- For questions, use [GitHub Discussions](https://github.com/gawwr4v/RadialMenu/discussions)

## Getting Started
1. Fork the repository
2. Clone your fork
3. Open in Android Studio (latest stable)
4. Sync Gradle — should work out of the box

## Requirements
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35

## Project Structure
- `radialmenu/` — the library (this is what gets published)
  - `src/commonMain/` — shared Kotlin/Compose code
  - `src/androidMain/` — Android-specific implementations
  - `src/desktopMain/` — Desktop JVM implementations
- `app/` — Android demo application
- `.github/` — CI/CD workflows and issue templates

## Making Changes
- All library code goes in `radialmenu/src/`
- Do NOT add external dependencies to the library module
- Do NOT modify `calculateCenterAngle()` without extensive testing
- Do NOT break the public API without a major version bump
- Add KDoc to every public class, method, and property you add

## Running Tests
```
./gradlew :radialmenu:allTests
./gradlew :radialmenu:assembleRelease
./gradlew :radialmenu:compileKotlinDesktop
```

All three must pass before submitting a PR.

## Code Style
- Follow Kotlin coding conventions
- Detekt is enforced — run `./gradlew detekt` before submitting
- No new lint warnings allowed

## Submitting a PR
1. Create a branch: `git checkout -b feature/your-feature-name`
2. Make your changes
3. Run all tests and builds
4. Update CHANGELOG.md under [Unreleased]
5. Open a PR with a clear description of what and why

## Reporting Bugs
Use the Bug Report issue template. Include:
- Library version
- Platform (Android/Desktop)
- Steps to reproduce
- Expected vs actual behavior

## Requesting Features
Use the Feature Request issue template.

## Code of Conduct
Be respectful. We're all here to build something useful.
