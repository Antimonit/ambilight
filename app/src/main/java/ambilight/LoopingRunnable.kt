package ambilight

import ambilight.gui.SegmentColorsUpdateListener
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
@ExperimentalUnsignedTypes
class LoopingRunnable(
	private val ambilight: Ambilight,
	private val config: LedConfig,
	private val listener: SegmentColorsUpdateListener,
	override var renderRate: Long,
	override var updateRate: Long,
	override var smoothness: Int,
	override var saturation: Double,
	override var brightness: Float,
	override var cutOff: Int,
	override var temperature: Int
) : Runnable, GUIListener {


	companion object {

		private const val MAX_FADE: UShort = 256u
		private const val Pr: Float = 0.299f
		private const val Pg: Float = 0.587f
		private const val Pb: Float = 0.114f
	}

	private fun Array<UByteArray>.toSigned() = this.map { it.toByteArray() }.toTypedArray()
	private fun Array<ByteArray>.toUnsigned() = this.map { it.toUByteArray() }.toTypedArray()

	private var currentTime: Long = System.currentTimeMillis()
	private val renderTime: Long get() = 1000 / renderRate
	private val updateTime: Long get() = 1000 / updateRate
	private var lastUpdateTime: Long = currentTime
	private var lastRenderTime: Long = currentTime

	override var isLivePreview: Boolean = true

	private val segmentColors: Array<UByteArray> = Array(config.ledCount) { UByteArray(3) }
	private var targetSegmentColors: Array<UByteArray> = segmentColors

	init {
		println("Created new looping Runnable")
	}

	override fun run() {

		while (true) {
			if (currentTime - lastRenderTime >= renderTime) {
				if (isLivePreview) {
					render()
				}
				lastRenderTime = System.currentTimeMillis()
			}

			if (currentTime - lastUpdateTime >= updateTime) {
				update()
				lastUpdateTime = System.currentTimeMillis()
			}

			currentTime = System.currentTimeMillis()
			while (currentTime - lastRenderTime < renderTime && currentTime - lastUpdateTime < updateTime) {
				try {
					Thread.sleep(1)
				} catch (ignored: InterruptedException) {
				}

				currentTime = System.currentTimeMillis()
			}
		}
	}

	private fun render() {
		targetSegmentColors = ambilight.getScreenSegmentsColors().toUnsigned()
		if (saturation != 1.0)
			updateSaturation()
		if (cutOff != 0)
			updateCutOff()
		if (brightness != 1f)
			updateBrightness()

		updateTemperature()
	}

	private fun update() {
		if (smoothness == 0) {
			listener.updatedSegmentColors(targetSegmentColors.toSigned())
		} else {
			smoothSegmentColors()
			listener.updatedSegmentColors(segmentColors.toSigned())
		}
	}

	private fun updateSaturation() {
		for (i in 0 until config.ledCount) {
			val (R, G, B) = targetSegmentColors[i].map { it.toFloat() }
			val P = sqrt(R * R * Pr + G * G * Pg + B * B * Pb)

			targetSegmentColors[i][0] = (P + (R - P) * saturation).roundToInt().coerceIn(0, 255).toUByte()
			targetSegmentColors[i][1] = (P + (G - P) * saturation).roundToInt().coerceIn(0, 255).toUByte()
			targetSegmentColors[i][2] = (P + (B - P) * saturation).roundToInt().coerceIn(0, 255).toUByte()
		}
	}

	private fun updateBrightness() {
		for (i in 0 until config.ledCount) {
			val (R, G, B) = targetSegmentColors[i].map { it.toFloat() }

			targetSegmentColors[i][0] = (R * brightness).roundToInt().toUByte()
			targetSegmentColors[i][1] = (G * brightness).roundToInt().toUByte()
			targetSegmentColors[i][2] = (B * brightness).roundToInt().toUByte()
		}
	}

	private fun updateTemperature() {
		val temperature = temperature / 100f

		for (i in 0 until config.ledCount) {
//			http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/

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

			val (R, G, B) = targetSegmentColors[i].map { it.toFloat() }

			targetSegmentColors[i][0] = (R * red / 256).roundToInt().toUByte()
			targetSegmentColors[i][1] = (G * green / 256).roundToInt().toUByte()
			targetSegmentColors[i][2] = (B * blue / 256).roundToInt().toUByte()
		}
	}

	private fun updateCutOff() {
		if (cutOff == 255) {
			for (i in 0 until config.ledCount) {
				targetSegmentColors[i][0] = 0u
				targetSegmentColors[i][1] = 0u
				targetSegmentColors[i][2] = 0u
			}
		} else {
			for (i in 0 until config.ledCount) {
				val (R, G, B) = targetSegmentColors[i].map { it.toFloat() }
				val hsvValue = 0.2126f * R + 0.7152f * G + 0.0722f * B
				val multi = (255 * (hsvValue - cutOff) / (255 - cutOff)).coerceAtLeast(0f)

				targetSegmentColors[i][0] = (R * multi / hsvValue).roundToInt().toUByte()
				targetSegmentColors[i][1] = (G * multi / hsvValue).roundToInt().toUByte()
				targetSegmentColors[i][2] = (B * multi / hsvValue).roundToInt().toUByte()
			}
		}
	}

	private fun smoothSegmentColors() {
		val fade = smoothness.toUShort()
		val fadeInv = MAX_FADE - fade

		for (ledNum in 0 until config.ledCount) {
			val (sR, sG, sB) = segmentColors[ledNum]
			val (tR, tG, tB) = targetSegmentColors[ledNum]

			segmentColors[ledNum][0] = ((sR * fade + tR * fadeInv) / MAX_FADE).toUByte()
			segmentColors[ledNum][1] = ((sG * fade + tG * fadeInv) / MAX_FADE).toUByte()
			segmentColors[ledNum][2] = ((sB * fade + tB * fadeInv) / MAX_FADE).toUByte()
		}
	}
}
