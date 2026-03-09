@file:Suppress("UnusedPrivateMember")
package io.github.gawwr4v.radialmenu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview

/**
 * Preview of [RadialMenuCanvas] with 3 items in default state.
 * Use this in Android Studio's Preview panel to inspect the menu appearance.
 */
@Preview(name = "RadialMenu - 3 items", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun RadialMenuCanvasPreview3Items() {
    val items = listOf(
        RadialMenuItem(id = 1, icon = rememberVectorPainter(Icons.Filled.Share), label = "Share"),
        RadialMenuItem(id = 2, icon = rememberVectorPainter(Icons.Filled.Favorite), label = "Like"),
        RadialMenuItem(id = 3, icon = rememberVectorPainter(Icons.Filled.Edit), label = "Edit")
    )
    RadialMenuCanvas(
        center = Offset(200f, 300f),
        dragOffset = Offset.Zero,
        selectionIndex = null,
        items = items,
        colors = RadialMenuColors.dark(),
        centerAngle = 270f,
        animationConfig = RadialMenuAnimationConfig.default()
    )
}

/**
 * Preview of [RadialMenuCanvas] with 5 items.
 */
@Preview(name = "RadialMenu - 5 items", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun RadialMenuCanvasPreview5Items() {
    val items = listOf(
        RadialMenuItem(id = 1, icon = rememberVectorPainter(Icons.Filled.Share), label = "Share"),
        RadialMenuItem(id = 2, icon = rememberVectorPainter(Icons.Filled.Favorite), label = "Like"),
        RadialMenuItem(id = 3, icon = rememberVectorPainter(Icons.Filled.Edit), label = "Edit"),
        RadialMenuItem(id = 4, icon = rememberVectorPainter(Icons.Filled.Star), label = "Star"),
        RadialMenuItem(id = 5, icon = rememberVectorPainter(Icons.Filled.Warning), label = "Flag")
    )
    RadialMenuCanvas(
        center = Offset(200f, 300f),
        dragOffset = Offset.Zero,
        selectionIndex = null,
        items = items,
        colors = RadialMenuColors.dark(),
        centerAngle = 270f,
        animationConfig = RadialMenuAnimationConfig.default()
    )
}

/**
 * Preview of [RadialMenuCanvas] with a selected item and badge.
 */
@Preview(
    name = "RadialMenu - with badge and selection",
    showBackground = true,
    backgroundColor = 0xFF121212
)
@Composable
private fun RadialMenuCanvasPreviewWithBadge() {
    val items = listOf(
        RadialMenuItem(
            id = 1,
            icon = rememberVectorPainter(Icons.Filled.Share),
            label = "Share",
            badgeCount = 3
        ),
        RadialMenuItem(
            id = 2,
            icon = rememberVectorPainter(Icons.Filled.Favorite),
            label = "Like"
        ),
        RadialMenuItem(
            id = 3,
            icon = rememberVectorPainter(Icons.Filled.Edit),
            label = "Edit",
            badgeText = "NEW"
        )
    )
    RadialMenuCanvas(
        center = Offset(200f, 300f),
        dragOffset = Offset(60f, -80f),
        selectionIndex = 1,
        items = items,
        colors = RadialMenuColors.dark(),
        centerAngle = 270f,
        animationConfig = RadialMenuAnimationConfig.default()
    )
}
