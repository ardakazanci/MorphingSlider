package com.ardakazanci.customslider.util

import kotlin.math.roundToInt


fun snapValue(value: Float, range: ClosedFloatingPointRange<Float>, tickCount: Int): Float {
    if (tickCount < 2) return value
    val stepSize = (range.endInclusive - range.start) / (tickCount - 1)
    val snappedSteps = (value - range.start) / stepSize
    val roundedSteps = snappedSteps.roundToInt()
    return (range.start + (roundedSteps * stepSize)).coerceIn(range.start, range.endInclusive)
}
