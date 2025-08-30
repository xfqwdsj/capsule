package com.kyant.capsule

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import kotlin.math.*

sealed interface Segment {
    val from: Point
    val to: Point

    fun pointAt(t: Double): Point
    fun unitTangentAt(t: Double): Point
    fun curvatureAt(t: Double): Double

    fun drawTo(path: Path)

    data class Line(
        override val from: Point,
        override val to: Point,
    ) : Segment {

        private val delta = to - from

        override fun pointAt(t: Double): Point {
            return from + delta * t
        }

        override fun unitTangentAt(t: Double): Point {
            return delta.normalized()
        }

        override fun curvatureAt(t: Double): Double {
            return 0.0
        }

        override fun drawTo(path: Path) {
            path.lineTo(to.x.toFloat(), to.y.toFloat())
        }
    }

    data class IntrinsicArc(
        override val from: Point,
        override val to: Point,
        val radius: Double,
    ) : Segment {
        val center: Point
        val startAngle: Double
        val sweepAngle: Double

        init {
            val mid = (from + to) * 0.5
            val dir = to - from
            val len = sqrt(dir.x * dir.x + dir.y * dir.y)
            val height = sqrt(radius * radius - (len * 0.5) * (len * 0.5))
            val norm = Point(-dir.y / len, dir.x / len)
            center = mid + norm * height * if (radius > 0) 1.0 else -1.0
            startAngle = atan2(from.y - center.y, from.x - center.x)
            val endAngle = atan2(to.y - center.y, to.x - center.x)
            sweepAngle = when {
                radius > 0 && endAngle >= startAngle -> endAngle - startAngle
                radius > 0 && endAngle < startAngle -> endAngle + 2.0 * PI - startAngle
                radius < 0 && endAngle <= startAngle -> endAngle - startAngle
                radius < 0 && endAngle > startAngle -> endAngle - 2.0 * PI - startAngle
                else -> 0.0
            }
        }

        override fun pointAt(t: Double): Point {
            val angle = startAngle + sweepAngle * t
            return Point(
                center.x + cos(angle) * radius,
                center.y + sin(angle) * radius,
            )
        }

        override fun unitTangentAt(t: Double): Point {
            val angle = startAngle + sweepAngle * t
            return Point(-sin(angle), cos(angle))
        }

        override fun curvatureAt(t: Double): Double {
            return 1.0 / radius
        }

        override fun drawTo(path: Path) {
            path.arcToRad(
                rect = Rect(
                    center = Offset(center.x.toFloat(), center.y.toFloat()),
                    radius = radius.toFloat(),
                ),
                startAngleRadians = startAngle.toFloat(),
                sweepAngleRadians = sweepAngle.toFloat(),
                forceMoveTo = false,
            )
        }
    }

    data class Arc(
        val center: Point,
        val radius: Double,
        val startAngle: Double,
        val sweepAngle: Double,
    ) : Segment {
        override val from: Point
            get() = Point(
                center.x + cos(startAngle) * radius,
                center.y + sin(startAngle) * radius,
            )

        override val to: Point
            get() = Point(
                center.x + cos(startAngle + sweepAngle) * radius,
                center.y + sin(startAngle + sweepAngle) * radius,
            )

        override fun pointAt(t: Double): Point {
            val angle = startAngle + sweepAngle * t
            return Point(
                center.x + cos(angle) * radius,
                center.y + sin(angle) * radius,
            )
        }

        override fun unitTangentAt(t: Double): Point {
            val angle = startAngle + sweepAngle * t
            return Point(-sin(angle), cos(angle))
        }

        override fun curvatureAt(t: Double): Double {
            return 1.0 / radius
        }

        override fun drawTo(path: Path) {
            path.arcToRad(
                rect = Rect(
                    center = Offset(center.x.toFloat(), center.y.toFloat()),
                    radius = radius.toFloat(),
                ),
                startAngleRadians = startAngle.toFloat(),
                sweepAngleRadians = sweepAngle.toFloat(),
                forceMoveTo = false,
            )
        }
    }

    data class Cubic(
        val p0: Point,
        val p1: Point,
        val p2: Point,
        val p3: Point,
    ) : Segment {
        override val from: Point
            get() = p0

        override val to: Point
            get() = p3

        override fun pointAt(t: Double): Point {
            val u = 1.0 - t
            return p0 * (u * u * u) + p1 * (3.0 * u * u * t) + p2 * (3.0 * u * t * t) + p3 * (t * t * t)
        }

        override fun unitTangentAt(t: Double): Point {
            val u = 1.0 - t
            return ((p1 - p0) * (3.0 * u * u) + (p2 - p1) * (6.0 * u * t) + (p3 - p2) * (3.0 * t * t)).normalized()
        }

        override fun curvatureAt(t: Double): Double {
            val u = 1.0 - t
            val d1 = (p1 - p0) * (3.0 * u * u) + (p2 - p1) * (6.0 * u * t) + (p3 - p2) * (3.0 * t * t)
            val d2 = (p2 - p1 * 2.0 + p0) * (6.0 * (1.0 - t)) + (p3 - p2 * 2.0 + p1) * (6.0 * t)
            val cross = d1.x * d2.y - d1.y * d2.x
            val len = sqrt(d1.x * d1.x + d1.y * d1.y)
            return if (len != 0.0) {
                cross / (len * len * len)
            } else {
                0.0
            }
        }

        override fun drawTo(path: Path) {
            path.cubicTo(
                p1.x.toFloat(), p1.y.toFloat(),
                p2.x.toFloat(), p2.y.toFloat(),
                p3.x.toFloat(), p3.y.toFloat(),
            )
        }
    }
}
