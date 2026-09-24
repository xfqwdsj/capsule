package com.kyant.capsule

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Interpolatable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.kyant.capsule.continuities.G1Continuity
import com.kyant.capsule.continuities.G2Continuity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class LerpTest {

    private val density = Density(1f)
    private val size = Size(100f, 100f)

    @Test
    fun continuousRoundedRectangleLerpsCornerSizes() {
        val start = ContinuousRoundedRectangle(0.dp)
        val stop = ContinuousRoundedRectangle(20.dp)

        val result = assertIs<ContinuousRoundedRectangle>(start.lerp(stop, 0.5f))

        assertEquals(10f, result.topStart.toPx(size, density))
        assertEquals(10f, result.topEnd.toPx(size, density))
        assertEquals(10f, result.bottomEnd.toPx(size, density))
        assertEquals(10f, result.bottomStart.toPx(size, density))
    }

    @Test
    fun continuousRoundedRectangleLerpsPercentCornerSizes() {
        val start = ContinuousRoundedRectangle(0.dp)
        val stop = ContinuousCapsule

        val result = assertIs<ContinuousRoundedRectangle>(start.lerp(stop, 0.5f))

        assertEquals(25f, result.topStart.toPx(size, density))
        assertEquals(25f, result.bottomEnd.toPx(size, density))
    }

    @Test
    fun continuousRoundedRectangleLerpsContinuity() {
        val start = ContinuousRoundedRectangle(8.dp, continuity = G1Continuity)
        val stop = ContinuousRoundedRectangle(8.dp, continuity = G2Continuity())

        val result = assertIs<ContinuousRoundedRectangle>(start.lerp(stop, 0.5f))

        assertIs<G2Continuity>(result.continuity)
    }

    @Test
    fun continuousRoundedRectangleLerpsFromRectangleShape() {
        val shape = ContinuousRoundedRectangle(20.dp)

        val result = assertIs<ContinuousRoundedRectangle>(shape.lerp(RectangleShape, 0.5f))

        assertEquals(10f, result.topStart.toPx(size, density))
        assertEquals(10f, result.bottomStart.toPx(size, density))
    }

    @Test
    fun continuousRoundedRectangleLerpsFromNull() {
        val shape = ContinuousRoundedRectangle(20.dp)

        val result = assertIs<ContinuousRoundedRectangle>(shape.lerp(null, 0.5f))

        assertEquals(10f, result.topStart.toPx(size, density))
    }

    @Test
    fun continuousRoundedRectangleLerpsUnknownShapeToNull() {
        assertNull(ContinuousRoundedRectangle(20.dp).lerp(RoundedCornerShape(20.dp), 0.5f))
    }

    @Test
    fun concentricShapeLerps() {
        val start = ContinuousRoundedRectangle(16.dp).concentricInset(4.dp)
        val stop = ContinuousRoundedRectangle(8.dp)

        val result = assertIs<ContinuousRoundedRectangle>(start.lerp(stop, 0.5f))

        assertEquals(10f, result.topStart.toPx(size, density))
    }

    @Test
    fun interpolatableLerpUsesShapeLerp() {
        val result = Interpolatable.lerp(
            ContinuousRoundedRectangle(0.dp),
            ContinuousRoundedRectangle(20.dp),
            0.5f,
        )

        val shape = assertIs<ContinuousRoundedRectangle>(result)
        assertEquals(10f, shape.topStart.toPx(size, density))
    }

    @Test
    fun interpolatableLerpFallsBackToOtherShape() {
        val result = Interpolatable.lerp(
            RectangleShape,
            ContinuousRoundedRectangle(20.dp),
            0.5f,
        )

        val shape = assertIs<ContinuousRoundedRectangle>(result)
        assertEquals(10f, shape.topStart.toPx(size, density))
    }

    @Test
    fun absoluteContinuousRoundedRectangleLerpsCornerSizes() {
        val start = AbsoluteContinuousRoundedRectangle(0.dp)
        val stop = AbsoluteContinuousRoundedRectangle(20.dp)

        val result = assertIs<AbsoluteContinuousRoundedRectangle>(start.lerp(stop, 0.5f))

        assertEquals(10f, result.topStart.toPx(size, density))
        assertEquals(10f, result.topEnd.toPx(size, density))
        assertEquals(10f, result.bottomEnd.toPx(size, density))
        assertEquals(10f, result.bottomStart.toPx(size, density))
    }

    @Test
    fun absoluteContinuousRoundedRectangleLerpsFromRectangleShape() {
        val shape = AbsoluteContinuousRoundedRectangle(20.dp)

        val result =
            assertIs<AbsoluteContinuousRoundedRectangle>(shape.lerp(RectangleShape, 0.5f))

        assertEquals(10f, result.topStart.toPx(size, density))
    }

    @Test
    fun absoluteContinuousRoundedRectangleLerpsUnknownShapeToNull() {
        assertNull(
            AbsoluteContinuousRoundedRectangle(20.dp).lerp(RoundedCornerShape(20.dp), 0.5f),
        )
    }
}
