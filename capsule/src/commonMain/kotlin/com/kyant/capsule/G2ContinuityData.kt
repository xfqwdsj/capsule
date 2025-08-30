package com.kyant.capsule

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal class G2ContinuityData(
    val circleFraction: Double,
    val extendedFraction: Double,
    val bezierCurvatureScale: Double,
    val circleCurvatureScale: Double,
) {
    val circleRadians = HalfPI * circleFraction
    val bezierRadians = (HalfPI - circleRadians) * 0.5

    private val sin = sin(bezierRadians)
    private val cos = cos(bezierRadians)
    private val halfTan = sin / (1.0 + cos)

    @Suppress("LocalVariableName")
    private val bezier =
        if (bezierCurvatureScale == 1.0 && circleCurvatureScale == 1.0) {
            // fast path
            CubicBezier(
                Point(-extendedFraction, 0.0),
                Point((1.0 - 1.5 / (1.0 + cos)) * halfTan, 0.0),
                Point(halfTan, 0.0),
                Point(sin, 1.0 - cos),
            )
        } else {
            val endTangent =
                if (circleFraction > 0.0) {
                    // find the unit tangent of the modified circle at the connection point
                    Segment.IntrinsicArc(
                        from = Point(sin, 1.0 - cos),
                        to = Point(
                            sin(bezierRadians + circleRadians),
                            1.0 - cos(bezierRadians + circleRadians),
                        ),
                        radius = 1.0 / circleCurvatureScale,
                    ).unitTangentAt(0.0)
                } else {
                    Point(1.0 / sqrt(2.0), 1.0 / sqrt(2.0))
                }

            /* solved using G2 continuity conditions:
            start = Point(-extendedFraction, 0.0)
            end = Point(sin, 1.0 - cos)
            startTangent = Point(1.0, 0.0)
            endTangent = endTangent
            startCurvature = 0.0
            endCurvature = bezierCurvatureScale
            */
            val B = 1.5 * bezierCurvatureScale
            val G = endTangent.y
            val dx = sin - (-extendedFraction)
            val dy = 1.0 - cos
            val C = -dy
            val D = dy * endTangent.x - dx * endTangent.y
            val lambda0 = -D / G - B * C * C / G / G / G
            CubicBezier(
                Point(-extendedFraction, 0.0),
                Point(-extendedFraction + lambda0, 0.0),
                Point(sin - dy * (endTangent.x / endTangent.y), 0.0),
                Point(sin, 1.0 - cos),
            )
        }

    private var _capsuleBezier: CubicBezier? = null

    // used when the corner radius is full => extendedFraction == 0.0
    private val capsuleBezier
        get() = _capsuleBezier
            ?: CubicBezier(
                Point.Zero,
                Point((1.0 - 1.5 / (1.0 + cos)) * halfTan, 0.0),
                Point(halfTan, 0.0),
                Point(sin, 1.0 - cos),
            ).also {
                _capsuleBezier = it
            }

    fun getBezier(
        extendedFraction: Double,
        bezierCurvatureScale: Double,
        circleCurvatureScale: Double,
    ): CubicBezier {
        if (extendedFraction == this.extendedFraction &&
            bezierCurvatureScale == this.bezierCurvatureScale &&
            circleCurvatureScale == this.circleCurvatureScale
        ) {
            return bezier
        }
        if (extendedFraction == 0.0) {
            return capsuleBezier
        }
        return G2ContinuityData(
            circleFraction = circleFraction,
            extendedFraction = extendedFraction,
            bezierCurvatureScale = bezierCurvatureScale,
            circleCurvatureScale = circleCurvatureScale,
        ).bezier
    }
}
