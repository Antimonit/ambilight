package me.khol.ambilight.mods

import me.khol.ambilight.LedColor
import kotlin.math.ln
import kotlin.math.pow

/**
 * http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
 */
class TemperatureMod(var temperature: Int) : DiscreteMod() {

	override fun update(led: LedColor) {
		val temperature = temperature / 100f

		val red = when {
			temperature <= 66 -> 255.0f
			else -> 329.698727446f * (temperature - 60).pow(-0.1332047592f)
		}.coerceIn(0f, 255f)

		val green = when {
			temperature <= 66 -> 99.4708025861f * ln(temperature) - 161.1195681661f
			else -> 288.1221695283f * (temperature - 60).pow(-0.0755148492f)
		}.coerceIn(0f, 255f)

		val blue = when {
			temperature >= 66 -> 255f
			temperature <= 19 -> 0f
			else -> 138.5177312231f * ln(temperature - 10) - 305.0447927307f
		}.coerceIn(0f, 255f)

		led.r = led.r * red / 256
		led.g = led.g * green / 256
		led.b = led.b * blue / 256
	}
}
