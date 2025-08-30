package com.kyant.capsule

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline

@Immutable
interface Continuity {
    val hasSmoothness: Boolean

    fun createRoundedRectanglePathSegments(
        width: Double,
        height: Double,
        topLeft: Double,
        topRight: Double,
        bottomRight: Double,
        bottomLeft: Double,
    ): PathSegments = emptyList()

    @Deprecated("Use createRoundedRectanglePathSegments instead")
    fun createRoundedRectangleOutline(
        size: Size,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
    ): Outline {
        val path = createRoundedRectanglePathSegments(
            width = size.width.toDouble(),
            height = size.height.toDouble(),
            topLeft = topLeft.toDouble(),
            topRight = topRight.toDouble(),
            bottomRight = bottomRight.toDouble(),
            bottomLeft = bottomLeft.toDouble(),
        ).toPath()
        return Outline.Generic(path)
    }

    fun createHorizontalCapsuleOutline(size: Size): Outline {
        val cornerRadius = size.height * 0.5f
        @Suppress("DEPRECATION")
        return createRoundedRectangleOutline(
            size,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
        )
    }

    fun createVerticalCapsuleOutline(size: Size): Outline {
        val cornerRadius = size.width * 0.5f
        @Suppress("DEPRECATION")
        return createRoundedRectangleOutline(
            size,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
        )
    }

    fun lerp(stop: Continuity, fraction: Float): Continuity

    companion object {
        @Stable
        val Default: Continuity = G2Continuity()
    }
}
