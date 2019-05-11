package me.khol.ambilight

import java.lang.Math.*
import kotlin.math.min

class AmbilightPreview(private val config: LedConfig) : Ambilight() {

	companion object {
		private const val dim = 1.5f
	}

	private val colors = Array(config.ledCount) { LedColor() }
	private val lights = List(5) { Light(config.ledsWidth, config.ledsHeight) }

	override fun getScreenSegmentsColors(): Array<LedColor> {
		for (i in 0 until config.ledCount) {
			val (ledX, ledY) = config[i]

			var r = 0f
			var g = 0f
			var b = 0f
			for (light in lights) {
				val distX = light.x - ledX
				val distY = light.y - ledY
				val distance = sqrt(distX * distX + distY * distY)
				val distanceMulti = max(0.0, (light.size - distance) / light.size)
				r += (light.r * distanceMulti).toInt()
				g += (light.g * distanceMulti).toInt()
				b += (light.b * distanceMulti).toInt()
			}

			colors[i].r = (r / dim).coerceAtMost(255.0f)
			colors[i].g = (g / dim).coerceAtMost(255.0f)
			colors[i].b = (b / dim).coerceAtMost(255.0f)
		}

		return colors
	}

	private inner class Light(ledWidth: Int, ledHeight: Int) {

		internal var x = 0.0
		internal var y = 0.0
		internal var velX = 0.0
		internal var velY = 0.0
		internal var size = 0.0
		internal var r = 0
		internal var g = 0
		internal var b = 0

		init {
			r = (256 * random()).toInt()
			g = (256 * random()).toInt()
			b = (256 * random()).toInt()
			x = random() * ledWidth
			y = random() * ledHeight
			velX = random()
			velY = random()
			size = (0.5 + random()) * min(ledWidth, ledHeight)

			println("Color" +
				" | x:" + x.toInt() +
				" | y:" + y.toInt() +
				" | size:" + size.toInt())
		}
	}
}
