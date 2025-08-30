package com.kyant.capsule

import kotlin.math.sqrt

data class Point(val x: Double, val y: Double) {
    operator fun unaryMinus(): Point {
        return Point(-x, -y)
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun times(operand: Double): Point {
        return Point(x * operand, y * operand)
    }

    operator fun div(operand: Double): Point {
        return Point(x / operand, y / operand)
    }

    fun normalized(): Point {
        val length = sqrt(x * x + y * y)
        return if (length != 0.0) this / length else Zero
    }

    companion object {
        val Zero: Point = Point(0.0, 0.0)
    }
}

fun lerp(start: Point, stop: Point, fraction: Double): Point {
    return Point(
        lerp(start.x, stop.x, fraction),
        lerp(start.y, stop.y, fraction),
    )
}
