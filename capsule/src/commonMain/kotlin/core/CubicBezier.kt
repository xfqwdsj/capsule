package com.kyant.capsule.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
public data class CubicBezier(
    val p0: Point,
    val p1: Point,
    val p2: Point,
    val p3: Point
) {

    @Stable
    public operator fun times(operand: Double): CubicBezier {
        return CubicBezier(
            p0 * operand,
            p1 * operand,
            p2 * operand,
            p3 * operand
        )
    }
}
