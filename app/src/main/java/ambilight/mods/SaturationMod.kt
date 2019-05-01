package ambilight.mods

import ambilight.LedColor
import kotlin.math.sqrt
import kotlin.reflect.KProperty

class SaturationMod(ledCount: Int, private var saturation: Float) : Mod(ledCount) {

	companion object {

		private const val Pr: Float = 0.299f
		private const val Pg: Float = 0.587f
		private const val Pb: Float = 0.114f
	}

	operator fun getValue(thisRef: Any?, property: KProperty<*>): Float = saturation
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
		saturation = value
	}

	override fun isUseful() = saturation != 1f

	override fun update(led: LedColor) {
		if (saturation == 1f)
			return

		val (R, G, B) = led
		val P = sqrt(R * R * Pr + G * G * Pg + B * B * Pb)

		led.r = (P + (R - P) * saturation).coerceIn(0f, 255f)
		led.g = (P + (G - P) * saturation).coerceIn(0f, 255f)
		led.b = (P + (B - P) * saturation).coerceIn(0f, 255f)
	}
}
