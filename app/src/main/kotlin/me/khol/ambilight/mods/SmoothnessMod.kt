package me.khol.ambilight.mods

import me.khol.ambilight.LedColor

class SmoothnessMod(private val ledCount: Int, var smoothness: Int) : Mod {

	companion object {
		private const val MAX_FADE = 256
	}

	private val smoothColors: Array<LedColor> = Array(ledCount) { LedColor() }

	override fun update(colors: Array<LedColor>): Array<LedColor> {
		if (smoothness == 0)
			return colors

		val fade = smoothness
		val fadeInv = MAX_FADE - fade

		for (ledNum in 0 until ledCount) {
			val smoothColor = smoothColors[ledNum]
			val color = colors[ledNum]
			val (sR, sG, sB) = smoothColor
			val (tR, tG, tB) = color

			smoothColor.r = ((sR * fade + tR * fadeInv) / MAX_FADE)
			smoothColor.g = ((sG * fade + tG * fadeInv) / MAX_FADE)
			smoothColor.b = ((sB * fade + tB * fadeInv) / MAX_FADE)
		}

		return smoothColors
	}
}
