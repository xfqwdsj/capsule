package com.kyant.capsule

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import com.kyant.capsule.continuities.G2Continuity
import com.kyant.capsule.path.PathSegments
import com.kyant.capsule.path.toPath

@Immutable
public interface Continuity {

    public fun createRoundedRectanglePathSegments(
        width: Double,
        height: Double,
        topLeft: Double,
        topRight: Double,
        bottomRight: Double,
        bottomLeft: Double
    ): PathSegments

    public fun createRoundedRectangleOutline(
        size: Size,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    ): Outline {
        val path =
            createRoundedRectanglePathSegments(
                width = size.width.toDouble(),
                height = size.height.toDouble(),
                topLeft = topLeft.toDouble(),
                topRight = topRight.toDouble(),
                bottomRight = bottomRight.toDouble(),
                bottomLeft = bottomLeft.toDouble()
            ).toPath()
        return Outline.Generic(path)
    }

    public fun createCapsuleOutline(size: Size): Outline {
        val radius = size.minDimension * 0.5f
        return createRoundedRectangleOutline(
            size = size,
            topLeft = radius,
            topRight = radius,
            bottomRight = radius,
            bottomLeft = radius
        )
    }

    public fun lerp(stop: Continuity, fraction: Double): Continuity

    public companion object {

        @Stable
        public val Default: Continuity = G2Continuity()
    }
}
