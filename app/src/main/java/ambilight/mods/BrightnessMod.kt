package ambilight.mods

import ambilight.LedColor
import kotlin.reflect.KProperty

class BrightnessMod(ledCount: Int, private var brightness: Float) : Mod(ledCount) {

	operator fun getValue(thisRef: Any?, property: KProperty<*>): Float = brightness
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
		brightness = value
	}

	override fun isUseful() = brightness != 1f

	override fun update(led: LedColor) {
		led.r *= brightness
		led.g *= brightness
		led.b *= brightness
	}
}
