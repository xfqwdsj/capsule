package com.kyant.capsule.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kyant.capsule.lerp
import kotlin.math.sqrt

@Immutable
public data class Point(val x: Double, val y: Double) {

    @Stable
    public operator fun unaryMinus(): Point {
        return Point(-x, -y)
    }

    @Stable
    public operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    @Stable
    public operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    @Stable
    public operator fun times(operand: Double): Point {
        return Point(x * operand, y * operand)
    }

    @Stable
    public operator fun div(operand: Double): Point {
        return Point(x / operand, y / operand)
    }

    @Stable
    public fun normalized(): Point {
        val length = sqrt(x * x + y * y)
        return if (length != 0.0) this / length else Zero
    }

    public companion object {

        @Stable
        public val Zero: Point = Point(0.0, 0.0)
    }
}

@Stable
public fun lerp(start: Point, stop: Point, fraction: Double): Point {
    return Point(
        lerp(start.x, stop.x, fraction),
        lerp(start.y, stop.y, fraction)
    )
}
