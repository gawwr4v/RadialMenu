/**
 * # RadialMenu
 * A Kotlin Multiplatform radial menu library for Android and Desktop.
 *
 * ## Quick Start
 * ### Compose (Android + Desktop)
 * ```kotlin
 * RadialMenuWrapper(
 *     items = listOf(
 *         RadialMenuItem(id = 1, icon = painterResource(R.drawable.ic_share), label = "Share"),
 *         RadialMenuItem(id = 2, icon = painterResource(R.drawable.ic_heart), label = "Like")
 *     ),
 *     onItemSelected = { item -> handleSelection(item) }
 * ) {
 *     Text("Long press me")
 * }
 * ```
 * ### Android View (XML)
 * ```xml
 * <io.github.gawwr4v.radialmenu.RadialMenuView
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:rm_menuRadius="90dp" />
 * ```
 *
 * @since 1.0.0
 */
package io.github.gawwr4v.radialmenu
