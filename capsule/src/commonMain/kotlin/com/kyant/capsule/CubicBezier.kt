package com.kyant.capsule

import androidx.compose.ui.util.fastCoerceAtLeast
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal data class CubicBezier(
    val p0: Point,
    val p1: Point,
    val p2: Point,
    val p3: Point,
) {
    fun derivativeAt(t: Double): Point {
        val u = 1.0 - t
        return (p1 - p0) * (3.0 * u * u) + (p2 - p1) * (6.0 * u * t) + (p3 - p2) * (3.0 * t * t)
    }

    fun derivativeAtEnd(): Point {
        return (p3 - p2) * 3.0
    }

    fun curvatureAt(t: Double): Double {
        val d1 = derivativeAt(t)
        val d2 = (p2 - p1 * 2.0 + p0) * (6.0 * (1.0 - t)) + (p3 - p2 * 2.0 + p1) * (6.0 * t)
        val cross = d1.x * d2.y - d1.y * d2.x
        val len = sqrt(d1.x * d1.x + d1.y * d1.y)
        return if (len != 0.0) {
            cross / (len * len * len)
        } else {
            0.0
        }
    }

    fun splitFirst(t: Double): CubicBezier {
        val p01 = lerp(p0, p1, t)
        val p12 = lerp(p1, p2, t)
        val p23 = lerp(p2, p3, t)

        val p012 = lerp(p01, p12, t)
        val p123 = lerp(p12, p23, t)

        val p0123 = lerp(p012, p123, t)

        val first = CubicBezier(p0 = p0, p1 = p01, p2 = p012, p3 = p0123)
        return first
    }

    companion object {
        @Suppress("LocalVariableName")
        fun generateG2ContinuousBezier(
            start: Point,
            end: Point,
            startTangentialAngle: Double,
            endTangentialAngle: Double,
            startCurvature: Double,
            endCurvature: Double,
        ): CubicBezier {
            val A = 1.5 * startCurvature
            val B = 1.5 * endCurvature
            val G = sin(endTangentialAngle - startTangentialAngle)
            val dx = end.x - start.x
            val dy = end.y - start.y
            val C = -dy * cos(startTangentialAngle) + dx * sin(startTangentialAngle)
            val D = dy * cos(endTangentialAngle) - dx * sin(endTangentialAngle)

            val lambda0: Double
            val lambda3: Double

            if (endTangentialAngle == startTangentialAngle) {
                lambda0 = sqrt(-C / A)
                lambda3 = sqrt(-D / B)
            } else {
                if (A == 0.0 || B == 0.0) {
                    lambda0 = -D / G - B * C * C / G / G / G
                    lambda3 = -C / G - A * D * D / G / G / G
                } else {
                    val a = 2.0 * D / B
                    val b = G * G * G / A / B / B
                    val c = (A * D * D + C * G * G) / A / B / B
                    val p = -a * a / 12.0 - c
                    val q = -a * a * a / 108.0 + a * c / 3.0 - b * b / 8.0
                    val w = cbrt(-q / 2.0 + sqrt((q * q / 4.0 + p * p * p / 27.0).fastCoerceAtLeast(0.0)))
                    val y = a / 6 + w - p / w / 3.0
                    val x1 = 0.5 * (-sqrt(y + y - a) + sqrt(-y - y - a + (b + b) / sqrt(y + y - a)))
                    val x2 = 0.5 * (-sqrt(y + y - a) - sqrt(-y - y - a + (b + b) / sqrt(y + y - a)))
                    val x3 = 0.5 * (sqrt(y + y - a) + sqrt(-y - y - a - (b + b) / sqrt(y + y - a)))
                    val x4 = 0.5 * (sqrt(y + y - a) - sqrt(-y - y - a - (b + b) / sqrt(y + y - a)))
                    lambda3 = when {
                        x1 >= 0.0 && x1 <= 1.0 -> x1
                        x2 >= 0.0 && x2 <= 1.0 -> x2
                        x3 >= 0.0 && x3 <= 1.0 -> x3
                        x4 >= 0.0 && x4 <= 1.0 -> x4
                        else -> 0.0
                    }
                    lambda0 = (-D - B * lambda3 * lambda3) / G
                }
            }

            val p0 = start
            val p1 = start + Point(
                (lambda0 * cos(startTangentialAngle)).fastCoerceAtLeast(0.0),
                (lambda0 * sin(startTangentialAngle)).fastCoerceAtLeast(0.0),
            )
            val p2 = end - Point(
                (lambda3 * cos(endTangentialAngle)).fastCoerceAtLeast(0.0),
                (lambda3 * sin(endTangentialAngle)).fastCoerceAtLeast(0.0),
            )
            val p3 = end

            return CubicBezier(p0, p1, p2, p3)
        }

        @Suppress("LocalVariableName")
        fun generateG2ContinuousBezier(
            start: Point,
            end: Point,
            startTangent: Point,
            endTangent: Point,
            startCurvature: Double,
            endCurvature: Double,
        ): CubicBezier {
            val A = 1.5 * startCurvature
            val B = 1.5 * endCurvature
            val G = startTangent.x * endTangent.y - startTangent.y * endTangent.x
            val dx = end.x - start.x
            val dy = end.y - start.y
            val C = -dy * startTangent.x + dx * startTangent.y
            val D = dy * endTangent.x - dx * endTangent.y

            val lambda0: Double
            val lambda3: Double

            if (G == 0.0) {
                lambda0 = sqrt(-C / A)
                lambda3 = sqrt(-D / B)
            } else {
                if (A == 0.0 || B == 0.0) {
                    lambda0 = -D / G - B * C * C / G / G / G
                    lambda3 = -C / G - A * D * D / G / G / G
                } else {
                    val a = 2.0 * D / B
                    val b = G * G * G / A / B / B
                    val c = (A * D * D + C * G * G) / A / B / B
                    val p = -a * a / 12.0 - c
                    val q = -a * a * a / 108.0 + a * c / 3.0 - b * b / 8.0
                    val w = cbrt(-q / 2.0 + sqrt((q * q / 4.0 + p * p * p / 27.0).fastCoerceAtLeast(0.0)))
                    val y = a / 6 + w - p / w / 3.0
                    val x1 = 0.5 * (-sqrt(y + y - a) + sqrt(-y - y - a + (b + b) / sqrt(y + y - a)))
                    val x2 = 0.5 * (-sqrt(y + y - a) - sqrt(-y - y - a + (b + b) / sqrt(y + y - a)))
                    val x3 = 0.5 * (sqrt(y + y - a) + sqrt(-y - y - a - (b + b) / sqrt(y + y - a)))
                    val x4 = 0.5 * (sqrt(y + y - a) - sqrt(-y - y - a - (b + b) / sqrt(y + y - a)))
                    lambda3 = when {
                        x1 >= 0.0 && x1 <= 1.0 -> x1
                        x2 >= 0.0 && x2 <= 1.0 -> x2
                        x3 >= 0.0 && x3 <= 1.0 -> x3
                        x4 >= 0.0 && x4 <= 1.0 -> x4
                        else -> 0.0
                    }
                    lambda0 = (-D - B * lambda3 * lambda3) / G
                }
            }

            val p0 = start
            val p1 = start + Point(
                (lambda0 * startTangent.x).fastCoerceAtLeast(0.0),
                (lambda0 * startTangent.y).fastCoerceAtLeast(0.0),
            )
            val p2 = end - Point(
                (lambda3 * endTangent.x).fastCoerceAtLeast(0.0),
                (lambda3 * endTangent.y).fastCoerceAtLeast(0.0),
            )
            val p3 = end

            return CubicBezier(p0, p1, p2, p3)
        }
    }
}
