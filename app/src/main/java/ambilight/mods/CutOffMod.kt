package ambilight.mods

import ambilight.LedColor
import kotlin.reflect.KProperty

class CutOffMod(ledCount: Int, private var cutOff: Int) : Mod(ledCount) {

	operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = cutOff
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
		cutOff = value
	}

	override fun isUseful() = cutOff != 0

	override fun update(led: LedColor) {
		if (cutOff == 255) {
			led.r = 0f
			led.g = 0f
			led.b = 0f
		} else {
			val hsvValue = 0.2126f * led.r + 0.7152f * led.g + 0.0722f * led.b
			val multi = (255 * (hsvValue - cutOff) / (255 - cutOff)).coerceAtLeast(0f)

			led.r = led.r * multi / hsvValue
			led.g = led.g * multi / hsvValue
			led.b = led.b * multi / hsvValue
		}
	}
}