package com.kyant.capsule

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import kotlin.math.min

@Immutable
data class G2Continuity(
    @param:FloatRange(from = 0.0, to = 1.0) val circleFraction: Double = 0.18,
    @param:FloatRange(from = 0.0) val extendedFraction: Double = 0.5,
    @param:FloatRange(from = 0.0, fromInclusive = false) val bezierCurvatureScale: Double = 1.15,
    @param:FloatRange(from = 0.0, fromInclusive = false) val circleCurvatureScale: Double = 1.16,
) : Continuity {
    override val hasSmoothness: Boolean = circleFraction < 1f && extendedFraction > 0f

    private val data =
        G2ContinuityData(
            circleFraction = circleFraction,
            extendedFraction = extendedFraction,
            bezierCurvatureScale = bezierCurvatureScale,
            circleCurvatureScale = circleCurvatureScale,
        )

    override fun createRoundedRectanglePathSegments(
        width: Double,
        height: Double,
        topLeft: Double,
        topRight: Double,
        bottomRight: Double,
        bottomLeft: Double,
    ): PathSegments {
        val centerX = width * 0.5
        val centerY = height * 0.5

        val hasCircle = circleFraction > 0f
        val extendedFraction = extendedFraction

        // safe extended fraction for each corner
        val topLeftFy = ((centerY - topLeft) / topLeft).fastCoerceAtMost(extendedFraction)
        val topLeftFx = ((centerX - topLeft) / topLeft).fastCoerceAtMost(extendedFraction)
        val topRightFx = ((centerX - topRight) / topRight).fastCoerceAtMost(extendedFraction)
        val topRightFy = ((centerY - topRight) / topRight).fastCoerceAtMost(extendedFraction)
        val bottomRightFy = ((centerY - bottomRight) / bottomRight).fastCoerceAtMost(extendedFraction)
        val bottomRightFx = ((centerX - bottomRight) / bottomRight).fastCoerceAtMost(extendedFraction)
        val bottomLeftFx = ((centerX - bottomLeft) / bottomLeft).fastCoerceAtMost(extendedFraction)
        val bottomLeftFy = ((centerY - bottomLeft) / bottomLeft).fastCoerceAtMost(extendedFraction)

        // safe extended length for each corner
        val topLeftDy = -topLeft * topLeftFy
        val topLeftDx = -topLeft * topLeftFx
        val topRightDx = -topRight * topRightFx
        val topRightDy = -topRight * topRightFy
        val bottomRightDy = -bottomRight * bottomRightFy
        val bottomRightDx = -bottomRight * bottomRightFx
        val bottomLeftDx = -bottomLeft * bottomLeftFx
        val bottomLeftDy = -bottomLeft * bottomLeftFy

        // safe circle curvature scale for each corner
        val circleCurvatureScale = if (circleCurvatureScale != 0.0) circleCurvatureScale else 1.0
        val topLeftCircleCurvatureScale =
            lerp(1.0, circleCurvatureScale, min(topLeftFx, topLeftFy) / extendedFraction)
        val topRightCircleCurvatureScale =
            lerp(1.0, circleCurvatureScale, min(topRightFx, topRightFy) / extendedFraction)
        val bottomRightCircleCurvatureScale =
            lerp(1.0, circleCurvatureScale, min(bottomRightFx, bottomRightFy) / extendedFraction)
        val bottomLeftCircleCurvatureScale =
            lerp(1.0, circleCurvatureScale, min(bottomLeftFx, bottomLeftFy) / extendedFraction)

        // safe bezier curvature scale for each corner
        val bezierCurvatureScale = bezierCurvatureScale
        val topLeftYBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, topLeftFy / extendedFraction)
        val topLeftXBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, topLeftFx / extendedFraction)
        val topRightXBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, topRightFx / extendedFraction)
        val topRightYBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, topRightFy / extendedFraction)
        val bottomRightYBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, bottomRightFy / extendedFraction)
        val bottomRightXBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, bottomRightFx / extendedFraction)
        val bottomLeftXBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, bottomLeftFx / extendedFraction)
        val bottomLeftYBezierCurvatureScale = lerp(1.0, bezierCurvatureScale, bottomLeftFy / extendedFraction)

        val segments = mutableListOf<Segment>()

        // draw clockwise

        var x = 0.0
        var y = topLeft
        var lastPoint = Point(x, y - topLeftDy)

        // top left corner
        if (topLeft > 0f) {
            // π -> 3/4 π
            data.getBezier(topLeftFy, topLeftYBezierCurvatureScale, topLeftCircleCurvatureScale).let { bezier ->
                val segment = Segment.Cubic(
                    lastPoint,
                    Point(x + bezier.p1.y * topLeft, y - bezier.p1.x * topLeft),
                    Point(x + bezier.p2.y * topLeft, y - bezier.p2.x * topLeft),
                    Point(x + bezier.p3.y * topLeft, y - bezier.p3.x * topLeft),
                )
                segments += segment
                lastPoint = segment.to
            }

            // circle
            if (hasCircle) {
                val segment = Segment.Arc(
                    center = Point(topLeft, topLeft),
                    radius = topLeft,
                    startAngle = HalfPI * 2.0 + data.bezierRadians,
                    sweepAngle = data.circleRadians,
                )
                val newSegment =
                    if (topLeftCircleCurvatureScale == 1.0) {
                        segment
                    } else {
                        Segment.IntrinsicArc(
                            from = segment.from,
                            to = segment.to,
                            radius = topLeft / topLeftCircleCurvatureScale,
                        )
                    }
                segments += newSegment
                lastPoint = segment.to
            }

            // 3/4 π -> 1/2 π
            x = topLeft
            y = 0.0
            data.getBezier(topLeftFx, topLeftXBezierCurvatureScale, topLeftCircleCurvatureScale).let { bezier ->
                val segment = Segment.Cubic(
                    lastPoint,
                    Point(x - bezier.p2.x * topLeft, y + bezier.p2.y * topLeft),
                    Point(x - bezier.p1.x * topLeft, y + bezier.p1.y * topLeft),
                    Point(
                        // Since [bezier.p0.x] may be smaller than [topLeftDx] (they are all negative), the point will
                        // be outside of the safe area, so we need to handle it.
                        // Fortunately, in this **special** G2 continuity case, the control points P_1, P_2 of the
                        // cubic Bezier curve are irrelevant of P_0 (only for horizontal coordinate, because the
                        // vertical coordinate should always be 0), so we can just clamp the horizontal coordinate of
                        // P_0 to a safe value without loss of G2 continuity.
                        x - (bezier.p0.x * topLeft).fastCoerceAtLeast(topLeftDx),
                        y + bezier.p0.y * topLeft,
                    ),
                )
                segments += segment
                lastPoint = segment.to
            }
        }

        // top line
        x = width - topRight
        y = 0.0
        segments += Segment.Line(lastPoint, Point(x + topRightDx, y))
        lastPoint = Point(x + topRightDx, y)

        // top right corner
        if (topRight > 0f) {
            // 1/2 π -> 1/4 π
            data.getBezier(topRightFx, topRightXBezierCurvatureScale, topRightCircleCurvatureScale).let { bezier ->
                val segment = Segment.Cubic(
                    lastPoint,
                    Point(x + bezier.p1.x * topRight, y + bezier.p1.y * topRight),
                    Point(x + bezier.p2.x * topRight, y + bezier.p2.y * topRight),
                    Point(x + bezier.p3.x * topRight, y + bezier.p3.y * topRight),
                )
                segments += segment
                lastPoint = segment.to
            }

            // circle
            if (hasCircle) {
                val segment = Segment.Arc(
                    center = Point(width - topRight, topRight),
                    radius = topRight,
                    startAngle = -(data.bezierRadians + data.circleRadians),
                    sweepAngle = data.circleRadians,
                )
                val newSegment =
                    if (topRightCircleCurvatureScale == 1.0) {
                        segment
                    } else {
                        Segment.IntrinsicArc(
                            from = segment.from,
                            to = segment.to,
                            radius = topRight / topRightCircleCurvatureScale,
                        )
                    }
                segments += newSegment
                lastPoint = segment.to
            }

            // 1/4 π -> 0
            x = width
            y = topRight
            data.getBezier(topRightFy, topRightYBezierCurvatureScale, topRightCircleCurvatureScale).let { bezier ->
                val segment = Segment.Cubic(
                    lastPoint,
                    Point(x - bezier.p2.y * topRight, y - bezier.p2.x * topRight),
                    Point(x - bezier.p1.y * topRight, y - bezier.p1.x * topRight),
                    Point(
                        x - bezier.p0.y * topRight,
                        y - (bezier.p0.x * topRight).fastCoerceAtLeast(topRightDy),
                    ),
                )
                segments += segment
                lastPoint = segment.to
            }
        }

        // right line
        x = width
        y = height - bottomRight
        segments += Segment.Line(lastPoint, Point(x, y + bottomRightDy))
        lastPoint = Point(x, y + bottomRightDy)

        // bottom right corner
        // 2 π -> 7/4 π
        if (bottomRight > 0f) {
            data.getBezier(bottomRightFy, bottomRightYBezierCurvatureScale, bottomRightCircleCurvatureScale)
                .let { bezier ->
                    val segment = Segment.Cubic(
                        lastPoint,
                        Point(x - bezier.p1.y * bottomRight, y + bezier.p1.x * bottomRight),
                        Point(x - bezier.p2.y * bottomRight, y + bezier.p2.x * bottomRight),
                        Point(x - bezier.p3.y * bottomRight, y + bezier.p3.x * bottomRight),
                    )
                    segments += segment
                    lastPoint = segment.to
                }

            // circle
            if (hasCircle) {
                val segment = Segment.Arc(
                    center = Point(width - bottomRight, height - bottomRight),
                    radius = bottomRight,
                    startAngle = data.bezierRadians,
                    sweepAngle = data.circleRadians,
                )
                val newSegment =
                    if (bottomRightCircleCurvatureScale == 1.0) {
                        segment
                    } else {
                        Segment.IntrinsicArc(
                            from = segment.from,
                            to = segment.to,
                            radius = bottomRight / bottomRightCircleCurvatureScale,
                        )
                    }
                segments += newSegment
                lastPoint = segment.to
            }

            // 7/4 π -> 3/2 π
            x = width - bottomRight
            y = height
            data.getBezier(bottomRightFx, bottomRightXBezierCurvatureScale, bottomRightCircleCurvatureScale)
                .let { bezier ->
                    val segment = Segment.Cubic(
                        lastPoint,
                        Point(x + bezier.p2.x * bottomRight, y - bezier.p2.y * bottomRight),
                        Point(x + bezier.p1.x * bottomRight, y - bezier.p1.y * bottomRight),
                        Point(
                            x + (bezier.p0.x * bottomRight).fastCoerceAtLeast(bottomRightDx),
                            y - bezier.p0.y * bottomRight,
                        ),
                    )
                    segments += segment
                    lastPoint = segment.to
                }
        }

        // bottom line
        x = bottomLeft
        y = height
        segments += Segment.Line(lastPoint, Point(x - bottomLeftDx, y))
        lastPoint = Point(x - bottomLeftDx, y)

        // bottom left corner
        if (bottomLeft > 0f) {
            // 3/2 π -> 5/4 π
            data.getBezier(bottomLeftFx, bottomLeftXBezierCurvatureScale, bottomLeftCircleCurvatureScale)
                .let { bezier ->
                    val segment = Segment.Cubic(
                        lastPoint,
                        Point(x - bezier.p1.x * bottomLeft, y - bezier.p1.y * bottomLeft),
                        Point(x - bezier.p2.x * bottomLeft, y - bezier.p2.y * bottomLeft),
                        Point(x - bezier.p3.x * bottomLeft, y - bezier.p3.y * bottomLeft),
                    )
                    segments += segment
                    lastPoint = segment.to
                }

            // circle
            if (hasCircle) {
                val segment = Segment.Arc(
                    center = Point(bottomLeft, height - bottomLeft),
                    radius = bottomLeft,
                    startAngle = HalfPI + data.bezierRadians,
                    sweepAngle = data.circleRadians,
                )
                val newSegment =
                    if (bottomLeftCircleCurvatureScale == 0.0) {
                        segment
                    } else {
                        Segment.IntrinsicArc(
                            from = segment.from,
                            to = segment.to,
                            radius = bottomLeft / bottomLeftCircleCurvatureScale,
                        )
                    }
                segments += newSegment
                lastPoint = segment.to
            }

            // 5/4 π -> π
            x = 0.0
            y = height - bottomLeft
            data.getBezier(bottomLeftFy, bottomLeftYBezierCurvatureScale, bottomLeftCircleCurvatureScale)
                .let { bezier ->
                    val segment = Segment.Cubic(
                        lastPoint,
                        Point(x + bezier.p2.y * bottomLeft, y + bezier.p2.x * bottomLeft),
                        Point(x + bezier.p1.y * bottomLeft, y + bezier.p1.x * bottomLeft),
                        Point(
                            x + bezier.p0.y * bottomLeft,
                            y + (bezier.p0.x * bottomLeft).fastCoerceAtLeast(bottomLeftDy),
                        ),
                    )
                    segments += segment
                    lastPoint = segment.to
                }
        }

        // left line
        segments += Segment.Line(lastPoint, segments.first().from)

        return segments
    }

    override fun lerp(stop: Continuity, fraction: Float): Continuity {
        return when (stop) {
            is G1Continuity ->
                G2Continuity(
                    circleFraction = lerp(circleFraction.toFloat(), 1f, fraction).toDouble(),
                    extendedFraction = lerp(extendedFraction.toFloat(), 0f, fraction).toDouble(),
                )

            is G2Continuity ->
                G2Continuity(
                    circleFraction = lerp(circleFraction, stop.circleFraction, fraction.toDouble()),
                    extendedFraction = lerp(extendedFraction, stop.extendedFraction, fraction.toDouble()),
                )

            else -> stop.lerp(this, 1f - fraction)
        }
    }
}
