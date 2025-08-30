package com.kyant.capsule

import androidx.compose.ui.graphics.Path

typealias PathSegments = List<Segment>

fun PathSegments.toPath(): Path {
    return Path().apply {
        if (isEmpty()) return@apply
        val startPoint = first().from
        moveTo(startPoint.x.toFloat(), startPoint.y.toFloat())
        forEach { it.drawTo(this) }
    }
}
