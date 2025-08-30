package com.kyant.capsule

import kotlin.math.PI

internal const val HalfPI = PI * 0.5

internal fun lerp(start: Double, stop: Double, fraction: Double): Double {
    return start + (stop - start) * fraction
}
