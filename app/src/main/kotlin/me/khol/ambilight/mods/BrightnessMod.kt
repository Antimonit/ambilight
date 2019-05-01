package me.khol.ambilight.mods

import me.khol.ambilight.LedColor

class BrightnessMod(var brightness: Float) : DiscreteMod() {

	override fun isUseful() = brightness != 1f

	override fun update(led: LedColor) {
		led.r *= brightness
		led.g *= brightness
		led.b *= brightness
	}
}
