package me.khol.ambilight.mods

import me.khol.ambilight.LedColor
import kotlin.math.sqrt

class SaturationMod(var saturation: Float) : DiscreteMod() {

	companion object {

		private const val Pr: Float = 0.299f
		private const val Pg: Float = 0.587f
		private const val Pb: Float = 0.114f
	}

	override fun isUseful() = saturation != 1f

	override fun update(led: LedColor) {
		val (R, G, B) = led
		val P = sqrt(R * R * Pr + G * G * Pg + B * B * Pb)

		led.r = (P + (R - P) * saturation).coerceIn(0f, 255f)
		led.g = (P + (G - P) * saturation).coerceIn(0f, 255f)
		led.b = (P + (B - P) * saturation).coerceIn(0f, 255f)
	}
}
