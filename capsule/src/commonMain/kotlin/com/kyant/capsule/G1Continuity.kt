package com.kyant.capsule

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.util.lerp

@Immutable
data object G1Continuity : Continuity {
    override val hasSmoothness: Boolean = false

    @Deprecated("Use createRoundedRectanglePathSegments instead")
    override fun createRoundedRectangleOutline(
        size: Size,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
    ): Outline {
        return Outline.Rounded(
            RoundRect(
                rect = size.toRect(),
                topLeft = CornerRadius(topLeft),
                topRight = CornerRadius(topRight),
                bottomRight = CornerRadius(bottomRight),
                bottomLeft = CornerRadius(bottomLeft),
            ),
        )
    }

    override fun lerp(stop: Continuity, fraction: Float): Continuity {
        return when (stop) {
            is G1Continuity -> this
            is G2Continuity ->
                G2Continuity(
                    circleFraction = lerp(1f, stop.circleFraction.toFloat(), fraction).toDouble(),
                    extendedFraction = lerp(0f, stop.extendedFraction.toFloat(), fraction).toDouble(),
                )

            else -> stop.lerp(this, 1f - fraction)
        }
    }
}
