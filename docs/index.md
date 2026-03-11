---
title: RadialMenu — Lightweight Radial Menu for Android
description: RadialMenu is a lightweight Android library for radial menus, circular menus, and pie menus. Built for Jetpack Compose and Android Views. Gesture-driven, open source, and Kotlin-first.
hide:
  - toc
---

<div class="homepage-hero">
  <img src="assets/radialMenuLogo.png" alt="RadialMenu logo" width="80" height="80">
  <h1>RadialMenu</h1>
  <p class="tagline">A lightweight, fully customizable radial menu for Android</p>
</div>

<p class="seo-synonyms">
  RadialMenu is also known as a circular menu, pie menu, arc menu, or wheel menu -
  all referring to the same gesture-driven radial interaction pattern popular in
  modern Android UI design. If you searched for any of those terms, you are in the right place.
</p>

```kotlin title="build.gradle.kts"
implementation("io.github.gawwr4v:radialmenu:1.0.2")
```

<div align="center" style="margin: 4rem 0; display: flex; justify-content: center; gap: 4rem; flex-wrap: wrap;">
  <div style="text-align: center;">
    <img src="assets/demo1.gif" alt="RadialMenu on Android" width="380" style="border-radius: 12px; border: 1px solid var(--md-border-color); margin-bottom: 0.5rem;">
    <div style="font-size: 0.9rem; color: var(--md-default-fg-color--light);">Android App</div>
  </div>
  <div style="text-align: center;">
    <img src="assets/demo2.gif" alt="RadialMenu on Desktop (Compose Multiplatform)" width="380" style="border-radius: 12px; border: 1px solid var(--md-border-color); margin-bottom: 0.5rem;">
    <div style="font-size: 0.9rem; color: var(--md-default-fg-color--light);">Desktop (Compose)</div>
  </div>
</div>

## Why RadialMenu?

Traditional Android context menus (like `PopupMenu` or `ContextMenu`) force users to read vertical lists, breaking their flow. Radial menus leverage **muscle memory**. Because items are arranged in a circle, the distance and direction to each item are consistent. Once a user learns where "Copy" or "Delete" is, they can trigger it with a single, rapid flick of the thumb without even looking.

**RadialMenu** was built to bring this premium interaction paradigm to Android with zero friction.

<div class="features-grid">
  <div class="feature-card">
    <div class="feature-icon">🎨</div>
    <h3>Fully Customizable</h3>
    <p>Style every aspect from colors to animations to match your app's exact design system.</p>
  </div>
  <div class="feature-card">
    <div class="feature-icon">⚡</div>
    <h3>Lightweight</h3>
    <p>Minimal dependencies and optimized drawing for maximum performance and fluid 60fps animations.</p>
  </div>
  <div class="feature-card">
    <div class="feature-icon">👆</div>
    <h3>Gesture-Driven</h3>
    <p>Intuitive interaction model: long-press to open, drag towards an item, release to select.</p>
  </div>
  <div class="feature-card">
    <div class="feature-icon">🧩</div>
    <h3>Jetpack Compose Ready</h3>
    <p>Built from the ground up for modern Android development with full Compose support.</p>
  </div>
</div>

<div class="seo-keywords" aria-hidden="true">
  radial menu android, circular menu android, pie menu android, arc menu android,
  wheel menu kotlin, jetpack compose radial menu, android gesture menu,
  kotlin multiplatform menu, compose custom menu, android open source menu library
</div>
