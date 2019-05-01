package ambilight.mods

import ambilight.LedColor
import kotlin.reflect.KProperty

class SmoothnessMod(ledCount: Int, private var smoothness: Int) : Mod(ledCount) {

	companion object {
		private const val MAX_FADE = 256
	}

	operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = smoothness
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
		smoothness = value
	}

	private val smoothColors: Array<LedColor> = Array(ledCount) { LedColor(0f, 0f, 0f) }

	override fun isUseful() = smoothness != 0

	override fun update(colors: Array<LedColor>): Array<LedColor> {
		if (smoothness == 0)
			return colors

		val fade = smoothness
		val fadeInv = MAX_FADE - fade

		for (ledNum in 0 until ledCount) {
			val smoothColor = smoothColors[ledNum]
			val (sR, sG, sB) = smoothColor
			val (tR, tG, tB) = colors[ledNum]

			smoothColor.r = ((sR * fade + tR * fadeInv) / MAX_FADE)
			smoothColor.g = ((sG * fade + tG * fadeInv) / MAX_FADE)
			smoothColor.b = ((sB * fade + tB * fadeInv) / MAX_FADE)
		}
		return smoothColors
	}
}
