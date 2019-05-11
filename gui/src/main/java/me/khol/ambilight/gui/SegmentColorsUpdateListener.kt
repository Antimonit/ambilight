package me.khol.ambilight.gui

import me.khol.ambilight.LedColor

interface SegmentColorsUpdateListener {

	fun updatedSegmentColors(segmentColors: Array<LedColor>)
}
